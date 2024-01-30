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

/**
 * Implements the driver for the Proxy design pattern example.<p> 
 *
 * Intent: <i>Provide a surrogate or placeholder for another object to control
 * access to it.</i><p>
 *
 * Participating objects are <code>OutputImplementation</code>s and 
 * <code>AlternateOutputImplementation</code>. 
 * 
 * The former acts as a <i>RealSubject</i>.
 *
 * Experimental setup:
 * <code>Main</code> issues three different kinds of requests to
 * the <i>RealSubject</i> (<code>OutputImplementation</code>) twice. 
 * <UL>
 * 	<LI> SAFE requests are delegated to a different object 
 *       (delegation proxy)
 *  <LI> REGULAR request are counted
 *  <LI> UNSAFE requests are blocked entirely.
 * </UL>
 *
 * <p><i>This is the AspectJ version.</i><p> 
 * 
 * Each concrete aspect defines exactly what requests it is interested
 * in. For those requests, it declares how to deal with the request.
 * 
 * The proxy implementation is localized. Even clients need not set
 * or remove proxies.  
 * 
 * Please note that the AspectJ version includes an additional proxy
 * that delegates safe request to a different object. An OO implementation
 * would be similar to the other OO proxies.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class Main {
    
    /** 
     * Creates a new Main object and runs the test suite.
     */  
    
	public Main() { 

	    /**
	     * The <i>RealSubject</i> that the client sends all requests to
	     */
	     
		OutputImplementation real = new OutputImplementation(); 

		System.out.println("\n===> Issuing SAFE request...");		
		real.safeRequest   ("Safe Reqeust");
		System.out.println("\n===> Issuing REGULAR request...");		
		real.regularRequest("Normal Request");
		System.out.println("\n===> Issuing UNSAFE request...");		
		real.unsafeRequest ("Unsafe Request");

		System.out.println("\n===> Issuing SAFE request...");		
		real.safeRequest   ("Safe Reqeust");
		System.out.println("\n===> Issuing REGULAR request...");		
		real.regularRequest("Normal Request");
		System.out.println("\n===> Issuing UNSAFE request...");		
		real.unsafeRequest ("Unsafe Request");
	}	    

    /** 
     * Implements the driver for the proxy design pattern example.
	 */

	public static void main (String[] args) { 
        Main main = new Main();
	}
}
