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
	
	// For array types, this is the array
	private ElementValue[] evalues;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (int i = 0; i < evalues.length; i++) {
			sb.append(evalues[i].toString());
			if ((i+1)<evalues.length) sb.append(",");
		}
		sb.append("}");
		return sb.toString();
	}
	    
	public ArrayElementValue(int type, ElementValue[] datums, ConstantPool cpool) {
		super(type,cpool);
    	if (type != ARRAY) 
    		throw new RuntimeException("Only element values of type array can be built with this ctor");
       	this.evalues = datums; 	
	}

	public void dump(DataOutputStream dos) throws IOException {
    	dos.writeByte(type);      // u1 type of value (ARRAY == '[')
    	dos.writeShort(evalues.length);
    	for (int i=0; i<evalues.length; i++) {
    		evalues[i].dump(dos);
    	}
    }
    
    public String stringifyValue() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("[");
    	for(int i=0; i<evalues.length; i++) {
    		sb.append(evalues[i].stringifyValue());
    		if ((i+1)<evalues.length) sb.append(",");
    	}
    	sb.append("]");
    	return sb.toString();
    }
    
    public ElementValue[] getElementValuesArray() { return evalues;}
    public int getElementValuesArraySize() { return evalues.length;}
   
}
