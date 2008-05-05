/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *     Sian January
 *******************************************************************************/
package org.aspectj.lib.tracing;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

/**
 * This simple abstract aspect is enabled by default and traces method 
 * signatures as well as arguments to stderr. An abstract scoping pointcut 
 * is provided for concrete, user-supplied sub-aspect to determine which 
 * classes should be traced.
 */
public abstract aspect SimpleTracing extends Tracing {

	/**
	 * Sub-aspects <b>must</b> implement this pointcut to determine what to trace
	 */
	protected abstract pointcut tracingScope ();

	private static SimpleDateFormat timeFormat;
	
	/**
	 * Enabled or disable tracing
	 * 
	 * @param enabled 
	 */
	public static void setEnabled (boolean enabled) {
		tracingEnabled = enabled;
	}
	
	public static boolean getEnabled () {
		return tracingEnabled;
	}

	/*
	 * Tracing pattern 2: Use if() pointcut to efficiently determine when to trace
	 *
	 * Tracing pattern 3: Use -XlazyTjp compiler option
	 */
	protected pointcut shouldTrace () :
		if(tracingEnabled) && tracingScope();
		
	private static boolean tracingEnabled = getBoolean("org.aspectj.lib.tracing",true);

	private static boolean getBoolean (String name, boolean def) {
		String defaultValue = String.valueOf(def);
		String value = System.getProperty(name,defaultValue);
		return Boolean.valueOf(value).booleanValue();
	}

	/*
	 * Tracing template methods
	 */
	protected void enter (JoinPoint jp, Object obj) {
		CodeSignature signature = (CodeSignature)jp.getSignature();
		println(signature.getDeclaringType(),formatMessage(">",signature.getDeclaringTypeName(),signature.getName(),obj,jp.getArgs()));
//		println("> " + signature.toShortString() + " " + formatParam("obj",obj) + " " + formatArgs(signature.getParameterNames(),jp.getArgs()));
	}
	
	protected void enter (JoinPoint jp) {
		CodeSignature signature = (CodeSignature)jp.getSignature();
		println(signature.getDeclaringType(),formatMessage(">",signature.getDeclaringTypeName(),signature.getName(),null,jp.getArgs()));
//		println("> " + jp.getSignature().toShortString() + " " + formatArgs(signature.getParameterNames(),jp.getArgs()));
	}
	
	protected void exit (JoinPoint.StaticPart sjp, Object ret) {
		CodeSignature signature = (CodeSignature)sjp.getSignature();
		println(signature.getDeclaringType(),formatMessage("<",signature.getDeclaringTypeName(),signature.getName(),ret,null));
//		println("< " + sjp.getSignature().toShortString() + " " + formatParam("ret",ret));
	}
	
	protected void exit (JoinPoint.StaticPart sjp) {
		CodeSignature signature = (CodeSignature)sjp.getSignature();
		println(signature.getDeclaringType(),formatMessage("<",signature.getDeclaringTypeName(),signature.getName(),null,null));
//		println("< " + sjp.getSignature().toShortString());
	}
	
	protected void exception (JoinPoint.StaticPart sjp, Throwable th) {
		CodeSignature signature = (CodeSignature)sjp.getSignature();
		println(signature.getDeclaringType(),formatMessage("E",signature.getName(),th));
//		println("E " + sjp.getSignature().toShortString() + " " + th.toString());
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
		    ) return obj;
		else if (obj.getClass().isArray()) {
			return formatArray(obj);
		}
		else if (obj instanceof Collection) {
			return formatCollection((Collection)obj);
		}
		else try {
			
			/* Use classname@hashcode */
			return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
		
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
	
	private static String formatDate (Date date) {
		if (timeFormat == null) {
			timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		}
		
		return timeFormat.format(date);
	}

	/**
	 * Template method that allows choice of destination for output
	 * 
	 * @param s message to be traced
	 */
	protected void println (Class clazz, String s) {
		System.err.println(s);
	}

}

