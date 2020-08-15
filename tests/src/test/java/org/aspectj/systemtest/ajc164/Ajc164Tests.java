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

import java.io.PrintWriter;
import java.util.List;

import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc164Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testGenericAspectsNpe_pr268689() {
		runTest("generics aspects npe");
	}

	public void testGenericAspectsNpe_pr268710() {
		runTest("generics aspects incorrect error");
	}

	public void testGenericsItdNpe_pr267559() {
		runTest("generics and itd npe");
	}

	public void testAnnoStyleLong_pr266564() {
		runTest("annotation style long");
	}

	public void testAnnoStyleLong_pr266564_2() {
		runTest("annotation style long - 2");
	}

	public void testUnusedPrivateWarning_pr266420() {
		runTest("unused private warning");
	}

	public void testUnusedPrivateWarning_pr266420_2() {
		runTest("unused private warning - 2");
	}

	public void testUnusedPrivateWarning_pr266420_3() {
		runTest("unused private warning - 3");
	}

	/**
	 * This test program can be used to compare handles for faulted in binary aspects with handles that would be used if the aspect
	 * was available as source. There are two compile steps in the xml for the test - commenting out the first will allow the source
	 * handles to be seen, leaving it in will switch to binary. Effectively the only difference should be that in the binary case
	 * the handles are prefixed 'binaries'.
	 * 
	 * Change due to bug 274558: now AJDT wants (blah.class as the 'source file' for the ITD so that is another difference when
	 * switching.
	 */
	public void testItdsAspectPathModel_pr265729_1() {
		runTest("aspectpath model");
		AsmManager model = AsmManager.lastActiveStructureModel;
		IHierarchy top = model.getHierarchy();
		printModel(model);
		IProgramElement ipe = null;

		// ITD METHOD
		// should be the ITD method from the binary aspect:
		// public Color Orange.getColor() { return Color.orange; }
		ipe = top.findElementForType("demo", "Orange");
		assertNotNull(ipe);
		assertEquals("<demo{Orange.java[Orange", ipe.getHandleIdentifier());
		IRelationship ir = model.getRelationshipMap().get(ipe).get(0);
		String itdMethodHandle = ir.getTargets().get(0);
		// handle when all source: <{Aspect.java}Aspect)Orange.getColor
		// assertEquals("/binaries<{Aspect.java}Aspect)Orange.getColor", itdMethodHandle);
		assertEquals("/binaries<(Aspect.class'Aspect)Orange.getColor", itdMethodHandle);
		IProgramElement itdpe = model.getHierarchy().findElementForHandle(itdMethodHandle);
		assertEquals("java.awt.Color", itdpe.getCorrespondingType(true));

		// ITD FIELD
		// should be the ITD field from the binary aspect:
		// public Color Strawberry.color
		ipe = top.findElementForType("demo", "Strawberry");
		assertNotNull(ipe);
		assertEquals("<demo{Strawberry.java[Strawberry", ipe.getHandleIdentifier());
		ir = model.getRelationshipMap().get(ipe).get(0);
		String itdFieldHandle = ir.getTargets().get(0);
		// source handle <{Aspect.java}Aspect)Strawberry.color
		// assertEquals("/binaries<{Aspect.java}Aspect)Strawberry.color", itdFieldHandle);
		assertEquals("/binaries<(Aspect.class'Aspect,Strawberry.color", itdFieldHandle);
		IProgramElement itdfpe = model.getHierarchy().findElementForHandle(itdMethodHandle);
		assertEquals("java.awt.Color", itdfpe.getCorrespondingType(true));

		// ITD CONSTRUCTOR
		// /binaries< Aspect.java}Aspect)java.awt.Color demo.Strawberry.color
		ipe = top.findElementForType("demo", "Fruit");
		assertNotNull(ipe);
		assertEquals("<demo{Fruit.java[Fruit", ipe.getHandleIdentifier());
		ir = model.getRelationshipMap().get(ipe).get(0);
		String itdCtorHandle = ir.getTargets().get(0);
		// source handle <{Aspect.java}Aspect)Fruit.Fruit_new)QColor;)QString;
		// assertEquals("/binaries<{Aspect.java}Aspect)Fruit.Fruit_new)QColor;)QString;", itdCtorHandle);
		assertEquals("/binaries<(Aspect.class'Aspect)Fruit.Fruit_new)QColor;)QString;", itdCtorHandle);
		IProgramElement itdcpe = model.getHierarchy().findElementForHandle(itdCtorHandle);
		List<char[]> ptypes = itdcpe.getParameterTypes();
		assertEquals("java.awt.Color", new String(ptypes.get(0)));
		assertEquals("java.lang.String", new String(ptypes.get(1)));
	}

	private void printModel(AsmManager model) {
		try {
			AsmManager.dumptree(model.getHierarchy().getRoot(), 0);
			model.dumprels(new PrintWriter(System.out));
		} catch (Exception e) {
		}
	}

	public void testGenericsAopXml_pr266220() {
		runTest("generics and aop.xml");
	}

	public void testOptimizingIf_pr266165_1() {
		runTest("optimizing if for constant reference - 1");
	}

	public void testOptimizingIf_pr266165_2() {
		runTest("optimizing if for constant reference - 2");
	}

	public void testOptimizingIf_pr266165_3() {
		runTest("optimizing if for constant reference - 3");
	}

	public void testOptimizingIf_pr266165_4() {
		runTest("optimizing if for constant reference - 4");
	}

	// public void testAnnoInherited_pr265695() {
	// runTest("anno inherited");
	// }
	//
	// public void testAnnoInherited_pr265695_2() {
	// runTest("new syntax for inherited anno - 1");
	// }
	//
	// public void testAnnoInherited_pr265695_3() {
	// runTest("new syntax for inherited anno - 3");
	// }

	public void testParserProblemSubArrayPatterns_pr148508() {
		runTest("parser problem for array subtypes");
	}

	public void testVarargs_pr265418() {
		runTest("varargs");
	}

	public void testIncorrectDateResolution_pr265360() {
		runTest("incorrect resolution of Date");
	}

	public void testDualPreClinit_pr233032() {
		runTest("dual preClinit");
	}

	public void testHandles_pr263310() {
		runTest("inner handles");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement ipe = null;
		ipe = findElementAtLine(top.getRoot(), 13);
		assertEquals("<p{HandleTestingAspect.java'HandleTestingAspect[InnerClass'InnerInnerAspect|1", ipe.getHandleIdentifier());
		// ipe = findElementAtLine(top.getRoot(), 29);
		// assertEquals("<x*OverrideOptions.aj}OverrideOptions&around!2",
		// ipe.getHandleIdentifier());
	}

	public void testHandles_pr263666() {
		runTest("around advice handles");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement ipe = null;
		ipe = findElementAtLine(top.getRoot(), 22);
		assertEquals("<x*OverrideOptions.aj'OverrideOptions&around", ipe.getHandleIdentifier());
		ipe = findElementAtLine(top.getRoot(), 29);
		assertEquals("<x*OverrideOptions.aj'OverrideOptions&around!2", ipe.getHandleIdentifier());
	}

	// Only one of two aspects named
	public void testAopConfig1() {
		runTest("aop config - 1");
	}

	// Only one of two aspects named - and named one is scoped to only affect
	// one type
	public void testAopConfig2() {
		runTest("aop config - 2");
	}

	// Invalid scope specified - cannot be parsed as type pattern
	public void testAopConfig3() {
		runTest("aop config - 3");
	}

	// excluding types from weaving
	public void testAopConfig4() {
		runTest("aop config - 4");
	}

	// excluding types from weaving
	public void testAopConfig5() {
		runTest("aop config - 5");
	}

	public void testAjcThisNotRead() {
		runTest("ajcthis not read");
	}

	public void testRecursiveCflow() {
		runTest("recursive cflow");
	}

	public void testAnnoDecprecedence_pr256779() {
		runTest("anno decprecedence");
	}

	//
	public void testBrokenLVT_pr194314_1() throws Exception {
		runTest("broken lvt - 1");
		Method m = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "Service"), "method_aroundBody1$advice");
		if (m.getLocalVariableTable() == null) {
			fail("Local variable table should not be null");
		}
		// Method:
		// private static final void method_aroundBody1$advice(Service, long,
		// JoinPoint, ServiceInterceptor, ProceedingJoinPoint);
		LocalVariable[] lvt = m.getLocalVariableTable().getLocalVariableTable();
		assertEquals(7, lvt.length); // no aroundClosure compared to second
		// version of this test
		assertEquals("LService; ajc$this(0) start=0 len=86", stringify(m.getLocalVariableTable(), 0));
		assertEquals("J l(1) start=0 len=86", stringify(m.getLocalVariableTable(), 1));
		assertEquals("Lorg/aspectj/lang/JoinPoint; thisJoinPoint(3) start=0 len=86", stringify(m.getLocalVariableTable(), 2));
		assertEquals("LServiceInterceptor; ajc$aspectInstance(4) start=0 len=86", stringify(m.getLocalVariableTable(), 3));
		assertEquals("Lorg/aspectj/lang/ProceedingJoinPoint; pjp(5) start=0 len=86", stringify(m.getLocalVariableTable(), 4));
		assertEquals("[Ljava/lang/Object; args(6) start=9 len=77", stringify(m.getLocalVariableTable(), 5));
		assertEquals("J id(7) start=21 len=65", stringify(m.getLocalVariableTable(), 6));

	}

	public void testBrokenLVT_pr194314_2() throws Exception {
		runTest("broken lvt - 2");
		Method m = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "Service"), "method_aroundBody1$advice");
		if (m.getLocalVariableTable() == null) {
			fail("Local variable table should not be null");
		}
		System.out.println(m.getLocalVariableTable());
		LocalVariable[] lvt = m.getLocalVariableTable().getLocalVariableTable();
		assertEquals(8, lvt.length);
		// private static final void method_aroundBody1$advice(Service, long,
		// JoinPoint, ServiceInterceptorCodeStyle, AroundClosure,
		// JoinPoint);
		assertEquals("LService; ajc$this(0) start=0 len=68", stringify(m.getLocalVariableTable(), 0));
		assertEquals("J l(1) start=0 len=68", stringify(m.getLocalVariableTable(), 1));
		assertEquals("Lorg/aspectj/lang/JoinPoint; thisJoinPoint(3) start=0 len=68", stringify(m.getLocalVariableTable(), 2));
		assertEquals("LServiceInterceptorCodeStyle; ajc$aspectInstance(4) start=0 len=68", stringify(m.getLocalVariableTable(), 3));
		assertEquals("Lorg/aspectj/runtime/internal/AroundClosure; ajc$aroundClosure(5) start=0 len=68",
				stringify(m.getLocalVariableTable(), 4));
		assertEquals("Lorg/aspectj/lang/JoinPoint; thisJoinPoint(6) start=0 len=68", stringify(m.getLocalVariableTable(), 5));
		assertEquals("[Ljava/lang/Object; args(7) start=9 len=59", stringify(m.getLocalVariableTable(), 6));
		assertEquals("J id(8) start=21 len=47", stringify(m.getLocalVariableTable(), 7));
	}

	/**
	 * This test checks that local variable table for the interMethodDispatcher is built correctly, for the related code see
	 * IntertypeMethodDeclaration.generateDispatchMethod(). It checks non-static and static ITDs. Once the information here is
	 * correct then around advice on ITDs can also be correct.
	 */
	public void testBrokenLVT_pr194314_3() throws Exception {
		runTest("broken lvt - 3");
		// Check intermethoddispatchers have the lvts correct
		// first ITD: public void I.foo(String s,int i,String[] ss) {}

		Method m = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "X"), "ajc$interMethodDispatch1$X$I$foo");
		LocalVariableTable lvt = m.getLocalVariableTable();
		assertNotNull(lvt);
		assertEquals("LI; ajc$this_(0) start=0 len=10", stringify(lvt, 0));
		assertEquals("Ljava/lang/String; s(1) start=0 len=10", stringify(lvt, 1));
		assertEquals("I i(2) start=0 len=10", stringify(lvt, 2));
		assertEquals("[Ljava/lang/String; ss(3) start=0 len=10", stringify(lvt, 3));

		// second ITD: public void I.fooStatic(Long l,int i,String[] ss) {}
		m = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "X"), "ajc$interMethodDispatch1$X$C$fooStatic");
		lvt = m.getLocalVariableTable();
		assertNotNull(lvt);
		assertEquals("J l(0) start=0 len=7", stringify(lvt, 0));
		assertEquals("I i(2) start=0 len=7", stringify(lvt, 1));
		assertEquals("[Ljava/lang/String; ss(3) start=0 len=7", stringify(lvt, 2));

		// Now let's check the around advice on the calls to those ITDs

		// non-static:

		m = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "C"), "foo_aroundBody1$advice");
		lvt = m.getLocalVariableTable();
		assertNotNull(lvt);

		assertEquals("LC; ajc$this(0) start=0 len=0", stringify(lvt, 0));
		assertEquals("LI; target(1) start=0 len=0", stringify(lvt, 1));
		assertEquals("Ljava/lang/String; s(2) start=0 len=0", stringify(lvt, 2));
		assertEquals("I i(3) start=0 len=0", stringify(lvt, 3));
		assertEquals("[Ljava/lang/String; ss(4) start=0 len=0", stringify(lvt, 4));
		assertEquals("LX; ajc$aspectInstance(5) start=0 len=0", stringify(lvt, 5));
		assertEquals("Lorg/aspectj/runtime/internal/AroundClosure; ajc$aroundClosure(6) start=0 len=0", stringify(lvt, 6));

		// static:

		m = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "C"), "fooStatic_aroundBody3$advice");
		lvt = m.getLocalVariableTable();
		assertNotNull(lvt);

		assertEquals("LC; ajc$this(0) start=0 len=0", stringify(lvt, 0));
		assertEquals("J l(1) start=0 len=0", stringify(lvt, 1));
		assertEquals("I i(3) start=0 len=0", stringify(lvt, 2));
		assertEquals("[Ljava/lang/String; ss(4) start=0 len=0", stringify(lvt, 3));
		assertEquals("LX; ajc$aspectInstance(5) start=0 len=0", stringify(lvt, 4));
		assertEquals("Lorg/aspectj/runtime/internal/AroundClosure; ajc$aroundClosure(6) start=0 len=0", stringify(lvt, 5));

	}

	// Single piece of advice on before execution of a method with a this and a
	// parameter
	public void testDebuggingBeforeAdvice_pr262509() throws Exception {
		runTest("debugging before advice");
		Method method = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "Foo"), "foo");
		assertEquals("LFoo; this(0) start=0 len=13", stringify(method.getLocalVariableTable(), 0));
		assertEquals("LBar; bar(1) start=0 len=13", stringify(method.getLocalVariableTable(), 1));
	}

	// Single piece of advice on before execution of a method with a this and a
	// parameter and other various locals within it
	public void testDebuggingBeforeAdvice_pr262509_2() throws Exception {
		// Compile with -preserveAllLocals
		runTest("debugging before advice - 2");
		Method method = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "Foo2"), "foo");
		// System.out.println(stringify(method.getLocalVariableTable()));
		List<LocalVariable> l = sortedLocalVariables(method.getLocalVariableTable());
		assertEquals("LBar; bar(1) start=0 len=34", stringify(l, 0));
		assertEquals("Ljava/lang/Exception; e(3) start=29 len=4", stringify(l, 1));
		assertEquals("LFoo2; this(0) start=0 len=34", stringify(l, 4));
		assertEquals("Ljava/lang/String; s(2) start=15 len=19", stringify(l, 2));
		// With the 1.8 compiler looks like len=7 and not len=10 here, the goto to jump to the return is no longer included 
		// in the variable range
		assertEquals("Ljava/lang/String; s2(3) start=18 len=7", stringify(l, 3));
	}

	// Two pieces of advice on before execution of a method with a this and a
	// parameter and another local within it
	public void testDebuggingBeforeAdvice_pr262509_3() throws Exception {
		// Compile with -preserveAllLocals
		runTest("debugging before advice - 3");
		Method method = getMethodFromClass(getClassFrom(ajc.getSandboxDirectory(), "Foo3"), "foo");
		System.out.println(stringify(method.getLocalVariableTable()));
		assertEquals("LFoo3; this(0) start=0 len=35", stringify(method.getLocalVariableTable(), 0));
		assertEquals("LBar; bar(1) start=0 len=35", stringify(method.getLocalVariableTable(), 1));
		assertEquals("Ljava/lang/Exception; e(2) start=30 len=4", stringify(method.getLocalVariableTable(), 2));
	}

	public void testRogueErrors_pr246393_1() {
		runTest("rogue errors - 1");
	}

	// public void testNameClash_pr262257() {
	// runTest("name clash");
	// fail("incomplete");
	// }

	public void testCompilingSpring_pr260384() {
		runTest("compiling spring");
	}

	public void testCompilingSpring_pr260384_2() {
		runTest("compiling spring - 2");
	}

	public void testCompilingSpring_pr260384_3() {
		runTest("compiling spring - 3");
	}

	public void testCompilingSpring_pr260384_4() {
		runTest("compiling spring - 4");
	}

	public void testAtAspectJDecp_pr164016() {
		runTest("ataspectj decp 164016");
	}

	public void testAtAspectJDecp_pr258788() {
		runTest("ataspectj decp 258788");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc164Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc164.xml");
	}

	private IProgramElement findElementAtLine(IProgramElement whereToLook, int line) {
		if (whereToLook == null) {
			return null;
		}
		if (whereToLook.getSourceLocation() != null && whereToLook.getSourceLocation().getLine() == line) {
			return whereToLook;
		}
		List<IProgramElement> kids = whereToLook.getChildren();
		for (IProgramElement object : kids) {
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
}