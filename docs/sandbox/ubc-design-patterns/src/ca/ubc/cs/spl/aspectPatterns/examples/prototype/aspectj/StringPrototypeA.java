package ca.ubc.cs.spl.aspectPatterns.examples.prototype.aspectj;

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
 * Implements a sample <i>Prototype</i> that imitates a simple String
 * class. The clone method is now implemented by the aspect.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/13/04
 * 
 * @see StringPrototypeB
 */ 

public class StringPrototypeA implements Cloneable {
    
    /**
     * the string that this object encapsulates
     */
         
    protected String text;
    
    /**
     * Creates a new StringPrototypeA object with the given string
     *
     * @param init the initial String for this object
     */

    public StringPrototypeA(String init) {
        text = init;
    }
    
    /**
     * Changes the string this object encapsulates
     *
     * @param newText the new text for this object.
     */  
    
    public void setText(String newText) {
        text = newText;
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    
    public String toString() {
        return "MyString: "+ text;
    } 
}
   