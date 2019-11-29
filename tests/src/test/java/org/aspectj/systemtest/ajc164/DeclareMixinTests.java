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

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * <h4>Design and test coverage</h4><br>
 * In many ways the design is similar to DeclareParents now - so we have to plug in at the same points, but the code generation for
 * generating the delegate object and the choice of which interfaces (and methods within those) to mixin is different.
 * 
 * <h4>Tested:</h4><br>
 * <ul>
 * <li>Factory method with void or primitive return value
 * <li>Check the factory method has at most one parameter
 * <li>incremental compilation
 * <li>error message if mixin target instance not compatible with factory method parameter
 * </ul>
 * 
 * <h4>Still to test/explore:</h4><br>
 * <ul>
 * <li>model relationships
 * <li>ltw
 * <li>generic factory methods
 * <li>showWeaveInfo
 * <li>Clashing with existing methods
 * <li>varying parameter type on the factory method
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

	// invalid return type for factory method
	public void testCaseJ() {
		runTest("casej");
	}

	// too many arguments to the factory method
	public void testCaseK() {
		runTest("casek");
	}

	// mixin of a class - should be an error (this one reported by the compiler due to a failed cast)
	public void testCaseL() {
		runTest("casel");
	}

	// mixin of a class - should be an error (this one reported by the annotation processing)
	public void testCaseM() {
		runTest("casem");
	}

	// factory returns class but interface specified - this is OK
	public void testCaseN() {
		runTest("casen");
	}

	// factory returns class but interface specified - not ok as class doesn't implement interface
	public void testCaseO() {
		runTest("caseo");
	}

	// interface subsetting used (factory returns class) - but only one method should be delegated
	public void testCaseP() {
		runTest("casep");
	}

	// factory return type implements two interfaces, both should be mixed as specified
	public void testCaseQ() {
		runTest("caseq");
	}

	// testing a pure marker interface - no methods added
	public void testCaseR() {
		runTest("caser");
	}

	// factory method has incompatible return type - verifyerror if we did use that factory
	public void testCaseS() {
		runTest("cases");
	}

	// weave info - what happens?
	public void testCaseT() {
		runTest("caset");
	}

	// --

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(DeclareMixinTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("declareMixin.xml");
	}

}