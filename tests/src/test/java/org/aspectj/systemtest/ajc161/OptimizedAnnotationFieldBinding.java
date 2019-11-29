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
package org.aspectj.systemtest.ajc161;
 
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * Optimising Annotation Field Binding - better code gen for the cases where the user just wants a field from the
 * annotation on a method but not the whole annotation.
 * 
 */
public class OptimizedAnnotationFieldBinding extends XMLBasedAjcTestCase {
	
    public void testCaseOne_Syntax() {
        runTest("case one - syntax");
    }
    public void testCaseTwo_NoSuchField() {
        runTest("case two - no such field");
    }
    public void testCaseThree_Ambiguous() {
        runTest("case three - ambiguous");
    }
    public void testCaseFour_DefaultValue() {
        runTest("case four - default value");
    }
    public void testCaseFive_NotAnEnum_CompilerLimitation() {
        runTest("case five - not an enum - compiler limitation");
    }
    public void testCaseSeven_AnnosInPackagesOne() {
        runTest("case seven - annos in packages one");
    }
    public void testCaseEight_AnnosInPackagesTwo() {
        runTest("case eight - annos in packages two");
    }
    public void testCaseNine_AllInDifferentPackages() {
        runTest("case nine - everything in different packages");
    }
    public void testCaseTen_BindingMultipleThings() {
        runTest("case ten - binding multiple things");
    }
    public void testCaseEleven_BindingMultipleAnnotationFields() {
        runTest("case eleven - binding multiple annotation fields");
    }
    public void testCaseTwelve_BindingAnnoAndAnnoValue() {
        runTest("case twelve - binding anno and anno value");
    }
    public void testCaseThirteen_bugNPE() {
        runTest("case thirteen - bug npe");
    }
    
	/////////////////////////////////////////
	public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(OptimizedAnnotationFieldBinding.class);
	}

	protected java.net.URL getSpecFile() {
	    return getClassResource("annotationFieldBinding.xml");
	}
}
