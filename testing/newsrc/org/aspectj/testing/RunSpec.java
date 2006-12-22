/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.util.FileUtil;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RunSpec implements ITestStep {

	private List expected = new ArrayList();
	private String classToRun;
	private String baseDir;
	private String options;
	private String cpath;
	private String orderedStderr;
	private AjcTest myTest;
	private OutputSpec stdErrSpec;
	private OutputSpec stdOutSpec;
	private String ltwFile;
	private String xlintFile;
	
	public RunSpec() {
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.testing.ITestStep#execute(org.aspectj.tools.ajc.AjcTestCase)
	 */
	public void execute(AjcTestCase inTestCase) {
		if (!expected.isEmpty()) {
			System.err.println("Warning, message spec for run command is currently ignored (org.aspectj.testing.RunSpec)");
		}
		String[] args = buildArgs();
//		System.err.println("? execute() inTestCase='" + inTestCase + "', ltwFile=" + ltwFile);
		boolean useLtw = copyLtwFile(inTestCase.getSandboxDirectory());
		copyXlintFile(inTestCase.getSandboxDirectory());
		try {
			setSystemProperty("test.base.dir", inTestCase.getSandboxDirectory().getAbsolutePath());
			
			AjcTestCase.RunResult rr = inTestCase.run(getClassToRun(),args,getClasspath(),useLtw);
			
			if (stdErrSpec != null) {
				stdErrSpec.matchAgainst(rr.getStdErr(),orderedStderr);
			}
			if (stdOutSpec != null) {
				stdOutSpec.matchAgainst(rr.getStdOut());
			}
		} finally {
			restoreProperties();
		}
	}
	
	/* 
	 * Logic to save/restore system properties. Copied from LTWTests.
	 * As Matthew noted, need to refactor LTWTests to use this 
  	 */

	private Properties savedProperties = new Properties();
  	 	
	public void setSystemProperty (String key, String value) {
		Properties systemProperties = System.getProperties();
		copyProperty(key,systemProperties,savedProperties);
		systemProperties.setProperty(key,value);
	}
	
	private static void copyProperty (String key, Properties from, Properties to) {
		String value = from.getProperty(key,NULL);
		to.setProperty(key,value);
	}
	
	private final static String NULL = "null";

	protected void restoreProperties() {
		Properties systemProperties = System.getProperties();
		for (Enumeration enu = savedProperties.keys(); enu.hasMoreElements(); ) {
			String key = (String)enu.nextElement();
			String value = savedProperties.getProperty(key);
			if (value == NULL) systemProperties.remove(key);
			else systemProperties.setProperty(key,value);
		}
	}

	public void addExpectedMessage(ExpectedMessageSpec message) {
		expected.add(message);
	}

	public void setBaseDir(String dir) {
		this.baseDir = dir;
	}

	public void setTest(AjcTest test) {
		this.myTest = test;
	}
	
	public String getOptions() {
		return options;
	}
	
	public void setOptions(String options) {
		this.options = options;
	}
	
	public String getClasspath() {
		if (cpath == null) return null;
		return this.cpath.replace('/', File.separatorChar).replace(',', File.pathSeparatorChar);
	}
 	
	public void setClasspath(String cpath) {
		this.cpath = cpath;
	}
	
	public void addStdErrSpec(OutputSpec spec) {
		this.stdErrSpec = spec;
	}
	public void addStdOutSpec(OutputSpec spec) {
		this.stdOutSpec = spec;
	}
	public void setOrderedStderr(String orderedStderr) {
		this.orderedStderr = orderedStderr;
	}
	/**
	 * @return Returns the classToRun.
	 */
	public String getClassToRun() {
		return classToRun;
	}
	/**
	 * @param classToRun The classToRun to set.
	 */
	public void setClassToRun(String classToRun) {
		this.classToRun = classToRun;
	}

	public String getLtwFile() {
		return ltwFile;
	}

	public void setLtwFile(String ltwFile) {
		this.ltwFile = ltwFile;
	}
	
	private String[] buildArgs() {
		if (options == null) return new String[0];
		StringTokenizer strTok = new StringTokenizer(options,",");
		String[] ret = new String[strTok.countTokens()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = strTok.nextToken();
		}
		return ret;
	}
	
	private boolean copyLtwFile (File sandboxDirectory) {
		boolean useLtw = false;
		
		if (ltwFile != null) {
            // TODO maw use flag rather than empty file name
			if (ltwFile.trim().length() == 0) return true;
			
			File from = new File(baseDir,ltwFile);
			File to = new File(sandboxDirectory,"META-INF" + File.separator + "aop.xml");
//			System.out.println("RunSpec.copyLtwFile() from=" + from.getAbsolutePath() + " to=" + to.getAbsolutePath());
			try {
				FileUtil.copyFile(from,to);
				useLtw = true;
			}
			catch (IOException ex) {
				AjcTestCase.fail(ex.toString());
			}
		}
		
		return useLtw;
	}

	public String getXlintFile() {
		return xlintFile;
	}

	public void setXlintFile(String xlintFile) {
		this.xlintFile = xlintFile;
	}

	private void copyXlintFile (File sandboxDirectory) {
		if (xlintFile != null) {
			File from = new File(baseDir,xlintFile);
			File to = new File(sandboxDirectory, File.separator + xlintFile);
			try {
				FileUtil.copyFile(from,to);
			}
			catch (IOException ex) {
				AjcTestCase.fail(ex.toString());
			}
		}
	}
}
