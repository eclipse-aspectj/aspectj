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

import org.aspectj.weaver.Advice;
import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Takes a method that already has the three extra parameters
 * thisJoinPointStaticPart, thisJoinPoint and thisEnclosingJoinPointStaticPart
 */

public class ProceedVisitor extends ASTVisitor {
	boolean needsDynamic = false;
	boolean needsStatic = false;
	boolean needsStaticEnclosing = false;
	boolean hasEffectivelyStaticRef = false;

	LocalVariableBinding thisJoinPointDec;
	LocalVariableBinding thisJoinPointStaticPartDec;
	LocalVariableBinding thisEnclosingJoinPointStaticPartDec;
	
	LocalVariableBinding thisJoinPointDecLocal;
	LocalVariableBinding thisJoinPointStaticPartDecLocal;
	LocalVariableBinding thisEnclosingJoinPointStaticPartDecLocal;

	boolean replaceEffectivelyStaticRefs = false;
	
	AbstractMethodDeclaration method;

	ProceedVisitor(AbstractMethodDeclaration method) {
		this.method = method;
		int index = method.arguments.length - 3;
		
		thisJoinPointStaticPartDecLocal = method.scope.locals[index];
		thisJoinPointStaticPartDec = method.arguments[index++].binding;
		thisJoinPointDecLocal = method.scope.locals[index];
		thisJoinPointDec = method.arguments[index++].binding;
		thisEnclosingJoinPointStaticPartDecLocal = method.scope.locals[index];
		thisEnclosingJoinPointStaticPartDec = method.arguments[index++].binding;
	}

	public void computeJoinPointParams() {
		// walk my body to see what is needed
		method.traverse(this, (ClassScope) null);

		//??? add support for option to disable this optimization
		//System.err.println("check:  "+ hasEffectivelyStaticRef + ", " + needsDynamic);
		if (hasEffectivelyStaticRef && !needsDynamic) {
			// replace effectively static refs with thisJoinPointStaticPart
			replaceEffectivelyStaticRefs = true;
			needsStatic = true;
			method.traverse(this, (ClassScope) null);
		}
	}

	boolean isRef(NameReference ref, Binding binding) {
		return ref.binding == binding;
	}

	boolean isRef(Expression expr, Binding binding) {
		//System.err.println("isRef: " + expr + ", " + binding);
		return expr != null
			&& expr instanceof NameReference
			&& isRef((NameReference) expr, binding);
	}

	public void endVisit(SingleNameReference ref, BlockScope scope) {
		if (isRef(ref, thisJoinPointDec))
			needsDynamic = true;
		else if (isRef(ref, thisJoinPointStaticPartDec))
			needsStatic = true;
		else if (isRef(ref, thisEnclosingJoinPointStaticPartDec))
			needsStaticEnclosing = true;
	}

	//        public void checkAndFix(ASTObject body) {
	//            this.process(body);
	//            if (needsFakeStatic && !needsDynamic) {
	//                if (!this.getCompiler().getOptions().noMetaJoinPointOptimization) {
	//                    makeFakeStatics = true;
	//                    needsStatic = true;
	//                    this.process(body);
	//                } else {
	//                    needsDynamic = true;
	//                }
	//            }
	//        }

	boolean canTreatAsStatic(String id) {
		return id.equals("toString")
			|| id.equals("toShortString")
			|| id.equals("toLongString")
			|| id.equals("getKind")
			|| id.equals("getSignature")
			|| id.equals("getSourceLocation")
			|| id.equals("getStaticPart");
	}

	//        boolean canTreatAsStatic(VarExpr varExpr) {
	//            ASTObject parent = varExpr.getParent();
	//            if (parent instanceof CallExpr) {
	//                Method calledMethod = ((CallExpr)parent).getMethod();
	//                return canTreatAsStatic(calledMethod);
	//
	//                //??? should add a case here to catch
	//                //??? tjp.getEnclosingExecutionJoinPoint().STATIC_METHOD()
	//            } else if (parent instanceof BinopExpr) {
	//                BinopExpr binop = (BinopExpr)parent;
	//                if (binop.getType().isEquivalent(this.getTypeManager().getStringType())) {
	//                    return true;
	//                } else {
	//                    return false;
	//                }
	//            } else {
	//                return false;
	//            }
	//        }

	boolean inBlockThatCantRun = false;

	public boolean visit(MessageSend call, BlockScope scope) {
		Expression receiver = call.receiver;
		if (isRef(receiver, thisJoinPointDec)) {
			if (canTreatAsStatic(new String(call.selector))) {
				if (replaceEffectivelyStaticRefs) {
					replaceEffectivelyStaticRef(call);
				} else {
					//System.err.println("has static reg");
					hasEffectivelyStaticRef = true;
					if (call.arguments != null) {
						int argumentsLength = call.arguments.length;
						for (int i = 0; i < argumentsLength; i++)
							call.arguments[i].traverse(this, scope);
					}
					return false;
				}
			}
		}

		return super.visit(call, scope);
	}

	private void replaceEffectivelyStaticRef(MessageSend call) {
		//System.err.println("replace static ref");
		NameReference receiver = (NameReference) call.receiver;
		receiver.binding = thisJoinPointStaticPartDecLocal; //thisJoinPointStaticPartDec;
//		receiver.codegenBinding = thisJoinPointStaticPartDecLocal;

		call.binding.declaringClass =
			(ReferenceBinding) thisJoinPointStaticPartDec.type;
	}

	public int removeUnusedExtraArguments() {
		int extraArgumentFlags = 0;
		
		this.computeJoinPointParams();
		MethodBinding binding = method.binding;
		
		
		int index = binding.parameters.length - 3;
		if (needsStaticEnclosing) {
			extraArgumentFlags |= Advice.ThisEnclosingJoinPointStaticPart;
		} else {
			removeParameter(index+2);
		}
		
		if (needsDynamic) {
			extraArgumentFlags |= Advice.ThisJoinPoint;
		} else {
			removeParameter(index+1);
		}
		
		if (needsStatic) {
			extraArgumentFlags |= Advice.ThisJoinPointStaticPart;
		} else {
			removeParameter(index+0);
		}
		
		return extraArgumentFlags;
	}

	private void removeParameter(int indexToRemove) {
//		TypeBinding[] parameters = method.binding.parameters;
		method.scope.locals = removeLocalBinding(indexToRemove, method.scope.locals);
		method.binding.parameters = removeParameter(indexToRemove, method.binding.parameters);
	}

	
	private static TypeBinding[] removeParameter(int index, TypeBinding[] bindings) {
		int len = bindings.length;
		TypeBinding[] ret = new TypeBinding[len-1];
		System.arraycopy(bindings, 0, ret, 0, index);
		System.arraycopy(bindings, index+1, ret, index, len-index-1);
		return ret;
	}

	private static LocalVariableBinding[] removeLocalBinding(int index, LocalVariableBinding[] bindings) {
		int len = bindings.length;
		//??? for performance we should do this in-place
		LocalVariableBinding[] ret = new LocalVariableBinding[len-1];
		System.arraycopy(bindings, 0, ret, 0, index);
		System.arraycopy(bindings, index+1, ret, index, len-index-1);
		return ret;
	}


}
