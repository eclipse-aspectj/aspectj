/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;

import org.apache.tools.ant.taskdefs.Mkdir;

@SuppressWarnings("deprecation")
public class AJPush extends ConditionalTask {
    private File src;

    public void setSrc(String v) { src = project.resolveFile(v); }

    private String key;

    public void setKey(String v) { key = v; }

    File releaseDir = null;
    File downloadDir = null;
    boolean waiting = false;

    public void execute() throws org.apache.tools.ant.BuildException {
        //File releaseDir = src.getParentFile();
        // todo: dependency on ant script variable name aj.release.dir
        releaseDir = project.resolveFile(project.getProperty("aj.release.dir"));
        // todo: dependency on ant script variable name download.dir
        downloadDir = project.resolveFile(project.getProperty("download.dir"));
        // For testing make sure these directories are made
        Mkdir mkdir = (Mkdir) project.createTask("mkdir");
        mkdir.setDir(releaseDir);
        mkdir.execute();
        mkdir = (Mkdir) project.createTask("mkdir");
        mkdir.setDir(downloadDir);
        mkdir.execute();
        log("Pushing from " + releaseDir + " to " + downloadDir);
        // add info to release.txt
        try {
            File releaseFile = new File(releaseDir, "release.txt");
            File downloadFile = new File(downloadDir, "release.txt");
            if (!releaseFile.canRead()) {
                releaseFile.createNewFile();
            }
            addReleaseInfo(src, releaseFile);
            // copy to staging web server
            project.copyFile(src, new File(downloadDir, src.getName()));
            project.copyFile(releaseFile, downloadFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void addReleaseInfo(File file, File propFile) throws IOException {
        Properties props = new Properties();
        if (propFile.canRead()) {
            props.load(new FileInputStream(propFile));
        }
        file.createNewFile(); // create new only if necessary
        long bytes = file.length();
        DecimalFormat df = new DecimalFormat();
        df.setGroupingSize(3);
        String bytesString = df.format(bytes);
        props.put("release." + key + ".size.bytes", bytesString);
        props.put("release." + key + ".date", project.getProperty("build.date"));
        props.put("release." + key + ".filename", file.getName());
        props.put("release.date", project.getProperty("build.date"));
        props.put("release.version", project.getProperty("build.version.short"));
        props.put("release.versionName", project.getProperty("build.version.long"));
        String userName = System.getProperty("user.name");
        if (userName != null) {
            props.put("release." + key + ".username", userName);
        }
        props.store(new FileOutputStream(propFile), null);
    }

}
