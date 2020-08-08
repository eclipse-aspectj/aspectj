/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     2004 updates
 * ******************************************************************/

package org.aspectj.testing.xml;


import java.io.File;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.LangUtil;

/**
 * A mutable ISourceLocation for XML initialization of tests.
 * This does not support reading/writing of the attributes
 * column, context, or endline.
 */
public class SoftSourceLocation implements ISourceLocation  {
    public static final File NONE = ISourceLocation.NO_FILE;
    public static final String XMLNAME = "source";

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
       // other attributes not supported
       File file = sl.getSourceFile();
       if ((null != file) && !ISourceLocation.NO_FILE.equals(file)) {
           String value = XMLWriter.attributeValue(file.getPath());
           out.printAttribute("file", value);
       }
       out.endElement(elementName);
    }

    private File sourceFile;
    private int line = -1; // required for no-line comparisons to work
    private int column;
    private int endLine;
    private String context;
    
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
    public String getContext() {
        return context;
    }
    
    public void setFile(String sourceFile) {
        this.sourceFile = new File(sourceFile);
    }
    
    public void setLine(String line) {
        setLineAsString(line);
    }

    public void setLineAsString(String  line) {
        this.line = convert(line);
        if (0 == endLine) {
            endLine = this.line;
        }
    }
    public String getLineAsString() {
        return ""+line;
    }
    
    public void setColumn(String column) {
        this.column = convert(column);
    }
    
    public void setEndLine(String line) {
        this.endLine = convert(line);
    }

    public void setContext(String context) {
        this.context = context;    
    }
    
    private int convert(String in) {
        return Integer.valueOf(in);
    }

	public String getLocationContext() {
		return null;
	}
	
	public int getOffset() {
		return -1;
	}
    
    /** @return String : {context\n}file:line:column */
    public String toString() {
        return (null == context ? "" : context + LangUtil.EOL)
            + getSourceFile().getPath() 
            + ":" + getLine() ;
    }

	public String getSourceFileName() {
		return null;
	}
}
