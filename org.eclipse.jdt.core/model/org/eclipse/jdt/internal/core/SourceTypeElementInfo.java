/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;
import org.eclipse.jdt.internal.compiler.env.ISourceType;

/** 
 * Element info for an IType element that originated from source. 
 */
public class SourceTypeElementInfo extends MemberElementInfo implements ISourceType {
	protected static final ISourceField[] NO_FIELDS = new ISourceField[0];
	protected static final ISourceMethod[] NO_METHODS = new ISourceMethod[0];
	protected static final ISourceType[] NO_TYPES = new ISourceType[0];
	/**
	 * The name of the superclass for this type. This name
	 * is fully qualified for binary types and is NOT
	 * fully qualified for source types.
	 */
	protected char[] fSuperclassName;
	
	/**
	 * The names of the interfaces this type implements or
	 * extends. These names are fully qualified in the case
	 * of a binary type, and are NOT fully qualified in the
	 * case of a source type
	 */
	protected char[][] fSuperInterfaceNames;
	
	/**
	 * The enclosing type name for this type.
	 *
	 * @see getEnclosingTypeName
	 */
	protected char[] fEnclosingTypeName = null;

	/**
	 * The name of the source file this type is declared in.
	 */
	protected char[] fSourceFileName= null;

	/**
	 * The name of the package this type is contained in.
	 */
	protected char[] fPackageName= null;

	/**
	 * The qualified name of this type.
	 */
	protected char[] fQualifiedName= null;







	/**
	 * The imports in this type's compilation unit
	 */
	protected char[][] fImports= null;

	/**
	 * Backpointer to my type handle - useful for translation
	 * from info to handle.
	 */
	protected IType fHandle= null;
	 








/**
 * Adds the given import to this type's collection of imports
 */
protected void addImport(char[] i) {
	if (fImports == null) {
		fImports = new char[][] {i};
	} else {
		char[][] copy = new char[fImports.length + 1][];
		System.arraycopy(fImports, 0, copy, 0, fImports.length);
		copy[fImports.length] = i;
		fImports = copy;
	}
}


/**
 * Returns the ISourceType that is the enclosing type for this
 * type, or <code>null</code> if this type is a top level type.
 */
public ISourceType getEnclosingType() {
	IJavaElement parent= fHandle.getParent();
	if (parent != null && parent.getElementType() == IJavaElement.TYPE) {
		try {
			return (ISourceType)((JavaElement)parent).getElementInfo();
		} catch (JavaModelException e) {
			return null;
		}
	} else {
		return null;
	}
}
/**
 * @see ISourceType
 */
public char[] getEnclosingTypeName() {
	return fEnclosingTypeName;
}
/**
 * @see ISourceType
 */
public ISourceField[] getFields() {
	int length = fChildren.length;
	if (length == 0) return NO_FIELDS;
	ISourceField[] fields = new ISourceField[length];
	int fieldIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = fChildren[i];
		if (child instanceof SourceField) {
			try {
				ISourceField field = (ISourceField)((SourceField)child).getElementInfo();
				fields[fieldIndex++] = field;
			} catch (JavaModelException e) {
			}
		}
	}
	if (fieldIndex == 0) return NO_FIELDS;
	System.arraycopy(fields, 0, fields = new ISourceField[fieldIndex], 0, fieldIndex);
	return fields;
}
/**
 * @see ISourceType
 */
public char[] getFileName() {
	return fSourceFileName;
}
/**
 * Returns the handle for this type info
 */
public IType getHandle() {
	return fHandle;
}
/**
 * @see ISourceType
 */
public char[][] getImports() {
	return fImports;
}
/**
 * @see ISourceType
 */
public char[][] getInterfaceNames() {
	return fSuperInterfaceNames;
}
/**
 * @see ISourceType
 */
public ISourceType[] getMemberTypes() {
	int length = fChildren.length;
	if (length == 0) return NO_TYPES;
	ISourceType[] memberTypes = new ISourceType[length];
	int typeIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = fChildren[i];
		if (child instanceof SourceType) {
			try {
				ISourceType type = (ISourceType)((SourceType)child).getElementInfo();
				memberTypes[typeIndex++] = type;
			} catch (JavaModelException e) {
			}
		}
	}
	if (typeIndex == 0) return NO_TYPES;
	System.arraycopy(memberTypes, 0, memberTypes = new ISourceType[typeIndex], 0, typeIndex);
	return memberTypes;
}
/**
 * @see ISourceType
 */
public ISourceMethod[] getMethods() {
	int length = fChildren.length;
	if (length == 0) return NO_METHODS;
	ISourceMethod[] methods = new ISourceMethod[length];
	int methodIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = fChildren[i];
		if (child instanceof SourceMethod) {
			try {
				ISourceMethod method = (ISourceMethod)((SourceMethod)child).getElementInfo();
				methods[methodIndex++] = method;
			} catch (JavaModelException e) {
			}
		}
	}
	if (methodIndex == 0) return NO_METHODS;
	System.arraycopy(methods, 0, methods = new ISourceMethod[methodIndex], 0, methodIndex);
	return methods;
}
/**
 * @see ISourceType
 */
public char[] getPackageName() {
	return fPackageName;
}
/**
 * @see ISourceType
 */
public char[] getQualifiedName() {
	return fQualifiedName;
}
/**
 * @see ISourceType
 */
public char[] getSuperclassName() {
	return fSuperclassName;
}
/**
 * @see ISourceType
 */
public boolean isBinaryType() {
	return false;
}
/**
 * @see ISourceType
 */
public boolean isClass() {
	return (this.flags & IConstants.AccInterface) == 0;
}
/**
 * @see ISourceType
 */
public boolean isInterface() {
	return (this.flags & IConstants.AccInterface) != 0;
}
/**
 * Sets the (unqualified) name of the type that encloses this type.
 */
protected void setEnclosingTypeName(char[] enclosingTypeName) {
	fEnclosingTypeName = enclosingTypeName;
}
/**
 * Sets the handle for this type info
 */
protected void setHandle(IType handle) {
	fHandle= handle;
}
/**
 * Sets the name of the package this type is declared in.
 */
protected void setPackageName(char[] name) {
	fPackageName= name;
}
/**
 * Sets this type's qualified name.
 */
protected void setQualifiedName(char[] name) {
	fQualifiedName= name;
}
/**
 * Sets the name of the source file this type is declared in.
 */
protected void setSourceFileName(char[] name) {
	fSourceFileName= name;
}
/**
 * Sets the (unqualified) name of this type's superclass
 */
protected void setSuperclassName(char[] superclassName) {
	fSuperclassName = superclassName;
}
/**
 * Sets the (unqualified) names of the interfaces this type implements or extends
 */
protected void setSuperInterfaceNames(char[][] superInterfaceNames) {
	fSuperInterfaceNames = superInterfaceNames;
}
public String toString() {
	return "Info for " + fHandle.toString(); //$NON-NLS-1$
}
}
