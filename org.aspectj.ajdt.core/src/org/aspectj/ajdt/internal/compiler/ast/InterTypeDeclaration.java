/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.*;

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.weaver.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public abstract class InterTypeDeclaration extends MethodDeclaration {
	//public AstNode myDeclaration;
	public TypeReference onType;
	protected ReferenceBinding onTypeBinding;

	protected ResolvedTypeMunger munger;
	protected int declaredModifiers;
	protected char[] declaredSelector;

	//protected Set superMethodsCalled;

	public InterTypeDeclaration(CompilationResult result, TypeReference onType) {
		super(result);
		this.onType = onType;
		modifiers = AccPublic | AccStatic;
	}
	
	public void setDeclaredModifiers(int modifiers) {
		this.declaredModifiers = modifiers;
	}
	
	public void setSelector(char[] selector) {
		declaredSelector = selector;
		this.selector = CharOperation.concat(selector, Integer.toHexString(sourceStart).toCharArray());
	}
	
	public void resolve(ClassScope upperScope) {
		if (ignoreFurtherInvestigation) return;
		
		
		ClassScope newParent = new InterTypeScope(upperScope, onTypeBinding);
			//interBinding.introducedField.declaringClass);
		scope.parent = newParent;
		this.scope.isStatic = Modifier.isStatic(declaredModifiers);
		super.resolve(newParent);
		fixSuperCallsInBody();
	}

	/**
	 * Called from AspectDeclarations.buildInterTypeAndPerClause
	 */
	public abstract void build(ClassScope classScope, CrosscuttingMembers xcut);

	public void fixSuperCallsInBody() {
		SuperFixerVisitor v = new SuperFixerVisitor(this, onTypeBinding);
		this.traverse(v, (ClassScope)null);
		HashSet set = new HashSet();
		for (Iterator i = v.superMethodsCalled.iterator(); i.hasNext(); ) {
			MethodBinding b = (MethodBinding)i.next();
			set.add(EclipseWorld.makeResolvedMember(b));
		}
		
		munger.setSuperMethodsCalled(set);
	}

	protected void resolveOnType(ClassScope classScope) {
		checkSpec();		
		onTypeBinding = (ReferenceBinding)onType.getTypeBinding(classScope);
		if (!onTypeBinding.isValidBinding()) {
			if (onTypeBinding instanceof ProblemReferenceBinding) {
				classScope.problemReporter().invalidType(onType, onTypeBinding);
			} else {
				//XXX trouble
			}
			ignoreFurtherInvestigation = true;
		}
	}
	
	
	protected void checkSpec() {
		if (Modifier.isProtected(declaredModifiers)) {
			scope.problemReporter().signalError(sourceStart, sourceEnd,
				"protected inter-type declarations are not allowed");
			ignoreFurtherInvestigation = true;
		}
	}
	
	protected List makeEffectiveSignatureAttribute(
		ResolvedMember sig,
		Shadow.Kind kind,
		boolean weaveBody)
	{
		List l = new ArrayList(1);
		l.add(new EclipseAttributeAdapter(
				new AjAttribute.EffectiveSignatureAttribute(sig, kind, weaveBody)));
		return l;
	}
	
	protected int generateInfoAttributes(ClassFile classFile) {
		munger.getSignature().setPosition(sourceStart, sourceEnd);
		
		//System.out.println("generating effective for " + this);
		List l;;
		Shadow.Kind kind = getShadowKindForBody();
		if (kind != null) {
			l = makeEffectiveSignatureAttribute(munger.getSignature(), kind, true);
		} else {
			l = new ArrayList(0); //AstUtil.getAjSyntheticAttribute();
		}

		return classFile.generateMethodInfoAttribute(binding, l);
	}

	protected abstract Shadow.Kind getShadowKindForBody();
}
