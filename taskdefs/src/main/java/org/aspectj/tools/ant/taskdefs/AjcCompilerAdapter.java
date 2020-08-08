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

import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;


import java.io.File;

/**
 * Partial implementation of CompilerAdapter for ajc task.
 * The Javac interface does not support argfiles which are
 * typically necessary to compile under ajc, so this 
 * implementation is not documented, recommended, or supported.
 * See Apache request for enhancement 
 * <a href="http://nagoya.apache.org/bugzilla/show_bug.cgi?id=7542">
 * 7542</a>.
 * @see CompilerAdapter
 */
public class AjcCompilerAdapter implements CompilerAdapter {
    // Cannot extend Ajc because CompilerAdapter.execute() returns boolean.

    Ajc10 ajc = null;

    /** @see CompilerAdapter */
    public void setJavac(Javac javac) {
        if (null == javac) {
            throw new IllegalArgumentException("null javac");
        }
        Object task = javac.getProject().createTask("ajc"); 
        String err = null;
        if (null == task) {
            err = "ajc not defined - put ajc taskdef library on classpath";
        } else if (!(task instanceof Ajc10)) {
            String name = task.getClass().getName();
            err = "Wrong class for Ajc task - ";
            if (name.equals(Ajc10.class.getName())) {
                err = err + "second class loader - put ajc taskdef library "
                    + "only on system classpath via ant/lib";
            } else {
                err = err + name;
            }
        }
        if (null != err) {
            throw new Error(err);
        }

        Ajc10 tempAjc = (Ajc10) task;
        Path srcDir = javac.getSrcdir();
        if (null != srcDir) {
            tempAjc.setSrcdir(srcDir);
        }
        File destDir = javac.getDestdir();
        if (null != destDir) {
            tempAjc.setDestdir(destDir.getPath());
        }
        Path classpath = javac.getClasspath();
        if (null != classpath) {
            tempAjc.setClasspath(classpath);
        }
        Path bootclasspath = javac.getBootclasspath();
        if (null != bootclasspath) {
            tempAjc.setBootclasspath(bootclasspath);
        }
        Path extDirs = javac.getExtdirs();
        if (null != extDirs) {
            tempAjc.setExtdirs(extDirs);
        }

        tempAjc.setFailonerror(javac.getFailonerror());
        tempAjc.setDeprecation(javac.getDeprecation()); // XXX unsupported
        tempAjc.setEncoding(javac.getEncoding());
        tempAjc.setDebug(javac.getDebug()); // XXX unsupported
        tempAjc.setOptimize(javac.getOptimize()); // XXX unsupported
        tempAjc.setDepend(javac.getDepend()?"on":"off"); // XXX unsupported
        tempAjc.setVerbose(javac.getVerbose()); 
        String target = javac.getTarget();
        if (null != target) {
            tempAjc.setTarget(target);
        }
        tempAjc.setIncludeantruntime(javac.getIncludeantruntime()); // XXX unsupported
        File[] files = javac.getFileList();
        if (null != files) {
			for (File file : files) {
				tempAjc.backdoorSetFile(file);
			}
        }
        ajc = tempAjc;
    }

    /** 
     * Run the compiler.
     * @see CompilerAdapter#execute() 
     * @throws BuildException if setJavac(Javac) was not called before each call of this
     */
    public boolean execute() throws BuildException {
        if (null == ajc) {
            throw new BuildException("setJavac(Javac) not completed");
        }
        ajc.execute();
        ajc = null; // enforce one-shot
        return true;
    }
}
