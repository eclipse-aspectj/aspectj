/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ISourceContext;


// this is a class o' static methods for reading attributes.  It's pretty much a bridge from 
// bcel to AjAttribute.
class BcelAttributes {

	public static List readAjAttributes(Attribute[] as, ISourceContext context) {
		List l = new ArrayList();
		for (int i = as.length - 1; i >= 0; i--) {
			Attribute a = as[i];
			if (a instanceof Unknown) {
				Unknown u = (Unknown) a;
				String name = u.getName();
				if (name.startsWith(AjAttribute.AttributePrefix)) {
					l.add(AjAttribute.read(name, u.getBytes(), context));
				}
			}
		}
		return l;
	}

	public static Attribute bcelAttribute(AjAttribute a, ConstantPoolGen pool) {
		int nameIndex = pool.addUtf8(a.getNameString());
		byte[] bytes = a.getBytes();
		int length = bytes.length;

		return new Unknown(nameIndex, length, bytes, pool.getConstantPool());

	}

}
