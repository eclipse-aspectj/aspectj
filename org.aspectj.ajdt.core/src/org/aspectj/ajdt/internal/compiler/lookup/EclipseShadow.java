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

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.Var;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;

/**
 * This is only used for declare soft right now.
 * 
 * It might be used for other compile-time matching, but in all such cases
 * this and target pcds can't be used.  We might not behave correctly in
 * such cases.
 */
public class EclipseShadow extends Shadow {
	EclipseWorld world;
	AstNode astNode;
	//XXXReferenceContext context;
	AbstractMethodDeclaration enclosingMethod;

	public EclipseShadow(EclipseWorld world, Kind kind, Member signature, AstNode astNode, 
							ReferenceContext context)
	{
		super(kind, signature, null);
		this.world = world;
		this.astNode = astNode;
		//XXX can this fail in practice?
		this.enclosingMethod = (AbstractMethodDeclaration)context;
	}

	public World getIWorld() {
		return world;
	}


	public TypeX getEnclosingType() {
		return world.fromBinding(enclosingMethod.binding.declaringClass);
	}
	
	public SourceLocation getSourceLocation() {
		//XXX need to fill this in ASAP
		return null;
	}

	public Member getEnclosingCodeSignature() {
		return world.makeResolvedMember(enclosingMethod.binding);
	}

	// -- all below here are only used for implementing, not in matching
	// -- we should probably pull out a super-interface to capture this in type system

	public Var getThisVar() {
		throw new RuntimeException("unimplemented");
	}

	public Var getTargetVar() {
		throw new RuntimeException("unimplemented");
	}

	public Var getArgVar(int i) {
		throw new RuntimeException("unimplemented");
	}

	public Var getThisJoinPointVar() {
		throw new RuntimeException("unimplemented");
	}

	public Var getThisJoinPointStaticPartVar() {
		throw new RuntimeException("unimplemented");
	}

	public Var getThisEnclosingJoinPointStaticPartVar() {
		throw new RuntimeException("unimplemented");
	}
	
	// --- factory methods
	
	public static EclipseShadow makeShadow(EclipseWorld world, AstNode astNode, 
							ReferenceContext context)
	{
		//XXX make sure we're getting the correct declaring type at call-site
		if (astNode instanceof AllocationExpression) {
			AllocationExpression e = (AllocationExpression)astNode;
			return new EclipseShadow(world, Shadow.ConstructorCall,
					world.makeResolvedMember(e.binding), astNode, context);
		} else if (astNode instanceof MessageSend) {
			MessageSend e = (MessageSend)astNode;
			return new EclipseShadow(world, Shadow.MethodCall,
					world.makeResolvedMember(e.binding), astNode, context);
		} else if (astNode instanceof ExplicitConstructorCall) {
			//??? these need to be ignored, they don't have shadows
			return null;
//			ExplicitConstructorCall e = (ExplicitConstructorCall)astNode;
//			return new EclipseShadow(world, Shadow.MethodCall,
//					world.makeResolvedMember(e.binding), astNode, context);					
		} else if (astNode instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration e = (AbstractMethodDeclaration)astNode;
			Shadow.Kind kind;
			if (e instanceof AdviceDeclaration) {
				kind = Shadow.AdviceExecution;
			} else if (e instanceof InterTypeMethodDeclaration) {
				return new EclipseShadow(world, Shadow.MethodExecution,
					((InterTypeDeclaration)e).getSignature(), astNode, context);
			} else if (e instanceof InterTypeConstructorDeclaration) {
				return new EclipseShadow(world, Shadow.ConstructorExecution,
					((InterTypeDeclaration)e).getSignature(), astNode, context);
			} else if (e instanceof InterTypeFieldDeclaration) {
				return null;
			} else if (e instanceof MethodDeclaration) {
				kind = Shadow.MethodExecution;
			} else if (e instanceof ConstructorDeclaration) {
				kind = Shadow.ConstructorExecution;
			} else {
				throw new RuntimeException("unimplemented: " + e);
			}
			return new EclipseShadow(world, kind,
					world.makeResolvedMember(e.binding), astNode, context);
		} else {
			throw new RuntimeException("unimplemented: " + astNode);
		}		
	}

	
}
