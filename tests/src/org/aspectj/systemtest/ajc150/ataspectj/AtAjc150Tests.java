/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;
import org.aspectj.systemtest.ajc150.TestUtils;

import java.io.File;

/**
 * A suite for @AspectJ aspects located in java5/ataspectj
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjc150Tests extends TestUtils {

    public static Test suite() {
        return XMLBasedAjcTestCase.loadSuite(org.aspectj.systemtest.ajc150.ataspectj.AtAjc150Tests.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        baseDir = new File("../tests/java5/ataspectj");
    }

    public void testSingletonAspectBinding() {
        runDefaultConfig("ataspectj.SingletonAspectBindingsTest");
    }

    public void testSingletonAspectBindingNoInline() {
        CompilationResult cR = ajc(baseDir, new String[]{"ataspectj/SingletonAspectBindingsTest.java", "ataspectj"+File.separator+"TestHelper.java", "-1.5", "-XnoInline"});
        MessageSpec ms = new MessageSpec(null, null); // Could assert certain warnings/errors came out
        assertMessages(cR, ms);
        RunResult rR = run("ataspectj.SingletonAspectBindingsTest"); // Could assert output of this, but if an exception is thrown the test will fail
    }

    public void testSingletonAspectBindingLazyTjp() {
        CompilationResult cR = ajc(baseDir, new String[]{"ataspectj/SingletonAspectBindingsTest.java", "ataspectj"+File.separator+"TestHelper.java", "-1.5", "-XlazyTjp"});
        MessageSpec ms = new MessageSpec(null, null); // Could assert certain warnings/errors came out
        assertMessages(cR, ms);
        RunResult rR = run("ataspectj.SingletonAspectBindingsTest"); // Could assert output of this, but if an exception is thrown the test will fail
    }

    public void testCflow() {
        runDefaultConfig("ataspectj.CflowTest");
    }

    public void testPointcutReference() {
        runDefaultConfig("ataspectj.PointcutReferenceTest");
    }

    //FIXME restore when AJC can compile it... #86452
//    public void testAfterX() {
//        runDefaultConfig("ataspectj.AfterXTest");
//    }

    public void testIfPointcut() {
        //FIXME @AJ impl + test
        //runDefaultConfig(IfPointcutTest.class);
    }

    public void testXXJoinPoint() {
        runDefaultConfig("ataspectj.XXJoinPointTest");
    }

    public void testPrecedence() {
        runDefaultConfig("ataspectj.PrecedenceTest");
    }

    //FIXME -- java.lang.VerifyError: (class: ataspectj/BindingTest, method: dup_aroundBody5$advice signature: (ILorg/aspectj/lang/JoinPoint;Lataspectj/BindingTest$TestAspect_1;ILorg/aspectj/lang/ProceedingJoinPoint;)Ljava/lang/Object;) Register 0 contains wrong type
//    public void testBindings() {
//        runDefaultConfig("ataspectj.BindingTest");
//    }

    private void runDefaultConfig(String klassName) {
        CompilationResult cR = ajc(baseDir, new String[]{klassName.replace('.','/')+".java", "ataspectj"+File.separator+"TestHelper.java", "-1.5"});
        MessageSpec ms = new MessageSpec(null, null);
        assertMessages(cR, ms);
        RunResult rR = run(klassName);
    }

}