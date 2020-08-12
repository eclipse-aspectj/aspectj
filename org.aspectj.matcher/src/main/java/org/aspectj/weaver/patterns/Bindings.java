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

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.UnresolvedType;

public class Bindings {
	public static final Bindings NONE = new Bindings(0);

	private BindingPattern[] bindings;

	public Bindings(BindingPattern[] bindings) {
		this.bindings = bindings;
	}

	public Bindings(int count) {
		this(new BindingPattern[count]);
	}

	public void register(BindingPattern binding, IScope scope) {
		int index = binding.getFormalIndex();
		BindingPattern existingBinding = bindings[index];
		if (existingBinding != null) {
			scope.message(IMessage.ERROR, existingBinding, binding, "multiple bindings" + index + ", " + binding);
		}
		bindings[index] = binding;
	}

	public void mergeIn(Bindings other, IScope scope) {
		for (int i = 0, len = other.bindings.length; i < len; i++) {
			if (other.bindings[i] != null) {
				register(other.bindings[i], scope);
			}
		}
	}

	/**
	 * signals an error if one has a binding and other doesn't
	 */
	public void checkEquals(Bindings other, IScope scope) {
		BindingPattern[] b1 = this.bindings;
		BindingPattern[] b2 = other.bindings;
		int len = b1.length;
		if (len != b2.length) {
			throw new BCException("INSANE");
		}

		for (int i = 0; i < len; i++) {
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
		for (int i = 0, len = bindings.length; i < len; i++) {
			if (i > 0)
				buf.append(", ");
			buf.append(bindings[i]);
		}
		buf.append(")");
		return buf.toString();
	}

	public int[] getUsedFormals() {
		// System.out.println("used: " + this);
		int[] ret = new int[bindings.length];
		int index = 0;
		for (int i = 0, len = bindings.length; i < len; i++) {
			if (bindings[i] != null) {
				ret[index++] = i;
			}
		}
		int[] newRet = new int[index];
		System.arraycopy(ret, 0, newRet, 0, index);
		// System.out.println("ret: " + index);
		return newRet;
	}

	public UnresolvedType[] getUsedFormalTypes() {
		UnresolvedType[] ret = new UnresolvedType[bindings.length];
		int index = 0;
		for (BindingPattern binding : bindings) {
			if (binding != null) {
				ret[index++] = ((BindingTypePattern) binding).getExactType();
			}
		}
		UnresolvedType[] newRet = new UnresolvedType[index];
		System.arraycopy(ret, 0, newRet, 0, index);
		// System.out.println("ret: " + index);
		return newRet;
	}

	public Bindings copy() {
		// int len = bindings.length;
		// boolean[] a = new boolean[len];
		// System.arraycopy(bindings, 0, a, 0, len);
		return new Bindings((BindingPattern[]) bindings.clone());
	}

	public void checkAllBound(IScope scope) {
		for (int i = 0, len = bindings.length; i < len; i++) {
			if (bindings[i] == null) {
				// ATAJ: avoid warnings for implicit bindings
				if (scope.getFormal(i) instanceof FormalBinding.ImplicitFormalBinding) {
					bindings[i] = new BindingTypePattern(scope.getFormal(i), false);
				} else {
					scope.message(IMessage.ERROR, scope.getFormal(i), "formal unbound in pointcut ");
				}
			}
		}

	}

	public int size() {
		return bindings.length;
	}

}
