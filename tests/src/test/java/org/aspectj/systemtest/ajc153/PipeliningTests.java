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

import java.net.URL;

//import org.aspectj.ajdt.internal.compiler.AjPipeliningCompilerAdapter;
import org.aspectj.ajdt.internal.compiler.AjPipeliningCompilerAdapter;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * testplan: (x = complete)
 * 
 * x @AspectJ aspects - are they recognized and sorted correctly ?
 * x compiling classes (various orderings)
 * x compiling classes (inheritance relationships)
 * x compiling aspects and classes (various orderings - aspects first/last)
 * x eclipse annotation transformation logic
 * x aspects extending classes
 * x nested types (and aspect inside a regular class)
 * x set of files that are only aspects
 * x pointcuts in super classes
 * - classes with errors
 * - aspects with errors
 * - Xterminate after compilation (now == skip weaving??)
 * 
 * That this pipeline works OK for large systems is kind of confirmed by using it to build shadows!
 *
 */
public class PipeliningTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// straightforward compilation
	public void testBuildTwoClasses() { runTest("build two classes");}
	public void testBuildOneAspectTwoClasses() { runTest("build one aspect and two classes");}
	public void testBuildTwoClassesOneAspect() { runTest("build two classes and one aspect");}
	public void testBuildTwoAspects() { runTest("build two aspects");}
	public void testBuildClassAndNestedAspect() { runTest("build one class and deeply nested aspect");}
	
	public void testAspectExtendsClass() { runTest("aspect extends class"); }
	
	// verifying the type sorting
	public void testRecognizingAnnotationStyleAspects1() { 
		AjPipeliningCompilerAdapter.pipelineTesting=true;
		runTest("recognizing annotation style aspects - 1");
		
		String filesContainingAspects = AjPipeliningCompilerAdapter.getPipelineDebugOutput("filesContainingAspects");
		assertTrue("Should be one file containing aspects but it thinks there are "+filesContainingAspects,filesContainingAspects.equals("1"));
				
		String weaveOrder = AjPipeliningCompilerAdapter.getPipelineDebugOutput("weaveOrder");
		String expectedOrder="[AtAJAspect.java,ClassOne.java]";
		assertTrue("Expected weaving order to be "+expectedOrder+" but was "+weaveOrder,weaveOrder.equals(expectedOrder));
	} 
	public void testRecognizingAnnotationStyleAspects2() { 
		AjPipeliningCompilerAdapter.pipelineTesting=true;
		runTest("recognizing annotation style aspects - 2");

		String filesContainingAspects = AjPipeliningCompilerAdapter.getPipelineDebugOutput("filesContainingAspects");
		assertTrue("Should be one file containing aspects but it thinks there are "+filesContainingAspects,filesContainingAspects.equals("1"));
				
		String weaveOrder = AjPipeliningCompilerAdapter.getPipelineDebugOutput("weaveOrder");
		String expectedOrder="[AtInnerAJAspect.java,ClassOne.java]";
		assertTrue("Expected weaving order to be "+expectedOrder+" but was "+weaveOrder,weaveOrder.equals(expectedOrder));
	}
	
	// verifying the new code for transforming Eclipse Annotations into AspectJ ones
	public void testAnnotationTransformation() { runTest("annotation transformation"); }

  // --
  protected void tearDown() throws Exception {
		super.tearDown();
		//AjPipeliningCompilerAdapter.pipelineTesting=false;
  }
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(PipeliningTests.class);
  }
  protected URL getSpecFile() {
    return getClassResource("pipelining.xml");
  }
}