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
 * Implements the abstract Memento design pattern. It defines the role of 
 * <i>Originator</i>. The <i>Memento</i> role is client-usable and as such
 * defined outside this aspect. 
 *
 * Concrete sub-aspects overwrite the two abstract methods to define how
 * <i>Memento</i>s get generated and how they are used to restore the state
 * of <i>Originator</i>s.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 */
 
public abstract aspect MementoProtocol {
	                                                                            
    /**
     * Defines the <i>Originator</i> type. Used only internally.
     */

	protected interface Originator {}  

    /**
     * Creates a <i>Memento</i> object for an <i>Originator</i>
     *
     * @param o the <i>Originator</i> to create a <i>Memento</i> for
     * @return the <i>Memento</i> storing the originator's state
     */
	
	public abstract Memento createMementoFor(Originator o);
		
    /**
     * Restores this <i>Originator</i> to a former state encapsulated in the 
     * <i>Memento</i> passed
     *
     * @param o the <i>Originator</i> to restore
     * @param m the <i>Memento</i> that stores the prior state
     */

	public abstract void setMemento(Originator o, Memento m);
}