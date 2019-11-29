/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *  Contributors
 *  Andy Clement 
 * ******************************************************************/
package org.aspectj.systemtest.ajc160;

 
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * Parameter annotation matching
 * 
 * The full implementation will require static matching, binding and possibly runtime 
 * matching (need to check on inheritance of annotations specified in these positions).
 * The extension to the syntax for method-signatures is to require parentheses when
 * specifying annotations relating to parameters and parameter types - the position of
 * the parentheses enable the distinction to be made between annotations attached to
 * the type of the parameter and annotations attached to the parameter itself.
 * 
 * For example:
 * 
 *   public void goo(MyType s) {}  // where MyType has annotation @B
 *   public void foo(@A String s) {} // String has no annotations, @A is a parameter annotation
 *   
 * A method signature to match the former would be:
 *    execution(public void goo(@B MyType)) 
 * or execution(public void goo((@B MyType)))
 *
 * To match the latter:
 *    execution(public void foo(@A (String)))
 * The parentheses around String are telling us that there are no annotations attached to the 
 * parameter type we are interested in, and @A should be treated as an intent to match on
 * parameter annotation A.
 * 
 * In a more complex case:
 *   public void hoo(@A MyType s) {} // now there are two potential annotations we are interested in
 * the match expression is:
 *   execution(public void hoo(@A (@B MyType)))
 * the parentheses associating @B with the type of the parameter leaving @A outside to be
 * treated as a parameter annotation.
 *
 * Testplan:
 * Test cases for the parameter annotation matching:
 * DONE:
 * 1. Basic pointcut parsing for the new method signature syntax 
 * 2. Expression matching with the new syntax
 * 3. Real static matching with the new syntax
 * 4. hasmethod should pick up the new syntax
 * 5. ellipsis usage not impacted
 * 6. recognizing varargs
 * 7. constructor matching
 * 8. binary weaving
 * 
 * NOT DONE:
 * . New pointcut designator parsing
 * . Expression matching with the new syntax
 * . Real matching with the new syntax
 * . Binding with the new syntax.
 * . complaining about annotations that dont target the right type
 * . itds
 * . annotation visibility tests
 * . wildcarded @Ann*1 - broken (not my doing)
 * . ltw
 * 
 */
public class ParameterAnnotationMatchingTests extends XMLBasedAjcTestCase {
	
    public void testDeow() { runTest("deow"); }
    public void testDeow2() { runTest("deow2"); }
	public void testNoWarningForWrongType() { runTest("no xlint for wrong target");}
	public void testVariousCombinations() { runTest("various combinations"); }
	public void testVariousCombinationsCtors() { runTest("various combinations - ctors"); }
	public void testHasMethod() { runTest("hasmethod"); }
	public void testBinaryWeaving() { runTest("binary weaving"); }
	// this is broken and it was already broken before I added parameter annotation matching !
//	public void testWildcardedAnnotationMatching() { runTest("wildcarded matching"); }
	
	
	/////////////////////////////////////////
	public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(ParameterAnnotationMatchingTests.class);
	}

	protected java.net.URL getSpecFile() {
	    return getClassResource("parameterAnnotations.xml");
	}
}
