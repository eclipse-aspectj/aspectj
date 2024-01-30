package ca.ubc.cs.spl.aspectPatterns.examples.templateMethod.aspectj;

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
 * Implements an instance of the Template Method design pattern.
 * Attaches the template method and its implementation to the 
 * <i>AbstractClass</i>, thereby allowing it to be an interface.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public aspect Generating {
	
	/**
	 * Defines the implementation of the <i>TemplateMethod()</i>.
	 * Generates a string with certain decorations.
	 * 
	 * @param s the string to be used to generate a decorated string from
	 */

	public String DecoratedStringGenerator.generate(String s) {
		s = prepare(s);
		s = filter(s);
		s = finalize(s);
		return s;
	}
}
	