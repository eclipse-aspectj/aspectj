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

package org.aspectj.testing.harness.bridge;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.testing.xml.IXmlWritable;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Calculate changes in a directory tree.
 * Usage:
 * <ul>
 * <li>Set up with any expected changes and/or an expected directory</li>
 * <li>Set up with any file checker</li>
 * <li>start(..) before changes. 
 *     This issues messages for any removed files not found,
 *     which represent an error in the expected changes.</li>
 * <li>Do whatever operations will change the directory</li>
 * <li>end(..).
 *     This issues messages for any files not removed, added, or updated,
 *     and, if any checker was set, any checker messages for matching
 *     added or updated files</li>
 * </ul>
 */
public class DirChanges {
    
    private static final boolean EXISTS = true;

    final Spec spec;
    
    /** start time, in milliseconds - valid only from start(..)..end(..) */
    long startTime;
    
    /** base directory of actual files - valid only from start(..)..end(..) */
    File baseDir;
    
    /** if set, this is run against any resulting existing files */
    IFileChecker fileChecker;
    
    /** handler valid from start..end of start(..) and end(..) methods */
    IMessageHandler handler;
    
	/**
	 * Constructor for DirChanges.
	 */
	public DirChanges(Spec spec) {
		LangUtil.throwIaxIfNull(spec, "spec");
        this.spec = spec;
    }

    /**
     * Inspect the base dir, and issue any messages for
     * removed files not present.
     * @param baseDir File for a readable directory
     * @return true if this started without sending messages
     *          for removed files not present.
     */
    public boolean start(IMessageHandler handler, File baseDir) {
        FileUtil.throwIaxUnlessCanReadDir(baseDir, "baseDir");
        final IMessageHandler oldHandler = this.handler;
        this.handler = handler;
        this.baseDir = baseDir;
        startTime = 0l;
        final boolean doCompare = false;
        boolean result 
                = exists("at start, did not expect file to be added", !EXISTS, spec.added, doCompare);            
        result &= exists("at start, expected file to be unchanged", EXISTS, spec.unchanged, doCompare);            
        result &= exists("at start, expected file to be updated", EXISTS, spec.updated, doCompare);            
        result &= exists("at start, expected file to be removed", EXISTS, spec.removed, doCompare);            
        startTime = System.currentTimeMillis();
        this.handler = oldHandler;
        return result;        
    }

    
    /**
     * Inspect the base dir, issue any messages for
     * files not added, files not updated, and files not removed,
     * and compare expected/actual files added or updated.
     * @throws IllegalStateException if called before start(..)
     */
    public boolean end(IMessageHandler handler, File srcBaseDir) {
        FileUtil.throwIaxUnlessCanReadDir(baseDir, "baseDir");
        if (0l == startTime) {
            throw new IllegalStateException("called before start");
        }
        final IMessageHandler oldHandler = this.handler;
        this.handler = handler;
        final boolean doCompare = (null != fileChecker);
        final boolean fastFail = spec.fastFail;
        boolean result
                = exists("at end, expected file was not added",   EXISTS, spec.added, doCompare);            
        if (result || !fastFail) {
            result &= exists("at end, expected file was not unchanged", EXISTS, spec.unchanged, doCompare, false);            
        }
        if (result || !fastFail) {
            result &= exists("at end, expected file was not updated", EXISTS, spec.updated, doCompare);            
        }
        if (result || !fastFail) {
            result &= exists("at end, file exists, was not removed", !EXISTS, spec.removed, doCompare); 
        }
        if (result || !fastFail) {
            result &= compareDir(srcBaseDir);          
        }
        // XXX validate that unchanged mod-time did not change

        this.handler = oldHandler;
        baseDir = null;
        startTime = 0l;
        return result;
    }

	/**
     * Verify that all files in any specified expected directory
     * have matching files in the base directory, putting any messages
     * in the handler (only one if the spec indicates fast-fail).
	 * @param srcBaseDir the File for the base directory of the test sources
     *         (any expected dir is specified relative to this directory)
	 * @return true if the same, false otherwise
	 */
	private boolean compareDir(File srcBaseDir) {
        if (null == spec.dirExpected) {
            return true;
        }
        File expDir = new File(srcBaseDir, spec.dirExpected);
        File actDir = baseDir;
        return TestUtil.sameDirectoryContents(handler, expDir, actDir, spec.fastFail);
	}


    /** @param comp FileMessageComparer (if any) given matching messages to compare */
    protected void setFileComparer(IFileChecker comp) {
        this.fileChecker = comp;
    }
    


    /**
     * Signal fail if any files do {not} exist or do {not} have last-mod-time after startTime
     * @param handler the IMessageHandler sink for messages
     * @param label the String infix for the fail message
     * @param exists if true, then file must exist and be modified after start time;
     *                if false, then the file must not exist or must be modified before start time.
     * @param pathList the List of path (without any Spec.defaultSuffix) of File
     *                  in Spec.baseDir to find (or not, if !exists)
     */
    protected boolean exists(
        String label,
        boolean exists,
        List pathList,
        boolean doCompare) {
        boolean expectStartEarlier = true;
      	return exists(label, exists, pathList,doCompare, true);    
    }
    protected boolean exists(
        String label,
        boolean exists,
        List pathList,
        boolean doCompare,
        boolean expectStartEarlier) {
        boolean result = true;
        if (!LangUtil.isEmpty(pathList)) {
            final File expDir = ((!doCompare || (null == spec.dirExpected))
                ? null 
                : new File(baseDir, spec.dirExpected));
            for (Iterator iter = pathList.iterator(); iter.hasNext();) {
                final String entry = (String) iter.next() ;
                String path = entry ;
                if (null != spec.defaultSuffix) {
                    if (".class".equals(spec.defaultSuffix)) {
                        path = path.replace('.', '/');
                    }
                    path = path + spec.defaultSuffix;
                }
                File actualFile = new File(baseDir, path);
                if (exists != (actualFile.canRead() && actualFile.isFile() 
                                && (expectStartEarlier
                                	? startTime <= actualFile.lastModified()
                                	: startTime > actualFile.lastModified()
                                	))) {
                    failMessage(handler, exists, label, path, actualFile); 
                    if (result) {
                        result = false;
                    }
                } else if (exists && doCompare && (null != fileChecker)) {
                    if (!fileChecker.checkFile(handler, path, actualFile) && result) {
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Generate fail message "{un}expected {label} file {path} in {baseDir}".
     * @param handler the IMessageHandler sink
     * @param label String message infix 
     * @param path the path to the file
     */
    protected void failMessage(
        IMessageHandler handler,
        boolean exists,
        String label,
        String path, 
        File file) {
        MessageUtil.fail(handler, label + " \"" + path + "\" in " + baseDir); 
    }


    /** Check actual File found at a path, usually to diff expected/actual contents */
    public static interface IFileChecker {
        /** 
         * Check file found at path.
         * Implementations should return false when adding fail (or worse)
         * message to the handler, and true otherwise.
         * @param handler IMessageHandler sink for messages, esp. fail.
         * @param path String for normalized path portion of actualFile.getPath()
         * @param actualFile File to file found
         * @return false if check failed and messages added to handler
         */
        boolean checkFile(IMessageHandler handler, String path, File actualFile);
    }  
    
//    /**
//     * Default FileChecker compares files literally, transforming any 
//     * with registered normalizers.
//     */
//    public static class FileChecker implements IFileChecker {
//        final File baseExpectedDir;
//        NormalizedCompareFiles fileComparer;
//
//        public FileChecker(File baseExpectedDir) {
//            this.baseExpectedDir = baseExpectedDir;    
//            fileComparer = new NormalizedCompareFiles();
//        }
//        public boolean checkFile(IMessageHandler handler, String path, File actualFile) {
//            if (null == baseExpectedDir) {
//                MessageUtil.error(handler, "null baseExpectedDir set on construction");
//            } else if (!baseExpectedDir.canRead() || !baseExpectedDir.isDirectory()) {
//                MessageUtil.error(handler, "bad baseExpectedDir: " + baseExpectedDir);
//            } else {
//                File expectedFile = new File(baseExpectedDir, path);
//                if (!expectedFile.canRead()) {
//                    MessageUtil.fail(handler, "cannot read expected file: " + expectedFile);
//                } else {
//                    return doCheckFile(handler, expectedFile, actualFile, path);
//                }
//            }
//            return false;
//        }
//
//        protected boolean doCheckFile(
//            IMessageHandler handler, 
//            File expectedFile, 
//            File actualFile,
//            String path) {
//            fileComparer.setHandler(handler);
//            FileLine[] expected = fileComparer.diff();
//            return false;
//        }
//    }
    
//    /**
//     * CompareFiles implementation that pre-processes input 
//     * to normalize it.  Currently it reads all files except
//     * .class files, which it disassembles first.
//     */
//    public static class NormalizedCompareFiles extends CompareFiles {
//        private final static String[] NO_PATHS = new String[0];
//        private static String normalPath(File file) { // XXX util
//            if (null == file) {
//                return "";
//            }
//            return file.getAbsolutePath().replace('\\', '/');
//        }
//        
//        private String[] baseDirs;
//        private IMessageHandler handler;
//        
//        public NormalizedCompareFiles() {
//        }
//        
//        void init(IMessageHandler handler, File[] baseDirs) {
//            this.handler = handler;
//            if (null == baseDirs) {
//                this.baseDirs = NO_PATHS;
//            } else {
//                this.baseDirs = new String[baseDirs.length];
//                for (int i = 0; i < baseDirs.length; i++) {
//					this.baseDirs[i] = normalPath(baseDirs[i]) + "/";
//				}
//            }
//        } 
//        
//        private String getClassName(File file) {
//            String result = null;
//            String path = normalPath(file);
//            if (!path.endsWith(".class")) {
//                    MessageUtil.error(handler, 
//                        "NormalizedCompareFiles expected " 
//                        + file
//                        + " to end with .class"); 
//            } else {
//                path = path.substring(0, path.length()-6);
//                for (int i = 0; i < baseDirs.length; i++) {
//                    if (path.startsWith(baseDirs[i])) {
//                        return path.substring(baseDirs[i].length()).replace('/', '.');
//                    }
//                }
//                MessageUtil.error(handler, 
//                    "NormalizedCompareFiles expected " 
//                    + file
//                    + " to start with one of " 
//                    + LangUtil.arrayAsList(baseDirs));
//            }
//        
//            return result;
//        }
//        
//        /** 
//         * Read file as normalized lines, sending handler any messages
//         * ERROR for input failures and FAIL for processing failures.
//         * @return NOLINES on error or normalized lines from file otherwise 
//         */
//        public FileLine[] getFileLines(File file) {
//            FileLineator capture = new FileLineator();
//            InputStream in = null;
//            try { 
//                if (!file.getPath().endsWith(".class")) {
//                    in = new FileInputStream(file);
//                    FileUtil.copyStream(
//                        new BufferedReader(new InputStreamReader(in)), 
//                        new PrintWriter(capture));
//                } else {
//                    String name = getClassName(file);
//                    if (null == name) {
//                        return new FileLine[0];
//                    }
//                    String path = normalPath(file);
//                    path = path.substring(0, path.length()-name.length());
//                    // XXX  sole dependency on bcweaver/bcel
//                    LazyClassGen.disassemble(path, name, capture);
//                }
//            } catch (IOException e) {
//                MessageUtil.fail(handler, 
//                    "NormalizedCompareFiles IOException reading " + file, e);
//                return null;
//            } finally {
//                if (null != in) {
//                    try { in.close(); }
//                    catch (IOException e) {} // ignore
//                }
//                capture.flush();
//                capture.close();
//            }
//            String missed = capture.getMissed();
//            if (!LangUtil.isEmpty(missed)) {
//                MessageUtil.fail(handler, 
//                    "NormalizedCompareFiles missed input: " 
//                    + missed);
//                return null;
//            } else {
//                return capture.getFileLines();
//            }                    
//        } 
//    
//        
//    }           
 
    /**
     * Specification for a set of File added, removed, or updated
     * in a given directory, or for a directory base for a tree of expected files. 
     * If defaultSuffix is specified, entries may be added without it.
     */
    public static class Spec implements IXmlWritable {
        public static final String XMLNAME = "dir-changes";
        String dirToken;
        String defaultSuffix;
        String dirExpected;
        /** if true, ok to fail on first mis-match */
        boolean fastFail;
        final ArrayList added;
        final ArrayList removed;
        final ArrayList updated;
        final ArrayList unchanged;
    
        /**
         * @param dirToken the symbol name of the base directory
         * @param clipSuffix the String suffix, if any, to clip automatically
         */
        public Spec() {
            added = new ArrayList();
            removed = new ArrayList();
            updated = new ArrayList();
            unchanged = new ArrayList();
        }
        
        /**
         * @param dirToken the symbol name of the base directory (classes, run)
         */
        public void setDirToken(String dirToken) {
            this.dirToken = dirToken;
        }
        
        /**
         * Set the directory containing the expected files.
         * Currently this only is used to verify files that are expected 
         * and found after the process completes; it does not stand on its
         * own as a specification of files.
         * @param expectedDirRelativePath path relative to the test base
         *         of the directory containing expected results for the output dir.
         */
        public void setExpDir(String expectedDirRelativePath) {
        }
        
        /**
         * @param clipSuffix the String suffix, if any, to clip automatically
         */
        public void setDefaultSuffix(String defaultSuffix) {
            this.defaultSuffix = defaultSuffix;
        }
        
        public void setAdded(String items) {
            XMLWriter.addFlattenedItems(added, items);
        }
        
        public void setRemoved(String items) {
            XMLWriter.addFlattenedItems(removed, items);
        }
        
        public void setUpdated(String items) {
            XMLWriter.addFlattenedItems(updated, items);
        }
        
        public void setUnchanged(String items) {
            XMLWriter.addFlattenedItems(unchanged, items);
        }
        public void setFastfail(boolean fastFail) {
            this.fastFail = fastFail; // XXX support in XML
        }
        
        /** @return true if some list was specified */
        private boolean hasFileList() {
            return (!LangUtil.isEmpty(added) 
            		|| !LangUtil.isEmpty(removed)
                    || !LangUtil.isEmpty(updated)
                    || !LangUtil.isEmpty(unchanged)
                    );
        }
        
        /** this writes nothing if there are no added, removed, or changed */
        public void writeXml(XMLWriter out) {
            if (!hasFileList()) {
                return;
            }
            String attr = "";
            // XXX need to permit defaults here...
            if (null != dirToken) {
                attr = out.makeAttribute("dirToken", dirToken) + " ";
            }
            if (null != defaultSuffix) {
                attr += out.makeAttribute("defaultSuffix", defaultSuffix);
            }
            out.startElement(XMLNAME, attr, false);
            if (!LangUtil.isEmpty(added)) {
                out.printAttribute("added", XMLWriter.flattenList(added));
            }
            if (!LangUtil.isEmpty(removed)) {
                out.printAttribute("removed", XMLWriter.flattenList(removed));
            }
            if (!LangUtil.isEmpty(updated)) {
                out.printAttribute("updated", XMLWriter.flattenList(updated));
            }
            if (!LangUtil.isEmpty(unchanged)) {
                out.printAttribute("unchanged", XMLWriter.flattenList(unchanged));
            }
            out.endElement(XMLNAME);
        }
        
        /**
		 * Write list as elements to XMLWriter.
		 * @param out XMLWriter output sink
		 * @param dirChanges List of DirChanges.Spec to write
		 */
		public static void writeXml(XMLWriter out, List dirChanges) {
            if (LangUtil.isEmpty(dirChanges)) {
                return;
            }
            LangUtil.throwIaxIfNull(out, "out");
            for (Iterator iter = dirChanges.iterator(); iter.hasNext();) {
				DirChanges.Spec spec = (DirChanges.Spec) iter.next();
				if (null == spec) {
                    continue;
                }
                spec.writeXml(out);
			}            
		}

} // class Spec

}
