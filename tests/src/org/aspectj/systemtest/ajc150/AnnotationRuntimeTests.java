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
    
    public void test001_NoBinding() {
        CompilationResult cR = binaryWeave("TestingAnnotations.jar","BindingLimitation.aj",1,0);
        List errors = cR.getErrorMessages();
        assertTrue("Binding not supported",errors.get(0).toString().startsWith("error Binding not supported"));
    }
    
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

    public void test007_Within_Code() {
    	baseDir = new File("../tests/java5/annotations/within_code");
        CompilationResult cR = binaryWeave("TestingAnnotations.jar","WithinAndWithinCodeTests.java",0,5);
        List warnings = new ArrayList();
        warnings.add(new Message(32,"@within match on non-inherited annotation"));
        warnings.add(new Message(39,"@within match on non-inherited annotation"));
        warnings.add(new Message(39,"@within match on inheritable annotation"));
        warnings.add(new Message(43,"@within match on inheritable annotation"));
        warnings.add(new Message(32,"@withincode match"));
        MessageSpec mSpec = new MessageSpec(warnings,new ArrayList());
        assertMessages(cR,mSpec); 	
    }
}
