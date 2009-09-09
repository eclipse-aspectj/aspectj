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
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.classfile.Utility;

public class AnnotationGen {
	public static final AnnotationGen[] NO_ANNOTATIONS = new AnnotationGen[0];
	
	private int typeIndex;
	private List /* ElementNameValuePairGen */<ElementNameValuePairGen> evs;
	private ConstantPool cpool;
	private boolean isRuntimeVisible = false;
	
	/**
	 * Here we are taking a fixed annotation of type Annotation and building a 
	 * modifiable AnnotationGen object.  If the pool passed in is for a different
	 * class file, then copyPoolEntries should have been passed as true as that
	 * will force us to do a deep copy of the annotation and move the cpool entries
	 * across.
	 * We need to copy the type and the element name value pairs and the visibility.
	 */
	public AnnotationGen(AnnotationGen a,ConstantPool cpool,boolean copyPoolEntries) {
		this.cpool = cpool;
		
		if (copyPoolEntries) {
			typeIndex = cpool.addUtf8(a.getTypeSignature());			
		} else {
			typeIndex = a.getTypeIndex();
		}
		
		isRuntimeVisible   = a.isRuntimeVisible();
		
		evs = copyValues(a.getValues(),cpool,copyPoolEntries);
	}
	
	private List<ElementNameValuePairGen> copyValues(List<ElementNameValuePairGen> in,ConstantPool cpool,boolean copyPoolEntries) {
		List<ElementNameValuePairGen> out = new ArrayList<ElementNameValuePairGen>();
		for (Iterator<ElementNameValuePairGen> iter = in.iterator(); iter.hasNext();) {
			ElementNameValuePairGen nvp = iter.next();
			out.add(new ElementNameValuePairGen(nvp,cpool,copyPoolEntries));
		}
		return out;
	}
	
	private AnnotationGen(ConstantPool cpool) {
		this.cpool = cpool;
		this.evs=new ArrayList<ElementNameValuePairGen>();
	}
	
	/**
	 * Retrieve an immutable version of this AnnotationGen
	 */
//	public AnnotationGen getAnnotation() {
//		return this;
////		AnnotationGen a = new AnnotationGen(typeIndex,cpool,isRuntimeVisible);
////		for (Iterator iter = evs.iterator(); iter.hasNext();) {
////			ElementNameValuePairGen element = (ElementNameValuePairGen) iter.next();
////			a.addElementNameValuePair(element.getElementNameValuePair());
////		}
////		return a;
//	}
	
	public AnnotationGen(ObjectType type,List /*ElementNameValuePairGen*/<ElementNameValuePairGen> elements,boolean vis,ConstantPool cpool) {
		this.cpool = cpool;
		if (type!=null)  this.typeIndex = cpool.addUtf8(type.getSignature()); // Only null for funky *temporary* FakeAnnotation objects
		evs = elements;
		isRuntimeVisible = vis;
	}
	
	public static AnnotationGen read(DataInputStream dis,ConstantPool cpool,boolean b) throws IOException {
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
			ElementNameValuePairGen envp = evs.get(i);
			envp.dump(dos);
		}
	}
	
	public void addElementNameValuePair(ElementNameValuePairGen evp) {
		if (evs == null) evs = new ArrayList<ElementNameValuePairGen>();
		evs.add(evp);
	}
	
	
	public int getTypeIndex() {
		return typeIndex;
	}
	
	public String getTypeSignature() {
//	  ConstantClass c = (ConstantClass)cpool.getConstant(typeIndex);
	  ConstantUtf8 utf8 = (ConstantUtf8)cpool.getConstant(typeIndex/*c.getNameIndex()*/);
	  return utf8.getBytes();
	}
	
	public String getTypeName() {
		return Utility.signatureToString(getTypeSignature());
	}
	
	/**
	 * Returns list of ElementNameValuePair objects
	 */
	public List<ElementNameValuePairGen> getValues() {
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
	
	/**
     * Return true if the annotation has a value with the specified name (n) and value (v)
     */
	public boolean hasNameValuePair(String n, String v) {
		for (int i=0;i<evs.size();i++) {
			ElementNameValuePairGen pair = evs.get(i);
			if (pair.getNameString().equals(n)) {
				if (pair.getValue().stringifyValue().equals(v)) return true;
			}
		}
		return false;
	}

    /**
     * Return true if the annotation has a value with the specified name (n)
     */
	public boolean hasNamedValue(String n) {
		for (int i=0;i<evs.size();i++) {
			ElementNameValuePairGen pair = evs.get(i);
			if (pair.getNameString().equals(n)) return true;
		}
		return false;
	}
}
