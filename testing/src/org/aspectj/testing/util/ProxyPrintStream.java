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

package org.aspectj.testing.util;

import java.io.IOException;
import java.io.PrintStream;

/** Wrap a delegate PrintStream, permitting output to be suppressed. */
public class ProxyPrintStream extends PrintStream {

    private final PrintStream delegate;
    private boolean hiding;
    public ProxyPrintStream(PrintStream delegate ) {
        super(NullPrintStream.NULL_OutputStream);
        LangUtil.throwIaxIfNull(delegate, "delegate");
        this.delegate = delegate;
    }
    public void hide() {
        hiding = true;
    }
    public void show() {
        hiding = false;
    }
    public boolean isHiding() {
        return hiding;
    }
    public void write(int b) {
        if (!hiding) delegate.write(b);
    }
    public void write(byte[] b) throws IOException {
        if (!hiding) delegate.write(b);
    }
    public void write(byte[] b, int off, int len) {
        if (!hiding) delegate.write(b, off, len);
    }
    public void print(boolean arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(char arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(char[] arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(double arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(float arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(int arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(long arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(Object arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void print(String arg0) {
        if (!hiding) delegate.print(arg0);
    }
    public void println() {
        if (!hiding) delegate.println();
    }
    public void println(boolean arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(char arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(char[] arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(double arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(float arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(int arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(long arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(Object arg0) {
        if (!hiding) delegate.println(arg0);
    }
    public void println(String arg0) {
        if (!hiding) delegate.println(arg0);
    }
}
