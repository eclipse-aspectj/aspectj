/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC           initial implementation 
 * ******************************************************************/

package org.aspectj.internal.tools.build;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.tools.ant.BuildException;

/**
 * Template class to build (eclipse) modules (and, weakly, products),
 * including any required modules.
 * When building modules, this assumes:
 * <ul>
 * <li>the name of the module is the base name of the module directory</li>
 * <li>all module directories are in the same base (workspace) directory</li>
 * <li>the name of the target module jar is {moduleName}.jar</li>
 * <li>a module directory contains a <code>.classpath</code> file with
 *     (currently line-parseable) entries per Eclipse (XML) conventions</li>
 * <li><code>Builder.RESOURCE_PATTERN</code> 
 *      identifies all resources to copy to output.</li>
 * <li>This can safely trim test-related code:
 *     <ul>
 *     <li>source directories named "testsrc"</li>
 *     <li>libraries named "junit.jar"</li>
 *     <li>required modules whose names start with "testing"</li>
 *     </ul>
 * <li>A file <code>{moduleDir}/{moduleName}.properties</code> 
 *     is a property file possibly
 *     containing entries defining requirements to be merged with the output jar
 *     (deprecated mechanism - use assembleAll or products)</li>
 * </ul>
 * This currently provides no control over the compile or assembly process, 
 * but clients can harvest <code>{moduleDir}/bin</code> directories to re-use 
 * the results of eclipse compiles.
 * <p>
 * When building products, this assumes:
 * <ul>
 * <li>the installer-resources directory is a peer of the products directory,
 *     itself the parent of the particular product directory.</li>
 * <li>the dist, jar, product, and base (module) directory are set</li>
 * <li>the product distribution consists of all (and only) the files 
 *     in the dist sub-directory of the product directory</li> 
 * <li>files in the dist sub-directory that are empty and end with .jar
 *     represent modules to build, either as named or through aliases
 *     known here.</li>
 * <li>When assembling the distribution, all non-binary files are to 
 *     be filtered.<li>
 * <li>the name of the product installer is aspectj-{productName}-{version}.jar, 
 *     where {productName} is the base name of the product directory</li>
 * </ul>
 * <p>
 * When run using main(String[]), all relevant Ant libraries and properties
 * must be defined.
 * <p>
 * Written to compile standalone.  Refactor if using utils, bridge, etc.
 */
public abstract class Builder {

	/**
	 *  This has only weak forms for build instructions needed:
	 * - resource pattern
	 * - compiler selection and control
	 * 
	 * Both assumed and generated paths are scattered;
	 * see XXXNameLiteral and XXXFileLiteral.
	 * 
	 * Builder is supposed to be thread-safe, but currently caches build
	 * properties to tunnel for filters. hmm.
	 */

	public static final String RESOURCE_PATTERN =
		"**/*.txt,**/*.rsc,**/*.gif,**/*.properties";

	public static final String BINARY_SOURCE_PATTERN =
		"**/*.rsc,**/*.gif,**/*.jar,**/*.zip";

	public static final String ALL_PATTERN = "**/*";

	/** enable copy filter semantics */
	protected static final boolean FILTER_ON = true;

	/** disable copy filter semantics */
	protected static final boolean FILTER_OFF = false;

	protected final Messager handler;
	protected boolean buildingEnabled;

	private final File tempDir;
	private final ArrayList tempFiles;
	private final boolean useEclipseCompiles;

	protected boolean verbose;

	protected Builder(
		File tempDir,
		boolean useEclipseCompiles,
		Messager handler) {
		Util.iaxIfNull(handler, "handler");
		this.useEclipseCompiles = useEclipseCompiles;
		this.handler = handler;
		this.tempFiles = new ArrayList();
		if ((null == tempDir)
			|| !tempDir.canWrite()
			|| !tempDir.isDirectory()) {
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

	public boolean build(BuildSpec buildSpec) {
		if (!buildingEnabled) {
			return false;
		}
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
					buildSpec.moduleDir =
						new File(buildSpec.baseDir, buildSpec.module);
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
					throw new BuildException(
						"no name, even from " + buildSpec.moduleDir);
				}
			}
		}

		if (null != buildSpec.productDir) {
			return buildProduct(buildSpec);
		}
		if (buildSpec.trimTesting
			&& (-1 != buildSpec.module.indexOf("testing"))) { // XXXNameLiteral
			String warning =
				"Warning - cannot trimTesting for testing modules: ";
			handler.log(warning + buildSpec.module);
		}
		Messager handler = new Messager();
		Modules modules =
			new Modules(
				buildSpec.baseDir,
				buildSpec.jarDir,
				buildSpec.trimTesting,
				handler);

		final Module moduleToBuild = modules.getModule(buildSpec.module);
		ArrayList errors = new ArrayList();
		try {
			return buildAll(
				moduleToBuild,
				errors,
				buildSpec.rebuild,
				buildSpec.assembleAll);
		} finally {
			if (0 < errors.size()) {
				String label = "error building " + buildSpec + ": ";
				for (Iterator iter = errors.iterator(); iter.hasNext();) {
					handler.error(label + iter.next());
				}
			}
		}
	}

	/**
	 * Clean up any temporary files, etc. after build completes
	 */
	public boolean cleanup() {
		boolean noErr = true;
		for (ListIterator iter = tempFiles.listIterator();
			iter.hasNext();
			) {
			File file = (File) iter.next();
			if (!Util.deleteContents(file) || !file.delete()) {
				if (noErr) {
					noErr = false;
				}
				handler.log("unable to clean up " + file);
			}
		}
		return noErr;
	}

	/**
	 * Build a module with all antecedants.
	 * @param module the Module to build
	 * @param errors the List sink for errors, if any
	 * @return false after successful build, when module jar should exist
	 */
	protected boolean buildAll(
		Module module,
		List errors,
		boolean rebuild,
		boolean assembleAll) {
		String[] buildList = getAntecedantModuleNames(module, rebuild);
		if ((null != buildList) && (0 < buildList.length)) {
			final Modules modules = module.getModules();
			final Messager handler = this.handler;
			final boolean log = (verbose && (null != handler));
			final boolean verbose = this.verbose;
			if (log) {
				handler.log(
					"modules to build: " + Arrays.asList(buildList));
			}
			for (int i = 0; i < buildList.length; i++) {

				if (!buildingEnabled) {
					return false;
				}
				String modName = buildList[i];
				if (log) {
					handler.log("building " + modName);
				}
				Module next = modules.getModule(modName);
				if (!buildOnly(next, errors)) {
					return false;
				}
			}
		}
		if (assembleAll && !assembleAll(module, handler)) {
			return false;
		}
		return true;
	}

	/**
	 * Build a module but no antecedants.
	 * @param module the Module to build
	 * @param errors the List sink for errors, if any
	 * @return false after successful build, when module jar should exist
	 */
	protected boolean buildOnly(Module module, List errors) {
		if (!buildingEnabled) {
			return false;
		}
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
			return (
				compile(module, classesDir, useEclipseCompiles, errors))
				&& assemble(module, classesDir, errors);
		} finally {
			if (!useEclipseCompiles && !Util.delete(classesDir)) {
				errors.add("buildOnly unable to delete " + classesDir);
			}
		}
	}

	/**
	 * Register temporary file or directory to be deleted when
	 * the build is complete, even if an Exception is thrown.
	 */
	protected void addTempFile(File tempFile) {
		if (null != tempFile) {
			tempFiles.add(tempFile);
		}
	}
	/**
	 * Build product by discovering any modules to build,
	 * building those, assembling the product distribution,
	 * and optionally creating an installer for it.
	 * @return true on success
	 */
	protected boolean buildProduct(BuildSpec buildSpec)
		throws BuildException {
		Util.iaxIfNull(buildSpec, "buildSpec");
		// XXX if installer and not out of date, do not rebuild unless rebuild set

		if (!buildSpec.trimTesting) {
			buildSpec.trimTesting = true;
			handler.log("testing trimmed for " + buildSpec);
		}
		Util.iaxIfNotCanReadDir(buildSpec.productDir, "productDir");
		Util.iaxIfNotCanReadDir(buildSpec.baseDir, "baseDir");
		Util.iaxIfNotCanWriteDir(buildSpec.distDir, "distDir");

		// ---- discover modules to build, and build them
		Modules modules =
			new Modules(
				buildSpec.baseDir,
				buildSpec.jarDir,
				buildSpec.trimTesting,
				handler);
		ProductModule[] productModules =
			discoverModules(buildSpec.productDir, modules);
		for (int i = 0; i < productModules.length; i++) {
			if (buildSpec.verbose) {
				handler.log(
					"building product module " + productModules[i]);
			}
			if (!buildProductModule(productModules[i])) {
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
		// filter-copy everything but the binaries
		File distDir = new File(buildSpec.productDir, "dist");
		// XXXFileLiteral
		String excludes = Builder.BINARY_SOURCE_PATTERN;
		String includes = Builder.ALL_PATTERN;
		if (!copyFiles(distDir, targDir, includes, excludes, FILTER_ON)) {
			return false;
		}

		// copy binaries (but not module flag files)       
		excludes = null;
		{
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < productModules.length; i++) {
				if (0 < buf.length()) {
					buf.append(",");
				}
				buf.append(productModules[i].relativePath);
			}
			if (0 < buf.length()) {
				excludes = buf.toString();
			}
		}
		includes = Builder.BINARY_SOURCE_PATTERN;
		if (!copyFiles(distDir,
			targDir,
			includes,
			excludes,
			FILTER_OFF)) {
			return false;
		}

		// copy binaries associated with module flag files
		for (int i = 0; i < productModules.length; i++) {
			ProductModule product = productModules[i];
			String targPath = targDirPath + "/" + product.relativePath;
			File jarFile =
				(product.assembleAll
					? product.module.getAssembledJar()
					: product.module.getModuleJar());
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

	protected boolean buildProductModule(ProductModule module) {
		boolean noRebuild = false;
		ArrayList errors = new ArrayList();
		try {
			return buildAll(
				module.module,
				errors,
				noRebuild,
				module.assembleAll);
		} finally {
			for (Iterator iter = errors.iterator(); iter.hasNext();) {
				handler.error(
					"error building " + module + ": " + iter.next());
			}
		}
	}

	/**
	 * Discover any modules that might need to be built
	 * in order to assemble the product distribution.
	 * This interprets empty .jar files as module deliverables.
	 */
	protected ProductModule[] discoverModules(
		File productDir,
		Modules modules) {
		final ArrayList found = new ArrayList();
			FileFilter filter = new FileFilter() {// empty jar files
	public boolean accept(File file) {
					if ((null != file)
						&& file.canRead()
						&& file.getPath().endsWith(
							".jar") // XXXFileLiteral
						&& (0l == file.length())) {
					found.add(file);
				}
				return true;
			}
		};
		Util.visitFiles(productDir, filter);
		ArrayList results = new ArrayList();
		for (Iterator iter = found.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			String jarName = moduleAliasFor(file.getName().toLowerCase());
			if (jarName.endsWith(".jar")
				|| jarName.endsWith(".zip")) { // XXXFileLiteral
				jarName = jarName.substring(0, jarName.length() - 4);
			} else {
				handler.log("can only replace .[jar|zip]: " + file);
				// XXX error?
			}
			boolean assembleAll = jarName.endsWith("-all");
			// XXXFileLiteral
			String name =
				(!assembleAll
					? jarName
					: jarName.substring(0, jarName.length() - 4));
			Module module = modules.getModule(name);
			if (null == module) {
				handler.log("unable to find module for " + file);
			} else {
				results.add(
					new ProductModule(
						productDir,
						file,
						module,
						assembleAll));
			}
		}
		return (ProductModule[]) results.toArray(new ProductModule[0]);
	}

	/**
	 * Map delivered-jar name to created-module name
	 * @param jarName the String (lowercased) of the jar/zip to map
	 */
	protected String moduleAliasFor(String jarName) {
		if ("aspectjtools.jar".equals(jarName)) { // XXXFileLiteral
			return "ajbrowser-all.jar";
		} else if ("aspectjrt.jar".equals(jarName)) {
			return "runtime.jar";
		} else {
			return jarName;
		}
	}

	/** 
	 * @return String[] names of modules to build for this module
	 */
	abstract protected String[] getAntecedantModuleNames(
		Module toBuild,
		boolean rebuild);

	/**
	 * Compile module classes to classesDir, saving String errors.
     * @param module the Module to compile
     * @param classesDir the File directory to compile to
     * @param useExistingClasses if true, don't recompile
     *        and ensure classes are available
     * @param errors the List to add error messages to
	 */
	abstract protected boolean compile(
		Module module,
		File classesDir,
        boolean useExistingClasses,
		List errors);

	/**
	 * Assemble the module distribution from the classesDir, saving String errors.
	 */
	abstract protected boolean assemble(
		Module module,
		File classesDir,
		List errors);

	/**
	 * Assemble the module distribution from the classesDir and all antecendants, 
	 * saving String errors.
	 */
	abstract protected boolean assembleAll(
		Module module,
		Messager handler);

	/**
	 * Generate the installer for this product to targDirPath
	 */
	abstract protected boolean buildInstaller(
		BuildSpec buildSpec,
		String targDirPath);

	/** 
	 * Copy fromFile to toFile, optionally filtering contents
	 */
	abstract protected boolean copyFile(
		File fromFile,
		File toFile,
		boolean filter);

	/** 
	 * Copy toDir any fromDir included files without any exluded files, 
	 * optionally filtering contents.
	 * @param fromDir File dir to read from - error if not readable
	 * @param toDir File dir to write to - error if not writable
	 * @param included String Ant pattern of included files (if null, include all)
	 * @param excluded String Ant pattern of excluded files (if null, exclude none)
	 * @param filter if FILTER_ON, then filter file contents using global token/value pairs
	 */
	abstract protected boolean copyFiles(
		File fromDir,
		File toDir,
		String included,
		String excluded,
		boolean filter);
}
