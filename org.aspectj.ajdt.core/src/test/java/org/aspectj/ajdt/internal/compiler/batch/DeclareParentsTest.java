/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

/**
 * These tests verify the behavior of the binary implementation of declare parents. Basically we attempt a source compile with all
 * the classes/aspects - we get some set of errors/warnings and weaving messages out from doing this. We then compile the source
 * files and aspects separately and binary weave them together - we should get the same set of weaving information messages. Where
 * possible we also execute one of the classes after the binary weave to check it passes the verifier and executes.
 * 
 * There are some notes about the implementation throughout these testcases and they are marked: 'IMPORTANT:'
 * 
 * 
 * Two things missing:
 * 
 * In the case where inherited methods can't be overridden to reduce visibility, we should cope with a subclass of the decp target
 * also trying to do it ? We need a way once we see a type to say 'hey, watch out for this guys kids...' - this will also help us
 * when working with abstract classes that don't provide method implementations where their children might.
 * 
 * Field inheritance? Is there something to worry about here?
 * 
 * Covariance on method overrides is supported but untested because we need a Java5 compiler (so we can write easy tests)
 */
public class DeclareParentsTest extends AjcTestCase {

	private static final boolean verbose = false;

	public static final String PROJECT_DIR = "binaryParents";

	private File baseDir;

	/**
	 * Check the order doesn't make a difference. (order1)
	 */
	public void testVerifyOrderOfProcessingIrrelevant1() {
		File testBase = new File(baseDir, "TestA");
		runSourceAndBinaryTestcase(testBase, new String[] { "Z.java", "B.java" }, new String[] { "AspectAB.aj" }, false);
		// runClass("B");
	}

	/**
	 * Check the order doesn't make a difference. (order2)
	 */
	public void testVerifyOrderOfProcessingIrrelevant2() {
		File testBase = new File(baseDir, "TestA");
		runSourceAndBinaryTestcase(testBase, new String[] { "B.java", "Z.java" }, new String[] { "AspectAB.aj" }, false);
		// runClass("B");
	}

	/**
	 * Three classes: Top1, Middle1, Bottom1. Bottom1 extends Top1. Middle1 extends Top1. AspectX1: declares Bottom1 extends Middle1
	 * Result: Should be OK, fits into the hierarchy no problem.
	 */
	public void testSimpleDeclareParents() {
		File testBase = new File(baseDir, "TestA");
		runSourceAndBinaryTestcase(testBase, new String[] { "Top1.java", "Middle1.java", "Bottom1.java" },
				new String[] { "AspectX1.java" }, false);
		// runClass("Bottom1");
	}

	/**
	 * Three classes: Top2, Middle2, Bottom2. Bottom2 extends Top2. Middle2 extends Top2. Bottom2 includes a call to super in a
	 * ctor. AspectX2: declares Bottom2 extends Middle2 Result: Should be OK, fits into the hierarchy no problem. Implementation:
	 * The super call should be modified from a Top2.<init> call to a Middle2.<init> call
	 */
	public void test_SuperCtorCall() {
		File testBase = new File(baseDir, "TestA");
		runSourceAndBinaryTestcase(testBase, new String[] { "Top2.java", "Middle2.java", "Bottom2.java" },
				new String[] { "AspectX2.java" }, false);
		// runClass("Bottom2");
	}

	/**
	 * Three classes: Top3, Middle3, Bottom3. Bottom3 extends Top3. Middle3 extends Top3. Bottom3 includes a call to a super method
	 * in an instance method. AspectX3: declares Bottom3 extends Middle3 Result: Should be OK. Implementation: We don't modify the
	 * call to Top3.m() that is in the Bottom3 class, we don't have to because the JVM will ensure that the m() chosen at runtime is
	 * the one nearest the Bottom3 class - when the hierarchy has changed this will be the Middle3.m() version and so it all works.
	 * IMPORTANT: This leaves a subtle difference in the code generated from decp application at source time and decp application at
	 * weave time - in the source time case the call in Bottom3 will have been set to Middle3.m() during code gen, whereas in the
	 * weave time case it will still say Top3.m() - I'm not sure this makes any practical difference though? We could easily fix it
	 * and morph the Top3.m() call to a Middle3.m() call but it would impact peformance crawling through all the bytecodes to make
	 * this change.
	 */
	public void test_SuperMethodCall() {
		File testBase = new File(baseDir, "TestA");
		runSourceAndBinaryTestcase(testBase, new String[] { "Top3.java", "Middle3.java", "Bottom3.java" },
				new String[] { "AspectX3.java" }, false);
		// runClass("Bottom3");
	}

	/**
	 * Three classes: Top4, Middle4, Bottom4. Bottom4 extends Top4. Middle4 extends Top4. AspectX4: declares Bottom4 extends Middle4
	 * Result: Should fail - because Middle4 doesn't include a ctor that takes a String, which is called by Bottom4
	 */
	public void test_missingCtorInIntroducedClass() {
		File testBase = new File(baseDir, "TestA");
		runSourceAndBinaryTestcase(testBase, new String[] { "Top4.java", "Middle4.java", "Bottom4.java" },
				new String[] { "AspectX4.java" }, true, false);
	}

	/**
	 * If overriding an instance method, can't make it static. If overriding a static method, can't make it an instance method.
	 * 
	 * Note: Error messages and locations for binary weaving are much better than their source counterparts !
	 */
	public void test_cantMakeInheritedInstanceMethodsStatic() {
		runSourceAndBinaryTestcase(new File(baseDir, "TestC"), new String[] { "A1.java", "B1.java" }, new String[] { "X1.java" },
				true, false);
	}

	/**
	 * Cannot extend a final class
	 */
	public void xxxtest_cantExtendFinalClass() { // XXX removed test, need to discuss with andy how to repair...
		runSourceAndBinaryTestcase(new File(baseDir, "TestC"), new String[] { "A2.java", "B2.java" }, new String[] { "X2.java" },
				true, true);
	}

	/**
	 * The Object class cannot be subject to declare parents extends
	 * 
	 * This is tested when the aspect is compiled - so couldn't occur during binary weaving of decp.
	 */

	/**
	 * if you inherit methods you cannot override them and reduce their visibility
	 */
	public void test_cantReduceVisibilityOfOverriddenMethods_1() {
		runSourceAndBinaryTestcase(new File(baseDir, "TestB"), new String[] { "Top1.java", "Middle1.java" },
				new String[] { "Aspect1.java" }, true, false);
	}

	/**
	 * if you inherit methods you cannot override them and reduce their visibility.
	 * 
	 * test 2 in this set checks methods from a superclass of the named new parent.
	 */
	public void test_cantReduceVisibilityOfOverriddenMethods_2() {
		runSourceAndBinaryTestcase(new File(baseDir, "TestB"), new String[] { "TopTop6.java", "Top6.java", "Middle6.java" },
				new String[] { "Aspect6.java" }, true, false);
	}

	/**
	 * If you inherit methods you cannot have incompatible return types (java1.5 will make this a little messier).
	 */
	public void test_overriddenMethodsCantHaveIncompatibleReturnTypes() {
		runSourceAndBinaryTestcase(new File(baseDir, "TestB"),
				new String[] { "Top2.java", "Middle2.java", "Super.java", "Sub.java" }, new String[] { "Aspect2.java" }, true);
	}

	/**
	 * Testing: If you inherit abstract methods and you are not abstract you need to provide an implementation.
	 * 
	 * Test 1 in this set is simple.
	 */
	public void test_inheritedAbstractMethodsMustBeImplemented_1() {
		runSourceAndBinaryTestcase(new File(baseDir, "TestB"),
				new String[] { "Top3.java", "Middle3.java", "Super.java", "Sub.java" }, new String[] { "Aspect3.java" }, true);
	}

	/**
	 * Testing: If the decp makes you implement an interface, you must provide the implementation
	 */
	public void test_interfaceMethodsImplemented() {
		File testBase = new File(baseDir, "TestD");
		runSourceAndBinaryTestcase(testBase, new String[] { "SimpleClass1.java", "SimpleIntf1.java" },
				new String[] { "SimpleAspect1.java" }, true);
	}

	/**
	 * Testing: If you inherit abstract methods and you are not abstract you need to provide an implementation.
	 * 
	 * Test 2 in this set includes methods further up the hierarchy that must be implemented.
	 */
	public void test_inheritedAbstractMethodsMustBeImplemented_2() {
		runSourceAndBinaryTestcase(new File(baseDir, "TestB"), new String[] { "TopTop4.java", "Top4.java", "Middle4.java" },
				new String[] { "Aspect4.java" }, true);
	}

	/**
	 * Testing: If you inherit abstract methods and you are not abstract you need to provide an implementation.
	 * 
	 * Test 3 in this set includes methods further up the hierarchy that must be implemented *and* the dependencies are satisfied by
	 * ITDs from the aspect
	 */
	public void test_inheritedAbstractMethodsMustBeImplemented_3() {
		runSourceAndBinaryTestcase(new File(baseDir, "TestD"), new String[] { "SimpleClass2.java" },
				new String[] { "SimpleAspect2.java" }, true);
	}

	/**
	 * If adding a type into a hierarchy, any missing ctor could be added via an ITDC so allow for that !
	 */
	public void test_missingCtorAddedViaITD() {
		File testBase = new File(baseDir, "TestE");
		runSourceAndBinaryTestcase(testBase, new String[] { "A.java", "B.java", "C.java" }, new String[] { "X.java" }, true);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////

	public void runSourceAndBinaryTestcase(File testBase, String[] classes, String[] aspects, boolean expectErrors) {
		runSourceAndBinaryTestcase(testBase, classes, aspects, expectErrors, true);
	}

	public void runSourceAndBinaryTestcase(File testBase, String[] classes, String[] aspects, boolean expectErrors,
			boolean compareErrors) {
		// Do a compile of everything together from source ...
		CompilationResult result = null;

		// Execute: "ajc <classes> <aspects> -showWeaveInfo"
		String[] sourceCompileCommandLine = new String[classes.length + aspects.length + 2];
		System.arraycopy(classes, 0, sourceCompileCommandLine, 0, classes.length);
		System.arraycopy(aspects, 0, sourceCompileCommandLine, classes.length, aspects.length);
		String[] extraOption = new String[] { "-showWeaveInfo", "-1.4"};
		System.arraycopy(extraOption, 0, sourceCompileCommandLine, classes.length + aspects.length, 2);
		result = ajc(testBase, sourceCompileCommandLine);
		if (!expectErrors)
			assertTrue("errors? \n" + result.getErrorMessages(), !result.hasErrorMessages());
		List<IMessage> sourceWeaveMessages = getWeaveMessages(result);
		int sourceWeaveMessagesCount = sourceWeaveMessages.size();
		List<IMessage> sourceErrorMessages = result.getErrorMessages();
		int sourceErrorMessagesCount = sourceErrorMessages.size();

		if (verbose) {
			System.err.println("Source Compilation: Error count = " + sourceErrorMessagesCount + "\n" + sourceErrorMessages);
			System.err.println("Source Compilation: Weaving count = " + sourceWeaveMessagesCount + "\n" + sourceWeaveMessages);
		}

		// Do separate compiles of the classes then the aspects then do a binary weave

		// Execute: "ajc <classes> -g -d classes"
		result = ajc(testBase, mergeOptions(classes, new String[] { "-g", "-d", "classes" }));
		setShouldEmptySandbox(false);
		// Execute: "ajc <aspects> -g -outjar aspects.jar -classpath classes -proceedOnError"
		result = ajc(testBase, mergeOptions(aspects, new String[] { "-g", "-outjar", "aspects.jar", "-classpath", "classes",
				"-proceedOnError" }));
		if (result.getErrorMessages().size() != 0)
			System.err.println("Expecting no errors from jar building but got\n" + result.getErrorMessages());
		assertTrue("Should get no errors for this compile, but got: " + result.getErrorMessages().size(), result.getErrorMessages()
				.size() == 0);
		// Execute: "ajc -inpath classes -showWeaveInfo -d classes2 -aspectpath aspects.jar"
		result = ajc(testBase, new String[] { "-inpath", "classes", "-showWeaveInfo", "-1.4", "-d", "classes2", "-aspectpath",
				"aspects.jar" });

		if (!expectErrors)
			assertTrue("unexpected errors? \n" + result.getErrorMessages(), !result.hasErrorMessages());

		List<IMessage> binaryWeaveMessages = getWeaveMessages(result);
		int binaryWeaveMessagesCount = binaryWeaveMessages.size();
		List<IMessage> binaryErrorMessages = result.getErrorMessages();
		int binaryErrorMessagesCount = binaryErrorMessages.size();

		if (verbose) {
			System.err.println("Binary Compilation: Error count = " + binaryErrorMessagesCount + "\n" + binaryErrorMessages);
			System.err.println("Binary Compilation: Weaving count = " + binaryWeaveMessagesCount + "\n" + binaryWeaveMessages);
			System.err.println("StandardError from final binary compile stage: " + result.getStandardError());
		}

		assertTrue("Should have same number of errors in either case: " + sourceErrorMessagesCount + "!="
				+ binaryErrorMessagesCount, sourceErrorMessagesCount == binaryErrorMessagesCount);

		// ///////////////////////////////////////////////////////////////////////////
		// Check the error messages are comparable (allow for differing orderings)
		if (compareErrors) {
			for (IMessage binaryMessage : binaryErrorMessages) {
				IMessage correctSourceMessage = null;
				for (Iterator<IMessage> iterator = sourceErrorMessages.iterator(); iterator.hasNext() && correctSourceMessage == null; ) {
					IMessage sourceMessage = iterator.next();

					if (sourceMessage.getMessage().equals(binaryMessage.getMessage())) {
						correctSourceMessage = sourceMessage;
					}
				}
				if (correctSourceMessage == null) {
					fail("This error obtained during binary weaving '" + binaryMessage
							+ "' has no equivalent in the list of messages from source compilation");
				}
				sourceErrorMessages.remove(correctSourceMessage);
			}
			if (sourceErrorMessages.size() > 0) {
				for (IMessage srcMsg : sourceErrorMessages) {
					System.err.println("This error message from source compilation '" + srcMsg
							+ "' didn't occur during binary weaving.");
				}
				fail("Got " + sourceErrorMessages.size() + " extra error messages during source compilation");
			}
		}

		// //////////////////////////////////////////////////////////////////////////
		// Check the weaving messages are comparable
		if (sourceWeaveMessagesCount != binaryWeaveMessagesCount) {
			fail("Didn't get same number of weave info messages when source weaving and binary weaving: "
					+ sourceWeaveMessagesCount + "!=" + binaryWeaveMessagesCount);
		}

		// Check weaving messages are comparable
		for (int i = 0; i < sourceWeaveMessages.size(); i++) {
			IMessage m1 = sourceWeaveMessages.get(i);
			IMessage m2 = binaryWeaveMessages.get(i);
			String s1 = m1.getDetails();
			String s2 = m2.getDetails();

			if (!s1.equals(s2)) {
				System.err.println("Source Weave Messages: #" + sourceWeaveMessages.size() + "\n" + sourceWeaveMessages);
				System.err.println("Binary Weave Messages: #" + binaryWeaveMessages.size() + "\n" + binaryWeaveMessages);
				fail("Two weaving messages aren't the same?? sourceMessage=[" + s1 + "] binaryMessage=[" + s2 + "]");
			}
			if (m1.getSourceLocation() != null || m2.getSourceLocation() != null) {
				if (!m1.getSourceLocation().equals(m2.getSourceLocation())) {
					fail("Different source locations for weaving messages? \n" + m1.getSourceLocation() + "\n"
							+ m2.getSourceLocation());
				}
			}
		}

		// // Check the result of binary weaving !
		// ClassPath cp = new ClassPath(ajc.getSandboxDirectory()+File.separator+"classes2"+
		// File.pathSeparator+System.getProperty("sun.boot.class.path"));
		// System.err.println(cp);
		// SyntheticRepository r = SyntheticRepository.getInstance(cp);
		// Repository.setRepository(r);
		// for (int i = 0; i < classes.length; i++) {
		// String name = classes[i].substring(0,classes[i].lastIndexOf("."));
		// List verificationProblems = verify(name);
		// assertTrue("Did not expect any verification problems for class: "+name+": \n"+verificationProblems,verificationProblems.
		// size()==0);
		// }
	}

	public String[] mergeOptions(String[] input, String[] extras) {
		String[] ret = new String[input.length + extras.length];
		System.arraycopy(input, 0, ret, 0, input.length);
		System.arraycopy(extras, 0, ret, input.length, extras.length);
		return ret;
	}

	private List<IMessage> getWeaveMessages(CompilationResult result) {
		List<IMessage> infoMessages = result.getInfoMessages();
		List<IMessage> weaveMessages = new ArrayList<>();
		for (IMessage element: infoMessages) {//Iterator iter = infoMessages.iterator(); iter.hasNext();) {
//			IMessage element = (IMessage) iter.next();
			if (element.getKind() == IMessage.WEAVEINFO)
				weaveMessages.add(element);
		}
		return weaveMessages;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		baseDir = new File("../org.aspectj.ajdt.core/testdata", PROJECT_DIR);
	}

	// private List verify(String name) {
	// List verifyProblems = new ArrayList();
	// System.out.println("Now verifying: " + name + "\n");
	//
	// Verifier v = VerifierFactory.getVerifier(name);
	// VerificationResult vr;
	//
	// vr = v.doPass1();
	// if (vr != VerificationResult.VR_OK)
	// verifyProblems.add("Pass1: " + vr.getMessage());
	//
	// vr = v.doPass2();
	// if (vr != VerificationResult.VR_OK)
	// verifyProblems.add("Pass2: " + vr.getMessage());
	//
	// if (vr == VerificationResult.VR_OK) {
	// JavaClass jc = Repository.lookupClass(name);
	// for (int i = 0; i < jc.getMethods().length; i++) {
	// vr = v.doPass3a(i);
	// if (vr != VerificationResult.VR_OK)
	// verifyProblems.add("Pass3a: " + jc.getMethods()[i] + " " + vr.getMessage());
	//
	// vr = v.doPass3b(i);
	// if (vr != VerificationResult.VR_OK)
	// verifyProblems.add("Pass3b: " + jc.getMethods()[i] + " " + vr.getMessage());
	// }
	// }
	//
	// System.out.println("Warnings:");
	// String[] warnings = v.getMessages();
	// if (warnings.length == 0)
	// System.out.println("<none>");
	// for (int j = 0; j < warnings.length; j++) {
	// System.out.println(warnings[j]);
	// }
	//
	// System.out.println("\n");
	//
	// // avoid swapping.
	// v.flush();
	// Repository.clearCache();
	// return verifyProblems;
	// }

	// private void runClass(String name) {
	// RunResult rr = null;
	// try {
	// rr = run(name, new String[] {}, ajc.getSandboxDirectory() + File.separator + "classes2");
	// } catch (VerifyError ve) {
	// ve.printStackTrace();
	// fail("Unexpected VerifyError for type upon which we declared parents");
	// }
	// // assertTrue("Didn't expect any errors from the run of "+name+", but got: "+rr.toString(),rr.get);
	// }

}
