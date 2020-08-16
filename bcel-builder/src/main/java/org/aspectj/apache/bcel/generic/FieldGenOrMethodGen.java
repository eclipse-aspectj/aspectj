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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Modifiers;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;

/**
 * Super class for FieldGen and MethodGen objects, since they have some methods in common!
 * 
 * @version $Id: FieldGenOrMethodGen.java,v 1.8 2009/09/15 19:40:14 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class FieldGenOrMethodGen extends Modifiers {

	protected String name;
	protected Type type;
	protected ConstantPool cp;
	private List<Attribute> attributeList = new ArrayList<>();
	protected List<AnnotationGen> annotationList = new ArrayList<>();

	protected FieldGenOrMethodGen() {
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConstantPool getConstantPool() {
		return cp;
	}

	public void setConstantPool(ConstantPool cp) {
		this.cp = cp;
	}

	public void addAttribute(Attribute a) {
		attributeList.add(a);
	}

	public void removeAttribute(Attribute a) {
		attributeList.remove(a);
	}

	public void removeAttributes() {
		attributeList.clear();
	}

	public List<AnnotationGen> getAnnotations() {
		return annotationList;
	}

	public void addAnnotation(AnnotationGen ag) {
		annotationList.add(ag);
	}

	public void removeAnnotation(AnnotationGen ag) {
		annotationList.remove(ag);
	}

	public void removeAnnotations() {
		annotationList.clear();
	}

	public List<Attribute> getAttributes() {
		return attributeList;
	}

	public Attribute[] getAttributesImmutable() {
		Attribute[] attributes = new Attribute[attributeList.size()];
		attributeList.toArray(attributes);
		return attributes;
	}

	protected void addAnnotationsAsAttribute(ConstantPool cp) {
		Collection<RuntimeAnnos> attrs = Utility.getAnnotationAttributes(cp, annotationList);
		if (attrs != null) {
			for (Attribute attr : attrs) {
				addAttribute(attr);
			}
		}
	}

	public abstract String getSignature();

}
