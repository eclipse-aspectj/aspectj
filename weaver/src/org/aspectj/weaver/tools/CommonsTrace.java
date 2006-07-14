/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.tools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonsTrace extends AbstractTrace {

	private Log log;
	private String className;
	
	public CommonsTrace (Class clazz) {
		super(clazz);
		this.log = LogFactory.getLog(clazz);
		this.className = tracedClass.getName();
	}
	
	public void enter(String methodName, Object thiz, Object[] args) {
		if (log.isDebugEnabled()) {
			log.debug("> " + formatMessage(className, methodName, thiz, args));
		}
	}

	public void enter(String methodName, Object thiz) {
		if (log.isDebugEnabled()) {
			log.debug("> " + formatMessage(className, methodName, thiz, null));
		}
	}

	public void exit(String methodName, Object ret) {
		if (log.isDebugEnabled()) {
			log.debug("< " + formatMessage(className, methodName, ret, null));
		}
	}

	public void exit(String methodName, Throwable th) {
		if (log.isDebugEnabled()) {
			log.debug("< " + formatMessage(className, methodName, th, null));
		}
	}

	public void exit(String methodName) {
		if (log.isDebugEnabled()) {
			log.debug("< " + formatMessage(className, methodName, null, null));
		}
	}

}
