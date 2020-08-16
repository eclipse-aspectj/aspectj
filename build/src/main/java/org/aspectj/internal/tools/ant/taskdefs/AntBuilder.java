/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/
package org.aspectj.internal.tools.ant.taskdefs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;
import org.aspectj.internal.tools.build.BuildSpec;
import org.aspectj.internal.tools.build.Builder;
import org.aspectj.internal.tools.build.Messager;
import org.aspectj.internal.tools.build.Module;
import org.aspectj.internal.tools.build.Result;
import org.aspectj.internal.tools.build.Util;

/**
 * Implement Builder in Ant.
 */
public class AntBuilder extends Builder {
	private static final boolean FORCE_FORK_FOR_LIBRARIES = false;

	/**
	 * Factory for a Builder.
	 *
	 * @param config the String configuration, where only substrings "verbose" and "useEclipseCompiles" are significant
	 * @param project the owning Project for all tasks (not null)
	 * @param tempDir the File path to a temporary dir for side effects (may be null)
	 * @return a Builder for this project and configuration
	 */
	public static Builder getBuilder(String config, Project project, File tempDir) {
		boolean useEclipseCompiles = false;
		boolean verbose = false;
		if (null != config) {
			if (config.contains("useEclipseCompiles")) {
				useEclipseCompiles = true;
			}
			if (config.contains("verbose")) {
				verbose = true;
			}
		}
		// Messager handler = new Messager(); // debugging
		Messager handler = new ProjectMessager(project);
		Builder result = new ProductBuilder(project, tempDir, useEclipseCompiles, handler);
		if (verbose) {
			result.setVerbose(true);
		}
		return result;
	}

	private static String resultToTargetName(Result result) {
		return result.getName();
	}

	/**
	 * Ensure targets exist for this module and all antecedants, so topoSort can work.
	 */
	private static void makeTargetsForResult(final Result result, final Hashtable<String,Target> targets) {
		final String resultTargetName = resultToTargetName(result);
		Target target = targets.get(resultTargetName);
		if (null == target) {
			// first add the target
			target = new Target();
			target.setName(resultTargetName);

			Result[] reqs = result.getRequired();
			StringBuffer depends = new StringBuffer();
			boolean first = true;
			for (Result reqResult : reqs) {
				if (!first) {
					depends.append(",");
				} else {
					first = false;
				}
				depends.append(resultToTargetName(reqResult));
			}
			if (0 < depends.length()) {
				target.setDepends(depends.toString());
			}
			targets.put(resultTargetName, target);

			// then recursively add any required results
			for (Result reqResult : reqs) {
				makeTargetsForResult(reqResult, targets);
			}
		}
	}

	private final Project project;

	protected AntBuilder(Project project, File tempDir, boolean useEclipseCompiles, Messager handler) {
		super(tempDir, useEclipseCompiles, handler);
		this.project = project;
		Util.iaxIfNull(project, "project");
	}

	/**
	 * Initialize task with project and "ajbuild-" + name as name. (Using bm- prefix distinguishes these tasks from tasks found in
	 * the build script.)
	 *
	 * @param task the Task to initialize - not null
	 * @param name the String name suffix for the task
	 * @return true unless some error
	 */
	protected boolean setupTask(Task task, String name) {
		task.setProject(project);
		task.setTaskName("ajbuild-" + name);
		return true;
	}

	/**
	 * Copy file, optionally filtering. (Filters set in project.)
	 *
	 * @param fromFile the readable File source to copy
	 * @param toFile the writable File destination file
	 * @param boolean filter if true, enable filtering
	 * @see org.aspectj.internal.tools.build.Builder#copyFile(File, File, boolean)
	 */
	@Override
	protected boolean copyFile(File fromFile, File toFile, boolean filter) {
		Copy copy = makeCopyTask(filter);
		copy.setFile(fromFile);
		copy.setTofile(toFile);
		executeTask(copy);
		return true;
	}

	/**
	 * (Filters set in project.)
	 *
	 * @see org.aspectj.internal.tools.ant.taskdefs.Builder#copyFiles(File, File, String, String, boolean)
	 */
	@Override
	protected boolean copyFiles(File fromDir, File toDir, String includes, String excludes, boolean filter) {
		Copy copy = makeCopyTask(filter);
		copy.setTodir(toDir);
		FileSet fileset = new FileSet();
		fileset.setDir(fromDir);
		if (null != includes) {
			fileset.setIncludes(includes);
		}
		if (null != excludes) {
			fileset.setExcludes(excludes);
		}
		copy.addFileset(fileset);
		executeTask(copy);

		return false;
	}

	protected void copyFileset(File toDir, FileSet fileSet, boolean filter) {
		Copy copy = makeCopyTask(filter);
		copy.addFileset(fileSet);
		copy.setTodir(toDir);
		executeTask(copy);
	}

	/**
	 * @param filter if FILTER_ON, use filters
	 */
	protected Copy makeCopyTask(boolean filter) {
		Copy copy = new Copy();
		setupTask(copy, "copy");
		if (FILTER_ON == filter) {
			copy.setFiltering(true);
		}
		return copy;
	}

	protected void dumpMinFile(Result result, File classesDir, List<String> errors) {
		String name = result.getName() + "-empty";
		File minFile = new File(classesDir, name);
		FileWriter fw = null;
		try {
			fw = new FileWriter(minFile);
			fw.write(name);
		} catch (IOException e) {
			errors.add("IOException writing " + name + " to " + minFile + ": " + Util.renderException(e));
		} finally {
			Util.close(fw);
		}

	}

	@Override
	protected boolean compile(Result result, File classesDir, boolean useExistingClasses, List<String> errors) {
		if (!classesDir.exists() && !classesDir.mkdirs()) {
			errors.add("compile - unable to create " + classesDir);
			return false;
		}
		if (useExistingClasses) {
			return true;
		}
		// -- source paths
		Path path = new Path(project);
		boolean hasSourceDirectories = false;
		boolean isJava5Compile = false;
		boolean isJava8Compile = false;
		for (File file: result.getSrcDirs()) {
			path.createPathElement().setLocation(file);
			if (!isJava5Compile
					&& (Util.Constants.JAVA5_SRC.equals(file.getName()) ||
						Util.Constants.JAVA5_TESTSRC.equals(file.getName()) ||
						new File(file.getParent(), ".isJava5").exists())) {
				isJava5Compile = true;
			}
			if (new File(file.getParent(),".isJava8").exists()) {
				isJava8Compile = true;
			}
			if (!hasSourceDirectories) {
				hasSourceDirectories = true;
			}
		}
		if (!hasSourceDirectories) {
			return true; // nothing to compile - ok
		}
		// XXX test whether build.compiler property takes effect automatically
		// I suspect it requires the proper adapter setup.
		Javac javac = new Javac();
		setupTask(javac, "javac");
		javac.setIncludeantruntime(false);
		javac.setDestdir(classesDir);
		javac.setSrcdir(path);
		javac.setVerbose(verbose);
		path = null;

		// -- classpath
		Path classpath = new Path(project);
		boolean hasLibraries = setupClasspath(result, classpath);
		if (hasLibraries && FORCE_FORK_FOR_LIBRARIES) {
			javac.setFork(true); // otherwise never releases library jars
			// can we build under 1.4, but fork javac 1.5 compile?
		}
		// also fork if using 1.5?

		// -- set output directory
		classpath.createPathElement().setLocation(classesDir);
		javac.setClasspath(classpath);

		// misc
		javac.setDebug(true);
		if (isJava8Compile) {
			javac.setSource("1.8");
			javac.setTarget("1.8");
		} else if (isJava5Compile) {
			// *cough*
			javac.setSource("1.6");
			javac.setTarget("1.6");
		} else {
			javac.setTarget("1.1"); // 1.1 class files - Javac in 1.4 uses 1.4
			javac.setSource("1.3");
		}
		// compile
		boolean passed = false;
		BuildException failure = null;
		try {
			passed = executeTask(AspectJSupport.wrapIfNeeded(result, javac));
		} catch (BuildException e) {
			failure = e;
		} catch (Error e) {
			failure = new BuildException(e);
		} catch (RuntimeException e) {
			failure = new BuildException(e);
		} finally {
			if (!passed) {
				String args = "" + Arrays.asList(javac.getCurrentCompilerArgs());
				if ("[]".equals(args)) {
					args = "{" + result.toLongString() + "}";
				}
				String m = "BuildException compiling " + result.toLongString() + args
						+ (null == failure ? "" : ": " + Util.renderException(failure));
				// debuglog System.err.println(m);
				errors.add(m);
			}
			javac.init(); // be nice to let go of classpath libraries...
		}
		return passed;
	}

	public boolean setupClasspath(Result result, Path classpath) { // XXX fix test access
		boolean hasLibraries = false;
		// required libraries
		for (File file : result.getLibJars()) {
			classpath.createPathElement().setLocation(file);
			if (!hasLibraries) {
				hasLibraries = true;
			}
		}
		// Westodo Kind kind = result.getKind();
		Result[] reqs = result.getRequired();
		// required modules and their exported libraries
		for (Result requiredResult : reqs) {
			classpath.createPathElement().setLocation(requiredResult.getOutputFile());
			if (!hasLibraries) {
				hasLibraries = true;
			}
			// also put on classpath libraries exported from required module
			// XXX exported modules not supported
			for (File file : requiredResult.getExportedLibJars()) {
				classpath.createPathElement().setLocation(file);
			}
		}
		return hasLibraries;
	}

	/**
	 * Merge classes directory and any merge jars into module jar with any specified manifest file. META-INF directories are
	 * excluded.
	 */
	@Override
	protected boolean assemble(Result result, File classesDir, List<String> errors) {
		if (!buildingEnabled) {
			return false;
		}
		if (!result.outOfDate()) {
			return true;
		}

		// ---- zip result up
		Zip zip = new Zip();
		setupTask(zip, "zip");
		zip.setDestFile(result.getOutputFile());
		ZipFileSet zipfileset = null;

		// -- merge any resources in any of the src directories
		//for (Iterator iter = result.getSrcDirs().iterator(); iter.hasNext();) {
		//	File srcDir = (File) iter.next();
		for (File srcDir: result.getSrcDirs()) {
			zipfileset = new ZipFileSet();
			zipfileset.setProject(project);
			zipfileset.setDir(srcDir);
			zipfileset.setIncludes(RESOURCE_PATTERN);
			zip.addZipfileset(zipfileset);
		}

		final Module module = result.getModule();

		File metaInfDir = new File(classesDir, "META-INF");
		Util.deleteContents(metaInfDir);

		// -- manifest
		File manifest = new File(module.moduleDir, module.name + ".mf.txt"); // XXXFileLiteral
		if (Util.canReadFile(manifest)) {
			if (Util.canReadDir(metaInfDir) || metaInfDir.mkdirs()) {
				// Jar spec requires a MANIFEST.MF not a manifest.mf
				copyFile(manifest, new File(metaInfDir, "MANIFEST.MF"), FILTER_ON); // XXXFileLiteral
			} else {
				errors.add("have manifest, but unable to create " + metaInfDir);
				return false;
			}
		}

		zipfileset = new ZipFileSet();
		zipfileset.setProject(project);
		zipfileset.setDir(classesDir);
		zipfileset.setIncludes("**/*");
		zip.addZipfileset(zipfileset);
		File[] contents = classesDir.listFiles();
		if ((null == contents) || (0 == contents.length)) {
			// *something* to zip up
			dumpMinFile(result, classesDir, errors);
		}

		try {
			handler.log("assemble " + module + " in " + result.getOutputFile());
			return executeTask(zip)
			// zip returns true when it doesn't create zipfile
					// because there are no entries to add, so verify done
					&& Util.canReadFile(result.getOutputFile());
		} catch (BuildException e) {
			errors.add("BuildException zipping " + module + ": " + e.getMessage());
			return false;
		} finally {
			result.clearOutOfDate();
		}
	}

	/**
	 * @see org.aspectj.internal.tools.build.Builder#buildAntecedants(Module)
	 */
	@Override
	protected Result[] getAntecedantResults(Result moduleResult) {
		Hashtable<String,Target> targets = new Hashtable<>();
		makeTargetsForResult(moduleResult, targets);
		String targetName = resultToTargetName(moduleResult);
		// bug: doc says topoSort returns String, but returns Target
		Collection<Target> result = project.topoSort(targetName, targets);
		// fyi, we don't rely on topoSort to detect cycles - see buildAll
		int size = result.size();
		if (0 == result.size()) {
			return new Result[0];
		}
		ArrayList<String> toReturn = new ArrayList<>();
		for (Target target : result) {
			String name = target.getName();
			if (null == name) {
				throw new Error("null name?");
			} else {
				toReturn.add(name);
			}
		}
		// topoSort always returns target name
		if ((1 == size) && targetName.equals(toReturn.get(0)) && !moduleResult.outOfDate()) {
			return new Result[0];
		}
		return Result.getResults(toReturn.toArray(new String[0]));
	}

	/**
	 * Generate Module.assembledJar with merge of itself and all antecedants
	 */
	@Override
	protected boolean assembleAll(Result result, Messager handler) {
		if (!buildingEnabled) {
			return false;
		}
		if (!result.outOfDate()) {
			return true;
		}

		Util.iaxIfNull(result, "result");
		Util.iaxIfNull(handler, "handler");
		if (!result.getKind().isAssembly()) {
			throw new IllegalStateException("not assembly: " + result);
		}

		// ---- zip result up
		Zip zip = new Zip();
		setupTask(zip, "zip");
		zip.setDestFile(result.getOutputFile());
		ZipFileSet zipfileset = null;
		final Module module = result.getModule();
		List<File> known = result.findJarRequirements();
		removeLibraryFilesToSkip(module, known);
		// -- merge any antecedents, less any manifest
		for (File jarFile: known) {
			zipfileset = new ZipFileSet();
			zipfileset.setProject(project);
			zipfileset.setSrc(jarFile);
			zipfileset.setIncludes("**/*");
			String name = jarFile.getName();
			name = name.substring(0, name.length() - 4); // ".jar".length()
			// required includes self - exclude manifest from others
			if (!module.name.equals(name)) {
				zipfileset.setExcludes("META-INF/MANIFEST.MF"); // XXXFileLiteral
				zipfileset.setExcludes("META-INF/manifest.mf");
				zipfileset.setExcludes("meta-inf/manifest.mf");
				zipfileset.setExcludes("meta-inf/MANIFEST.MF");
			}
			zip.addZipfileset(zipfileset);
		}

		try {
			handler.log("assembling all " + module + " in " + result.getOutputFile());
			if (verbose) {
				handler.log("knownAntecedants: " + known);
			}
			return executeTask(zip);
		} catch (BuildException e) {
			handler.logException("BuildException zipping " + module, e);
			return false;
		} finally {
			result.clearOutOfDate();
		}
	}

	/**
	 * @see org.aspectj.internal.tools.ant.taskdefs.Builder#buildInstaller(BuildSpec, String)
	 */
	@Override
	protected boolean buildInstaller(BuildSpec buildSpec, String targDirPath) {
		return false;
	}

	/** task.execute() and any advice */
	protected boolean executeTask(Task task) {
		if (!buildingEnabled) {
			return false;
		}
		task.execute();
		return true;
	}

	/**
	 * Support for compiling basic AspectJ projects. Projects may only compile all (and only) their source directories; aspectpath,
	 * inpath, etc. are not supported. To load the compiler, this assumes the user has either defined a project property
	 * "aspectj.home" or that there exists <code>{module-dir}/lib/aspectj/lib/aspectj[tools|rt].jar</code>.
	 */
	static class AspectJSupport {
		static final String AJCTASK = "org.aspectj.tools.ant.taskdefs.AjcTask";
		static final String ASPECTJRT_JAR_VARIABLE = "ASPECTJRT_LIB";
		static final String LIBASPECTJ_RPATH = "/lib/aspectj";
		static final Map nameToAspectjrtjar = new HashMap();
		static final String NONE = "NONE";

		/**
		 * If this module should be compiled with AspectJ, return a task to do so.
		 *
		 * @param module the Module to compile
		 * @param javac the Javac compile commands
		 * @return javac or a Task to compile with AspectJ if needed
		 */
		static Task wrapIfNeeded(Result result, Javac javac) {
			final Project project = javac.getProject();
			Path runtimeJar = null;
			final Module module = result.getModule();
			if (runtimeJarOnClasspath(result)) {
				// yes aspectjrt.jar on classpath
			} else if (result.getClasspathVariables().contains(ASPECTJRT_JAR_VARIABLE)) {
				// yes, in variables - find aspectjrt.jar to add to classpath
				runtimeJar = getAspectJLib(project, module, "aspectjrt.jar");
			} else {
				// no
				// System.out.println("javac " + result + " " + javac.getClasspath());
				return javac;
			}
			// System.out.println("aspectj " + result + " " + javac.getClasspath());
			Path aspectjtoolsJar = getAspectJLib(project, module, "aspectjtools.jar");
			return aspectJTask(javac, aspectjtoolsJar, runtimeJar);
		}

		/** @return true if aspectjrt.jar is on classpath */
		private static boolean runtimeJarOnClasspath(Result result) {
			for (File file: result.getLibJars()) {
				if ("aspectjrt.jar".equals(file.getName())) {
					return true;
				}
			}
			return false;
		}

		static Path getAspectJLib(Project project, Module module, String name) {
			Path result = null;
			String[] libDirNames = { "aspectj.home", "ASPECTJ_HOME", LIBASPECTJ_RPATH };
			String[] libDirs = new String[libDirNames.length];
			for (int i = 0; i < libDirNames.length; i++) {
				if (LIBASPECTJ_RPATH == libDirNames[i]) {
					libDirs[i] = module.getFullPath(LIBASPECTJ_RPATH);
				} else {
					libDirs[i] = project.getProperty(libDirNames[i]);
				}
				if (null != libDirs[i]) {
					libDirs[i] = Util.path(libDirs[i], "lib");
					result = new Path(project, Util.path(libDirs[i], name));
					String path = result.toString();
					if (new File(path).canRead()) {
						return result;
					}
				}
			}
			String m = "unable to find " + name + " in " + Arrays.asList(libDirs);
			throw new BuildException(m);
		}

		/**
		 * Wrap AspectJ compiler as Task. Only works for javac-like source compilation of everything under srcDir. Written
		 * reflectively to compile in the build module, which can't depend on the whole tree.
		 *
		 * @param javac the Javac specification
		 * @param toolsJar the Path to the aspectjtools.jar
		 * @param runtimeJar the Path to the aspectjrt.jar
		 * @return javac or another Task invoking the AspectJ compiler
		 */
		@SuppressWarnings("unchecked")
		static Task aspectJTask(Javac javac, Path toolsJar, Path runtimeJar) {
			Object task = null;
			String url = null;
			try {
				url = "file:" + toolsJar.toString().replace('\\', '/');
				URL[] cp = new URL[] { new URL(url) };
				ClassLoader parent = Task.class.getClassLoader();
				ClassLoader loader = new URLClassLoader(cp, parent);
				Class c = loader.loadClass(AJCTASK);
				task = c.newInstance();
				// Westodo Project project = javac.getProject();
				Method m = c.getMethod("setupAjc", new Class[] { Javac.class });
				m.invoke(task, new Object[] { javac });
				m = c.getMethod("setFork", new Class[] { boolean.class });
				m.invoke(task, new Object[] { Boolean.TRUE });
				m = c.getMethod("setForkclasspath", new Class[] { Path.class });
				m.invoke(task, new Object[] { toolsJar });
				m = c.getMethod("setSourceRoots", new Class[] { Path.class });
				m.invoke(task, new Object[] { javac.getSrcdir() });
				if (null != runtimeJar) {
					m = c.getMethod("setClasspath", new Class[] { Path.class });
					m.invoke(task, new Object[] { runtimeJar });
				}
			} catch (BuildException e) {
				throw e;
			} catch (Throwable t) {
				StringBuffer sb = new StringBuffer();
				sb.append("classpath=");
				sb.append(url);
				throw new BuildException(sb.toString(), t);
			}
			return (Task) task;
		}

		private AspectJSupport() {
			throw new Error("no instances");
		}
	}
}

// finally caught by failing to comply with proper ant initialization
// /**
// * Build a module that has a build script.
// * @param buildSpec the module to build
// * @param buildScript the script file
// * @throws BuildException if build fails
// */
// private void buildByScript(BuildSpec buildSpec, File buildScript)
// throws BuildException {
// Ant ant = new Ant();
// ant.setProject(getProject());
// ant.setAntfile(buildScript.getAbsolutePath());
// ant.setDescription("building module " + buildSpec.module);
// ant.setDir(buildScript.getParentFile());
// ant.setInheritAll(true);
// ant.setInheritRefs(false);
// ant.setLocation(getLocation());
// ant.setOwningTarget(getOwningTarget());
// // by convention, for build.xml, use module name to publish
// ant.setTarget(buildSpec.module);
// ant.setTaskName("ant");
// loadAntProperties(ant, buildSpec);
// ant.execute();
// }
//
// /** override definitions */
// private void loadAntProperties(Ant ant, BuildSpec buildSpec) {
// Property property = ant.createProperty();
// property.setName(BuildSpec.baseDir_NAME);
// property.setFile(buildSpec.baseDir);
// property = ant.createProperty();
// property.setName(buildSpec.distDir_NAME);
// property.setFile(buildSpec.distDir);
// property = ant.createProperty();
// property.setName(BuildSpec.tempDir_NAME);
// property.setFile(buildSpec.tempDir);
// property = ant.createProperty();
// property.setName(BuildSpec.jarDir_NAME);
// property.setFile(buildSpec.jarDir);
// property = ant.createProperty();
// property.setName(BuildSpec.stagingDir_NAME);
// property.setFile(buildSpec.stagingDir);
// }

/**
 * Segregate product-building API's from module-building APIs for clarity. These are called by the superclass if the BuildSpec
 * warrants. XXX extremely brittle/arbitrary assumptions.
 *
 * @see BuildModule for assumptions
 */
class ProductBuilder extends AntBuilder {

	private static String getProductInstallResourcesSrc(BuildSpec buildSpec) {
		final String resourcesName = "installer-resources"; // XXXFileLiteral
		File dir = buildSpec.productDir.getParentFile();
		if (null == dir) {
			return Util.path(new String[] { "..", "..", resourcesName });
		}
		dir = dir.getParentFile();
		if (null == dir) {
			return Util.path("..", resourcesName);
		} else {
			dir = new File(dir, resourcesName);
			return dir.getPath();
		}
	}

	private static String getProductInstallerFileName(BuildSpec buildSpec) { // XXXFileLiteral
		return "aspectj-" + buildSpec.productDir.getName() + "-" + Util.shortVersion(buildSpec.version) + ".jar";
	}

	/**
	 * Calculate name of main, typically InitialCap, and hence installer class.
	 *
	 * @return $$installer$$.org.aspectj." + ProductName + "Installer"
	 */

	private static String getProductInstallerMainClass(BuildSpec buildSpec) {
		String productName = buildSpec.productDir.getName();
		String initial = productName.substring(0, 1).toUpperCase();
		productName = initial + productName.substring(1);
		return "$installer$.org.aspectj." + productName + "Installer"; // XXXNameLiteral
	}

	/** @see Builder.getBuilder(String, Project, File) */
	ProductBuilder(Project project, File tempDir, boolean useEclipseCompiles, Messager handler) {
		super(project, tempDir, useEclipseCompiles, handler);
	}

	/**
	 * Delegate for super.buildProduct(..) template method.
	 */
	@Override
	protected boolean copyBinaries(BuildSpec buildSpec, File distDir, File targDir, String excludes) {
		Copy copy = makeCopyTask(false);
		copy.setTodir(targDir);
		FileSet fileset = new FileSet();
		fileset.setDir(distDir);
		fileset.setIncludes(Builder.BINARY_SOURCE_PATTERN);
		if (null != excludes) {
			fileset.setExcludes(excludes);
		}
		copy.addFileset(fileset);
		return executeTask(copy);
	}

	/**
	 * Delegate for super.buildProduct(..) template method.
	 */
	@Override
	protected boolean copyNonBinaries(BuildSpec buildSpec, File distDir, File targDir) {
		// filter-copy everything but the binaries
		Copy copy = makeCopyTask(true);
		copy.setTodir(targDir);
		Util.iaxIfNotCanReadDir(distDir, "product dist directory");
		FileSet fileset = new FileSet();
		fileset.setDir(distDir);
		fileset.setExcludes(Builder.BINARY_SOURCE_PATTERN);
		copy.addFileset(fileset);
		return executeTask(copy);
	}

	@Override
	protected boolean buildInstaller(BuildSpec buildSpec, String targDirPath) {
		if (buildSpec.verbose) {
			handler.log("creating installer for " + buildSpec);
		}
		AJInstaller installer = new AJInstaller();
		setupTask(installer, "installer");
		installer.setBasedir(targDirPath);
		// installer.setCompress();
		File installSrcDir = new File(buildSpec.productDir, "install"); // XXXFileLiteral
		Util.iaxIfNotCanReadDir(installSrcDir, "installSrcDir");
		installer.setHtmlSrc(installSrcDir.getPath());
		String resourcePath = getProductInstallResourcesSrc(buildSpec);
		File resourceSrcDir = new File(resourcePath);
		Util.iaxIfNotCanReadDir(resourceSrcDir, "resourceSrcDir");
		installer.setResourcesSrc(resourcePath);
		String name = getProductInstallerFileName(buildSpec);
		File outFile = new File(buildSpec.jarDir, name);
		installer.setZipfile(outFile.getPath());
		installer.setMainclass(getProductInstallerMainClass(buildSpec));
		installer.setInstallerclassjar(getBuildJar(buildSpec));
		return executeTask(installer);

		// -- test installer XXX
		// create text setup file
		// run installer with setup file
		// cleanup installed product
	}

	private String getBuildJar(BuildSpec buildSpec) {
		return buildSpec.baseDir.getPath() + "/lib/build/build.jar"; // XXX
	}

	// private Module moduleForReplaceFile(File replaceFile, Modules modules) {
	// String jarName = moduleAliasFor(replaceFile.getName().toLowerCase());
	// if (jarName.endsWith(".jar") || jarName.endsWith(".zip")) { // XXXFileLiteral
	// jarName = jarName.substring(0, jarName.length()-4);
	// } else {
	// throw new IllegalArgumentException("can only replace .[jar|zip]");
	// }
	// boolean assembleAll = jarName.endsWith("-all");
	// String name = (!assembleAll ? jarName : jarName.substring(0, jarName.length()-4));
	// return modules.getModule(name);
	// }
	//
}

class ProjectMessager extends Messager {
	private final Project project;

	public ProjectMessager(Project project) {
		Util.iaxIfNull(project, "project");
		this.project = project;
	}

	@Override
	public boolean log(String s) {
		project.log(s);
		return true;
	}

	@Override
	public boolean error(String s) {
		project.log(s, Project.MSG_ERR);
		return true;
	}

	@Override
	public boolean logException(String context, Throwable thrown) {
		project.log(context + Util.renderException(thrown), Project.MSG_ERR);
		return true;
	}

}