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

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class AjLookupEnvironment extends LookupEnvironment {
	public EclipseWorld world = null;
	
	private boolean builtInterTypesAndPerClauses = false;
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
		builtInterTypesAndPerClauses = false;
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
	
		// collect inter-type declarations as well
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			units[i].scope.buildFieldsAndMethods();
		}
		
		// need to build inter-type declarations for all AspectDeclarations at this point
		for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
			SourceTypeBinding[] b = units[i].scope.topLevelTypes;
			for (int j = 0; j < b.length; j++) {
				buildInterTypeAndPerClause(b[j].scope);
			}
		}
		builtInterTypesAndPerClauses = true;
		doPendingWeaves();
	
		// now do weaving
		Collection typeMungers = world.getTypeMungers();
		Collection declareParents = world.getDeclareParents();
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

		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			buildInterTypeAndPerClause(((SourceTypeBinding) memberTypes[i]).scope);
		}
	}
	
	
	
		
	private void weaveInterTypeDeclarations(CompilationUnitScope unit, Collection typeMungers, Collection declareParents) {
		for (int i = 0, length = unit.topLevelTypes.length; i < length; i++)
		    weaveInterTypeDeclarations(unit.topLevelTypes[i], typeMungers, declareParents);
	
		//System.err.println("done with inter types");
	}
	
	
	private void weaveInterTypeDeclarations(SourceTypeBinding sourceType) {
		if (!builtInterTypesAndPerClauses) {
			pendingTypesToWeave.add(sourceType);
		} else {
			//System.err.println("weaving: " + new String(sourceType.sourceName()) + ", " + world.getTypeMungers());
			weaveInterTypeDeclarations(sourceType, world.getTypeMungers(), world.getDeclareParents());
		}
	}
	
	
	private void weaveInterTypeDeclarations(SourceTypeBinding sourceType, Collection typeMungers, Collection declareParents) {
		ResolvedTypeX onType = world.fromEclipse(sourceType);
		onType.clearInterTypeMungers();
		
		for (Iterator i = declareParents.iterator(); i.hasNext();) {
			doDeclareParents((DeclareParents)i.next(), sourceType);
		}
		
		for (Iterator i = typeMungers.iterator(); i.hasNext();) {
			EclipseTypeMunger munger = (EclipseTypeMunger) i.next();
			//System.out.println("weaving: " + munger);
			//if (munger.match(scope))
			if (munger.matches(onType)) {
				onType.addInterTypeMunger(munger);
			}
		}
		
		for (Iterator i = onType.getInterTypeMungers().iterator(); i.hasNext();) {
			EclipseTypeMunger munger = (EclipseTypeMunger) i.next();
			munger.munge(sourceType);
		}
		
		

		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			if (memberTypes[i] instanceof SourceTypeBinding) {
				weaveInterTypeDeclarations((SourceTypeBinding) memberTypes[i], typeMungers, declareParents);
			}
		}
	}

	private void doDeclareParents(DeclareParents declareParents, SourceTypeBinding sourceType) {
		if (declareParents.match(world.fromEclipse(sourceType))) {
			TypePatternList l = declareParents.getParents();
			for (int i=0, len=l.size(); i < len; i++) {
				addParent(declareParents, sourceType, l.get(i));
			}
		}
	}

	private void addParent(DeclareParents declareParents, SourceTypeBinding sourceType, TypePattern typePattern) {
		//if (!typePattern.assertExactType(world.getMessageHandler())) return;
		if (typePattern == TypePattern.NO) return;  // already had an error here
		TypeX iType = typePattern.getExactType();
//		if (iType == null) {
//			throw new RuntimeException("yikes: " + typePattern);
//		}
		//if (iType == ResolvedTypeX.MISSING || iType == null) return;
		ReferenceBinding b = (ReferenceBinding)world.makeTypeBinding(iType); //"
				
		if (b.isClass()) {
			if (sourceType.isInterface()) {
				world.getMessageHandler().handleMessage(MessageUtil.error(
					"interface can not extend a class", declareParents.getSourceLocation()
				));
				// how to handle xcutting errors???
			}
			
			if (sourceType == b || sourceType.isSuperclassOf(b)) {
				world.getMessageHandler().handleMessage(MessageUtil.error(
					"class can not extend itself", declareParents.getSourceLocation()
				));
				return;
			}
			sourceType.superclass = b;
		} else {
			//??? it's not considered an error to extend yourself, nothing happens
			if (sourceType.equals(b)) {
				return;
			}
			
			if (sourceType.isInterface() && b.implementsInterface(sourceType, true)) {
				world.getMessageHandler().handleMessage(MessageUtil.error(
					"interface can not extend itself", declareParents.getSourceLocation()
				));
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
