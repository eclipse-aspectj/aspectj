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


package org.aspectj.bridge;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;


/**
 * Implement messages.
 * This implementation is immutable if ISourceLocation is immutable.
 */
public class Message implements IMessage { // XXX toString or renderer?
    private final String message;
    private final IMessage.Kind kind;
    private final Throwable thrown;
    private final ISourceLocation sourceLocation;
    
    /** convenience for constructing failure messages */
    public static Message fail(String message, Throwable thrown) {
        return new Message(message, IMessage.FAIL, thrown, null);
    }
    
    /**
     * Create a (compiler) error or warning message
     * @param message the String used as the underlying message
     * @param sourceLocation the ISourceLocation, if any, associated with this message
     * @param isError if true, use IMessage.ERROR; else use IMessage.WARNING
     */
    public Message(String message, ISourceLocation location, boolean isError) {
        this(message, (isError ? IMessage.ERROR : IMessage.WARNING), null,
            location);
    }
    
    /**
     * Create a message, handling null values for message and kind
     * if thrown is not null.
     * @param message the String used as the underlying message
     * @param kind the IMessage.Kind of message - not null
     * @param thrown the Throwable, if any, associated with this message
     * @param sourceLocation the ISourceLocation, if any, associated with this message
     * @throws IllegalArgumentException if message is null and
     * thrown is null or has a null message, or if kind is null
     * and thrown is null.
     */
    public Message(String message, IMessage.Kind kind, Throwable thrown,
                    ISourceLocation sourceLocation) {
        this.message = message;
        this.kind = kind;
        this.thrown = thrown;
        this.sourceLocation = sourceLocation;
        if (null == message) {
            if (null != thrown) {
                message = thrown.getMessage();
            } 
            if (null == message) {
                throw new IllegalArgumentException("null message");
            }
        }
        if (null == kind) {
             throw new IllegalArgumentException("null kind");
        }
    }
    
    /** @return the kind of this message */
    public IMessage.Kind getKind() {
        return kind;
    }

    /** @return true if kind == IMessage.ERROR */
    public boolean isError() {
        return kind == IMessage.ERROR;
    }
    
    /** @return true if kind == IMessage.WARNING */
    public boolean isWarning() {
        return kind == IMessage.WARNING;
    }

    /** @return true if kind == IMessage.DEBUG */
    public boolean isDebug() {
        return kind == IMessage.DEBUG;
    }

    /** 
     * @return true if kind == IMessage.INFO  
     */
    public boolean isInfo() {
        return kind == IMessage.INFO;
    }
    
    /** @return true if  kind == IMessage.ABORT  */
    public boolean isAbort() {
        return kind == IMessage.ABORT;
    }    
    
    /** 
     * @return true if kind == IMessage.FAIL
     */
    public boolean isFailed() {
        return kind == IMessage.FAIL;
    }
    
    /** @return non-null String with simple message */
    final public String getMessage() {
        return message;
    }
    
    /** @return Throwable associated with this message, or null if none */
    final public Throwable getThrown() {
        return thrown;
    }

    /** @return ISourceLocation associated with this message, or null if none */
    final public ISourceLocation getISourceLocation() {
        return sourceLocation;
    }
    
    public String toString() {
        return Message.renderToString(this);
    }
    
    public static String renderToString(IMessage message) { 
        ISourceLocation loc = message.getISourceLocation();
        String locString = (null == loc ? "" : " at " + loc);
        Throwable thrown = message.getThrown();
        return message.getKind() + locString + ": " + message.getMessage()
            + (null == thrown ? "" : render(thrown));
    }

    public static String render(Throwable thrown) { // XXX cf LangUtil.debugStr
        if (null == thrown) return "null throwable";
        Throwable t = null;
        if (thrown instanceof InvocationTargetException) {
            t = ((InvocationTargetException)thrown).getTargetException();
        } else if (thrown instanceof ClassNotFoundException) {
            t = ((ClassNotFoundException) thrown).getException();
        }
        if (null != t) {
            return render(t);
        }
        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println(" Message rendering thrown=" + thrown.getClass().getName());
        writer.println(thrown.getMessage());
        thrown.printStackTrace(writer);
        try { buf.close(); } 
        catch (IOException ioe) {} 
        return buf.getBuffer().toString();        
    }

}
