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
 * StringAccumulator.java created on May 14, 2002
 *
 */
package org.aspectj.testing.util;



/**
 * Accumulate String with delimiters.
 */
public class StringAccumulator implements ObjectChecker {

    private final String prefix;
    private final String infix;
    private final String suffix;
    private final String nullString;
    private final StringBuffer sb;
    private int index;

    /**
     * Accumulate string with delimiter between elements,
     * treaing null elements as "".
     */
    public StringAccumulator(String delimiter) {
        this(delimiter, null, null, "");
    }
    
    /**
     * Constructor for StringAccumulator which specifies how to 
     * process each result, optionally postfixing or prefixing
     * or infixing (adding index plus infix to prefix). e.g.,
     * for prefix="[", infix="]\"", postfix="\"\n", then each entry
     * becomes a line: <pre>"[{index}]"{entry}"\n</pre>
     * 
     * @param prefix if not null, prepend to each result
     * @param infix if not null, the add index and infix before each result, after prefix
     * @param postfix if not null, append to each result
     * @param nullString if null, ignore null completely (no index); otherwise render null as nullString
     * @param type
     */
    public StringAccumulator(String prefix, String infix, String suffix, String nullString) {
        this.prefix = prefix;
        this.infix = infix;
        this.suffix = suffix;
        this.nullString = nullString;
        sb = new StringBuffer();
    }
    
    /** Clear buffer and index */
    public synchronized void clear() { 
        sb.setLength(0); 
        index = 0;
    }

    /**
     * Accumulate input.toString into
     * @return true
     * @see StandardObjectChecker#doIsValid(Object)
     */
    public synchronized boolean isValid(Object input) {
        if (input == null) {
            if (nullString == null) return true; // ignore
            input = nullString;
        }
        if (null != prefix) sb.append(prefix);
        if (null != infix) {
            sb.append(index++ + infix);
        }
        sb.append(input.toString());
        if (null != suffix) sb.append(suffix);
        return true;
    }
    
    /** @return result accumulated so far */
    public String toString() {
        return sb.toString();
    }
    /** @return result accumulated so far */
    public String debugString() {
        return "StringAccumulator prefix=" + prefix + " infix=" + infix + " suffix=" + suffix
                + " nullString=" + nullString + " index=" + index + " toString=" + toString();
    }

}
