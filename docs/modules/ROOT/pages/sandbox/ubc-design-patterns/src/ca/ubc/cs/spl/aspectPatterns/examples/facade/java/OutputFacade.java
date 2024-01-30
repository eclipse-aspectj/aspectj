package ca.ubc.cs.spl.aspectPatterns.examples.facade.java;

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
 * Implements the <i>Facade</i> role in the pattern by providing a 
 * higher-level interface to the operations provided by 
 * <code>RegularScreen</code>, <code>Decoration</code>, 
 * and <code>StringTransformer</code>.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */
 
public class OutputFacade {

    /**
     * Prints a string using <code>RegularScreen</code>.
     *
     * @param s the string to print 
     */

    public void printNormal(String s) {
        RegularScreen.print(s);
        RegularScreen.newline();
    }
                        
    /**
     * Prints a two versions of string with decorations 
     * using <code>RegularScreen</code> and <code>Decoration</code>.
     *
     * @param s the string to print 
     */

    public void printFancy(String s) {
		printDecoration();

        RegularScreen.print(StringTransformer.transformToUpper(s+" (uppercase)"));
        RegularScreen.newline();

		printDecoration();

        RegularScreen.print(StringTransformer.transformToLower(s+" (lowercase)"));
        RegularScreen.newline();
        
		printDecoration();
    }

	/**
	 * Prints a decorator string.
	 */

	public void printDecoration() {
		RegularScreen.print(Decoration.getDecoration());
		RegularScreen.newline();
		RegularScreen.newline();
	}
}