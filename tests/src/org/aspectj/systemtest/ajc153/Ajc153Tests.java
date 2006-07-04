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
package org.aspectj.systemtest.ajc153;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.Repository;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.apache.bcel.verifier.VerificationResult;
import org.aspectj.apache.bcel.verifier.Verifier;
import org.aspectj.apache.bcel.verifier.VerifierFactory;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc153Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

  // public void testArgnamesAndJavac_pr148381() { runTest("argNames and javac");}
  // public void testCFlowXMLAspectLTW_pr149096() { runTest("cflow xml concrete aspect"); }
  public void testIllegalStateException_pr148737() { runTest("illegalstateexception for non generic type");}
  public void testAtajInheritance_pr149305_1() { runTest("ataj inheritance - 1");}
  public void testAtajInheritance_pr149305_2() { runTest("ataj inheritance - 2");}
  public void testAtajInheritance_pr149305_3() { runTest("ataj inheritance - 3");}


  // work in progress

//	public void testVerificationFailureForAspectOf_pr148693() throws ClassNotFoundException {
//		runTest("verification problem");
//		verifyClass("mypackage.MyAspect");
//	}
	

	// TODO refactor into a util class
	/**
	 * Performs verification of a class - the supplied class is expected to exist in the sandbox
	 * directory so typically this is called after a small compile step has been invoked to build it.
	 */
	public void verifyClass(String clazzname) {
		JavaClass jc = null;
		try {
			jc = getClassFrom(ajc.getSandboxDirectory().getAbsolutePath(),clazzname);
		} catch (ClassNotFoundException cnfe) {
			fail("Could not find "+clazzname+" in the sandbox: "+ajc.getSandboxDirectory());
		}
		assertTrue("Could not find class",jc!=null);
		Repository.setRepository(jc.getRepository());
		Verifier v = VerifierFactory.getVerifier("mypackage.MyAspect"); 
		VerificationResult vr = v.doPass1();
		System.err.println(vr);
		
		assertTrue("Verification: "+vr,vr.getStatus()==VerificationResult.VERIFIED_OK);
		vr = v.doPass2();
		System.err.println(vr);
		assertTrue("Verification: "+vr,vr.getStatus()==VerificationResult.VERIFIED_OK);
		Method[] ms = jc.getMethods();
		for (int i = 0; i < ms.length; i++) {
			System.err.println("Pass3a for "+ms[i]);
			vr = v.doPass3a(i);
			System.err.println(vr);
			assertTrue("Verification: "+vr,vr.getStatus()==VerificationResult.VERIFIED_OK);
			System.err.println("Pass3b for "+ms[i]);
			vr = v.doPass3b(i);
			System.err.println(vr);
			assertTrue("Verification: "+vr,vr.getStatus()==VerificationResult.VERIFIED_OK);
		}
	}
	
	protected  JavaClass getClassFrom(String frompath,String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(frompath);
		return repos.loadClass(clazzname);
	}

	public SyntheticRepository createRepos(String cpentry) {
		ClassPath cp = new ClassPath(
				cpentry+File.pathSeparator+
				System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	}	
  
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc153Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc153/ajc153.xml");
  }
  
}