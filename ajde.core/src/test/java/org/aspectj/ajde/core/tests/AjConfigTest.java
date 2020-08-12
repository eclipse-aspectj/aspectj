/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajde.core.JavaOptions;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.internal.AjdeCoreBuildManager;
import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;

/**
 * Tests that the AjBuildConfig is populated correctly from the ICompilerConfiguration
 */
public class AjConfigTest extends AjdeCoreTestCase {

	private TestCompilerConfiguration compilerConfig;
	private AjdeCoreBuildManager ajdeBuildManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("SimpleProject");
		ajdeBuildManager = new AjdeCoreBuildManager(getCompiler());
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ajdeBuildManager = null;
		compilerConfig = null;
	}

	public void testJavaOptionsMap() {
		Map<String,String> options = JavaOptions.getDefaultJavaOptions();
		options.put(JavaOptions.WARN_DEPRECATION, JavaOptions.WARNING);
		compilerConfig.setJavaOptions(options);
		Map<String,String> found = genAjBuildConfig().getOptions().getMap();
		String warning = found.get(JavaOptions.WARN_DEPRECATION);
		assertEquals("expected to be warning on deprecation but found setting " + " was " + warning, JavaOptions.WARNING, warning);
	}

	public void testAspectPath() {
		Set<File> aspects = new HashSet<>();
		compilerConfig.setAspectPath(aspects);
		AjBuildConfig buildConfig = genAjBuildConfig();
		List<File> aPath = buildConfig.getAspectpath();
		assertTrue("no aspect path", aPath.isEmpty());

		File f = new File("jarone.jar");
		aspects.add(f);
		buildConfig = genAjBuildConfig();
		List<File> aPath2 = buildConfig.getAspectpath();
		assertEquals("expected one entry on the aspectpath but found " + aPath2.size(), 1, aPath2.size());
		assertTrue("expected to find file " + f.getName() + " on the aspectpath" + " but didn't", aPath2.contains(f));

		File f2 = new File("jartwo.jar");
		aspects.add(f2);
		buildConfig = genAjBuildConfig();
		List<File> aPath3 = buildConfig.getAspectpath();
		assertEquals("expected two entries on the aspectpath but found " + aPath3.size(), 2, aPath3.size());
		assertTrue("expected to find file " + f.getName() + " on the aspectpath" + " but didn't", aPath3.contains(f));
		assertTrue("expected to find file " + f2.getName() + " on the aspectpath" + " but didn't", aPath3.contains(f2));
	}

	public void testInpath() {
		Set<File> jars = new HashSet<>();
		compilerConfig.setInpath(jars);
		AjBuildConfig buildConfig = genAjBuildConfig();
		List<File> inJars = buildConfig.getInpath();
		assertTrue("expected to find nothing on the inpath but found " + inJars, inJars.isEmpty());

		File f = new File("jarone.jar");
		jars.add(f);
		buildConfig = genAjBuildConfig();
		List<File> inJars2 = buildConfig.getInpath();
		assertTrue("expected to find one file on the inpath but found " + inJars2.size() + ": " + inJars2, inJars2.size() == 1);
		assertTrue("expected to find file " + f.getName() + " on the inpath" + " but didn't", inJars2.contains(f));

		File f2 = new File("jartwo.jar");
		jars.add(f2);
		buildConfig = genAjBuildConfig();
		List<File> inJars3 = buildConfig.getInpath();
		assertEquals("expected two entries on the inpath but found " + inJars3.size(), 2, inJars3.size());
		assertTrue("expected to find file " + f.getName() + " on the inpath" + " but didn't", inJars3.contains(f));
		assertTrue("expected to find file " + f2.getName() + " on the inpath" + " but didn't", inJars3.contains(f2));
	}

	public void testOutJar() {
		String outJar = "mybuild.jar";
		compilerConfig.setOutjar(outJar);
		AjBuildConfig buildConfig = genAjBuildConfig();
		assertNotNull("expected to find a non null output jar but " + "didn't", buildConfig.getOutputJar());
		assertEquals("expected to find outjar 'mybuild.jar' but instead " + "found " + buildConfig.getOutputJar().toString(),
				outJar, buildConfig.getOutputJar().toString());
	}

	public void testXHasMember() {
		compilerConfig.setNonStandardOptions("-XhasMember");
		AjBuildConfig buildConfig = genAjBuildConfig();
		assertTrue("expected XhasMember to be enabled but wasn't ", buildConfig.isXHasMemberEnabled());
	}

	public void testOutputLocationManager() {
		IOutputLocationManager mgr = compilerConfig.getOutputLocationManager();
		String expectedDefaultOutputDir = mgr.getDefaultOutputLocation().getAbsolutePath();
		AjBuildConfig buildConfig = genAjBuildConfig();
		String found = buildConfig.getCompilationResultDestinationManager().getDefaultOutputLocation().getAbsolutePath();
		assertEquals("expected to find default output location " + expectedDefaultOutputDir + " but found " + found,
				expectedDefaultOutputDir, found);
	}

	public void testSourcePathResources() {
		Map<String, File> m = new HashMap<>();
		m.put("newFile.txt", getWorkingDir());
		compilerConfig.setSourcePathResources(m);
		AjBuildConfig buildConfig = genAjBuildConfig();
		Map<String, File> found = buildConfig.getSourcePathResources();
		for (String resource : found.keySet()) {
			assertEquals("expected to find resource with name newFile.txt but " + "found " + resource, "newFile.txt", resource);
			File from = buildConfig.getSourcePathResources().get(resource);
			assertEquals("expected to find resource with file " + getWorkingDir() + "but found " + from, getWorkingDir(), from);
		}
	}

	public void testClasspath() {
		String classpath = compilerConfig.getClasspath();
		List<String> found = genAjBuildConfig().getClasspath();
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iterator = found.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			sb.append(name);
			if (iterator.hasNext()) {
				sb.append(File.pathSeparator);
			}
		}
		assertEquals("expected to find classpath " + classpath + " but found " + sb.toString(), classpath, sb.toString());
	}

	public void testNonStandardOptions() {
		compilerConfig.setNonStandardOptions("-XterminateAfterCompilation");
		AjBuildConfig buildConfig = genAjBuildConfig();
		assertTrue("XterminateAfterCompilation", buildConfig.isTerminateAfterCompilation());
		compilerConfig.setNonStandardOptions("-XserializableAspects");
		buildConfig = genAjBuildConfig();
		assertTrue("XserializableAspects", buildConfig.isXserializableAspects());
		compilerConfig.setNonStandardOptions("-XnoInline");
		buildConfig = genAjBuildConfig();
		assertTrue("XnoInline", buildConfig.isXnoInline());
		compilerConfig.setNonStandardOptions("-Xlint");
		buildConfig = genAjBuildConfig();
		assertEquals("Xlint", AjBuildConfig.AJLINT_DEFAULT, buildConfig.getLintMode());
		compilerConfig.setNonStandardOptions("-Xlint:error");
		buildConfig = genAjBuildConfig();
		assertEquals("Xlint", AjBuildConfig.AJLINT_ERROR, buildConfig.getLintMode());

		// and a few options thrown in at once
		compilerConfig.setNonStandardOptions("-Xlint -XnoInline -XserializableAspects");
		buildConfig = genAjBuildConfig();
		assertEquals("Xlint", AjBuildConfig.AJLINT_DEFAULT, buildConfig.getLintMode());
		assertTrue("XnoInline", buildConfig.isXnoInline());
		assertTrue("XserializableAspects", buildConfig.isXserializableAspects());
	}

	public void testProjectSourceFiles() throws IOException {
		String f = getAbsoluteProjectDir() + File.separator + "C.java";
		List<String> files = new ArrayList<>();
		files.add(f);
		compilerConfig.setProjectSourceFiles(files);
		AjBuildConfig buildConfig = genAjBuildConfig();
		String found = buildConfig.getFiles().get(0).getCanonicalPath();// AbsolutePath();
		assertEquals("expected source file " + f + ", but found " + found, f, found);
	}

	private AjBuildConfig genAjBuildConfig() {
		AjBuildConfig buildConfig = ajdeBuildManager.generateAjBuildConfig();
		assertNotNull("exepected to generate a non null AjBuildConfig but " + "didn't", buildConfig);
		return buildConfig;
	}

}
