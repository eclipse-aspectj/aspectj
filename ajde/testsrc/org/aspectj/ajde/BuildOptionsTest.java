/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * 	   AMC 01.21.2003 extended to cover new AspectJ1.1 options
 * ******************************************************************/


package org.aspectj.ajde;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestSuite;

import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.ajde.ui.internal.AjcBuildOptions;
import org.aspectj.ajde.ui.internal.UserPreferencesStore;

public class BuildOptionsTest extends AjdeTestCase {

	private AjcBuildOptions buildOptions = null;
	private UserPreferencesAdapter preferencesAdapter = null;

	public BuildOptionsTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(BuildOptionsTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(BuildOptionsTest.class);	
		return result;
	}

	public void testCharacterEncoding() {
		buildOptions.setCharacterEncoding("mumble");
		assertTrue("character encoding", buildOptions.getCharacterEncoding().equals("mumble"));
	}
	
//	public void testPortingMode() {
//		buildOptions.setPortingMode(true);
//		assertTrue("porting mode", buildOptions.getPortingMode());
//	}

	public void testVerboseMode() {
		buildOptions.setVerboseMode(true);
		assertTrue("verbose mode", buildOptions.getVerboseMode());	
	}		
	
	public void testNonStandardOptions() {
		buildOptions.setNonStandardOptions( "-Xlint" );
		assertEquals( "non std options", "-Xlint", 
			buildOptions.getNonStandardOptions());
	}
	
	public void testComplianceLevel() {
		buildOptions.setComplianceLevel( BuildOptionsAdapter.VERSION_14 );
		assertEquals( "compliance level",
					  BuildOptionsAdapter.VERSION_14,
					  buildOptions.getComplianceLevel());	
	}
	
	public void testSourceCompatibilityLevel() {
		buildOptions.setSourceCompatibilityLevel(BuildOptionsAdapter.VERSION_13);
		assertEquals( "source level",
					  BuildOptionsAdapter.VERSION_13,
					  buildOptions.getSourceCompatibilityLevel());				
	}
	
	public void testWarnings() {
		buildOptions.setWarnings( null );
		assertNull( "null warning set", buildOptions.getWarnings());
		HashSet s = new HashSet();
		buildOptions.setWarnings( s );
		Set s2 = buildOptions.getWarnings();
		assertTrue( "empty warning set", s2.isEmpty() );
		s.add( BuildOptionsAdapter.WARN_ASSERT_IDENITIFIER );
		s.add( BuildOptionsAdapter.WARN_MASKED_CATCH_BLOCKS );
		buildOptions.setWarnings( s );
		s2 = buildOptions.getWarnings();
		assertTrue( "two warnings", s2.size() == 2 );
		boolean warn_assert_found = false;
		boolean warn_catch_found = false;
		Iterator it = s2.iterator();
		while (it.hasNext()) {
			String option = (String) it.next();
			if ( option.equals( BuildOptionsAdapter.WARN_ASSERT_IDENITIFIER ) ) {
				warn_assert_found = true;
			}					
			if ( option.equals( BuildOptionsAdapter.WARN_MASKED_CATCH_BLOCKS ) ) {
				warn_catch_found = true;
			}					
		}
		assertTrue( "assert warning found", warn_assert_found );	
		assertTrue( "catch waning found", warn_catch_found );
	}
	
	public void testDebugLevel() {
		buildOptions.setDebugLevel( null );
		assertNull( "null debug set", buildOptions.getDebugLevel());
		HashSet s = new HashSet();
		buildOptions.setDebugLevel( s );
		Set s2 = buildOptions.getDebugLevel();
		assertTrue( "empty debug set", s2.isEmpty() );
		s.add( BuildOptionsAdapter.DEBUG_LINES );
		s.add( BuildOptionsAdapter.DEBUG_SOURCE );
		buildOptions.setDebugLevel( s );
		s2 = buildOptions.getDebugLevel();
		assertTrue( "two warnings", s2.size() == 2 );
		boolean debug_lines_found = false;
		boolean debug_source_found = false;
		Iterator it = s2.iterator();
		while (it.hasNext()) {
			String option = (String) it.next();
			if ( option.equals( BuildOptionsAdapter.DEBUG_LINES ) ) {
				debug_lines_found = true;
			}					
			if ( option.equals( BuildOptionsAdapter.DEBUG_SOURCE ) ) {
				debug_source_found = true;
			}					
		}
		assertTrue( "debug lines found", debug_lines_found );	
		assertTrue( "debug source found", debug_source_found );				
	}

	public void testNoImportError() {
		buildOptions.setNoImportError(true);
		assertTrue("no import error", buildOptions.getNoImportError());			
	}
	
	public void testPreserveLocals() {
		buildOptions.setPreserveAllLocals(true);
		assertTrue("preserve all locals", buildOptions.getPreserveAllLocals());					
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		preferencesAdapter = new UserPreferencesStore(false);
		buildOptions = new AjcBuildOptions(preferencesAdapter);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		buildOptions.setCharacterEncoding("");
//		buildOptions.setPortingMode(true);
	}
}

