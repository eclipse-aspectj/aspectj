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

import java.util.Arrays;

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedHandler;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ShadowMunger;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Takes a method that already has the three extra parameters
 * thisJoinPointStaticPart, thisJoinPoint and thisEnclosingJoinPointStaticPart
 */

public class AccessForInlineVisitor extends AbstractSyntaxTreeVisitorAdapter {
	PrivilegedHandler handler;
	EclipseWorld world;
	public AccessForInlineVisitor(EclipseWorld world, PrivilegedHandler handler) {
		this.world = world;
		this.handler = handler;
	}
	
	public void endVisit(SingleNameReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding)ref.binding;
			makePublic(fieldBinding.declaringClass);
			if (isPublic(fieldBinding)) return;
			ref.binding = handler.getPrivilegedAccessField(fieldBinding);
		}
	}

	public void endVisit(QualifiedNameReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding)ref.binding;
			makePublic(fieldBinding.declaringClass);
			if (isPublic(fieldBinding)) return;
			ref.binding = handler.getPrivilegedAccessField(fieldBinding);
		}
	}

	public void endVisit(FieldReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding)ref.binding;
			makePublic(fieldBinding.declaringClass);
			if (isPublic(fieldBinding)) return;
			ref.binding = handler.getPrivilegedAccessField(fieldBinding);
		}
	}
	public void endVisit(MessageSend send, BlockScope scope) {
		if (send instanceof Proceed) return;
		if (send.binding == null) return;
		if (isPublic(send.binding)) return;
		makePublic(send.binding.declaringClass);
		send.binding = send.codegenBinding = handler.getPrivilegedAccessMethod(send.binding);
	}
	public void endVisit(AllocationExpression send, BlockScope scope) {
		if (send.binding == null) return;
		if (isPublic(send.binding)) return;
		makePublic(send.binding.declaringClass);
		send.binding = handler.getPrivilegedAccessMethod(send.binding);
	}	
	public void endVisit(
		QualifiedTypeReference ref,
		BlockScope scope)
	{
		makePublic(ref.binding);
	}
	
	public void endVisit(
		SingleTypeReference ref,
		BlockScope scope)
	{
		makePublic(ref.binding);
	}
	
	private boolean isPublic(FieldBinding fieldBinding) {
		// these are always effectively public to the inliner
		if (fieldBinding instanceof InterTypeFieldBinding) return true;
		return fieldBinding.isPublic();
	}

	private boolean isPublic(MethodBinding methodBinding) {
		// these are always effectively public to the inliner
		if (methodBinding instanceof InterTypeMethodBinding) return true;
		return methodBinding.isPublic();
	}

	private void makePublic(TypeBinding binding) {
		if (binding instanceof ReferenceBinding) {
			ReferenceBinding rb = (ReferenceBinding)binding;
			if (!rb.isPublic()) handler.notePrivilegedTypeAccess(rb);
		} else if (binding instanceof ArrayBinding) {
			makePublic( ((ArrayBinding)binding).leafComponentType );
		} else {
			return;
		}
	}
}
