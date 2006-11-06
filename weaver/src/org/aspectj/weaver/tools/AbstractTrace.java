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
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.aspectj.bridge.IMessage.Kind;

public abstract class AbstractTrace implements Trace {

	protected Class tracedClass;

	private static SimpleDateFormat timeFormat;
	
	protected AbstractTrace (Class clazz) {
		this.tracedClass = clazz;
	}
	
	public abstract void enter (String methodName, Object thiz, Object[] args);
	
	public abstract void enter(String methodName, Object thiz);

	public abstract void exit(String methodName, Object ret);

	public abstract void exit(String methodName, Throwable th);

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

	public void exit (String methodName, int i) {
		exit(methodName,new Integer(i));
	}

	public void event (String methodName, Object thiz, Object arg) {
		event(methodName,thiz,new Object[] { arg });
	}

	public void warn(String message) {
		warn(message,null);
	}

	public void error(String message) {
		error(message,null);
	}

	public void fatal (String message) {
		fatal(message,null);
	}
	
	/*
	 * Formatting
	 */
	protected String formatMessage(String kind, String className, String methodName, Object thiz, Object[] args) {
		StringBuffer message = new StringBuffer();
		Date now = new Date();
		message.append(formatDate(now)).append(" ");
		message.append(Thread.currentThread().getName()).append(" ");
		message.append(kind).append(" ");
		message.append(className);
		message.append(".").append(methodName);
		if (thiz != null) message.append(" ").append(formatObj(thiz));
		if (args != null) message.append(" ").append(formatArgs(args));
		return message.toString();
	}
	
	protected String formatMessage(String kind, String text, Throwable th) {
		StringBuffer message = new StringBuffer();
		Date now = new Date();
		message.append(formatDate(now)).append(" ");
		message.append(Thread.currentThread().getName()).append(" ");
		message.append(kind).append(" ");
		message.append(text);
		if (th != null) message.append(" ").append(formatObj(th));
		return message.toString();
	}
	
	private static String formatDate (Date date) {
		if (timeFormat == null) {
			timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		}
		
		return timeFormat.format(date);
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
			    || obj instanceof URL
			    || obj instanceof Kind
		    ) return obj;
		else if (obj.getClass().isArray()) {
			return formatArray(obj);
		}
		else if (obj instanceof Collection) {
			return formatCollection((Collection)obj);
		}
		else try {
			
			/* Classes can provide an alternative implementation of toString() */
			if (obj instanceof Traceable) {
				Traceable t = (Traceable)obj;
				return t.toTraceString();
			}
			
			/* Use classname@hashcode */
			else return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
		
		/* Object.hashCode() can be override and may thow an exception */	
		} catch (Exception ex) {
			return obj.getClass().getName() + "@FFFFFFFF";
		}
	}
	
	protected String formatArray (Object obj) {
		return obj.getClass().getComponentType().getName() + "[" + Array.getLength(obj) + "]"; 
	}
	
	protected String formatCollection (Collection c) {
		return c.getClass().getName() + "(" + c.size() + ")"; 
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
	
	protected Object[] formatObjects(Object[] args) {
		for (int i = 0; i < args.length; i++) {
			args[i] = formatObj(args[i]);
		}
		
		return args;
	}
}