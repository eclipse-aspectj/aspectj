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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.PrototypeProtocol;

/**
 * Concretizes the abstract Prototype design pattern. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/13/04
 */ 
 
public aspect StringPrototypes extends PrototypeProtocol { 

    /**
     * Assigns the <i>Prototype</i> role to </code>StringPrototypeA</code>
     */
    
    declare parents: StringPrototypeA implements Prototype;

    /**
     * Assigns the <code>Prototype</code> role to </code>StringPrototypeB</code>
     */

    declare parents: StringPrototypeB implements Prototype;

    /**
     * Provides an alternative method for cases when the default
     * <code>clone()</code> method fails: Clones objects "by hand".
     * 
     * @param object the prototype object to clone
     * @return a copy of the object
     */

    protected Object createCloneFor(Prototype object) {
        if (object instanceof StringPrototypeB) {
            return new StringPrototypeB(((StringPrototypeB)object).toString());
        } else {
            return null;
        }
    } 

}