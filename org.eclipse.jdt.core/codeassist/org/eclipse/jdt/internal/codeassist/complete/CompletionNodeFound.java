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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class CompletionNodeFound extends RuntimeException {
	public AstNode astNode;
	public Binding qualifiedBinding;
	public Scope scope;
public CompletionNodeFound() {
	this(null, null, null); // we found a problem in the completion node
}
public CompletionNodeFound(AstNode astNode, Binding qualifiedBinding, Scope scope) {
	this.astNode = astNode;
	this.qualifiedBinding = qualifiedBinding;
	this.scope = scope;
}
public CompletionNodeFound(AstNode astNode, Scope scope) {
	this(astNode, null, scope);
}
}
