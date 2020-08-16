/********************************************************************
 * Copyright (c) 2008 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement          initial implementation
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.IOException;

import org.aspectj.ajde.core.ICompilerConfiguration;

/**
 * Testing the performance of incremental compilation as it would be in AJDT.
 * 
 * @author AndyClement
 */
public class IncrementalPerformanceTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	/**
	 * Build a project of 64 source files and no aspects.<br>
	 * <ul>
	 * <li>First build is a full build.
	 * <li>Second build is an incremental build with no changes at all.
	 * <li>Third build is an incremental build with just a source file touched (not changed).
	 * </ul>
	 * 
	 * <p>
	 * 162-dev, 28Aug08 times: Thinkpad T61p: 3203/3140/3234/3156 173/172/172/172 313/297/297/312
	 */
	public void testBuildingProject64Files() {
		String proj = "Proj64";

		// A full build:
		initialiseProject(proj);
		build(proj);
		checkWasFullBuild();
		long fullbuildtime = getTimeTakenForBuild(proj);
		System.out.println("Full build time: " + fullbuildtime + "ms");

		// An incremental build with no source file changes at all. What should happen?
		// We need to determine that nothing has to be done as fast as possible, this is all about
		// determining from the configuration that nothing has changed and returning as fast as possible. Any
		// delays here are unnecessary burden that will hurt every other kind of compilation.
		build(proj);
		checkWasntFullBuild();
		checkCompileWeaveCount(proj, 0, 0);
		long nochangebuild = getTimeTakenForBuild(proj);
		System.out.println("Incr build time for no changes at all: " + nochangebuild + "ms");

		// An incremental build with no source file changes at all *and* we tell the compiler there are
		// no source changes (so it doesn't need to check timestamps). super fast
		addProjectSourceFileChanged(proj, null);
		build(proj);
		checkWasntFullBuild();
		checkCompileWeaveCount(proj, 0, 0);
		long nochangebuildandDoTellCompiler = getTimeTakenForBuild(proj);
		System.out.println("Incr build time for no changes at all and telling the compiler that: " + nochangebuildandDoTellCompiler
				+ "ms");

		// Now we touch a file (C0.java) and call build. What should happen?
		// We need to determine what has changed, we'll do that by walking over the set of input files and
		// checking their last modified stamps. So although we won't rebuild a buildConfig object, we will
		// call lastModifiedTime() a lot to determine which file has changed.
		alter(proj, "inc1");
		build(proj);
		checkWasntFullBuild();
		checkCompileWeaveCount(proj, 1, 1);
		long whitespacechangeDontTellCompiler = getTimeTakenForBuild(proj);
		System.out.println("Incr build time for whitespace change: " + whitespacechangeDontTellCompiler + "ms");

		// Similar to previous test, touch that file, but this time tell the compiler which file has changed. What should happen?
		// As we are telling the compiler what has changed, it will not jump through hoops checking the last mod time of
		// every source file in the project configuration.
		alter(proj, "inc1");
		addProjectSourceFileChanged(proj, getProjectRelativePath(proj, "src/out/C0.java"));
		build(proj);
		checkWasntFullBuild();
		checkCompileWeaveCount(proj, 1, 1);
		long whitespacechangeDoTellCompiler = getTimeTakenForBuild(proj);
		System.out.println("Incr build time for whitespace change (where we tell the compiler what changed): "
				+ whitespacechangeDoTellCompiler + "ms");

		// Lets assert what really ought to be true
		assertTrue(nochangebuild < fullbuildtime);
		assertTrue(whitespacechangeDontTellCompiler < fullbuildtime);
		assertTrue(whitespacechangeDoTellCompiler < fullbuildtime);

		assertTrue(nochangebuild < whitespacechangeDontTellCompiler);
		// assertTrue(nochangebuild < whitespacechangeDoTellCompiler);

		// assertTrue(whitespacechangeDoTellCompiler < whitespacechangeDontTellCompiler);
	}

	/**
	 * Project dependencies are captured by using classpath. The dependee project has the bin folder for the project upon which it
	 * depends on its classpath. This can make it expensive when determining whether to build the dependee project as we may need to
	 * analyse all the classpath entries, we don't know which are project related. However, a new API in ICompilerConfiguration
	 * called getClasspathElementsWithModifiedContents() can be returned by an implementor to tell us which parts of the classpath
	 * to check.
	 */
	public void testBuildingTwoProjects() {

		String projA = "Proj64";
		String projB = "Dependee";

		// A full build:
		initialiseProject(projA);
		initialiseProject(projB);
		configureNewProjectDependency(projB, projA);
		build(projA);
		checkWasFullBuild();
		build(projB);
		checkWasFullBuild();

		alter(projA, "C43changeOne"); // C43 made package private
		build(projA);
		setNextChangeResponse(projB, ICompilerConfiguration.EVERYTHING);
		build(projB);
		long timeTakenWhenFullyAnalysingClasspath = getTimeTakenForBuild(projB);
		checkWasntFullBuild();

		alter(projA, "C43changeOne"); // C43 made package private
		build(projA);
		addClasspathEntryChanged(projB, getProjectRelativePath(projA, "bin").getPath());
		// waitForReturn();
		build(projB);
		long timeTakenWhenFullyToldSpecifically = getTimeTakenForBuild(projB);
		// waitFor10();
		checkWasntFullBuild();

		System.out.println("Without: " + timeTakenWhenFullyAnalysingClasspath + "ms   With: " + timeTakenWhenFullyToldSpecifically
				+ "ms");

	}

	// --- helper code ---

	@SuppressWarnings("unused")
	private void waitFor10() {
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void waitForReturn() {
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		testdataSrcDir = "../tests/incrementalPerformance";
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		testdataSrcDir = "../tests/multiIncremental";
	}

}