/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.ajc;

import junit.framework.TestCase;

/**
 * @author Mik Kersten 
 */
public class ConsoleMessageHandlerTestCase extends TestCase {

	/**
	 * Constructor for ConsoleWriterTestCase.
	 * @param name
	 */
	public ConsoleMessageHandlerTestCase(String name) {
		super(name);
	}

	public void testIgnoringInfoMessages() {
		testOutput(false);
	}

	public void testHandlingInfoMessages() {
		testOutput(true);
	}
  
	private void testOutput(boolean verboseMode) {
		//XXX update to new MessageHandler
//		final String MESSAGE = "test;";
//
// 		StreamPrintWriter output = new StreamPrintWriter(new PrintWriter(System.out));
//	 	ConsoleMessageHandler writer = new ConsoleMessageHandler(output);
//	 	if (!verboseMode) writer.ignore(IMessage.INFO);
//	 	
//		writer.handleMessage(new Message(MESSAGE, Message.INFO, null, null));
//		if (verboseMode) {
//			assertTrue("message=" + output.getContents(), output.getContents().equals(MESSAGE + "\n"));
//		} else {
//			assertTrue("message=" + output.getContents(), output.getContents().equals(""));	
//		}
//		
//		output.flushBuffer();
//		writer.handleMessage(new Message(MESSAGE, Message.ERROR, null, null));
//		assertTrue(output.getContents(), output.getContents().equals(MESSAGE + "\n"));	
	}  
}
