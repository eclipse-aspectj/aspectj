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
 * Implements the driver for the Intepreter design pattern example.<p> 
 *
 * Intent: <i>Given a language, defeine a representation fro its grammar along
 * with an interpreter that uses the representation to interpret sentences
 * in the language.</i><p>
 *
 * Participating objects are <code>BooleanContant</code>, <code>VariableExpression
 * </code>, <code>OrExpression</code>, <code>AndExpression</code>, and <code>NotExpression</code>
 * as <i>Expressions</i>. The <i>AbstractExpression</i> interface is defined 
 * in <code>BooelanExp</i>.<p>
 *
 * This example implements an interpreter for a language of boolean 
 * expressions. As a sample expression, "((true & x) | (y & !x))" is
 * interpreted for all possible boolean values for x and y. After that, 
 * y is replaced by another expression and the whole expression is
 * evaluated again. 
 *
 * <p><i>This is the AspectJ version.</i><p>
 *
 * The very nature of this pattern introduces coupling between all 
 * participants. Unfortunately, removing the pattern code from the 
 * participants does not work as nicely here as with other patterns. The
 * reason is that the roles are defining, i.e., each participant's 
 * functionality is determined (only) by its role. Removing pattern
 * specific code into an aspect would leave the <i>Expressions</i> etc.
 * empty and would make the aspect a monolithic module. <p>
 *
 * However, it is still possible to augment or change the behaviour of the
 * system without changing all participant classes. To show this, we 
 * assumed that <code>BooleanExpression.replace(String, BooleanExpression)</code> and
 * <code>BooleanExpression.copy()</code> were added later. The methods are
 * defined in an aspect, so that other classes did not have to change.<p>
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

public class Main {

    /** 
     * Assigns boolean values to two <code>VariableExpression</code>s and evaluates
     * an expression in the given context.
     *
     * @param x a boolean variable expression
     * @param xValue the value to assign to x
     * @param y another boolean variable expression
     * @param yValue the value to assign to y
     * @param context the context to evaluate the expression in
     * @param exp the expression to evaluate
     */

	private static void assignAndEvaluate(  VariableExpression x, boolean xValue,
						VariableExpression y, boolean yValue,
						VariableContext context, BooleanExpression exp) {
		context.assign(x, xValue);
		context.assign(y, yValue);
		boolean result = exp.evaluate(context);
		System.out.println("The result for (x="+xValue+", y="+yValue+") is: "+result);
	}

    /**
     * Implements the driver for the Intepreter design pattern example.<p> 
     *
     * This example implements an interpreter for a language of boolean 
     * expressions. As a sample expression, "((true & x) | (y & !x))" is
     * interpreted for all possible boolean values for x and y. After that, 
     * y is replaced by another expression and the whole expression is
     * evaluated again.   
     *
     * @args command-line parameters, unused.
     */

	public static void main(String[] args) {
		BooleanExpression exp = null;
		VariableContext context = new VariableContext();
		
		VariableExpression x = new VariableExpression("X");
		VariableExpression y = new VariableExpression("Y");		
		
		exp = new OrExpression(new AndExpression(new BooleanConstant(true), x), 
					    new AndExpression(y, new NotExpression(x)));
		
		System.out.println("Testing Expr: ((true & x) | (y & !x))");			 

		assignAndEvaluate(x, false, y, false, context, exp);
		assignAndEvaluate(x, false, y, true,  context, exp);
		assignAndEvaluate(x, true,  y, false, context, exp);
		assignAndEvaluate(x, true,  y, true,  context, exp);
		
		VariableExpression z = new VariableExpression("Z");
		NotExpression   notZ = new NotExpression(z);
		
		BooleanExpression replacement = exp.replace("Y", notZ);
		context.assign(z, false);
		boolean result = replacement.evaluate(context);

		System.out.println("The result for the replacement is: "+result);
	}
}
		