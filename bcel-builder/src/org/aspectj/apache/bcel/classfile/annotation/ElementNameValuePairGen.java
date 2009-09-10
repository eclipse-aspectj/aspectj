/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation 
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.classfile.ConstantPool;

public class ElementNameValuePairGen {
	private int nameIdx;
	private ElementValueGen value;
	private ConstantPool cpool;

	public ElementNameValuePairGen(ElementNameValuePairGen pair, ConstantPool cpool, boolean copyPoolEntries) {
		this.cpool = cpool;
		// J5ASSERT:
		// Could assert nvp.getNameString() points to the same thing as cpool.getConstant(nvp.getNameIndex())
		// if (!nvp.getNameString().equals(((ConstantUtf8)cpool.getConstant(nvp.getNameIndex())).getBytes())) {
		// throw new RuntimeException("envp buggered");
		// }
		if (copyPoolEntries) {
			nameIdx = cpool.addUtf8(pair.getNameString());
		} else {
			nameIdx = pair.getNameIndex();
		}
		value = ElementValueGen.copy(pair.getValue(), cpool, copyPoolEntries);
	}

	protected ElementNameValuePairGen(int idx, ElementValueGen value, ConstantPool cpool) {
		this.nameIdx = idx;
		this.value = value;
		this.cpool = cpool;
	}

	public ElementNameValuePairGen(String name, ElementValueGen value, ConstantPool cpool) {
		this.nameIdx = cpool.addUtf8(name);
		this.value = value;
		this.cpool = cpool;
	}

	protected void dump(DataOutputStream dos) throws IOException {
		dos.writeShort(nameIdx); // u2 name of the element
		value.dump(dos);
	}

	public int getNameIndex() {
		return nameIdx;
	}

	public final String getNameString() {
		return cpool.getConstantUtf8(nameIdx).getBytes();
	}

	public final ElementValueGen getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getNameString()).append("=").append(value.stringifyValue());
		return sb.toString();
	}
}
