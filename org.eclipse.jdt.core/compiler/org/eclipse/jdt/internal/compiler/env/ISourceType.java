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
package org.eclipse.jdt.internal.compiler.env;

public interface ISourceType extends IGenericType {
/**
 * Answer the source end position of the type's declaration.
 */

int getDeclarationSourceEnd();
/**
 * Answer the source start position of the type's declaration.
 */

int getDeclarationSourceStart();
/**
 * Answer the enclosing type
 * or null if the receiver is a top level type.
 */

ISourceType getEnclosingType();
/**
 * Answer the receiver's fields or null if the array is empty.
 *
 * NOTE: Multiple fields with the same name can exist in the result.
 */

ISourceField[] getFields();
/**
 * Answer the unresolved names of the receiver's imports
 * or null if the array is empty.
 *
 * An import is a qualified, dot separated name.
 * For example, java.util.Hashtable or java.lang.*.
 */

char[][] getImports();
/**
 * Answer the unresolved names of the receiver's interfaces
 * or null if the array is empty.
 *
 * A name is a simple name or a qualified, dot separated name.
 * For example, Hashtable or java.util.Hashtable.
 */

char[][] getInterfaceNames();
/**
 * Answer the receiver's member types
 * or null if the array is empty.
 */

ISourceType[] getMemberTypes();
/**
 * Answer the receiver's methods or null if the array is empty.
 *
 * NOTE: Multiple methods with the same name & parameter types can exist in the result.
 */

ISourceMethod[] getMethods();
/**
 * Answer the simple source name of the receiver.
 */

char[] getName();
/**
 * Answer the source end position of the type's name.
 */

int getNameSourceEnd();
/**
 * Answer the source start position of the type's name.
 */

int getNameSourceStart();
/**
 * Answer the qualified name of the receiver's package separated by periods
 * or null if its the default package.
 *
 * For example, {java.util.Hashtable}.
 */

char[] getPackageName();
/**
 * Answer the qualified name of the receiver.
 *
 * The name is a qualified, dot separated name.
 * For example, java.util.Hashtable.
 */

char[] getQualifiedName();
/**
 * Answer the unresolved name of the receiver's superclass
 * or null if it does not have one.
 *
 * The name is a simple name or a qualified, dot separated name.
 * For example, Hashtable or java.util.Hashtable.
 */

char[] getSuperclassName();
}
