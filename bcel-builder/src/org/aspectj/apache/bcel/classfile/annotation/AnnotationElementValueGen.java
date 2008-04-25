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

import org.aspectj.apache.bcel.classfile.ConstantPool;


public class AnnotationElementValueGen extends ElementValueGen {
	
	// For annotation element values, this is the annotation
	private AnnotationGen a;
	
	public AnnotationElementValueGen(AnnotationGen a,ConstantPool cpool) {
		super(ANNOTATION,cpool);
		this.a = a;
	}
	    
	public AnnotationElementValueGen(int type, AnnotationGen annotation, ConstantPool cpool) {
		super(type,cpool);
    	if (type != ANNOTATION) 
    		throw new RuntimeException("Only element values of type annotation can be built with this ctor");
       	this.a = annotation; 	
	}

	public AnnotationElementValueGen(AnnotationElementValueGen value, ConstantPool cpool,boolean copyPoolEntries) {
		super(ANNOTATION,cpool);
		a = new AnnotationGen(value.getAnnotation(),cpool,copyPoolEntries);
	}

	public void dump(DataOutputStream dos) throws IOException {
    	dos.writeByte(type);      // u1 type of value (ANNOTATION == '@')
    	a.dump(dos);
    }
    
    public String stringifyValue() {
    	throw new RuntimeException("Not implemented yet");
    }
	
	/**
     * Return immutable variant of this AnnotationElementValueGen
     */
	public ElementValueGen getElementValue() {
		return new AnnotationElementValueGen(this.type,a,cpGen);
	}
    
    public AnnotationGen getAnnotation() { return a;}
   
}
