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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

/**
 * This is a kind of KindedPointcut.  This belongs either in 
 * a hierarchy with it or in a new place to share code
 * with other potential future statement-level pointcuts like
 * synchronized and throws
 */
public class HandlerPointcut extends Pointcut {
	TypePattern exceptionType;

	public HandlerPointcut(TypePattern exceptionType) {
		this.exceptionType = exceptionType;
	}

    public FuzzyBoolean fastMatch(ResolvedTypeX type) {
    	//??? should be able to do better by finding all referenced types in type
		return FuzzyBoolean.MAYBE;
	}
	
	public FuzzyBoolean match(Shadow shadow) {
		if (shadow.getKind() != Shadow.ExceptionHandler) return FuzzyBoolean.NO;
		
		// we know we have exactly one parameter since we're checking an exception handler
		return exceptionType.matches(
				shadow.getSignature().getParameterTypes()[0].resolve(shadow.getIWorld()), 
				TypePattern.STATIC);
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
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
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
		//XXX add error if exact binding and not an exception
	}
	public Test findResidue(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		return new HandlerPointcut(exceptionType);
	}
}
