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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;

/**
 * Element info for <code>ClassFile</code> handles.
 */
 
/* package */ class ClassFileInfo extends OpenableElementInfo {
	/** 
	 * The children of the <code>BinaryType</code> corresponding to our
	 * <code>ClassFile</code>. These are kept here because we don't have
	 * access to the <code>BinaryType</code> info (<code>ClassFileReader</code>).
	 * <p>
	 * The children are lazily initialized, on the first call to
	 * <code>getBinaryChildren()</code>, which in turn is called by
	 * <code>BinaryType.getChildren()</code>. 
	 */
	protected IJavaElement[] fBinaryChildren = null;
	/**
	 * Back-pointer to the IClassFile to allow lazy initialization.
	 */
	protected IClassFile fClassFile = null;
/**
 * Creates a new <code>ClassFileInfo</code> for <code>classFile</code>.
 */
ClassFileInfo(IClassFile classFile) {
	fClassFile = classFile;
}
/**
 * Creates the handles and infos for the fields of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateFieldInfos(IType type, IBinaryType typeInfo, HashMap newElements, ArrayList children) {
	// Make the fields
	IBinaryField[] fields = typeInfo.getFields();
	if (fields == null) {
		return;
	}
	for (int i = 0, fieldCount = fields.length; i < fieldCount; i++) {
		IBinaryField fieldInfo = fields[i];
		IField field = new BinaryField(type, new String(fieldInfo.getName()));
		newElements.put(field, fieldInfo);
		children.add(field);
	}
}
/**
 * Creates the handles and infos for the inner types of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateInnerClassInfos(IType type, IBinaryType typeInfo, HashMap newElements, ArrayList children) {
	// Add inner types
	// If the current type is an inner type, innerClasses returns
	// an extra entry for the current type.  This entry must be removed.
	// Can also return an entry for the enclosing type of an inner type.
	IBinaryNestedType[] innerTypes = typeInfo.getMemberTypes();
	if (innerTypes != null) {
		for (int i = 0, typeCount = innerTypes.length; i < typeCount; i++) {
			IBinaryNestedType binaryType = innerTypes[i];
			IClassFile classFile= ((IPackageFragment)fClassFile.getParent()).getClassFile(new String(ClassFile.unqualifiedName(binaryType.getName())) + ".class"); //$NON-NLS-1$
			IType innerType = new BinaryType(classFile, new String(ClassFile.simpleName(binaryType.getName())));
			children.add(innerType);
		}
	}
}
/**
 * Creates the handles and infos for the methods of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateMethodInfos(IType type, IBinaryType typeInfo, HashMap newElements, ArrayList children) {
	IBinaryMethod[] methods = typeInfo.getMethods();
	if (methods == null) {
		return;
	}
	for (int i = 0, methodCount = methods.length; i < methodCount; i++) {
		IBinaryMethod methodInfo = methods[i];
		String[] pNames= Signature.getParameterTypes(new String(methodInfo.getMethodDescriptor()));
		char[][] paramNames= new char[pNames.length][];
		for (int j= 0; j < pNames.length; j++) {
			paramNames[j]= pNames[j].toCharArray();
		}
		char[][] parameterTypes = ClassFile.translatedNames(paramNames);
		String selector = new String(methodInfo.getSelector());
		if (methodInfo.isConstructor()) {
			selector = type.getElementName();
		}
		for (int j= 0; j < pNames.length; j++) {
			pNames[j]= new String(parameterTypes[j]);
		}
		IMethod method = new BinaryMethod(type, selector, pNames);
		children.add(method);
		newElements.put(method, methodInfo);
	}
}
/**
 * Returns the list of children (<code>BinaryMember</code>s) of the
 * <code>BinaryType</code> of our <code>ClassFile</code>.
 */
IJavaElement[] getBinaryChildren() throws JavaModelException {
	if (fBinaryChildren == null) {
		readBinaryChildren();
	}
	return fBinaryChildren;
}
/**
 * Returns true iff the <code>readBinaryChildren</code> has already
 * been called.
 */
boolean hasReadBinaryChildren() {
	return fBinaryChildren != null;
}
/**
 * Creates the handles for <code>BinaryMember</code>s defined in this
 * <code>ClassFile</code> and adds them to the
 * <code>JavaModelManager</code>'s cache.
 */
private void readBinaryChildren() {
	ArrayList children = new ArrayList();
	HashMap newElements = new HashMap();
	BinaryType type = null;
	IBinaryType typeInfo = null;
	JavaModelManager manager = (JavaModelManager) JavaModelManager.getJavaModelManager();
	try {
		type = (BinaryType) fClassFile.getType();
		typeInfo = (IBinaryType) manager.getInfo(type);
	} catch (JavaModelException npe) {
		return;
	}
	if (typeInfo != null) { //may not be a valid class file
		generateFieldInfos(type, typeInfo, newElements, children);
		generateMethodInfos(type, typeInfo, newElements, children);
		generateInnerClassInfos(type, typeInfo, newElements, children);
	}
	
	for (Iterator iter = newElements.entrySet().iterator(); iter.hasNext();) {
		Map.Entry entry = (Map.Entry) iter.next();
		manager.putInfo(
			(IJavaElement) entry.getKey(), 
			entry.getValue());
	}
	fBinaryChildren = new IJavaElement[children.size()];
	children.toArray(fBinaryChildren);
}
/**
 * Removes the binary children handles and remove their infos from
 * the <code>JavaModelManager</code>'s cache.
 */
void removeBinaryChildren() {
	if (fBinaryChildren != null) {
		JavaModelManager manager = (JavaModelManager) JavaModelManager.getJavaModelManager();
		for (int i = 0; i <fBinaryChildren.length; i++) {
			manager.removeInfo(fBinaryChildren[i]);
		}
		fBinaryChildren = fgEmptyChildren;
	}
}
}
