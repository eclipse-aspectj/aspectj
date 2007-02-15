/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation {date}
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;


public class ElementNameValuePair {
	private int nameIdx;
	private ElementValue value;
	private ConstantPool cpool;

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getNameString()+"="+value.toString());
		return sb.toString();
	}
	public ElementNameValuePair(int idx,ElementValue value,ConstantPool cpool) {
		this.nameIdx = idx;
		this.value   = value;
		this.cpool   = cpool;
	}
	
	protected void dump(DataOutputStream dos) throws IOException {
		dos.writeShort(nameIdx); // u2 name of the element
		value.dump(dos);
	}
	
	public int getNameIndex() {
		return nameIdx;
	}
	
	public final String getNameString() {
	  ConstantUtf8 c = (ConstantUtf8)cpool.getConstant(nameIdx,Constants.CONSTANT_Utf8);
	  return c.getBytes();
	}
	
	public final ElementValue getValue() {
		return value;
	}

	public String toShortString() {
		StringBuffer result = new StringBuffer();
		result.append(getNameString()).append("=").append(getValue().toShortString());
		return result.toString();
	}
}
