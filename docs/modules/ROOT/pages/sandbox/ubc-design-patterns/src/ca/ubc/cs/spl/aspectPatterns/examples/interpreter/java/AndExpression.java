package ca.ubc.cs.spl.aspectPatterns.examples.interpreter.java;

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
 * Implements AND expressions for booleans. This is a concrete boolean 
 * <i>NonterminalExpression</i>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */

public class AndExpression implements BooleanExpression {

    /**
     * stores the first part of this AND <i>Expression</i>
     */
     
	protected BooleanExpression expression1;

    /**
     * stores the second part of this AND <i>Expression</i>
     */

	protected BooleanExpression expression2; 

    /** 
     * Creates a new AND <i>Expression</i> with the given parts
     *
     * @param expression1 the first <i>Expression</i>
     * @param expression1 the second <i>Expression</i>
     */	                    
     
	public AndExpression(BooleanExpression op1, BooleanExpression op2) {
		this.expression1 = op1;
		this.expression2 = op2;
	}
	
    /**
     * Evaluates this <i>Expression</i> in the given 
     * <code>VariableContext</code>
     *
     * @param c the <i>Context</i> to evaluate the <i>Expression</i> in
     * @return the boolean value of this AND <i>Expression</i>
     */
 
 	public boolean evaluate(VariableContext c) {
		return (expression1.evaluate(c) && expression2.evaluate(c)); 
	}
	
    /**
     * Replaces a variable with an <i>Expression</i>
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return a copy of this <i>Expression</i> with the variable replaced
     */

	public BooleanExpression replace(String name, BooleanExpression exp) {
		return new AndExpression(expression1.replace(name, exp), expression2.replace(name,exp));
	}
		
    /**
     * Copies this <i>Expression</i>
     *
     * @returns the copied <i>Expression</i>
     */
     
	public BooleanExpression copy() {
		return new AndExpression(expression1.copy(), expression2.copy());
	}
}