/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.Tester;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.WrappedRunIterator;
import org.aspectj.testing.util.TestClassLoader;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.testing.xml.SoftMessage;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.loadtime.WeavingURLClassLoader;

/**
 * Run a class in this VM using reflection.
 * Forked mode supported, but through system properties:
 * - javarun.fork: anything to enable forking
 * - javarun.java: path to java executable (optional)
 * - javarun.java.home: JAVA_HOME for java (optional)
 *   (normally requires javarun.java)
 * - javarun.classpath: a prefix to the run classpath (optional)
 */
public class JavaRun implements IAjcRun {

	private static void appendClasspath(StringBuffer cp, Object[] entries) {
		if (!LangUtil.isEmpty(entries)) {
			for (Object entry : entries) {
				if (entry instanceof String) {
					cp.append((String) entry);
					cp.append(File.pathSeparator);
				} else if (entry instanceof File) {
					String s = FileUtil.getBestPath((File) entry);
					if (null != s) {
						cp.append(s);
						cp.append(File.pathSeparator);
					}
				}
			}
		}
	}

	Spec spec;
	private Sandbox sandbox;

	/** programmatic initialization per spec */
	public JavaRun(Spec spec) {
		this.spec = spec;
	}
	// XXX init(Spec)

	/**
	 * This checks the spec for a class name
	 * and checks the sandbox for a readable test source directory,
	 * a writable run dir, and (non-null, possibly-empty) lists
	 * of readable classpath dirs and jars,
	 * and, if fork is enabled, that java can be read.
	 * @return true if all checks pass
	 * @see org.aspectj.testing.harness.bridge.AjcTest.IAjcRun#setup(File, File)
	 */
	@Override
	public boolean setupAjcRun(Sandbox sandbox, Validator validator) {
		this.sandbox = sandbox;
		sandbox.javaRunInit(this);
		return (validator.nullcheck(spec.className, "class name")
				&& validator.nullcheck(sandbox, "sandbox")
				&& validator.canReadDir(sandbox.getTestBaseSrcDir(this), "testBaseSrc dir")
				&& validator.canWriteDir(sandbox.runDir, "run dir")
				&& validator.canReadFiles(sandbox.getClasspathJars(true, this), "classpath jars")
				&& validator.canReadDirs(sandbox.getClasspathDirectories(true, this, true), "classpath dirs")
				&& (!spec.forkSpec.fork
						|| validator.canRead(spec.forkSpec.java, "java"))
				);

	}

	/** caller must record any exceptions */
	@Override
	public boolean run(IRunStatus status)
			throws IllegalAccessException,
			InvocationTargetException,
			ClassNotFoundException,
			NoSuchMethodException {
		boolean completedNormally = false;
		boolean passed = false;
		if (!LangUtil.isEmpty(spec.dirChanges)) {
			MessageUtil.info(status, "XXX dirChanges not implemented in JavaRun");
		}
		try {
			final boolean readable = true;
			File[] libs = sandbox.getClasspathJars(readable, this);
			boolean includeClassesDir = true;
			File[] dirs = sandbox.getClasspathDirectories(readable, this, includeClassesDir);
			completedNormally = (spec.forkSpec.fork)
					? runInOtherVM(status, libs, dirs)
							: runInSameVM(status, libs, dirs);
					passed = completedNormally;
		} finally {
			if (!passed  || !status.runResult()) {
				MessageUtil.info(status, spec.toLongString());
				MessageUtil.info(status, "sandbox: " + sandbox);
			}
		}
		return passed;
	}
	protected boolean runInSameVM(IRunStatus status, File[] libs, File[] dirs) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		ClassLoader loader = null;
		boolean completedNormally = false;
		boolean passed = false;
		ByteArrayOutputStream outSnoop = null;
		PrintStream oldOut = null;
		ByteArrayOutputStream errSnoop = null;
		PrintStream oldErr = null;
		if (spec.outStreamIsError) {
			outSnoop = new ByteArrayOutputStream();
			oldOut = System.out;
			System.setOut(new PrintStream(outSnoop, true));
		}
		if (spec.errStreamIsError) {
			errSnoop = new ByteArrayOutputStream();
			oldErr = System.err;
			System.setErr(new PrintStream(errSnoop, true));
		}
		Class targetClass = null;
		try {
			final URL[] clAndLibs;
			{
				File[] files = sandbox.findFiles(spec.classpath);
				URL[] clURLs = FileUtil.getFileURLs(files);
				URL[] libURLs = FileUtil.getFileURLs(libs);
				clAndLibs = new URL[clURLs.length + libURLs.length];
				System.arraycopy(clURLs, 0, clAndLibs , 0, clURLs.length);
				System.arraycopy(libURLs, 0, clAndLibs, clURLs.length, libURLs.length);
			}
			if (!spec.isLTW()) {
				loader = new TestClassLoader(clAndLibs, dirs);
			} else {
				final URL[] aspectURLs;
				{
					File[] files = sandbox.findFiles(spec.aspectpath);
					aspectURLs = FileUtil.getFileURLs(files);
				}
				ArrayList classpath = new ArrayList(Arrays.asList(aspectURLs));
				final URL[] classURLs;
				{
					classpath.addAll(Arrays.asList(clAndLibs));
					URL[] urls = FileUtil.getFileURLs(dirs);
					classpath.addAll(Arrays.asList(urls));
					classpath.add(FileUtil.getFileURL(Globals.F_aspectjrt_jar));
					classpath.add(FileUtil.getFileURL(Globals.F_testingclient_jar));
					classURLs = (URL[]) classpath.toArray(new URL[0]);
				}

				ClassLoader parent = JavaRun.class.getClassLoader();
				loader = new WeavingURLClassLoader(classURLs, aspectURLs, parent);
			}
			// make the following load test optional
			// Class testAspect = loader.loadClass("org.aspectj.lang.JoinPoint");
			targetClass = loader.loadClass(spec.className);
			Method main = targetClass.getMethod("main", Globals.MAIN_PARM_TYPES);
			setupTester(sandbox.getTestBaseSrcDir(this), loader, status);
			RunSecurityManager.ME.setJavaRunThread(this);
			main.invoke(null, new Object[] { spec.getOptionsArray() });
			completedNormally = true;
			boolean snoopFailure =
					((null != errSnoop) && 0 < errSnoop.size())
					|| ((null != outSnoop) && 0 < outSnoop.size());
			passed = !snoopFailure && (null == spec.expectedException);
		} catch (AbortException e) {
			if (expectedException(e)) {
				passed = true;
			} else {
				throw e;
			}
		} catch (InvocationTargetException e) {
			// this and following clauses catch ExitCalledException
			Throwable thrown = LangUtil.unwrapException(e);
			if (null == thrown) {
				throw e;
			}
			if (thrown instanceof RunSecurityManager.ExitCalledException) {
				int i = ((RunSecurityManager.ExitCalledException) thrown).exitCode;
				status.finish(i);
			} else if (thrown instanceof RunSecurityManager.AwtUsedException) {
				MessageUtil.fail(status, "test code should not use the AWT event queue");
				throw (RunSecurityManager.AwtUsedException) thrown;
				// same as: status.thrown(thrown);
			} else if (expectedException(thrown)) {
				passed = true;
			} else if (thrown instanceof RuntimeException) {
				throw (RuntimeException) thrown;
			} else if (thrown instanceof Error) {
				throw (Error) thrown;
			} else {
				throw e;
			}
		} catch (RunSecurityManager.ExitCalledException e) {
			// XXX need to update run validator (a) to accept null result or (b) to require zero result, and set 0 if completed normally
			status.finish(e.exitCode);
		} catch (ClassNotFoundException e) {
			String[] classes = FileUtil.listFiles(sandbox.classesDir);
			MessageUtil.info(status, "sandbox.classes: " + Arrays.asList(classes));
			MessageUtil.fail(status, null, e);
		} finally {
			if (null != oldOut) {
				System.setOut(oldOut);
			}
			if (null != oldErr) {
				System.setErr(oldErr);
			}
			RunSecurityManager.ME.releaseJavaRunThread(this);
			if (!completedNormally) {
				MessageUtil.info(status, "targetClass: " + targetClass);
				MessageUtil.info(status, "loader: " + loader);
			}
		}
		return passed;
	}

	/**
	 * Run in another VM by grabbing Java, bootclasspath, classpath, etc.
	 * This assumes any exception or output to System.err is a failure,
	 * and any normal completion is a pass.
	 * @param status
	 * @param libs
	 * @param dirs
	 * @return
	 */
	protected boolean runInOtherVM(IRunStatus status, File[] libs, File[] dirs) {
		// assert spec.fork || !LangUtil.isEmpty(spec.aspectpath);
		ArrayList<String> cmd = new ArrayList<>();
		cmd.add(FileUtil.getBestPath(spec.forkSpec.java));
		if (!LangUtil.isEmpty(spec.forkSpec.vmargs)) {
			cmd.addAll(Arrays.asList(spec.forkSpec.vmargs));
		}
		final String classpath;
		{
			StringBuffer cp = new StringBuffer();
			appendClasspath(cp, spec.forkSpec.bootclasspath);
			appendClasspath(cp, dirs);
			appendClasspath(cp, libs);
			File[] classpathFiles = sandbox.findFiles(spec.classpath);
			int cpLength = (null == classpathFiles ? 0 : classpathFiles.length);
			int spLength = (null == spec.classpath ? 0 : spec.classpath.length);
			if (cpLength != spLength) {
				throw new Error("unable to find " + Arrays.asList(spec.classpath)
				+ " got " + Arrays.asList(classpathFiles));
			}
			appendClasspath(cp, classpathFiles);
			File[] stdlibs = {Globals.F_aspectjrt_jar, Globals.F_testingclient_jar};
			appendClasspath(cp, stdlibs);
			classpath = cp.toString();
		}
		if (!spec.isLTW()) {
			cmd.add("-classpath");
			cmd.add(classpath);
		} else {
			// verify 1.4 or above, assuming same vm as running this
			if (!Globals.supportsJava("1.4")) {
				throw new Error("load-time weaving test requires Java 1.4+");
			}
			cmd.add("-Djava.system.class.loader=org.aspectj.weaver.WeavingURLClassLoader");
			// assume harness VM classpath has WeavingURLClassLoader (but not others)
			cmd.add("-classpath");
			cmd.add(System.getProperty("java.class.path"));

			File[] aspectJars = sandbox.findFiles(spec.aspectpath);
			if (aspectJars.length != spec.aspectpath.length) {
				throw new Error("unable to find " + Arrays.asList(spec.aspectpath));
			}
			StringBuffer cp = new StringBuffer();
			appendClasspath(cp, aspectJars);
			cmd.add("-Daj.aspect.path=" + cp.toString());
			cp.append(classpath); // appendClasspath writes trailing delimiter
			cmd.add("-Daj.class.path=" + cp.toString());
		}
		cmd.add(spec.className);
		cmd.addAll(spec.options);
		String[] command = cmd.toArray(new String[0]);

		final IMessageHandler handler = status;
		// setup to run asynchronously, pipe streams through, and report errors
		class DoneFlag {
			boolean done;
			boolean failed;
			int code;
		}
		final StringBuffer commandLabel = new StringBuffer();
		final DoneFlag doneFlag = new DoneFlag();
		LangUtil.ProcessController controller
		= new LangUtil.ProcessController() {
			@Override
			protected void doCompleting(Thrown ex, int result) {
				if (!ex.thrown && (0 == result)) {
					doneFlag.done = true;
					return; // no errors
				}
				// handle errors
				String context = spec.className
						+ " command \""
						+ commandLabel
						+ "\"";
				if (null != ex.fromProcess) {
					if (!expectedException(ex.fromProcess)) {
						String m = "Exception running " + context;
						MessageUtil.abort(handler, m, ex.fromProcess);
						doneFlag.failed = true;
					}
				} else if (0 != result) {
					doneFlag.code = result;
				}
				if (null != ex.fromInPipe) {
					String m = "Error processing input pipe for " + context;
					MessageUtil.abort(handler, m, ex.fromInPipe);
					doneFlag.failed = true;
				}
				if (null != ex.fromOutPipe) {
					String m = "Error processing output pipe for " + context;
					MessageUtil.abort(handler, m, ex.fromOutPipe);
					doneFlag.failed = true;
				}
				if (null != ex.fromErrPipe) {
					String m = "Error processing error pipe for " + context;
					MessageUtil.abort(handler, m, ex.fromErrPipe);
					doneFlag.failed = true;
				}
				doneFlag.done = true;
			}
		};
		controller.init(command, spec.className);
		if (null != spec.forkSpec.javaHome) {
			controller.setEnvp(new String[] {"JAVA_HOME=" + spec.forkSpec.javaHome});
		}
		commandLabel.append(Arrays.asList(controller.getCommand()).toString());
		final ByteArrayOutputStream errSnoop
		= new ByteArrayOutputStream();
		final ByteArrayOutputStream outSnoop
		= new ByteArrayOutputStream();
		controller.setErrSnoop(errSnoop);
		controller.setOutSnoop(outSnoop);
		controller.start();
		// give it 3 minutes...
		long maxTime = System.currentTimeMillis() + 3 * 60 * 1000;
		boolean waitingForStop = false;
		while (!doneFlag.done) {
			if (maxTime < System.currentTimeMillis()) {
				if (waitingForStop) { // hit second timeout - bail
					break;
				}
				MessageUtil.fail(status, "timeout waiting for process");
				doneFlag.failed = true;
				controller.stop();
				// wait 1 minute to evaluate results of stopping
				waitingForStop = true;
				maxTime = System.currentTimeMillis() + 1 * 60 * 1000;
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		boolean foundException = false;
		if (0 < errSnoop.size()) {
			if (expectedException(errSnoop)) {
				foundException = true;
			} else if (spec.errStreamIsError) {
				MessageUtil.error(handler, errSnoop.toString());
				if (!doneFlag.failed) {
					doneFlag.failed = true;
				}
			} else {
				MessageUtil.info(handler, "Error stream: " + errSnoop.toString());
			}
		}
		if (0 < outSnoop.size()) {
			if (expectedException(outSnoop)) {
				foundException = true;
			} else if (spec.outStreamIsError) {
				MessageUtil.error(handler, outSnoop.toString());
				if (!doneFlag.failed) {
					doneFlag.failed = true;
				}
			} else {
				MessageUtil.info(handler, "Output stream: " + outSnoop.toString());
			}
		}
		if (!foundException) {
			if (null != spec.expectedException) {
				String m = " expected exception " + spec.expectedException;
				MessageUtil.fail(handler, m);
				doneFlag.failed = true;
			} else if (0 != doneFlag.code) {
				String m = doneFlag.code + " result from " + commandLabel;
				MessageUtil.fail(handler, m);
				doneFlag.failed = true;
			}
		}
		if (doneFlag.failed) {
			MessageUtil.info(handler, "other-vm command-line: " + commandLabel);
		}
		return !doneFlag.failed;
	}

	protected boolean expectedException(Throwable thrown) {
		if (null != spec.expectedException) {
			String cname = thrown.getClass().getName();
			if (cname.contains(spec.expectedException)) {
				return true; // caller sets value for returns normally
			}
		}
		return false;
	}

	protected boolean expectedException(ByteArrayOutputStream bout) {
		return ((null != spec.expectedException)
				&& (bout.toString().contains(spec.expectedException)));
	}

	/**
	 * Clear (static) testing state and setup base directory,
	 * unless spec.skipTesting.
	 * @return null if successful, error message otherwise
	 */
	protected void setupTester(File baseDir, ClassLoader loader, IMessageHandler handler) {
		if (null == loader) {
			setupTester(baseDir, handler);
			return;
		}
		File baseDirSet = null;
		try {
			if (!spec.skipTester) {
				Class tc = loader.loadClass("org.aspectj.testing.Tester");
				// Tester.clear();
				Method m = tc.getMethod("clear", new Class[0]);
				m.invoke(null, new Object[0]);
				// Tester.setMessageHandler(handler);
				m = tc.getMethod("setMessageHandler", new Class[] {IMessageHandler.class});
				m.invoke(null, new Object[] { handler});

				//Tester.setBASEDIR(baseDir);
				m = tc.getMethod("setBASEDIR", new Class[] {File.class});
				m.invoke(null, new Object[] { baseDir});

				//baseDirSet = Tester.getBASEDIR();
				m = tc.getMethod("getBASEDIR", new Class[0]);
				baseDirSet = (File) m.invoke(null, new Object[0]);

				if (!baseDirSet.equals(baseDir)) {
					String l = "AjcScript.setupTester() setting "
							+ baseDir + " returned " + baseDirSet;
					MessageUtil.debug(handler, l);
				}
			}
		} catch (Throwable t) {
			MessageUtil.abort(handler, "baseDir=" + baseDir, t);
		}
	}

	/**
	 * Clear (static) testing state and setup base directory,
	 * unless spec.skipTesting.
	 * This implementation assumes that Tester is defined for the
	 * same class loader as this class.
	 * @return null if successful, error message otherwise
	 */
	protected void setupTester(File baseDir, IMessageHandler handler) {
		File baseDirSet = null;
		try {
			if (!spec.skipTester) {
				Tester.clear();
				Tester.setMessageHandler(handler);
				Tester.setBASEDIR(baseDir);
				baseDirSet = Tester.getBASEDIR();
				if (!baseDirSet.equals(baseDir)) {
					String l = "AjcScript.setupTester() setting "
							+ baseDir + " returned " + baseDirSet;
					MessageUtil.debug(handler, l);
				}
			}
		} catch (Throwable t) {
			MessageUtil.abort(handler, "baseDir=" + baseDir, t);
		}
	}
	@Override
	public String toString() {
		return "JavaRun(" + spec + ")";
	}

	/**
	 * Struct class for fork attributes and initialization.
	 * This supports defaults for forking using system properties
	 * which will be overridden by any specification.
	 * (It differs from CompilerRun, which supports option
	 * overriding by passing values as harness arguments.)
	 */
	public static class ForkSpec {
		/**
		 * key for system property for default value for forking
		 * (true if set to true)
		 */
		public static String FORK_KEY = "javarun.fork";
		public static String JAVA_KEY = "javarun.java";
		public static String VM_ARGS_KEY = "javarun.vmargs";
		public static String JAVA_HOME_KEY = "javarun.java.home";
		public static String BOOTCLASSPATH_KEY = "javarun.bootclasspath";
		static final ForkSpec FORK;
		static {
			ForkSpec fork  = new ForkSpec();
			fork.fork = Boolean.getBoolean(FORK_KEY);
			fork.java = getFile(JAVA_KEY);
			if (null == fork.java) {
				fork.java = LangUtil.getJavaExecutable();
			}
			fork.javaHome = getFile(JAVA_HOME_KEY);
			fork.bootclasspath = XMLWriter.unflattenList(getProperty(BOOTCLASSPATH_KEY));
			fork.vmargs = XMLWriter.unflattenList(getProperty(VM_ARGS_KEY));
			FORK = fork;
		}
		private static File getFile(String key) {
			String path = getProperty(key);
			if (null != path) {
				File result = new File(path);
				if (result.exists()) {
					return result;
				}
			}
			return null;
		}
		private static String getProperty(String key) {
			try {
				return System.getProperty(key);
			} catch (Throwable t) {
				return null;
			}
		}
		private boolean fork;
		private String[] bootclasspath;
		private File java;
		private File javaHome;
		private String[] vmargs;

		private ForkSpec() {
			copy(FORK);
		}

		private void copy(ForkSpec forkSpec) {
			if (null != forkSpec) {
				fork = forkSpec.fork;
				bootclasspath = forkSpec.bootclasspath;
				java = forkSpec.java;
				javaHome = forkSpec.javaHome;
				vmargs = forkSpec.vmargs;
			}
		}

		/**
		 * @return "" or bootclasspath with File.pathSeparator internal delimiters
		 */
		String getBootclasspath() {
			if (LangUtil.isEmpty(bootclasspath)) {
				return "";
			}
			return FileUtil.flatten(bootclasspath, null);
		}
	}

	/**
	 * Initializer/factory for JavaRun.
	 * The classpath is not here but precalculated in the Sandbox.
	 */
	public static class Spec extends AbstractRunSpec {
		static {
			try {
				System.setSecurityManager(RunSecurityManager.ME);
			} catch (Throwable t) {
				System.err.println("JavaRun: Security manager set - no System.exit() protection");
			}
		}
		public static final String XMLNAME = "run";
		/**
		 * skip description, skip sourceLocation,
		 * do keywords, do options, skip paths, do comment,
		 * skip staging,   skip badInput,
		 * do dirChanges, do messages but skip children.
		 */
		private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
				"", "", null, null, "", null, "", "", false, false, true);

		/** fully-qualified name of the class to run */
		protected String className;

		/** Alternative to classname for specifying what to run modulename/type */
		protected String module;

		/** minimum required version of Java, if any */
		protected String javaVersion;

		/** if true, skip Tester setup (e.g., if Tester n/a) */
		protected boolean skipTester;

		/** if true, report text to output stream as error */
		protected boolean outStreamIsError;

		/** if true, report text to error stream as error */
		protected boolean errStreamIsError = true;

		protected final ForkSpec forkSpec;
		protected String[] aspectpath;
		protected boolean useLTW;
		protected String[] classpath;
		protected String expectedException;

		public Spec() {
			super(XMLNAME);
			setXMLNames(NAMES);
			forkSpec = new ForkSpec();
		}

		protected void initClone(Spec spec)
				throws CloneNotSupportedException {
			super.initClone(spec);
			spec.className = className;
			spec.errStreamIsError = errStreamIsError;
			spec.javaVersion = javaVersion;
			spec.outStreamIsError = outStreamIsError;
			spec.skipTester = skipTester;
			spec.forkSpec.copy(forkSpec);
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			Spec result = new Spec();
			initClone(result);
			return result;
		}

		public boolean isLTW() {
			return useLTW || (null != aspectpath);
		}

		/**
		 * @param version "1.1", "1.2", "1.3", "1.4"
		 * @throws IllegalArgumentException if version is not recognized
		 */
		public void setJavaVersion(String version) {
			Globals.supportsJava(version);
			this.javaVersion = version;
		}

		/** @className fully-qualified name of the class to run */
		public void setClassName(String className) {
			this.className = className;
		}

		public void setModule(String module) {
			this.module = module;
		}

		public void setLTW(String ltw) {
			useLTW = TestUtil.parseBoolean(ltw);
		}

		public void setAspectpath(String path) {
			this.aspectpath = XMLWriter.unflattenList(path);
		}
		public void setException(String exception) {
			this.expectedException = exception;
		}

		public void setClasspath(String path) {
			this.classpath = XMLWriter.unflattenList(path);
		}
		public void setErrStreamIsError(String errStreamIsError) {
			this.errStreamIsError = TestUtil.parseBoolean(errStreamIsError);
		}

		public void setOutStreamIsError(String outStreamIsError) {
			this.outStreamIsError = TestUtil.parseBoolean(outStreamIsError);
		}

		/** @param skip if true, then do not set up Tester */
		public void setSkipTester(boolean skip) {
			skipTester = skip;
		}

		public void setFork(boolean fork) {
			forkSpec.fork = fork;
		}

		/**
		 * @param vmargs comma-delimited list of arguments for java,
		 * typically -Dname=value,-DanotherName="another value"
		 */
		public void setVmArgs(String vmargs) {
			forkSpec.vmargs = XMLWriter.unflattenList(vmargs);
		}

		/** override to set dirToken to Sandbox.RUN_DIR */
		@Override
		public void addDirChanges(DirChanges.Spec spec) {
			if (null == spec) {
				return;
			}
			spec.setDirToken(Sandbox.RUN_DIR);
			super.addDirChanges(spec);
		}

		/** @return a JavaRun with this as spec if setup completes successfully. */
		@Override
		public IRunIterator makeRunIterator(Sandbox sandbox, Validator validator) {
			JavaRun run = new JavaRun(this);
			if (run.setupAjcRun(sandbox, validator)) {
				// XXX need name for JavaRun
				return new WrappedRunIterator(this, run);
			}
			return null;
		}

		/**
		 * Write this out as a run element as defined in
		 * AjcSpecXmlReader.DOCTYPE.
		 * @see AjcSpecXmlReader#DOCTYPE
		 * @see IXmlWritable#writeXml(XMLWriter)
		 */
		@Override
		public void writeXml(XMLWriter out) {
			String attr = XMLWriter.makeAttribute("class", className);
			out.startElement(xmlElementName, attr, false);
			if (skipTester) {
				out.printAttribute("skipTester", "true");
			}
			if (null != javaVersion) {
				out.printAttribute("vm", javaVersion);
			}
			if (outStreamIsError) {
				out.printAttribute("outStreamIsError", "true");
			}
			if (!errStreamIsError) { // defaults to true
				out.printAttribute("errStreamIsError", "false");
			}
			super.writeAttributes(out);
			out.endAttributes();
			if (!LangUtil.isEmpty(dirChanges)) {
				DirChanges.Spec.writeXml(out, dirChanges);
			}
			SoftMessage.writeXml(out, getMessages());
			out.endElement(xmlElementName);
		}
		@Override
		public String toLongString() {
			return toString() + "[" + super.toLongString() + "]";
		}

		@Override
		public String toString() {
			if (skipTester) {
				return "JavaRun(" + className + ", skipTester)";
			} else {
				return "JavaRun(" + className + ")";
			}
		}

		/**
		 * This implementation skips if:
		 * <ul>
		 * <li>current VM is not at least any specified javaVersion </li>
		 * </ul>
		 * @return false if this wants to be skipped, true otherwise
		 */
		@Override
		protected boolean doAdoptParentValues(RT parentRuntime, IMessageHandler handler) {
			if (!super.doAdoptParentValues(parentRuntime, handler)) {
				return false;
			}
			if ((null != javaVersion) && (!Globals.supportsJava(javaVersion))) {
				skipMessage(handler, "requires Java version " + javaVersion);
				return false;
			}
			return true;
		}
	}
	/**
	 * This permits everything but System.exit() in the context of a
	 * thread set by JavaRun.
	 * XXX need to update for thread spawned by that thread
	 * XXX need to update for awt thread use after AJDE wrapper doesn't
	 */
	public static class RunSecurityManager extends SecurityManager {
		public static RunSecurityManager ME = new RunSecurityManager();
		private Thread runThread;
		private RunSecurityManager(){}
		private synchronized void setJavaRunThread(JavaRun run) {
			LangUtil.throwIaxIfNull(run, "run");
			runThread = Thread.currentThread();
		}
		private synchronized void releaseJavaRunThread(JavaRun run) {
			LangUtil.throwIaxIfNull(run, "run");
			runThread = null;
		}
		/** @throws ExitCalledException if called from the JavaRun-set thread */
		@Override
		public void checkExit(int exitCode) throws ExitCalledException {
			if ((null != runThread) && runThread.equals(Thread.currentThread())) {
				throw new ExitCalledException(exitCode);
			}
		}
		public void checkAwtEventQueueAccess() {
			if ((null != runThread) && runThread.equals(Thread.currentThread())) {
				throw new AwtUsedException();
			}
		}
		public void checkSystemClipboardAccess() {
			// permit
		}
		// used by constrained calls
		public static class ExitCalledException extends SecurityException {
			public final int exitCode;
			public ExitCalledException(int exitCode) {
				this.exitCode = exitCode;
			}
		}
		public static class AwtUsedException extends SecurityException {
			public AwtUsedException() { }
		}
		// permit everything else
		@Override
		public void checkAccept(String arg0, int arg1) {
		}
		@Override
		public void checkAccess(Thread arg0) {
		}
		@Override
		public void checkAccess(ThreadGroup arg0) {
		}
		@Override
		public void checkConnect(String arg0, int arg1) {
		}
		@Override
		public void checkConnect(String arg0, int arg1, Object arg2) {
		}
		@Override
		public void checkCreateClassLoader() {
		}
		@Override
		public void checkDelete(String arg0) {
		}
		@Override
		public void checkExec(String arg0) {
		}
		@Override
		public void checkLink(String arg0) {
		}
		@Override
		public void checkListen(int arg0) {
		}
		public void checkMemberAccess(Class arg0, int arg1) {
		}
		@Override
		public void checkMulticast(InetAddress arg0) {
		}
		@Override
		public void checkMulticast(InetAddress arg0, byte arg1) {
		}
		@Override
		public void checkPackageAccess(String arg0) {
		}
		@Override
		public void checkPackageDefinition(String arg0) {
		}
		@Override
		public void checkPermission(Permission arg0) {
		}
		@Override
		public void checkPermission(Permission arg0, Object arg1) {
		}
		@Override
		public void checkPrintJobAccess() {
		}
		@Override
		public void checkPropertiesAccess() {
		}
		@Override
		public void checkPropertyAccess(String arg0) {
		}
		@Override
		public void checkRead(FileDescriptor arg0) {
		}
		@Override
		public void checkRead(String arg0) {
		}
		@Override
		public void checkRead(String arg0, Object arg1) {
		}
		@Override
		public void checkSecurityAccess(String arg0) {
		}
		@Override
		public void checkSetFactory() {
		}
		public boolean checkTopLevelWindow(Object arg0) {
			return true;
		}
		@Override
		public void checkWrite(FileDescriptor arg0) {
		}
		@Override
		public void checkWrite(String arg0) {
		}

	}

}
