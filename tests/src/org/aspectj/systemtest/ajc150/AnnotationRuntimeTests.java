/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.tools.ajc.CompilationResult;

/**
* Tests for @this, @target, @args
*/
public class AnnotationRuntimeTests extends TestUtils {

    protected void setUp() throws Exception {
        super.setUp();
    	baseDir = new File("../tests/java5/annotations/thisOrtarget");
    }
    
//    No longer a limitation ASC 31Jan05
//    public void test001_BindingWithAtTargetAllowed() {
//        CompilationResult cR = binaryWeave("TestingAnnotations.jar","BindingWithAtTarget.aj",0,0);
//        List errors = cR.getErrorMessages();
//        RunResult rR = run("TestingAnnotations");
//        System.err.println(rR.getStdErr());
//    }
    
    public void test002_MustHaveRuntimeRetention() {
        CompilationResult cR = binaryWeave("TestingAnnotations.jar","NotRuntimeRetention.aj",2,0);
        List errors = new ArrayList();
        errors.add(new Message(7,"Annotation type MyClassRetentionAnnotation does not have runtime retention"));
        errors.add(new Message(13,"Annotation type MyClassRetentionAnnotation does not have runtime retention"));
        
        MessageSpec messageSpec = new MessageSpec(new ArrayList(), errors);
        assertMessages(cR, messageSpec);
    }
    
    public void test003_InheritableOrNot() {
        CompilationResult cR = binaryWeave("TestingAnnotations.jar","ThisOrTargetTests.aj",0,0);   
    }

    public void test004_CantUseinDecEoW() {
        CompilationResult cR = binaryWeave("TestingAnnotations.jar","DeclareEoW.java",4,0);
        List errors = new ArrayList();
        errors.add(new Message(3,"this() pointcut designator cannot be used in declare statement"));
        errors.add(new Message(5,"target() pointcut designator cannot be used in declare statement"));
        MessageSpec messageSpec = new MessageSpec(new ArrayList(), errors);
        assertMessages(cR, messageSpec);
    }

    // TODO extra tests
    // run the result of test003 and validate matches (needs 1.5 runtime)
    // test inheritable annotation not present on type [should generate runtime test]
    
    public void test005_ArgsSuite() {
    	baseDir = new File("../tests/java5/annotations/args");
        CompilationResult cR = binaryWeave("TestingArgsAnnotations.jar","AtArgsAspect.java",0,0);  
        // TODO need to RUN the result of these tests...
        System.out.println(cR);
    }

    public void test006_CantUseinDecEoW() {
    	baseDir = new File("../tests/java5/annotations/args");
        CompilationResult cR = binaryWeave("TestingArgsAnnotations.jar","DeclareEoW.java",2,0);
        List errors = new ArrayList();
        errors.add(new Message(3,"args() pointcut designator cannot be used in declare statement"));
        MessageSpec messageSpec = new MessageSpec(new ArrayList(), errors);
        assertMessages(cR, messageSpec);
    }

}
