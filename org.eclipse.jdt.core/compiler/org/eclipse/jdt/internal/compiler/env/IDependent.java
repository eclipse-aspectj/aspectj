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
 * This represents the target (i.e.&nbsp;the file) of a type dependency.
 *
 * All implementors of this interface are containers for types or types
 * themselves which must be able to identify their source file name
 * when file dependencies are collected.
 */
public interface IDependent {
/**
 * Answer the file name which defines the type.
 *
 * The path part (optional) must be separated from the actual
 * file proper name by a java.io.File.separator.
 *
 * The proper file name includes the suffix extension (e.g.&nbsp;".java")
 *
 * e.g.&nbsp;"c:/com/ibm/compiler/java/api/Compiler.java" 
 */

char[] getFileName();
}
