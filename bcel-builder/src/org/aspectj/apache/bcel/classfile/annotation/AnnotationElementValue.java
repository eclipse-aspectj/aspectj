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

public class AnnotationElementValue extends ElementValue {

	private AnnotationGen a;

	public AnnotationElementValue(AnnotationGen a, ConstantPool cpool) {
		super(ANNOTATION, cpool);
		this.a = a;
	}

	public AnnotationElementValue(int type, AnnotationGen annotation, ConstantPool cpool) {
		super(type, cpool);
		assert type == ANNOTATION;
		this.a = annotation;
	}

	public AnnotationElementValue(AnnotationElementValue value, ConstantPool cpool, boolean copyPoolEntries) {
		super(ANNOTATION, cpool);
		a = new AnnotationGen(value.getAnnotation(), cpool, copyPoolEntries);
	}

	@Override
	public void dump(DataOutputStream dos) throws IOException {
		dos.writeByte(type); // u1 type of value (ANNOTATION == '@')
		a.dump(dos);
	}

	@Override
	public String stringifyValue() {
		throw new RuntimeException("Not implemented yet");
	}

	public AnnotationGen getAnnotation() {
		return a;
	}

}
