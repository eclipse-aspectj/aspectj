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
package org.aspectj.apache.bcel.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisParamAnnos;
import org.aspectj.apache.bcel.generic.Type;

/**
 * This class represents the method info structure, i.e., the representation for a method in the class. See JVM specification for
 * details. A method has access flags, a name, a signature and a number of attributes.
 * 
 * @version $Id: Method.java,v 1.11 2009/09/15 19:40:12 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public final class Method extends FieldOrMethod {

	public static final AnnotationGen[][] NO_PARAMETER_ANNOTATIONS = new AnnotationGen[][] {};

	public static final Method[] NoMethods = new Method[0];

	private boolean parameterAnnotationsOutOfDate = true;
	private AnnotationGen[][] unpackedParameterAnnotations;

	private Method() {
		parameterAnnotationsOutOfDate = true;
	}

	/**
	 * Initialize from another object. Note that both objects use the same references (shallow copy). Use clone() for a physical
	 * copy.
	 */
	public Method(Method c) {
		super(c);
		parameterAnnotationsOutOfDate = true;
	}

	Method(DataInputStream file, ConstantPool constant_pool) throws IOException {
		super(file, constant_pool);
	}

	public Method(int access_flags, int name_index, int signature_index, Attribute[] attributes, ConstantPool constant_pool) {
		super(access_flags, name_index, signature_index, attributes, constant_pool);
		parameterAnnotationsOutOfDate = true;
	}

	public void accept(ClassVisitor v) {
		v.visitMethod(this);
	}

	// CUSTARD mutable or not?
	@Override
	public void setAttributes(Attribute[] attributes) {
		parameterAnnotationsOutOfDate = true;
		super.setAttributes(attributes);
	}

	/**
	 * @return Code attribute of method, if any
	 */
	public final Code getCode() {
		return AttributeUtils.getCodeAttribute(attributes);
	}

	public final ExceptionTable getExceptionTable() {
		return AttributeUtils.getExceptionTableAttribute(attributes);
	}

	/**
	 * Return LocalVariableTable of code attribute if any (the call is forwarded to the Code attribute)
	 */
	public final LocalVariableTable getLocalVariableTable() {
		Code code = getCode();
		if (code != null)
			return code.getLocalVariableTable();
		return null;
	}

	/**
	 * Return LineNumberTable of code attribute if any (the call is forwarded to the Code attribute)
	 */
	public final LineNumberTable getLineNumberTable() {
		Code code = getCode();
		if (code != null)
			return code.getLineNumberTable();
		return null;
	}

	/**
	 * Return string representation close to declaration format, eg: 'public static void main(String[] args) throws IOException'
	 */
	@Override
	public final String toString() {
		ConstantUtf8 c;
		String name, signature, access; // Short cuts to constant pool
		StringBuffer buf;

		access = Utility.accessToString(modifiers);

		// Get name and signature from constant pool
		c = (ConstantUtf8) cpool.getConstant(signatureIndex, Constants.CONSTANT_Utf8);
		signature = c.getValue();

		c = (ConstantUtf8) cpool.getConstant(nameIndex, Constants.CONSTANT_Utf8);
		name = c.getValue();

		signature = Utility.methodSignatureToString(signature, name, access, true, getLocalVariableTable());
		buf = new StringBuffer(signature);

        for (Attribute a : attributes) {
            if (!((a instanceof Code) || (a instanceof ExceptionTable)))
                buf.append(" [" + a.toString() + "]");
        }

		ExceptionTable e = getExceptionTable();
		if (e != null) {
			String str = e.toString();
			if (!str.equals(""))
				buf.append("\n\t\tthrows " + str);
		}

		return buf.toString();
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
		if (!parameterAnnotationsOutOfDate)
			return;
		parameterAnnotationsOutOfDate = false;

		int parameterCount = getArgumentTypes().length;
		if (parameterCount == 0) {
			unpackedParameterAnnotations = NO_PARAMETER_ANNOTATIONS;
			return;
		}

		RuntimeVisParamAnnos parameterAnnotationsVis = null;
		RuntimeInvisParamAnnos parameterAnnotationsInvis = null;

		// Find attributes that contain annotation data
		Attribute[] attrs = getAttributes();

        for (Attribute attribute : attrs) {
            if (attribute instanceof RuntimeVisParamAnnos) {
                parameterAnnotationsVis = (RuntimeVisParamAnnos) attribute;
            }
            else if (attribute instanceof RuntimeInvisParamAnnos) {
                parameterAnnotationsInvis = (RuntimeInvisParamAnnos) attribute;
            }
        }

		boolean foundSome = false;
		// Build a list of annotation arrays, one per argument
		if (parameterAnnotationsInvis != null || parameterAnnotationsVis != null) {
			List<AnnotationGen[]> annotationsForEachParameter = new ArrayList<>();
			AnnotationGen[] visibleOnes = null;
			AnnotationGen[] invisibleOnes = null;
			for (int i = 0; i < parameterCount; i++) {
				int count = 0;
				visibleOnes = new AnnotationGen[0];
				invisibleOnes = new AnnotationGen[0];
				if (parameterAnnotationsVis != null) {
					visibleOnes = parameterAnnotationsVis.getAnnotationsOnParameter(i);
					count += visibleOnes.length;
				}
				if (parameterAnnotationsInvis != null) {
					invisibleOnes = parameterAnnotationsInvis.getAnnotationsOnParameter(i);
					count += invisibleOnes.length;
				}

				AnnotationGen[] complete = AnnotationGen.NO_ANNOTATIONS;
				if (count != 0) {
					complete = new AnnotationGen[visibleOnes.length + invisibleOnes.length];
					System.arraycopy(visibleOnes, 0, complete, 0, visibleOnes.length);
					System.arraycopy(invisibleOnes, 0, complete, visibleOnes.length, invisibleOnes.length);
					foundSome = true;
				}
				annotationsForEachParameter.add(complete);
			}
			if (foundSome) {
				unpackedParameterAnnotations = annotationsForEachParameter.toArray(new AnnotationGen[][] {});
				return;
			}
		}
		unpackedParameterAnnotations = NO_PARAMETER_ANNOTATIONS;
	}

	public AnnotationGen[] getAnnotationsOnParameter(int i) {
		ensureParameterAnnotationsUnpacked();
		if (unpackedParameterAnnotations == NO_PARAMETER_ANNOTATIONS) {
			return AnnotationGen.NO_ANNOTATIONS;
		}
		return unpackedParameterAnnotations[i];
	}

	public AnnotationGen[][] getParameterAnnotations() {
		ensureParameterAnnotationsUnpacked();
		return unpackedParameterAnnotations;
	}

}
