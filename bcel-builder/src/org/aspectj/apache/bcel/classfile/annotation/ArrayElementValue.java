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

import org.aspectj.apache.bcel.classfile.ConstantPool;

public class ArrayElementValue extends ElementValue {
	private static final ElementValue[] NO_VALUES = new ElementValue[0];

	// J5TODO: Should we make this an array or a list? A list would be easier to modify ...
	private ElementValue[] evalues = NO_VALUES;

	public ElementValue[] getElementValuesArray() {
		return evalues;
	}

	public int getElementValuesArraySize() {
		return evalues.length;
	}

	public ArrayElementValue(ConstantPool cp) {
		super(ARRAY, cp);
	}

	public ArrayElementValue(int type, ElementValue[] datums, ConstantPool cpool) {
		super(type, cpool);
		if (type != ARRAY)
			throw new RuntimeException("Only element values of type array can be built with this ctor");
		this.evalues = datums;
	}

	public ArrayElementValue(ArrayElementValue value, ConstantPool cpool, boolean copyPoolEntries) {
		super(ARRAY, cpool);
		evalues = new ElementValue[value.getElementValuesArraySize()];
		ElementValue[] in = value.getElementValuesArray();
		for (int i = 0; i < in.length; i++) {
			evalues[i] = ElementValue.copy(in[i], cpool, copyPoolEntries);
		}
	}

	@Override
	public void dump(DataOutputStream dos) throws IOException {
		dos.writeByte(type); // u1 type of value (ARRAY == '[')
		dos.writeShort(evalues.length);
		for (int i = 0; i < evalues.length; i++) {
			evalues[i].dump(dos);
		}
	}

	@Override
	public String stringifyValue() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < evalues.length; i++) {
			ElementValue element = evalues[i];
			sb.append(element.stringifyValue());
			if ((i + 1) < evalues.length)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	public void addElement(ElementValue gen) {
		ElementValue[] old = evalues;
		evalues = new ElementValue[evalues.length + 1];
		System.arraycopy(old, 0, evalues, 0, old.length);
		evalues[old.length] = gen;
	}

}
