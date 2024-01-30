package ca.ubc.cs.spl.aspectPatterns.examples.singleton.aspectj;

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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.SingletonProtocol;

/**
 * Implements a concrete instance of the <i>Singleton</i> pattern. It declares
 * Printer to be Singleton and defines an exception to the constructor
 * protection: PrinterSubclass (a subclass of the Singleton) can still
 * access Printer's constructor.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/18/04
 * 
 * @see Printer
 * @see PrinterSubclass
 */


public aspect SingletonInstance extends SingletonProtocol { 
    
    /**
     * Assigns the Singleton to <code>Printer</code>. This is all that is 
     * necessary to provide <code>Printer</i>'s constructor with the
     * Singleton protection.
     */
  
	declare parents: Printer implements Singleton;		                
	
    /**
     * This declaration allows <code>PrinterSubclass</code> (and all its
     * subclasses) to access <code>Printer</code>'s constructor within
     * its constructor (to allow for <code>super(..)</code> calls).
     */
 
 	protected pointcut protectionExclusions(): 
 		call((PrinterSubclass+).new(..));  
}