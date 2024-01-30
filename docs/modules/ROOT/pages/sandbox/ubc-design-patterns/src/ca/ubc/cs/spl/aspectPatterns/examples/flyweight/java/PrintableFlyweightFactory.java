package ca.ubc.cs.spl.aspectPatterns.examples.flyweight.java;

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
 * Implements a <i>FlyweightFactory</i> that employs a creation-on-demand 
 * policy.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see PrintableFlyweight
 */

public class PrintableFlyweightFactory { 
    
    /** 
     * stores the existing flyweights by character they represent
     */

	private Hashtable printables = new Hashtable();

    /**
     * Returns the flyweight representing the argument character.
     * If the appropriate flyweight does not yet exist, it is created 
     * on demand.
     *
     * @param c the character for which the the flyweight is returned
     * @return the <i>Flyweight</i> representing the argument character
     */

	public PrintableFlyweight getPrintableFlyweight(char c) {

		Character ch = new Character(c); 
		
		if (printables.containsKey(ch)) {
			return (PrintableFlyweight) printables.get(ch); 
		} else {
			PrintableFlyweight flyweight = null;
			if (Character.isWhitespace(c)) {
				flyweight = new WhitespaceFlyweight(c);
			} else { 
				flyweight = new CharacterFlyweight(c);
			}
			printables.put(ch, flyweight);
			return flyweight; 
		} 
	}
}			