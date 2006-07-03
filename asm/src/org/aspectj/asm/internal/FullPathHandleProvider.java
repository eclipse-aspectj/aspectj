/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.asm.internal;

import java.io.File;
import java.util.StringTokenizer;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IElementHandleProvider;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;

/**
 * HandleProvider of the form '<full path to src file>|line|column|offset'
 * 
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

	public int getOffSetForHandle(String handle) {
		StringTokenizer st = new StringTokenizer(handle, ID_DELIM);
        st.nextToken(); // skip over the file
        st.nextToken(); // skip over the line number
        st.nextToken(); // skip over the column
        return new Integer(st.nextToken()).intValue();
	}

	public String createHandleIdentifier(IProgramElement ipe) {
		if (ipe == null) return null;
		if (ipe.getHandleIdentifier(false) != null) {
			return ipe.getHandleIdentifier(false);
		}
		String handle = null;  
		if (ipe.getSourceLocation() != null) {
			handle = createHandleIdentifier(ipe.getSourceLocation());
		} else {
			handle = createHandleIdentifier(ISourceLocation.NO_FILE,-1,-1,-1);
		}
		ipe.setHandleIdentifier(handle);
		return handle;
	}

	public boolean dependsOnLocation() {
		// handles contain information from the source location therefore 
		// return true;
		return true;
	}

	public void initialize() {
		// nothing to initialize...
	}
}
