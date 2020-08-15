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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.util.LangUtil;

import junit.framework.TestCase;

/**
 * 
 */
public class MessageListXmlReaderTest extends TestCase {

	List tempFiles = new ArrayList();
	public MessageListXmlReaderTest(String name) {
		super(name);
	}

    public void setUp() {
    	tempFiles.clear();
    }

    public void tearDown() {
    	if (!LangUtil.isEmpty(tempFiles)) {
			for (Object tempFile : tempFiles) {
				File file = (File) tempFile;
				if (file.canRead()) {
					file.delete();
				}
			}
		}
    }
    public void testNothingOthersSkipped() {}
     
    public void skiptestMessageReading() throws Exception { 
        
        //assertTrue("XXX need better XML wrapping - has < character", false);
        //checkXmlRoundTrip("testdata/dirChangesTestDir/diff/expectedMessages");
        //checkXmlRoundTrip("testdata/dirChangesTestDir/same/expectedMessages");
    }

    void checkXmlRoundTrip(String path) throws Exception {
        String xmlPath = path + ".xml";
        String xml2Path = path + ".tmp.xml";
        final File file1 = new File(xmlPath);
        assertTrue("" + file1, file1.canRead());

        MessageListXmlReader reader = new MessageListXmlReader();
        IMessage[] messages = reader.readMessages(file1);
        assertNotNull(messages);
        assertTrue("empty messages", 0 != messages.length);

        File file2 = new File(xml2Path);
        String warning = reader.writeMessages(file2, messages);
        assertTrue(warning, null == warning);
        tempFiles.add(file2);

        IMessage[] messages2 = reader.readMessages(file2);
        assertEquals(LangUtil.arrayAsList(messages).toString(),
                    LangUtil.arrayAsList(messages2).toString());
    }
}
