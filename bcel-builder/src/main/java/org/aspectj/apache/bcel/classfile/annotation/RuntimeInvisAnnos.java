/* *******************************************************************
 * Copyright (c) 2004, 2013 IBM, VMware
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

public class RuntimeInvisAnnos extends RuntimeAnnos {
	
	  public RuntimeInvisAnnos(int nameIdx, int len, ConstantPool cpool) { 
	    super(Constants.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS, false, nameIdx, len, cpool);
	  } 

	  public RuntimeInvisAnnos(int nameIdx, int len, 
	  		DataInputStream dis,ConstantPool cpool) throws IOException {
	    this(nameIdx, len, cpool);
	    readAnnotations(dis,cpool);
	  }
	  
	  public RuntimeInvisAnnos(int nameIndex, int len, byte[] rvaData,ConstantPool cpool) {
		super(Constants.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS,false,nameIndex,len,rvaData,cpool);
	  }

	  public void accept(ClassVisitor v) {
	  	v.visitRuntimeInvisibleAnnotations(this);
	  }

	  public final void dump(DataOutputStream dos) throws IOException {
	  	super.dump(dos);
	  	writeAnnotations(dos);
	  }

	  public Attribute copy(ConstantPool constant_pool) {
	  	throw new RuntimeException("Not implemented yet!");
	  }
}