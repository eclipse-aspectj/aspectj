/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.systemtest.ajc153;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class JDTLikeHandleProviderTests extends XMLBasedAjcTestCase {

	// IElementHandleProvider handleProvider;

	protected void setUp() throws Exception {
		super.setUp();
		// handleProvider = AsmManager.getDefault().getHandleProvider();
		// AsmManager.getDefault().setHandleProvider(new JDTLikeHandleProvider());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		// AsmManager.getDefault().setHandleProvider(handleProvider);
	}

	public void testMoreThanOneNamedPointcut() {
		runTest("More than one named pointcut");
	}

	public void testAspectHandle() {
		runTest("aspect handle");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement pe = top.findElementForType("pkg", "A1");
		String expected = "<pkg*A1.aj'A1";
		String found = pe.getHandleIdentifier();
		assertEquals("handleIdentifier - expected " + expected + ", but found " + found, expected, found);
	}

	public void testAdviceHandle() {
		runTest("advice handle");
		compareHandles(IProgramElement.Kind.ADVICE, "before(): <anonymous pointcut>", "<pkg*A2.aj'A2&before");
	}

	public void testPointcutHandle() {
		runTest("pointcut handle");
		compareHandles(IProgramElement.Kind.POINTCUT, "p()", "<pkg*A4.aj'A4\"p");
	}

	public void testGetIPEWithAspectHandle() {
		runTest("get IProgramElement with aspect handle");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle = "<pkg*A1.aj'A1";
		IProgramElement ipe = top.getElement(handle);
		assertNotNull("should have found ipe with handle " + handle, ipe);
		IProgramElement ipe2 = top.getElement(handle);
		assertEquals("should be the same IPE", ipe, ipe2);
	}

	public void testAdviceHandleWithCrossCutting() {
		runTest("advice handle with crosscutting");
		compareHandles(IProgramElement.Kind.ADVICE, "before(): <anonymous pointcut>", "<pkg*A3.aj'A3&before");
	}

	public void testPointcutHandleWithArgs() {
		runTest("pointcut handle with args");
		compareHandles(IProgramElement.Kind.POINTCUT, "p(java.lang.Integer)", "<*A6.aj'A6\"p\"QInteger;");
	}

	public void testAdviceHandleWithArgs() {
		runTest("advice handle with args");
		compareHandles(IProgramElement.Kind.ADVICE, "afterReturning(java.lang.Integer): p..",
				"<pkg*A8.aj'A8&afterReturning&QInteger;");
	}

	public void testFieldITD() {
		runTest("field itd handle");
		compareHandles(IProgramElement.Kind.INTER_TYPE_FIELD, "C.x", "<pkg*A9.aj'A9,C.x");
	}

	public void testMethodITD() {
		runTest("method itd handle");
		compareHandles(IProgramElement.Kind.INTER_TYPE_METHOD, "C.method()", "<pkg*A9.aj'A9)C.method");
	}

	public void testMethodITDWithArgs() {
		runTest("method itd with args handle");
		compareHandles(IProgramElement.Kind.INTER_TYPE_METHOD, "C.methodWithArgs(int)", "<pkg*A9.aj'A9)C.methodWithArgs)I");
	}

	public void testConstructorITDWithArgs() {
		runTest("constructor itd with args");
		compareHandles(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR, "C.C(int,java.lang.String)",
				"<pkg*A13.aj'A13)C.C_new)I)QString;");
	}

	public void testDeclareParentsHandle() {
		runTest("declare parents handle");
		compareHandles(IProgramElement.Kind.DECLARE_PARENTS, "declare parents: implements C2", "<pkg*A7.aj'A7`declare parents");
	}

	public void testTwoDeclareParents() {
		runTest("two declare parents in same file");
		compareHandles(IProgramElement.Kind.DECLARE_PARENTS, "declare parents: extends C5", "<pkg*A7.aj'A7`declare parents!2");
	}

	public void testMethodCallHandle() {
		runTest("method call handle");
		compareHandles(IProgramElement.Kind.CODE, "method-call(void pkg.C.m2())", "<pkg*A10.aj[C~m1?method-call(void pkg.C.m2())");
	}

	public void testDeclareAtType() {
		// AJDT: =AJHandleProject/src<pkg*A.aj}A`declare \@type
		runTest("declare @type");
		compareHandles(IProgramElement.Kind.DECLARE_ANNOTATION_AT_TYPE, "declare @type: pkg.C : @MyAnnotation",
				"<pkg*A12.aj'A`declare \\@type");
	}

	public void testDeclareAtField() {
		// AJDT: =AJHandleProject/src<pkg*A.aj}A`declare \@field
		runTest("declare @field");
		compareHandles(IProgramElement.Kind.DECLARE_ANNOTATION_AT_FIELD, "declare @field: int pkg.C.someField : @MyAnnotation",
				"<pkg*A12.aj'A`declare \\@field");
	}

	public void testDeclareAtMethod() {
		// AJDT: =AJHandleProject/src<pkg*A.aj}A`declare \@method
		runTest("declare @method");
		compareHandles(IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD,
				"declare @method: public void pkg.C.method1() : @MyAnnotation", "<pkg*A12.aj'A`declare \\@method");
	}

	public void testDeclareAtConstructor() {
		// AJDT: =AJHandleProject/src<pkg*A.aj}A`declare \@constructor
		runTest("declare @constructor");
		compareHandles(IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR, "declare @constructor: pkg.C.new() : @MyAnnotation",
				"<pkg*A12.aj'A`declare \\@constructor");
	}

	// what about 2 pieces of before advice with the same
	// signature and the same pointcut
	public void testTwoPiecesOfAdviceWithSameSignatureAndPointcut() {
		runTest("two pieces of advice with the same signature and pointcut");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement parent = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.ASPECT, "A5");
		List children = parent.getChildren();
		String handle1 = null;
		String handle2 = null;
		for (Object child : children) {
			IProgramElement element = (IProgramElement) child;
			if (element.getKind().equals(IProgramElement.Kind.ADVICE)) {
				if (handle1 == null) {
					handle1 = element.getHandleIdentifier();
				} else {
					handle2 = element.getHandleIdentifier();
				}
			}
		}
		String expected1 = "<pkg*A5.aj'A5&before";
		String expected2 = "<pkg*A5.aj'A5&before!2";
		boolean b = expected1.equals(handle1);
		if (b) {
			assertEquals("handleIdentifier - expected " + expected2 + ", but found " + handle2, expected2, handle2);
		} else {
			assertEquals("handleIdentifier - expected " + expected1 + ", but found " + handle2, expected1, handle2);
			assertEquals("handleIdentifier - expected " + expected2 + ", but found " + handle1, expected2, handle1);
		}
	}

	public void testDeclareWarningHandle() {
		runTest("declare warning handle");
		compareHandles(IProgramElement.Kind.DECLARE_WARNING, "declare warning: \"Illegal call.\"",
				"<pkg*A11.aj'A11`declare warning");
	}

	public void testTwoDeclareWarningHandles() {
		runTest("two declare warning handles");
		compareHandles(IProgramElement.Kind.DECLARE_WARNING, "declare warning: \"blah\"", "<pkg*A11.aj'A11`declare warning!2");
	}

	// this is to ensure the logic for not including '1' in the count
	// works correctly. We don't want a decw ipe with count 1 but we do
	// want one with count 10.
	public void testTenDeclareWarningHandles() {
		runTest("ten declare warning handles");
		compareHandles(IProgramElement.Kind.DECLARE_WARNING, "declare warning: \"warning 1\"",
				"<*DeclareWarnings.aj'DeclareWarnings`declare warning");
		compareHandles(IProgramElement.Kind.DECLARE_WARNING, "declare warning: \"warning 10\"",
				"<*DeclareWarnings.aj'DeclareWarnings`declare warning!10");

	}

	// these two handles are the same unless we have added a counter
	// on the end
	public void testIPEsWithSameNameHaveUniqueHandles_methodCall() {
		runTest("ipes with same name have unique handles - method-call");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle1 = "<*TwoMethodCalls.aj[Main~main~\\[QString;?method-call("
				+ "void java.io.PrintStream.println(java.lang.String))";
		assertNotNull("expected to find node with handle " + handle1 + ", but did not", top.getElement(handle1));

		String handle2 = "<*TwoMethodCalls.aj[Main~main~\\[QString;?method-call("
				+ "void java.io.PrintStream.println(java.lang.String))!2";
		assertNotNull("expected to find node with handle " + handle2 + ", but did not", top.getElement(handle2));

		String handle3 = "<*TwoMethodCalls.aj[Main~main~\\[QString;?method-call("
				+ "void java.io.PrintStream.println(java.lang.String))!3";
		assertNull("expected not to find node with handle " + handle3 + ", but found one", top.getElement(handle3));
	}

	// these two handles should be different anyway so second one
	// shouldn't have the "2"
	public void testIPEsWithDiffNamesDontHaveCounter_methodCall() {
		runTest("ipes with different names do not have counter - method-call");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle1 = "<*TwoDiffMethodCalls.aj[Main~main~\\[QString;?method-call("
				+ "void java.io.PrintStream.println(java.lang.String))";
		assertNotNull("expected to find node with handle " + handle1 + ", but did not", top.getElement(handle1));

		String handle2 = "<*TwoDiffMethodCalls.aj[Main~method~\\[QString;?method-call("
				+ "void java.io.PrintStream.println(java.lang.String))";
		assertNotNull("expected to find node with handle " + handle2 + ", but did not", top.getElement(handle2));
	}

	public void testIPEsWithSameNameHaveUniqueHandles_handler() {
		runTest("ipes with same name have unique handles - handler");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle1 = "<*Handler.aj[C~method?exception-handler(void C." + "<catch>(java.io.FileNotFoundException))";
		assertNotNull("expected to find node with handle " + handle1 + ", but did not", top.getElement(handle1));

		String handle2 = "<*Handler.aj[C~method?exception-handler(void C." + "<catch>(java.io.FileNotFoundException))!2";
		assertNotNull("expected to find node with handle " + handle2 + ", but did not", top.getElement(handle2));
	}

	public void testIPEsWithSameNameHaveUniqueHandles_get() {
		runTest("ipes with same name have unique handles - get");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle1 = "<*Get.aj[C1~method1?field-get(int C1.x)";
		assertNotNull("expected to find node with handle " + handle1 + ", but did not", top.getElement(handle1));

		String handle2 = "<*Get.aj[C1~method1?field-get(int C1.x)!2";
		assertNotNull("expected to find node with handle " + handle2 + ", but did not", top.getElement(handle2));
	}

	public void testIPEsWithSameNameHaveUniqueHandles_set() {
		runTest("ipes with same name have unique handles - set");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle1 = "<*Set.aj[C1~method?field-set(int C1.x)";
		assertNotNull("expected to find node with handle " + handle1 + ", but did not", top.getElement(handle1));

		String handle2 = "<*Set.aj[C1~method?field-set(int C1.x)!2";
		assertNotNull("expected to find node with handle " + handle2 + ", but did not", top.getElement(handle2));
	}

	public void testTwoPiecesOfBeforeAdviceInInjarAspectHaveUniqueHandles_pr159896() {
		runTest("advice with same name in injar aspect should have unique handles");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle1 = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.ADVICE, "before(): p..").getHandleIdentifier();
		String handle2 = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.ADVICE, "before(): exec..")
				.getHandleIdentifier();
		assertFalse("expected the two advice nodes to have unique handles but" + " did not", handle1.equals(handle2));
		try {
			AsmManager.lastActiveStructureModel.dumptree(AsmManager.lastActiveStructureModel.getHierarchy().getRoot(), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testTwoDeclareWarningsInInjarAspectHaveUniqueHandles_pr159896() {
		runTest("declare warnings in injar aspect should have unique handles");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String handle1 = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_WARNING, "declare warning: \"blah\"")
				.getHandleIdentifier();
		String handle2 = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_WARNING, "declare warning: \"blah2\"")
				.getHandleIdentifier();
		assertFalse("expected the two declare warning nodes to have unique handles but" + " did not", handle1.equals(handle2));
	}

	// if have one declare warning and one declare error statement within an
	// injar
	// aspect, neither of them should have a counter (i.e. "!2") at the end of
	// their handle
	public void testOnlyIncrementSameDeclareTypeFromInjar_pr159896() {
		runTest("dont increment counter for different declares");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		String warning = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_WARNING,
				"declare warning: \"warning\"").getHandleIdentifier();
		assertTrue("shouldn't have incremented counter for declare warning handle " + "because only one declare warning statement",
				!warning.contains("!0") && !warning.contains("!2"));
		String error = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ERROR, "declare error: \"error\"")
				.getHandleIdentifier();
		assertTrue("shouldn't have incremented counter for declare error handle " + "because only one declare error statement",
				!error.contains("!0") && !error.contains("!2"));
	}

	// public void testOnlyIncrementSameAdviceKindFromInjar_pr159896() {
	// runTest("dont increment counter for different advice kinds");
	// IHierarchy top = AsmManager.getDefault().getHierarchy();
	// String before = top.findElementForLabel(top.getRoot(),
	// IProgramElement.Kind.ADVICE, "before(): p..")
	// .getHandleIdentifier();
	// assertTrue("shouldn't have incremented counter for before handle "
	// + "because only one before advice", before.indexOf("!0") == -1
	// && before.indexOf("!2") == -1 && before.indexOf("!3") == -1);
	// String after = top.findElementForLabel(top.getRoot(),
	// IProgramElement.Kind.ADVICE, "after(): p..")
	// .getHandleIdentifier();
	// assertTrue("shouldn't have incremented counter for after handle "
	// + "because only one after advice", after.indexOf("!0") == -1
	// && after.indexOf("!2") == -1 && after.indexOf("!3") == -1);
	// String around = top.findElementForLabel(top.getRoot(),
	// IProgramElement.Kind.ADVICE, "around(): p1..")
	// .getHandleIdentifier();
	// assertTrue("shouldn't have incremented counter for around handle "
	// + "because only one around advice", around.indexOf("!0") == -1
	// && around.indexOf("!2") == -1 && around.indexOf("!3") == -1);
	//
	// }

	// ---------- following tests ensure we produce the same handles as jdt
	// -----//
	// ---------- (apart from the prefix)

	// NOTES: there is no ipe equivalent to a package fragment root or
	//        

	public void testCompilationUnitSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java
		runTest("compilation unit same as jdt");
		compareHandles(IProgramElement.Kind.FILE_JAVA, "Demo.java", "<tjp{Demo.java");
	}

	public void testClassSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{C.java[C
		runTest("class same as jdt");
		compareHandles(IProgramElement.Kind.CLASS, "C", "<pkg{C.java[C");
	}

	public void testInterfaceSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{C.java[MyInterface
		runTest("interface same as jdt");
		compareHandles(IProgramElement.Kind.INTERFACE, "MyInterface", "<pkg{C.java[MyInterface");
	}

	public void testConstructorSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{C.java[C~C
		runTest("constructor same as jdt");
		compareHandles(IProgramElement.Kind.CONSTRUCTOR, "C()", "<pkg{C.java[C~C");
	}

	public void testConstructorWithArgsSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{C.java[C~C~QString;
		runTest("constructor with args same as jdt");
		compareHandles(IProgramElement.Kind.CONSTRUCTOR, "C(java.lang.String)", "<pkg{C.java[C~C~QString;");
	}

	// public void testPackageDeclarationSameAsJDT() {
	// // JDT: =TJP Example/src<tjp{Demo.java%tjp
	// fail("package declaration isn't the same");
	// runTest("package declaration same as jdt");
	// compareHandles(IProgramElement.Kind.PACKAGE,
	// "tjp",
	// "<tjp{Demo.java%tjp");
	// }

	public void testImportDeclarationSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java#java.io.*
		runTest("import declaration same as jdt");
		compareHandles(IProgramElement.Kind.IMPORT_REFERENCE, "java.io.*", "<tjp{Demo.java#java.io.*");
	}

	public void testTypeSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo
		runTest("type same as jdt");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement pe = top.findElementForType("tjp", "Demo");
		String expected = "<tjp{Demo.java[Demo";
		String found = pe.getHandleIdentifier();
		assertEquals("handleIdentifier - expected " + expected + ", but found " + found, expected, found);
	}

	public void testFieldSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo^d
		runTest("field same as jdt");
		compareHandles(IProgramElement.Kind.FIELD, "d", "<tjp{Demo.java[Demo^d");
	}

	public void testInitializationSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo|1
		// and =TJP Example/src<tjp{Demo.java[Demo|2
		runTest("initialization same as jdt");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement parent = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.CLASS, "Demo");
		List children = parent.getChildren();
		String handle1 = null;
		String handle2 = null;
		for (Object child : children) {
			IProgramElement element = (IProgramElement) child;
			if (element.getKind().equals(IProgramElement.Kind.INITIALIZER)) {
				if (handle1 == null) {
					handle1 = element.getHandleIdentifier();
				} else {
					handle2 = element.getHandleIdentifier();
				}
			}
		}
		String expected1 = "<tjp{Demo.java[Demo|1";
		String expected2 = "<tjp{Demo.java[Demo|2";
		boolean b = expected1.equals(handle1);
		System.err.println("actual: " + handle1);
		System.err.println("actual: " + handle2);
		if (b) {
			assertEquals("handleIdentifier - expected " + expected2 + ", but found " + handle2, expected2, handle2);
		} else {
			assertEquals("handleIdentifier - expected " + expected1 + ", but found " + handle2, expected1, handle2);
			assertEquals("handleIdentifier - expected " + expected2 + ", but found " + handle1, expected2, handle1);
		}
	}

	public void testMethodWithStringArrayArgsSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo~main~\[QString;
		runTest("method with string array as argument same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "main(java.lang.String[])", "<tjp{Demo.java[Demo~main~\\[QString;");
	}

	public void testMethodWithIntArrayArgsSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo~m~\[I
		runTest("method with int array as argument same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "m(int[])", "<tjp{Demo.java[Demo~m~\\[I");
	}

	public void testMethodWithNoArgsSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo~go
		runTest("method with no args same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "go()", "<tjp{Demo.java[Demo~go");
	}

	public void testMethodWithTwoArgsSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo~foo~I~QObject;
		runTest("method with two args same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "foo(int,java.lang.Object)", "<tjp{Demo.java[Demo~foo~I~QObject;");
	}

	public void testMethodWithTwoStringArgsSameAsJDT() {
		// JDT: =TJP Example/src<tjp{Demo.java[Demo~m2~QString;~QString;
		runTest("method with two string args same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "m2(java.lang.String,java.lang.String)",
				"<tjp{Demo.java[Demo~m2~QString;~QString;");
	}

	public void testEnumSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{E.java[E
		runTest("enum same as jdt");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement pe = top.findElementForType("pkg", "E");
		String expected = "<pkg{E.java[E";
		String found = pe.getHandleIdentifier();
		assertEquals("handleIdentifier - expected " + expected + ", but found " + found, expected, found);
	}

	public void testEnumValueSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{E.java[E^A
		runTest("enum value same as jdt");
		compareHandles(IProgramElement.Kind.ENUM_VALUE, "A", "<pkg{E.java[E^A");
	}

	public void testAnnotationSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{MyAnnotation.java[MyAnnotation
		runTest("annotation same as jdt");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement pe = top.findElementForType("pkg", "MyAnnotation");
		String expected = "<pkg{MyAnnotation.java[MyAnnotation";
		String found = pe.getHandleIdentifier();
		assertEquals("handleIdentifier - expected " + expected + ", but found " + found, expected, found);
	}

	public void testMethodWithListArgSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{Java5Class.java[Java5Class~method2~QList;
		runTest("method with list arg same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "method2(java.util.List)", "<pkg{Java5Class.java[Java5Class~method2~QList;");
	}

	public void testMethodWithGenericArgSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{Java5Class.java[Java5Class
		// ~genericMethod1~QList\<QString;>;
		runTest("method with generic arg same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "genericMethod1(java.util.List<java.lang.String>)",
				"<pkg{Java5Class.java[Java5Class~genericMethod1~QList\\<QString;>;");
	}

	public void testMethodWithTwoGenericArgsSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{Java5Class.java[Java5Class
		// ~genericMethod2~QList\<QString;>;~QMyGenericClass\<QInteger;>;
		runTest("method with two generic args same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "genericMethod2(java.util.List<java.lang.String>,"
				+ "pkg.MyGenericClass<java.lang.Integer>)", "<pkg{Java5Class.java[Java5Class~genericMethod2~QList"
				+ "\\<QString;>;~QMyGenericClass\\<QInteger;>;");
	}

	public void testMethodWithTwoTypeParametersSameAsJDT() {
		// JDT: =Java5 Handles/src<pkg{Java5Class.java[Java5Class~genericMethod4
		// ~QMyGenericClass2\<QString;QInteger;>;
		runTest("method with two type parameters same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "genericMethod4(pkg.MyGenericClass2<java.lang.String,java.lang.Integer>)",
				"<pkg{Java5Class.java[Java5Class~genericMethod4" + "~QMyGenericClass2\\<QString;QInteger;>;");
	}

	public void testMethodWithTwoArgsSameAsJDT_2() {
		// JDT: =Java5 Handles/src<pkg{Java5Class.java[Java5Class
		// ~genericMethod3~I~QList\<QString;>;
		runTest("method with two args one of which is generic same as jdt");
		compareHandles(IProgramElement.Kind.METHOD, "genericMethod3(int,java.util.List<java.lang.String>)",
				"<pkg{Java5Class.java[Java5Class~genericMethod3~I~QList\\<QString;>;");
	}

	/*
	 * Still to do; PROJECT, PACKAGE, FILE, FILE_ASPECTJ, FILE_LST, DECLARE_ERROR, DECLARE_SOFT, DECLARE_PRECEDENCE,
	 */

	// ----------- helper methods ---------------
	private void compareHandles(IProgramElement.Kind kind, String ipeName, String expectedHandle) {
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement pe = top.findElementForLabel(top.getRoot(), kind, ipeName);
		String found = pe.getHandleIdentifier();
		assertEquals("handleIdentifier - expected " + expectedHandle + ", but found " + found, expectedHandle, found);
	}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(JDTLikeHandleProviderTests.class);
	}

	protected URL getSpecFile() {
		return getClassResource("jdtlikehandleprovider.xml");
	}

}
