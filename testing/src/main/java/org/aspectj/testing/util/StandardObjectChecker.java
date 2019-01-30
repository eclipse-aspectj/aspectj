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
 * StandardObjectChecker.java created on May 7, 2002
 *
 */
package org.aspectj.testing.util;

import java.util.List;

/**
 * Superclass for checkers that require non-null input
 * of a given type.
 * Clients may supply delegator for further checks,
 * or a list to collect results.
 * Subclasses may instead implement doIsValid().
 * @author isberg
 */
public class StandardObjectChecker implements ObjectChecker {

    public final Class type;
    private final ObjectChecker delegate;
    private final List collector;
    private final boolean collectionResult;
    
    /**
     * Create one with no delegate.
     * @param type the Class of the type required of the input
     */
    public StandardObjectChecker(Class type) {
        this(type, ANY, (List) null, true);
    }
    
    /**
     * @param type the Class of the type required of the input
     * @param delegate the ObjectChecker to delegate to after
     *         checking for non-null input of the correct type.
     */
    public StandardObjectChecker(Class type, ObjectChecker delegate) {
        this(type, delegate, null, true);
    }

    /**
     * same as StandardObjectChecker(type, collector, true)
     * @param type the Class of the type required of the input
     * @param collector the list to collect valid entries
     */
    public StandardObjectChecker(Class type, List collector) {
        this(type, ANY, collector, true);
    }
    
    /**
     * @param type the Class of the type required of the input
     * @param collector the list to collect valid entries
     * @param collectionResult the value to return when entry was added
     */
    public StandardObjectChecker(Class type, List collector, boolean collectionResult) {
        this(type, ANY, collector, collectionResult);
    }
    
    /**
     * @param type the Class of the type required of the input
     * @param collector the list to collect valid entries
     */
    public StandardObjectChecker(Class type, ObjectChecker delegate, 
                                List collector, boolean collectionResult) {
        if (null == type) throw new IllegalArgumentException("null type");
        this.type = type;
        this.delegate = delegate;
        this.collector = collector;
        this.collectionResult = collectionResult;
    }
    
    /**
     * Check if object is valid by confirming is is non-null and of the
     * right type, then delegating to any delegate or calling doIsValid(),
     * then (if true) passing to any collector, and returning
     * false if the collector failed or the collection result otherwise.
     * @see ObjectChecker#isValid(Object)
     * @return true unless input is null or wrong type 
     *          or if one of subclass (doIsValid(..)) or delegates
     *          (list, collector) returns false.
     */
    public final boolean isValid(Object input) {
        if ((null == input) || (!(type.isAssignableFrom(input.getClass())))) {
            return false;
        } else if (null != delegate) {
            if (!delegate.isValid(input)) {
                return false;
            }
        } 
        if (!doIsValid(input)) {
            return false;
        }
        if (null == collector) {
            return true;
        } else if (!collector.add(input)) {
            return false;
        } else {
            return collectionResult;
        }
    }
    
    /**
     * Delegate of isValid guarantees that the input
     * is not null as is assignable to the specified type.
     * Subclasses implement their funtionality here.
     * This implementation returns true;
     * @return true
     */
    public boolean doIsValid(Object input) {
        return true;
    }

}
