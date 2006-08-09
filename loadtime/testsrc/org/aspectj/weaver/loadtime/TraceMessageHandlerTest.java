/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import junit.framework.TestCase;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.IMessage.Kind;

public class TraceMessageHandlerTest extends TestCase {

	private TraceMessageHandler messageHandler;
	
	protected void setUp() throws Exception {
		super.setUp();
		messageHandler = new TraceMessageHandler();
	}

	public void testTraceMessageHandler() {
		TraceMessageHandler tmh = new TraceMessageHandler();
	}

	public void testDontIgnore() {
		Kind kind = IMessage.WEAVEINFO;
		messageHandler.dontIgnore(kind);
		boolean b = messageHandler.isIgnoring(kind);
		assertFalse("Don't filter message kinds",b);
	}

	public void testHandleMessageInfo () {
		IMessage message = MessageUtil.info("testHandleMessage");
		boolean b = messageHandler.handleMessage(message);
		assertTrue("Message not handled",b);
	}

	public void testIsIgnoring() {
		Kind kind = IMessage.WEAVEINFO;
		boolean b = messageHandler.isIgnoring(kind);
		assertFalse("Don't filter message kinds",b);
	}

	public void testRender() {
		String text = "testRender";
		IMessage message = MessageUtil.info(text);
		String s = messageHandler.render(message);
		assertTrue("Message not rendered correctly",s.indexOf(text) != -1);
	}

}
