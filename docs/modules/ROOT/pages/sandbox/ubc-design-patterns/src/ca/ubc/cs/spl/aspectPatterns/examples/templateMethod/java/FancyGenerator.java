package ca.ubc.cs.spl.aspectPatterns.examples.templateMethod.java;

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
 * Implements a <i>ConcreteClass</i> that decorates strings by turnung all
 * characters into lowercase and then capitalizing the consonants. It also
 * adds an explanation at the end.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class FancyGenerator extends DecoratedStringGenerator {
    
    /**
     * Prepares a string for decoration. Turns the string into lowercase.
     *
     * @param s the string to filter
     * @return the prepared string
     */
    
	public String prepare  (String s) {
		return s.toLowerCase();
	}
	
    /**
     * Filters a string. Capitalizes all consonants.
     *
     * @param s the string to filter
     * @return the filtered string
     */

	public String filter   (String s) {
		s = s.replace('a', 'A'); 
		s = s.replace('e', 'E');
		s = s.replace('i', 'I'); 
		s = s.replace('o', 'O');
		s = s.replace('u', 'U');
		return s; 
	}
		
    /**
     * Finalizes a string by adding an explanation to it.
     *
     * @param s the string to finalize
     * @return the finalized string
     */
		
	public String finalize (String s) {
		return (s+".\n(all consonants capitalized)");
	}
}