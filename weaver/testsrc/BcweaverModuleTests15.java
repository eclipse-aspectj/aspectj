/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.weaver.BoundedReferenceTypeTestCase;
import org.aspectj.weaver.MemberTestCase15;
import org.aspectj.weaver.ReferenceTypeTestCase;
import org.aspectj.weaver.TypeVariableReferenceTypeTestCase;
import org.aspectj.weaver.TypeVariableTestCase;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXTestCase;
import org.aspectj.weaver.patterns.WildTypePatternResolutionTestCase;

public class BcweaverModuleTests15 extends TestCase {
	   public static Test suite() { 
	        TestSuite suite = new TestSuite(BcweaverModuleTests15.class.getName());
	        suite.addTestSuite(TypeVariableTestCase.class);
	        suite.addTestSuite(ReferenceTypeTestCase.class);
	        suite.addTestSuite(BoundedReferenceTypeTestCase.class);
	        suite.addTestSuite(TypeVariableReferenceTypeTestCase.class);
	        suite.addTestSuite(MemberTestCase15.class);
	        suite.addTestSuite(BcelGenericSignatureToTypeXTestCase.class);
	        suite.addTestSuite(WildTypePatternResolutionTestCase.class);
	        return suite;
	    }

	    public BcweaverModuleTests15(String name) { super(name); }
}
