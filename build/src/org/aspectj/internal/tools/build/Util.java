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

 
package org.aspectj.internal.tools.build;

import java.io.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/** 
 * Build-only utilities.
 * Many mirror utils module APIs.
 */
public class Util {
    public static class Constants {
        public static final String TESTSRC = "testsrc";
        public static final String JAVA5_SRC = "java5-src";
    }
    // XXX quick hack for Java 5 support
    public static final boolean JAVA5_VM;
    static {
        boolean java5VM = false;
        try {
            java5VM = (null != Class.forName("java.lang.annotation.Annotation"));
        } catch (Throwable t) {
            // ignore
        }
        JAVA5_VM = java5VM;
    }

    /** 
     * Map version in long form to short, 
     * e.g., replacing "alpha" with "a"
     */
    public static String shortVersion(String version) {
        version = Util.replace(version, "alpha", "a");
        version = Util.replace(version, "beta", "b");
        version = Util.replace(version, "candidate", "rc");
        version = Util.replace(version, "development", "d");
        version = Util.replace(version, "dev", "d");
        return version;
    }
    
    /**
     * Replace any instances of {replace} in {input} with {with}.
     * @param input the String to search/replace
     * @param replace the String to search for in input
     * @param with the String to replace with in input
     * @return input if it has no replace, otherwise a new String
     */
    public static String replace(String input, String replace, String with) {
        int loc = input.indexOf(replace);
        if (-1 != loc) {
            String result = input.substring(0, loc);
            result += with;
            int start = loc + replace.length();
            if (start < input.length()) {
                result += input.substring(start);
            }
            input = result;
        }
        return input;
    }

    /** @return false if filter returned false for any file in baseDir subtree */
    public static boolean visitFiles(File baseDir, FileFilter filter) {
        Util.iaxIfNotCanReadDir(baseDir, "baseDir");
        Util.iaxIfNull(filter, "filter");
        File[] files = baseDir.listFiles();
        boolean passed = true;
        for (int i = 0; passed && (i < files.length); i++) {
			passed = files[i].isDirectory()
                ? visitFiles(files[i], filter)
                : filter.accept(files[i]);
		}
        return passed;
    }
    
    /** @throws IllegalArgumentException if cannot read dir */
    public static void iaxIfNotCanReadDir(File dir, String name) {
        if (!canReadDir(dir)) {
            throw new IllegalArgumentException(name + " dir not readable: " + dir);
        }
    }
    
    /** @throws IllegalArgumentException if cannot read file */
    public static void iaxIfNotCanReadFile(File file, String name) {
        if (!canReadFile(file)) {
            throw new IllegalArgumentException(name + " file not readable: " + file);
        }
    }
    
    /** @throws IllegalArgumentException if cannot write dir */
    public static void iaxIfNotCanWriteDir(File dir, String name) {
        if (!canWriteDir(dir)) {
            throw new IllegalArgumentException(name + " dir not writeable: " + dir);
        }
    }
    
    /** @throws IllegalArgumentException if input is null */
    public static void iaxIfNull(Object input, String name) {
        if (null == input) {
            throw new IllegalArgumentException("null " + name);
        }
    }
    
    /** render exception to String */
    public static String renderException(Throwable thrown) {
        if (null == thrown) {
            return "(Throwable) null";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println(thrown.getMessage());
        thrown.printStackTrace(pw); 
        pw.flush();
        return sw.getBuffer().toString(); 
    }
    
    /** @return true if dir is a writable directory */
    public static boolean canWriteDir(File dir) {
        return (null != dir) && dir.canWrite() && dir.isDirectory();
    }
    
    public static String path(String first, String second) {
        return first + File.separator + second;
    }

    public static String path(String[] segments) {
        StringBuffer sb = new StringBuffer();
        if ((null != segments)) {
            for (int i = 0; i < segments.length; i++) {
                if (0 < i) {
                    sb.append(File.separator);
                }
                sb.append(segments[i]);
            }
        }
        return sb.toString();
    }
    
    /** @return true if dir is a readable directory */
    public static boolean canReadDir(File dir) {
        return (null != dir) && dir.canRead() && dir.isDirectory();
    }
    
    /** @return true if dir is a readable file */
    public static boolean canReadFile(File file) {
        return (null != file) && file.canRead() && file.isFile();
    }
    
    /** 
     * Delete file or directory.
     * @param dir the File file or directory to delete.
     * @return true if all contents of dir were deleted 
     */
    public static boolean delete(File dir) {
        return deleteContents(dir) && dir.delete();
    }
    
    /** 
     * Delete contents of directory.
     * The directory itself is not deleted.
     * @param dir the File directory whose contents should be deleted.
     * @return true if all contents of dir were deleted 
     */
    public static boolean deleteContents(File dir) {
        if ((null == dir) || !dir.canWrite()) {
            return false;
        } else if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
				if (!deleteContents(files[i]) || !files[i].delete()) {
                    return false;
                }
			}
        }
        return true;
    }
    
    /** @return File temporary directory with the given prefix */
    public static File makeTempDir(String prefix) {
        if (null == prefix) {
            prefix = "tempDir";
        }
        File tempFile = null;
        for (int i = 0; i < 10; i++) {
            try {
                tempFile =  File.createTempFile(prefix,"tmp");       
                tempFile.delete();
                if (tempFile.mkdirs()) {
                    break;
                }
                tempFile = null;
            } catch (IOException e) {
            }
        }
        return tempFile;        
    }
    /**
     * Close stream with the usual checks.
     * @param stream the InputStream to close - ignored if null 
     * @return null if closed without IOException, message otherwise 
     */
    public static String close(Writer stream) {
        String result = null;
        if (null != stream) {
            try {
                stream.close();
            } catch(IOException e) {
                result = e.getMessage();
            }
        }
        return result;
    }

    /**
     * @param list the Object[] to test
     * @return true if list is null or empty
     */
    public static boolean isEmpty(Object[] list) {
        return ((null == list) || (0 == list.length));
    }

}

