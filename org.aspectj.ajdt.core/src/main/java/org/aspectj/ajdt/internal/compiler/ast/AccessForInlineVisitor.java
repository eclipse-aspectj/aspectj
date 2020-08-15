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

//import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.InlineAccessFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeMethodBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedHandler;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ResolvedMember;

/**
 * Walks the body of around advice
 * 
 * Makes sure that all member accesses are to public members. Will convert to use access methods when needed to ensure that. This
 * makes it much simpler (and more modular) to inline the body of an around.
 * 
 * ??? constructors are handled different and require access to the target type. changes to
 * org.eclipse.jdt.internal.compiler.ast.AllocationExpression would be required to fix this issue.
 * 
 * @author Jim Hugunin
 */

public class AccessForInlineVisitor extends ASTVisitor {
	PrivilegedHandler handler;
	AspectDeclaration inAspect;
	EclipseFactory world; // alias for inAspect.world
	private Map<TypeBinding, Map<FieldBinding, ResolvedMember>> alreadyProcessedReceivers = new HashMap<>();

	// set to true for ClassLiteralAccess and AssertStatement
	// ??? A better answer would be to transform these into inlinable forms
	public boolean isInlinable = true;

	public AccessForInlineVisitor(AspectDeclaration inAspect, PrivilegedHandler handler) {
		this.inAspect = inAspect;
		this.world = inAspect.factory;
		this.handler = handler;
	}

	public void endVisit(SingleNameReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			ref.binding = getAccessibleField((FieldBinding) ref.binding, ref.actualReceiverType);
		}
	}

	public void endVisit(QualifiedNameReference ref, BlockScope scope) {
		if (ref.binding instanceof FieldBinding) {
			ref.binding = getAccessibleField((FieldBinding) ref.binding, ref.actualReceiverType);
		}
		if (ref.otherBindings != null && ref.otherBindings.length > 0) {
			TypeBinding receiverType;
			if (ref.binding instanceof FieldBinding) {
				receiverType = ((FieldBinding) ref.binding).type;
			} else if (ref.binding instanceof VariableBinding) {
				receiverType = ((VariableBinding) ref.binding).type;
			} else {
				// !!! understand and fix this case later
				receiverType = ref.otherBindings[0].declaringClass;
			}
			boolean cont = true; // don't continue if we come across a problem
			for (int i = 0, len = ref.otherBindings.length; i < len && cont; i++) {
				FieldBinding binding = ref.otherBindings[i];
				ref.otherBindings[i] = getAccessibleField(binding, receiverType);
				if (!(binding instanceof ProblemFieldBinding) && binding != null)
					receiverType = binding.type; // TODO Why is this sometimes null?
				else
					cont = false;
			}
		}
	}

	public void endVisit(FieldReference ref, BlockScope scope) {
		ref.binding = getAccessibleField(ref.binding, ref.actualReceiverType);
	}

	public void endVisit(MessageSend send, BlockScope scope) {
		if (send instanceof Proceed)
			return;
		if (send.binding == null || !send.binding.isValidBinding())
			return;

		if (send.isSuperAccess() && !send.binding.isStatic()) {
			send.receiver = new ThisReference(send.sourceStart, send.sourceEnd);
			// send.arguments = AstUtil.insert(new ThisReference(send.sourceStart, send.sourceEnd), send.arguments);
			MethodBinding superAccessBinding = getSuperAccessMethod(send.binding);
			AstUtil.replaceMethodBinding(send, superAccessBinding);
		} else if (!isPublic(send.binding) && !isCloneMethod(send.binding)) {
			send.syntheticAccessor = getAccessibleMethod(send.binding, send.actualReceiverType);
		}

	}
	
	private boolean isCloneMethod(MethodBinding binding) {
		return (CharOperation.equals(binding.selector, TypeConstants.CLONE)) &&
				(CharOperation.equals(binding.declaringClass.compoundName, TypeConstants.JAVA_LANG_OBJECT));
	}

	public void endVisit(AllocationExpression send, BlockScope scope) {
		if (send.binding == null || !send.binding.isValidBinding())
			return;
		// XXX TBD
		if (isPublic(send.binding))
			return;
		makePublic(send.binding.declaringClass);
		send.binding = handler.getPrivilegedAccessMethod(send.binding, send);
	}

	public void endVisit(QualifiedTypeReference ref, BlockScope scope) {
		makePublic(ref.resolvedType); // getTypeBinding(scope)); //??? might be trouble
	}

	public void endVisit(SingleTypeReference ref, BlockScope scope) {
		makePublic(ref.resolvedType); // getTypeBinding(scope)); //??? might be trouble
	}

	private FieldBinding getAccessibleField(FieldBinding binding, TypeBinding receiverType) {
		// System.err.println("checking field: " + binding);
		if (binding == null || !binding.isValidBinding())
			return binding;

		makePublic(receiverType);
		if (isPublic(binding))
			return binding;
		if (binding instanceof PrivilegedFieldBinding)
			return binding;
		if (binding instanceof InterTypeFieldBinding)
			return binding;

		if (binding.isPrivate() && binding.declaringClass != inAspect.binding) {
			binding.modifiers = AstUtil.makePackageVisible(binding.modifiers);
		}

		// Avoid repeatedly building ResolvedMembers by using info on any done previously in this visitor
		Map<FieldBinding, ResolvedMember> alreadyResolvedMembers = alreadyProcessedReceivers.computeIfAbsent(receiverType, k -> new HashMap<>());
		ResolvedMember m = alreadyResolvedMembers.get(binding);
		if (m == null) {
			m = world.makeResolvedMember(binding, receiverType);
			alreadyResolvedMembers.put(binding, m);
		}

		if (inAspect.accessForInline.containsKey(m)) {
			return (FieldBinding) inAspect.accessForInline.get(m);
		}
		FieldBinding ret = new InlineAccessFieldBinding(inAspect, binding, m);
		inAspect.accessForInline.put(m, ret);
		return ret;
	}

	private MethodBinding getAccessibleMethod(MethodBinding binding, TypeBinding receiverType) {
		if (!binding.isValidBinding())
			return binding;

		makePublic(receiverType); // ???
		if (isPublic(binding))
			return binding;
		if (binding instanceof InterTypeMethodBinding)
			return binding;

		if (binding instanceof ParameterizedMethodBinding) { // pr124999
			binding = binding.original();
		}

		ResolvedMember m = null;
		if (binding.isPrivate() && binding.declaringClass != inAspect.binding) {
			// does this always mean that the aspect is an inner aspect of the bindings
			// declaring class? After all, the field is private but we can see it from
			// where we are.
			binding.modifiers = AstUtil.makePackageVisible(binding.modifiers);
			m = world.makeResolvedMember(binding);
		} else {
			// Sometimes receiverType and binding.declaringClass are *not* the same.

			// Sometimes receiverType is a subclass of binding.declaringClass. In these situations
			// we want the generated inline accessor to call the method on the subclass (at
			// runtime this will be satisfied by the super).
			m = world.makeResolvedMember(binding, receiverType);
		}
		if (inAspect.accessForInline.containsKey(m))
			return (MethodBinding) inAspect.accessForInline.get(m);
		MethodBinding ret = world.makeMethodBinding(AjcMemberMaker.inlineAccessMethodForMethod(inAspect.typeX, m));
		inAspect.accessForInline.put(m, ret);
		return ret;
	}

	static class SuperAccessMethodPair {
		public ResolvedMember originalMethod;
		public MethodBinding accessMethod;

		public SuperAccessMethodPair(ResolvedMember originalMethod, MethodBinding accessMethod) {
			this.originalMethod = originalMethod;
			this.accessMethod = accessMethod;
		}
	}

	private MethodBinding getSuperAccessMethod(MethodBinding binding) {
		ResolvedMember m = world.makeResolvedMember(binding);
		ResolvedMember superAccessMember = AjcMemberMaker.superAccessMethod(inAspect.typeX, m);
		if (inAspect.superAccessForInline.containsKey(superAccessMember)) {
			return ((SuperAccessMethodPair) inAspect.superAccessForInline.get(superAccessMember)).accessMethod;
		}
		MethodBinding ret = world.makeMethodBinding(superAccessMember);
		inAspect.superAccessForInline.put(superAccessMember, new SuperAccessMethodPair(m, ret));
		return ret;
	}

	private boolean isPublic(FieldBinding fieldBinding) {
		// these are always effectively public to the inliner
		if (fieldBinding instanceof InterTypeFieldBinding)
			return true;
		return fieldBinding.isPublic();
	}

	private boolean isPublic(MethodBinding methodBinding) {
		// these are always effectively public to the inliner
		if (methodBinding instanceof InterTypeMethodBinding)
			return true;
		return methodBinding.isPublic();
	}

	private void makePublic(TypeBinding binding) {
		if (binding == null || !binding.isValidBinding())
			return; // has already produced an error
		if (binding instanceof ReferenceBinding) {
			ReferenceBinding rb = (ReferenceBinding) binding;
			if (!rb.isPublic()) {
				try {
					if (rb instanceof ParameterizedTypeBinding) {
						rb = (ReferenceBinding) rb.erasure();
					}
				} catch (Throwable t) { // TODO remove post 1.7.0
					t.printStackTrace();
				}
				handler.notePrivilegedTypeAccess(rb, null); // ???
			}
		} else if (binding instanceof ArrayBinding) {
			makePublic(((ArrayBinding) binding).leafComponentType);
		} else {
			return;
		}
	}

	public void endVisit(AssertStatement assertStatement, BlockScope scope) {
		isInlinable = false;
	}

	public void endVisit(ClassLiteralAccess classLiteral, BlockScope scope) {
		isInlinable = false;
	}

	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		// we don't want to transform any local anonymous classes as they won't be inlined
		return false;
	}

}
