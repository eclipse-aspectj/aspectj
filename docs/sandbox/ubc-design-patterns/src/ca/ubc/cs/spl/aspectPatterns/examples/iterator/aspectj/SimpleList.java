package ca.ubc.cs.spl.aspectPatterns.examples.iterator.aspectj;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either https://www.mozilla.org/MPL/ or https://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 *
 * For more details and the latest version of this code, please see:
 * https://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):
 */

/**
 * Defines an interface for a basic list.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 *
 * @see OpenList
 */

public interface SimpleList {

    /**
     * Returns the number of elements in the list
     *
     * @return the number of elements in the list
     */

	public int count();

	/**
	 * Appends an object to the list
	 *
	 * @param o the object to append
	 * @return true if successful, false otherwise
	 */

	public boolean append(Object o);

	/**
	 * Removes an object from the list
	 *
	 * @param o the object to remove
	 * @return true if successful, false otherwise
	 */

	public boolean remove(Object o);

	/**
	 * Returns an object from the list
	 *
	 * @param index the position of the object
	 * @return the object at position index
	 */

	public Object get(int index);
}
