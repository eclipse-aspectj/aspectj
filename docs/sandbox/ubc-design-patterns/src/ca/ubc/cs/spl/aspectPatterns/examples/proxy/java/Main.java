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
 * Implements the driver for the RequestCounter design pattern example.<p> 
 *
 * Intent: <i>Provide a surrogate or placeholder for another object to control
 * access to it.</i><p>
 *
 * Participating objects are <code>OutputImplementation</code> and 
 * <code>RequestCounter</code> as <i>RealSubject</i> and <i>Proxy</i>, 
 * respectively.
 *  
 * Both implement the <code>OutputSubject</code> interface, which represents
 * the <i>Subject</i> interface.
 *
 * Experimental setup:
 * <code>Main</code> issues three different kinds of requests to
 * the <i>RealSubject</i> (<code>OutputImplementation</code>) twice. 
 * <UL>
 * 	<LI> SAFE requests are not affected
 *  <LI> REGULAR request are counted
 *  <LI> UNSAFE requests are blocked entirely.
 * </UL>
 *
 * <p><i>This is the Java version.</i><p> 
 *
 * <i>Proxy</i>s needs to implement all methods of 
 * <code>OutputSubject</code>, even those it is not interested in. 
 * They need to be aware of their role in the pattern.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class Main {   

    /**
     * Implements the driver for the proxy design pattern. <p>
     */

	public static void main (String[] args) { 
		OutputSubject real          = new OutputImplementation();
		OutputSubject countingProxy = new RequestCounter(real);
		OutputSubject blockingProxy = new RequestBlocker(countingProxy);
		
		System.out.println("\n===> Issuing SAFE request...");		
		blockingProxy.safeRequest   ("Safe Reqeust");
		System.out.println("\n===> Issuing REGULAR request...");		
		blockingProxy.regularRequest("Normal Request");
		System.out.println("\n===> Issuing UNSAFE request...");		
		blockingProxy.unsafeRequest ("Unsafe Request");

		System.out.println("\n===> Issuing SAFE request...");		
		blockingProxy.safeRequest   ("Safe Reqeust");
		System.out.println("\n===> Issuing REGULAR request...");		
		blockingProxy.regularRequest("Normal Request");
		System.out.println("\n===> Issuing UNSAFE request...");		
		blockingProxy.unsafeRequest ("Unsafe Request");
	}
}
