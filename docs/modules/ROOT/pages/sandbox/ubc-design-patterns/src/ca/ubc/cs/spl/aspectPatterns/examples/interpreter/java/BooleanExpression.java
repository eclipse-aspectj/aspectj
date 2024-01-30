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
 * Declares the <i>AbstractExpression</i> interface for boolean expressions.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */

public interface BooleanExpression {
    
    /**
     * Evaluates this <i>Expression</i> in the given <i>VariableContext</i>
     *
     * @param c the <i>Context</i> to evaluate the <i>Expression</i> in
     * @return the boolean value of the <i>Expression</i>
     */
     
	public boolean evaluate(VariableContext c);

    /**
     * Replaces a variable with an <i>Expression</i>
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return a copy of this <i>Expression</i> with the variable replaced
     */
     
	public BooleanExpression replace(String name, BooleanExpression exp);

    /**
     * Copies this <i>Expression</i>
     *
     * @return the copied <i>Expression</i>
     */
     
	public BooleanExpression copy();
}