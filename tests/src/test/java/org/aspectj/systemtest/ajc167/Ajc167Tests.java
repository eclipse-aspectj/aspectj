/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc167;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc167Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// public void testGenericsItds_298665() {
	// runTest("itd generics");
	// }
	public void testGenericAspectSignatures_296533() {
		runTest("generic aspect signatures");
	}

	public void testOptimizingAnnotationStringValueBinding() {
		runTest("optimizing string anno value binding");
	}

	public void testOptimizingAnnotationBinding() {
		runTest("optimizing annotation binding");
	}

	// bit flakey - can depend on machine load
	// public void testOptimizingAnnotationBindingPerfTest() {
	// runTest("optimizing annotation binding - 2");
	// }

	/*
	 * public void testPerThisLTW_295092() { runTest("perthis ltw"); }
	 */

	public void testNpeOnBrokenCode_296054() {
		runTest("npe on broken code");
	}

	public void testBrokenGeneratedCode_296040() {
		runTest("broken generated code");
	}

	public void testHierarchyBuilderNPE_pr293457() {
		runTest("hierarchy builder npe");
	}

	public void testTimers_1() {
		runTest("timers - 1");
	}

	// Test harness parse of -Xset:a=b,c=d will see c=d as a second option
	// public void testTimers_2() {
	// runTest("timers - 2");
	// }

	public void testAnnoMatching_pr293203() {
		runTest("anno matching");
	}

	public void testScalaOuterClassNames_pr288064() {
		runTest("outer class names - scala");
	}

	public void testScalaOuterClassNames_pr288064_ltw() {
		runTest("outer class names - scala - ltw");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc167Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc167.xml");
	}

}