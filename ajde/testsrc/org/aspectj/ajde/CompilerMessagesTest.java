/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     AMC 2003     initial version
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.IOException;
import java.util.List;

//import org.aspectj.bridge.*;
import org.aspectj.bridge.IMessage;

/**
 * @author colyer
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CompilerMessagesTest extends AjdeTestCase {
	
    // TODO-path
	private final String CONFIG_FILE_PATH = "../examples/declare-warning/all.lst";

	public CompilerMessagesTest(String name) {
		super(name);
	}

	public void testMessages() {
		// bug 33474
		// The build has happened, what messages did the compiler give, and do they
		// contain the information we expect?
		List msgs = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
        if (2 != msgs.size()) {
            assertTrue("not two messages: " + msgs, false);
        }
		assertEquals("Two warning messages should be produced",2,msgs.size());
		NullIdeTaskListManager.SourceLineTask task = 
			(NullIdeTaskListManager.SourceLineTask) msgs.get(0);
		assertEquals( 8, task.getContainedMessage().getSourceLocation().getLine());
		assertEquals( "Please don't call init methods", task.message.getMessage());
		try {
			String fullyQualifiedFile = task.getContainedMessage().getSourceLocation().getSourceFile().getCanonicalPath();
			// this name has a tester specific prefix, followed by the location of the file.
			// we can validate the ending.
			fullyQualifiedFile = fullyQualifiedFile.replace('\\','/');  // ignore platform differences in slashes
			assertTrue( "Fully-qualified source file location returned", 
				fullyQualifiedFile.endsWith("/examples/declare-warning/apackage/SomeClass.java"));
		} catch (IOException ex) {
			assertTrue( "Unable to convert source file location: " + task.getContainedMessage().getSourceLocation().getSourceFile(), false);
		}
	}
 

	public void testDeclareMessageContents() {
		List msgs = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		IMessage msg = (IMessage)((NullIdeTaskListManager.SourceLineTask)msgs.get(1)).getContainedMessage();
		assertEquals( "Please don't call setters" , msg.getMessage());
		assertEquals("field-set(int apackage.SomeClass.x)", msg.getDetails());
	}


	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp("examples");
		doSynchronousBuild(CONFIG_FILE_PATH);	
	}

	/*
	 * @see AjdeTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
