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

import org.aspectj.lang.*;

/**
 * This root abstract aspect determines the basic tracing behaviour 
 * i.e. entry/exit/exception using the method/constructor execution() pointcut 
 * and before/after returning/after throwing advice. Determining what 
 * methods and constructors belonging to which classes is delegated to a 
 * user-supplied concrete aspect using an abstract pointcut. When tracing 
 * occurs and what is done with the captured data is delegated to an abstract, 
 * infrastructure-specific sub-aspect through template methods.
 */
public abstract aspect Tracing {

	/**
	 * Sub-aspects <b>must</b> implement this pointcut to determine what and when to 
	 * trace
	 */
	protected abstract pointcut shouldTrace ();

	private pointcut staticContext () : !this(Object);
    private pointcut nonStaticContext (Object obj) : this(obj);
    private pointcut voidMethod () : execution(void *(..));
    
	public final static pointcut methodExecution () : execution(* *(..));
	public final static pointcut constructorExecution () : execution(new(..));
	public final static pointcut objectMethod () : execution(* Object.*(..));

	/** 
	 * Sub-aspects <b>may</b> override this point to determine which methods if any 
	 * are traced. By default include only public methods and those not inherited 
	 * from java.lang.Object e.g. toString().
	 */
	protected pointcut includedMethod () :
		execution(public * *(..))
		&& !objectMethod();


	/** 
	 * Sub-aspects <b>may</b> override this point to determine which constructors if any 
	 * are traced. By default include only public constructors.
	 */
	protected pointcut includedConstructor () :
		execution(public new(..));
	
	/*
	 * Exclude methods and constructors in Tracing and sub-aspects as well as 
	 * those in the control flow of Tracing advice or constructors to avoid recursion.
	 */
	private pointcut excluded () : 
		within(Tracing+)
//		|| cflow((adviceexecution() || execution(new(..))) && within(Tracing+))
		|| cflow((adviceexecution() && within(Tracing+)))
		 ;

	/*
	 * Trace only method execution included by the user but excluded by the aspect e.g. itself
	 */
	private pointcut tracedMethod () :
		methodExecution()
		&& includedMethod()
		&& !excluded()
		;

	/*
	 * Trace only constructor execution included by the user but excluded by the aspect e.g. itself
	 */
	private pointcut tracedConstructor (Object obj) :
		constructorExecution()
		&& includedConstructor()
		&& !excluded()
		&& this(obj)
		;
	
	/*
	 * Trace entry to instance methods
	 * 
	 * Tracing pattern 1: Only use thisJoinPoint in before()
	 */
	before (Object obj) : tracedMethod() && nonStaticContext(obj) && shouldTrace() {
		enter(thisJoinPoint,obj);
	}
	
	/*
	 * Trace entry to static methods
	 * 
	 * Tracing pattern 1: Only use thisJoinPoint in before()
	 */
	before () : tracedMethod() && staticContext() && shouldTrace() {
		enter(thisJoinPoint);
	}
	
	/*
	 * Trace exit from void methods
	 * 
	 * Tracing pattern 1: Use thisJoinPointStaticPart in after()
	 */
	after() returning() : tracedMethod() && voidMethod() && shouldTrace() {
		exit(thisJoinPointStaticPart);
	}
	
	/*
	 * Trace exit from non-void methods including return value
	 * 
	 * Tracing pattern 1: Use thisJoinPointStaticPart in after()
	 */
	after() returning(Object ret) : tracedMethod() && !voidMethod() && shouldTrace() {
		exit(thisJoinPointStaticPart,ret);
	}
	
	/*
	 * Trace exceptions thrown from methods and constructors
	 * 
	 * Tracing pattern 1: Use thisJoinPointStaticPart in after()
	 */
	after() throwing(Throwable th) : (tracedMethod() || tracedConstructor(Object)) && shouldTrace() {
		if (shouldTrace(th)) exception(thisJoinPointStaticPart,th);
	}
	
	/*
	 * Trace entry to constructors
	 * 
	 * Tracing pattern 1: Only use thisJoinPoint in before()
	 */
	before () : tracedConstructor(Object) && shouldTrace() {
		enter(thisJoinPoint);
	}
	
	/*
	 * Trace exit from constructors including new object
	 * 
	 * Tracing pattern 1: Only use thisJoinPoint in before()
	 */
	after (Object obj) : tracedConstructor(obj) && shouldTrace() {
		exit(thisJoinPointStaticPart,obj);
	}
	
	/*
	 * Template methods to log data implemented by infrastructure-specific sub-aspects
	 * e.g. java.util.logging.Logger 
	 */
	protected abstract void enter (JoinPoint jp, Object obj);
	
	protected abstract void enter (JoinPoint jp);
	
	protected abstract void exit (JoinPoint.StaticPart sjp);
	
	protected abstract void exit (JoinPoint.StaticPart sjp, Object ret);
	
	protected abstract void exception (JoinPoint.StaticPart sjp, Throwable th);

	/** 
	 * Format arguments into a comma separated list
	 * 
	 * @param names array of argument names
	 * @param args array of arguments
	 * @return the formated list
	 */
	protected String formatArgs (String[] names, Object[] args) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < args.length; i++) {
			sb.append(formatParam(names[i],args[i]));
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
	protected Object formatObj (Object obj) {
		if (obj == null
				|| obj instanceof String
			    || obj instanceof Number
			    || obj instanceof Boolean
			    || obj instanceof Character
			    || obj instanceof Class
			    || obj instanceof StringBuffer
		    ) return obj;
		else try {
			return obj.getClass().getName() + "@" + Integer.toString(obj.hashCode(),16);
		} catch (Exception ex) {
			return obj.getClass().getName();
		}
	}
	
	/**
	 * Format parameter into name=value pair
	 * 
	 * @param name parameter name
	 * @param arg parameted to be formatted
	 * @return the formated parameter
	 */
	protected String formatParam (String name, Object arg) {
		return name + "=" + formatObj(arg);
	}
	
	/**
	 * By default we do not trace errors e.g. OutOfMemoryError because the 
	 * system my be in an inconsistent state. However users may override this
	 * 
	 * @param th excpeption or error to be traced
	 * @return whether it should be traced
	 */
	protected boolean shouldTrace (Throwable th) {
		return !(th instanceof Error);
	}
}
