package ca.ubc.cs.spl.aspectPatterns.examples.observer.aspectj;

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
 * Provides a means to output messages. Objects of this class act as
 * output devices. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.11, 04/01/04
 */
  
public class Screen {
    
    /**
     * the individual name of this screen object
     */

    private String name;
    
    /**
     * creates a new <code>Screen</code> object with the provided name.
     *
     * @param name the name for the new <code>Screen</code> object 
     */
    
    public Screen(String s) {
        this.name = s;
    }


    /**
     * Prints the name of the <code>Screen</code> object and the argument 
     * string to stdout.
     *
     * @param s the string to print
     */
     
    public void display (String s) {
	    System.out.println(name + ": " + s);
    }
}
