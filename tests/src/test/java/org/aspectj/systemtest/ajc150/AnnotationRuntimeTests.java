/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
* Tests for @this, @target, @args
*/
public class AnnotationRuntimeTests extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(AnnotationRuntimeTests.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
    
//    No longer a limitation ASC 31Jan05
//    public void test001_BindingWithAtTargetAllowed() {
//        CompilationResult cR = binaryWeave("TestingAnnotations.jar","BindingWithAtTarget.aj",0,0);
//        List errors = cR.getErrorMessages();
//        RunResult rR = run("TestingAnnotations");
//        System.err.println(rR.getStdErr());
//    }
    
    public void test002_MustHaveRuntimeRetention() {
    	runTest("must have runtime retention");
    }
    
    public void test003_InheritableOrNot() {
    	runTest("inheritable or not");
    }

    public void test004_CantUseinDecEoW() {
    	runTest("use of @this/target in deow");
    }
  
    public void test005_ArgsSuite() {
    	runTest("@args tests");
    }

    public void test006_CantUseinDecEoW() {
    	runTest("use of @args in deow");
    }

}
