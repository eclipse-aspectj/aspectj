/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;

/**
 * Internal implementation of variable bindings.
 */
class VariableBinding implements IVariableBinding {

	private org.eclipse.jdt.internal.compiler.lookup.VariableBinding binding;
	private BindingResolver resolver;
	private String name;
	private ITypeBinding declaringClass;
	private ITypeBinding type;

	VariableBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.VariableBinding binding) {
		this.resolver = resolver;
		this.binding = binding;
	}

	/*
	 * @see IVariableBinding#isField()
	 */
	public boolean isField() {
		return this.binding instanceof FieldBinding;
	}

	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (this.name == null) {
			this.name = new String(this.binding.name);
		}
		return this.name;
	}

	/*
	 * @see IVariableBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		if (isField()) {
			FieldBinding fieldBinding = (FieldBinding) this.binding;
			if (this.declaringClass == null) {
				this.declaringClass = this.resolver.getTypeBinding(fieldBinding.declaringClass);
			}
			return this.declaringClass;
		} else {
			return null;
		}
	}

	/*
	 * @see IVariableBinding#getType()
	 */
	public ITypeBinding getType() {
		if (type == null) {
			type = this.resolver.getTypeBinding(this.binding.type);
		}
		return type;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.VARIABLE;
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		if (isField()) {
			return ((FieldBinding) this.binding).getAccessFlags();
		}
		return 0;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		if (isField()) {
			return ((FieldBinding) this.binding).isDeprecated();
		}
		return false;
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		if (isField()) {
			return ((FieldBinding) this.binding).isSynthetic();
		}
		return false;
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (isField()) {
			StringBuffer buffer = new StringBuffer();
			if (this.getDeclaringClass() != null) {
				buffer.append(this.getDeclaringClass().getKey());
			}
			buffer.append(this.getName());
			return buffer.toString();
		}			
		return null;
	}
	
	/*
	 * @see IVariableBinding#getVariableId()
	 */
	public int getVariableId() {
		return this.binding.id;
	}

}
