/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg       initial implementation 
 * ******************************************************************/

// START-SAMPLE tracing-traceJoinPoints Trace join points executed to log

package org.aspectj.lib.tracing;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.io.*;

/**
 * Print join points being executed in context to a log.xml file.
 * To use this, define the abstract pointcuts in a subaspect.
 * @author Jim Hugunin, Wes Isberg
 */
public abstract aspect TraceJoinPoints 
    extends TraceJoinPointsBase {

    // abstract protected pointcut entry();
    
    PrintStream out;
    int logs = 0;
    int depth = 0;
    boolean terminal = false;

    /**
     * Emit a message in the log, e.g.,
     * <pre>TraceJoinPoints tjp = TraceJoinPoints.aspectOf();
     * if (null != tjp) tjp.message("Hello, World!");</pre>
     */
    public void message(String s) {
        out.println("<message>" + prepareMessage(s) + "</message>");
    }

    protected void startLog() {
        makeLogStream();
    }
    
    protected void completeLog() {
        closeLogStream();
    }
    
    protected void logEnter(JoinPoint.StaticPart jp) {
        if (terminal) out.println(">");
        indent(depth);
        out.print("<" + jp.getKind());
        writeSig(jp);
        writePos(jp);

        depth += 1;
        terminal = true;
    }
    
    protected void logExit(JoinPoint.StaticPart jp) {
        depth -= 1;
        if (terminal) {
            getOut().println("/>");
        } else {
            indent(depth);
            getOut().println("</" + jp.getKind() + ">");
        }
        terminal = false;
    }
    
    protected PrintStream getOut() {
        if (null == out) {
            String m = "not in the control flow of entry()";
            throw new IllegalStateException(m);
        }
        return out;
    }

    protected void makeLogStream() {
        try {
            String name = "log" + logs++ + ".xml";
            out = new PrintStream(new FileOutputStream(name));
        } catch (IOException ioe) {
            out = System.err;
        }
    }

    protected void closeLogStream() {
        PrintStream out = this.out;
        if (null != out) {
            out.close();
            // this.out = null;
        }
    }

    /** @return input String formatted for XML */
    protected String prepareMessage(String s) {  // XXX unimplemented
        return s; 
    } 
    
    void message(String sink, String s) {
        if (null == sink) {
            message(s);
        } else {
            getOut().println("<message sink=" + quoteXml(sink)
                        + " >" + prepareMessage(s) + "</message>");
        }
    }
    
    void writeSig(JoinPoint.StaticPart jp) {
        PrintStream out = getOut();
        out.print(" sig=");
        out.print(quoteXml(jp.getSignature().toShortString()));
    }

    void writePos(JoinPoint.StaticPart jp) {
        SourceLocation loc = jp.getSourceLocation();
        if (loc == null) return;
        PrintStream out = getOut();

        out.print(" pos=");
        out.print(quoteXml(loc.getFileName() +
                           ":" + loc.getLine() +
                           ":" + loc.getColumn()));
    }

    protected String quoteXml(String s) { // XXX weak
        return "\"" + s.replace('<', '_').replace('>', '_') + "\"";
    }

    protected void indent(int i) {
        PrintStream out = getOut();
        while (i-- > 0) out.print("  ");
    }
}
// END-SAMPLE tracing-traceJoinPoints        

          