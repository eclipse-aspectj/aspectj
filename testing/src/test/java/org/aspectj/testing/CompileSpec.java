/* *******************************************************************
 * Copyright (c) 2004,2016 IBM Corporation
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.testing.util.TestUtil;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class CompileSpec implements ITestStep {

	private List<ExpectedMessageSpec> expected = new ArrayList<>();
	
	private String files;
	private boolean includeClassesDir;
	private String aspectpath;
	private String classpath;
	private String modulepath;
	private String inpath;
	private String sourceroots;
	private String outjar;
	private String outxml;
	private String xlintfile;
	private String options;
	private String baseDir;
	private String extdirs;
	private AjcTest myTest;
	
	public CompileSpec() {
	}
	
	public void execute(AjcTestCase inTestCase) {
		File base = new File(baseDir);
		String[] args = buildArgs();
		CompilationResult result = inTestCase.ajc(base,args);
		AjcTestCase.MessageSpec messageSpec = buildMessageSpec();
		String failMessage = "test \"" + myTest.getTitle() + "\" failed";
		inTestCase.assertMessages(result,failMessage,messageSpec);
		inTestCase.setShouldEmptySandbox(false); // so subsequent steps in same test see my results
	}

	public void addExpectedMessage(ExpectedMessageSpec message) {
		expected.add(message);
	}

	public void setBaseDir(String dir) {
		this.baseDir = dir;
	}
	
	protected String getBaseDir() { return baseDir; }
		
	public void setTest(AjcTest t) {
		this.myTest = t;
		if (options != null && (options.contains("-1.5"))) {
		    myTest.setVm("1.5");
		}
	}
	
	protected AjcTest getTest() { return myTest; }
	
	/**
	 * @return Returns the aspectpath.
	 */
	public String getAspectpath() {
		return aspectpath;
	}
	/**
	 * @param aspectpath The aspectpath to set.
	 */
	public void setAspectpath(String aspectpath) {
		this.aspectpath = aspectpath.replace(',',File.pathSeparatorChar);
	}
	/**
	 * @return Returns the classpath.
	 */
	public String getClasspath() {
		return classpath;
	}
	/**
	 * @param classpath The classpath to set.
	 */
	public void setClasspath(String classpath) {
		this.classpath = classpath.replace(',',File.pathSeparatorChar);
	}

	public String getModulepath() {
		return this.modulepath;
	}
	
	public void setModulepath(String modulepath) {
		this.modulepath = modulepath.replace(',', File.pathSeparatorChar);
	}
	/**
	 * @return Returns the files.
	 */
	public String getFiles() {
		return files;
	}
	/**
	 * @param files The files to set.
	 */
	public void setFiles(String files) {
		this.files = files;
	}
	/**
	 * @return Returns the includeClassesDir.
	 */
	public boolean isIncludeClassesDir() {
		return includeClassesDir;
	}
	/**
	 * @param includeClassesDir The includeClassesDir to set.
	 */
	public void setIncludeClassesDir(boolean includeClassesDir) {
		this.includeClassesDir = includeClassesDir;
	}
	/**
	 * @return Returns the inpath.
	 */
	public String getInpath() {
		return inpath;
	}
	/**
	 * @param inpath The inpath to set.
	 */
	public void setInpath(String inpath) {
		this.inpath = inpath.replace(',',File.pathSeparatorChar).replace(';',File.pathSeparatorChar);
	}
	/**
	 * @return Returns the options.
	 */
	public String getOptions() {
		return options;
	}
	/**
	 * @param options The options to set.
	 */
	public void setOptions(String options) {
		int i = options.indexOf("!eclipse");
		if (i != -1) {
			this.options = options.substring(0,i);
			this.options += options.substring(i + "!eclipse".length());
		} else {
			this.options = options;
		}
	}
	/**
	 * @return Returns the outjar.
	 */
	public String getOutjar() {
		return outjar;
	}
	/**
	 * @param outjar The outjar to set.
	 */
	public void setOutjar(String outjar) {
		this.outjar = outjar;
	}

	/**
	 * @return Returns the outxml.
	 */
	public String getOutxmlfile() {
		return outxml;
	}

	/**
	 * @param outxml The the of the aop.xml file to generate
	 */
	public void setOutxmlfile(String outxml) {
		this.outxml = outxml;
	}
	/**
	 * @return Returns the sourceroots.
	 */
	public String getSourceroots() {
		return sourceroots;
	}
	/**
	 * @param sourceroots The sourceroots to set.
	 */
	public void setSourceroots(String sourceroots) {
		this.sourceroots = sourceroots;
	}
	/**
	 * @return Returns the xlintfile.
	 */
	public String getXlintfile() {
		return xlintfile;
	}
	/**
	 * @param xlintfile The xlintfile to set.
	 */
	public void setXlintfile(String xlintfile) {
		this.xlintfile = xlintfile;
	}
	
	public String getExtdirs() { return extdirs;}
	public void setExtdirs(String extdirs) { this.extdirs = extdirs; }
	
	protected String[] buildArgs() {
		StringBuffer args = new StringBuffer();
		// add any set options, and then files to compile at the end
		if (getAspectpath() != null) {
			args.append("-aspectpath ");
			args.append(getAspectpath());
			args.append(" ");
		}
		if (getSourceroots() != null) {
			args.append("-sourceroots ");
			args.append(getSourceroots());
			args.append(" ");
		}
		if (getOutjar() != null) {
			args.append("-outjar ");
			args.append(getOutjar());
			args.append(" ");
		}
		if (getOutxmlfile() != null) {
			args.append("-outxmlfile ");
			args.append(getOutxmlfile());
			args.append(" ");
		}
		if (getOptions() != null) {
			StringTokenizer strTok = new StringTokenizer(getOptions(),",");
			while (strTok.hasMoreTokens()) {
				// For an option containing a comma, pass in a { in its place
				args.append(strTok.nextToken().replace('{', ','));
				args.append(" ");
			}
		}
		if (getClasspath() != null) {
			args.append("-classpath ");
			args.append(getClasspath());
			args.append(" ");
		}
		if (getModulepath() != null) {
			args.append("-p ");
			args.append(rewrite(getModulepath()));
			args.append(" ");
		}
		if (getXlintfile() != null) {
			args.append("-Xlintfile ");
			args.append(getXlintfile());
			args.append(" ");
		}
		if (getExtdirs() != null) {
			args.append("-extdirs ");
			args.append(getExtdirs());
			args.append(" ");
		}
		List<String> fileList = new ArrayList<>();
		List<String> jarList = new ArrayList<>();
		// convention that any jar on file list should be added to inpath
		String files = getFiles();
	    if (files == null) files = "";
		StringTokenizer strTok = new StringTokenizer(files,",");
		while (strTok.hasMoreTokens()) {
			final String file = strTok.nextToken();
			if (file.endsWith(".jar")) {
				jarList.add(file);
            } else {
				fileList.add(file);
			}
		}
		if ((getInpath() != null) || !jarList.isEmpty()) {
			args.append("-inpath ");
			if (getInpath() != null) args.append(getInpath());
			for (String jar: jarList) {
				args.append(File.pathSeparator);
				args.append(jar);
			}
			args.append(" ");
		}
		for (String file: fileList) {
			args.append(file);
			args.append(" ");
		}
		String argumentString = args.toString();
		strTok = new StringTokenizer(argumentString," ");
		String[] ret = new String[strTok.countTokens()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = strTok.nextToken();
		}
		return ret;
	}
	
	private String rewrite(String path) {
		path = path.replace("$runtimemodule", TestUtil.aspectjrtPath(true).toString());
		path = path.replace("$runtime", TestUtil.aspectjrtPath().toString());
		return path;
	}

	protected AjcTestCase.MessageSpec buildMessageSpec() {
		List<AjcTestCase.Message> infos = null;
		List<AjcTestCase.Message> warnings = new ArrayList<>();
		List<AjcTestCase.Message> errors = new ArrayList<>();
		List<AjcTestCase.Message> fails = new ArrayList<>();
		List<AjcTestCase.Message> weaveInfos = new ArrayList<>();
		for (ExpectedMessageSpec exMsg: expected) {
			String kind = exMsg.getKind();
			if (kind.equals("info")) {
				if (infos == null) infos = new ArrayList<>();
				infos.add(exMsg.toMessage());
			} else if (kind.equals("warning")) {
				warnings.add(exMsg.toMessage());
			} else if (kind.equals("error")) {
				errors.add(exMsg.toMessage());				
			} else if (kind.equals("fail")) {
				fails.add(exMsg.toMessage());
			} else if (kind.equals("abort")) {
				fails.add(exMsg.toMessage());
			} else if (kind.equals("weave")) {
				weaveInfos.add(exMsg.toMessage());
			}
		}
		return new AjcTestCase.MessageSpec(infos,warnings,errors,fails,weaveInfos);
	}

}
