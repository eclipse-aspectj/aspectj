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

public class ProblemReferenceBinding extends ReferenceBinding {
	public Binding original;
	private int problemId;
// NOTE: must only answer the subset of the name related to the problem

public ProblemReferenceBinding(char[][] compoundName, int problemId) {
	this(compoundName, null, problemId);
}
public ProblemReferenceBinding(char[] name, int problemId) {
	this(new char[][] {name}, null, problemId);
}

public ProblemReferenceBinding(char[][] compoundName, Binding original, int problemId) {
	this.compoundName = compoundName;
	this.original = original;
	this.problemId = problemId;
}
public ProblemReferenceBinding(char[] name, Binding original, int problemId) {
	this(new char[][] {name}, original, problemId);
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return problemId;
}
}
