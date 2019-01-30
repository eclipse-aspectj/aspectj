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

import java.io.PrintStream;

/**
 * Manage system err and system out streams.
 * Clients can suppress stream output during StreamsHandler lifecycle
 * and intermittantly listen to both streams if signalled on construction.
 * To print to the underlying streams (without hiding or listening),
 * use either the log methods (which manage lineation)
 * or the out and err fields.
 * <pre>
 * boolean hideStreams = true;
 * boolean listen = true;
 * StreamsHander streams = new StreamsHander(hideStreams, listen);
 * streams.startListening();
 * ...
 * streams.out.println("this goes out to without listening"); XXX verify
 * StreamsHandler.Result result = streams.stopListening();
 * streams.restoreStreams();
 * System.out.println("Suppressed output stream follows");
 * System.out.print(result.out);
 * System.out.println("Suppressed error stream follows");
 * System.out.print(result.err);
 * </pre>
 * Warning: does not distinguish streams from different threads.
 */
public class StreamsHandler {

    /** real output stream and sink for log if logToOut */
    public final PrintStream out;

    /** real error stream and sink for log if !logToOut */
    public final PrintStream err;

    /** if true, then can listen using startListening() */
    protected final boolean listening;

    /** if logToOut, then out, else err */
    private final PrintStream log;
    
    /** true if the last logged item was a newline */
    private boolean loggedLine;

    /** sniffs stream to gather test output to System.out */
    protected StreamSniffer outSniffer;

    /** sniffs stream to gather test output to System.err */
    protected StreamSniffer errSniffer;

    /** permits us to hide output stream (after sniffing by outSniffer */
    protected ProxyPrintStream outDelegate;

    /** permits us to hide error stream (after sniffing by errSniffer */
    protected ProxyPrintStream errDelegate;

    /** when sniffing, this has sniffed contents of output stream */
    protected StringBuffer outListener;

    /** when sniffing, this has sniffed contents of error stream */
    protected StringBuffer errListener;

    /** @param hide if true, then suppress stream output (can still listen) */
    public StreamsHandler(boolean listen) {
        this(listen, false);
    }

    /**
     * @param listen possible to sniff streams only if true
     * @param logToOut if true, then log methods go to System.out -- otherwise, System.err. 
     */
    public StreamsHandler(
        boolean listen,
        boolean logToOut) {
        this.err = System.err;
        this.out = System.out;
        outDelegate = new ProxyPrintStream(System.out);
        errDelegate = new ProxyPrintStream(System.err);
        this.listening = listen;
//        final PrintStream HIDE = NullPrintStream.NULL_PrintStream;
        outSniffer = new StreamSniffer(outDelegate);
        System.setOut(new PrintStream(outSniffer));
        errSniffer = new StreamSniffer(errDelegate);
        System.setErr(new PrintStream(errSniffer));
        log = (logToOut ? this.out : this.err);
        loggedLine = true;
    }

    /** render output and error streams (after sniffing) */
    public void show() {
        outDelegate.show();
        errDelegate.show();
    }

    /** suppress output and error streams (after sniffing) */
    public void hide() {
        outDelegate.hide();
        errDelegate.hide();
    }

    /** restore streams.  Do not use this after restoring. */
    public void restoreStreams() {
        if (null != outSniffer) {
            outSniffer = null;
            errSniffer = null;
            System.setOut(out);
            System.setErr(err);
        }
    }

    /** @return PrintStream used for direct logging */
    public PrintStream getLogStream() {
        return log;
    }
    
    /** log item without newline. */
    public void log(String s) {
        log.print(s);
        if (loggedLine) {
            loggedLine = false;
        }
    }

    /** 
     * Log item with newline.
     * If previous log did not have a newline, 
     * then this prepends a newline.
     */
    public void lnlog(String s) {
        if (!loggedLine) {
            log.println("");
        }
        log.println(s);
    }

    /** 
     * Start listening to both streams.
     * Tosses any old data captured.
     * (Has no effect if not listening.)
     * @throws IllegalStateException if called after restoreStreams()
     * @see endListening()
     */
    public void startListening() {
        if (null == outSniffer) {
            throw new IllegalStateException("no listening after restore");
        }
        if (listening) {
            if (null != outListener) {
                outListener.setLength(0);
                errListener.setLength(0);
            } else {
                outListener = new StringBuffer();
                outSniffer.setBuffer(outListener);
                errListener = new StringBuffer();
                errSniffer.setBuffer(errListener);
            }
        }
    }

    /** 
     * End listening to both streams and return data captured.
     * Must call startListening() first.
     * @throws IllegalStateException if called when not listening
     * @return Result with sniffed output and error String
     * @see startListening()
     */
    public Result endListening() {
        return endListening(true);
    }

    /** 
     * End listening to both streams and return data captured.
     * Must call startListening() first.
     * @param getResult if false, return Result.EMPTY 
     *         and avoid converting buffer to String.
     * @throws IllegalStateException if called when not listening
     * @return Result with sniffed output and error String
     * @see startListening()
     */
    public Result endListening(boolean getResult) {
        if (!listening) {
            return Result.EMPTY;
        }
        if (null == outListener) {
            throw new IllegalStateException("listening not started");
        }
        Result result = (!getResult ? Result.EMPTY
                : new Result(outListener.toString(), errListener.toString()));
        errListener = null;
        outListener = null;
        return result;        
    }

    /** output and error String */
    public static class Result {
        static final Result EMPTY = new Result(null, null);
        public final String out;
        public final String err;
        private Result(String out, String err) {
            this.out = out;
            this.err = err;
        }
    }
}
