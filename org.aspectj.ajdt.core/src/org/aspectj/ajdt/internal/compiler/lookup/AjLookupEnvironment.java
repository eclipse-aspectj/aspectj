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

import java.util.*;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.*;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

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
			}
		}
		factory.finishTypeMungers();
	
		// now do weaving
		Collection typeMungers = factory.getTypeMungers();
		
		Collection declareParents = factory.getDeclareParents();

		doPendingWeaves();

		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			weaveInterTypeDeclarations(units[i].scope, typeMungers, declareParents);
			units[i] = null; // release unnecessary reference to the parsed unit
		}
		
		stepCompleted = BUILD_FIELDS_AND_METHODS;
		lastCompletedUnitIndex = lastUnitIndex;
	}

	private void doPendingWeaves() {
		for (Iterator i = pendingTypesToWeave.iterator(); i.hasNext(); ) {
			SourceTypeBinding t = (SourceTypeBinding)i.next();
			weaveInterTypeDeclarations(t);
		}
		pendingTypesToWeave.clear();
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
//		if (new String(sourceType.sourceName()).equals("Target")) {
//			Thread.currentThread().dumpStack();
//		}
//		
//		System.out.println("weaving types: " + new String(sourceType.sourceName()));
//		System.out.println("  mungers: " + typeMungers);
		ResolvedTypeX onType = factory.fromEclipse(sourceType);
		onType.clearInterTypeMungers();
		
		for (Iterator i = declareParents.iterator(); i.hasNext();) {
			doDeclareParents((DeclareParents)i.next(), sourceType);
		}
		
		for (Iterator i = typeMungers.iterator(); i.hasNext();) {
			EclipseTypeMunger munger = (EclipseTypeMunger) i.next();
			if (munger.matches(onType)) {
				onType.addInterTypeMunger(munger);
			}
		}
		
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
		if (declareParents.match(factory.fromEclipse(sourceType))) {
			TypePatternList l = declareParents.getParents();
			for (int i=0, len=l.size(); i < len; i++) {
				addParent(declareParents, sourceType, l.get(i));
			}
		}
	}

	private void addParent(DeclareParents declareParents, SourceTypeBinding sourceType, TypePattern typePattern) {
		if (typePattern == TypePattern.NO) return;  // already had an error here
		TypeX iType = typePattern.getExactType();
		ReferenceBinding b = (ReferenceBinding)factory.makeTypeBinding(iType); //"
				
		if (b.isClass()) {
			if (sourceType.isInterface()) {
				factory.showMessage(IMessage.ERROR, 
					"interface can not extend a class", 
					declareParents.getSourceLocation(), null
				);
				// how to handle xcutting errors???
			}
			
			if (sourceType == b || sourceType.isSuperclassOf(b)) {
				factory.showMessage(IMessage.ERROR,
					"class can not extend itself", declareParents.getSourceLocation(), null
				);
				return;
			}
			sourceType.superclass = b;
		} else {
			//??? it's not considered an error to extend yourself, nothing happens
			if (sourceType.equals(b)) {
				return;
			}
			
			if (sourceType.isInterface() && b.implementsInterface(sourceType, true)) {
				factory.showMessage(IMessage.ERROR,
					"interface can not extend itself", declareParents.getSourceLocation(), null
				);
				return;
			}
			if (sourceType == b || b.isSuperclassOf(sourceType)) return;
			ReferenceBinding[] oldI = sourceType.superInterfaces;
			ReferenceBinding[] newI;
			if (oldI == null) {
				newI = new ReferenceBinding[1];
				newI[0] = b;
			} else {
				int n = oldI.length;
				newI = new ReferenceBinding[n+1];
				System.arraycopy(oldI, 0, newI, 0, n);
				newI[n] = b;
			}
			sourceType.superInterfaces = newI;
		}
		
	}
	
	
	
	private List pendingTypesToFinish = new ArrayList();
	boolean inBinaryTypeCreation = false;
	public BinaryTypeBinding createBinaryTypeFrom(
		IBinaryType binaryType,
		PackageBinding packageBinding,
		boolean needFieldsAndMethods)
	{
		if (inBinaryTypeCreation) {
			BinaryTypeBinding ret = super.createBinaryTypeFrom(
				binaryType,
				packageBinding,
				needFieldsAndMethods);
			pendingTypesToFinish.add(ret);
			return ret;
		}
		
		
		inBinaryTypeCreation = true;
		try {
			BinaryTypeBinding ret = super.createBinaryTypeFrom(
				binaryType,
				packageBinding,
				needFieldsAndMethods);
			weaveInterTypeDeclarations(ret);
			
			return ret;
		} finally {
			inBinaryTypeCreation = false;
			if (!pendingTypesToFinish.isEmpty()) {
				for (Iterator i = pendingTypesToFinish.iterator(); i.hasNext(); ) {
					weaveInterTypeDeclarations((BinaryTypeBinding)i.next());
				}
				pendingTypesToFinish.clear();
			}
		}		
	}
}
