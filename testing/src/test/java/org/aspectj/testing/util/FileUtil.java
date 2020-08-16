/* *******************************************************************
 * Copyright (c) 1999-2000 Xerox Corporation. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.testing.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** 
 * misc file utilities
 */
public class FileUtil {

    /** default filename if URL has none (i.e., a directory URL): index.html */
    public static final String DEFAULT_URL_FILENAME = "index.html";

    /**
     * @param args the String[] 
     *   <code>{ "-copy", "-srcFile" | "-srcUrl", {src}, "-destFile", {destFile} }</code>
     */
    public static void main (String[] args) {
        if (null == args) return;
        for (int i = 0; (i+4) < args.length; i++) {
            if ("-copy".equals(args[i])) {
                String arg = args[++i];
                String src = null;
                String destFile = null;
                boolean srcIsFile = ("-srcFile".equals(arg));
                if (srcIsFile) {
                    src = args[++i];
                } else if ("-srcUrl".equals(arg)) {
                    src = args[++i];
                }
                if ((null != src) && ("-destFile".equals(args[++i]))) {
                    destFile = args[++i];
                    StringBuffer errs = new StringBuffer();
                    if (srcIsFile) {
                        copyFile(new File(src), new File(destFile), errs);
                    } else {
                        URL url = null;
                        try { url = new URL(src) ; }
                        catch (MalformedURLException e) { render(e, errs); }
                        if (null != url) {
                            copyURL(url, new File(destFile), errs);
                        }
                    }
                    if (0 < errs.length()) {
                        System.err.println("Error copying " + src + " to " + destFile);
                        System.err.println(errs.toString());
                            
                    }
                }
            } // ("-copy".equals(args[i])){
        } 
    } // end of main ()
    
    /**
     * Generate a list of missing and extra files by comparison to a 
     * timestamp, optionally excluding certain files.
     * This is a call to select all files after a given time:
     * 
     * <pre>Diffs d = dirDiffs(dir, givenTime, null, null, null);</pre> 
     * 
     * Given files
     * <pre>classes/Foo.class
     * classes/bar/Bash.class
     * classes/Old.class
     * classes/one/Unexpected.class
     * classes/start.gif</pre>
     * where only Old.class predated startTime, this is a call that 
     * reports "one/Unexpected.class" as unexpected and "Foo"
     * as missing:
     * <pre>String requireSuffix = ".class";
     * String[] expectedPaths = new String[] { "Foo", "bar/Bas" };
     * File file = new File("classes");
     * Diffs d = dirDiffs(dir, startTime, requireSuffix,expectedPaths, true);</pre> 
     * 
     * @param label the String to use for the Diffs label
     * @param dir the File for the dir to search
     * @param startTime collect files modified after this time
     *         (ignored if less than 0)
     * @param requireSuffix ignore all actual files without this suffix
     *         (ignored if null)
     * @param expectedPaths paths (relative to dir) of the expected files
     *         (if null, none expected)
     * @param acceptFilePrefix if true, then accept a file which
     *         differs from an expected file name only by a suffix
     *         (which need not begin with ".").
     */
    public static Diffs dirDiffs( // XXX too complicated, weak prefix checking
        final String label,
        final File dir, 
        final long startTime, 
        final String requiredSuffix, 
        final String[] expectedPaths, 
        final boolean acceptFilePrefix) {
        
        LangUtil.throwIaxIfNull(dir, "dir");
        final boolean checkExpected = !LangUtil.isEmpty(expectedPaths);
        
        // normalize sources to ignore
        final List expected = (!checkExpected ? null : new ArrayList());
        if (checkExpected) {
			for (String srcPath : expectedPaths) {
				if (!LangUtil.isEmpty(srcPath)) {
					expected.add(org.aspectj.util.FileUtil.weakNormalize(srcPath));
				}
			}
        }
        
        // gather, normalize paths changed
        FileFilter touchedCollector = new FileFilter() {
            /** 
             * For files complying with time and suffix rules,
             * return true (accumulate - unexpected) 
             * unless they match expected files,
             * (deleting any matches from sources
             * so the remainder is missing).
             * @return true for unexpected files after date */
            public boolean accept(File file) {
                if (file.isFile()
                    && ((0 > startTime) 
                        || (startTime < file.lastModified()))) {
                    String path = file.getPath();
                    if ((null == requiredSuffix) || path.endsWith(requiredSuffix)) {                        
                        path = org.aspectj.util.FileUtil.weakNormalize(path);
                        if (checkExpected) {
                            if (!acceptFilePrefix) {
                                // File.equals(..) does lexical compare
                                if (expected.contains(path)) {
                                    expected.remove(path);
                                    // found - do not add to unexpected
                                    return false; 
                                }                      
                            } else {
								for (Object o : expected) {
									String exp = (String) o;
									if (path.startsWith(exp)) {
										String suffix = path.substring(exp.length());
										if (!suffix.contains("/")) { // normalized...
											expected.remove(path);
											// found - do not add to unexpected
											return false;
										}
									}
								}
                            }
                        }
                        // add if is file, right time, and have or don't need suffix
                        return true;
                    }
                }
                // skip if not file or not right time
                return false;
            }
        };
		List unexp = new ArrayList(Arrays.asList(dir.listFiles(touchedCollector)));
        
        // report any unexpected changes
        return Diffs.makeDiffs(label, expected, unexp, String.CASE_INSENSITIVE_ORDER);
    }


    /**
     * Visit the entries in a zip file, halting when visitor balks.
     * Errors are silently ignored.
     * @throws IllegalArgumentException if zipfile or visitor is null
     */
    public static void visitZipEntries(ZipFile zipfile, StringVisitor visitor) {
        visitZipEntries(zipfile, visitor, (StringBuffer) null);
    }
        
    /**
     * Visit the entries in a zip file, halting when visitor balks.
     * Errors are reported in errs, if not null.
     * @throws IllegalArgumentException if zipfile or visitor is null
     */
    public static void visitZipEntries(ZipFile zipfile, StringVisitor visitor, 
                                       StringBuffer errs) {
        if (null == zipfile) throw new IllegalArgumentException("null zipfile");
        if (null == visitor) throw new IllegalArgumentException("null visitor");
        int index = 0;
        try {
            Enumeration enu = zipfile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enu.nextElement();
                index++;
                if (! visitor.accept(entry.getName())) {
                    break;
                }
            }
        } catch (Throwable e) {  
            if (null != errs) {
                errs.append("FileUtil.visitZipEntries error accessing entry " + index 
                         + ": " + e.getMessage());
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                errs.append(sw.toString());
            }
        } finally {
            if (null != zipfile) {
                try { zipfile.close(); } 
                catch (IOException x) {} // ignore
            }
        }
    }

    /**
	 * descend filesystem tree, invoking FileFilter.accept() on files.
	 * E.g., To list files from current directory:
	 * <code><pre>descendFileTree(new File("."), new FileFilter() {
	 *     public boolean accept(File f){
	 *        System.out.println(f.getAbsolutePath());
     *        return true;
	 *     }});</code></pre>
	 * @param file root/starting point.  If a file, the only one visited.
	 * @param filter supplies accept(File) routine
	 */
    public static void descendFileTree(File file, FileFilter filter) {
        descendFileTree(file, filter, false);
    }

    /**
     * Descend filesystem tree, invoking FileFilter.accept() on files
     * and, if userRecursion, on dirs. If userRecursion, accept() must
     * call descendFileTree() again to recurse down directories.
     * This calls fileFilter.accept(File) on all files before doing any dirs.
     * E.g., To list only files from Unix root:
     * <code><pre>descendFileTree(new File("/"), new FileFilter() {
     *     public boolean run(File f){
     *        System.out.println(f.getAbsolutePath());
     *        return true;
     *     }}, false);</code></pre>
     * To list files/dir from root using user recursion:
     * <code><pre>descendFileTree(new File("/"), new FileFilter() {
     *     public boolean run(File f){ 
     *        System.out.println(f.getAbsolutePath());
     *        if (f.isDirectory() && (-1 == f.getName().indexOf("CVS")))
     *           return descendFileTree(f, this, true);
     *        return true;
     *     }}, true);</code></pre>
     * @param file root/starting point.  If a file, the only one visited.
     * @param filter supplies boolean accept(File) method
     * @param userRecursion - if true, do accept() on dirs; else, recurse
     * @return false if any fileFilter.accept(File) did.
     * @throws IllegalArgumentException if file or fileFilter is null
     */
    public static boolean descendFileTree(File file, FileFilter fileFilter, 
                                          boolean userRecursion) {
        if (null == file) {throw new IllegalArgumentException("parm File"); }
        if (null == fileFilter){throw new IllegalArgumentException("parm FileFilter");}

        if (!file.isDirectory()) {
            return fileFilter.accept(file);
        } else if (file.canRead()) { 
            // go through files first
            File[] files = file.listFiles(ValidFileFilter.FILE_EXISTS); 
            if (null != files) {
				for (File value : files) {
					if (!fileFilter.accept(value)) {
						return false;
					}
				}
            }
            // now recurse to handle directories
            File[] dirs = file.listFiles(ValidFileFilter.DIR_EXISTS);
            if (null != dirs) {
				for (File dir : dirs) {
					if (userRecursion) {
						if (!fileFilter.accept(dir)) {
							return false;
						}
					} else {
						if (!descendFileTree(dir, fileFilter, userRecursion)) {
							return false;
						}
					}
				}
            }
        } // readable directory (ignore unreadable ones) 
        return true;
    } // descendFiles

    /**
     * Return the names of all files below a directory.
     * If file is a directory, then all files under the directory
     * are returned.  If file is absolute or relative, all the files are.
     * If file is a zip or jar file, then all entries in the zip or jar
     * are listed.  Entries inside those jarfiles/zipfiles are not listed.
     * There are no guarantees about ordering.
     * @param dir the File to list for
     * @param results the Collection to use for the results (may be null)
     * @throws IllegalArgumentException if null == dir 
     * @return a Collection of String of paths, including paths inside jars
     */
    public static Collection<String> directoryToString(File dir, Collection results) {
        if (null == dir) throw new IllegalArgumentException("null dir");
        final Collection<String> result = (results != null? results : new Vector());
        if (isZipFile(dir)) {
            zipFileToString(dir, result);
        } else if (!dir.isDirectory()) {
            throw new IllegalArgumentException("not a dir: " + dir);
        } else {
            AccumulatingFileFilter acFilter = new AccumulatingFileFilter() {
                    public boolean accumulate(File file) {
                        String name = file.getPath();
                        result.add(name);
                        if (isZipFile(file)) {
                            zipFileToString(file, result);
                        }
                        return true;
                    }
                };
            descendFileTree(dir, acFilter, false);
        }
        return result;
    } // directoryToString

    /**
     * Render as String the entries in a zip or jar file,
     * converting each to String beforehand (as jarpath!jarentry)
     * applying policies for whitespace, etc.
     * @param file the File to enumerate ZipEntry for
     * @param results the Colection to use to return the FileLine - may be null
     * @return FileLines with string as text and 
     *        canonical as string modified by any canonicalizing policies.
     */
    public static Collection zipFileToString(final File zipfile, Collection results) {
        Collection result = (results != null ? results : new Vector());
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipfile); // ZipFile.OPEN_READ| ZipFile.OPEN_DELETE); delete is 1.3 only
            Enumeration enu = zip.entries();
            while (enu.hasMoreElements()) {
                results.add(renderZipEntry(zipfile, (ZipEntry) enu.nextElement()));
            }
            zip.close();
            zip = null;
        } catch (Throwable t) {
            String err = "Error opening " + zipfile + " attempting to continue...";
            System.err.println(err);
            t.printStackTrace(System.err);
        } finally {
            if (null != zip) {
                try { zip.close(); }
                catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return result;
    }

    /**
     * @return true if file represents an existing file with a zip extension
     */
    public static boolean isZipFile(File f) {
        String s = null;
        if ((null == f)  || (null == (s = f.getPath()))) {
            return false;
        } else {
            return (f.canRead() && !f.isDirectory() 
                    && (s.endsWith(".zip")
                        || (s.endsWith(".jar"))));
        }
    }

    /**
     * Render a zip/entry combination to String
     */
    public static String renderZipEntry(File zipfile, ZipEntry entry) {
        String filename = (null == zipfile ? "null File" : zipfile.getName());
        String entryname = (null == entry ? "null ZipEntry" : entry.getName());
        return filename + "!" + entryname;
    }

    /**
     * Write all files in directory out to jarFile
     * @param jarFile the File to create and write to 
     * @param directory the File representing the directory to read
     * @param mainClass the value of the main class attribute - may be null
     */
    public static boolean createJarFile(File jarFile, File directory,
                                        String mainClass, FileFilter filter) {
        String label = "createJarFile("+jarFile
            +","+directory +","+mainClass +","+filter + "): ";
        Log.signal(label + " start");
        if (null == directory) 
            throw new IllegalArgumentException("null directory");
        Manifest manifest = createManifest(mainClass);
        Log.signal(label + " manifest=" + manifest);
        JarOutputStream out =  null;
        try {
            File jarFileDir = jarFile.getParentFile();
            if (null == jarFileDir) {
                Log.signal(label + " null jarFileDir");
            } else if (!jarFileDir.exists() && !jarFileDir.mkdirs()) { // XXX convert to Error
                Log.signal(label + " unable to create jarFileDir: " + jarFileDir); 
            }
            OutputStream os = new FileOutputStream(jarFile);
            out = (null == manifest ? new JarOutputStream(os)
                : new JarOutputStream(os, manifest));
            Log.signal(label + " out=" + out);
            ZipAccumulator reader = new ZipAccumulator(directory, out, filter);
            Log.signal(label + " reader=" + reader);
            FileUtil.descendFileTree(directory, reader);
            out.closeEntry();
            return true;
        } catch (IOException e) {
            e.printStackTrace(System.err); // todo
        } finally {
            if (null != out) {
                try { out.close();}
                catch (IOException e) {} // todo ignored
            }
        }
                                   
        return false;
    }    

    protected static Manifest createManifest(String mainClass) {
        final String mainKey = "Main-Class";
        Manifest result = null;
        if (null != mainClass) {
            String entry = "Manifest-Version: 1.0\n"
                + mainKey + ": " + mainClass + "\n";
            try {
                result = new Manifest(new StringBufferInputStream(entry));
                Attributes attributes = result.getMainAttributes();
                String main = attributes.getValue(mainKey);
                if (null == main) {
                    attributes.putValue(mainKey, mainClass);
                    main = attributes.getValue(mainKey);
                    if (null == main) {
                        Log.signal("createManifest unable to set main " 
                                   + mainClass);
                    }
                }
            } catch (IOException e) { // todo ignoring
                Log.signal(e, " IOException creating manifest with " + mainClass);
            }
        }
        return result;
    }


    /** read a file out to the zip stream */
    protected static void addFileToZip(File in, File parent,
                                       ZipOutputStream out) 
        throws IOException {
        String path = in.getCanonicalPath();
        String parentPath = parent.getCanonicalPath();
        if (!path.startsWith(parentPath)) {
            throw new Error("not parent: " + parentPath + " of " + path);
        } else {
            path = path.substring(1+parentPath.length());
            path = path.replace('\\', '/'); // todo: use filesep
        }
        ZipEntry entry = new ZipEntry(path);
        entry.setTime(in.lastModified());
        // todo: default behavior is DEFLATED

        out.putNextEntry(entry);

        InputStream input = null;
        try {
            input = new FileInputStream(in);
            byte[] buf = new byte[1024];
            int count;
            while (0 < (count = input.read(buf, 0, buf.length))) {
                out.write(buf, 0, count);
            }
        } finally {
            if (null != input) input.close(); 
        }
    }    


    public static void returnTempDir(File dir) {
        deleteDirectory(dir);
    }

    /** @return true if path ends with gif, properties, jpg */
    public static boolean isResourcePath(String path) {
        if (null == path) return false;
        path = path.toLowerCase();
        return (path.endsWith(".gif")
                || path.endsWith(".properties")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".props")
                );
    }

    public static void render(Throwable t, StringBuffer err) { // todo: move
        String name = t.getClass().getName();
        int loc = name.lastIndexOf(".");
        name = name.substring(1+loc);
        err.append(name + ": " + t.getMessage() + "\n"); // todo
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        err.append(sw.toString());
    }

    private static boolean report(StringBuffer err, String context, String status, 
                                  Throwable throwable) {
        boolean failed = ((null != status) || (null != throwable));
        if ((null != err) && (failed)) {
            if (null != context) {
                err.append(context);
            }
            if (null != status) {
                err.append(status);
            }
            if (null != throwable) {
                render(throwable, err);
            }
        }
        return failed;
    }

    /** 
     * Copy file.
     * @param src the File to copy - must exist
     * @param dest the File for the target file or directory (will not create directories)
     * @param err the StringBuffer for returning any errors - may be null
     **/
    public static boolean copyFile(File src, File dest, StringBuffer err) {
        boolean result = false;
        String label = "start";
        Throwable throwable = null;
        try {
            if (!ValidFileFilter.FILE_EXISTS.accept(src)) {
                label = "src file does not exist";
            } else {
                if (dest.isDirectory()) {
                    dest = new File(dest, src.getName());
                }
                if (ValidFileFilter.FILE_EXISTS.accept(dest)) {
                    label = "dest file exists";
                } 
                boolean closeWhenDone = true;
                result = copy(new FileInputStream(src),
                              new FileOutputStream(dest),
                              closeWhenDone);
            }
            label = null;
        } catch (Throwable t) {
            throwable = t;
        }
        String context = "FileUtil.copyFile(src, dest, err)"; 
        boolean report = report(err, context, label, throwable); 
        return (result && !report);
    }

    /** 
     * Copy URL to file.
     * @param src the URL to copy - must exist
     * @param dest the File for the target file or directory (will not create directories)
     * @param err the StringBuffer for returning any errors - may be null
     **/
    public static boolean copyURL(URL url, File dest, StringBuffer err) { // todo untested.
        boolean result = false;
        String label = "start";
        Throwable throwable = null;
        try {
            if (dest.isDirectory()) {
                String filename = url.getFile();
                if ((null == filename) || (0 == filename.length())) {
                    filename = DEFAULT_URL_FILENAME;
                }
                dest = new File(dest, filename);
            }
            if (ValidFileFilter.FILE_EXISTS.accept(dest)) {
                label = "dest file exists";
            } 
            boolean closeWhenDone = true;
            result = copy(url.openConnection().getInputStream(),
                          new FileOutputStream(dest),
                          closeWhenDone);
            label = null;
        } catch (Throwable t) {
            throwable = t;
        }
        String context = "FileUtil.copyURL(src, dest, err)"; // add actual parm to labels?
        boolean report = report(err, context, label, throwable); 
        return (result && report);
    }

     /** 
     * Copy input to output - does not close either
     * @param src the InputStream to copy - must exist
     * @param dest the OutputStream for the target 
     * @param close if true, close when done
     */
    public static boolean copy(InputStream src, OutputStream dest, 
                               boolean close) 
        throws IOException {
        boolean result = false;
        IOException throwable = null;
        try {
            byte[] buf = new byte[8*1024];
            int count;
            while (0 < (count = src.read(buf, 0, buf.length))) {
                dest.write(buf, 0, count);
            }
            result = true;
        } catch (IOException t) {
            throwable = t;
        } finally {
            if (close) {
                try { if (null != src) src.close(); } 
                catch (IOException e) {
                    if (null == throwable) { throwable = e; }
                }
                try { if (null != dest) dest.close(); } 
                catch (IOException i) {
                    if (null == throwable) { throwable = i; }
                }
            }
        }
        if (null != throwable) throw throwable;
        return result;
    }

    /**
     * @return true if dir was an existing directory that is now deleted
     */
    protected static boolean deleteDirectory(File dir) {
        return ((null != dir) 
                && dir.exists()
                && dir.isDirectory()
                && FileUtil.descendFileTree(dir, DELETE_FILES, false)
                && FileUtil.descendFileTree(dir, DELETE_DIRS, true)
                && dir.delete());
    }

	public static String[] getPaths(File[] files) { // util
        String[] result = new String[files.length];
        for (int i = 0; i < result.length; i++) {
			result[i] = files[i].getPath(); // preserves absolute?
		}
        return result;
    }

    //-------- first-order, input and visible interface  

    protected static final FileFilter DELETE_DIRS = new FileFilter() {
            public boolean accept(File file) {
                return ((null != file) && file.isDirectory()
                        && file.exists() && file.delete());
            }
        };
    protected static final FileFilter DELETE_FILES = new FileFilter() {
            public boolean accept(File file) {
                return ((null != file) && !file.isDirectory()
                        && file.exists() && file.delete());
            }
        };

} // class FileUtil

/** 
 * Localize FileUtil log/signals for now 
 * ordinary signals are ignored,
 * but exceptions are printed to err
 * and errors are thrown as Error
  */
class Log {
    /** ordinary logging - may be suppressed */
    public static final void signal(String s) {
        //System.err.println(s);
    }
    /** print stack trace to System.err */
    public static final void signal(Throwable t, String s) {
        System.err.println(s);
        t.printStackTrace(System.err);
    }
    /** @throws Error(s) always */
    public static final void error(String s) {
       throw new Error(s);
    }
}

/** read each file out to the zip file */
class ZipAccumulator implements FileFilter {
    final File parentDir;
    final ZipOutputStream out;
    final FileFilter filter;
    public ZipAccumulator(File parentDir, ZipOutputStream out,
                          FileFilter filter) {
        this.parentDir = parentDir;
        this.out = out;
        this.filter = filter;
    }
    public boolean accept(File f) {
        if ((null != filter) && (!filter.accept(f))) {
            return false;
        }
        try {
            FileUtil.addFileToZip(f, parentDir, out);
            return true;
        } catch (IOException e) {
            e.printStackTrace(System.err); // todo
        }
        return false;
    }
}

