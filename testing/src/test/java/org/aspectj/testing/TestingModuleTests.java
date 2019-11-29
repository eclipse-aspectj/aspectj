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
package org.aspectj.testing;

import org.aspectj.testing.harness.bridge.AbstractRunSpecTest;
import org.aspectj.testing.harness.bridge.AjcSpecTest;
import org.aspectj.testing.harness.bridge.CompilerRunSpecTest;
import org.aspectj.testing.harness.bridge.CompilerRunTest;
import org.aspectj.testing.harness.bridge.DirChangesTest;
import org.aspectj.testing.harness.bridge.ParseTestCase;
import org.aspectj.testing.taskdefs.AjcTaskCompileCommandTest;
import org.aspectj.testing.util.BridgeUtilTest;
import org.aspectj.testing.util.FileUtilTest;
import org.aspectj.testing.util.IteratorWrapperTest;
import org.aspectj.testing.util.LangUtilTest;
import org.aspectj.testing.util.MessageUtilTest;
import org.aspectj.testing.util.StreamGrabberTest;
import org.aspectj.testing.util.StructureModelUtilTest;
import org.aspectj.testing.util.TestDiffsTest;
import org.aspectj.testing.util.options.OptionsTest;
import org.aspectj.testing.util.options.ValuesTest;
import org.aspectj.testing.xml.AjcSpecXmlReaderTest;
import org.aspectj.testing.xml.MessageListXmlReaderTest;
import org.aspectj.testing.xml.XMLWriterTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestingModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TestingModuleTests.class.getName());

        suite.addTestSuite(ValuesTest.class);
        suite.addTestSuite(OptionsTest.class);
        suite.addTestSuite(StructureModelUtilTest.class);
        suite.addTestSuite(FileUtilTest.class);
        suite.addTestSuite(BridgeUtilTest.class);
        suite.addTestSuite(IteratorWrapperTest.class);
        suite.addTestSuite(StreamGrabberTest.class);
        suite.addTestSuite(TestDiffsTest.class);
        suite.addTestSuite(LangUtilTest.class);
        suite.addTestSuite(MessageUtilTest.class);
        suite.addTestSuite(XMLWriterTest.class);
        suite.addTestSuite(AjcSpecXmlReaderTest.class);
        suite.addTestSuite(MessageListXmlReaderTest.class);
        suite.addTestSuite(CompilerRunTest.class);
        suite.addTestSuite(CompilerRunSpecTest.class);
        suite.addTestSuite(AjcSpecTest.class);
        suite.addTestSuite(DirChangesTest.class);
        suite.addTestSuite(ParseTestCase.class);
        suite.addTestSuite(AbstractRunSpecTest.class);
        suite.addTestSuite(AjcTaskCompileCommandTest.class);

//        suite.addTest(org.aspectj.testing.harness.bridge.TestingBridgeTests.suite()); 
//        suite.addTest(org.aspectj.testing.taskdefs.TaskdefTests.suite()); 
//        suite.addTest(org.aspectj.testing.util.UtilTests.suite()); 
//        suite.addTest(org.aspectj.testing.util.options.OptionsTests.suite()); 
//        suite.addTest(org.aspectj.testing.xml.TestingXmlTests.suite()); 
        return suite;
    }

    public TestingModuleTests(String name) { super(name); }

}  
