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
package org.aspectj.systemtest.ajc163;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.Utils;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc163Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testParameterAnnotationsOnITDs_pr256669() { // regular itd
		runTest("parameter annotations on ITDs");
	}

	public void testParameterAnnotationsOnITDs_pr256669_2() { // static itd
		runTest("parameter annotations on ITDs - 2");
	}

	public void testParameterAnnotationsOnITDs_pr256669_3() { // multiple parameters
		runTest("parameter annotations on ITDs - 3");
	}

	public void testParameterAnnotationsOnITDs_pr256669_4() { // itd on interface
		runTest("parameter annotations on ITDs - 4");
	}

	public void testOrderingIssue_1() {
		runTest("ordering issue");
	}

	public void testOrderingIssue_2() {
		runTest("ordering issue - 2");
	}

	// public void testGenericPointcuts_5() {
	// runTest("generic pointcuts - 5");
	// }

	public void testGenericPointcuts_1() {
		runTest("generic pointcuts - 1");
	}

	public void testGenericPointcuts_2() {
		runTest("generic pointcuts - 2");
	}

	public void testGenericPointcuts_3() {
		runTest("generic pointcuts - 3");
	}

	public void testGenericPointcuts_4() {
		runTest("generic pointcuts - 4");
	}

	// public void testBrokenLVT_pr194314_1() throws Exception {
	// runTest("broken lvt - 1");
	// JavaClass jc = Utils.getClassFrom(ajc.getSandboxDirectory().getAbsolutePath(), "Service");
	// Method[] ms = jc.getMethods();
	// Method m = null;
	// for (int i = 0; i < ms.length; i++) {
	// if (ms[i].getName().equals("method_aroundBody1$advice")) {
	// m = ms[i];
	// }
	// }
	// if (m.getLocalVariableTable() == null) {
	// fail("Local variable table should not be null");
	// }
	// System.out.println(m.getLocalVariableTable());
	// LocalVariable[] lvt = m.getLocalVariableTable().getLocalVariableTable();
	// assertEquals(8, lvt.length);
	// }
	//
	// public void testBrokenLVT_pr194314_2() throws Exception {
	// runTest("broken lvt - 2");
	// JavaClass jc = Utils.getClassFrom(ajc.getSandboxDirectory().getAbsolutePath(), "Service");
	// Method[] ms = jc.getMethods();
	// Method m = null;
	// for (int i = 0; i < ms.length; i++) {
	// if (ms[i].getName().equals("method_aroundBody1$advice")) {
	// m = ms[i];
	// }
	// }
	// if (m.getLocalVariableTable() == null) {
	// fail("Local variable table should not be null");
	// }
	// System.out.println(m.getLocalVariableTable());
	// LocalVariable[] lvt = m.getLocalVariableTable().getLocalVariableTable();
	// assertEquals(8, lvt.length);
	// // assertEquals(2, m.getLocalVariableTable().getLocalVariableTable().length);
	//
	// // Before I've started any work on this:
	// // LocalVariable(start_pc = 0, length = 68, index = 0:ServiceInterceptorCodeStyle this)
	// // LocalVariable(start_pc = 0, length = 68, index = 1:org.aspectj.runtime.internal.AroundClosure ajc_aroundClosure)
	// // LocalVariable(start_pc = 0, length = 68, index = 2:org.aspectj.lang.JoinPoint thisJoinPoint)
	// // LocalVariable(start_pc = 9, length = 59, index = 3:Object[] args)
	// // LocalVariable(start_pc = 21, length = 47, index = 4:long id)
	//
	// // Method signature:
	// // private static final void method_aroundBody1$advice(Service, long, org.aspectj.lang.JoinPoint,
	// // ServiceInterceptorCodeStyle, org.aspectj.runtime.internal.AroundClosure, org.aspectj.lang.JoinPoint);
	// //
	// // Service, JoinPoint, ServiceInterceptorCodeStyle, AroundClosure, JoinPoint
	//
	// // args should be in slot 7 and the long in position 8
	//
	// }

	public void testDontAddMethodBodiesToInterface_pr163005() {
		runTest("do not add method bodies to an interface");
	}

	public void testDontAddMethodBodiesToInterface_pr163005_2() {
		runTest("do not add method bodies to an interface - 2");
	}

	public void testDontAddMethodBodiesToInterface_pr163005_3() {
		runTest("do not add method bodies to an interface - 3");
	}

	public void testMissingLocalVariableTableEntriesOnAroundAdvice_pr173978() throws Exception {
		runTest("missing local variable table on around advice");
		JavaClass jc = Utils.getClassFrom(ajc.getSandboxDirectory().getAbsolutePath(), "Test");
		Method[] ms = jc.getMethods();
		Method m = null;
		for (int i = 0; i < ms.length; i++) {
			if (ms[i].getName().equals("sayHello")) {
				m = ms[i];
			}
		}
		if (m.getLocalVariableTable() == null) {
			fail("Local variable table should not be null");
		}
		assertEquals(2, m.getLocalVariableTable().getLocalVariableTable().length);
		// LocalVariableTable:
		// Start Length Slot Name Signature
		// 0 12 0 this LTest;
		// 0 12 1 message Ljava/lang/String;
		LocalVariable lv = m.getLocalVariableTable().getLocalVariable(0);
		assertNotNull(lv);
		assertEquals("this", lv.getName());
		assertEquals(0, lv.getStartPC(), 0);
		assertEquals(12, lv.getLength(), 12);
		assertEquals("LTest;", lv.getSignature());
		lv = m.getLocalVariableTable().getLocalVariable(1);
		assertNotNull(lv);
		assertEquals("message", lv.getName());
		assertEquals(0, lv.getStartPC(), 0);
		assertEquals(12, lv.getLength(), 12);
		assertEquals("Ljava/lang/String;", lv.getSignature());
		// print(m.getLocalVariableTable());
	}

	public void testTerminateAfterCompilation_pr249710() {
		runTest("terminateAfterCompilation");
	}

	public void testItdCCE_pr250091() {
		runTest("itd cce");
	}

	public void testBreakingRecovery_pr226163() {
		runTest("breaking recovery");
	}

	public void testGenericMethodConversions_pr250632() {
		runTest("type conversion in generic itd");
	}

	public void testGenericMethodBridging_pr250493() {
		runTest("bridge methods for generic itds");
	}

	public void testGenericFieldBridging_pr252285() {
		runTest("bridge methods for generic itd fields");
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc163Tests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc163/ajc163.xml");
	}

	// ---

	private void print(LocalVariableTable localVariableTable) {
		LocalVariable[] lvs = localVariableTable.getLocalVariableTable();
		for (int i = 0; i < lvs.length; i++) {
			LocalVariable localVariable = lvs[i];
			System.out.println(localVariable);
		}
	}

}