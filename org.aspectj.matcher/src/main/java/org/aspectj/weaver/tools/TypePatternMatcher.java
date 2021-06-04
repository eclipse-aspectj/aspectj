/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * ******************************************************************/
package org.aspectj.weaver.tools;

/**
 * A compiled AspectJ type pattern that can be used to
 * match against types at runtime.
 */
public interface TypePatternMatcher {

	/**
	 * Does this type pattern matcher match the
	 * given type (Class).
	 */
	boolean matches(Class aClass);
}
