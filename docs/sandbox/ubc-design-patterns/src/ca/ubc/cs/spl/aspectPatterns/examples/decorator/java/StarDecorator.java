package ca.ubc.cs.spl.aspectPatterns.examples.decorator.java;

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
 * Implements a decorator that adds stars (" *** ") before and after the
 * string to decorate.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */  
         

public class StarDecorator extends OutputDecorator {

    /**
     * Adds three stars before and after the argument string before passing
     * the call on to the component this decorator decorates. 
     *
     * @param s the string to be decorated.
     */

	public void print(String s) {
		outputComponent.print(" *** " + s + " *** ");
	}

    /**
     * Creates a StarDecorator for the given output component
     *
     * @param output the <i>Component</i> to decorate.
     */

	public StarDecorator(Output output) {
		super(output);
	}


}