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
 * Implements a concrete Proxy pattern instance. Here, all unsafe requests 
 * from <code>Main</code> <code>OutputImplementation</code> 
 * are blocked.<p> 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */    

public aspect RequestBlocking extends ProxyProtocol {  
    
    /** 
     * Assigns the <i>Subject</i> role to <code>OutputImplementation</code>.
     */
    
    declare parents: OutputImplementation implements Subject;

    /**
     * Captures all accesses to the <i>Subject</i> that should be protected by
     * this proxy. Here: All calls to <code>
     * OutputImplementation.unsafeRequest(..)</code>.
     */

	protected pointcut requests(): 
		call(* OutputImplementation.unsafeRequest(..));

	/**
	 * Checks whether the access needs to be handled by the proxy or not.
	 * Here: All accesses that come from <code>Main</code> objects are
	 * denied.
	 *
     * @param caller the object responsible for the protected access
     * @param subject the subject receiving the call  
     * @param joinPoint the joinpoint associated with the protected access
     *
     * @return true if the access is from a Main object, false otherwise
     */

	protected boolean isProxyProtected(Object caller, 
	                            Subject subject, 
	                            JoinPoint joinPoint) { 
		if (joinPoint.getThis() instanceof Main) {
			System.out.println("[RequestBlocking] intercepting unsafe " +
			"requests from Main");
			return true;
		} else {
			return false;
		}
	}
}
