package ca.ubc.cs.spl.aspectPatterns.examples.builder.aspectj;

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
 * Implements a concrete builder design pattern instance. This aspect allows
 * to have the <code>Creator</code> abstract class to become an interface,
 * without losing the possibility to declare default implementations and even
 * variables.
 *
 * This also illiustrates the tradeoffs: The current version of AspectJ 
 * (1.0.4) does not allow protected introduction. To achieve the same result
 * as in the OO case, the result variable has to be introduced as public
 * (to be inherited). To make sure that no other classes can access that
 * variable, we define an error here that the compiler reports when other
 * classes try to access the result variable.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/26/04
 * 
 * @see Builder
 * @see TextBuilder
 * @see StructureBuilder
 */

public aspect CreatorImplementation {
    
    /**
     * Declares the result variable on the <code>Creator</code> interface.
     */
    
    public String Creator.representation;

    /**
     * Declares the <i>getResult()</i> method with a default implementation 
     * to the <code>Creator</code> interface.
     *
     * @returns the representation string for the builder.
     */

    public String Creator.getRepresentation() {
        return representation;
    }
    
    /**
     * Declares a compiler error that gets reported if other classes 
     * (except Creators or this aspect) try to access the result variable.
     */
    
    declare error: (set(public String Creator+.representation) 
                 || get(public String Creator+.representation)) 
              && ! (within(Creator+) 
                 || within(CreatorImplementation)): 
       "variable result is aspect protected. Use getResult() to access it";
}
