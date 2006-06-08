/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;


// this is a class o' static methods for reading attributes.  It's pretty much a bridge from 
// bcel to AjAttribute.
class BcelAttributes {

	/**
	 * Process an array of Bcel attributes - looking for those with the name prefix org.aspectj.weaver.  The returned 
	 * list contains the AspectJ attributes identified and unpacked to 'AjAttribute' objects.
	 */
	public static List readAjAttributes(String classname,Attribute[] as, ISourceContext context,
			                              World w,AjAttribute.WeaverVersionInfo version) {
		List l = new ArrayList();
		
		// first pass, look for version
		List forSecondPass = new ArrayList();
		for (int i = as.length - 1; i >= 0; i--) {
			Attribute a = as[i];
			if (a instanceof Unknown) {
				Unknown u = (Unknown) a;
				String name = u.getName();
				if (name.charAt(0)=='o') { // 'o'rg.aspectj
					if (name.startsWith(AjAttribute.AttributePrefix)) {
						if (name.endsWith(WeaverVersionInfo.AttributeName)) {
							version = (AjAttribute.WeaverVersionInfo)AjAttribute.read(version,name,u.getBytes(),context,w);
							if (version.getMajorVersion() > WeaverVersionInfo.getCurrentWeaverMajorVersion()) {
								throw new BCException("Unable to continue, this version of AspectJ supports classes built with weaver version "+
										WeaverVersionInfo.toCurrentVersionString()+" but the class "+classname+" is version "+version.toString());
							}
	                    }
						forSecondPass.add(a);
					}
				}
			}
		}
				
		for (int i = forSecondPass.size()-1; i >= 0; i--) {
			Unknown a = (Unknown)forSecondPass.get(i);
			String name = a.getName();
			AjAttribute attr = AjAttribute.read(version,name,a.getBytes(),context,w); 
			if (attr!=null) l.add(attr);
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