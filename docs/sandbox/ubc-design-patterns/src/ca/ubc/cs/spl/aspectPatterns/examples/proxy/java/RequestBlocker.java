package ca.ubc.cs.spl.aspectPatterns.examples.proxy.java;

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

/**
 * Acts as <i>Proxy</i> according to GoF. It implements the
 * <code>OutputSubject</code> interface.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */  

public class RequestBlocker implements OutputSubject { 
    
    /**
     * a reference to the <i>Subject</i> (used to forward requests to)
     */
     
	private OutputSubject realSubject; 
		
    /**
     * Creates a new <code>RequestBlocker</code> with the given 
     * <i>Subject</i>.
     *
     * @param subject The <i>Subject</i> to forward method calls to
     */

	public RequestBlocker(OutputSubject subject) {
		this.realSubject = subject;
	}
	
    /**
	 * Forwards the request to its subject. We are not interested in
	 * this kind of request, but must implement the method (and the 
	 * request forwarding) anyway since the method is part of the 
	 * <code>RequestBlocker</code> interface.
     *
     * @param s the string to print
     */
     
	public void safeRequest(String s) {
		realSubject.safeRequest(s);                                                
		System.out.println("[RequestBlocker:] Not interested in safe requests," +
			" but must implement anyway");		
	}
	
	/**
	 * Forwards the request to its subject. We are not interested in
	 * this kind of request, but must implement the method (and the 
	 * request forwarding) anyway since the method is part of the 
	 * <code>RequestBlocker</code> interface.
	 *
	 * @param s the string to print
	 */

	public void regularRequest(String s) {
		realSubject.regularRequest(s);                                                
		System.out.println("[RequestBlocker:] Not interested in regular requests," +
			" but must implement anyway");		
	}

	/**
	 * Blocks unsafe requests.
	 *
	 * @param s the string to print
	 */

	public void unsafeRequest(String s) {
		realSubject.unsafeRequest(s);                                                
		System.out.println("[RequestBlocker:] " + s + " blocked.");		
	}
	
}