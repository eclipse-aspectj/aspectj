/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.util.*;

public class ProblemFactory extends DefaultProblemFactory {

static SimpleLookupTable factories = new SimpleLookupTable(5);

private ProblemFactory(Locale locale) {
	super(locale);
}

public static ProblemFactory getProblemFactory(Locale locale) {
	ProblemFactory factory = (ProblemFactory) factories.get(locale);
	if (factory == null)
		factories.put(locale, factory = new ProblemFactory(locale));
	return factory;
}
}
