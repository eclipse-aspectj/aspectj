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
 * Defines the <i>Component</i> interface for the composite design pattern.<p>
 * The implementation is anologuous to the one presented in GoF. Contemporary
 * Java implementations would probably change the <code>getChild(int)</code>
 * and <code>getChildCount()</code> methods to a single method that returns
 * a <code>Collection</code>. The AspectJ version has an appropriate
 * implementation.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see Directory 
 * @see File
 */
  
public interface FileSystemComponent { 
    
    /**
     * Adds a child to the component
     *
     * @param component the child to add
     */
     
	public void add(FileSystemComponent component);

    /**
     * Removes a child from the component
     *
     * @param component the child to remove
     */
     
	public void remove(FileSystemComponent component);

    /**
     * Returns the child of the component ath the given position
     *
     * @param index the position of the child
     */
     
	public FileSystemComponent getChild(int index);

    /**
     * Returns the number of chilren a component has
     *
     * @returns the number of children of this component
     */
     
	public int getChildCount();
	
	/**
	 * Returns the size of this FileSystemComponent
	 * 
	 * @return the size of the component (on disk)
	 */
	public int getSize();
}
