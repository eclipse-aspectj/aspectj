package ca.ubc.cs.spl.aspectPatterns.examples.iterator.aspectj;

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

import java.util.NoSuchElementException;
import java.util.Iterator;

/**
 * Implements a concrete instance of the Iterator design pattern. This
 * concrete aspect attaches a factory method to the <i>Aggregate</i> class
 * and provides an implementation for the Iterator via an inner class.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 *
 * @see SimpleList 
 * @see OpenList
 */

public aspect OpenListIteration {  
    
    /**
     * Implements the factory method to create the reverse iterator for the
     * OpenList class using the open classes mechanism.
     */

    Iterator OpenList.createReverseIterator() {
    	return new ReverseIterator(this);
    }

    /**
     * Another possibility to define the factory method here. If we
     * have the factory method on the aspect, multiple instances of the
     * same pattern will not be confused an can use the same aggregate
     * (multiple factory methods won't conflict that way, even if they
     * have the same signature). This is the preferable implementation.
     */

    Iterator createIteratorFor(OpenList list) {
    	return new ReverseIterator(list);
    }

    /**
     * Provides the implementation of the reverse iterator. Instead
     * of defining an inner class, one could also instantiate  
     * <code>java.util.Iterator</code> as an anonymous class and overwrite
     * the appropriate methods.
     */

    static class ReverseIterator implements Iterator {
    
        /**
         * the positition of the current element
         */
    
    	protected int current; 
    	
    	/**
    	 * the list this iterator operates on
    	 */
    	 
    	protected SimpleList list; 
    	
    	/**
    	 * Returns true if the iteration has more elements.
    	 *
    	 * @return true if the iteration has more elements
    	 */
    	
    	public boolean hasNext() {
    		return (current > 0);
    	}
    	
    	/**
    	 * This opional method is not implemented for this iterator.
    	 */ 
    	
    	public void remove() {
    	    throw new UnsupportedOperationException("remove() not supported");
        }                     
    	
        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration. 
         */
    
    	public Object next() {
    		if (!hasNext()) {
    			throw new ArrayIndexOutOfBoundsException("Iterator out of Bounds"); 
    		} else {
        		return list.get(--current);
    	    }
    	}
    	
    	/**
    	 * Creates a new ReverseIterator from the given list.
    	 *
    	 * @param list the list to generate an iterator from
    	 */
    	
    	public ReverseIterator(SimpleList list) {
    		super();  
    		this.list = list;
    		current = list.count();
    	}
    }
}
	