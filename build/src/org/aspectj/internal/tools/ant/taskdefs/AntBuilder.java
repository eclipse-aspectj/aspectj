/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.internal.tools.ant.taskdefs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

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
import org.aspectj.internal.tools.build.Modules;
import org.aspectj.internal.tools.build.ProductModule;
import org.aspectj.internal.tools.build.Util;

/**
 * Implement Builder in Ant.
 */
public class AntBuilder extends Builder {
    /*
     * XXX This just constructs and uses Ant Task objects, 
     * which in some cases causes the tasks to fail.
     */

    /**
     * Factory for a Builder.
     * @param config the String configuration, where only substrings
     *        "verbose" and "useEclipseCompiles" are significant
     * @param project the owning Project for all tasks (not null)
     * @param tempDir the File path to a temporary dir for side effects (may be null)
     * @return a Builder for this project and configuration 
     */
    public static Builder getBuilder(String config, Project project, File tempDir) {
        boolean useEclipseCompiles = false;
        boolean verbose = false;        
        if (null != config) {
            if (-1 != config.indexOf("useEclipseCompiles")) {
                useEclipseCompiles = true;
            }
            if (-1 != config.indexOf("verbose")) {
                verbose = true;
            }
        }
        Messager handler = new ProjectMessager(project);
        Builder result = new ProductBuilder(project, tempDir, useEclipseCompiles, handler);
        if (verbose) {
            result.setVerbose(true);
        }
        return result;
    }
    
    /** 
     * Make and register target for this module and antecedants.
     * This ensures that the (direct) depends list is generated
     * for each target.
     * This depends on topoSort to detect cycles. XXX unverified
     */
    private static void makeTargetsForModule(
        final Module module, 
        final Hashtable targets, 
        final boolean rebuild) {
        Target target = (Target) targets.get(module.name);
        if (null == target) {
            // first add the target
            target = new Target();
            target.setName(module.name);
            List req = module.getRequired();
            StringBuffer depends = new StringBuffer();
            boolean first = true;
            for (Iterator iterator = req.iterator(); iterator.hasNext();) {
                Module reqModule = (Module) iterator.next();
                if (rebuild || reqModule.outOfDate(false)) {
                    if (!first) {
                        depends.append(",");
                    } else {
                        first = false;
                    }
                    depends.append(reqModule.name);
                }
            }
            if (0 < depends.length()) {
                target.setDepends(depends.toString());
            }
            targets.put(module.name, target);

            // then recursively add any required modules
            for (Iterator iterator = module.getRequired().iterator();
                iterator.hasNext();
                ) {
                Module reqModule = (Module) iterator.next();
                if (rebuild || reqModule.outOfDate(false)) {
                    makeTargetsForModule(reqModule, targets, rebuild);
                }                
            }                        
        }
    }

    private final Project project; // XXX s.b. used only in setupTask

    protected AntBuilder(Project project, File tempDir, boolean useEclipseCompiles,
        Messager handler) {
        super(tempDir, useEclipseCompiles, handler);
        this.project = project;
        Util.iaxIfNull(project, "project");
    }
    
    /**
     * Initialize task with project and "ajbuild-" + name as name. (Using bm-
     * prefix distinguishes these tasks from tasks found in the build script.)
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
     * Copy file, optionally filtering.
     * (Filters set in project.)
     * @param fromFile the readable File source to copy
     * @param toFile the writable File destination file
     * @param boolean filter if true, enable filtering
     * @see org.aspectj.internal.tools.build.Builder#copyFile(File, File, boolean)
     */
    protected boolean copyFile(File fromFile, File toFile, boolean filter) {
        Copy copy = makeCopyTask(filter);
        copy.setFile(fromFile);
        copy.setTofile(toFile);
        executeTask(copy);
        return true;
    }
    
    /**
     * (Filters set in project.)
      * @see org.aspectj.internal.tools.ant.taskdefs.Builder#copyFiles(File, File, String, String, boolean)
      */
    protected boolean copyFiles(
         File fromDir,
         File toDir,
         String includes,
         String excludes,
         boolean filter) {
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

    protected boolean compile(
        Module module, 
        File classesDir,
        boolean useExistingClasses, 
        List errors) {
        
        // -- source paths
        Path path = new Path(project);
        boolean hasSourceDirectories = false;
        for (Iterator iter = module.getSrcDirs().iterator(); iter.hasNext();) {
            File file = (File) iter.next();
            path.createPathElement().setLocation(file);
            if (!hasSourceDirectories) {
                hasSourceDirectories = true;
            }
        }
        if (!classesDir.exists() && !classesDir.mkdirs()) {
            errors.add("compile - unable to create " + classesDir);
            return false;
        }
        if (!hasSourceDirectories) { // none - dump minimal file and exit
            File minFile = new File(classesDir, module.name);
            FileWriter fw = null;
            try {
                fw = new FileWriter(minFile);
                fw.write(module.name);
            } catch (IOException e) {
                errors.add("IOException writing " 
                    + module.name
                    + " to "
                    + minFile
                    + ": "
                    + Util.renderException(e));
            } finally {
                Util.close(fw);
            }
            return true; // nothing to compile - ok
        }
        if (useExistingClasses) {
            return true;
        }
        // XXX  test whether build.compiler property takes effect automatically
        // I suspect it requires the proper adapter setup.
        Javac javac = new Javac(); 
        setupTask(javac, "javac");
        javac.setDestdir(classesDir);
        javac.setSrcdir(path);
        path = null;
        
        // -- classpath
        Path classpath = new Path(project);
        boolean hasLibraries = setupClasspath(module, classpath);
        // need to add system classes??
        boolean inEclipse = true; // XXX detect, fork only in eclipse
        if (hasLibraries && inEclipse) {
            javac.setFork(true); // XXX otherwise never releases library jars
        }
        
        // -- set output directory
        classpath.createPathElement().setLocation(classesDir);
        javac.setClasspath(classpath);
        // misc
        javac.setDebug(true);
        javac.setTarget("1.1"); // 1.1 class files - Javac in 1.4 uses 1.4
        // compile
        try {
            return executeTask(javac);
        } catch (BuildException e) {
            String args = "" + Arrays.asList(javac.getCurrentCompilerArgs());
            errors.add("BuildException compiling " + module.toLongString() + args 
                + ": " + Util.renderException(e));
            return false;
        } finally {
            javac.init(); // be nice to let go of classpath libraries...
        }
    }
    
    public boolean setupClasspath(Module module, Path classpath) { // XXX fix test access
        boolean hasLibraries = false;
        // required libraries
        for (Iterator iter = module.getLibJars().iterator(); iter.hasNext();) {
            File file = (File) iter.next();            
            classpath.createPathElement().setLocation(file);
            if (!hasLibraries) {
                hasLibraries = true;
            }
        }
        // required modules and their exported libraries
        for (Iterator iter = module.getRequired().iterator(); iter.hasNext();) {
            Module required = (Module) iter.next();            
            classpath.createPathElement().setLocation(required.getModuleJar());
            if (!hasLibraries) {
                hasLibraries = true;
            }
            // also put on classpath libraries exported from required module
            // XXX exported modules not supported
            for (Iterator iterator = required.getExportedLibJars().iterator();
                iterator.hasNext();
                ) {
                classpath.createPathElement().setLocation((File) iterator.next());
            }
        }
        return hasLibraries;
    }
    
    /**
     * Merge classes directory and any merge jars into module jar
     * with any specified manifest file.  
     * META-INF directories are excluded.
     */
    protected boolean assemble(Module module, File classesDir, List errors) {
        if (!buildingEnabled) {
            return false;
        }
        // ---- zip result up
        Zip zip = new Zip();
        setupTask(zip, "zip");
        zip.setDestFile(module.getModuleJar());
        ZipFileSet zipfileset = null;
        
        // -- merge any resources in any of the src directories
        for (Iterator iter = module.getSrcDirs().iterator(); iter.hasNext();) {
            File srcDir = (File) iter.next();
            zipfileset = new ZipFileSet();
            zipfileset.setProject(project);
            zipfileset.setDir(srcDir);
            zipfileset.setIncludes(RESOURCE_PATTERN);
            zip.addZipfileset(zipfileset);
        }
        
        // -- merge any merge jars
        List mergeJars =  module.getMerges();
        final boolean useManifest = false;
        if (0 < mergeJars.size()) {
            for (Iterator iter = mergeJars.iterator(); iter.hasNext();) {
                File mergeJar = (File) iter.next();
                zipfileset = new ZipFileSet();
                zipfileset.setProject(project);
                zipfileset.setSrc(mergeJar);
                zipfileset.setIncludes("**/*");
                zipfileset.setExcludes("META-INF/manifest.mf"); // XXXFileLiteral
                zipfileset.setExcludes("meta-inf/manifest.MF");
                zipfileset.setExcludes("META-INF/MANIFEST.mf"); 
                zipfileset.setExcludes("meta-inf/MANIFEST.MF");
                zip.addZipfileset(zipfileset);
            }
        }
        // merge classes; put any meta-inf/manifest.mf here
        File metaInfDir = new File(classesDir, "META-INF");
        Util.deleteContents(metaInfDir);

        // -- manifest
        File manifest = new File(module.moduleDir, module.name + ".mf.txt");  // XXXFileLiteral
        if (Util.canReadFile(manifest)) {
            if (Util.canReadDir(metaInfDir) || metaInfDir.mkdirs()) {
                copyFile(manifest, new File(metaInfDir, "manifest.mf"), FILTER_ON);  // XXXFileLiteral
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

        try {
            handler.log("assembling " + module  + " in " + module.getModuleJar());
            return executeTask(zip)
                // zip returns true when it doesn't create zipfile
                // because there are no entries to add, so verify done
                && Util.canReadFile(module.getModuleJar());
        } catch (BuildException e) {
            errors.add("BuildException zipping " + module + ": " + e.getMessage());
            return false;
        } finally {
            module.clearOutOfDate();
        }
    }
    /**
	 * @see org.aspectj.internal.tools.build.Builder#buildAntecedants(Module)
	 */
	protected String[] getAntecedantModuleNames(Module module, boolean rebuild) {
        Hashtable targets = new Hashtable();
        makeTargetsForModule(module, targets, rebuild);   
        // XXX bug: doc says topoSort returns String, but returns Target 
        Collection result = project.topoSort(module.name, targets);
        // XXX is it topoSort that should detect cycles?
        int size = result.size();
        if (0 == result.size()) {
            return new String[0];
        }
        ArrayList toReturn = new ArrayList();
        for (Iterator iter = result.iterator(); iter.hasNext();) {
            Target target = (Target) iter.next();
            String name = target.getName();
            if (null == name) {
                throw new Error("null name?");
            } else {
                toReturn.add(name);
            }
        }
        // topoSort always returns module.name    
        if ((1 == size) 
            && module.name.equals(toReturn.get(0))
            && !module.outOfDate(false)) {
            return new String[0];
        }
        return (String[]) toReturn.toArray(new String[0]);
    }
        
    /**
     * Generate Module.assembledJar with merge of itself and all antecedants
     */                    
    protected boolean assembleAll(Module module, Messager handler) {
        if (!buildingEnabled) {
            return false;
        }
        Util.iaxIfNull(module, "module");
        Util.iaxIfNull(handler, "handler");
        if (module.outOfDate(false)) {
            throw new IllegalStateException("module out of date: " + module);
        }
        
        // ---- zip result up
        Zip zip = new Zip();
        setupTask(zip, "zip");
        zip.setDestFile(module.getAssembledJar());
        ZipFileSet zipfileset = null;
        
        ArrayList known = module.findKnownJarAntecedants();
        
        // -- merge any antecedents, less any manifest
        for (Iterator iter = known.iterator(); iter.hasNext();) {
            File jarFile = (File) iter.next();
            zipfileset = new ZipFileSet();
            zipfileset.setProject(project);
            zipfileset.setSrc(jarFile);
            zipfileset.setIncludes("**/*");
            zipfileset.setExcludes("META-INF/MANIFEST.MF");  // XXXFileLiteral
            zipfileset.setExcludes("META-INF/manifest.mf");
            zipfileset.setExcludes("meta-inf/manifest.mf");
            zipfileset.setExcludes("meta-inf/MANIFEST.MF");
            zip.addZipfileset(zipfileset);
        }
        
        // merge the module jar itself, including same manifest (?)
        zipfileset = new ZipFileSet();
        zipfileset.setProject(project);
        zipfileset.setSrc(module.getModuleJar());
        zip.addZipfileset(zipfileset);

        try {
            handler.log("assembling all " + module  + " in " + module.getAssembledJar());
            if (verbose) {
	            handler.log("knownAntecedants: " + known);
            }
            return executeTask(zip);
        } catch (BuildException e) {
            handler.logException("BuildException zipping " + module, e);
            return false;
        } finally {
            module.clearOutOfDate();
        }
    }

    /**
	 * @see org.aspectj.internal.tools.ant.taskdefs.Builder#buildInstaller(BuildSpec, String)
	 */
	protected boolean buildInstaller(
		BuildSpec buildSpec,
		String targDirPath) {
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

}

     
// finally caught by failing to comply with proper ant initialization
//  /**
//   * Build a module that has a build script.
//   * @param buildSpec the module to build
//   * @param buildScript the script file
//     * @throws BuildException if build fails
//   */
//  private void buildByScript(BuildSpec buildSpec, File buildScript) 
//        throws BuildException {
//      Ant ant = new Ant();
//        ant.setProject(getProject());
//        ant.setAntfile(buildScript.getAbsolutePath());
//        ant.setDescription("building module " + buildSpec.module);
//        ant.setDir(buildScript.getParentFile());
//        ant.setInheritAll(true);
//        ant.setInheritRefs(false);
//        ant.setLocation(getLocation());
//        ant.setOwningTarget(getOwningTarget());
//        // by convention, for build.xml, use module name to publish
//        ant.setTarget(buildSpec.module);
//        ant.setTaskName("ant");
//        loadAntProperties(ant, buildSpec);
//        ant.execute();
//     }
//     
//     /** override definitions */
//     private void loadAntProperties(Ant ant, BuildSpec buildSpec) {
//        Property property = ant.createProperty();
//        property.setName(BuildSpec.baseDir_NAME);
//        property.setFile(buildSpec.baseDir);
//        property = ant.createProperty();
//        property.setName(buildSpec.distDir_NAME);
//        property.setFile(buildSpec.distDir);
//        property = ant.createProperty();
//        property.setName(BuildSpec.tempDir_NAME);
//        property.setFile(buildSpec.tempDir);
//        property = ant.createProperty();
//        property.setName(BuildSpec.jarDir_NAME);
//        property.setFile(buildSpec.jarDir);        
//        property = ant.createProperty();
//        property.setName(BuildSpec.stagingDir_NAME);
//        property.setFile(buildSpec.stagingDir);        
//    }


/** 
 * Segregate product-building API's from module-building APIs for clarity.
 * These are called by the superclass if the BuildSpec warrants. 
 * XXX extremely brittle/arbitrary assumptions.
 * @see BuildModule for assumptions
 */
class ProductBuilder extends AntBuilder {

    private static String getProductInstallResourcesSrc(BuildSpec buildSpec) {
        final String resourcesName = "installer-resources";  // XXXFileLiteral
        File dir = buildSpec.productDir.getParentFile();
        String result = null;
        if (null == dir) {
            return "../../" + resourcesName;
        } 
        dir = dir.getParentFile();
        if (null == dir) {
            return "../" + resourcesName;
        } else {
            dir = new File(dir, resourcesName);
            return dir.getPath();
        }
    }

    private static String getProductInstallerFileName(BuildSpec buildSpec) {   // XXXFileLiteral
        return "aspectj-" 
            + buildSpec.productDir.getName()
            + "-"
            + Util.shortVersion(buildSpec.version)
            + ".jar"; 
    }
    
   /**
     * Calculate name of main, typically InitialCap, and hence installer class.
     * @return $$installer$$.org.aspectj." + ProductName + "Installer" 
     */
     
    private static String getProductInstallerMainClass(BuildSpec buildSpec) {
        String productName = buildSpec.productDir.getName();
        String initial = productName.substring(0, 1).toUpperCase();
        productName = initial + productName.substring(1);
        return "$installer$.org.aspectj." + productName + "Installer";   // XXXNameLiteral
    }
    
    /** @see Builder.getBuilder(String, Project, File) */
    ProductBuilder(
        Project project, 
        File tempDir, 
        boolean useEclipseCompiles,
        Messager handler) {
        super(project, tempDir, useEclipseCompiles, handler);
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
        Modules modules = new Modules(
            buildSpec.baseDir, 
            buildSpec.jarDir, 
            buildSpec.trimTesting, 
            handler);
        ProductModule[] productModules = discoverModules(buildSpec.productDir, modules);
        for (int i = 0; i < productModules.length; i++) {
            if (buildSpec.verbose) {
                handler.log("building product module " + productModules[i]); 
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
        Copy copy = makeCopyTask(true);
        copy.setTodir(targDir);
        File distDir = new File(buildSpec.productDir, "dist");       // XXXFileLiteral
        Util.iaxIfNotCanReadDir(distDir, "product dist directory");
        FileSet fileset = new FileSet();
        fileset.setDir(distDir);
        fileset.setExcludes(Builder.BINARY_SOURCE_PATTERN);
        copy.addFileset(fileset);
        if (!executeTask(copy)) {
            return false;
        }
        
        // copy binaries (but not module flag files)
        String excludes = null;
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
        copy = makeCopyTask(false);
        copy.setTodir(targDir);
        fileset = new FileSet();
        fileset.setDir(distDir);
        fileset.setIncludes(Builder.BINARY_SOURCE_PATTERN);
        if (null != excludes) {
            fileset.setExcludes(excludes);
        }
        copy.addFileset(fileset);
        if (!executeTask(copy)) {
            return false;
        }

        // copy binaries associated with module flag files
        for (int i = 0; i < productModules.length; i++) {
            ProductModule product = productModules[i];
            String targPath = targDirPath + "/" + product.relativePath;
            File jarFile = (product.assembleAll 
                ? product.module.getAssembledJar()
                : product.module.getModuleJar() );
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
    
    protected boolean buildInstaller(BuildSpec buildSpec, String targDirPath) {
        if (buildSpec.verbose) {
            handler.log("creating installer for " + buildSpec);
        }
        AJInstaller installer = new AJInstaller();
        setupTask(installer, "installer");
        installer.setBasedir(targDirPath);
        //installer.setCompress();
        File installSrcDir = new File(buildSpec.productDir, "install");   // XXXFileLiteral
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
        return buildSpec.baseDir.getPath() 
            + "/lib/build/build.jar" ; // XXX
    }
    
    private Module moduleForReplaceFile(File replaceFile, Modules modules) {
        String jarName = moduleAliasFor(replaceFile.getName().toLowerCase());
        if (jarName.endsWith(".jar") || jarName.endsWith(".zip")) {   // XXXFileLiteral
            jarName = jarName.substring(0, jarName.length()-4);
        } else {
            throw new IllegalArgumentException("can only replace .[jar|zip]");
        }
        boolean assembleAll = jarName.endsWith("-all");
        String name = (!assembleAll ? jarName : jarName.substring(0, jarName.length()-4));
        return modules.getModule(name);
    }
    
}


class ProjectMessager extends Messager {
    private final Project project;
    public ProjectMessager(Project project) {
        Util.iaxIfNull(project, "project");
        this.project = project;
    }
    
    public boolean log(String s) {
        project.log(s);
        return true;
    }
    public boolean error(String s) {
        project.log(s, Project.MSG_ERR);
        return true;
    }
    public boolean logException(String context, Throwable thrown) {
        project.log(context + Util.renderException(thrown), Project.MSG_ERR);
        return true;
    }

}