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
import java.util.Vector;

/** 
 * A FileFilter that accumulates the results when called if they exist.
 * Subclasses override accumulate to determine whether it should be
 * accumulated.
 */
public class AccumulatingFileFilter extends ValidFileFilter {
    Vector<File> files = new Vector<>();
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
        int numFiles = files.size();
        File[] result = new File[numFiles];
        if (0 < numFiles) {
            files.copyInto(result);
        }
        return result;
    }
}
