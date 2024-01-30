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

import java.util.Hashtable;

/**
 * Implements a <i>VariableContext</i> for the interpretation of boolean 
 * expressions<p>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see BooleanExpression
 */

public class VariableContext { 
    
    /**
     * stores the mapping between variable names and values
     */

	protected Hashtable assignments = new Hashtable();

    /**
     * Returns the current value for a variable
     *
     * @param name the name of the variable
     * @returns the value of the variable
     */
    
	public boolean lookup(String name) {
		Boolean value = (Boolean) assignments.get(name);
		if (value == null) {
			throw new ExpressionException("No variable \""+name+"\" declared.");
		}
		return value.booleanValue();
	}  
	
	/**
	 * Assigns a boolean value to a <code>VariableExpression</code>
	 *
	 * @param varExp the varaible expression to assign a value to
	 * @param bool the boolean value to assign 
	 */
	
	public void assign(VariableExpression varExp, boolean bool) {
		assignments.put(varExp.getName(), new Boolean(bool));
	}
}