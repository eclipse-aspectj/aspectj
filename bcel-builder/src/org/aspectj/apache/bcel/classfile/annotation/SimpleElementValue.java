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
import org.aspectj.apache.bcel.classfile.ConstantDouble;
import org.aspectj.apache.bcel.classfile.ConstantFloat;
import org.aspectj.apache.bcel.classfile.ConstantInteger;
import org.aspectj.apache.bcel.classfile.ConstantLong;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;

/**
 * An element value representing a primitive or string value.
 */
public class SimpleElementValue extends ElementValue {	
	
	// For primitive types and string type, this points to the value entry in the cpool
	// For 'class' this points to the class entry in the cpool
	private int idx;
	
    public SimpleElementValue(int type,int idx,ConstantPool cpool) {
    	super(type,cpool);
    	this.idx = idx;
    }
    
    public int getIndex() {
    	return idx;
    }
    
    
    public String getValueString() {
    	if (type != STRING) 
    		throw new RuntimeException("Dont call getValueString() on a non STRING ElementValue");
		ConstantUtf8 c = (ConstantUtf8)cpool.getConstant(idx,Constants.CONSTANT_Utf8);
		return c.getBytes();
    }
    
    public int getValueInt() {
    	if (type != PRIMITIVE_INT) 
    		throw new RuntimeException("Dont call getValueString() on a non STRING ElementValue");
		ConstantInteger c = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
		return c.getBytes();
    }
    
    public byte getValueByte() {
    	if (type != PRIMITIVE_BYTE) 
    		throw new RuntimeException("Dont call getValueByte() on a non BYTE ElementValue");
		ConstantInteger c = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
		return (byte)c.getBytes();
    }
    
    public char getValueChar() {
    	if (type != PRIMITIVE_CHAR) 
    		throw new RuntimeException("Dont call getValueChar() on a non CHAR ElementValue");
		ConstantInteger c = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
		return (char)c.getBytes();
    }
    
    public long getValueLong() {
    	if (type != PRIMITIVE_LONG) 
    		throw new RuntimeException("Dont call getValueLong() on a non LONG ElementValue");
    	ConstantLong j = (ConstantLong)cpool.getConstant(idx);
    	return j.getBytes();
    }
    
    public float getValueFloat() {
    	if (type != PRIMITIVE_FLOAT)
    		throw new RuntimeException("Dont call getValueFloat() on a non FLOAT ElementValue");
    	ConstantFloat f = (ConstantFloat)cpool.getConstant(idx);
    	return f.getBytes();
    }


    public double getValueDouble() {
    	if (type != PRIMITIVE_DOUBLE)
    		throw new RuntimeException("Dont call getValueDouble() on a non DOUBLE ElementValue");
    	ConstantDouble d = (ConstantDouble)cpool.getConstant(idx);
    	return d.getBytes();
    }
    
    public boolean getValueBoolean() {
    	if (type != PRIMITIVE_BOOLEAN)
    		throw new RuntimeException("Dont call getValueBoolean() on a non BOOLEAN ElementValue");
    	ConstantInteger bo = (ConstantInteger)cpool.getConstant(idx);
    	return (bo.getBytes()!=0);
    }
    
    public short getValueShort() {
    	if (type != PRIMITIVE_SHORT)
    		throw new RuntimeException("Dont call getValueShort() on a non SHORT ElementValue");
    	ConstantInteger s = (ConstantInteger)cpool.getConstant(idx);
    	return (short)s.getBytes();
    }
    
    public String toString() {
    	return stringifyValue();
    }
    
    // Whatever kind of value it is, return it as a string
    public String stringifyValue() {
    	switch (type) {
    	  case PRIMITIVE_INT:
    	  	ConstantInteger c = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
    		return Integer.toString(c.getBytes());
    	  case PRIMITIVE_LONG:
    	  	ConstantLong j = (ConstantLong)cpool.getConstant(idx,Constants.CONSTANT_Long);
    		return Long.toString(j.getBytes());
    	  case PRIMITIVE_DOUBLE:
    	  	ConstantDouble d = (ConstantDouble)cpool.getConstant(idx,Constants.CONSTANT_Double);
    		return Double.toString(d.getBytes());
    	  case PRIMITIVE_FLOAT:
    	  	ConstantFloat f = (ConstantFloat)cpool.getConstant(idx,Constants.CONSTANT_Float);
    		return Float.toString(f.getBytes());
    	  case PRIMITIVE_SHORT:
    		ConstantInteger s = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
    		return Integer.toString(s.getBytes());
    	  case PRIMITIVE_BYTE:
    		ConstantInteger b = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
    		return Integer.toString(b.getBytes());
    	  case PRIMITIVE_CHAR:
    		ConstantInteger ch = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
    		return new Character((char)ch.getBytes()).toString();
    	  case PRIMITIVE_BOOLEAN:
    		ConstantInteger bo = (ConstantInteger)cpool.getConstant(idx,Constants.CONSTANT_Integer);
    		if (bo.getBytes() == 0) return "false";
    		if (bo.getBytes() != 0) return "true";
    	  case STRING:
    		ConstantUtf8 cu8 = (ConstantUtf8)cpool.getConstant(idx,Constants.CONSTANT_Utf8);
    		return cu8.getBytes();
    		
 		  default:
   			throw new RuntimeException("SimpleElementValue class does not know how to stringify type "+type);
    	}
    }
    
    public void dump(DataOutputStream dos) throws IOException {
    	dos.writeByte(type); // u1 kind of value
    	switch (type) {
    		case PRIMITIVE_INT: 
    		case PRIMITIVE_BYTE:
    		case PRIMITIVE_CHAR:
    		case PRIMITIVE_FLOAT:
    		case PRIMITIVE_LONG:
    		case PRIMITIVE_BOOLEAN:
    		case PRIMITIVE_SHORT:
    		case PRIMITIVE_DOUBLE:
    		case STRING:
    			dos.writeShort(idx);
    			break;
   			default:
   				throw new RuntimeException("SimpleElementValue doesnt know how to write out type "+type);
    	}
    }
 
}
