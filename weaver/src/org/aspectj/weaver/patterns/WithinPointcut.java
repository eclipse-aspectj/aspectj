/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.*;

import org.aspectj.weaver.*;
import org.aspectj.weaver.ast.*;
import org.aspectj.util.*;

public class WithinPointcut extends Pointcut {
	TypePattern type;
	
	public WithinPointcut(TypePattern type) {
		this.type = type;
	}
    
	public FuzzyBoolean match(Shadow shadow) {
		TypeX enclosingType = shadow.getEnclosingType();
		while (enclosingType != null) {
			if (type.matchesStatically(shadow.getIWorld().resolve(enclosingType))) {
				return FuzzyBoolean.YES;
			}
			enclosingType = enclosingType.getDeclaringType();
		}
		return FuzzyBoolean.NO;
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.WITHIN);
		type.write(s);
		writeLocation(s);
	}
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		TypePattern type = TypePattern.read(s, context);
		WithinPointcut ret = new WithinPointcut(type);
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		type = type.resolveBindings(scope, bindings, false);
	}

	public void postRead(ResolvedTypeX enclosingType) {
		type.postRead(enclosingType);
	}

	public boolean equals(Object other) {
		if (!(other instanceof WithinPointcut)) return false;
		WithinPointcut o = (WithinPointcut)other;
		return o.type.equals(this.type);
	}
    public int hashCode() {
        int result = 43;
        result = 37*result + type.hashCode();
        return result;
    }

	public String toString() {
		return "within(" + type + ")";
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		return this; //??? no pointers out of here so we're okay
	}
}
