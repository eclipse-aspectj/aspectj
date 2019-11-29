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

package org.aspectj.testing.harness.bridge;

import java.io.PrintWriter;
import java.util.List;

import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.xml.XMLWriter;

import junit.framework.TestCase;

/**
 * 
 */
public class AbstractRunSpecTest extends TestCase {

	public AbstractRunSpecTest(String name) {
		super(name);
	}

    public void testXmlWrite() {
        AbstractRunSpec spec = new TestSpec();
        spec.setOptions("-option1,-option2");
        spec.setKeywords("keyword1, keyword2");
        spec.setPaths("path1.java, path2.java");
        spec.setDescription("some description, with extra");
        XMLWriter out = new XMLWriter(new PrintWriter(System.out));
        spec.writeXml(out);
        //out.close();//FIXME this close System.out and makes the IntelliJ test runner hang (AV)
    }

    public void testSetOptions() {
        AbstractRunSpec spec = new TestSpec();
        spec.setOptions("1,2");
        List options = spec.getOptionsList();
        String s = "" + options;
        assertTrue(s, "[1, 2]".equals(s));
    }
    
    static class TestSpec extends AbstractRunSpec {
        TestSpec() {
            super("testspec");            
        }
        /**
		 * @see org.aspectj.testing.harness.bridge.AbstractRunSpec#makeRunIterator(Sandbox, Validator)
		 */
		public IRunIterator makeRunIterator(
			Sandbox sandbox,
			Validator validator) {
			return null;
		}
    }
}
