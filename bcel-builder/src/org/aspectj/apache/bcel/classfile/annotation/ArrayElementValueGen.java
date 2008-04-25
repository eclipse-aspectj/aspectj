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


public class ArrayElementValueGen extends ElementValueGen {
	private static final ElementValueGen[] NO_VALUES = new ElementValueGen[0];
	
	// J5TODO: Should we make this an array or a list?  A list would be easier to modify ...
	private ElementValueGen[] evalues = NO_VALUES;
	
    public ElementValueGen[] getElementValuesArray() { return evalues;}
    public int getElementValuesArraySize() { return evalues.length;}
	    
	public ArrayElementValueGen(ConstantPool cp) {
		super(ARRAY,cp);
	}
	
	public ArrayElementValueGen(int type, ElementValueGen[] datums, ConstantPool cpool) {
		super(type,cpool);
    	if (type != ARRAY) 
    		throw new RuntimeException("Only element values of type array can be built with this ctor");
       	this.evalues = datums;
	}
	
	/**
	 * Return immutable variant of this ArrayElementValueGen
	 */
	public ElementValueGen getElementValue() {
		ElementValueGen[] immutableData = new ElementValueGen[evalues.length];
		for (int  i = 0; i<evalues.length;i++) {
			immutableData[i] = evalues[i];
		}
		return new ArrayElementValueGen(type,immutableData,cpGen);
	}

	/**
	 * @param value
	 * @param cpool
	 */
	public ArrayElementValueGen(ArrayElementValueGen value, ConstantPool cpool,boolean copyPoolEntries) {
		super(ARRAY,cpool);
		evalues = new ElementValueGen[value.getElementValuesArraySize()];
		ElementValueGen[] in = value.getElementValuesArray();
		for (int i = 0; i < in.length; i++) {
			evalues[i]=ElementValueGen.copy(in[i],cpool,copyPoolEntries);
		}
	}

	public void dump(DataOutputStream dos) throws IOException {
    	dos.writeByte(type);      // u1 type of value (ARRAY == '[')
    	dos.writeShort(evalues.length);
    	for (int i =0;i<evalues.length;i++) {
			evalues[i].dump(dos);
		}
    }
    
    public String stringifyValue() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("[");
    	for (int i =0;i<evalues.length;i++) {
			ElementValueGen element = (ElementValueGen) evalues[i];
			sb.append(element.stringifyValue());
    		if ((i+1)<evalues.length) sb.append(",");
		}
    	sb.append("]");
    	return sb.toString();
    }
    

	public void addElement(ElementValueGen gen) {
		ElementValueGen[] old = evalues;
		evalues = new ElementValueGen[evalues.length+1];
		System.arraycopy(old,0,evalues,0,old.length);
		evalues[old.length]=gen;
	}
   
}
