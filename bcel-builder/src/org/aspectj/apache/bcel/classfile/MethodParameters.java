/* *******************************************************************
 * Copyright (c) 2013 VMware
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
package org.aspectj.apache.bcel.classfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

// see http://cr.openjdk.java.net/~abuckley/8misc.pdf
public class MethodParameters extends Attribute {

	public final static int[] NO_PARAMETER_NAME_INDEXES = new int[0];
	public final static int[] NO_PARAMETER_ACCESS_FLAGS = new int[0];
	
	public final static int ACCESS_FLAGS_FINAL     = 0x0010;
	public final static int ACCESS_FLAGS_SYNTHETIC = 0x1000;
	public final static int ACCESS_FLAGS_MANDATED  = 0x8000;
	
	// if 'isInPackedState' then this data needs unpacking
	private boolean isInPackedState = false;
	private byte[] data;
	private int[] names;
	private int[] accessFlags;
	
	public MethodParameters(int index, int length, DataInputStream dis, ConstantPool cpool) throws IOException {
		super(Constants.ATTR_METHOD_PARAMETERS,index,length,cpool);
		data = new byte[length];
		dis.readFully(data,0,length);
		isInPackedState = true;
	}
	
	private void ensureInflated() {
		if (names!=null) return;
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			int parametersCount = dis.readUnsignedByte();
			if (parametersCount == 0) {
				names = NO_PARAMETER_NAME_INDEXES;
				accessFlags = NO_PARAMETER_ACCESS_FLAGS;
			} else {
				names = new int[parametersCount];
				accessFlags = new int[parametersCount];
				for (int i=0;i<parametersCount;i++) {
					names[i] = dis.readUnsignedShort();
					accessFlags[i] = dis.readUnsignedShort();
				}
			}
			isInPackedState = false;
		} catch (IOException ioe) {
			throw new RuntimeException("Unabled to inflate type annotation data, badly formed?");
		}
	}
	
	public void dump(DataOutputStream dos) throws IOException {
		super.dump(dos);
		if (isInPackedState) {
			dos.write(data);
		} else {
			dos.writeByte(names.length);
			for (int i=0;i<names.length;i++) {
				dos.writeShort(names[i]);
				dos.writeShort(accessFlags[i]);
			}
		}
	}
	
	public int getParametersCount() {
		ensureInflated();
		return names.length;
	}
	
	public String getParameterName(int parameter) {
		ensureInflated();
		ConstantUtf8 c = (ConstantUtf8) cpool.getConstant(names[parameter], Constants.CONSTANT_Utf8);
		return c.getValue();
	}
	
	public int getAccessFlags(int parameter) {
		ensureInflated();
		return accessFlags[parameter];
	}

	public boolean isFinal(int parameter) {
		return (getAccessFlags(parameter) & ACCESS_FLAGS_FINAL)!=0;
	}
	
	public boolean isSynthetic(int parameter) {
		return (getAccessFlags(parameter) & ACCESS_FLAGS_SYNTHETIC)!=0;
	}
	
	public boolean isMandated(int parameter) {
		return (getAccessFlags(parameter) & ACCESS_FLAGS_MANDATED)!=0;
	}

	@Override
	public void accept(ClassVisitor v) {
		v.visitMethodParameters(this);
	}
}
