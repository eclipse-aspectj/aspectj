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

package org.aspectj.testing.util;

import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;

/** result struct for expected/actual diffs for Collection */
public class Diffs {

    public static final Filter ACCEPT_ALL = new Filter() {
        public boolean accept(Object o) {
            return true;
        }
    };
     // XXX List -> Collection b/c comparator orders 
    public static final Diffs NONE 
        = new Diffs("NONE", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    
    /** name of the thing being diffed - used only for reporting */
    public final String label;
    
    /** immutable List */
    public final List  missing; 

    /** immutable List */
    public final List unexpected;

    /** true if there are any missing or unexpected */
    public final boolean different;
    
    private Diffs(String label, List missing, List unexpected) {
        this.label = label;
        this.missing = missing;
        this.unexpected = unexpected;
        different = ((0 != this.missing.size()) 
                      || (0 != this.unexpected.size()));
    }

    public Diffs(
        String label, 
        List expected, 
        List actual, 
        Comparator comparator) {
        this(label, expected, actual, comparator, ACCEPT_ALL, ACCEPT_ALL);
    }
    
    public Diffs(
        String label, 
        List expected, 
        List actual, 
        Comparator comparator,
        Filter missingFilter,
        Filter unexpectedFilter) {
        label = label.trim();
        if (null == label) {
            label = ": ";
        } else if (!label.endsWith(":")) {
            label += ": ";
        }
        this.label = " " + label;
        ArrayList miss = new ArrayList();
        ArrayList unexpect = new ArrayList();
        
        org.aspectj.testing.util.LangUtil.makeSoftDiffs(expected, actual, miss, unexpect, comparator);
        if (null != missingFilter) {
            for (ListIterator iter = miss.listIterator(); iter.hasNext();) {
                if (!missingFilter.accept(iter.next())) {
                    iter.remove();
                }
            }
        }
        missing = Collections.unmodifiableList(miss);
        if (null != unexpectedFilter) {
            for (ListIterator iter = unexpect.listIterator(); iter.hasNext();) {
                if (!unexpectedFilter.accept(iter.next())) {
                    iter.remove();
                }
            }
        }
        unexpected = Collections.unmodifiableList(unexpect);
        different = ((0 != this.missing.size()) 
                      || (0 != this.unexpected.size()));
    }
    
    /** 
     * Report missing and extra items to handler.
     * For each item in missing or unexpected, this creates a {kind} IMessage with 
     * the text "{missing|unexpected} {label}: {message}"
     * where {message} is the result of 
     * <code>MessageUtil.renderMessage(IMessage)</code>.
     * @param handler where the messages go - not null
     * @param kind the kind of message to construct - not null
     * @param label the prefix for the message text - if null, "" used
     * @see MessageUtil#renderMessage(IMessage)
     */
    public void report(IMessageHandler handler, IMessage.Kind kind) {
        LangUtil.throwIaxIfNull(handler, "handler");
        LangUtil.throwIaxIfNull(kind, "kind");
        if (different) {
            for (Iterator iter = missing.iterator(); iter.hasNext();) {
                String s = MessageUtil.renderMessage((IMessage) iter.next());
                MessageUtil.fail(handler, "missing " + label + s);         
            }
            for (Iterator iter = unexpected.iterator(); iter.hasNext();) {
                String s = MessageUtil.renderMessage((IMessage) iter.next());
                MessageUtil.fail(handler, "unexpected " + label + s);         
            }
        }
    }
    
    /** @return "{label}: (unexpected={#}, missing={#})" */
    public String toString() {
        return label + "(unexpected=" + unexpected.size() 
            + ", missing=" + missing.size() + ")";
    }
    public static interface Filter {
        /** @return true to keep input in list of messages */
        boolean accept(Object input);
    }
}

