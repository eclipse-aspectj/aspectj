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
 *     Adrian Colyer  added constructor to populate javaOptions with
 * 					  default settings - 01.20.2003
 * 					  Bugzilla #29768, 29769
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * All configuration information needed to run the AspectJ compiler.
 */
public class AjBuildConfig { // XXX needs bootclasspath?
	
	public static final String AJLINT_IGNORE = "ignore";
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
    
    /** if true, then global values override local when joining */
    private boolean override = true;

    // incremental variants handled by the compiler client, but parsed here
    private boolean incrementalMode;
    private File incrementalFile;
    
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
		javaOptions.put(
			CompilerOptions.OPTION_LocalVariableAttribute,
			//CompilerOptions.DO_NOT_GENERATE);
			CompilerOptions.GENERATE);
		javaOptions.put(
			CompilerOptions.OPTION_LineNumberAttribute,
			//CompilerOptions.DO_NOT_GENERATE);
			CompilerOptions.GENERATE);
		javaOptions.put(
			CompilerOptions.OPTION_SourceFileAttribute,
			//CompilerOptions.DO_NOT_GENERATE);
			CompilerOptions.GENERATE);
		javaOptions.put(
			CompilerOptions.OPTION_PreserveUnusedLocal,
			CompilerOptions.OPTIMIZE_OUT);
		javaOptions.put(
			CompilerOptions.OPTION_ReportUnreachableCode,
			CompilerOptions.ERROR);
		javaOptions.put(
			CompilerOptions.OPTION_ReportInvalidImport, 
			CompilerOptions.ERROR);
		javaOptions.put(
			CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
			CompilerOptions.WARNING);
		javaOptions.put(
			CompilerOptions.OPTION_ReportMethodWithConstructorName,
			CompilerOptions.WARNING);
		javaOptions.put(
			CompilerOptions.OPTION_ReportDeprecation, 
				CompilerOptions.WARNING);
		javaOptions.put(
			CompilerOptions.OPTION_ReportHiddenCatchBlock,
			CompilerOptions.WARNING);
		javaOptions.put(
			CompilerOptions.OPTION_ReportUnusedLocal, 
			CompilerOptions.IGNORE);
		javaOptions.put(
			CompilerOptions.OPTION_ReportUnusedParameter,
			CompilerOptions.IGNORE);
		javaOptions.put(
			CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
			CompilerOptions.IGNORE);
		javaOptions.put(
			CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
			CompilerOptions.IGNORE);
		javaOptions.put(
			CompilerOptions.OPTION_ReportAssertIdentifier,
			CompilerOptions.IGNORE);
		javaOptions.put(
			CompilerOptions.OPTION_Compliance,
			CompilerOptions.VERSION_1_3);
		javaOptions.put(
			CompilerOptions.OPTION_Source,
			CompilerOptions.VERSION_1_3);
		javaOptions.put(
			CompilerOptions.OPTION_TargetPlatform,
			CompilerOptions.VERSION_1_1);				
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
	public List getClasspath() { // XXX setters don't respect javadoc contract...
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
     * @return List (String) classpath of injars, aspectpath entries,
     *   specified classpath (bootclasspath, extdirs, and classpath),
     *   and output dir or jar
     */
    public List getFullClasspath() {
        List full = new ArrayList();
        for (Iterator i = inJars.iterator(); i.hasNext(); ) {
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
    
    /** @return true if any config file, sourceroots, sourcefiles, or injars */
    public boolean hasSources() {
        return ((null != configFile)
            || (0 < sourceRoots.size())
            || (0 < files.size())
            || (0 < inJars.size())
            );
    }
    
    /** @return null if no errors, String errors otherwise */
    public String configErrors() {
        StringBuffer result = new StringBuffer();
        // ok, permit both.  sigh.
//        if ((null != outputDir) && (null != outputJar)) {
//            result.append("specified both outputDir and outputJar");
//        }
        // incremental => only sourceroots
        // 
        return (0 == result.length() ? null : result.toString());
    }

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
        join(ajOptions, global.ajOptions);
        join(aspectpath, global.aspectpath);
        join(classpath, global.classpath);
        if (null == configFile) {
            configFile = global.configFile; // XXX correct?
        }
        if (!emacsSymMode && global.emacsSymMode) {
            emacsSymMode = true;
        }
        join(files, global.files);
        if (!generateModelMode && global.generateModelMode) {
            generateModelMode = true;
        }
        if (null == incrementalFile) {
            incrementalFile = global.incrementalFile;
        }
        if (!incrementalMode && global.incrementalMode) {
            incrementalMode = true;
        }
        join(inJars, global.inJars);
        join(javaOptions, global.javaOptions);
        if ((null == lintMode) 
            || (AJLINT_DEFAULT.equals(lintMode))) {
            lintMode = global.lintMode;
        }
        if (null == lintSpecFile) {
            lintSpecFile = global.lintSpecFile;
        }
        if (!noWeave && global.noWeave) {
            noWeave = true;
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
        if (!XnoInline && global.XnoInline) {
            XnoInline = true;
        }
        if (!XserializableAspects && global.XserializableAspects) {
            XserializableAspects = true;
        }
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

}
