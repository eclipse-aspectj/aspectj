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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.util.zip.ZipFile;


/**
 * 
 */
public class FileUtil {
    /** default parent directory File when a file has a null parent */
    public static final File DEFAULT_PARENT = new File("."); // XXX user.dir?

    /** unmodifiable List of String source file suffixes (including leading ".") */
    public static final List SOURCE_SUFFIXES
        = Collections.unmodifiableList(Arrays.asList(new String[] { ".java", ".aj"}));
    
    final static int[] INT_RA = new int[0];
    
    /** accept all files */
    public static final FileFilter ALL = new FileFilter() {
        public boolean accept(File f) { return true; }
    };
    public static final FileFilter DIRS_AND_WRITABLE_CLASSES
        = new FileFilter() {
            public boolean accept(File file) {
                return ((null != file)
                    && (file.isDirectory()
                        || (file.canWrite()
                            && file.getName().toLowerCase().endsWith(".class"))));
            }
        };

    /** @return true if file path has a zip/jar suffix */
    public static boolean hasZipSuffix(File file) {
        return ((null != file) && hasZipSuffix(file.getPath()));
    }

    /** @return true if path ends with .zip or .jar */
    public static boolean hasZipSuffix(String path) {
        return ((null != path) && (0 != zipSuffixLength(path)));
    }
    
    /** @return 0 if file has no zip/jar suffix or 4 otherwise  */
    public static int zipSuffixLength(File file) {
        return (null == file ? 0 : zipSuffixLength(file.getPath()));
    }
    
    /** @return 0 if no zip/jar suffix or 4 otherwise */
    public static int zipSuffixLength(String path) {
        if ((null != path) && (4 < path.length())){
            String test = path.substring(path.length()-4).toLowerCase();
            if (".zip".equals(test) || ".jar".equals(test)) {
                return 4;
            } 
        }
        return 0;
    }

    /** @return true if file path has a source suffix */
    public static boolean hasSourceSuffix(File file) {
        return ((null != file) && hasSourceSuffix(file.getPath()));
    }

    /** @return true if path ends with .java or .aj */
    public static boolean hasSourceSuffix(String path) {
        return ((null != path) && (0 != sourceSuffixLength(path)));
    }
    
    /** @return 0 if file has no source suffix or the length of the suffix otherwise */
    public static int sourceSuffixLength(File file) {
        return (null == file ? 0 : sourceSuffixLength(file.getPath()));
    }
    
    /** @return 0 if no source suffix or the length of the suffix otherwise */
    public static int sourceSuffixLength(String path) {
        if (LangUtil.isEmpty(path)) {
            return 0;
        }
        
        for (Iterator iter = SOURCE_SUFFIXES.iterator(); iter.hasNext();) {
			String suffix = (String) iter.next();
            if (path.endsWith(suffix)
                || path.toLowerCase().endsWith(suffix)) {
                return suffix.length();
            }
		} 
        return 0;
    }

    /** @return true if this is a readable directory */
    public static boolean canReadDir(File dir) {
        return ((null != dir) && dir.canRead() && dir.isDirectory());
    }
    
    /** @return true if this is a readable file */
    public static boolean canReadFile(File file) {
        return ((null != file) && file.canRead() && file.isFile());
    }
    
    /** @return true if dir is a writable directory */
    public static boolean canWriteDir(File dir) {
        return ((null != dir) && dir.canWrite() && dir.isDirectory());
    }
    
    /** @return true if this is a writable file */
    public static boolean canWriteFile(File file) {
        return ((null != file) && file.canWrite() && file.isFile());
    }
    
    /** @throws IllegalArgumentException unless file is readable and not a directory */
    public static void throwIaxUnlessCanReadFile(File file, String label) {
        if (!canReadFile(file)) {
            throw new IllegalArgumentException(label + " not readable file: " + file);
        }
    }

    /** @throws IllegalArgumentException unless dir is a readable directory */
    public static void throwIaxUnlessCanReadDir(File dir, String label) {
        if (!canReadDir(dir)) {
            throw new IllegalArgumentException(label + " not readable dir: " + dir);
        }
    }
    
    /** @throws IllegalArgumentException unless file is readable and not a directory */
    public static void throwIaxUnlessCanWriteFile(File file, String label) {
        if (!canWriteFile(file)) {
            throw new IllegalArgumentException(label + " not writable file: " + file);
        }
    }

    /** @throws IllegalArgumentException unless dir is a readable directory */
    public static void throwIaxUnlessCanWriteDir(File dir, String label) {
        if (!canWriteDir(dir)) {
            throw new IllegalArgumentException(label + " not writable dir: " + dir);
        }
    }
    
    /** @return array same length as input, with String paths */
    public static String[] getPaths(File[] files) {
        if ((null == files) || (0 == files.length)) {
            return new String[0];
        }
        String[] result = new String[files.length];
        for (int i = 0; i < result.length; i++) {
			if (null != files[i]) {
                result[i] = files[i].getPath();
            }
		}
        return result;
    }

    /** @return array same length as input, with String paths */
    public static String[] getPaths(List files) {
        final int size = (null == files ? 0 : files.size());
        if (0 == size) {
            return new String[0];
        }
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            File file = (File) files.get(i);
            if (null != file) {
                result[i] = file.getPath();
            }
        }
        return result;
    }


    /**
     * Extract the name of a class from the path to its file.
     * If the basedir is null, then the class is assumed to be in
     * the default package unless the classFile has one of the 
     * top-level suffixes { com, org, java, javax } as a parent directory.
     * @param basedir the File of the base directory (prefix of classFile)
     * @param classFile the File of the class to extract the name for
     * @throws IllegalArgumentException if classFile is null or does not end with 
     *          ".class" or a non-null basedir is not a prefix of classFile
     */
    public static String fileToClassName(File basedir, File classFile) {
        LangUtil.throwIaxIfNull(classFile, "classFile");
        String classFilePath = normalizedPath(classFile);
        if (!classFilePath.endsWith(".class")) {
            String m = classFile + " does not end with .class";
            throw new IllegalArgumentException(m);
        }
        classFilePath = classFilePath.substring(0, classFilePath.length()-6);
        if (null != basedir) {
            String basePath = normalizedPath(basedir);
            if (!classFilePath.startsWith(basePath)) {
                String m = classFile + " does not start with " + basedir;
                throw new IllegalArgumentException(m);
            }    
            classFilePath = classFilePath.substring(basePath.length()+1);
        } else {
            final String[] suffixes = new String[] { "com", "org", "java", "javax"};
            boolean found = false;
            for (int i = 0; !found && (i < suffixes.length); i++) {
				int loc = classFilePath.indexOf(suffixes[i] + "/");
                if ((0 == loc) 
                    ||  ((-1 != loc) && ('/' == classFilePath.charAt(loc-1)))) {
                    classFilePath = classFilePath.substring(loc);
                    found = true;
                }
			}
            if (!found) {
                int loc = classFilePath.lastIndexOf("/");
                if (-1 != loc) { // treat as default package
                    classFilePath = classFilePath.substring(loc+1);
                }
            }
        }
        return classFilePath.replace('/', '.');        
    }
    
    /** 
     * Normalize path for comparisons by rendering absolute,
     * clipping basedir prefix,
     *  trimming and changing '\\' to '/'
     * @param file the File with the path to normalize
     * @param basedir the File for the prefix of the file to normalize - ignored if null
     * @return "" if null or normalized path otherwise
     * @throws IllegalArgumentException if basedir is not a prefix of file
     */
    public static String normalizedPath(File file, File basedir) {
        String filePath = normalizedPath(file);
        if (null != basedir) {
            String basePath = normalizedPath(basedir);
            if (filePath.startsWith(basePath)) {
                filePath = filePath.substring(basePath.length());
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
            }
        }
        return filePath;
    }

    /**
     * Render a set of files to String as a path by getting absolute
     * paths of each and delimiting with infix.
     * @param files the File[] to flatten - may be null or empty
     * @param infix the String delimiter internally between entries 
     *        (if null, then use File.pathSeparator).
     * (alias to <code>flatten(getAbsolutePaths(files), infix)</code> 
     * @return String with absolute paths to entries in order, 
     *         delimited with infix
     */
    public static String flatten(File[] files, String infix) {
        if (LangUtil.isEmpty(files)) {
            return "";
        }
        return flatten(getPaths(files), infix);
    }
    
    /**
     * Flatten File[] to String.
     * @param files the File[] of paths to flatten - null ignored
     * @param infix the String infix to use - null treated as File.pathSeparator
     */
    public static String flatten(String[] paths, String infix) {
        if (null == infix) {
            infix = File.pathSeparator;
        }
        StringBuffer result = new StringBuffer();            
        boolean first = true;
        for (int i = 0; i < paths.length; i++) {
			String path = paths[i];
            if (null == path) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                result.append(infix);
            }            
            result.append(path);
		}
        return result.toString();
    }
    
    /** 
     * Normalize path for comparisons by rendering absolute 
     *  trimming and changing '\\' to '/'
     * @return "" if null or normalized path otherwise
     */
    public static String normalizedPath(File file) {
        return (null == file ? "" : weakNormalize(file.getAbsolutePath()));
    }
    
    /** 
     * Weakly normalize path for comparisons by
     *  trimming and changing '\\' to '/'
     */
    public static String weakNormalize(String path) {
        if (null != path) {
            path = path.replace('\\', '/').trim();
        }
        return path;
    }
    
    /**
     * Render as best path, canonical or absolute.
     * @param file the File to get the path for (not null)
     * @return String of the best-available path
     * @throws IllegalArgumentException if file is null
     */
    public static String getBestPath(File file) {
        LangUtil.throwIaxIfNull(file, "file");
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }
    
    /** @return array same length as input, with String absolute paths */
    public static String[] getAbsolutePaths(File[] files) {
        if ((null == files) || (0 == files.length)) {
            return new String[0];
        }
        String[] result = new String[files.length];
        for (int i = 0; i < result.length; i++) {
            if (null != files[i]) {
                result[i] = files[i].getAbsolutePath();
            }
        }
        return result;
    }
    
    /** 
     * Recursively delete the contents of dir, but not the dir itself 
     * @return the total number of files deleted 
     */    
    public static int deleteContents(File dir) {
        return deleteContents(dir, ALL);
    }
    
    /** 
     * Recursively delete some contents of dir, but not the dir itself.
     * This deletes any subdirectory which is empty after its files
     * are deleted.
     * @return the total number of files deleted 
     */    
    public static int deleteContents(File dir, FileFilter filter) {
        return deleteContents(dir, filter, true);
    }
    
    /** 
     * Recursively delete some contents of dir, but not the dir itself.
     * If deleteEmptyDirs is true, this deletes any subdirectory 
     * which is empty after its files are deleted.
     * @param dir the File directory (if a file, the the file is deleted)
     * @return the total number of files deleted 
     */    
    public static int deleteContents(File dir, FileFilter filter, 
                                    boolean deleteEmptyDirs) {
        if (null == dir) {
            throw new IllegalArgumentException("null dir");
        }
        if ((!dir.exists()) || (!dir.canWrite())) {
            return 0;
        }
        if (!dir.isDirectory()) {
            dir.delete();
            return 1;
        }
        String[] fromFiles = dir.list();
        int result = 0;
        for (int i = 0; i < fromFiles.length; i++) {
            String string = fromFiles[i];
            File file = new File(dir, string);
            if ((null == filter) || filter.accept(file)) {
                if (file.isDirectory()) {
                    result += deleteContents(file, filter, deleteEmptyDirs);
                    if (deleteEmptyDirs && (0 == file.list().length)) {
                        file.delete();
                    }
                } else {
                    file.delete();
                    result++;
                }
            }
        }
        return result;
    }
    
    /** 
     * Copy contents of fromDir into toDir
     * @param fromDir must exist and be readable
     * @param toDir must exist or be creatable and be writable
     * @return the total number of files copied 
     */
    public static int copyDir(File fromDir, File toDir) throws IOException {
        return copyDir(fromDir, toDir, null, null);
    }
    
    /** 
     * Recursively copy files in fromDir (with any fromSuffix) to toDir,
     * replacing fromSuffix with toSuffix if any.
     * This silently ignores dirs and files that are not readable
     * but throw IOException for directories that are not writable.
     * This does not clean out the original contents of toDir.
     * (subdirectories are not renamed per directory rules)
     * @param fromSuffix select files with this suffix - select all if null or empty
     * @param toSuffix replace fromSuffix with toSuffix in the destination file
     *         name - ignored if null or empty, 
     *         appended to name if fromSuffix is null or empty
     * @return the total number of files copied 
     */
    public static int copyDir(File fromDir, File toDir,
                               final String fromSuffix, String toSuffix) throws IOException {
        return copyDir(fromDir, toDir, fromSuffix, toSuffix, (FileFilter) null);
    }
    
    /** 
     * Recursively copy files in fromDir (with any fromSuffix) to toDir,
     * replacing fromSuffix with toSuffix if any,
     * and adding the destination file to any collector.
     * This silently ignores dirs and files that are not readable
     * but throw IOException for directories that are not writable.
     * This does not clean out the original contents of toDir.
     * (subdirectories are not renamed per directory rules)
     * This calls any delegate FilenameFilter to collect any selected file.
     * @param fromSuffix select files with this suffix - select all if null or empty
     * @param toSuffix replace fromSuffix with toSuffix in the destination file
     *         name - ignored if null or empty, 
     *         appended to name if fromSuffix is null or empty
     * @param collector the List sink for destination files - ignored if null
     * @return the total number of files copied 
     */
    public static int copyDir(File fromDir, File toDir, final String fromSuffix, 
                             final String toSuffix, final List collector) throws IOException {
        //int before = collector.size();        
        if (null == collector) {
            return copyDir(fromDir, toDir, fromSuffix, toSuffix);
        } else {
            FileFilter collect = new FileFilter() {
    			public boolean accept(File pathname) {
    				return collector.add(pathname);
    			}
            };
            return copyDir(fromDir, toDir, fromSuffix, toSuffix, collect);
         }
    }
    
    /** 
     * Recursively copy files in fromDir (with any fromSuffix) to toDir,
     * replacing fromSuffix with toSuffix if any.
     * This silently ignores dirs and files that are not readable
     * but throw IOException for directories that are not writable.
     * This does not clean out the original contents of toDir.
     * (subdirectories are not renamed per directory rules)
     * This calls any delegate FilenameFilter to collect any selected file.
     * @param fromSuffix select files with this suffix - select all if null or empty
     * @param toSuffix replace fromSuffix with toSuffix in the destination file
     *         name - ignored if null or empty, 
     *         appended to name if fromSuffix is null or empty
     * @return the total number of files copied 
     */
    public static int copyDir(File fromDir, File toDir, final String fromSuffix, 
                             final String toSuffix, final FileFilter delegate) throws IOException {
                                
        if ((null == fromDir) || (!fromDir.canRead())) {
            return 0;
        }
        final boolean haveSuffix = ((null != fromSuffix) && (0 < fromSuffix.length()));
        final int slen = (!haveSuffix ? 0 : fromSuffix.length());
        
        if (!toDir.exists()) {
            toDir.mkdirs();
        }
        final String[] fromFiles;
        if (!haveSuffix) {
            fromFiles = fromDir.list();
        } else {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (new File(dir, name).isDirectory()
                        || (name.endsWith(fromSuffix)));
                }
            };
            fromFiles = fromDir.list(filter);
        }
        int result = 0;
        final int MAX = (null == fromFiles ? 0 : fromFiles.length);
        for (int i = 0; i < MAX; i++) {
            String filename = fromFiles[i];
            File fromFile = new File(fromDir, filename);
            if (fromFile.canRead()) {
                if (fromFile.isDirectory()) {
                    result += copyDir(fromFile, new File(toDir, filename), fromSuffix, toSuffix, delegate);
                } else if (fromFile.isFile()) {
                    if (haveSuffix) {
                        filename = filename.substring(0, filename.length()-slen);
                    }
                    if (null != toSuffix) {
                        filename = filename + toSuffix;
                    }
                    File targetFile = new File(toDir, filename);
                    if ((null == delegate) || delegate.accept(targetFile)) {
                        copyFile(fromFile, targetFile);
                    }
                    result++;
                }
            }
        }
        return result;
    }
        
    /** 
     * Recursively list files in srcDir.
     * @return ArrayList with String paths of File under srcDir (relative to srcDir)
     */
    public static String[] listFiles(File srcDir)  {
        ArrayList result = new ArrayList();
        if ((null != srcDir) && srcDir.canRead()) {
            listFiles(srcDir, null, result);
        }     
        return (String[]) result.toArray(new String[0]);
    }
    
    public static final FileFilter aspectjSourceFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			return name.endsWith(".java") || name.endsWith(".aj");
		}
    };
    
    
    /** 
     * Recursively list files in srcDir.
     * @return ArrayList with String paths of File under srcDir (relative to srcDir)
     */
    public static File[] listFiles(File srcDir, FileFilter fileFilter)  {
        ArrayList result = new ArrayList();
        if ((null != srcDir) && srcDir.canRead()) {
            listFiles(srcDir, result, fileFilter);
        }     
        return (File[]) result.toArray(new File[result.size()]);
    }

    /**
     * Convert String[] paths to File[] as offset of base directory 
     * @param basedir the non-null File base directory for File to create with paths
     * @param paths the String[] of paths to create
     * @return File[] with same length as paths
     */
    public static File[] getBaseDirFiles(
        File basedir, 
        String[] paths) {
        return getBaseDirFiles(basedir, paths, (String[]) null);
    }
    
    /**
     * Convert String[] paths to File[] as offset of base directory 
     * @param basedir the non-null File base directory for File to create with paths
     * @param paths the String[] of paths to create
     * @param suffixes the String[] of suffixes to limit sources to - ignored if null
     * @return File[] with same length as paths
     */
    public static File[] getBaseDirFiles(
        File basedir, 
        String[] paths, 
        String[] suffixes) {
        LangUtil.throwIaxIfNull(basedir, "basedir");
        LangUtil.throwIaxIfNull(paths, "paths");
        File[] result = null;
        if (!LangUtil.isEmpty(suffixes)) {
            ArrayList list = new ArrayList();
            for (int i = 0; i < paths.length; i++) {
                boolean listed = false;
                String path = paths[i];
                for (int j = 0; !listed && (j < suffixes.length); j++) {
                    String suffix = suffixes[j];
					if (listed = path.endsWith(suffix)) {
                        list.add(new File(basedir, paths[i]));
                    }
				}
            }
            result = (File[]) list.toArray(new File[0]);
        } else {
            result = new File[paths.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new File(basedir, paths[i]);
            }
        }
        return result;
    }

    /**
     * Copy files from source dir into destination directory,
     * creating any needed directories.  This differs from copyDir in not
     * being recursive; each input with the source dir creates a full path.
     * However, if the source is a directory, it is copied as such.
     * @param srcDir an existing, readable directory containing relativePaths files
     * @param relativePaths a set of paths relative to srcDir to readable File to copy
     * @param destDir an existing, writable directory to copy files to
     * @throws IllegalArgumentException if input invalid, IOException if operations fail
     */
    public static File[] copyFiles(File srcDir, String[] relativePaths, File destDir) 
            throws IllegalArgumentException, IOException {
        final String[] paths = relativePaths;
        throwIaxUnlessCanReadDir(srcDir, "srcDir");
        throwIaxUnlessCanWriteDir(destDir, "destDir");
        LangUtil.throwIaxIfNull(paths, "relativePaths");
        File[] result = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            LangUtil.throwIaxIfNull(path, "relativePaths-entry");
            File src = new File(srcDir, relativePaths[i]);
            File dest = new File(destDir, path);
            File destParent = dest.getParentFile();
            if (!destParent.exists()) {
                destParent.mkdirs();
            }
            LangUtil.throwIaxIfFalse(canWriteDir(destParent), "dest-entry-parent");
            copyFile(src, dest); // both file-dir and dir-dir copies
            result[i] = dest;
        }
        return result;
    }

    /** 
     * Copy fromFile to toFile, handling file-file, dir-dir, and file-dir
     * copies.
     * @param fromFile the File path of the file or directory to copy - must be
     * readable
     * @param toFile the File path of the target file or directory - must be
     * writable (will be created if it does not exist)
     */
    public static void copyFile(File fromFile, File toFile) throws IOException {
        LangUtil.throwIaxIfNull(fromFile, "fromFile");
        LangUtil.throwIaxIfNull(toFile, "toFile");
        LangUtil.throwIaxIfFalse(!toFile.equals(fromFile), "same file");
        if (toFile.isDirectory()) {   // existing directory 
            throwIaxUnlessCanWriteDir(toFile, "toFile");
            if (fromFile.isFile()) {  // file-dir
                File targFile = new File(toFile, fromFile.getName());
                copyValidFiles(fromFile, targFile);
            } else if (fromFile.isDirectory()) { // dir-dir
                copyDir(fromFile, toFile);
            } else {
                LangUtil.throwIaxIfFalse(false, "not dir or file: " + fromFile);
            }
        } else if (toFile.isFile()) {     // target file exists
            if (fromFile.isDirectory()) {
                LangUtil.throwIaxIfFalse(false, "can't copy to file dir: " + fromFile);
            }
            copyValidFiles(fromFile, toFile); // file-file                
        } else { // target file is a non-existent path -- could be file or dir
            File toFileParent = ensureParentWritable(toFile);
            if (fromFile.isFile()) {
                copyValidFiles(fromFile, toFile);
            } else if (fromFile.isDirectory()) {
                toFile.mkdirs();
                throwIaxUnlessCanWriteDir(toFile, "toFile");
                copyDir(fromFile, toFile);                
            } else {
                LangUtil.throwIaxIfFalse(false, "not dir or file: " + fromFile);
            }
        }
    }
    
    /**
     * Ensure that the parent directory to path can be written.
     * If the path has a null parent, DEFAULT_PARENT is tested.
     * If the path parent does not exist, this tries to create it.
     * @param path the File path whose parent should be writable
     * @return the File path of the writable parent directory
     * @throws IllegalArgumentException if parent cannot be written
     *         or path is null.
     */
    public static File ensureParentWritable(File path) {
        LangUtil.throwIaxIfNull(path, "path");
        File pathParent = path.getParentFile();
        if (null == pathParent) {
            pathParent = DEFAULT_PARENT;     
        }
        if (!pathParent.canWrite()) {
            pathParent.mkdirs();
        }
        throwIaxUnlessCanWriteDir(pathParent, "pathParent");
        return pathParent;
    }
    
    /** 
     * Copy file to file.
     * @param fromFile the File to copy (readable, non-null file)
     * @param toFile the File to copy to (non-null, parent dir exists)
     * @throws IOException
     */
    public static void copyValidFiles(File fromFile, File toFile) throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(fromFile);
            out = new FileOutputStream(toFile);
            copyStream(in, out);
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }
    
    /** do line-based copying */
    public static void copyStream(DataInputStream in, PrintStream out) throws IOException {
        LangUtil.throwIaxIfNull(in, "in");
        LangUtil.throwIaxIfNull(in, "out");
        String s;
        while (null != (s = in.readLine())) {
            out.println(s);
        }
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        final int MAX = 4096;
        byte[] buf = new byte[MAX];
        for (int bytesRead = in.read(buf, 0, MAX);
            bytesRead != -1;
            bytesRead = in.read(buf, 0, MAX)) {
            out.write(buf, 0, bytesRead);
        }
    }

    public static void copyStream(Reader in, Writer out) throws IOException {
        final int MAX = 4096;
        char[] buf = new char[MAX];
        for (int bytesRead = in.read(buf, 0, MAX);
            bytesRead != -1;
            bytesRead = in.read(buf, 0, MAX)) {
            out.write(buf, 0, bytesRead);
        }
    }
        
    /** 
     * Make a new child directory of parent
     * @param parent a File for the parent (writable)
     * @param child a prefix for the child directory
     * @return a File dir that exists with parentDir as the parent file or null
     */
    public static File makeNewChildDir(File parent, String child) {
        if (null == parent || ! parent.canWrite() || !parent.isDirectory()) {
            throw new IllegalArgumentException("bad parent: " + parent);
        } else if (null == child) {
            child = "makeNewChildDir";
        } else if (!isValidFileName(child)) {
            throw new IllegalArgumentException("bad child: " + child);
        }
        File result = new File(parent, child);
        int safety = 1000;
        for (String suffix = FileUtil.randomFileString();
            ((0 < --safety) && result.exists());
            suffix = FileUtil.randomFileString()) {
            result = new File(parent, child+suffix);
        }
        if ((null == result) || result.exists()) {
            System.err.println("exhausted files for child dir in " + parent);
            return null;
        } 
        return ((result.mkdirs() && result.exists()) ? result : null);
    }

    /**
     * Make a new temporary directory in the same directory
     * that the system uses for temporary files, or if
     * that files, in the current directory.
     * @param name the preferred (simple) name of the directory - may be null.
     * @return File of an existing new temp dir, or null if unable to create
     */
    public static File getTempDir(String name) {
        if (null == name) {
            name = "FileUtil_getTempDir";
        } else if (!isValidFileName(name)) {
            throw new IllegalArgumentException(" invalid: " + name);
        }
        File result = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("ignoreMe", ".txt");
            File tempParent = tempFile.getParentFile();
            result = makeNewChildDir(tempParent, name);
        } catch (IOException t) {
            result = makeNewChildDir(new File("."), name);
        } finally {
            if (null != tempFile) {
                tempFile.delete();
            }            
        }
        return result;
    }
        
    public static URL[] getFileURLs(File[] files) { 
        if ((null == files) || (0 == files.length)) {
            return new URL[0];
        }
        URL[] result = new URL[files.length]; // XXX dangerous non-copy...
        for (int i = 0; i < result.length; i++) {
			result[i] = getFileURL(files[i]);
		}
        return result;
    }
	
    /**
     * Get URL for a File. 
     * This appends "/" for directories.
     * prints errors to System.err 
     * @param file the File to convert to URL (not null)
     */
    public static URL getFileURL(File file) { 
		LangUtil.throwIaxIfNull(file, "file");
        URL result = null;
        try {
           String url =  "file:" + file.getAbsolutePath().replace('\\', '/');
            result = new URL(url + (file.isDirectory() ? "/" : ""));
        } catch (MalformedURLException e) {
            String m = "Util.makeURL(\"" + file.getPath() + "\" MUE " + e.getMessage();
            System.err.println(m);
        }
		return result;
	}
	
	
    
    /**
     * Write contents to file, returning null on success or error message otherwise.
     * This tries to make any necessary parent directories first.
     * @param file the File to write (not null)
     * @param contents the String to write (use "" if null)
     * @return String null on no error, error otherwise 
     */
    public static String writeAsString(File file, String contents) {
        LangUtil.throwIaxIfNull(file, "file");
        if (null == contents) {
            contents = "";
        }
        Writer out = null;
        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                return "unable to make parent dir for " + file;
            }
            Reader in = new StringReader(contents);
            out = new FileWriter(file);
            FileUtil.copyStream(in, out);
            return null;
        } catch (IOException e) {
            return LangUtil.unqualifiedClassName(e) + " writing " + file 
                + ": " + e.getMessage();
        } finally {
            if (null != out) {
                try { out.close(); }
                catch (IOException e) {} // ignored
            }
        }        
    }


	/**
	 * Reads a boolean array with our encoding
	 */
	public static boolean[] readBooleanArray(DataInputStream s) throws IOException {
		int len = s.readInt();
		boolean[] ret = new boolean[len];
		for (int i=0; i < len; i++) ret[i] = s.readBoolean();
		return ret;
	}


	/**
	 * Writes a boolean array with our encoding
	 */
	public static void writeBooleanArray(boolean[] a, DataOutputStream s) throws IOException {
		int len = a.length;
		s.writeInt(len);
		for (int i=0; i < len; i++) s.writeBoolean(a[i]);
	}


	/**
	 * Reads an int array with our encoding
	 */
	public static int[] readIntArray(DataInputStream s) throws IOException {
		int len = s.readInt();
		int[] ret = new int[len];
		for (int i=0; i < len; i++) ret[i] = s.readInt();
		return ret;
	}


	/**
	 * Writes an int array with our encoding
	 */
	public static void writeIntArray(int[] a, DataOutputStream s) throws IOException {
		int len = a.length;
		s.writeInt(len);
		for (int i=0; i < len; i++) s.writeInt(a[i]);
	}



	/**
	 * Reads an int array with our encoding
	 */
	public static String[] readStringArray(DataInputStream s) throws IOException {
		int len = s.readInt();
		String[] ret = new String[len];
		for (int i=0; i < len; i++) ret[i] = s.readUTF();
		return ret;
	}


	/**
	 * Writes an int array with our encoding
	 */
	public static void writeStringArray(String[] a, DataOutputStream s) throws IOException {
		if (a == null) {
			s.writeInt(0);
			return;
		}
		int len = a.length;
		s.writeInt(len);
		for (int i=0; i < len; i++) s.writeUTF(a[i]);
	}


	/**
	 * Returns the contents of this file as a String
	 */
	public static String readAsString(File file) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file));
		StringBuffer b = new StringBuffer();
		while (true) {
			int ch = r.read();
			if (ch == -1) break;
			b.append((char)ch);
		}
		r.close();
		return b.toString();
	}
	
	/**
	 * Returns the contents of this stream as a String
	 */
	public static String readAsString(InputStream in) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		StringBuffer b = new StringBuffer();
		while (true) {
			int ch = r.read();
			if (ch == -1) break;
			b.append((char)ch);
		}
		in.close();
		r.close();
		return b.toString();
	}
	

	/**
	 * Returns the contents of this file as a byte[]
	 */
	public static byte[] readAsByteArray(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		byte[] ret = FileUtil.readAsByteArray(in);
		in.close();
		return ret;
	}


	/**
	 * Reads this input stream and returns contents as a byte[]
	 */
	public static byte[] readAsByteArray(InputStream inStream) throws IOException {
		int size = 1024;
		byte[] ba = new byte[size];
		int readSoFar = 0;
		
		while (true) {
			int nRead = inStream.read(ba, readSoFar, size-readSoFar);
			if (nRead == -1) break;
			readSoFar += nRead;
			if (readSoFar == size) {
				int newSize = size * 2;
				byte[] newBa = new byte[newSize];
				System.arraycopy(ba, 0, newBa, 0, size);
				ba = newBa;
				size = newSize;
			}
		}
		
		byte[] newBa = new byte[readSoFar];
		System.arraycopy(ba, 0, newBa, 0, readSoFar);
		return newBa;
	}

    final static String FILECHARS = "abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    /** @return semi-random String of length 6 usable as filename suffix */
    static String randomFileString() {
        final double FILECHARS_length = FILECHARS.length();
        final int LEN = 6;
        final char[] result = new char[LEN];
        int index = (int) (Math.random() * 6d);
        for (int i = 0; i < LEN; i++) {
            if (index >= LEN) {
                index = 0;
            }
            result[index++] = FILECHARS.charAt((int) (Math.random() * FILECHARS_length));		
		}
        return new String(result);     
    }

	public static InputStream getStreamFromZip(String zipFile, String name) {
		try {
			ZipFile zf = new ZipFile(zipFile);
			try {
	    		ZipEntry entry = zf.getEntry(name);
	    		return zf.getInputStream(entry);
			} finally {
				//??? is it safe not to close this zf.close();
			}
		} catch (IOException ioe) {
			return null;
		}
	}
	

	public static void extractJar(String zipFile, String outDir) throws IOException {
		ZipInputStream zs = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry entry;
		while ( (entry = zs.getNextEntry()) != null) {
			if (entry.isDirectory()) continue;
			byte[] in = readAsByteArray(zs);
			
			File outFile = new File(outDir + "/" + entry.getName());
			//if (!outFile.getParentFile().exists()) 
			//System.err.println("parent: " + outFile.getParentFile());
			//System.err.println("parent: " + outFile.getParentFile());
			outFile.getParentFile().mkdirs();
			FileOutputStream os = new FileOutputStream(outFile);
			os.write(in);
			os.close();
			zs.closeEntry();
		}
		zs.close();
	}

	/**
	 * Do line-based search  for literal text in source files, 
     * returning file:line where found.
	 * @param sought the String text to seek in the file
	 * @param sources the List of String paths to the source files
     * @param listAll if false, only list first match in file
     * @param errorSink the PrintStream to print any errors to (one per line)
     *         (use null to silently ignore errors)
	 * @return List of String of the form file:line for each found entry
     *          (never null, might be empty)
	 */
	public static List lineSeek(String sought, List sources, boolean listAll,
        PrintStream errorSink) {
        if (LangUtil.isEmpty(sought) || LangUtil.isEmpty(sources)) {
            return Collections.EMPTY_LIST;
        }
        ArrayList result = new ArrayList();
        for (Iterator iter = sources.iterator(); iter.hasNext();) {
			String path = (String) iter.next();
            String error = lineSeek(sought, path, listAll, result);
            if ((null != error) && (null != errorSink)) {
                errorSink.println(error);
            }
		}
        return result;
	}
    
    /**
     * Do line-based search  for literal text in source file, 
     * returning line where found as a String 
     * in the form {sourcePath}:line:column submitted to the
     * collecting parameter sink.
     * Any error is rendered to String and returned as the result.
     * 
     * @param sought the String text to seek in the file
     * @param sources the List of String paths to the source files
     * @param listAll if false, only list first match in file
     * @param List sink the List for String entries of the form {sourcePath}:line:column
     * @return String error if any, or add String entries to sink
     */
    public static String lineSeek(String sought, String sourcePath, boolean listAll,
        ArrayList sink) {
        if (LangUtil.isEmpty(sought) || LangUtil.isEmpty(sourcePath)) {
            return "nothing sought";
        }
        if (LangUtil.isEmpty(sourcePath)) {
            return "no sourcePath";
        }
        final File file = new File(sourcePath);
        if (!file.canRead() || !file.isFile()) {
            return "sourcePath not a readable file";
        }
        int lineNum = 0;
        FileReader fin = null;
        try {
            fin = new FileReader(file);
            BufferedReader reader = new BufferedReader(fin);
            String line;
            while (null != (line = reader.readLine())) {
                lineNum++;
                int loc = line.indexOf(sought);
                if (-1 != loc) {
                    sink.add(sourcePath + ":" + lineNum + ":" + loc);
                    if (!listAll) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            return LangUtil.unqualifiedClassName(e) + " reading " + sourcePath 
                + ":" + lineNum;
        } finally {
            try { if (null != fin) fin.close(); }
            catch (IOException e) {} // ignore
        }
        return null;
    }

	public static BufferedOutputStream makeOutputStream(File file) throws FileNotFoundException {
		File parent = file.getParentFile();
		if (parent != null) parent.mkdirs();
		return new BufferedOutputStream(new FileOutputStream(file));
	}

    /** map name to result, removing any fromSuffix and adding any toSuffix */
    private static String map(String name, String fromSuffix, String toSuffix) {
        if (null != name) {
            if (null != fromSuffix) {
                name = name.substring(0, name.length()-fromSuffix.length());
            }
            if (null != toSuffix) {
                name = name + toSuffix;
            }
        }
        return name;
    }

    private static void listFiles(final File baseDir, ArrayList result, FileFilter filter)  {
        File[] files = baseDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                listFiles(f, result, filter);
            } else {
                if (filter.accept(f)) result.add(f);
            }
        }
    }

    /** @return true if input is not null and contains no path separator */
    private static boolean isValidFileName(String input) {
        return ((null != input) && (-1 == input.indexOf(File.pathSeparator)));
    }

    private static void listFiles(final File baseDir, String dir, ArrayList result)  {
        final String dirPrefix = (null == dir ? "" : dir + "/");
        final File dirFile = (null == dir ? baseDir : new File(baseDir.getPath() + "/" + dir));
        final String[] files = dirFile.list();
        for (int i = 0; i < files.length; i++) {
            File f = new File(dirFile, files[i]);
            String path = dirPrefix + files[i];
            if (f.isDirectory()) {
                listFiles(baseDir, path, result);
            } else {
                result.add(path);
            }
        }
    }

    private FileUtil() { throw new Error("utility class"); }

}
