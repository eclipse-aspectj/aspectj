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
package org.eclipse.jdt.internal.compiler.ast;

public abstract class AbstractVariableDeclaration extends Statement {
	public int modifiers;

	public TypeReference type;
	public Expression initialization;

	public char[] name;
	public int declarationEnd;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiersSourceStart;
	public AbstractVariableDeclaration() {
	}
	public abstract String name();
	
	public String toString(int tab) {

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		s += type.toString(0) + " " + new String(name()); //$NON-NLS-1$
		if (initialization != null)
			s += " = " + initialization.toStringExpression(tab); //$NON-NLS-1$
		return s;
	}
}