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

package org.aspectj.testing.xml;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.testing.harness.bridge.AjcSpecTest;
import org.aspectj.testing.harness.bridge.AjcTest;
import org.aspectj.testing.harness.bridge.FlatSuiteReader;
import org.aspectj.util.LangUtil;

//import junit.framework.*;
import junit.framework.TestCase;

/**
 * 
 */
public class AjcSpecXmlReaderTest extends TestCase {

	List<File> tempFiles = new ArrayList<>();
	/**
	 * Constructor for AjcSpecXmlReaderTest.
	 * @param name
	 */
	public AjcSpecXmlReaderTest(String name) {
		super(name);
	}

    public void setUp() {
    	tempFiles.clear();
        //System.out.println("XXX test requires compiler and bridgeImpl projects on classpath");
    }

    public void tearDown() {
    	if (!LangUtil.isEmpty(tempFiles)) {
			for (File file : tempFiles) {
				if (file.canRead()) {
					file.delete();
				}
			}
		}
    }
    
    /** test that all AjcSpecXmlReader.me.expectedProperties() are bean-writable */
    public void testBeanInfo() throws IntrospectionException {
//        AjcSpecXmlReader me = AjcSpecXmlReader.getReader();
        AjcSpecXmlReader.BProps[] expected
            = AjcSpecXmlReader.expectedProperties();
        PropertyDescriptor[] des;
		for (AjcSpecXmlReader.BProps bProps : expected) {
			Class<?> clazz = bProps.cl;
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			assertTrue(null != beanInfo);
			des = beanInfo.getPropertyDescriptors();
			for (int j = 0; j < bProps.props.length; j++) {
				String name = bProps.props[j];
				String fqn = clazz.getName() + "." + name;
				boolean gotIt = false;
				for (PropertyDescriptor de : des) {
					String desName = de.getName();
					if (name.equals(desName)) {
						assertTrue(fqn, null != de.getWriteMethod());
						gotIt = true;
					}
				}
				assertTrue("no such property: " + fqn, gotIt);
			}
		}
        
    }
//    public void testAjcTests() throws IOException { 
//        checkXmlRoundTrip("../tests/ajcTests");
//    }

    public void testAjcTests10() throws IOException { 
        checkXmlRoundTrip("../tests/ajcTests10");
    }

    public void testAjcTestsBroken() throws IOException { 
        checkXmlRoundTrip("../tests/ajcTestsBroken");
    }

    public void testAjcTestsAttic() throws IOException { 
        checkXmlRoundTrip("../tests/ajcTestsAttic");
    }
    
    public void testAjcHarnessTests() throws IOException { 
        checkXmlRoundTrip("../tests/ajcHarnessTests");
    }

    void checkXmlRoundTrip(String path) throws IOException {
        String xmlPath = path + ".xml";
        String xml2Path = path + ".tmp.xml";

        final File file1 = new File(xmlPath);
        final List<File> toDelete = new ArrayList<>();
        final AjcSpecXmlReader writer = AjcSpecXmlReader.getReader();

        assertTrue("" + file1, file1.canRead());
        AjcTest.Suite.Spec suite1 = writer.readAjcSuite(file1);
        assertNotNull(suite1);

        File file2 = new File(xml2Path);
        String warning = writer.writeSuiteToXmlFile(file2, suite1);
        toDelete.add(file2);
        assertTrue(warning, null == warning);

        AjcTest.Suite.Spec suite2 = writer.readAjcSuite(file1);
        assertNotNull(suite2);
        AjcSpecTest.sameAjcSuiteSpec(suite1, suite2, this);
        
        // check clone while we're here
        try {
            Object clone = (AjcTest.Suite.Spec) suite1.clone();
            AjcSpecTest.sameAjcSuiteSpec(suite1, (AjcTest.Suite.Spec) clone, this);
            clone = (AjcTest.Suite.Spec) suite2.clone();
            AjcSpecTest.sameAjcSuiteSpec(suite2, (AjcTest.Suite.Spec) clone, this);
            AjcSpecTest.sameAjcSuiteSpec(suite1, (AjcTest.Suite.Spec) clone, this);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(System.err);
            assertTrue("CloneNotSupportedException: " + e.getMessage(), false);
        }

		for (File file : toDelete) {
			file.delete();
		}
    }

    void checkRoundTrip(String path) throws IOException, Exception {
        // XXX once .txt gone, add bugId and keywords to test
        String txtPath = path + ".txt";
        String xmlPath = path + ".tmp.xml";
        String xml2Path = path + ".tmp2.xml";

        // read flat, write the xml variant, read back, and compare
        AjcSpecXmlReader writer = AjcSpecXmlReader.getReader();
        File file0 = new File(txtPath);
        File file1 = new File(xmlPath);
        List<File> toDelete = new ArrayList<>();
        AjcTest.Suite.Spec suite0 = null;
        if (file0.canRead()) {
            System.out.println("reading " + file0);
            suite0 = FlatSuiteReader.ME.readSuite(file0);            
            String warning = writer.writeSuiteToXmlFile(file1, suite0);
            toDelete.add(file1);
            assertTrue(warning, null == warning);
        } else {
            file1 = new File(path + ".xml");
            if (file1.canRead()) {
                System.out.println("reading " + file1);
                suite0 = writer.readAjcSuite(file1);
            } else {
            	System.err.println("Skipping as not in module: " + file0);
            	return;
            }
        }
        assertNotNull(suite0);
        
        //System.err.println("----------------------- suite0 " + txtPath);
        //suite0.printAll(System.err, "");
        assertTrue("" + file1, file1.canRead());
        System.out.println("reading " + file1);
        AjcTest.Suite.Spec suite1 = writer.readAjcSuite(file1);
        assertNotNull(suite1);
        //System.err.println("----------------------- suite1 " + xmlPath);
        //suite0.printAll(System.err, "");
        AjcSpecTest.sameAjcSuiteSpec(suite0, suite1, this);

        // same for second-generation xml
        file1 = new File(xml2Path);
        String warning = writer.writeSuiteToXmlFile(file1, suite1);
        toDelete.add(file1);
        // XXX enable later assertTrue(warning, null == warning);
        if (null != warning) {
            System.out.println("warning " + file1 + ": " + warning);
        }
        System.out.println("reading " + file1);
        AjcTest.Suite.Spec suite2 = writer.readAjcSuite(file1);
        assertNotNull(suite2);
        //System.err.println("----------------------- suite2 " + xml2Path);
        AjcSpecTest.sameAjcSuiteSpec(suite1, suite2, this);
        AjcSpecTest.sameAjcSuiteSpec(suite0, suite2, this);

		for (File file : toDelete) {
			file.delete();
		}
    }
}

	// ------------------- XXX retry execution round-trips when eclipse working
        //AjcSpecTest.sameAjcTestSpec(txtList, xmlSpec, this);
        
//        List xmlList = writer.readAjcTests(xmlFile);
//        AjcSpecTest.sameAjcTestLists(txtList, xmlList, this);
//        List xml2List = writer.readAjcTests(xmlFile);
//        AjcSpecTest.sameAjcTestLists(xmlList, xml2List, this);


        // ------------------ now run them both and compare results
//        // run the flat and xml variants, then compare
//        MessageHandler xmlHandler = new MessageHandler();
//        IRunStatus xmlStatus = runFile(xmlPath, xmlHandler);
//        MessageHandler txtHandler = new MessageHandler();
//        IRunStatus txtStatus = runFile(txtPath, txtHandler);
//        
//        
//        // both should pass or fail..
//        IRunValidator v = RunValidator.NORMAL;
//        boolean xmlPassed = v.runPassed(xmlStatus);
//        boolean txtPassed = v.runPassed(txtStatus);
//        boolean passed = (xmlPassed == txtPassed);
//        if (!xmlPassed) {
//            MessageUtil.print(System.err, xmlStatus);
//        }
//        if (!txtPassed) {
//            MessageUtil.print(System.err, txtStatus);
//        }
//        if (!passed) { // calculate diffs
//        }
//    IRunStatus runFile(String path, MessageHandler handler) throws IOException {
//        Main runner = new Main(new String[] {path}, handler);
//        String id = "AjcSpecXmlReaderTest.runFile(" + path + ")";
//        return runner.runMain(id, handler, System.err); 
//    }
