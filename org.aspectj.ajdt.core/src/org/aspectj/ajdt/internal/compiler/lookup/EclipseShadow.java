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
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.Var;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;

/**
 * This is only used for declare soft right now.
 * 
 * It might be used for other compile-time matching, but in all such cases
 * this and target pcds can't be used.  We might not behave correctly in
 * such cases.
 */
public class EclipseShadow extends Shadow {
	EclipseFactory world;
	ASTNode astNode;
	ReferenceContext context;
	//AbstractMethodDeclaration enclosingMethod;

	public EclipseShadow(EclipseFactory world, Kind kind, Member signature, ASTNode astNode, 
							ReferenceContext context)
	{
		super(kind, signature, null);
		this.world = world;
		this.astNode = astNode;
		this.context = context;
	}

	public World getIWorld() {
		return world.getWorld();
	}


	public TypeX getEnclosingType() {
		if (context instanceof TypeDeclaration) {
			return EclipseFactory.fromBinding(((TypeDeclaration)context).binding);
		} else if (context instanceof AbstractMethodDeclaration) {
			return EclipseFactory.fromBinding(((AbstractMethodDeclaration)context).binding.declaringClass);
		} else {
			return ResolvedTypeX.MISSING;
		}
	}
	
	public ISourceLocation getSourceLocation() {
		//XXX need to fill this in ASAP
		return null;
	}

	public Member getEnclosingCodeSignature() {
		if (context instanceof TypeDeclaration) {
			return new Member(Member.STATIC_INITIALIZATION, getEnclosingType(), 0, 
						ResolvedTypeX.VOID, "<clinit>", TypeX.NONE);
		} else if (context instanceof AbstractMethodDeclaration) {
			return EclipseFactory.makeResolvedMember(((AbstractMethodDeclaration)context).binding);
		} else {
			return null;
		}
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
	
	public static EclipseShadow makeShadow(EclipseFactory world, ASTNode astNode, 
							ReferenceContext context)
	{
		//XXX make sure we're getting the correct declaring type at call-site
		if (astNode instanceof AllocationExpression) {
			AllocationExpression e = (AllocationExpression)astNode;
			return new EclipseShadow(world, Shadow.ConstructorCall,
					EclipseFactory.makeResolvedMember(e.binding), astNode, context);
		} else if (astNode instanceof MessageSend) {
			MessageSend e = (MessageSend)astNode;
			if (e.isSuperAccess()) return null;  // super calls don't have shadows
			return new EclipseShadow(world, Shadow.MethodCall,
					EclipseFactory.makeResolvedMember(e.binding), astNode, context);
		} else if (astNode instanceof ExplicitConstructorCall) {
			//??? these should be ignored, they don't have shadows
			return null;				
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
			} else if (e instanceof Clinit) {
				kind = Shadow.StaticInitialization; 
			} else {
				return null;
				//throw new RuntimeException("unimplemented: " + e);
			}
			return new EclipseShadow(world, kind,
					EclipseFactory.makeResolvedMember(e.binding), astNode, context);
		} else if (astNode instanceof TypeDeclaration) {
			return new EclipseShadow(world, Shadow.StaticInitialization,
							new Member(Member.STATIC_INITIALIZATION, 
								EclipseFactory.fromBinding(((TypeDeclaration)astNode).binding), 0, 
								ResolvedTypeX.VOID, "<clinit>", TypeX.NONE),
							astNode, context);
		} else {
			return null;
			//throw new RuntimeException("unimplemented: " + astNode);
		}		
	}

	
}
