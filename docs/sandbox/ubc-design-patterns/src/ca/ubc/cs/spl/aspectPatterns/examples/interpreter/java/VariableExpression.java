package ca.ubc.cs.spl.aspectPatterns.examples.interpreter.java;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either https://www.mozilla.org/MPL/ or https://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 *
 * For more details and the latest version of this code, please see:
 * https://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):
 */

/**
 * Implements a variable expression for booleans. This is a concrete
 * boolean <i>NonterminalExpression</i>
 * expression
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */

public class VariableExpression implements BooleanExpression {

    /**
     * the name of the variable this object represents
     */

	protected String name = null;

	/**
	 * Creates a new <code>VariableExpression</code> with the given name
	 *
	 * @param name the name of the new variable
	 */

	public VariableExpression(String name) {
		this.name = name;
	}

	/**
	 * Accessor for the variable's name
	 *
	 * @return the name of the variable
	 */

	public String getName() {
		return name;
	}

    /**
     * Evaluates this <i>Expression</i> in the given <i>Context</i>
     *
     * @param c the context to evaluate the <i>Expression</i> in
     * @return the boolean value of the <i>Expression</i>
     */

	public boolean evaluate(VariableContext c) {
		return c.lookup(name);
	}

    /**
     * Replaces a variable with an <i>Expression</i>
     *
     * @param name the name of the variable
     * @param exp the <i>Expression</i> to replace the variable
     * @return a copy of this <i>Expression</i> with the variable replaced
     */

	public BooleanExpression replace(String name, BooleanExpression exp) {
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

	public BooleanExpression copy() {
		return new VariableExpression(name);
	}
}
