/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.testing.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.util.Reflection;
import org.junit.Assert;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Things that junit should perhaps have, but doesn't. Also, the bytecode weaver is required to compare class files, but not to compare other files.
 */
public final class TestUtil {
	private static final String SANDBOX_NAME = "ajcSandbox";
	private static final boolean JAVA_5_VM;
	private static final String ASPECTJRT_KEY = "aspectjrt";
	private static final String TESTING_CLIENT_KEY = "testing-client";
	public static final URL BAD_URL;
	private static final File LIB_DIR;
	private static final Properties LIB_RPATHS;
	private static final Map LIB_ENTRIES;
	private static File ASPECTJRT_PATH;
	private static File ASPECTJRTJAR_PATH;
	static {
		{
			String[] paths = {
					"sp:aspectjrt.path",
					"sp:aspectjrt.jar",
					"../runtime/target/classes",
					"../lib/test/aspectjrt.jar"};
			ASPECTJRT_PATH = FileUtil.getBestFile(paths);
			ASPECTJRTJAR_PATH = FileUtil.getBestFile(paths, true);
		}
		{
			boolean j5 = false;
			try {
				Class.forName("java.lang.annotation.Annotation");
				j5 = true;
			} catch (Throwable t) {
			}
			JAVA_5_VM = j5;
		}
		{
			URL url = null;
			try {
				url = new URL("http://eclipse.org/BADURL");
			} catch (MalformedURLException e) {
				// ignore - hopefully never
			}
			BAD_URL = url;
		}
		{
			File file = new File("lib");
			if (!isLibDir(file)) {
				File cur = new File(".").getAbsoluteFile();
				File parent = cur.getParentFile();
				while (null != parent) {
					file = new File(parent, "lib");
					if (isLibDir(file)) {
						break;
					}
					parent = parent.getParentFile();
				}
				if (null == parent) {
					file = new File("NOT IN ASPECTJ TREE");
				}
			}
			LIB_DIR = file;
		}

		LIB_RPATHS = new Properties();
		LIB_RPATHS.setProperty(ASPECTJRT_KEY, "tests/aspectjrt.jar");
		LIB_RPATHS.setProperty(TESTING_CLIENT_KEY, "tests/testing-client.jar");
		// TODO support others loaded dynamically

		Map<String,Object> map = new HashMap<>();
		for (Object o : LIB_RPATHS.keySet()) {
			String key = (String) o;
			String path = LIB_RPATHS.getProperty(key);
			File file = null;
			URL url = null;
			try {
				file = libFile(path);
				url = libURL(path);
			} catch (IllegalArgumentException e) {
				file = new File(path + " not found");
				url = BAD_URL;
			} finally {
				map.put(key + ".file", file);
				map.put(key + ".url", url);
			}
		}
		// TODO support changing entries, etc.
		LIB_ENTRIES = Collections.unmodifiableMap(map);
	}

	private static boolean isLibDir(File lib) {
		return new File(lib, "test" + File.separator + "aspectjrt.jar").exists();
	}

	private TestUtil() {
		super();
	}

	public static boolean is15VMOrGreater() {
		return JAVA_5_VM;
	}

	public static File aspectjrtPath() {
		return ASPECTJRT_PATH;
	}

	// needsJar for module packaged runtime
	public static File aspectjrtPath(boolean needsJar) {
		if (needsJar) {
			return ASPECTJRTJAR_PATH;
		} else {
			return ASPECTJRT_PATH;
		}
	}

	public static URL fileToURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static String filesToPath(File[] entries) {
		return toPath(entries);
	}

	public static String urlsToPath(URL[] entries) {
		return toPath(entries);
	}

	/**
	 * untyped interface for mixed entries
	 */
	public static String filesOrurlsToPath(Object[] entries) {
		return toPath(entries);
	}

	/**
	 * This relies on these being File (where toString() == getPath()) or URL (where toString() == toExternalForm()).
	 *
	 * @param entries the Object[] of File or URL elements
	 * @return the String with entries dlimited by the File.pathSeparator
	 */
	private static String toPath(Object[] entries) {
		if ((null == entries) || (0 == entries.length)) {
			return "";
		}
		StringBuilder path = new StringBuilder();
		boolean started = false;
		for (Object entry : entries) {
			if (null != entry) {
				if (started) {
					path.append(File.pathSeparator);
				} else {
					started = true;
				}
				path.append(entry.toString());
			}
		}
		return path.toString();
	}

	public static String aspectjrtClasspath() {
		return TestUtil.aspectjrtPath().getPath();
	}

	/**
	 * @param input the String to parse for [on|off|true|false]
	 * @throws IllegalArgumentException if input is bad
	 **/
	public static boolean parseBoolean(String input) {
		return parseBoolean(input, true);
	}

	/**
	 * @param input the String to parse for [on|off|true|false]
	 * @param iaxOnError if true and input is bad, throw IllegalArgumentException
	 * @return true if input is true, false otherwise
	 * @throws IllegalArgumentException if iaxOnError and input is bad
	 */
	public static boolean parseBoolean(final String input, boolean iaxOnError) {
		final String syntax = ": [on|off|true|false]";
		if (null == input) {
			return false;
		}
		String lc = input.trim().toLowerCase();
		boolean result = false;
		boolean valid = false;
		switch (lc.length()) {
		case 2:
			if (valid = "on".equals(lc)) {
				result = true;
			}
			break;
		case 3:
			valid = "off".equals(lc);
			break;
		case 4:
			if (valid = "true".equals(lc)) {
				result = true;
			}
			break;
		case 5:
			valid = "false".equals(lc);
			break;
		}
		if (iaxOnError && !valid) {
			throw new IllegalArgumentException(input + syntax);
		}
		return result;
	}

	public static File aspectjrtJarFile() {
		return (File) LIB_ENTRIES.get(ASPECTJRT_KEY + ".file");
	}

	public static URL aspectjrtJarURL() {
		return (URL) LIB_ENTRIES.get(ASPECTJRT_KEY + ".url");
	}

	public static File testingClientJarFile() {
		return (File) LIB_ENTRIES.get(TESTING_CLIENT_KEY + ".file");
	}

	public static URL testingClientJarURL() {
		return (URL) LIB_ENTRIES.get(TESTING_CLIENT_KEY + ".url");
	}

	/**
	 *
	 * @param rpath the String relative path from the library directory to a resource that must exist (may be a directory), using
	 *        forward slashes as a file separator
	 * @return the File path
	 * @throws IllegalArgumentException if no such directory or file
	 */
	public static File libFile(String rpath) {
		if ((null == rpath) || (0 == rpath.length())) {
			throw new IllegalArgumentException("no input");
		}
		rpath = rpath.replace('/', File.separatorChar);
		File result = new File(LIB_DIR, rpath);
		if (result.exists()) {
			return result;
		}
		throw new IllegalArgumentException("not in " + LIB_DIR + ": " + rpath);
	}

	/**
	 * Like libPath, only it returns a URL.
	 *
	 * @return URL or null if it does not exist
	 * @throws IllegalArgumentException if no such directory or file
	 */
	public static URL libURL(String rpath) {
		File file = libFile(rpath);
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("bad URL from: " + file);
		}
	}

	// ---- arrays

	public static void assertArrayEquals(String msg, Object[] expected, Object[] found) {
		TestCase.assertEquals(msg, Arrays.asList(expected), Arrays.asList(found));
	}

	// ---- unordered

	public static void assertSetEquals(Collection<?> expected, Collection<?> found) {
		assertSetEquals(null, expected, found);
	}

	public static void assertSetEquals(String msg, Object[] expected, Object[] found) {
		assertSetEquals(msg, Arrays.asList(expected), Arrays.asList(found));
	}

	public static void assertSetEquals(String msg, Collection<?> expected, Collection<?> found) {
		msg = (msg == null) ? "" : msg + ": ";

		Set<Object> results1 = new HashSet<>(found);
		results1.removeAll(expected);

		Set<Object> results2 = new HashSet<>(expected);
		results2.removeAll(found);

		if (results1.isEmpty()) {
			TestCase.assertTrue(msg + "Expected but didn't find: " + results2.toString(), results2.isEmpty());
		} else if (results2.isEmpty()) {
			TestCase.assertTrue(msg + "Didn't expect: " + results1.toString(), results1.isEmpty());
		} else {
			TestCase.assertTrue(msg + "Expected but didn't find: " + results2.toString() + "\nDidn't expect: "
					+ results1.toString(), false);
		}
	}

	// ---- objects

	public static void assertCommutativeEquals(Object a, Object b, boolean should) {
		TestCase.assertEquals(a + " equals " + b, should, a.equals(b));
		TestCase.assertEquals(b + " equals " + a, should, b.equals(a));
		assertHashEquals(a, b, should);
	}

	private static void assertHashEquals(Object s, Object t, boolean should) {
		if (should) {
			TestCase.assertTrue(s + " does not hash to same as " + t, s.hashCode() == t.hashCode());
		} else {
			if (s.hashCode() == t.hashCode()) {
				System.err.println("warning: hash collision with hash = " + t.hashCode());
				System.err.println("  for " + s);
				System.err.println("  and " + t);
			}
		}
	}

	// -- reflective stuff

	public static void runMain(String classPath, String className) {
		runMethod(classPath, className, "main", new Object[] { new String[0] });
	}

	public static Object runMethod(String classPath, String className, String methodName, Object[] args) {
		classPath += File.pathSeparator + System.getProperty("java.class.path");

		ClassLoader loader = new URLClassLoader(pathToURLs(classPath), null);

		Class c = null;
		try {
			c = loader.loadClass(className);
		} catch (ClassNotFoundException e) {
			Assert.assertTrue("unexpected exception: " + e, false);
		}
		return Reflection.invokestaticN(c, methodName, args);
	}

	/**
	 * @see sun.misc.URLClassPath#pathToURLs(String)
	 */
	public static URL[] pathToURLs(String path) {
		StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		URL[] urls = new URL[st.countTokens()];
		int count = 0;
		while (st.hasMoreTokens()) {
			File f = new File(st.nextToken());
			try {
				f = new File(f.getCanonicalPath());
			} catch (IOException x) {
				// use the non-canonicalized filename
			}
			try {
				urls[count++] = f.toURI().toURL();
			} catch (IOException x) { }
		}

		if (urls.length != count) {
			URL[] tmp = new URL[count];
			System.arraycopy(urls, 0, tmp, 0, count);
			urls = tmp;
		}
		return urls;
	}

	/**
	 * Checks that two multi-line strings have the same value. Each line is trimmed before comparison.
	 * Produces an error on the particular line of conflict.
	 */
	public static void assertMultiLineStringEquals(String message, String s1, String s2) {
		try {
			BufferedReader r1 = new BufferedReader(new StringReader(s1));
			BufferedReader r2 = new BufferedReader(new StringReader(s2));

			List<String> lines = new ArrayList<>();
			String l1, l2;

			int index = 1;
			while (true) {
				l1 = readNonBlankLine(r1);
				l2 = readNonBlankLine(r2);
				if (l1 == null || l2 == null)
					break;
				if (l1.equals(l2)) {
					lines.add(l1);
				} else {
					showContext(lines);
					Assert.assertEquals(message + "(line " + index + "):\n" + l1 + "\n" + l2, l1, l2);
				}
				index++;
			}
			if (l1 != null)
				showContext(lines);
			Assert.assertTrue(message + ": unexpected " + l1, l1 == null);
			if (l2 != null)
				showContext(lines);
			Assert.assertTrue(message + ": unexpected " + l2, l2 == null);
		} catch (IOException ioe) {
			Assert.assertTrue(message + ": caught " + ioe.getMessage(), false);
		}
	}

	private static void showContext(List lines) {
		int n = lines.size();
		for (int i = Math.max(0, n - 8); i < n; i++) {
			System.err.println(lines.get(i));
		}
	}

	private static String readNonBlankLine(BufferedReader r) throws IOException {
		String l = r.readLine();
		if (l == null)
			return null;
		l = l.trim();
		// comment to include comments when reading
		int commentLoc = l.indexOf("//");
		if (-1 != commentLoc) {
			l = l.substring(0, commentLoc).trim();
		}
		if ("".equals(l))
			return readNonBlankLine(r);
		return l;
	}

	/**
	 * If there is an expected dir, expect each file in its subtree to match a corresponding actual file in the base directory. This
	 * does NOT check that all actual files have corresponding expected files. This ignores directory paths containing "CVS".
	 *
	 * @param handler the IMessageHandler sink for error messages
	 * @param expectedBaseDir the File path to the directory containing expected files, all of which are compared with any
	 *        corresponding actual files
	 * @param actualBaseDir the File path to the base directory from which to find any actual files corresponding to expected files.
	 * @return true if all files in the expectedBaseDir directory tree have matching files in the actualBaseDir directory tree.
	 */
	public static boolean sameDirectoryContents(final IMessageHandler handler, final File expectedBaseDir,
			final File actualBaseDir, final boolean fastFail) {
		LangUtil.throwIaxIfNull(handler, "handler");
		if (!FileUtil.canReadDir(expectedBaseDir)) {
			MessageUtil.fail(handler, " expected dir not found: " + expectedBaseDir);
			return false;
		}
		if (!FileUtil.canReadDir(actualBaseDir)) {
			MessageUtil.fail(handler, " actual dir not found: " + actualBaseDir);
			return false;
		}
		String[] paths = FileUtil.listFiles(expectedBaseDir);
		boolean result = true;
		for (String path : paths) {
			if (path.contains("CVS")) {
				continue;
			}
			if (!sameFiles(handler, expectedBaseDir, actualBaseDir, path) && result) {
				result = false;
				if (fastFail) {
					break;
				}
			}
		}
		return result;
	}

	// ------------ File-comparison utilities (XXX need their own class...)
	/**
	 * Test interface to compare two files, line by line, and report differences as one FAIL message if a handler is supplied. This
	 * preprocesses .class files by disassembling.
	 *
	 * @param handler the IMessageHandler for any FAIL messages (null to ignore)
	 * @param expectedFile the File path to the canonical file
	 * @param actualFile the File path to the actual file, if any
	 * @return true if the input files are the same, based on per-line comparisons
	 */
	public static boolean sameFiles(IMessageHandler handler, File expectedFile, File actualFile) {
		return doSameFile(handler, null, null, expectedFile, actualFile);
	}

	/**
	 * Test interface to compare two files, line by line, and report differences as one FAIL message if a handler is supplied. This
	 * preprocesses .class files by disassembling. This method assumes that the files are at the same offset from two respective
	 * base directories.
	 *
	 * @param handler the IMessageHandler for any FAIL messages (null to ignore)
	 * @param expectedBaseDir the File path to the canonical file base directory
	 * @param actualBaseDir the File path to the actual file base directory
	 * @param path the String path offset from the base directories
	 * @return true if the input files are the same, based on per-line comparisons
	 */
	public static boolean sameFiles(IMessageHandler handler, File expectedBaseDir, File actualBaseDir, String path) {
		File actualFile = new File(actualBaseDir, path);
		File expectedFile = new File(expectedBaseDir, path);
		return doSameFile(handler, expectedBaseDir, actualBaseDir, expectedFile, actualFile);
	}

	public static String disassembleClass(File basedir, File file) {
		String basedirPath = FileUtil.normalizedPath(basedir);
		String name = FileUtil.fileToClassName(basedir, file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    String utf8 = StandardCharsets.UTF_8.name();
	    try (PrintStream out = new PrintStream(baos, true, utf8)) {
			Class<?> c = Class.forName("org.aspectj.weaver.bcel.LazyClassGen");
			Method m = c.getMethod("disassemble", new Class[] { String.class, String.class, PrintStream.class });
			m.invoke(null, new Object[] { basedirPath, name, out });
			return baos.toString(utf8);
	    } catch (Throwable t) {
	    	throw new IllegalStateException(t);
		}
	}

	public static List<String> toLines(String inputString) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new StringReader(inputString))) {
			String line = null;
			while ((line= br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to create lines from string "+inputString,ioe);
		}
		return lines;
	}

	public static String toString(List<String> lines) {
		return String.join("\n", lines);
	}

	private static int longestLine(List<String> lines) {
		int longest = -1;
		for (String line: lines) {
			if (line.length()>longest) {
				longest = line.length();
			}
		}
		return longest;
	}

	private static boolean doSameFile(IMessageHandler handler, File expectedBaseDir, File actualBaseDir, File expectedFile,
			File actualFile) {
		String path = expectedFile.getPath();
		List<String> expectedLines = null;
		List<String> actualLines = null;
		List<String> errorContext = new ArrayList<>();
		if (path.endsWith(".class")) {
			expectedLines = toLines(disassembleClass(expectedBaseDir, expectedFile));
			actualLines = toLines(disassembleClass(actualBaseDir, actualFile));
		} else {
			expectedLines = FileUtil.readAsLines(expectedFile);
			actualLines = FileUtil.readAsLines(actualFile);
		}
		// TODO replace this section with assertMultiLineStringEquals ?
		int longestLine = longestLine(expectedLines);
		int actualLongestLine = longestLine(actualLines);
		String padding = null;
		if (actualLongestLine>longestLine) {
			longestLine = actualLongestLine;
		}
		StringBuilder s = new StringBuilder();
		for (int i=0;i<longestLine;i++) {
			s.append(" ");
		}
		padding = s.toString();
		if (expectedLines.isEmpty() && !actualLines.isEmpty()) {
			MessageUtil.fail(handler, "Expected no output but "+path+" contained:\n"+toString(actualLines));
			return false;
		} else {
			for (int l=0;l<expectedLines.size();l++) {
				String expectedLine = expectedLines.get(l);
				if (actualLines.size()<(l-1)) {
					MessageUtil.fail(handler, "Error comparing "+path+" - ran out of data in actual output compared to expect after "+(l+1)+" lines");
					return false;
				}
				String actualLine = actualLines.get(l);
				if (!expectedLine.equals(actualLine)) {
					errorContext.add(padded(actualLine,padding,longestLine)+" ! "+
									 padded(expectedLine, padding, longestLine));
					MessageUtil.fail(handler, "Error comparing actual vs expected "+path+" - line "+(l+1)+" differs\n"+
							"expected: ["+expectedLine+"]\n"+
							"  actual: ["+actualLine+"]\n"+
							String.join("\n", errorContext));
					return false;
				}
				errorContext.add(padded(actualLine,padding,longestLine)+" = "+
								 padded(expectedLine, padding, longestLine));
			}
			if (actualLines.size()>expectedLines.size()) {
				StringBuilder extra = new StringBuilder();
				for (int l=expectedLines.size();l<actualLines.size();l++) {
					extra.append(actualLines.get(l)).append("\n");
				}
				MessageUtil.fail(handler, "Error comparing actual vs expected "+path+" - actual data contains more lines, the first "+
						expectedLines.size()+" were as expected, extra data was: \n"+extra.toString()+"\n"+String.join("\n", errorContext));
				return false;
			}
		}
		return true;
	}

	private static String padded(String line, String padding, int longestLine) {
		StringBuilder s = new StringBuilder();
		String line2 = line.replace("\t","    ");
		s.append(line2).append(padding.substring(0,longestLine-line2.length()));
		return s.toString();
	}

	public static String cleanTestName(String name) {
		name = name.replace(' ', '_');
		return name;
	}

	public static Test skipTest(String tests) {
		// could printStackTrace to give more context if needed...
		System.err.println("skipping tests " + tests);
		return testNamed("skipping tests " + tests);
	}

	public static Test testNamed(String named) {
		final String name = cleanTestName(named);
		return new Test() {
			public int countTestCases() {
				return 1;
			}

			public void run(TestResult r) {
				r.startTest(this);
				r.endTest(this);
			}

			public String toString() {
				return name;
			}
		};
	}

	/**
	 * @param sink the TestSuite sink to add result to
	 * @param sourceName the String fully-qualified name of the class with a suite() method to load
	 */
	public static void loadTestsReflectively(TestSuite sink, String sourceName, boolean ignoreError) {
		Throwable thrown = null;
		try {
			ClassLoader loader = sink.getClass().getClassLoader();
			Class sourceClass = loader.loadClass(sourceName);
			if (!Modifier.isPublic(sourceClass.getModifiers())) {
				errorSuite(sink, sourceName, "not public class");
				return;
			}
			Method suiteMethod = sourceClass.getMethod("suite", new Class[0]);
			int mods = suiteMethod.getModifiers();
			if (!Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
				errorSuite(sink, sourceName, "not static method");
				return;
			}
			if (!Modifier.isPublic(mods)) {
				errorSuite(sink, sourceName, "not public method");
				return;
			}
			if (!Test.class.isAssignableFrom(suiteMethod.getReturnType())) {
				errorSuite(sink, sourceName, "suite() does not return Test");
				return;
			}
			Object result = suiteMethod.invoke(null, new Object[0]);
			Test test = (Test) result;
			if (!(test instanceof TestSuite)) {
				sink.addTest(test);
			} else {
				TestSuite source = (TestSuite) test;
				Enumeration<Test> tests = source.tests();
				while (tests.hasMoreElements()) {
					sink.addTest(tests.nextElement());
				}
			}

		} catch (ClassNotFoundException e) {
			thrown = e;
		} catch (SecurityException e) {
			thrown = e;
		} catch (NoSuchMethodException e) {
			thrown = e;
		} catch (IllegalArgumentException e) {
			thrown = e;
		} catch (IllegalAccessException e) {
			thrown = e;
		} catch (InvocationTargetException e) {
			thrown = e;
		}
		if (null != thrown) {
			if (ignoreError) {
				System.err.println("Error loading " + sourceName);
				thrown.printStackTrace(System.err);
			} else {
				errorSuite(sink, sourceName, thrown);
			}
		}
	}

	private static void errorSuite(TestSuite sink, String sourceName, Throwable thrown) {
		sink.addTest(new ErrorTest(sourceName, thrown));
	}

	private static void errorSuite(TestSuite sink, String sourceName, String err) {
		String message = "bad " + sourceName + ": " + err;
		sink.addTest(new ErrorTest(message));
	}

	/**
	 * Junit test failure, e.g., to report suite initialization errors at test time.
	 */
	public static class ErrorTest implements Test {
		private final Throwable thrown;

		public ErrorTest(Throwable thrown) {
			this.thrown = thrown;
		}

		public ErrorTest(String message) {
			this.thrown = new Error(message);
		}

		public ErrorTest(String message, Throwable thrown) {
			this(new TestError(message, thrown));
		}

		public int countTestCases() {
			return 1;
		}

		public void run(TestResult result) {
			result.startTest(this);
			result.addError(this, thrown);
		}
	}

	/**
	 * Nested exception - remove when using 1.4 or later.
	 */
	public static class TestError extends Error {
		private Throwable thrown;

		public TestError(String message) {
			super(message);
		}

		public TestError(String message, Throwable thrown) {
			super(message);
			this.thrown = thrown;
		}

		public Throwable getCause() {
			return thrown;
		}

		public void printStackTrace() {
			printStackTrace(System.err);
		}

		public void printStackTrace(PrintStream ps) {
			printStackTrace(new PrintWriter(ps));
		}

		public void printStackTrace(PrintWriter pw) {
			super.printStackTrace(pw);
			if (null != thrown) {
				pw.print("Caused by: ");
				thrown.printStackTrace(pw);
			}
		}
	}

	public static File createEmptySandbox() {
		File sandbox;

		String os = System.getProperty("os.name");
		File tempDir = null;
		// AMC - I did this rather than use the JDK default as I hate having to go look
		// in c:\documents and settings\......... for the results of a failed test.
		if (os.startsWith("Windows")) {
			tempDir = new File("N:\\temp");
			if (!tempDir.exists()) {
				tempDir = new File("C:\\temp");
				if (!tempDir.exists()) {
					tempDir.mkdir();
				}
			}
		} else {
			tempDir = new File("/tmp");
		}
		File sandboxRoot = new File(tempDir, SANDBOX_NAME);
		if (!sandboxRoot.exists()) {
			sandboxRoot.mkdir();
		}

		try {
			File workspace = new File(".." + File.separator);
			String workspaceName = workspace.getCanonicalPath();
			int index = workspaceName.lastIndexOf(File.separator);
			workspaceName = workspaceName.substring(index + 1);

			File workspaceRoot = new File(sandboxRoot, workspaceName);
			if (!workspaceRoot.exists()) {
				workspaceRoot.mkdir();
			}

			FileUtil.deleteContents(workspaceRoot);

			sandbox = File.createTempFile("ajcTest", ".tmp", workspaceRoot);
			sandbox.delete();
			sandbox.mkdir();

		} catch (IOException ioEx) {
			throw new AssertionFailedError("Unable to create sandbox directory for test");
		}

		return sandbox;
	}

	/**
	 * Capture PrintStream output to String[] (delimiting component String on println()), also showing any missed text.
	 */
	public static class LineStream extends PrintStream {
		StringBuffer sb = new StringBuffer();

		ByteArrayOutputStream missed;

		ArrayList<String> sink;

		public LineStream() {
			super(new ByteArrayOutputStream());
			this.sink = new ArrayList<>();
			missed = (ByteArrayOutputStream) out;
		}

		/** @return any text not captured by our overrides */
		public String getMissed() {
			return missed.toString();
		}

		/** clear captured lines (but not missed text) */
		public void clear() {
			sink.clear();
		}

		/**
		 * Get String[] of lines printed, delimited by println(..) calls.
		 *
		 * @return lines printed, exclusive of any not yet terminated by newline
		 */
		public String[] getLines() {
			return sink.toArray(new String[0]);
		}

		// ---------- PrintStream overrides
		public void println(Object x) {
			println(x.toString());
		}

		public void print(Object obj) {
			print(obj.toString());
		}

		public void println(char c) {
			sb.append(c);
			println();
		}

		public void println(char[] c) {
			sb.append(c);
			println();
		}

		public void print(char c) {
			sb.append(c);
		}

		public void print(char[] c) {
			sb.append(c);
		}

		public void println(String s) {
			print(s);
			println();
		}

		public void print(String s) {
			sb.append(s);
		}

		public void println() {
			String line = sb.toString();
			sink.add(line);
			sb.setLength(0);
		}
	}

	public static boolean haveDisassembler() {
		try {
			return (Class.forName("org.aspectj.weaver.bcel.LazyClassGen") != null);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
