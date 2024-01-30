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
 * Represents an <i>Object Adapter</i>. Implements the <i>Target</i> interface
 * and stores a private variable of type <i>Adaptee</i> (here: <code>
 * SystemOutWriter</code>) to which it forwards appropriate method calls. <p>
 * 
 * It is not possible to use a class adapter in Java as it requires multiple
 * inheritance. 
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/26/04
 *
 * @see Writer
 * @see SystemOutPrinter
 */
 
public class PrinterAdapter implements Writer { 
    
    /**
     * the adaptee to forward appropriate messages to.
     */
     
	private SystemOutPrinter adaptee = null;


    /**
     * Creates a new Adapter for a given Adaptee.
     *
     * @param screen the screen to adapt
     */

    public PrinterAdapter(SystemOutPrinter adaptee) {
        this.adaptee = adaptee;
    }
	
	/**
	 * Implements the <i>Target</i> interface. This is where the actual
	 * adaption happens: calls to write(String) trigger calls
	 * to printToSystemOut(String) on the adaptee.
     *
     * @param s the string to print
     * @see Writer#write(String)
     * @see SystemOutPrinter#printToSystemOut(String)
	 */
	
	public void write(String s) {
		adaptee.printToSystemOut(s);
	}
}