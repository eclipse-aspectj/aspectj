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

import java.io.*;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class KindedPointcut extends Pointcut {
	Shadow.Kind kind;
	SignaturePattern signature;

	public KindedPointcut(
		Shadow.Kind kind,
		SignaturePattern signature) {
		this.kind = kind;
		this.signature = signature;
	}
	
    public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		return FuzzyBoolean.MAYBE;
	}	
	public FuzzyBoolean match(Shadow shadow) {
		if (shadow.getKind() != kind) return FuzzyBoolean.NO;
		
		if (!signature.matches(shadow.getSignature(), shadow.getIWorld())) return  FuzzyBoolean.NO;

		return  FuzzyBoolean.YES;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof KindedPointcut)) return false;
		KindedPointcut o = (KindedPointcut)other;
		return o.kind == this.kind && o.signature.equals(this.signature);
	}
    
    public int hashCode() {
        int result = 17;
        result = 37*result + kind.hashCode();
        result = 37*result + signature.hashCode();
        return result;
    }
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(kind.getSimpleName());
		buf.append("(");
		buf.append(signature.toString());
		buf.append(")");
		return buf.toString();
	}
	
	
	public void postRead(ResolvedTypeX enclosingType) {
		signature.postRead(enclosingType);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.KINDED);
		kind.write(s);
		signature.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		Shadow.Kind kind = Shadow.Kind.read(s);
		SignaturePattern sig = SignaturePattern.read(s, context);
		KindedPointcut ret = new KindedPointcut(kind, sig);
		ret.readLocation(context, s);
		return ret;
	}

	// XXX note: there is no namebinding in any kinded pointcut.
	// still might want to do something for better error messages
	// We want to do something here to make sure we don't sidestep the parameter
	// list in capturing type identifiers.
	public void resolveBindings(IScope scope, Bindings bindings) {
		if (kind == Shadow.Initialization) {
//			scope.getMessageHandler().handleMessage(
//				MessageUtil.error(
//					"initialization unimplemented in 1.1beta1",
//					this.getSourceLocation()));
		}
		signature = signature.resolveBindings(scope, bindings);
	}
	public Test findResidue(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		return new KindedPointcut(kind, signature);
	}

	public Shadow.Kind getKind() {
		return kind;
	}

}
