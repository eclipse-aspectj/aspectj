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

import java.util.Hashtable;

/**
 * Defines the general behavior of the Singleton design pattern.
 *
 * Each concrete sub-aspect of SingletonProtocol defines the Singleton
 * property for one or more types.
 *
 * The sub-aspect defines two things: <ol>
 *
 *   <li> what types are <i>Singleton</i> <br>
 *
 *   <li> what classes can access the <i>Singleton</i>'s constructor (if any)
 *        despite its property
 * </ol>
 *
 * for this implementation we choose to illustrate that it is not necessary
 * to provide a factory method for accessing the <i>Singleton</i>
 * instance (like <i>getSingleton()</i>). The regular
 * constructor may be used instead.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/18/04
 */
  
public abstract aspect SingletonProtocol {
	
	/**
	 * stores the <i>Singleton</i> instances
	 */
	
    private Hashtable singletons = new Hashtable(); 
    
    /**
     * Defines the <i>Singleton</i> role. It is realized as <code>public
     * </code> to allow for more flexibility (i.e., alternatively, types
     * can just themselves declare that they implement the interface to
     * aquire the <i>Singleton</i> property.
     */

    public interface Singleton {}
    
    /**
     * Placeholder for exceptions to the <i>Singleton</i>'s constructor
     * protection. For example, non-singleton subclasses may need to 
     * access the protected constructor of the <i>Singleton</i> normally.
     * 
     * An alternative implementation would be to define an interface
     * for singleton exceptions similar to the one above.
     */
    
    protected pointcut protectionExclusions();

	/**
	 * Protects the <i>Singleton</i>'s constructor. Creates the unique
	 * instance on demant and returns it instead of a new object.
	 * 
	 * @return the singleton instance 
	 */
                                                                                                                            
	Object around(): call((Singleton+).new(..)) && !protectionExclusions() {                    
	    Class singleton = thisJoinPoint.getSignature().getDeclaringType(); 
		if (singletons.get(singleton) == null) {                         // How to access the static instance variable here?
		    singletons.put(singleton, proceed()); 
		}
		return singletons.get(singleton);
	} 
}