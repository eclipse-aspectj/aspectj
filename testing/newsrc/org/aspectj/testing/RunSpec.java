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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.tools.ajc.AjcTestCase;

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
	private AjcTest myTest;
	private OutputSpec stdErrSpec;
	private OutputSpec stdOutSpec;
	
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
		AjcTestCase.RunResult rr = inTestCase.run(getClassToRun(),args,null);
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
	
	private String[] buildArgs() {
		if (options == null) return new String[0];
		StringTokenizer strTok = new StringTokenizer(options,",");
		String[] ret = new String[strTok.countTokens()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = strTok.nextToken();
		}
		return ret;
	}
}
