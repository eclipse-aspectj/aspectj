/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc160;

import java.io.File;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * These are tests for AspectJ1.6 - they do not require a 1.6 VM.
 */
public class Ajc160Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
  public void testGenericMethodsAndOrdering_ok_pr171953_2() { runTest("problem with generic methods and ordering - ok");}
  public void testGenericMethodsAndOrdering_bad_pr171953_2() { runTest("problem with generic methods and ordering - bad");}
  public void testItdAndJoinpointSignatureCollection_ok_pr171953() { runTest("problem with itd and join point signature collection - ok");}
  public void testItdAndJoinpointSignatureCollection_bad_pr171953() { runTest("problem with itd and join point signature collection - bad");}
  public void testGenericMethodsAndItds_pr171952() { runTest("generic methods and ITDs");}
  //public void testUsingDecpAnnotationWithoutAspectAnnotation_pr169428() { runTest("using decp annotation without aspect annotation");}
  public void testItdsParameterizedParameters_pr170467() { runTest("itds and parameterized parameters");}
  public void testComplexGenerics_pr168044() { runTest("complex generics - 1");}
  public void testIncorrectlyMarkingFieldTransient_pr168063() { runTest("incorrectly marking field transient");}
  public void testInheritedAnnotations_pr169706() { runTest("inherited annotations");}
  public void testGenericFieldNPE_pr165885() { runTest("generic field npe");}
  public void testIncorrectOptimizationOfIstore_pr166084() { runTest("incorrect optimization of istore"); }
  public void testDualParameterizationsNotAllowed_pr165631() { runTest("dual parameterizations not allowed"); }
  	
	public void testSuppressWarnings1_pr166238() {
		runTest("Suppress warnings1");
	}

	public void testSuppressWarnings2_pr166238() {
		runTest("Suppress warnings2");
	}
 
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc160Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc160/ajc160.xml");
  }

  
}