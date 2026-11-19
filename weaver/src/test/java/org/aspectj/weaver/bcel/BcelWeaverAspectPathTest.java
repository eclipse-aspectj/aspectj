/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

import junit.framework.TestCase;

/**
 * Tests that BcelWeaver.addLibraryJarFile registers exactly the aspects in an aspect path jar, however it finds them.
 */
public class BcelWeaverAspectPathTest extends TestCase {

	private static final String CODE_STYLE_ASPECT_JAR = "../weaver/testdata/dummyAspect.jar";
	private static final String CODE_STYLE_ASPECT = "DummyAspect";

	private File tempDirectory;

	protected void setUp() throws Exception {
		super.setUp();
		tempDirectory = Files.createTempDirectory("bcelweaveraspectpathtest").toFile();
	}

	protected void tearDown() throws Exception {
		deleteRecursively(tempDirectory);
		super.tearDown();
	}

	public void testAnnotationStyleAspectIsRegistered() throws IOException {
		File jar = jarOf(plainClass("crafted.Before"), annotationStyleAspect("crafted.Aspect1"), plainClass("crafted.After"));

		BcelWorld world = addToAspectPath(jar);

		assertTrue(isRegisteredAspect(world, "crafted.Aspect1"));
		assertFalse(isRegisteredAspect(world, "crafted.Before"));
		assertFalse(isRegisteredAspect(world, "crafted.After"));
	}

	public void testCodeStyleAspectIsRegistered() throws IOException {
		File jar = jarOf(plainClass("crafted.Before"), codeStyleAspect(), plainClass("crafted.After"));

		BcelWorld world = addToAspectPath(jar);

		assertTrue(isRegisteredAspect(world, CODE_STYLE_ASPECT));
		assertFalse(isRegisteredAspect(world, "crafted.Before"));
		assertFalse(isRegisteredAspect(world, "crafted.After"));
	}

	public void testEveryAspectInAJarIsRegistered() throws IOException {
		File jar = jarOf(annotationStyleAspect("crafted.Aspect1"), plainClass("crafted.Plain"), codeStyleAspect(),
				annotationStyleAspect("crafted.Aspect2"));

		BcelWorld world = addToAspectPath(jar);

		assertTrue(isRegisteredAspect(world, "crafted.Aspect1"));
		assertTrue(isRegisteredAspect(world, CODE_STYLE_ASPECT));
		assertTrue(isRegisteredAspect(world, "crafted.Aspect2"));
		assertFalse(isRegisteredAspect(world, "crafted.Plain"));
	}

	public void testJarWithoutAspectsRegistersNone() throws IOException {
		File jar = jarOf(plainClass("crafted.Plain1"), plainClass("crafted.Plain2"));

		BcelWorld world = addToAspectPath(jar);

		assertFalse(isRegisteredAspect(world, "crafted.Plain1"));
		assertFalse(isRegisteredAspect(world, "crafted.Plain2"));
	}

	public void testClassThatOnlyMentionsAspectMarkersIsNotRegistered() throws IOException {
		ClassGen mentionsMarkers = classGen("crafted.MentionsMarkers");
		mentionsMarkers.getConstantPool().addUtf8("org.aspectj.weaver.Aspect");
		mentionsMarkers.getConstantPool().addUtf8("Lorg/aspectj/lang/annotation/Aspect;");
		File jar = jarOf(new TestClass("crafted.MentionsMarkers", mentionsMarkers.getJavaClass().getBytes()));

		BcelWorld world = addToAspectPath(jar);

		assertFalse(isRegisteredAspect(world, "crafted.MentionsMarkers"));
	}

	public void testAspectsAreRegisteredWhenTheSameJarIsAddedAgain() throws IOException {
		File jar = jarOf(plainClass("crafted.Plain"), annotationStyleAspect("crafted.Aspect1"), codeStyleAspect());

		BcelWorld first = addToAspectPath(jar);
		BcelWorld second = addToAspectPath(jar);

		for (BcelWorld world : new BcelWorld[] { first, second }) {
			assertTrue(isRegisteredAspect(world, "crafted.Aspect1"));
			assertTrue(isRegisteredAspect(world, CODE_STYLE_ASPECT));
			assertFalse(isRegisteredAspect(world, "crafted.Plain"));
			assertEquals(jar.getAbsolutePath(), world.resolve(UnresolvedType.forName("crafted.Aspect1")).getBinaryPath());
		}
	}

	public void testNonAspectClassesCanStillBeResolvedFromTheClasspath() throws IOException {
		File jar = jarOf(plainClass("crafted.Plain"), annotationStyleAspect("crafted.Aspect1"));

		BcelWorld scanned = addToAspectPath(jar, jar);
		BcelWorld remembered = addToAspectPath(jar, jar);

		for (BcelWorld world : new BcelWorld[] { scanned, remembered }) {
			ResolvedType plain = world.resolve(UnresolvedType.forName("crafted.Plain"));
			assertFalse(plain.isMissing());
			assertFalse(plain.isAspect());
		}
	}

	public void testChangedJarIsScannedAgain() throws IOException {
		File jar = new File(tempDirectory, "library.jar");
		writeJar(jar, plainClass("crafted.Plain"));
		assertFalse(isRegisteredAspect(addToAspectPath(jar), "crafted.Aspect1"));

		writeJar(jar, plainClass("crafted.Plain"), annotationStyleAspect("crafted.Aspect1"));

		assertTrue(isRegisteredAspect(addToAspectPath(jar), "crafted.Aspect1"));
		// and what was found this time is what is remembered, not the result for the jar as it was
		assertTrue(isRegisteredAspect(addToAspectPath(jar), "crafted.Aspect1"));
	}

	public void testJarThatNoLongerHoldsRememberedEntriesIsScannedAgain() throws IOException {
		File jar = new File(tempDirectory, "library.jar");
		writeJar(jar, annotationStyleAspect("crafted.AspectA"));
		long length = jar.length();
		long lastModified = jar.lastModified();
		assertTrue(isRegisteredAspect(addToAspectPath(jar), "crafted.AspectA"));

		// Same path, length and modification time as before, so only the contents tell the jars apart
		writeJar(jar, annotationStyleAspect("crafted.AspectB"));
		assertEquals("the test needs jars of equal length", length, jar.length());
		assertTrue(jar.setLastModified(lastModified));
		assertEquals(lastModified, jar.lastModified());

		BcelWorld world = addToAspectPath(jar);

		assertTrue(isRegisteredAspect(world, "crafted.AspectB"));
		assertFalse(isRegisteredAspect(world, "crafted.AspectA"));
	}

	private BcelWorld addToAspectPath(File aspectPathJar, File... classpath) throws IOException {
		List<String> classpathEntries = new ArrayList<>();
		for (File classpathEntry : classpath) {
			classpathEntries.add(classpathEntry.getAbsolutePath());
		}
		BcelWorld world = new BcelWorld(classpathEntries, new MessageHandler(), null);
		new BcelWeaver(world).addLibraryJarFile(aspectPathJar);
		return world;
	}

	private static boolean isRegisteredAspect(BcelWorld world, String className) {
		return world.getCrosscuttingMembersSet().containsAspect(UnresolvedType.forName(className));
	}

	private static final class TestClass {
		final String entryName;
		final byte[] bytes;

		TestClass(String className, byte[] bytes) {
			this.entryName = className.replace('.', '/') + ".class";
			this.bytes = bytes;
		}
	}

	private static ClassGen classGen(String className) {
		return new ClassGen(className, "java.lang.Object", "Crafted.java", Constants.ACC_PUBLIC, null);
	}

	private static TestClass plainClass(String className) {
		return new TestClass(className, classGen(className).getJavaClass().getBytes());
	}

	private static TestClass annotationStyleAspect(String className) {
		ClassGen aspect = classGen(className);
		ConstantPool cp = aspect.getConstantPool();
		aspect.addAnnotation(new AnnotationGen(new ObjectType("org.aspectj.lang.annotation.Aspect"), new ArrayList<NameValuePair>(),
				true, cp));
		return new TestClass(className, aspect.getJavaClass().getBytes());
	}

	/** An aspect compiled by ajc, which marks it with a class attribute instead of an annotation. */
	private static TestClass codeStyleAspect() throws IOException {
		try (JarFile jar = new JarFile(CODE_STYLE_ASPECT_JAR); InputStream in = jar.getInputStream(jar.getEntry(CODE_STYLE_ASPECT + ".class"))) {
			return new TestClass(CODE_STYLE_ASPECT, FileUtil.readAsByteArray(in));
		}
	}

	private File jarOf(TestClass... classes) throws IOException {
		File jar = Files.createTempFile(tempDirectory.toPath(), "library", ".jar").toFile();
		writeJar(jar, classes);
		return jar;
	}

	/** Entries are stored rather than compressed, so jars whose entries differ only in a few characters have equal lengths. */
	private static void writeJar(File jar, TestClass... classes) throws IOException {
		Map<String, byte[]> entries = new LinkedHashMap<>();
		for (TestClass testClass : classes) {
			entries.put(testClass.entryName, testClass.bytes);
		}
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
			for (Map.Entry<String, byte[]> e : entries.entrySet()) {
				CRC32 crc = new CRC32();
				crc.update(e.getValue());
				JarEntry entry = new JarEntry(e.getKey());
				entry.setMethod(ZipEntry.STORED);
				entry.setSize(e.getValue().length);
				entry.setCompressedSize(e.getValue().length);
				entry.setCrc(crc.getValue());
				jos.putNextEntry(entry);
				jos.write(e.getValue());
				jos.closeEntry();
			}
		}
	}

	private static void deleteRecursively(File f) {
		File[] children = f.listFiles();
		if (children != null) {
			for (File c : children) {
				deleteRecursively(c);
			}
		}
		f.delete();
	}
}
