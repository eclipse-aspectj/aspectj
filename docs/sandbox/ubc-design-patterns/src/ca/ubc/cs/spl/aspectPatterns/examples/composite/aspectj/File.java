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
 * Implements a <i>Leaf</i>. Note that in this AspectJ version, the 
 * participants are decoupled from the pattern. Thus, this leaf does
 * not need to implement the <i>Component</i> interface.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see Component 
 * @see Directory
 */
 
public class File { 
    
	/**
	 * stores the name for this File
	 */

	protected String name;
	
	/**
	 * stores the size for this File
	 */

	protected int size;
	/**
	 * Creates a new File with a given name and size
	 *
	 * @param name the name for the new File
	 * @param size the size for the new File
	 */

	public File(String name, int size) {
		this.name = name;
		this.size = size;
	}
	 
	/**
	 * Overwrites the <code>toString()</code> method from <code>Object</code>
	 * to print information about this object
	 */

	public String toString() {
		return ("File: "+name+" ("+size+" KB)");
	}
 
 	/**
	 * Returns the size of this File
	 * 
	 * @return the size of this File (on disk)
	 */
	public int getSize() {
		return size;
	}
}
