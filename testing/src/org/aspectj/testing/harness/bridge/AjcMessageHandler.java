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

package org.aspectj.testing.harness.bridge;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.util.BridgeUtil;
import org.aspectj.testing.util.Diffs;
import org.aspectj.util.LangUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * Adapter to pass messages through to handler,
 * suppress output of expected errors and warnings,
 * and calculate differences between expected and
 * actual messages.
 */
public class AjcMessageHandler extends MessageHandler {     

    /** Comparator for enclosed IMessage diffs, etc. */
    public static final Comparator COMP_IMessage
        = BridgeUtil.Comparators.WEAK_IMessage;
        
    /** Comparator for enclosed File diffs, etc. */
    public static final Comparator COMP_File
        = BridgeUtil.Comparators.WEAK_File;


    /** unmodifiable list of IMessage messages of type IMesssage.ERROR */
    final List expectedErrors; // revert to IMessageHolder for generality?
    
    /** unmodifiable list of IMessage messages of type IMesssage.WARNING */
    final List expectedWarnings;

    /** unmodifiable list of File expected to be recompiled */
    final List expectedRecompiled;
    
    /** list of File actually recompiled */
    List actualRecompiled;
    
    /** if true, ignore warnings when calculating diffs and passed() */
    boolean ignoreWarnings;
    
    /** cache expected/actual diffs, nullify if any new message */
    transient CompilerDiffs diffs;

	private boolean expectingCommandTrue;
    
    /** 
     * @param messages List of IMessage to extract ERROR and WARNING from 
     */
    AjcMessageHandler(List messages) {
        this(MessageUtil.getMessages(messages, IMessage.ERROR),
            MessageUtil.getMessages(messages, IMessage.WARNING));
    }

   /** 
     * @param errors unmodifiable List of IMessage of kind ERROR to adopt
     * @param warnings unmodifiable List of IMessage of kind WARNING to adopt 
     */
    AjcMessageHandler(List errors, List warnings) {
        this(errors, warnings, null);
    }

    AjcMessageHandler(List errors, List warnings, List recompiled) {
        this.expectedErrors = LangUtil.safeList(errors);
        this.expectedWarnings = LangUtil.safeList(warnings);
        this.expectedRecompiled = LangUtil.safeList(recompiled);
        expectingCommandTrue = (0 == expectedErrors.size());
    }
    
    public void setIgnoreWarnings(boolean ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
    }
    
    /** clear out any actual values to be re-run */
    public void init() {
        super.init();
        actualRecompiled = null;
        diffs = null;
    }

    /**
     * Return true if we have this kind of
     * message for the same line and store all messages.
     * @see bridge.tools.impl.ErrorHandlerAdapter#doShowMessage(IMessage)
     * @return true if message handled (processing should abort)
     */
    public boolean handleMessage(IMessage message) {
        if (null == message) {
            throw new IllegalArgumentException("null message");
        }
        messages.add(message);
        IMessage.Kind kind = message.getKind();
        return expecting(message, getExpected(kind));
    }
    
    /** 
     * Set the actual files recompiled.
     * @param List of File recompiled - may be null; adopted but not modified
     * @throws IllegalStateException if they have been set already.
     */
    public void setRecompiled(List list) {
        if (null != actualRecompiled) {
            throw new IllegalStateException("actual recompiled already set");
        }
        this.actualRecompiled = LangUtil.safeList(list);
    }
    
    /** @return immutable List of IMessage expected of this kind */
    public List getExpected(IMessage.Kind kind) {
        if (IMessage.ERROR == kind) {
            return expectedErrors;
        } else if (IMessage.WARNING == kind) {
            return expectedWarnings;
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    /** 
     * Check if the message was expected, and clear diffs if not.
     * @return true if we expect a message of this kind with this line number 
     */
    protected boolean expecting(IMessage message, List sink) { // XXX ignores File
        if ((null != message) && (0 < sink.size())) {
            // XXX need to cache list: int[] errLines, warningLines;
            for (Iterator iter = sink.iterator(); iter.hasNext();) {
    			IMessage m = (IMessage) iter.next();
                if (0 == COMP_IMessage.compare(message, m)) {
                    return true;
                }
    		}
        }
        if (null != diffs) {
            diffs = null;
        }
        return false;
    }

    /** Generate differences between expected and actual errors and warnings */
    public CompilerDiffs getCompilerDiffs() {
        if (null == diffs) {
            final List actualErrors = Arrays.asList(getMessages(IMessage.ERROR, IMessageHolder.EQUAL));
            final List actualWarnings = Arrays.asList(getMessages(IMessage.WARNING, IMessageHolder.EQUAL));
            Diffs errors = new Diffs("error", expectedErrors, actualErrors, COMP_IMessage);
            Diffs warnings = new Diffs("warning", expectedWarnings, actualWarnings, COMP_IMessage);
            Diffs recompiled = new Diffs("recompiled", expectedRecompiled, actualRecompiled, COMP_File);
            diffs = new CompilerDiffs(errors, warnings, recompiled);
        }
        return diffs;        
    }
    
    /** calculate passed based on default or set value for ignoreWarnings */
    public boolean passed() {
        return passed(ignoreWarnings);
    }
    
    /** @return true if we are expecting the command to fail - i.e., any expected errors */
    public boolean expectingCommandTrue() {
        return expectingCommandTrue;
    }
    
    /**
     * Get the (current) result of this run,
     * ignoring differences in warnings on request.
     * Note it may return passed (true) when there are expected error messages.
     * @return false 
     * if there are any fail or abort messages,
     * or if the expected errors, warnings, or recompiled do not match actual.
     * 
     */
    public boolean passed(boolean ignoreWarnings) {
        if (hasAnyMessage(IMessage.FAIL, IMessageHolder.EQUAL)) {
            return false;
        }   
        
        CompilerDiffs diffs = getCompilerDiffs();
        if (!ignoreWarnings) {
            return (!diffs.different);
        } else {
            return ((!diffs.errors.different) && (!diffs.recompiled.different));
        }
    }
    
    /**
     * Report results to a handler,
     * adding all messages
     * and creating fail messages for each diff.
     */
    public void report(IMessageHandler handler) {
        if (null == handler) {
            MessageUtil.debug(this, "report got null handler");
        }
        IMessage[] messages = getMessages(null, IMessageHolder.EQUAL);
        for (int i = 0; i < messages.length; i++) {
			handler.handleMessage(messages[i]);
		}
        CompilerDiffs diffs = getCompilerDiffs();
        if (diffs.different) {
            diffs.errors.report(handler, IMessage.FAIL);
            diffs.warnings.report(handler, IMessage.FAIL);
            diffs.recompiled.report(handler, IMessage.FAIL);
        }
    }
    
    /** @return String consisting of differences and any other messages */
    public String toString() {
        CompilerDiffs diffs = getCompilerDiffs();
        StringBuffer sb = new StringBuffer(super.toString());
        final String EOL = "\n";
        sb.append(EOL);
        render(sb, "  unexpected error ", EOL, diffs.errors.unexpected);
        render(sb, "unexpected warning ", EOL, diffs.warnings.unexpected);
        render(sb, "     missing error ", EOL, diffs.errors.missing);
        render(sb, "   missing warning ", EOL, diffs.warnings.missing);
        render(sb, "              fail ", EOL, getList(IMessage.FAIL));
        render(sb, "             abort ", EOL, getList(IMessage.ABORT));
        render(sb, "              info ", EOL, getList(IMessage.INFO));
        return sb.toString(); // XXX cache toString
    }
    
    /** @return immutable list of a given kind - use  null for all kinds */
    private List getList(IMessage.Kind kind) {
        if ((null == kind) || (0 == numMessages(kind, IMessageHolder.EQUAL))) {
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(getMessages(kind, IMessageHolder.EQUAL));        
    }
    
    /** @return "" if no items or {prefix}{item}{suffix}... otherwise */
    private void render( // LangUtil instead?
        StringBuffer result,
        String prefix,
        String suffix,
        List items) {
        if ((null != items)) {
            for (Iterator iter = items.iterator(); iter.hasNext();) {
			     result.append(prefix + iter.next() + suffix);
            }
        }
    }
    
    /** compiler results for errors, warnings, and recompiled files */
    public static class CompilerDiffs {
        public final Diffs errors;
        public final Diffs warnings;
        public final Diffs recompiled;
        public final boolean different;
        
        public CompilerDiffs(Diffs errors, Diffs warnings) {
            this(errors, warnings, Diffs.NONE);
        }
        public CompilerDiffs(Diffs errors, Diffs warnings, Diffs recompiled) {
            this.errors = errors;
            this.warnings = warnings;
            this.recompiled = recompiled;
            different = (warnings.different || errors.different
                         || recompiled.different);
        }
    }
}
