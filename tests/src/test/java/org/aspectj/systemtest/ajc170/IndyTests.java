/*******************************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc170;

import java.io.IOException;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class IndyTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// staticinitialization on bytecode containing methodtyperef/methodhandle/bootstrapmethods/invokedynamic
	public void testInvokeDynamic_staticinitialization() throws IOException {
		runTest("indy - 1");
	}

	// execution pointcuts on same bytecode
	public void testInvokeDynamic_execution() throws IOException {
		runTest("indy - 2");
	}

	// call pointcuts on same bytecode
	public void testInvokeDynamic_call() throws IOException {
		runTest("indy - 3");
	}

	// within(Code1) on same bytecode
	public void testInvokeDynamic_target() throws IOException {
		runTest("indy - 4");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(IndyTests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("indy.xml");
//		return new File("../tests/src/org/aspectj/systemtest/ajc170/indy.xml");
	}

}
