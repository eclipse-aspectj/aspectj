package ca.ubc.cs.spl.aspectPatterns.examples.templateMethod.java;

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
 * Defines the interface for generating decorated strings. 
 * In this example, it acts as the <i>AbstractClass</i>. 
 * 
 * The template method is <code>generate(String)</code>,
 * which uses all other methods defined in this abstract class.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public abstract class DecoratedStringGenerator { 
    
    /**
     * Decorates a string. This is the <i>TemplateMethod()</i>.
     *
     * @param s the string to decorate
     * @return the decorated string
     */

	public String generate(String s) {
		s = prepare(s);
		s = filter(s);
		s = finalize(s);
		return s;
	}
	                        
    /**
     * Prepares a string for decoration.
     *
     * @param s the string to filter
     * @return the prepared string
     */

	public abstract String prepare  (String s);

    /**
     * Filters a string. 
     *
     * @param s the string to filter
     * @return the filtered string
     */

	public abstract String filter   (String s);

    /**
     * Finalizes a string. This is the last step in the template method.
     *
     * @param s the string to finalize
     * @return the finalized string
     */
	
	public abstract String finalize (String s);
}		