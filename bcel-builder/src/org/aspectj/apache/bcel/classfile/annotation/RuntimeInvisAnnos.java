package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ClassVisitor;

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