/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.ajde.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.aspectj.asm.AsmManager;
import org.aspectj.tools.ajc.Ajc;

/**
 * Testcase class to be used by all ajde.core tests. Provides
 * helper methods to set up the environment in a sandbox
 * as well as to drive a build.
 */
public class AjdeCoreTestCase extends TestCase {

    public final static String testdataSrcDir = "../ajde.core/testdata";
	protected static File sandboxDir;
	
	private String projectDir;
    private AjCompiler compiler;

    
	protected void setUp() throws Exception {
		super.setUp();
		// Create a sandbox in which to work
		sandboxDir = Ajc.createEmptySandbox();
		// AMC - added this next line as a temporary workaround for 
		// listener leakage in AsmManager induced by the Ajde test suite.
		AsmManager.getDefault().removeAllListeners();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		compiler.clearLastState();
		compiler = null;
	}
	
	/**
	 * Fill in the working directory with the project files and
	 * creates a compiler instance for this project
	 */
	public void initialiseProject(String projectName) throws IOException {
		File projectSrc=new File(testdataSrcDir + File.separatorChar + projectName);
		File destination=new File(getWorkingDir(),projectName);
		if (!destination.exists()) {destination.mkdir();}
		copy(projectSrc,destination);
		projectDir = destination.getCanonicalPath();//getAbsolutePath();
		
		compiler = new AjCompiler(
				projectDir,
				new TestCompilerConfiguration(projectDir),
				new TestBuildProgressMonitor(),
				new TestMessageHandler()); 
	}
	
	/**
	 * @return the working directory
	 */
	protected File getWorkingDir() { 
		return sandboxDir; 
	}
	
	/**
	 * @return the absolute path of the project directory
	 * for example c:\temp\ajcSandbox\ajcTest15200.tmp\myProject
	 */
	protected String getAbsoluteProjectDir() {
		return projectDir;
	}
	
	/**
	 * Copy the contents of some directory to another location - the
	 * copy is recursive.
	 */
	private void copy(File from, File to) {
		String contents[] = from.list();
		if (contents==null) return;
		for (int i = 0; i < contents.length; i++) {
			String string = contents[i];
			File f = new File(from,string);
			File t = new File(to,string);
			
			if (f.isDirectory()) {
				t.mkdir();
				copy(f,t);
			} else if (f.isFile()) {
				try {
					org.aspectj.util.FileUtil.copyFile(f,t);
				} catch (IOException e) {
					throw new AssertionFailedError("Unable to copy " + f + " to " + t);
				}
			} 
		}
	}
	
	protected File openFile(String path) {
		return new File(projectDir + File.separatorChar + path);
	}
	
	public void doBuild() {
		doBuild(true);
	}
	
	public void doBuild(boolean buildFresh) {
		if (buildFresh) {
			compiler.buildFresh();
		} else {
			compiler.build();
		}
	}
	
	public AjCompiler getCompiler() {
		return compiler;
	}
	
	public boolean checkFor(String what) {
		List ll = ((TestMessageHandler)compiler.getMessageHandler()).getMessages();
		for (Iterator iter = ll.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element.toString().indexOf(what) != -1)
				return true;
		}
		return false;
	}
	
	public void dumpTaskData() {
		List ll = ((TestMessageHandler)compiler.getMessageHandler()).getMessages();
		for (Iterator iter = ll.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			System.out.println("RecordedMessage>"+element);
		}
	}
	
	public List getSourceFileList(String[] files) {
		List sourceFiles = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			sourceFiles.add(getAbsoluteProjectDir() + File.separator + files[i]);
		}
		return sourceFiles;
	}
	
}
