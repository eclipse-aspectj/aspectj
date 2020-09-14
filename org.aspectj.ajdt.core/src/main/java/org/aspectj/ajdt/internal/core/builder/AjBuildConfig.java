/* *******************************************************************
 * Copyright (c) 2002 - 2018 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Adrian Colyer  added constructor to populate javaOptions with
 * 					  default settings - 01.20.2003
 * 					  Bugzilla #29768, 29769
 *      Andy Clement 
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.ajdt.ajc.BuildArgParser;
import org.aspectj.ajdt.internal.compiler.CompilationResultDestinationManager;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.aspectj.org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IModule;
import org.aspectj.util.FileUtil;

/**
 * All configuration information needed to run the AspectJ compiler. Compiler options (as opposed to path information) are held in
 * an AjCompilerOptions instance
 */
public class AjBuildConfig implements CompilerConfigurationChangeFlags {
	
	public static final Classpath[] NO_CHECKED_CLASSPATHS = new Classpath[0];
	
	private boolean shouldProceed = true;

	public static final String AJLINT_IGNORE = "ignore";
	public static final String AJLINT_WARN = "warn";
	public static final String AJLINT_ERROR = "error";
	public static final String AJLINT_DEFAULT = "default";

	private File outputDir;
	private File outputJar;
	private String outxmlName;
	private CompilationResultDestinationManager compilationResultDestinationManager = null;
	private List<File> sourceRoots = new ArrayList<>();
	private List<File> changedFiles;
	private List<File> files = new ArrayList<>();
	private List<File> xmlfiles = new ArrayList<>();
	private String processor;
	private String processorPath;
	private List<BinarySourceFile> binaryFiles = new ArrayList<>(); // .class files in indirs...
	private List<File> inJars = new ArrayList<>();
	private List<File> inPath = new ArrayList<>();
	private Map<String, File> sourcePathResources = new HashMap<>();
	private List<File> aspectpath = new ArrayList<>();
	private List<String> classpath = new ArrayList<>();
	private List<String> modulepath = new ArrayList<>();
	// Expensive to compute (searching modules, parsing module-info)
	private Collection<Classpath> modulepathClasspathEntries = null;
	private List<String> modulesourcepath = new ArrayList<>();
	// Expensive to compute (searching modules, parsing module-info)
	private Collection<Classpath> modulesourcepathClasspathEntries = null;
	private Classpath[] checkedClasspaths = null;
	private List<String> bootclasspath = new ArrayList<>();
	private List<String> cpElementsWithModifiedContents = new ArrayList<>();
	private IModule moduleDesc;

	private File configFile;
	private String lintMode = AJLINT_DEFAULT;
	private Map<String,String> lintOptionsMap = null;
	private File lintSpecFile = null;

	private int changes = EVERYTHING; // bitflags, see CompilerConfigurationChangeFlags

	private final AjCompilerOptions options;

	private final BuildArgParser buildArgParser;

	// incremental variants handled by the compiler client, but parsed here
	private boolean incrementalMode;
	private File incrementalFile;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BuildConfig[" + (configFile == null ? "null" : configFile.getAbsoluteFile().toString()) + "] #Files="
				+ files.size() + " AopXmls=#" + xmlfiles.size());
		return sb.toString();
	}

	public static class BinarySourceFile {
		public BinarySourceFile(File dir, File src) {
			this.fromInPathDirectory = dir;
			this.binSrc = src;
		}

		public File fromInPathDirectory;
		public File binSrc;

		@Override
		public boolean equals(Object obj) {
			if (obj != null && (obj instanceof BinarySourceFile)) {
				BinarySourceFile other = (BinarySourceFile) obj;
				return (binSrc.equals(other.binSrc));
			}
			return false;
		}

		@Override
		public int hashCode() {
			return binSrc != null ? binSrc.hashCode() : 0;
		}
	}

	/**
	 * Intialises the javaOptions Map to hold the default JDT Compiler settings. Added by AMC 01.20.2003 in reponse to bug #29768
	 * and enh. 29769. The settings here are duplicated from those set in org.eclipse.jdt.internal.compiler.batch.Main, but I've
	 * elected to copy them rather than refactor the JDT class since this keeps integration with future JDT releases easier (?).
	 */
	public AjBuildConfig(BuildArgParser buildArgParser) {
		this.buildArgParser = buildArgParser;
		options = new AjCompilerOptions();
	}

	public BuildArgParser getBuildArgParser() {
		return buildArgParser;
	}

	/**
	 * returned files includes
	 * <ul>
	 * <li>files explicitly listed on command-line</li>
	 * <li>files listed by reference in argument list files</li>
	 * <li>files contained in sourceRootDir if that exists</li>
	 * </ul>
	 *
	 * @return all source files that should be compiled.
	 */
	public List<File> getFiles() {
		return files;
	}

	public List<File> getXmlFiles() {
		return xmlfiles;
	}
	
	public void setProcessor(String processor) {
		this.processor = processor;
	}
	
	/**
	 * @return the list of processor classes to execute
	 */
	public String getProcessor() {
		return this.processor;
	}
	
	public void setProcessorPath(String processorPath) {
		this.processorPath = processorPath;
	}
	
	/**
	 * @return the processor path which can be searched for processors (via META-INF/services)
	 */
	public String getProcessorPath() {
		return this.processorPath;
	}

	/**
	 * returned files includes all .class files found in a directory on the inpath, but does not include .class files contained
	 * within jars.
	 */
	public List<BinarySourceFile> getBinaryFiles() {
		return binaryFiles;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public CompilationResultDestinationManager getCompilationResultDestinationManager() {
		return this.compilationResultDestinationManager;
	}

	public void setCompilationResultDestinationManager(CompilationResultDestinationManager mgr) {
		this.compilationResultDestinationManager = mgr;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public void setXmlFiles(List<File> xmlfiles) {
		this.xmlfiles = xmlfiles;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public AjCompilerOptions getOptions() {
		return options;
	}

	/**
	 * This does not include -bootclasspath but includes -extdirs and -classpath
	 */
	public List<String> getClasspath() {
		return classpath;
	}

	public void setClasspath(List<String> classpath) {
		this.classpath = classpath;
		checkedClasspaths = null;
	}

	public List<String> getModulepath() {
		return modulepath;
	}
	
	public List<String> getModulesourcepath() {
		return modulesourcepath;
	}

	public void setModulepath(List<String> modulepath) {
		this.modulepath = modulepath;
		checkedClasspaths = null;
	}

	public void setModulesourcepath(List<String> modulesourcepath) {
		this.modulesourcepath = modulesourcepath;
		checkedClasspaths = null;
	}

	public void setCheckedClasspaths(Classpath[] checkedClasspaths) {
		this.checkedClasspaths = checkedClasspaths;	
		checkedClasspaths = null;
	}
	
	private List<Classpath> processFilePath(List<File> path, java.lang.String encoding) {
		List<Classpath> entries = new ArrayList<>();
		for (File file: path) {
			entries.add(FileSystem.getClasspath(file.getAbsolutePath(), encoding, null, ClasspathLocation.BINARY, null));
		}
		return entries;
	}

	private List<Classpath> processStringPath(List<String> path, java.lang.String encoding) {
		List<Classpath> entries = new ArrayList<>();
		for (String file: path) {
			entries.add(FileSystem.getClasspath(file, encoding, null, ClasspathLocation.BINARY, null));
		}
		return entries;
	}

	public Classpath[] getCheckedClasspaths() {
		if (checkedClasspaths == null) {
			computeCheckedClasspath();
		}
		return checkedClasspaths;
	}

	public List<String> getBootclasspath() {
		return bootclasspath;
	}

	public void setBootclasspath(List<String> bootclasspath) {
		this.bootclasspath = bootclasspath;
	}

	public File getOutputJar() {
		return outputJar;
	}

	public String getOutxmlName() {
		return outxmlName;
	}

	public List<File> getInpath() {
		// Elements of the list are either archives (jars/zips) or directories
		return Collections.unmodifiableList(inPath);
	}

	public List<File> getInJars() {
		return inJars;
	}

	public Map<String, File> getSourcePathResources() {
		return sourcePathResources;
	}

	public void setOutputJar(File outputJar) {
		this.outputJar = outputJar;
	}

	public void setOutxmlName(String name) {
		this.outxmlName = name;
	}

	public void setInJars(List<File> sourceJars) {
		this.inJars = sourceJars;
		checkedClasspaths = null;
	}

	public void processInPath() {
		// remember all the class files in directories on the inpath
		binaryFiles = new ArrayList<>();
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getPath().endsWith(".class");
			}
		};
		for (File inpathElement : inPath) {
			if (inpathElement.isDirectory()) {
				File[] files = FileUtil.listFiles(inpathElement, filter);
				for (File file : files) {
					binaryFiles.add(new BinarySourceFile(inpathElement, file));
				}
			}
		}
	}

	public void setInPath(List<File> dirsOrJars) {
		inPath = dirsOrJars;
		checkedClasspaths = null;
		processInPath();
	}

	public List<File> getSourceRoots() {
		return sourceRoots;
	}

	public void setSourceRoots(List<File> sourceRootDir) {
		this.sourceRoots = sourceRootDir;
	}

	public File getConfigFile() {
		return configFile;
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}

	public void setIncrementalMode(boolean incrementalMode) {
		this.incrementalMode = incrementalMode;
	}

	public boolean isIncrementalMode() {
		return incrementalMode;
	}

	public void setIncrementalFile(File incrementalFile) {
		this.incrementalFile = incrementalFile;
	}

	public boolean isIncrementalFileMode() {
		return (null != incrementalFile);
	}

	/**
	 * @return List (String) classpath of bootclasspath, injars, inpath, aspectpath entries, specified classpath (extdirs, and
	 *         classpath), and output dir or jar
	 */
	public List<String> getFullClasspath() {
		List<String> full = new ArrayList<>(getBootclasspath()); // XXX Is it OK that boot classpath overrides inpath/injars/aspectpath?
		for (File file: inJars) {
			full.add(file.getAbsolutePath());
		}
		for (File file: inPath) {
			full.add(file.getAbsolutePath());
		}
		for (File file: aspectpath) {
			full.add(file.getAbsolutePath());
		}
		full.addAll(getClasspath());
//		full.addAll(getModulepath());
//		full.addAll(getModulesourcepath());
		// if (null != outputDir) {
		// full.add(outputDir.getAbsolutePath());
		// } else if (null != outputJar) {
		// full.add(outputJar.getAbsolutePath());
		// }
		return full;
	}

	public File getLintSpecFile() {
		return lintSpecFile;
	}

	public void setLintSpecFile(File lintSpecFile) {
		this.lintSpecFile = lintSpecFile;
	}

	public List<File> getAspectpath() {
		return Collections.unmodifiableList(aspectpath);
	}

	public void setAspectpath(List<File> aspectpath) {
		this.aspectpath = aspectpath;
		checkedClasspaths = null;
	}
	
	public void addToAspectpath(File file) {
		this.aspectpath.add(file);
		checkedClasspaths = null;
	}

	public void addToInjars(File file) {
		this.inJars.add(file);
		checkedClasspaths = null;
	}

	/** @return true if any config file, sourceroots, sourcefiles, injars or inpath */
	public boolean hasSources() {
		return ((null != configFile) || (0 < sourceRoots.size()) || (0 < files.size()) || (0 < inJars.size()) || (0 < inPath.size()));
	}

	// /** @return null if no errors, String errors otherwise */
	// public String configErrors() {
	// StringBuffer result = new StringBuffer();
	// // ok, permit both. sigh.
	// // if ((null != outputDir) && (null != outputJar)) {
	// // result.append("specified both outputDir and outputJar");
	// // }
	// // incremental => only sourceroots
	// //
	// return (0 == result.length() ? null : result.toString());
	// }

	/**
	 * Install global values into local config unless values conflict:
	 * <ul>
	 * <li>Collections are unioned</li>
	 * <li>values takes local value unless default and global set</li>
	 * <li>this only sets one of outputDir and outputJar as needed</li>
	 * </ul>
	 * <p>This also configures super if javaOptions change.</p>
	 *
	 * @param global the AjBuildConfig to read globals from
	 */
	public void installGlobals(AjBuildConfig global) { // XXX relies on default values
		// don't join the options - they already have defaults taken care of.
		// Map optionsMap = options.getMap();
		// join(optionsMap,global.getOptions().getMap());
		// options.set(optionsMap);
		options.defaultEncoding = global.options.defaultEncoding;// pr244321
		join(aspectpath, global.aspectpath);
		join(classpath, global.classpath);
		if (null == configFile) {
			configFile = global.configFile; // XXX correct?
		}
		if (!isEmacsSymMode() && global.isEmacsSymMode()) {
			setEmacsSymMode(true);
		}
		join(files, global.files);
		join(xmlfiles, global.xmlfiles);
		if (!isGenerateModelMode() && global.isGenerateModelMode()) {
			setGenerateModelMode(true);
		}
		if (null == incrementalFile) {
			incrementalFile = global.incrementalFile;
		}
		if (!incrementalMode && global.incrementalMode) {
			incrementalMode = true;
		}

		if (isCheckRuntimeVersion() && !global.isCheckRuntimeVersion()) {
			setCheckRuntimeVersion(false);
		}

		join(inJars, global.inJars);
		join(inPath, global.inPath);
		if ((null == lintMode) || (AJLINT_DEFAULT.equals(lintMode))) {
			setLintMode(global.lintMode);
		}
		if (null == lintSpecFile) {
			lintSpecFile = global.lintSpecFile;
		}
		if (!isTerminateAfterCompilation() && global.isTerminateAfterCompilation()) {
			setTerminateAfterCompilation(true);
		}
		if ((null == outputDir) && (null == outputJar)) {
			if (null != global.outputDir) {
				outputDir = global.outputDir;
			}
			if (null != global.outputJar) {
				outputJar = global.outputJar;
			}
		}
		join(sourceRoots, global.sourceRoots);
		if (!isXnoInline() && global.isXnoInline()) {
			setXnoInline(true);
		}
		if (!isXserializableAspects() && global.isXserializableAspects()) {
			setXserializableAspects(true);
		}
		if (!isXlazyTjp() && global.isXlazyTjp()) {
			setXlazyTjp(true);
		}
		if (!getProceedOnError() && global.getProceedOnError()) {
			setProceedOnError(true);
		}
		setTargetAspectjRuntimeLevel(global.getTargetAspectjRuntimeLevel());
		setXJoinpoints(global.getXJoinpoints());
		if (!isXHasMemberEnabled() && global.isXHasMemberEnabled()) {
			setXHasMemberSupport(true);
		}
		if (!isXNotReweavable() && global.isXNotReweavable()) {
			setXnotReweavable(true);
		}
		setOutxmlName(global.getOutxmlName());
		setXconfigurationInfo(global.getXconfigurationInfo());
		setAddSerialVerUID(global.isAddSerialVerUID());
		if (!isXmlConfigured() && global.isXmlConfigured()) {
			setXmlConfigured(global.isXmlConfigured());
		}
		setTiming(global.isTiming());
		setMakeReflectable(global.isMakeReflectable());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void join(Collection local, Collection global) {
		for (Object next : global) {
			if (!local.contains(next)) {
				local.add(next);
			}
		}
	}

	public void setSourcePathResources(Map<String, File> map) {
		sourcePathResources = map;
	}

	/**
	 * used to indicate whether to proceed after parsing config
	 */
	public boolean shouldProceed() {
		return shouldProceed;
	}

	public void doNotProceed() {
		shouldProceed = false;
	}

	public String getLintMode() {
		return lintMode;
	}

	public Map<String,String> getLintOptionsMap() {
		return lintOptionsMap;
	}

	// options...

	public void setLintMode(String lintMode) {
		String lintValue = null;
		this.lintMode = lintMode;
		if (AJLINT_IGNORE.equals(lintMode)) {
			lintValue = AjCompilerOptions.IGNORE;
		} else if (AJLINT_WARN.equals(lintMode)) {
			lintValue = AjCompilerOptions.WARNING;
		} else if (AJLINT_ERROR.equals(lintMode)) {
			lintValue = AjCompilerOptions.ERROR;
		} else {
			// Possibly a name=value comma separated list of configurations
			if (lintMode.contains("=")) {
				this.lintMode = AJLINT_DEFAULT;
				lintOptionsMap = new HashMap<>();
				StringTokenizer tokenizer = new StringTokenizer(lintMode,",");
				while (tokenizer.hasMoreElements()) {
					String option = tokenizer.nextToken();
					int equals = option.indexOf("=");
					if (equals!=-1) {
						String key = option.substring(0,equals);
						String value = option.substring(equals+1);
						lintOptionsMap.put(key,value);
					}
				}
			}
		}

		if (lintValue != null || lintOptionsMap != null ) {
			Map<String, String> lintOptions = new HashMap<>();
			setOption(AjCompilerOptions.OPTION_ReportInvalidAbsoluteTypeName, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportInvalidWildcardTypeName, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportUnresolvableMember, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportTypeNotExposedToWeaver, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportShadowNotInStructure, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportUnmatchedSuperTypeInCall, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportCannotImplementLazyTJP, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportNeedSerialVersionUIDField, lintValue, lintOptions);
			setOption(AjCompilerOptions.OPTION_ReportIncompatibleSerialVersion, lintValue, lintOptions);
			options.set(lintOptions);
		}
	}

	private void setOption(String optionKey, String lintValue, Map<String,String> lintOptionsAccumulator) {
		if (lintOptionsMap!=null && lintOptionsMap.containsKey(optionKey)) {
			String v = lintOptionsMap.get(lintOptionsMap);
			if (AJLINT_IGNORE.equals(v)) {
				lintValue = AjCompilerOptions.IGNORE;
			} else if (AJLINT_WARN.equals(v)) {
				lintValue = AjCompilerOptions.WARNING;
			} else if (AJLINT_ERROR.equals(v)) {
				lintValue = AjCompilerOptions.ERROR;
			}
		}
		if (lintValue != null) {
			lintOptionsAccumulator.put(optionKey,lintValue);
		}
	}

	public boolean isTerminateAfterCompilation() {
		return options.terminateAfterCompilation;
	}

	public void setTerminateAfterCompilation(boolean b) {
		options.terminateAfterCompilation = b;
	}

	public boolean isXserializableAspects() {
		return options.xSerializableAspects;
	}

	public void setXserializableAspects(boolean xserializableAspects) {
		options.xSerializableAspects = xserializableAspects;
	}

	public void setXJoinpoints(String jps) {
		options.xOptionalJoinpoints = jps;
	}

	public String getXJoinpoints() {
		return options.xOptionalJoinpoints;
	}

	public boolean isXnoInline() {
		return options.xNoInline;
	}

	public void setXnoInline(boolean xnoInline) {
		options.xNoInline = xnoInline;
	}

	public boolean isXlazyTjp() {
		return options.xLazyThisJoinPoint;
	}

	public void setXlazyTjp(boolean b) {
		options.xLazyThisJoinPoint = b;
	}

	public void setXnotReweavable(boolean b) {
		options.xNotReweavable = b;
	}

	public void setXconfigurationInfo(String info) {
		options.xConfigurationInfo = info;
	}

	public String getXconfigurationInfo() {
		return options.xConfigurationInfo;
	}

	public void setXHasMemberSupport(boolean enabled) {
		options.xHasMember = enabled;
	}

	public boolean isXHasMemberEnabled() {
		return options.xHasMember;
	}

	public void setXdevPinpointMode(boolean enabled) {
		options.xdevPinpoint = enabled;
	}

	public boolean isXdevPinpoint() {
		return options.xdevPinpoint;
	}

	public void setAddSerialVerUID(boolean b) {
		options.addSerialVerUID = b;
	}

	public boolean isAddSerialVerUID() {
		return options.addSerialVerUID;
	}

	public void setXmlConfigured(boolean b) {
		options.xmlConfigured = b;
	}

	public void setMakeReflectable(boolean b) {
		options.makeReflectable = b;
	}

	public boolean isXmlConfigured() {
		return options.xmlConfigured;
	}

	public boolean isMakeReflectable() {
		return options.makeReflectable;
	}

	public boolean isXNotReweavable() {
		return options.xNotReweavable;
	}

	public boolean isGenerateJavadocsInModelMode() {
		return options.generateJavaDocsInModel;
	}

	public void setGenerateJavadocsInModelMode(boolean generateJavadocsInModelMode) {
		options.generateJavaDocsInModel = generateJavadocsInModelMode;
	}

	public boolean isGenerateCrossRefsMode() {
		return options.generateCrossRefs;
	}

	public void setGenerateCrossRefsMode(boolean on) {
		options.generateCrossRefs = on;
	}

	public boolean isCheckRuntimeVersion() {
		return options.checkRuntimeVersion;
	}

	public void setCheckRuntimeVersion(boolean on) {
		options.checkRuntimeVersion = on;
	}

	public boolean isEmacsSymMode() {
		return options.generateEmacsSymFiles;
	}

	public void setEmacsSymMode(boolean emacsSymMode) {
		options.generateEmacsSymFiles = emacsSymMode;
	}

	public boolean isGenerateModelMode() {
		return options.generateModel;
	}

	public void setGenerateModelMode(boolean structureModelMode) {
		options.generateModel = structureModelMode;
	}

	public boolean isNoAtAspectJAnnotationProcessing() {
		return options.noAtAspectJProcessing;
	}

	public void setNoAtAspectJAnnotationProcessing(boolean noProcess) {
		options.noAtAspectJProcessing = noProcess;
	}

	public void setShowWeavingInformation(boolean b) {
		options.showWeavingInformation = true;
	}

	public boolean getShowWeavingInformation() {
		return options.showWeavingInformation;
	}

	public void setProceedOnError(boolean b) {
		options.proceedOnError = b;
	}

	public boolean getProceedOnError() {
		return options.proceedOnError;
	}

	public void setBehaveInJava5Way(boolean b) {
		options.behaveInJava5Way = b;
	}

	public boolean getBehaveInJava5Way() {
		return options.behaveInJava5Way;
	}

	public void setTiming(boolean b) {
		options.timing = b;
	}

	public boolean isTiming() {
		return options.timing;
	}

	public void setTargetAspectjRuntimeLevel(String level) {
		options.targetAspectjRuntimeLevel = level;
	}

	public String getTargetAspectjRuntimeLevel() {
		return options.targetAspectjRuntimeLevel;
	}

	/**
	 * Indicates what has changed in this configuration compared to the last time it was used, allowing the state management logic
	 * to make intelligent optimizations and skip unnecessary work.
	 *
	 * @param changes set of bitflags, see {@link CompilerConfigurationChangeFlags} for flags
	 */
	public void setChanged(int changes) {
		this.changes = changes;
	}

	/**
	 * Return the bit flags indicating what has changed since the last time this config was used.
	 *
	 * @return the bitflags according too {@link CompilerConfigurationChangeFlags}
	 */
	public int getChanged() {
		return this.changes;
	}

	public void setModifiedFiles(List<File> projectSourceFilesChanged) {
		this.changedFiles = projectSourceFilesChanged;
	}

	public List<File> getModifiedFiles() {
		return this.changedFiles;
	}

	public void setClasspathElementsWithModifiedContents(List<String> cpElementsWithModifiedContents) {
		this.cpElementsWithModifiedContents = cpElementsWithModifiedContents;
	}

	public List<String> getClasspathElementsWithModifiedContents() {
		return this.cpElementsWithModifiedContents;
	}

	public void setProjectEncoding(String projectEncoding) {
		options.defaultEncoding = projectEncoding;
	}
	
	public String getProjectEncoding() {
		return options.defaultEncoding;
	}

	public void setModuleDesc(IModule moduleDesc) {
		this.moduleDesc = moduleDesc;
	}

	public IModule getModuleDesc() {
		return moduleDesc;
	}

	public void addToInpath(Set<File> newInpathEntries) {
		if (newInpathEntries != null && newInpathEntries.size() != 0) {
			for (File newInpathEntry: newInpathEntries) {
				if (!inPath.contains(newInpathEntry)) {
					inPath.add(newInpathEntry);
				}
			}
			checkedClasspaths = null;
		}
	}

	public void addToInpath(File newInpathEntry) {
//		if (!inPath.contains(newInpathEntry)) {
			inPath.add(newInpathEntry);
//		}
		checkedClasspaths = null;
	}

	public void addToAspectpath(Set<File> newAspectpathEntries) {
		if (newAspectpathEntries != null && newAspectpathEntries.size() != 0) {
			for (File newAspectpathEntry: newAspectpathEntries) {
				if (!aspectpath.contains(newAspectpathEntry)) {
					aspectpath.add(newAspectpathEntry);
				}
			}
			checkedClasspaths = null;
		}
	}

	public void setModulepathClasspathEntries(ArrayList<Classpath> modulepathClasspathEntries) {
		this.modulepathClasspathEntries = modulepathClasspathEntries;
	}

	public void setModulesourcepathClasspathEntries(ArrayList<Classpath> modulesourcepathClasspathEntries) {
		this.modulesourcepathClasspathEntries = modulesourcepathClasspathEntries;
	}

	public File removeAspectPathEntry(int i) {
		checkedClasspaths = null;
		return aspectpath.remove(i);
	}

	public String removeClasspathEntry(int i) {
		checkedClasspaths = null;
		return classpath.remove(i);
	}

	public File removeInpathEntry(int i) {
		checkedClasspaths = null;
		return inPath.remove(i);
	}

	public File removeInjarsEntry(int i) {
		checkedClasspaths = null;
		return inJars.remove(0);
	}


	// This is similar to the calculation done in Main.setPaths() but it isn't as sophisticated
	// as that one (doesn't need to be) and it also considers the additional paths for an
	// AspectJ project (aspectpath/inpath/injars)
	private void computeCheckedClasspath() {		
		// Follow what we do in getFullClasspath():
		// bootclasspath, injars, inpath, aspectpath, classpath, modulepath

		String encoding = getProjectEncoding();
		// What to do about bootclasspath on java 9?
		
		// ArrayList<Classpath> allPaths = handleBootclasspath(bootclasspaths, customEncoding);
		ArrayList<FileSystem.Classpath> allPaths = new ArrayList<>();
	 	allPaths.addAll(processStringPath(bootclasspath, encoding));
		allPaths.addAll(processFilePath(inJars, encoding));
	 	allPaths.addAll(processFilePath(inPath, encoding)); 
	 	allPaths.addAll(processFilePath(aspectpath, encoding)); 
	 	if (modulepathClasspathEntries != null) {
	 		allPaths.addAll(modulepathClasspathEntries);
	 	}
	 	if (modulesourcepathClasspathEntries != null) {
	 		allPaths.addAll(modulesourcepathClasspathEntries);
	 	}
	 	// The classpath is done after modules to give precedence to modules that share the
	 	// same paths as classpath elements (the upcoming normalize will remove later dups)
	 	allPaths.addAll(processStringPath(classpath, encoding));
	 	for (Iterator<FileSystem.Classpath> iter = allPaths.iterator();iter.hasNext();) {
	 		Classpath next = iter.next();
	 		if (next == null) {
	 			iter.remove();
	 		}
	 	}
		allPaths = FileSystem.ClasspathNormalizer.normalize(allPaths);
		this.checkedClasspaths = new FileSystem.Classpath[allPaths.size()];
		allPaths.toArray(this.checkedClasspaths);
		for (Classpath checkedClasspath : checkedClasspaths) {
			if (checkedClasspath == null) {
				throw new IllegalStateException();
			}
		}
	}
	
}
