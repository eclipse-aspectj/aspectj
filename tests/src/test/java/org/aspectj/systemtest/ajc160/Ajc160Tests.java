/*******************************************************************************
 * Copyright (c) 2007-2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc160;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * These are tests for AspectJ1.6.0
 */
public class Ajc160Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// AspectJ1.6.0rc1
	// public void testPipelineCompilationGenericReturnType_pr226567() {
	// runTest("pipeline compilation and generic return type");
	// }
	public void testPipelineCompilationAnonymous_pr225916() {
		runTest("pipeline compilation and anonymous type");
	}

	public void testGenericITDs_pr214994() {
		runTest("generic itd");
	}

	public void testGenericDecpLtw_pr223605() {
		runTest("generic decp ltw");
	}

	public void testDuplicateITDsNPE_pr173602() {
		runTest("duplicate itd npe");
	}

	public void testLTWITDs_pr223094() {
		runTest("ltw inherited itds");
	}

	// public void testBrokenIfArgsCflowAtAj_pr145018() {
	// runTest("ataj crashing with cflow, if and args");
	// }
	public void testClassCastOnArrayType_pr180264() {
		runTest("classcastexception on array type");
	}

	// public void testITDWithArray_pr201748() { runTest("itd with array");}
	public void testBadMessage() {
		runTest("incorrect itd error with generics");
	}

	public void testBadMessage2() {
		runTest("incorrect itd error with generics - 2");
	}

	public void testHasMethodAnnoValueInt_various() {
		runTest("hasmethod anno value - I");
	}

	public void testHasMethodAnnoValueBoolean_various() {
		runTest("hasmethod anno value - Z");
	}

	public void testHasMethodAnnoValueString_various() {
		runTest("hasmethod anno value - S");
	}

	public void testGenericTypeParameterizedWithArrayType_pr167197() {
		runTest("generic type parameterized with array type");
	}

	public void testGenericTypeParameterizedWithArrayType_pr167197_2() {
		runTest("generic type parameterized with array type - 2");
	}

	// AspectJ1.6.0m2 and earlier
	public void testBoundsCheckShouldFail_pr219298() {
		runTest("bounds check failure");
	}

	public void testBoundsCheckShouldFail_pr219298_2() {
		runTest("bounds check failure - 2");
	}

	public void testGenericMethodMatching_pr204505_1() {
		runTest("generics method matching - 1");
	}

	public void testGenericMethodMatching_pr204505_2() {
		runTest("generics method matching - 2");
	}

	public void testDecFieldProblem_pr218167() {
		runTest("dec field problem");
	}

	public void testGenericsSuperITD_pr206911() {
		runTest("generics super itd");
	}

	public void testGenericsSuperITD_pr206911_2() {
		runTest("generics super itd - 2");
	}

	public void testSerializationAnnotationStyle_pr216311() {
		runTest("serialization and annotation style");
	}

	public void testDecpRepetition_pr214559() {
		runTest("decp repetition problem");
	} // all code in one file

	public void testDecpRepetition_pr214559_2() {
		runTest("decp repetition problem - 2");
	} // all code in one file, default package

	public void testDecpRepetition_pr214559_3() {
		runTest("decp repetition problem - 3");
	} // across multiple files

	public void testISEAnnotations_pr209831() {
		runTest("illegal state exception with annotations");
	}

	public void testISEAnnotations_pr209831_2() {
		runTest("illegal state exception with annotations - 2");
	}

	// See HasMemberTypePattern.hasMethod()
	// public void testHasMethodSemantics() { runTest("hasmethod semantics"); }

	// See BcelTypeMunger line 786 relating to these
	// String sig = interMethodDispatcher.getSignature();BROKE - should get the generic signature here and use that.
	// public void testITDLostGenerics_pr211146() { runTest("itd lost generic signature");}
	// public void testITDLostGenerics_pr211146_2() { runTest("itd lost generic signature - field");}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc160Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc160.xml");
	}

}