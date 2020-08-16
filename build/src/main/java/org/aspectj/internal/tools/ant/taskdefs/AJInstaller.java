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

//XXX INCLUDES CODE FROM ANT -- UNDER APACHE LICENSE
package org.aspectj.internal.tools.ant.taskdefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;

@SuppressWarnings("deprecation")
public class AJInstaller extends MatchingTask {
    static final String INCLUDE_CLASSES = "$installer$/org/aspectj/*.class";
    static final String MAIN_CLASS = "$installer$.org.aspectj.Main";
    static final String CONTENTS_FILE = "$installer$/org/aspectj/resources/contents.txt";
    private String htmlSrc;

    public void setHtmlSrc(String v) { htmlSrc = v; }

    private String resourcesSrc;

    public void setResourcesSrc(String v) { resourcesSrc = v; }

    private String mainclass;

    public void setMainclass(String v) { mainclass = v; }

    private File installerClassJar;

    public void setInstallerclassjar(String v) {
        installerClassJar = project.resolveFile(v);
    }

    protected List<String> contentsNames = new ArrayList<>();

    protected long contentsBytes = 0;

    protected void addToContents(File file, String vPath) {
        contentsNames.add(vPath);
        contentsBytes += file.length();
    }

    String[] getFiles(File baseDir) {
        DirectoryScanner ds = new DirectoryScanner();
        setBasedir(baseDir.getAbsolutePath());
        ds.setBasedir(baseDir);
        //ds.setIncludes(new String [] {pattern});
        ds.scan();
        return ds.getIncludedFiles();
    }

    protected Copy getCopyTask() {
        Copy cd = (Copy)project.createTask("copy");
        if (null == cd) {
            log("project.createTask(\"copy\") failed - direct", Project.MSG_VERBOSE);
            cd = new Copy();
            cd.setProject(getProject());
        }
        return cd;
    }
    protected void finishZipOutputStream(ZipOutputStream zOut) throws IOException, BuildException {
        writeContents(zOut);
        writeManifest(zOut);
        File tempDir = setupTempDir();
        String tmp = tempDir.getAbsolutePath();

        // installer class files
        Expand expand = new Expand();
        expand.setProject(getProject());
        expand.setSrc(installerClassJar);
        expand.setDest(new File(tmp));
        PatternSet patterns = new PatternSet();
        patterns.setIncludes(INCLUDE_CLASSES);
        expand.addPatternset(patterns);
        expand.execute();

        // move the correct resource files into the jar
        Copy cd = getCopyTask();
        fileset = new FileSet();
        fileset.setDir(new File(resourcesSrc));
        fileset.setIncludes("*");
        fileset.setExcludes("contents.txt,properties.txt");
        cd.addFileset(fileset);
        cd.setTodir(new File(tmp+"/$installer$/org/aspectj/resources"));
        cd.execute();
        project.getGlobalFilterSet().addFilter("installer.main.class", this.mainclass);
        Copy cf = getCopyTask();
        fileset = new FileSet();
        fileset.setDir(new File(resourcesSrc));
        fileset.setIncludes("properties.txt");
        cf.setFiltering(true);
        cf.addFileset(fileset);
        cf.setTodir(new File(tmp+"/$installer$/org/aspectj/resources"));
        cf.execute();
        // move the correct resource files into the jar
        cd = getCopyTask();
        fileset = new FileSet();
        fileset.setDir(new File(htmlSrc));
        fileset.setIncludes("*");
        cd.addFileset(fileset);
        cd.setTodir(new File(tmp+"/$installer$/org/aspectj/resources"));
        cd.execute();
        // now move these files into the jar
        setBasedir(tmp);
        writeFiles(zOut, getFiles(tempDir));
        // and delete the tmp dir
        Delete dt = (Delete)project.createTask("delete");
        if (null == dt) {
            dt = new Delete();
            dt.setProject(getProject());
        }
        dt.setDir(tempDir);
        dt.execute();
        tempDir = null;
    }

    static final char NEWLINE = '\n';

    protected void writeContents(ZipOutputStream zOut) throws IOException {
        // write to a StringBuffer
        StringBuffer buf = new StringBuffer();
        buf.append(contentsBytes);
        buf.append(NEWLINE);
		for (String name : contentsNames) {
			buf.append(name);
			buf.append(NEWLINE);
		}
        zipFile(new StringBufferInputStream(buf.toString()), zOut, CONTENTS_FILE, System.currentTimeMillis());
    }

    protected void writeManifest(ZipOutputStream zOut) throws IOException {
        // write to a StringBuffer
        StringBuffer buf = new StringBuffer();
        buf.append("Manifest-Version: 1.0");
        buf.append(NEWLINE);
        buf.append("Main-Class: " + MAIN_CLASS);
        buf.append(NEWLINE);
        zipFile(new StringBufferInputStream(buf.toString()), zOut, "META-INF/MANIFEST.MF", System.currentTimeMillis());
    }

    //XXX cut-and-paste from Zip super-class (under apache license)
    private File zipFile;
    private File baseDir;
    private boolean doCompress = true;
    protected String archiveType = "zip";

    /**
     * This is the name/location of where to
     * create the .zip file.
     */
	public void setZipfile(String zipFilename) {
        zipFile = project.resolveFile(zipFilename);
    }

    /**
     * This is the base directory to look in for
     * things to zip.
     */
    public void setBasedir(String baseDirname) {
        baseDir = project.resolveFile(baseDirname);
    }

    /**
     * Sets whether we want to compress the files or only store them.
     */
    public void setCompress(String compress) {
        doCompress = Project.toBoolean(compress);
    }

    protected void initZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException
    {
    }

    protected void zipDir(File dir, ZipOutputStream zOut, String vPath)
        throws IOException
    {
    }

    protected void zipFile(InputStream in, ZipOutputStream zOut, String vPath,
                           long lastModified)
        throws IOException
    {
        ZipEntry ze = new ZipEntry(vPath);
        ze.setTime(lastModified);

        /*
         * XXX ZipOutputStream.putEntry expects the ZipEntry to know its
         * size and the CRC sum before you start writing the data when using
         * STORED mode.
         *
         * This forces us to process the data twice.
         *
         * I couldn't find any documentation on this, just found out by try
         * and error.
         */
        if (!doCompress) {
            long size = 0;
            CRC32 cal = new CRC32();
            if (!in.markSupported()) {
                // Store data into a byte[]
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8 * 1024];
                int count = 0;
                do {
                    size += count;
                    cal.update(buffer, 0, count);
                    bos.write(buffer, 0, count);
                    count = in.read(buffer, 0, buffer.length);
                } while (count != -1);
                in = new ByteArrayInputStream(bos.toByteArray());
            } else {
                in.mark(Integer.MAX_VALUE);
                byte[] buffer = new byte[8 * 1024];
                int count = 0;
                do {
                    size += count;
                    cal.update(buffer, 0, count);
                    count = in.read(buffer, 0, buffer.length);
                } while (count != -1);
                in.reset();
            }
            ze.setSize(size);
            ze.setCrc(cal.getValue());
        }
        zOut.putNextEntry(ze);
        byte[] buffer = new byte[8 * 1024];
        int count = 0;
        do {
            zOut.write(buffer, 0, count);
            count = in.read(buffer, 0, buffer.length);
        } while (count != -1);
    }

    protected void zipFile(File file, ZipOutputStream zOut, String vPath)
        throws IOException
    {
        if ( !vPath.startsWith("$installer$") ) {
            addToContents(file, vPath);
        }
        FileInputStream fIn = new FileInputStream(file);
        try {
            zipFile(fIn, zOut, vPath, file.lastModified());
        } finally {
            fIn.close();
        }
    }
    private File setupTempDir() throws BuildException {
        File tmpDirF = null;
        File tmpDir = null;
        try {
            tmpDirF = File.createTempFile("tgz", ".di");
            tmpDir = new File(tmpDirF.getParentFile(), "AJInstaller");
            tmpDirF.delete();
        } catch (IOException e) {
            // retrying below
        }
        if (null == tmpDir || !tmpDir.mkdirs()) {
            tmpDir = new File("AJInstaller.finishZipOutputStream.tmp");
            if (!tmpDir.mkdirs()) {
                throw new BuildException("unable to make temp dir");
            }
        }
        return tmpDir;
    }

    public void execute() throws BuildException {
        if (installerClassJar == null) {
            throw new BuildException("installerClassJar attribute must be set!");
        }
        if (!installerClassJar.canRead()
            || !installerClassJar.getPath().endsWith(".jar")) {
            throw new BuildException("not readable jar:" + installerClassJar);
        }
//        if (installerClassDir == null) {
//            throw new BuildException("installerClassDir attribute must be set!");
//        }
//        if (!installerClassDir.exists()) {
//            throw new BuildException("no such directory: installerClassDir=" + installerClassDir);
//        }
        if (baseDir == null) {
            throw new BuildException("basedir attribute must be set!");
        }
        if (!baseDir.exists()) {
            throw new BuildException("basedir does not exist!");
        }
        DirectoryScanner ds = super.getDirectoryScanner(baseDir);
        String[] files = ds.getIncludedFiles();
        String[] dirs  = ds.getIncludedDirectories();
        log("Building installer: "+ zipFile.getAbsolutePath());
        ZipOutputStream zOut = null;
        try {
            zOut = new ZipOutputStream(new FileOutputStream(zipFile));
            if (doCompress) {
                zOut.setMethod(ZipOutputStream.DEFLATED);
            } else {
                zOut.setMethod(ZipOutputStream.STORED);
            }
            initZipOutputStream(zOut);
            writeDirs(zOut, dirs);
            writeFiles(zOut, files);
            finishZipOutputStream(zOut); // deletes temp dir
        } catch (IOException ioe) {
            String msg = "Problem creating " + archiveType + " " + ioe.getMessage();
            throw new BuildException(msg, ioe, location);
        } finally {
            if (zOut != null) {
                try {
                    // close up
                    zOut.close();
                }
                catch (IOException e) {}
            }
        }
    }

    protected void writeDirs(ZipOutputStream zOut, String[] dirs) throws IOException {
		for (String dir : dirs) {
			File f = new File(baseDir, dir);
			String name = dir.replace(File.separatorChar, '/') + "/";
			zipDir(f, zOut, name);
		}
    }

    protected void writeFiles(ZipOutputStream zOut, String[] files) throws IOException {
		for (String file : files) {
			File f = new File(baseDir, file);
			String name = file.replace(File.separatorChar, '/');
			zipFile(f, zOut, name);
		}
    }

}
