package ca.ubc.cs.spl.aspectPatterns.examples.builder.java;

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
 * Defines the interface for <i>Builder</i>s. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/26/04
 * 
 * @see TextCreator
 * @see XMLCreator
 */

public abstract class Creator {
    
    /** 
     * An inheritable variable that carries the result of the build.
     */
    
    protected String representation;

    /** 
     * Defines a <i>buildPart()</i> operation for type parts.
     *
     * @param type the type to process
     */ 

    public abstract void processType(String type);

    /** 
     * Defines a <i>buildPart()</i> operation for attribute parts.
     *
     * @param newAttribute the attribute to process
     */ 

    public abstract void processAttribute(String newAttribute);

    /** 
     * Defines a <i>buildPart()</i> operation for value parts.
     *
     * @param newValue the value to process
     */ 

    public abstract void processValue(String newValue);
    
    /** 
     * Defines a <i>getResult()</i> operation for <i>Builder</i>s.
     * This is the default implementation.
     * 
     * @return a representation of the build result
     */ 

    public String getRepresentation() {
        return representation;
    }  
}
    