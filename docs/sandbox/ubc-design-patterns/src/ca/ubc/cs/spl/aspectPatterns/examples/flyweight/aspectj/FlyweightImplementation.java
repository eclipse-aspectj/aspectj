package ca.ubc.cs.spl.aspectPatterns.examples.flyweight.aspectj;  

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
 
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.FlyweightProtocol;

/**
 * Implements a concrete instance of the flyweight pattern using the 
 * abstract implementation found in <code>FlyweightProtocol</code>.
 * 
 * It assigns the <i>Flyweight</i> pattern role to the participants and 
 * provides the implementation for creating <i>Flyweight</i>s.
 * 
 * A concenience method is provided to utilize the abstract aspect's 
 * <i>getFlyweight(...)</i> implementation with an appropriate return type.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */


public aspect FlyweightImplementation extends FlyweightProtocol {
	
    /** 
     * Assigns the <i>Flyweight</i> role to CharacterFlyweight.
     */

	declare parents: CharacterFlyweight	 implements Flyweight;

    /** 
     * Assigns the <i>Flyweight</i> role to WhitespaceFlyweight.
     */

	declare parents: WhitespaceFlyweight implements Flyweight;

    /**
     * Actually creates the <i>Flyweight</i> for a given <i>Key</i>. This 
     * method is called by <code>getFlyweight(Object)</code> if the 
     * flyweight does not already exist.
     *
     * @param key the key identifying the particular <i>Flyweight</i>
     * @return the <i>Flyweight</i> representing the key
     */

	protected Flyweight createFlyweight(Object key) {
		char c = ((Character) key).charValue(); 
		Flyweight flyweight = null;
		if (Character.isWhitespace(c)) {
			flyweight = new WhitespaceFlyweight(c);
		} else { 
			flyweight = new CharacterFlyweight(c);
		}
		return flyweight; 
	}  
	
    /**
     * Provides a custom interface to access the <i>Flyweights</i>. 
     * Refers to the general <code>getFlyweight(Object)</code> method 
     * defined on the abstract aspect.
     *
     * @param c the character identifying the particular flyweight
     * @return the flyweight representing the character
     */

	public PrintableFlyweight getPrintableFlyweight(char c) {
		return (PrintableFlyweight) getFlyweight(new Character(c));
	}
}