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
import org.aspectj.weaver.ast.Test;

public class OrPointcut extends Pointcut {
	private Pointcut left, right;

	public OrPointcut(Pointcut left, Pointcut right) {
		super();
		this.left = left;
		this.right = right;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
	}


	public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		return left.fastMatch(type).or(right.fastMatch(type));
	}

	public FuzzyBoolean match(Shadow shadow) {
		return left.match(shadow).or(right.match(shadow));
	}
	
	public String toString() {
		return "(" + left.toString() + " || " + right.toString() + ")";
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof OrPointcut)) return false;
		OrPointcut o = (OrPointcut)other;
		return o.left.equals(left) && o.right.equals(right);
	}
    public int hashCode() {
        int result = 31;
        result = 37*result + left.hashCode();
        result = 37*result + right.hashCode();
        return result;
    }
	/**
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(IScope, Bindings)
	 */
	public void resolveBindings(IScope scope, Bindings bindings) {
		Bindings old = bindings == null ? null : bindings.copy();
		
		left.resolveBindings(scope, bindings);
		right.resolveBindings(scope, old);
		if (bindings != null) bindings.checkEquals(old, scope);
		
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.OR);
		left.write(s);
		right.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		OrPointcut ret = new OrPointcut(Pointcut.read(s, context), Pointcut.read(s, context));
		ret.readLocation(context, s);
		return ret;
	
	}
	public Test findResidue(Shadow shadow, ExposedState state) {
		return Test.makeOr(left.findResidue(shadow, state), right.findResidue(shadow, state));
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		return new OrPointcut(left.concretize1(inAspect, bindings),
								right.concretize1(inAspect, bindings));
	}
}
