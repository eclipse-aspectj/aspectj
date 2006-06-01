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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 
 * Wrap FileFilter to collect any accepted 
 */
public class CollectorFileFilter implements FileFilter {
    /** returned from getFiles() when there are no files to get */
    public static final List EMPTY 
        = Collections.unmodifiableList(new ArrayList(0));

    /** used for collecting filters */
    protected ArrayList files;

    /** filter delegate - may be null */
    protected final FileFilter filter;

    /** return false from accept only when !alwaysTrue 
     * and filter is null or fails 
     */
    protected final boolean alwaysTrue;

    /** this(null, true) */
    public CollectorFileFilter() {
        this(null, true);
    }

    /*
     * @param filter the FileFilter delegate - may be null
    * @param alwaysTrue return false from accept only when !alwaysTrue 
     * and filter is null or fails 
    */
    public CollectorFileFilter(FileFilter filter, boolean alwaysTrue){
        this.filter = filter;
        this.alwaysTrue = alwaysTrue;
    }

    /**
     * Accept file into collection if filter is null or passes. 
     * @return false only when !alwaysTrue and filter fails. 
     */
    public boolean accept(File f) {
        if ((null == filter) || filter.accept(f)) {
            add(f);
            return true;
        }
        return alwaysTrue;
    }

    /** gather files */
    protected synchronized void add(File f) {
        if (null != f) {
            if (null == files) {
                files = new ArrayList();
            }
            files.add(f);
        }
    }

    /** 
     * return clone of gathered-files 
     * @return EMPTY if no files or a clone of the collection otherwise
     */
    public synchronized List getFiles() {
        if ((null == files) || (0 == files.size())) {
            return EMPTY;
        }
        return (List) files.clone();
    }
}
