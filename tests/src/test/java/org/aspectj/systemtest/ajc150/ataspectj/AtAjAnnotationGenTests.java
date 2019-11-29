/*******************************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * initial development             Jonas Bon�r, Alexandre Vasseur 
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import java.net.URL;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * A suite for @AspectJ aspects located in java5/ataspectj
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjAnnotationGenTests extends XMLBasedAjcTestCase {
	
	public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(AtAjAnnotationGenTests.class);
	}

	protected URL getSpecFile() {
	  return getClassResource("annotationgen.xml");
	}
	
	public void testSimpleAspect() {
		runTest("annotation gen for simple aspect");
	}
	
	public void testSimpleAspectIn14Mode() {
		runTest("annotation gen for simple aspect pre 1.5");
	}
	
	public void testAspectAlreadyAnnotated() {
		runTest("annotation gen for simple annotated aspect");		
	}
	
	public void testPrivilegedAspect() {
		runTest("annotation gen for privileged aspect");
	}
	
	public void testPerThisAspect() {
		runTest("annotation gen for perthis aspect");
	}
	
	public void testPerTargetAspect() {
		runTest("annotation gen for pertarget aspect");
	}
	
	public void testPerCflowAspect() {
		runTest("annotation gen for percflow aspect");
	}
	
	public void testPerCflowbelowAspect() {
		runTest("annotation gen for percflowbelow aspect");
	}
	
	public void testPertypewithinAspect() {
		runTest("annotation gen for pertypewithin aspect");
	}
  
	public void testInnerAspectOfClass() {
		runTest("annotation gen for inner aspect of aspect");
	}
	
	public void testInnerAspectOfAspect() {
		runTest("annotation gen for inner aspect of class");
	}
	
	public void testAdvice() {
		runTest("annotation gen for advice declarations");
	}

	public void testSimplePointcut() {
		runTest("annotation gen for simple pointcut");
	}

	public void testPointcutModifiers() {
		runTest("annotation gen for pointcut modifiers");
	}

	public void testPointcutParams() {
		runTest("annotation gen for pointcut params");		
	}

	public void testPointcutRefs() {
		runTest("annotation gen for pointcut refs");		
	}
	
	public void testBeforeWithBadReturn() {
		runTest("before ann with non-void return");
	}
	
	public void testTwoAnnotationsOnSameElement() {
		runTest("two anns on same element");
	}
	
	public void testBadPcutInAdvice() {
		runTest("bad pcut in after advice");
	}
	
	public void testBadParameterBinding() {
		runTest("bad parameter binding in advice");
	}
	
	public void testSimpleAtPointcut() {
		runTest("simple pointcut no params");
	}
	
	public void testPointcutMedley() {
		runTest("pointcut medley");
	}
	
	public void testAdviceDeclaredInClass() {
		runTest("advice in a class");
	}
	
	public void testDeows() {
		runTest("ann gen for deows");
	}
	
	// no reliable way to get around classpath issues for
	// running this test as part of release script :(
//	public void testRuntimePointcutsReferencingCompiledPointcuts() {
//		runTest("runtime pointcut resolution referencing compiled pointcuts");
//	}
	
	public void testDecP() {
		runTest("ann gen for decp");
	}
	
	public void testDecPAdvanced() {
		runTest("ann gen for decp 2");
	}
	
	public void testDecS() {
		runTest("ann gen for decs");
	}
	
	public void testDecPrecedence() {
		runTest("ann gen for dec precedence");
	}

	public void testDecAnnotation() {
		runTest("ann gen for dec annotation");
	}
	
	public void testITDs() {
		runTest("ann gen for itds");
	}
}

