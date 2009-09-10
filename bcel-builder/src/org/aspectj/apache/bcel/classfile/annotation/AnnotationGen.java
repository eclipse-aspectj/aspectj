/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.generic.ObjectType;

public class AnnotationGen {
	public static final AnnotationGen[] NO_ANNOTATIONS = new AnnotationGen[0];

	private int typeIndex;
	private List<ElementNameValuePairGen> pairs = Collections.emptyList();
	private ConstantPool cpool;
	private boolean isRuntimeVisible = false;

	/**
	 * Here we are taking a fixed annotation of type Annotation and building a modifiable AnnotationGen object. If the pool passed
	 * in is for a different class file, then copyPoolEntries should have been passed as true as that will force us to do a deep
	 * copy of the annotation and move the cpool entries across. We need to copy the type and the element name value pairs and the
	 * visibility.
	 */
	public AnnotationGen(AnnotationGen a, ConstantPool cpool, boolean copyPoolEntries) {
		this.cpool = cpool;
		if (copyPoolEntries) {
			typeIndex = cpool.addUtf8(a.getTypeSignature());
		} else {
			typeIndex = a.getTypeIndex();
		}
		isRuntimeVisible = a.isRuntimeVisible();
		pairs = copyValues(a.getValues(), cpool, copyPoolEntries);
	}

	private List<ElementNameValuePairGen> copyValues(List<ElementNameValuePairGen> in, ConstantPool cpool, boolean copyPoolEntries) {
		List<ElementNameValuePairGen> out = new ArrayList<ElementNameValuePairGen>();
		for (ElementNameValuePairGen nvp : in) {
			out.add(new ElementNameValuePairGen(nvp, cpool, copyPoolEntries));
		}
		return out;
	}

	private AnnotationGen(ConstantPool cpool) {
		this.cpool = cpool;
	}

	/**
	 * Retrieve an immutable version of this AnnotationGen
	 */
	public AnnotationGen(ObjectType type, List<ElementNameValuePairGen> pairs, boolean runtimeVisible, ConstantPool cpool) {
		this.cpool = cpool;
		if (type != null) {
			this.typeIndex = cpool.addUtf8(type.getSignature()); // Only null for funky *temporary* FakeAnnotation objects
		}
		this.pairs = pairs;
		isRuntimeVisible = runtimeVisible;
	}

	public static AnnotationGen read(DataInputStream dis, ConstantPool cpool, boolean b) throws IOException {
		AnnotationGen a = new AnnotationGen(cpool);
		a.typeIndex = dis.readUnsignedShort();
		int elemValuePairCount = dis.readUnsignedShort();
		for (int i = 0; i < elemValuePairCount; i++) {
			int nidx = dis.readUnsignedShort();
			a.addElementNameValuePair(new ElementNameValuePairGen(nidx, ElementValueGen.readElementValue(dis, cpool), cpool));
		}
		a.isRuntimeVisible(b);
		return a;
	}

	public void dump(DataOutputStream dos) throws IOException {
		dos.writeShort(typeIndex); // u2 index of type name in cpool
		dos.writeShort(pairs.size()); // u2 element_value pair count
		for (int i = 0; i < pairs.size(); i++) {
			ElementNameValuePairGen envp = pairs.get(i);
			envp.dump(dos);
		}
	}

	public void addElementNameValuePair(ElementNameValuePairGen evp) {
		if (pairs == Collections.EMPTY_LIST) {
			pairs = new ArrayList<ElementNameValuePairGen>();
		}
		pairs.add(evp);
	}

	public int getTypeIndex() {
		return typeIndex;
	}

	public String getTypeSignature() {
		ConstantUtf8 utf8 = (ConstantUtf8) cpool.getConstant(typeIndex);
		return utf8.getValue();
	}

	public String getTypeName() {
		return Utility.signatureToString(getTypeSignature());
	}

	public List<ElementNameValuePairGen> getValues() {
		return pairs;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("AnnotationGen:[" + getTypeName() + " #" + pairs.size() + " {");
		for (int i = 0; i < pairs.size(); i++) {
			s.append(pairs.get(i));
			if (i + 1 < pairs.size())
				s.append(",");
		}
		s.append("}]");
		return s.toString();
	}

	public String toShortString() {
		StringBuffer s = new StringBuffer();
		s.append("@" + getTypeName() + "(");
		for (int i = 0; i < pairs.size(); i++) {
			s.append(pairs.get(i));
			if (i + 1 < pairs.size())
				s.append(",");
		}
		s.append(")");
		return s.toString();
	}

	private void isRuntimeVisible(boolean b) {
		isRuntimeVisible = b;
	}

	public boolean isRuntimeVisible() {
		return isRuntimeVisible;
	}

	/**
	 * @return true if the annotation has a value with the specified name and (toString'd) value
	 */
	public boolean hasNameValuePair(String name, String value) {
		for (ElementNameValuePairGen pair : pairs) {
			if (pair.getNameString().equals(name)) {
				if (pair.getValue().stringifyValue().equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return true if the annotation has a value with the specified name
	 */
	public boolean hasNamedValue(String name) {
		for (ElementNameValuePairGen pair : pairs) {
			if (pair.getNameString().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
