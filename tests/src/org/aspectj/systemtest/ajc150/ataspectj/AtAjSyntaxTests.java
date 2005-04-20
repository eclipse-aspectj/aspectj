/*******************************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * initial development             Jonas Bonér, Alexandre Vasseur 
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * A suite for @AspectJ aspects located in java5/ataspectj
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjSyntaxTests extends XMLBasedAjcTestCase {
	
	public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(AtAjSyntaxTests.class);
	}

	protected File getSpecFile() {
	  return new File("../tests/src/org/aspectj/systemtest/ajc150/ataspectj/atajc150.xml");
	}
	
    public void testSimpleBefore() {
        runTest("SimpleBefore");
    }
	
    public void testSimpleAfter() {
        runTest("SimpleAfter");
    }
	
    public void testSingletonAspectBinding() {
        runTest("singletonAspectBindings");
    }
	
    public void testCflow() {
        runTest("CflowTest");
    }
	
    public void testPointcutReference() {
        runTest("PointcutReferenceTest");
    }
	
    public void testXXJoinPoint() {
        runTest("XXJoinPointTest");
    }

    public void testPrecedence() {
        runTest("PrecedenceTest");
    }

// FIXME alex restore when AJC can compile it... #86452
//    public void testAfterX() {
//        runTest("AfterXTest");
//    }

    //FIXME AV uncomment when IfPointcutTest.TestAspect can be compiled ie if() pcd can be parsed.
    // right now the aspect is commented out.
//    public void testIfPointcut() {
//        runTest("IfPointcutTest");
//    }

// FIXME alex java.lang.VerifyError: (class: ataspectj/BindingTest, method: dup_aroundBody5$advice signature: (ILorg/aspectj/lang/JoinPoint;Lataspectj/BindingTest$TestAspect_1;ILorg/aspectj/lang/ProceedingJoinPoint;)Ljava/lang/Object;) Register 0 contains wrong type
//    public void testBindings() {
//        runTest("BindingTest");
//    }
}