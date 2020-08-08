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

import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.testing.Utils;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc163Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	public void testGenericMethodBridging_pr251326() {
		runTest("itd anonymous inner class in wrong package");
	}

	public void testOrderingRepetitive_pr259279() {
		runTest("ordering repetitive method");
	}

	public void testOrderingRepetitive_pr259279_2() {
		runTest("ordering repetitive method - 2");
	}

	public void testExtendingASI_pr252722() {
		runTest("extending AbstractSecurityInterceptor");
	}

	public void testExtendingASI_pr252722_2() {
		runTest("extending AbstractSecurityInterceptor - 2");
	}

	public void testExtendingASI_pr252722_3() {
		runTest("extending AbstractSecurityInterceptor - 3");
	}

	public void testExtendingASI_pr252722_4() {
		runTest("extending AbstractSecurityInterceptor - 4");
	}

	public void testGetNode_pr258653() {
		runTest("getNode");
	}

	public void testAtTargetPlus_pr255856() {
		runTest("attarget with plus");
	}

	public void testNonNullAtArgs_pr257833() {
		runTest("param annos and at args");
	}

	public void testIncorrectArgOrdering_pr219419() {
		runTest("incorrect arg ordering anno style");
	}

	public void testIncorrectArgOrdering_pr219419_2() {
		runTest("incorrect arg ordering anno style - 2");
	}

	public void testIncorrectArgOrdering_pr219419_3() {
		runTest("incorrect arg ordering anno style - 3");
	}

	// similar to 3 but parameters other way round
	public void testIncorrectArgOrdering_pr219419_4() {
		runTest("incorrect arg ordering anno style - 4");
	}

	// similar to 3 but also JoinPoint passed into advice
	public void testIncorrectArgOrdering_pr219419_5() {
		runTest("incorrect arg ordering anno style - 5");
	}

	public void testDecpAnnoStyle_pr257754() {
		runTest("decp anno style");
	}

	public void testDecpAnnoStyle_pr257754_2() {
		runTest("decp anno style - 2");
	}

	public void testPoorAtAjIfMessage_pr256458() {
		runTest("poor ataj if message - 1");
	}

	public void testPoorAtAjIfMessage_pr256458_2() {
		runTest("poor ataj if message - 2");
	}

	/*
	 * public void testInheritedAnnotations_pr128664() { runTest("inherited annotations"); }
	 * 
	 * public void testInheritedAnnotations_pr128664_2() { runTest("inherited annotations - 2"); }
	 */
	public void testGetMethodNull_pr154427() {
		runTest("getMethod returning null");
	}

	public void testItdOnAnonInner_pr171042() {
		runTest("itd on anonymous inner");
	}

	public void testMixedStyles_pr213751() {
		runTest("mixed styles");
	}

	public void testHandles_pr249216c24() {
		runTest("handles - escaped square brackets");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement ipe = null;
		ipe = findElementAtLine(top.getRoot(), 4);// public java.util.List<String> Ship.i(List<String>[][] u)
		assertEquals("<{Handles.java'Handles)Ship.i)\\[\\[QList\\<QString;>;", ipe.getHandleIdentifier());

		ipe = findElementAtLine(top.getRoot(), 7);// public java.util.List<String> Ship.i(Set<String>[][] u)
		assertEquals("<{Handles.java'Handles)Ship.i)\\[\\[QSet\\<QString;>;", ipe.getHandleIdentifier());

		// public java.util.Set<String> i(java.util.Set<String>[][] u)
		ipe = findElementAtLine(top.getRoot(), 10);
		assertEquals("<{Handles.java'Handles~i~\\[\\[Qjava.util.Set\\<QString;>;", ipe.getHandleIdentifier());

		ipe = findElementAtLine(top.getRoot(), 13);// public java.util.Set<String> i(java.util.Set<String>[][] u,int i) {
		assertEquals("<{Handles.java'Handles~i~\\[\\[Qjava.util.Set\\<QString;>;~I", ipe.getHandleIdentifier());

		ipe = findElementAtLine(top.getRoot(), 16);// public java.util.Set<String> i2(java.util.Set<? extends
		// Collection<String>>[][] u) {
		assertEquals("<{Handles.java'Handles~i2~\\[\\[Qjava.util.Set\\<+QCollection\\<QString;>;>;", ipe.getHandleIdentifier());

		ipe = findElementAtLine(top.getRoot(), 19);// public java.util.Set<String> i3(java.util.Set<? extends
		// Collection<String[]>>[][] u)
		assertEquals("<{Handles.java'Handles~i3~\\[\\[Qjava.util.Set\\<+QCollection\\<\\[QString;>;>;", ipe.getHandleIdentifier());

		ipe = findElementAtLine(top.getRoot(), 22);
		assertEquals("<{Handles.java'Handles~i4~Qjava.util.Set\\<+QCollection\\<QString;>;>;", ipe.getHandleIdentifier());

		ipe = findElementAtLine(top.getRoot(), 25);
		assertEquals("<{Handles.java'Handles~i5~Qjava.util.Set\\<*>;", ipe.getHandleIdentifier());

	}

	public void testFQType_pr256937() {
		runTest("fully qualified return type");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement itd = findElementAtLine(top.getRoot(), 10);
		String type = itd.getCorrespondingType(true);
		assertEquals("java.util.List<java.lang.String>", type);

		itd = findElementAtLine(top.getRoot(), 16);
		type = itd.getCorrespondingType(true);
		assertEquals("java.util.List<java.lang.String>", type);
	}

	private IProgramElement findElementAtLine(IProgramElement whereToLook, int line) {
		if (whereToLook == null) {
			return null;
		}
		if (whereToLook.getSourceLocation() != null && whereToLook.getSourceLocation().getLine() == line) {
			return whereToLook;
		}
		List<IProgramElement> kids = whereToLook.getChildren();
		for (IProgramElement object: kids) {
			if (object.getSourceLocation() != null && object.getSourceLocation().getLine() == line) {
				return object;
			}
			IProgramElement gotSomething = findElementAtLine(object, line);
			if (gotSomething != null) {
				return gotSomething;
			}
		}
		return null;
	}

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
		for (Method method : ms) {
			if (method.getName().equals("sayHello")) {
				m = method;
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

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc163.xml");
	}

	// ---

	private void print(LocalVariableTable localVariableTable) {
		LocalVariable[] lvs = localVariableTable.getLocalVariableTable();
		for (LocalVariable localVariable : lvs) {
			System.out.println(localVariable);
		}
	}

}