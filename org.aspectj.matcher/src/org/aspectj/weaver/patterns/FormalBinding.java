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

import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.UnresolvedType;

public class FormalBinding implements IHasPosition {
	private final UnresolvedType type;
	private final String name;
	private final int index;
	private final int start, end;

	public FormalBinding(UnresolvedType type, String name, int index, int start, int end) {
		this.type = type;
		this.name = name;
		this.index = index;
		this.start = start;
		this.end = end;
	}

	public FormalBinding(UnresolvedType type, int index) {
		this(type, "unknown", index, 0, 0);
	}

	public FormalBinding(UnresolvedType type, String name, int index) {
		this(type, name, index, 0, 0);
	}

	// ----

	public String toString() {
		return type.toString() + ":" + index;
	}

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public UnresolvedType getType() {
		return type;
	}

	// ----

	public static final FormalBinding[] NONE = new FormalBinding[0];

	/**
	 * A marker class for bindings for which we want to ignore unbound issue and consider them as implicit binding - f.e. to handle
	 * JoinPoint in @AJ advices
	 * 
	 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
	 */
	public static class ImplicitFormalBinding extends FormalBinding {
		public ImplicitFormalBinding(UnresolvedType type, String name, int index) {
			super(type, name, index);
		}
	}

}
