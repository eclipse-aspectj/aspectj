/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.weaver.VersionedDataInputStream;

public class ModifiersPattern extends PatternNode {
	private int requiredModifiers;
	private int forbiddenModifiers;
	
	public static final int TRIVIAL = 0x4000;
	
	public static final ModifiersPattern ANY = new ModifiersPattern(0, 0);
	
	public ModifiersPattern(int requiredModifiers, int forbiddenModifiers) {
		this.requiredModifiers = requiredModifiers;
		this.forbiddenModifiers = forbiddenModifiers;
	}

	public String toString() {
		if (this == ANY) return "";
		
		String ret = Modifier.toString(requiredModifiers);
		if (forbiddenModifiers == 0) return ret;
		else return ret + " !" + Modifier.toString(forbiddenModifiers);
	}
    
	public boolean equals(Object other) {
		if (!(other instanceof ModifiersPattern)) return false;
		ModifiersPattern o = (ModifiersPattern)other;
		return o.requiredModifiers == this.requiredModifiers &&
				o.forbiddenModifiers == this.forbiddenModifiers;
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + requiredModifiers;
        result = 37*result + forbiddenModifiers;
        return result;
    }	
    
	public boolean matches(int modifiers) {
		// Comparison here is based on 'real' Java modifiers
		return (((modifiers & requiredModifiers)&0xfff) == (requiredModifiers&0xfff)) &&
		        (((modifiers & forbiddenModifiers)&0xfff) == 0);
	}
	

	public static ModifiersPattern read(VersionedDataInputStream s) throws IOException {
		int requiredModifiers = s.readShort();
		int forbiddenModifiers = s.readShort();
		if (requiredModifiers == 0 && forbiddenModifiers == 0) return ANY;
		return new ModifiersPattern(requiredModifiers, forbiddenModifiers);
	}

	/**
	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		//s.writeByte(MODIFIERS_PATTERN);
		s.writeShort(requiredModifiers);
		s.writeShort(forbiddenModifiers);
	}
	
	
	private static Map modifierFlags = null;
	
	public static int getModifierFlag(String name) {
		return getModifierFlag(name,false);
	}

	public static int getModifierFlag(String name,boolean allowTrivial) {
		if (modifierFlags == null) {
			modifierFlags = new HashMap();
			int flag = 1;
			while (flag <= Modifier.STRICT) {
				String flagName = Modifier.toString(flag);
				modifierFlags.put(flagName, new Integer(flag));
				flag = flag << 1;
			}
			modifierFlags.put("trivial",new Integer(TRIVIAL));
		}
		Integer flag = (Integer)modifierFlags.get(name);
		if (flag == null) return -1;
		if (flag.intValue()==TRIVIAL && !allowTrivial) return -2;
		return flag.intValue();
	}
	
	public boolean concernedWithTriviality() {
		return ((requiredModifiers|forbiddenModifiers)&TRIVIAL)!=0;
	}
	
	public boolean matchesTriviality(boolean isTrivial) {
		int matchMods = (isTrivial?TRIVIAL:0x0000);
		return ((matchMods & requiredModifiers) == requiredModifiers) &&
		        ((matchMods & forbiddenModifiers) == 0);
	}
	
	public boolean requires(int flag) {
		return ((requiredModifiers&flag)!=0);
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}