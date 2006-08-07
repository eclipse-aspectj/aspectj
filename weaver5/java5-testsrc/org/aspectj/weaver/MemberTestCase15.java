/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class MemberTestCase15 extends TestCase {

	  public void testCanBeParameterizedRegularMethod() {
	    	BcelWorld world = new BcelWorld();
	    	ResolvedType javaLangClass = world.resolve(UnresolvedType.forName("java/lang/Class"));
	    	ResolvedMember[] methods = javaLangClass.getDeclaredMethods();
	    	ResolvedMember getAnnotations = null;
	    	for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals("getAnnotations")) {
					getAnnotations = methods[i];
					break;
				}
			}
	    	if (getAnnotations != null) { // so can run on non-Java 5
//	    		System.out.println("got it");
	    		assertFalse(getAnnotations.canBeParameterized());
	    	}
	    }
	    
	    public void testCanBeParameterizedGenericMethod() {
	    	BcelWorld world = new BcelWorld();
	    	world.setBehaveInJava5Way(true);
	    	ResolvedType javaLangClass = world.resolve(UnresolvedType.forName("java.lang.Class"));
	    	javaLangClass = javaLangClass.getGenericType();
	    	if (javaLangClass == null) return;  // for < 1.5
	    	ResolvedMember[] methods = javaLangClass.getDeclaredMethods();
	    	ResolvedMember asSubclass = null;
	    	for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals("asSubclass")) {
					asSubclass = methods[i];
					break;
				}
			}
	    	if (asSubclass != null) { // so can run on non-Java 5
//	    		System.out.println("got it");
	    		assertTrue(asSubclass.canBeParameterized());
	    	}    	
	    }
	    
	    public void testCanBeParameterizedMethodInGenericType() {
	       	BcelWorld world = new BcelWorld();
	       	world.setBehaveInJava5Way(true);
	    	ResolvedType javaUtilList = world.resolve(UnresolvedType.forName("java.util.List"));
	    	javaUtilList = javaUtilList.getGenericType();
	    	if (javaUtilList == null) return;  // for < 1.5
	    	ResolvedMember[] methods = javaUtilList.getDeclaredMethods();
	    	ResolvedMember add = null;
	    	for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals("add")) {
					add = methods[i];
					break;
				}
			}
	    	if (add != null) { // so can run on non-Java 5
//	    		System.out.println("got it");
	    		assertTrue(add.canBeParameterized());
	    	}    	    	
	    }
	    
}
