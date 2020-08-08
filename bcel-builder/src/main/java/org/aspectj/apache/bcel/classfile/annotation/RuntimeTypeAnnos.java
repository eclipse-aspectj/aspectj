/* *******************************************************************
 * Copyright (c) 2013 VMware
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;

public abstract class RuntimeTypeAnnos extends Attribute {
	
	private boolean visible;
	private TypeAnnotationGen[] typeAnnotations; // null until inflated
	
	// Keep just a byte stream of the data until someone actually asks for the information within
	private byte[] annotation_data;

	public RuntimeTypeAnnos(byte attrid, boolean visible, int nameIdx, int len, ConstantPool cpool) {
		super(attrid,nameIdx,len,cpool);
		this.visible = visible;
	}

	protected void readTypeAnnotations(DataInputStream dis,ConstantPool cpool) throws IOException {
		annotation_data = new byte[length];
		dis.readFully(annotation_data,0,length);
	}

	public final void dump(DataOutputStream dos) throws IOException {
	  super.dump(dos);
	  writeTypeAnnotations(dos);
	}	  

	protected void writeTypeAnnotations(DataOutputStream dos) throws IOException {
		if (typeAnnotations == null) {			
			dos.write(annotation_data,0,length);
		} else {
			dos.writeShort(typeAnnotations.length);
			for (TypeAnnotationGen typeAnnotation : typeAnnotations) {
				typeAnnotation.dump(dos);
			}
		}
	}

//	public RuntimeTypeAnnos(byte attrid,boolean visible,int nameIdx,int len,byte[] data,ConstantPool cpool) {
//		super(attrid,nameIdx,len,cpool);
//		this.visible = visible;
//		parameterAnnotations = new ArrayList<AnnotationGen[]>();
//		annotation_data = data;
//	}

	public Attribute copy(ConstantPool constant_pool) {
	  	throw new RuntimeException("Not implemented yet!");
	}

	public TypeAnnotationGen[] getTypeAnnotations() {
		ensureInflated();
		return typeAnnotations;
	}
	
	
	public boolean areVisible() {
		return visible;
	}

	private void ensureInflated() {
		if (typeAnnotations !=null) {
			return;
		}
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotation_data));
			int numTypeAnnotations = dis.readUnsignedShort();
			if (numTypeAnnotations == 0) {
				typeAnnotations = TypeAnnotationGen.NO_TYPE_ANNOTATIONS;
			} else {
				typeAnnotations = new TypeAnnotationGen[numTypeAnnotations];
				for (int i=0; i<numTypeAnnotations; i++) {
					typeAnnotations[i] = TypeAnnotationGen.read(dis,getConstantPool(),visible);
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Unabled to inflate type annotation data, badly formed?");
		}
	}

	public String toString() {
		return "Runtime"+(visible?"Visible":"Invisible")+"TypeAnnotations ["+(isInflated()?"inflated":"not yet inflated")+"]";
	}

	public boolean isInflated() {
		return typeAnnotations != null;
	}

}
