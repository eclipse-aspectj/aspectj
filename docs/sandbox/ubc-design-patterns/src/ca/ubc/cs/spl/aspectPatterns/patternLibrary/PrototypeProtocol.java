package ca.ubc.cs.spl.aspectPatterns.patternLibrary;

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
 * Implements the abstract Prototype design pattern. It attaches a default
 * <code>clone()</code> method on all <i>Prototype</i> participants and
 * provides a static <code>cloneObject(Prototype)</clone> method. The default
 * implementation of that method is to try to use the <code>clone()</code> 
 * method and, failing that, to call its protected <code>
 * createCloneFor(Prototype)</code> method. Concrete subaspects can either 
 * overwrite none or one (or both) of the methods to tailor their
 * particular design pattern instance to its individual needs
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/13/04
 */ 

public abstract aspect PrototypeProtocol { 
    
    /**
     * Defines the <i>Prototype</i> role.
     */
    
    protected interface Prototype {}
    
    /**
     * Attaches a default <code>clone()</code> method to all prototypes.
     * This makes use of Java's clone() mechanism that creates a deep copy
     * of the object in question.
     * 
     * @return a copy of the object
     */

    public Object Prototype.clone() throws CloneNotSupportedException {   
        return super.clone();
    }
    
    /**
     * Provides a static default aspect method for cloning prototypes.
     * It uses the attached clone() method if possible. If not, it calls the
     * static <code>createCloneFor(Prototype)</code> method.
     * 
     * @param object the prototype object to clone
     * @return a copy of the object
     */
    
    
    public Object cloneObject(Prototype object) {
        try {
            return object.clone();
        } catch (CloneNotSupportedException ex) {
            return createCloneFor(object);
        }
    }
    
    /**
     * Provides an alternative method for cases when the default
     * <code>clone()</code> method fails. This method can be 
     * overwritten by concrete subaspects. In this default implementation
     * it return null.
     * 
     * @param object the prototype object to clone
     * @return a copy of the object, but null in this case
     */

    protected Object createCloneFor(Prototype object) { 
        return null;
    }
}