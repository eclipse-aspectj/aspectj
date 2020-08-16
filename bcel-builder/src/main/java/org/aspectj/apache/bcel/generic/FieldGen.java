package org.aspectj.apache.bcel.generic;

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

import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantDouble;
import org.aspectj.apache.bcel.classfile.ConstantFloat;
import org.aspectj.apache.bcel.classfile.ConstantInteger;
import org.aspectj.apache.bcel.classfile.ConstantLong;
import org.aspectj.apache.bcel.classfile.ConstantObject;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantString;
import org.aspectj.apache.bcel.classfile.ConstantValue;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;

/**
 * Template class for building up a field. The only extraordinary thing one can do is to add a constant value attribute to a field
 * (which must of course be compatible with the declared type).
 * 
 * @version $Id: FieldGen.java,v 1.11 2011/10/03 22:41:24 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see Field
 */
public class FieldGen extends FieldGenOrMethodGen {
	private Object value = null;

	/**
	 * Declare a field. If it is static (isStatic() == true) and has a basic type like int or String it may have an initial value
	 * associated with it as defined by setInitValue().
	 * 
	 * @param modifiers access qualifiers
	 * @param type field type
	 * @param name field name
	 * @param cpool constant pool
	 */
	public FieldGen(int modifiers, Type type, String name, ConstantPool cpool) {
		setModifiers(modifiers);
		setType(type);
		setName(name);
		setConstantPool(cpool);
	}

	/**
	 * Instantiate from existing field.
	 * 
	 * @param field Field object
	 * @param cp constant pool (must contain the same entries as the field's constant pool)
	 */
	public FieldGen(Field field, ConstantPool cp) {
		this(field.getModifiers(), Type.getType(field.getSignature()), field.getName(), cp);

		Attribute[] attrs = field.getAttributes();

		for (Attribute attr : attrs) {
			if (attr instanceof ConstantValue) {
				setValue(((ConstantValue) attr).getConstantValueIndex());
			} else if (attr instanceof RuntimeAnnos) {
				RuntimeAnnos runtimeAnnotations = (RuntimeAnnos) attr;
				List<AnnotationGen> l = runtimeAnnotations.getAnnotations();
				for (AnnotationGen element : l) {
					addAnnotation(new AnnotationGen(element, cp, false));
				}
			} else {
				addAttribute(attr);
			}
		}
	}

	public void setValue(int index) {
		ConstantPool cp = this.cp;
		Constant c = cp.getConstant(index);
		if (c instanceof ConstantInteger) {
			value = ((ConstantInteger) c).getIntValue();
		} else if (c instanceof ConstantFloat) {
			value = ((ConstantFloat) c).getValue();
		} else if (c instanceof ConstantDouble) {
			value = ((ConstantDouble) c).getValue();
		} else if (c instanceof ConstantLong) {
			value = ((ConstantLong) c).getValue();
		} else if (c instanceof ConstantString) {
			value = ((ConstantString)c).getString(cp);
		} else {
			value = ((ConstantObject) c).getConstantValue(cp);
		}
	}

	public void setValue(String constantString) {
		value = constantString;
	}

	public void wipeValue() {
		value = null;
	}

	private void checkType(Type atype) {
		if (type == null)
			throw new ClassGenException("You haven't defined the type of the field yet");

		if (!isFinal())
			throw new ClassGenException("Only final fields may have an initial value!");

		if (!type.equals(atype))
			throw new ClassGenException("Types are not compatible: " + type + " vs. " + atype);
	}

	/**
	 * Get field object after having set up all necessary values.
	 */
	public Field getField() {
		String signature = getSignature();
		int nameIndex = cp.addUtf8(name);
		int signatureIndex = cp.addUtf8(signature);

		if (value != null) {
			checkType(type);
			int index = addConstant();
			addAttribute(new ConstantValue(cp.addUtf8("ConstantValue"), 2, index, cp));
		}

		addAnnotationsAsAttribute(cp);

		return new Field(modifiers, nameIndex, signatureIndex, getAttributesImmutable(), cp);
	}

	private int addConstant() {
		switch (type.getType()) {
		case Constants.T_INT:
		case Constants.T_CHAR:
		case Constants.T_BYTE:
		case Constants.T_BOOLEAN:
		case Constants.T_SHORT:
			return cp.addInteger((Integer) value);

		case Constants.T_FLOAT:
			return cp.addFloat((Float) value);

		case Constants.T_DOUBLE:
			return cp.addDouble((Double) value);

		case Constants.T_LONG:
			return cp.addLong((Long) value);

		case Constants.T_REFERENCE:
			return cp.addString(((String) value));

		default:
			throw new RuntimeException("Oops: Unhandled : " + type.getType());
		}
	}

	@Override
	public String getSignature() {
		return type.getSignature();
	}

	public String getInitialValue() {
		return (value == null ? null : value.toString());
	}

	public void setInitialStringValue(String value) {
		this.value = value;
	}

	/**
	 * Return string representation close to declaration format, `public static final short MAX = 100', e.g..
	 */
	@Override
	public final String toString() {
		String access = Utility.accessToString(modifiers);
		access = access.equals("") ? "" : (access + " ");
		String signature = type.toString();
		String name = getName();

		StringBuffer buf = new StringBuffer(access).append(signature).append(" ").append(name);
		String value = getInitialValue();

		if (value != null) {
			buf.append(" = ").append(value);
		}
		// TODO: Add attributes and annotations to the string
		return buf.toString();
	}

}
