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


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.internal.tools.PointcutExpressionImpl;

/**
 * This is a kind of KindedPointcut.  This belongs either in 
 * a hierarchy with it or in a new place to share code
 * with other potential future statement-level pointcuts like
 * synchronized and throws
 */
public class HandlerPointcut extends Pointcut {
	TypePattern exceptionType;

	private static final Set MATCH_KINDS = new HashSet();
	static {
		MATCH_KINDS.add(Shadow.ExceptionHandler);
	}
	
	public HandlerPointcut(TypePattern exceptionType) {
		this.exceptionType = exceptionType;
		this.pointcutKind = HANDLER;
	}

	public Set couldMatchKinds() {
		return MATCH_KINDS;
	}
	
    public FuzzyBoolean fastMatch(FastMatchInfo type) {
    	//??? should be able to do better by finding all referenced types in type
		return FuzzyBoolean.MAYBE;
	}
	
	public FuzzyBoolean fastMatch(Class targetType) {
		return FuzzyBoolean.MAYBE;
	}
	
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if (shadow.getKind() != Shadow.ExceptionHandler) return FuzzyBoolean.NO;
		
		exceptionType.resolve(shadow.getIWorld());
		
		// we know we have exactly one parameter since we're checking an exception handler
		return exceptionType.matches(
				shadow.getSignature().getParameterTypes()[0].resolve(shadow.getIWorld()), 
				TypePattern.STATIC);
	}
	
 	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart jpsp) {
		if (!jp.getKind().equals(JoinPoint.EXCEPTION_HANDLER)) return FuzzyBoolean.NO;
		if (jp.getArgs().length > 0) {
			Object caughtException = jp.getArgs()[0];
			return exceptionType.matches(caughtException,TypePattern.STATIC);
		} else {
			return FuzzyBoolean.NO;
		}
	}
	
 	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		if (args.length > 0) {
			return (exceptionType.matches(args[0],TypePattern.STATIC) == FuzzyBoolean.YES);
		} else return false;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(String joinpointKind, Member member,
			Class thisClass, Class targetClass, Member withinCode) {
		if (!(member instanceof PointcutExpressionImpl.Handler)) {
			return FuzzyBoolean.NO;
		} else {
			Class exceptionClass = ((PointcutExpressionImpl.Handler)member).getHandledExceptionType();
			return exceptionType.matches(exceptionClass,TypePattern.STATIC);
		}
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof HandlerPointcut)) return false;
		HandlerPointcut o = (HandlerPointcut)other;
		return o.exceptionType.equals(this.exceptionType);	}
    
    public int hashCode() {
        int result = 17;
        result = 37*result + exceptionType.hashCode();
        return result;
    }
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("handler(");
		buf.append(exceptionType.toString());
		buf.append(")");
		return buf.toString();
	}
	

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.HANDLER);
		exceptionType.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		HandlerPointcut ret = new HandlerPointcut(TypePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	// XXX note: there is no namebinding in any kinded pointcut.
	// still might want to do something for better error messages
	// We want to do something here to make sure we don't sidestep the parameter
	// list in capturing type identifiers.
	public void resolveBindings(IScope scope, Bindings bindings) {
		exceptionType = exceptionType.resolveBindings(scope, bindings, false, false);
		boolean invalidParameterization = false;
		if (exceptionType.getTypeParameters().size() > 0) invalidParameterization = true ;
		UnresolvedType exactType = exceptionType.getExactType();
		if (exactType != null && exactType.isParameterizedType()) invalidParameterization = true;
		if (invalidParameterization) {
			// no parameterized or generic types for handler
			scope.message(
					MessageUtil.error(WeaverMessages.format(WeaverMessages.HANDLER_PCD_DOESNT_SUPPORT_PARAMETERS),
									getSourceLocation()));
		}
		//XXX add error if exact binding and not an exception
	}
	
	public void resolveBindingsFromRTTI() {
		exceptionType = exceptionType.resolveBindingsFromRTTI(false,false);
	}
	
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	public Pointcut concretize1(ResolvedType inAspect, IntMap bindings) {
		Pointcut ret = new HandlerPointcut(exceptionType);
		ret.copyLocationFrom(this);
		return ret;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
