/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
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
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.CompilationResultDestinationManager;
import org.aspectj.util.FileUtil;

/**
 * All configuration information needed to run the AspectJ compiler.
 * Compiler options (as opposed to path information) are held in an AjCompilerOptions instance
 */
public class AjBuildConfig {
	
	private boolean shouldProceed = true;
	
	public static final String AJLINT_IGNORE = "ignore";
	public static final String AJLINT_WARN = "warn";
	public static final String AJLINT_ERROR = "error";
	public static final String AJLINT_DEFAULT = "default";
	
	private File outputDir;
	private File outputJar;
	private String outxmlName;
	private CompilationResultDestinationManager compilationResultDestinationManager = null;
	private List/*File*/ sourceRoots = new ArrayList();
	private List/*File*/ files = new ArrayList();
	private List /*File*/ binaryFiles = new ArrayList();  // .class files in indirs...
	private List/*File*/ inJars = new ArrayList();
	private List/*File*/ inPath = new ArrayList();
	private Map/*String->File*/ sourcePathResources = new HashMap();
	private List/*File*/ aspectpath = new ArrayList();
	private List/*String*/ classpath = new ArrayList();
	private List/*String*/ bootclasspath = new ArrayList();
	
	private File configFile;
	private String lintMode = AJLINT_DEFAULT;
	private File lintSpecFile = null;
	
	private AjCompilerOptions options;
    
    /** if true, then global values override local when joining */
    private boolean override = true;

    // incremental variants handled by the compiler client, but parsed here
    private boolean incrementalMode;
    private File incrementalFile;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BuildConfig["+(configFile==null?"null":configFile.getAbsoluteFile().toString())+"] #Files="+files.size());
		return sb.toString();
	}
    
	public static class BinarySourceFile {
		public BinarySourceFile(File dir, File src) {
			this.fromInPathDirectory = dir;
			this.binSrc = src;
		}
		public File fromInPathDirectory;
		public File binSrc;
		
		public boolean equals(Object obj) {
			if ((obj instanceof BinarySourceFile) &&
				(obj != null)) {
				BinarySourceFile other = (BinarySourceFile)obj;
				return(binSrc.equals(other.binSrc));
			}
			return false;
		}
		public int hashCode() {
			return binSrc != null ? binSrc.hashCode() : 0; 
		}
	}
    
	/**
	 * Intialises the javaOptions Map to hold the default 
	 * JDT Compiler settings. Added by AMC 01.20.2003 in reponse
	 * to bug #29768 and enh. 29769.
	 * The settings here are duplicated from those set in
	 * org.eclipse.jdt.internal.compiler.batch.Main, but I've elected to
	 * copy them rather than refactor the JDT class since this keeps
	 * integration with future JDT releases easier (?).
	 */
	public AjBuildConfig( ) {
		options = new AjCompilerOptions();
	}

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

	/**
	 * returned files includes all .class files found in
	 * a directory on the inpath, but does not include
	 * .class files contained within jars.
	 */
	public List/*BinarySourceFile*/ getBinaryFiles() {
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
	
	public void setFiles(List files) {
		this.files = files;
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
	public List getClasspath() { // XXX setters don't respect javadoc contract...
		return classpath;
	}

	public void setClasspath(List classpath) {
		this.classpath = classpath;
	}
	
	public List getBootclasspath() {
		return bootclasspath;
	}
	
	public void setBootclasspath(List bootclasspath) {
		this.bootclasspath = bootclasspath;
	}

	public File getOutputJar() {
		return outputJar;
	}

	public String getOutxmlName() {
		return outxmlName;
	}
	
	public List/*File*/ getInpath() {
		// Elements of the list are either archives (jars/zips) or directories
		return inPath;
	}

	public List/*File*/ getInJars() {
		return inJars;
	}

	public Map getSourcePathResources() {
		return sourcePathResources;
	}

	public void setOutputJar(File outputJar) {
		this.outputJar = outputJar;
	}

	public void setOutxmlName(String name) {
		this.outxmlName = name;
	}

	public void setInJars(List sourceJars) {
		this.inJars = sourceJars;
	}
	
	public void setInPath(List dirsOrJars) {
		inPath = dirsOrJars;
		
		// remember all the class files in directories on the inpath
		binaryFiles = new ArrayList();
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getPath().endsWith(".class");
			}};
		for (Iterator iter = dirsOrJars.iterator(); iter.hasNext();) {
			File inpathElement = (File) iter.next();
			if (inpathElement.isDirectory()) {
			    File[] files = FileUtil.listFiles(inpathElement, filter);
				for (int i = 0; i < files.length; i++) {
					binaryFiles.add(new BinarySourceFile(inpathElement,files[i]));
				}
			}			
		}
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
     * @return List (String) classpath of bootclasspath, injars, inpath, aspectpath 
     *   entries, specified classpath (extdirs, and classpath), and output dir or jar
     */
    public List getFullClasspath() {
        List full = new ArrayList();
        full.addAll(getBootclasspath()); // XXX Is it OK that boot classpath overrides inpath/injars/aspectpath?
        for (Iterator i = inJars.iterator(); i.hasNext(); ) {
            full.add(((File)i.next()).getAbsolutePath());
        }
        for (Iterator i = inPath.iterator();i.hasNext();) {
        	full.add(((File)i.next()).getAbsolutePath());
        }
        for (Iterator i = aspectpath.iterator(); i.hasNext(); ) {
            full.add(((File)i.next()).getAbsolutePath());        
        }
        full.addAll(getClasspath());
//        if (null != outputDir) {
//            full.add(outputDir.getAbsolutePath());
//        } else if (null != outputJar) {
//            full.add(outputJar.getAbsolutePath());
//        }
        return full;
    }
    
	public File getLintSpecFile() {
		return lintSpecFile;
	}

	public void setLintSpecFile(File lintSpecFile) {
		this.lintSpecFile = lintSpecFile;
	}

	public List getAspectpath() {
		return aspectpath;
	}

	public void setAspectpath(List aspectpath) {
		this.aspectpath = aspectpath;
	}

    /** @return true if any config file, sourceroots, sourcefiles, injars or inpath */
    public boolean hasSources() {
        return ((null != configFile)
            || (0 < sourceRoots.size())
            || (0 < files.size())
            || (0 < inJars.size())
            || (0 < inPath.size())
            );
    }
    
//    /** @return null if no errors, String errors otherwise */
//    public String configErrors() {
//        StringBuffer result = new StringBuffer();
//        // ok, permit both.  sigh.
////        if ((null != outputDir) && (null != outputJar)) {
////            result.append("specified both outputDir and outputJar");
////        }
//        // incremental => only sourceroots
//        // 
//        return (0 == result.length() ? null : result.toString());
//    }

    /**
     * Install global values into local config
     * unless values conflict:
     * <ul>
     * <li>Collections are unioned</li>
     * <li>values takes local value unless default and global set</li>
     * <li>this only sets one of outputDir and outputJar as needed</li>
     * <ul>
     * This also configures super if javaOptions change.
     * @param global the AjBuildConfig to read globals from
     */
    public void installGlobals(AjBuildConfig global) { // XXX relies on default values
    	// don't join the options - they already have defaults taken care of.
//        Map optionsMap = options.getMap();
//        join(optionsMap,global.getOptions().getMap());
//        options.set(optionsMap);
        join(aspectpath, global.aspectpath);
        join(classpath, global.classpath);
        if (null == configFile) {
            configFile = global.configFile; // XXX correct?
        }
        if (!isEmacsSymMode() && global.isEmacsSymMode()) {
            setEmacsSymMode(true);
        }
        join(files, global.files);
        if (!isGenerateModelMode() && global.isGenerateModelMode()) {
            setGenerateModelMode(true);
        }
        if (null == incrementalFile) {
            incrementalFile = global.incrementalFile;
        }
        if (!incrementalMode && global.incrementalMode) {
            incrementalMode = true;
        }
        join(inJars, global.inJars);
        join(inPath, global.inPath);
        if ((null == lintMode) 
            || (AJLINT_DEFAULT.equals(lintMode))) {
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
    }

    void join(Collection local, Collection global) {
        for (Iterator iter = global.iterator(); iter.hasNext();) {
            Object next = iter.next();
            if (!local.contains(next)) {
                local.add(next);        
            }
        }
    }
    void join(Map local, Map global) {
        for (Iterator iter = global.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            if (override || (null == local.get(key))) { // 
                Object value = global.get(key);
                if (null != value) {
                    local.put(key, value);
                }
            }
        }
    }

	public void setSourcePathResources(Map map) {
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
	
	// options...

	public void setLintMode(String lintMode) {
		this.lintMode = lintMode;
		String lintValue = null;
		if (AJLINT_IGNORE.equals(lintMode)) {
			lintValue = AjCompilerOptions.IGNORE;
		} else if (AJLINT_WARN.equals(lintMode)) {
			lintValue = AjCompilerOptions.WARNING;
		} else if (AJLINT_ERROR.equals(lintMode)) {
			lintValue = AjCompilerOptions.ERROR;
		}
		
		if (lintValue != null) {
			Map lintOptions = new HashMap();
			lintOptions.put(AjCompilerOptions.OPTION_ReportInvalidAbsoluteTypeName,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportInvalidWildcardTypeName,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportUnresolvableMember,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportTypeNotExposedToWeaver,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportShadowNotInStructure,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportUnmatchedSuperTypeInCall,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportCannotImplementLazyTJP,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportNeedSerialVersionUIDField,lintValue);
			lintOptions.put(AjCompilerOptions.OPTION_ReportIncompatibleSerialVersion,lintValue);
			options.set(lintOptions);
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
		

	public boolean isXNotReweavable() {
		return options.xNotReweavable;
	}

	public boolean isGenerateJavadocsInModelMode() {
		return options.generateJavaDocsInModel;
	}
	
	public void setGenerateJavadocsInModelMode(
			boolean generateJavadocsInModelMode) {
		options.generateJavaDocsInModel = generateJavadocsInModelMode;
	}

	public boolean isGenerateCrossRefsMode() {
		return options.generateCrossRefs;
	}

	public void setGenerateCrossRefsMode(boolean on) {
		options.generateCrossRefs = on;
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
	
	public void setTargetAspectjRuntimeLevel(String level) {
		options.targetAspectjRuntimeLevel = level;
	}

	public String getTargetAspectjRuntimeLevel() {
		return options.targetAspectjRuntimeLevel;
	}
}
