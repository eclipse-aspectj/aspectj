/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *   Andy Clement   pushed down into bcel module
 * ******************************************************************/

package org.aspectj.apache.bcel.generic;

public final class LocalVariableTag extends Tag {
	private final String signature;
	private String name;
	private int slot;
	private final int startPosition;
	private boolean remapped = false;

	private int hashCode = 0;
	private Type type; // not always known, in which case signature has to be used

	// AMC - pr101047, two local vars with the same name can share the same slot, but must in that case
	// have different start positions.
	public LocalVariableTag(String signature, String name, int slot, int startPosition) {
		this.signature = signature;
		this.name = name;
		this.slot = slot;
		this.startPosition = startPosition;
	}

	public LocalVariableTag(Type type, String signature, String name, int slot, int startPosition) {
		this.type = type;
		this.signature = signature;
		this.name = name;
		this.slot = slot;
		this.startPosition = startPosition;
	}

	public String getName() {
		return name;
	}

	public int getSlot() {
		return slot;
	}

	public String getType() {
		return signature;
	}

	public Type getRealType() {
		return type;
	}

	public void updateSlot(int newSlot) {
		this.slot = newSlot;
		this.remapped = true;
		this.hashCode = 0;
	}

	public void setName(String name) {
		this.name = name;
		this.hashCode = 0;
	}

	public boolean isRemapped() {
		return this.remapped;
	}

	public String toString() {
		return "local " + slot + ": " + signature + " " + name;
	}

	public boolean equals(Object other) {
		if (!(other instanceof LocalVariableTag)) {
			return false;
		}
		LocalVariableTag o = (LocalVariableTag) other;
		return o.slot == slot && o.startPosition == startPosition && o.signature.equals(signature) && o.name.equals(name);
	}

	public int hashCode() {
		if (hashCode == 0) {
			int ret = signature.hashCode();
			ret = 37 * ret + name.hashCode();
			ret = 37 * ret + slot;
			ret = 37 * ret + startPosition;
			hashCode = ret;
		}
		return hashCode;
	}
}
