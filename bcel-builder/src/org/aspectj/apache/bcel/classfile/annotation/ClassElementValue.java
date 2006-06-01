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


public class ClassElementValue extends ElementValue {	
	
	// For primitive types and string type, this points to the value entry in the cpool
	// For 'class' this points to the class entry in the cpool
	private int idx;
	
    public ClassElementValue(int type,int idx,ConstantPool cpool) {
    	super(type,cpool);
    	this.idx = idx;
    }
    
    public int getIndex() {
    	return idx;
    }
     
    public String getClassString() {
		ConstantUtf8 c = (ConstantUtf8)cpool.getConstant(idx,Constants.CONSTANT_Utf8);
		return c.getBytes();
    }
    
    public String stringifyValue() {
    	ConstantUtf8 cu8 = (ConstantUtf8)cpool.getConstant(idx,Constants.CONSTANT_Utf8);
    	return cu8.getBytes();
    }
    
    public void dump(DataOutputStream dos) throws IOException {
    	dos.writeByte(type); // u1 kind of value
    	dos.writeShort(idx);
    }
 
}
