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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This represents an (eclipse) build module/unit used by a Builder to compile
 * classes and/or assemble zip file of classes, optionally with all antecedants.
 * This implementation infers attributes from two files in the module directory:
 * <ul>
 * <li>an Eclipse project <code>.classpath</code> file containing required
 * libraries and modules (collectively, "antecedants") </li>
 * <li>a file <code>{moduleName}.mf.txt</code> is taken as the manifest of
 * any .jar file produced, after filtering. </li>
 * </ul>
 * 
 * @see Builder
 * @see Modules#getModule(String)
 */
public class Module {
    private static final String[] ATTS = new String[] { "exported", "kind",
            "path", "sourcepath" };

    private static final int getATTSIndex(String key) {
        for (int i = 0; i < ATTS.length; i++) {
            if (ATTS[i].equals(key))
                return i;
        }
        return -1;
    }

    /**
     * @return true if file is null or cannot be read or was last modified after
     *         time
     */
    private static boolean outOfDate(long time, File file) {
        return ((null == file) || !file.canRead() || (file.lastModified() > time));
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

    private static void addIfNew(List source, List sink) {
        for (Iterator iter = source.iterator(); iter.hasNext();) {
            Object item = iter.next();
            if (!sink.contains(item)) {
                sink.add(item);
            }
        }
    }

    /**
     * Recursively find antecedant jars.
     * 
     * @see findKnownJarAntecedants()
     */
    private static void doFindKnownJarAntecedants(Module module, List known) {
        Util.iaxIfNull(module, "module");
        Util.iaxIfNull(known, "known");
        addIfNew(module.getLibJars(), known);
        for (Iterator iter = module.getRequired().iterator(); iter.hasNext();) {
            Module required = (Module) iter.next();
            File requiredJar = required.getModuleJar();
            if (!known.contains(requiredJar)) {
                known.add(requiredJar);
                doFindKnownJarAntecedants(required, known);
            }
        }
    }

    /** @return true if this is a source file */
    private static boolean isSourceFile(File file) {
        String path = file.getPath();
        return (path.endsWith(".java") || path.endsWith(".aj")); // XXXFileLiteral
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

    /** String list of classpath variables */
    private final List classpathVariables;

    /**
     * File list of library jars exported to clients (duplicates some libJars
     * entries)
     */
    private final List exportedLibJars;

    /** File list of source directories */
    private final List srcDirs;

    /** properties from the modules {name}.properties file */
    private final Properties properties;

    /** Module list of required modules */
    private final List required;

    /** List of File that are newer than moduleJar. Null until requested */
    // private List newerFiles;
    /** true if this has been found to be out of date */
    private boolean outOfDate;

    /** true if we have calculated whether this is out of date */
    private boolean outOfDateSet;

    /** if true, trim testing-related source directories, modules, and libraries */
    private final boolean trimTesting;

    /** logger */
    private final Messager messager;

    Module(File moduleDir, File jarDir, String name, Modules modules,
            boolean trimTesting, Messager messager) {
        Util.iaxIfNotCanReadDir(moduleDir, "moduleDir");
        Util.iaxIfNotCanReadDir(jarDir, "jarDir");
        Util.iaxIfNull(name, "name");
        Util.iaxIfNull(modules, "modules");
        this.moduleDir = moduleDir;
        this.trimTesting = trimTesting;
        this.libJars = new ArrayList();
        this.exportedLibJars = new ArrayList();
        this.required = new ArrayList();
        this.srcDirs = new ArrayList();
        this.classpathVariables = new ArrayList();
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

    /** @return unmodifiable List of String classpath variables */
    public List getClasspathVariables() {
        return Collections.unmodifiableList(classpathVariables);
    }

    /** @return unmodifiable List of required modules String names */
    public List getRequired() {
        return Collections.unmodifiableList(required);
    }

    /** @return unmodifiable list of exported library files, guaranteed readable */
    public List getExportedLibJars() {
        return Collections.unmodifiableList(exportedLibJars);
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
     * @param recalculate
     *            if true, then force recalculation
     * @return true if the target jar for this module is older than any source
     *         files in a source directory or any required modules or any
     *         libraries or if any libraries or required modules are missing
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
                    for (Iterator srcFiles = sourceFiles(srcDir); srcFiles
                            .hasNext();) {
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
     * Add any (File) library jar or (File) required module jar to the List
     * known, if not added already.
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
        return "Module [name=" + name + ", srcDirs=" + srcDirs + ", required="
                + required + ", moduleJar=" + moduleJar + ", libJars="
                + libJars + "]";
    }

    private boolean init() {
        return initClasspath() && initProperties() && reviewInit();
    }

    /** read eclipse .classpath file XXX line-oriented hack */
    private boolean initClasspath() {
        // meaning testsrc directory, junit library, etc.
        File file = new File(moduleDir, ".classpath"); // XXXFileLiteral
        FileReader fin = null;
        try {
            fin = new FileReader(file);
            BufferedReader reader = new BufferedReader(fin);
            String line;
            XMLItem item = new XMLItem("classpathentry", new ICB());
            while (null != (line = reader.readLine())) {
                line = line.trim();
                // dumb - only handle comment-only lines
                if (!line.startsWith("<?xml") && ! line.startsWith("<!--")) {
                    item.acceptLine(line);
                }
            }
            return (0 < (srcDirs.size() + libJars.size()));
        } catch (IOException e) {
            messager.logException("IOException reading " + file, e);
        } finally {
            if (null != fin) {
                try {
                    fin.close();
                } catch (IOException e) {
                } // ignore
            }
        }
        return false;
    }

    private boolean update(String toString, String[] attributes) {
        String kind = attributes[getATTSIndex("kind")];
        String path = attributes[getATTSIndex("path")];
        String exp = attributes[getATTSIndex("exported")];
        boolean exported = ("true".equals(exp));                 
        return update(kind, path, toString, exported);
    }
    
    private boolean update(String kind, String path, String toString, boolean exported) {    
        String libPath = null;
        if ("src".equals(kind)) {
            if (path.startsWith("/")) { // module
                String moduleName = path.substring(1);
                Module req = modules.getModule(moduleName);
                if (null != req) {
                    required.add(req);
                    return true;
                } else {
                    messager.error("update unable to create required module: "
                            + moduleName);
                }
            } else { // src dir
                String fullPath = getFullPath(path);
                File srcDir = new File(fullPath);
                if (srcDir.canRead() && srcDir.isDirectory()) {
                    srcDirs.add(srcDir);
                    return true;
                } else {
                    messager.error("not a src dir: " + srcDir);
                }
            }
        } else if ("lib".equals(kind)) {
            libPath = path;
        } else if ("var".equals(kind)) {
            final String JAVA_HOME = "JAVA_HOME/";
            if (path.startsWith(JAVA_HOME)) {
                path = path.substring(JAVA_HOME.length());
                String home = System.getProperty("java.home");
                if (null != home) {
                    libPath = Util.path(home, path);
                    File f = new File(libPath);
                    if (!f.exists() && home.endsWith("jre")) {
                        f = new File(home).getParentFile();
                        libPath = Util.path(f.getPath(), path);
                    }
                }
            }
            if (null == libPath) {
                warnVariable(path, toString);
                classpathVariables.add(path);
            }
        } else if ("con".equals(kind)) {
            if (-1 == path.indexOf("JRE")) { // warn non-JRE containers
                messager.log("cannot handle con yet: " + toString);
            }
        } else if ("out".equals(kind) || "output".equals(kind)) {
            // ignore output entries
        } else {
            messager.log("unrecognized kind " + kind + " in " + toString);
        }
        if (null != libPath) {
            File libJar = new File(libPath);
            if (!libJar.exists()) {
                libJar = new File(getFullPath(libPath));
            }
            if (libJar.canRead() && libJar.isFile()) {
                libJars.add(libJar);
                if (exported) {
                    exportedLibJars.add(libJar);
                }
                return true;
            } else {
                messager.error("no such library jar " + libJar + " from "
                        + toString);
            }
        }
        return false;
    }

    private void warnVariable(String path, String toString) {
        String[] known = { "JRE_LIB", "ASPECTJRT_LIB", "JRE15_LIB" };
        for (int i = 0; i < known.length; i++) {
            if (known[i].equals(path)) {
                return;
            }
        }
        messager.log("Module cannot handle var yet: " + toString);
    }

    /** @return true if any properties were read correctly */
    private boolean initProperties() {
        File file = new File(moduleDir, name + ".properties"); // XXXFileLiteral
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
                try {
                    fin.close();
                } catch (IOException e) {
                } // ignore
            }
        }
    }

    /**
     * Post-process initialization. This implementation trims testing-related
     * source directories, libraries, and modules if trimTesting is
     * enabled/true. For modules whose names start with "testing",
     * testing-related sources are trimmed, but this does not trim dependencies
     * on other modules prefixed "testing" or on testing libraries like junit.
     * That means testing modules can be built with trimTesting enabled.
     * 
     * @return true if initialization post-processing worked
     */
    protected boolean reviewInit() {
        try {
            for (ListIterator iter = srcDirs.listIterator(); iter.hasNext();) {
                File srcDir = (File) iter.next();
                String lcname = srcDir.getName().toLowerCase();
                if (trimTesting
                        && (Util.Constants.TESTSRC.equals(lcname) || Util.Constants.JAVA5_TESTSRC
                                .equals(lcname))) {
                    iter.remove();
                } else if (!Util.JAVA5_VM
                        && (Util.Constants.JAVA5_SRC.equals(lcname) || Util.Constants.JAVA5_TESTSRC
                                .equals(lcname))) {
                    // assume optional for pre-1.5 builds
                    iter.remove();
                }
            }
            if (!trimTesting) {
                return true;
            }
            if (!name.startsWith("testing")) {
                for (ListIterator iter = libJars.listIterator(); iter.hasNext();) {
                    File libJar = (File) iter.next();
                    String name = libJar.getName();
                    if ("junit.jar".equals(name.toLowerCase())) { // XXXFileLiteral
                        iter.remove(); // XXX if verbose log
                    }
                }
                for (ListIterator iter = required.listIterator(); iter
                        .hasNext();) {
                    Module required = (Module) iter.next();
                    String name = required.name;
                    // XXX testing-util only ?
                    if (name.toLowerCase().startsWith("testing")) { // XXXFileLiteral
                        iter.remove(); // XXX if verbose log
                    }
                }
            }
        } catch (UnsupportedOperationException e) {
            return false; // failed XXX log also if verbose
        }
        return true;
    }

    /** resolve path absolutely, assuming / means base of modules dir */
    public String getFullPath(String path) {
        String fullPath;
        if (path.startsWith("/")) {
            fullPath = modules.baseDir.getAbsolutePath() + path;
        } else {
            fullPath = moduleDir.getAbsolutePath() + "/" + path;
        }
        // check for absolute paths (untested - none in our modules so far)
        File testFile = new File(fullPath);
        // System.out.println("Module.getFullPath: " + fullPath + " - " +
        // testFile.getAbsolutePath());
        if (!testFile.exists()) {
            testFile = new File(path);
            if (testFile.exists() && testFile.isAbsolute()) {
                fullPath = path;
            }
        }
        return fullPath;
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

    class ICB implements XMLItem.ICallback {
        public void end(Properties attributes) {
            String kind = attributes.getProperty("kind");
            String path = attributes.getProperty("path");
            String exp = attributes.getProperty("exported");
            boolean exported = ("true".equals(exp));
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            attributes.list(new PrintStream(bout));
            update(kind, path, bout.toString(), exported);
        }
    }

    public static class XMLItem {
        public interface ICallback {
            void end(Properties attributes);
        }
        static final String START_NAME = "classpathentry";

        static final String ATT_STARTED = "STARTED";

        final ICallback callback;
        final StringBuffer input = new StringBuffer();

        final String[] attributes = new String[ATTS.length];
        final String targetEntity;
        String entityName;
        String attributeName;

        XMLItem(String targetEntity, ICallback callback) {
            this.callback = callback;
            this.targetEntity = targetEntity;
            reset();
            
        }

        private void reset() {
            input.setLength(0);
            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = null;
            }
            entityName = null;
            attributeName = null;
        }

        String[] tokenize(String line) {
            final String DELIM = " \n\t\\<>\"=";
            StringTokenizer st = new StringTokenizer(line, DELIM, true);
            ArrayList result = new ArrayList();
            StringBuffer quote = new StringBuffer();
            boolean inQuote = false;
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if ((1 == s.length()) && (-1 != DELIM.indexOf(s))) {
                    if ("\"".equals(s)) { // end quote (or escaped)
                        if (inQuote) {
                            inQuote = false;
                            quote.append("\"");
                            result.add(quote.toString());
                            quote.setLength(0);
                        } else {
                            quote.append("\"");
                            inQuote = true;
                        }
                    } else {
                        result.add(s);
                    }
                } else {  // not a delimiter
                    if (inQuote) {
                        quote.append(s);
                    } else {
                        result.add(s);
                    }
                }
            }
            return (String[]) result.toArray(new String[0]);
        }

        public void acceptLine(String line) {
            String[] tokens = tokenize(line);
            for (int i = 0; i < tokens.length; i++) {
                next(tokens[i]);
            }
        }
        
        private Properties attributesToProperties() {
            Properties result = new Properties();
            for (int i = 0; i < attributes.length; i++) {
                String a = attributes[i];
                if (null != a) {
                    result.setProperty(ATTS[i], a);
                }
            }
            return result;
        }

        void errorIfNotNull(String name, String value) {
            if (null != value) {
                error("Did not expect " + name + ": " + value);
            }
        }
        
        void errorIfNull(String name, String value) {
            if (null == value) {
                error("expected value for " + name);
            }
        }
        
        boolean activeEntity() {
            return targetEntity.equals(entityName);
        }
        
        /**
         * Assumes that comments and "<?xml"-style lines are removed.
         */
        public void next(String s) {
            if ((null == s) || (0 == s.length())) {
                return;
            }
            input.append(s);
            s = s.trim();
            if (0 == s.length()) {
                return;
            }
            if ("<".equals(s)) {
                errorIfNotNull("entityName", entityName);
                errorIfNotNull("attributeName", attributeName);
            } else if (">".equals(s)) {
                errorIfNull("entityName", entityName);
                if ("/".equals(attributeName)) {
                    attributeName = null;
                } else {
                    errorIfNotNull("attributeName", attributeName);
                }
                if (activeEntity()) {
                    callback.end(attributesToProperties());
                }
                entityName = null;
            } else if ("=".equals(s)) {
                errorIfNull("entityName", entityName);
                errorIfNull("attributeName", attributeName);
            } else if (s.startsWith("\"")) {
                errorIfNull("entityName", entityName);
                errorIfNull("attributeName", attributeName);
                writeAttribute(attributeName, s);
                attributeName = null;
            } else {
                if (null == entityName) {
                    reset();
                    entityName = s;
                } else if (null == attributeName) {
                    attributeName = s;
                } else {
                    System.out.println("unknown state - not value, attribute, or entity: " + s);
                }
            }
        }

        void readAttribute(String s) {
            for (int i = 0; i < ATTS.length; i++) {
                if (s.equals(ATTS[i])) {
                    attributes[i] = ATT_STARTED;
                    break;
                }
            }
        }

        void writeAttribute(String name, String value) {
            for (int i = 0; i < ATTS.length; i++) {
                if (name.equals(ATTS[i])) {
                    if (!value.startsWith("\"") || !value.endsWith("\"")) {
                        error("bad attribute value: " + value);
                    }
                    value = value.substring(1, value.length() - 1);
                    attributes[i] = value;
                    return;
                }
            }
        }

        void error(String s) {
            throw new Error(s + " at input " + input);
        }
    }
}

