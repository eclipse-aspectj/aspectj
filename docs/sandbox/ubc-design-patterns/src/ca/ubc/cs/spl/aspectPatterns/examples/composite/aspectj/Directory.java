package ca.ubc.cs.spl.aspectPatterns.examples.composite.aspectj;

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
 * Implements a <i>Composite</i>. Note that in this AspectJ version, the 
 * participants are decoupled from the pattern. Thus, this composite does
 * not need to implement an interface or even keep track of its children.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see Component 
 * @see File
 */
 
public class Directory { 

	/**
	 * stores the name of this Directory
	 */

	protected String name;
	
	/**
	 * Creates a new Directory with a given name
	 *
	 * @param name the name for the new Directory object
	 */

	public Directory(String name) {
		this.name = name;
	}
	 
	/**
	 * Overwrites the <code>toString()</code> method from <code>Object</code>
	 * to print information about this Directory
	 */

	public String toString() {
		return ("Directory: "+name);
	}
}
        