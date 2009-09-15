package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ClassVisitor;

public class RuntimeInvisParamAnnos extends RuntimeParamAnnos {
	  
	  public RuntimeInvisParamAnnos(int nameIdx, int len, ConstantPool cpool) { 
	    super(Constants.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS, false, nameIdx, len, cpool);
	  }

	  public RuntimeInvisParamAnnos(int nameIdx, int len, 
	  		DataInputStream dis,ConstantPool cpool) throws IOException {
	    this(nameIdx, len, cpool);
	    readParameterAnnotations(dis,cpool);
	  }
	  
	  public RuntimeInvisParamAnnos(int nameIndex, int len, byte[] rvaData,ConstantPool cpool) {
		super(Constants.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS,false,nameIndex,len,rvaData,cpool);
	  }

	  public void accept(ClassVisitor v) {
	  	v.visitRuntimeInvisibleParameterAnnotations(this);
	  }

	  public Attribute copy(ConstantPool constant_pool) {
	  	throw new RuntimeException("Not implemented yet!");
	  }
}