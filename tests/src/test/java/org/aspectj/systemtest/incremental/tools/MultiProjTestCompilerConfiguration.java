/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.util.LangUtil;

/**
 * ICompilerConfiguration which mirrors the way AJDT behaves. Always returns that its 1.5 compliant and enables the setting of all
 * options except output jar.
 */
public class MultiProjTestCompilerConfiguration implements ICompilerConfiguration {

	private boolean verbose = false;

	private String classPath = "";
	private Set<File> aspectPath = null;
	private Map<String,File> sourcePathResources = null;
	private IOutputLocationManager outputLocationManager = null;
	private List<String> dependants;
	private Map<String,String> javaOptionsMap;
	private Set<File> inpath;
	private String encoding = null;
	private String outjar;
	private String processor;
	private String processorPath;
	private String nonstandardoptions;
	private List<File> modifiedFiles;
	private List<String> modifiedDirs;
	private List<String> projectSourceFiles = new ArrayList<>();
	private List<String> xmlConfigFiles = new ArrayList<>();
	private String projectPath;

	int changed;

	public MultiProjTestCompilerConfiguration(String projectPath) {
		this.projectPath = projectPath;
	}

	public Set<File> getAspectPath() {
		log("ICompilerConfiguration.getAspectPath(" + aspectPath + ")");
		return aspectPath;
	}

	public String getClasspath() {
		log("ICompilerConfiguration.getClasspath()");
		// AJDT has all the output directories on it's classpath
		StringBuffer sb = new StringBuffer();
		List<File> allOutputPaths = getOutputLocationManager().getAllOutputLocations();
		for (File dir: allOutputPaths) {
			sb.append(File.pathSeparator + dir.getAbsolutePath());
		}
		String cp = sb.toString() + File.pathSeparator + new File(AjdeInteractionTestbed.testdataSrcDir) + File.pathSeparator
				+ System.getProperty("sun.boot.class.path") + File.pathSeparator + "../runtime/target/classes" + File.pathSeparator
				+ this.classPath + File.pathSeparator + System.getProperty("aspectjrt.path") + File.pathSeparator
				+ "../lib/junit/junit.jar" + "c:/batik/batik-1.6/lib/batik-util.jar;"
				+ "c:/batik/batik-1.6/lib/batik-awt-util.jar;" + "c:/batik/batik-1.6/lib/batik-dom.jar;"
				+ "c:/batik/batik-1.6/lib/batik-svggen.jar;" + File.pathSeparator + ".." + File.separator + "lib" + File.separator
				+ "test" + File.separator + "aspectjrt.jar";
		verifyClasspath(cp);
		if (LangUtil.is19VMOrGreater()) {
			cp = LangUtil.getJrtFsFilePath() + File.pathSeparator + cp;
		}

		// look at dependant projects
		if (dependants != null) {
			for (String dependant: dependants) {
				cp = AjdeInteractionTestbed.getFile(dependant, "bin") + File.pathSeparator + cp;
			}
		}
		// System.err.println("For project "+projectPath+" getClasspath() returning "+cp);
		return cp;
	}

	public static void verifyClasspath(String cp) {
		StringTokenizer st = new StringTokenizer(cp,File.pathSeparator);
		while (st.hasMoreElements()) {
			String cpElement = st.nextToken();
//			System.out.println("Checking: "+cpElement+" exists? "+new File(cpElement).exists());
		}
	}

	public Set<File> getInpath() {
		log("ICompilerConfiguration.getInPath()");
		return inpath;
	}

	public Map<String, String> getJavaOptionsMap() {
		log("ICompilerConfiguration.getJavaOptionsMap()");
		if (javaOptionsMap != null && !javaOptionsMap.isEmpty())
			return javaOptionsMap;

		Map<String, String> ht = new Hashtable<>();
		ht.put("org.eclipse.jdt.core.compiler.compliance", "1.5");
		ht.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5");
		ht.put("org.eclipse.jdt.core.compiler.source", "1.5");
		return ht;
	}

	public String getNonStandardOptions() {
		log("ICompilerConfiguration.getNonStandardOptions( " + nonstandardoptions + ")");
		return nonstandardoptions;
	}

	public String getOutJar() {
		log("ICompilerConfiguration.getOutJar()");
		return null;
	}

	public IOutputLocationManager getOutputLocationManager() {
		log("ICompilerConfiguration.getOutputLocationManager()");
		if (outputLocationManager == null) {
			outputLocationManager = new MultiProjTestOutputLocationManager(projectPath);
		}
		return outputLocationManager;
	}

	public List<String> getProjectSourceFiles() {
		log("ICompilerConfiguration.getProjectSourceFiles()");
		return projectSourceFiles;
	}

	public List<String> getProjectXmlConfigFiles() {
		return xmlConfigFiles;
	}

	public List<File> getProjectSourceFilesChanged() {
		log("ICompilerConfiguration.getProjectSourceFilesChanged()");
		return modifiedFiles;
	}

	public Map<String,File> getSourcePathResources() {
		log("ICompilerConfiguration.getSourcePathResources()");
		return sourcePathResources;
	}

	public void log(String s) {
		if (verbose)
			System.out.println(s);
	}

	public void addDependancy(String projectItDependsOn) {
		if (dependants == null) {
			dependants = new ArrayList<>();
		}
		dependants.add(projectItDependsOn);
	}

	// -------------------- setter methods useful for testing ---------------
	public void setAspectPath(Set<File> aspectPath) {
		this.aspectPath = aspectPath;
		this.changed |= ICompilerConfiguration.ASPECTPATH_CHANGED;
	}

	public void setInpath(Set<File> inpath) {
		this.inpath = inpath;
		this.changed |= ICompilerConfiguration.INPATH_CHANGED;
	}

	public void setOutjar(String outjar) {
		this.outjar = outjar;
		this.changed |= ICompilerConfiguration.OUTJAR_CHANGED;
	}
	
	public void setProcessor(String processor) {
		this.processor = processor;
		this.changed |= ICompilerConfiguration.PROCESSOR_CHANGED;
	}

	public void setProcessorPath(String processorPath) {
		this.processorPath = processorPath;
		this.changed |= ICompilerConfiguration.PROCESSOR_CHANGED;
	}

	public void setJavaOptions(Map<String,String> javaOptions) {
		this.javaOptionsMap = javaOptions;
		this.changed |= ICompilerConfiguration.JAVAOPTIONS_CHANGED;
	}

	public void setNonStandardOptions(String options) {
		this.nonstandardoptions = options;
		this.changed |= ICompilerConfiguration.NONSTANDARDOPTIONS_CHANGED;
	}

	public void setProjectSourceFiles(List<String> projectSourceFiles) {
		this.projectSourceFiles = projectSourceFiles;
		this.changed |= ICompilerConfiguration.PROJECTSOURCEFILES_CHANGED;
	}

	public void setProjectXmlConfigFiles(List<String> xmlConfigFiles) {
		this.xmlConfigFiles = xmlConfigFiles;
		this.changed |= ICompilerConfiguration.XMLCONFIG_CHANGED;
	}

	public void addProjectSourceFileChanged(File f) {
		if (this.modifiedFiles == null) {
			this.modifiedFiles = new ArrayList<>();
		}
		if (f != null) {
			modifiedFiles.add(f);
		}
	}

	public void addClasspathEntryChanged(String f) {
		if (this.modifiedDirs == null) {
			this.modifiedDirs = new ArrayList<>();
		}
		if (f != null) {
			modifiedDirs.add(f);
		}
	}

	public void setSourcePathResources(Map<String,File> sourcePathResources) {
		this.sourcePathResources = sourcePathResources;
		this.changed |= ICompilerConfiguration.PROJECTSOURCERESOURCES_CHANGED;
	}

	public void setOutputLocationManager(IOutputLocationManager manager) {
		this.outputLocationManager = manager;
	}

	public void setClasspath(String path) {
		this.classPath = path;
		this.changed |= ICompilerConfiguration.CLASSPATH_CHANGED;
	}

	public int getConfigurationChanges() {
		return changed;
		// return EVERYTHING;
	}

	public void configurationRead() {
		changed = NO_CHANGES;
		modifiedFiles = null;
		modifiedDirs = null;
	}

	public List<String> getClasspathElementsWithModifiedContents() {
		return modifiedDirs;
	}

	public void setProjectEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getProjectEncoding() {
		return this.encoding;
	}

	public String getProcessor() {
		return this.processor;
	}

	public String getProcessorPath() {
		return this.processorPath;
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
