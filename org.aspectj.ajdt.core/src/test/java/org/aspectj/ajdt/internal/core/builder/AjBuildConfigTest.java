/* *******************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.aspectj.ajdt.ajc.BuildArgParser;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;

import junit.framework.TestCase;

/**
 * @author Andy Clement
 */
public class AjBuildConfigTest extends TestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		testHandler.clear();
	}

	public void testBasicJar() {
		// There should be only one reference to foo.jar in the checked classpaths
		BuildArgParser buildArgParser = new BuildArgParser(testHandler);
		AjBuildConfig buildConfig = buildArgParser.genBuildConfig(toArgs("-classpath foo.jar"));
		Classpath[] checkedClasspaths = buildConfig.getCheckedClasspaths();
		checkOccurrencesOf(checkedClasspaths, "foo.jar",1);
		checkMessages();
	}

	public void testBasicDir() {
		// Directory on classpath
		BuildArgParser buildArgParser = new BuildArgParser(testHandler);
		AjBuildConfig buildConfig = buildArgParser.genBuildConfig(toArgs("-classpath /madeup/location"));
		Classpath[] checkedClasspaths = buildConfig.getCheckedClasspaths();
		checkOccurrencesOf(checkedClasspaths, "madeup/location",0);
		checkMessages();
	}

	public void testBasicDir2() {
		// Non existent directory on classpath
		BuildArgParser buildArgParser = new BuildArgParser(testHandler);
		AjBuildConfig buildConfig = new AjBuildConfig(buildArgParser);
		buildArgParser.populateBuildConfig(buildConfig, toArgs("-classpath /madeup/location"), true, null);
		Classpath[] checkedClasspaths = buildConfig.getCheckedClasspaths();
		checkOccurrencesOf(checkedClasspaths, "madeup/location",0);
		// There is no message about the location not existing in the set that is going to be checked
		// but the message is routed to the internal logger setup inside the JDT Main class.  By default
		// that is a string logger but when built through AspectJ Main.main() it will route that message
		// to System.out/System.err
		checkMessages();
	}

	public void testAspectPath() {
		// There should be only one reference to foo.jar in the checked classpaths
		BuildArgParser buildArgParser = new BuildArgParser(testHandler);
		AjBuildConfig buildConfig = buildArgParser.genBuildConfig(toArgs("-classpath foo.jar -aspectpath bar.jar"));
		Classpath[] checkedClasspaths = buildConfig.getCheckedClasspaths();
		checkOccurrencesOf(checkedClasspaths, "foo.jar", 1);
		checkOccurrencesOf(checkedClasspaths, "bar.jar", 0);
		checkMessages("skipping missing, empty or corrupt aspectpath entry: bar.jar");
	}

	public void testInPath() {
		// There should be only one reference to foo.jar in the checked classpaths
		BuildArgParser buildArgParser = new BuildArgParser(testHandler);
		AjBuildConfig buildConfig = buildArgParser.genBuildConfig(toArgs("-classpath foo.jar -inpath bar.jar"));
		Classpath[] checkedClasspaths = buildConfig.getCheckedClasspaths();
		checkOccurrencesOf(checkedClasspaths, "foo.jar", 1);
		checkOccurrencesOf(checkedClasspaths, "bar.jar", 0);
		checkMessages("skipping missing, empty or corrupt aspectpath entry: bar.jar");
	}

	public void testInJars() {
		// There should be only one reference to foo.jar in the checked classpaths
		BuildArgParser buildArgParser = new BuildArgParser(testHandler);
		AjBuildConfig buildConfig = buildArgParser.genBuildConfig(toArgs("-classpath foo.jar -injars bar.jar"));
		Classpath[] checkedClasspaths = buildConfig.getCheckedClasspaths();
		checkOccurrencesOf(checkedClasspaths, "foo.jar", 1);
		checkOccurrencesOf(checkedClasspaths, "bar.jar", 0);
		checkMessages("skipping missing, empty or corrupt aspectpath entry: bar.jar");
	}

	// TODO why does this misbehave on java8? (It doesn't remove the duplicate jar references when normalizing the classpath)
	public void xtestClashingJars() {
		File tempJar = createTempJar("foo");
		try {
			BuildArgParser buildArgParser = new BuildArgParser(testHandler);
			String[] args = toArgs("-classpath "+tempJar.getAbsolutePath()+
					" -inpath "+tempJar.getAbsolutePath()+" -aspectpath "+tempJar.getAbsolutePath());
			AjBuildConfig buildConfig = buildArgParser.genBuildConfig(args);
			Classpath[] checkedClasspaths = buildConfig.getCheckedClasspaths();
			System.out.println(Arrays.toString(checkedClasspaths));
			checkOccurrencesOf(checkedClasspaths, "/foo", 1);
			checkMessages();
		} finally {
			tempJar.delete();
		}
	}

	// ---

	private File createTempJar(String jarname) {
		try {
			File file = File.createTempFile(jarname, ".jar");
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			JarOutputStream jos = new JarOutputStream(new FileOutputStream(file),manifest);
			JarEntry je = new JarEntry("foo");
			je.setSize(0);
			jos.putNextEntry(je);
			jos.close();
			return file;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void checkMessages(String... expectedMessageSubstrings) {
		List<IMessage> messages = testHandler.getMessages();
		if (expectedMessageSubstrings.length == 0 && messages.size() != 0) {
			fail("Expected no messages but found:\n" + messages);
		}
		if (expectedMessageSubstrings.length != messages.size()) {
			fail("Incompatible number of actual messages (" + messages.size() + ") vs expected messages ("
					+ expectedMessageSubstrings + ")\n" + "expected:\n" + Arrays.toString(expectedMessageSubstrings)
					+ "\nactual:\n" + messages);
		}
	}

	private void checkOccurrencesOf(Classpath[] classpath, String string, int expectedCount) {
		int count = 0;
		for (Classpath cpentry : classpath) {
			// Example: /Users/aclement/gits/org.aspectj/org.aspectj.ajdt.core/foo.jar
			String path = cpentry.getPath();
			if (path.contains(string)) {
				count++;
			}
		}
		if (count != expectedCount) {
			fail("Did not find expected " + expectedCount + " occurrences of " + string + " in classpaths: "
					+ Arrays.toString(classpath));
		}
	}

	TestMessageHandler testHandler = new TestMessageHandler();

	static class TestMessageHandler implements IMessageHandler {

		List<IMessage> messages = new ArrayList<>();

		@Override
		public boolean isIgnoring(Kind kind) {
			return false;
		}

		public List<IMessage> getMessages() {
			return messages;
		}

		@Override
		public void ignore(Kind kind) {
		}

		@Override
		public boolean handleMessage(IMessage message) throws AbortException {
			messages.add(message);
			return true;
		}

		@Override
		public void dontIgnore(Kind kind) {
		}

		public void clear() {
			messages.clear();
		}
	};

	private String[] toArgs(String commandLine) {
		return commandLine.split(" ");
	}

}
