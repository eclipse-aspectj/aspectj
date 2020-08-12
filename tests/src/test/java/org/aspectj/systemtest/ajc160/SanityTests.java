/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc160;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/*
 * Some very trivial tests that help verify things are OK.
 * Followed by some Java6 specific checks to ensure the class files are well formed.
 * A Java6 JDK is not required to run these tests as they introspect the .class files
 * rather than executing them.
 */
public class SanityTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// Incredibly trivial test programs that check the compiler works at all (these are easy-ish to debug)
	public void testSimpleJava_A() {
		runTest("simple - a");
	}

	public void testSimpleJava_B() {
		runTest("simple - b");
	}

	public void testSimpleCode_C() {
		runTest("simple - c");
	}

	public void testSimpleCode_D() {
		runTest("simple - d");
	}

	public void testSimpleCode_E() {
		runTest("simple - e");
	}

	public void testSimpleCode_F() {
		runTest("simple - f");
	}

	public void testSimpleCode_G() {
		runTest("simple - g");
	}

	public void testSimpleCode_H() {
		runTest("simple - h", true);
	}

	public void testSimpleCode_I() {
		runTest("simple - i");
	}

	// Check the version number in the classfiles is correct when Java6 options specified
	public void testVersionCorrect1() throws ClassNotFoundException {
		runTest("simple - j");
		checkVersion("A", 50, 0);
	}

	public void testVersionCorrect2() throws ClassNotFoundException {
		runTest("simple - k");
		checkVersion("A", 50, 0);
	}

	public void testVersionCorrect3() throws ClassNotFoundException {
		runTest("simple - l");
		checkVersion("A", 50, 0);
	}

	public void testVersionCorrect4() throws ClassNotFoundException {// check it is 49.0 when -1.5 is specified
		runTest("simple - m");
		checkVersion("A", 49, 0);
	}

	// Check the stackmap stuff appears for methods in a Java6 file
	// public void testStackMapAttributesAppear() throws ClassNotFoundException {
	// runTest("simple - n");
	// checkStackMapExistence("A","<init>_<clinit>");
	// checkStackMapExistence("X","<init>_<clinit>_ajc$pointcut$$complicatedPointcut$1fe");
	// }

	/* For the specified class, check that each method has a stackmap attribute */
	@SuppressWarnings("unused")
	private void checkStackMapExistence(String classname, String toIgnore) throws ClassNotFoundException {
		toIgnore = "_" + (toIgnore == null ? "" : toIgnore) + "_";
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), classname);
		Method[] methods = jc.getMethods();
		for (Method method : methods) {
			if (toIgnore.contains("_" + method.getName() + "_")) {
				continue;
			}
			boolean hasStackMapAttribute = findAttribute(method.getAttributes(), "StackMapTable");
			if (!hasStackMapAttribute) {
				fail("Could not find StackMap attribute for method " + method.getName());
			}
		}
	}

	private boolean findAttribute(Attribute[] attrs, String attributeName) {
		if (attrs == null) {
			return false;
		}
		for (Attribute attribute : attrs) {
			if (attribute.getName().equals(attributeName)) {
				return true;
			}
			// System.out.println(attribute.getName());
			if (attribute.getName().equals("Code")) {
				Code c = (Code) attribute;
				Attribute[] codeAttributes = c.getAttributes();
				for (Attribute codeAttribute : codeAttributes) {
					if (codeAttribute.getName().equals(attributeName)) {
						return true;
						// System.out.println(codeAttribute.getName());
					}
				}
			}
		}
		return false;
	}

	// Check the stackmap stuff is removed when a method gets woven (for now...)
	// public void testStackMapAttributesDeletedInWovenCode() {
	// fail("Not implemented");
	// }

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(SanityTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("sanity-tests.xml");
	}

}
