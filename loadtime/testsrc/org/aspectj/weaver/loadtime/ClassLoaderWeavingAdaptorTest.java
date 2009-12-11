/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.weaver.World;
import org.aspectj.weaver.World.TypeMap;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.tools.WeavingAdaptor;

public class ClassLoaderWeavingAdaptorTest extends TestCase {

	public void testClassLoaderWeavingAdaptor() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader, null);
	}

	public void testGetNamespace() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader, null);
		String namespace = adaptor.getNamespace();
		assertEquals("Namespace should be empty", "", namespace);
	}

	public void testGeneratedClassesExistFor() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader, null);
		boolean exist = adaptor.generatedClassesExistFor("Junk");
		assertFalse("There should be no generated classes", exist);
	}

	public void testFlushGeneratedClasses() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader, null);
		adaptor.flushGeneratedClasses();
		boolean exist = adaptor.generatedClassesExistFor("Junk");
		assertFalse("There should be no generated classes", exist);
	}

	/**
	 * Testing fast excludes of the pattern "com.foo..*". World should not have any new types in it after rejection.
	 */
	public void testFastExclusionOne() throws Exception {
		TestClassLoaderWeavingAdaptor adaptor = getAdaptor(null, "testdata..*");

		String orangesSub = "testdata.sub.Oranges";
		JavaClass orangesClass = getClassFrom(orangesSub);
		byte[] orangesBytes = orangesClass.getBytes();

		boolean accepted = adaptor.accept(orangesSub, orangesBytes);
		assertFalse("Should not be accepted", accepted);
		TypeMap map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());

		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());

		// Important thing here is that the rejection of testdata.sub.Oranges did not require it to be loaded into the world at all
	}

	/**
	 * Testing fast includes of the pattern "*". World should not have any new types in it after inclusion.
	 */
	public void testFastInclusionOne() throws Exception {
		TestClassLoaderWeavingAdaptor adaptor = getAdaptor("*", null);

		String orangesSub = "testdata.sub.Oranges";
		JavaClass orangesClass = getClassFrom(orangesSub);
		byte[] orangesBytes = orangesClass.getBytes();

		boolean accepted = adaptor.accept(orangesSub, orangesBytes);
		assertTrue("Should be accepted", accepted);
		TypeMap map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());

		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());

		// Important thing here is that the rejection of testdata.sub.Oranges did not require it to be loaded into the world at all
	}

	/**
	 * Testing fast excludes of the pattern "*Oranges". World should not have any new types in it after rejection.
	 */
	public void testFastExclusionTwo() throws Exception {
		TestClassLoaderWeavingAdaptor adaptor = getAdaptor(null, "*Oranges");

		String oranges = "testdata.Oranges";
		JavaClass orangesClass = getClassFrom(oranges);
		byte[] orangesBytes = orangesClass.getBytes();

		boolean accepted = adaptor.accept(oranges, orangesBytes);
		assertFalse("Should not be accepted", accepted);
		TypeMap map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());
		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());

		String orangesSub = "testdata.sub.Oranges";

		JavaClass orangesSubClass = getClassFrom(orangesSub);
		byte[] orangesSubBytes = orangesSubClass.getBytes();

		accepted = adaptor.accept(orangesSub, orangesSubBytes);
		assertFalse("Should not be accepted", accepted);
		map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());
		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());
	}

	/**
	 * Testing fast excludes of the pattern "*..*Oranges*". World should not have any new types in it after rejection.
	 */
	public void testFastExclusionThree() throws Exception {
		TestClassLoaderWeavingAdaptor adaptor = getAdaptor(null, "*..*ran*");

		String oranges = "testdata.Oranges";
		JavaClass orangesClass = getClassFrom(oranges);
		byte[] orangesBytes = orangesClass.getBytes();

		boolean accepted = adaptor.accept(oranges, orangesBytes);
		assertFalse("Should not be accepted", accepted);
		TypeMap map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());

		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());

		String orangesSub = "testdata.sub.Oranges";
		JavaClass orangesSubClass = getClassFrom(orangesSub);
		byte[] orangesSubBytes = orangesSubClass.getBytes();

		accepted = adaptor.accept(orangesSub, orangesSubBytes);
		assertFalse("Should not be accepted", accepted);
		map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());
		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());

		String apples = "testdata.Apples";
		JavaClass applesClass = getClassFrom(apples);
		byte[] applesBytes = applesClass.getBytes();

		accepted = adaptor.accept(apples, applesBytes);
		assertTrue("Should be accepted", accepted);
		map = accessTypeMap(adaptor);

		// The aspect and the Apples type
		assertEquals(1, map.getExpendableMap().size());
		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());
	}

	/**
	 * Testing fast inclusion checking of exact include names eg. "testdata.sub.Oranges"
	 */
	public void testFastInclusionTwo() throws Exception {
		TestClassLoaderWeavingAdaptor adaptor = getAdaptor("testdata.sub.Oranges", null);

		String oranges = "testdata.Oranges";
		JavaClass orangesClass = getClassFrom(oranges);
		byte[] orangesBytes = orangesClass.getBytes();

		boolean accepted = adaptor.accept(oranges, orangesBytes);
		assertFalse("Should not be accepted", accepted);
		TypeMap map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());

		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());

		String orangesSub = "testdata.sub.Oranges";
		JavaClass orangesSubClass = getClassFrom(orangesSub);
		byte[] orangesSubBytes = orangesSubClass.getBytes();

		accepted = adaptor.accept(orangesSub, orangesSubBytes);
		assertTrue("Should be accepted", accepted);
		map = accessTypeMap(adaptor);

		// The aspect
		assertEquals(1, map.getExpendableMap().size());
		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());

		String apples = "testdata.Apples";
		JavaClass applesClass = getClassFrom(apples);
		byte[] applesBytes = applesClass.getBytes();

		accepted = adaptor.accept(apples, applesBytes);
		assertFalse("Should not be accepted", accepted);
		map = accessTypeMap(adaptor);

		// The aspect and the Apples type
		assertEquals(1, map.getExpendableMap().size());
		// primitives, void and jlObject
		assertEquals(10, map.getMainMap().size());
	}

	/**
	 * Testing fast excludes of the pattern groovy related pattern -
	 */
	// public void testFastExclusionFour() throws Exception {
	// TestClassLoaderWeavingAdaptor adaptor = getAdaptor("*", "testdata..* && !testdata.sub.Oran*");
	//
	// String oranges = "testdata.Oranges";
	// JavaClass orangesClass = getClassFrom(oranges);
	// byte[] orangesBytes = orangesClass.getBytes();
	//
	// boolean accepted = adaptor.accept(oranges, orangesBytes);
	// assertFalse("Should not be accepted", accepted);
	// TypeMap map = accessTypeMap(adaptor);
	//
	// // The aspect
	// assertEquals(1, map.getExpendableMap().size());
	//
	// // primitives, void and jlObject
	// assertEquals(10, map.getMainMap().size());
	//
	// String orangesSub = "testdata.sub.Oranges";
	// JavaClass orangesSubClass = getClassFrom(orangesSub);
	// byte[] orangesSubBytes = orangesSubClass.getBytes();
	//
	// accepted = adaptor.accept(orangesSub, orangesSubBytes);
	// assertTrue("Should be accepted", accepted);
	// map = accessTypeMap(adaptor);
	//
	// // The aspect
	// assertEquals(1, map.getExpendableMap().size());
	// // primitives, void and jlObject
	// assertEquals(10, map.getMainMap().size());
	// }

	public void testAcceptanceSpeedStarDotDotStar() throws Exception {
		URLClassLoader loader = new URLClassLoader(new URL[] { new File("../loadtime/bin").toURI().toURL() }, null);

		JavaClass jc = getClassFrom("../loadtime/bin", "org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOne");
		byte[] bs = jc.getBytes();
		jc = getClassFrom("../loadtime/bin", "org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOneCGLIB");
		byte[] bs2 = jc.getBytes();
		// InputStream is = loader.getResourceAsStream("org.aspectj.weaver.loadtime.ClassLoaderWeaverAdaptorTests$TestOne");
		assertNotNull(bs);
		TestWeavingContext wc = new TestWeavingContext(loader);
		Definition d = new Definition();
		d.getExcludePatterns().add("*..*CGLIB*");
		d.getAspectClassNames().add("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$AnAspect");
		wc.addDefinition(d);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader, wc);
		boolean exist = adaptor.generatedClassesExistFor("Junk");
		assertFalse("There should be no generated classes", exist);

		// before:
		// Acceptance 331ms
		// Rejection 3368ms

		// after:
		// Acceptance 343ms
		// Rejection 80ms

		long stime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			boolean b = adaptor.accept("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOne", bs);
			assertTrue("Should be accepted", b);
		}
		long etime = System.currentTimeMillis();
		System.out.println("Acceptance " + (etime - stime) + "ms");
		stime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			adaptor.delegateForCurrentClass = null;
			boolean b = adaptor.accept("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOneCGLIB", bs2);
			assertFalse("Should not be accepting CGLIB", b);
		}
		etime = System.currentTimeMillis();
		System.out.println("Rejection " + (etime - stime) + "ms");

	}

	// TODO
	// shouldn't add it to the type patterns if we are going to fast handle it
	// include for exact name, what does it mean?
	// excludes="!xxxx" should also be fast matched...

	public void testAcceptanceSpeedExactName() throws Exception {
		URLClassLoader loader = new URLClassLoader(new URL[] { new File("../loadtime/bin").toURI().toURL() }, null);

		JavaClass jc = getClassFrom("../loadtime/bin", "org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOne");
		byte[] bs = jc.getBytes();
		jc = getClassFrom("../loadtime/bin", "org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOneCGLIB");
		byte[] bs2 = jc.getBytes();
		// InputStream is = loader.getResourceAsStream("org.aspectj.weaver.loadtime.ClassLoaderWeaverAdaptorTests$TestOne");
		assertNotNull(bs);
		TestWeavingContext wc = new TestWeavingContext(loader);
		Definition d = new Definition();
		d.getExcludePatterns().add("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest.TestOneCGLIB");
		d.getAspectClassNames().add("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$AnAspect");
		wc.addDefinition(d);
		TestClassLoaderWeavingAdaptor adaptor = new TestClassLoaderWeavingAdaptor();
		adaptor.initialize(loader, wc);
		boolean exist = adaptor.generatedClassesExistFor("Junk");
		assertFalse("There should be no generated classes", exist);

		// before:
		// Acceptance 331ms
		// Rejection 3160ms

		// after:
		// Acceptance 379ms
		// Rejection 103ms

		long stime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			boolean b = adaptor.accept("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOne", bs);
			assertTrue("Should be accepted", b);
		}
		long etime = System.currentTimeMillis();
		System.out.println("Acceptance " + (etime - stime) + "ms");
		stime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			adaptor.delegateForCurrentClass = null;
			boolean b = adaptor.accept("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$TestOneCGLIB", bs2);
			assertFalse("Should not be accepting CGLIB", b);
		}
		etime = System.currentTimeMillis();
		System.out.println("Rejection " + (etime - stime) + "ms");
		BcelWorld world = adaptor.getWorld();
		Field f = World.class.getDeclaredField("typeMap");
		f.setAccessible(true);
		TypeMap typeMap = (TypeMap) f.get(world);
		System.out.println(typeMap.getExpendableMap().size());
		System.out.println(typeMap.getMainMap().size());
		printExpendableMap(typeMap.getExpendableMap());
		printMainMap(typeMap.getMainMap());
	}

	// --- infrastructure ---

	private TypeMap accessTypeMap(TestClassLoaderWeavingAdaptor adaptor) {
		return adaptor.getWorld().getTypeMap();
	}

	public TestClassLoaderWeavingAdaptor getAdaptor(String includePattern, String excludePattern) {
		return getAdaptor(includePattern == null ? null : new String[] { includePattern }, excludePattern == null ? null
				: new String[] { excludePattern });
	}

	public TestClassLoaderWeavingAdaptor getAdaptor(String[] includePatterns, String[] excludePatterns) {
		try {
			URLClassLoader loader = new URLClassLoader(new URL[] { new File("../loadtime/bin").toURI().toURL() }, null);
			TestWeavingContext wc = new TestWeavingContext(loader);
			Definition d = new Definition();
			if (includePatterns != null) {
				for (String s : includePatterns) {
					d.getIncludePatterns().add(s);
				}
			}
			if (excludePatterns != null) {
				for (String s : excludePatterns) {
					d.getExcludePatterns().add(s);
				}
			}
			// need some random aspect or the weaver will shut down!
			d.getAspectClassNames().add("org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest$AnAspect");
			wc.addDefinition(d);
			TestClassLoaderWeavingAdaptor adaptor = new TestClassLoaderWeavingAdaptor();
			adaptor.initialize(loader, wc);
			return adaptor;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void printMaps(TypeMap map) {
		printExpendableMap(map.getExpendableMap());
		printMainMap(map.getMainMap());
	}

	private void printExpendableMap(Map m) {
		for (Object o : m.keySet()) {
			String sig = (String) o;
			System.out.println(sig + "=" + m.get(sig));
		}
	}

	private void printMainMap(Map m) {
		for (Object o : m.keySet()) {
			String sig = (String) o;
			System.out.println(sig + "=" + m.get(sig));
		}
	}

	static class TestClassLoaderWeavingAdaptor extends ClassLoaderWeavingAdaptor {

		public BcelWorld getWorld() {
			return bcelWorld;
		}
	}

	public static JavaClass getClassFrom(String clazzname) throws ClassNotFoundException {
		return getClassFrom("../loadtime/bin", clazzname);
	}

	public static JavaClass getClassFrom(String frompath, String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(frompath);
		return repos.loadClass(clazzname);
	}

	public static SyntheticRepository createRepos(String cpentry) {
		ClassPath cp = new ClassPath(cpentry + File.pathSeparator + System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	}

	@Aspect
	static class AnAspect {

	}

	class TestOne {

	}

	class TestOneCGLIB {

	}

	static class TestWeavingContext extends DefaultWeavingContext {

		List testList = new ArrayList();

		public TestWeavingContext(ClassLoader loader) {
			super(loader);
		}

		public void addDefinition(Definition d) {
			testList.add(d);
		}

		@Override
		public List getDefinitions(final ClassLoader loader, final WeavingAdaptor adaptor) {
			return testList;
		}
	}
}

// --- testclasses and aspects ---