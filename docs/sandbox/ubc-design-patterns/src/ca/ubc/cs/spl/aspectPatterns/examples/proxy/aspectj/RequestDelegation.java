package ca.ubc.cs.spl.aspectPatterns.examples.proxy.aspectj; 

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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.ProxyProtocol;
import org.aspectj.lang.JoinPoint;

/**
 * Implements a concrete proxy pattern instance. Here, all method calls from 
 * <code>Main</code> to <code>OutputImplementation.print(String)</code> are blocked.<p> 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */  
 
public aspect RequestDelegation extends ProxyProtocol { 
    
    /** 
     * Assigns the <i>Subject</i> role to <code>OutputImplementation
     * </code>.
     */
    
    declare parents: OutputImplementation implements Subject;
    
    private AlternateOutputImplementation alternateSubject = 
    	new AlternateOutputImplementation();

    /**
     * Captures all accesses to the subject that should be protected by 
     * this pattern instance. Here: All calls to <code>
     * OutputImplementation.safeRequest(..)</code>.
     */

	protected pointcut requests(): 
		call(* OutputImplementation.safeRequest(..));


	/**
	 * Checks whether the request should be handled by the Proxy or not.
	 * Here: All accesses matched by the <code>protectedAccesses()</code> 
	 * joinpoint.
	 *
	 * @param caller the object responsible for the protected access
	 * @param subject the subject receiving the call  
	 * @param joinPoint the joinpoint associated with the protected access
	 *
	 * @return true if the access is covered by the proxy, false otherwise
	 */

	protected boolean isProxyProtected(Object caller, 
	                            Subject subject, 
	                            JoinPoint joinPoint) {
	    System.out.println("[RequestDelegation] delegating a safe request " +
	        "to a different type of object"); 
		return true;
	}

	/**
	 * For delegation: Provides an alternative return value if access 
	 * is proxy protected. A default implementation is supplied so that 
	 * concrete subaspects are not forced to implement the method.
	 * Here, it also calls an appropriate method on a delegate 
	 * (to illustrate how delegation would work).
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
	    Object[] args = joinPoint.getArgs();
	    if (args != null) {
	        alternateSubject.alternateRequest((String)args[0]);
	    } else {
	        alternateSubject.alternateRequest("");
	    }
	    return null;
	}
}
