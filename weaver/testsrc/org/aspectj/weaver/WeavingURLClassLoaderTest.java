/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Matthew Webster     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.TestCase;

import org.aspectj.bridge.AbortException;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.tools.WeavingAdaptor;

/**
 * @author websterm
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class WeavingURLClassLoaderTest extends TestCase {

	private final static String CLASSES_JAR = BcweaverTests.TESTDATA_PATH + "/ltw-classes.jar";
	private final static String WOVEN_JAR = BcweaverTests.TESTDATA_PATH + "/ltw-woven.jar";
	private final static String JUNK_JAR = BcweaverTests.TESTDATA_PATH + "/ltw-junk.jar";
	private final static String ADVICE_ASPECTS = BcweaverTests.TESTDATA_PATH + "/ltw-aspects.jar";
	private final static String DW_ADVICE_ASPECTS = BcweaverTests.TESTDATA_PATH + "/ltw-dwaspects.jar";
	private final static String DE_ADVICE_ASPECTS = BcweaverTests.TESTDATA_PATH + "/ltw-deaspects.jar";
	private final static String AROUNDCLOSURE_ASPECTS = BcweaverTests.TESTDATA_PATH + "/ltw-acaspects.jar";
	private final static String ITD_ASPECTS = BcweaverTests.TESTDATA_PATH + "/ltw-itdaspects.jar";
	private final static String PER_ASPECTS = BcweaverTests.TESTDATA_PATH + "/ltw-peraspects.jar";

	private final static String NULL = "null";

	private Properties savedProperties;

	public WeavingURLClassLoaderTest(String name) {
		super(name);
	}

	public void testLoadClass () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,"");
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] {}); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testLoadWovenClass () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,"");
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,WOVEN_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveWovenClass () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,ADVICE_ASPECTS + File.pathSeparator + WOVEN_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			fail("Expecting org.aspectj.bridge.AbortException");
		}
		catch (Exception ex) {
			assertTrue("Expecting org.aspectj.bridge.AbortException caught " + ex,(ex instanceof AbortException));
		}
	}

	public void testWeavingURLClassLoader () {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspects = FileUtil.getFileURL(new File(ADVICE_ASPECTS));
		URL[] classURLs = new URL[] { aspects, classes };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveAdvice () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveAdviceWithVerbose () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		setSystemProperty(WeavingAdaptor.WEAVING_ADAPTOR_VERBOSE,"true");
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveAdviceWithWeaveInfo () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		setSystemProperty(WeavingAdaptor.SHOW_WEAVE_INFO_PROPERTY,"true");
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveDeclareWarningAdvice () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,DW_ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,DW_ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] {} ); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeaveDeclareErrorAdvice () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,DE_ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,DE_ADVICE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] {} ); 
			fail("Expecting org.aspectj.bridge.AbortException");
		}
		catch (Exception ex) {
			assertTrue("Expecting org.aspectj.bridge.AbortException caught " + ex,(ex instanceof AbortException));
		}
	}

	public void testWeaveAroundClosure () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,AROUNDCLOSURE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,AROUNDCLOSURE_ASPECTS + File.pathSeparator + CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAroundClosure" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeavingITD () {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspects = FileUtil.getFileURL(new File(ITD_ASPECTS));
		URL[] classURLs = new URL[] { aspects, classes };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWInterfaceITD", "LTWFieldITD", "LTWMethodITD" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeavingPer () {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspects = FileUtil.getFileURL(new File(PER_ASPECTS));
		URL[] classURLs = new URL[] { aspects, classes };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWPerthis" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testWeavingAspects () {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspects1 = FileUtil.getFileURL(new File(ADVICE_ASPECTS));
		URL aspects2 = FileUtil.getFileURL(new File(AROUNDCLOSURE_ASPECTS));
		URL aspects3 = FileUtil.getFileURL(new File(ITD_ASPECTS));
		URL aspects4 = FileUtil.getFileURL(new File(PER_ASPECTS));
		URL[] classURLs = new URL[] {  aspects1, aspects2, aspects3, aspects4, classes };
		URL[] aspectURLs = new URL[] { aspects1, aspects2, aspects3, aspects4 };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect", "LTWAroundClosure", "LTWPerthis", "LTWInterfaceITD", "LTWFieldITD", "LTWMethodITD", "LTWPerthis"}); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testJunkJar () {		
		File junkJar = new File(JUNK_JAR);
		assertFalse(junkJar + " should not exist",junkJar.exists());
		
		URL classes = FileUtil.getFileURL(junkJar);
		URL[] classURLs = new URL[] { classes };
		URL[] aspectURLs = new URL[] { };
		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			fail("Expecting java.lang.ClassNotFoundException");
		}
		catch (Exception ex) {
			assertTrue("Expecting java.lang.ClassNotFoundException caught " + ex,(ex instanceof ClassNotFoundException));
		}
	}

	public void testJunkAspectJar () {		
		File junkJar = new File(JUNK_JAR);
		assertFalse(junkJar + " should not exist",junkJar.exists());
		
		URL aspects = FileUtil.getFileURL(junkJar);
		URL[] classURLs = new URL[] { aspects };
		URL[] aspectURLs = new URL[] { aspects };
		
		try {
			WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());
			fail("Expecting org.aspectj.bridge.AbortException");
		}
		catch (Exception ex) {
			assertTrue("Expecting org.aspectj.bridge.AbortException caught " + ex,(ex instanceof org.aspectj.bridge.AbortException));
		}
	}

	public void testAddURL () {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspects = FileUtil.getFileURL(new File(ADVICE_ASPECTS));
		URL[] classURLs = new URL[] { aspects };
		URL[] aspectURLs = new URL[] { aspects };

		WeavingURLClassLoader loader = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());
		loader.addURL(classes);

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public void testParentChild() {
		URL classes = FileUtil.getFileURL(new File(CLASSES_JAR));
		URL aspects = FileUtil.getFileURL(new File(ADVICE_ASPECTS));
		
		URL[] classURLs = new URL[] { aspects };
		URL[] aspectURLs = new URL[] { aspects };
		WeavingURLClassLoader parent = new WeavingURLClassLoader(classURLs,aspectURLs,getClass().getClassLoader());
		
		classURLs = new URL[] { classes };
		aspectURLs = new URL[] { };
		WeavingURLClassLoader child = new WeavingURLClassLoader(classURLs,aspectURLs,parent);

		try {
			Class clazz = child.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	/*
	 * Aspects on ASPECTPATH but missing from CLASSPATH
	 */
	public void testIncompletePath () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,ADVICE_ASPECTS);
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("LTWHelloWorld");
			invokeMain(clazz,new String[] { "LTWAspect" }); 
			fail("Expecting java.lang.NoClassDefFoundError");
		}
		catch (Exception ex) {
		}
	}

	/*
	 * Ensure package object is correct
	 */
	public void testPackage () {
		setSystemProperty(WeavingURLClassLoader.WEAVING_ASPECT_PATH,"");
		setSystemProperty(WeavingURLClassLoader.WEAVING_CLASS_PATH,CLASSES_JAR);
		WeavingURLClassLoader loader = new WeavingURLClassLoader(getClass().getClassLoader());

		try {
			Class clazz = loader.loadClass("ltw.LTWPackageTest");
			invokeMain(clazz,new String[] { }); 
			Package pakkage = clazz.getPackage();
			assertTrue("Expected 'ltw' got " + pakkage,(pakkage != null));
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}

	public static void invokeMain (Class clazz, String[] args)
	{
		Class[] paramTypes = new Class[1];
		paramTypes[0] = args.getClass();
	
		try {
			Method method = clazz.getDeclaredMethod("main",paramTypes);
			Object[] params = new Object[1];
			params[0] = args;
			method.invoke(null,params);
		}
		catch (InvocationTargetException ex) {
			throw new RuntimeException(ex.getTargetException().toString());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.toString());
		}
	}

	private void setSystemProperty (String key, String value) {
		Properties systemProperties = System.getProperties();
		copyProperty(key,systemProperties,savedProperties);
		systemProperties.setProperty(key,value);
	}
	
	private static void copyProperty (String key, Properties from, Properties to) {
		String value = from.getProperty(key,NULL);
		to.setProperty(key,value);
	}

	protected void setUp() throws Exception {
		super.setUp();
		savedProperties = new Properties();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		/* Restore system properties */
		Properties systemProperties = System.getProperties();
		for (Enumeration enum = savedProperties.keys(); enum.hasMoreElements(); ) {
			String key = (String)enum.nextElement();
			String value = savedProperties.getProperty(key);
			if (value == NULL) systemProperties.remove(key);
			else systemProperties.setProperty(key,value);
		}
	}

}
