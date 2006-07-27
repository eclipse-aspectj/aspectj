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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractTrace implements Trace {

	protected Class tracedClass;

	private static SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss.SSS");
	
	protected AbstractTrace (Class clazz) {
		this.tracedClass = clazz;
	}
	
	public abstract void enter (String methodName, Object thiz, Object[] args);
	
	public abstract void enter(String methodName, Object thiz);

	public abstract void exit(String methodName, Object ret);

	public abstract void exit(String methodName, Throwable th);

	public void error(String message) {
		// TODO Auto-generated method stub

	}

	public void error(String message, Throwable th) {
		// TODO Auto-generated method stub

	}

	public void event(String methodName) {
		// TODO Auto-generated method stub

	}

	public void event(String methodName, Object thiz, Object[] args) {
		// TODO Auto-generated method stub

	}

	public void exit(String methodName) {
		// TODO Auto-generated method stub

	}

	public void info(String message) {
		// TODO Auto-generated method stub

	}

	public void warn(String message) {
		// TODO Auto-generated method stub

	}

	public void warn(String message, Throwable th) {
		// TODO Auto-generated method stub

	}
	
	/*
	 * Convenience methods
	 */
	public void enter (String methodName) {
		enter(methodName,null,null);
	}

	public void enter (String methodName, Object thiz, Object arg) {
		enter(methodName,thiz,new Object[] { arg });
	}

	public void enter (String methodName, Object thiz, boolean z) {
		enter(methodName,thiz,new Boolean(z));
	}

	public void exit (String methodName, boolean b) {
		exit(methodName,new Boolean(b));
	}
	
	protected String formatMessage(String kind, String className, String methodName, Object thiz, Object[] args) {
		StringBuffer message = new StringBuffer();
		Date now = new Date();
		message.append(time.format(now)).append(" ");
		message.append(Thread.currentThread().getName()).append(" ");
		message.append(kind).append(" ");
		message.append(className);
		message.append(".").append(methodName);
		if (thiz != null) message.append(" ").append(formatObj(thiz));
		if (args != null) message.append(" ").append(formatArgs(args));
		return message.toString();
	}

	/**
	 * Format objects safely avoiding toString which can cause recursion,
	 * NullPointerExceptions or highly verbose results.
	 *  
	 * @param obj parameter to be formatted
	 * @return the formated parameter
	 */
	protected Object formatObj(Object obj) {
		
		/* These classes have a safe implementation of toString() */
		if (obj == null
				|| obj instanceof String
			    || obj instanceof Number
			    || obj instanceof Boolean
			    || obj instanceof Exception
			    || obj instanceof Character
			    || obj instanceof Class
			    || obj instanceof File
			    || obj instanceof StringBuffer
		    ) return obj;
		else try {
			
			/* Classes can provide an alternative implementation of toString() */
			if (obj instanceof Traceable) {
				Traceable t = (Traceable)obj;
				return t.toTraceString();
			}
			
			/* Use classname@hashcode */
			else return obj.getClass().getName() + "@" + Integer.toString(obj.hashCode(),16);
		
		/* Object.hashCode() can be override and may thow an exception */	
		} catch (Exception ex) {
			return obj.getClass().getName();
		}
	}

	/** 
	 * Format arguments into a comma separated list
	 * 
	 * @param names array of argument names
	 * @param args array of arguments
	 * @return the formated list
	 */
	protected String formatArgs(Object[] args) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < args.length; i++) {
			sb.append(formatObj(args[i]));
			if (i < args.length-1) sb.append(", ");
		}
		
		return sb.toString();
	}
}
