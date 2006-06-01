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
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde;

import java.io.File;

import org.aspectj.util.FileUtil;

import junit.framework.*;
 
public class AjdeTests extends TestCase {

    // TODO-path
    private static final File TESTDATA_PATH;
    static {
        String[] paths = { "../ajde/testdata" };
        TESTDATA_PATH  = FileUtil.getBestFile(paths);
    }
    public static String testDataPath(String file) {
        if (null == file) {
            return TESTDATA_PATH.getPath();
        }
        File f = new File(TESTDATA_PATH, file);
        f = FileUtil.getBestFile(f);
        return (null == f ? "" : f.getPath());
    }

    public static Test suite() { 
        TestSuite suite = new TestSuite(AjdeTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(SymbolFileGenerationTest.class);
        suite.addTestSuite(ShowWeaveMessagesTestCase.class);
		suite.addTestSuite(DuplicateManifestTest.class);
		suite.addTestSuite(BuildOptionsTest.class); 
        suite.addTestSuite(BuildConfigurationTests.class);
        suite.addTestSuite(StructureModelRegressionTest.class); 
        suite.addTestSuite(StructureModelTest.class); 
        suite.addTestSuite(VersionTest.class); 
		suite.addTestSuite(CompilerMessagesTest.class);
		suite.addTestSuite(AsmDeclarationsTest.class);
		suite.addTestSuite(AsmRelationshipsTest.class);
		suite.addTestSuite(InpathTestcase.class);
		suite.addTestSuite(ReweavableTestCase.class);
		suite.addTestSuite(ResourceCopyTestCase.class);
		suite.addTestSuite(ModelPerformanceTest.class);
		suite.addTestSuite(SavedModelConsistencyTest. class);
		suite.addTestSuite(BuildCancellingTest.class);
		suite.addTestSuite(JarManifestTest.class);
		suite.addTestSuite(ExtensionTests.class);
		suite.addTestSuite(GenericsTest.class); 
		suite.addTestSuite(OutxmlTest.class);
		
        //$JUnit-END$
        return suite;
    }

    public AjdeTests(String name) { super(name); }

}  
