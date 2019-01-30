/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     AMC 2003         initial version
 *     Helen Hawkins    Converted to new interface (bug 148190)
 * ******************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.ajde.core.TestMessageHandler.TestMessage;
import org.aspectj.bridge.IMessage;

public class CompilerMessagesTest extends AjdeCoreTestCase {

	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;
	private String[] files = { "apackage" + File.separator + "InitCatcher.java",
			"apackage" + File.separator + "SomeClass.java" };
	
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("declare-warning");
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		doBuild(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}
	
	public void testMessages() {
		// bug 33474
		// The build has happened, what messages did the compiler give, and do they
		// contain the information we expect?
		List<TestMessage> msgs = handler.getMessages();
        if (2 != msgs.size()) {
            assertTrue("not two messages: " + msgs, false);
        }
		assertEquals("Two warning messages should be produced",2,msgs.size());
		TestMessageHandler.TestMessage msg = 
			(TestMessageHandler.TestMessage) msgs.get(0);
		assertEquals( 8, msg.getContainedMessage().getSourceLocation().getLine());
		assertEquals( "Please don't call init methods", msg.getContainedMessage().getMessage());
		try {
			String fullyQualifiedFile = msg.getContainedMessage().getSourceLocation().getSourceFile().getCanonicalPath();
			// this name has a tester specific prefix, followed by the location of the file.
			// we can validate the ending.
			fullyQualifiedFile = fullyQualifiedFile.replace('\\','/');  // ignore platform differences in slashes
			assertTrue( "Fully-qualified source file location returned", 
				fullyQualifiedFile.endsWith("/declare-warning/apackage/SomeClass.java"));
		} catch (IOException ex) {
			assertTrue( "Unable to convert source file location: " + msg.getContainedMessage().getSourceLocation().getSourceFile(), false);
		}
	}
	
	public void testDeclareMessageContents() {
		List<TestMessage> msgs = handler.getMessages();
		IMessage msg = ((TestMessageHandler.TestMessage)msgs.get(1)).getContainedMessage();
		assertEquals( "Please don't call setters" , msg.getMessage());
		assertEquals("field-set(int apackage.SomeClass.x)", msg.getDetails());
	}
}
