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
package org.aspectj.internal.lang.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.InterTypeConstructorDeclaration;
import org.aspectj.lang.reflect.InterTypeDeclaration;
import org.aspectj.lang.reflect.InterTypeFieldDeclaration;
import org.aspectj.lang.reflect.InterTypeMethodDeclaration;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class InterTypeTest extends TestCase {

	public void testITDImpl() throws ClassNotFoundException {
		AjType<InterTypeTest> thisClass = AjTypeSystem.getAjType(InterTypeTest.class);
		AjType<Runnable> runnable = AjTypeSystem.getAjType(java.lang.Runnable.class);
		InterTypeDeclaration itd = new InterTypeDeclarationImpl(thisClass,"java.lang.Runnable",5);
		assertEquals(thisClass,itd.getDeclaringType());
		assertEquals(runnable,itd.getTargetType());
		assertEquals(5, itd.getModifiers());
	}
	
	public void testITDField() throws ClassNotFoundException {
		AjType<InterTypeTest> thisClass = AjTypeSystem.getAjType(InterTypeTest.class);
		AjType<Runnable> runnable = AjTypeSystem.getAjType(java.lang.Runnable.class);
		InterTypeDeclaration itd = new InterTypeDeclarationImpl(thisClass,"java.lang.Runnable",5);
		AjType<String> stringType = AjTypeSystem.getAjType(java.lang.String.class);
		Type t = Type.class;
		InterTypeFieldDeclaration itdf = new InterTypeFieldDeclarationImpl(thisClass,"java.lang.Runnable",5,"f",stringType,t);
		assertEquals("f",itdf.getName());
		assertEquals(stringType,itdf.getType());
		assertEquals(t,itdf.getGenericType());
	}
	
	public void testITDCons() throws ClassNotFoundException, NoSuchMethodException {
		AjType<InterTypeTest> thisClass = AjTypeSystem.getAjType(InterTypeTest.class);
		AjType<Runnable> runnable = AjTypeSystem.getAjType(java.lang.Runnable.class);
		Method base = InterTypeTest.class.getDeclaredMethod("interCons",InterTypeTest.class,String.class,int.class);
		InterTypeConstructorDeclaration itdc = 
			new InterTypeConstructorDeclarationImpl(thisClass,"java.lang.Runnable",5,base);
		assertEquals(2,itdc.getParameterTypes().length);
		assertEquals(String.class,itdc.getParameterTypes()[0].getJavaClass());
		assertEquals(int.class,itdc.getParameterTypes()[1].getJavaClass());
		assertEquals(2,itdc.getGenericParameterTypes().length);
		assertEquals(base.getGenericParameterTypes()[1],((AjType<?>)itdc.getGenericParameterTypes()[0]).getJavaClass());
		assertEquals(base.getGenericParameterTypes()[2],((AjType<?>)itdc.getGenericParameterTypes()[1]).getJavaClass());
		assertEquals(0,itdc.getExceptionTypes().length);
	}
	
	public void testITDMethod() throws NoSuchMethodException {
		AjType<InterTypeTest> thisClass = AjTypeSystem.getAjType(InterTypeTest.class);
		AjType<Runnable> runnable = AjTypeSystem.getAjType(java.lang.Runnable.class);
		Method base = InterTypeTest.class.getDeclaredMethod("interMethod",InterTypeTest.class,String.class,int.class);
		InterTypeMethodDeclaration itdm = new InterTypeMethodDeclarationImpl(thisClass,"java.lang.Runnable",5,"foo",base);
		assertEquals("foo",itdm.getName());
		assertEquals(int.class,itdm.getReturnType().getJavaClass());
		assertEquals(int.class,((AjType<?>)itdm.getGenericReturnType()).getJavaClass());
		assertEquals(2,itdm.getParameterTypes().length);
		assertEquals(String.class,itdm.getParameterTypes()[0].getJavaClass());
		assertEquals(int.class,itdm.getParameterTypes()[1].getJavaClass());
		assertEquals(2,itdm.getGenericParameterTypes().length);
		assertEquals(base.getGenericParameterTypes()[1],((AjType<?>)itdm.getGenericParameterTypes()[0]).getJavaClass());
		assertEquals(base.getGenericParameterTypes()[2],((AjType<?>)itdm.getGenericParameterTypes()[1]).getJavaClass());
		assertEquals(0,itdm.getExceptionTypes().length);	
	}
	
	public static void interCons(InterTypeTest itt, String s, int i) {  }
	
	public static int interMethod(InterTypeTest itt, String s, int i) { return 5; }
}
