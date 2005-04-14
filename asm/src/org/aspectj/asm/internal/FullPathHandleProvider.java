/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.asm.internal;

import java.io.File;
import java.util.StringTokenizer;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IElementHandleProvider;
import org.aspectj.bridge.ISourceLocation;

/**
 * @author Mik Kersten
 */
public class FullPathHandleProvider implements IElementHandleProvider {

    static final String ID_DELIM = "|";
    
    public String createHandleIdentifier(ISourceLocation location) {
        StringBuffer sb = new StringBuffer();
        sb.append(AsmManager.getDefault()
                            .getCanonicalFilePath(location.getSourceFile()));
        sb.append(ID_DELIM);
        sb.append(location.getLine());
        sb.append(ID_DELIM);
        sb.append(location.getColumn());
        sb.append(ID_DELIM);
        sb.append(location.getOffset());
        return sb.toString();
    }
    
    public String createHandleIdentifier(File sourceFile, int line,int column,int offset) {
        StringBuffer sb = new StringBuffer();
        sb.append(AsmManager.getDefault().getCanonicalFilePath(sourceFile));
        sb.append(ID_DELIM);
        sb.append(line);
        sb.append(ID_DELIM);
        sb.append(column);
        sb.append(ID_DELIM);
        sb.append(offset);
        return sb.toString();       
    }

    public String getFileForHandle(String handle) {
        StringTokenizer st = new StringTokenizer(handle, ID_DELIM);
        String file = st.nextToken();
        return file;
    }

    public int getLineNumberForHandle(String handle) {
        StringTokenizer st = new StringTokenizer(handle, ID_DELIM);
        st.nextToken(); // skip over the file
        return new Integer(st.nextToken()).intValue();
    }
}
