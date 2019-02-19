/*******************************************************************************
 * Copyright (c) 2018-2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc193; 

import java.io.File;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava10OrLater;
import org.aspectj.weaver.WeaverStateInfo;

import junit.framework.Test;

/**
 * @author Andy Clement
 */ 
public class Ajc193Tests extends XMLBasedAjcTestCaseForJava10OrLater {

	public void testNestedAroundProceed() {
		runTest("nested around proceed");
	}

	public void testDeclareMixinOverweavingControl() {
		runTest("overweaving decm - control");
	}

	public void testDeclareMixinOverweavingReweaving() {
		runTest("overweaving decm - reweaving");
	}

	public void testDeclareMixinOverweaving() {
		runTest("overweaving decm - 1");
	}

	public void xtestDeclareMixinOverweaving2() {
		runTest("overweaving decm - 2");
	}
	
	public void xtestOverweavingDeclareMixinTargetingAspect() {
		runTest("mood indicator 4");
	}
	
	public void testOverweavingAtDecPControl() {
		runTest("overweaving atdecp - control");
	}

	public void testOverweavingAtDecP() {
		runTest("overweaving atdecp");
	}

	public void testComplexOverweaving1() {
		// This is the same code as the other test but overweaving OFF
		runTest("overweaving");
	}
	
	public void testComplexOverweaving2() throws Exception {
		// This is the same code as the other test but overweaving ON
		runTest("overweaving 2");
		// Asserting the weaver state info in the tests that will drive overweaving behaviour:
		
		// After step 1 of the test, MyAspect will have been applied. 
		JavaClass jc = getClassFrom(new File(ajc.getSandboxDirectory(),"ow1.jar"), "Application");
		WeaverStateInfo wsi = getWeaverStateInfo(jc);
		assertEquals("[LMyAspect;]", wsi.getAspectsAffectingType().toString());
		assertTrue(wsi.getUnwovenClassFileData().length>0);
		
		// After overweaving, MyAspect2 should also be getting applied but the unwovenclassfile
		// data has been blanked out - because we can no longer use it, only overweaving is possible
		// once one overweaving step is done
		jc = getClassFrom(ajc.getSandboxDirectory(), "Application");
		wsi = getWeaverStateInfo(jc);
		assertEquals("[LMyAspect2;, LMyAspect;]", wsi.getAspectsAffectingType().toString());
		assertEquals(0,wsi.getUnwovenClassFileData().length);
	}
	
	// Two steps of overweaving
	public void testComplexOverweaving3() throws Exception {
		// This is the same code as the other test but overweaving ON
		runTest("overweaving 3");
		// Asserting the weaver state info in the tests that will drive overweaving behaviour:
		
		// After step 1 of the test, MyAspect will have been applied. 
		JavaClass jc = getClassFrom(new File(ajc.getSandboxDirectory(),"ow1.jar"), "Application");
		WeaverStateInfo wsi = getWeaverStateInfo(jc);
		assertEquals("[LMyAspect;]", wsi.getAspectsAffectingType().toString());
		assertTrue(wsi.getUnwovenClassFileData().length>0);
		
		// After overweaving, MyAspect2 should also be getting applied but the unwovenclassfile
		// data has been blanked out - because we can no longer use it, only overweaving is possible
		// once one overweaving step is done
		jc = getClassFrom(new File(ajc.getSandboxDirectory(),"ow3.jar"), "Application");
		wsi = getWeaverStateInfo(jc);
		assertEquals("[LMyAspect2;, LMyAspect;]", wsi.getAspectsAffectingType().toString());
		assertEquals(0,wsi.getUnwovenClassFileData().length);

		jc = getClassFrom(ajc.getSandboxDirectory(), "Application");
		wsi = getWeaverStateInfo(jc);
		assertEquals("[LMyAspect3;, LMyAspect2;, LMyAspect;]", wsi.getAspectsAffectingType().toString());
		assertEquals(0,wsi.getUnwovenClassFileData().length);
	}

	// overweaving then attempt non overweaving - should fail
	public void testComplexOverweaving4() throws Exception {
		// This is the same code as the other test but overweaving ON
		runTest("overweaving 4");
		// Asserting the weaver state info in the tests that will drive overweaving behaviour:
		
		// After step 1 of the test, MyAspect will have been applied. 
		JavaClass jc = getClassFrom(new File(ajc.getSandboxDirectory(),"ow1.jar"), "Application");
		WeaverStateInfo wsi = getWeaverStateInfo(jc);
		assertEquals("[LMyAspect;]", wsi.getAspectsAffectingType().toString());
		assertTrue(wsi.getUnwovenClassFileData().length>0);		
	}
	
	// Altered version of this test from org.aspectj.systemtest.ajc150.Enums for 542682
	public void testDecpOnEnumNotAllowed_xlints() {
		runTest("wildcard enum match in itd");
	}

	public void testEnumDecmixinMessage() {
		runTest("declare mixin a");
	}
	
	public void testIsAbstractType() {
		runTest("is abstract");
	}

	public void testIsAbstractType2() {
		runTest("is abstract - 2");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc193Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc193.xml");
	}

}
