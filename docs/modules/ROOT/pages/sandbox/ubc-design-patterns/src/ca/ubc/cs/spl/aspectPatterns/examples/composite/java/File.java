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
 
/**
 * Implements a <i>Leaf</i>. Leafs have no children.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see Directory
 */

public class File implements FileSystemComponent {

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
     * Adds a child to the Component. Since Files have no
     * children, this method does nothing.
     *
     * @param component the child to add
     */

	public void	 add(FileSystemComponent component) {}

    /**
     * Removes a child from the Component. Since Files have no
     * children, this method does nothing.
     *
     * @param component the child to add
     */

	public void	 remove(FileSystemComponent component) {}

    /**
     * Returns a child of the Component. Since Files are <i>Leaf</i>s, they
     * don't have any children. Thus, this method returns null.
     *
     * @param index the position of the child
     * @return always null, since Files do not have children
     */
     
	public FileSystemComponent getChild(int index) {
		return null;
	}

    /**
     * Returns the number of chilren this Component has. Since Files 
     * are <i>Leaf</i>s, they don't have any children. Thus, this method 
     * returns 0.
     *
     * @returns always 0, since Files do not have children
     */
 
	public int	 getChildCount() {
		return 0;
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
