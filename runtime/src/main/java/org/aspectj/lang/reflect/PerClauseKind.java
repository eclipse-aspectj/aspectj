/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.lang.reflect;

/**
 * The different per-clauses (aspect instantiation models)
 * supported by AspectJ
 */
public enum PerClauseKind {
	SINGLETON,
	PERTHIS,
	PERTARGET,
	PERCFLOW,
	PERCFLOWBELOW,
	PERTYPEWITHIN;
}
