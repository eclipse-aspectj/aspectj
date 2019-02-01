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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/** 
 * Check if version source file has the specified build version,
 * and ensure a tag file reflects whether it does or not.
 */
public class VersionUptodate extends Task {
    public VersionUptodate() {}

    private String buildVersion;
    private File versionSource;
    private File versionTagFile;
    
    /** 
     * @param buildVersion String expected as Version.text - required
     */
    public void setVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    /** 
     * @param versionSource the File Version.java containing text constant 
     * - required
     */
    public void setVersionSourceFile(File versionSource) {
        this.versionSource = versionSource;
    }
    
    /** 
     * @param versionTagFile the File whose existence signals that the version
     * is uptodate after this task executes - required.
     */
    public void setVersionTagFile(File versionTagFile) {
        this.versionTagFile = versionTagFile;
    }
    
    /**
     * If the Version.java source file contains the correct
     * build version, then create the output tag file,
     * else delete it if it exists.
     * @throws BuildException if tagFile not creatable and version is incorrect
     *          or if version is correct and tagFile cannot be deleted.
     */
    public void execute() throws BuildException {
        if (null == buildVersion) {
            throw new BuildException("require buildVersion");
        }
        if ((null == versionSource) || !versionSource.canRead()){
            throw new BuildException("require versionSource");
        }
        if (null == versionTagFile){
            throw new BuildException("require versionTagFile");
        }
        if (sameVersion(versionSource, buildVersion)) {
            if (!versionTagFile.exists()) {
                createFile(versionTagFile, buildVersion);
            }
        } else if (null == versionTagFile) {
            throw new BuildException("no tag file, and version out of date");
        } else if (versionTagFile.exists()) {
            if (!versionTagFile.delete()) {
                throw new BuildException("version out of date, but cannot delete " + versionTagFile);
            }
        }
    }
    
	/**
	 * Detect whether version is correct in Java sources.
     * @param versionSource
	 * @param buildVersion
	 * @return boolean
	 */
	private boolean sameVersion(File versionSource, String buildVersion) {
        // XXX build and load instead of scanning?
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(versionSource);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while (null != (line = reader.readLine())) {
                int loc = line.indexOf("static final String text = ");
                if (-1 != loc) {
                    return (-1 != line.indexOf(buildVersion , loc));
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            if (null != fileReader) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                }
            }
        }        
	}
    
    /**
     * Create file with contents
     */
    private void createFile(File versionTagFile, String contents) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(versionTagFile);
            char[] buf = new char[contents.length()];
            contents.getChars(0, buf.length, buf, 0);
            writer.write(contents);
        } catch (IOException e) {
            throw new BuildException("writing " + versionTagFile, e);
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e){
                    // ignore
                }
            }
        }
    }
    
} 

