/*******************************************************************************
 * Copyright (c) 2012 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc180;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc180WeavingJava8Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testWeaveJava8Class() throws Exception {
		runTest("weave java8 class");
	}

	public void testWeaveJava8ClassDefaultMethod() throws Exception {
		runTest("weave java8 class - default method");
	}

	public void testAnnotatedSuperInterface() throws Exception {
		runTest("annotated superinterface");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc180WeavingJava8Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc180/ajc180.xml");
	}

}
