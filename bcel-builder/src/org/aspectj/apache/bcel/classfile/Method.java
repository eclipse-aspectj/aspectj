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

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisibleParameterAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleParameterAnnotations;
import org.aspectj.apache.bcel.generic.Type;

/**
 * This class represents the method info structure, i.e., the representation 
 * for a method in the class. See JVM specification for details.
 * A method has access flags, a name, a signature and a number of attributes.
 *
 * @version $Id: Method.java,v 1.2.10.1 2007/02/12 09:34:02 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public final class Method extends FieldOrMethod {
	
  public static final Method[] NoMethods = new Method[0];
  
  // annotations on parameters of this method
  private boolean parameterAnnotationsOutOfDate = true;
  private RuntimeVisibleParameterAnnotations parameterAnnotationsVis; 
  private RuntimeInvisibleParameterAnnotations parameterAnnotationsInvis;

  private Method() { }

  /**
   * Initialize from another object. Note that both objects use the same
   * references (shallow copy). Use clone() for a physical copy.
   */
  public Method(Method c) {
    super(c);
  }

  Method(DataInputStream file, ConstantPool constant_pool) throws IOException {
    super(file, constant_pool);
  }

  public Method(int access_flags, int name_index, int signature_index, Attribute[] attributes, ConstantPool constant_pool) {
    super(access_flags, name_index, signature_index, attributes, constant_pool);
  }

  public void accept(Visitor v) {
    v.visitMethod(this);
  }
  
  public final Code getCode() {
	return AttributeUtils.getCodeAttribute(attributes);
  }

  public final ExceptionTable getExceptionTable() {
    return AttributeUtils.getExceptionTableAttribute(attributes);
  }

  /** 
   * Return LocalVariableTable of code attribute if any (the call is forwarded
   * to the Code attribute)
   */
  public final LocalVariableTable getLocalVariableTable() {
    Code code = getCode();
    if (code != null) return code.getLocalVariableTable();
    return null;
  }

  /** 
   * Return LineNumberTable of code attribute if any (the call is forwarded
   * to the Code attribute)
   */
  public final LineNumberTable getLineNumberTable() {
    Code code = getCode();
    if (code != null) return code.getLineNumberTable();
    return null;
  }

  /**
   * Return string representation close to declaration format, eg:
   * 'public static void main(String[] args) throws IOException'
   */
  public final String toString() {
    ConstantUtf8  c;
    String        name, signature, access; // Short cuts to constant pool
    StringBuffer  buf;

    access = Utility.accessToString(accessflags);

    // Get name and signature from constant pool
    c = (ConstantUtf8)cpool.getConstant(signatureIndex, 
						Constants.CONSTANT_Utf8);
    signature = c.getBytes();

    c = (ConstantUtf8)cpool.getConstant(nameIndex, Constants.CONSTANT_Utf8);
    name = c.getBytes();

    signature = Utility.methodSignatureToString(signature, name, access, true,
						getLocalVariableTable());
    buf = new StringBuffer(signature);

    for(int i=0; i < attributes.length; i++) {
      Attribute a = attributes[i];
      if(!((a instanceof Code) || (a instanceof ExceptionTable))) buf.append(" [" + a.toString() + "]");
    }

    ExceptionTable e = getExceptionTable();
    if(e != null) {
      String str = e.toString();
      if(!str.equals(""))
	buf.append("\n\t\tthrows " + str);
    }
 
    return buf.toString();
  }

  /**
   * Return a deep copy of this method
   */
  public final Method copy(ConstantPool constant_pool) {
    return (Method)copy_(constant_pool);
  }

  /**
   * @return return type of method
   */
  public Type getReturnType() {
    return Type.getReturnType(getSignature());
  }

  /**
   * @return array of method argument types
   */
  public Type[] getArgumentTypes() {
    return Type.getArgumentTypes(getSignature());
  }
  
  private void ensureParameterAnnotationsUnpacked() {
  	if (parameterAnnotationsOutOfDate) { 
  		// Find attributes that contain annotation data
  		
  		for (int i = 0; i < attributes.length; i++) {
			Attribute attribute = attributes[i];
			if (attribute instanceof RuntimeVisibleParameterAnnotations) {				
				parameterAnnotationsVis = (RuntimeVisibleParameterAnnotations)attribute;
			}
			if (attribute instanceof RuntimeInvisibleParameterAnnotations) {				
				parameterAnnotationsInvis = (RuntimeInvisibleParameterAnnotations)attribute;
			}
		}
  		parameterAnnotationsOutOfDate = false;
  	}
  }

  public AnnotationGen[] getAnnotationsOnParameter(int i) {
  	ensureParameterAnnotationsUnpacked();
  	
  	AnnotationGen[] visibleOnes = AnnotationGen.NO_ANNOTATIONS;
  	if (parameterAnnotationsVis!=null) visibleOnes = parameterAnnotationsVis.getAnnotationsOnParameter(i);
  	AnnotationGen[] invisibleOnes = AnnotationGen.NO_ANNOTATIONS;
  	if (parameterAnnotationsInvis!=null) invisibleOnes = parameterAnnotationsInvis.getAnnotationsOnParameter(i);
  	AnnotationGen[] complete = new AnnotationGen[visibleOnes.length+invisibleOnes.length];
  	System.arraycopy(visibleOnes,0,complete,0,visibleOnes.length);
  	System.arraycopy(invisibleOnes,0,complete,visibleOnes.length,invisibleOnes.length);
    return complete;
  }
    
}
