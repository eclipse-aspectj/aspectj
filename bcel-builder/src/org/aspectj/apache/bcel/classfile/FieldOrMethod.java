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
import  org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnotations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** 
 * Abstract super class for fields and methods.
 *
 * @version $Id: FieldOrMethod.java,v 1.5 2005/06/26 20:27:23 acolyer Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class FieldOrMethod extends AccessFlags implements Cloneable, Node {
  protected int          name_index;      // Points to field name in constant pool 
  protected int          signature_index; // Points to encoded signature
  protected int          attributes_count;// No. of attributes
  protected Attribute[]  attributes;      // Collection of attributes
  private   Annotation[] annotations;     // annotations defined on the field or method 
  protected ConstantPool constant_pool;

  private String signatureAttributeString = null;
  private boolean searchedForSignatureAttribute = false;
  

  // Annotations are collected from certain attributes, don't do it more than necessary!
  private boolean annotationsOutOfDate = true;

  FieldOrMethod() {}
  
  /**
   * Initialize from another object. Note that both objects use the same
   * references (shallow copy). Use clone() for a physical copy.
   */
  protected FieldOrMethod(FieldOrMethod c) {
    this(c.getAccessFlags(), c.getNameIndex(), c.getSignatureIndex(),
	 c.getAttributes(), c.getConstantPool());
  }

  /**
   * Construct object from file stream.
   * @param file Input stream
   * @throws IOException
   * @throws ClassFormatException
   */
  protected FieldOrMethod(DataInputStream file, ConstantPool constant_pool)
    throws IOException, ClassFormatException
  {
    this(file.readUnsignedShort(), file.readUnsignedShort(),
	 file.readUnsignedShort(), null, constant_pool);

    attributes_count = file.readUnsignedShort();
    attributes       = new Attribute[attributes_count];
    for(int i=0; i < attributes_count; i++)
      attributes[i] = Attribute.readAttribute(file, constant_pool);
  }

  /**
   * @param access_flags Access rights of method
   * @param name_index Points to field name in constant pool
   * @param signature_index Points to encoded signature
   * @param attributes Collection of attributes
   * @param constant_pool Array of constants
   */
  protected FieldOrMethod(int access_flags, int name_index, int signature_index,
			  Attribute[] attributes, ConstantPool constant_pool)
  {
    this.access_flags    = access_flags;
    this.name_index      = name_index;
    this.signature_index = signature_index;
    this.constant_pool   = constant_pool;

    setAttributes(attributes);
  }

  /**
   * Dump object to file stream on binary format.
   *
   * @param file Output file stream
   * @throws IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
    file.writeShort(access_flags);
    file.writeShort(name_index);
    file.writeShort(signature_index);
    file.writeShort(attributes_count);

    for(int i=0; i < attributes_count; i++)
      attributes[i].dump(file);
  }

  /**
   * @return Collection of object attributes.
   */   
  public final Attribute[] getAttributes() { return attributes; }

  /**
   * @param attributes Collection of object attributes.
   */
  public void setAttributes(Attribute[] attributes) {
    this.attributes  = attributes;
    attributes_count = (attributes == null)? 0 : attributes.length;
  }

  /**
   * @return Constant pool used by this object.
   */   
  public final ConstantPool getConstantPool() { return constant_pool; }

  /**
   * @param constant_pool Constant pool to be used for this object.
   */   
  public final void setConstantPool(ConstantPool constant_pool) {
    this.constant_pool = constant_pool;
  }

  /**
   * @return Index in constant pool of object's name.
   */   
  public final int getNameIndex() { return name_index; }

  /**
   * @param name_index Index in constant pool of object's name.
   */
  public final void setNameIndex(int name_index) {
    this.name_index = name_index;
  }

  /**
   * @return Index in constant pool of field signature.
   */   
  public final int getSignatureIndex() { return signature_index; }    

  /**
   * @param signature_index Index in constant pool of field signature.
   */
  public final void setSignatureIndex(int signature_index) {
    this.signature_index = signature_index;
  }

  /**
   * @return Name of object, i.e., method name or field name
   */   
  public final String getName() {
    ConstantUtf8  c;
    c = (ConstantUtf8)constant_pool.getConstant(name_index, 
						Constants.CONSTANT_Utf8);
    return c.getBytes();
  }

  /**
   * @return String representation of object's type signature (java style)
   */   
  public final String getSignature() {
    ConstantUtf8  c;
    c = (ConstantUtf8)constant_pool.getConstant(signature_index,
						Constants.CONSTANT_Utf8);
    return c.getBytes();
  }

  /**
   * This will return the contents of a signature attribute attached to a member, or if there
   * is none it will return the same as 'getSignature()'.  Signature attributes are attached
   * to members that were declared generic.
   */
  public final String getDeclaredSignature() {
    if (getGenericSignature()!=null) return getGenericSignature();
	return getSignature();  
  }
  
  /**
   * @return deep copy of this field
   */
  protected FieldOrMethod copy_(ConstantPool constant_pool) {
    FieldOrMethod c = null;

    try {
      c = (FieldOrMethod)clone();
    } catch(CloneNotSupportedException e) {}

    c.constant_pool    = constant_pool;
    c.attributes       = new Attribute[attributes_count];

    for(int i=0; i < attributes_count; i++)
      c.attributes[i] = attributes[i].copy(constant_pool);

    return c;
  }
  

  /**
   * Ensure we have unpacked any attributes that contain annotations.
   * We don't remove these annotation attributes from the attributes list, they
   * remain there.
   */
  private void ensureAnnotationsUpToDate() {
  	if (annotationsOutOfDate) { 
  		// Find attributes that contain annotation data
  		Attribute[] attrs = getAttributes();
  		List accumulatedAnnotations = new ArrayList();
  		for (int i = 0; i < attrs.length; i++) {
			Attribute attribute = attrs[i];
			if (attribute instanceof RuntimeAnnotations) {				
				RuntimeAnnotations runtimeAnnotations = (RuntimeAnnotations)attribute;
				accumulatedAnnotations.addAll(runtimeAnnotations.getAnnotations());
			}
		}
  		annotations = (Annotation[])accumulatedAnnotations.toArray(new Annotation[]{});
  		annotationsOutOfDate = false;
  	}
  }
  
  public Annotation[] getAnnotations() {
    ensureAnnotationsUpToDate();
  	return annotations;
  }
  
  public void addAnnotation(Annotation a) {
    ensureAnnotationsUpToDate();
  	int len = annotations.length;
	Annotation[] newAnnotations = new Annotation[len+1];
	System.arraycopy(annotations, 0, newAnnotations, 0, len);
	newAnnotations[len] = a;
	annotations = newAnnotations;
  }
  
  
  /**
   * Hunts for a signature attribute on the member and returns its contents.  So where the 'regular' signature
   * may be (Ljava/util/Vector;)V the signature attribute may in fact say 'Ljava/lang/Vector<Ljava/lang/String>;'
   * Coded for performance - searches for the attribute only when requested - only searches for it once.
   */
  public final String getGenericSignature() {
	if (!searchedForSignatureAttribute) {
	  boolean found=false;
      for(int i=0; !found && i < attributes_count; i++) {
        if(attributes[i] instanceof Signature) {
		  signatureAttributeString = ((Signature)attributes[i]).getSignature();
		  found=true;
        }
      }
	  searchedForSignatureAttribute=true;
	}
	return signatureAttributeString;
  }
  
}
