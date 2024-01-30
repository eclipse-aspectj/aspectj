package ca.ubc.cs.spl.aspectPatterns.patternLibrary;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 * 
 * For more details and the latest version of this code, please see:
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */

import org.aspectj.lang.JoinPoint; 

/**
 * Defines the abstracted Proxy design pattern.<p>  
 *
 * Concrete sub-aspects define the following: <UL>
 *  <LI> Which class(es) are <i>RealSubject</i>s
 *  <LI> Which requests need to be handled by the <i>Proxy</i>
 *       (methods, field accesses)
 *  <LI> What to return in case of a proxy-covered
 *       access to a method.
 * </UL>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */    
 
public abstract aspect ProxyProtocol { 
    
    /**
     * Defines the Subject role (used for correct typing)
     */
    
    protected interface Subject {}  
    
    /**
     * Captures all accesses to the subject that should be covered by 
     * this pattern instance.
     */
                                        
	protected abstract pointcut requests();     
	
	/** 
	 * Extends the <code>requests()</code> pointcut to include a 
	 * field for the joinpoint. Used internally only.
	 */
	
	private pointcut requestsByCaller(Object caller): 
	    requests() && this(caller);

    /**
     * Intercepts accesses to protected parts of the OutputSubject. 
     * If access is proxy protected, the method 
     * <code>handleProxyProtection(..)</code> is called instead.
     *
     * @param caller the object responsible for the protected access
     * @param subject the subject receiving the call
     */

	Object around(Object caller, Subject subject): 
	    requestsByCaller(caller) && target(subject) { 
	        
		if (! isProxyProtected(caller, subject, thisJoinPoint) )
			return proceed(caller, subject); 
		return handleProxyProtection(caller, subject, thisJoinPoint);
	} 
	
	/**
	 * Checks whether the request should be handled by the Proxy or not
	 *
     * @param caller the object responsible for the protected access
     * @param subject the subject receiving the call  
     * @param joinPoint the joinpoint associated with the protected access
     *
     * @return true if the access is covered by the proxy, false otherwise
     */
	
	protected abstract boolean isProxyProtected(Object caller, 
	                                     Subject subject, 
	                                     JoinPoint joinPoint);

	/**
	 * For delegation: Provides an alternative return value if access 
	 * is proxy protected. A default implementation is supplied so that 
	 * concrete subaspects are not forced to implement the method.
	 *
     * @param caller the object responsible for the proxy protected access
     * @param subject the subject receiving the call  
     * @param joinPoint the joinpoint associated with the proxy protected 
     * access
     *
     * @return an alternative return value
     */

	protected Object handleProxyProtection(Object caller, 
	                                       Subject subject, 
	                                       JoinPoint joinPoint) {
		return null;                                       	
	}
} 
