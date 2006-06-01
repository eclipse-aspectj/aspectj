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


/*
 * NullPrintStream.java created on May 29, 2002
 *
 */
package org.aspectj.testing.util;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Ignore any output to a NullPrintStream.
 * Clients use singleton NULL_PrintStream or NULL_OutputStream.
 * @author isberg
 */
public final class NullPrintStream extends PrintStream {

    public static final OutputStream NULL_OutputStream = NullOutputStream.ME;
    public static final PrintStream NULL_PrintStream = new NullPrintStream();

    private NullPrintStream() {
        super(NULL_OutputStream);
    }
    public void write(int b) {
    }
    public void write(byte[] b) {
    }
    public void write(byte[] b, int off, int len) {
    }
    public void print(boolean arg0) {
    }
    public void print(char arg0) {
    }
    public void print(char[] arg0) {
    }
    public void print(double arg0) {
    }
    public void print(float arg0) {
    }
    public void print(int arg0) {
    }
    public void print(long arg0) {
    }
    public void print(Object arg0) {
    }
    public void print(String arg0) {
    }
    public void println() {
    }
    public void println(boolean arg0) {
    }
    public void println(char arg0) {
    }
    public void println(char[] arg0) {
    }
    public void println(double arg0) {
    }
    public void println(float arg0) {
    }
    public void println(int arg0) {
    }
    public void println(long arg0) {
    }
    public void println(Object arg0) {
    }
    public void println(String arg0) {
    }

}

final class NullOutputStream extends OutputStream {
    static final OutputStream ME = new NullOutputStream();

    private NullOutputStream() {
    }
    public void write(int b) {
    }
    public void write(byte[] b) {
    }
    public void write(byte[] b, int off, int len) {
    }
}
