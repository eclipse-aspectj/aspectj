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
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;

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
		StringBuffer sb = new StringBuffer();
		ConstantUtf8 cu8 = (ConstantUtf8) cpool.getConstant(a.getTypeIndex(), Constants.CONSTANT_Utf8);
		sb.append(cu8.getValue());
		// haven't really tested this values section:
		List<NameValuePair> pairs = a.getValues();
		if (pairs != null && pairs.size() > 0) {
			sb.append("(");
			for (int p = 0; p < pairs.size(); p++) {
				if (p > 0) {
					sb.append(",");
				}
				sb.append(pairs.get(p).getNameString()).append("=").append(pairs.get(p).getValue().stringifyValue());
			}
			sb.append(")");
		}
		return sb.toString();
	}

	public AnnotationGen getAnnotation() {
		return a;
	}

}
