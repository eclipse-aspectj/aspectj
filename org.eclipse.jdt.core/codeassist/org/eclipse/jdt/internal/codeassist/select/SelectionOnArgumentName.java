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
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnArgumentName extends Argument {
	public SelectionOnArgumentName(char[] name , long posNom , TypeReference tr , int modifiers){
		super(name, posNom, tr, modifiers);
	}
	
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		throw new SelectionNodeFound(binding);
	}
	
	public void bind(MethodScope scope, TypeBinding typeBinding, boolean used) {
		super.bind(scope, typeBinding, used);
		
		throw new SelectionNodeFound(binding);
	}
	
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<SelectionOnArgumentName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(name());
		if (initialization != null) s += " = " + initialization.toStringExpression(); //$NON-NLS-1$
		s += ">"; //$NON-NLS-1$
		return s;
	}
}
