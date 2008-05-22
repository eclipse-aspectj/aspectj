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

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc161Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	// AspectJ1.6.1
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