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
 
/**
/**
 * A <i>ConcreteFlyweight</i> storing a single regular character
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */

public class CharacterFlyweight implements PrintableFlyweight {

    /**
     * The character this <i>ConcreteFlyweight</i> represents
     */
     
	private char c;
	

    /** 
     * Creates a new <i>ConcreteFlyweight</i> and sets it to represent a 
     * particular regular character
     * 
     * @param c the character to represent
     */
     
	public CharacterFlyweight(char c) {
		this.c = c;
	}
	
    /** 
     * Prints the stored character
     *
     * @param uppercase whether the character should be printed in
     * uppercase
     */ 
     
	public void print(boolean uppercase) {
		System.out.print(uppercase ? Character.toUpperCase(c) : c);
	}
}