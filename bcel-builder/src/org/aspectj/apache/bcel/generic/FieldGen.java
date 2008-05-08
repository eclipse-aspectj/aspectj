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

import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantObject;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantValue;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnotations;

/** 
 * Template class for building up a field.  The only extraordinary thing
 * one can do is to add a constant value attribute to a field (which must of
 * course be compatible with the declared type).
 *
 * @version $Id: FieldGen.java,v 1.4.8.3 2008/05/08 19:26:45 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see Field
 */
public class FieldGen extends FieldGenOrMethodGen {
  private Object value = null;

  /**
   * Declare a field. If it is static (isStatic() == true) and has a
   * basic type like int or String it may have an initial value
   * associated with it as defined by setInitValue().
   *
   * @param access_flags access qualifiers
   * @param type  field type
   * @param name field name
   * @param cp constant pool
   */
  public FieldGen(int access_flags, Type type, String name, ConstantPool cp) {
    setModifiers(access_flags);
    setType(type);
    setName(name);
    setConstantPool(cp);
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

    for(int i=0; i < attrs.length; i++) {
      if(attrs[i] instanceof ConstantValue) {
	    setValue(((ConstantValue)attrs[i]).getConstantValueIndex());
      } else if (attrs[i] instanceof RuntimeAnnotations) {
		RuntimeAnnotations runtimeAnnotations = (RuntimeAnnotations)attrs[i];
		List l = runtimeAnnotations.getAnnotations();
		for (Iterator it = l.iterator(); it.hasNext();) {
			AnnotationGen element = (AnnotationGen) it.next();
			addAnnotation(new AnnotationGen(element,cp,false));
		}
      } else {
	    addAttribute(attrs[i]);
      }
    }
  }

  private void setValue(int index) {
    ConstantPool cp  = this.cp;
    Constant     c   = cp.getConstant(index);
    value = ((ConstantObject)c).getConstantValue(cp);
  }


  public void wipeValue() {
    value = null;
  }

  private void checkType(Type atype) {
    if(type == null)
      throw new ClassGenException("You haven't defined the type of the field yet");
    
    if(!isFinal())
      throw new ClassGenException("Only final fields may have an initial value!");

    if(!type.equals(atype))
      throw new ClassGenException("Types are not compatible: " + type + " vs. " + atype);
  }

  /**
   * Get field object after having set up all necessary values.
   */
  public Field getField() {
    String      signature       = getSignature();
    int         name_index      = cp.addUtf8(name);
    int         signature_index = cp.addUtf8(signature);

    if(value != null) {
      checkType(type);
      int index = addConstant();
      addAttribute(new ConstantValue(cp.addUtf8("ConstantValue"),
				     2, index, cp));
    }
    
     addAnnotationsAsAttribute(cp);

    return new Field(modifiers, name_index, signature_index, getAttributesImmutable(), cp);
  }

  private int addConstant() {
    switch(type.getType()) {
    case Constants.T_INT: case Constants.T_CHAR: case Constants.T_BYTE:
    case Constants.T_BOOLEAN: case Constants.T_SHORT:
      return cp.addInteger(((Integer)value).intValue());
      
    case Constants.T_FLOAT:
      return cp.addFloat(((Float)value).floatValue());

    case Constants.T_DOUBLE:
      return cp.addDouble(((Double)value).doubleValue());

    case Constants.T_LONG:
      return cp.addLong(((Long)value).longValue());

    case Constants.T_REFERENCE:
      return cp.addString(((String)value));

    default:
      throw new RuntimeException("Oops: Unhandled : " + type.getType());
    }
  }

  public String  getSignature()  { return type.getSignature(); }


  public String getInitialValue() {
    if(value != null) {
      return value.toString();
    } else
      return null;
  }

  /**
   * Return string representation close to declaration format,
   * `public static final short MAX = 100', e.g..
   *
   * @return String representation of field
   */
  public final String toString() {
    String name, signature, access; // Short cuts to constant pool

    access    = Utility.accessToString(modifiers);
    access    = access.equals("")? "" : (access + " ");
    signature = type.toString();
    name      = getName();

    StringBuffer buf = new StringBuffer(access + signature + " " + name);
    String value = getInitialValue();

    if(value != null)
      buf.append(" = " + value);
    
    
    // TODO: Add attributes and annotations to the string

    return buf.toString();
  }

  /** @return deep copy of this field
   */
  public FieldGen copy(ConstantPool cp) {
    FieldGen fg = (FieldGen)clone();

    fg.setConstantPool(cp);
    return fg;
  }
}
