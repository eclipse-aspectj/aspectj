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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.compiler.impl.Constant;

/**
 * Element info for IField elements.
 */

/* package */ class SourceFieldElementInfo extends MemberElementInfo implements ISourceField {

	/**
	 * The type name of this field.
	 */
	protected char[] fTypeName;
	
	/**
	 * The field's constant value
	 */
	protected Constant fConstant;
/**
 * Constructs an info object for the given field.
 */
protected SourceFieldElementInfo() {
	fConstant = Constant.NotAConstant;	 
}
/**
 * Returns the constant associated with this field or
 * Constant.NotAConstant if the field is not constant.
 */
public Constant getConstant() {
	return fConstant;
}
/**
 * Returns the type name of the field.
 */
public char[] getTypeName() {
	return fTypeName;
}
/**
 * Returns the type signature of the field.
 *
 * @see Signature
 */
protected String getTypeSignature() {
	return Signature.createTypeSignature(fTypeName, false);
}
/**
 * Returns the constant associated with this field or
 * Constant.NotAConstant if the field is not constant.
 */
public void setConstant(Constant constant) {
	fConstant = constant; 
}
/**
 * Sets the type name of the field.
 */
protected void setTypeName(char[] typeName) {
	fTypeName= typeName;
}
}
