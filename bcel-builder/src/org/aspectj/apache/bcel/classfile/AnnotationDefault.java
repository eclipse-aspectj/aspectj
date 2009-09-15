/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;

/**
 * This attribute is attached to a method and indicates the default 
 * value for an annotation element.
 */
public class AnnotationDefault extends Attribute {
	
	private ElementValue value;

	public AnnotationDefault(int nameIndex, int len, DataInputStream dis, ConstantPool cpool) throws IOException {
		this(nameIndex, len, ElementValue.readElementValue(dis,cpool), cpool);
	}

	private AnnotationDefault(int nameIndex, int len, ElementValue value, ConstantPool cpool) {
	    super(Constants.ATTR_ANNOTATION_DEFAULT, nameIndex, len, cpool);
	    this.value = value;
	}

	public Attribute copy(ConstantPool constant_pool) {
		throw new RuntimeException("Not implemented yet!");
		// is this next line sufficient?
		// return (EnclosingMethod)clone();
	}
	
	public final ElementValue getElementValue() { return value; }  
	
    public final void dump(DataOutputStream dos) throws IOException {
	    super.dump(dos);
	    value.dump(dos);
    }    

	public void accept(ClassVisitor v) {
	  v.visitAnnotationDefault(this);
	}
}
