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
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;

public class ExactTypePattern extends TypePattern {
	protected TypeX type;

	public ExactTypePattern(TypeX type, boolean includeSubtypes) {
		super(includeSubtypes);
		this.type = type;
	}
	
	protected boolean matchesExactly(ResolvedTypeX matchType) {
		return this.type.equals(matchType);
	}
	
	public TypeX getType() { return type; }

	public FuzzyBoolean matchesInstanceof(ResolvedTypeX matchType) {
		// in our world, Object is assignable from anything
		if (type.equals(ResolvedTypeX.OBJECT)) return FuzzyBoolean.YES;
		
		if (type.isAssignableFrom(matchType, matchType.getWorld())) {
			return FuzzyBoolean.YES;
		}
		
		return matchType.isCoerceableFrom(type) ? FuzzyBoolean.MAYBE : FuzzyBoolean.NO;
	}
	
    public boolean equals(Object other) {
    	if (!(other instanceof ExactTypePattern)) return false;
    	ExactTypePattern o = (ExactTypePattern)other;
    	return o.type.equals(this.type);
    }
    
    public int hashCode() {
        return type.hashCode();
    }
	
	public void write(DataOutputStream out) throws IOException {
		out.writeByte(TypePattern.EXACT);
		type.write(out);
		out.writeBoolean(includeSubtypes);
		writeLocation(out);
	}
	
	public static TypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		TypePattern ret = new ExactTypePattern(TypeX.read(s), s.readBoolean());
		ret.readLocation(context, s);
		return ret;
	}

    public String toString() {
    	//Thread.currentThread().dumpStack();
    	return "ExactTypePattern(" + type.toString() + (includeSubtypes ? "+" : "") + ")";
    }
	public TypePattern resolveBindings(IScope scope, Bindings bindings, 
    								boolean allowBinding, boolean requireExactType)
    { 
		throw new BCException("trying to re-resolve");
		
	}

}
