package ca.ubc.cs.spl.aspectPatterns.examples.interpreter.aspectj;

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
 * Implements <code>replace(String, BooleanExpression)<code> and
 * <code>copy()</code> methods for all concrete
 * <code>BooleanExpression</code>s. 
 *
 * The very nature of the interpreter pattern introduces coupling between all
 * participants. Unfortunately, removing the pattern code from the 
 * participants does not work as nicely here as with other patterns. The
 * reason is that the roles are defining, i.e., each participant's 
 * functionality is determined (only) by its role. If aspects were used
 * to implement the entire pattern functionality, it would leave 
 * the <i>Expressions</i> etc. empty and would make the aspect a 
 * monolithic module. <p>
 *
 * However, it is still possible to augment or change the behaviour of the
 * system without changing all participant classes. To show this, we 
 * assumed that <code>BooleanExpression.replace(String, BooleanExpression)
 * </code> and <code>BooleanExpression.copy()</code> were added later. 
 * An aspect is used to implement those methods, so that other 
 * classes do not have to change (we only changed the interface, but 
 * even that was not necessary).<p>
 *
 * In general, however, this pattern does not lend itself nicely to 
 * aspectification.<p>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see BooleanExpression
 */

public aspect BooleanInterpretation {
                                            
	// AndExpressionression

    /**
     * Replaces a variable with an <i>Expression</i>. 
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return a copy of this <i>Expression</i> with the variable replaced
     */

	public BooleanExpression AndExpression.replace(String name, BooleanExpression exp) {
		return new AndExpression(expression1.replace(name, exp), expression2.replace(name,exp));
	}
		
    /**
     * Copies this <i>Expression</i>
     *
     * @return the copied <i>Expression</i>
     */
     
	public BooleanExpression AndExpression.copy() {
		return new AndExpression(expression1.copy(), expression2.copy());
	}                                            

	// BooleanConstant

    /**
     * Replaces a variable with an <i>Expression</i>. 
     * Has no effect on constants.
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return the unchanged expression
     */

	public BooleanExpression BooleanConstant.replace(String name, BooleanExpression exp) {
		return this;  
	}
	
    /**
     * Copies this <i>Expression</i>
     *
     * @return the copied <i>Expression</i>
     */
     
	public BooleanExpression BooleanConstant.copy() {
		return new BooleanConstant(value);
	}

	// OrExpression
	
    /**
     * Replaces a variable with an <i>Expression</i>. 
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return a copy of this <i>Expression</i> with the variable replaced
     */

	public BooleanExpression OrExpression.replace(String name, BooleanExpression exp) {
		return new OrExpression(expression1.replace(name, exp), expression2.replace(name,exp));
	}
		
    /**
     * Copies this <i>Expression</i>
     *
     * @return the copied <i>Expression</i>
     */
     
	public BooleanExpression OrExpression.copy() {
		return new OrExpression(expression1.copy(), expression2.copy());
	}
	
	// VariableExpression

    /**
     * Replaces a variable with an <i>Expression</i>. 
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return a copy of this <i>Expression</i> with the variable replaced
     */

	public BooleanExpression VariableExpression.replace(String name, BooleanExpression exp) {
		if (name.equals(this.name)) {
			return exp.copy();
		} else {
			return new VariableExpression(this.name);
		}
	}
	
    /**
     * Copies this <i>Expression</i>
     *
     * @return the copied <i>Expression</i>
     */
     
	public BooleanExpression VariableExpression.copy() {
		return new VariableExpression(name);
	}

	// NotExpressionressionressionression

	
    /**
     * Replaces a variable with an <i>Expression</i>. 
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return a copy of this <i>Expression</i> with the variable replaced
     */

	public BooleanExpression NotExpression.replace(String name, BooleanExpression exp) {
		return new NotExpression(this.exp.replace(name, exp));
	}
		
    /**
     * Copies this <i>Expression</i>
     *
     * @return the copied <i>Expression</i>
     */
     
	public BooleanExpression NotExpression.copy() {
		return new NotExpression(exp.copy());
	}		
}

