/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.ajdt;

import org.aspectj.ajdt.ajc.*;
import org.aspectj.ajdt.internal.compiler.batch.*;
import org.aspectj.ajdt.internal.core.builder.*;
import org.aspectj.tools.ajc.*;

import junit.framework.*;

public class AjdtCoreModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(AjdtCoreModuleTests.class.getName());
        suite.addTestSuite(AjdtCommandTestCase.class); 
        suite.addTestSuite(BuildArgParserTestCase.class); 
        suite.addTestSuite(ConsoleMessageHandlerTestCase.class); 

        suite.addTestSuite(BasicCommandTestCase.class); 
        suite.addTestSuite(BinaryFormsTestCase.class); 
		suite.addTestSuite(CompileAndRunTestCase.class); 
		suite.addTestSuite(PerformanceTestCase.class); 
        suite.addTestSuite(ImageTestCase.class); 
        suite.addTestSuite(MultipleCompileTestCase.class); 
        suite.addTestSuite(JavadocTest.class);
        suite.addTestSuite(PartiallyExposedHierarchyTestCase.class);
        suite.addTestSuite(CompilerDumpTestCase.class); 
        suite.addTestSuite(ProceedOnErrorTestCase.class);
        suite.addTestSuite(DeclareParentsTest.class);
        suite.addTestSuite(DumpTestCase.class);
        suite.addTestSuite(AjBuildManagerTest.class); 
        suite.addTestSuite(AjBuildConfigTest.class); 
		suite.addTestSuite(AsmBuilderTest.class); 
		suite.addTestSuite(AjCompilerOptionsTest.class);
		suite.addTestSuite(AjStateTest.class);
		suite.addTestSuite(OutjarTest.class);
		
        suite.addTestSuite(MainTest.class);
        suite.addTestSuite(ASTVisitorTest.class);
        suite.addTestSuite(ASTitdTest.class);
        suite.addTestSuite(AjASTTest.class);
        suite.addTestSuite(AjNaiveASTFlattenerTest.class);
        suite.addTestSuite(AjcTestCaseTest.class);
        suite.addTestSuite(AjAST5Test.class);
        return suite;
    }

    public AjdtCoreModuleTests(String name) { super(name); }

}  
