/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.LazyClassGen;
import org.aspectj.weaver.patterns.DeclareParents;

/**
 * Overrides the default eclipse LookupEnvironment for two purposes.
 * 
 * 1. To provide some additional phases to <code>completeTypeBindings</code>
 *    that weave declare parents and inter-type declarations at the correct time.
 * 
 * 2. To intercept the loading of new binary types to ensure the they will have
 *    declare parents and inter-type declarations woven when appropriate.
 * 
 * @author Jim Hugunin
 */
public class AjLookupEnvironment extends LookupEnvironment {
	public EclipseFactory factory = null;
	
//	private boolean builtInterTypesAndPerClauses = false;
	private List pendingTypesToWeave = new ArrayList();
	private Map dangerousInterfaces = new HashMap();
	
	public AjLookupEnvironment(
		ITypeRequestor typeRequestor,
		CompilerOptions options,
		ProblemReporter problemReporter,
		INameEnvironment nameEnvironment) {
		super(typeRequestor, options, problemReporter, nameEnvironment);
	}
	
	//??? duplicates some of super's code
	public void completeTypeBindings() {
//		builtInterTypesAndPerClauses = false;
		//pendingTypesToWeave = new ArrayList();
		stepCompleted = BUILD_TYPE_HIERARCHY;
		
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			units[i].scope.checkAndSetImports();
		}
		stepCompleted = CHECK_AND_SET_IMPORTS;
	
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			units[i].scope.connectTypeHierarchy();
		}
		stepCompleted = CONNECT_TYPE_HIERARCHY;
	
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			units[i].scope.buildFieldsAndMethods();
		}
		
		// would like to gather up all TypeDeclarations at this point and put them in the factory
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			SourceTypeBinding[] b = units[i].scope.topLevelTypes;
			for (int j = 0; j < b.length; j++) {
				factory.addSourceTypeBinding(b[j]);
			}
		}
		
		// need to build inter-type declarations for all AspectDeclarations at this point
        for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
            SourceTypeBinding[] b = units[i].scope.topLevelTypes;
            for (int j = 0; j < b.length; j++) {
                buildInterTypeAndPerClause(b[j].scope);
                addCrosscuttingStructures(b[j].scope);
            }
        }        

		factory.finishTypeMungers();
	
		// now do weaving
		Collection typeMungers = factory.getTypeMungers();
		
		Collection declareParents = factory.getDeclareParents();

		doPendingWeaves();
		
		// We now have some list of types to process, and we are about to apply the type mungers.
		// There can be situations where the order of types passed to the compiler causes the
		// output from the compiler to vary - THIS IS BAD.  For example, if we have class A
		// and class B extends A.  Also, an aspect that 'declare parents: A+ implements Serializable'
		// then depending on whether we see A first, we may or may not make B serializable.
		
		// The fix is to process them in the right order, ensuring that for a type we process its 
		// supertypes and superinterfaces first.  This algorithm may have problems with:
		// - partial hierarchies (e.g. suppose types A,B,C are in a hierarchy and A and C are to be woven but not B)
		// - weaving that brings new types in for processing (see pendingTypesToWeave.add() calls) after we thought
		//   we had the full list.
		// 
		// but these aren't common cases (he bravely said...)
		boolean typeProcessingOrderIsImportant = declareParents.size()>0;
		
		if (typeProcessingOrderIsImportant) {
			List typesToProcess = new ArrayList();
			for (int i=lastCompletedUnitIndex+1; i<=lastUnitIndex; i++) {
				CompilationUnitScope cus = units[i].scope;
				SourceTypeBinding[] stbs = cus.topLevelTypes;
				for (int j=0; j<stbs.length; j++) {
					SourceTypeBinding stb = stbs[j];
					typesToProcess.add(stb);
				}
			}

			while (typesToProcess.size()>0) {
				// A side effect of weaveIntertypes() is that the processed type is removed from the collection
				weaveIntertypes(typesToProcess,(SourceTypeBinding)typesToProcess.get(0),typeMungers,declareParents);
			}
		
		} else {
			// Order isn't important
			for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
				//System.err.println("Working on "+new String(units[i].getFileName()));
				weaveInterTypeDeclarations(units[i].scope, typeMungers, declareParents);
			}
		}
		
        for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
            SourceTypeBinding[] b = units[i].scope.topLevelTypes;
            for (int j = 0; j < b.length; j++) {
                resolvePointcutDeclarations(b[j].scope);
            }
        }
        
        for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
            SourceTypeBinding[] b = units[i].scope.topLevelTypes;
            for (int j = 0; j < b.length; j++) {
            	addAdviceLikeDeclares(b[j].scope);
            }
        }
        
        for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
            units[i] = null; // release unnecessary reference to the parsed unit
        }
                
		stepCompleted = BUILD_FIELDS_AND_METHODS;
		lastCompletedUnitIndex = lastUnitIndex;
	}
	
	/**
	 * Weave the parents and intertype decls into a given type.  This method looks at the
	 * supertype and superinterfaces for the specified type and recurses to weave those first
	 * if they are in the full list of types we are going to process during this compile... it stops recursing
	 * the first time it hits a type we aren't going to process during this compile.  This could cause problems 
	 * if you supply 'pieces' of a hierarchy, i.e. the bottom and the top, but not the middle - but what the hell
	 * are you doing if you do that?
	 */
	private void weaveIntertypes(List typesToProcess,SourceTypeBinding typeToWeave,Collection typeMungers,Collection declareParents) {
		// Look at the supertype first
	    ReferenceBinding superType = typeToWeave.superclass();
	    //System.err.println("Been asked to weave "+new String(typeToWeave.getFileName()));
	    if (typesToProcess.contains(superType) && superType instanceof SourceTypeBinding) {
	    	//System.err.println("Recursing to supertype "+new String(superType.getFileName()));
	    	weaveIntertypes(typesToProcess,(SourceTypeBinding)superType,typeMungers,declareParents);
	    }
	    // Then look at the superinterface list
		ReferenceBinding[] interfaceTypes = typeToWeave.superInterfaces();
	    for (int i = 0; i < interfaceTypes.length; i++) {
	    	ReferenceBinding binding = interfaceTypes[i];
	    	if (typesToProcess.contains(binding) && binding instanceof SourceTypeBinding) {
		    	//System.err.println("Recursing to superinterface "+new String(binding.getFileName()));
	    		weaveIntertypes(typesToProcess,(SourceTypeBinding)binding,typeMungers,declareParents);
	    	}
		}
	    weaveInterTypeDeclarations(typeToWeave,typeMungers,declareParents,false);
	    typesToProcess.remove(typeToWeave);
	}

	private void doPendingWeaves() {
		for (Iterator i = pendingTypesToWeave.iterator(); i.hasNext(); ) {
			SourceTypeBinding t = (SourceTypeBinding)i.next();
			weaveInterTypeDeclarations(t);
		}
		pendingTypesToWeave.clear();
	}
    
    private void addAdviceLikeDeclares(ClassScope s) {
        TypeDeclaration dec = s.referenceContext;
        
        if (dec instanceof AspectDeclaration) {
            ResolvedTypeX typeX = factory.fromEclipse(dec.binding);
            factory.getWorld().getCrosscuttingMembersSet().addAdviceLikeDeclares(typeX);
        }
        
        SourceTypeBinding sourceType = s.referenceContext.binding;
        ReferenceBinding[] memberTypes = sourceType.memberTypes;
        for (int i = 0, length = memberTypes.length; i < length; i++) {
            addAdviceLikeDeclares(((SourceTypeBinding) memberTypes[i]).scope);
        }
    }

    private void addCrosscuttingStructures(ClassScope s) {
        TypeDeclaration dec = s.referenceContext;
        
        if (dec instanceof AspectDeclaration) {
            ResolvedTypeX typeX = factory.fromEclipse(dec.binding);
            factory.getWorld().getCrosscuttingMembersSet().addOrReplaceAspect(typeX);
        
            if (typeX.getSuperclass().isAspect() && !typeX.getSuperclass().isExposedToWeaver()) {
                factory.getWorld().getCrosscuttingMembersSet().addOrReplaceAspect(typeX.getSuperclass());
            }
        }
        
        SourceTypeBinding sourceType = s.referenceContext.binding;
        ReferenceBinding[] memberTypes = sourceType.memberTypes;
        for (int i = 0, length = memberTypes.length; i < length; i++) {
            addCrosscuttingStructures(((SourceTypeBinding) memberTypes[i]).scope);
        }
    }
    
    private void resolvePointcutDeclarations(ClassScope s) {
        TypeDeclaration dec = s.referenceContext;
        SourceTypeBinding sourceType = s.referenceContext.binding;
        boolean hasPointcuts = false;
        AbstractMethodDeclaration[] methods = dec.methods;
        boolean initializedMethods = false;
        if (methods != null) {
            for (int i=0; i < methods.length; i++) {
                if (methods[i] instanceof PointcutDeclaration) {
                	hasPointcuts = true;
                    if (!initializedMethods) {
                        sourceType.methods(); //force initialization
                        initializedMethods = true;
                    }
                    ((PointcutDeclaration)methods[i]).resolvePointcut(s);
                }
            }
        }

		if (hasPointcuts || dec instanceof AspectDeclaration) {
        	ResolvedTypeX.Name name = (ResolvedTypeX.Name)factory.fromEclipse(sourceType);
        	EclipseSourceType eclipseSourceType = (EclipseSourceType)name.getDelegate();
        	eclipseSourceType.checkPointcutDeclarations();
		}
		
        ReferenceBinding[] memberTypes = sourceType.memberTypes;
        for (int i = 0, length = memberTypes.length; i < length; i++) {
            resolvePointcutDeclarations(((SourceTypeBinding) memberTypes[i]).scope);
        }
    }
    
    

	
	private void buildInterTypeAndPerClause(ClassScope s) {
		TypeDeclaration dec = s.referenceContext;
		if (dec instanceof AspectDeclaration) {
			((AspectDeclaration)dec).buildInterTypeAndPerClause(s);
		}
		
		SourceTypeBinding sourceType = s.referenceContext.binding;
		// test classes don't extend aspects
		if (sourceType.superclass != null) {
			ResolvedTypeX parent = factory.fromEclipse(sourceType.superclass);
			if (parent.isAspect() && !(dec instanceof AspectDeclaration)) {
				factory.showMessage(IMessage.ERROR, "class \'" + new String(sourceType.sourceName) + 
						"\' can not extend aspect \'" + parent.getName() + "\'",
						factory.fromEclipse(sourceType).getSourceLocation(), null);
			}
		}

		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			buildInterTypeAndPerClause(((SourceTypeBinding) memberTypes[i]).scope);
		}
	}
		
	private void weaveInterTypeDeclarations(CompilationUnitScope unit, Collection typeMungers, Collection declareParents) {
		for (int i = 0, length = unit.topLevelTypes.length; i < length; i++) {
		    weaveInterTypeDeclarations(unit.topLevelTypes[i], typeMungers, declareParents, false);
		}
	}
	
	private void weaveInterTypeDeclarations(SourceTypeBinding sourceType) {
		if (!factory.areTypeMungersFinished()) {
			if (!pendingTypesToWeave.contains(sourceType)) pendingTypesToWeave.add(sourceType);
		} else {
			weaveInterTypeDeclarations(sourceType, factory.getTypeMungers(), factory.getDeclareParents(), true);
		}
	}
	
	private void weaveInterTypeDeclarations(SourceTypeBinding sourceType, Collection typeMungers, Collection declareParents, boolean skipInners) {
		ResolvedTypeX onType = factory.fromEclipse(sourceType);
		WeaverStateInfo info = onType.getWeaverState();

		if (info != null && !info.isOldStyle()) {		
			Collection mungers = 
				onType.getWeaverState().getTypeMungers(onType);
				
			//System.out.println(onType + " mungers: " + mungers);
			for (Iterator i = mungers.iterator(); i.hasNext(); ) {
				ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
				EclipseTypeMunger munger = factory.makeEclipseTypeMunger(m);
				if (munger.munge(sourceType)) {
					if (onType.isInterface() &&
						munger.getMunger().needsAccessToTopmostImplementor())
					{
						if (!onType.getWorld().getCrosscuttingMembersSet().containsAspect(munger.getAspectType())) {
							dangerousInterfaces.put(onType, 
								"implementors of " + onType + " must be woven by " +
								munger.getAspectType());
						}
					}
				}
				
			}
			
			return;
		}
		
		//System.out.println("dangerousInterfaces: " + dangerousInterfaces);
		
		for (Iterator i = dangerousInterfaces.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			ResolvedTypeX interfaceType = (ResolvedTypeX)entry.getKey();
			if (onType.isTopmostImplementor(interfaceType)) {
				factory.showMessage(IMessage.ERROR, 
					onType + ": " + entry.getValue(),
					onType.getSourceLocation(), null);
			}
		}
		
		boolean needOldStyleWarning = (info != null && info.isOldStyle());
		
		onType.clearInterTypeMungers();
		
		for (Iterator i = declareParents.iterator(); i.hasNext();) {
			doDeclareParents((DeclareParents)i.next(), sourceType);
		}
		
		for (Iterator i = typeMungers.iterator(); i.hasNext();) {
			EclipseTypeMunger munger = (EclipseTypeMunger) i.next();
			if (munger.matches(onType)) {
				if (needOldStyleWarning) {
					factory.showMessage(IMessage.WARNING, 
						"The class for " + onType + " should be recompiled with ajc-1.1.1 for best results",
						onType.getSourceLocation(), null);
					needOldStyleWarning = false;
				}
				onType.addInterTypeMunger(munger);
				//TODO: Andy Should be done at weave time.
				// Unfortunately we can't do it at weave time unless the type mungers remember where
				// they came from.  Thats why we do it here during complation because at this time
				// they do know their source location.  I've put a flag in ResolvedTypeMunger that
				// records whether type mungers are currently set to remember their source location.
				// The flag is currently set to false, it should be set to true when we do the
				// work to version all AspectJ attributes.
				// (When done at weave time, it is done by invoking addRelationship() on 
				// AsmRelationshipProvider (see BCELTypeMunger)
				if (!ResolvedTypeMunger.persistSourceLocation) // Do it up front if we bloody have to
				 AsmInterTypeRelationshipProvider.getDefault().addRelationship(onType, munger);
			}
		}
		
        //???onType.checkInterTypeMungers();
        onType.checkInterTypeMungers();
		for (Iterator i = onType.getInterTypeMungers().iterator(); i.hasNext();) {
			EclipseTypeMunger munger = (EclipseTypeMunger) i.next();
			//System.out.println("applying: " + munger + " to " + new String(sourceType.sourceName));
			munger.munge(sourceType);
		}
		
		if (skipInners) return;

		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			if (memberTypes[i] instanceof SourceTypeBinding) {
				weaveInterTypeDeclarations((SourceTypeBinding) memberTypes[i], typeMungers, declareParents, false);
			}
		}
	}
	
	private void doDeclareParents(DeclareParents declareParents, SourceTypeBinding sourceType) {
		List newParents = declareParents.findMatchingNewParents(factory.fromEclipse(sourceType));
		if (!newParents.isEmpty()) {
			for (Iterator i = newParents.iterator(); i.hasNext(); ) {
				ResolvedTypeX parent = (ResolvedTypeX)i.next();
				if (dangerousInterfaces.containsKey(parent)) {
					ResolvedTypeX onType = factory.fromEclipse(sourceType);
					factory.showMessage(IMessage.ERROR, 
										onType + ": " + dangerousInterfaces.get(parent),
										onType.getSourceLocation(), null);
				}
				AsmRelationshipProvider.getDefault().addDeclareParentsRelationship(declareParents.getSourceLocation(),factory.fromEclipse(sourceType), newParents);
				addParent(sourceType, parent);
			}
		}
	}

	private void reportDeclareParentsMessage(WeaveMessage.WeaveMessageKind wmk,SourceTypeBinding sourceType,ResolvedTypeX parent) {
		if (!factory.getWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
			String filename = new String(sourceType.getFileName());
			
			int takefrom = filename.lastIndexOf('/');
			if (takefrom == -1 ) takefrom = filename.lastIndexOf('\\');
			filename = filename.substring(takefrom+1);

			factory.getWorld().getMessageHandler().handleMessage(
			WeaveMessage.constructWeavingMessage(wmk,
				new String[]{CharOperation.toString(sourceType.compoundName),
						filename,
						parent.getClassName(),
						getShortname(parent.getSourceLocation().getSourceFile().getPath())}));
		}
	}
	
	private String getShortname(String path)  {
		int takefrom = path.lastIndexOf('/');
		if (takefrom == -1) {
			takefrom = path.lastIndexOf('\\');
		}
		return path.substring(takefrom+1);
	}

	private void addParent(SourceTypeBinding sourceType, ResolvedTypeX parent) {
		ReferenceBinding parentBinding = (ReferenceBinding)factory.makeTypeBinding(parent); 
		
		if (parentBinding.isClass()) {
			sourceType.superclass = parentBinding;
			
			// TAG: WeavingMessage    DECLARE PARENTS: EXTENDS
			// Compiler restriction: Can't do EXTENDS at weave time
			// So, only see this message if doing a source compilation
			reportDeclareParentsMessage(WeaveMessage.WEAVEMESSAGE_DECLAREPARENTSEXTENDS,sourceType,parent);
			
		} else {
			ReferenceBinding[] oldI = sourceType.superInterfaces;
			ReferenceBinding[] newI;
			if (oldI == null) {
				newI = new ReferenceBinding[1];
				newI[0] = parentBinding;
			} else {
				int n = oldI.length;
				newI = new ReferenceBinding[n+1];
				System.arraycopy(oldI, 0, newI, 0, n);
				newI[n] = parentBinding;
			}
			sourceType.superInterfaces = newI;
			warnOnAddedInterface(factory.fromEclipse(sourceType),parent);
			

			// TAG: WeavingMessage    DECLARE PARENTS: IMPLEMENTS
			// This message will come out of BcelTypeMunger.munge if doing a binary weave
			reportDeclareParentsMessage(WeaveMessage.WEAVEMESSAGE_DECLAREPARENTSIMPLEMENTS,sourceType,parent);
			
		}
		
	}

	public void warnOnAddedInterface (ResolvedTypeX type, ResolvedTypeX parent) {
		World world = factory.getWorld();
		ResolvedTypeX serializable = world.getCoreType(TypeX.SERIALIZABLE);
		if (serializable.isAssignableFrom(type)
			&& !serializable.isAssignableFrom(parent)
			&& !LazyClassGen.hasSerialVersionUIDField(type)) {
			world.getLint().needsSerialVersionUIDField.signal(
				new String[] {
					type.getName().toString(),
					"added interface " + parent.getName().toString()
				},
				null,
				null);               
		}
	}
	
	
	
	private List pendingTypesToFinish = new ArrayList();
	boolean inBinaryTypeCreationAndWeaving = false;
	boolean processingTheQueue = false;
	
	public BinaryTypeBinding createBinaryTypeFrom(
		IBinaryType binaryType,
		PackageBinding packageBinding,
		boolean needFieldsAndMethods)
	{

		if (inBinaryTypeCreationAndWeaving) {
			BinaryTypeBinding ret = super.createBinaryTypeFrom(
				binaryType,
				packageBinding,
				needFieldsAndMethods);
			pendingTypesToFinish.add(ret);
			return ret;
		}
		
		inBinaryTypeCreationAndWeaving = true;
		try {
			BinaryTypeBinding ret = super.createBinaryTypeFrom(
				binaryType,
				packageBinding,
				needFieldsAndMethods);
			weaveInterTypeDeclarations(ret);			
			return ret;
		} finally {
			inBinaryTypeCreationAndWeaving = false;
			
			// Start processing the list...
			if (pendingTypesToFinish.size()>0) {
				processingTheQueue = true;
				while (!pendingTypesToFinish.isEmpty()) {
					BinaryTypeBinding nextVictim = (BinaryTypeBinding)pendingTypesToFinish.remove(0);
					// During this call we may recurse into this method and add 
					// more entries to the pendingTypesToFinish list.
					weaveInterTypeDeclarations(nextVictim);
				}
				processingTheQueue = false;
			}
		}		
	}
}
