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

/**
 * This interface denotes a compilation unit, providing its name and content.
 */
public interface ICompilationUnit extends IDependent {
/**
 * Answer the contents of the compilation unit.
 *
 * In normal use, the contents are requested twice.
 * Once during the initial lite parsing step, then again for the
 * more detailed parsing step.
 */
char[] getContents();
/**
 * Answer the name of the top level public type.
 * For example, {Hashtable}.
 */
char[] getMainTypeName();
/**
 * Answer the name of the package according to the directory structure
 * or null if package consistency checks should be ignored.
 * For example, {java, lang}.
 */
char[][] getPackageName();
}
