/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.ajc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.bridge.CountingMessageHandler;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageWriter;
import org.aspectj.org.eclipse.jdt.core.compiler.InvalidInputException;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.testing.util.TestUtil;

import junit.framework.TestCase;

/**
 * Some black-box test is happening here.
 */
public class BuildArgParserTestCase extends TestCase {

	private static final String TEST_DIR = Constants.TESTDATA_PATH + File.separator + "ajc" + File.separator;
	private MessageWriter messageWriter = new MessageWriter(new PrintWriter(System.out), false);

	public BuildArgParserTestCase(String name) {
		super(name);
	}
	
	private AjBuildConfig genBuildConfig(String[] args, IMessageHandler handler) {
		return new BuildArgParser(handler).genBuildConfig(args);
	}

	public void testDefaultClasspathAndTargetCombo() throws Exception {
		String ENTRY = "1.jar" + File.pathSeparator + "2.jar";
		final String classpath = System.getProperty("java.class.path");
		try {
            System.setProperty("java.class.path", ENTRY); // see finally below
            BuildArgParser parser = new BuildArgParser(messageWriter);
    		AjBuildConfig config = parser.genBuildConfig(new String[] { });
            /*String err = */parser.getOtherMessages(true);       
            //!!!assertTrue(err, null == err);
            assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("1.jar"));
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("2.jar"));
    
    		config = genBuildConfig(new String[] { "-1.3" }, messageWriter);
    		// these errors are deffered to the compiler now
            //err = parser.getOtherMessages(true);       
            //!!!assertTrue(err, null == err);
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("1.jar"));
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("2.jar"));
    
			parser = new BuildArgParser(messageWriter);
    		config = parser.genBuildConfig(new String[] { "-1.3" });
            /*err = */parser.getOtherMessages(true);       
            //!!!assertTrue(err, null == err);
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("1.jar"));
    		assertTrue(
    			config.getClasspath().toString(),
    			config.getClasspath().contains("2.jar"));
    			
    		config = genBuildConfig(new String[] { 
    			"-classpath", ENTRY, "-1.4" }, messageWriter);
			//			these errors are deffered to the compiler now
            //err = parser.getOtherMessages(true);       
            //assertTrue("expected errors for missing jars", null != err);
    		List cp = config.getClasspath();
    		boolean jar1Found = false;
    		boolean jar2Found = false;
			for (Object o : cp) {
				String element = (String) o;
				if (element.contains("1.jar")) jar1Found = true;
				if (element.contains("2.jar")) jar2Found = true;
			}
    		assertTrue(
    			config.getClasspath().toString(),
    			jar1Found);
    		assertTrue(
    			config.getClasspath().toString(),
    			jar2Found);
    			
        } finally {
            // do finally to avoid messing up classpath for other tests
            System.setProperty("java.class.path", classpath);
            String setPath = System.getProperty("java.class.path");
            String m = "other tests will fail - classpath not reset";
            assertEquals(m, classpath, setPath); 
        }
	}
	
	public void testPathResolutionFromConfigArgs() {
		String FILE_PATH =   "@" + TEST_DIR + "configWithClasspathExtdirsBootCPArgs.lst";
		AjBuildConfig config = genBuildConfig(new String[] { FILE_PATH }, messageWriter);
		List<String> classpath = config.getFullClasspath();
		// note that empty or corrupt jars are NOT included in the classpath
		// should have three entries, resolved relative to location of .lst file
		assertEquals("Three entries in classpath",3,classpath.size());
		Iterator<String> cpIter = classpath.iterator();
		try {
		    assertEquals("Should be relative to TESTDIR",new File(TEST_DIR+File.separator+"xyz").getCanonicalPath(),cpIter.next());
		    assertEquals("Should be relative to TESTDIR",new File(TEST_DIR+File.separator+"myextdir" + File.separator + "dummy.jar").getCanonicalPath(),cpIter.next());
		    assertEquals("Should be relative to TESTDIR",new File(TEST_DIR+File.separator+"abc.jar").getCanonicalPath(),cpIter.next());
			List<File> files = config.getFiles();
			assertEquals("Two source files",2,files.size());
			Iterator<File> fIter = files.iterator();
			assertEquals("Should be relative to TESTDIR",new File(TEST_DIR+File.separator+"Abc.java").getCanonicalFile(),fIter.next());
			assertEquals("Should be relative to TESTDIR",new File(TEST_DIR+File.separator+"xyz"+File.separator+"Def.aj").getCanonicalFile(),fIter.next());
		    
		} catch (IOException ex) {
		    fail("Test case failure attempting to create canonical path: " + ex);
		}
		
	}
	
	public void testAjOptions() throws InvalidInputException {
		AjBuildConfig config = genBuildConfig(new String[] {  "-Xlint" }, messageWriter);
 	
		assertTrue(
			"default options",
			config.getLintMode().equals(AjBuildConfig.AJLINT_DEFAULT));			
	}

	public void testAspectpath() throws InvalidInputException {
		final String SOURCE_JAR = Constants.TESTDATA_PATH + "/testclasses.jar";
		final String SOURCE_JARS = Constants.TESTDATA_PATH + "/testclasses.jar" + File.pathSeparator 
			+ "../weaver/testdata/tracing.jar" + File.pathSeparator 
			+ "../weaver/testdata/dummyAspect.jar";
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-aspectpath", SOURCE_JAR }, 
			messageWriter);
		
		assertTrue(config.getAspectpath().get(0).getName(), config.getAspectpath().get(0).getName().equals("testclasses.jar"));

		config = genBuildConfig(new String[] { 
			"-aspectpath", SOURCE_JARS }, 
			messageWriter);
		assertTrue("size", + config.getAspectpath().size() == 3);
	}

	public void testInJars() throws InvalidInputException {
		final String SOURCE_JAR = Constants.TESTDATA_PATH + "/testclasses.jar";
		final String SOURCE_JARS = Constants.TESTDATA_PATH + "/testclasses.jar" + File.pathSeparator 
			+ "../weaver/testdata/tracing.jar" + File.pathSeparator 
			+ "../weaver/testdata/dummyAspect.jar";
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-injars", SOURCE_JAR }, 
			messageWriter);
		//XXX don't let this remain in both places in beta1			
//		assertTrue(
//			"" + config.getAjOptions().get(AjCompilerOptions.OPTION_InJARs),  
//			config.getAjOptions().get(AjCompilerOptions.OPTION_InJARs).equals(CompilerOptions.PRESERVE));
		assertTrue(config.getInJars().get(0).getName(), config.getInJars().get(0).getName().equals("testclasses.jar"));

		config = genBuildConfig(new String[] { 
			"-injars", SOURCE_JARS }, 
			messageWriter);
		assertTrue("size", + config.getInJars().size() == 3);
	}

	public void testBadInJars() throws InvalidInputException {
		final String SOURCE_JARS = Constants.TESTDATA_PATH + "/testclasses.jar" + File.pathSeparator + "b.far" + File.pathSeparator + "c.jar";
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-injars", SOURCE_JARS }, 
			messageWriter);
		assertTrue("size: " + config.getInJars().size(), config.getInJars().size() == 1);
	}

	public void testBadPathToSourceFiles() {
		CountingMessageHandler countingHandler = new CountingMessageHandler(messageWriter);
		/*AjBuildConfig config = */genBuildConfig(new String[]{ "inventedDir/doesntexist/*.java"},countingHandler);
		assertTrue("Expected an error for the invalid path.",countingHandler.numMessages(IMessage.ERROR,false)==1);	
	}


	public void testMultipleSourceRoots() throws InvalidInputException, IOException {
		final String SRCROOT_1 = Constants.TESTDATA_PATH + "/src1/p1";
		final String SRCROOT_2 = Constants.TESTDATA_PATH + "/ajc";
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-sourceroots", SRCROOT_1 + File.pathSeparator + SRCROOT_2 }, 
			messageWriter);
		
		assertEquals(getCanonicalPath(new File(SRCROOT_1)), config.getSourceRoots().get(0).getAbsolutePath());
		
		Collection expectedFiles = Arrays.asList(new File[] {
			new File(SRCROOT_1+File.separator+"A.java").getCanonicalFile(),
			new File(SRCROOT_1+File.separator+"Foo.java").getCanonicalFile(),
			new File(SRCROOT_2+File.separator+"A.java").getCanonicalFile(),
			new File(SRCROOT_2+File.separator+"B.java").getCanonicalFile(),
			new File(SRCROOT_2+File.separator+"X.aj").getCanonicalFile(),
			new File(SRCROOT_2+File.separator+"Y.aj").getCanonicalFile(),
			new File(SRCROOT_2+File.separator+"pkg"+File.separator+"Hello.java").getCanonicalFile(),
		});
  
		//System.out.println(config.getFiles());

		TestUtil.assertSetEquals(expectedFiles, config.getFiles());
	}

	/**
	 * @param file
	 * @return String
	 */
	private String getCanonicalPath(File file) {
		String ret = "";
		try {
			ret = file.getCanonicalPath();
		} catch (IOException ioEx) {
			fail("Unable to canonicalize " + file + " : " + ioEx);
		}
		return ret;
	}

	public void testSourceRootDir() throws InvalidInputException, IOException {
		final String SRCROOT = Constants.TESTDATA_PATH + "/ajc";
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-sourceroots", SRCROOT }, 
			messageWriter);

		assertEquals(getCanonicalPath(new File(SRCROOT)), config.getSourceRoots().get(0).getAbsolutePath());
		
		Collection expectedFiles = Arrays.asList(new File[] {
			new File(SRCROOT+File.separator+"A.java").getCanonicalFile(),
			new File(SRCROOT+File.separator+"B.java").getCanonicalFile(),
			new File(SRCROOT+File.separator+"X.aj").getCanonicalFile(),
			new File(SRCROOT+File.separator+"Y.aj").getCanonicalFile(),
			new File(SRCROOT+File.separator+"pkg"+File.separator+"Hello.java").getCanonicalFile(),
		});

		//System.out.println(config.getFiles());

		TestUtil.assertSetEquals(expectedFiles, config.getFiles());
	}

	public void testBadSourceRootDir() throws InvalidInputException {
		AjBuildConfig config = genBuildConfig(new String[] {   
			"-sourceroots", 
			Constants.TESTDATA_PATH + "/mumbleDoesNotExist" + File.pathSeparator 
            + Constants.TESTDATA_PATH + "/ajc" }, 
			messageWriter);

		assertTrue(config.getSourceRoots().toString(), config.getSourceRoots().size() == 1);

		config = genBuildConfig(new String[] { 
			"-sourceroots" }, 
			messageWriter);

		assertTrue("" + config.getSourceRoots(), config.getSourceRoots().size() == 0);
			
	}

	//??? we've decided not to make this an error
	public void testSourceRootDirWithFiles() throws InvalidInputException, IOException {
		final String SRCROOT = Constants.TESTDATA_PATH + "/ajc/pkg";
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-sourceroots", SRCROOT,  Constants.TESTDATA_PATH + "/src1/A.java"}, 
			messageWriter);

		assertEquals(getCanonicalPath(new File(SRCROOT)), config.getSourceRoots().get(0).getAbsolutePath());
		
		Collection expectedFiles = Arrays.asList(new File[] {
			new File(SRCROOT+File.separator+"Hello.java").getCanonicalFile(),
			new File(Constants.TESTDATA_PATH +File.separator+"src1"+File.separator+"A.java").getCanonicalFile(),
		});

		TestUtil.assertSetEquals(expectedFiles, config.getFiles());
		
	}

	public void testExtDirs() throws Exception {
		final String DIR = Constants.TESTDATA_PATH;
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-extdirs", DIR }, 
			messageWriter);
		assertTrue(config.getClasspath().toString(), config.getClasspath().contains(
			new File(DIR + File.separator + "testclasses.jar").getCanonicalPath()
		));
	}

	public void testBootclasspath() throws InvalidInputException {
		final String PATH = "mumble" + File.separator + "rt.jar";
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-bootclasspath", PATH }, 
			messageWriter);		
		assertTrue("Should find '" + PATH + "' contained in the first entry of '" + config.getBootclasspath().toString(),
				config.getBootclasspath().get(0).contains(PATH));

		config = genBuildConfig(new String[] { 
			}, 
			messageWriter);		
		assertTrue(config.getBootclasspath().toString(), !config.getBootclasspath().get(0).equals(PATH)); 
	}

	public void testOutputJar() throws InvalidInputException {
		final String OUT_JAR = Constants.TESTDATA_PATH + "/testclasses.jar";
		
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-outjar", OUT_JAR }, 
			messageWriter);

		//XXX don't let this remain in both places in beta1
//		assertTrue(
//			"will generate: " + config.getAjOptions().get(AjCompilerOptions.OPTION_OutJAR),  
//			config.getAjOptions().get(AjCompilerOptions.OPTION_OutJAR).equals(CompilerOptions.GENERATE));
		assertEquals(
			getCanonicalPath(new File(OUT_JAR)),config.getOutputJar().getAbsolutePath()); 
	
		File nonExistingJar = new File(Constants.TESTDATA_PATH + "/mumbleDoesNotExist.jar");
		config = genBuildConfig(new String[] { 
			"-outjar", nonExistingJar.getAbsolutePath() }, 
			messageWriter);
		assertEquals(
			getCanonicalPath(nonExistingJar), 
			config.getOutputJar().getAbsolutePath());	

		nonExistingJar.delete();
	}
	
	//XXX shouldn't need -1.4 to get this to pass
	public void testCombinedOptions() throws InvalidInputException {
		AjBuildConfig config = genBuildConfig(new String[] {  "-Xlint:error", "-target", "1.4"}, messageWriter);
		assertTrue(
				"target set",  
				config.getOptions().targetJDK == ClassFileConstants.JDK1_4); 

		assertTrue(
			"Xlint option set",
			config.getLintMode().equals(AjBuildConfig.AJLINT_ERROR));			
	}
	
	public void testOutputDirectorySetting() throws InvalidInputException {
		AjBuildConfig config = genBuildConfig(new String[] {  "-d", TEST_DIR }, messageWriter);
		
		assertTrue(
			new File(config.getOutputDir().getPath()).getAbsolutePath() + " ?= " + 
			new File(TEST_DIR).getAbsolutePath(),
			config.getOutputDir().getAbsolutePath().equals((new File(TEST_DIR)).getAbsolutePath()));	
	}

	public void testClasspathSetting() throws InvalidInputException {
		String ENTRY = "1.jar" + File.pathSeparator + "2.jar";
		AjBuildConfig config = genBuildConfig(new String[] {  "-classpath", ENTRY }, messageWriter);
		
   		List cp = config.getClasspath();
		boolean jar1Found = false;
		boolean jar2Found = false;
		for (Object o : cp) {
			String element = (String) o;
			if (element.contains("1.jar")) jar1Found = true;
			if (element.contains("2.jar")) jar2Found = true;
		}
		assertTrue(
			config.getClasspath().toString(),
			jar1Found);
		assertTrue(
			config.getClasspath().toString(),
			jar2Found);
	}

	public void testArgInConfigFile() throws InvalidInputException {
		String FILE_PATH =   "@" + TEST_DIR + "configWithArgs.lst";
		String OUT_PATH = "bin";
		AjBuildConfig config = genBuildConfig(new String[] { FILE_PATH }, messageWriter);
		
        assertNotNull(config);
        File outputDir = config.getOutputDir();
        assertNotNull(outputDir);        
		assertEquals(outputDir.getPath(), OUT_PATH);
	}

	public void testNonExistentConfigFile() throws IOException {
		String FILE_PATH =   "@" + TEST_DIR + "../bug-40257/d1/test.lst";
		AjBuildConfig config = genBuildConfig(new String[] { FILE_PATH }, messageWriter);

		String a = new File(TEST_DIR + "../bug-40257/d1/A.java").getCanonicalPath();
		String b = new File(TEST_DIR + "../bug-40257/d1/d2/B.java").getCanonicalPath();
		String c = new File(TEST_DIR + "../bug-40257/d3/C.java").getCanonicalPath();
		List pathList = new ArrayList();
		for (File file : config.getFiles()) {
			pathList.add(file.getCanonicalPath());
		}
		assertTrue(pathList.contains(a));
		assertTrue(pathList.contains(b));
		assertTrue(pathList.contains(c));
			
	}

	public void testXlint() throws InvalidInputException {
//		AjdtCommand command = new AjdtCommand();
		AjBuildConfig config = genBuildConfig(new String[] {"-Xlint"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_DEFAULT));
		config = genBuildConfig(new String[] {"-Xlint:warn"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_WARN));
		config = genBuildConfig(new String[] {"-Xlint:error"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_ERROR));
		config = genBuildConfig(new String[] {"-Xlint:ignore"}, messageWriter);
		assertTrue("", config.getLintMode().equals(AjBuildConfig.AJLINT_IGNORE));
	}

	public void testXlintfile() throws InvalidInputException {
		String lintFile = Constants.TESTDATA_PATH + "/lintspec.properties"; 
//		String badLintFile = "lint.props";
		AjBuildConfig config = genBuildConfig(new String[] {"-Xlintfile", lintFile}, messageWriter);
		assertTrue(new File(lintFile).exists());
		assertEquals(getCanonicalPath(new File(lintFile)),config.getLintSpecFile().getAbsolutePath());	
	}
	
	/**
	 * The option '-1.5' are currently eaten by the AspectJ argument parser - since
	 * the JDT compiler upon which we are based doesn't understand them - *this should change* when we
	 * switch to a 1.5 compiler base.  They are currently used to determine whether the weaver should
	 * behave in a '1.5' way - for example autoboxing behaves differently when the 1.5 flag is specified.
	 * Under 1.4 Integer != int
	 * Under 1.5 Integer == int
	 * (this applies to all primitive types)
	 */
	public void testSource15() throws InvalidInputException {
//		AjBuildConfig config = genBuildConfig(new String[]{"-source","1.5"},messageWriter);
//		assertTrue("should be in 1.5 mode",config.getJave5Behaviour());
		AjBuildConfig config = genBuildConfig(new String[]{"-1.5"},messageWriter);
		assertTrue("should be in 1.5 mode",config.getBehaveInJava5Way());
		config = genBuildConfig(new String[]{"-source","1.4"},messageWriter);
		assertTrue("should not be in 1.5 mode",!config.getBehaveInJava5Way());
		assertTrue("should be in 1.4 mode",config.getOptions().sourceLevel == ClassFileConstants.JDK1_4);
		config = genBuildConfig(new String[]{"-source","1.3"},messageWriter);
		assertTrue("should not be in 1.5 mode",!config.getBehaveInJava5Way());
		assertTrue("should be in 1.3 mode",config.getOptions().sourceLevel == ClassFileConstants.JDK1_3);
	}

	public void testOptions() throws InvalidInputException {
//		AjdtCommand command = new AjdtCommand();
		String TARGET = "1.4";
		AjBuildConfig config = genBuildConfig(new String[] {"-target", TARGET, "-source", TARGET}, messageWriter);
		assertTrue(
			"target set",  
			config.getOptions().targetJDK == ClassFileConstants.JDK1_4);
		assertTrue(
			"source set",  
			config.getOptions().sourceLevel == ClassFileConstants.JDK1_4);
	}
	
	public void testLstFileExpansion() throws IOException, FileNotFoundException, InvalidInputException {
		String FILE_PATH =  TEST_DIR + "config.lst";
		String SOURCE_PATH_1 = "A.java";
		String SOURCE_PATH_2 = "B.java";

//        File f = new File(FILE_PATH);
		
		AjBuildConfig config = genBuildConfig(new String[] { "@" + FILE_PATH }, messageWriter);
		List resultList = config.getFiles();
		
		assertTrue("correct number of files", resultList.size() == 2);	
		assertTrue(resultList.toString() + new File(TEST_DIR + SOURCE_PATH_1).getCanonicalFile(),
			resultList.contains(new File(TEST_DIR + SOURCE_PATH_1).getCanonicalFile()));
		assertTrue(resultList.toString() + SOURCE_PATH_2,
			resultList.contains(new File(TEST_DIR + SOURCE_PATH_2).getCanonicalFile()));			
	}
	

	//??? do we need to remove this limitation
//	public void testArgInConfigFileAndRelativizingPathParam() throws InvalidInputException {
//		String FILE_PATH =   "@" + TEST_DIR + "configWithArgs.lst";
//		String OUT_PATH = TEST_DIR + "bin";
//		AjBuildConfig config = genBuildConfig(new String[] { FILE_PATH });
//		
//		assertTrue(
//			config.getOutputDir().getPath() + " ?= " + OUT_PATH,
//			config.getOutputDir().getAbsolutePath().equals((new File(OUT_PATH)).getAbsolutePath()));	
//	}
	
	public void testAjFileInclusion() throws InvalidInputException {
		genBuildConfig(new String[] { TEST_DIR + "X.aj", TEST_DIR + "Y.aj"}, messageWriter);
	}
	
	public void testOutxml () {
		IMessageHolder messageHolder = new MessageHandler();
		AjBuildConfig config = genBuildConfig(new String[] { "-outxml", "-showWeaveInfo" }, messageHolder);
        assertTrue("Warnings: " + messageHolder,!messageHolder.hasAnyMessage(IMessage.WARNING, true));
		assertEquals("Wrong outxml","META-INF/aop-ajc.xml",config.getOutxmlName());
		assertTrue("Following option currupted",config.getShowWeavingInformation());
	}
	
	public void testOutxmlfile () {
		IMessageHolder messageHolder = new MessageHandler();
		AjBuildConfig config = genBuildConfig(new String[] { "-outxmlfile", "custom/aop.xml", "-showWeaveInfo" }, messageHolder);
        assertTrue("Warnings: " + messageHolder,!messageHolder.hasAnyMessage(IMessage.WARNING, true));
		assertEquals("Wrong outxml","custom/aop.xml",config.getOutxmlName());
		assertTrue("Following option currupted",config.getShowWeavingInformation());
	}

	public void testNonstandardInjars() {
		AjBuildConfig config = setupNonstandardPath("-injars");
		assertEquals("bad path: " + config.getInJars(), 3, config.getInJars().size());
	}
	
	public void testNonstandardInpath() {
		AjBuildConfig config = setupNonstandardPath("-inpath");
		assertEquals("bad path: " + config.getInpath(), 3, config.getInpath().size());
	}
	
	public void testNonstandardAspectpath() {
		AjBuildConfig config = setupNonstandardPath("-aspectpath");
		assertEquals("bad path: " + config.getAspectpath(), 3, config.getAspectpath().size());
	}

	public void testNonstandardClasspath() throws IOException {
		AjBuildConfig config = setupNonstandardPath("-classpath");
		checkPathSubset(config.getClasspath());
	}
	
	public void testNonstandardBootpath() throws IOException {
		AjBuildConfig config = setupNonstandardPath("-bootclasspath");
		checkPathSubset(config.getBootclasspath());
	}
	
	private void checkPathSubset(List path) throws IOException {
		String files[] = { "aspectjJar.file", "jarChild", "parent.zip" };
		for (String s : files) {
			File file = new File(NONSTANDARD_JAR_DIR + s);
			assertTrue("bad path: " + path, path.contains(file.getCanonicalPath()));
		}
	}

	public void testNonstandardOutjar() {
		final String OUT_JAR = NONSTANDARD_JAR_DIR + File.pathSeparator + "outputFile";
		
		AjBuildConfig config = genBuildConfig(new String[] { 
			"-outjar", OUT_JAR }, 
			messageWriter);

		File newJar = new File(OUT_JAR);
		assertEquals(
			getCanonicalPath(newJar),config.getOutputJar().getAbsolutePath()); 
	
		newJar.delete();
	}

	public void testNonstandardOutputDirectorySetting() throws InvalidInputException {
		String filePath = Constants.TESTDATA_PATH + File.separator + "ajc.jar" + File.separator;
		File testDir = new File(filePath);
		AjBuildConfig config = genBuildConfig(new String[] {  "-d", filePath }, messageWriter);
		
		assertEquals(testDir.getAbsolutePath(), config.getOutputDir().getAbsolutePath());	
	}
	
	private static final String NONSTANDARD_JAR_DIR = Constants.TESTDATA_PATH + "/OutjarTest/folder.jar/";
	
	private AjBuildConfig setupNonstandardPath(String pathType) {
		String NONSTANDARD_PATH_ENTRY = NONSTANDARD_JAR_DIR+"aspectjJar.file" + File.pathSeparator + NONSTANDARD_JAR_DIR+"aspectJar.file" + File.pathSeparator + NONSTANDARD_JAR_DIR+"jarChild" + File.pathSeparator + NONSTANDARD_JAR_DIR+"parent.zip";		
		
		return genBuildConfig(new String[] { 
			pathType, NONSTANDARD_PATH_ENTRY }, 
			messageWriter);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
