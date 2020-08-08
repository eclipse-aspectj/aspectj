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

package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeMethodBinding;
import org.aspectj.asm.internal.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;

/**
 * Takes a method that already has the three extra parameters thisJoinPointStaticPart, thisJoinPoint and
 * thisEnclosingJoinPointStaticPart
 */

public class SuperFixerVisitor extends ASTVisitor {
	Set superMethodsCalled = new HashSet();
	AbstractMethodDeclaration method;
	ReferenceBinding targetClass;
	private int depthCounter = 0; // Keeps track of whether we are inside any nested local type declarations

	SuperFixerVisitor(AbstractMethodDeclaration method, ReferenceBinding targetClass) {
		this.method = method;
		this.targetClass = targetClass;
	}
	
	private static final char[] ctor = "<init>".toCharArray();

	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		if (localTypeDeclaration.binding instanceof LocalTypeBinding) {
			if (((LocalTypeBinding)localTypeDeclaration.binding).isAnonymousType()) {
				localTypeDeclaration.binding.modifiers |=Modifier.PUBLIC;
				MethodBinding[] bindings = localTypeDeclaration.binding.methods;
				if (bindings!=null) {
					for (MethodBinding binding : bindings) {
						if (CharOperation.equals(binding.selector, ctor)) {
							binding.modifiers |= Modifier.PUBLIC;
						}
					}
				}
//				localTypeDeclaration.modifiers|=Modifier.PUBLIC;
			}
		}
		depthCounter++;
		return super.visit(localTypeDeclaration, scope);
	}

	public void endVisit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		depthCounter--;
	}

	public void endVisit(MessageSend call, BlockScope scope) {
		// System.out.println("endVisit: " + call);
		// an error has already occurred
		if (call.binding/*codegenBinding*/ == null)
			return;

		MethodBinding superBinding = call.binding/*codegenBinding*/;
		if (superBinding instanceof ProblemMethodBinding) {
			return;
		}
		// InterTypeMethodBindings are always statically bound, so there's no
		// need to treat super calls specially here
		if (superBinding instanceof InterTypeMethodBinding) {
			return;
			// InterTypeMethodBinding m = (InterTypeMethodBinding)superBinding;
			// if (m.postDispatchMethod != null) {
			// call.binding = m.postDispatchMethod;
			// }
			// return;
		}
		if (superBinding instanceof ParameterizedMethodBinding) {
			superBinding = ((ParameterizedMethodBinding) superBinding).original();
		}
		EclipseFactory factory = ((AjLookupEnvironment) method.scope.environment()).factory;
		if (depthCounter != 0 && targetClass.isInterface()) {// pr198196 - when calling MarkerInterface.super.XXX()
			if (call.isSuperAccess() && !call.binding.isStatic()) {
				MethodScope currentMethodScope = scope.methodScope();
				SourceTypeBinding sourceType = currentMethodScope.enclosingSourceType();
				FieldBinding field = sourceType.addSyntheticFieldForInnerclass(targetClass);
				call.receiver = new KnownFieldReference(field, call.receiver.sourceStart, call.receiver.sourceEnd);
			} else {
				return;
			}
		} else if (depthCounter == 0) { // Allow case testSuperItds_pr198196_2/3

			char[] accessName;
			if (call.isSuperAccess() && !call.binding.isStatic()) {
				call.receiver = new ThisReference(call.receiver.sourceStart, call.receiver.sourceEnd);
				accessName = NameMangler.superDispatchMethod(factory.fromBinding(targetClass), new String(superBinding.selector))
						.toCharArray();
			} else if (call.receiver.isThis() && call.binding.isProtected() && !call.binding.isStatic()) {
				// XXX this is a hack that violates some binary compatibility rules
				ReferenceBinding superBindingDeclaringClass = superBinding.declaringClass;
				if (superBindingDeclaringClass.isParameterizedType()) {
					superBindingDeclaringClass = ((ParameterizedTypeBinding) superBindingDeclaringClass).type;
				}
				if (superBindingDeclaringClass.equals(targetClass)) {
					accessName = NameMangler.protectedDispatchMethod(factory.fromBinding(targetClass),
							new String(superBinding.selector)).toCharArray();
				} else {
					accessName = NameMangler.superDispatchMethod(factory.fromBinding(targetClass),
							new String(superBinding.selector)).toCharArray();
				}
			} else {
				return;
			}

			// ??? do we want these to be unique
			MethodBinding superAccessBinding = new MethodBinding(ClassFileConstants.AccPublic, accessName, superBinding.returnType,
					superBinding.parameters, superBinding.thrownExceptions, targetClass);

			AstUtil.replaceMethodBinding(call, superAccessBinding);
		} else {
			return;
		}
		ResolvedMember targetMember = null;
		if (superBinding.declaringClass.isParameterizedType()) { // pr206911
			targetMember = factory.makeResolvedMember(superBinding, ((ParameterizedTypeBinding) superBinding.declaringClass)
					.genericType());
		} else {
			targetMember = factory.makeResolvedMember(superBinding);
		}
		superMethodsCalled.add(targetMember);
	}
}
