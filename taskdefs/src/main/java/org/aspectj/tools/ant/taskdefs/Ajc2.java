/* *******************************************************************
 * Copyright (c) 2000-2001 Xerox Corporation. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.util.JavaEnvUtils;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Ant task for the AspectJ compiler -- AJC.
 * List (.lst) files are passed in as includes.
 *
 * This task was developed by the <a href="http://aspectj.org">AspectJ Project</a>
 *
 * @author <a href="mailto:palm@parc.xerox.com">Jeffrey Palm</a>
 * @see    org.aspectj.tools.ant.taskdefs.compilers.Ajc
 */
public class Ajc2 extends Javac {

    /**
     * The name of the adapter we use.
     */
    public final static String ADAPTER_CLASS =
        "org.aspectj.tools.ant.taskdefs.compilers.Ajc";

    /* ----------------------------------------------------------------------
     * Attribute members
     * ----------------------------------------------------------------------
     */
   
    /**
     * How many concurrent threads to use for compilation,
     * defaults to 0 -- multi-threading disabled.
     */
    private Integer threads;

    /**
     * Don't generate any comments into the woven code.
     */
    private boolean nocomments;

    /**
     * Don't generate .ajsym or .ajsline files.
     */
    private boolean nosymbols;

    /**
     * Generate regular Java code into <workingdir>.
     */
    private boolean preprocess;

    /**
     * Specify where to place intermediate .java files <dir>
     * defaults to ./ajworkingdir.
     */
    private File workingdir;

    /**
     * The file is a line-delimited list of arguments
     * these arguments are inserted into the argument list
     */
    private List argfiles;


    /* ----------------------------------------------------------------------
     * Misc. members
     * ----------------------------------------------------------------------
     */

    /**
     * Whether we have used the <code>excludes</code> attribute.
     */
    private boolean haveExcludes = false;

    /**
     * Whether we have used the <code>includes</code> attribute.
     */
    private boolean haveIncludes = false;

    /**
     * Whether we have used the <code>excludes</code> attribute.
     * @return Whether we have used the <code>excludes</code> attribute.
     */
    protected boolean hasExcludes() {
        return haveExcludes;
    }

    /**
     * Whether we have used the <code>includes</code> attribute.
     * @return Whether we have used the <code>includes</code> attribute.
     */
    protected boolean hasIncludes() {
        return haveIncludes;
    }

    /* ----------------------------------------------------------------------
     * Attribute access methods
     * ----------------------------------------------------------------------
     */
    
    /**
     * Sets the number of threads.
     *
     * @param threads the number of threads.
     * @see           Ajc2#threads
     */
    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    /**
     * Returns the number of threads.
     *
     * @return the number of threads.
     * @see    Ajc2#threads
     */
    public Integer getThreads() {
        return threads;
    }

    /**
     * Set the <code>-nocomments</code> flag.
     *
     * @param nocomments true turns on the flag.
     * @see              Ajc2#nocomments
     */
    public void setNocomments(boolean nocomments) {
        this.nocomments = nocomments;
    }

    /**
     * Returns if the <code>-nocomments</code> flag is turned on.
     *
     * @return <code>true</code> if the <code>-nocomments</code> flag is on.
     * @see    Ajc2#nocomments
     */
    public boolean getNocomments() {
        return nocomments;
    }

    /**
     * Set the <code>-nosymbols</code> flag.
     *
     * @param nosymbols true turns on the flag.
     * @see             Ajc2#nosymbols
     */
    public void setNosymbols(boolean nosymbols) {
        this.nosymbols = nosymbols;
    }

    /**
     * Returns if the <code>-nosymbols</code> flag is turned on.
     *
     * @return <code>true</code> if the <code>-nosymbols</code> flag is on.
     * @see    Ajc2#nosymbols
     */    
    public boolean getNosymbols() {
        return nosymbols;
    }

    /**
     * Set the <code>-preprocess</code> flag.
     *
     * @param preprocess <code>true</code> turns on the <code>-preprocess</code> flag.
     * @see    Ajc2#preprocess
     */
    public void setPreprocess(boolean preprocess) {
        this.preprocess = preprocess;
    }

    /**
     * Returns if the <code>-preprocess</code> flag is turned on.
     *
     * @return <code>true</code> if the <code>-preprocess</code> flag is on.
     * @see    Ajc2#preprocess
     */      
    public boolean getPreprocess() {
        return preprocess;
    }

    /**
     * Sets the workingdir.
     *
     * @param workingdir the new workingdir.
     * @see   Ajc2#workingdir
     */
    public void setWorkingdir(File workingdir) {
        this.workingdir = workingdir;
    }

    /**
     * Returns the current workingdir.
     *
     * @return the current workingdir.
     * @see    Ajc2#workingdir
     */
    public File getWorkingdir() {
        return workingdir;
    }

    /**
     * Sets the the argument files by the comma-delimited String passed in.
     *
     * @param argfiles comma-delimited String contained argument files.
     */
    public void setArgfiles(String argfiles) {
        StringTokenizer tok = new StringTokenizer(argfiles, ",");
        while (tok.hasMoreTokens()) {
            File argfile = project.resolveFile(tok.nextToken());
            if (argfile != null && argfile.exists() && !argfile.isDirectory()) {
                createArgfile().setFile(argfile);
            }
        }
    }

    /**
     * Creates a nested <code>Argfile</code>, add it to the list
     * <code>argfiles</code>, and returns the new <code>Argfile</code>
     * instance.
     *
     * @return a new <code>Argfile</code> instance.
     */
    public Argfile createArgfile() {
        Argfile argfile = new Argfile();
        if (argfiles == null) {
            argfiles = new ArrayList();
        }
        argfiles.add(argfile);
        return argfile;
    }

    /**
     * Returns the java.util.List of argfiles.
     * This could be <code>null</code>.
     *
     * @return the list of argfiles.
     */
    public List getArgfiles() {
        return argfiles;
    }

    /**
     * A simple class with one member -- <code>file</code> -- that
     * represents an argument file.
     */
    public static class Argfile {
        private File file;
        public void setFile(File file) { this.file = file; }
        public File getFile() { return file; }
        public String toString() { return file.getAbsolutePath(); }
    }

    /* ----------------------------------------------------------------------
     * Misc. methods
     * ----------------------------------------------------------------------
     */

    /* Leaving the includes blank in the Javac task defaults to including all.
     * In Ajc, if the user specifies an argfile, but leaves the includes and
     * excludes blank that person probably meant to just includes to argfile.
     * So, we keep track of whether includes and/or excludes have been explicitly
     * used with the have{In,Ex}cludes members, and setting these flags
     * in the create... and set... methods.
     *
     * When constructing the compileList, if both haveIncludes and haveExcludes
     * are false, but the user has specified an argfile, then we set includes
     * to '!**' before adding the contents of the argfile.
     */

    /**
     * Override Javac.createInclude() to set <code>haveIncludes</code>
     * to <code>true</code>.
     *
     * @return new PatternSet.NameEntry to be added to the include list.
     * @see    org.apache.tools.ant.taskdefs.Javac#createInclude()
     */
    public PatternSet.NameEntry createInclude() {
        haveIncludes = true;
        return super.createInclude();
    }

    /**
     * Override Javac.createExclude() to set <code>haveExcludes</code>
     * to <code>true</code>.
     *
     * @return new PatternSet.NameEntry to be added to the exclude list.
     * @see    org.apache.tools.ant.taskdefs.Javac#createExclude()
     */    
    public PatternSet.NameEntry createExclude() {
        haveExcludes = true;
        return super.createExclude();
    }

    /**
     * Override Javac.setIncludes(String) to set <code>haveIncludes</code>
     * to <code>true</code>.
     *
     * @param includes Comma-separated list of includes.
     * @see   org.apache.tools.ant.taskdefs.Javac#setIncludes(java.lang.String)
     */
    public void setIncludes(String includes) {
        haveIncludes = true;
        super.setIncludes(includes);
    }

    /**
     * Override Javac.setExcludes(String) to set <code>haveExcludes</code>
     * to <code>true</code>.
     *
     * @param excludes Comma-separated list of excludes.
     * @see   org.apache.tools.ant.taskdefs.Javac#setExcludes(java.lang.String)
     */    
    public void setExcludes(String excludes) {
        haveExcludes = true;
        super.setExcludes(excludes);
    }

    public String getAdapterClass() {
        return ADAPTER_CLASS;
    }
    

    public final void execute() throws BuildException {
        prepare();
        executeAfterPrepare();
    }

    /**
     * Executes by first setting the <code>build.compiler</code> property
     * to AjcCompiler, then invokes the super.execute() method.
     *
     * @throws org.apache.tools.ant.BuildException
     * @see    org.apache.tools.ant.taskdefs.Javac#execute()
     */    
    public void executeAfterPrepare() throws BuildException {
        
        // Save the old build.compiler property
        String oldBuildCompiler = project.getProperty("build.compiler");

        // If oldBuildCompiler is null try to resolve it
        if (oldBuildCompiler == null) {
            String javaVersion = JavaEnvUtils.getJavaVersion();
            if (javaVersion.equals(JavaEnvUtils.JAVA_1_0)) {
                // Cannot happen
            } else if (javaVersion.equals(JavaEnvUtils.JAVA_1_1)) {
                oldBuildCompiler = "classic";
            } else if (javaVersion.equals(JavaEnvUtils.JAVA_1_2)) {
                oldBuildCompiler = "classic";
            } else if (javaVersion.equals(JavaEnvUtils.JAVA_1_3)) {
                oldBuildCompiler = "modern";
            }
        }

        // Set the new adapter
        project.setProperty("build.compiler", getAdapterClass());
        BuildException caught = null;
        try {
            super.execute();
        } catch (BuildException be) {
            caught = be;
        } finally {
            
            // Reset to the old compiler
            if (oldBuildCompiler != null) {
                project.setProperty("build.compiler", oldBuildCompiler);
            }
        }

        // If we caught an exception executing throw it
        if (caught != null) {
            throw caught;
        }
     }

    /**
     * Guaranteed to be called before doing real execute.
     */
    public void prepare() {
        if (argfiles != null && !haveIncludes &&
            !haveExcludes && getSrcdir() == null) {
            useDefaultSrcdir();
        }
    }

    protected final void useDefaultSrcdir() {
        setSrcdir(new Path(project, "."));
    }

    /**
     * Overrides Javac.scanDir(..) so that it doesn't check dependencies.
     *
     * @see org.apache.tools.ant.taskdefs.Javac#scanDir
     */
    protected void scanDir(File srcDir, File destDir, String files[]) {
        List newFiles = new ArrayList();

        // Add the files listed in the argfiles to the includes
        List newIncludes = new ArrayList();
        List newArguments = new ArrayList();
        if (argfiles != null) {
			for (Object o : argfiles) {
				File argfile = ((Argfile) o).getFile();
				expandArgfile(argfile, newIncludes, newArguments);
			}
        }

        // If there aren't any includes, but we've used an argfile then we should
        // set the includes to be the one's found in newIncludes
        // If we do this, we need to re-read files from the directory scanner
        if (!haveIncludes && !haveExcludes && argfiles != null) {
            log("Setting includes to '!**'", Project.MSG_VERBOSE);
            setIncludes("!**");
            //files = getDirectoryScanner(srcDir).getIncludedFiles();
        }

        // Otherwise we want to add all .java files to the compileList
        else {
			for (String file : files) {
				File newFile = new File(srcDir, file);
				if (newFile != null &&
						newFile.exists() &&
						newFile.getName().endsWith(".java")) {
					newFiles.add(newFile);
				}
			}
        }

        // Add the new included files
		for (Object newInclude : newIncludes) {
			newFiles.add((File) newInclude);
		}

        // This is the same behavior found in Javac
        int newFileSize = newFiles.size();
        if (newFileSize > 0) {
            File[] newCompileList = new File[compileList.length + newFileSize];
            System.arraycopy(compileList, 0, newCompileList, 0, compileList.length);
            System.arraycopy(newFiles.toArray(), 0, newCompileList,
                             compileList.length, newFileSize);
            compileList = newCompileList;
        }
    }

    private void expandArgfile(File argfile, List includes, List arguments) {

        log("argfile:" + argfile, Project.MSG_VERBOSE);
        
        // All paths are relative to the parent
        File parent = argfile.getParentFile();

        // Sanity check
        if (parent == null || !parent.exists() || !parent.isDirectory()) {
            return;
        }

        // Read the file
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(argfile));
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                // Skip blank lines
                if ("".equals(line)) {
                    continue;
                }

                // Allow '#' and '//' line comments
                int isharp = line.indexOf("#");
                if (isharp != -1) {
                    line = line.substring(0, isharp);
                }

//                int istar = -1;

                // Argument
                if (line.startsWith("-")) {
                    arguments.add(line);
                }

                // If there are stars we'll try to resolve the file here
                else if (line.contains("*")) {
                    log("The argfile line '" + line + "' is invalid",
                        Project.MSG_WARN);
                }

                // Another argfile
                else if (line.startsWith("@")) {
                    String newArgfileName = line.substring(1);
                    File newArgfile = new File(parent, newArgfileName);
                    expandArgfile(newArgfile, includes, arguments);
                }

                // Source file
                else {
                    File newfile = new File(line);
                    if (!newfile.isAbsolute()) {
                        newfile = new File(parent, line);
                    }
                    if (newfile != null && newfile.exists() &&
                        !newfile.isDirectory() &&
                        newfile.getName().endsWith(".java")) {
                        includes.add(newfile);
                    }
                }

            }
        } catch (IOException ioe) {
            log("trouble with argfile: " + argfile + ":" + ioe, Project.MSG_ERR);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe2) {
            }
        }
    }
}
