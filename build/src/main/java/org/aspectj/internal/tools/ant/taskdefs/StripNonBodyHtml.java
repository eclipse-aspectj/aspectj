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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Task to convert html source files into files with only body content.
 *
 * <p> This task can take the following arguments:</p>
 *
 * <ul>
 *   <li>srcdir</li>
 *   <li>destdir</li>
 *   <li>include</li>
 *   <li>exclude</li>
 * </ul>
 *
 * <p>Of these arguments, only <b>sourcedir</b> is required.</p>
 *
 * <p> When this task executes, it will scan the srcdir based on the
 * include and exclude properties.</p>
 */

public class StripNonBodyHtml extends MatchingTask {

    private File srcDir;
    private File destDir = null;

    public void setSrcdir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    public void execute() throws BuildException {
        if (srcDir == null) {
            throw new BuildException("srcdir attribute must be set!");
        }
        if (!srcDir.exists()) {
            throw new BuildException("srcdir does not exist!");
        }
        if (!srcDir.isDirectory()) {
            throw new BuildException("srcdir is not a directory!");
        }
        if (destDir != null) {
            if (!destDir.exists()) {
                throw new BuildException("destdir does not exist!");
            }
            if (!destDir.isDirectory()) {
                throw new BuildException("destdir is not a directory!");
            }
        }

        DirectoryScanner ds = super.getDirectoryScanner(srcDir);
        String[] files = ds.getIncludedFiles();

        log("stripping " + files.length + " files");
        int stripped = 0;
		for (String file : files) {
			if (processFile(file)) {
				stripped++;
			} else {
				log(file + " not stripped");
			}
		}
        log(stripped + " files successfully stripped");
    }

    boolean processFile(String filename) throws BuildException {
        File srcFile = new File(srcDir, filename);
        File destFile;
        if (destDir == null) {
            destFile = srcFile;
        } else {
            destFile = new File(destDir, filename);
            destFile.getParentFile().mkdirs();
        }
        try {
            return strip(srcFile, destFile);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private boolean strip(File f, File g) throws IOException {
        BufferedInputStream in =
            new BufferedInputStream(new FileInputStream(f));
        String s = readToString(in);
        in.close();
        return writeBodyTo(s, g);
    }

    private ByteArrayOutputStream temp = new ByteArrayOutputStream();
    private byte[] buf = new byte[2048];

    private String readToString(InputStream in) throws IOException {
        ByteArrayOutputStream temp = this.temp;
        byte[] buf = this.buf;
        String s = "";
        try {
            while (true) {
                int i = in.read(buf, 0, 2048);
                if (i == -1) break;
                temp.write(buf, 0, i);

            }
            s = temp.toString();
        } finally {
            temp.reset();
        }
        return s;
    }

    private boolean writeBodyTo(String s, File f) throws IOException {
        int start;//, end;
        try {
            start = findStart(s);
            findEnd(s, start);
        } catch (ParseException e) {
            return false; // if we get confused, just don't write the file.
        }
        s = processBody(s,f);
        BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(f));

        out.write(s.getBytes());
        out.close();
        return true;
    }

    /**
     * Process body. This implemenation strips text
     *   between  &lt!-- start strip --&gt
     *   and      &lt!-- end strip --&gt
     *   inclusive.
     */
    private String processBody(String body, File file) {
        if (null == body) return  body;
        final String START = "<!-- start strip -->";
        final String END = "<!-- end strip -->";
        return stripTags(body, file.toString(), START, END);
    }

   /**
     * Strip 0..n substrings in input: "s/${START}.*${END}//g"
     * @param input the String to strip
     * @param source the name of the source for logging purposes
     * @param start the starting tag (case sensitive)
     * @param end the ending tag (case sensitive)
     */
    String stripTags(String input, final String SOURCE, 
                     final String START, final String END) {
        if (null == input) return  input;
        StringBuffer buffer = new StringBuffer(input.length());
        String result = input;
        int curLoc = 0;
        while (true) {
            int startLoc = input.indexOf(START, curLoc);
            if (-1 == startLoc) {
                buffer.append(input.substring(curLoc));
                result = buffer.toString();
                break; // <------------ valid exit
            } else {
                int endLoc = input.indexOf(END, startLoc);
                if (-1 == endLoc) {
                    log(SOURCE + " stripTags - no end tag - startLoc=" + startLoc);
                    break; // <------------ invalid exit
                } else if (endLoc < startLoc) { 
                    log(SOURCE + " stripTags - impossible: startLoc=" 
                        + startLoc + " > endLoc=" + endLoc);
                    break; // <------------ invalid exit
                } else {
                    buffer.append(input.substring(curLoc, startLoc));
                    curLoc = endLoc + END.length();
                }
            }
        }
        return result;
    }

    private int findStart(String s) throws ParseException {
        int len = s.length();
        int start = 0;
        while (true) {
            start = s.indexOf("<body", start);
            if (start == -1) {
                start = s.indexOf("<BODY", start);
                if (start == -1) throw barf();
            }
            start = start + 5;
            if (start >= len) throw barf();
            char ch = s.charAt(start);
            if (ch == '>') return start + 1;
            if (Character.isWhitespace(ch)) {
                start = s.indexOf('>', start);
                if (start == -1) return -1;
                return start + 1;
            }
        }
    }

    private int findEnd(String s, int start) throws ParseException {
        int end;
        end = s.indexOf("</body>", start);
        if (end == -1) {
            end = s.indexOf("</BODY>", start);
            if (end == -1) throw barf();
        }
        return end;
    }

    private static class ParseException extends Exception {
        private static final long serialVersionUID = -1l;        
    }

    private static ParseException barf() {
        return new ParseException();
    }
}
