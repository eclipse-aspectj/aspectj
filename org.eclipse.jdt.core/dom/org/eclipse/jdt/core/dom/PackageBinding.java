/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

/**
 * Internal implementation of package bindings.
 */
class PackageBinding implements IPackageBinding {

	private static final String[] NO_NAME_COMPONENTS = new String[0];
	private static final String UNNAMED = "UNNAMED";//$NON-NLS-1$
	private static final char PACKAGE_NAME_SEPARATOR = '.';
	
	private org.eclipse.jdt.internal.compiler.lookup.PackageBinding binding;
	private BindingResolver resolver;
	private String name;
	private String[] components;
		
	PackageBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.PackageBinding binding) {
		this.binding = binding;
		this.resolver = resolver;
	}
	
	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (name == null) {
			computeNameAndComponents();
		}
		return name;
	}

	/*
	 * @see IPackageBinding#isUnnamed()
	 */
	public boolean isUnnamed() {
		return getName().equals(UNNAMED);
	}

	/*
	 * @see IPackageBinding#getNameComponents()
	 */
	public String[] getNameComponents() {
		if (components == null) {
			computeNameAndComponents();
		}
		return components;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.PACKAGE;
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		return -1;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		return false;
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		return getName();
	}
	
	private String concat(String[] array, char c) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, max = array.length; i < max - 1; i++) {
			buffer.append(array[i]).append(c);
		}
		buffer.append(array[array.length - 1]);
		return buffer.toString();
	}

	private void computeNameAndComponents() {
		char[][] compoundName = this.binding.compoundName;
		if (compoundName == TypeConstants.NoCharChar || compoundName == null) {
			name = UNNAMED;
			components = NO_NAME_COMPONENTS;
		} else {
			int length = compoundName.length;
			components = new String[length];
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < length - 1; i++) {
				components[i] = new String(compoundName[i]);
				buffer.append(compoundName[i]).append(PACKAGE_NAME_SEPARATOR);
			}
			components[length - 1] = new String(compoundName[length - 1]);
			buffer.append(compoundName[length - 1]);
			name = buffer.toString();
		}
	}		
}
