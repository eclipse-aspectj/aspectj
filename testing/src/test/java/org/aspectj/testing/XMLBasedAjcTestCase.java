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
package org.aspectj.testing;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import junit.framework.TestCase;
import org.apache.commons.digester.Digester;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjAttribute.WeaverState;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.bcel.BcelConstantPoolReader;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Root class for all Test suites that are based on an AspectJ XML test suite file. Extends AjcTestCase allowing a mix of
 * programmatic and spec-file driven testing. See org.aspectj.systemtest.incremental.IncrementalTests for an example of this mixed
 * style.
 * <p>
 * The class org.aspectj.testing.MakeTestClass will generate a subclass of this class for you, given a suite spec. file as input...
 * </p>
 */
public abstract class XMLBasedAjcTestCase extends AjcTestCase {

	private static Map<String,AjcTest> testMap = new HashMap<>();
	private static boolean suiteLoaded = false;
	private AjcTest currentTest = null;
	private Stack<Boolean> clearTestAfterRun = new Stack<>();

	public XMLBasedAjcTestCase() {
	}
	
	/**
	 * You must define a suite() method in subclasses, and return the result of calling this method. (Don't you hate static methods
	 * in programming models). For example:
	 * 
	 * <pre>
	 * public static Test suite() {
	 * 	 return XMLBasedAjcTestCase.loadSuite(MyTestCaseClass.class);
	 * }
	 * </pre>
	 * 
	 * @param testCaseClass
	 * @return
	 */
	public static Test loadSuite(Class<? extends TestCase> testCaseClass) {
		TestSuite suite = new TestSuite(testCaseClass.getName());
		suite.addTestSuite(testCaseClass);
		TestSetup wrapper = new TestSetup(suite) {
			protected void setUp() throws Exception {
				super.setUp();
				suiteLoaded = false;
			}

			protected void tearDown() throws Exception {
				super.tearDown();
				suiteLoaded = false;
			}
		};
		return wrapper;
	}

	/**
	 * The file containing the XML specification for the tests.
	 */
	protected abstract URL getSpecFile();

	/*
	 * Return a map from (String) test title -> AjcTest
	 */
	protected Map<String,AjcTest> getSuiteTests() {
		return testMap;
	}

	protected WeaverStateInfo getWeaverStateInfo(JavaClass jc) {
		WeaverStateInfo wsi = null;
		try {
			for (Attribute attribute : jc.getAttributes()) {
				if (attribute.getName().equals("org.aspectj.weaver.WeaverState")) {
					if (wsi != null) {
						fail("Found two WeaverState attributes");
					}
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					attribute.dump(new DataOutputStream(baos));
					baos.close();
					byte[] byteArray = baos.toByteArray();
					byte[] newbytes = new byte[byteArray.length-6];
					System.arraycopy(byteArray, 6, newbytes, 0, newbytes.length);
					WeaverState read = (WeaverState)
							AjAttribute.read(new WeaverVersionInfo(), WeaverState.AttributeName,
									newbytes, null, null,
									new BcelConstantPoolReader(jc.getConstantPool()));
					wsi = read.reify();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wsi;
	}

	/**
	 * This helper method runs the test with the given title in the suite spec file. All tests steps in given ajc-test execute in
	 * the same sandbox.
	 */
	protected void runTest(String title, boolean print) {
		try {
			currentTest = (AjcTest) testMap.get(title);
			final boolean clearTest = clearTestAfterRun();
			if (currentTest == null) {
				if (clearTest) {
					System.err.println("test already run: " + title);
					return;
				} else {
					fail("No test '" + title + "' in suite.");
				}
			}
			boolean run = currentTest.runTest(this);
			assertTrue("Test not run", run);
			if (clearTest) {
				testMap.remove(title);
			}
		} finally {
			if (print) {
				System.out.println("SYSOUT");
				System.out.println(ajc.getLastCompilationResult().getStandardOutput());
			}
		}
	}

	protected void runTest(String title) {
		runTest(title, false);
	}

	/**
	 * Get the currently executing test. Useful for access to e.g. AjcTest.getTitle() etc..
	 */
	protected AjcTest getCurrentTest() {
		return currentTest;
	}

	/**
	 * For use by the Digester. As the XML document is parsed, it creates instances of AjcTest objects, which are added to this
	 * TestCase by the Digester by calling this method.
	 */
	public void addTest(AjcTest test) {
		testMap.put(test.getTitle(), test);
	}

	protected final void pushClearTestAfterRun(boolean val) {
		clearTestAfterRun.push(val ? Boolean.FALSE : Boolean.TRUE);
	}

	protected final boolean popClearTestAfterRun() {
		return clearTest(true);
	}

	protected final boolean clearTestAfterRun() {
		return clearTest(false);
	}

	private boolean clearTest(boolean pop) {
		if (clearTestAfterRun.isEmpty()) {
			return false;
		}
		boolean result = clearTestAfterRun.peek();
		if (pop) {
			clearTestAfterRun.pop();
		}
		return result;
	}

	protected void checkVersion(String classname, int major, int minor)  {
		JavaClass jc;
		try {
			jc = getClassFrom(ajc.getSandboxDirectory(), classname);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot find class "+classname,e);
		}
		if (jc.getMajor() != major) {
			fail("Expected major version to be " + major + " but was " + jc.getMajor());
		}
		if (jc.getMinor() != minor) {
			fail("Expected minor version to be " + minor + " but was " + jc.getMinor());
		}
	}

	/*
	 * The rules for parsing a suite spec file. The Digester using bean properties to match attributes in the XML document to
	 * properties in the associated classes, so this simple implementation should be very easy to maintain and extend should you
	 * ever need to.
	 */
	protected Digester getDigester() {
		Digester digester = new Digester();
		digester.push(this);
		digester.addObjectCreate("suite/ajc-test", AjcTest.class);
		digester.addSetProperties("suite/ajc-test");
		digester.addSetNext("suite/ajc-test", "addTest", "org.aspectj.testing.AjcTest");
		digester.addObjectCreate("suite/ajc-test/compile", CompileSpec.class);
		digester.addSetProperties("suite/ajc-test/compile");
		digester.addSetNext("suite/ajc-test/compile", "addTestStep", "org.aspectj.testing.ITestStep");
		digester.addObjectCreate("suite/ajc-test/file", FileSpec.class);
		digester.addSetProperties("suite/ajc-test/file");
		digester.addSetNext("suite/ajc-test/file", "addTestStep", "org.aspectj.testing.ITestStep");
		digester.addObjectCreate("suite/ajc-test/run", RunSpec.class);
		digester.addSetProperties("suite/ajc-test/run", "class", "classToRun");
		digester.addSetProperties("suite/ajc-test/run", "module", "moduleToRun");
		digester.addSetProperties("suite/ajc-test/run", "ltw", "ltwFile");
		digester.addSetProperties("suite/ajc-test/run", "xlintfile", "xlintFile");
		digester.addSetProperties("suite/ajc-test/run/stderr", "ordered", "orderedStderr");
		digester.addSetNext("suite/ajc-test/run", "addTestStep", "org.aspectj.testing.ITestStep");
		digester.addObjectCreate("*/message", ExpectedMessageSpec.class);
		digester.addSetProperties("*/message");
		digester.addSetNext("*/message", "addExpectedMessage", "org.aspectj.testing.ExpectedMessageSpec");
		digester.addObjectCreate("suite/ajc-test/weave", WeaveSpec.class);
		digester.addSetProperties("suite/ajc-test/weave");
		digester.addSetNext("suite/ajc-test/weave", "addTestStep", "org.aspectj.testing.ITestStep");

		digester.addObjectCreate("suite/ajc-test/ant", AntSpec.class);
		digester.addSetProperties("suite/ajc-test/ant");
		digester.addSetNext("suite/ajc-test/ant", "addTestStep", "org.aspectj.testing.ITestStep");
		digester.addObjectCreate("suite/ajc-test/ant/stderr", OutputSpec.class);
		digester.addSetProperties("suite/ajc-test/ant/stderr");
		digester.addSetNext("suite/ajc-test/ant/stderr", "addStdErrSpec", "org.aspectj.testing.OutputSpec");
		digester.addObjectCreate("suite/ajc-test/ant/stdout", OutputSpec.class);
		digester.addSetProperties("suite/ajc-test/ant/stdout");
		digester.addSetNext("suite/ajc-test/ant/stdout", "addStdOutSpec", "org.aspectj.testing.OutputSpec");

		digester.addObjectCreate("suite/ajc-test/run/stderr", OutputSpec.class);
		digester.addSetProperties("suite/ajc-test/run/stderr");
		digester.addSetNext("suite/ajc-test/run/stderr", "addStdErrSpec", "org.aspectj.testing.OutputSpec");
		digester.addObjectCreate("suite/ajc-test/run/stdout", OutputSpec.class);
		digester.addSetProperties("suite/ajc-test/run/stdout");
		digester.addSetNext("suite/ajc-test/run/stdout", "addStdOutSpec", "org.aspectj.testing.OutputSpec");
		digester.addObjectCreate("*/line", OutputLine.class);
		digester.addSetProperties("*/line");
		digester.addSetNext("*/line", "addLine", "org.aspectj.testing.OutputLine");
		return digester;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.tools.ajc.AjcTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		if (!suiteLoaded) {
			testMap = new HashMap<>();
			System.out.println("LOADING SUITE: " + getSpecFile().getPath());
			Digester d = getDigester();
			try {
				InputStreamReader isr = new InputStreamReader(getSpecFile().openConnection().getInputStream());
				d.parse(isr);
			} catch (Exception ex) {
				fail("Unable to load suite " + getSpecFile().getPath() + " : " + ex);
			}
			suiteLoaded = true;
		}
	}

	protected long nextIncrement(boolean doWait) {
		long time = System.currentTimeMillis();
		if (doWait) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException intEx) {
			}
		}
		return time;
	}

	protected void copyFile(String from, String to) throws Exception {
		String dir = getCurrentTest().getDir();
		FileUtil.copyFile(new File(dir + File.separator + from), new File(ajc.getSandboxDirectory(), to));
	}

	protected void copyFileAndDoIncrementalBuild(String from, String to) throws Exception {
		copyFile(from, to);
		CompilationResult result = ajc.doIncrementalCompile();
		assertNoMessages(result, "Expected clean compile from test '" + getCurrentTest().getTitle() + "'");
	}

	protected void copyFileAndDoIncrementalBuild(String from, String to, MessageSpec expectedResults) throws Exception {
		String dir = getCurrentTest().getDir();
		FileUtil.copyFile(new File(dir + File.separator + from), new File(ajc.getSandboxDirectory(), to));
		CompilationResult result = ajc.doIncrementalCompile();
		assertMessages(result, "Test '" + getCurrentTest().getTitle() + "' did not produce expected messages", expectedResults);
	}

	protected void deleteFile(String file) {
		new File(ajc.getSandboxDirectory(), file).delete();
	}

	protected void deleteFileAndDoIncrementalBuild(String file, MessageSpec expectedResult) throws Exception {
		deleteFile(file);
		CompilationResult result = ajc.doIncrementalCompile();
		assertMessages(result, "Test '" + getCurrentTest().getTitle() + "' did not produce expected messages", expectedResult);
	}

	protected void deleteFileAndDoIncrementalBuild(String file) throws Exception {
		deleteFileAndDoIncrementalBuild(file, MessageSpec.EMPTY_MESSAGE_SET);
	}

	protected void assertAdded(String file) {
		assertTrue("File " + file + " should have been added", new File(ajc.getSandboxDirectory(), file).exists());
	}

	protected void assertDeleted(String file) {
		assertFalse("File " + file + " should have been deleted", new File(ajc.getSandboxDirectory(), file).exists());
	}

	protected void assertUpdated(String file, long sinceTime) {
		File f = new File(ajc.getSandboxDirectory(), file);
		assertTrue("File " + file + " should have been updated", f.lastModified() > sinceTime);
	}

	public SyntheticRepository createRepos(File cpentry) {
		ClassPath cp = new ClassPath(cpentry + File.pathSeparator + System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	}
	
	protected byte[] loadFileAsByteArray(File f) {
		try {
			byte[] bs = new byte[100000];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			int pos = 0;
			int len = 0;
			while ((len=bis.read(bs, pos, 100000-pos))!=-1) {
				pos+=len;
			}
			bis.close();
			return bs;
		} catch (Exception e) {
			return null;
		}
	}

	public JavaClass getClassFrom(File where, String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(where);
		return repos.loadClass(clazzname);
	}

	protected Method getMethodStartsWith(JavaClass jc, String prefix) {
		return getMethodStartsWith(jc,prefix,1);
	}
	
	protected Attribute getAttributeStartsWith(Attribute[] attributes, String prefix) {
		StringBuilder buf = new StringBuilder();
		for (Attribute a: attributes) {
			if (a.getName().startsWith(prefix)) {
				return a;
			}
			buf.append(a.toString()).append("\n");
		}
		fail("Failed to find '"+prefix+"' in attributes:\n"+buf.toString());
		return null;
	}
	
	protected Method getMethodStartsWith(JavaClass jc, String prefix, int whichone) {
		Method[] meths = jc.getMethods();
		for (Method method : meths) {
			System.out.println(method);
			if (method.getName().startsWith(prefix)) {
				whichone--;
				if (whichone == 0) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * Sort it by name then start position
	 */
	public List<LocalVariable> sortedLocalVariables(LocalVariableTable lvt) {
		List<LocalVariable> l = new ArrayList<>();
		LocalVariable lv[] = lvt.getLocalVariableTable();
		Collections.addAll(l, lv);
		l.sort(new MyComparator());
		return l;
	}

	public String stringify(LocalVariableTable lvt, int slotIndex) {
		LocalVariable lv[] = lvt.getLocalVariableTable();
		LocalVariable lvEntry = lv[slotIndex];
		StringBuffer sb = new StringBuffer();
		sb.append(lvEntry.getSignature()).append(" ").append(lvEntry.getName()).append("(").append(lvEntry.getIndex())
				.append(") start=").append(lvEntry.getStartPC()).append(" len=").append(lvEntry.getLength());
		return sb.toString();
	}

	public String stringify(List<LocalVariable> l, int slotIndex) {
		LocalVariable lvEntry = (LocalVariable) l.get(slotIndex);
		StringBuffer sb = new StringBuffer();
		sb.append(lvEntry.getSignature()).append(" ").append(lvEntry.getName()).append("(").append(lvEntry.getIndex())
				.append(") start=").append(lvEntry.getStartPC()).append(" len=").append(lvEntry.getLength());
		return sb.toString();
	}

	public String stringify(LocalVariableTable lvt) {
		if (lvt == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("LocalVariableTable.  Entries=#" + lvt.getTableLength()).append("\n");
		LocalVariable lv[] = lvt.getLocalVariableTable();
		for (LocalVariable lvEntry : lv) {
			sb.append(lvEntry.getSignature()).append(" ").append(lvEntry.getName()).append("(").append(lvEntry.getIndex())
					.append(") start=").append(lvEntry.getStartPC()).append(" len=").append(lvEntry.getLength()).append("\n");
		}

		return sb.toString();
	}

	public static class CountingFilenameFilter implements FilenameFilter {

		private String suffix;
		private int count;

		public CountingFilenameFilter(String s) {
			this.suffix = s;
		}

		public boolean accept(File dir, String name) {
			if (name.endsWith(suffix)) {
				count++;
			}
			return false;
		}

		public int getCount() {
			return count;
		}
	}

	public static class MyComparator implements Comparator<LocalVariable> {
		public int compare(LocalVariable o1, LocalVariable o2) {
			LocalVariable l1 = (LocalVariable) o1;
			LocalVariable l2 = (LocalVariable) o2;
			if (l1.getName().equals(l2.getName())) {
				return l1.getStartPC() - l2.getStartPC();
			} else {
				return l1.getName().compareTo(l2.getName());
			}
		}

	}

	protected Method getMethodFromClass(JavaClass clazz, String methodName) {
		Method[] meths = clazz.getMethods();
		for (Method method : meths) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}

  protected URL getClassResource(String resourceName) {
    return getClass().getResource(resourceName);
  }

	protected Method findMethod(JavaClass jc, String string) {
		for (Method m : jc.getMethods()) {
			if (m.getName().equals(string)) {
				return m;
			}
		}
		return null;
	}

	protected ResolvedMember findMethod(ResolvedType outerType, String string) {
		for (ResolvedMember method: outerType.getDeclaredMethods()) {
			if (method.getName().equals(string)) {
				return method;
			}
		}
		return null;
	}

	
}
