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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ClassVisitor;
import org.aspectj.apache.bcel.classfile.ConstantPool;

public class RuntimeVisAnnos extends RuntimeAnnos {
	
	  public RuntimeVisAnnos(int nameIdx, int len, ConstantPool cpool) { 
	    super(Constants.ATTR_RUNTIME_VISIBLE_ANNOTATIONS, true, nameIdx, len, cpool);
	  } 

	  public RuntimeVisAnnos(int nameIdx, int len, 
	  		DataInputStream dis,ConstantPool cpool) throws IOException {
	    this(nameIdx, len, cpool);
	    readAnnotations(dis,cpool);
	  }

	public RuntimeVisAnnos(int nameIndex, int len, byte[] rvaData,ConstantPool cpool) {
		super(Constants.ATTR_RUNTIME_VISIBLE_ANNOTATIONS,true,nameIndex,len,rvaData,cpool);
	}

	public void accept(ClassVisitor v) {
	  	v.visitRuntimeVisibleAnnotations(this);
	  }

	  public final void dump(DataOutputStream dos) throws IOException {
	  	super.dump(dos);
	  	writeAnnotations(dos);
	  }

	  public Attribute copy(ConstantPool constant_pool) {
	  	throw new RuntimeException("Not implemented yet!");
	  }
}