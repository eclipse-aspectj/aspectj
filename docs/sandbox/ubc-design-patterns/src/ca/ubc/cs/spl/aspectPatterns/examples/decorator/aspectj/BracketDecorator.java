package ca.ubc.cs.spl.aspectPatterns.examples.decorator.aspectj;

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
 * Adds brackets ("[", "]") before and after the string to decorate.
 * Acts as a <i>ConcreteDecorator</i>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */  
         

public aspect BracketDecorator {
    
    /**
     * Identifies the execution points of interest: all calls to 
     * <code>ConcreteOutput.print(String)</code>.
     */

    protected pointcut printCall(String s): 
      	call(public void ConcreteOutput.print(String)) && args(s);

    /**
     * Adds brackets before and after the argument string before passing
     * the call on to the component this decorator aspect decorates. 
     *
     * @param s the string to be decorated.
     */

    void around(String s): printCall(s) {
        s = "[" + s + "]";					// Decorates the string
        proceed(s);
    }
}