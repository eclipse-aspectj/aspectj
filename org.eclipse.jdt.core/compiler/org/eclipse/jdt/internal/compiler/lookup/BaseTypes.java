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
package org.eclipse.jdt.internal.compiler.lookup;

public interface BaseTypes {
	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, "int".toCharArray(), new char[] {'I'}); //$NON-NLS-1$
	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, "byte".toCharArray(), new char[] {'B'}); //$NON-NLS-1$
	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, "short".toCharArray(), new char[] {'S'}); //$NON-NLS-1$
	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, "char".toCharArray(), new char[] {'C'}); //$NON-NLS-1$
	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, "long".toCharArray(), new char[] {'J'}); //$NON-NLS-1$
	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, "float".toCharArray(), new char[] {'F'}); //$NON-NLS-1$
	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, "double".toCharArray(), new char[] {'D'}); //$NON-NLS-1$
	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, "boolean".toCharArray(), new char[] {'Z'}); //$NON-NLS-1$
	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, "null".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$
	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, "void".toCharArray(), new char[] {'V'}); //$NON-NLS-1$
}
