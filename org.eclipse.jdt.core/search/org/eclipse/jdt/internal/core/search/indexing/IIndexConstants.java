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
package org.eclipse.jdt.internal.core.search.indexing;

public interface IIndexConstants {

	/* index encoding */
	char[] REF= "ref/".toCharArray(); //$NON-NLS-1$
	char[] FIELD_REF= "fieldRef/".toCharArray(); //$NON-NLS-1$
	char[] METHOD_REF= "methodRef/".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_REF= "constructorRef/".toCharArray(); //$NON-NLS-1$
	char[] TYPE_REF= "typeRef/".toCharArray(); //$NON-NLS-1$
	char[] SUPER_REF = "superRef/".toCharArray(); //$NON-NLS-1$
	char[] TYPE_DECL = "typeDecl/".toCharArray(); //$NON-NLS-1$
	int 	TYPE_DECL_LENGTH = 9;
	char[] CLASS_DECL= "typeDecl/C/".toCharArray(); //$NON-NLS-1$
	char[] INTERFACE_DECL= "typeDecl/I/".toCharArray(); //$NON-NLS-1$
	char[] METHOD_DECL= "methodDecl/".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_DECL= "constructorDecl/".toCharArray(); //$NON-NLS-1$
	char[] FIELD_DECL= "fieldDecl/".toCharArray(); //$NON-NLS-1$
	char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
	char[][] COUNTS= 
		new char[][] { new char[] {'0'}, new char[] {'1'}, new char[] {'2'}, new char[] {'3'}, new char[] {'4'}, new char[] {'5'}, new char[] {'6'}, new char[] {'7'}, new char[] {'8'}, new char[] {'9'}
	};
	char CLASS_SUFFIX = 'C';
	char INTERFACE_SUFFIX = 'I';
	char TYPE_SUFFIX = 0;
	char SEPARATOR= '/';

	char[] ONE_STAR = new char[] {'*'};
	char[][] ONE_STAR_CHAR = new char[][] {ONE_STAR};
	char[] NO_CHAR = new char[0];
	char[][] NO_CHAR_CHAR = new char[0][];

	// used as special marker for enclosing type name of local and anonymous classes
	char[] ONE_ZERO = new char[] {'0'}; 
	char[][] ONE_ZERO_CHAR = new char[][] {ONE_ZERO};
}
