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

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.InlineAccessFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeMethodBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedHandler;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ResolvedMember;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Walks the body of around advice
 * 
 * Makes sure that all member accesses are to public members.  Will
 * convert to use access methods when needed to ensure that.  This
 * makes it much simpler (and more modular) to inline the body of
 * an around.
 * 
 * ??? constructors are handled different and require access to the
 * target type.  changes to org.eclipse.jdt.internal.compiler.ast.AllocationExpression
 * would be required to fix this issue.
 * 
 * @author Jim Hugunin
 */

public class AccessForInlineVisitor extends AbstractSyntaxTreeVisitorAdapter {
	PrivilegedHandler handler;
	AspectDeclaration inAspect;
	EclipseFactory world; // alias for inAspect.world
	
	public AccessForInlineVisitor(AspectDeclaration inAspect, PrivilegedHandler handler) {
		this.inAspect = inAspect;
		this.world = inAspect.world;
		this.handler = handler;
	}
	
	
	public void endVisit(SingleNameReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			ref.binding = getAccessibleField((FieldBinding)ref.binding);
		}
	}

	public void endVisit(QualifiedNameReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			ref.binding = getAccessibleField((FieldBinding)ref.binding);
		}
	}

	public void endVisit(FieldReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			ref.binding = getAccessibleField((FieldBinding)ref.binding);
		}
	}
	public void endVisit(MessageSend send, BlockScope scope) {
		if (send instanceof Proceed) return;
		if (send.binding == null) return;
		
		if (send.isSuperAccess() && !send.binding.isStatic()) {
			send.receiver = new ThisReference();
			send.binding = send.codegenBinding = 
				getSuperAccessMethod((MethodBinding)send.binding);
		} else if (!isPublic(send.binding)) {
			send.syntheticAccessor = getAccessibleMethod((MethodBinding)send.binding);
		}
	}
	public void endVisit(AllocationExpression send, BlockScope scope) {
		if (send.binding == null) return;
		//XXX TBD
		if (isPublic(send.binding)) return;
		makePublic(send.binding.declaringClass);
		send.binding = handler.getPrivilegedAccessMethod(send.binding, send);
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
	
	private FieldBinding getAccessibleField(FieldBinding binding) {
		if (!binding.isValidBinding()) return binding;
		
		makePublic(binding.declaringClass);
		if (isPublic(binding)) return binding;
		if (binding instanceof PrivilegedFieldBinding) return binding;
		if (binding instanceof InterTypeFieldBinding) return binding;

		if (binding.isPrivate() &&  binding.declaringClass != inAspect.binding) {
			binding.modifiers = AstUtil.makePackageVisible(binding.modifiers);
		}
		
		ResolvedMember m = world.makeResolvedMember(binding);
		if (inAspect.accessForInline.containsKey(m)) return (FieldBinding)inAspect.accessForInline.get(m);
		FieldBinding ret = new InlineAccessFieldBinding(inAspect, binding);
		inAspect.accessForInline.put(m, ret);
		return ret;
	}
	
	private MethodBinding getAccessibleMethod(MethodBinding binding) {
		if (!binding.isValidBinding()) return binding;
		
		makePublic(binding.declaringClass);  //???
		if (isPublic(binding)) return binding;
		if (binding instanceof InterTypeMethodBinding) return binding;

		if (binding.isPrivate() &&  binding.declaringClass != inAspect.binding) {
			binding.modifiers = AstUtil.makePackageVisible(binding.modifiers);
		}

		
		ResolvedMember m = world.makeResolvedMember(binding);
		if (inAspect.accessForInline.containsKey(m)) return (MethodBinding)inAspect.accessForInline.get(m);
		MethodBinding ret = world.makeMethodBinding(
			AjcMemberMaker.inlineAccessMethodForMethod(inAspect.typeX, m)
			);
		inAspect.accessForInline.put(m, ret);
		return ret;
	}
	
	private MethodBinding getSuperAccessMethod(MethodBinding binding) {
		ResolvedMember m = world.makeResolvedMember(binding);
		if (inAspect.superAccessForInline.containsKey(m)) return (MethodBinding)inAspect.superAccessForInline.get(m);
		MethodBinding ret = world.makeMethodBinding(
			AjcMemberMaker.superAccessMethod(inAspect.typeX, m)
			);
		inAspect.superAccessForInline.put(m, ret);
		return ret;
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
			if (!rb.isPublic()) handler.notePrivilegedTypeAccess(rb, null); //???
		} else if (binding instanceof ArrayBinding) {
			makePublic( ((ArrayBinding)binding).leafComponentType );
		} else {
			return;
		}
	}
}
