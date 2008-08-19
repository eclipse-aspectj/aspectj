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
package org.aspectj.systemtest.ajc162;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc162Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	// AspectJ1.6.2	
	public void testGenericDecp_pr241047() { runTest("generic decp"); }
	public void testGenericDecp_pr241047_2() { runTest("generic decp - 2"); }
	public void testGenericItds_pr242797_1() { runTest("generic itds - 1"); }
	public void testGenericItds_pr242797_2() { runTest("generic itds - 2"); }
	public void testGenericItds_pr242797_3() { runTest("generic itds - 3"); }
	public void testPrivilegedGenerics_pr240693() { runTest("privileged generics"); }
//	public void testParamAnnosPipelining_pr241847() { runTest("param annos pipelining");}
//	public void testParamAnnoInner_pr241861() { runTest("param annotation inner class"); }
	public void testAnnotationDecp_pr239441() { runTest("annotation decp"); }
	public void testAtAspectJPerTarget_pr198181() { runTest("ataspectj ltw pertarget"); }
	public void testAnnotationValueDecp_pr238992() { runTest("annotation value decp"); }
	public void testAnnotationValueDecp_pr238992_2() { runTest("annotation value decp - 2"); }
	public void testAnnotationValueDecp_pr238992_3() { runTest("annotation value decp - 3"); }
	public void testAnnotationValueDecp_pr238992_4() { runTest("annotation value decp - 4"); }
	public void testAnnotationValueDecp_pr238992_5() { runTest("annotation value decp - 5"); }
	
	/*
	 * test plan
	 * execution(* *(..,String,..))
	 * args(..,String,..)
	 * @args(..,Foo,..)
	 * 
	 */
//	public void testParameterSubsettingMatching_pr233718_Matching() { runTest("parameter subsetting - matching");}
//	public void testParameterSubsettingMatching_pr233718_ArgsMatching() { runTest("parameter subsetting - args matching");}
//	public void testParameterSubsettingMatching_pr233718_ArgsBinding() { runTest("parameter subsetting - args binding");}

	public static Test suite() {
      return XMLBasedAjcTestCase.loadSuite(Ajc162Tests.class);
    }

    protected File getSpecFile() {
      return new File("../tests/src/org/aspectj/systemtest/ajc162/ajc162.xml");
    }
  
}