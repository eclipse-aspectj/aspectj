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

package org.aspectj.testing.xml;


import org.aspectj.bridge.ISourceLocation;

import java.io.File;

/**
 * Immutable source location.
 * This guarantees that the source file is not null
 * and that the numeric values are positive and line <= endLine.
 * @see org.aspectj.lang.reflect.SourceLocation
 * @see org.aspectj.compiler.base.parser.SourceInfo
 * @see org.aspectj.tools.ide.SourceLine
 * @see org.aspectj.testing.harness.ErrorLine
 */
public class SoftSourceLocation implements ISourceLocation  { // XXX endLine?
    public static final File NONE = new File("SoftSourceLocation.NONE");
    public static final String XMLNAME = "source-location";

    /**
     * Write an ISourceLocation as XML element to an XMLWriter sink.
     * @param out the XMLWriter sink
     * @param sl the ISourceLocation to write.
     */
    public static void writeXml(XMLWriter out, ISourceLocation sl) {
       if ((null == out) || (null == sl)) {
            return;
       }
       final String elementName = XMLNAME;
       out.startElement(elementName, false);
       out.printAttribute("line", "" + sl.getLine());
       out.printAttribute("column", "" + sl.getColumn());
       out.printAttribute("endLine", "" + sl.getEndLine());
       File file = sl.getSourceFile();
       if (null != file) {
           out.printAttribute("sourceFile", file.getPath());
       }
       out.endElement(elementName);
    }

    private File sourceFile;
    private int line;
    private int column;
    private int endLine;
    
    public SoftSourceLocation() {
    }
    
    public File getSourceFile() {
        return (null != sourceFile ? sourceFile : NONE);
    }
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public int getEndLine() {
        return line;
    }
    
    public void setFile(String sourceFile) {
        this.sourceFile = new File(sourceFile);
    }

    public void setLine(String  line) {
        this.line = convert(line);
        if (0 == endLine) {
            endLine = this.line;
        }
    }
    
    public void setColumn(String column) {
        this.column = convert(column);
    }
    
    public void setEndLine(String line) {
        this.endLine = convert(line);
    }
    
    
    private int convert(String in) {
        return Integer.valueOf(in).intValue();
    }

	public String getLocationContext() {
		return null;
	}
    
    /** @return String : file:line:column */
    public String toString() {
    	return getSourceFile().getPath() + ":" + getLine() + ":" + getColumn();
    }
}
