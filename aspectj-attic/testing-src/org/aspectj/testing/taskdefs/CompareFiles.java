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


package org.aspectj.testing.taskdefs;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.*;

/**
 * Wrap file comparison utility as ant taskdef.
 * (Whitespace semantics track the String.trim() and StringTokenizer class.)
 * <table>
 * <tr><td>lhsFile</td><td>path to left-hand-side file to compare (required)</td></tr>
 * <tr><td>rhsFile</td><td>path to right-hand-side file to compare (required)</td></tr>
 * <tr><td>output</td><td>path to output file (System.out otherwise)</td></tr>
 * <tr><td>ignoreCase</td><td>convert to uppercase before comparison (boolean yes/no)</td></tr>
 * <tr><td>trimWhitespace</td><td>ignore leading/trailing white space(boolean yes/no)</td></tr>
 * <tr><td>collapseWhitespace</td><td>convert all white space runs to a single space (boolean yes/no)</td></tr>
 * <tr><td>filterSpec</td><td>all specifications for a filter, based on the RegexpFilter class
 *               (currently, syntax: <code>{file | {-i|-t|-b|-s <pattern>|-s <patternFile>}..}</code></td></tr>
 * </table>
 * @see org.aspectj.testing.compare.RegexpFilter#init(String[],RegexpFilter)
 */
public class CompareFiles extends org.apache.tools.ant.Task {
    /*
      Unable to implement multiple inheritance except by delegation:
      - Task subclass must be outer or ant throws InstantiationException
      - if/since outer, the subclass getter/setters cannot refer to 
        protected fields in worker superclass absent inner worker 
        subclass delegate methods.  yuck.
      - getting access errors when trying to use the RuntimeConfigurable
        to initialize the task.  Looking at the Ant code, it does not
        appear to be used for tasks??  I found none using it and the
        initialization seems wrong...
    */
    final private Worker worker;
    public CompareFiles() { 
        worker = new Worker();
    }
    protected File lhsFile;
    protected File rhsFile;
    public void setLhsFile(File file)             { lhsFile = file; }
    public void setRhsFile(File file)             { rhsFile = file; }
    public void setOutput(File file)              { worker.setOutput(file); }
    public void setFilterSpec(String str)         { worker.setFilterSpec(str); }
    public void setCollapseWhitespace(boolean ok) { worker.setCollapseWhitespace(ok); }
    public void setTrimWhitespace(boolean ok)     { worker.setTrimWhitespace(ok); }
    public void setIgnoreCase(boolean ok)         { worker.setIgnoreCase(ok); }
    public void execute() throws BuildException   { 
        if (!lhsFile.canRead()) {
            log("FAIL taskdefs.CompareFiles: bad lhsFile: " + lhsFile);
        } else if (!rhsFile.canRead()) {
            log("FAIL taskdefs.CompareFiles: bad rhsFile: " + rhsFile);
        } else if (rhsFile.isDirectory() != lhsFile.isDirectory()) {
            log("FAIL taskdefs.CompareFiles: both must be dirs."
                + " lhsFile=" + lhsFile
                + " rhsFile=" + rhsFile);
        } else {
            worker.dodiff(lhsFile, rhsFile); 
        }
    }
} // class CompareFiles

/** worker class exposes state and converts Exception to BuildException */
class Worker extends org.aspectj.testing.compare.CompareFiles {
    String spec = null;
    public void setOutput(File file)              { output = file; }
    public void setCollapseWhitespace(boolean ok) { collapseWhitespace = ok; }
    public void setTrimWhitespace(boolean ok)     { trimWhitespace = ok; }
    public void setIgnoreCase(boolean ok)         { ignoreCase = ok; }
    public void setFilterSpec(String str)         { spec = str; }
    public void initFilter() {
        if (null != spec) {
            initFilter(spec, false);
        }
    }

    public void dodiff(File lhsFile, File rhsFile) throws BuildException {
        initFilter();
        try {
            super.diff(lhsFile, rhsFile);
        } catch (IOException t) {
            String s = t.getClass().getName() + ": " + t.getMessage();
            throw new BuildException(s, t);
        } catch (Throwable e) {
            String s = e.getClass().getName() + ": " + e.getMessage();
            throw new BuildException("Error - " + s, e);
        } 
    }    
} // class CompareFiles$Worker

