/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.internal.tools.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This represents an (eclipse) build module/unit 
 * used by a Builder to compile classes 
 * and/or assemble zip file
 * of classes, optionally with all antecedants.
 * This implementation infers attributes from two
 * files in the module directory:
 * <ul>
 * <li>an Eclipse project <code>.classpath</code> file
 *     containing required libraries and modules
 *     (collectively, "antecedants")
 *     </li>
 * <li>a file <code>{moduleName}.mf.txt</code> is taken as
 *     the manifest of any .jar file produced, after filtering.
 *     </li>
 * </ul>
 * 
 * @see Builder
 * @see Modules#getModule(String)
 */
public class Module {
    
    /** @return true if file is null or cannot be read or was
     *           last modified after time
     */
    private static boolean outOfDate(long time, File file) {
        return ((null == file) 
            || !file.canRead()
            || (file.lastModified() > time));
    }
    
	/** @return all source files under srcDir */
	private static Iterator sourceFiles(File srcDir) {
        ArrayList result = new ArrayList();
        sourceFiles(srcDir, result);
        return result.iterator();
    }
    
	private static void sourceFiles(File srcDir, List result) {
        if ((null == srcDir) || !srcDir.canRead() || !srcDir.isDirectory()) {
            return;
        }
        File[] files = srcDir.listFiles();
        for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
                sourceFiles(files[i], result);
            } else if (isSourceFile(files[i])) {
                result.add(files[i]);
            }
		}
	}

    /** 
     * Recursively find antecedant jars. 
     * @see findKnownJarAntecedants()
     */
    private static void doFindKnownJarAntecedants(Module module, ArrayList known) {
        Util.iaxIfNull(module, "module");
        Util.iaxIfNull(known, "known");
        
        for (Iterator iter = module.getLibJars().iterator(); iter.hasNext();) {
            File libJar = (File) iter.next();
            if (!skipLibraryJarAntecedant(libJar)
                && !known.contains(libJar)) { // XXX what if same referent, diff path...
                known.add(libJar);
            }
        }
        for (Iterator iter = module.getRequired().iterator(); iter.hasNext();) {
            Module required = (Module) iter.next();
            File requiredJar = required.getModuleJar();
            if (!skipModuleJarAntecedant(requiredJar)
            	&& !known.contains(requiredJar)) {
                known.add(requiredJar);
                doFindKnownJarAntecedants(required, known);
            }
        }
    }
    
    /** XXX gack explicitly skip Ant */
    private static boolean skipLibraryJarAntecedant(File libJar) {
        if (null == libJar) {
            return true;
        }
        String path = libJar.getPath().replace('\\', '/');
        return (-1 != path.indexOf("/lib/ant/lib/"));
    }

    /** XXX gack explicitly skip runtime */
    private static boolean skipModuleJarAntecedant(File requiredJar) {
    	if (null == requiredJar) {
    		return true;
    	} else {
	        return "runtime.jar".equals(requiredJar.getName());
    	}
    }

	/**@return true if this is a source file */
	private static boolean isSourceFile(File file) {
        String path = file.getPath();
        return (path.endsWith(".java") || path.endsWith(".aj"));   // XXXFileLiteral
	}
    
    public final boolean valid;

    public final File moduleDir;

    public final String name;

    /** reference back to collection for creating required modules */
    final Modules modules;
    
    /** path to output jar - may not exist */
    private final File moduleJar;
    
    /** path to fully-assembed jar - may not exist */
    private final File assembledJar;
    
    /** File list of library jars */
    private final List libJars;

    /** File list of source directories */
    private final List srcDirs;

    /** properties from the modules {name}.properties file */
    private final Properties properties;

    /** Module list of required modules */
    private final List required;
    
    /** List of File that are newer than moduleJar.  Null until requested */
    //private List newerFiles;
    /** true if this has been found to be out of date */
    private boolean outOfDate;
    
    /** true if we have calculated whether this is out of date */
    private boolean outOfDateSet;
    
    /** if true, trim testing-related source directories, modules, and libraries */
    private final boolean trimTesting;
    
    /** logger */
    private final Messager messager;
    
    Module(File moduleDir, 
        File jarDir, 
        String name, 
        Modules modules, 
        boolean trimTesting,
        Messager messager) {
        Util.iaxIfNotCanReadDir(moduleDir, "moduleDir");
        Util.iaxIfNotCanReadDir(jarDir, "jarDir");
        Util.iaxIfNull(name, "name");
        Util.iaxIfNull(modules, "modules");
        this.moduleDir = moduleDir;
        this.trimTesting = trimTesting;
        this.libJars = new ArrayList();
        this.required = new ArrayList();
        this.srcDirs = new ArrayList();
        this.properties = new Properties();
        this.name = name;
        this.modules = modules;
        this.messager = messager;
        this.moduleJar = new File(jarDir, name + ".jar");       
        this.assembledJar = new File(jarDir, name + "-all.jar");       
        valid = init();
    }
        
    /** @return path to output jar - may not exist */
    public File getModuleJar() {
        return moduleJar;
    }
    
    /** @return path to output assembled jar - may not exist */
    public File getAssembledJar() {
        return assembledJar;
    }
    
    /** @return unmodifiable List of required modules String names*/
    public List getRequired() {
        return Collections.unmodifiableList(required);
    }
    
    /** @return unmodifiable list of required library files, guaranteed readable */
    public List getLibJars() {
        return Collections.unmodifiableList(libJars);
    }
    
    /** @return unmodifiable list of source directories, guaranteed readable */
    public List getSrcDirs() {
        return Collections.unmodifiableList(srcDirs);
    }
    
    /** @return Modules registry of known modules, including this one */
    public Modules getModules() {
        return modules;
    }
    
    /** @return List of File jar paths to be merged into module-dist */
    public List getMerges() {
        String value = properties.getProperty(name + ".merges");
        if ((null == value) || (0 == value.length())) {
            return Collections.EMPTY_LIST;
        }
        ArrayList result = new ArrayList();
        StringTokenizer st = new StringTokenizer(value);
        while (st.hasMoreTokens()) {
            result.addAll(findJarsBySuffix(st.nextToken()));
        }
        return result;
    }
    
   
    public void clearOutOfDate() {
        outOfDate = false;
        outOfDateSet = false;
    }
    
    /**
     * @param recalculate if true, then force recalculation 
     * @return true if the target jar for this module is older than
     *          any source files in a source directory
     *          or any required modules
     *          or any libraries
     *          or if any libraries or required modules are missing
     */
    public boolean outOfDate(boolean recalculate) {
        if (recalculate) {
            outOfDateSet = false;
        }
        if (!outOfDateSet) {
            outOfDate = false;
            try {
                if (!(moduleJar.exists() && moduleJar.canRead())) {
                    return outOfDate = true;
                }
                final long time = moduleJar.lastModified();
                File file;
                for (Iterator iter = srcDirs.iterator(); iter.hasNext();) {
                  File srcDir = (File) iter.next();
                    for (Iterator srcFiles = sourceFiles(srcDir); srcFiles.hasNext();) {
                        file = (File) srcFiles.next();
                        if (outOfDate(time, file)) {
                            return outOfDate = true;
                        }
                    }
                }
                // required modules
                for (Iterator iter = getRequired().iterator(); iter.hasNext();) {
                    Module required = (Module) iter.next();
                    file = required.getModuleJar();
                    if (outOfDate(time, file)) {
                        return outOfDate = true;
                    }
                }
                // libraries
                for (Iterator iter = getLibJars().iterator(); iter.hasNext();) {
                    file = (File) iter.next();
                    if (outOfDate(time, file)) {
                        return outOfDate = true;
                    }
                }
            } finally {
                outOfDateSet = true;
            }
        }
        return outOfDate;
    }
    /**
     * Add any (File) library jar  or (File) required module jar
     * to the List known, if not added already.
     */
    public ArrayList findKnownJarAntecedants() {
        ArrayList result = new ArrayList();
        doFindKnownJarAntecedants(this, result);
        return result;   
    }
    
    public String toString() {
        return name;
    }

    public String toLongString() {
        return  
            "Module [name="
            + name
            + ", srcDirs="
            + srcDirs
            + ", required="
            + required
            + ", moduleJar="
            + moduleJar
            + ", libJars="
            + libJars
            + "]";
    }

    private boolean init() {
        return initClasspath() && initProperties() && reviewInit();
    }

    /** read eclipse .classpath file XXX line-oriented hack */
    private boolean initClasspath() {
        // meaning testsrc directory, junit library, etc.
        File file = new File(moduleDir, ".classpath");   // XXXFileLiteral
        FileReader fin = null;
        try {
            fin = new FileReader(file);
            BufferedReader reader = new BufferedReader(fin);
            String line;
            String lastKind = null;
            while (null != (line = reader.readLine())) {
                lastKind = parseLine(line, lastKind);
            }
            return (0 < srcDirs.size());
        } catch (IOException e) {
            messager.logException("IOException reading " + file, e);
        } finally {
            if (null != fin) {
                try { fin.close(); }
                catch (IOException e) {} // ignore
            }
        }
        return false;
    }

    /** @return true if any properties were read correctly */
    private boolean initProperties() {
        File file = new File(moduleDir, name + ".properties");   // XXXFileLiteral
        if (!Util.canReadFile(file)) {
            return true; // no properties to read
        }
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            properties.load(fin);
            return true;
        } catch (IOException e) {
            messager.logException("IOException reading " + file, e);
            return false;
        } finally {
            if (null != fin) {
                try { fin.close(); }
                catch (IOException e) {} // ignore
            }
        }
    }
    
    /** 
     * Post-process initialization.  
     * This implementation trims testing-related source 
     * directories, libraries, and modules if trimTesting is enabled/true.  
     * To build testing modules, trimTesting must be false.
     * @return true if initialization post-processing worked 
     */
    protected boolean reviewInit() {   
        if (!trimTesting) {
            return true;
        }
        try {
            for (ListIterator iter = srcDirs.listIterator(); iter.hasNext();) {
    			File srcDir = (File) iter.next();
    		    String name = srcDir.getName();
                if ("testsrc".equals(name.toLowerCase())) { // XXXFileLiteral
                    iter.remove(); // XXX if verbose log
                }	
    		}
            for (ListIterator iter = libJars.listIterator(); iter.hasNext();) {
                File libJar = (File) iter.next();
                String name = libJar.getName();
                if ("junit.jar".equals(name.toLowerCase())) {  // XXXFileLiteral              
                    iter.remove(); // XXX if verbose log
                }   
            }
            for (ListIterator iter = required.listIterator(); iter.hasNext();) {
                Module required = (Module) iter.next();
                String name = required.name;
                // XXX testing-util only ?
                if (name.toLowerCase().startsWith("testing")) {  // XXXFileLiteral
                    iter.remove(); // XXX if verbose log
                }   
            }
        } catch (UnsupportedOperationException e) {
            return false; // failed XXX log also if verbose
        }
        return true;
    }
    
    private String parseLine(String line, String lastKind) {
        if (null == line) {
            return null;
        }
        String kind;
        int loc = line.indexOf("kind=\"");
        if ((-1 == loc) || (loc + 9 > line.length())) {
            // no kind string - fail unless have lastKind
            if (null == lastKind) {
                return null; 
            } else {
                kind = lastKind;
            }
        } else { // have kind string - get kind
            loc += 6; // past kind="
            kind = line.substring(loc, loc+3);
        }
        
        // now look for value
        loc = line.indexOf("path=\"");
        if (-1 == loc)  { // no value - return lastKind
            return kind;
        } 
        loc += 6; // past path="
        int end = line.indexOf("\"", loc);
        if (-1 == end) {
            throw new Error("unterminated path in " + line);
        } 
        final String path = line.substring(loc, end);
        
        if ("src".equals(kind)) {
            if (path.startsWith("/")) { // module
                String moduleName = path.substring(1);
                Module req = modules.getModule(moduleName);
                if (null != req) {
                    required.add(req);
                } else {
                    messager.error("unable to create required module: " + moduleName);
                }                
            } else {                    // src dir
                File srcDir = new File(moduleDir, path);
                if (srcDir.canRead() && srcDir.isDirectory()) {
                    srcDirs.add(srcDir); 
                } else {
                    messager.error("not a src dir: " + srcDir);
                }
            }
        } else if ("lib".equals(kind)) {
            String libPath = path.startsWith("/") 
                ? modules.baseDir.getAbsolutePath() + path
                : path;
             File libJar = new File(libPath);
             if (libJar.canRead() && libJar.isFile()) {
                libJars.add(libJar);
             } else {
                messager.error("no such library jar " + libJar + " from " + line);                
             }
        } else if ("var".equals(kind)) {
            if (!"JRE_LIB".equals(path)) {
                messager.log("cannot handle var yet: " + line);
            }
        } else if ("con".equals(kind)) {
            messager.log("cannot handle con yet: " + line);
        } else if ("out".equals(kind)) {
            // ignore output entries
        } else {
            messager.log("unrecognized kind " + kind + " in " + line);
        }
        return null;
    }
    
    /** @return List of File of any module or library jar ending with suffix */
    private ArrayList findJarsBySuffix(String suffix) {
        ArrayList result = new ArrayList();
        if (null != suffix) {
            // library jars
            for (Iterator iter = getLibJars().iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                if (file.getPath().endsWith(suffix)) {
                    result.add(file);
                }
            }
            // module jars
            for (Iterator iter = getRequired().iterator(); iter.hasNext();) {
                Module module = (Module) iter.next();
                File file = module.getModuleJar();
                if (file.getPath().endsWith(suffix)) {
                    result.add(file);
                }
            }
        }
        return result;
    }
}
    
