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

import java.util.logging.Level;
import java.util.logging.Logger;

public class Jdk14Trace extends AbstractTrace {

	private Logger logger;
	private String name;
	
	public Jdk14Trace (Class clazz) {
		super(clazz);
		this.name = clazz.getName();
		this.logger = Logger.getLogger(name);
	}
	
	public void enter(String methodName, Object thiz, Object[] args) {
		if (logger.isLoggable(Level.FINE)) {
			logger.entering(name,methodName,formatObj(thiz));
			if (args != null && logger.isLoggable(Level.FINER)) {
				logger.entering(name,methodName,args);
			}			
		}
	}

	public void enter(String methodName, Object thiz) {
		enter(methodName,thiz,null);
	}

	public void exit(String methodName, Object ret) {
		if (logger.isLoggable(Level.FINE)) {
			logger.exiting(name,methodName,ret);
		}
	}

	public void exit(String methodName, Throwable th) {
		if (logger.isLoggable(Level.FINE)) {
			logger.exiting(name,methodName,th);
		}
	}

	public void exit(String methodName) {
		if (logger.isLoggable(Level.FINE)) {
			logger.exiting(name,methodName);
		}
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isLoggable(Level.FINE);
	}
	
}
