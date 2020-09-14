/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
//import java.io.FileFilter;
//import java.io.FileWriter;
//import java.io.IOException;

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
 * </p>
 * 
 * <p><u>Warnings</u>:</p>
 * <ol>
 * <li>cleaning will not work if no destination directory
 *     is specified in the javac task.
 *     (RFE: find and kill .class files in source dirs?)</li>
 * <li>cleaning will makes stepwise build processes fail
 * if they depend on the results of the prior compilation being
 * in the same directory, since this deletes <strong>all</strong>
 * .class files.</li>
 * <li>If no files are out of date, then the adapter is <b>never</b> called
 *     and thus cannot gain control to clean out the destination dir.
 *     </li>
 * </ol>
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

    /** track whether we re-called <code>javac.execute()</code> */
    private static final ThreadLocal inSelfCall = new ThreadLocal() {
        public Object initialValue() {
            return Boolean.FALSE;
        }
    };

    Javac javac;

    public void setJavac(Javac javac) {
        this.javac = javac;
        javac.setTaskName(javac.getTaskName() + " - ajc");
    }

    public boolean execute() throws BuildException {
        if (null == javac) {
            throw new IllegalStateException("null javac");
        }
        if (!(Boolean) inSelfCall.get()
            && afterCleaningDirs()) {
            // if we are not re-calling ourself and we cleaned dirs,
            // then re-call javac to get the list of all source files.
            inSelfCall.set(Boolean.TRUE);
            javac.execute();
            // javac re-invokes us after recalculating file list
        } else {
            try {
                AjcTask ajc = new AjcTask();
                String err = ajc.setupAjc(javac);
                if (null != err) {
                    throw new BuildException(err, javac.getLocation());
                }
                ajc.execute();
                // handles BuildException for failonerror, etc.
            } finally {
                inSelfCall.set(Boolean.FALSE);
            }
        }
        return true;
    }

    /**
     * If destDir exists and property CLEAN is set, 
     * this cleans out the dest dir of any .class files,
     * and returns true to signal a recursive call.
     * @return true if destDir was cleaned.
     */
    private boolean afterCleaningDirs() {
        String clean = javac.getProject().getProperty(CLEAN);
        if (null == clean) {
            return false;
        }
        File destDir = javac.getDestdir();
        if (null == destDir) {
            javac.log(
                CLEAN + " specified, but no dest dir to clean",
                Project.MSG_WARN);
            return false;
        }
        javac.log(
            CLEAN + " cleaning .class files from " + destDir,
            Project.MSG_VERBOSE);
        FileUtil.deleteContents(
            destDir,
            FileUtil.DIRS_AND_WRITABLE_CLASSES,
            true);
        return true;
    }
}
