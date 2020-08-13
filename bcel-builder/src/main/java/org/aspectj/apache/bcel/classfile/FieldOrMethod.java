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
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;

/**
 * Abstract super class for fields and methods.
 * 
 * @version $Id: FieldOrMethod.java,v 1.12 2009/09/15 19:40:12 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class FieldOrMethod extends Modifiers implements Node {
	protected int nameIndex;
	protected int signatureIndex;
	protected Attribute[] attributes;

	protected ConstantPool cpool;
	private String name; // lazily initialized
	private String signature; // lazily initialized
	private AnnotationGen[] annotations; // lazily initialized
	private String signatureAttributeString = null;
	private boolean searchedForSignatureAttribute = false;

	protected FieldOrMethod() {
	}

	/**
	 * Initialize from another object. Note that both objects use the same references (shallow copy). Use clone() for a physical
	 * copy.
	 */
	protected FieldOrMethod(FieldOrMethod c) {
		this(c.getModifiers(), c.getNameIndex(), c.getSignatureIndex(), c.getAttributes(), c.getConstantPool());
	}

	protected FieldOrMethod(DataInputStream file, ConstantPool cpool) throws IOException {
		this(file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), null, cpool);
		attributes = AttributeUtils.readAttributes(file, cpool);
	}

	protected FieldOrMethod(int accessFlags, int nameIndex, int signatureIndex, Attribute[] attributes, ConstantPool cpool) {
		this.modifiers = accessFlags;
		this.nameIndex = nameIndex;
		this.signatureIndex = signatureIndex;
		this.cpool = cpool;
		this.attributes = attributes;
	}

	/**
	 * @param attributes Collection of object attributes.
	 */
	public void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
	}

	public final void dump(DataOutputStream file) throws IOException {
		file.writeShort(modifiers);
		file.writeShort(nameIndex);
		file.writeShort(signatureIndex);
		AttributeUtils.writeAttributes(attributes, file);
	}

	public final Attribute[] getAttributes() {
		return attributes;
	}

	public final ConstantPool getConstantPool() {
		return cpool;
	}

	public final int getNameIndex() {
		return nameIndex;
	}

	public final int getSignatureIndex() {
		return signatureIndex;
	}

	public final String getName() {
		if (name == null) {
			ConstantUtf8 c = (ConstantUtf8) cpool.getConstant(nameIndex, Constants.CONSTANT_Utf8);
			name = c.getValue();
		}
		return name;
	}

	public final String getSignature() {
		if (signature == null) {
			ConstantUtf8 c = (ConstantUtf8) cpool.getConstant(signatureIndex, Constants.CONSTANT_Utf8);
			signature = c.getValue();
		}
		return signature;
	}

	/**
	 * This will return the contents of a signature attribute attached to a member, or if there is none it will return the same as
	 * 'getSignature()'. Signature attributes are attached to members that were declared generic.
	 */
	public final String getDeclaredSignature() {
		if (getGenericSignature() != null)
			return getGenericSignature();
		return getSignature();
	}

	public AnnotationGen[] getAnnotations() {
		// Ensure we have unpacked any attributes that contain annotations.
		// We don't remove these annotation attributes from the attributes list, they
		// remain there.
		if (annotations == null) {
			// Find attributes that contain annotation data
			List<AnnotationGen> accumulatedAnnotations = new ArrayList<>();
			for (Attribute attribute : attributes) {
				if (attribute instanceof RuntimeAnnos) {
					RuntimeAnnos runtimeAnnotations = (RuntimeAnnos) attribute;
					accumulatedAnnotations.addAll(runtimeAnnotations.getAnnotations());
				}
			}
			if (accumulatedAnnotations.size() == 0) {
				annotations = AnnotationGen.NO_ANNOTATIONS;
			} else {
				annotations = accumulatedAnnotations.toArray(new AnnotationGen[] {});
			}
		}
		return annotations;
	}

	/**
	 * Hunts for a signature attribute on the member and returns its contents. So where the 'regular' signature may be
	 * (Ljava/util/Vector;)V the signature attribute may in fact say 'Ljava/lang/Vector<Ljava/lang/String>;' Coded for performance -
	 * searches for the attribute only when requested - only searches for it once.
	 */
	public final String getGenericSignature() {
		if (!searchedForSignatureAttribute) {
			Signature sig = AttributeUtils.getSignatureAttribute(attributes);
			signatureAttributeString = (sig == null ? null : sig.getSignature());
			searchedForSignatureAttribute = true;
		}
		return signatureAttributeString;
	}

}
