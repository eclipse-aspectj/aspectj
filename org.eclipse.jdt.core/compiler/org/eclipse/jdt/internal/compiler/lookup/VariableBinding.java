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

import org.eclipse.jdt.internal.compiler.impl.Constant;

public abstract class VariableBinding extends Binding {
	public int modifiers;
	public TypeBinding type;
	public char[] name;
	public Constant constant;
	public int id; // for flow-analysis (position in flowInfo bit vector)
public boolean isConstantValue() {
	return constant != Constant.NotAConstant;
}
/* Answer true if the receiver is final and cannot be changed
*/

public final boolean isFinal() {
	return (modifiers & AccFinal) != 0;
}
public char[] readableName() {
	return name;
}
public String toString() {
	String s = (type != null) ? type.debugName() : "UNDEFINED TYPE"; //$NON-NLS-1$
	s += " "; //$NON-NLS-1$
	s += (name != null) ? new String(name) : "UNNAMED FIELD"; //$NON-NLS-1$
	return s;
}
}
