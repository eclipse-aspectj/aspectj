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

public class ProblemMethodBinding extends MethodBinding {
	private int problemId;
	public MethodBinding closestMatch;
public ProblemMethodBinding(char[] selector, TypeBinding[] args, int problemId) {
	this.selector = selector;
	this.parameters = (args == null || args.length == 0) ? NoParameters : args;
	this.problemId = problemId;
}
public ProblemMethodBinding(char[] selector, TypeBinding[] args, ReferenceBinding declaringClass, int problemId) {
	this.selector = selector;
	this.parameters = (args == null || args.length == 0) ? NoParameters : args;
	this.declaringClass = declaringClass;
	this.problemId = problemId;
}
public ProblemMethodBinding(MethodBinding closestMatch, char[] selector, TypeBinding[] args, int problemId) {
	this(selector, args, problemId);
	this.closestMatch = closestMatch;
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return problemId;
}
}
