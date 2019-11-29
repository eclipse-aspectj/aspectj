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
 * Parameter value matching
 * 
 */
public class AnnotationValueMatchingTests extends XMLBasedAjcTestCase {
	
	public void testParsing() { runTest("parsing"); }
	public void testBroken1() { runTest("broken - 1"); }
	public void testParsingAllAnnotationValueKinds() { runTest("allkinds"); }
	public void testSimpleCase() { runTest("simple"); }
	public void testReferencingEnums1() { runTest("enum references - 1"); }
	public void testReferencingEnums2() { runTest("enum references - 2"); }
	public void testReferencingEnums3() { runTest("enum references - 3"); }
	public void testIntValueMatching() { runTest("int value matching");}
	public void testFloatValueMatching() { runTest("float value matching");}
	public void testDoubleValueMatching() { runTest("double value matching");}
	public void testByteValueMatching() { runTest("byte value matching");}
	public void testLongValueMatching() { runTest("long value matching");}
	public void testBooleanValueMatching() { runTest("boolean value matching");}
	public void testShortValueMatching() { runTest("short value matching");}
	public void testCharValueMatching() { runTest("char value matching");}
	public void testStringValueMatching() { runTest("string value matching");}
	public void testExampleOne() { runTest("example one");}
	public void testError_InvalidValueTypes() { runTest("error case");}
	public void testErrorOne_NonExistingValue() { runTest("error - non existing value");}

	/////////////////////////////////////////
	public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(AnnotationValueMatchingTests.class);
	}

	protected java.net.URL getSpecFile() {
	    return getClassResource("annotationValueMatching.xml");
	}
}
