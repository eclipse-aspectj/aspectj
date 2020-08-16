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


package org.aspectj.testing.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;

import junit.framework.TestCase;

/**
 * 
 */
public class MessageUtilTest extends TestCase {
    public MessageUtilTest(String s) {
        super(s);
    }
    
    MessageHandler samples;
    List /* Exception */ exceptions;
    List /*ISourceLocation*/ locations;
    List /*String */ messageTexts;
    

    public void testMessageRendering() {
        MessageHandler messages = getSampleMessages();
        System.out.println("testMessageRendering(): run manually evaluate by inspection");
        PrintStream oldOut = System.out;
        // comment to inspect manually
        System.setOut(NullPrintStream.NULL_PrintStream);
        try {
            MessageUtil.print(System.out, messages, "all             label -> ", MessageUtil.MESSAGE_LABEL, MessageUtil.PICK_ALL);
            MessageUtil.print(System.out, messages, "info            short -> ", MessageUtil.MESSAGE_SHORT, MessageUtil.PICK_INFO);
            MessageUtil.print(System.out, messages, "fail            line  -> ", MessageUtil.MESSAGE_LINE, MessageUtil.PICK_FAIL);
            MessageUtil.print(System.out, messages, "debug      wide line  -> ", MessageUtil.MESSAGE_WIDELINE, MessageUtil.PICK_DEBUG);
            MessageUtil.print(System.out, messages, "warn    no-loc label  -> ", MessageUtil.MESSAGE_LABEL_NOLOC, MessageUtil.PICK_WARNING);
            MessageUtil.print(System.out, messages, "abort  force-loc line -> ", MessageUtil.MESSAGE_LINE_FORCE_LOC, MessageUtil.PICK_ABORT);
            MessageUtil.print(System.out, messages, "info+           short -> ", MessageUtil.MESSAGE_SHORT, MessageUtil.PICK_INFO_PLUS);
            MessageUtil.print(System.out, messages, "fail+           line  -> ", MessageUtil.MESSAGE_LINE, MessageUtil.PICK_FAIL_PLUS);
            MessageUtil.print(System.out, messages, "debug+     wide line  -> ", MessageUtil.MESSAGE_WIDELINE, MessageUtil.PICK_DEBUG_PLUS);
            MessageUtil.print(System.out, messages, "warn+   no-loc label  -> ", MessageUtil.MESSAGE_LABEL_NOLOC, MessageUtil.PICK_WARNING_PLUS);
            MessageUtil.print(System.out, messages, "abort+ force-loc line -> ", MessageUtil.MESSAGE_LINE_FORCE_LOC, MessageUtil.PICK_ABORT_PLUS);
        } finally {
            System.setOut(oldOut);
        }
    }


    List getSampleMessageTexts() {
        if (null == messageTexts) {
			List result = new ArrayList(Arrays.asList(new String[]
					{"one", "two", "now is the time for all good men..."}));
            messageTexts = result;
        }
        return messageTexts;
    }
    
    List getSampleExceptions() {
        if (null == exceptions) {
            List result = new ArrayList();
            int i = 1;
            result.add(new Error("Error " + i++));
            result.add(new RuntimeException("RuntimeException " + i++));
            result.add(new IOException("IOException " + i++));
            exceptions = result;
        }
        return exceptions;
    }

    List getSampleLocations() {
        if (null == locations) {
            List result = new ArrayList();
            File file = new File("testsrc/org/aspectj/testing/util/MessageUtilTest.java");
            result.add(new SourceLocation(file, 1, 2, 1));
            result.add(new SourceLocation(file, 100, 100, 0));
            locations = result;
        }
        return locations;
    }

    MessageHandler getSampleMessages() {
        MessageHandler result = new MessageHandler();
		for (IMessage.Kind kind : IMessage.KINDS) {
			for (Object item : getSampleLocations()) {
				ISourceLocation sourceLoc = (ISourceLocation) item;
				for (Object value : getSampleMessageTexts()) {
					String text = (String) value;
					for (Object o : getSampleExceptions()) {
						Throwable thrown = (Throwable) o;
						result.handleMessage(new Message(text, kind, thrown, sourceLoc));
					}
					result.handleMessage(new Message(text, kind, null, sourceLoc));
				}
				result.handleMessage(new Message("", kind, null, sourceLoc));
			}
			result.handleMessage(new Message("", kind, null, null));
		}
        return result;
    }
    
}
