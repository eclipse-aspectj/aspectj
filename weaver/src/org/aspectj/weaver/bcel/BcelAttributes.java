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

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;


// this is a class o' static methods for reading attributes.  It's pretty much a bridge from 
// bcel to AjAttribute.
class BcelAttributes {

	public static List readAjAttributes(String classname,Attribute[] as, ISourceContext context,IMessageHandler msgHandler) {
		List l = new ArrayList();
		AjAttribute.WeaverVersionInfo version = new WeaverVersionInfo();
		for (int i = as.length - 1; i >= 0; i--) {
			Attribute a = as[i];
			if (a instanceof Unknown) {
				Unknown u = (Unknown) a;
				String name = u.getName();
				if (name.startsWith(AjAttribute.AttributePrefix)) {
					AjAttribute attr = AjAttribute.read(version,name,u.getBytes(),context,msgHandler); 
					if (attr!=null && attr instanceof AjAttribute.WeaverVersionInfo) {
						version = (AjAttribute.WeaverVersionInfo)attr;
						
						// Do a version check, this weaver can't process versions 
						// from a future AspectJ (where the major number has changed)
						if (version.getMajorVersion() > WeaverVersionInfo.getCurrentWeaverMajorVersion()) {
							throw new BCException("Unable to continue, this version of AspectJ supports classes built with weaver version "+
									WeaverVersionInfo.toCurrentVersionString()+" but the class "+classname+" is version "+version.toString());
						}
					}
					if (attr!=null) l.add(attr);
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
