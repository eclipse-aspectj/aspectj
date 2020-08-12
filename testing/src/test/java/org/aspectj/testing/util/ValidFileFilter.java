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

/**
 * FileFilter that accepts existing files
 * with static singleton variants 
 * made from inner subclasses.
 */
public class ValidFileFilter implements FileFilter {
    //----------------------------- singleton variants
    public static final FileFilter EXIST = new ValidFileFilter();
    public static final FileFilter FILE_EXISTS = new FilesOnlyFilter();
    public static final FileFilter DIR_EXISTS = new DirsOnlyFilter();
    public static final FileFilter CLASS_FILE = new ClassOnlyFilter();
    public static final FileFilter JAVA_FILE = new JavaOnlyFilter();
    public static final FileFilter RESOURCE = new ResourcesOnlyFilter();

    //----------------------------- members
    protected final FileFilter delegate;
    protected ValidFileFilter(){ 
        this(null);
    }
    protected ValidFileFilter(FileFilter delegate){
        this.delegate = delegate;
    }

    /**
     * Implement <code>FileFilter.accept(File)</code> by checking 
     * taht input is not null, exists, and is accepted by any delegate.
     */
    public boolean accept(File f) {
        return ((null != f) && (f.exists())
                && ((null == delegate) || delegate.accept(f)));
    }

    //----------------------------- inner subclasses
    static class FilesOnlyFilter extends ValidFileFilter {
        public boolean accept(File f) {
            return (super.accept(f) && (!f.isDirectory()));
        }
    }
    static class ResourcesOnlyFilter extends FilesOnlyFilter {
        public boolean accept(File f) {
            return (super.accept(f) && (FileUtil.isResourcePath(f.getPath())));
        }
    }
    static class DirsOnlyFilter extends ValidFileFilter {
        public final boolean accept(File f) {
            return (super.accept(f) && (f.isDirectory()));
        }
    }
    // todo: StringsFileFilter, accepts String[] variants for each
    static class StringFileFilter extends ValidFileFilter {
        public static final boolean IGNORE_CASE = true;
        protected final String prefix;
        protected final String substring;
        protected final String suffix;
        protected final boolean ignoreCase;
        /** true if one of the String specifiers is not null */
        protected final boolean haveSpecifier;
        public StringFileFilter(String prefix, String substring, 
                                String suffix, boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            this.prefix     = preprocess(prefix);
            this.substring  = preprocess(substring);
            this.suffix     = preprocess(suffix);
            haveSpecifier   = ((null != prefix) || (null != substring) 
                               || (null != suffix));
        }
        private final String preprocess(String input) {
            if ((null != input) && ignoreCase) {
                input = input.toLowerCase();
            }
            return input;
        }
        public boolean accept(File f) {
            if (!(super.accept(f))) {
                return false;
            } else if (haveSpecifier) {
                String path = preprocess(f.getPath());
                if ((null == path) || (0 == path.length())) {
                    return false;
                }
                if ((null != prefix) && (!(path.startsWith(prefix)))) {
                    return false;
                }
                if ((null != substring) && (!path.contains(substring))) {
                    return false;
                }
                if ((null != suffix) && (!(path.endsWith(suffix)))) {
                    return false;
                }
            } 
            return true;
        }
    } // class StringFileFilter

    static class ClassOnlyFilter extends StringFileFilter {
        ClassOnlyFilter() {
            super(null, null, ".class", IGNORE_CASE);
        }
    }
    static class JavaOnlyFilter extends StringFileFilter {
        JavaOnlyFilter() {
            super(null, null, ".java", IGNORE_CASE);
        }
    }
} // class ValidFileFilter

