/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc154;

import java.lang.reflect.Field;

import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LineNumber;
import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;

import junit.framework.Test;

/**
 * These are tests for AspectJ1.5.4
 */
public class Ajc154Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// public void testNewDesignatorsReferencePointcuts_pr205907() {
	// BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
	// Set set = new HashSet();
	// set.add(beanHandler);
	// PatternParser.setTestDesignators(set);
	// //parser.registerPointcutDesignatorHandler(beanHandler);
	// runTest("new pointcut designators in a reference pointcut");
	// }
	// public void testAfterThrowingAnnotationStyle_pr211674_1() { runTest("after throwing annotation style problem - 1");}
	// public void testAfterThrowingAnnotationStyle_pr211674_2() { runTest("after throwing annotation style problem - 2");}

	// crappy solution - see the bug
	// public void testCflowLtwProblem_pr166647_1() {
	// try {
	// runTest("ltw and cflow problem");
	// } catch (AssertionFailedError afe) {
	// // this is OK.... sadly
	// // at least lets check we warned the user it was going to happen:
	// String stderr = (getLastRunResult() == null ? "" : getLastRunResult().getStdErr());
	// // Expected line:
	// // [WeavingURLClassLoader] warning XML Defined aspects must be woven in cases where cflow pointcuts are involved.
	// // Currently the include/exclude patterns exclude 'x.Aspect2' [Xlint:mustWeaveXmlDefinedAspects]
	// assertTrue("Did not see warning about needing to weave xml defined aspects", stderr
	// .indexOf("warning XML Defined aspects must be woven in cases where cflow pointcuts are involved.") != -1);
	// assertTrue("Xlint warning was expected '[Xlint:mustWeaveXmlDefinedAspects]'", stderr
	// .indexOf("[Xlint:mustWeaveXmlDefinedAspects]") != -1);
	// }
	// }

	// Testing some issues with declare at type
	public void testDeclareAtTypeProblems_pr211052_1() {
		runTest("declare atType problems - 1");
	}

	public void testDeclareAtTypeProblems_pr211052_2() {
		runTest("declare atType problems - 2");
	}

	public void testDeclareAtTypeProblems_pr211052_3() {
		runTest("declare atType problems - 3");
	}

	public void testDeclareAtTypeProblems_pr211052_4() {
		runTest("declare atType problems - 4");
	}

	public void testDeclareAtTypeProblems_pr211052_5() {
		runTest("declare atType problems - 5");
	}

	// declare at type and binary weaving
	public void testDeclareAtTypeProblems_pr211052_6() {
		runTest("declare atType problems - 6");
	}

	public void testDeclareAtTypeProblems_pr211052_7() {
		runTest("declare atType problems - 7");
	}

	public void testNPEWithMissingAtAspectAnnotationInPointcutLibrary_pr162539_1() {
		runTest("NPE with missing @aspect annotation in pointcut library - 1");
	}

	public void testNPEWithMissingAtAspectAnnotationInPointcutLibrary_pr162539_2() {
		runTest("NPE with missing @aspect annotation in pointcut library - 2");
	}

	public void testWrongNumberOfTypeParameters_pr176991() {
		runTest("wrong number of type parameters");
	}

	public void testArgNamesDoesNotWork_pr148381_1() {
		runTest("argNames does not work - simple");
	}

	public void testArgNamesDoesNotWork_pr148381_2() {
		runTest("argNames does not work - error1");
	}

	public void testArgNamesDoesNotWork_pr148381_3() {
		runTest("argNames does not work - error2");
	}

	public void testArgNamesDoesNotWork_pr148381_4() {
		runTest("argNames does not work - error3");
	}

	public void testDecpProblemWhenTargetAlreadyImplements_pr169432_1() {
		runTest("declare parents problem when target already implements interface - 1");
	}

	public void testDecpProblemWhenTargetAlreadyImplements_pr169432_2() {
		runTest("declare parents problem when target already implements interface - 2");
	}

	public void testDecpProblemWhenTargetAlreadyImplements_pr169432_3() {
		runTest("declare parents problem when target already implements interface - 3");
	}

	public void testVariousLtwAroundProblems_pr209019_1() {
		runTest("various issues with ltw and around advice - 1");
	}

	public void testVariousLtwAroundProblems_pr209019_2() {
		runTest("various issues with ltw and around advice - 2");
	}

	public void testVariousLtwAroundProblems_pr209019_3() {
		runTest("various issues with ltw and around advice - 3");
	}

	public void testVariousLtwAroundProblems_pr209019_4() {
		runTest("various issues with ltw and around advice - 4");
	}

	public void testAbstractAnnotationStylePointcutWithContext_pr202088() {
		runTest("abstract annotation style pointcut with context");
	}

	public void testNoErrorForAtDecpInNormalClass_pr169428() {
		runTest("no error for atDecp in normal class");
	}

	public void testJarsZipsNonStandardSuffix_pr186673() {
		runTest("jars and zips with non-standard suffix");
	}

	public void testItdOnGenericInnerInterface_pr203646() {
		runTest("npe with itd on inner generic interface");
	}

	public void testItdOnGenericInnerInterface_pr203646_A() {
		runTest("npe with itd on inner generic interface - exampleA");
	}

	public void testItdOnGenericInnerInterface_pr203646_B() {
		runTest("npe with itd on inner generic interface - exampleB");
	}

	public void testItdOnGenericInnerInterface_pr203646_C() {
		runTest("npe with itd on inner generic interface - exampleC");
	}

	public void testItdOnGenericInnerInterface_pr203646_D() {
		runTest("npe with itd on inner generic interface - exampleD");
	}

	// public void testItdOnGenericInnerInterface_pr203646_E() { runTest("npe with itd on inner generic interface - exampleE");} //
	// needs parser change
	public void testItdOnGenericInnerInterface_pr203646_F() {
		runTest("npe with itd on inner generic interface - exampleF");
	}

	public void testItdOnGenericInnerInterface_pr203646_G() {
		runTest("npe with itd on inner generic interface - exampleG");
	}

	public void testItdClashForTypesFromAspectPath_pr206732() {
		runTest("itd clash for types from aspectpath");
	}

	// public void testAnnotationStyleAndMultiplePackages_pr197719() {
	// runTest("annotation style syntax and cross package extension"); }

	/**
	 * Complex test that attempts to damage a class like a badly behaved bytecode transformer would and checks if AspectJ can cope.
	 * 
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void testCopingWithGarbage_pr175806_1() throws ClassNotFoundException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {

		// Compile the program we are going to mess with
		runTest("coping with bad tables");

		// Load up the class and the method 'main' we are interested in
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "A");
		Method[] meths = jc.getMethods();
		Method oneWeWant = null;
		for (int i = 0; i < meths.length && oneWeWant == null; i++) {
			Method method = meths[i];
			if (method.getName().equals("main")) {
				oneWeWant = meths[i];
			}
		}

		/**
		 * For the main method: Stack=2, Locals=3, Args_size=1 0: iconst_5 1: istore_1 2: ldc #18; //String 3 4: astore_2 5:
		 * getstatic #24; //Field java/lang/System.out:Ljava/io/PrintStream; 8: aload_2 9: invokevirtual #30; //Method
		 * java/io/PrintStream.println:(Ljava/lang/String;)V 12: goto 23 15: pop 16: getstatic #24; //Field
		 * java/lang/System.out:Ljava/io/PrintStream; 19: iload_1 20: invokevirtual #33; //Method java/io/PrintStream.println:(I)V
		 * 23: return Exception table: from to target type 2 15 15 Class java/lang/Exception
		 * 
		 * LineNumberTable: line 4: 0 line 6: 2 line 7: 5 line 8: 15 line 9: 16 line 11: 23 LocalVariableTable: Start Length Slot
		 * Name Signature 0 24 0 argv [Ljava/lang/String; 2 22 1 i I 5 10 2 s Ljava/lang/String;
		 */

		ConstantPool cp = oneWeWant.getConstantPool();
		// ConstantPool cpg = new ConstantPool(cp);

		// Damage the line number table, entry 2 (Line7:5) so it points to an invalid (not on an instruction boundary) position of 6
		Field ff = LineNumber.class.getDeclaredField("startPC");
		ff.setAccessible(true);
		ff.set(oneWeWant.getLineNumberTable().getLineNumberTable()[2], 6);
		// oneWeWant.getLineNumberTable().getLineNumberTable()[2].setStartPC(6);

		// Should be 'rounded down' when transforming it into a MethodGen, new position will be '5'
		// System.out.println("BEFORE\n"+oneWeWant.getLineNumberTable().toString());
		MethodGen toTransform = new MethodGen(oneWeWant, "A", cp, false);
		LineNumberTable lnt = toTransform.getMethod().getLineNumberTable();
		assertTrue("Should have been 'rounded down' to position 5 but is " + lnt.getLineNumberTable()[2].getStartPC(), lnt
				.getLineNumberTable()[2].getStartPC() == 5);
		// System.out.println("AFTER\n"+lnt.toString());
	}

	public void testCopingWithGarbage_pr175806_2() throws ClassNotFoundException {

		// Compile the program we are going to mess with
		runTest("coping with bad tables");

		// Load up the class and the method 'main' we are interested in
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "A");
		Method[] meths = jc.getMethods();
		Method oneWeWant = null;
		for (int i = 0; i < meths.length && oneWeWant == null; i++) {
			Method method = meths[i];
			if (method.getName().equals("main")) {
				oneWeWant = meths[i];
			}
		}
		// see previous test for dump of main method

		ConstantPool cp = oneWeWant.getConstantPool();
		// ConstantPoolGen cpg = new ConstantPoolGen(cp);

		// Damage the local variable table, entry 2 (" 2      22      1    i       I") so it points to an invalid start pc of 3
		oneWeWant.getLocalVariableTable().getLocalVariable(1).setStartPC(3);

		// Should be 'rounded down' when transforming it into a MethodGen, new position will be '2'
		// This next line will go BANG with an NPE if we don't correctly round the start pc down to 2
		new MethodGen(oneWeWant, "A", cp, true);
	}

	public void testGenericAspectGenericPointcut_pr174449() {
		runTest("problem with generic aspect and generic pointcut");
	}

	public void testGenericAspectGenericPointcut_noinline_pr174449() {
		runTest("problem with generic aspect and generic pointcut - noinline");
	}

	public void testGenericMethodsAndOrdering_ok_pr171953_2() {
		runTest("problem with generic methods and ordering - ok");
	}

	public void testGenericMethodsAndOrdering_bad_pr171953_2() {
		runTest("problem with generic methods and ordering - bad");
	}

	public void testItdAndJoinpointSignatureCollection_ok_pr171953() {
		runTest("problem with itd and join point signature collection - ok");
	}

	public void testItdAndJoinpointSignatureCollection_bad_pr171953() {
		runTest("problem with itd and join point signature collection - bad");
	}

	public void testGenericMethodsAndItds_pr171952() {
		runTest("generic methods and ITDs");
	}

	// public void testUsingDecpAnnotationWithoutAspectAnnotation_pr169428() {
	// runTest("using decp annotation without aspect annotation");}
	public void testItdsParameterizedParameters_pr170467() {
		runTest("itds and parameterized parameters");
	}

	public void testComplexGenerics_pr168044() {
		runTest("complex generics - 1");
	}

	public void testIncorrectlyMarkingFieldTransient_pr168063() {
		runTest("incorrectly marking field transient");
	}

	public void testInheritedAnnotations_pr169706() {
		runTest("inherited annotations");
	}

	public void testGenericFieldNPE_pr165885() {
		runTest("generic field npe");
	}

	public void testIncorrectOptimizationOfIstore_pr166084() {
		runTest("incorrect optimization of istore");
	}

	public void testDualParameterizationsNotAllowed_pr165631() {
		runTest("dual parameterizations not allowed");
	}

	public void testSuppressWarnings1_pr166238() {
		runTest("Suppress warnings1");
	}

	public void testSuppressWarnings2_pr166238() {
		runTest("Suppress warnings2");
	}

	public void testNullReturnedFromGetField_pr172107() {
		runTest("null returned from getField()");
	}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc154Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc154.xml");
	}

	// ---
	private class BeanDesignatorHandler implements PointcutDesignatorHandler {

		private String askedToParse;
		public boolean simulateDynamicTest = false;

		public String getDesignatorName() {
			return "bean";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.aspectj.weaver.tools.PointcutDesignatorHandler#parse(java.lang.String)
		 */
		public ContextBasedMatcher parse(String expression) {
			this.askedToParse = expression;
			return new BeanPointcutExpression(expression, this.simulateDynamicTest);
		}

		public String getExpressionLastAskedToParse() {
			return this.askedToParse;
		}
	}

	private class BeanPointcutExpression implements ContextBasedMatcher {

		private final String beanNamePattern;
		private final boolean simulateDynamicTest;

		public BeanPointcutExpression(String beanNamePattern, boolean simulateDynamicTest) {
			this.beanNamePattern = beanNamePattern;
			this.simulateDynamicTest = simulateDynamicTest;
		}

		public boolean couldMatchJoinPointsInType(Class aClass) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.aspectj.weaver.tools.ContextBasedMatcher#couldMatchJoinPointsInType(java.lang.Class)
		 */
		public boolean couldMatchJoinPointsInType(Class aClass, MatchingContext context) {
			if (this.beanNamePattern.equals(context.getBinding("beanName"))) {
				return true;
			} else {
				return false;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.aspectj.weaver.tools.ContextBasedMatcher#mayNeedDynamicTest()
		 */
		public boolean mayNeedDynamicTest() {
			return this.simulateDynamicTest;
		}

		public FuzzyBoolean matchesStatically(MatchingContext matchContext) {
			if (this.simulateDynamicTest) {
				return FuzzyBoolean.MAYBE;
			}
			if (this.beanNamePattern.equals(matchContext.getBinding("beanName"))) {
				return FuzzyBoolean.YES;
			} else {
				return FuzzyBoolean.NO;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.aspectj.weaver.tools.ContextBasedMatcher#matchesDynamically(org.aspectj.weaver.tools.MatchingContext)
		 */
		public boolean matchesDynamically(MatchingContext matchContext) {
			return this.beanNamePattern.equals(matchContext.getBinding("beanName"));
		}
	}

}