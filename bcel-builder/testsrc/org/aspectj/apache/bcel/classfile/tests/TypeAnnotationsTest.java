/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation 
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;


public class TypeAnnotationsTest extends BcelTestCase {
	

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testMarkerAnnotationsOnTypes() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("MarkedType");
		ClassGen cg = new ClassGen(clazz);
		AnnotationGen[] annotations = cg.getAnnotations();
		assertTrue("Should be a MarkerAnnotation and a MarkerAnnotationInvisible - but found: "+annotations.length,annotations.length==2);
	}
	
	
}
