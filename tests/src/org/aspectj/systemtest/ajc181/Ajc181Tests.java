/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc181;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc181Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testJarWeaving_433351_4() {
		runTest("jar weaving 4");
	}

	public void testJarWeaving_433351_3() {
		runTest("jar weaving 3");
	}

	public void testJarWeaving_433351_2() {
		runTest("jar weaving 2");
	}

	public void testJarWeaving_433351() {
		runTest("jar weaving");
	}
	
	public void testParameterNamesAttribute_436531() {
		runTest("parameter names attribute");
	}
	
	public void testVariableNotInitialized_431976() {
		runTest("variable not initialized");
	}

	public void testThisEnclosingJoinPointStaticPartNotInitialized_431976() {
		runTest("thisEnclosingJoinPointStaticPart not initialized");
	}
	
	public void testLvarTable_435446() throws Exception {
		runTest("lvartable");
	}
	
	public void testBrokenAnnotations_377096() throws Exception {
		runTest("broken annotations");
		Method method = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "C"), "xxx");
		method.getAnnotations();
	}
	
	public void testDefaultMethods_433744() {
		runTest("default methods");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc181Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc181/ajc181.xml");
	}

}
