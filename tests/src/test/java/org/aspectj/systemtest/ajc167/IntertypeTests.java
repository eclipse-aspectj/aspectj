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
package org.aspectj.systemtest.ajc167;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * Tests for the new all singing all dancing intertype syntax.
 * 
 * @author Andy Clement
 */
public class IntertypeTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// absolutely trivial, just parse something and dont crash
	public void testSimple() {
		runTest("simple");
	}
	
	// simple field inside the intertype scope
	public void testSimpleWithField() {
		runTest("simple with field");
	}

	// simple field inside the intertype scope and method after the intertype scope
	public void testSimpleWithField2() {
		runTest("simple with field2");
	}
	
	// now a method that new's up an instance of the type targetted by the intertype scope
	public void testNewInstance() {
		runTest("new instance");
	}

	// now aspect method attempts to new up target of the itd scope and access something introduced by it
	public void testNewInstanceAndAccess() {
		runTest("new instance and access");
	}
	
	// two fields
	public void testNewInstanceAndAccess2() {
		runTest("new instance and access to two fields");
	}
	
	// more tests:
	// intertype X { int a,b,c=4; }
	// intertype X { int a=4,b=3; } // can we say that?

	// extends/implements on the intertype become declare parents
	// annotations on the intertype become declare @type
	
	// what about recovery when we have a problem leaving the scope of the intertype block? How do we make sure 
	// we don't accidentally use the activeScope for ordinary methods? Can we even check that...
	
	
	// --

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(IntertypeTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("intertype.xml");
//		return new File("../tests/src/org/aspectj/systemtest/ajc167/intertype.xml");
	}

}