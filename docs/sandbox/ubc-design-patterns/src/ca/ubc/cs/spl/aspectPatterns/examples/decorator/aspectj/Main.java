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
 * Implements the driver for the Decorator design pattern example.<p> 
 *
 * Intent: <i>Attach additional responsibilities to an object dynamically. 
 * Decorators provide a flexible alternative to subclassing for extending 
 * functionality.</i><p>
 *
 * Participating classes are <code>Output</code>s as <i>Component</i>s,
 * <code>ConcreteOutput</code> as <i>ConcreteComponent</i>. The decorators 
 * are <code>OutputDecorator</code> as <i>Decorator</i>, and <code>
 * StarDecorator</code> and <code>BracketDecorator</code> as <i>
 * ConcreteDecorator</i>s.<p>
 *
 * Experimental setup: The concrete decorator (ConcreteOutput) prints a
 * string, Decorators (StarDecorator and BracketDecorator) wrap other
 * output around it. Output should be: "[ *** <String> *** ]"
 *
 * <p><i>This is the AspectJ version.</i><p> 
 *
 * This version does not allow for dynamic composition of decorators. 
 * However, this version decouples <i>ConcreteComponent</i>s, clients 
 * and <i>Decorators</i>. Neither clients nor <i>ConcreteComponents</i>
 * need to have pattern code in them.
 * 
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see ConcreteOutput
 * @see StarDecorator 
 * @see BracketDecorator
 */
 
public class Main {

	/**
	 * Implements the driver for the Decorator design pattern example.<p> 
	 *
	 * Experimental setup: Concrete decorator (ConcreteOutput) prints a
	 * string, Decorators (StarDecorator and BracketDecorator) wrap other
	 * output around it. Output should be: "[ *** <String> *** ]" 
	 *
	 * @param args command line paramters, unused
	 */
	
	public static void main(String[] args) {
	    
		ConcreteOutput original = new ConcreteOutput();
		
		original.print("<String>");    
		
		System.out.println();
	}
}