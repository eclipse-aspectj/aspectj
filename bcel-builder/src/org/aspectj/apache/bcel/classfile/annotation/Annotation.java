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

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.Utility;

/**
 * An annotation is an immutable object (AnnotationGen is the mutable variant) - it basically contains a list
 * of name-value pairs.
 */
public class Annotation {
	private int typeIndex;
	// OPTIMIZE don't need a new list instance for every annotation instance!
	private List /* ElementNameValuePair */ evs = new ArrayList();
	private ConstantPool cpool;
	private boolean isRuntimeVisible;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ANNOTATION ["+getTypeSignature()+"] ["+
				(isRuntimeVisible?"runtimeVisible":"runtimeInvisible")+"] [");
		for (Iterator iter = evs.iterator(); iter.hasNext();) {
			ElementNameValuePair element = (ElementNameValuePair) iter.next();
			sb.append(element.toString());
			if (iter.hasNext()) sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	private Annotation(ConstantPool cpool) {
		this.cpool = cpool;
	}
	
	public Annotation(int index,ConstantPool cpool,boolean visible) {
		this.cpool = cpool;
		this.typeIndex = index;
		this.isRuntimeVisible = visible;
	}
	
	protected static Annotation read(DataInputStream dis,ConstantPool cpool,boolean isRuntimeVisible) throws IOException {
		Annotation a = new Annotation(cpool);
		a.typeIndex = dis.readUnsignedShort();
		int elemValuePairCount = dis.readUnsignedShort();
		for (int i=0;i<elemValuePairCount;i++) {
			int nidx = dis.readUnsignedShort();		
			a.addElementNameValuePair(
					new ElementNameValuePair(nidx,ElementValue.readElementValue(dis,cpool),cpool));
		}
		a.isRuntimeVisible(isRuntimeVisible);
		return a;
	}
	
	protected void dump(DataOutputStream dos) throws IOException {
		dos.writeShort(typeIndex);	// u2 index of type name in cpool
		dos.writeShort(evs.size()); // u2 element_value pair count
		for (int i = 0 ; i<evs.size();i++) {
			ElementNameValuePair envp = (ElementNameValuePair) evs.get(i);
			envp.dump(dos);
		}
	}
	
	public void addElementNameValuePair(ElementNameValuePair evp) {
		evs.add(evp);
	}
	
	
	public int getTypeIndex() {
		return typeIndex;
	}
	
	public String getTypeSignature() {
	  ConstantUtf8 c = (ConstantUtf8)cpool.getConstant(typeIndex,Constants.CONSTANT_Utf8);
	  return c.getBytes();
	}
	
	public String getTypeName() {
		ConstantUtf8 c = (ConstantUtf8)cpool.getConstant(typeIndex,Constants.CONSTANT_Utf8);
		return Utility.signatureToString(c.getBytes());
	}
	
	/**
	 * Returns list of ElementNameValuePair objects
	 */
	public List getValues() {
		return evs;
	}

	protected void isRuntimeVisible(boolean b) {
		isRuntimeVisible = b;
	}
	
	public boolean isRuntimeVisible() {
		return isRuntimeVisible;
	}

	public String toShortString() {
		StringBuffer result = new StringBuffer();
		result.append("@");
		result.append(getTypeName());
		if (getValues().size()>0) {
			result.append("(");
			for (Iterator iter = getValues().iterator(); iter.hasNext();) {
				ElementNameValuePair element = (ElementNameValuePair) iter.next();
				result.append(element.toShortString());
			}
			result.append(")");
		}
		return result.toString();
	}

	/**
     * Return true if the annotation has a value with the specified name (n) and value (v)
     */
	public boolean hasNameValuePair(String n, String v) {
		for (int i=0;i<evs.size();i++) {
			ElementNameValuePair pair = (ElementNameValuePair)evs.get(i);
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
			ElementNameValuePair pair = (ElementNameValuePair)evs.get(i);
			if (pair.getNameString().equals(n)) return true;
		}
		return false;
	}
}
