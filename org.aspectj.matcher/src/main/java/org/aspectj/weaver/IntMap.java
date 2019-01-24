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

package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.List;

public class IntMap {
	// public static final IntMap EMPTY = new IntMap(0) {
	// public boolean directlyInAdvice() { return true; }
	// public ShadowMunger getEnclosingAdvice() { return null; } //XXX possible
	// };

	// XXX begin hack to avoid a signature refactoring in Pointcut
	private ResolvedType concreteAspect;
	private ShadowMunger enclosingAdvice;
	private List<ResolvedPointcutDefinition> enclosingDefinition = new ArrayList<ResolvedPointcutDefinition>();

	public void pushEnclosingDefinition(ResolvedPointcutDefinition def) {
		enclosingDefinition.add(def);
	}

	public void popEnclosingDefinitition() {
		enclosingDefinition.remove(enclosingDefinition.size() - 1);
	}

	public ResolvedPointcutDefinition peekEnclosingDefinition() {
		if (enclosingDefinition.size() == 0) {
			return null;
		}
		return enclosingDefinition.get(enclosingDefinition.size() - 1);
	}

	public boolean directlyInAdvice() {
		return enclosingDefinition.isEmpty();
	}

	public ShadowMunger getEnclosingAdvice() {
		return enclosingAdvice;
	}

	public void setEnclosingAdvice(ShadowMunger advice) {
		this.enclosingAdvice = advice;
	}

	public Member getAdviceSignature() {
		if (enclosingAdvice instanceof Advice) {
			return ((Advice) enclosingAdvice).getSignature();
		} else {
			return null;
		}
	}

	public ResolvedType getConcreteAspect() {
		return concreteAspect;
	}

	public void setConcreteAspect(ResolvedType concreteAspect) {
		this.concreteAspect = concreteAspect;
	}

	public void copyContext(IntMap bindings) {
		this.enclosingAdvice = bindings.enclosingAdvice;
		this.enclosingDefinition = bindings.enclosingDefinition;
		this.concreteAspect = bindings.concreteAspect;
	}

	// XXX end hack to avoid a signature refactoring in Pointcut

	private static final int MISSING = -1;

	private int[] map;

	private IntMap(int[] map) {
		this.map = map;
	}

	public IntMap() {
		map = new int[0];
	}

	public IntMap(int initialCapacity) {
		map = new int[initialCapacity];
		for (int i = 0; i < initialCapacity; i++) {
			map[i] = MISSING;
		}
	}

	public void put(int key, int val) {
		/* assert (val >= 0 && key >= 0) */
		if (key >= map.length) {
			int[] tmp = new int[key * 2 + 1]; // ??? better expansion function
			System.arraycopy(map, 0, tmp, 0, map.length);
			for (int i = map.length, len = tmp.length; i < len; i++) {
				tmp[i] = MISSING;
			}
			map = tmp;
		}
		map[key] = val;
	}

	public int get(int key) {
		return map[key];
	}

	public boolean hasKey(int key) {
		return (key < map.length && map[key] != MISSING);
	}

	// ---- factory methods

	public static IntMap idMap(int size) {
		int[] map = new int[size];
		for (int i = 0; i < size; i++) {
			map[i] = i;
		}
		return new IntMap(map);
	}

	// ---- from object

	public String toString() {
		StringBuffer buf = new StringBuffer("[");
		boolean seenFirst = false;
		for (int i = 0, len = map.length; i < len; i++) {
			if (map[i] != MISSING) {
				if (seenFirst) {
					buf.append(", ");
				}
				seenFirst = true;
				buf.append(i);
				buf.append(" -> ");
				buf.append(map[i]);
			}
		}
		buf.append("]");
		return buf.toString();
	}

}
