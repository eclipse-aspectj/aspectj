/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Matthew Webster         initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ltw;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.tools.WeavingAdaptor;

import junit.framework.Test;

public class LTWTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(LTWTests.class);
  }

  protected java.net.URL getSpecFile() {
	    return getClassResource("ltw.xml");
  }
  
  public void testInclusionAndPattern() {
    runTest("Inclusion and patterns");  		
  }
	
  public void testExclusionAndPattern() {
	runTest("Exclusion and patterns");  		
  }
	  	
  public void testAndPatternsAspects() {
	runTest("And patterns aspects");  		
  }
	  	  	
  	public void test001(){
  		runTest("Ensure 1st aspect is rewoven when weaving 2nd aspect");
  	}

	public void testOutxmlFile (){
	    runTest("Ensure valid aop.xml file is generated");
	}
	public void testOutxmlJar (){
	    runTest("Ensure valid aop.xml is generated for -outjar");
	}
	
  	public void testNoAopxml(){
  		setSystemProperty(WeavingAdaptor.WEAVING_ADAPTOR_VERBOSE,"true");
  		runTest("Ensure no weaving without visible aop.xml");
  	}

	public void testDefineConcreteAspect(){
  		runTest("Define concrete sub-aspect using aop.xml");
  	}

  	public void testDeclareAbstractAspect(){
//		setSystemProperty(WeavingAdaptor.WEAVING_ADAPTOR_VERBOSE,"true");
//		setSystemProperty(WeavingAdaptor.SHOW_WEAVE_INFO_PROPERTY,"true");
  		runTest("Use abstract aspect for ITD using aop.xml");
  	}

  	public void testAspectsInclude () {
  		runTest("Ensure a subset of inherited aspects is used for weaving");
  	}

  	public void testAspectsIncludeWithLintWarning () {
  		runTest("Ensure weaver lint warning issued when an aspect is not used for weaving");
  	}

  	public void testXsetEnabled () {
  		runTest("Set Xset properties enabled");
  	}
  	public void testXsetDisabled () {
  		runTest("Set Xset properties disabled");
  	}
  	
  	public void testXlintfileEmpty () {
  		runTest("Empty Xlint.properties file");
  	}

  	public void testXlintfileMissing () {
  		runTest("Warning with missing Xlint.properties file");
  	}

  	public void testXlintWarningAdviceDidNotMatchSuppressed () {
  		runTest("Warning when advice doesn't match suppressed for LTW");
  	}

  	public void testXlintfile () {
  		runTest("Override suppressing of warning when advice doesn't match using -Xlintfile");
  	}

  	public void testXlintDefault () {
  		runTest("Warning when advice doesn't match using -Xlint:default");
  	}

  	public void testXlintWarning () {
  		runTest("Override suppressing of warning when advice doesn't match using -Xlint:warning");
  	}
  	
  	public void testNonstandardJarFiles() {
  		runTest("Nonstandard jar file extensions");  		
  	}
  	
  	public void testOddzipOnClasspath() {  	
		runTest("Odd zip on classpath");  		
  	}
  	
  	public void testJ14LTWWithXML() {  	
		runTest("JDK14 LTW with XML");
  	}
  	
//  	public void testJ14LTWWithASPECTPATH() {  	
//		runTest("JDK14 LTW with ASPECTPATH");  		
//  	}
  	

    //public void testDiscardingWovenTypes() { 
    //  runTest("discarding woven types - 1");
    //}
      
    public void testWeavingTargetOfCallAggressivelyInLTW_DeclareParents_pr133770() {
	  runTest("aggressive ltw - decp");
    }

    public void testWeavingTargetOfCallAggressivelyInLTW_DeclareParents_pr133770_Deactivate() {
	  runTest("aggressive ltw - decp - deactivate");
    }

    public void testWeavingTargetOfCallAggressivelyInLTW_DeclareParents_Nested_pr133770() {
  	  runTest("aggressive ltw - decp - 2");
    }
    
    public void testWeavingTargetOfCallAggressivelyInLTW_DeclareParents_Hierarchy_pr133770() {
      runTest("aggressive ltw - hierarchy");
    }
    
  	public void testSeparateCompilationDeclareParentsCall_pr133770() {
  		runTest("separate compilation with ltw: declare parents and call");
  	}
  	
  	public void testConfigurationSystemProperty_pr149289() {
  		runTest("override default path using -Dorg.aspectj.weaver.loadtime.configuration");
  	}
  	
  	public void testSimpleLTW_pr159854 () {
  		runTest("simple LTW");
  	}
  	
  	public void testDumpOnError_pr155033 () {
  		runTest("dump on error");

  		File dir = getSandboxDirectory();
        CountingFilenameFilter cff = new CountingFilenameFilter(".txt");
        dir.listFiles(cff);
        assertEquals("Missing ajcore file in " + dir.getAbsolutePath(),1,cff.getCount());
	}
  	
  	public void testMultipleDumpOnError_pr155033 () {
  		runTest("multiple dump on error");

  		File dir = getSandboxDirectory();
        CountingFilenameFilter cff = new CountingFilenameFilter(".txt");
        dir.listFiles(cff);
        assertEquals("Missing ajcore file in " + dir.getAbsolutePath(),2,cff.getCount());
	}
  	  	
  	/*
  	 * Allow system properties to be set and restored
  	 * TODO maw move to XMLBasedAjcTestCase or RunSpec
  	 */
	private final static String NULL = "null";

	private Properties savedProperties;
  	
	protected void setSystemProperty (String key, String value) {
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
		for (Enumeration enu = savedProperties.keys(); enu.hasMoreElements(); ) {
			String key = (String)enu.nextElement();
			String value = savedProperties.getProperty(key);
			if (value == NULL) systemProperties.remove(key);
			else systemProperties.setProperty(key,value);
		}
	}
}