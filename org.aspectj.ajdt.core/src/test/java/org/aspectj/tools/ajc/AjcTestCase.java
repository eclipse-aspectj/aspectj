/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adrian Colyer, Abraham Nevado (lucierna)
 * ******************************************************************/
package org.aspectj.tools.ajc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.util.LangUtil;

import junit.framework.TestCase;

/**
 * A TestCase class that acts as the superclass for all test cases wishing to drive the ajc compiler.
 * <p>
 * This class provides a number of utility methods that make programmatic testing of the compiler easy. See AjcTestCaseTest for a
 * couple of simple tests written using this class.
 * </p>
 * <p>
 * See the XMLBasedAjcTestCase subclass for TestCase class that can be used to drive compiler tests based on an ajcTests.xml format
 * test specification file.
 * </p>
 *
 * @see org.aspectj.tools.ajc.AjcTestCase.Message
 * @see org.aspectj.tools.ajc.AjcTestCase.MessageSpec
 * @see org.aspectj.tools.ajc.AjcTestCase.RunResult
 * @see org.aspectj.tools.ajc.AjcTestCaseTest
 * @see org.aspectj.testing.XMLBasedAjcTestCase
 */
public abstract class AjcTestCase extends TestCase {

	private RunResult lastRunResult;

	/**
	 * The Ajc (compiler) instance used for the test. Created afresh during the test setup.
	 */
	protected Ajc ajc;

	// see Ajc and AntSpec
	public static final String DEFAULT_CLASSPATH_ENTRIES =
			Ajc.outputFolders("bridge","util","loadtime","weaver","asm","testing-client","runtime","org.aspectj.matcher")
			//			File.pathSeparator + ".." + File.separator + "bridge" + File.separator
			//			+ "bin" + File.pathSeparator + ".." + File.separator + "util" + File.separator + "bin" + File.pathSeparator + ".."
			//			+ File.separator + "loadtime" + File.separator + "bin" + File.pathSeparator + ".." + File.separator + "weaver"
			//			+ File.separator + "bin" + File.pathSeparator + ".." + File.separator + "weaver5" + File.separator + "bin"
			//			+ File.pathSeparator + ".." + File.separator + "asm" + File.separator + "bin" + File.pathSeparator + ".."
			//			+ File.separator + "testing-client" + File.separator + "bin" + File.pathSeparator + ".." + File.separator + "runtime"
			//			+ File.separator + "bin" + File.pathSeparator + ".." + File.separator + "aspectj5rt" + File.separator + "bin"
			//			+ File.pathSeparator + ".." + File.separator + "org.aspectj.matcher" + File.separator + "bin"
			+ File.pathSeparator
			+ ".." + File.separator + "lib" + File.separator + "junit" + File.separator
			+ "junit.jar"
			+ File.pathSeparator
			+ ".."
			+ File.separator
			+ "lib"
			+ File.separator
			+ "bcel"
			+ File.separator
			+ "bcel.jar"
			+ File.pathSeparator
			+ ".."
			+ File.separator
			+ "lib"
			+ File.separator
			+ "bcel"
			+ File.separator
			+ "bcel-verifier.jar"

			+ File.pathSeparator + ".." +  File.separator + "lib" + File.separator + "asm" + File.separator + "asm-8.0.1.renamed.jar"

			// When the build machine executes the tests, it is using code built into jars rather than code build into
			// bin directories. This means for the necessary types to be found we have to put these jars on the classpath:
			+ File.pathSeparator + ".." + File.separator + "aj-build" + File.separator + "jars" + File.separator + "bridge.jar"
			+ File.pathSeparator + ".." + File.separator + "aj-build" + File.separator + "jars" + File.separator + "util.jar"
			+ File.pathSeparator + ".." + File.separator + "aj-build" + File.separator + "jars" + File.separator
			+ "org.aspectj.matcher.jar" + File.pathSeparator + ".." + File.separator + "aj-build" + File.separator + "jars"
			+ File.separator + "loadtime.jar" + File.pathSeparator + ".." + File.separator + "aj-build" + File.separator + "jars"
			+ File.separator + "weaver.jar" + File.pathSeparator + ".." + File.separator + "aj-build" + File.separator + "jars"
			+ File.pathSeparator + ".." + File.separator + "aj-build" + File.separator + "jars"
			+ File.separator + "asm.jar" + File.pathSeparator + ".." + File.separator + "lib" + File.separator + "test"
			+ File.separator + "testing-client.jar"
			// hmmm, this next one should perhaps point to an aj-build jar...
			+ File.pathSeparator + ".." + File.separator + "lib" + File.separator + "test" + File.separator + "aspectjrt.jar";

	/*
	 * Save reference to real stderr and stdout before starting redirection
	 */
	public final static PrintStream err = System.err;
	public final static PrintStream out = System.out;
	private final static DelegatingOutputStream delegatingErr;
	private final static DelegatingOutputStream delegatingOut;
	public final static boolean DEFAULT_VERBOSE = getBoolean("aspectj.tests.verbose", true);
	public final static boolean DEFAULT_ERR_VERBOSE = getBoolean("org.aspectj.tools.ajc.AjcTestCase.verbose.err", DEFAULT_VERBOSE);
	public final static boolean DEFAULT_OUT_VERBOSE = getBoolean("org.aspectj.tools.ajc.AjcTestCase.verbose.out", DEFAULT_VERBOSE);

	private Process exec;

	/**
	 * Helper class that represents the specification of an individual message expected to be produced during a compilation run.
	 * <p>
	 * Message objects are combined in a MessageSpec which can then be passed to the various assertMessage methods.
	 * </p>
	 *
	 * @see org.aspectj.tools.ajc.AjcTestCase.MessageSpec
	 */
	public static class Message {
		private int line = -1;
		private String text;
		private String sourceFileName;
		private ISourceLocation[] seeAlsos;
		public boolean careAboutOtherMessages = true;

		/**
		 * Create a message that will match any compiler message on the given line.
		 */
		public Message(int line) {
			this.line = line;
		}

		/**
		 * Create a message that will match any compiler message on the given line, where the message text contains
		 * <code>text</code>.
		 */
		public Message(int line, String text) {
			this.line = line;
			this.text = text;
			if (this.text != null && text.startsWith("*")) {
				// Don't care what other messages are around
				this.careAboutOtherMessages = false;
				this.text = this.text.substring(1);
			}
		}

		/**
		 * Create a message that will match any compiler message on the given line, where the message text contains
		 * <code>text</code>.
		 * <p>
		 * If srcFile is non-null, the source file location of the message must end with <code>srcFile</code>.
		 * </p>
		 * <p>
		 * If <code>seeAlso</code> is non-null, each source location in seeAlso must be matched by an extraSourceLocation in the
		 * message.
		 * </p>
		 */
		public Message(int line, String srcFile, String text, ISourceLocation[] seeAlso) {
			this.line = line;
			StringBuffer srcFileName = new StringBuffer();
			if (srcFile != null) {
				char[] chars = srcFile.toCharArray();
				for (char c : chars) {
					if ((c == '\\') || (c == '/')) {
						srcFileName.append(File.separator);
					} else {
						srcFileName.append(c);
					}
				}
				this.sourceFileName = srcFileName.toString();
			}
			this.text = text;
			if (this.text != null && text.startsWith("*")) {
				// Don't care what other messages are around
				this.careAboutOtherMessages = false;
				this.text = this.text.substring(1);
			}
			this.seeAlsos = seeAlso;
		}

		/**
		 * Create a message spec that will match any compiler message where the message text includes <code>text</code>.
		 */
		public Message(String text) {
			this.text = text;
			if (this.text != null && text.startsWith("*")) {
				// Don't care what other messages are around
				this.careAboutOtherMessages = false;
				this.text = this.text.substring(1);
			}
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
				if (!message.getMessage().contains(text)) {
					return false;
				}
			}
			if (seeAlsos != null) {
				List<ISourceLocation> extraLocations = message.getExtraSourceLocations();
				if (extraLocations.size() != seeAlsos.length) {
					return false;
				}
				for (ISourceLocation seeAlso : seeAlsos) {
					if (!hasAMatch(extraLocations, seeAlso)) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean hasAMatch(List<ISourceLocation> srcLocations, ISourceLocation sLoc) {
			for (ISourceLocation thisLoc: srcLocations) {
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
		@Override
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
				for (ISourceLocation seeAlso : seeAlsos) {
					buff.append("\t\t");
					buff.append(seeAlso.getSourceFile().getPath());
					buff.append(":");
					buff.append(seeAlso.getLine());
				}
			}
			return buff.toString();
		}
	}

	/**
	 * Helper class that represents the specification of a set of messages expected to be produced from a compiler run.
	 * <p>
	 * Instances of MessageSpec are passed to the assertMessage methods to validate <code>CompilationResult</code>s.
	 */
	public static class MessageSpec {

		/**
		 * Convenience constant that matches a CompilationResult with any number of information messages, but no others.
		 */
		public static final MessageSpec EMPTY_MESSAGE_SET = new MessageSpec(null, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
				Collections.EMPTY_LIST, Collections.EMPTY_LIST);

		boolean ignoreInfos = true;
		public List<AjcTestCase.Message> fails;
		public List<AjcTestCase.Message> infos;
		public List<AjcTestCase.Message> warnings;
		public List<AjcTestCase.Message> errors;
		public List<AjcTestCase.Message> weaves;

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
		 * Create a message specification to test a CompilationResult for a given set of info, warning, error, and fail messages.
		 *
		 * @param infos The set of info messages to test for. Specifying a non-null value for this parameter enables info message
		 *        comparison.
		 * @param warnings The set of warning messages to test for - can pass null to indicate empty set.
		 * @param errors The set of error messages to test for - can pass null to indicate empty set.
		 * @param fails The set of fail or abort messages to test for - can pass null to indicate empty set.
		 */
		public MessageSpec(List<AjcTestCase.Message> infos, List<AjcTestCase.Message> warnings,
				List<AjcTestCase.Message> errors, List<AjcTestCase.Message> fails, List<AjcTestCase.Message> weaves) {
			if (infos != null) {
				this.infos = infos;
				ignoreInfos = false;
			} else {
				this.infos = Collections.emptyList();
			}
			this.warnings = ((warnings == null) ? Collections.<AjcTestCase.Message>emptyList() : warnings);
			this.errors = ((errors == null) ? Collections.<AjcTestCase.Message>emptyList() : errors);
			this.fails = ((fails == null) ? Collections.<AjcTestCase.Message>emptyList() : fails);
			this.weaves = ((weaves == null) ? Collections.<AjcTestCase.Message>emptyList() : weaves);
		}

		/**
		 * Create a message specification to test a CompilationResult for a given set of info, warning, and error messages. The
		 * presence of any fail or abort messages in a CompilationResult will be a test failure.
		 */
		public MessageSpec(List<AjcTestCase.Message> infos, List<AjcTestCase.Message> warnings, List<AjcTestCase.Message> errors) {
			this(infos, warnings, errors, null, null);
		}

		/**
		 * Create a message specification to test a CompilationResult for a given set of warning, and error messages. The presence
		 * of any fail or abort messages in a CompilationResult will be a test failure. Informational messages will be ignored.
		 */
		public MessageSpec(List<AjcTestCase.Message> warnings, List<AjcTestCase.Message> errors) {
			this(null, warnings, errors, null, null);
		}
	}

	public static class EmptyMessageSpec extends MessageSpec {
		public EmptyMessageSpec() {
			super(null, null);
		}
	}

	/**
	 * Helper class representing the results of running a test program built by the compiler. Provides access to the standard out
	 * and error of the program, and the actual command that was executed.
	 */
	public static class RunResult {
		private final String command;
		private final String stdOut;
		private final String stdErr;

		protected RunResult(String command, String stdOut, String stdErr) {
			this.command = command;
			this.stdOut = stdOut;
			this.stdErr = stdErr;
		}

		/**
		 * Return the command that was executed, e.g. "java Driver".
		 */
		public String getCommand() {
			return command;
		}

		/**
		 * The standard output from the run.
		 */
		public String getStdOut() {
			return stdOut;
		}

		/**
		 * The standard error from the run.
		 */
		public String getStdErr() {
			return stdErr;
		}

		/**
		 * Returns the command that was executed to produce this result.
		 */
		@Override
		public String toString() {
			return command;
		}
	}

	/**
	 * Assert that no (non-informational) messages where produced during a compiler run.
	 */
	public void assertNoMessages(CompilationResult result) {
		assertNoMessages(result, "Not expecting any compiler messages to be produced");
	}

	/**
	 * Assert that no (non-informational) messages where produced during a compiler run.
	 */
	public void assertNoMessages(CompilationResult result, String message) {
		assertMessages(result, message, MessageSpec.EMPTY_MESSAGE_SET);
	}

	/**
	 * Assert that messages in accordance with the <code>expected</code> message specification where produced during a compiler run.
	 */
	public void assertMessages(CompilationResult result, MessageSpec expected) {
		assertMessages(result, "Compilation results did not meet expected messages specification", expected);
	}

	/**
	 * Assert that messages in accordance with the <code>expected</code> message specification where produced during a compiler run.
	 */
	public void assertMessages(CompilationResult result, String message, MessageSpec expected) {
		if (result == null)
			fail("Attempt to compare null compilation results against expected.");
		List<AjcTestCase.Message> missingFails = copyAll(expected.fails);
		List<AjcTestCase.Message> missingInfos = copyAll(expected.infos);
		List<AjcTestCase.Message> missingWarnings = copyAll(expected.warnings);
		List<AjcTestCase.Message> missingErrors = copyAll(expected.errors);
		List<AjcTestCase.Message> missingWeaves = copyAll(expected.weaves);
		List<IMessage> extraFails = copyAll(result.getFailMessages());
		List<IMessage> extraInfos = copyAll(result.getInfoMessages());
		List<IMessage> extraWarnings = copyAll(result.getWarningMessages());
		List<IMessage> extraErrors = copyAll(result.getErrorMessages());
		List<IMessage> extraWeaves = copyAll(result.getWeaveMessages());
		compare(expected.fails, result.getFailMessages(), missingFails, extraFails);
		compare(expected.warnings, result.getWarningMessages(), missingWarnings, extraWarnings);
		compare(expected.errors, result.getErrorMessages(), missingErrors, extraErrors);
		if (!expected.isIgnoringInfoMessages()) {
			compare(expected.infos, result.getInfoMessages(), missingInfos, extraInfos);
		}
		compare(expected.weaves, result.getWeaveMessages(), missingWeaves, extraWeaves);

		boolean infosEmpty = expected.isIgnoringInfoMessages() ? true : (missingInfos.isEmpty() && extraInfos.isEmpty());
		if (!(missingFails.isEmpty() && missingWarnings.isEmpty() && missingErrors.isEmpty() && missingWeaves.isEmpty()
				&& extraFails.isEmpty() && extraWarnings.isEmpty() && extraErrors.isEmpty() && extraWeaves.isEmpty() && infosEmpty)) {
			StringBuffer failureReport = new StringBuffer(message);
			failureReport.append("\n");
			if (!expected.isIgnoringInfoMessages()) {
				addMissing(failureReport, "info", missingInfos);
			}
			addMissing(failureReport, "warning", missingWarnings);
			addMissing(failureReport, "error", missingErrors);
			addMissing(failureReport, "fail", missingFails);
			addMissing(failureReport, "weaveInfo", missingWeaves);
			if (!expected.isIgnoringInfoMessages()) {
				addExtra(failureReport, "info", extraInfos);
			}
			addExtra(failureReport, "warning", extraWarnings);
			addExtra(failureReport, "error", extraErrors);
			addExtra(failureReport, "fail", extraFails);
			addExtra(failureReport, "weaveInfo", extraWeaves);
			failureReport.append("\ncommand was: ajc");
			String[] args = result.getArgs();
			for (String arg : args) {
				failureReport.append(" ");
				failureReport.append(arg);
			}
			String report = failureReport.toString();
			System.err.println(failureReport);
			fail(message + "\n" + report);
		}
	}

	/**
	 * Helper method to build a new message list for passing to a MessageSpec.
	 */
	protected List<Message> newMessageList(Message m1) {
		List<Message> ret = new ArrayList<>();
		ret.add(m1);
		return ret;
	}

	/**
	 * Helper method to build a new message list for passing to a MessageSpec.
	 */
	protected List<Message> newMessageList(Message m1, Message m2) {
		List<Message> ret = new ArrayList<>();
		ret.add(m1);
		ret.add(m2);
		return ret;
	}

	/**
	 * Helper method to build a new message list for passing to a MessageSpec.
	 */
	protected List<Message> newMessageList(Message m1, Message m2, Message m3) {
		List<Message> ret = new ArrayList<>();
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
		Collections.addAll(ret, messages);
		return ret;
	}

	/**
	 * Perform a compilation and return the result.
	 *
	 * @param baseDir the base directory relative to which all relative paths and directories in the arguments will be interpreted.
	 * @param args the compiler arguments, as you would specify on the command-line. See the Ajc class for a description of the
	 *        argument processing done in order to run the compilation in a sandbox.
	 * @see org.aspectj.tools.ajc.Ajc
	 */
	public CompilationResult ajc(File baseDir, String[] args) {
		try {
			ajc.setBaseDir(baseDir);
			args = fixupArgs(args);
			return ajc.compile(args);
		} catch (IOException ioEx) {
			fail("IOException thrown during compilation: " + ioEx);
		}
		return null;
	}

	public File getSandboxDirectory() {
		return ajc.getSandboxDirectory();
	}

	/**
	 * Indicate whether or not the sandbox should be emptied before the next compile.
	 *
	 * @see org.aspectj.tools.ajc.Ajc#setShouldEmptySandbox(boolean)
	 */
	public void setShouldEmptySandbox(boolean empty) {
		ajc.setShouldEmptySandbox(empty);
	}

	public RunResult getLastRunResult() {
		return lastRunResult;
	}

	/**
	 * Run the given class (main method), and return the result in a RunResult. The program runs with a classpath containing the
	 * sandbox directory, runtime, testing-client, bridge, and util projects (all used by the Tester class), and any jars in the
	 * sandbox.
	 */
	public RunResult run(String className) {
		return run(className, new String[0], null);
	}

	public RunResult run(String className, String[] args, String classpath) {
		return run(className, null, args, "", "", null, false,false);
	}

	/**
	 * Run the given class, and return the result in a RunResult. The program runs with a classpath containing the sandbox
	 * directory, runtime, testing-client, bridge, and util projects (all used by the Tester class), and any jars in the sandbox.
	 *
	 * @param args the arguments to pass to the program.
	 * @param classpath the execution classpath, the sandbox directory, runtime, testing-client, bridge, and util projects will all
	 *        be appended to the classpath, as will any jars in the sandbox.
	 * @param runSpec
	 */
	public RunResult run(String className, String moduleName, String[] args, String vmargs, final String classpath, String modulepath, boolean useLTW, boolean useFullLTW) {

		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				args[i] = substituteSandbox(args[i]);
			}
		}
		lastRunResult = null;
		StringBuffer cp = new StringBuffer();
		if (classpath != null) {
			// allow replacing this special variable, rather than copying all files to allow tests of jars that don't end in .jar
			cp.append(substituteSandbox(classpath));
			cp.append(File.pathSeparator);
		}
		if (moduleName == null) {
			// When running modules, we want more control so don't try to be helpful by adding all jars
			cp.append(ajc.getSandboxDirectory().getAbsolutePath());
			getAnyJars(ajc.getSandboxDirectory(), cp);
		}
		StringBuffer mp = new StringBuffer();
		if (modulepath != null) {
			mp.append(substituteSandbox(modulepath));
			mp.append(File.pathSeparator);
		}

		URLClassLoader sandboxLoader;
		ClassLoader parentLoader = getClass().getClassLoader().getParent();

		/* Sandbox -> AspectJ -> Extension -> Bootstrap */
		if ( !useFullLTW && useLTW) {
			//			URLClassLoader testLoader = (URLClassLoader) getClass().getClassLoader();
			/*
			 * Create a new AspectJ class loader using the existing test CLASSPATH and any missing Java 5 projects
			 */
			URL[] testUrls = new URL[0];//testLoader.getURLs();
			// What are the URLs on java 8?
			URL[] java5Urls = getURLs(DEFAULT_CLASSPATH_ENTRIES);
			URL[] urls = new URL[testUrls.length + java5Urls.length];
			System.arraycopy(testUrls, 0, urls, 0, testUrls.length);
			System.arraycopy(java5Urls, 0, urls, testUrls.length, java5Urls.length);
			// ClassLoader aspectjLoader = new URLClassLoader(getURLs(DEFAULT_CLASSPATH_ENTRIES),parent);
			ClassLoader aspectjLoader = new URLClassLoader(urls, parentLoader);
			URL[] sandboxUrls = getURLs(cp.toString());
			sandboxLoader = createWeavingClassLoader(sandboxUrls, aspectjLoader);
			// sandboxLoader = createWeavingClassLoader(sandboxUrls,testLoader);
		} else if(useFullLTW  && useLTW) {
			if(vmargs == null){
				vmargs ="";
			}

			File directory = new File (".");
			String absPath = directory.getAbsolutePath();
			String javaagent= absPath+File.separator+".."+File.separator+"aj-build"+File.separator+"dist"+File.separator+"tools"+File.separator+"lib"+File.separator+"aspectjweaver.jar";
			try {
				String command ="java " +vmargs+ " -classpath " + cp +" -javaagent:"+javaagent + " " + className ;

				// Command is executed using ProcessBuilder to allow setting CWD for ajc sandbox compliance
				ProcessBuilder pb = new ProcessBuilder(tokenizeCommand(command));
				pb.directory( new File(ajc.getSandboxDirectory().getAbsolutePath()));
				exec = pb.start();
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
				exec.waitFor();
				lastRunResult = createResultFromBufferReaders(command,stdInput, stdError);
			} catch (Exception e) {
				System.out.println("Error executing full LTW test: " + e);
				e.printStackTrace();
			}
			return lastRunResult;
		} else if (moduleName != null) {
			// CODE FOR RUNNING MODULES
			if(vmargs == null){
				vmargs ="";
			}
			try {
				if (mp.indexOf("$runtimemodule") != -1) {
					mp = mp.replace(mp.indexOf("$runtimemodule"),"$runtimemodule".length(),TestUtil.aspectjrtPath(true).toString());
				}
				if (mp.indexOf("$runtime") != -1) {
					mp = mp.replace(mp.indexOf("$runtime"),"$runtime".length(),TestUtil.aspectjrtPath().toString());
				}
				if (cp.indexOf("aspectjrt")==-1) {
					cp.append(TestUtil.aspectjrtPath().getPath()).append(File.pathSeparator);
				}
				String command = LangUtil.getJavaExecutable().getAbsolutePath() + " " +vmargs+ (cp.length()==0?"":" -classpath " + cp) + " -p "+mp+" --module "+moduleName   ;
				if (Ajc.verbose) {
					System.out.println("Command is "+command);
				}
				// Command is executed using ProcessBuilder to allow setting CWD for ajc sandbox compliance
				ProcessBuilder pb = new ProcessBuilder(tokenizeCommand(command));
				pb.directory( new File(ajc.getSandboxDirectory().getAbsolutePath()));
				exec = pb.start();
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
				exec.waitFor();
				lastRunResult = createResultFromBufferReaders(command,stdInput, stdError);
			} catch (Exception e) {
				System.out.println("Error executing module test: " + e);
				e.printStackTrace();
			}
			return lastRunResult;
		} else if (vmargs!=null && (vmargs.contains("--enable-preview") || vmargs.contains("--add-modules") || vmargs.contains("--limit-modules") || vmargs.contains("--add-reads"))) {
			// If --add-modules supplied, need to fork the test
			try {
				//				if (mp.indexOf("$runtime") != -1) {
				//					mp = mp.replace(mp.indexOf("$runtime"),"$runtime".length(),TestUtil.aspectjrtPath().toString());
				//				}
				if (cp.indexOf("aspectjrt")==-1) {
					cp.append(File.pathSeparator).append(TestUtil.aspectjrtPath().getPath());
				}
				String command = LangUtil.getJavaExecutable().getAbsolutePath() + " " +vmargs+ (cp.length()==0?"":" -classpath " + cp) + " " + className   ;
				if (Ajc.verbose) {
					System.out.println("\nCommand is "+command);
				}
				// Command is executed using ProcessBuilder to allow setting CWD for ajc sandbox compliance
				ProcessBuilder pb = new ProcessBuilder(tokenizeCommand(command));
				pb.directory( new File(ajc.getSandboxDirectory().getAbsolutePath()));
				exec = pb.start();
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
				exec.waitFor();
				lastRunResult = createResultFromBufferReaders(command,stdInput, stdError);
			} catch (Exception e) {
				System.out.println("Error executing module test: " + e);
				e.printStackTrace();
			}
			return lastRunResult;
		} else {
			cp.append(DEFAULT_CLASSPATH_ENTRIES);
			URL[] urls = getURLs(cp.toString());
			sandboxLoader = new URLClassLoader(urls, parentLoader);
		}
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();


		StringBuffer command = new StringBuffer();
		command.append("java -classpath ");
		command.append(cp.toString());
		command.append(" ");
		command.append(className);
		for (String arg : args) {
			command.append(" ");
			command.append(arg);
		}
		//		try {
		//			// Enable the security manager
		//			Policy.setPolicy(new MyPolicy());
		//			SecurityManager sm = new SecurityManager();
		//			System.setSecurityManager(sm);
		//		} catch (SecurityException se) {
		//			// SecurityManager already set
		//		}

		ClassLoader contexClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			try {
				Class<?> testerClass = sandboxLoader.loadClass("org.aspectj.testing.Tester");
				Method setBaseDir = testerClass.getDeclaredMethod("setBASEDIR", new Class[] { File.class });
				setBaseDir.invoke(null, new Object[] { ajc.getSandboxDirectory() });
			} catch (InvocationTargetException itEx) {
				fail("Unable to prepare org.aspectj.testing.Tester for test run: " + itEx.getTargetException());
			} catch (Exception ex) {
				fail("Unable to prepare org.aspectj.testing.Tester for test run: " + ex);
			}
			startCapture(baosErr, baosOut);

			/* Frameworks like XML use context class loader for dynamic loading */
			Thread.currentThread().setContextClassLoader(sandboxLoader);

			Class<?> toRun = sandboxLoader.loadClass(className);
			Method mainMethod = toRun.getMethod("main", new Class[] { String[].class });

			mainMethod.invoke(null, new Object[] { args });
		} catch (ClassNotFoundException cnf) {
			fail("Can't find class: " + className);
		} catch (NoSuchMethodException nsm) {
			fail(className + " does not have a main method");
		} catch (IllegalAccessException illEx) {
			fail("main method in class " + className + " is not public");
		} catch (InvocationTargetException invTgt) {
			// the main method threw an exception...
			fail("Exception thrown by " + className + ".main(String[]) :" + invTgt.getTargetException());
		} finally {

			//			try {
			//				// Enable the security manager
			//				SecurityManager sm = new SecurityManager();
			//				System.setSecurityManager(null);
			//			} catch (SecurityException se) {
			//				se.printStackTrace();
			//				// SecurityManager already set
			//			}
			Thread.currentThread().setContextClassLoader(contexClassLoader);
			stopCapture(baosErr, baosOut);
			lastRunResult = new RunResult(command.toString(), new String(baosOut.toByteArray()), new String(baosErr.toByteArray()));
		}
		return lastRunResult;
	}

	private List<String >tokenizeCommand(String command) {
		StringTokenizer st = new StringTokenizer(command," ", false);
		List<String> arguments = new ArrayList<>();
		while(st.hasMoreElements()){
			String nextToken =st.nextToken();
			arguments.add(nextToken);
		}

		return arguments;
	}

	private RunResult createResultFromBufferReaders(String command,
			BufferedReader stdInput, BufferedReader stdError) throws IOException {
		String line = "";
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();

		PrintWriter stdOutWriter = new PrintWriter(baosOut);
		PrintWriter stdErrWriter = new PrintWriter(baosErr);

		if (Ajc.verbose) {
			System.out.println();
		}
		while ((line = stdInput.readLine()) != null) {
			stdOutWriter.println(line);
			if (Ajc.verbose) {
				System.out.println(line);
			}
		}
		stdOutWriter.flush();
		while ((line = stdError.readLine()) != null) {
			stdErrWriter.println(line);
			if (Ajc.verbose) {
				System.err.println(line);
			}
		}
		stdErrWriter.flush();

		baosOut.close();
		baosErr.close();

		return new RunResult(command.toString(), new String(baosOut.toByteArray()), new String(baosErr.toByteArray()));
	}

	//	static class MyPolicy extends Policy {
	//
	//		@Override
	//		public boolean implies(ProtectionDomain domain, Permission permission) {
	//			// if (permission != SecurityConstants.GET_POLICY_PERMISSION) {
	//			// // System.out.println(domain + " " + permission.getName());
	//			// System.out.println(permission.getName());
	//			// }
	//			// if (true) {
	//			// return true;
	//			// }
	//			if (permission instanceof PropertyPermission) {
	//				return true;
	//			}
	//			if (permission instanceof RuntimePermission) {
	//				return true;
	//			}
	//			if (permission instanceof FilePermission) {
	//				// System.out.println(permission);
	//				return true;
	//			}
	//			if (permission instanceof ReflectPermission) {
	//				return true;
	//			}
	//			// System.out.println(permission);
	//			return super.implies(domain, permission);
	//			// return true;
	//		}
	//	}

	/*
	 * Must create weaving class loader reflectively using new parent so we don't have a reference to a World loaded from CLASSPATH
	 * which won't be able to resolve Java 5 specific extensions and may cause ClassCastExceptions
	 */
	private URLClassLoader createWeavingClassLoader(URL[] urls, ClassLoader parent) {
		URLClassLoader loader = null;

		try {
			Class loaderClazz = Class.forName("org.aspectj.weaver.loadtime.WeavingURLClassLoader", false, parent);
			Class[] parameterTypes = new Class[] { urls.getClass(), ClassLoader.class };
			Object[] parameters = new Object[] { urls, parent };
			Constructor constructor = loaderClazz.getConstructor(parameterTypes);
			loader = (URLClassLoader) constructor.newInstance(parameters);
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
			fail("Cannot create weaving class loader: " + ex.getTargetException());
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Cannot create weaving class loader: " + ex.toString());
		}

		return loader;
	}

	private URL[] getURLs(String classpath) {
		StringTokenizer strTok = new StringTokenizer(classpath, File.pathSeparator);
		URL[] urls = new URL[strTok.countTokens()];
		try {
			for (int i = 0; i < urls.length; i++) {
				urls[i] = new File(strTok.nextToken()).getCanonicalFile().toURL();
			}
		} catch (Exception malEx) {
			fail("Bad classpath specification: " + classpath);
		}

		return urls;
	}

	private String substituteSandbox(String path) {
		// the longhand form of the non 1.3 API: path.replace("$sandbox", ajc.getSandboxDirectory().getAbsolutePath());
		while (path.contains("$sandbox")) {
			int pos = path.indexOf("$sandbox");
			String firstbit = path.substring(0, pos);
			String endbit = path.substring(pos + 8);
			path = firstbit + ajc.getSandboxDirectory().getAbsolutePath() + endbit;
		}
		return path;
	}

	/**
	 * Any central pre-processing of args. This supplies aspectjrt.jar if available and classpath not set.
	 *
	 * @param args the String[] args to fix up
	 * @return the String[] args to use
	 */
	protected String[] fixupArgs(String[] args) {
		if (null == args) {
			return null;
		}
		int cpIndex = -1;
		boolean hasruntime = false;
		for (int i = 0; i < args.length - 1; i++) {
			args[i] = adaptToPlatform(args[i]);
			if ("-classpath".equals(args[i])) {
				cpIndex = i;
				args[i + 1] = substituteSandbox(args[i + 1]);
				String next = args[i + 1];
				hasruntime = ((null != next) && (next.contains("aspectjrt.jar")));
			} else if ("-p".equals(args[i]) || "--module-path".equals(args[i])) {
				args[i + 1] = substituteSandbox(args[i + 1]);
			}
		}
		if (-1 == cpIndex) {
			String[] newargs = new String[args.length + 2];
			newargs[0] = "-classpath";
			newargs[1] = TestUtil.aspectjrtPath(false).getPath();
			System.arraycopy(args, 0, newargs, 2, args.length);
			args = newargs;
			cpIndex = 1;
		} else {
			if (!hasruntime) {
				cpIndex++;
				String[] newargs = new String[args.length];
				System.arraycopy(args, 0, newargs, 0, args.length);
				newargs[cpIndex] = args[cpIndex] + File.pathSeparator + TestUtil.aspectjrtPath().getPath();
				args = newargs;
			}
		}
		boolean needsJRTFS = LangUtil.is19VMOrGreater();
		if (needsJRTFS) {
			if (!args[cpIndex].contains(LangUtil.JRT_FS)) {
				String jrtfsPath = LangUtil.getJrtFsFilePath();
				args[cpIndex] = jrtfsPath + File.pathSeparator + args[cpIndex];
			}
		}
		return args;
	}

	private String adaptToPlatform(String s) {
		String ret = s.replace(';', File.pathSeparatorChar);
		// ret = ret.replace(':',File.pathSeparatorChar);
		return ret;
	}

	private <T> List<T> copyAll(List<T> in) {
		if (in == Collections.EMPTY_LIST)
			return in;

		List<T> out = new ArrayList<>();
		for (T t : in) {
			out.add(t);
		}
		return out;
	}

	/**
	 * Compare the set of expected messages against the set of actual messages, leaving in missingElements the set of messages that
	 * were expected but did not occur, and in extraElements the set of messages that occured but were not excpected
	 *
	 * @param expected the expected messages
	 * @param actual the actual messages
	 * @param missingElements the missing messages, when passed in must contain all of the expected messages
	 * @param extraElements the additional messages, when passed in must contain all of the actual messages
	 */
	private void compare(List<AjcTestCase.Message> expected, List<IMessage> actual, List<AjcTestCase.Message> missingElements, List<IMessage> extraElements) {
		for (Message expectedMessage: expected) {
			for (IMessage actualMessage: actual) {
				if (expectedMessage.matches(actualMessage)) {
					if (expectedMessage.careAboutOtherMessages) {
						missingElements.remove(expectedMessage);
						extraElements.remove(actualMessage);
					}
					else {
						missingElements.clear();
						extraElements.clear();
					}
				}
			}
		}
	}

	private void addMissing(StringBuffer buff, String type, List<AjcTestCase.Message> messages) {
		if (!messages.isEmpty()) {
			buff.append("Missing expected ");
			buff.append(type);
			buff.append(" messages:\n");
			for (Message message : messages) {
				buff.append("\t");
				buff.append(message.toString());
				buff.append("\n");
			}
		}
	}

	private void addExtra(StringBuffer buff, String type, List messages) {
		if (!messages.isEmpty()) {
			buff.append("Unexpected ");
			buff.append(type);
			buff.append(" messages:\n");
			for (Object message : messages) {
				buff.append("\t");
				buff.append(message.toString());
				buff.append("\n");
			}
		}
	}

	// add any jars in the directory to the classpath
	private void getAnyJars(File dir, StringBuffer buff) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.getName().endsWith(".jar")) {
				buff.append(File.pathSeparator);
				buff.append(file.getAbsolutePath());
			} else if (file.isDirectory()) {
				getAnyJars(file, buff);
			}
		}
	}

	private static void startCapture(OutputStream errOS, OutputStream outOS) {
		delegatingErr.add(errOS);
		delegatingOut.add(outOS);

		delegatingErr.setVerbose(DEFAULT_ERR_VERBOSE);
		delegatingOut.setVerbose(DEFAULT_OUT_VERBOSE);
	}

	private static void stopCapture(OutputStream errOS, OutputStream outOS) {
		delegatingErr.setVerbose(true);
		delegatingOut.setVerbose(true);

		delegatingErr.remove(errOS);
		delegatingOut.remove(outOS);
	}

	private static boolean getBoolean(String name, boolean def) {
		String defaultValue = String.valueOf(def);
		String value = System.getProperty(name, defaultValue);
		return Boolean.valueOf(value);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ajc = new Ajc();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		// ajc = null;
	}

	static {
		// new RuntimeException("*** AjcTestCase.<clinit>()").printStackTrace();
		delegatingErr = new DelegatingOutputStream(err);
		System.setErr(new PrintStream(delegatingErr));
		delegatingOut = new DelegatingOutputStream(out);
		System.setOut(new PrintStream(delegatingOut));
	}


}
