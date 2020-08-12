/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adrian Colyer,  Abraham Nevado (lucierna)
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
 * @author Adrian Colyer
 */
public class RunSpec implements ITestStep {

	private List<ExpectedMessageSpec> expected = new ArrayList<>();
	private String classToRun;
	private String moduleToRun;  // alternative to classToRun on JDK9+
	private String baseDir;
	private String options;
	private String cpath;
	private String mpath;
	private String orderedStderr;
	private AjcTest myTest;
	private OutputSpec stdErrSpec;
	private OutputSpec stdOutSpec;
	private String ltwFile;
	private String xlintFile;
	private String vmargs;
	private String usefullltw;

	@Override
	public String toString() {
		return "RunSpec: Running '"+classToRun+"' in directory '"+baseDir+"'.  Classpath of '"+cpath+"'";
	}
	public RunSpec() {
	}

	@Override
	public void execute(AjcTestCase inTestCase) {
		if (!expected.isEmpty()) {
			System.err.println("Warning, message spec for run command is currently ignored (org.aspectj.testing.RunSpec)");
		}
		String[] args = buildArgs();
		//		 System.err.println("? execute() inTestCase='" + inTestCase + "', ltwFile=" + ltwFile);
		boolean useLtw = copyLtwFile(inTestCase.getSandboxDirectory());

		copyXlintFile(inTestCase.getSandboxDirectory());
		try {
			setSystemProperty("test.base.dir", inTestCase.getSandboxDirectory().getAbsolutePath());

			AjcTestCase.RunResult rr = inTestCase.run(getClassToRun(), getModuleToRun(), args, vmargs, getClasspath(), getModulepath(), useLtw, "true".equalsIgnoreCase(usefullltw));

			if (stdErrSpec != null) {
				stdErrSpec.matchAgainst(rr.getStdErr(), orderedStderr);
			}
			if (stdOutSpec != null) {
				stdOutSpec.matchAgainst(rr.getStdOut());
			}
		} finally {
			restoreProperties();
		}
	}

	/*
	 * Logic to save/restore system properties. Copied from LTWTests. As Matthew noted, need to refactor LTWTests to use this
	 */

	private Properties savedProperties = new Properties();

	public void setSystemProperty(String key, String value) {
		Properties systemProperties = System.getProperties();
		copyProperty(key, systemProperties, savedProperties);
		systemProperties.setProperty(key, value);
	}

	private static void copyProperty(String key, Properties from, Properties to) {
		String value = from.getProperty(key, NULL);
		to.setProperty(key, value);
	}

	private final static String NULL = "null";

	protected void restoreProperties() {
		Properties systemProperties = System.getProperties();
		for (Enumeration<Object> enu = savedProperties.keys(); enu.hasMoreElements();) {
			String key = (String) enu.nextElement();
			String value = savedProperties.getProperty(key);
			if (value == NULL)
				systemProperties.remove(key);
			else
				systemProperties.setProperty(key, value);
		}
	}

	@Override
	public void addExpectedMessage(ExpectedMessageSpec message) {
		expected.add(message);
	}

	@Override
	public void setBaseDir(String dir) {
		this.baseDir = dir;
	}

	@Override
	public void setTest(AjcTest test) {
		this.myTest = test;
	}

	public AjcTest getTest() {
		return this.myTest;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public String getClasspath() {
		if (cpath == null)
			return null;
		return this.cpath.replace('/', File.separatorChar).replace(',', File.pathSeparatorChar);
	}

	public String getModulepath() {
		if (mpath == null)
			return null;
		return this.mpath.replace('/', File.separatorChar).replace(',', File.pathSeparatorChar);
	}

	public void setModulepath(String mpath) {
		this.mpath = mpath;
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

	public String getClassToRun() {
		return classToRun;
	}

	public void setClassToRun(String classToRun) {
		this.classToRun = classToRun;
	}

	public void setModuleToRun(String moduleToRun) {
		this.moduleToRun = moduleToRun;
	}

	public String getModuleToRun() {
		return this.moduleToRun;
	}

	public String getLtwFile() {
		return ltwFile;
	}

	public void setLtwFile(String ltwFile) {
		this.ltwFile = ltwFile;
	}

	private String[] buildArgs() {
		if (options == null)
			return new String[0];
		StringTokenizer strTok = new StringTokenizer(options, ",");
		String[] ret = new String[strTok.countTokens()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = strTok.nextToken();
		}
		return ret;
	}

	private boolean copyLtwFile(File sandboxDirectory) {
		boolean useLtw = false;

		if (ltwFile != null) {
			// TODO maw use flag rather than empty file name
			if (ltwFile.trim().length() == 0)
				return true;

			File from = new File(baseDir, ltwFile);
			File to = new File(sandboxDirectory, "META-INF" + File.separator + "aop.xml");
			// System.out.println("RunSpec.copyLtwFile() from=" + from.getAbsolutePath() + " to=" + to.getAbsolutePath());
			try {
				FileUtil.copyFile(from, to);
				useLtw = true;
			} catch (IOException ex) {
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

	public void setVmargs(String vmargs) {
		this.vmargs = vmargs;
	}

	public String getVmargs() {
		return vmargs;
	}


	public String getUsefullltw() {
		return usefullltw;
	}

	public void setUsefullltw(String usefullltw) {
		this.usefullltw = usefullltw;
	}

	private void copyXlintFile(File sandboxDirectory) {
		if (xlintFile != null) {
			File from = new File(baseDir, xlintFile);
			File to = new File(sandboxDirectory, File.separator + xlintFile);
			try {
				FileUtil.copyFile(from, to);
			} catch (IOException ex) {
				AjcTestCase.fail(ex.toString());
			}
		}
	}
}
