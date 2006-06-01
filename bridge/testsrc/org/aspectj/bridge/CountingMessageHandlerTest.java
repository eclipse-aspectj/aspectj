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

package org.aspectj.bridge;


import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 */
public class CountingMessageHandlerTest extends TestCase {

   private static final String ME 
        = "org.aspectj.bridge.CountingMessageHandlerTest"; // XXX

    /** @param args ignored */
    public static void main(String[] args) {
        TestRunner.main(new String[] {ME});
    }

	/**
	 * Constructor for MessageTest.
	 * @param name
	 */
	public CountingMessageHandlerTest(String name) {
		super(name);
	}

    public void testSimpleWrapping() {
        MessageHandler m = new MessageHandler();
        CountingMessageHandler me = new CountingMessageHandler(m);
        checkCountingMessageHandler(me);
    }

    public void testCounterWrapping() {
        MessageHandler m = new MessageHandler();
        CountingMessageHandler first = new CountingMessageHandler(m);
        CountingMessageHandler me = new CountingMessageHandler(first);
        checkCountingMessageHandler(me);
    }

    void checkCountingMessageHandler(CountingMessageHandler me) {
        MessageUtil.warn(me, "warn 1");
        assertTrue(!me.hasErrors());
        assertEquals(0 , me.numMessages(IMessage.ERROR, false));
        assertEquals(1 , me.numMessages(IMessage.WARNING, false));
        assertEquals(0 , me.numMessages(IMessage.INFO, false));
        assertEquals(0 , me.numMessages(IMessage.ERROR, true));
        assertEquals(1 , me.numMessages(IMessage.WARNING, true));
        assertEquals(1 , me.numMessages(IMessage.INFO, true));
        
        MessageUtil.info(me, "info 1");
        assertTrue(!me.hasErrors());
        assertEquals(0 , me.numMessages(IMessage.ERROR, false));
        assertEquals(1 , me.numMessages(IMessage.WARNING, false));
        assertEquals(1 , me.numMessages(IMessage.INFO, false));
        assertEquals(0 , me.numMessages(IMessage.ERROR, true));
        assertEquals(1 , me.numMessages(IMessage.WARNING, true));
        assertEquals(2 , me.numMessages(IMessage.INFO, true));

        MessageUtil.error(me, "error 1");
        assertTrue(me.hasErrors());
        assertEquals(1 , me.numMessages(IMessage.ERROR, false));
        assertEquals(1 , me.numMessages(IMessage.WARNING, false));
        assertEquals(1 , me.numMessages(IMessage.INFO, false));
        assertEquals(1 , me.numMessages(IMessage.ERROR, true));
        assertEquals(2 , me.numMessages(IMessage.WARNING, true));
        assertEquals(3 , me.numMessages(IMessage.INFO, true));
    }    
}
