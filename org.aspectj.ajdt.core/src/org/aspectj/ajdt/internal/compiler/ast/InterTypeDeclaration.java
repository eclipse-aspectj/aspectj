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


package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeScope;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.weaver.*;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.*;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Base type for all inter-type declarations including methods, fields and constructors.
 *
 * @author Jim Hugunin
 */
public abstract class InterTypeDeclaration extends AjMethodDeclaration {
	public TypeReference onType;
	protected ReferenceBinding onTypeBinding;

	protected ResolvedTypeMunger munger;
	protected int declaredModifiers;
	protected char[] declaredSelector;
	
	// XXXAJ5 - When the compiler is changed, these will exist somewhere in it...
	private final static short ACC_ANNOTATION   = 0x2000;
	private final static short ACC_ENUM         = 0x4000;


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
		this.selector = CharOperation.concat(getPrefix(),this.selector);
	}
	
	// return the selector prefix for this itd that is to be used before resolution replaces it with a "proper" name
	protected abstract char[] getPrefix();
	
	/**
	 * Checks that the target for the ITD is not an annotation.  If it is, an error message
	 * is signaled.  We return true if it is annotation so the caller knows to stop processing.
	 * kind is 'constructor', 'field', 'method'
	 */
	public boolean isTargetAnnotation(ClassScope classScope,String kind) {
		if ((onTypeBinding.getAccessFlags() & ACC_ANNOTATION)!=0) { 
			classScope.problemReporter().signalError(sourceStart,sourceEnd,
			  "can't make inter-type "+kind+" declarations on annotation types.");
			ignoreFurtherInvestigation = true;
			return true;
		}
		return false;
	}
	
	/**
	 * Checks that the target for the ITD is not an enum.  If it is, an error message
	 * is signaled.  We return true if it is enum so the caller knows to stop processing.
	 */
	public boolean isTargetEnum(ClassScope classScope,String kind) {
		if ((onTypeBinding.getAccessFlags() & ACC_ENUM)!=0) { 
			classScope.problemReporter().signalError(sourceStart,sourceEnd,
			  "can't make inter-type "+kind+" declarations on enum types.");
			ignoreFurtherInvestigation = true;
			return true;
		}
		return false;
	}
	
	public void resolve(ClassScope upperScope) {
		if (ignoreFurtherInvestigation) return;
		
		
		ClassScope newParent = new InterTypeScope(upperScope, onTypeBinding);
		scope.parent = newParent;
		this.scope.isStatic = Modifier.isStatic(declaredModifiers);
		fixSuperCallsForInterfaceContext(upperScope);
		if (ignoreFurtherInvestigation) return;
		
		super.resolve(newParent);
		fixSuperCallsInBody();
	}

	private void fixSuperCallsForInterfaceContext(ClassScope scope) {
		if (onTypeBinding.isInterface()) {
			InterSuperFixerVisitor v =
				new InterSuperFixerVisitor(this, 
						EclipseFactory.fromScopeLookupEnvironment(scope), scope);
			this.traverse(v, scope);
		}
	}

	/**
	 * Called from AspectDeclarations.buildInterTypeAndPerClause
	 */
	public abstract EclipseTypeMunger build(ClassScope classScope);

	public void fixSuperCallsInBody() {
		SuperFixerVisitor v = new SuperFixerVisitor(this, onTypeBinding);
		this.traverse(v, (ClassScope)null);
		munger.setSuperMethodsCalled(v.superMethodsCalled);
	}

	protected void resolveOnType(ClassScope classScope) {
		checkSpec();		
		onTypeBinding = (ReferenceBinding)onType.getTypeBindingPublic(classScope);
		if (!onTypeBinding.isValidBinding()) {
			classScope.problemReporter().invalidType(onType, onTypeBinding);
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
	
	protected void setMunger(ResolvedTypeMunger munger) {
		munger.getSignature().setPosition(sourceStart, sourceEnd);
		munger.getSignature().setSourceContext(new EclipseSourceContext(compilationResult));
		this.munger = munger;
	}
	
	protected int generateInfoAttributes(ClassFile classFile) {
		List l;;
		Shadow.Kind kind = getShadowKindForBody();
		if (kind != null) {
			l = makeEffectiveSignatureAttribute(munger.getSignature(), kind, true);
		} else {
			l = new ArrayList(0);
		}
		addDeclarationStartLineAttribute(l,classFile);

		return classFile.generateMethodInfoAttribute(binding, false, l);
	}

	protected abstract Shadow.Kind getShadowKindForBody();
	
	public ResolvedMember getSignature() { 
		if (munger==null) return null; // Can be null in an erroneous program I think
		return munger.getSignature(); 
	}

	public char[] getDeclaredSelector() {
		return declaredSelector;
	}

}
