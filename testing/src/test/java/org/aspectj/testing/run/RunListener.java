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

 
package org.aspectj.testing.run;

import java.io.PrintWriter;
import java.util.List;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.MessageUtil;

/**
 * A generic RunListener for easier partial implementations.
 * It can take a RunI selector (called on completion) 
 * and/or a List to accumulate complete IRunStatus
 * (if the selector is null or returns true).
 * It can also take a PrintWriter and String to print traces of each event
 * as "{prefix} [addingChild|runStarting|runCompleted]({IRunStatus})"
 */
public class RunListener implements IRunListener {
    protected final List list;
    protected final IRun selector;
    protected final PrintWriter writer;
    protected final String prefix;
     
    protected RunListener() {
        this((List) null, (IRun) null, (PrintWriter) null, (String) null);
    }
    
    /**
     * @param sink the List sink for any IRunStatus if the selector is null
     *              or returns true for run(IRunStatus) - ignored if null.
     * @param selector the IRun called on completion,
     *                  perhaps to select those to be accumulated
     *                  (should NOT throw Exception)
     */
    public RunListener(List sink, IRun selector) {
        this(sink, selector, (PrintWriter) null, (String) null);
    }

    /**
     * @param writer the PrintWriter to print events to - may be null
     * @param prefix the String prefixing any printing - if null, ""
     */
    public RunListener(PrintWriter writer, String prefix) {
        this((List) null, (IRun) null, writer, prefix);
    }
        
    /**
     * @param sink the List sink for any IRunStatus if the selector is null
     *              or returns true for run(IRunStatus) - ignored if null.
     * @param selector the IRun called on completion,
     *                  perhaps to select those to be accumulated
     *                  (should NOT throw Exception)
     * @param writer the PrintWriter to print events to - may be null
     * @param prefix the String prefixing any printing - if null, ""
     */
    public RunListener(List sink, IRun selector, PrintWriter writer, String prefix) {
        this.prefix = (null == prefix ? "" : prefix);
        this.writer = writer;
        this.selector = selector;
        list = sink;
    }
        
    /**
     * @see org.aspectj.testing.harness.run.IRunListener#addingChild(IRunStatus, IRunStatus)
     */
    public void addingChild(IRunStatus parent, IRunStatus child) {
        if (null != writer) {
            writer.println(prefix + " addingChild(\"" + parent 
                + "\", \"" + child + "\")");
        }
    }

    /**
	 * @see org.aspectj.testing.harness.run.IRunListener#runStarting(IRunStatus)
	 */
	public void runStarting(IRunStatus run) {
        if (null != writer) {
            writer.println(prefix + " runStarting(\"" + run + "\")");
        }
	}
    
    /**
     * Print to writer (if any), run selector (if any), and add to list
     * (if any and if selector is null or returns true).
	 * @see org.aspectj.testing.harness.run.IRunListener#runCompleted(IRunStatus)
     * @throws AbortException wrapping any Exception thrown by any selector
     *                         (error for selector to throw Exception)
	 */
	public void runCompleted(IRunStatus run) {
        if (null != writer) {
            writer.println(prefix + " runCompleted(\"" + run + "\")");
        }
        try {
            if (((null == selector) || selector.run(run)) && (null != list)) {
                list.add(run);
            }
        } catch (Throwable e) {
            String m = "Selectors should not throw exceptions!";
            throw new AbortException(MessageUtil.abort(m, e));
        }
	}

}
