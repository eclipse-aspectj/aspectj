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

public class WithincodePointcut extends Pointcut {
	SignaturePattern signature;
	
	public WithincodePointcut(SignaturePattern signature) {
		this.signature = signature;
	}
    
	public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		return FuzzyBoolean.MAYBE;
	}
    
	public FuzzyBoolean match(Shadow shadow) {
		//This will not match code in local or anonymous classes as if
		//they were withincode of the outer signature
		return FuzzyBoolean.fromBoolean(
			signature.matches(shadow.getEnclosingCodeSignature(), shadow.getIWorld()));
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.WITHINCODE);
		signature.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		WithincodePointcut ret = new WithincodePointcut(SignaturePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		signature = signature.resolveBindings(scope, bindings);
	}

	public void postRead(ResolvedTypeX enclosingType) {
		signature.postRead(enclosingType);
	}

	public boolean equals(Object other) {
		if (!(other instanceof WithincodePointcut)) return false;
		WithincodePointcut o = (WithincodePointcut)other;
		return o.signature.equals(this.signature);
	}
    public int hashCode() {
        int result = 43;
        result = 37*result + signature.hashCode();
        return result;
    }

	public String toString() {
		return "withincode(" + signature + ")";
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		return new WithincodePointcut(signature);
	}
}
