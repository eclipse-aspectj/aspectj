/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Wes Isberg     initial implementation
 * ******************************************************************/


package org.aspectj.tools.ant.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.aspectj.util.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Adapt ajc to javac commands.
 * Note that the srcdirs set for javac are NOT passed on to ajc;
 * instead, the list of source files generated is passed to ajc.
 * <p>
 * Javac usually prunes the source file list based on the timestamps
 * of corresponding .class files, which is wrong for ajc which
 * requires all the files every time.  To work around this,
 * set the global property CLEAN ("build.compiler.clean") to delete
 * all .class files in the destination directory before compiling.
 * This clean process only permits one compile process at a time 
 * for each destination directory because it tracks recursion by
 * writing a tag file to the destination directory.
 * 
 * <p><u>Warnings</u>: 
 * <ol>
 * <li>cleaning will not work if no destination directory
 *     is specified in the javac task
 *     (because we don't know where to put the tag file).</li>
 * <li>cleaning will makes stepwise build processes fail
 * if they depend on the results of the prior compilation being
 * in the same directory, since this deletes <strong>all</strong>
 * .class files.</li>
 * <li>If no files are out of date, then the adapter is <b>never</b> called
 *     and thus cannot gain control to clean out the destination dir.
 *     </li>
 * <p>
 * 
 * @author Wes Isberg
 * @since AspectJ 1.1, Ant 1.5.1
 */
public class Ajc11CompilerAdapter implements CompilerAdapter {
    /** 
     * Define this system/project property to signal that the 
     * destination directory should be cleaned 
     * and javac reinvoked
     * to get the complete list of files every time.
     */    
    public static final String CLEAN = "build.compiler.clean";
    
    Javac javac;
    
    public void setJavac(Javac javac) {
        this.javac = javac;
    }
    
    public boolean execute() throws BuildException {
        checkJavac();
        if (recurse()) {
            javac.execute(); // re-invokes us after recalculating file list
        } else { 
            try {
                AjcTask ajc = new AjcTask();
                String err = ajc.setupAjc(javac);
                if (null != err) {
                    throw new BuildException(err, javac.getLocation());
                }
                ajc.execute(); // handles BuildException for failonerror, etc.
            } finally {
                doneRecursing();
            }
        }
        return true;
    }

    /** @throws IllegalStateException if javac is not defined */
    protected void checkJavac() {
        if (null == javac) {
            throw new IllegalStateException("null javac");
        }
    }
    
    /**
     * Get javac dest dir.
     * @param client the String label for the client seeking the directory
     *        (only used in throwing BuildException)
     * @return File dest dir
     * @throws BuildException if not specified and required
     */
    protected File getDestDir(String client) {
        checkJavac();
        File destDir = javac.getDestdir();
        if (null == destDir) {
            throw new BuildException("require destDir for " + client);
        }
        return destDir;
    }

    protected File getTagFile() {
        return new File(getDestDir("getting tag file directory"), 
                        "Ajc11CompilerAdapter.tag");
    }

    /**
     * If property CLEAN is set, then this
     * cleans out the dest dir of any .class files,
     * installs a tag file, and returns true to signal a recursive call.
     * That means this returns false if CLEAN is not set
     * or if the tag file already exists (i.e., already recursing).
     * The result is that javac is re-invoked after the dest dir
     * is cleaned, so it picks up all the correct source files.
     * (This is a costly hack to work around Javac's forcing the
     * pruning of the file list.)
     * @return true if javac should be re-invoked.
     */
    protected boolean recurse() {
        checkJavac();
        String cleanDirs = javac.getProject().getProperty(CLEAN);
        if (null == cleanDirs) {
            return false;
        } 
        File destDir = getDestDir("recursing to clean");
        File tagFile = getTagFile();
        if (tagFile.exists()) {
            return false;
        }
        try {
            javac.log(CLEAN + " cleaning .class files from " + destDir,
                Project.MSG_VERBOSE);
            FileUtil.deleteContents(destDir, FileUtil.DIRS_AND_WRITABLE_CLASSES, true);
            FileWriter fw = new FileWriter(tagFile);
            fw.write("Ajc11CompilerAdapter.recursing");
            fw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected void doneRecursing() {
        File tagFile = getTagFile();
        if (tagFile.exists()) {
            tagFile.delete();
        } 
    }
}

