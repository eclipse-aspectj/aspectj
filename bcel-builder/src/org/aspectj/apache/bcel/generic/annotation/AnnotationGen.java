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
package org.aspectj.apache.bcel.generic.annotation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.ObjectType;

public class AnnotationGen {
	private int typeIndex;
	private List /* ElementNameValuePairGen */ evs;
	private ConstantPoolGen cpool;
	private boolean isRuntimeVisible = false;
	
	/**
	 * Here we are taking a fixed annotation of type Annotation and building a 
	 * modifiable AnnotationGen object.  If the pool passed in is for a different
	 * class file, then copyPoolEntries should have been passed as true as that
	 * will force us to do a deep copy of the annotation and move the cpool entries
	 * across.
	 * We need to copy the type and the element name value pairs and the visibility.
	 */
	public AnnotationGen(Annotation a,ConstantPoolGen cpool,boolean copyPoolEntries) {
		this.cpool = cpool;
		
		if (copyPoolEntries) {
			typeIndex = cpool.addUtf8(a.getTypeSignature());			
		} else {
			typeIndex = a.getTypeIndex();
		}
		
		isRuntimeVisible   = a.isRuntimeVisible();
		
		evs = copyValues(a.getValues(),cpool,copyPoolEntries);
	}
	
	private List copyValues(List in,ConstantPoolGen cpool,boolean copyPoolEntries) {
		List out = new ArrayList();
		for (Iterator iter = in.iterator(); iter.hasNext();) {
			ElementNameValuePair nvp = (ElementNameValuePair) iter.next();
			out.add(new ElementNameValuePairGen(nvp,cpool,copyPoolEntries));
		}
		return out;
	}
	
	private AnnotationGen(ConstantPoolGen cpool) {
		this.cpool = cpool;
	}
	
	/**
	 * Retrieve an immutable version of this AnnotationGen
	 */
	public Annotation getAnnotation() {
		Annotation a = new Annotation(typeIndex,cpool.getConstantPool(),isRuntimeVisible);
		for (Iterator iter = evs.iterator(); iter.hasNext();) {
			ElementNameValuePairGen element = (ElementNameValuePairGen) iter.next();
			a.addElementNameValuePair(element.getElementNameValuePair());
		}
		return a;
	}
	
	public AnnotationGen(ObjectType type,List /*ElementNameValuePairGen*/ elements,boolean vis,ConstantPoolGen cpool) {
		this.cpool = cpool;
		this.typeIndex = cpool.addUtf8(type.getSignature());
		evs = elements;
		isRuntimeVisible = vis;
	}
	
	public static AnnotationGen read(DataInputStream dis,ConstantPoolGen cpool,boolean b) throws IOException {
		AnnotationGen a = new AnnotationGen(cpool);
		a.typeIndex = dis.readUnsignedShort();
		int elemValuePairCount = dis.readUnsignedShort();
		for (int i=0;i<elemValuePairCount;i++) {
			int nidx = dis.readUnsignedShort();		
			a.addElementNameValuePair(
					new ElementNameValuePairGen(nidx,ElementValueGen.readElementValue(dis,cpool),cpool));
		}
		a.isRuntimeVisible(b);
		return a;
	}
	
	public void dump(DataOutputStream dos) throws IOException {
		dos.writeShort(typeIndex);	// u2 index of type name in cpool
		dos.writeShort(evs.size()); // u2 element_value pair count
		for (int i = 0 ; i<evs.size();i++) {
			ElementNameValuePairGen envp = (ElementNameValuePairGen) evs.get(i);
			envp.dump(dos);
		}
	}
	
	public void addElementNameValuePair(ElementNameValuePairGen evp) {
		if (evs == null) evs = new ArrayList();
		evs.add(evp);
	}
	
	
	public int getTypeIndex() {
		return typeIndex;
	}
	
	public final String getTypeSignature() {
//	  ConstantClass c = (ConstantClass)cpool.getConstant(typeIndex);
	  ConstantUtf8 utf8 = (ConstantUtf8)cpool.getConstant(typeIndex/*c.getNameIndex()*/);
	  return utf8.getBytes();
	}
	
	public final String getTypeName() {
		return getTypeSignature();// BCELBUG: Should I use this instead? Utility.signatureToString(getTypeSignature());
	}
	
	/**
	 * Returns list of ElementNameValuePair objects
	 */
	public List getValues() {
		return evs;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("AnnotationGen:["+getTypeName()+" #"+evs.size()+" {");
		for (int i = 0; i<evs.size();i++) {
			s.append(evs.get(i));
			if (i+1<evs.size()) s.append(",");
		}
		s.append("}]");
		return s.toString();
	}
	
	public String toShortString() {
		StringBuffer s = new StringBuffer();
		s.append("@"+getTypeName()+"(");
		for (int i = 0; i<evs.size();i++) {
			s.append(evs.get(i));
			if (i+1<evs.size()) s.append(",");
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
}
