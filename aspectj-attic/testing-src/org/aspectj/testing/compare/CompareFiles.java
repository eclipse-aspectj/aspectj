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

package org.aspectj.testing.compare;

import org.aspectj.util.LangUtil;

import jdiff.util.Diff;
import jdiff.text.FileLine;
import jdiff.util.DiffNormalOutput;

import java.util.*;
import java.util.zip.*;
import java.io.*;

/**  
 * Compare two files and emit differences.
 * Requires the jdiff library in jdiff/jdiff.jar
 */
public class CompareFiles {
    protected static String[] NO_STRINGS = new String[]{};
    /** standard rendering of null references */
    public static final String NULL = "null";
    /** filter for the both files  */
    protected RegexpFilter filter;
    /** ignore case by converting lines to upper case */
    protected boolean ignoreCase  = false;
    /** collapse internal whitespace by converting to space character */
    protected boolean collapseWhitespace  = true;
    /** trim leading and trailing whitespace from lines before comparison */
    protected boolean trimWhitespace  = false;
    /** output to this File - if not set, System.out */
    protected File output  = null;

    /**
     * Compare two files by lines, emitting output to System.out as a series of edits.
     * @param args  the String[] containing two files to diff plus any number of
     *              <li>-i "ignore": ignore case</li>
     *              <li>-t "trim"  : ignore leading and trailing white space</li>
     *              <li>-b "blanks": ignore differences in all white space</li>
     * @param lhs the File on the left-hand-side of the comparison
     * @param rhs the File on the left-hand-side of the comparison
     * @throws IllegalArgumentException if cannot read both files
     */
    public static void main(String[] args) {
        new CompareFiles().comparefiles(args);
    }

    /**
     * Write results of a diff to some output Writer
     * @param result the DiffResult containing the results of the diff
     * @param output the Writer to output results to - if null, 
     *               defaults to System.out, but will be closed if not null
     * @throws IllegalArgumentException if null == result or output 
     */
    public static void writeDiffResult(DiffResult result, File output) 
        throws IOException {
        if (null == result) throw new IllegalArgumentException("null result");
        Writer writer = (null != output ? new FileWriter(output) 
            : new OutputStreamWriter(System.out));
        DiffNormalOutput out = new DiffNormalOutput(result.lhsLines, result.rhsLines);
        out.setOut(writer);
        out.setLineSeparator(LangUtil.EOL);
        try {
            out.writeScript(result.edits);
        } finally {
            if (null != output) {
                try { writer.close(); } 
                catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    } // writeDiffResult

    /**
     * descend filesystem tree, invoking FileRunnerI.accept() on files.
     * E.g., To list files from current directory:
     * <code><pre>descendFileTree(new File("."), new FileRunnerI() {
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
                for (int i = 0; i < files.length; i++) {
                    if (!fileFilter.accept(files[i])) {
                        return false;
                    }
                }
            }
            // now recurse to handle directories
            File[] dirs = file.listFiles(ValidFileFilter.DIR_EXISTS);
            if (null != dirs) {
                for (int i = 0; i < dirs.length; i++) {
                    if (userRecursion) {
                        if (!fileFilter.accept(dirs[i])) {
                            return false;
                        }
                    } else {
                        if (!descendFileTree(dirs[i], fileFilter,userRecursion)) {
                            return false;
                        }
                    }
                }
            }
        } // readable directory (ignore unreadable ones) 
        return true;
    } // descendFiles

    /**
     * Render a zip/entry combination to String
     */
    private static String renderZipEntry(File zipfile, ZipEntry entry) {
        String filename = (null == zipfile ? "null File" : zipfile.getName());
        String entryname = (null == entry ? "null ZipEntry" : entry.getName());
        //return filename + "!" + entryname;
        return "" + entryname;
    }

    /**
     * Initialise the filter.  Users must do this before the filter is
     * lazily constructed or must set update to true.
     * @param args the String with any args valid for RegexpFilter.init(String, RegexpFilter)
     * @param update if true, use existing filter settings unless
     *            overwritten by the new ones;
     *            if false, create a new filter using args.
     * @throws IllegalArgumentException if cannot read both files
     * @see RegexpFilter#init(String, RegexpFilter)
     */
    public void initFilter(String arg, boolean update) {
        filter = RegexpFilter.init(arg, update ? filter : null);
    }

    /**
     * Initialise the filter.  Users must do this before the filter is
     * lazily constructed or must set update to true.
     * @param args the String[] with any args valid for RegexpFilter
     * @param update if true, use existing filter settings unless
     *            overwritten by the new ones;
     *            if false, create a new filter using args.
     * @throws IllegalArgumentException if cannot read both files
     * @see RegexpFilter#init(String[], RegexpFilter)
     */
    public void initFilter(String[] args, boolean update) {
        filter = RegexpFilter.init(args, update ? filter : null);
    }

    /**
     * Compare two files by lines, emitting output to System.out as a series of edits.
     * @param args  the String[] containing two files to diff 
     *              (lhs, rhs) plus any args valid for RegexpFilter
     * @throws IllegalArgumentException if cannot read both files
     */
    public final void comparefiles(String[] args) {
        if (errMessage(null == args, "null args", null)) return;
        if (errMessage(args.length < 2, "need more args", null)) return;
        File lhs = new File(args[0]);
        File rhs = new File(args[1]);
        if (errMessage(!lhs.canRead(), "!lhs.canRead()", null)) return;
        if (errMessage(!rhs.canRead(), "!rhs.canRead()", null)) return;
        int filterArgsLength = args.length - 2;
        if (0 >= filterArgsLength) {
            initFilter(NO_STRINGS, false);
        } else {
            String[] filterArgs = new String[filterArgsLength];
            System.arraycopy(args, 0, filterArgs, 0, filterArgsLength);
            initFilter(filterArgs, false);
        }
        
        try {
            if (errMessage(!diff(lhs, rhs), "diff(lhs,rhs)",null)) return;
        } catch (IOException t) {
            if (errMessage(false, null, t)) return;
        }
    } // main

    /**
     * Compare two files/dirs, emitting output as a series of edits.
     * If both files are directories, then this compares their contents
     * (including the contents of any zip or jar files) as a series of paths
     * (i.e., it will recognize added or removed or changed filenames, but
     * not files whose contents have changed).
     * Output will go to the File specifies as "output" or System.out if none specified.
     * This is costly, creating an in-memory copy, one String per line, of both files.
     * @param lhs the File/dir on the left-hand-side of the comparison
     * @param rhs the File/dir on the left-hand-side of the comparison
     * @throws IllegalArgumentException if either parm is null or not existing,
     *         or if one is a dir and the other a file
     * @throws IOException if getFileLines or diff utilities do
     * @return false if there was some error
     */
    public final boolean diff(File lhs, File rhs) throws IOException {
        DiffResult result = null;
        String err = null;
        if ((null == lhs) || (null == rhs) 
            || (!lhs.exists()) || (!rhs.exists()) 
            || (!lhs.canRead()) || (!rhs.canRead()) 
            || (lhs.isDirectory() != rhs.isDirectory())) {
            err = "need 2 readable files or dirs or zip files - got lhs=" + lhs + " rhs=" + rhs;
        } else if (lhs.isDirectory()) {
            result = diffDirUtil(lhs, rhs);
        } else {
            boolean lhsIsZip = isZipFile(lhs);
            if (lhsIsZip != isZipFile(rhs)) {
                err = "need 2 readable files or dirs or zip files - got lhs=" + lhs + " rhs=" + rhs;
            } else if (lhsIsZip) {
                result = diffDirUtil(lhs, rhs);
            } else {
                result = diffUtil(lhs, rhs);
            }
        }
        if (null != err) throw new IllegalArgumentException(err);
        if (errMessage(null == result, null, null)) return false;
        writeDiffResult(result, output);
        return true;
    }

    /**
     * Compare two files, returning results for further evaluation or processing
     * @param lhs the File on the left-hand-side of the comparison
     * @param rhs the File on the left-hand-side of the comparison
     * @throws IllegalArgumentException if either parm is null
     * @throws IOException if getFileLines or diff utilities do
     * @return false if there was some error
     */
    public final DiffResult diffUtil(File lhs, File rhs) 
        throws IOException {
        if (errMessage(null == lhs, "null lhs", null)) return null;
        if (errMessage(null == rhs, "null rhs", null)) return null;
        FileLine[] lhsLines = getFileLines(lhs);
        FileLine[] rhsLines = getFileLines(rhs);
        Diff.change edits = new Diff(lhsLines, rhsLines).diff_2(false);
        return new DiffResult(lhsLines, rhsLines, edits);
    }

    /**
     * Read all lines of a file into a String[] (not very efficient),
     * implementing flag policies.
     * @param file the File to read
     * @return a FileLine[] with elements for each line in the file
     */
    public FileLine[] getFileLines(File file) throws IOException {
        if (file.isDirectory()) return getFileLinesForDir(file);
        final Vector results = new Vector();
        
        BufferedReader in = null;
        try {
            in = getReader(file);
            String line;
            while (null != (line = in.readLine())) { 
                results.add(toFileLine(line));
            }
        } finally {
            if (null != in) { in.close(); } 
        }
        final FileLine[] lines = new FileLine[results.size()];
        results.copyInto(lines);
        return lines;
    }
    
    /**
     * Compare two directories or zip files by listing contents (including contents of zip/jar files) 
     * and differencing the results.  This does NOT call diff on each file, but only
     * recognizes when files or zip entries have been added or removed.
     * @param lhsDir the File for an existing, readable directory (as left-hand-side)
     * @param rhsDir the File for an existing, readable directory (as right-hand-side)
     * @throws IllegalArgumentException if null == lhs or rhsDir 
     */
    public DiffResult diffDirUtil(File lhsDir, File rhsDir) {
        FileLine[] lhsLines = getFileLinesForDir(lhsDir);
        FileLine[] rhsLines = getFileLinesForDir(rhsDir);
        // now do the comparison as if they were two files
        Diff.change edits = new Diff(lhsLines, rhsLines).diff_2(false);
        return new DiffResult(lhsLines, rhsLines, edits);
    } 
    
    /**
     * Render all sub-elements of a directory as a list of FileLine[], including entries 
     * in zip and jar files.  The directory prefix is not included in the FileLine.
     * @param dir the File representing the directory to list
     * @throws IllegalArgumentException if null == dir  or !dir.isDirectory()
     */
    public FileLine[] getFileLinesForDir(File dir) {
        if (null == dir) throw new IllegalArgumentException("null dir");
        if (!dir.isDirectory() && ! isZipFile(dir)) throw new IllegalArgumentException("not a dir: " + dir);
        Collection items = directoryToString(dir, null);
        return toFileLine(items, dir.getPath(), true);
    } 

    /** @return true if test or if null != error */
    protected boolean errMessage(boolean test, String message, Throwable error) {
        if (test && (null != message)) { System.err.println(message); }
        if (null != error) { error.printStackTrace(System.err); }
        return (test || (null != error));
    }

    /**
     * Convert current setting into an initialization list for filter
     */
    protected String[] getFilterArgs() {
        return new String[] 
            { (trimWhitespace     ? "-t" : "-T")
            , (collapseWhitespace ? "-b" : "-B")
            , (ignoreCase         ? "-i" : "-I")
            };
    }

    /**
     * Lazy construction of filter
     */
    protected RegexpFilter getFilter() {
        if (null == filter) {
            filter = RegexpFilter.init(getFilterArgs(), null);
        }
        return filter;
    }

    /**
     * Factory for reader used by getFileLines(File).
     * Default implementation creates a BufferedReader.
     * Subclasses may implement pre-processing filters here.
     */
    protected BufferedReader getReader(File file) throws IOException {
        return new BufferedReader(new FileReader(file));
    }

    /**
     * Create a FileLine from the input string,
     * applying policies for whitespace, etc.
     * @param string the String to wrap as a FileLine
     * @return FileLine with string as text and 
     *        canonical as string modified by any canonicalizing policies.
     */
    protected FileLine toFileLine(String string) {
        String canonical = getFilter().process(string);
        return new FileLine(string, canonical);
    }


    protected boolean isZipFile(File f) {
        String s = null;
        if ((null == f)  || (null == (s = f.getPath()))) {
            return false;
        } else {
            return (f.exists() && !f.isDirectory() && (s.endsWith(".jar")));
        }
    }

    /**
     * Convert to an array of FileLine by optionally removing prefix and/or sorting
     * @param collection the Collection of String to process
     */
    protected FileLine[] toFileLine(Collection collection, String ignorePrefix, boolean sort) {
        if (null == collection) throw new IllegalArgumentException("null collection");
        List list = new ArrayList();
        list.addAll(collection);
        if (null != ignorePrefix) {
            for (int i = 0; i < list.size(); i++) {
                String next = list.get(i).toString();
                if (next.startsWith(ignorePrefix)) {
                    list.set(i, next.substring(ignorePrefix.length()));
                }
            }
        }
        if (sort) {
            Collections.sort(list);
        }
        FileLine[] result = new FileLine[list.size()];
        int i = 0;
        for (Iterator it = list.iterator(); it.hasNext();) {
            result[i++] = toFileLine(it.next().toString());
        }
        if (i < result.length) {
            throw new Error("list lost elements? " + (result.length-i)); 
        }
        return result;
    }

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
    protected Collection directoryToString(File dir, Collection results) {
        if (null == dir) throw new IllegalArgumentException("null dir");
        final Collection result = (results != null? results : new Vector());
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
    protected Collection zipFileToString(final File zipfile, Collection results) {
        Collection result = (results != null ? results : new Vector());
        ZipFile zip = null;
        try {
            //ZipFile.OPEN_READ | ZipFile.OPEN_DELETE); delete is 1.3 only
            zip = new ZipFile(zipfile); 
            Enumeration enum = zip.entries();
            // now emitting filename only once, so entries match even if filename does not
            results.add("ZipFileName: " + zipfile);
            while (enum.hasMoreElements()) {
                results.add(renderZipEntry(zipfile, (ZipEntry) enum.nextElement()));
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
     * return structure for diffUtil 
     */
    public final class DiffResult {
        public final FileLine[] lhsLines;
        public final FileLine[] rhsLines;
        public final Diff.change edits;
        public DiffResult (FileLine[] lhsLines,
                           FileLine[] rhsLines,
                           Diff.change edits) {
            this.lhsLines = lhsLines;
            this.rhsLines = rhsLines;
            this.edits = edits;
        }
    }

} // class CompareFiles

class ValidFileFilter implements FileFilter {
    public static final FileFilter EXIST = new ValidFileFilter();
    public static final FileFilter FILE_EXISTS = new FilesOnlyFilter();
    public static final FileFilter DIR_EXISTS = new DirsOnlyFilter();
    protected ValidFileFilter(){}
    public boolean accept(File f) {
        return ((null != f) && (f.exists()));
    }
    static class FilesOnlyFilter extends ValidFileFilter {
        public final boolean accept(File f) {
            return (super.accept(f) && (!f.isDirectory()));
        }
    }
    static class DirsOnlyFilter extends ValidFileFilter {
        public final boolean accept(File f) {
            return (super.accept(f) && (f.isDirectory()));
        }
    }
}

/** 
 * A FileFilter that accumulates the results when called if they exist.
 * Subclasses override accumulate to determine whether it should be
 * accumulated.
 */
class AccumulatingFileFilter extends ValidFileFilter {
    Vector files = new Vector();
    public final boolean accept(File f) {
        if (super.accept(f) && (accumulate(f))) {
            files.add(f);
        }
        return true;
    }

    /** 
     * This implementation accumulates everything.
     * Subclasses should override to implement filter
     * @param file a File guaranteed to exist 
     * @return true if file should be accumulated. 
     */
    public boolean accumulate(File f) {
        return true;
    }
    /**
     * @return list of files currently accumulated 
     */
    public File[] getFiles() {
        return (File[]) files.toArray(new File[0]);
    }
}
