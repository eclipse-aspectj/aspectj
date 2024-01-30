package ca.ubc.cs.spl.aspectPatterns.examples.mediator.java;

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
 * This is the <i>Mediator</i> interface. It defines a method for dealing
 * with changes in <i>Colleague</i>s that require updates. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 */
 
public interface GUIMediator {
    
    /**
     * Defines the method signature for notifying <i>Mediator</i>s of changes 
     * to <i>Colleague</i>s. This method is called by colleagues who 
     * pass themselves as an argument (push model).
     *
     * @param colleague the changing colleage
     */
    
	public void colleagueChanged(GUIColleague colleague);
}