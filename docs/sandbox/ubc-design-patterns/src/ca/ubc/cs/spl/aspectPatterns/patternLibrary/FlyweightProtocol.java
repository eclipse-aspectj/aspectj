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
 * Implements a the abstracted Flyweight design pattern. Included is the 
 * general creation-on-demand logic. Concrete subaspects are used to
 * defines the actual <i>FlyweightFactories</i>.
 *
 * Concrete subaspects need only to assign the flyweight roles and to
 * overwrite the <code>createFlyweight(Object)<code> method.
 * 
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */

public abstract aspect FlyweightProtocol {
    
    /** 
     * stores the existing <i>Flyweight</i> by key
     */

	private Hashtable flyweights = new Hashtable();

    /** 
     * defines the <i>Flyweight</i> role.
     */

	protected interface Flyweight{};
	
    /**
     * Creates a <i>Flyweight</i> for a given key. This method is called by
     * <code>getFlyweight(Object)</code> if the flyweight does not already
     * exist.
     *
     * @param key the key identifying the particular flyweight
     * @return the <i>Flyweight</i> representing the key
     */

	protected abstract Flyweight createFlyweight(Object key);
	
    /**
     * Returns the <i>Flyweight</i> for a particular key.
     * If the appropriate <i>Flyweight</i> does not yet exist, it is created 
     * on demand.
     *
     * @param key the key identifying the particular <i>Flyweight</i>
     * @return the <i>Flyweight</i> representing the key
     */

	public Flyweight getFlyweight(Object key) {
		if (flyweights.containsKey(key)) {
			return (Flyweight) flyweights.get(key);
		} else {
			Flyweight flyweight = createFlyweight(key);
			flyweights.put(key, flyweight);  
			return flyweight;
		}
	}
}