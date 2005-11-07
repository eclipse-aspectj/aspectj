/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	private AjcTest myTest;
	private OutputSpec stdErrSpec;
	private OutputSpec stdOutSpec;
	private String ltwFile;
	
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
		AjcTestCase.RunResult rr = inTestCase.run(getClassToRun(),args,getClasspath(),useLtw);
		if (stdErrSpec != null) {
			stdErrSpec.matchAgainst(rr.getStdErr());
		}
		if (stdOutSpec != null) {
			stdOutSpec.matchAgainst(rr.getStdOut());
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
		return this.cpath.replace('/', File.separatorChar);
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
				ex.printStackTrace();
			}
		}
		
		return useLtw;
	}
}
