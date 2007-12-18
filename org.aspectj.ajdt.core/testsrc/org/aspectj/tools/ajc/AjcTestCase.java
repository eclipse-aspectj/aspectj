/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.tools.ajc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.testing.util.TestUtil;

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
	
	public static final String DEFAULT_CLASSPATH_ENTRIES = 
		File.pathSeparator + ".." + File.separator + "bridge" + File.separator + "bin"
		+ File.pathSeparator + ".." + File.separator + "util" + File.separator + "bin"
        + File.pathSeparator+ ".."+File.separator+"loadtime"+File.separator+"bin" 
        + File.pathSeparator+ ".."+File.separator+"weaver"+File.separator+"bin" 
        + File.pathSeparator+ ".."+File.separator+"weaver5"+File.separator+"bin" 
        + File.pathSeparator+ ".."+File.separator+"asm"+File.separator+"bin" 
		+ File.pathSeparator + ".." + File.separator + "testing-client" + File.separator + "bin"
        + File.pathSeparator + ".." + File.separator + "runtime" + File.separator + "bin"
		+ File.pathSeparator + ".." + File.separator + "aspectj5rt" + File.separator + "bin"
		+ File.pathSeparator+ ".."+File.separator+"lib"+File.separator+"junit"+File.separator+"junit.jar"
        + File.pathSeparator+ ".."+File.separator+"lib"+File.separator+"bcel"+File.separator+"bcel.jar" 
        
        // When the build machine executes the tests, it is using code built into jars rather than code build into
        // bin directories.  This means for the necessary types to be found we have to put these jars on the classpath:
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"bridge.jar"
        + File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"util.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"loadtime.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"weaver.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"weaver5.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"asm.jar"
		+ File.pathSeparator+".."+File.separator+"lib"     +File.separator+"test"+File.separator+"testing-client.jar"
		// hmmm, this next one should perhaps point to an aj-build jar...
		+ File.pathSeparator+".."+File.separator+"lib"     +File.separator+"test"+File.separator+"aspectjrt.jar"
        ;
	
	public static final String JAVA5_CLASSPATH_ENTRIES = 
		File.pathSeparator + ".." + File.separator + "aspectj5rt" + File.separator + "bin"
        + File.pathSeparator+ ".."+File.separator+"loadtime5"+File.separator+"bin" 
        + File.pathSeparator+ ".."+File.separator+"weaver5"+File.separator+"bin" 

		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"aspectj5rt.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"loadtime5.jar"
		+ File.pathSeparator+".."+File.separator+"aj-build"+File.separator+"jars"+File.separator+"weaver5.jar";
        
	/*
	 * Save reference to real stderr and stdout before starting redirection
	 */
	public final static PrintStream err = System.err;
	public final static PrintStream out = System.out;
	private final static DelegatingOutputStream delegatingErr;
	private final static DelegatingOutputStream delegatingOut;
	public final static boolean DEFAULT_VERBOSE = getBoolean("org.aspectj.tools.ajc.AjcTestCase.verbose",true); 
	public final static boolean DEFAULT_ERR_VERBOSE = getBoolean("org.aspectj.tools.ajc.AjcTestCase.verbose.err",DEFAULT_VERBOSE); 
	public final static boolean DEFAULT_OUT_VERBOSE = getBoolean("org.aspectj.tools.ajc.AjcTestCase.verbose.out",DEFAULT_VERBOSE); 

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
            args = fixupArgs(args);
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
    public void testNothingForAntJUnit() {}
    
	/**
	 * Run the given class (main method), and return the result in a RunResult. The program runs with
	 * a classpath containing the sandbox directory, runtime, testing-client, bridge, and
	 * util projects (all used by the Tester class), and any jars in the sandbox.
	 */
	public RunResult run(String className){
		return run(className,new String[0],null);
	}

	public RunResult run(String className, String[] args, String classpath)  {
		return run(className,args,null,false);
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
	public RunResult run(String className, String[] args, final String classpath, boolean useLTW)  {
		lastRunResult = null;
		StringBuffer cp = new StringBuffer();
		if (classpath != null) {
			// allow replacing this special variable, rather than copying all files to allow tests of jars that don't end in .jar
			cp.append(substituteSandbox(classpath));
			cp.append(File.pathSeparator);
		}
		cp.append(ajc.getSandboxDirectory().getAbsolutePath());
		getAnyJars(ajc.getSandboxDirectory(),cp);
		
		URLClassLoader sandboxLoader;
		URLClassLoader testLoader = (URLClassLoader)getClass().getClassLoader();
		ClassLoader parentLoader = testLoader.getParent();
		
		/* Sandbox -> AspectJ -> Extension -> Bootstrap */
		if (useLTW) {
			
			/*
			 * Create a new AspectJ class loader using the existing test CLASSPATH 
			 * and any missing Java 5 projects 
			 */
			URL[] testUrls = testLoader.getURLs();
			URL[] java5Urls = getURLs(JAVA5_CLASSPATH_ENTRIES);
			URL[] urls = new URL[testUrls.length + java5Urls.length];
			System.arraycopy(testUrls,0,urls,0,testUrls.length);
			System.arraycopy(java5Urls,0,urls,testUrls.length,java5Urls.length);
//			ClassLoader aspectjLoader = new URLClassLoader(getURLs(DEFAULT_CLASSPATH_ENTRIES),parent);
			ClassLoader aspectjLoader = new URLClassLoader(urls,parentLoader);
			URL[] sandboxUrls = getURLs(cp.toString());
			sandboxLoader = createWeavingClassLoader(sandboxUrls,aspectjLoader);
//			sandboxLoader = createWeavingClassLoader(sandboxUrls,testLoader);
		}

		/* Sandbox + AspectJ -> Extension -> Bootstrap */
		else {
			cp.append(DEFAULT_CLASSPATH_ENTRIES);
			URL[] urls = getURLs(cp.toString());
			sandboxLoader = new URLClassLoader(urls,parentLoader);
		}

		StringBuffer command = new StringBuffer("java -classpath ");
		command.append(cp.toString());
		command.append(" ");
		command.append(className);
		for (int i = 0; i < args.length; i++) {
			command.append(" ");
			command.append(args[i]);
		}

		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		ClassLoader contexClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			try {
				Class testerClass = sandboxLoader.loadClass("org.aspectj.testing.Tester");
				Method setBaseDir = testerClass.getDeclaredMethod("setBASEDIR",new Class[] {File.class});
				setBaseDir.invoke(null,new Object[] {ajc.getSandboxDirectory()});
			} catch (InvocationTargetException itEx) {
				fail ("Unable to prepare org.aspectj.testing.Tester for test run: " + itEx.getTargetException());
			} catch (Exception ex) {
				fail ("Unable to prepare org.aspectj.testing.Tester for test run: " + ex);
			}
			startCapture(baosErr,baosOut);
			
			/* Frameworks like XML use context class loader for dynamic loading */
			Thread.currentThread().setContextClassLoader(sandboxLoader);
			
			Class toRun = sandboxLoader.loadClass(className);
			Method mainMethod = toRun.getMethod("main",new Class[] {String[].class});
			mainMethod.invoke(null,new Object[] {args});
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
			Thread.currentThread().setContextClassLoader(contexClassLoader);
			stopCapture(baosErr,baosOut);
			lastRunResult = new RunResult(command.toString(),new String(baosOut.toByteArray()),new String(baosErr.toByteArray()));
		}
		return lastRunResult;
	}
	
	/* Must create weaving class loader reflectively using new parent so we 
	 * don't have a reference to a World loaded from CLASSPATH which won't
	 * be able to resolve Java 5 specific extensions and may cause
	 * ClassCastExceptions
	 */  
	private URLClassLoader createWeavingClassLoader (URL[] urls, ClassLoader parent) {
		URLClassLoader loader = null;
		
		try {
			Class loaderClazz = Class.forName("org.aspectj.weaver.loadtime.WeavingURLClassLoader",false,parent);
			Class[] parameterTypes = new Class[] { urls.getClass(), ClassLoader.class };
			Object[] parameters = new Object[] { urls, parent };
			Constructor constructor = loaderClazz.getConstructor(parameterTypes);
			loader = (URLClassLoader)constructor.newInstance(parameters);
		}
		catch (InvocationTargetException ex) {
			ex.printStackTrace();
			fail("Cannot create weaving class loader: " + ex.getTargetException());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Cannot create weaving class loader: " + ex.toString());
		}
		
		return loader;
	}
	
	private URL[] getURLs (String classpath) {
		StringTokenizer strTok = new StringTokenizer(classpath,File.pathSeparator);
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

	private String substituteSandbox(String classpath) {
		// the longhand form of the non 1.3 API: classpath.replace("$sandbox", ajc.getSandboxDirectory().getAbsolutePath());
		while (classpath.indexOf("$sandbox")!=-1) {
			int pos = classpath.indexOf("$sandbox");
			String firstbit = classpath.substring(0,pos);
			String endbit = classpath.substring(pos+8);
			classpath = firstbit+ ajc.getSandboxDirectory().getAbsolutePath()+endbit;
		}
		return classpath;
	}
    
    /**
     * Any central pre-processing of args.
     * This supplies aspectjrt.jar if available and classpath not set.
     * @param args the String[] args to fix up
     * @return the String[] args to use
     */
	protected String[] fixupArgs(String[] args) {
        if (null == args) {
            return null;
        }
        int cpIndex = -1;
        boolean hasruntime = false;      
        for (int i = 0; i < args.length-1; i++) {
        		args[i] = adaptToPlatform(args[i]);
            if ("-classpath".equals(args[i])) {
                cpIndex = i;
                args[i+1] = substituteSandbox(args[i+1]);
                String next = args[i+1];
                hasruntime = ((null != next) 
                        && (-1 != next.indexOf("aspectjrt.jar")));
            }
        }
        if (-1 == cpIndex) {
            String[] newargs = new String[args.length + 2];
            newargs[0] = "-classpath";
            newargs[1] = TestUtil.aspectjrtPath().getPath();
            System.arraycopy(args, 0, newargs, 2, args.length);
            args = newargs;
        } else {
            if (!hasruntime) {
                cpIndex++;
                String[] newargs = new String[args.length];
                System.arraycopy(args, 0, newargs, 0, args.length);
                newargs[cpIndex] = args[cpIndex] + File.pathSeparator
                + TestUtil.aspectjrtPath().getPath();
                args = newargs;
            }
        }
        return args;
    }
	
	private String adaptToPlatform(String s) {
		String ret = s.replace(';',File.pathSeparatorChar);
		//ret = ret.replace(':',File.pathSeparatorChar);
		return ret;
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
	
	private static void startCapture (OutputStream errOS, OutputStream outOS) {
		delegatingErr.add(errOS);
		delegatingOut.add(outOS);

		delegatingErr.setVerbose(DEFAULT_ERR_VERBOSE);
		delegatingOut.setVerbose(DEFAULT_OUT_VERBOSE);
	}
	
	private static void stopCapture (OutputStream errOS, OutputStream outOS) {
		delegatingErr.setVerbose(true);
		delegatingOut.setVerbose(true);

		delegatingErr.remove(errOS);
		delegatingOut.remove(outOS);
	}
	
	private static boolean getBoolean (String name, boolean def) {
		String defaultValue = String.valueOf(def);
		String value = System.getProperty(name,defaultValue);
		return Boolean.valueOf(value).booleanValue();
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
        //ajc = null;
	}
	
	static {
//		new RuntimeException("*** AjcTestCase.<clinit>()").printStackTrace();
		delegatingErr = new DelegatingOutputStream(err);
		System.setErr(new PrintStream(delegatingErr));
		delegatingOut = new DelegatingOutputStream(out);
		System.setOut(new PrintStream(delegatingOut));
	}
}
