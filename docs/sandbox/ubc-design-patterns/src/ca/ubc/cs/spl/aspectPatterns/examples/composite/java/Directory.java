package ca.ubc.cs.spl.aspectPatterns.examples.composite.java;

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
 
import java.util.LinkedList;				

/**
 * Implements a <i>Composite</i>. Children are stored in a linked list.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see File
 */

public class Directory implements FileSystemComponent {
    
    /**
     * stores the children for this Directory (files and subdirectories)
     */
      
	protected LinkedList children = new LinkedList();		// Component interface

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

	                                                                             
    /**
     * Adds a child to the component
     *
     * @param component the child to add
     */
     
	public void	add(FileSystemComponent component) {
		this.children.add(component);
	} 

    /**
     * Removes a child from the component
     *
     * @param component the child to remove
     */
	public void remove(FileSystemComponent component) {
		this.children.remove(component);
	}

    /**
     * Returns a child of the Directory at the given position
     *
     * @param index the position of the child
     */
     
	public FileSystemComponent getChild(int index) {
		return (FileSystemComponent) children.get(index);
	}

    /**
     * Returns the number of chilren this Directory has
     *
     * @returns the number of children of this Directory
     */
 
	public int getChildCount() {
		return children.size();
	}
	
	/**
	 * Returns the size of this Directory. For simplicity, we define that only
	 * files have a tangible size, so this method returns 0.
	 * 
	 * @return the size of the component (on disk)
	 */
	public int getSize() {
		return 0;
	}
}
		