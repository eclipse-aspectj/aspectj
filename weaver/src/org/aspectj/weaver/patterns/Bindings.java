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

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.BCException;

public class Bindings {
	public static final Bindings NONE = new Bindings(0);
	
	private BindingTypePattern[] bindings;
	
	public Bindings(BindingTypePattern[] bindings) {
		this.bindings = bindings;
	}
	
	public Bindings(int count) {
		this(new BindingTypePattern[count]);
	}
	
	public void register(BindingTypePattern binding, IScope scope) {
		int index = binding.getFormalIndex();
		BindingTypePattern existingBinding = bindings[index];
		if (existingBinding != null) {
			scope.message(IMessage.ERROR, existingBinding, binding,
						"multiple bindings" + index + ", " + binding);
		}
		bindings[index] = binding;
	}
	
	public void mergeIn(Bindings other, IScope scope) {
		for (int i=0, len=other.bindings.length; i < len; i++) {
			if (other.bindings[i] != null) {
				register(other.bindings[i], scope);
			}
		}
	}
	
	
	
	/**
	 * signals an error if one has a binding and other doesn't
	 */
	public void checkEquals(Bindings other, IScope scope) {
		BindingTypePattern[] b1 = this.bindings;
		BindingTypePattern[] b2 = other.bindings;
		int len = b1.length;
		if (len != b2.length) {
			throw new BCException("INSANE");
		}
		
		for (int i=0; i < len; i++) {
			if (b1[i] == null && b2[i] != null) {
				scope.message(IMessage.ERROR, b2[i], "inconsistent binding");
				b1[i] = b2[i]; // done just to produce fewer error messages
			} else if (b2[i] == null && b1[i] != null) {
				scope.message(IMessage.ERROR, b1[i], "inconsistent binding");
				b2[i] = b1[i]; // done just to produce fewer error messages
			}
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("Bindings(");
		for (int i=0, len=bindings.length; i < len; i++) {
			if (i > 0) buf.append(", ");
			buf.append(bindings[i]);
		}
		buf.append(")");
		return buf.toString();
	}
	
	
	public int[] getUsedFormals() {
		//System.out.println("used: " + this);
		int[] ret = new int[bindings.length];
		int index = 0;
		for (int i=0, len=bindings.length; i < len; i++) {
			if (bindings[i] != null) {
				ret[index++] = i;
			}
		}
		int[] newRet = new int[index];
		System.arraycopy(ret, 0, newRet, 0, index);
		//System.out.println("ret: " + index);
		return newRet;
	}


	public Bindings copy() {
//		int len = bindings.length;
//		boolean[] a = new boolean[len];
//		System.arraycopy(bindings, 0, a, 0, len);
		return new Bindings((BindingTypePattern[])bindings.clone());
	}

	public void checkAllBound(IScope scope) {
		for (int i=0, len=bindings.length; i < len; i++) {
			if (bindings[i] == null) {
				scope.message(IMessage.ERROR, scope.getFormal(i), "formal unbound in pointcut");
			}
		}

	}

	public int size() { return bindings.length; }

	public void checkEmpty(IScope scope, String message) {
		for (int i=0, len=bindings.length; i < len; i++) {
			if (bindings[i] != null) {
				scope.message(IMessage.ERROR, bindings[i], message);
			}
		}
	}

}
