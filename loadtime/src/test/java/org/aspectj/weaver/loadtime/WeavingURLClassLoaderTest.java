/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Webster     initial implementation
 * ******************************************************************/

package org.aspectj.weaver.loadtime;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.aspectj.bridge.AbortException;
import org.aspectj.testing.util.TestUtil.TestError;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.tools.WeavingAdaptor;

import junit.framework.TestCase;

/**
 * @author websterm
 *
 */
public class WeavingURLClassLoaderTest extends TestCase {

	private final static String TESTDATA_PATH = "../weaver/testdata";

	private final static String ASPECTJRT = "../runtime/target/classes";
	private final static String CLASSES_JAR = TESTDATA_PATH + "/ltw-classes.jar";
	private final static String WOVEN_JAR = TESTDATA_PATH + "/ltw-woven.jar";
	private final static String JUNK_JAR = TESTDATA_PATH + "/ltw-junk.jar";
	private final static String ADVICE_ASPECTS = TESTDATA_PATH + "/ltw-aspects.jar";
	private final static String DW_ADVICE_ASPECTS = TESTDATA_PATH + "/ltw-dwaspects.jar";
	private final static String DE_ADVICE_ASPECTS = TESTDATA_PATH + "/ltw-deaspects.jar";
	private final static String AROUNDCLOSURE_ASPECTS = TESTDATA_PATH + "/ltw-acaspects.jar";
	private final static String ITD_ASPECTS = TESTDATA_PATH + "/ltw-itdaspects.jar";
	private final static String PER_ASPECTS = TESTDATA_PATH + "/ltw-peraspects.jar";
	private final static String TEST_BASE = TESTDATA_PATH + "/WeavingURLClassLoaderTest/builtLibs";

	private final static String NULL = "null";

	private Properties savedProperties;

	public WeavingURLClassLoaderTest(String name) {
		super(name);
	}

	public void testLoadClass() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, "");
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] {});
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	/*
	 * We won't get an exception because the aspect path is empty and there is no aop.xml file so the weaver will be disabled and no
	 * reweaving will take place
	 */
	public void testLoadWovenClass() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, "");
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, WOVEN_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testGarbageName() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, "");
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, WOVEN_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			loader.loadClass("[Lorg.springframework.webflow.config.FlowLocation;Editor");
		} catch (ClassNotFoundException cnfe) {
			// success!
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	/*
	 * We get an exception because the class was not built reweavable
	 */
	public void testWeaveWovenClass() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, ADVICE_ASPECTS + File.pathSeparator + WOVEN_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			loader.loadClass("LTWHelloWorld");
			fail("Expecting org.aspectj.bridge.AbortException");
		} catch (Exception ex) {
			assertTrue("Expecting org.aspectj.bridge.AbortException caught " + ex, (ex instanceof AbortException));
		}
	}

	public void testWeavingURLClassLoader() {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects = FileUtil.getFileURL(new File(ADVICE_ASPECTS));
		URL[] classURLs = new URL[] { aspects, classes, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveAdvice() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR
				+ File.pathSeparator + ASPECTJRT);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveAdviceWithVerbose() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR
				+ File.pathSeparator + ASPECTJRT);
		setSystemProperty(WeavingAdaptor.WEAVING_ADAPTOR_VERBOSE, "true");
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveAdviceWithWeaveInfo() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR
				+ File.pathSeparator + ASPECTJRT);
		setSystemProperty(WeavingAdaptor.SHOW_WEAVE_INFO_PROPERTY, "true");
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveDeclareWarningAdvice() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, DW_ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, DW_ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] {});
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveDeclareErrorAdvice() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, DE_ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, DE_ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] {});
			fail("Expecting org.aspectj.bridge.AbortException");
		} catch (Exception ex) {
			assertTrue("Expecting org.aspectj.bridge.AbortException caught " + ex, (ex instanceof AbortException));
		}
	}

	public void testWeaveAroundClosure() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, AROUNDCLOSURE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, AROUNDCLOSURE_ASPECTS + File.pathSeparator + CLASSES_JAR
				+ File.pathSeparator + ASPECTJRT);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAroundClosure" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeavingITD() {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects = FileUtil.getFileURL(new File(ITD_ASPECTS));
		URL[] classURLs = new URL[] { aspects, classes, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWInterfaceITD", "LTWFieldITD", "LTWMethodITD" });
		} catch (Exception ex) {
			ex.printStackTrace();
			// throw new RuntimeException("Failed!", ex);
			fail(ex.toString());
			// } finally {
			// System.exit(0);
		}
	}

	public void testWeavingPer() {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects = FileUtil.getFileURL(new File(PER_ASPECTS));
		URL[] classURLs = new URL[] { aspects, classes, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWPerthis" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeavingAspects() {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects1 = FileUtil.getFileURL(new File(ADVICE_ASPECTS));
		URL aspects2 = FileUtil.getFileURL(new File(AROUNDCLOSURE_ASPECTS));
		URL aspects3 = FileUtil.getFileURL(new File(ITD_ASPECTS));
		URL aspects4 = FileUtil.getFileURL(new File(PER_ASPECTS));
		URL[] classURLs = new URL[] { aspects1, aspects2, aspects3, aspects4, classes, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects1, aspects2, aspects3, aspects4 };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect", "LTWAroundClosure", "LTWPerthis", "LTWInterfaceITD", "LTWFieldITD",
					"LTWMethodITD", "LTWPerthis" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testJunkJar() {
		File junkJar = new File(JUNK_JAR);
		assertFalse(junkJar + " should not exist", junkJar.exists());

		URL classes = FileUtil.getFileURL(junkJar);
		URL[] classURLs = new URL[] { classes };
		URL[] aspectURLs = new URL[] {};
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

		try {
			loader.loadClass("LTWHelloWorld");
			fail("Expecting java.lang.ClassNotFoundException");
		} catch (Exception ex) {
			assertTrue("Expecting java.lang.ClassNotFoundException caught " + ex, (ex instanceof ClassNotFoundException));
		}
	}

	public void testJunkAspectJar() {
		File junkJar = new File(JUNK_JAR);
		assertFalse(junkJar + " should not exist", junkJar.exists());

		URL aspects = FileUtil.getFileURL(junkJar);
		URL[] classURLs = new URL[] { aspects };
		URL[] aspectURLs = new URL[] { aspects };

		try {
			new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());
			fail("Expecting org.aspectj.bridge.AbortException");
		} catch (Exception ex) {
			assertTrue("Expecting org.aspectj.bridge.AbortException caught " + ex,
					(ex instanceof org.aspectj.bridge.AbortException));
		}
	}

	public void testAddURL() {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects = FileUtil.getFileURL(new File(ADVICE_ASPECTS));
		URL[] classURLs = new URL[] { aspects, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects };

		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());
		loader.addURL(classes);

		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testParentChild() {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects = FileUtil.getFileURL(new File(ADVICE_ASPECTS));

		URL[] classURLs = new URL[] { aspects, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader parent = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

		classURLs = new URL[] { classes };
		aspectURLs = new URL[] {};
		WeavingURLClassLoader child = new WeavingURLClassLoader(classURLs, aspectURLs, parent);

		try {
			Class<?> clazz = child.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	/*
	 * Aspects on ASPECTPATH but missing from CLASSPATH
	 */
	public void testIncompletePath() throws Exception {
		System.out.println("ADVICE_ASPECTS exists? " + new File(ADVICE_ASPECTS).exists());
		System.out.println("ASPECTJRT exists? " + new File(ASPECTJRT).exists());
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, ADVICE_ASPECTS+File.pathSeparator+new File(ASPECTJRT).toString());
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,
				CLASSES_JAR + File.pathSeparator + new File(ASPECTJRT).toString());
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());
		Class<?> loadClass = loader.loadClass("org.aspectj.lang.JoinPoint$StaticPart");
		System.out.println("JPSP: " + loadClass);
		try {
			Class<?> clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz, new String[] { "LTWAspect" });
			fail("Expecting java.lang.NoClassDefFoundError");
		} catch (Exception ex) {
			// Expecting: java.lang.NoClassDefFoundError: LTWAspect
			String m = ex.getMessage();
			if (!m.contains("java.lang.NoClassDefFoundError")) {
				new RuntimeException("Unexpected problem in testIncompletePath", ex).printStackTrace();
				fail("Expecting java.lang.NoClassDefFoundError but caught " + ex);
			}
		}
	}

	/*
	 * Ensure package object is correct
	 */
	public void testPackage() {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH, "");
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH, CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class<?> clazz = loader.loadClass("ltw.LTWPackageTest");
			invokeMain(clazz, new String[] {});
			Package pakkage = clazz.getPackage();
			assertTrue("Expected 'ltw' got " + pakkage, (pakkage != null));
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testZipAspects() {
		try {
			doTestZipAspects(TEST_BASE + "/aspect.zip");
		} catch (Error ex) {
			failWithException(ex);
		} catch (Exception ex) {
			failWithException(ex);
		}
	}

	public void testJarAspects() {
		try {
			doTestZipAspects(TEST_BASE + "/aspect.jar");
		} catch (Error ex) {
			failWithException(ex);
		} catch (Exception ex) {
			failWithException(ex);
		}
	}

	/** PR#106736 */
	public void testClassAspects() {
		try {
			doTestZipAspects(TEST_BASE + "/classes");
		} catch (Error ex) {
			failWithException(ex);
		} catch (Exception ex) {
			failWithException(ex);
		}
	}

	public void testZipJarAspectsTest() {
		try {
			doTestZipAspectsTest();
			// bug: doTestZipAspects("") attempts to load packag.Aspect?
			fail("expected error to be thrown");
		} catch (InvocationTargetException ex) {
			// expecting error
			assertTrue(ex.getTargetException() instanceof Error);
		} catch (RuntimeException ex) {
			// expecting error
			String message = ex.getMessage();
			// expecting error - seems to be wrapped wrong
			if (!message.contains("around advice")) {
				failWithException(ex);
			}
		} catch (Error ex) {
			failWithException(ex);
		} catch (Exception ex) {
			failWithException(ex);
		}
	}

	public void testWeavingURLClassLoaderOddJars() throws Exception {
		URL classes = FileUtil.getFileURL(new File(TEST_BASE + "/test.jar/main.file"));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects = FileUtil.getFileURL(new File(TEST_BASE + "/aspectNoExt"));
		URL[] classURLs = new URL[] { aspects, classes, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

		Class<?> clazz = loader.loadClass("packag.Main");
		invokeMain(clazz, new String[] {});
	}

	public void testWeavingURLClassLoaderMissingJars() throws Exception {
		try {
			URL classes = FileUtil.getFileURL(new File(TEST_BASE + "/test.jar/main.file"));
			URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
			URL aspects = FileUtil.getFileURL(new File(TEST_BASE + "/MissingFile"));
			URL[] classURLs = new URL[] { aspects, classes, aspectjrt };
			URL[] aspectURLs = new URL[] { aspects };
			WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, getClass().getClassLoader());

			Class<?> clazz = loader.loadClass("packag.Main");
			invokeMain(clazz, new String[] {});
			fail("Should reject bad aspect MissingFile");
		} catch (AbortException ae) {
			assertTrue("Unexpected cause: " + ae.getMessage(), ae.getMessage().contains("bad aspect library"));
		}
	}

	private void doTestZipAspects(String aspectLib) throws Exception {
		File classZip = new File(TEST_BASE + "/main.zip");
		File zipLib = new File(aspectLib);
		URL classes = FileUtil.getFileURL(classZip);
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL aspects = FileUtil.getFileURL(zipLib);
		URL[] classURLs = new URL[] { aspects, classes, aspectjrt };
		URL[] aspectURLs = new URL[] { aspects };
		ClassLoader parent = getClass().getClassLoader();
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, aspectURLs, parent);
		Class<?> clazz = loader.loadClass("packag.Main");
		invokeMain(clazz, new String[] {});
		// throws Error unless advice applies
	}

	private void doTestZipAspectsTest() throws Exception {
		URL classes = FileUtil.getFileURL(new File(TEST_BASE + "/main.zip"));
		URL aspectjrt = FileUtil.getFileURL(new File(ASPECTJRT));
		URL[] classURLs = new URL[] { classes, aspectjrt };
		ClassLoader parent = getClass().getClassLoader();
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs, new URL[] {}, parent);
		Class<?> clazz = loader.loadClass("packag.Main");
		invokeMain(clazz, new String[] {});
		// throws Error because advice does not apply
	}

	private void failWithException(Throwable t) {
		throw new TestError(t.getMessage(), t);
	}

	public static void invokeMain(Class clazz, String[] args) {
		Class<?>[] paramTypes = new Class[1];
		paramTypes[0] = args.getClass();

		try {
			Method method = clazz.getDeclaredMethod("main", paramTypes);
			Object[] params = new Object[1];
			params[0] = args;
			method.invoke(null, params);
		} catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof RuntimeException) {
				throw (RuntimeException) ex.getTargetException();
			} else {
				throw new RuntimeException(ex.getTargetException().toString());
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex.toString());
		}
	}

	private void setSystemProperty(String key, String value) {
		Properties systemProperties = System.getProperties();
		copyProperty(key, systemProperties, savedProperties);
		systemProperties.setProperty(key, value);
	}

	private static void copyProperty(String key, Properties from, Properties to) {
		String value = from.getProperty(key, NULL);
		to.setProperty(key, value);
	}

	protected void setUp() throws Exception {
		super.setUp();
		savedProperties = new Properties();
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		/* Restore system properties */
		Properties systemProperties = System.getProperties();
		for (Enumeration<Object> enu = savedProperties.keys(); enu.hasMoreElements();) {
			String key = (String) enu.nextElement();
			String value = savedProperties.getProperty(key);
			if (value == NULL) {
				systemProperties.remove(key);
			} else {
				systemProperties.setProperty(key, value);
			}
		}
	}

}
