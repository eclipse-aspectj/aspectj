/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     AMC 2003     initial version
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author colyer
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CompilerMessagesTest extends AjdeTestCase {
	
	private final String CONFIG_FILE_PATH = "../examples/declare-warning/all.lst";

	/**
	 * Constructor for CompilerMessagesTest.
	 * @param name
	 */
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
		assertEquals( 8, task.location.getLine());
		assertEquals( "Please don't call init methods", task.message);
		try {
			String fullyQualifiedFile = task.location.getSourceFile().getCanonicalPath();
			// this name has a tester specific prefix, followed by the location of the file.
			// we can validate the ending.
			fullyQualifiedFile = fullyQualifiedFile.replace('\\','/');  // ignore platform differences in slashes
			assertTrue( "Fully-qualified source file location returned", 
				fullyQualifiedFile.endsWith("testdata/examples/declare-warning/apackage/SomeClass.java"));
		} catch (IOException ex) {
			assertTrue( "Unable to convert source file location: " + task.location.getSourceFile(), false);
		}
	}


	public void testDeclareMessageContents() {
		List msgs = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		assertEquals( "Please don't call setters" , ((NullIdeTaskListManager.SourceLineTask) msgs.get(1)).message);

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
