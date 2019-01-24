package org.aspectj.apache.bcel.classfile;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.generic.Type;

/**
 * This class represents the field info structure, i.e., the representation for a variable in the class. See JVM specification for
 * details.
 * 
 * @version $Id: Field.java,v 1.6 2009/09/15 03:33:52 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public final class Field extends FieldOrMethod {

	public static final Field[] NoFields = new Field[0];

	private Type fieldType = null; // lazily initialized

	private Field() {
	}

	public Field(Field c) {
		super(c);
	}

	Field(DataInputStream dis, ConstantPool cpool) throws IOException {
		super(dis, cpool);
	}

	public Field(int modifiers, int nameIndex, int signatureIndex, Attribute[] attributes, ConstantPool cpool) {
		super(modifiers, nameIndex, signatureIndex, attributes, cpool);
	}

	public void accept(ClassVisitor v) {
		v.visitField(this);
	}

	/**
	 * @return constant value associated with this field (may be null)
	 */
	public final ConstantValue getConstantValue() {
		return AttributeUtils.getConstantValueAttribute(attributes);
	}

	/**
	 * Return string representation close to declaration format, eg: 'public static final short MAX = 100'
	 */
	@Override
	public final String toString() {
		// Get names from constant pool
		StringBuffer buf = new StringBuffer(Utility.accessToString(modifiers));
		if (buf.length() > 0) {
			buf.append(" ");
		}
		String signature = Utility.signatureToString(getSignature());

		buf.append(signature).append(" ").append(getName());

		ConstantValue cv = getConstantValue();
		if (cv != null) {
			buf.append(" = ").append(cv);
		}

		// append all attributes that are *not* "ConstantValue"
		for (Attribute a : attributes) {
			if (!(a instanceof ConstantValue)) {
				buf.append(" [").append(a.toString()).append("]");
			}
		}

		return buf.toString();
	}

	/** return the type of the field */
	public Type getType() {
		if (fieldType == null) {
			fieldType = Type.getReturnType(getSignature());
		}
		return fieldType;
	}
}
