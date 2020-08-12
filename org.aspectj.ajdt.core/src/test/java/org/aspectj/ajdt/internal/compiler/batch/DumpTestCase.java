/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster 
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.weaver.Dump;

/**
 * @author websterm
 *
 * Test Dump facility. Ensure it can be configured and files contain expected contents. Testcase 
 * returns Dump configuration to orginal state.
 */
public class DumpTestCase extends TestCase {

	private File dumpFile;
	private IMessage.Kind savedDumpCondition;

	public DumpTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		dumpFile = null;
		savedDumpCondition = Dump.getDumpOnExit();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		if (dumpFile != null && dumpFile.exists()) {
			boolean deleted = dumpFile.delete();
			assertTrue("Dump file '" + dumpFile.getPath() + "' could not be deleted",deleted);
		} 
		Dump.setDumpOnExit(savedDumpCondition);
	}

	public void testSetDumpOnException () {
		Dump.setDumpOnException(true);
		assertTrue("DumpOnException should be true",Dump.getDumpOnException());
	}
	
	public void testSetDumpOnExit () {
		assertTrue("Should be able to set condition 'error'",Dump.setDumpOnExit("error"));
		assertTrue("Should be able to set condition 'warning'",Dump.setDumpOnExit("warning"));
		assertFalse("Should not be able to set condition 'junk'",Dump.setDumpOnExit("junk"));
	}
	
	public void testDump () {
		String fileName = Dump.dump("testDump()");
		dumpFile = new File(fileName);
		assertTrue("Dump file '" + fileName + "' should exist",dumpFile.exists());
	}
	
	public void testDumpWithException () {
		String message = "testDumpWithException()";
		String fileName = recursiveCall(message,100);
		dumpFile = new File(fileName);
		assertContents(dumpFile,"Exception Information",message);
	}
	
	public void testDumpOnExit () {
		Dump.setDumpOnExit("abort");
		Dump.saveMessageHolder(null);
		String fileName = Dump.dumpOnExit();
		dumpFile = new File(fileName);
		assertTrue("Dump file '" + fileName + "' should exist",dumpFile.exists());
	}
	
	public void testDumpOnExitExcluded () {
		Dump.setDumpOnExit("abort");
		IMessageHolder holder = new MessageHandler();
		Dump.saveMessageHolder(holder);
		holder.handleMessage(new Message("testDumpOnExitExcluded()",IMessage.ERROR,null,null));
		String fileName = Dump.dumpOnExit();
		dumpFile = new File(fileName);
		assertEquals("Dump '" + fileName + "' should be excluded",Dump.DUMP_EXCLUDED,fileName);
	}
	
	public void testDumpOnExitIncluded () {
		Dump.setDumpOnExit("error");
		IMessageHolder holder = new MessageHandler();
		Dump.saveMessageHolder(holder);
		IMessage error = new Message("testDumpOnExitIncluded()",IMessage.ERROR,null,null);
		holder.handleMessage(error);
		String fileName = Dump.dumpOnExit();
		dumpFile = new File(fileName);
		assertContents(dumpFile,"Compiler Messages",error.getMessage());
	}
	
	/* Ensure dump file exists and contains certain contents under a given heading */
	public static void assertContents (File dumpFile, String heading, String contents) {
		assertTrue("Dump file '" + dumpFile.getPath() + "' should exist",dumpFile.exists());
		assertTrue("Dump file '" + dumpFile.getPath()+ "' should contain '" + contents + "'",fileContains(dumpFile,heading,contents));
	}
	
	private static boolean fileContains (File dumpFile, String heading, String contents) {
		boolean result = false;
		
		try { 
			BufferedReader reader = new BufferedReader(new FileReader(dumpFile));
			String currentHeading = "";
			String record;
			while ((null != (record = reader.readLine())) && (result == false)) {
				if (record.startsWith("----")) currentHeading = record;
				else if ((record.contains(contents)) && currentHeading.contains(heading)) result = true;
			}
			reader.close();
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
		
		return result;
	}
	
	/* Generate a big stack trace */
	private String recursiveCall (String message, int depth) {
		if (depth == 0) {
			Throwable th = new RuntimeException(message);
			return Dump.dumpWithException(th);
		}
		else {
			return recursiveCall(message,--depth);
		}
	}

}
