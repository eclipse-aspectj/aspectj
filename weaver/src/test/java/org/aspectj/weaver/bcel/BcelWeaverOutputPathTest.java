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
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.util.FileUtil;

import junit.framework.TestCase;

public class BcelWeaverOutputPathTest extends TestCase {

	private File tempDirectory;
	private File outDir;
	private File maliciousJar;

	protected void setUp() throws Exception {
		super.setUp();
		tempDirectory = File.createTempFile("bcelweaveroutputpathtest", "root");
		tempDirectory.delete();
		tempDirectory.mkdirs();

		outDir = new File(tempDirectory, "out");
		outDir.mkdirs();

		byte[] bytes;
		try (InputStream is = getClass().getResourceAsStream(getClass().getSimpleName() + ".class")) {
			bytes = FileUtil.readAsByteArray(is);
		}

		// Create a rogue jar with a relative path in it
		maliciousJar = new File(tempDirectory, "bad.jar");
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(maliciousJar))) {
			jos.putNextEntry(new JarEntry("../escaped.class"));
			jos.write(bytes);
			jos.closeEntry();
		}
	}

	protected void tearDown() throws Exception {
		deleteRecursively(tempDirectory);
		super.tearDown();
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

	private File createTypicalJar() throws IOException {
		File jar = new File(tempDirectory, "safe.jar");
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
			jos.putNextEntry(new JarEntry("META-INF/resource.txt"));
			jos.write("helloworld".getBytes());
			jos.closeEntry();
		}
		return jar;
	}

	private File createJarWithEntry(String entryName) throws IOException {
		File jar = new File(tempDirectory, "single.jar");
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
			jos.putNextEntry(new JarEntry(entryName));
			jos.write("data".getBytes());
			jos.closeEntry();
		}
		return jar;
	}

	private List<IMessage> addJarToWeaver(File jar) {
		CollectingMessageHandler messageHandler = new CollectingMessageHandler();
		BcelWeaver weaver = new BcelWeaver(new BcelWorld(new ArrayList<String>(), messageHandler, null));
		weaver.addJarFile(jar, outDir, false);
		return messageHandler.messages;
	}

	private void assertEntryAccepted(String entryName) throws IOException {
		List<IMessage> messages = addJarToWeaver(createJarWithEntry(entryName));
		assertTrue("expected '" + entryName + "' to be accepted, got: " + messages, messages.isEmpty());
	}

	private void assertEntryRejected(String entryName, String expectedReason) throws IOException {
		List<IMessage> messages = addJarToWeaver(createJarWithEntry(entryName));
		assertEquals("expected '" + entryName + "' to be rejected, got: " + messages, 1, messages.size());
		String text = messages.get(0).getMessage();
		assertTrue("expected message to name the entry, got: " + text, text.contains(entryName));
		assertTrue("expected message to contain '" + expectedReason + "', got: " + text, text.contains(expectedReason));
	}

	private static class CollectingMessageHandler implements IMessageHandler {
		final List<IMessage> messages = new ArrayList<>();

		public boolean handleMessage(IMessage message) {
			messages.add(message);
			return true;
		}

		public boolean isIgnoring(IMessage.Kind kind) {
			return false;
		}

		public void dontIgnore(IMessage.Kind kind) {
		}

		public void ignore(IMessage.Kind kind) {
		}
	}

	public void testNullOutputDirectoryIsTolerated() throws IOException {
		CollectingMessageHandler messageHandler = new CollectingMessageHandler();
		BcelWorld world = new BcelWorld(new ArrayList<String>(), messageHandler, null);
		BcelWeaver weaver = new BcelWeaver(world);
		List<UnwovenClassFile> added = weaver.addJarFile(createTypicalJar(), null, false);
		assertTrue("no class entries in the jar, so nothing should be added", added.isEmpty());
		assertTrue("expected no messages, got: " + messageHandler.messages, messageHandler.messages.isEmpty());
	}

	public void testMaliciousJarContentsCannotResolveOutsideOutputDirectory() throws IOException {
		CollectingMessageHandler messageHandler = new CollectingMessageHandler();
		BcelWorld world = new BcelWorld(new ArrayList<String>(), messageHandler, null);
		BcelWeaver weaver = new BcelWeaver(world);

		List<UnwovenClassFile> added = weaver.addJarFile(maliciousJar, outDir, false);
		assertTrue("expected the malicious entry to be rejected, not added", added.isEmpty());
		assertEquals("expected error message about the rejected entry", 1, messageHandler.messages.size());
		String messageText = messageHandler.messages.get(0).getMessage();
		assertTrue("expected message to name the offending entry, got: " + messageText,
				messageText.contains("escaped.class"));
		assertTrue("expected message to explain why, got: " + messageText,
				messageText.contains("outside destination directory"));
		assertFalse("entry must not have escaped the output directory",
				new File(tempDirectory, "escaped.class").exists());
	}

	public void testNormalNestedEntryIsAccepted() throws IOException {
	    assertEntryAccepted("META-INF/resource.txt");
	}

	public void testEntryThatNormalizesInsideOutDirIsAccepted() throws IOException {
	    assertEntryAccepted("a/../b.txt");
	}

	public void testEntryResolvingToOutDirItselfIsAccepted() throws IOException {
	    // pins the equality case now covered by Path.startsWith
	    assertEntryAccepted("a/..");
	}

	public void testEntryTraversingAboveOutDirIsRejected() throws IOException {
	    assertEntryRejected("a/../../x.txt", "outside destination directory");
	}

	public void testSiblingDirectorySharingPrefixIsRejected() throws IOException {
	    // outDir is <temp>/out, so ../out2/x.txt is a string-prefix match but not a child
	    assertEntryRejected("../out2/x.txt", "outside destination directory");
	}
	public void testAbsoluteEntryIsRejected() throws IOException {
	    // Path.resolve() replaces the base for an absolute argument
	    assertEntryRejected("/abs/x.txt", "outside destination directory");
	}

	public void testEntryWithNulCharacterIsReportedNotThrown() throws IOException {
	    // InvalidPathException is unchecked; it must surface as the normal error message
	    assertEntryRejected("bad\u0000name.txt", "not a valid path");
	}
}