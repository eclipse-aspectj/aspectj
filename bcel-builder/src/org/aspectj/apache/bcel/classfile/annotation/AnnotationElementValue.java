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

/**
 * An element value that is an annotation.
 */
public class AnnotationElementValue extends ElementValue {
	
	// For annotation element values, this is the annotation
	private Annotation a;
	    
	public AnnotationElementValue(int type, Annotation annotation, ConstantPool cpool) {
		super(type,cpool);
    	if (type != ANNOTATION) 
    		throw new RuntimeException("Only element values of type annotation can be built with this ctor");
       	this.a = annotation; 	
	}

	public void dump(DataOutputStream dos) throws IOException {
    	dos.writeByte(type);      // u1 type of value (ANNOTATION == '@')
    	a.dump(dos);
    }
    
    public String stringifyValue() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(a.toString());
    	return sb.toString();
    }
    
    public String toString() {
    	return stringifyValue();
    }
    
    public Annotation getAnnotation() { return a;}
   
}
