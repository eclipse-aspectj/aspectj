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

/**
  * Check input for validity.
  */
public interface ObjectChecker {
    /** this returns true for any input, even if null */
	ObjectChecker ANY = new ObjectChecker() {
        public final boolean isValid(Object input) { return true; }
        public final String toString() { return "ObjectChecker.ANY"; }
    };

    /** this returns true for any non-null object */
	ObjectChecker NOT_NULL = new ObjectChecker() {
        public boolean isValid(Object input) { return (null != input); }
        public String toString() { return "ObjectChecker.NOT_NULL"; }
    };

    /** @return true if input is 0 Integer or any other non-Integer reference. */
	ObjectChecker ANY_ZERO = new ObjectChecker() {
        public boolean isValid(Object input) {
            if (input instanceof Integer) {
                return (0 == (Integer) input);
            } else {
                return true;
            }
        }
        public String toString() { return "ObjectChecker.ANY_ZERO"; }
    };

    /**
     * Check input for validity.
     * @param input the Object to check
     * @return true if input is ok
     */
	boolean isValid(Object input);
}
