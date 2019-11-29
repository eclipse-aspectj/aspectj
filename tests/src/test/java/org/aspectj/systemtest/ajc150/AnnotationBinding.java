/*******************************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class AnnotationBinding extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(AnnotationBinding.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc150.xml");
	}

	// /////////////////////////////////// @ANNOTATION and CALL

	// Very simple annotation binding for 'call() && @annotation()'
	public void testCallAnnotationBinding1() {
		runTest("call annotation binding 1");
	}

	// 'call() && @annotation()' when the called method has multiple arguments
	public void testCallAnnotationBinding2() {
		runTest("call annotation binding 2");
	}

	// 'call() && @annotation()' when the called method takes primitive arguments (YUCK!)
	public void testCallAnnotationBinding3() {
		runTest("call annotation binding 3");
	}

	// 'call() && @annotation()' when runtime type will exhibit different annotation (due to interface implementing)
	public void testCallAnnotationBinding4() {
		runTest("call annotation binding 4");
	}

	// 'call() && @annotation()' when target doesnt have an annotation !
	public void testCallAnnotationBinding5() {
		runTest("call annotation binding 5");
	}

	// 'call() && @annotation()' when runtime type will exhibit different annotation (due to subclassing)
	public void testCallAnnotationBinding6() {
		runTest("call annotation binding 6");
	}

	// 'call() && @annotation()' using named pointcut
	public void testCallAnnotationBinding7() {
		runTest("call annotation binding 7");
	}

	// /////////////////////////////////// @TARGET

	// 'call() && @target()'
	public void testAtTargetAnnotationBinding1() {
		runTest("@target annotation binding 1");
	}

	// 'call() && @target() && @target'
	public void testAtTargetAnnotationBinding2() {
		runTest("@target annotation binding 2");
	}

	// 'call() && @target()' - using a type hierarchy where some levels are missing annotations
	public void testAtTargetAnnotationBinding3() {
		runTest("@target annotation binding 3");
	}

	// 'call() && @target()' - using a type hierarchy where some levels are missing annotations
	// but the annotation is inherited
	public void testAtTargetAnnotationBinding4() {
		runTest("@target annotation binding 4");
	}

	// @target() with an annotation in a package
	public void testAtTargetAnnotationBinding5() {
		runTest("@target annotation binding 5");
	}

	// /////////////////////////////////// @THIS

	// 'call() && @this()'
	public void testAtThisAnnotationBinding1() {
		runTest("@this annotation binding 1");
	}

	// 'call() && @this() && @this'
	public void testAtThisAnnotationBinding2() {
		runTest("@this annotation binding 2");
	}

	// 'call() && @this()' - using a type hierarchy where some levels are missing annotations
	public void testAtThisAnnotationBinding3() {
		runTest("@this annotation binding 3");
	}

	// 'call() && @this()' - using a type hierarchy where some levels are missing annotations
	// but the annotation is inherited
	public void testAtThisAnnotationBinding4() {
		runTest("@this annotation binding 4");
	}

	// '@this() and @target()' used together
	public void testAtThisAtTargetAnnotationBinding() {
		runTest("@this annotation binding 5");
	}

	// /////////////////////////////////// @ARGS

	// complex case when there are 3 parameters
	public void testAtArgs1() {
		runTest("@args annotation binding 1");
	}

	// simple case when there is only one parameter
	public void testAtArgs2() {
		runTest("@args annotation binding 2");
	}

	// simple case when there is only one parameter and no binding
	public void testAtArgs3() {
		runTest("@args annotation binding 3");
	}

	// complex case binding different annotation kinds
	public void testAtArgs4() {
		runTest("@args annotation binding 4");
	}

	// check @args and execution()
	public void testAtArgs5() {
		runTest("@args annotation binding 5");
	}

	// /////////////////////////////////// @ANNOTATION and EXECUTION

	// 'execution() && @annotation()'
	public void testExecutionAnnotationBinding1() {
		runTest("execution and @annotation");
	}

	// /////////////////////////////////// @ANNOTATION and SET

	// 'set() && @annotation()'
	public void testFieldAnnotationBinding1() {
		runTest("set and @annotation");
	}

	// 'get() && @annotation()'
	public void testFieldAnnotationBinding2() {
		runTest("get and @annotation");
	}

	// 'get() && @annotation()' when using array fields
	public void testFieldAnnotationBinding3() {
		runTest("get and @annotation with arrays");
	}

	// /////////////////////////////////// @ANNOTATION and CTOR-CALL

	// 'ctor-call(new) && @annotation()'
	public void testCtorCallAnnotationBinding1() {
		runTest("cons call and @annotation");
	}

	// /////////////////////////////////// @ANNOTATION and CTOR-CALL

	// 'ctor-execution() && @annotation()'
	public void testCtorExecAnnotationBinding1() {
		runTest("cons exe and @annotation");
	}

	// /////////////////////////////////// @ANNOTATION and STATICINITIALIZATION

	// 'staticinitialization() && @annotation()'
	public void testStaticInitAnnotationBinding1() {
		runTest("staticinit and @annotation");
	}

	// /////////////////////////////////// @ANNOTATION and PREINITIALIZATION

	// 'preinitialization() && @annotation()'
	public void testPreInitAnnotationBinding1() {
		runTest("preinit and @annotation");
	}

	// /////////////////////////////////// @ANNOTATION and INITIALIZATION

	// 'initialization() && @annotation()'
	public void testInitAnnotationBinding1() {
		runTest("init and @annotation");
	}

	// /////////////////////////////////// @ANNOTATION and ADVICEEXECUTION

	// 'adviceexecution() && @annotation()'
	public void testAdviceExecAnnotationBinding1() {
		runTest("adviceexecution and @annotation");
	}

	// /////////////////////////////////// @ANNOTATION and HANDLER

	// 'handler() && @annotation()'
	public void testHandlerAnnotationBinding1() {
		runTest("handler and @annotation");
	}

	// /////////////////////////////////// @WITHIN

	// '@within()'
	public void testWithinBinding1() {
		runTest("@within");
	}

	// '@within()' but multiple types around (some annotated)
	public void testWithinBinding2() {
		runTest("@within - multiple types");
	}

	// /////////////////////////////////// @WITHINCODE

	// '@withincode() && call(* println(..))'
	public void testWithinCodeBinding1() {
		runTest("@withincode() and call(* println(..))");
	}

	// /////////////////////////////////// @ANNOTATION complex tests

	// Using package names for the types (including the annotation) - NO BINDING
	public void testPackageNamedTypesNoBinding() {
		runTest("packages and no binding");
	}

	// Using package names for the types (including the annotation) - INCLUDES BINDING
	public void testPackageNamedTypesWithBinding() {
		runTest("packages and binding");
	}

	// declare parents: @Color * implements Serializable
	public void testDeclareParentsWithAnnotatedAnyPattern() {
		runTest("annotated any pattern");
	}

	// Should error (in a nice way!) on usage of an annotation that isnt imported
	public void testAnnotationUsedButNotImported() {
		runTest("annotation not imported");
	}

	// Binding with calls/executions of static methods
	public void testCallsAndExecutionsOfStaticMethods() {
		runTest("binding with static methods");
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// annotation binding with ITDs

	public void testAnnotationBindingAndITDs1() {
		runTest("simple binding annotation values where itd method is annotated");
	}

	public void testAnnotationBindingAndITDs2() {
		runTest("simple binding annotation values where itd field is annotated");
	}

	public void testAnnotationBindingAndITDs3() {
		runTest("simple binding annotation values where itd ctor is annotated");
	}

	public void testAnnotationBindingAndITDs4() {
		runTest("simple binding annotation values where itd method is annotated via declare");
	}

	public void testAnnotationBindingAndITDs5() {
		runTest("simple binding annotation values where itd field is annotated via declare");
	}

	public void testAnnotationBindingAndITDs6() {
		runTest("simple binding annotation values where itd field is annotated multiple times via declare");
	}

	public void testAnnotationBindingAndITDs7() {
		runTest("simple binding annotation values where itd ctor is annotated via declare");
	}

	public void testAnnotationBindingAndITDs4_asmtest() {
		// AsmManager.setReporting("c:/debug.txt",true,true,true,true);
		runTest("simple binding annotation values where itd method is annotated via declare");

		if (getCurrentTest().canRunOnThisVM()) {
			AsmManager asm = AsmManager.lastActiveStructureModel;
			IHierarchy top = asm.getHierarchy();

			IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD,
					"declare @method: int A.m() : @Fruit(\"orange\")");
			assertTrue("Couldn't find 'declare @method' element in the tree", ipe != null);

			List<IRelationship> l = asm.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD,
					"declare @method: int A.n() : @Fruit(\"banana\")");
			assertTrue("Couldn't find 'declare @method element in the tree", ipe != null);

			l = asm.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			Relationship rel = (Relationship) l.get(0);
			assertTrue("Should have 1 target but has " + rel.getTargets().size(), rel.getTargets().size() == 1);
			String tgt = rel.getTargets().get(0);
			int lineNumber = asm.getHandleProvider().getLineNumberForHandle(tgt);
			assertTrue("Should point to line 10 but doesnt: " + lineNumber, lineNumber == 10);
		}
	}

	public void testAnnotationBindingAndITDs5_asmtest() {
		// AsmManager.setReporting("c:/debug.txt",true,true,true,true);
		runTest("simple binding annotation values where itd field is annotated via declare");

		if (getCurrentTest().canRunOnThisVM()) {
			AsmManager asm = AsmManager.lastActiveStructureModel;
			IHierarchy top = asm.getHierarchy();

			IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_FIELD,
					"declare @field: int A.i : @Fruit(\"orange\")");
			assertTrue("Couldn't find 'declare @type' element in the tree", ipe != null);

			List<IRelationship> l = asm.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_FIELD,
					"declare @field: java.lang.String A.j : @Fruit(\"banana\")");
			assertTrue("Couldn't find 'declare @field element in the tree", ipe != null);

			l = asm.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			Relationship rel = (Relationship) l.get(0);
			assertTrue("Should have 1 target but has " + rel.getTargets().size(), rel.getTargets().size() == 1);
			String tgt = rel.getTargets().get(0);
			int lineNumber = asm.getHandleProvider().getLineNumberForHandle(tgt);
			assertTrue("Should point to line 10 but doesnt: " + lineNumber, lineNumber == 10);

		}
	}

	public void testAnnotationBindingAndITDs7_asmtest() {
		// AsmManager.setReporting("c:/debug.txt",true,true,true,true);
		runTest("simple binding annotation values where itd ctor is annotated via declare");

		if (getCurrentTest().canRunOnThisVM()) {

			AsmManager asm = AsmManager.lastActiveStructureModel;
			IHierarchy top = asm.getHierarchy();

			IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR,
					"declare @constructor: A.new(java.lang.String) : @Fruit(\"pear\")");
			assertTrue("Couldn't find 'declare @constructor' element in the tree", ipe != null);

			List<IRelationship> l = asm.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR,
					"declare @constructor: A.new(int) : @Fruit(\"orange\")");
			assertTrue("Couldn't find 'declare @constructor element in the tree", ipe != null);

			l = asm.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			Relationship rel = (Relationship) l.get(0);
			assertTrue("Should have 1 target but has " + rel.getTargets().size(), rel.getTargets().size() == 1);
			String tgt = rel.getTargets().get(0);
			int lineNumber = asm.getHandleProvider().getLineNumberForHandle(tgt);
			assertTrue("Should point to line 10 but doesnt: " + lineNumber, lineNumber == 10);

		}
	}

	public void testAnnotationBindingArgsVerifyError_pr92053() {
		runTest("AtArgs causes a VerifyError: Unable to pop operand off an empty stack");
	}

}