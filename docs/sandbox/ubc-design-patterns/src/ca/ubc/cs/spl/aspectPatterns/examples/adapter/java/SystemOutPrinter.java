package ca.ubc.cs.spl.aspectPatterns.examples.adapter.java;

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
 * Provides a specialized print method. Acts as the <i>Adaptee</i> in the 
 * pattern context.
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/26/04
 */
 
public class SystemOutPrinter {

    /**
     * Prints the argument string to <code>System.out</code>. In the pattern
     * context, this is the <i>specificRequest()</i> method on 
     * the <i>Adaptee</i>.
     *
     * @param s the string to be printed
     * @see Writer#write(String) the adapted method
     */
      

	public void printToSystemOut(String s) {
		System.out.println(s);
	}
}