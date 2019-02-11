/*******************************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc192;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.NestHost;
import org.aspectj.apache.bcel.classfile.NestMembers;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc192Tests extends XMLBasedAjcTestCase {

	public void testITDLambdas() throws Exception {
		runTest("itd lambdas");
	}
	
	public void test11Flags() throws Exception {
		runTest("11flags");
	}

	public void testNestmates() throws Exception {
		runTest("nestmates");
		JavaClass outer = getClassFrom(ajc.getSandboxDirectory(), "Outer");
		JavaClass inner = getClassFrom(ajc.getSandboxDirectory(), "Outer$Inner");
		NestMembers nestMembers = (NestMembers) getAttributeStartsWith(outer.getAttributes(),"NestMembers");
		assertEquals(1,nestMembers.getClasses().length);
		assertEquals("Outer$Inner",nestMembers.getClassesNames()[0]);
		NestHost nestHost = (NestHost) getAttributeStartsWith(inner.getAttributes(),"NestHost");
		assertEquals("Outer",nestHost.getHostClassName());
	}

	// Verifying not destroyed on weaving
	public void testNestmates2() throws Exception {
		runTest("nestmates 2");
		JavaClass outer = getClassFrom(ajc.getSandboxDirectory(), "Outer2");
		JavaClass inner = getClassFrom(ajc.getSandboxDirectory(), "Outer2$Inner2");
		NestMembers nestMembers = (NestMembers) getAttributeStartsWith(outer.getAttributes(),"NestMembers");
		assertEquals(1,nestMembers.getClasses().length);
		assertEquals("Outer2$Inner2",nestMembers.getClassesNames()[0]);
		NestHost nestHost = (NestHost) getAttributeStartsWith(inner.getAttributes(),"NestHost");
		assertEquals("Outer2",nestHost.getHostClassName());
	}
	
	public void testCflowFinal() {
		runTest("no final on cflow elements");
	}
	
	// TODO Still to be fixed, the workaround to not mix style is good enough for now...
	public void xtestAroundAdvice_AnnoStyle() {
		runTest("around advice");
	}

	public void testAroundAdvice_CodeStyle() {
		runTest("around advice - 2");
	}

	public void testPTW_nonPrivileged() {
		runTest("ptw");
	}

	public void testPTW_nonPrivilegedSamePackage() {
		runTest("ptw - same package");
	}
	
	public void testPTW_privileged() {
		runTest("ptw - privileged");
	}

	public void testPTWW_privilegedSamePackage() {
		runTest("ptw - privileged same package");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc192Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
        return getClassResource("ajc192.xml");
	}

}
