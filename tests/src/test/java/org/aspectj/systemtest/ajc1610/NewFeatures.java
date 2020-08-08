/*******************************************************************************
 * Copyright (c) 2010 Lucierna 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abraham Nevado - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc1610;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class NewFeatures extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testMakeSJPOptimizationLDCNo() {
		this.runTest("makeSJP optimization - LDC - No");
		try {
			JavaClass myClass = getMyClass("B");
			Method preClinitMethod = getPreClinitMethod(myClass);
			NewFeatures.assertTrue("For 1.4 it must use classForName", preClinitMethod.getCode().toString().contains("forName"));
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	@SuppressWarnings("unused")
	public void testMakeSJPOptimizationCollapsedSJPYes14() {
		this.runTest("makeSJP optimization - Collapsed SJP - Yes 1.4");
		try {
			JavaClass myClass = getMyClass("B");
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	public void testMakeSJPOptimizationLDCYes() {
		this.runTest("makeSJP optimization - LDC - Yes");
		try {
			JavaClass myClass = getMyClass("B");
			Method preClinitMethod = getPreClinitMethod(myClass);
			NewFeatures.assertTrue("For 1.5 it must not use classForName", !preClinitMethod.getCode().toString()
					.contains("forName"));
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	public void testMakeSJPOptimizationCollapsedSJPYes() {
		this.runTest("makeSJP optimization - Collapsed SJP - Yes");
		try {
			JavaClass myClass = getMyClass("B");
			Method preClinitMethod = getPreClinitMethod(myClass);
			NewFeatures.assertTrue("MakedMethodSig MUST not be present",
					!preClinitMethod.getCode().toString().contains("makeMethodSig"));
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	public void testMakeSJPOptimizationCollapsedSJPNo() {
		this.runTest("makeSJP optimization - Collapsed SJP - No");
		try {
			JavaClass myClass = getMyClass("B");
			Method preClinitMethod = getPreClinitMethod(myClass);
			NewFeatures.assertTrue("MakedMethodSig required", preClinitMethod.getCode().toString().contains("makeMethodSig"));
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	public void testMakeSJPOptimizationNoExceptionNo() {
		this.runTest("makeSJP optimization - No Exception - No");
		try {
			JavaClass myClass = getMyClass("B");
			Method preClinitMethod = getPreClinitMethod(myClass);
			NewFeatures
					.assertTrue(
							"MakedMethodSig required",
							preClinitMethod
									.getCode()
									.toString()
									.contains(
											"invokevirtual	org.aspectj.runtime.reflect.Factory.makeMethodSig (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/aspectj/lang/reflect/MethodSignature;"));
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	public void testMakeSJPOptimizationNoExceptionYes() {
		this.runTest("makeSJP optimization - No Exception - Yes");
		try {
			JavaClass myClass = getMyClass("B");
			Method preClinitMethod = getPreClinitMethod(myClass);
			NewFeatures
					.assertTrue(
							"MakedMethodSig required",
							preClinitMethod
									.getCode()
									.toString()
									.contains(
											"org.aspectj.runtime.reflect.Factory.makeSJP (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Lorg/aspectj/lang/JoinPoint$StaticPart;"));
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	public void testMakeSJPOptimizationRemoveExtraColon() {
		this.runTest("makeSJP optimization - Remove Colon");
		try {
			JavaClass myClass = getMyClass("B");
			Method preClinitMethod = getPreClinitMethod(myClass);
			System.out.println(preClinitMethod.getCode().toString());
			NewFeatures.assertTrue("MakedMethodSig required",
					preClinitMethod.getCode().toString().contains("50:   ldc		\"java.lang.String\" (108)"));
		} catch (Exception e) {
			NewFeatures.fail(e.toString());
		}
	}

	// ///////////////////////////////////////

	private Method getPreClinitMethod(JavaClass myClass) {
		Method lm[] = myClass.getMethods();
		for (Method method : lm) {
			if (method.getName().equals("ajc$preClinit")) {
				return method;
			}
		}
		return null;
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(NewFeatures.class);
	}

	private JavaClass getMyClass(String className) throws ClassNotFoundException {
		return getClassFrom(ajc.getSandboxDirectory(), className);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("newfeatures-tests.xml");
	}

}