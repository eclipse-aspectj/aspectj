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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.VersionedDataInputStream;

public class ModifiersPattern extends PatternNode {
	private int requiredModifiers;
	private int forbiddenModifiers;

	public static final ModifiersPattern ANY = new ModifiersPattern(0, 0);

	private static Map<String, Integer> modifierFlags = null;

	static {
		modifierFlags = new HashMap<>();
		int flag = 1;
		while (flag <= Modifier.STRICT) {
			String flagName = Modifier.toString(flag);
			modifierFlags.put(flagName, flag);
			flag = flag << 1;
		}
		/* Modifier.SYNTHETIC */
		modifierFlags.put("synthetic", 0x1000);
	}

	public ModifiersPattern(int requiredModifiers, int forbiddenModifiers) {
		this.requiredModifiers = requiredModifiers;
		this.forbiddenModifiers = forbiddenModifiers;
	}

	public String toString() {
		if (this == ANY) {
			return "";
		}

		String ret = Modifier.toString(requiredModifiers);
		if (forbiddenModifiers == 0) {
			return ret;
		} else {
			return ret + " !" + Modifier.toString(forbiddenModifiers);
		}
	}

	public boolean equals(Object other) {
		if (!(other instanceof ModifiersPattern)) {
			return false;
		}
		ModifiersPattern o = (ModifiersPattern) other;
		return o.requiredModifiers == this.requiredModifiers && o.forbiddenModifiers == this.forbiddenModifiers;
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + requiredModifiers;
		result = 37 * result + forbiddenModifiers;
		return result;
	}

	public boolean matches(int modifiers) {
		return ((modifiers & requiredModifiers) == requiredModifiers) && ((modifiers & forbiddenModifiers) == 0);
	}

	public static ModifiersPattern read(VersionedDataInputStream s) throws IOException {
		int requiredModifiers = s.readShort();
		int forbiddenModifiers = s.readShort();
		if (requiredModifiers == 0 && forbiddenModifiers == 0) {
			return ANY;
		}
		return new ModifiersPattern(requiredModifiers, forbiddenModifiers);
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		// s.writeByte(MODIFIERS_PATTERN);
		s.writeShort(requiredModifiers);
		s.writeShort(forbiddenModifiers);
	}

	public static int getModifierFlag(String name) {
		Integer flag = modifierFlags.get(name);
		if (flag == null) {
			return -1;
		}
		return flag;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
