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
 * Defines the <i>Decorator</i> interface. This is realized as an abstract 
 * class to allow for default implementations (set varible "output", 
 * provide default implementation for <code>print(String)</code>).
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */  
                       
public abstract class OutputDecorator implements Output {

    /**
     * the <i>Component</i> to decorate
     */  

	protected Output outputComponent;
	
    /**
     * Prints the argument string to <code>System.out</code>. This method is
     * overwritten by concrete decorators. The default implementation
     * forwards the method call to the decorated <i>Component</i>.
     *
     * @param s the string to be printed.
     */
      
	public void print(String s) { 
		outputComponent.print(s);
	}
	
    /**
     * Defines the constructor signature. Also provides a default 
     * implementation so that concrete decorators don't have to 
     * re-implement it. Subclasses (<i>ConcreteDecorator</i>s) can just 
     * call <code>super(..)</code> and don't have
     * to deal with setting the variable themselves.
     *
     * @param output the <i>Component</i> to decorate.
     */
 
 	public OutputDecorator(Output output) {
		this.outputComponent = output;
	}
}