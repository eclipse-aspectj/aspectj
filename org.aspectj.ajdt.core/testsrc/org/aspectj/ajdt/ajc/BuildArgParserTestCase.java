/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.ajc;

import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.ajdt.internal.core.builder.AjCompilerOptions;
import org.aspectj.bridge.MessageWriter;
import org.aspectj.testing.util.TestUtil;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

/**
 * Some black-box test is happening here.
 */
public class BuildArgParserTestCase extends TestCase {

	private BuildArgParser parser = new BuildArgParser();	
	private static final String TEST_DIR = "testdata" + File.separator + "ajc" + File.separator;
	private MessageWriter messageWriter = new MessageWriter(new PrintWriter(System.out), false);

	public BuildArgParserTestCase(String name) {
		super(name);
	}

	public void testDefaultClasspathAndTargetCombo() throws InvalidInputException {
		String ENTRY = "1.jar;2.jar";
		final String classpath = System.getProperty("java.class.path");
		try {
            System.setProperty("java.class.path", ENTRY); // see finally below
    		AjBuildConfig config = parser.genBuildConfig(new String[] { }, messageWriter);
            String err = parser.getOtherMessages(true);       
            assertTrue(err, null == err);
            assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("1.jar"));
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("2.jar"));
    
    		config = parser.genBuildConfig(new String[] { "-1.3" }, messageWriter);
            err = parser.getOtherMessages(true);       
            assertTrue(err, null == err);
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("1.jar"));
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("2.jar"));
    
    		config = parser.genBuildConfig(new String[] { "-1.3" }, messageWriter);
            err = parser.getOtherMessages(true);       
            assertTrue(err, null == err);
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("1.jar"));
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("2.jar"));
    			
    		config = parser.genBuildConfig(new String[] { 
    			"-classpath", ENTRY, "-1.4" }, messageWriter);
            err = parser.getOtherMessages(true);       
            assertTrue("expected errors for missing jars", null != err);
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("1.jar"));
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("2.jar"));
    			
        } finally {
            // do finally to avoid messing up classpath for other tests
            System.setProperty("java.class.path", classpath);
            String setPath = System.getProperty("java.class.path");
            String m = "other tests will fail - classpath not reset";
            assertEquals(m, classpath, setPath); 
        }
	}
	
	public void testAjOptions() throws InvalidInputException {
		AjBuildConfig config = parser.genBuildConfig(new String[] {  "-Xlint" }, messageWriter);
 	
		assertTrue(
			"default options",
			config.getAjOptions().get(AjCompilerOptions.OPTION_Xlint).equals(
				CompilerOptions.GENERATE));			
	}

	public void testAspectpath() throws InvalidInputException {
		final String SOURCE_JAR = "testdata/testclasses.jar";
		final String SOURCE_JARS = "testdata/testclasses.jar" + File.pathSeparator 
			+ "../weaver/testdata/tracing.jar" + File.pathSeparator 
			+ "../weaver/testdata/dummyAspect.jar";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-aspectpath", SOURCE_JAR }, 
			messageWriter);
		
		assertTrue(((File)config.getAspectpath().get(0)).getName(), ((File)config.getAspectpath().get(0)).getName().equals("testclasses.jar"));

		config = parser.genBuildConfig(new String[] { 
			"-aspectpath", SOURCE_JARS }, 
			messageWriter);
		assertTrue("size", + config.getAspectpath().size() == 3);
	}

	public void testInJars() throws InvalidInputException {
		final String SOURCE_JAR = "testdata/testclasses.jar";
		final String SOURCE_JARS = "testdata/testclasses.jar" + File.pathSeparator 
			+ "../weaver/testdata/tracing.jar" + File.pathSeparator 
			+ "../weaver/testdata/dummyAspect.jar";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-injars", SOURCE_JAR }, 
			messageWriter);
		//XXX don't let this remain in both places in beta1			
		assertTrue(
			"" + config.getAjOptions().get(AjCompilerOptions.OPTION_InJARs),  
			config.getAjOptions().get(AjCompilerOptions.OPTION_InJARs).equals(CompilerOptions.PRESERVE));
		assertTrue(((File)config.getInJars().get(0)).getName(), ((File)config.getInJars().get(0)).getName().equals("testclasses.jar"));

		config = parser.genBuildConfig(new String[] { 
			"-injars", SOURCE_JARS }, 
			messageWriter);
		assertTrue("size", + config.getInJars().size() == 3);
	}

	public void testBadInJars() throws InvalidInputException {
		final String SOURCE_JARS = "testdata/testclasses.jar" + File.pathSeparator + "b.far" + File.pathSeparator + "c.jar";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-injars", SOURCE_JARS }, 
			messageWriter);
		assertTrue("size: " + config.getInJars().size(), config.getInJars().size() == 1);
	}

	public void testMultipleSourceRoots() throws InvalidInputException {
		final String SRCROOT_1 = "testdata/src1/p1";
		final String SRCROOT_2 = "testdata/ajc";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-sourceroots", SRCROOT_1 + File.pathSeparator + SRCROOT_2 }, 
			messageWriter);

		assertEquals(new File(SRCROOT_1).getAbsolutePath(), ((File)config.getSourceRoots().get(0)).getAbsolutePath());
		
		Collection expectedFiles = Arrays.asList(new File[] {
			new File(SRCROOT_1+File.separator+"A.java").getAbsoluteFile(),
			new File(SRCROOT_1+File.separator+"Foo.java").getAbsoluteFile(),
			new File(SRCROOT_2+File.separator+"A.java").getAbsoluteFile(),
			new File(SRCROOT_2+File.separator+"B.java").getAbsoluteFile(),
			new File(SRCROOT_2+File.separator+"X.aj").getAbsoluteFile(),
			new File(SRCROOT_2+File.separator+"Y.aj").getAbsoluteFile(),
			new File(SRCROOT_2+File.separator+"pkg"+File.separator+"Hello.java").getAbsoluteFile(),
		});
  
		//System.out.println(config.getFiles());

		TestUtil.assertSetEquals(expectedFiles, config.getFiles());
	}

	public void testSourceRootDir() throws InvalidInputException {
		final String SRCROOT = "testdata/ajc";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-sourceroots", SRCROOT }, 
			messageWriter);

		assertEquals(new File(SRCROOT).getAbsolutePath(), ((File)config.getSourceRoots().get(0)).getAbsolutePath());
		
		Collection expectedFiles = Arrays.asList(new File[] {
			new File(SRCROOT+File.separator+"A.java").getAbsoluteFile(),
			new File(SRCROOT+File.separator+"B.java").getAbsoluteFile(),
			new File(SRCROOT+File.separator+"X.aj").getAbsoluteFile(),
			new File(SRCROOT+File.separator+"Y.aj").getAbsoluteFile(),
			new File(SRCROOT+File.separator+"pkg"+File.separator+"Hello.java").getAbsoluteFile(),
		});

		//System.out.println(config.getFiles());

		TestUtil.assertSetEquals(expectedFiles, config.getFiles());
	}

	public void testBadSourceRootDir() throws InvalidInputException {
		AjBuildConfig config = parser.genBuildConfig(new String[] {   
			"-sourceroots", 
			"testdata/mumbleDoesNotExist;testdata/ajc" }, 
			messageWriter);

		assertTrue(config.getSourceRoots().toString(), config.getSourceRoots().size() == 1);

		config = parser.genBuildConfig(new String[] { 
			"-sourceroots" }, 
			messageWriter);

		assertTrue("" + config.getSourceRoots(), config.getSourceRoots().size() == 0);
			
	}

	//??? we've decided not to make this an error
	public void testSourceRootDirWithFiles() throws InvalidInputException {
		final String SRCROOT = "testdata/ajc/pkg";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-sourceroots", SRCROOT,  "testdata/src1/A.java"}, 
			messageWriter);

		assertEquals(new File(SRCROOT).getAbsolutePath(), ((File)config.getSourceRoots().get(0)).getAbsolutePath());
		
		Collection expectedFiles = Arrays.asList(new File[] {
			new File(SRCROOT+File.separator+"Hello.java").getAbsoluteFile(),
			new File("testdata"+File.separator+"src1"+File.separator+"A.java").getAbsoluteFile(),
		});

		TestUtil.assertSetEquals(expectedFiles, config.getFiles());
		
	}

	public void testExtDirs() throws InvalidInputException {
		final String DIR = "testdata";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-extdirs", DIR }, 
			messageWriter);
		assertTrue(config.getClasspath().toString(), config.getClasspath().contains(
			new File(DIR + File.separator + "testclasses.jar").getAbsolutePath()
		));
	}

	public void testBootclasspath() throws InvalidInputException {
		final String PATH = "mumble/rt.jar";
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-bootclasspath", PATH }, 
			messageWriter);		
		assertTrue(config.getClasspath().toString(), config.getClasspath().get(0).equals(PATH)); 

		config = parser.genBuildConfig(new String[] { 
			}, 
			messageWriter);		
		assertTrue(config.getClasspath().toString(), !config.getClasspath().get(0).equals(PATH)); 
	}

	public void testOutputJar() throws InvalidInputException {
		final String OUT_JAR = "testdata/testclasses.jar";
		
		AjBuildConfig config = parser.genBuildConfig(new String[] { 
			"-outjar", OUT_JAR }, 
			messageWriter);

		//XXX don't let this remain in both places in beta1
		assertTrue(
			"will generate: " + config.getAjOptions().get(AjCompilerOptions.OPTION_OutJAR),  
			config.getAjOptions().get(AjCompilerOptions.OPTION_OutJAR).equals(CompilerOptions.GENERATE));
		assertTrue(
			"testclasses jar: " + config.getOutputJar().getName(), 
			config.getOutputJar().getAbsolutePath().equals(new File(OUT_JAR).getAbsolutePath()));
	
		File nonExistingJar = new File("testdata/mumbleDoesNotExist.jar");
		config = parser.genBuildConfig(new String[] { 
			"-outjar", nonExistingJar.getAbsolutePath() }, 
			messageWriter);
		assertTrue(
			"testclasses jar: " + config.getOutputJar().getName(), 
			config.getOutputJar().getAbsolutePath().equals(nonExistingJar.getAbsolutePath()));	

		nonExistingJar.delete();
	}
	
	//XXX shouldn't need -1.4 to get this to pass
	public void testCombinedOptions() throws InvalidInputException {
		AjBuildConfig config = parser.genBuildConfig(new String[] {  "-Xlint", "-target", "1.4", "-1.4" }, messageWriter);
		String TARGET = "1.4";
		assertTrue(
			"target set",  
			config.getJavaOptions().get(CompilerOptions.OPTION_TargetPlatform).equals(TARGET));

		assertTrue(
			"Xlint option set",
			config.getAjOptions().get(AjCompilerOptions.OPTION_Xlint).equals(
				CompilerOptions.GENERATE));			
	}
	
	public void testOutputDirectorySetting() throws InvalidInputException {
		AjBuildConfig config = parser.genBuildConfig(new String[] {  "-d", TEST_DIR }, messageWriter);
		
		assertTrue(
			new File(config.getOutputDir().getPath()).getAbsolutePath() + " ?= " + 
			new File(TEST_DIR).getAbsolutePath(),
			config.getOutputDir().getAbsolutePath().equals((new File(TEST_DIR)).getAbsolutePath()));	
	}

	public void testClasspathSetting() throws InvalidInputException {
		String ENTRY = "1.jar;2.jar";
		AjBuildConfig config = parser.genBuildConfig(new String[] {  "-classpath", ENTRY }, messageWriter);
		
		assertTrue(
			config.getClasspath().toString(),
			config.getClasspath().contains("1.jar"));

		assertTrue(
			config.getClasspath().toString(),
			config.getClasspath().contains("2.jar"));
	}

	public void testArgInConfigFile() throws InvalidInputException {
		String FILE_PATH =   "@" + TEST_DIR + "configWithArgs.lst";
		String OUT_PATH = "bin";
		AjBuildConfig config = parser.genBuildConfig(new String[] { FILE_PATH }, messageWriter);
		
        assertNotNull(config);
        File outputDir = config.getOutputDir();
        assertNotNull(outputDir);        
		assertEquals(outputDir.getPath(), OUT_PATH);
	}

	public void testXlint() throws InvalidInputException {
		AjdtCommand command = new AjdtCommand();
		AjBuildConfig config = parser.genBuildConfig(new String[] {"-Xlint"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_DEFAULT));
		config = parser.genBuildConfig(new String[] {"-Xlint:warn"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_WARN));
		config = parser.genBuildConfig(new String[] {"-Xlint:error"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_ERROR));
		config = parser.genBuildConfig(new String[] {"-Xlint:ignore"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_INGORE));
	}

	public void testXlintfile() throws InvalidInputException {
		String lintFile = "testdata/lintspec.properties"; 
		String badLintFile = "lint.props";
		AjBuildConfig config = parser.genBuildConfig(new String[] {"-Xlintfile", lintFile}, messageWriter);
		assertTrue(new File(lintFile).exists());
		assertTrue(config.getLintSpecFile().getAbsolutePath(), config.getLintSpecFile().getAbsolutePath().equals(new File(lintFile).getAbsolutePath()));	
	}

	public void testOptions() throws InvalidInputException {
		AjdtCommand command = new AjdtCommand();
		String TARGET = "1.4";
		AjBuildConfig config = parser.genBuildConfig(new String[] {"-target", TARGET, "-source", TARGET}, messageWriter);
		assertTrue(
			"target set",  
			config.getJavaOptions().get(CompilerOptions.OPTION_TargetPlatform).equals(TARGET));
		assertTrue(
			"source set",  
			config.getJavaOptions().get(CompilerOptions.OPTION_Compliance).equals(CompilerOptions.VERSION_1_4));
	}
	
	public void testLstFileExpansion() throws IOException, FileNotFoundException, InvalidInputException {
		String FILE_PATH =  TEST_DIR + "config.lst";
		String SOURCE_PATH_1 = "A.java";
		String SOURCE_PATH_2 = "B.java";

        File f = new File(FILE_PATH);
		
		AjBuildConfig config = parser.genBuildConfig(new String[] { "@" + FILE_PATH }, messageWriter);
		List resultList = config.getFiles();
		
		assertTrue("correct number of files", resultList.size() == 2);	
		assertTrue(resultList.toString() + new File(TEST_DIR + SOURCE_PATH_1).getAbsoluteFile(),
			resultList.contains(new File(TEST_DIR + SOURCE_PATH_1).getAbsoluteFile()));
		assertTrue(resultList.toString() + SOURCE_PATH_2,
			resultList.contains(new File(TEST_DIR + SOURCE_PATH_2).getAbsoluteFile()));			
	}
	

	//??? do we need to remove this limitation
//	public void testArgInConfigFileAndRelativizingPathParam() throws InvalidInputException {
//		String FILE_PATH =   "@" + TEST_DIR + "configWithArgs.lst";
//		String OUT_PATH = TEST_DIR + "bin";
//		AjBuildConfig config = parser.genBuildConfig(new String[] { FILE_PATH });
//		
//		assertTrue(
//			config.getOutputDir().getPath() + " ?= " + OUT_PATH,
//			config.getOutputDir().getAbsolutePath().equals((new File(OUT_PATH)).getAbsolutePath()));	
//	}
	
	public void testAjFileInclusion() throws InvalidInputException {
		parser.genBuildConfig(new String[] { TEST_DIR + "X.aj", TEST_DIR + "Y.aj"}, messageWriter);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
