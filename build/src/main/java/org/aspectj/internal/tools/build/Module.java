/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.aspectj.internal.tools.build.Result.Kind;
import org.aspectj.internal.tools.build.Util.OSGIBundle;
import org.aspectj.internal.tools.build.Util.OSGIBundle.RequiredBundle;


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

//    private static final int getATTSIndex(String key) {
//        for (int i = 0; i < ATTS.length; i++) {
//            if (ATTS[i].equals(key))
//                return i;
//        }
//        return -1;
//    }

    /**
     * @return true if file is null or cannot be read or was last modified after
     *         time
     */
    private static boolean outOfDate(long time, File file) {
        return ((null == file) || !file.canRead() || (file.lastModified() > time));
    }

    /** @return all source files under srcDir */
    private static Iterator<File> sourceFiles(File srcDir) {
        List<File> result = new ArrayList<>();
        sourceFiles(srcDir, result);
        return result.iterator();
    }

    private static void sourceFiles(File srcDir, List<File> result) {
        if ((null == srcDir) || !srcDir.canRead() || !srcDir.isDirectory()) {
            return;
        }
        File[] files = srcDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				sourceFiles(file, result);
			} else if (isSourceFile(file)) {
				result.add(file);
			}
		}
    }

    private static void addIfNew(List<File> source, List<File> sink) {
    		for (File item: source) {
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
     static void doFindJarRequirements(Result result, List<File> known) {
        Util.iaxIfNull(result, "result");
        Util.iaxIfNull(known, "known");
        addIfNew(result.getLibJars(), known);
        addIfNew(result.getExportedLibJars(), known);
        Result[] reqs = result.getRequired();
		 for (Result requiredResult : reqs) {
			 File requiredJar = requiredResult.getOutputFile();
			 if (!known.contains(requiredJar)) {
				 known.add(requiredJar);
				 doFindJarRequirements(requiredResult, known);
			 }
		 }
    }

    /** @return true if this is a source file */
    private static boolean isSourceFile(File file) {
        String path = file.getPath();
        return (path.endsWith(".java") || path.endsWith(".aj")); // XXXFileLiteral
    }

//    /** @return List of File of any module or library jar ending with suffix */
//    private static ArrayList findJarsBySuffix(String suffix, Kind kind,
//            List libJars, List required) {
//        ArrayList result = new ArrayList();
//        if (null != suffix) {
//            // library jars
//            for (Iterator iter = libJars.iterator(); iter.hasNext();) {
//                File file = (File) iter.next();
//                if (file.getPath().endsWith(suffix)) {
//                    result.add(file);
//                }
//            }
//            // module jars
//            for (Iterator iter = required.iterator(); iter.hasNext();) {
//                Module module = (Module) iter.next();
//                Result moduleResult = module.getResult(kind);
//                File file = moduleResult.getOutputFile();
//                if (file.getPath().endsWith(suffix)) {
//                    result.add(file);
//                }
//            }
//        }
//        return result;
//    }

    public final boolean valid;

    public final File moduleDir;

    public final String name;

    /** reference back to collection for creating required modules */
    private final Modules modules;

    private final Result release;

    private final Result test;

    private final Result testAll;

    private final Result releaseAll;

    /** path to output jar - may not exist */
    private final File moduleJar;

    /** File list of library jars */
    private final List<File> libJars;

    /** List of classpath variables */
    private final List<String> classpathVariables;

    /**
     * List of library jars exported to clients (duplicates some libJars
     * entries)
     */
    private final List<File> exportedLibJars;

    /** File list of source directories */
    private final List<File> srcDirs;

    /** properties from the modules {name}.properties file */
    private final Properties properties;

    /** List of required modules */
    private final List<Module> requiredModules;

    /** logger */
    private final Messager messager;

    Module(File moduleDir, File jarDir, String name, Modules modules,
            Messager messager) {
        Util.iaxIfNotCanReadDir(moduleDir, "moduleDir");
        Util.iaxIfNotCanReadDir(jarDir, "jarDir");
        Util.iaxIfNull(name, "name");
        Util.iaxIfNull(modules, "modules");
        this.moduleDir = moduleDir;
        this.libJars = new ArrayList<>();
        this.exportedLibJars = new ArrayList<>();
        this.requiredModules = new ArrayList<>();
        this.srcDirs = new ArrayList<>();
        this.classpathVariables = new ArrayList<>();
        this.properties = new Properties();
        this.name = name;
        this.modules = modules;
        this.messager = messager;
        this.moduleJar = new File(jarDir, name + ".jar");
        this.release = new Result(Result.RELEASE, this, jarDir);
        this.releaseAll = new Result(Result.RELEASE_ALL, this, jarDir);
        this.test = new Result(Result.TEST, this, jarDir);
        this.testAll = new Result(Result.TEST_ALL, this, jarDir);
        valid = init();
    }


    /** @return Modules registry of known modules, including this one */
    public Modules getModules() {
        return modules;
    }

    /**
     * @param kind
     *            the Kind of the result to recalculate
     * @param recalculate
     *            if true, then force recalculation
     * @return true if the target jar for this module is older than any source
     *         files in a source directory or any required modules or any
     *         libraries or if any libraries or required modules are missing
     */
    public static boolean outOfDate(Result result) {
        File outputFile = result.getOutputFile();
        if (!(outputFile.exists() && outputFile.canRead())) {
            return true;
        }
        final long time = outputFile.lastModified();
        File file;
		for (File srcDir : result.getSrcDirs()) {
			for (Iterator<File> srcFiles = sourceFiles(srcDir); srcFiles.hasNext(); ) {
				file = srcFiles.next();
				if (outOfDate(time, file)) {
					return true;
				}
			}
		}
        // required modules
        Result[] reqs = result.getRequired();
		for (Result requiredResult : reqs) {
			file = requiredResult.getOutputFile();
			if (outOfDate(time, file)) {
				return true;
			}
		}
        // libraries
		for (File value : result.getLibJars()) {
			file = value;
			if (outOfDate(time, file)) {
				return true;
			}
		}
        return false;
    }



    public String toString() {
        return name;
    }

    public String toLongString() {
        return "Module [name=" + name + ", srcDirs=" + srcDirs + ", required="
                + requiredModules + ", moduleJar=" + moduleJar + ", libJars="
                + libJars + "]";
    }

    public Result getResult(Kind kind) {
        return kind.assemble ? (kind.normal ? releaseAll : testAll)
                : (kind.normal ? release : test);
    }

    List<File> srcDirs(Result result) {
        myResult(result);
        return srcDirs;
    }

    List<File> libJars(Result result) {
        myResult(result);
        return libJars;
    }

    List<String> classpathVariables(Result result) {
        myResult(result);
        return classpathVariables;
    }

    List<File> exportedLibJars(Result result) {
        myResult(result);
        return exportedLibJars;
    }

    List<Module> requiredModules(Result result) {
        myResult(result);
        return requiredModules;
    }

    private void myResult(Result result) {
        if ((null == result) || this != result.getModule()) {
            throw new IllegalArgumentException("not my result: " + result + ": " + this);
        }
    }

    private boolean init() {
        boolean cp = initClasspath();
        boolean mf = initManifest();
        if (!cp && !mf) {
            return false;
        }
        return initProperties() && reviewInit() && initResults();
    }

    /** read OSGI manifest.mf file XXX hacked */
    private boolean initManifest() {
        File metaInf = new File(moduleDir, "META-INF");
        if (!metaInf.canRead() || !metaInf.isDirectory()) {
            return false;
        }
        File file = new File(metaInf, "MANIFEST.MF"); // XXXFileLiteral
        if (!file.exists()) {
            return false; // ok, not OSGI
        }
        InputStream fin = null;
        OSGIBundle bundle = null;
        try {
            fin = new FileInputStream(file);
            bundle = new OSGIBundle(fin);
        } catch (IOException e) {
            messager.logException("IOException reading " + file, e);
            return false;
        } finally {
            Util.closeSilently(fin);
        }
        RequiredBundle[] bundles = bundle.getRequiredBundles();
		for (RequiredBundle required : bundles) {
			update("src", "/" + required.name, required.text, false);
		}
        String[] libs = bundle.getClasspath();
		for (String lib : libs) {
			update("lib", lib, lib, false);
		}

        return true;
    }

    /** read eclipse .classpath file XXX line-oriented hack */
    private boolean initClasspath() {
        // meaning testsrc directory, junit library, etc.
        File file = new File(moduleDir, ".classpath"); // XXXFileLiteral
        if (!file.exists()) {
            return false; // OSGI???
        }
        FileReader fin = null;
        try {
            fin = new FileReader(file);
            BufferedReader reader = new BufferedReader(fin);
            String line;
            XMLItem item = new XMLItem("classpathentry", new ICB());
            while (null != (line = reader.readLine())) {
                line = line.trim();
                // dumb - only handle comment-only lines
                if (!line.startsWith("<?xml") && !line.startsWith("<!--")) {
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

//    private boolean update(String toString, String[] attributes) {
//        String kind = attributes[getATTSIndex("kind")];
//        String path = attributes[getATTSIndex("path")];
//        String exp = attributes[getATTSIndex("exported")];
//        boolean exported = ("true".equals(exp));
//        return update(kind, path, toString, exported);
//    }

    private boolean update(String kind, String path, String toString,
            boolean exported) {
        String libPath = null;
        if ("src".equals(kind)) {
            if (path.startsWith("/")) { // module
                String moduleName = path.substring(1);
                Module req = modules.getModule(moduleName);
                if (null != req) {
                    requiredModules.add(req);
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
        	// 'special' for container pointing at AspectJ runtime...
        	if (path.equals("org.eclipse.ajdt.core.ASPECTJRT_CONTAINER")) {
        		classpathVariables.add("ASPECTJRT_LIB");
        	} else {
	            if (!path.contains("JRE")) { // warn non-JRE containers
	                messager.log("cannot handle con yet: " + toString);
	            }
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
		for (String s : known) {
			if (s.equals(path)) {
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
     * Post-process initialization. This implementation trims java5 source dirs
     * if not running in a Java 5 VM.
     * @return true if initialization post-processing worked
     */
    protected boolean reviewInit() {
        try {
            for (ListIterator<File> iter = srcDirs.listIterator(); iter.hasNext();) {
                File srcDir = iter.next();
                String lcname = srcDir.getName().toLowerCase();
                if (!Util.JAVA5_VM
                        && (Util.Constants.JAVA5_SRC.equals(lcname) || Util.Constants.JAVA5_TESTSRC
                                .equals(lcname))) {
                    // assume optional for pre-1.5 builds
                    iter.remove();
                }
            }
        } catch (UnsupportedOperationException e) {
            return false; // failed XXX log also if verbose
        }
        return true;
    }

    /**
     * After reviewInit, setup four kinds of results.
     */
    protected boolean initResults() {
        return true; // results initialized lazily
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
            ArrayList<String> result = new ArrayList<>();
            StringBuffer quote = new StringBuffer();
            boolean inQuote = false;
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if ((1 == s.length()) && (DELIM.contains(s))) {
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
                } else { // not a delimiter
                    if (inQuote) {
                        quote.append(s);
                    } else {
                        result.add(s);
                    }
                }
            }
            return result.toArray(new String[0]);
        }

        public void acceptLine(String line) {
            String[] tokens = tokenize(line);
			for (String token : tokens) {
				next(token);
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
                    System.out
                            .println("unknown state - not value, attribute, or entity: "
                                    + s);
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
