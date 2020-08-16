/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC           initial implementation
 * ******************************************************************/

package org.aspectj.internal.tools.build;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.aspectj.internal.tools.build.Result.Kind;

/**
 * Template class to build (eclipse) modules (and, weakly, products), including
 * any required modules. When building modules, this assumes:
 * <ul>
 * <li>the name of the module is the base name of the module directory</li>
 * <li>all module directories are in the same base (workspace) directory</li>
 * <li>the name of the target module jar is {moduleName}.jar</li>
 * <li>a module directory contains a <code>.classpath</code> file with
 * (currently line-parseable) entries per Eclipse (XML) conventions</li>
 * <li><code>Builder.RESOURCE_PATTERN</code> identifies all resources to copy
 * to output.</li>
 * <li>This can safely trim test-related code:
 * <ul>
 * <li>source directories named "testsrc"</li>
 * <li>libraries named "junit.jar"</li>
 * <li>required modules whose names start with "testing"</li>
 * </ul>
 * <li>A file <code>{moduleDir}/{moduleName}.properties</code> is a property
 * file possibly containing entries defining requirements to be merged with the
 * output jar (deprecated mechanism - use assembleAll or products)</li>
 * </ul>
 * This currently provides no control over the compile or assembly process, but
 * clients can harvest <code>{moduleDir}/bin</code> directories to re-use the
 * results of eclipse compiles.
 * <p>
 * When building products, this assumes:
 * <ul>
 * <li>the installer-resources directory is a peer of the products directory,
 * itself the parent of the particular product directory.</li>
 * <li>the dist, jar, product, and base (module) directory are set</li>
 * <li>the product distribution consists of all (and only) the files in the
 * dist sub-directory of the product directory</li>
 * <li>files in the dist sub-directory that are empty and end with .jar
 * represent modules to build, either as named or through aliases known here.</li>
 * <li>When assembling the distribution, all non-binary files are to be
 * filtered.
 * <li>
 * <li>the name of the product installer is
 * aspectj-{productName}-{version}.jar, where {productName} is the base name of
 * the product directory</li>
 * </ul>
 * <p>
 * When run using main(String[]), all relevant Ant libraries and properties must
 * be defined.
 * <p>
 * Written to compile standalone. Refactor if using utils, bridge, etc.
 */
public abstract class Builder {

    /**
     * This has only weak forms for build instructions needed: - resource
     * pattern - compiler selection and control
     *
     * Both assumed and generated paths are scattered; see XXXNameLiteral and
     * XXXFileLiteral.
     *
     * Builder is supposed to be thread-safe, but currently caches build
     * properties to tunnel for filters. hmm.
     */

    public static final String RESOURCE_PATTERN;

    public static final String BINARY_SOURCE_PATTERN;

    public static final String ALL_PATTERN;

    /** enable copy filter semantics */
    protected static final boolean FILTER_ON = true;

    /** disable copy filter semantics */
    protected static final boolean FILTER_OFF = false;

    /** define libraries to skip as comma-delimited values for this key */
    private static final String SKIP_LIBRARIES_KEY = "skip.libraries";

    /** List (String) names of libraries to skip during assembly */
    private static final List<String> SKIP_LIBRARIES;

    private static final String ERROR_KEY = "error loading properties";

    private static final Properties PROPS;
    static {
        PROPS = new Properties();
        List<String> skips = Collections.emptyList();
        String resourcePattern = "**/*.txt,**/*.rsc,**/*.gif,**/*.properties";
        String allPattern = "**/*";
        String binarySourcePattern = "**/*.rsc,**/*.gif,**/*.jar,**/*.zip";
        String name = Builder.class.getName().replace('.', '/') + ".properties";
        try {
            InputStream in = Builder.class.getClassLoader()
                    .getResourceAsStream(name);
            PROPS.load(in);
            allPattern = PROPS.getProperty("all.pattern");
            resourcePattern = PROPS.getProperty("resource.pattern");
            binarySourcePattern = PROPS.getProperty("binarySource.pattern");
            skips = commaStrings(PROPS.getProperty(SKIP_LIBRARIES_KEY));
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            String m = "error loading " + name + ": " + t.getClass() + " " + t;
            PROPS.setProperty(ERROR_KEY, m);
        }
        SKIP_LIBRARIES = skips;
        ALL_PATTERN = allPattern;
        BINARY_SOURCE_PATTERN = binarySourcePattern;
        RESOURCE_PATTERN = resourcePattern;
    }

    /**
     * Splits strings into an unmodifable <code>List</code> of String using
     * comma as the delimiter and trimming whitespace from the result.
     *
     * @param text
     *            <code>String</code> to split.
     * @return unmodifiable List (String) of String delimited by comma in text
     */
    public static List<String> commaStrings(String text) {
        if ((null == text) || (0 == text.length())) {
            return Collections.EMPTY_LIST;
        }
        List<String> strings = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(text, ",");
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken().trim();
            if (0 < token.length()) {
                strings.add(token);
            }
        }
        return Collections.unmodifiableList(strings);
    }

    /**
     * Map delivered-jar name to created-module name
     *
     * @param jarName
     *            the String (lowercased) of the jar/zip to map
     */
    private String moduleAliasFor(String jarName) {
        String result = PROPS.getProperty("alias." + jarName, jarName);
        if (verbose && result.equals(jarName)) {
            String m = "expected alias for " + jarName;
            handler.error(m + PROPS.getProperty(ERROR_KEY, ""));
        }
        return result;
    }

    protected final Messager handler;

    protected boolean buildingEnabled;

    private final File tempDir;

    private final List tempFiles;

    private final boolean useEclipseCompiles;

    protected boolean verbose;

    protected Builder(File tempDir, boolean useEclipseCompiles, Messager handler) {
        Util.iaxIfNull(handler, "handler");
        this.useEclipseCompiles = useEclipseCompiles;
        this.handler = handler;
        this.tempFiles = new ArrayList();
        if ((null == tempDir) || !tempDir.canWrite() || !tempDir.isDirectory()) {
            this.tempDir = Util.makeTempDir("Builder");
        } else {
            this.tempDir = tempDir;
        }
        buildingEnabled = true;
    }

    /** tell builder to stop or that it's ok to run */
    public void setBuildingEnabled(boolean enabled) {
        buildingEnabled = enabled;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private void verifyBuildSpec(BuildSpec buildSpec) {
        if (null == buildSpec.productDir) { // ensure module properties
            // derive moduleDir from baseDir + module
            if (null == buildSpec.moduleDir) {
                if (null == buildSpec.baseDir) {
                    throw new BuildException("require baseDir or moduleDir");
                } else if (null == buildSpec.module) {
                    throw new BuildException("require module with baseDir");
                } else {
                    if (null == buildSpec.baseDir) {
                        buildSpec.baseDir = new File("."); // user.home?
                    }
                    buildSpec.moduleDir = new File(buildSpec.baseDir,
                            buildSpec.module);
                }
            } else if (null == buildSpec.baseDir) {
                // derive baseDir from moduleDir parent
                buildSpec.baseDir = buildSpec.moduleDir.getParentFile();
                // rule: base is parent
                if (null == buildSpec.baseDir) {
                    buildSpec.baseDir = new File("."); // user.home?
                }
                handler.log("Builder using derived baseDir: "
                        + buildSpec.baseDir);
            }
            Util.iaxIfNotCanReadDir(buildSpec.moduleDir, "moduleDir");
            if (null == buildSpec.module) {
                // derive module name from directory
                buildSpec.module = buildSpec.moduleDir.getName();
                if (null == buildSpec.module) {
                    throw new BuildException("no name, even from "
                            + buildSpec.moduleDir);
                }
            }
        }
    }

    /**
     * Find the Result (and hence Module and Modules) for this BuildSpec.
     */
    protected Result specifyResultFor(BuildSpec buildSpec) {
        if (buildSpec.trimTesting
                && (buildSpec.module.contains("testing"))) { // XXXNameLiteral
            String warning = "Warning - cannot trimTesting for testing modules: ";
            handler.log(warning + buildSpec.module);
        }
        Messager handler = new Messager();
        Modules modules = new Modules(buildSpec.baseDir, buildSpec.jarDir,
                handler);

        final Module moduleToBuild = modules.getModule(buildSpec.module);
        Kind kind = Result.kind(buildSpec.trimTesting,
                buildSpec.assembleAll);
        return moduleToBuild.getResult(kind);
    }

    public final boolean build(BuildSpec buildSpec) {
        if (!buildingEnabled) {
            return false;
        }
        verifyBuildSpec(buildSpec);

        if (null != buildSpec.productDir) {
            return buildProduct(buildSpec);
        }
        Result result = specifyResultFor(buildSpec);
        List<String> errors = new ArrayList<>();
        try {
            return buildAll(result, errors);
        } finally {
            if (0 < errors.size()) {
                String label = "error building " + buildSpec + ": ";
				for (String error : errors) {
					String m = label + error;
					handler.error(m);
				}
            }
        }
    }

    /**
     * Clean up any temporary files, etc. after build completes
     */
    public boolean cleanup() {
        boolean noErr = true;
		for (Object tempFile : tempFiles) {
			File file = (File) tempFile;
			if (!Util.deleteContents(file) || !file.delete()) {
				if (noErr) {
					noErr = false;
				}
				handler.log("unable to clean up " + file);
			}
		}
        return noErr;
    }

    protected final boolean isLogging() {
        return (verbose && (null != this.handler));
    }

    protected Result[] skipUptodate(Result[] results) {
        if (null == results) {
            return new Result[0];
        }
        Result[] done = new Result[results.length];
        int to = 0;
        for (int i = 0; i < done.length; i++) {
            if ((null != results[i]) && results[i].outOfDate()) {
                done[to++] = results[i];
            }
        }
        if (to < results.length) {
            Result[] newdone = new Result[to];
            System.arraycopy(done, 0, newdone, 0, newdone.length);
            done = newdone;
        }
        return done;
    }

    /**
     * Build a result with all antecedants.
     *
     * @param result
     *            the Result to build
     * @param errors
     *            the List sink for errors, if any
     * @return false after successful build, when module jar should exist
     */
    protected final boolean buildAll(Result result, List<String> errors) {
        Result[] buildList = skipUptodate(getAntecedantResults(result));
        List<String> doneList = new ArrayList<>();
        if ((null != buildList) && (0 < buildList.length)) {
            if (isLogging()) {
                handler.log("modules to build: " + Arrays.asList(buildList));
            }
			for (Result required : buildList) {
				if (!buildingEnabled) {
					return false;
				}
				String requiredName = required.getName();
				if (!doneList.contains(requiredName)) {
					doneList.add(requiredName);
					if (!buildOnly(required, errors)) {
						return false;
					}
				}
			}
        }
        return true;
    }

    /**
     * Build a module but no antecedants.
     *
     * @param module
     *            the Module to build
     * @param errors
     *            the List sink for errors, if any
     * @return false after successful build, when module jar should exist
     */
    protected final boolean buildOnly(Result result, List<String> errors) {
        if (!result.outOfDate()) {
            return true;
        }
        if (isLogging()) {
            handler.log("building " + result);
        }
        if (!buildingEnabled) {
            return false;
        }
        if (result.getKind().assemble) {
            return assembleAll(result, handler);
        }
        Module module = result.getModule();
        final File classesDir;
        if (useEclipseCompiles) {
            classesDir = new File(module.moduleDir, "bin"); // FileLiteral
        } else {
            String name = "classes-" + System.currentTimeMillis();
            classesDir = new File(tempDir, name);
        }
        if (verbose) {
            handler.log("buildOnly " + module);
        }
        try {
            return (compile(result, classesDir,useEclipseCompiles, errors))
                    && assemble(result, classesDir, errors);
        } finally {
            if (!useEclipseCompiles && !Util.delete(classesDir)) {
                errors.add("buildOnly unable to delete " + classesDir);
            }
        }
    }

    /**
     * Register temporary file or directory to be deleted when the build is
     * complete, even if an Exception is thrown.
     */
    protected void addTempFile(File tempFile) {
        if (null != tempFile) {
            tempFiles.add(tempFile);
        }
    }

    /**
     * Build product by discovering any modules to build, building those,
     * assembling the product distribution, and optionally creating an installer
     * for it.
     *
     * @return true on success
     */
    protected final boolean buildProduct(BuildSpec buildSpec)
            throws BuildException {
        Util.iaxIfNull(buildSpec, "buildSpec");

        if (!buildSpec.trimTesting) {
            buildSpec.trimTesting = true;
            handler.log("testing trimmed for " + buildSpec);
        }
        Util.iaxIfNotCanReadDir(buildSpec.productDir, "productDir");
        Util.iaxIfNotCanReadDir(buildSpec.baseDir, "baseDir");
        Util.iaxIfNotCanWriteDir(buildSpec.distDir, "distDir");

        // ---- discover modules to build, and build them
        Modules modules = new Modules(buildSpec.baseDir, buildSpec.jarDir,
                handler);
        ProductModule[] productModules = discoverModules(buildSpec.productDir,
                modules);
		for (ProductModule module : productModules) {
			if (buildSpec.verbose) {
				handler.log("building product module " + module);
			}
			if (!buildProductModule(module)) {
				return false;
			}
		}
        if (buildSpec.verbose) {
            handler.log("assembling product module for " + buildSpec);
        }

        // ---- assemble product distribution
        final String productName = buildSpec.productDir.getName();
        final File targDir = new File(buildSpec.distDir, productName);
        final String targDirPath = targDir.getPath();
        if (targDir.canWrite()) {
            Util.deleteContents(targDir);
        }

        if (!targDir.canWrite() && !targDir.mkdirs()) {
            if (buildSpec.verbose) {
                handler.log("buildProduct unable to create " + targDir);
            }
            return false;
        }

        // copy non-binaries (with filter)
        File distDir = new File(buildSpec.productDir, "dist");
        if (!copyNonBinaries(buildSpec, distDir, targDir)) {
            return false;
        }

        // copy binaries (but not module flag files)
        String excludes = null;
        {
            StringBuffer buf = new StringBuffer();
			for (ProductModule productModule : productModules) {
				if (0 < buf.length()) {
					buf.append(",");
				}
				buf.append(productModule.relativePath);
			}
            if (0 < buf.length()) {
                excludes = buf.toString();
            }
        }

        if (!copyBinaries(buildSpec, distDir, targDir, excludes)) {
            return false;
        }

        // copy binaries associated with module flag files
		for (final ProductModule product : productModules) {
			final Kind kind = Result.kind(Result.NORMAL, product.assembleAll);
			Result result = product.module.getResult(kind);
			String targPath = Util.path(targDirPath, product.relativePath);
			File jarFile = result.getOutputFile();
			copyFile(jarFile, new File(targPath), FILTER_OFF);
		}
        handler.log("created product in " + targDir);

        // ---- create installer
        if (buildSpec.createInstaller) {
            return buildInstaller(buildSpec, targDirPath);
        } else {
            return true;
        }
    }

    protected boolean copyBinaries(BuildSpec buildSpec, File distDir,
            File targDir, String excludes) {
        String includes = Builder.BINARY_SOURCE_PATTERN;
        return copyFiles(distDir, targDir, includes, excludes, FILTER_OFF);
    }

    /**
     * filter-copy everything but the binaries
     */
    protected boolean copyNonBinaries(BuildSpec buildSpec, File distDir,
            File targDir) {
        String excludes = Builder.BINARY_SOURCE_PATTERN;
        String includes = Builder.ALL_PATTERN;
        return copyFiles(distDir, targDir, includes, excludes, FILTER_ON);
    }

    protected final boolean buildProductModule(ProductModule module) {
        List<String> errors = new ArrayList<>();
        try {
            Kind productKind = Result.kind(Result.NORMAL, Result.ASSEMBLE);
            Result result = module.module.getResult(productKind);
            return buildAll(result, errors);
        } finally {
			for (String error : errors) {
				handler.error("error building " + module + ": " + error);
			}
        }
    }

    /**
     * Discover any modules that might need to be built in order to assemble the
     * product distribution. This interprets empty .jar files as module
     * deliverables.
     */
    protected ProductModule[] discoverModules(File productDir, Modules modules) {
        final List<File> found = new ArrayList<>();
        FileFilter filter = new FileFilter() {// empty jar files
            public boolean accept(File file) {
                if ((null != file) && file.canRead()
                        && file.getPath().endsWith(".jar") // XXXFileLiteral
                        && (0l == file.length())) {
                    found.add(file);
                }
                return true;
            }
        };
        Util.visitFiles(productDir, filter);
        ArrayList<ProductModule> results = new ArrayList<>();
        for (File file: found) {
            String jarName = moduleAliasFor(file.getName().toLowerCase());
            if (jarName.endsWith(".jar") || jarName.endsWith(".zip")) { // XXXFileLiteral
                jarName = jarName.substring(0, jarName.length() - 4);
            } else {
                handler.log("can only replace .[jar|zip]: " + file);
                // XXX error?
            }
            boolean assembleAll = jarName.endsWith("-all");
            // XXXFileLiteral
            String name = (!assembleAll ? jarName : jarName.substring(0,
                    jarName.length() - 4));
            Module module = modules.getModule(name);
            if (null == module) {
                handler.log("unable to find module for " + file);
            } else {
                results.add(new ProductModule(productDir, file, module,
                        assembleAll));
            }
        }
        return results.toArray(new ProductModule[0]);
    }

    /**
     * Subclasses should query whether to include library files in the assembly.
     *
     * @param module
     *            the Module being built
     * @param libraries
     *            the List of File path to the jar to consider assembling
     * @return true if the jar should be included, false otherwise.
     */
    protected void removeLibraryFilesToSkip(Module module, List<File> libraries) {
        for (ListIterator<File> liter = libraries.listIterator(); liter.hasNext();) {
            File library = liter.next();
            final String fname = library.getName();
            if (null != fname) {
				for (String name : SKIP_LIBRARIES) {
					if (fname.equals(name)) {
						liter.remove();
						break;
					}
				}
            }
        }
    }

    /**
     * @return String[] names of results to build for this module
     */
    abstract protected Result[] getAntecedantResults(Result toBuild);

    /**
     * Compile module classes to classesDir, saving String errors.
     *
     * @param module
     *            the Module to compile
     * @param classesDir
     *            the File directory to compile to
     * @param useExistingClasses
     *            if true, don't recompile and ensure classes are available
     * @param errors
     *            the List to add error messages to
     */
    abstract protected boolean compile(Result result, File classesDir,
            boolean useExistingClasses, List<String> errors);

    /**
     * Assemble the module distribution from the classesDir, saving String
     * errors.
     *
     * @see #removeLibraryFilesToSkip(Module, File)
     */
    abstract protected boolean assemble(Result result, File classesDir,
            List<String> errors);

    /**
     * Assemble the module distribution from the classesDir and all
     * antecendants, saving String errors.
     *
     * @see #removeLibraryFilesToSkip(Module, File)
     */
    abstract protected boolean assembleAll(Result result, Messager handler);

    /**
     * Generate the installer for this product to targDirPath
     */
    abstract protected boolean buildInstaller(BuildSpec buildSpec,
            String targDirPath);

    /**
     * Copy fromFile to toFile, optionally filtering contents
     */
    abstract protected boolean copyFile(File fromFile, File toFile,
            boolean filter);

    /**
     * Copy toDir any fromDir included files without any exluded files,
     * optionally filtering contents.
     *
     * @param fromDir
     *            File dir to read from - error if not readable
     * @param toDir
     *            File dir to write to - error if not writable
     * @param included
     *            String Ant pattern of included files (if null, include all)
     * @param excluded
     *            String Ant pattern of excluded files (if null, exclude none)
     * @param filter
     *            if FILTER_ON, then filter file contents using global
     *            token/value pairs
     */
    abstract protected boolean copyFiles(File fromDir, File toDir,
            String included, String excluded, boolean filter);
}
