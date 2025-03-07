/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.ui.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajde.core.JavaOptions;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Test implementation of ICompilerConfiguration. Allows users to configure the settings via setter methods. By default returns null
 * for all options except getClasspath(), getJavaOptionsMap() (by default returns that it's 1.3 compliant),
 * getOutputLocationManager(), getSourcePathResources() (it recursively looks for them) and getProjectSourceFiles(). If no source
 * files are specified by the user, then getProjectSourceFiles() returns an empty list.
 */
public class TestCompilerConfiguration implements ICompilerConfiguration {

	private String projectPath;

	private Set<File> aspectpath;
	private Set<File> inpath;
	private String outjar;
	private Map<String,String> javaOptions;
	private String nonStandardOptions;
	private List<String> projectSourceFiles = new ArrayList<>();
	private Map<String,File> sourcePathResources;

	private String srcDirName = "src";

	private IOutputLocationManager outputLoc;

	public TestCompilerConfiguration(String projectPath) {
		this.projectPath = projectPath;
	}

	public void configurationRead() {
	}

	public List<String> getProjectXmlConfigFiles() {
		return Collections.emptyList();
	}

	public Set<File> getAspectPath() {
		return aspectpath;
	}

	public String getClasspath() {
		String cp = projectPath + File.pathSeparator + System.getProperty("sun.boot.class.path") + File.pathSeparator
				+ TestUtil.aspectjrtClasspath();
		if (LangUtil.isVMGreaterOrEqual(9)) {
			cp = LangUtil.getJrtFsFilePath()+File.pathSeparator+cp;
		}
		return cp;
	}

	public Set<File> getInpath() {
		return inpath;
	}

	public Map<String,String> getJavaOptionsMap() {
		if (javaOptions == null) {
			javaOptions = new Hashtable<>();
			javaOptions.put(JavaOptions.COMPLIANCE_LEVEL, JavaOptions.VERSION_18);
			javaOptions.put(JavaOptions.SOURCE_COMPATIBILITY_LEVEL, JavaOptions.VERSION_18);
		}
		return javaOptions;
	}

	public String getNonStandardOptions() {
		return nonStandardOptions;
	}

	public String getOutJar() {
		return outjar;
	}

	public IOutputLocationManager getOutputLocationManager() {
		if (outputLoc == null) {
			outputLoc = new TestOutputLocationManager(projectPath);
		}
		return outputLoc;
	}

	public List<String> getProjectSourceFiles() {
		return projectSourceFiles;
	}

	public List<File> getProjectSourceFilesChanged() {
		return null;
	}

	public Map<String,File> getSourcePathResources() {
		if (sourcePathResources == null) {
			sourcePathResources = new HashMap<>();

			/* Allow the user to override the testProjectPath by using sourceRoots */
			File[] srcBase = new File[] { new File(projectPath + File.separator + srcDirName) };

			for (File file : srcBase) {
				File[] fromResources = FileUtil.listFiles(file, new FileFilter() {
					public boolean accept(File pathname) {
						String name = pathname.getName().toLowerCase();
						return !name.endsWith(".class") && !name.endsWith(".java") && !name.endsWith(".aj")
								&& !name.endsWith(".lst") && !name.endsWith(".jar");
					}
				});
				for (File fromResource : fromResources) {
					String normPath = FileUtil.normalizedPath(fromResource, file);
					sourcePathResources.put(normPath, fromResource);
				}
			}
		}
		return sourcePathResources;
	}

	// -------------------- setter methods useful for testing ---------------
	public void setAspectPath(Set<File> aspectPath) {
		this.aspectpath = aspectPath;
	}

	public void setInpath(Set<File> inpath) {
		this.inpath = inpath;
	}

	public void setOutjar(String outjar) {
		this.outjar = outjar;
	}

	public void setJavaOptions(Map<String,String> javaOptions) {
		this.javaOptions = javaOptions;
	}

	public void setNonStandardOptions(String options) {
		this.nonStandardOptions = options;
	}

	public void setProjectSourceFiles(List<String> projectSourceFiles) {
		this.projectSourceFiles = projectSourceFiles;
	}

	public void setSourcePathResources(Map<String,File> sourcePathResources) {
		this.sourcePathResources = sourcePathResources;
	}

	public void setSourceDir(String srcDirName) {
		this.srcDirName = srcDirName;
	}

	public int getConfigurationChanges() {
		return ICompilerConfiguration.EVERYTHING;
	}

	public List<String> getClasspathElementsWithModifiedContents() {
		return null;
	}

	public String getProjectEncoding() {
		return null;
	}

	public String getProcessor() {
		return null;
	}

	public String getProcessorPath() {
		return null;
	}

	@Override
	public String getModulepath() {
		return null;
	}

	@Override
	public String getModuleSourcepath() {
		return null;
	}

}
