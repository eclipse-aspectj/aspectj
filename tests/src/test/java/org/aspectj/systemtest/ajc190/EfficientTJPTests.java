/*******************************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc190;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Assert;
import junit.framework.Test;

/**
 * 
 * @author Andy Clement
 */
public class EfficientTJPTests extends XMLBasedAjcTestCase {

	public void testThisJoinPointMethodExecution() {
		// Test setting it via sys props rather than passing the option directly
		try {
			System.setProperty("ASPECTJ_OPTS", "-Xajruntimetarget:1.9");
			runTest("tjp 1");
			checkPreClinitContains("One","Factory.makeMethodSJP");
		} finally {
			System.setProperty("ASPECTJ_OPTS", "");
		}
	}

	public void testThisEnclosingJoinPointMethodExecution() {
		runTest("tjp 2");
		checkPreClinitContains("Two","Factory.makeMethodESJP");
	}

	public void testThisJoinPointConstructorExecution() {
		runTest("tjp 3");
		checkPreClinitContains("Three","Factory.makeConstructorSJP");
	}

	public void testThisEnclosingJoinPointConstructorExecution() {
		runTest("tjp 3a");
		checkPreClinitContains("ThreeA","Factory.makeConstructorESJP");
	}

	public void testThisJoinPointHandler() {
		runTest("tjp 4");
		checkPreClinitContains("Four","Factory.makeCatchClauseSJP");
	}

	public void testThisEnclosingJoinPointHandler() {
		runTest("tjp 4a");
		checkPreClinitContains("FourA","Factory.makeMethodESJP");
	}
	
	public void testThisJoinPointFieldGet() {
		runTest("tjp get fields");
		checkPreClinitContains("Fields","Factory.makeFieldSJP");
	}
	
	public void testThisEnclosingJoinPointFieldGet() {
		runTest("tjp get fieldsE");		
		checkPreClinitContains("FieldsE","Factory.makeMethodESJP");
	}
	
	public void testThisJoinPointFieldSet() {
		runTest("tjp set fields");		
		checkPreClinitContains("Fields2","Factory.makeFieldSJP");
	}

	public void testThisJoinPointClinit() {
		runTest("tjp clinit");		
		checkPreClinitContains("Clinit","Factory.makeInitializerSJP");
	}

	public void testThisEnclosingJoinPointClinit() {
		runTest("tejp clinit");
		checkPreClinitContains("ClinitE","Factory.makeInitializerESJP");
	}
	
	public void testThisJoinPointAdvice() {
		// covers enclosing joinpoint too
		runTest("tjp advice");
		checkPreClinitContains("X","Factory.makeAdviceESJP");
	}
	
	public void testThisJoinPointInitialization() {
		runTest("tjp init");
		checkPreClinitContains("A","Factory.makeConstructorESJP");
		checkPreClinitContains("B","Factory.makeConstructorESJP");
	}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(EfficientTJPTests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("features190.xml");
	}
	
	public void checkPreClinitContains(String classname, String text) {
		try {
			JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), classname);
			Method[] meths = jc.getMethods();
			for (Method method : meths) {
				if (method.getName().equals("ajc$preClinit")) {
					String code = method.getCode().getCodeString();
					assertTrue("Expected to contain '" + text + "':\n" + code, code.contains(text));
					return;
				}
			}
			Assert.fail("Unable to find ajc$preClinit in class "+classname);
		} catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

}
