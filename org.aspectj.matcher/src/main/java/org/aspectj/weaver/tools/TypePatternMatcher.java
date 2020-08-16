/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
