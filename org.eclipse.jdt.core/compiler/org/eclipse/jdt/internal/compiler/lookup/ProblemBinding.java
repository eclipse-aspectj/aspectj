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

import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class ProblemBinding extends Binding {
	public char[] name;
	public ReferenceBinding searchType;
	private int problemId;
// NOTE: must only answer the subset of the name related to the problem

public ProblemBinding(char[][] compoundName, int problemId) {
	this(CharOperation.concatWith(compoundName, '.'), problemId);
}
// NOTE: must only answer the subset of the name related to the problem

public ProblemBinding(char[][] compoundName, ReferenceBinding searchType, int problemId) {
	this(CharOperation.concatWith(compoundName, '.'), searchType, problemId);
}
ProblemBinding(char[] name, int problemId) {
	this.name = name;
	this.problemId = problemId;
}
ProblemBinding(char[] name, ReferenceBinding searchType, int problemId) {
	this(name, problemId);
	this.searchType = searchType;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return VARIABLE | TYPE;
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return problemId;
}
public char[] readableName() {
	return name;
}
}
