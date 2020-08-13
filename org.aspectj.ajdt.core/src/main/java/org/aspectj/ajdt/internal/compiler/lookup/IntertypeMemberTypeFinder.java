/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 * Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.HashSet;
import java.util.Set;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ITypeFinder;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

/**
 * The member finder looks after intertype declared inner classes on a type, there is one member finder per type that was hit by an
 * new inner type declaration.
 * 
 * @author Andy Clement
 * @since 1.6.9
 */
public class IntertypeMemberTypeFinder implements ITypeFinder {

	// Target that has these new types
	public SourceTypeBinding targetTypeBinding;

	// The new types declared onto the target
	private Set<ReferenceBinding> intertypeMemberTypes = new HashSet<>();

	public void addInterTypeMemberType(ReferenceBinding binding) {
		intertypeMemberTypes.add(binding);
		// ReferenceBinding[] rbs = targetTypeBinding.memberTypes();
		// ReferenceBinding[] newRbs = new ReferenceBinding[rbs.length + 1];
		// System.arraycopy(rbs, 0, newRbs, 1, rbs.length);
		// newRbs[0] = binding;
		// (targetTypeBinding).memberTypes = newRbs;

	}

	public ReferenceBinding getMemberType(char[] memberTypeName) {
		for (ReferenceBinding intertypeMemberType : intertypeMemberTypes) {
			if (CharOperation.equals(intertypeMemberType.sourceName, memberTypeName)) {
				return intertypeMemberType;
			}
		}
		return null;
	}

	public ReferenceBinding[] getMemberTypes() {
		ReferenceBinding[] array = new ReferenceBinding[intertypeMemberTypes.size()];
		return intertypeMemberTypes.toArray(array);
	}

}
