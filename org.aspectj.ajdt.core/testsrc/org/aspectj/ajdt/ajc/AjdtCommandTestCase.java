/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.ajc;

import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.MessageWriter;
import org.aspectj.util.StreamPrintWriter;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import java.io.PrintWriter;

import junit.framework.TestCase;

/**
 * Some black-box test is happening here.
 */
public class AjdtCommandTestCase extends TestCase {

	private StreamPrintWriter outputWriter = new StreamPrintWriter(new PrintWriter(System.out));
	private AjdtCommand command = new AjdtCommand();	
	private MessageWriter messageWriter = new MessageWriter(outputWriter, false);
	
	public AjdtCommandTestCase(String name) {
		super(name);
//		command.buildArgParser.out = outputWriter;
	}
	
	public void testIncrementalOption() throws InvalidInputException {
		AjBuildConfig config = command.genBuildConfig(new String[] {  "-incremental" }, messageWriter);
		
		assertTrue(
			"didn't specify source root",
			outputWriter.getContents().indexOf("specify a source root") != -1);	
		
		outputWriter.flushBuffer();		
		config = command.genBuildConfig(
			new String[] { "-incremental", "-sourceroots", "testdata/src1" }, 
			messageWriter);
	
		assertTrue(
			outputWriter.getContents(),
			outputWriter.getContents().equals(""));			

		outputWriter.flushBuffer();		
		config = command.genBuildConfig(
			new String[] { "-incremental", "testdata/src1/Hello.java" }, 
			messageWriter);
	  
		assertTrue(
			"specified a file",
			outputWriter.getContents().indexOf("can not directly specify files") != -1);	;
	}
	
	public void testBadOptionAndUsagePrinting() throws InvalidInputException {
		try {
			command.genBuildConfig(new String[] { "-mubleBadOption" }, messageWriter);		
		} catch (AbortException ae) { }
		
		assertTrue(
			outputWriter.getContents() + " contains? " + "Usage",
			outputWriter.getContents().indexOf("Usage") != -1);		
		
	}
	
	public void testHelpUsagePrinting() {
		try {
			command.genBuildConfig(new String[] { "-help" }, messageWriter);
		} catch (AbortException  ae) { }
		assertTrue(
			outputWriter.getContents() + " contains? " + "Usage",
			outputWriter.getContents().indexOf("Usage") != -1);			
	}
	
	public void testVersionOutput() throws InvalidInputException {
		try {
			command.genBuildConfig(new String[] { "-version" }, messageWriter);
		} catch (AbortException ae) { }
		assertTrue(
			"version output",
			outputWriter.getContents().indexOf("AspectJ Compiler") != -1);		
	}
	
	public void testNonExistingLstFile() {
		command.genBuildConfig(new String[] { "@mumbleDoesNotExist" }, messageWriter);
		
		assertTrue(
			outputWriter.getContents(),
			outputWriter.getContents().indexOf("file does not exist") != -1);			
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		outputWriter.flushBuffer();
	}
}
