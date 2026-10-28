/* *******************************************************************
 * Copyright (c) 1999-2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/
package org.aspectj.testing.util;

/**
 * Check input for validity.
 */
public interface ObjectChecker<T> {

	/** this returns true for any input, even if null */
	ObjectChecker<Object> ANY = new ObjectChecker<Object>() {
		public final boolean isValid(Object input) {
			return true;
		}

		public final String toString() {
			return "ObjectChecker.ANY";
		}
	};

	/** this returns true for any non-null object */
	ObjectChecker<Object> NOT_NULL = new ObjectChecker<Object>() {
		public boolean isValid(Object input) {
			return (null != input);
		}

		public String toString() {
			return "ObjectChecker.NOT_NULL";
		}
	};

	/** @return true if input is 0 Integer or any other non-Integer reference. */
	ObjectChecker<Object> ANY_ZERO = new ObjectChecker<Object>() {
		public boolean isValid(Object input) {
			if (input instanceof Integer) {
				return (0 == (Integer) input);
			} else {
				return true;
			}
		}

		public String toString() {
			return "ObjectChecker.ANY_ZERO";
		}
	};

	/**
	 * Check input for validity.
	 * 
	 * @param input the Object to check
	 * @return true if input is ok
	 */
	boolean isValid(T input);
}
