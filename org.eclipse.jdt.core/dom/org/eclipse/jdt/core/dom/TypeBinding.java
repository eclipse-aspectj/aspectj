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

import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Internal implementation of type bindings.
 */
class TypeBinding implements ITypeBinding {

	private static final String NO_NAME = ""; //$NON-NLS-1$	
	private static final ITypeBinding[] NO_INTERFACES = new ITypeBinding[0];
	private static final ITypeBinding[] NO_DECLARED_TYPES = new ITypeBinding[0];
	private static final IVariableBinding[] NO_DECLARED_FIELDS = new IVariableBinding[0];
	private static final IMethodBinding[] NO_DECLARED_METHODS = new IMethodBinding[0];
	
	private org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;
	private BindingResolver resolver;
	private IVariableBinding[] fields;
	private IMethodBinding[] methods;
	
	public TypeBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding) {
		this.binding = binding;
		this.resolver = resolver;
	}
	
	/*
	 * @see ITypeBinding#isPrimitive()
	 */
	public boolean isPrimitive() {
		return binding.isBaseType();
	}

	/*
	 * @see ITypeBinding#isArray()
	 */
	public boolean isArray() {
		return binding.isArrayType();
	}

	/*
	 * @see ITypeBinding#getElementType()
	 */
	public ITypeBinding getElementType() {
		if (!this.isArray()) {
			return null;
		}
		ArrayBinding arrayBinding = (ArrayBinding) binding;
		return resolver.getTypeBinding(arrayBinding.leafComponentType);
	}

	/*
	 * @see ITypeBinding#getDimensions()
	 */
	public int getDimensions() {
		if (!this.isArray()) {
			return 0;
		}
		ArrayBinding arrayBinding = (ArrayBinding) binding;
		return arrayBinding.dimensions;
	}

	/*
	 * @see ITypeBinding#isClass()
	 */
	public boolean isClass() {
		return this.binding.isClass();
	}

	/*
	 * @see ITypeBinding#isInterface()
	 */
	public boolean isInterface() {
		return this.binding.isInterface();
	}

	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			if (referenceBinding.isAnonymousType()) {
				return NO_NAME;
			} else if (referenceBinding.isMemberType()) {
				char[] name = referenceBinding.compoundName[referenceBinding.compoundName.length - 1];
				return new String(CharOperation.subarray(name, CharOperation.lastIndexOf('$', name) + 1, name.length));
			} else if (referenceBinding.isLocalType()) {
				char[] name = referenceBinding.compoundName[referenceBinding.compoundName.length - 1];
				return new String(CharOperation.subarray(name, CharOperation.lastIndexOf('$', name) + 1, name.length));
			} else {
				return new String(referenceBinding.compoundName[referenceBinding.compoundName.length - 1]);
			}
		} else if (this.binding.isArrayType()) {
			ArrayBinding arrayBinding = (ArrayBinding) this.binding;
			int dimensions = arrayBinding.dimensions;
			char[] brackets = new char[dimensions * 2];
			for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
				brackets[i] = ']';
				brackets[i - 1] = '[';
			}
			StringBuffer buffer = new StringBuffer();
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding leafComponentTypeBinding = arrayBinding.leafComponentType;
			if (leafComponentTypeBinding.isClass() || leafComponentTypeBinding.isInterface()) {
				ReferenceBinding referenceBinding2 = (ReferenceBinding) leafComponentTypeBinding;
				if (referenceBinding2.isMemberType()) {
					char[] name = referenceBinding2.compoundName[referenceBinding2.compoundName.length - 1];
					buffer.append(CharOperation.subarray(name, CharOperation.lastIndexOf('$', name) + 1, name.length));
				} else if (referenceBinding2.isLocalType()) {
					char[] name = referenceBinding2.compoundName[referenceBinding2.compoundName.length - 1];
					buffer.append(CharOperation.subarray(name, CharOperation.lastIndexOf('$', name) + 1, name.length));
				} else {
					buffer.append(referenceBinding2.compoundName[referenceBinding2.compoundName.length - 1]);
				}
			} else {
				buffer.append(leafComponentTypeBinding.readableName());
			}
			buffer.append(brackets);
			return buffer.toString();
		} else {
			return new String(this.binding.readableName());
		}
	}

	/*
	 * @see ITypeBinding#getPackage()
	 */
	public IPackageBinding getPackage() {
		if (this.binding.isBaseType() || this.binding.isArrayType()) {
			return null;
		} else {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return this.resolver.getPackageBinding(referenceBinding.getPackage());
		}
	}

	/*
	 * @see ITypeBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		if (this.binding.isArrayType() || this.binding.isBaseType()) {
			return null;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		if (referenceBinding.isNestedType()) {
			return this.resolver.getTypeBinding(referenceBinding.enclosingType());
		} else {
			return null;
		}
	}

	/*
	 * @see ITypeBinding#getSuperclass()
	 */
	public ITypeBinding getSuperclass() {
		if (this.binding.isArrayType() || this.binding.isBaseType() || this.binding.isInterface()) {
			return null;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		ReferenceBinding superclass = referenceBinding.superclass();
		if (superclass == null) {
			return null;
		}
		return this.resolver.getTypeBinding(superclass);		
	}

	/*
	 * @see ITypeBinding#getInterfaces()
	 */
	public ITypeBinding[] getInterfaces() {
		if (this.binding.isArrayType() || this.binding.isBaseType()) {
			return NO_INTERFACES;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		ReferenceBinding[] interfaces = referenceBinding.superInterfaces();
		int length = interfaces.length;
		if (length == 0) {
			return NO_INTERFACES;
		} else {
			ITypeBinding[] newInterfaces = new ITypeBinding[length];
			for (int i = 0; i < length; i++) {
				newInterfaces[i] = this.resolver.getTypeBinding(interfaces[i]);
			}
			return newInterfaces;
		}
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		if (this.binding.isClass()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			if (referenceBinding.isAnonymousType()) {
				return referenceBinding.getAccessFlags() & ~Modifier.FINAL;
			}
			return referenceBinding.getAccessFlags();
		} else if (this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			// clear the AccAbstract and the AccInterface bits
			return referenceBinding.getAccessFlags() & ~(Modifier.ABSTRACT | 0x200);
		} else {
			return 0;
		}
	}

	/*
	 * @see ITypeBinding#getDeclaredModifiers()
	 */
	public int getDeclaredModifiers() {
		return getModifiers();
	}

	/*
	 * @see ITypeBinding#isTopLevel()
	 */
	public boolean isTopLevel() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return !referenceBinding.isNestedType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isNested()
	 */
	public boolean isNested() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isNestedType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isMember()
	 */
	public boolean isMember() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isMemberType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isLocal()
	 */
	public boolean isLocal() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isLocalType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isAnonymous()
	 */
	public boolean isAnonymous() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isAnonymousType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#getDeclaredTypes()
	 */
	public ITypeBinding[] getDeclaredTypes() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			ReferenceBinding[] members = referenceBinding.memberTypes();
			int length = members.length;
			ITypeBinding[] newMembers = new ITypeBinding[length];
			for (int i = 0; i < length; i++) {
				newMembers[i] = this.resolver.getTypeBinding(members[i]);
			}
			return newMembers;
		} else {
			return NO_DECLARED_TYPES;
		}
	}

	/*
	 * @see ITypeBinding#getDeclaredFields()
	 */
	public IVariableBinding[] getDeclaredFields() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			FieldBinding[] fields = referenceBinding.fields();
			int length = fields.length;
			IVariableBinding[] newFields = new IVariableBinding[length];
			for (int i = 0; i < length; i++) {
				newFields[i] = this.resolver.getVariableBinding(fields[i]);
			}
			return newFields;
		} else {
			return NO_DECLARED_FIELDS;
		}
	}

	/*
	 * @see ITypeBinding#getDeclaredMethods()
	 */
	public IMethodBinding[] getDeclaredMethods() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			org.eclipse.jdt.internal.compiler.lookup.MethodBinding[] methods = referenceBinding.methods();
			int length = methods.length;
			IMethodBinding[] newMethods = new IMethodBinding[length];
			for (int i = 0; i < length; i++) {
				newMethods[i] = this.resolver.getMethodBinding(methods[i]);
			}
			return newMethods;
		} else {
			return NO_DECLARED_METHODS;
		}
	}

	/*
	 * @see ITypeBinding#isFromSource()
	 */
	public boolean isFromSource() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return !referenceBinding.isBinaryBinding();
		}
		return false;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.TYPE;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isDeprecated();
		}
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
		if (isLocal()) {
			return null;
		}
		if (this.binding.isClass() || this.binding.isInterface()) {
			StringBuffer buffer = new StringBuffer();
			buffer
				.append(getPackage().getName())
				.append('.')
				.append(getName());
			return buffer.toString();
		} else if (this.binding.isArrayType()) {
			return this.getElementType().getKey() + this.getDimensions();
		}
		// this is a primitive type
		return this.getName();
	}

	/**
	 * @see ITypeBinding#isNullType()
	 */
	public boolean isNullType() {
		return this.binding == BaseTypes.NullBinding;
	}

}
