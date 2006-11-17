/**********************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer - initial version
...
**********************************************************************/
package org.aspectj.ajde;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestSuite;

import org.aspectj.ajde.NullIdeTaskListManager.SourceLineTask;
import org.aspectj.ajde.internal.CompilerAdapter;
import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.ajde.ui.internal.AjcBuildOptions;
import org.aspectj.ajde.ui.internal.UserPreferencesStore;
import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.util.LangUtil;

/**
 * Tests that a correctly populated AjBuildConfig object is created
 * in reponse to the setting in BuildOptionsAdapter and 
 * ProjectPropretiesAdapter
 */
public class BuildConfigurationTests extends AjdeTestCase {

	private CompilerAdapter compilerAdapter;
	private AjBuildConfig buildConfig = null;
	private AjcBuildOptions buildOptions = null;
	private UserPreferencesAdapter preferencesAdapter = null;
	private NullIdeProperties projectProperties = null;
    private NullIdeTaskListManager taskListManager;
	private static final String configFile = 
		AjdeTests.testDataPath("examples/figures-coverage/all.lst");
	

	public BuildConfigurationTests( String name ) {
		super( name );	
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(BuildConfigurationTests.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(BuildConfigurationTests.class);	
		return result;
	}


	// The tests...
	public void testCharacterEncoding() {
		buildOptions.setCharacterEncoding( "UTF-8" );
		buildConfig = compilerAdapter.genBuildConfig( configFile );
        assertTrue(configFile + " failed", null != buildConfig);			
		Map options = buildConfig.getOptions().getMap();
		String encoding = (String) options.get( CompilerOptions.OPTION_Encoding );
		assertEquals( "character encoding", "UTF-8", encoding );
	}

	public void testComplianceLevelJava13() {
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_13 );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);		
		assertEquals( "compliance level", CompilerOptions.VERSION_1_3, compliance);
		assertEquals( "source level", CompilerOptions.VERSION_1_3, sourceLevel );
	}
	
	public void testComplianceLevelJava14() {
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_14 );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);		
		assertEquals( "compliance level", CompilerOptions.VERSION_1_4, compliance);
		assertEquals( "source level", CompilerOptions.VERSION_1_4, sourceLevel );
	}

	public void testCompilanceLevelJava6() {
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_16 );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);
		
		if (Ajde.getDefault().compilerIsJava6Compatible()) {
			assertEquals("expected compliance level to be 1.6 but found " + compliance, "1.6", compliance);
			assertEquals("expected source level to be 1.6 but found " + sourceLevel, "1.6", sourceLevel );
			assertTrue("expected to 'behaveInJava5Way' but aren't",buildConfig.getBehaveInJava5Way());			
		} else {
			List l = taskListManager.getSourceLineTasks();
			String expectedError = "Java 6.0 compliance level is unsupported";
			String found = ((SourceLineTask)l.get(0)).getContainedMessage().getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" +
					" error message but found " + found ,expectedError,found);
		}
	}

	public void testSourceCompatibilityLevelJava6() {
		buildOptions.setSourceCompatibilityLevel(BuildOptionsAdapter.VERSION_16 );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);
		
		if (Ajde.getDefault().compilerIsJava6Compatible()) {
			assertEquals("expected compliance level to be 1.6 but found " + compliance, "1.6", compliance);
			assertEquals("expected source level to be 1.6 but found " + sourceLevel, "1.6", sourceLevel );
			assertTrue("expected to 'behaveInJava5Way' but aren't",buildConfig.getBehaveInJava5Way());			
		} else {
			List l = taskListManager.getSourceLineTasks();
			String expectedError = "Java 6.0 source level is unsupported";
			String found = ((SourceLineTask)l.get(0)).getContainedMessage().getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" +
					" error message but found " + found ,expectedError,found);
		}
	}
	
	public void testCompilanceLevelJava5() {
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_15 );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);		
		assertEquals("expected compliance level to be 1.5 but found " + compliance, CompilerOptions.VERSION_1_5, compliance);
		assertEquals("expected source level to be 1.5 but found " + sourceLevel, CompilerOptions.VERSION_1_5, sourceLevel );
		assertTrue("expected to 'behaveInJava5Way' but aren't",buildConfig.getBehaveInJava5Way());
	}
	
	public void testSourceCompatibilityLevel() {
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_13);
		buildOptions.setSourceCompatibilityLevel( BuildOptionsAdapter.VERSION_14);
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);		
		assertEquals( "compliance level", CompilerOptions.VERSION_1_3, compliance);
		assertEquals( "source level", CompilerOptions.VERSION_1_4, sourceLevel );		
	}
	
	public void testSourceIncompatibilityLevel() {
		// this config should "fail" and leave source level at 1.4
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_14);
		buildOptions.setSourceCompatibilityLevel( BuildOptionsAdapter.VERSION_13);
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);		
		assertEquals( "compliance level", CompilerOptions.VERSION_1_4, compliance);
		assertEquals( "source level", CompilerOptions.VERSION_1_4, sourceLevel );		
	}

	public void testSourceCompatibilityLevelJava5() {
		buildOptions.setSourceCompatibilityLevel( BuildOptionsAdapter.VERSION_15);
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);		
		assertEquals("expected compliance level to be 1.5 but found " + compliance, CompilerOptions.VERSION_1_5, compliance);
		assertEquals("expected source level to be 1.5 but found " + sourceLevel, CompilerOptions.VERSION_1_5, sourceLevel );
		assertTrue("expected to 'behaveInJava5Way' but aren't",buildConfig.getBehaveInJava5Way());
	}
	
	public void testSourceIncompatibilityLevelJava5() {
		// because compliance is set to be 1.5 then source compatibility
		// will be set to 1.5
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_15);
		buildOptions.setSourceCompatibilityLevel( BuildOptionsAdapter.VERSION_14);
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String compliance = (String) options.get(CompilerOptions.OPTION_Compliance);
		String sourceLevel = (String) options.get(CompilerOptions.OPTION_Source);		
		assertEquals("expected compliance level to be 1.5 but found " + compliance, CompilerOptions.VERSION_1_5, compliance);
		assertEquals("expected source level to be 1.5 but found " + sourceLevel, CompilerOptions.VERSION_1_5, sourceLevel );
		assertTrue("expected to 'behaveInJava5Way' but aren't",buildConfig.getBehaveInJava5Way());
	}
	
	public void testNullWarnings() {
		buildOptions.setWarnings( null );	
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		
		// this should leave us with the default warnings
		assertOptionEquals( "report overriding package default",
							options, 
						    CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report method with cons name",
							options, 
						    CompilerOptions.OPTION_ReportMethodWithConstructorName,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report deprecation",
							options, 
						    CompilerOptions.OPTION_ReportDeprecation,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report hidden catch block",
							options, 
						    CompilerOptions.OPTION_ReportHiddenCatchBlock,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report unused local",
							options, 
						    CompilerOptions.OPTION_ReportUnusedLocal,
						    CompilerOptions.IGNORE);
		assertOptionEquals( "report unused param",
							options, 
						    CompilerOptions.OPTION_ReportUnusedParameter,
						    CompilerOptions.IGNORE);
		assertOptionEquals( "report synthectic access",
							options, 
						    CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
						    CompilerOptions.IGNORE);
		assertOptionEquals( "report non-externalized string literal",
							options, 
						    CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
						    CompilerOptions.IGNORE);
		assertOptionEquals( "report assert identifer",
							options, 
						    CompilerOptions.OPTION_ReportAssertIdentifier,
						    CompilerOptions.WARNING);						    
	}
	
//	public void testEmptyWarnings() {
//		buildOptions.setWarnings( new HashSet() );	
//		buildConfig = compilerAdapter.genBuildConfig( configFile );			
//		Map options = buildConfig.getJavaOptions();
//		
//		// this should leave us with the user specifiable warnings
//		// turned off
//		assertOptionEquals( "report overriding package default",
//							options, 
//						    CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
//						    CompilerOptions.WARNING);
//		assertOptionEquals( "report method with cons name",
//							options, 
//						    CompilerOptions.OPTION_ReportMethodWithConstructorName,
//						    CompilerOptions.WARNING);
//		assertOptionEquals( "report deprecation",
//							options, 
//						    CompilerOptions.OPTION_ReportDeprecation,
//						    CompilerOptions.WARNING); 
//		assertOptionEquals( "report hidden catch block",
//							options, 
//						    CompilerOptions.OPTION_ReportHiddenCatchBlock,
//						    CompilerOptions.WARNING);
//		assertOptionEquals( "report unused local",
//							options, 
//						    CompilerOptions.OPTION_ReportUnusedLocal,
//						    CompilerOptions.WARNING);
//		assertOptionEquals( "report unused param",
//							options, 
//						    CompilerOptions.OPTION_ReportUnusedParameter,
//						    CompilerOptions.WARNING);
//		assertOptionEquals( "report synthectic access",
//							options, 
//						    CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
//						    CompilerOptions.WARNING);
//		assertOptionEquals( "report non-externalized string literal",
//							options, 
//						    CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
//						    CompilerOptions.WARNING);
//		assertOptionEquals( "report assert identifer",
//							options, 
//						    CompilerOptions.OPTION_ReportAssertIdentifier,
//						    CompilerOptions.WARNING);						    		
//	}
	
	public void testSetOfWarnings() {
		HashSet warnings = new HashSet();
		warnings.add( BuildOptionsAdapter.WARN_ASSERT_IDENITIFIER );		
		warnings.add( BuildOptionsAdapter.WARN_CONSTRUCTOR_NAME );		
		warnings.add( BuildOptionsAdapter.WARN_DEPRECATION );		
		warnings.add( BuildOptionsAdapter.WARN_MASKED_CATCH_BLOCKS );		
		warnings.add( BuildOptionsAdapter.WARN_PACKAGE_DEFAULT_METHOD );		
		warnings.add( BuildOptionsAdapter.WARN_SYNTHETIC_ACCESS );		
		warnings.add( BuildOptionsAdapter.WARN_UNUSED_ARGUMENTS );		
		warnings.add( BuildOptionsAdapter.WARN_UNUSED_IMPORTS );		
		warnings.add( BuildOptionsAdapter.WARN_UNUSED_LOCALS );		
		warnings.add( BuildOptionsAdapter.WARN_NLS );

		buildOptions.setWarnings( warnings );	
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		
		// this should leave us with all the user specifiable warnings
		// turned on
		assertOptionEquals( "report overriding package default",
							options, 
						    CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report method with cons name",
							options, 
						    CompilerOptions.OPTION_ReportMethodWithConstructorName,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report deprecation",
							options, 
						    CompilerOptions.OPTION_ReportDeprecation,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report hidden catch block",
							options, 
						    CompilerOptions.OPTION_ReportHiddenCatchBlock,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report unused local",
							options, 
						    CompilerOptions.OPTION_ReportUnusedLocal,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report unused param",
							options, 
						    CompilerOptions.OPTION_ReportUnusedParameter,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report synthectic access",
							options, 
						    CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report non-externalized string literal",
							options, 
						    CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
						    CompilerOptions.WARNING);
		assertOptionEquals( "report assert identifer",
							options, 
						    CompilerOptions.OPTION_ReportAssertIdentifier,
						    CompilerOptions.WARNING);						    		
	}
	
	public void testNoDebugOptions() {
		buildOptions.setDebugLevel( null );	
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		
		// this should leave us with the default debug settings
		assertOptionEquals( "debug source",
							options, 
						    CompilerOptions.OPTION_SourceFileAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug lines",
							options, 
						    CompilerOptions.OPTION_LineNumberAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug vars",
							options, 
						    CompilerOptions.OPTION_LocalVariableAttribute,
						    CompilerOptions.GENERATE);						    						
	}
	
	public void testEmptyDebugOptions() {
		buildOptions.setDebugLevel( new HashSet() );	
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		
		// this should leave us with the default debug 
		assertOptionEquals( "debug source",
							options, 
						    CompilerOptions.OPTION_SourceFileAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug lines",
							options, 
						    CompilerOptions.OPTION_LineNumberAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug vars",
							options, 
						    CompilerOptions.OPTION_LocalVariableAttribute,
						    CompilerOptions.GENERATE);						    								
	}
	
	public void testDebugAll() {
		HashSet debugOpts = new HashSet();
		debugOpts.add( BuildOptionsAdapter.DEBUG_ALL );
		buildOptions.setDebugLevel( debugOpts );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		
		// this should leave us with all debug on
		assertOptionEquals( "debug source",
							options, 
						    CompilerOptions.OPTION_SourceFileAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug lines",
							options, 
						    CompilerOptions.OPTION_LineNumberAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug vars",
							options, 
						    CompilerOptions.OPTION_LocalVariableAttribute,
						    CompilerOptions.GENERATE);						    								
		
	}
	
	public void testDebugSet() {
		HashSet debugOpts = new HashSet();
		debugOpts.add( BuildOptionsAdapter.DEBUG_SOURCE );
		debugOpts.add( BuildOptionsAdapter.DEBUG_VARS );
		buildOptions.setDebugLevel( debugOpts );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		
		// this should leave us with all debug on
		assertOptionEquals( "debug source",
							options, 
						    CompilerOptions.OPTION_SourceFileAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug lines",
							options, 
						    CompilerOptions.OPTION_LineNumberAttribute,
						    CompilerOptions.GENERATE);
		assertOptionEquals( "debug vars",
							options, 
						    CompilerOptions.OPTION_LocalVariableAttribute,
						    CompilerOptions.GENERATE);						    											
	}
	
	public void testNoImport() {
		buildOptions.setNoImportError( true );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
//		Map options = buildConfig.getJavaOptions();
//		String noImport = (String) options.get( CompilerOptions.OPTION_ReportInvalidImport );
//		assertEquals( "no import", CompilerOptions.WARNING, noImport );
//		buildOptions.setNoImportError( false );
	}
	
	public void testPreserveAllLocals() {
		buildOptions.setPreserveAllLocals( true );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		Map options = buildConfig.getOptions().getMap();
		String preserve = (String) options.get( CompilerOptions.OPTION_PreserveUnusedLocal );
		assertEquals( "preserve unused", CompilerOptions.PRESERVE, preserve );
	}	

	public void testNonStandardOptions() {
		buildOptions.setNonStandardOptions( "-XterminateAfterCompilation" );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		assertTrue( "XterminateAfterCompilation", buildConfig.isTerminateAfterCompilation() );
		buildOptions.setNonStandardOptions( "-XserializableAspects" );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
		assertTrue( "XserializableAspects", buildConfig.isXserializableAspects() );
		buildOptions.setNonStandardOptions( "-XnoInline" );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
		assertTrue( "XnoInline", buildConfig.isXnoInline());
		buildOptions.setNonStandardOptions( "-Xlint" );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
		assertEquals( "Xlint", AjBuildConfig.AJLINT_DEFAULT, 
		                       buildConfig.getLintMode());
		buildOptions.setNonStandardOptions( "-Xlint:error" );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
		assertEquals( "Xlint", AjBuildConfig.AJLINT_ERROR, 
		                       buildConfig.getLintMode());

		// XXX test for lintfile
//		buildOptions.setNonStandardOptions( "-Xlintfile testdata/AspectJBuildManagerTest/lint.properties" );
//		buildConfig = compilerAdapter.genBuildConfig( configFile );			
//		assertEquals( "Xlintfile", new File( "testdata/AspectJBuildManagerTest/lint.properties" ).getAbsolutePath(), 
//		                       buildConfig.getLintSpecFile().toString());
		// and a few options thrown in at once
		buildOptions.setNonStandardOptions( "-Xlint -XnoInline -XserializableAspects" );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		assertEquals( "Xlint", AjBuildConfig.AJLINT_DEFAULT, 
		                       buildConfig.getLintMode());
		assertTrue( "XnoInline", buildConfig.isXnoInline());
		assertTrue( "XserializableAspects", buildConfig.isXserializableAspects() );			                       						
	}

	public void testSourceRoots() {
		Set roots = new HashSet();
		projectProperties.setSourceRoots( roots );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		List configRoots = buildConfig.getSourceRoots();	
		assertTrue( "no source dirs", configRoots.isEmpty() );
		
		File f = new File( AjdeTests.testDataPath("examples/figures/figures-coverage" ));
		roots.add( f );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		List configRoots2 = buildConfig.getSourceRoots();	
		assertTrue( "one source dir", configRoots2.size() == 1 );
		assertTrue( "source dir", configRoots2.contains(f) );

		
		File f2 = new File( AjdeTests.testDataPath("examples/figures/figures-demo"));
		roots.add( f2 );		
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		List configRoots3 = buildConfig.getSourceRoots();	
		assertTrue( "two source dirs", configRoots3.size() == 2 );
		assertTrue( "source dir 1", configRoots3.contains(f) );
		assertTrue( "source dir 2", configRoots3.contains(f2) );
	}
	
	public void testInJars() {
		Set jars = new HashSet();
		projectProperties.setInJars( jars );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		List inJars = buildConfig.getInJars();	
		assertTrue( "no in jars", inJars.isEmpty() );
		
		File f = new File( "jarone.jar" );
		jars.add( f );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		List inJars2 = buildConfig.getInJars();
		assertTrue( "one in jar", inJars2.size() == 1 );
		assertTrue( "in jar", inJars2.contains(f) );

		
		File f2 = new File( "jartwo.jar" );
		jars.add( f2 );		
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
		List inJars3 = buildConfig.getInJars();	
		assertTrue( "two in jars", inJars3.size() == 2 );
		assertTrue( "in jar 1", inJars3.contains(f) );
		assertTrue( "in jar 2", inJars3.contains(f2) );		
	}
	
	public void testAspectPath() {
		Set aspects = new HashSet();
		projectProperties.setAspectPath( aspects );
		buildConfig = compilerAdapter.genBuildConfig( configFile );
        assertTrue(configFile + " failed", null != buildConfig);            
		List aPath = buildConfig.getAspectpath();	
		assertTrue( "no aspect path", aPath.isEmpty() );
		
		File f = new File( "jarone.jar" );
		aspects.add( f );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		List aPath2 = buildConfig.getAspectpath();	
		assertEquals("aspectpath", 1, aPath2.size());
		assertTrue( "1 aspectpath", aPath2.contains(f) );

		
		File f2 = new File( "jartwo.jar" );
		aspects.add( f2 );		
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		List aPath3 = buildConfig.getAspectpath();	
		assertTrue( "two jars in path", aPath3.size() == 2 );
		assertTrue( "1 aspectpath", aPath3.contains(f) );
		assertTrue( "2 aspectpath", aPath3.contains(f2) );		
	}
	
	public void testOutJar() {
		String outJar = "mybuild.jar";
		projectProperties.setOutJar( outJar );
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		assertNotNull("output jar", buildConfig.getOutputJar());
        assertEquals( "out jar", outJar, buildConfig.getOutputJar().toString() );				
	}
	
	public void testXHasMember() {
		buildOptions.setNonStandardOptions("-XhasMember");
		buildConfig = compilerAdapter.genBuildConfig( configFile );			
        assertTrue(configFile + " failed", null != buildConfig);            
		assertTrue( "XhasMember", buildConfig.isXHasMemberEnabled() );
	}

	protected void setUp() throws Exception {
		preferencesAdapter = new UserPreferencesStore(false);
		buildOptions = new AjcBuildOptions(preferencesAdapter);
		compilerAdapter = new CompilerAdapter();
		projectProperties = new NullIdeProperties( "" );
        taskListManager = new NullIdeTaskListManager();
        ErrorHandler handler = new NullIdeErrorHandler();
        try {
            Ajde.init(            
                null,
                taskListManager,
                null,
                projectProperties,  
                buildOptions,
                null,
                null,
                handler);  
        } catch (Throwable t) {
            String s = "Unable to initialize AJDE "
                + LangUtil.renderException(t);
            assertTrue(s, false);
        }
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
        preferencesAdapter = null;
        buildOptions = null;
        compilerAdapter = null;
        projectProperties = null;
        taskListManager = null;
	}

	private void assertOptionEquals( String reason, Map options, String optionName, String value) {
		String mapValue = (String) options.get(optionName);
		assertEquals( reason, value, mapValue );
	}
	
}
