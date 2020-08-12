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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// OPTIMIZE move out for now? check what doc says about using these variants on trace (commons/14)
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
				logger.entering(name,methodName,formatObjects(args));
			}			
		}
	}

	public void enter(String methodName, Object thiz) {
		enter(methodName,thiz,null);
	}

	public void exit(String methodName, Object ret) {
		if (logger.isLoggable(Level.FINE)) {
			logger.exiting(name,methodName,formatObj(ret));
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

	public void event(String methodName, Object thiz, Object[] args) {
		if (logger.isLoggable(Level.FINE)) {
			logger.logp(Level.FINER,name,methodName,"EVENT",formatObj(thiz));
			if (args != null && logger.isLoggable(Level.FINER)) {
				logger.logp(Level.FINER,name,methodName,"EVENT",formatObjects(args));
			}			
		}
	}

	public void event(String methodName) {
		if (logger.isLoggable(Level.FINE)) {
			logger.logp(Level.FINER,name,methodName,"EVENT");
		}
	}

	public boolean isTraceEnabled() {
		return logger.isLoggable(Level.FINER);
	}

	public void setTraceEnabled (boolean b) {
		if (b) {
			logger.setLevel(Level.FINER);
			Handler[] handlers = logger.getHandlers();
			if (handlers.length == 0) {
				Logger parent = logger.getParent();
				if (parent != null) handlers = parent.getHandlers();
			}
			for (Handler handler : handlers) {
				handler.setLevel(Level.FINER);
			}
		}
		else {
			logger.setLevel(Level.INFO);
		}
	}

	public void debug (String message) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(message);
		}
	}

	public void info(String message) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info(message);
		}
	}

	public void warn (String message, Throwable th) {
		if (logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING,message,th);
		}
	}

	public void error (String message, Throwable th) {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE,message,th);
		}
	}

	public void fatal (String message, Throwable th) {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE,message,th);
		}
	}
	
}
