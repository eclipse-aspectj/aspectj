/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc164;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * <h4>Design and test coverage</h4><br>
 * In many ways the design is similar to DeclareParents now - so we have to plug in at the same points, but the code generation for
 * generating the delegate object and the choice of which interfaces (and methods within those) to mixin is different.
 * 
 * <h4>Design considerations:</h4><br>
 * <ul>
 * <li>model relationships
 * <li>incremental compilation
 * <li>ltw
 * <li>generic factory methods
 * <li>showWeaveInfo
 * <li>Clashing with existing methods
 * <li>varying parameter type on the factory method
 * <li>See CaseF - what if mixin target is not assignable to the parameter type? create cast?
 * </ul>
 * 
 * @author Andy Clement
 */
public class DeclareMixinTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// Very basics with a simple static factory method
	public void testCaseA() {
		runTest("casea");
	}

	// non static factory method, will need aspectOf() calling on
	// the aspect before the factory is called
	public void testCaseB() {
		runTest("caseb");
	}

	// factory method takes the object for which the delegate exists
	public void testCaseC() {
		runTest("casec");
	}

	// factory method is non static and takes the object for which the delegate is being created
	public void testCaseD() {
		runTest("cased");
	}

	// multiple instances causing factory invocation multiple times (but is cached)
	public void testCaseE() {
		runTest("casee");
	}

	// Factory method directly takes the type specified in the Mixin target (strongly typed)
	public void testCaseF() {
		runTest("casef");
	}

	// targeting multiple types from the Mixin
	public void testCaseG() {
		runTest("caseg");
	}

	// Null value for mixin target pattern
	public void testCaseH() {
		runTest("caseh");
	}

	// Invalid interfaces annotation value entries
	public void testCaseI() {
		runTest("casei");
	}

	// --

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(DeclareMixinTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc164/declareMixin.xml");
	}

}