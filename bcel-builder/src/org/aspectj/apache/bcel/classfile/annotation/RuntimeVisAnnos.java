package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ClassVisitor;

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