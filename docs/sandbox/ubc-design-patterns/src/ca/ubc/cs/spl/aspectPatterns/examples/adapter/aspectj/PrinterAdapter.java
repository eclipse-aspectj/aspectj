package ca.ubc.cs.spl.aspectPatterns.examples.adapter.aspectj;

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
 * Adapts <code>SystemOutPrinter</code> to match the <code>Writer</code>
 * interface. 
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/26/04
 *
 * @see Target
 * @see Adaptee
 */
 
public aspect PrinterAdapter { 
    
    /**
     * Ensures that <code>SystemOutPrinter</code> implements <code>Writer
     * </code>. This means that the <i>Adaptee</i> effectively becomes its 
     * own <i>Adapter</i>. 
     */
     
	declare parents: SystemOutPrinter implements Writer;
	
	/**
	 * Defines a <code>write(String)</code> method on <code>Adaptee</code>
	 * to ensure compliance with the <i>Writer</i> interface.
	 *  
	 * On the pattern level, this means that <i>Adaptee</i> now implements
	 * <i>request()</i>. 
     *
     * @param s the string to print
     * @see Writer#write(String)
     * @see SystemOutPrinter#printToSystemOut(String)
	 */
	
	public void SystemOutPrinter.write(String s) {
		printToSystemOut(s);
	}
}