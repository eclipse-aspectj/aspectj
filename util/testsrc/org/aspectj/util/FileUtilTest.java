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

package org.aspectj.util;

import java.io.EOFException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 */
public class FileUtilTest extends TestCase {
    public static final String[] NONE = new String[0];

    boolean log = false;

    /** List of File files or directories to delete when exiting */
    final ArrayList tempFiles;
    public FileUtilTest(String s) {
        super(s);
        tempFiles = new ArrayList();
    }
       
	public static void main(String[] args) {
        TestRunner.main(new String[] {"org.aspectj.util.FileUtilUT"});
	}
    
    public void tearDown() {
        for (ListIterator iter = tempFiles.listIterator(); iter.hasNext();) {
			File dir = (File) iter.next();
			FileUtil.deleteContents(dir);
            dir.delete();
            iter.remove();
		}
    }

    public void testNotIsFileIsDirectory() {
        File noSuchFile = new File("foo");
        assertTrue(!noSuchFile.isFile());
        assertTrue(!noSuchFile.isDirectory());
    }
    
    public void testCopyFiles() {
        // bad input
        Class iaxClass = IllegalArgumentException.class;
        
        checkCopyFiles(null, null, iaxClass, false);
        
        File noSuchFile = new File("foo");
        checkCopyFiles(noSuchFile, null, iaxClass, false);
        checkCopyFiles(noSuchFile, noSuchFile, iaxClass, false);
        
        File tempDir = FileUtil.getTempDir("testCopyFiles");
        tempFiles.add(tempDir);
        File fromFile = new File(tempDir, "fromFile");
        String err = FileUtil.writeAsString(fromFile, "contents of from file");
        assertTrue(err, null == err);
        checkCopyFiles(fromFile, null, iaxClass, false);
        checkCopyFiles(fromFile, fromFile, iaxClass, false);
        
        // file-file
        File toFile = new File(tempDir, "toFile");
        checkCopyFiles(fromFile, toFile, null, true);
        
        // file-dir
        File toDir= new File(tempDir, "toDir");
        assertTrue(toDir.mkdirs());
        checkCopyFiles(fromFile, toDir, null, true);
        
        // dir-dir        
        File fromDir= new File(tempDir, "fromDir");
        assertTrue(fromDir.mkdirs());
        checkCopyFiles(fromFile, fromDir, null, false);
        File toFile2 = new File(fromDir, "toFile2");
        checkCopyFiles(fromFile, toFile2, null, false);
        checkCopyFiles(fromDir, toDir, null, true);
    }
    
    void checkCopyFiles(File from, File to, Class exceptionClass, boolean clean) {
        try {
            FileUtil.copyFile(from, to);
            assertTrue(null == exceptionClass);
            if (to.isFile()) {
                assertTrue(from.length() == to.length()); // XXX cheap test
            } else {
                File toFile = new File(to, from.getName());                
                assertTrue(from.length() == toFile.length()); 
            }
        } catch (Throwable t) {
            assertTrue(null != exceptionClass);
            assertTrue(exceptionClass.isAssignableFrom(t.getClass()));
        } finally {
            if (clean && (null != to) && (to.exists())) {
                if (to.isDirectory()) {
                    FileUtil.deleteContents(to);
                }
                to.delete();
            }
        }
    }
    public void testDirCopySubdirs() throws IOException { // XXX dir diff
        File srcDir = new File("src");
        File destDir = FileUtil.getTempDir("testDirCopySubdirs");
        FileUtil.copyDir(srcDir, destDir);
        FileUtil.deleteContents(destDir);
        destDir.delete();
    }

    public void testDirCopySubdirsSuffix() throws IOException { // XXX dir diff
        File srcDir = new File("src");
        File destDir = FileUtil.getTempDir("testDirCopySubdirsSuffix");
        FileUtil.copyDir(srcDir, destDir, ".java", ".aj");

        FileUtil.deleteContents(destDir);
        destDir.delete();
    }
        
    public void testGetURL() {
        String[] args = new String[] 
           {".", "../util/testdata", "../lib/test/aspectjrt.jar" }; 
        for (int i = 0; i < args.length; i++) {
            checkGetURL(args[i]);
		}
    }    

    /**
     * Method checkGetURL.
     * @param string
     * @param uRL
     */
    private void checkGetURL(String arg) {
        assertTrue(null != arg);
        File f = new File(arg);
        assertTrue(null != f);
        URL url = FileUtil.getFileURL(f);
        assertTrue(null != url);
        log("url       " + url);
        if (!f.exists()) {
            log("not exist        " + f);
        } else if (f.isDirectory()) {
            log("directory        " + f);
        } else {
            log("     file        " + f);
            InputStream in = null;
            try {
                in = url.openStream();
            } catch (IOException e) {
                assertTrue("IOException: " + e, false);
            } finally {
                if (null != in) {
                    try { in.close(); }
                    catch (IOException e) {} 
                }
            }
        }
    }

        
    public void testGetTempDir() {
        boolean pass = true;
        boolean delete  = true;
        checkGetTempDir("parent", null, pass, delete);
        checkGetTempDir(null, "child", pass, delete);
        tempFiles.add(checkGetTempDir("parent", "child", pass, !delete));
        tempFiles.add(checkGetTempDir("parent", "child", pass, !delete));
        tempFiles.add(checkGetTempDir("parent", "child", pass, !delete));
    }

    File checkGetTempDir(String parent, String child, boolean ok, boolean delete) {
        File parentDir = FileUtil.getTempDir(parent);
        assertTrue("unable to create " + parent, null != parentDir);
        File dir = FileUtil.makeNewChildDir(parentDir, child);
        log("parent=" + parent + " child=" + child + " -> " + dir);
        assertTrue("dir: " + dir, ok == (dir.canWrite() && dir.isDirectory()));
        if (delete) {
            dir.delete();
        }
        return dir;
    }
    
    public void testRandomFileString() {
        ArrayList results = new ArrayList();
        for (int i = 0; i < 1000; i++) {
            String s = FileUtil.randomFileString();
            if (results.contains(s)) {
                System.err.println("warning: got duplicate at iteration " + i);
            }
            results.add(s);
//			System.err.print(" " + s);
//            if (0 == (i % 5)) {
//                System.err.println("");
//            }
		}
    }
    
    public void testNormalizedPath() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("FileUtilTest", "tmp");
        } catch (IOException e) {
            System.err.println("aborting test - unable to create temp file");
        }
        File parentDir = tempFile.getParentFile();
        String tempFilePath = FileUtil.normalizedPath(tempFile, parentDir);
        assertEquals(tempFile.getName(), tempFilePath);
    }

    public void testFileToClassName() {

        File basedir = new File("/base/dir");
        File classFile = new File(basedir, "foo/Bar.class");
        assertEquals("foo.Bar", FileUtil.fileToClassName(basedir, classFile));

        classFile = new File(basedir, "foo\\Bar.class");
        assertEquals("foo.Bar", FileUtil.fileToClassName(basedir, classFile));

        assertEquals("Bar", FileUtil.fileToClassName(null, classFile));

        classFile = new File("/home/classes/org/aspectj/lang/JoinPoint.class");
        assertEquals("org.aspectj.lang.JoinPoint", FileUtil.fileToClassName(null, classFile));

        classFile = new File("/home/classes/com/sun/tools/Javac.class");
        assertEquals("com.sun.tools.Javac", FileUtil.fileToClassName(null, classFile));
     }

    public void testDeleteContents() {
        File f = new File("testdata/foo");
        f.mkdirs();
        File g = new File(f, "bar");
        g.mkdirs();
        File h = new File(g, "bash");
        h.mkdirs();
        int d = FileUtil.deleteContents(f);
        assertTrue(0 == d);
        assertTrue(0 == f.list().length);
        f.delete();
        assertTrue(!f.exists());
    }
    
    public void testLineSeek() {
        String path = "testdata/testLineSeek";
        File file = new File(path);
        path = file.getPath();
        String contents = "0123456789" + LangUtil.EOL;
        contents += contents;
        FileUtil.writeAsString(file, contents);
        tempFiles.add(file);
        List sourceList = new ArrayList();
        sourceList.add(file.getPath());
        
        final ArrayList errors = new ArrayList();
        final PrintStream errorSink = new PrintStream(System.err, true) {
            public void println(String error) {
                errors.add(error);
            }
        };
        for (int i = 0; i < 10; i++) {
            List result = FileUtil.lineSeek(""+i, sourceList, true, errorSink);
            assertEquals(2, result.size());
            assertEquals(path + ":1:" + i, result.get(0));
            assertEquals(path + ":2:" + i, result.get(1));
            if (!LangUtil.isEmpty(errors)) { // XXX prefer fast-fail?
                assertTrue("errors: " + errors, false);
            }
		}
        
    }
     public void testLineSeekMore() {
        final int MAX = 3; // 1..10
        final String prefix = new File("testdata/testLineSeek").getPath();
        // setup files 0..MAX with 2*MAX lines
        String[] sources = new String[MAX];
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < sources.length; i++) {
            sources[i] = new File(prefix + i).getPath();
            sb.append("not matched");
            sb.append(LangUtil.EOL);
            sb.append("0123456789");
            sb.append(LangUtil.EOL);
        }
        final String contents = sb.toString();
        for (int i = 0; i < sources.length; i++) {
            File file = new File(sources[i]);
            FileUtil.writeAsString(file, contents);
            tempFiles.add(file);
		}
        // now test
        final ArrayList errors = new ArrayList();
        final PrintStream errorSink = new PrintStream(System.err, true) {
			public void println(String error) {
                errors.add(error);
			}
        };
        List sourceList = new ArrayList();
        sourceList.addAll(Arrays.asList(sources));
        sourceList = Collections.unmodifiableList(sourceList);
        for (int k = 0; k < sources.length; k++) {
            List result = FileUtil.lineSeek(""+k, sourceList, true, errorSink);            
            // number k found in every other line of every file at index k
            Iterator iter = result.iterator();
            for (int i = 0; i < MAX; i++) {           // for each file
                for (int j = 1; j < (MAX+1); j++) {   // for every other line
                    assertTrue(iter.hasNext());
                    assertEquals(prefix + i + ":" + 2*j + ":" + k, iter.next());
                }           
            }
            if (!LangUtil.isEmpty(errors)) { // XXX prefer fast-fail?
                assertTrue("errors: " + errors, false);
            }
		}        
    }
    
    public void testDirCopyNoSubdirs() throws IOException {
        String[] srcFiles = new String[] { "one.java", "two.java", "three.java"};
        String[] destFiles = new String[] { "three.java", "four.java", "five.java" };
        String[] allFiles = new String[] 
            { "one.java", "two.java", "three.java", "four.java", "five.java" };
        File srcDir = makeDir("FileUtilUT_srcDir", srcFiles);    
        File destDir = makeDir("FileUtilUT_destDir", destFiles);
        assertTrue(null != srcDir);
        assertTrue(null != destDir);
        assertTrue(NONE == dirContains(srcDir, srcFiles));
        assertTrue(NONE == dirContains(destDir, destFiles));
        
        FileUtil.copyDir(srcDir, destDir);
        String[] resultOne = dirContains(destDir, allFiles);
        FileUtil.copyDir(srcDir, destDir);
        String[] resultTwo = dirContains(destDir, allFiles);

        FileUtil.deleteContents(srcDir);
        FileUtil.deleteContents(destDir);
        srcDir.delete();
        destDir.delete();

        assertTrue(NONE == resultOne);
        assertTrue(NONE == resultTwo);
    }
    
    public void testDirCopyNoSubdirsWithSuffixes() throws IOException {
        String[] srcFiles = new String[] { "one.java", "two.java", "three.java"};
        String[] destFiles = new String[] { "three.java", "four.java", "five.java" };
        String[] allFiles = new String[] 
            { "one.aj", "two.aj", "three.aj", "three.java", "four.java", "five.java" };
        File srcDir = makeDir("FileUtilUT_srcDir", srcFiles);    
        File destDir = makeDir("FileUtilUT_destDir", destFiles);
        assertTrue(null != srcDir);
        assertTrue(null != destDir);
        assertTrue(NONE == dirContains(srcDir, srcFiles));
        assertTrue(NONE == dirContains(destDir, destFiles));
        
        FileUtil.copyDir(srcDir, destDir, ".java", ".aj");
        String[] resultOne = dirContains(destDir, allFiles);
        FileUtil.copyDir(srcDir, destDir, ".java", ".aj");
        String[] resultTwo = dirContains(destDir, allFiles);

        FileUtil.deleteContents(srcDir);
        FileUtil.deleteContents(destDir);
        srcDir.delete();
        destDir.delete();

        assertTrue(NONE == resultOne);
        assertTrue(NONE == resultTwo);
    }
    
    public void testDirCopySubdirsSuffixRoundTrip() throws IOException { 
        final File srcDir = new File("src");
        final File one = FileUtil.getTempDir("testDirCopySubdirsSuffixRoundTrip_1");
        final File two = FileUtil.getTempDir("testDirCopySubdirsSuffixRoundTrip_2");
        FileUtil.copyDir(srcDir, one); // no selection
        FileUtil.copyDir(two, one, ".java", ".aj"); // only .java files
        FileUtil.copyDir(one, two, ".aj", ".java");
        
        FileUtil.deleteContents(one);
        one.delete();
        FileUtil.deleteContents(two);
        two.delete();
    }
    
    /** 
     * Verify that dir contains files, 
     * and return the names of other files in dir.
     * @return the contents of dir after excluding files 
     *           or NONE if none
     * @throws AssertionFailedError if any files are not in dir
     */
    String[] dirContains(File dir, final String[] files) {
        final ArrayList toFind = new ArrayList(Arrays.asList(files));
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File d, String name) {
                return !toFind.remove(name);
            }
        };
        String[] result = dir.list(filter);
        if (0 < toFind.size()) {
            assertTrue(""+toFind, false);
        }        
        return (result.length == 0 ? NONE : result);
    }
    
    File makeDir(String loc, String[] files) throws IOException {
        File d = new File(loc);
        d.mkdirs();
        assertTrue(d.exists());
        assertTrue(d.canWrite());
        for (int i = 0; i < files.length; i++) {
			File f = new File(d, files[i]);
            assertTrue(files[i], f.createNewFile());
		}
        return d;
    }

    private void log(String s) {
        if (log) {
            System.err.println(s);
        }
    }
}
