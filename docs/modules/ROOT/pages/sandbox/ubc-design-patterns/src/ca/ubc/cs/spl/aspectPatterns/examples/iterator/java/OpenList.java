package ca.ubc.cs.spl.aspectPatterns.examples.iterator.java;  

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

import java.util.Iterator;

/**
 * Implements a basic open list. This implementation is based on 
 * <code>java.util.LinkedList</code>. In essence, this class acts as an 
 * adapter for the Java class.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 */

public class OpenList implements SimpleList {
    
	java.util.LinkedList list = new java.util.LinkedList();
	
    /** 
     * Returns the number of elements in the list
     *
     * @return the number of elements in the list
     */
     
	public int count() {
		return list.size();
	}
	
	/**
	 * Appends an object to the list. Since this is an open list, inserting 
	 * elements is assumed to succeed.
	 *
	 * @param o the object to append
	 * @return true if successful, false otherwise
	 */
	 
	public boolean append(Object o) {
		list.addLast(o);
		return true;
	}
	
	/**
	 * Removes an object from the list
	 *
	 * @param o the object to remove
	 * @return true if successful, false otherwise
	 */
	 
	public boolean remove(Object o) {
		return list.remove(o);   
	} 
	
	/**
	 * Returns an object from the list
	 *
	 * @param index the position of the object
	 * @return the object at the specified index  
	 */
	 
	public Object get(int index) {
		return list.get(index);
	}
	
	/**
	 * Returns a reverse iterator for this list. 
	 *
	 * @return the a reverse iterator for this list
	 */
	 
	public Iterator createReverseIterator() {
		return new ReverseIterator(this);
	}
}