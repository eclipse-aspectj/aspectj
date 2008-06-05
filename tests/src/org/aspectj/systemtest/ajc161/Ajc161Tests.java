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
package org.aspectj.systemtest.ajc161;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc161Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	// AspectJ1.6.1
	public void testCrazyGenericsInnerTypes_pr235829() { runTest("crazy generics and inner types");}
	public void testAnnotationExposureGenerics_pr235597() { runTest("annotation exposure and generics");}
    public void testIncorrectRelationship_pr235204() {
        runTest("incorrect call relationship");
        IRelationshipMap irm = AsmManager.getDefault().getRelationshipMap();
        Set entries = irm.getEntries();
        String gotit = "";
        for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
            Object object = (Object) iterator.next();
            gotit = (String) object;
            break;
        }
        if (gotit.indexOf("method-call") == -1) {
            String expected = "<recursivepackage{RecursiveCatcher.java}RecursiveCatcher~recursiveCall~I?method-call(void recursivepackage.RecursiveCatcher.recursiveCall(int))";
            fail("Expected '" + expected + "' but got '" + gotit + "'");
        }
    }

    public void testITDPrecedence_pr233838_1() { runTest("itd precedence - 1"); }
	public void testITDPrecedence_pr233838_2() { runTest("itd precedence - 2"); }
	public void testGetFieldGenerics_pr227401() { runTest("getfield problem with generics");}
	public void testGenericAbstractAspects_pr231478() { runTest("generic abstract aspects"); }
    public void testFieldJoinpointsAndAnnotationValues_pr227993() { runTest("field jp anno value"); }
    public void testGenericsBoundsDecp_pr231187() { runTest("generics bounds decp"); }
    public void testGenericsBoundsDecp_pr231187_2() { runTest("generics bounds decp - 2"); }
	public void testLtwInheritedCflow_pr230134() { runTest("ltw inherited cflow"); }
    public void testAroundAdviceOnFieldSet_pr229910() { runTest("around advice on field set"); }
    public void testPipelineCompilationGenericReturnType_pr226567() { runTest("pipeline compilation and generic return type"); }

    public static Test suite() {
      return XMLBasedAjcTestCase.loadSuite(Ajc161Tests.class);
    }

    protected File getSpecFile() {
      return new File("../tests/src/org/aspectj/systemtest/ajc161/ajc161.xml");
    }
  
}