package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Visitor;

public class RuntimeVisibleParameterAnnotations extends RuntimeParameterAnnotations {
	
	  public RuntimeVisibleParameterAnnotations(int nameIdx, int len, ConstantPool cpool) { 
	    super(Constants.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS, true, nameIdx, len, cpool);
	  }
	  
	  public RuntimeVisibleParameterAnnotations(int nameIndex, int len, byte[] rvaData,ConstantPool cpool) {
		super(Constants.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS,true,nameIndex,len,rvaData,cpool);
	  }

	  public RuntimeVisibleParameterAnnotations(int nameIdx, int len, 
	  		DataInputStream dis,ConstantPool cpool) throws IOException {
	    this(nameIdx, len, cpool);
	    readParameterAnnotations(dis,cpool);
	  }

	  public void accept(Visitor v) {
	  	v.visitRuntimeVisibleParameterAnnotations(this);
	  }

	  public Attribute copy(ConstantPool constant_pool) {
	  	throw new RuntimeException("Not implemented yet!");
	  }
}