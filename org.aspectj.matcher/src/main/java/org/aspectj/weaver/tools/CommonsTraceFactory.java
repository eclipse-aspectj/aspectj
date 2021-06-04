/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.tools;

import org.apache.commons.logging.LogFactory;
//OPTIMIZE move out of main weaver for now?
public class CommonsTraceFactory extends TraceFactory {

	private LogFactory logFactory = LogFactory.getFactory();

	public Trace getTrace(Class clazz) {
		return new CommonsTrace(clazz);
	}

}
