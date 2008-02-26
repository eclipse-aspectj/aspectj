/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.FakeAnnotation;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.bcel.LazyClassGen;
import org.aspectj.weaver.patterns.DeclareAnnotation;
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
public class AjLookupEnvironment extends LookupEnvironment implements AnonymousClassCreationListener {
	public  EclipseFactory factory = null;
	
//	private boolean builtInterTypesAndPerClauses = false;
	private List pendingTypesToWeave = new ArrayList();
	
	// Q: What are dangerousInterfaces?
	// A: An interface is considered dangerous if an ITD has been made upon it and that ITD
	//    requires the top most implementors of the interface to be woven *and yet* the aspect
	//    responsible for the ITD is not in the 'world'.
	// Q: Err, how can that happen?
	// A: When a type is on the inpath, it is 'processed' when completing type bindings.  At this
	//    point we look at any type mungers it was affected by previously (stored in the weaver
	//    state info attribute).  Effectively we are working with a type munger and yet may not have its
	//    originating aspect in the world.  This is a problem if, for example, the aspect supplied
	//    a 'body' for a method targetting an interface - since the top most implementors should
	//    be woven by the munger from the aspect.  When this happens we store the interface name here
	//    in the map - if we later process a type that is the topMostImplementor of a dangerous
	//    interface then we put out an error message.
	
	/** interfaces targetted by ITDs that have to be implemented by accessing the topMostImplementor
	 *  of the interface, yet the aspect where the ITD originated is not in the world */
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
		AsmManager.setCompletingTypeBindings(true);
		ContextToken completeTypeBindingsToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.COMPLETING_TYPE_BINDINGS, "");
//		builtInterTypesAndPerClauses = false;
		//pendingTypesToWeave = new ArrayList();
		stepCompleted = BUILD_TYPE_HIERARCHY;
		
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.CHECK_AND_SET_IMPORTS, units[i].compilationResult.fileName);
			units[i].scope.checkAndSetImports();
			CompilationAndWeavingContext.leavingPhase(tok);
		}
		stepCompleted = CHECK_AND_SET_IMPORTS;
	
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.CONNECTING_TYPE_HIERARCHY, units[i].compilationResult.fileName);
			units[i].scope.connectTypeHierarchy();
			CompilationAndWeavingContext.leavingPhase(tok);
		}
		stepCompleted = CONNECT_TYPE_HIERARCHY;
	
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.BUILDING_FIELDS_AND_METHODS, units[i].compilationResult.fileName);
			units[i].scope.checkParameterizedTypes();
			units[i].scope.buildFieldsAndMethods();
			CompilationAndWeavingContext.leavingPhase(tok);
		}
		
		// would like to gather up all TypeDeclarations at this point and put them in the factory
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			SourceTypeBinding[] b = units[i].scope.topLevelTypes;
			for (int j = 0; j < b.length; j++) {
				factory.addSourceTypeBinding(b[j],units[i]);
			}
		}
		
		// We won't find out about anonymous types until later though, so register to be
		// told about them when they turn up.
		AnonymousClassPublisher.aspectOf().setAnonymousClassCreationListener(this);
		
		// need to build inter-type declarations for all AspectDeclarations at this point
		// this MUST be done in order from super-types to subtypes
		List typesToProcess = new ArrayList();
		for (int i=lastCompletedUnitIndex+1; i<=lastUnitIndex; i++) {
			CompilationUnitScope cus = units[i].scope;
			SourceTypeBinding[] stbs = cus.topLevelTypes;
			for (int j=0; j<stbs.length; j++) {
				SourceTypeBinding stb = stbs[j];
				typesToProcess.add(stb);
			}
		}
		factory.getWorld().getCrosscuttingMembersSet().reset();
		while (typesToProcess.size()>0) {
			// removes types from the list as they are processed...
			collectAllITDsAndDeclares((SourceTypeBinding)typesToProcess.get(0),typesToProcess);
		}		
				
		factory.finishTypeMungers();
	
		// now do weaving
		Collection typeMungers = factory.getTypeMungers();
		
		Collection declareParents = factory.getDeclareParents();
		Collection declareAnnotationOnTypes = factory.getDeclareAnnotationOnTypes();

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
		boolean typeProcessingOrderIsImportant = declareParents.size()>0 || declareAnnotationOnTypes.size()>0; //DECAT
		
		if (typeProcessingOrderIsImportant) {
			typesToProcess = new ArrayList();
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
				weaveIntertypes(typesToProcess,(SourceTypeBinding)typesToProcess.get(0),typeMungers,declareParents,declareAnnotationOnTypes);
			}
		
		} else {
			// Order isn't important
			for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
				//System.err.println("Working on "+new String(units[i].getFileName()));
				weaveInterTypeDeclarations(units[i].scope, typeMungers, declareParents,declareAnnotationOnTypes);
			}
		}
		
		for (int i = lastCompletedUnitIndex +1; i<=lastUnitIndex; i++) {
			SourceTypeBinding[] b = units[i].scope.topLevelTypes;
            for (int j = 0; j < b.length; j++) {
            	verifyAnyTypeParametersMeetBounds(b[j]);
            }
		}
		
        for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
            SourceTypeBinding[] b = units[i].scope.topLevelTypes;
            for (int j = 0; j < b.length; j++) {
            	ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.RESOLVING_POINTCUT_DECLARATIONS, b[j].sourceName);
                resolvePointcutDeclarations(b[j].scope);
                CompilationAndWeavingContext.leavingPhase(tok);
            }
        }
        
        for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
            SourceTypeBinding[] b = units[i].scope.topLevelTypes;
            for (int j = 0; j < b.length; j++) {
            	ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.ADDING_DECLARE_WARNINGS_AND_ERRORS, b[j].sourceName);
            	addAdviceLikeDeclares(b[j].scope);
                CompilationAndWeavingContext.leavingPhase(tok);
            }
        }
        
        for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
            units[i] = null; // release unnecessary reference to the parsed unit
        }
                
		stepCompleted = BUILD_FIELDS_AND_METHODS;
		lastCompletedUnitIndex = lastUnitIndex;
		AsmManager.setCompletingTypeBindings(false);
		factory.getWorld().getCrosscuttingMembersSet().verify();
		CompilationAndWeavingContext.leavingPhase(completeTypeBindingsToken);	
	}
	
	
	/**
	 * For any given sourcetypebinding, this method checks that if it is a parameterized aspect that
	 * the type parameters specified for any supertypes meet the bounds for the generic type
	 * variables.
	 */
	private void verifyAnyTypeParametersMeetBounds(SourceTypeBinding sourceType) {
		ResolvedType onType = factory.fromEclipse(sourceType);
		if (onType.isAspect()) {
			ResolvedType superType = factory.fromEclipse(sourceType.superclass);
			// Don't need to check if it was used in its RAW form or isnt generic
			if (superType.isGenericType() || superType.isParameterizedType()) {
				TypeVariable[] typeVariables         = superType.getTypeVariables();
				UnresolvedType[] typeParams          = superType.getTypeParameters();
				if (typeVariables!=null && typeParams!=null) {
					for (int i = 0; i < typeVariables.length; i++) {
						boolean ok = typeVariables[i].canBeBoundTo(typeParams[i].resolve(factory.getWorld()));
						if (!ok) { // the supplied parameter violates the bounds
							// Type {0} does not meet the specification for type parameter {1} ({2}) in generic type {3}
							String msg = 
								WeaverMessages.format(
									WeaverMessages.VIOLATES_TYPE_VARIABLE_BOUNDS,
									typeParams[i],
									new Integer(i+1),
									typeVariables[i].getDisplayName(),
									superType.getGenericType().getName());
							factory.getWorld().getMessageHandler().handleMessage(MessageUtil.error(msg,onType.getSourceLocation()));
						}
					}
			}
			}
		}
		
		
		
	}

	public void doSupertypesFirst(ReferenceBinding rb,Collection yetToProcess) {
	    if (rb instanceof SourceTypeBinding) {
		    if (yetToProcess.contains(rb)) {
		    		collectAllITDsAndDeclares((SourceTypeBinding)rb, yetToProcess);
		    }
	    } else if (rb instanceof ParameterizedTypeBinding) {
	        // If its a PTB we need to pull the SourceTypeBinding out of it.
	    		ParameterizedTypeBinding ptb = (ParameterizedTypeBinding)rb;
		    	if (ptb.type instanceof SourceTypeBinding && yetToProcess.contains(ptb.type)) {
		    		collectAllITDsAndDeclares((SourceTypeBinding)ptb.type, yetToProcess);
		    	}
	    }
	}
	/**
	 * Find all the ITDs and Declares, but it is important we do this from the supertypes
	 * down to the subtypes.
	 * @param sourceType
	 * @param yetToProcess
	 */
	private void collectAllITDsAndDeclares(SourceTypeBinding sourceType, Collection yetToProcess) {
		// Look at the supertype first
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.COLLECTING_ITDS_AND_DECLARES, sourceType.sourceName);

         yetToProcess.remove(sourceType);
		// look out our direct supertype
		doSupertypesFirst(sourceType.superclass(),yetToProcess);
	    
	    // now check our membertypes (pr119570)
		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			SourceTypeBinding rb = (SourceTypeBinding)memberTypes[i];
			if (!rb.superclass().equals(sourceType))
			  doSupertypesFirst(rb.superclass(),yetToProcess);
		}
		
        buildInterTypeAndPerClause(sourceType.scope);
        addCrosscuttingStructures(sourceType.scope);
		CompilationAndWeavingContext.leavingPhase(tok);
	}
	
	/**
	 * Weave the parents and intertype decls into a given type.  This method looks at the
	 * supertype and superinterfaces for the specified type and recurses to weave those first
	 * if they are in the full list of types we are going to process during this compile... it stops recursing
	 * the first time it hits a type we aren't going to process during this compile.  This could cause problems 
	 * if you supply 'pieces' of a hierarchy, i.e. the bottom and the top, but not the middle - but what the hell
	 * are you doing if you do that?
	 */
	private void weaveIntertypes(List typesToProcess,SourceTypeBinding typeToWeave,Collection typeMungers,
			                     Collection declareParents,Collection declareAnnotationOnTypes) {
		// Look at the supertype first
	    ReferenceBinding superType = typeToWeave.superclass();
	    if (typesToProcess.contains(superType) && superType instanceof SourceTypeBinding) {
	    	//System.err.println("Recursing to supertype "+new String(superType.getFileName()));
	    	weaveIntertypes(typesToProcess,(SourceTypeBinding)superType,typeMungers,declareParents,declareAnnotationOnTypes);
	    }
	    // Then look at the superinterface list
		ReferenceBinding[] interfaceTypes = typeToWeave.superInterfaces();
	    for (int i = 0; i < interfaceTypes.length; i++) {
	    	ReferenceBinding binding = interfaceTypes[i];
	    	if (typesToProcess.contains(binding) && binding instanceof SourceTypeBinding) {
		    	//System.err.println("Recursing to superinterface "+new String(binding.getFileName()));
	    		weaveIntertypes(typesToProcess,(SourceTypeBinding)binding,typeMungers,declareParents,declareAnnotationOnTypes);
	    	}
		}
	    weaveInterTypeDeclarations(typeToWeave,typeMungers,declareParents,declareAnnotationOnTypes,false);
	    typesToProcess.remove(typeToWeave);
	}

	private void doPendingWeaves() {
		for (Iterator i = pendingTypesToWeave.iterator(); i.hasNext(); ) {
			SourceTypeBinding t = (SourceTypeBinding)i.next();
			ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.WEAVING_INTERTYPE_DECLARATIONS, t.sourceName);
			weaveInterTypeDeclarations(t);
			CompilationAndWeavingContext.leavingPhase(tok);
		}
		pendingTypesToWeave.clear();
	}
    
    private void addAdviceLikeDeclares(ClassScope s) {
        TypeDeclaration dec = s.referenceContext;
        
        if (dec instanceof AspectDeclaration) {
            ResolvedType typeX = factory.fromEclipse(dec.binding);
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
            ResolvedType typeX = factory.fromEclipse(dec.binding);
            factory.getWorld().getCrosscuttingMembersSet().addOrReplaceAspect(typeX,false);
        
            if (typeX.getSuperclass().isAspect() && !typeX.getSuperclass().isExposedToWeaver()) {
                factory.getWorld().getCrosscuttingMembersSet().addOrReplaceAspect(typeX.getSuperclass(),false);
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
        
		if (hasPointcuts || dec instanceof AspectDeclaration || couldBeAnnotationStyleAspectDeclaration(dec)) {
        	ReferenceType name = (ReferenceType)factory.fromEclipse(sourceType);
        	EclipseSourceType eclipseSourceType = (EclipseSourceType)name.getDelegate();
        	eclipseSourceType.checkPointcutDeclarations();
		}
		
        ReferenceBinding[] memberTypes = sourceType.memberTypes;
        for (int i = 0, length = memberTypes.length; i < length; i++) {
            resolvePointcutDeclarations(((SourceTypeBinding) memberTypes[i]).scope);
        }
    }
    
    /**
     * Return true if the declaration has @Aspect annotation.  Called 'couldBe' rather than
     * 'is' because someone else may have defined an annotation called Aspect - we can't
     * verify the full name (including package name) because it may not have been resolved
     * just yet and rather going through expensive resolution when we dont have to, this
     * gives us a cheap check that tells us whether to bother.
     */
	private boolean couldBeAnnotationStyleAspectDeclaration(TypeDeclaration dec) { 
        Annotation[] annotations = dec.annotations;
        boolean couldBeAtAspect = false;
        if (annotations != null) {
			for (int i = 0; i < annotations.length  && !couldBeAtAspect; i++) {
				if (annotations[i].toString().equals("@Aspect")) couldBeAtAspect=true;
			}
		}
        return couldBeAtAspect;
	}

	private void buildInterTypeAndPerClause(ClassScope s) {
		TypeDeclaration dec = s.referenceContext;
		if (dec instanceof AspectDeclaration) {
			((AspectDeclaration)dec).buildInterTypeAndPerClause(s);
		}
		
		SourceTypeBinding sourceType = s.referenceContext.binding;
		// test classes don't extend aspects
		if (sourceType.superclass != null) {
			ResolvedType parent = factory.fromEclipse(sourceType.superclass);
			if (parent.isAspect() && !isAspect(dec)) {
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
	
	private boolean isAspect(TypeDeclaration decl) {
		if ((decl instanceof AspectDeclaration)) {
			return true;
		} else if (decl.annotations == null) {
			return false;
		} else {
			for (int i = 0; i < decl.annotations.length; i++) {
				Annotation ann = decl.annotations[i];
				if (ann.type instanceof SingleTypeReference) {
					if (CharOperation.equals("Aspect".toCharArray(),((SingleTypeReference)ann.type).token)) return true;
				} else if (ann.type instanceof QualifiedTypeReference) {
					QualifiedTypeReference qtr = (QualifiedTypeReference) ann.type;
					if (qtr.tokens.length != 5) return false;
					if (!CharOperation.equals("org".toCharArray(),qtr.tokens[0])) return false;
					if (!CharOperation.equals("aspectj".toCharArray(),qtr.tokens[1])) return false;
					if (!CharOperation.equals("lang".toCharArray(),qtr.tokens[2])) return false;
					if (!CharOperation.equals("annotation".toCharArray(),qtr.tokens[3])) return false;
					if (!CharOperation.equals("Aspect".toCharArray(),qtr.tokens[4])) return false;
					return true;
				}
			}
		}
		return false;		
	}
		
	private void weaveInterTypeDeclarations(CompilationUnitScope unit, Collection typeMungers, 
			                                Collection declareParents, Collection declareAnnotationOnTypes) {
		for (int i = 0, length = unit.topLevelTypes.length; i < length; i++) {
		    weaveInterTypeDeclarations(unit.topLevelTypes[i], typeMungers, declareParents, declareAnnotationOnTypes,false);
		}
	}
	
	private void weaveInterTypeDeclarations(SourceTypeBinding sourceType) {
		if (!factory.areTypeMungersFinished()) {
			if (!pendingTypesToWeave.contains(sourceType)) pendingTypesToWeave.add(sourceType);
		} else {
			weaveInterTypeDeclarations(sourceType, factory.getTypeMungers(), factory.getDeclareParents(), factory.getDeclareAnnotationOnTypes(),true);
		}
	}
	
	private void weaveInterTypeDeclarations(SourceTypeBinding sourceType, Collection typeMungers, 
			Collection declareParents, Collection declareAnnotationOnTypes, boolean skipInners) {

		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.WEAVING_INTERTYPE_DECLARATIONS, sourceType.sourceName);


		ResolvedType onType = factory.fromEclipse(sourceType);
		

		
		// AMC we shouldn't need this when generic sigs are fixed??
		if (onType.isRawType()) onType = onType.getGenericType();

		WeaverStateInfo info = onType.getWeaverState();
		
		// this test isnt quite right - there will be a case where we fail to flag a problem
		// with a 'dangerous interface' because the type is reweavable when we should have
		// because the type wasn't going to be rewoven... if that happens, we should perhaps
		// move this test and dangerous interface processing to the end of this method and
		// make it conditional on whether any of the typeMungers passed into here actually
		// matched this type.
		if (info != null && !info.isOldStyle() 	&& !info.isReweavable()) {
			processTypeMungersFromExistingWeaverState(sourceType,onType);
			CompilationAndWeavingContext.leavingPhase(tok);
			return;
		}

		// Check if the type we are looking at is the topMostImplementor of a dangerous interface - 
		// report a problem if it is.
		for (Iterator i = dangerousInterfaces.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			ResolvedType interfaceType = (ResolvedType)entry.getKey();
			if (onType.isTopmostImplementor(interfaceType)) {
				factory.showMessage(IMessage.ERROR, 
					onType + ": " + entry.getValue(),
					onType.getSourceLocation(), null);
			}
		}
		
		boolean needOldStyleWarning = (info != null && info.isOldStyle());
		
		onType.clearInterTypeMungers();
		
		// FIXME asc perf Could optimize here, after processing the expected set of types we may bring
		// binary types that are not exposed to the weaver, there is no need to attempt declare parents
		// or declare annotation really - unless we want to report the not-exposed to weaver
		// messages...
		
		List decpToRepeat = new ArrayList();
		List decaToRepeat = new ArrayList();
		boolean anyNewParents = false;
		boolean anyNewAnnotations = false;

		// first pass 
		// try and apply all decps - if they match, then great.  If they don't then
		// check if they are starred-annotation patterns.  If they are not starred
		// annotation patterns then they might match later...remember that...
		for (Iterator i = declareParents.iterator(); i.hasNext();) {
			DeclareParents decp = (DeclareParents)i.next();
			boolean didSomething = doDeclareParents(decp, sourceType);
			if (didSomething) {
				anyNewParents = true;
			} else {
				if (!decp.getChild().isStarAnnotation()) decpToRepeat.add(decp);
			}
		}

		for (Iterator i = declareAnnotationOnTypes.iterator(); i.hasNext();) {
			DeclareAnnotation deca = (DeclareAnnotation)i.next();
			boolean didSomething = doDeclareAnnotations(deca, sourceType,true);
			if (didSomething) {
				anyNewAnnotations = true;
			} else {
				if (!deca.getTypePattern().isStar()) decaToRepeat.add(deca);
			}
		}
		
        // now lets loop over and over until we have done all we can
		while ((anyNewAnnotations || anyNewParents) && 
				(!decpToRepeat.isEmpty() || !decaToRepeat.isEmpty())) {
			anyNewParents = anyNewAnnotations = false;
			List forRemoval = new ArrayList();
			for (Iterator i = decpToRepeat.iterator(); i.hasNext();) {
				DeclareParents decp = (DeclareParents)i.next();
				boolean didSomething = doDeclareParents(decp, sourceType);
				if (didSomething) {
					anyNewParents = true;
					forRemoval.add(decp);
				}
			}
			decpToRepeat.removeAll(forRemoval);

            forRemoval = new ArrayList();
			for (Iterator i = declareAnnotationOnTypes.iterator(); i.hasNext();) {
				DeclareAnnotation deca = (DeclareAnnotation)i.next();
				boolean didSomething = doDeclareAnnotations(deca, sourceType,false);
				if (didSomething) {
					anyNewAnnotations = true;
					forRemoval.add(deca);
				} 
			}
			decaToRepeat.removeAll(forRemoval);
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
			}
		}
		
		

        onType.checkInterTypeMungers();
		for (Iterator i = onType.getInterTypeMungers().iterator(); i.hasNext();) {
			EclipseTypeMunger munger = (EclipseTypeMunger) i.next();
			//System.out.println("applying: " + munger + " to " + new String(sourceType.sourceName));
			munger.munge(sourceType,onType);
		}
		
		// Call if you would like to do source weaving of declare @method/@constructor 
		// at source time... no need to do this as it can't impact anything, but left here for
		// future generations to enjoy.  Method source is commented out at the end of this module
		// doDeclareAnnotationOnMethods();
     
		// Call if you would like to do source weaving of declare @field 
		// at source time... no need to do this as it can't impact anything, but left here for
		// future generations to enjoy.  Method source is commented out at the end of this module
		// doDeclareAnnotationOnFields();

		
		if (skipInners) {
			CompilationAndWeavingContext.leavingPhase(tok);
			return;
		}

		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			if (memberTypes[i] instanceof SourceTypeBinding) {
				weaveInterTypeDeclarations((SourceTypeBinding) memberTypes[i], typeMungers, declareParents,declareAnnotationOnTypes, false);
			}
		}
			CompilationAndWeavingContext.leavingPhase(tok);
		}
		
	/**
	 * Called when we discover we are weaving intertype declarations on some type that has
	 * an existing 'WeaverStateInfo' object - this is typically some previously woven type
	 * that has been passed on the inpath.
	 * 
	 * sourceType and onType are the 'same type' - the former is the 'Eclipse' version and
	 * the latter is the 'Weaver' version.
	 */
	private void processTypeMungersFromExistingWeaverState(SourceTypeBinding sourceType,ResolvedType onType) {
		Collection previouslyAppliedMungers = onType.getWeaverState().getTypeMungers(onType);
		
		for (Iterator i = previouslyAppliedMungers.iterator(); i.hasNext(); ) {
			ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
			EclipseTypeMunger munger = factory.makeEclipseTypeMunger(m);
			if (munger.munge(sourceType,onType)) {
				if (onType.isInterface() &&	munger.getMunger().needsAccessToTopmostImplementor()) {
					if (!onType.getWorld().getCrosscuttingMembersSet().containsAspect(munger.getAspectType())) {
						dangerousInterfaces.put(onType, "implementors of "+onType+" must be woven by "+munger.getAspectType());
					}
				}
			}
			
		}
	}
	
	private boolean doDeclareParents(DeclareParents declareParents, SourceTypeBinding sourceType) {
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.PROCESSING_DECLARE_PARENTS, sourceType.sourceName);
		ResolvedType resolvedSourceType = factory.fromEclipse(sourceType);
		List newParents = declareParents.findMatchingNewParents(resolvedSourceType,false);
		if (!newParents.isEmpty()) {
			for (Iterator i = newParents.iterator(); i.hasNext(); ) {
				ResolvedType parent = (ResolvedType)i.next();
				if (dangerousInterfaces.containsKey(parent)) {
					ResolvedType onType = factory.fromEclipse(sourceType);
					factory.showMessage(IMessage.ERROR, 
										onType + ": " + dangerousInterfaces.get(parent),
										onType.getSourceLocation(), null);
				}
				if (Modifier.isFinal(parent.getModifiers())) {
					factory.showMessage(IMessage.ERROR,"cannot extend final class " + parent.getClassName(),declareParents.getSourceLocation(),null);
				} else {
				    // do not actually do it if the type isn't exposed - this will correctly reported as a problem elsewhere
					if (!resolvedSourceType.isExposedToWeaver()) return false;
					AsmRelationshipProvider.getDefault().addDeclareParentsRelationship(declareParents.getSourceLocation(),factory.fromEclipse(sourceType), newParents);
					addParent(sourceType, parent);
				}
			}
			CompilationAndWeavingContext.leavingPhase(tok);
			return true;
		}
		CompilationAndWeavingContext.leavingPhase(tok);
		return false;
	}
	
	private String stringifyTargets(long bits) {
		if ((bits & TagBits.AnnotationTargetMASK)==0) return "";
		Set s = new HashSet();
		if ((bits&TagBits.AnnotationForAnnotationType)!=0) s.add("ANNOTATION_TYPE");
		if ((bits&TagBits.AnnotationForConstructor)!=0) s.add("CONSTRUCTOR");
		if ((bits&TagBits.AnnotationForField)!=0) s.add("FIELD");
		if ((bits&TagBits.AnnotationForLocalVariable)!=0) s.add("LOCAL_VARIABLE");
		if ((bits&TagBits.AnnotationForMethod)!=0) s.add("METHOD");
		if ((bits&TagBits.AnnotationForPackage)!=0) s.add("PACKAGE");
		if ((bits&TagBits.AnnotationForParameter)!=0) s.add("PARAMETER");
		if ((bits&TagBits.AnnotationForType)!=0) s.add("TYPE");
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			sb.append(element);
			if (iter.hasNext()) sb.append(",");
		}
		sb.append("}");
		return sb.toString();
	}
	
	private boolean doDeclareAnnotations(DeclareAnnotation decA, SourceTypeBinding sourceType,boolean reportProblems) {
		ResolvedType rtx = factory.fromEclipse(sourceType);
		if (!decA.matches(rtx)) return false;
		if (!rtx.isExposedToWeaver()) return false;

		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.PROCESSING_DECLARE_ANNOTATIONS, sourceType.sourceName);
	
		// Get the annotation specified in the declare
		UnresolvedType aspectType = decA.getAspect();
		if (aspectType instanceof ReferenceType) {
			ReferenceType rt = (ReferenceType) aspectType;
			if (rt.isParameterizedType() || rt.isRawType()) {
				aspectType = rt.getGenericType();
			}
		}
		TypeBinding tb = factory.makeTypeBinding(aspectType);
		
		// Hideousness follows:
		
		// There are multiple situations to consider here and they relate to the combinations of
		// where the annotation is coming from and where the annotation is going to be put:
		//
		// 1. Straight full build, all from source - the annotation is from a dec@type and
		//    is being put on some type.  Both types are real SourceTypeBindings. WORKS
		// 2. Incremental build, changing the affected type - the annotation is from a
		//    dec@type in a BinaryTypeBinding (so has to be accessed via bcel) and the
		//    affected type is a real SourceTypeBinding.  Mostly works (pr128665)
		// 3. ?
		
		SourceTypeBinding stb = (SourceTypeBinding)tb;
		Annotation[] toAdd = null;
		long abits = 0;
		
		// Might have to retrieve the annotation through BCEL and construct an eclipse one for it.
		if (stb instanceof BinaryTypeBinding) {
			ReferenceType rt = (ReferenceType)factory.fromEclipse(stb);
			ResolvedMember[] methods = rt.getDeclaredMethods();
			ResolvedMember decaMethod = null;
			String nameToLookFor = decA.getAnnotationMethod();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(nameToLookFor)) {decaMethod = methods[i];break;}
			}
			if (decaMethod!=null) { // could assert this ...
				AnnotationX[] axs = decaMethod.getAnnotations();
				toAdd = new Annotation[1];
				toAdd[0] = createAnnotationFromBcelAnnotation(axs[0],decaMethod.getSourceLocation().getOffset(),factory);
				// BUG BUG BUG - We dont test these abits are correct, in fact we'll be very lucky if they are.
				// What does that mean?  It means on an incremental compile you might get away with an
				// annotation that isn't allowed on a type being put on a type.
				abits = toAdd[0].resolvedType.getAnnotationTagBits(); 
			}		
		} else {
			// much nicer, its a real SourceTypeBinding so we can stay in eclipse land
			MethodBinding[]	mbs = stb.getMethods(decA.getAnnotationMethod().toCharArray());
			abits = mbs[0].getAnnotationTagBits(); // ensure resolved
			TypeDeclaration typeDecl = ((SourceTypeBinding)mbs[0].declaringClass).scope.referenceContext;
			AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(mbs[0]);
			toAdd = methodDecl.annotations; // this is what to add
			toAdd[0] = createAnnotationCopy(toAdd[0]);
			if (toAdd[0].resolvedType!=null) // pr148536
			  abits = toAdd[0].resolvedType.getAnnotationTagBits();
		}
		
		if (sourceType instanceof BinaryTypeBinding) {
			// In this case we can't access the source type binding to add a new annotation, so let's put something
			// on the weaver type temporarily
			ResolvedType theTargetType = factory.fromEclipse(sourceType);
			TypeBinding theAnnotationType = toAdd[0].resolvedType;
			String sig = new String(theAnnotationType.signature());
			UnresolvedType bcelAnnotationType = UnresolvedType.forSignature(sig);
			String name = bcelAnnotationType.getName();
			if (theTargetType.hasAnnotation(bcelAnnotationType)) {
				CompilationAndWeavingContext.leavingPhase(tok);
				return false;
			}
			
			// FIXME asc tidy up this code that duplicates whats below!
			// Simple checks on the bits
			boolean giveupnow = false;
			if (((abits & TagBits.AnnotationTargetMASK)!=0)) {
				if ( isAnnotationTargettingSomethingOtherThanAnnotationOrNormal(abits)) {
					// error will have been already reported
					giveupnow = true;
				} else if (  (sourceType.isAnnotationType() && (abits & TagBits.AnnotationForAnnotationType)==0) ||
				      (!sourceType.isAnnotationType() && (abits & TagBits.AnnotationForType)==0) ) {
				
				  if (reportProblems) {
				    if (decA.isExactPattern()) {
				      factory.showMessage(IMessage.ERROR,
						WeaverMessages.format(WeaverMessages.INCORRECT_TARGET_FOR_DECLARE_ANNOTATION,rtx.getName(),toAdd[0].type,stringifyTargets(abits)),
						decA.getSourceLocation(), null);
				    } 
				    // dont put out the lint - the weaving process will do that
//				    else {
//					  if (factory.getWorld().getLint().invalidTargetForAnnotation.isEnabled()) {
//						  factory.getWorld().getLint().invalidTargetForAnnotation.signal(new String[]{rtx.getName(),toAdd[0].type.toString(),stringifyTargets(abits)},decA.getSourceLocation(),null);
//					  }
//				    }
				  }
				  giveupnow=true;
			    }
			}
			if (giveupnow) { 
				CompilationAndWeavingContext.leavingPhase(tok);
				return false;
			}
			
			theTargetType.addAnnotation(new AnnotationX(new FakeAnnotation(name,sig,(abits & TagBits.AnnotationRuntimeRetention)!=0),factory.getWorld()));
			CompilationAndWeavingContext.leavingPhase(tok);
			return true;
		}
		
		Annotation currentAnnotations[] = sourceType.scope.referenceContext.annotations;
		if (currentAnnotations!=null) 
		for (int i = 0; i < currentAnnotations.length; i++) {
			Annotation annotation = currentAnnotations[i];
			String a = CharOperation.toString(annotation.type.getTypeName());
			String b = CharOperation.toString(toAdd[0].type.getTypeName());
			// FIXME asc we have a lint for attempting to add an annotation twice to a method,
			// we could put it out here *if* we can resolve the problem of errors coming out
			// multiple times if we have cause to loop through here
			if (a.equals(b)) {
				CompilationAndWeavingContext.leavingPhase(tok);
				return false;
			}
		}
		
		if (((abits & TagBits.AnnotationTargetMASK)!=0)) {
			if ( (abits & (TagBits.AnnotationForAnnotationType | TagBits.AnnotationForType))==0) {
				// this means it specifies something other than annotation or normal type - error will have been already reported, just resolution process above
				CompilationAndWeavingContext.leavingPhase(tok);
				return false;
			}
			if (  (sourceType.isAnnotationType() && (abits & TagBits.AnnotationForAnnotationType)==0) ||
			      (!sourceType.isAnnotationType() && (abits & TagBits.AnnotationForType)==0) ) {
			
			if (reportProblems) {
			  if (decA.isExactPattern()) {
			    factory.showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.INCORRECT_TARGET_FOR_DECLARE_ANNOTATION,rtx.getName(),toAdd[0].type,stringifyTargets(abits)),
					decA.getSourceLocation(), null);
			  } 
			  // dont put out the lint - the weaving process will do that
//			  else {
//				if (factory.getWorld().getLint().invalidTargetForAnnotation.isEnabled()) {
//					factory.getWorld().getLint().invalidTargetForAnnotation.signal(new String[]{rtx.getName(),toAdd[0].type.toString(),stringifyTargets(abits)},decA.getSourceLocation(),null);
//				}
//			  }
			}
			CompilationAndWeavingContext.leavingPhase(tok);
			return false;
		  }
		}
		
		// Build a new array of annotations
		
		// remember the current set (rememberAnnotations only does something the first time it is called for a type)
		sourceType.scope.referenceContext.rememberAnnotations(); 
		
		AsmRelationshipProvider.getDefault().addDeclareAnnotationRelationship(decA.getSourceLocation(),rtx.getSourceLocation());
		Annotation abefore[] = sourceType.scope.referenceContext.annotations;
		Annotation[] newset = new Annotation[toAdd.length+(abefore==null?0:abefore.length)];
		System.arraycopy(toAdd,0,newset,0,toAdd.length);
		if (abefore!=null) {
			System.arraycopy(abefore,0,newset,toAdd.length,abefore.length);
		}
		sourceType.scope.referenceContext.annotations = newset;
		CompilationAndWeavingContext.leavingPhase(tok);
		return true;
	}
	
	/**
	 * Transform an annotation from its AJ form to an eclipse form.  We *DONT* care about the
	 * values of the annotation.  that is because it is only being stuck on a type during
	 * type completion to allow for other constructs (decps, decas) that might be looking for it -
	 * when the class actually gets to disk it wont have this new annotation on it and during
	 * weave time we will do the right thing copying across values too.
	 */
	private static Annotation createAnnotationFromBcelAnnotation(AnnotationX annX,int pos, EclipseFactory factory) {
		String name = annX.getTypeName();
		TypeBinding tb = factory.makeTypeBinding(annX.getSignature());
		String theName = annX.getSignature().getBaseName();
		char[][] typeName = CharOperation.splitOn('.',name.replace('$','.').toCharArray()); //pr149293 - not bulletproof...
		long[] positions = new long[typeName.length];
		for (int i = 0; i < positions.length; i++) positions[i]=pos;
		TypeReference annType = new QualifiedTypeReference(typeName,positions);
		NormalAnnotation ann = new NormalAnnotation(annType,pos);
		ann.resolvedType=tb; // yuck - is this OK in all cases?
		// We don't need membervalues...
//		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
//		MemberValuePair[] mvps = new MemberValuePair[2];
//		mvps[0] = new MemberValuePair("value".toCharArray(),pos,pos,pcExpr);
//		Expression argNamesExpr = new StringLiteral(argNames.toCharArray(),pos,pos);
//		mvps[1] = new MemberValuePair("argNames".toCharArray(),pos,pos,argNamesExpr);
//		ann.memberValuePairs = mvps;
		return ann;
	}
	
	/** Create a copy of an annotation, not deep but deep enough so we don't copy across fields that will get us into trouble like 'recipient' */
	private static Annotation createAnnotationCopy(Annotation ann) {
		NormalAnnotation ann2 = new NormalAnnotation(ann.type,ann.sourceStart);
		ann2.memberValuePairs = ann.memberValuePairs();
		ann2.resolvedType = ann.resolvedType;
		ann2.bits = ann.bits;
		return ann2;
//		String name = annX.getTypeName();
//		TypeBinding tb = factory.makeTypeBinding(annX.getSignature());
//		String theName = annX.getSignature().getBaseName();
//		char[][] typeName = CharOperation.splitOn('.',name.replace('$','.').toCharArray()); //pr149293 - not bulletproof...
//		long[] positions = new long[typeName.length];
//		for (int i = 0; i < positions.length; i++) positions[i]=pos;
//		TypeReference annType = new QualifiedTypeReference(typeName,positions);
//		NormalAnnotation ann = new NormalAnnotation(annType,pos);
//		ann.resolvedType=tb; // yuck - is this OK in all cases?
//		// We don't need membervalues...
////		Expression pcExpr = new StringLiteral(pointcutExpression.toCharArray(),pos,pos);
////		MemberValuePair[] mvps = new MemberValuePair[2];
////		mvps[0] = new MemberValuePair("value".toCharArray(),pos,pos,pcExpr);
////		Expression argNamesExpr = new StringLiteral(argNames.toCharArray(),pos,pos);
////		mvps[1] = new MemberValuePair("argNames".toCharArray(),pos,pos,argNamesExpr);
////		ann.memberValuePairs = mvps;
//		return ann;
	}

	private boolean isAnnotationTargettingSomethingOtherThanAnnotationOrNormal(long abits) {
		return (abits & (TagBits.AnnotationForAnnotationType | TagBits.AnnotationForType))==0;
	}
	

	private void reportDeclareParentsMessage(WeaveMessage.WeaveMessageKind wmk,SourceTypeBinding sourceType,ResolvedType parent) {
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

	private void addParent(SourceTypeBinding sourceType, ResolvedType parent) {
		ReferenceBinding parentBinding = (ReferenceBinding)factory.makeTypeBinding(parent); 
		if (parentBinding == null) return; // The parent is missing, it will be reported elsewhere.
        sourceType.rememberTypeHierarchy();
        if (parentBinding.isClass()) {
			sourceType.superclass = parentBinding;
			
            // this used to be true, but I think I've fixed it now, decp is done at weave time!			
			// TAG: WeavingMessage    DECLARE PARENTS: EXTENDS
			// Compiler restriction: Can't do EXTENDS at weave time
			// So, only see this message if doing a source compilation
		    // reportDeclareParentsMessage(WeaveMessage.WEAVEMESSAGE_DECLAREPARENTSEXTENDS,sourceType,parent);
			
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
			// warnOnAddedInterface(factory.fromEclipse(sourceType),parent); // now reported at weave time...
			

            // this used to be true, but I think I've fixed it now, decp is done at weave time!			
			// TAG: WeavingMessage    DECLARE PARENTS: IMPLEMENTS
			// This message will come out of BcelTypeMunger.munge if doing a binary weave
	        // reportDeclareParentsMessage(WeaveMessage.WEAVEMESSAGE_DECLAREPARENTSIMPLEMENTS,sourceType,parent);
			
		}
        
        // also add it to the bcel delegate if there is one
        if (sourceType instanceof BinaryTypeBinding) {
	        ResolvedType onType = factory.fromEclipse(sourceType);
	        ReferenceType rt = (ReferenceType)onType;
	        ReferenceTypeDelegate rtd = rt.getDelegate();
	        if (rtd instanceof BcelObjectType) {
		        ((BcelObjectType)rtd).addParent(parent);
		    }
        }
        
	}

	public void warnOnAddedInterface (ResolvedType type, ResolvedType parent) {
		World world = factory.getWorld();
		ResolvedType serializable = world.getCoreType(UnresolvedType.SERIALIZABLE);
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
		boolean needFieldsAndMethods,
		AccessRestriction accessRestriction)
	{

		if (inBinaryTypeCreationAndWeaving) {
			BinaryTypeBinding ret = super.createBinaryTypeFrom(
				binaryType,
				packageBinding,
				needFieldsAndMethods,
				accessRestriction);
			pendingTypesToFinish.add(ret);
			return ret;
		}
		
		inBinaryTypeCreationAndWeaving = true;
		try {
			BinaryTypeBinding ret = super.createBinaryTypeFrom(
				binaryType,
				packageBinding,
				needFieldsAndMethods,
				accessRestriction);
			factory.getWorld().validateType(factory.fromBinding(ret));
			// if you need the bytes to pass to validate, here they are:((ClassFileReader)binaryType).getReferenceBytes()
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

	/**
	 * Callback driven when the compiler detects an anonymous type during block resolution.
	 * We need to add it to the weaver so that we don't trip up later.
	 * @param aBinding
	 */
	public void anonymousTypeBindingCreated(LocalTypeBinding aBinding) {
		factory.addSourceTypeBinding(aBinding,null);
	}
}

// commented out, supplied as info on how to manipulate annotations in an eclipse world
//
// public void doDeclareAnnotationOnMethods() {
// Do the declare annotation on fields/methods/ctors
//Collection daoms = factory.getDeclareAnnotationOnMethods();
//if (daoms!=null && daoms.size()>0 && !(sourceType instanceof BinaryTypeBinding)) {
//	System.err.println("Going through the methods on "+sourceType.debugName()+" looking for DECA matches");
//	// We better take a look through them...
//	for (Iterator iter = daoms.iterator(); iter.hasNext();) {
//		DeclareAnnotation element = (DeclareAnnotation) iter.next();
//		System.err.println("Looking for anything that might match "+element+" on "+sourceType.debugName()+"  "+getType(sourceType.compoundName).debugName()+"  "+(sourceType instanceof BinaryTypeBinding));
//		
//		ReferenceBinding rbb = getType(sourceType.compoundName);
//		// fix me if we ever uncomment this code... should iterate the other way round, over the methods then over the decas
//		sourceType.methods();
//		MethodBinding sourceMbs[] = sourceType.methods;
//		for (int i = 0; i < sourceMbs.length; i++) {
//			MethodBinding sourceMb = sourceMbs[i];
//			MethodBinding mbbbb = ((SourceTypeBinding)rbb).getExactMethod(sourceMb.selector,sourceMb.parameters);
//			boolean isCtor = sourceMb.selector[0]=='<';
//			
//			if ((element.isDeclareAtConstuctor() ^ !isCtor)) {
//			System.err.println("Checking "+sourceMb+" ... declaringclass="+sourceMb.declaringClass.debugName()+" rbb="+rbb.debugName()+"  "+sourceMb.declaringClass.equals(rbb));
//			
//			ResolvedMember rm = null;
//			rm = EclipseFactory.makeResolvedMember(mbbbb);
//			if (element.matches(rm,factory.getWorld())) {
//				System.err.println("MATCH");
//				
//				// Determine the set of annotations that are currently on the method
//				ReferenceBinding rb = getType(sourceType.compoundName);
////				TypeBinding tb = factory.makeTypeBinding(decA.getAspect());
//				MethodBinding mb = ((SourceTypeBinding)rb).getExactMethod(sourceMb.selector,sourceMb.parameters);
//				//long abits = mbs[0].getAnnotationTagBits(); // ensure resolved
//				TypeDeclaration typeDecl = ((SourceTypeBinding)sourceMb.declaringClass).scope.referenceContext;
//				AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(sourceMb);
//				Annotation[] currentlyHas = methodDecl.annotations; // this is what to add
//				//abits = toAdd[0].resolvedType.getAnnotationTagBits();
//				
//				// Determine the annotations to add to that method
//				TypeBinding tb = factory.makeTypeBinding(element.getAspect());
//				MethodBinding[] aspectMbs = ((SourceTypeBinding)tb).getMethods(element.getAnnotationMethod().toCharArray());
//				long abits = aspectMbs[0].getAnnotationTagBits(); // ensure resolved
//				TypeDeclaration typeDecl2 = ((SourceTypeBinding)aspectMbs[0].declaringClass).scope.referenceContext;
//				AbstractMethodDeclaration methodDecl2 = typeDecl2.declarationOf(aspectMbs[0]);
//				Annotation[] toAdd = methodDecl2.annotations; // this is what to add
//				// abits = toAdd[0].resolvedType.getAnnotationTagBits();
//System.err.println("Has: "+currentlyHas+"    toAdd: "+toAdd);
//				
//				// fix me? should check if it already has the annotation
//				//Annotation abefore[] = sourceType.scope.referenceContext.annotations;
//				Annotation[] newset = new Annotation[(currentlyHas==null?0:currentlyHas.length)+1];
//				System.arraycopy(toAdd,0,newset,0,toAdd.length);
//				if (currentlyHas!=null) {
//					System.arraycopy(currentlyHas,0,newset,1,currentlyHas.length);
//				}
//				methodDecl.annotations = newset;
//				System.err.println("New set on "+CharOperation.charToString(sourceMb.selector)+" is "+newset);
//			} else
//				System.err.println("NO MATCH");
//		}
//	}
//	}
//}
//}

// commented out, supplied as info on how to manipulate annotations in an eclipse world
//
// public void doDeclareAnnotationOnFields() {
//		Collection daofs = factory.getDeclareAnnotationOnFields();
//		if (daofs!=null && daofs.size()>0 && !(sourceType instanceof BinaryTypeBinding)) {
//			System.err.println("Going through the fields on "+sourceType.debugName()+" looking for DECA matches");
//			// We better take a look through them...
//			for (Iterator iter = daofs.iterator(); iter.hasNext();) {
//				DeclareAnnotation element = (DeclareAnnotation) iter.next();
//				System.err.println("Processing deca "+element+" on "+sourceType.debugName()+"  "+getType(sourceType.compoundName).debugName()+"  "+(sourceType instanceof BinaryTypeBinding));
//				
//				ReferenceBinding rbb = getType(sourceType.compoundName);
//				// fix me? should iterate the other way round, over the methods then over the decas
//				sourceType.fields(); // resolve the bloody things
//				FieldBinding sourceFbs[] = sourceType.fields;
//				for (int i = 0; i < sourceFbs.length; i++) {
//					FieldBinding sourceFb = sourceFbs[i];
//					//FieldBinding fbbbb = ((SourceTypeBinding)rbb).getgetExactMethod(sourceMb.selector,sourceMb.parameters);
//					
//					System.err.println("Checking "+sourceFb+" ... declaringclass="+sourceFb.declaringClass.debugName()+" rbb="+rbb.debugName());
//					
//					ResolvedMember rm = null;
//					rm = EclipseFactory.makeResolvedMember(sourceFb);
//					if (element.matches(rm,factory.getWorld())) {
//						System.err.println("MATCH");
//						
//						// Determine the set of annotations that are currently on the field
//						ReferenceBinding rb = getType(sourceType.compoundName);
////						TypeBinding tb = factory.makeTypeBinding(decA.getAspect());
//						FieldBinding fb = ((SourceTypeBinding)rb).getField(sourceFb.name,true);
//						//long abits = mbs[0].getAnnotationTagBits(); // ensure resolved
//						TypeDeclaration typeDecl = ((SourceTypeBinding)sourceFb.declaringClass).scope.referenceContext;
//						FieldDeclaration fd = typeDecl.declarationOf(sourceFb);
//						//AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(sourceMb);
//						Annotation[] currentlyHas = fd.annotations; // this is what to add
//						//abits = toAdd[0].resolvedType.getAnnotationTagBits();
//						
//						// Determine the annotations to add to that method
//						TypeBinding tb = factory.makeTypeBinding(element.getAspect());
//						MethodBinding[] aspectMbs = ((SourceTypeBinding)tb).getMethods(element.getAnnotationMethod().toCharArray());
//						long abits = aspectMbs[0].getAnnotationTagBits(); // ensure resolved
//						TypeDeclaration typeDecl2 = ((SourceTypeBinding)aspectMbs[0].declaringClass).scope.referenceContext;
//						AbstractMethodDeclaration methodDecl2 = typeDecl2.declarationOf(aspectMbs[0]);
//						Annotation[] toAdd = methodDecl2.annotations; // this is what to add
//						// abits = toAdd[0].resolvedType.getAnnotationTagBits();
//System.err.println("Has: "+currentlyHas+"    toAdd: "+toAdd);
//						
//						// fix me? check if it already has the annotation
//
//
//						//Annotation abefore[] = sourceType.scope.referenceContext.annotations;
//						Annotation[] newset = new Annotation[(currentlyHas==null?0:currentlyHas.length)+1];
//						System.arraycopy(toAdd,0,newset,0,toAdd.length);
//						if (currentlyHas!=null) {
//							System.arraycopy(currentlyHas,0,newset,1,currentlyHas.length);
//						}
//						fd.annotations = newset;
//						System.err.println("New set on "+CharOperation.charToString(sourceFb.name)+" is "+newset);
//					} else
//						System.err.println("NO MATCH");
//				}
//			
//			}
//		}
