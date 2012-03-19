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
package org.aspectj.systemtest.ajc170;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.UnresolvedType;

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

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(IndyTests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc170/indy.xml");
	}

}
