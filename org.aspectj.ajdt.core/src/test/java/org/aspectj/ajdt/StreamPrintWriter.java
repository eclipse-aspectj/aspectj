/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.ajdt;

import java.io.*;

/**
 * Used for writing converting text written to an output stream into
 * a string.  Deprecated - use StringWriter:
 * <pre>
 * StringWriter sw = new StringWriter();
 * PrintWriter pw = new PrintWriter(sw, true);
 * ... write to pw
 * String result = sw.getBuffer().toString();
 * </pre>
 * @deprecated use StringWriter to construct PrintWriter
 * @author Mik Kersten
 */
public class StreamPrintWriter extends PrintWriter {
    private String contents = "";

    public StreamPrintWriter(Writer out) {
        super(out);
    }

    public String getContents() {
        return contents;
    }

    public void flushBuffer() {
        contents = "";
        super.flush();
    }

    public void print(char x) {
        contents += x + "\n";
    }

    public void print(char[] x) {
        contents += new String( x );
    }

    public void print(int x) {
        contents += x;
    }

    public void print(String x) {
        contents += x;
    }

    public void println(char x) {
        contents += x + "\n";
    }

    public void println(char[] x) {
        contents += new String( x ) + "\n";
    }

    public void println(int x) {
        contents += x + "\n";
    }

    public void println(String x) {
        contents += x + "\n";
    }

    public void write( byte[] x ) {
        contents += new String( x );
    }

    public void write( byte[] x, int i1, int i2 ) {
        StringWriter writer = new StringWriter();
        String s = new String( x );
        writer.write( s.toCharArray(), i1, i2 );
        contents += writer.getBuffer().toString();
    }

    public void write( int c ) {
        contents += c;
    }

    public void write( String s ) {
        contents += s;
    }

    public void write( String s, int i1, int i2 ) {
        contents += s;
    }
}
