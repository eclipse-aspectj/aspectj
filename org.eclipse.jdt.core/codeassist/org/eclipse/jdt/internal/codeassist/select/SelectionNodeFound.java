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

import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionNodeFound extends RuntimeException {
	public Binding binding;
public SelectionNodeFound() {
	this(null); // we found a problem in the selection node
}
public SelectionNodeFound(Binding binding) {
	this.binding = binding;
}
}
