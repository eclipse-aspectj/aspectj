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

public aspect RequestCounting extends ProxyProtocol {  
	
	/**
	 * An internal counter for the number of calls to <code>
	 * print(String)</code>.
	 */
	 
	private int regularRequests = 0;

    
    /** 
     * Assigns the <i>OutputSubject</i> role to <code>OutputImplementation</code>.
     */
    
    declare parents: OutputImplementation implements Subject;

    /**
     * Captures all accesses to the subject that should be protected by 
     * this pattern instance. Here: All calls to <code>
     * OutputImplementation.print(..)</code>.
     */

	protected pointcut requests(): 
		call(* OutputImplementation.regularRequest(..));

	/**
	 * Checks whether the access should be denied or not. Here: All accesses
	 * that come from <code>Main</code> objects are denied.
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
			regularRequests++;
			System.out.println("[RequestCounter:] That was regular request nr. " +
				regularRequests);
		} 
		return false;		
	}
}
