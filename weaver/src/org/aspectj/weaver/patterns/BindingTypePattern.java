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
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.TypeX;

public class BindingTypePattern extends ExactTypePattern {
	private int formalIndex;

	public BindingTypePattern(TypeX type, int index) {
		super(type, false);
		this.formalIndex = index;
	}

	public BindingTypePattern(FormalBinding binding) {
		this(binding.getType(), binding.getIndex());
	}
	
	public int getFormalIndex() {
		return formalIndex;
	}

    public boolean equals(Object other) {
    	if (!(other instanceof BindingTypePattern)) return false;
    	BindingTypePattern o = (BindingTypePattern)other;
    	return o.type.equals(this.type) && o.formalIndex == this.formalIndex;
    }
    public int hashCode() {
        int result = 17;
        result = 37*result + type.hashCode();
        result = 37*result + formalIndex;
        return result;
    }
	
	public void write(DataOutputStream out) throws IOException {
		out.writeByte(TypePattern.BINDING);
		type.write(out);
		out.writeShort((short)formalIndex);
		writeLocation(out);
	}
	
	public static TypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		TypePattern ret = new BindingTypePattern(TypeX.read(s), s.readShort());
		ret.readLocation(context, s);
		return ret;
	}
	
	public TypePattern remapAdviceFormals(IntMap bindings) {			
		if (!bindings.hasKey(formalIndex)) {
			return new ExactTypePattern(type, false);
		} else {
			int newFormalIndex = bindings.get(formalIndex);
			return new BindingTypePattern(type, newFormalIndex);
		}
	}

    public String toString() {
    	//Thread.currentThread().dumpStack();
    	return "BindingTypePattern(" + type.toString() + ", " + formalIndex + ")";
    }
}
