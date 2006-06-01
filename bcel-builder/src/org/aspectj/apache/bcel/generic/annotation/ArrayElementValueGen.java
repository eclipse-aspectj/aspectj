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

package org.aspectj.apache.bcel.generic.annotation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;


public class ArrayElementValueGen extends ElementValueGen {
	
	// J5TODO: Should we make this an array or a list?  A list would be easier to modify ...
	private List /*ElementValueGen*/ evalues;
	    
	public ArrayElementValueGen(ConstantPoolGen cp) {
		super(ARRAY,cp);
		evalues = new ArrayList();
	}
	
	public ArrayElementValueGen(int type, ElementValueGen[] datums, ConstantPoolGen cpool) {
		super(type,cpool);
    	if (type != ARRAY) 
    		throw new RuntimeException("Only element values of type array can be built with this ctor");
       	this.evalues = new ArrayList();
       	for (int i = 0; i < datums.length; i++) {
			evalues.add(datums[i]);
		}
	}
	
	/**
	 * Return immutable variant of this ArrayElementValueGen
	 */
	public ElementValue getElementValue() {
		ElementValue[] immutableData = new ElementValue[evalues.size()];
		int i =0;
		for (Iterator iter = evalues.iterator(); iter.hasNext();) {
			ElementValueGen element = (ElementValueGen) iter.next();
			immutableData[i++] = element.getElementValue();
		}
		return new ArrayElementValue(type,immutableData,cpGen.getConstantPool());
	}

	/**
	 * @param value
	 * @param cpool
	 */
	public ArrayElementValueGen(ArrayElementValue value, ConstantPoolGen cpool,boolean copyPoolEntries) {
		super(ARRAY,cpool);
		evalues = new ArrayList();
		ElementValue[] in = value.getElementValuesArray();
		for (int i = 0; i < in.length; i++) {
			evalues.add(ElementValueGen.copy(in[i],cpool,copyPoolEntries));
		}
	}

	public void dump(DataOutputStream dos) throws IOException {
    	dos.writeByte(type);      // u1 type of value (ARRAY == '[')
    	dos.writeShort(evalues.size());
    	for (Iterator iter = evalues.iterator(); iter.hasNext();) {
			ElementValueGen element = (ElementValueGen) iter.next();
			element.dump(dos);
		}
    }
    
    public String stringifyValue() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("[");
    	for (Iterator iter = evalues.iterator(); iter.hasNext();) {
			ElementValueGen element = (ElementValueGen) iter.next();
			sb.append(element.stringifyValue());
    		if (iter.hasNext()) sb.append(",");
		}
    	sb.append("]");
    	return sb.toString();
    }
    
    public List getElementValues() { return evalues;}
    public int getElementValuesSize() { return evalues.size();}

	public void addElement(ElementValueGen gen) {
		evalues.add(gen);
	}
   
}
