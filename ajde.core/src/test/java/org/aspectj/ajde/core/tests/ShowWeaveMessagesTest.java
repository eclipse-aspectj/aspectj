/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement     Initial version
 *    Helen Hawkins    Converted to new interface (bug 148190)
 * ******************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.JavaOptions;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.bridge.IMessage;

/**
 * Weaving messages are complicated things. There are multiple places where weaving takes place and the places vary depending on
 * whether we are doing a binary weave or going from source. All places that output weaving messages are tagged: // TAG:
 * WeavingMessage so you can easily find them!
 *
 * Advice is the simplest to deal with as that is advice weaving is always done in the weaver.
 *
 * Next is intertype declarations. These are also always done in the weaver but in the case of a binary weave we don't know the
 * originating source line for the ITD.
 *
 * Finally, declares. Declare Parents: extends Can only be done when going from source, if attempted by a binary weave then an error
 * message (compiler limitation) is produced. Declare Parents: implements Is (currently!) done at both compile time and weave time.
 * If going from source then the message is produced by the code in the compiler. if going from binary then the message is produced
 * by the weaver. Declare Soft: Comes out with 'advice' as a special kind of advice: softener advice
 *
 *
 * Q: Where are the messages turned on/off? A: It is a bit messy. See BuildArgParser.genBuildConfig(). Basically that method is the
 * first time we parse the option set. Whether weaving messages are on or off is stored in the build config. As soon as we have
 * parser the options and determined that weave messages are on, we grab the top level message handler and tell it not to ignore
 * WeaveInfo messages.
 *
 *
 * TODO - Other forms of declare? Do they need messages? e.g. declare precedence *
 */
public class ShowWeaveMessagesTest extends AjdeCoreTestCase {

	private static boolean regenerate;
	private static boolean debugTests = false;

	static {
		// Switch this to true for a single iteration if you want to reconstruct the
		// 'expected weaving messages' files.
		regenerate = false;
	}

	public static final String PROJECT_DIR = "WeaveInfoMessagesTest";

	public static final String binDir = "bin";
	public static final String expectedResultsDir = "expected";

	public String[] one = new String[] { "AspectAdvice.aj", "Simple.java" };
	public String[] two = new String[] { "AspectITD.aj", "Simple.java" };
	public String[] three = new String[] { "AspectDeclare.aj", "Simple.java" };
	public String[] four = new String[] { "AspectDeclareExtends.aj", "Simple.java" };
	public String[] five = new String[] { "Simple.java", "AspectDeclareSoft.aj" };
	public String[] six = new String[] { "AspectDeclareAnnotations.aj" };
	public String[] seven = new String[] { "AspectDeclareAnnotations.aj" };
	public String[] empty = new String[] {};

	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject(PROJECT_DIR);
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		handler.dontIgnore(IMessage.WEAVEINFO);
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
		compilerConfig.setNonStandardOptions("-showWeaveInfo");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}

	/**
	 * Weave all the possible kinds of advice and verify the messages that come out.
	 */
	public void testWeaveMessagesAdvice() {
		if (debugTests)
			System.out.println("testWeaveMessagesAdvice: Building with One.lst");
		compilerConfig.setProjectSourceFiles(getSourceFileList(one));
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("advice", true);
	}

	/**
	 * Weave field and method ITDs and check the weave messages that come out.
	 */
	public void testWeaveMessagesITD() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesITD: Building with Two.lst");
		compilerConfig.setProjectSourceFiles(getSourceFileList(two));
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("itd", true);
	}

	/**
	 * Weave "declare parents: implements" and check the weave messages that come out.
	 */
	public void testWeaveMessagesDeclare() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesDeclare: Building with Three.lst");
		compilerConfig.setProjectSourceFiles(getSourceFileList(three));
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare1", true);
	}

	/**
	 * Weave "declare parents: extends" and check the weave messages that come out. Can't do equivalent binary test - as can't do
	 * extends in binary.
	 */
	public void testWeaveMessagesDeclareExtends() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesDeclareExtends: Building with Four.lst");
		compilerConfig.setProjectSourceFiles(getSourceFileList(four));
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare.extends", true);
	}

	/**
	 * Weave "declare soft: type: pointcut" and check the weave messages that come out.
	 */
	public void testWeaveMessagesDeclareSoft() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesDeclareSoft: Building with Five.lst");
		compilerConfig.setProjectSourceFiles(getSourceFileList(five));
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare.soft", true);
	}

	/**
	 * Weave 'declare @type, @constructor, @method and @field' and check the weave messages that come out.
	 */
	public void testWeaveMessagesDeclareAnnotation() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesDeclareAnnotation: Building with Six.lst");
		compilerConfig.setProjectSourceFiles(getSourceFileList(six));
		setRunIn15Mode();
		compilerConfig.setNonStandardOptions("-showWeaveInfo -1.5");
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare.annotation", true);
	}

	/**
	 * Weave 'declare @type, @constructor, @method and @field' and check the weave messages don't come out without the
	 * -showWeaveInfo arg.
	 */
	public void testWeaveMessagesDeclareAnnotationWeaveInfoOff() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesDeclareAnnotation: Building with Seven.lst");
		compilerConfig.setProjectSourceFiles(getSourceFileList(seven));
		compilerConfig.setNonStandardOptions("");
		setRunIn15Mode();
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare.annotationNoWeaveInfo", true);
	}

	// BINARY WEAVING TESTS

	/**
	 * Binary weave variant of the advice weaving test above - to check messages are ok for binary weave. Unlike the source level
	 * weave, in this test we are using an aspect on the aspectpath - which means it has already had its necessary parts woven - so
	 * the list of weaving messages we expect is less.
	 */
	public void testWeaveMessagesBinaryAdvice() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryAdvice: Simple.jar + AspectAdvice.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectAdvice.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("advice.binary", true);
	}

	public void testWeaveMessagesBinaryITD() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryITD: Simple.jar + AspectITD.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectITD.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("itd", false);
	}

	public void testWeaveMessagesBinaryDeclare() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryDeclare: Simple.jar + AspectDeclare.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectDeclare.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare1", false);
	}

	/**
	 * Weave "declare soft: type: pointcut" and check the weave messages that come out.
	 */
	public void testWeaveMessagesBinaryDeclareSoft() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryDeclareSoft: Simple.jar + AspectDeclareSoft.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectDeclareSoft.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare.soft.binary", true);
	}

	public void testWeaveMessagesBinaryAdviceInPackageFromJar() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryAdviceInPackageFromJar: Simple.jar + AspectInPackage.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectInPackage.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("advice.binary.package.jar", true);
	}

	public void testWeaveMessagesBinaryAdviceInPackage() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryAdviceInPackage: Simple.jar + AspectInPackage.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("pkg"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("advice.binary.package", true);
	}

	// BINARY WEAVING WHEN WE'VE LOST THE SOURCE POINTERS

	public void testWeaveMessagesBinaryAdviceNoDebugInfo() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryAdvice: Simple.jar + AspectAdvice.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple_nodebug.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectAdvice_nodebug.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("advice.binary.nodebug", true);
	}

	public void testWeaveMessagesBinaryITDNoDebugInfo() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryITD: Simple.jar + AspectITD.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple_nodebug.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectITD_nodebug.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("itd.nodebug", true);
	}

	public void testWeaveMessagesBinaryDeclareNoDebugInfo() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryDeclareNoDebugInfo: Simple.jar + AspectDeclare.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple_nodebug.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectDeclare_nodebug.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare1.nodebug", true);
	}

	/**
	 * Weave "declare soft: type: pointcut" and check the weave messages that come out.
	 */
	public void testWeaveMessagesBinaryDeclareSoftNoDebugInfo() {
		if (debugTests)
			System.out.println("\ntestWeaveMessagesBinaryDeclareSoftNoDebugInfo: Simple.jar + AspectDeclareSoft.jar");
		Set<File> inpath = new HashSet<>();
		inpath.add(openFile("Simple_nodebug.jar"));
		compilerConfig.setInpath(inpath);
		Set<File> aspectpath = new HashSet<>();
		aspectpath.add(openFile("AspectDeclareSoft_nodebug.jar"));
		compilerConfig.setAspectPath(aspectpath);
		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
		verifyWeavingMessages("declare.soft.binary.nodebug", true);
	}

	public void verifyWeavingMessages(String testid, boolean source) {
		File expectedF = openFile(expectedResultsDir + File.separator + testid + ".txt");
		if (regenerate && source) {
			// Create the file
			saveWeaveMessages(expectedF);
		} else {
			// Verify the file matches what we have
			compareWeaveMessages(expectedF);
		}
	}

	/**
	 * Compare weaving messages with what is in the file
	 */
	private void compareWeaveMessages(File f) {
		List<String> fileContents = new ArrayList<>();
		BufferedReader fr;
		try {
			// Load the file in
			fr = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = fr.readLine()) != null)
				fileContents.add(line);
			List<String> originalFileContents = new ArrayList<>(fileContents);

			// See if the messages match
			int msgCount = 0;
			List<TestMessageHandler.TestMessage> l = handler.getMessages();
			for (TestMessageHandler.TestMessage testMessage : l) {
				IMessage msg = testMessage.getContainedMessage();
				if (debugTests)
					System.out.println("Looking at [" + msg + "]");
				if (msg.getKind().equals(IMessage.WEAVEINFO)) {
					if (!fileContents.contains(msg.getMessage())) {
						fail("Could not find message '" + msg.getMessage() + "' in the expected results.  Expected results are:\n"
								+ stringify(originalFileContents));
					} else {
						fileContents.remove(msg.getMessage());
					}
					msgCount++;
				}
			}
			assertTrue("Didn't get these expected messages: " + fileContents, fileContents.size() == 0);
			if (debugTests)
				System.out.println("Successfully verified " + msgCount + " weaving messages");
		} catch (Exception e) {
			fail("Unexpected exception saving weaving messages:" + e);
		}
	}

	private String stringify(List<String> l) {
		StringBuffer result = new StringBuffer();
		for (String str: l) {
			result.append(str);
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Store the weaving messages in the specified file.
	 */
	private void saveWeaveMessages(File f) {
		System.out.println("Saving weave messages into " + f.getName());
		FileWriter fw;
		try {
			fw = new FileWriter(f);
			List<TestMessageHandler.TestMessage> l = handler.getMessages();
			for (TestMessageHandler.TestMessage testMessage : l) {
				IMessage msg = testMessage.getContainedMessage();
				if (msg.getKind().equals(IMessage.WEAVEINFO)) {
					fw.write(msg.getMessage() + "\n");
				}
			}
			fw.close();
		} catch (Exception e) {
			fail("Unexpected exception saving weaving messages:" + e);
		}
	}

	private void setRunIn15Mode() {
		Map<String, String> m = new Hashtable<>();
		m.put(JavaOptions.COMPLIANCE_LEVEL, JavaOptions.VERSION_15);
		m.put(JavaOptions.SOURCE_COMPATIBILITY_LEVEL, JavaOptions.VERSION_15);
		m.put(JavaOptions.TARGET_COMPATIBILITY_LEVEL, JavaOptions.VERSION_15);
		compilerConfig.setJavaOptions(m);
	}

}
