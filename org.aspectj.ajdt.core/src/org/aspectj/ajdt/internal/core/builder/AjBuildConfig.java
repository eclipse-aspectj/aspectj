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


package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.util.*;

/**
 * All configuration information needed to run the AspectJ compiler.
 */
public class AjBuildConfig {
	
	public static final String AJLINT_INGORE = "ignore";
	public static final String AJLINT_WARN = "warn";
	public static final String AJLINT_ERROR = "error";
	public static final String AJLINT_DEFAULT = "default";
	
	private File outputDir;
	private File outputJar;
	private List/*File*/ sourceRoots = new ArrayList();
	private List/*File*/ files = new ArrayList();
	private List/*File*/ inJars = new ArrayList();
	private List/*File*/ aspectpath = new ArrayList();
	private List/*String*/ classpath = new ArrayList();
	private Map javaOptions = new HashMap();
	private Map ajOptions = new HashMap();
	private File configFile;
	private boolean generateModelMode = false;
	private boolean emacsSymMode = false;
	private boolean noWeave = false;
	private boolean XserializableAspects = false;
	private boolean XnoInline = false;
	private String lintMode = AJLINT_DEFAULT;
	private File lintSpecFile = null;

	/**
	 * returned files includes <ul>
	 * <li>files explicitly listed on command-line</li>
	 * <li>files listed by reference in argument list files</li>
	 * <li>files contained in sourceRootDir if that exists</li>
	 * </ul>
	 * 
	 * @return all source files that should be compiled.
	 */
	public List/*File*/ getFiles() {
		return files;
	}

	public File getOutputDir() {  
		return outputDir;
	}

	public void setFiles(List files) {
		this.files = files;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public Map getAjOptions() {
		return ajOptions;
	}

	/**
	 * @return the Map expected by org.eclipse.jdt.core.internal.Compiler.
	 */
	public Map getJavaOptions() {
		return javaOptions;
	}

	public void setAjOptions(Map ajOptions) {
		this.ajOptions = ajOptions;
	}

	public void setJavaOptions(Map javaOptions) {
		this.javaOptions = javaOptions;
	}
	
	/**
	 * This includes all entries from -bootclasspath, -extdirs, -classpath, 
	 */
	public List getClasspath() {
		return classpath;
	}

	public void setClasspath(List classpath) {
		this.classpath = classpath;
	}

	public File getOutputJar() {
		return outputJar;
	}

	public List/*File*/ getInJars() {
		return inJars;
	}

	public void setOutputJar(File outputJar) {
		this.outputJar = outputJar;
	}

	public void setInJars(List sourceJars) {
		this.inJars = sourceJars;
	}

	public List getSourceRoots() {
		return sourceRoots;
	}

	public void setSourceRoots(List sourceRootDir) {
		this.sourceRoots = sourceRootDir;
	}

	public File getConfigFile() {
		return configFile;
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}

	public boolean isEmacsSymMode() {
		return emacsSymMode;
	}

	public void setEmacsSymMode(boolean emacsSymMode) {
		this.emacsSymMode = emacsSymMode;
	}

	public boolean isGenerateModelMode() {
		return generateModelMode;
	}

	public void setGenerateModelMode(boolean structureModelMode) {
		this.generateModelMode = structureModelMode;
	}
	
	/**
	 * This includes injars and aspectpath
	 */
	public List getFullClasspath() {
		if (inJars.isEmpty() && aspectpath.isEmpty()) return getClasspath();
		List full = new ArrayList();
		for (Iterator i = inJars.iterator(); i.hasNext(); ) {
			full.add(((File)i.next()).getAbsolutePath());
		}
		for (Iterator i = aspectpath.iterator(); i.hasNext(); ) {
			full.add(((File)i.next()).getAbsolutePath());
		}
		full.addAll(getClasspath());
		return full;
	}

	public String getLintMode() {
		return lintMode;
	}

	public File getLintSpecFile() {
		return lintSpecFile;
	}

	public List getAspectpath() {
		return aspectpath;
	}

	public boolean isNoWeave() {
		return noWeave;
	}

	public void setLintMode(String lintMode) {
		this.lintMode = lintMode;
	}

	public void setLintSpecFile(File lintSpecFile) {
		this.lintSpecFile = lintSpecFile;
	}

	public void setAspectpath(List aspectpath) {
		this.aspectpath = aspectpath;
	}

	public void setNoWeave(boolean noWeave) {
		this.noWeave = noWeave;
	}

	public boolean isXserializableAspects() {
		return XserializableAspects;
	}

	public void setXserializableAspects(boolean xserializableAspects) {
		XserializableAspects = xserializableAspects;
	}

	public boolean isXnoInline() {
		return XnoInline;
	}

	public void setXnoInline(boolean xnoInline) {
		XnoInline = xnoInline;
	}

}
