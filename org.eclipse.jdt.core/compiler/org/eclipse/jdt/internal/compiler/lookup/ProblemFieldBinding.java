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

public class ProblemFieldBinding extends FieldBinding {
	private int problemId;
// NOTE: must only answer the subset of the name related to the problem

public ProblemFieldBinding(ReferenceBinding declaringClass, char[][] compoundName, int problemId) {
	this(declaringClass, CharOperation.concatWith(compoundName, '.'), problemId);
}
public ProblemFieldBinding(ReferenceBinding declaringClass, char[] name, int problemId) {
	this.declaringClass = declaringClass;
	this.name = name;
	this.problemId = problemId;
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return problemId;
}
}
