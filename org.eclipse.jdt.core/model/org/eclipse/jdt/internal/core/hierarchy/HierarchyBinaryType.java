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
package org.eclipse.jdt.internal.core.hierarchy;

import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class HierarchyBinaryType implements IBinaryType {
	private int modifiers;
	private boolean isClass;
	private char[] name;
	private char[] enclosingTypeName;
	private char[] superclass;
	private char[][] superInterfaces = NoInterface;
	
public HierarchyBinaryType(int modifiers, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface){

	this.modifiers = modifiers;
	this.isClass = classOrInterface == IIndexConstants.CLASS_SUFFIX;
	if (enclosingTypeName == null){
		this.name = CharOperation.concat(qualification, typeName, '/');
	} else {
		this.name = CharOperation.concat(qualification, '/', enclosingTypeName, '$', typeName); //rebuild A$B name
		this.enclosingTypeName = CharOperation.concat(qualification, enclosingTypeName,'/');
		CharOperation.replace(this.enclosingTypeName, '.', '/');
	}
	CharOperation.replace(this.name, '.', '/');
}
/**
 * Answer the resolved name of the enclosing type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the receiver is a top level type.
 *
 * For example, java.lang.String is java/lang/String.
 */
public char[] getEnclosingTypeName() {
	return this.enclosingTypeName;
}
/**
 * Answer the receiver's fields or null if the array is empty.
 */
public IBinaryField[] getFields() {
	return null;
}
/**
 * Answer the file name which defines the type.
 *
 * The path part (optional) must be separated from the actual
 * file proper name by a java.io.File.separator.
 *
 * The proper file name includes the suffix extension (e.g. ".java")
 *
 * e.g. "c:/com/ibm/compiler/java/api/Compiler.java" 
 */
public char[] getFileName() {
	return null;
}
/**
 * Answer the resolved names of the receiver's interfaces in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 */
public char[][] getInterfaceNames() {
	return this.superInterfaces;
}
/**
 * Answer the receiver's nested types or null if the array is empty.
 *
 * This nested type info is extracted from the inner class attributes.
 * Ask the name environment to find a member type using its compound name.
 */
public IBinaryNestedType[] getMemberTypes() {
	return null;
}
/**
 * Answer the receiver's methods or null if the array is empty.
 */
public IBinaryMethod[] getMethods() {
	return null;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 */
public int getModifiers() {
	return this.modifiers;
}
/**
 * Answer the resolved name of the type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec.
 *
 * For example, java.lang.String is java/lang/String.
 */
public char[] getName() {
	return this.name;
}
/**
 * Answer the resolved name of the receiver's superclass in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if it does not have one.
 *
 * For example, java.lang.String is java/lang/String.
 */
public char[] getSuperclassName() {
	return this.superclass;
}
public boolean isAnonymous() {
	return false; // index did not record this information (since unused for hierarchies)
}

/**
 * Answer whether the receiver contains the resolved binary form
 * or the unresolved source form of the type.
 */
public boolean isBinaryType() {
	return true;
}
/**
 * isClass method comment.
 */
public boolean isClass() {
	return this.isClass;
}
/**
 * isInterface method comment.
 */
public boolean isInterface() {
	return !this.isClass;
}
public boolean isLocal() {
	return false;  // index did not record this information (since unused for hierarchies)
}
public boolean isMember() {
	return false;  // index did not record this information (since unused for hierarchies)
}

public void recordSuperType(char[] superTypeName, char[] superQualification, char superClassOrInterface){

	// index encoding of p.A$B was B/p.A$, rebuild the proper name
	if (superQualification != null){
		int length = superQualification.length;
		if (superQualification[length-1] == '$'){
			char[] enclosingSuperName = CharOperation.lastSegment(superQualification, '.');
			superTypeName = CharOperation.concat(enclosingSuperName, superTypeName);
			superQualification = CharOperation.subarray(superQualification, 0, length - enclosingSuperName.length - 1);
		}
	}
	
	if (superClassOrInterface == IIndexConstants.CLASS_SUFFIX){
		// interfaces are indexed as having superclass references to Object by default,
		// this is an artifact used for being able to query them only.
		if (!this.isClass) return; 
		char[] encodedName = CharOperation.concat(superQualification, superTypeName, '/');
		CharOperation.replace(encodedName, '.', '/'); 
		this.superclass = encodedName;
	} else {
		char[] encodedName = CharOperation.concat(superQualification, superTypeName, '/');
		CharOperation.replace(encodedName, '.', '/'); 
		if (this.superInterfaces == NoInterface){
			this.superInterfaces = new char[][] { encodedName };
		} else {
			int length = this.superInterfaces.length;
			System.arraycopy(this.superInterfaces, 0, this.superInterfaces = new char[length+1][], 0, length);
			this.superInterfaces[length] = encodedName;
		}
	}
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	if (this.modifiers == IConstants.AccPublic) {
		buffer.append("public "); //$NON-NLS-1$
	}
	if (this.isClass()) {
		buffer.append("class "); //$NON-NLS-1$
	} else {
		buffer.append("interface "); //$NON-NLS-1$
	}
	if (this.name != null) {
		buffer.append(this.name);
	}
	if (this.superclass != null) {
		buffer.append("\n  extends "); //$NON-NLS-1$
		buffer.append(this.superclass);
	}
	int length;
	if (this.superInterfaces != null && (length = this.superInterfaces.length) != 0) {
		buffer.append("\n implements "); //$NON-NLS-1$
		for (int i = 0; i < length; i++) {
			buffer.append(this.superInterfaces[i]);
			if (i != length - 1) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}
	}
	return buffer.toString();
}

/**
 * @see org.eclipse.jdt.internal.compiler.env.IBinaryType
 */
public char[] sourceFileName() {
	return null;
}
}
