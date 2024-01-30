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

package ca.ubc.cs.spl.aspectPatterns.examples.facade.aspectj;

/**
 * Enforces the encapsulation of the Facade by declaring a compile-time
 * warning if the <i>subsystem</i> is accessed from any other class but the
 * facade. We use <code>declare warning</code> here, but <code>declare error
 * </code> can also be used.
 *  
 * Instead of protecting the encapsulated subsystem against (only) mehod 
 * calls, other pointcut types (or combinations) could be used to achieve 
 * different effects.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.11, 03/29/04
 */

public aspect FacadePolicyEnforcement {
	
	/**
	 * Enumerates all calls to encapsulated methods. It is of course easier 
	 * to define this pointcut if the encapsulated subsystem is in a separate
	 * package, and the protected classes do not have to be enumerated.
	 */
	
	pointcut callsToEncapsulatedMethods(): 
		call(* (Decoration || RegularScreen || StringTransformer).*(..));
	
	/**
	 * Defines what constitutes legal accesses to the protected subsystem.
	 */
	
	pointcut facade(): within(OutputFacade);
	
	/**
	 * Whenever a method in the encapsulated susbsystem is called, a compile
	 * time warning gets created - except if the method call comes from the
	 * <i>Facade</i> 
	 */

	declare warning: callsToEncapsulatedMethods() && !facade():
		"Calling encapsulated method directly - use Facade methods instead";
}
