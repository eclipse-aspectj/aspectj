/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.tools.ajc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;

import junit.framework.TestCase;

/**
 * A TestCase class that acts as the superclass for all test cases wishing
 * to drive the ajc compiler.
 * <p>
 * This class provides a number of utility methods that make programmatic 
 * testing of the compiler easy. See AjcTestCaseTest for a couple of simple
 * tests written using this class.
 * </p>
 * <p>
 * See the XMLBasedAjcTestCase subclass for TestCase class that can be 
 * used to drive compiler tests based on an ajcTests.xml format test
 * specification file.</p>
 * @see org.aspectj.tools.ajc.AjcTestCase.Message
 * @see org.aspectj.tools.ajc.AjcTestCase.MessageSpec
 * @see org.aspectj.tools.ajc.AjcTestCase.RunResult
 * @see org.aspectj.tools.ajc.AjcTestCaseTest
 * @see org.aspectj.testing.XMLBasedAjcTestCase
 */
public class AjcTestCase extends TestCase {

	private RunResult lastRunResult;
	
	/**
	 * The Ajc (compiler) instance used for thet test. Created afresh
	 * during the test setup.
	 */
	protected Ajc ajc;
	
	private static final String DEFAULT_CLASSPATH_ENTRIES = 
        File.pathSeparator + 
		".." + File.separator + "runtime" + File.separator + "bin" + 
		File.pathSeparator +
		".." + File.separator + "testing-client" + File.separator + "bin" +
		File.pathSeparator +
		".." + File.separator + "bridge" + File.separator + "bin" +
		File.pathSeparator +
		".." + File.separator + "util" + File.separator + "bin" +
		File.pathSeparator + ".." + File.separator + "aspectj5rt" + File.separator + "bin" +
        //Alex: adding "_IDE" since there is no "bin" output when working within IDEA.
        // my convention is thus to have a "modules/_IDE" folder where IDEA will write
        // Since modules/* have circular dependancies, there is no way to have multiple "modules"
        // (like Eclipse projects in one workspace) in IDEA, so all will be build there.
        // Note: adding it last means that a change in the IDE aspectj5rt module f.e. without
        // "ant compile" to rebuild "aspect5rt/bin" will not expose the IDE changes...
        // but I don't want to have it first to avoid side effects when running from Ant.
        File.pathSeparator + ".." + File.separator + "_IDE" +
		File.pathSeparator+ ".."+File.separator+"lib"+File.separator+"junit"+File.separator+"junit.jar";

	/**
	 * Helper class that represents the specification of an individual
	 * message expected to be produced during a compilation run.
	 * <p>
	 * Message objects are combined in a MessageSpec which can then be 
	 * passed to the various assertMessage methods.</p>
	 * @see org.aspectj.tools.ajc.AjcTestCase.MessageSpec
	 */
	public static class Message {
		private int line = -1;
		private String text;
		private String sourceFileName;
		private ISourceLocation[] seeAlsos;
		
		/**
		 * Create a message that will match any compiler message on
		 * the given line.
		 */
		public Message(int line) {
			this.line = line;
		}
		
		/**
		 * Create a message that will match any compiler message on
		 * the given line, where the message text contains <code>text</code>.
		 */
		public Message(int line, String text) {
			this.line = line;
			this.text = text;
		}
		
		/**
		 * Create a message that will match any compiler message on
		 * the given line, where the message text contains <code>text</code>.
		 * <p>
		 * If srcFile is non-null, the source file location of the message must
		 * end with <code>srcFile</code>.
		 * </p>
		 * <p>
		 * If <code>seeAlso</code> is non-null, each source location in seeAlso
		 * must be matched by an extraSourceLocation in the message.
		 * </p>
		 */
		public Message(int line, String srcFile, String text, ISourceLocation[] seeAlso) {
			this.line = line;
			StringBuffer srcFileName = new StringBuffer();
			if (srcFile != null) {
				char[] chars = srcFile.toCharArray();
				for (int i = 0; i < chars.length; i++) {
					if ((chars[i] == '\\') || (chars[i] == '/')) {
						srcFileName.append(File.separator);
					} else {
						srcFileName.append(chars[i]);
					}
				}
				this.sourceFileName = srcFileName.toString();			
			}
			this.text = text;
			this.seeAlsos = seeAlso;
		}
		
		/**
		 * Create a message spec that will match any compiler message where
		 * the message text includes <code>text</code>.
		 */
		public Message(String text) {
			this.text = text;
		}

		/**
		 * Return true if this message spec matches the given compiler message.
		 */
		public boolean matches(IMessage message) {
			ISourceLocation loc = message.getSourceLocation();
			if ((loc == null) && ((line != -1) || (sourceFileName != null))) {
				return false;
			}
			if (line != -1) {
				if (loc.getLine() != line) {
					return false;
				}
			}
			if (sourceFileName != null) {
				if (!loc.getSourceFile().getPath().endsWith(sourceFileName)) {
					return false;
				}
			}
			if (text != null) {
				if (message.getMessage().indexOf(text) == -1) {
					return false;
				}
			}
			if (seeAlsos != null) {
				List extraLocations = message.getExtraSourceLocations();
				if (extraLocations.size() != seeAlsos.length) {
					return false;
				}
				for (int i = 0; i < seeAlsos.length; i++) {
					if (!hasAMatch(extraLocations,seeAlsos[i])) {
						return false;
					}
				}
			}
			return true;
		}
		
		private boolean hasAMatch(List srcLocations,ISourceLocation sLoc) {
			for (Iterator iter = srcLocations.iterator(); iter.hasNext();) {
				ISourceLocation thisLoc = (ISourceLocation) iter.next();
				if (thisLoc.getLine() == sLoc.getLine()) {
					if (thisLoc.getSourceFile().getPath().equals(sLoc.getSourceFile().getPath())) {
						return true;
					}
				}				
			}
			return false;
		}

		/**
		 * Returns a string indicating what this <code>Message</code> will match.
		 */
		public String toString() {
			StringBuffer buff = new StringBuffer();
			buff.append("message ");
			if (sourceFileName != null) {
				buff.append("in file ");
				buff.append(sourceFileName);
				buff.append(" ");
			}
			if (line != -1) {
				buff.append("on line ");
				buff.append(line);
				buff.append(" ");
			}
			if (text != null) {
				buff.append("containing text '");
				buff.append(text);
				buff.append("' ");
			}
			if (seeAlsos != null) {
				buff.append("\n\twith see alsos:");
				for (int i = 0; i < seeAlsos.length; i++) {
					buff.append("\t\t");
					buff.append(seeAlsos[i].getSourceFile().getPath());
					buff.append(":");
					buff.append(seeAlsos[i].getLine());
				}
			}
			return buff.toString();
		}
	}
	
	/**
	 * Helper class that represents the specification of a set of
	 * messages expected to be produced from a compiler run.
	 * <p>
	 * Instances of MessageSpec are passed to the assertMessage methods
	 * to validate <code>CompilationResult</code>s.
	 */
	public static class MessageSpec {
		
		/**
		 * Convenience constant that matches a CompilationResult with
		 * any number of information messages, but no others.
		 */
		public static final MessageSpec EMPTY_MESSAGE_SET = 
			new MessageSpec(
					null,
					Collections.EMPTY_LIST,
					Collections.EMPTY_LIST,
					Collections.EMPTY_LIST,
					Collections.EMPTY_LIST);
		
		boolean ignoreInfos = true;
		public List fails;
		public List infos;
		public List warnings;
		public List errors;
		public List weaves;
		
		/**
		 * Set to true to enable or disable comparison of information messages. 
		 */
		public void setInfoComparison(boolean enabled) {
			this.ignoreInfos = !enabled;
		}
		
		/**
		 * True if information messages are not being included in matching. 
		 */
		public boolean isIgnoringInfoMessages() {
			return ignoreInfos;
		}
		
		/**
		 * Create a message specification to test a CompilationResult for a 
		 * given set of info, warning, error, and fail messages.
		 * @param infos The set of info messages to test for. Specifying a non-null value
		 * for this parameter enables info message comparison.
		 * @param warnings The set of warning messages to test for - can pass null to indicate
		 * empty set.
		 * @param errors The set of error messages to test for - can pass null to indicate
		 * empty set.
		 * @param fails The set of fail or abort messages to test for - can pass null to indicate
		 * empty set.
		 */
		public MessageSpec(List infos, List warnings, List errors, List fails, List weaves) {
			if (infos != null) {
				this.infos = infos;
				ignoreInfos = false;
			} else {
				this.infos = Collections.EMPTY_LIST;
			}
			this.warnings = ((warnings == null) ? Collections.EMPTY_LIST : warnings);
			this.errors = ((errors == null) ? Collections.EMPTY_LIST : errors);
			this.fails = ((fails == null) ? Collections.EMPTY_LIST : fails);
			this.weaves = ((weaves == null) ? Collections.EMPTY_LIST : weaves);
		}
		
		/**
		 * Create a message specification to test a CompilationResult for a given
		 * set of info, warning, and error messages. The presence of any fail or
		 * abort messages in a CompilationResult will be a test failure.
		 */
		public MessageSpec(List infos, List warnings, List errors) {
			this(infos,warnings,errors,null,null);
		}

		/**
		 * Create a message specification to test a CompilationResult for a given
		 * set of warning, and error messages. The presence of any fail or
		 * abort messages in a CompilationResult will be a test failure. Informational
		 * messages will be ignored.
		 */
		public MessageSpec(List warnings, List errors) {
			this(null,warnings,errors,null,null);
		}
	}
	
	public static class EmptyMessageSpec extends MessageSpec {
		public EmptyMessageSpec() {
			super(null,null);
		}
	}
	
	/**
	 * Helper class representing the results of running a test program built
	 * by the compiler. Provides access to the standard out and error of the
	 * program, and the actual command that was executed.
	 */
	public static class RunResult {
		private String command;
		private String stdOut;
		private String stdErr;
		
		protected RunResult(String command, String stdOut, String stdErr) {
			this.command = command;
			this.stdOut = stdOut;
			this.stdErr = stdErr;
		}
		
		/**
		 * Return the command that was executed, e.g. "java Driver".
		 */
		public String getCommand() { return command; }
		/**
		 * The standard output from the run.
		 */
		public String getStdOut() {return stdOut;}
		/**
		 * The standard error from the run.
		 */
		public String getStdErr() {return stdErr;}
		
		/**
		 * Returns the command that was executed to produce this result.
		 */
		public String toString() { return command; }
	}
	
	/**
	 * Assert that no (non-informational) messages where produced during a compiler run.
	 */
	public void assertNoMessages(CompilationResult result) {
		assertNoMessages(result,"Not expecting any compiler messages to be produced");
	}
	
	/**
	 * Assert that no (non-informational) messages where produced during a compiler run.
	 */
	public void assertNoMessages(CompilationResult result, String message) {
		assertMessages(result, message,MessageSpec.EMPTY_MESSAGE_SET);
	}

	/**
	 * Assert that messages in accordance with the <code>expected</code> message specification
	 * where produced during a compiler run.
	 */
	public void assertMessages(CompilationResult result, MessageSpec expected) {
		assertMessages(result, "Compilation results did not meet expected messages specification",expected);
	}
	
	/**
	 * Assert that messages in accordance with the <code>expected</code> message specification
	 * where produced during a compiler run.
	 */
	public void assertMessages(CompilationResult result, String message, MessageSpec expected) {
		if (result == null) fail("Attempt to compare null compilation results against expected.");
		List missingFails = copyAll(expected.fails);
		List missingInfos = copyAll(expected.infos);
		List missingWarnings = copyAll(expected.warnings);
		List missingErrors = copyAll(expected.errors);
		List missingWeaves = copyAll(expected.weaves);
		List extraFails = copyAll(result.getFailMessages());
		List extraInfos = copyAll(result.getInfoMessages());
		List extraWarnings = copyAll(result.getWarningMessages());
		List extraErrors = copyAll(result.getErrorMessages());
		List extraWeaves = copyAll(result.getWeaveMessages());
		compare(expected.fails,result.getFailMessages(),missingFails,extraFails);
		compare(expected.warnings,result.getWarningMessages(),missingWarnings,extraWarnings);
		compare(expected.errors,result.getErrorMessages(),missingErrors,extraErrors);
		if (!expected.isIgnoringInfoMessages()) {
			compare(expected.infos,result.getInfoMessages(),missingInfos,extraInfos);
		}
		compare(expected.weaves,result.getWeaveMessages(),missingWeaves,extraWeaves);

		boolean infosEmpty = expected.isIgnoringInfoMessages() ? true: (missingInfos.isEmpty() && extraInfos.isEmpty());
		if ( !(missingFails.isEmpty() && missingWarnings.isEmpty() && missingErrors.isEmpty() && missingWeaves.isEmpty() &&
			   extraFails.isEmpty() && extraWarnings.isEmpty() && extraErrors.isEmpty() && extraWeaves.isEmpty() && infosEmpty)) {
			StringBuffer failureReport = new StringBuffer(message);
			failureReport.append("\n");
			if (!expected.isIgnoringInfoMessages()) {
				addMissing(failureReport,"info",missingInfos);
			}
			addMissing(failureReport,"warning",missingWarnings);
			addMissing(failureReport,"error",missingErrors);
			addMissing(failureReport,"fail",missingFails);
			addMissing(failureReport,"weaveInfo",missingWeaves);
			if (!expected.isIgnoringInfoMessages()) {
				addExtra(failureReport,"info",extraInfos);
			}
			addExtra(failureReport,"warning",extraWarnings);
			addExtra(failureReport,"error",extraErrors);
			addExtra(failureReport,"fail",extraFails);
			addExtra(failureReport,"weaveInfo",extraWeaves);
			failureReport.append("\ncommand was: ajc");
			String[] args = result.getArgs();
			for (int i = 0; i < args.length; i++) {
				failureReport.append(" ");
				failureReport.append(args[i]);
			}
			String report = failureReport.toString();
			System.err.println(failureReport);
			fail(message + "\n" + report);
		}
	}

	/**
	 * Helper method to build a new message list for passing to a MessageSpec.
	 */
	protected List newMessageList(Message m1) {
		List ret = new ArrayList();
		ret.add(m1);
		return ret;
	}
	
	/**
	 * Helper method to build a new message list for passing to a MessageSpec.
	 */
	protected List newMessageList(Message m1, Message m2) {
		List ret = new ArrayList();
		ret.add(m1);
		ret.add(m2);
		return ret;		
	}
	
	/**
	 * Helper method to build a new message list for passing to a MessageSpec.
	 */
	protected List newMessageList(Message m1, Message m2, Message m3) {
		List ret = new ArrayList();
		ret.add(m1);
		ret.add(m2);
		ret.add(m3);
		return ret;				
	}
	
	/**
	 * Helper method to build a new message list for passing to a MessageSpec.
	 */
	protected List newMessageList(Message[] messages) {
		List ret = new ArrayList();
		for (int i = 0; i < messages.length; i++) {
			ret.add(messages[i]);
		}
		return ret;
	}

	/**
	 * Perform a compilation and return the result.
	 * @param baseDir the base directory relative to which all relative paths and
	 * directories in the arguments will be interpreted.
	 * @param args the compiler arguments, as you would specify on the command-line. 
	 * See the Ajc class for a description of the argument processing done in
	 * order to run the compilation in a sandbox.
	 * @see org.aspectj.tools.ajc.Ajc  
	 */
	public CompilationResult ajc(File baseDir, String[] args) {
		try {
			ajc.setBaseDir(baseDir);
			return ajc.compile(args);
		} catch(IOException ioEx ) {
			fail("IOException thrown during compilation: " + ioEx);
		}
		return null;
	}
	
	public File getSandboxDirectory() {
		return ajc.getSandboxDirectory();
	}
	
	/**
	 * Indicate whether or not the sandbox should be emptied before the next compile.
	 * @see org.aspectj.tools.ajc.Ajc#setShouldEmptySandbox(boolean)
	 */
	public void setShouldEmptySandbox(boolean empty) {
		ajc.setShouldEmptySandbox(empty);
	}

	public RunResult getLastRunResult() {
		return lastRunResult;
	}
	
	/**
	 * Run the given class (main method), and return the result in a RunResult. The program runs with
	 * a classpath containing the sandbox directory, runtime, testing-client, bridge, and
	 * util projects (all used by the Tester class), and any jars in the sandbox.
	 */
	public RunResult run(String className){
		return run(className,new String[0],null);
	}
	
	/**
	 * Run the given class, and return the result in a RunResult. The program runs with
	 * a classpath containing the sandbox directory, runtime, testing-client, bridge, and
	 * util projects (all used by the Tester class), and any jars in the sandbox.
	 * @param args the arguments to pass to the program.
	 * @param classpath the execution classpath, the sandbox directory, runtime, testing-client,
	 * bridge, and util projects will all be appended to the classpath, as will any jars in
	 * the sandbox.
	 */
	public RunResult run(String className, String[] args, String classpath)  {
		lastRunResult = null;
		StringBuffer cp = new StringBuffer();
		if (classpath != null) {
			cp.append(classpath);
			cp.append(File.pathSeparator);
		}
		cp.append(ajc.getSandboxDirectory().getAbsolutePath());
		cp.append(DEFAULT_CLASSPATH_ENTRIES);
		getAnyJars(ajc.getSandboxDirectory(),cp);
		classpath = cp.toString();
		StringBuffer command = new StringBuffer("java -classpath ");
		command.append(classpath);
		command.append(" ");
		command.append(className);
		for (int i = 0; i < args.length; i++) {
			command.append(" ");
			command.append(args[i]);
		}
		PrintStream systemOut = System.out;
		PrintStream systemErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		StringTokenizer strTok = new StringTokenizer(classpath,File.pathSeparator);
		URL[] urls = new URL[strTok.countTokens()];
		try {
			for (int i = 0; i < urls.length; i++) {
				urls[i] = new File(strTok.nextToken()).getCanonicalFile().toURL();
			}
		} catch (Exception malEx) {
			fail("Bad classpath specification: " + classpath);
		}
		URLClassLoader cLoader = new URLClassLoader(urls,null);
		//System.out.println(cLoader.getParent());
		try {
			try {
				Class testerClass = cLoader.loadClass("org.aspectj.testing.Tester");
				Method setBaseDir = testerClass.getDeclaredMethod("setBASEDIR",new Class[] {File.class});
				setBaseDir.invoke(null,new Object[] {ajc.getSandboxDirectory()});
			} catch (Exception ex) {
				fail ("Unable to prepare org.aspectj.testing.Tester for test run: " + ex);
			}
			Class toRun = cLoader.loadClass(className);
			Method mainMethod = toRun.getMethod("main",new Class[] {String[].class});
			System.setOut(new PrintStream(baosOut));
			System.setErr(new PrintStream(baosErr));
			mainMethod.invoke(null,new Object[] {args});
			lastRunResult = new RunResult(command.toString(),new String(baosOut.toByteArray()),new String(baosErr.toByteArray()));
		} catch(ClassNotFoundException cnf) {
			fail("Can't find class: " + className);
		} catch(NoSuchMethodException nsm) {
			fail(className + " does not have a main method");
		} catch (IllegalAccessException illEx) {
			fail("main method in class " + className + " is not public");
		} catch (InvocationTargetException invTgt) {
			// the main method threw an exception...
			fail("Exception thrown by " + className + ".main(String[]) :" + invTgt.getTargetException());
		} finally {
			System.setOut(systemOut);
			System.setErr(systemErr);
		}
		return lastRunResult;
	}
	
	private List copyAll(List in) {
		if (in == Collections.EMPTY_LIST) return in;
		
		List out = new ArrayList();
		for (Iterator iter = in.iterator(); iter.hasNext();) {
			out.add(iter.next());			
		}
		return out;
	}
	
	/**
	 * Compare the set of expected messages against the set of actual messages,
	 * leaving in missingElements the set of messages that were expected but did not
	 * occur, and in extraElements the set of messages that occured but were not 
	 * excpected
	 * @param expected  the expected messages
	 * @param actual the actual messages
	 * @param missingElements the missing messages, when passed in must contain all of the expected messages
	 * @param extraElements the additional messages, when passed in must contain all of the actual messages
	 */
	private void compare(List expected, List actual, List missingElements, List extraElements) {
		for (Iterator expectedIter = expected.iterator(); expectedIter.hasNext();) {
			Message expectedMessage = (Message) expectedIter.next();
			for (Iterator actualIter = actual.iterator(); actualIter.hasNext();) {
				IMessage actualMessage = (IMessage) actualIter.next();
				if (expectedMessage.matches(actualMessage)) {
					missingElements.remove(expectedMessage);
					extraElements.remove(actualMessage);
				}
			}
		}
	}

	private void addMissing(StringBuffer buff,String type, List messages) {
		if (!messages.isEmpty()) {
			buff.append("Missing expected ");
			buff.append(type);
			buff.append(" messages:\n");
			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				buff.append("\t");
				buff.append(iter.next().toString());
				buff.append("\n");
			}
		}
	}
	
	private void addExtra(StringBuffer buff, String type, List messages) {
		if (!messages.isEmpty()) {
			buff.append("Unexpected ");
			buff.append(type);
			buff.append(" messages:\n");
			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				buff.append("\t");
				buff.append(iter.next().toString());
				buff.append("\n");
			}
		}		
	}
	
	// add any jars in the directory to the classpath
	private void getAnyJars(File dir,StringBuffer buff) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".jar")) {
				buff.append(File.pathSeparator);
				buff.append(files[i].getAbsolutePath());
			} else if (files[i].isDirectory()) {
				getAnyJars(files[i],buff);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ajc = new Ajc();
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
