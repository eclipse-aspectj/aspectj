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

import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;


public class ThrowsPattern extends PatternNode {
	private TypePatternList required;
	private TypePatternList forbidden;
	
	public static final ThrowsPattern ANY =
		new ThrowsPattern(TypePatternList.EMPTY, TypePatternList.EMPTY);
	
	public ThrowsPattern(TypePatternList required, TypePatternList forbidden) {
		this.required = required;
		this.forbidden = forbidden;
	}

	public String toString() {
		if (this == ANY) return "";
		
		
		String ret = "throws " + required.toString();
		if (forbidden.size() > 0) {
			ret = ret + " !(" + forbidden.toString() + ")";
		}
		return ret;
	}
    
	public boolean equals(Object other) {
		if (!(other instanceof ThrowsPattern)) return false;
		ThrowsPattern o = (ThrowsPattern)other;
		return o.required.equals(this.required) &&
				o.forbidden.equals(this.forbidden);
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + required.hashCode();
        result = 37*result + forbidden.hashCode();
        return result;
    }	
    
    public ThrowsPattern resolveBindings(IScope scope, Bindings bindings) {
    	if (this == ANY) return this;
    	required = required.resolveBindings(scope, bindings, false, false);
    	forbidden = forbidden.resolveBindings(scope, bindings, false, false);
    	return this;
    }
    
	public boolean matches(TypeX[] tys, World world) {
		if (this == ANY) return true;
		
		//System.out.println("matching: " + this + " with " + Arrays.asList(tys));
		
		ResolvedTypeX[] types = world.resolve(tys);
		int len = types.length;
		for (int j=0, lenj = required.size(); j < lenj; j++) {
			if (! matchesAny(required.get(j), types)) {
				return false;
			}
		}
		for (int j=0, lenj = forbidden.size(); j < lenj; j++) {
			if (matchesAny(forbidden.get(j), types)) {
				return false;
			}
		}
		return true;
	}

	private boolean matchesAny(
		TypePattern typePattern,
		ResolvedTypeX[] types) 
	{
		for (int i = types.length - 1; i >= 0; i--) {
			if (typePattern.matchesExactly(types[i])) return true;	
		}
		return false;
	}

	public static ThrowsPattern read(DataInputStream s, ISourceContext context) throws IOException {
		TypePatternList required = TypePatternList.read(s, context);
		TypePatternList forbidden = TypePatternList.read(s, context);
		if (required.size() == 0 && forbidden.size() == 0) return ANY;
		ThrowsPattern ret = new ThrowsPattern(required, forbidden);
		//XXXret.readLocation(context, s);
		return ret;
	}

	public void write(DataOutputStream s) throws IOException {
		required.write(s);
		forbidden.write(s);
		//XXXwriteLocation(s);
	}
}
