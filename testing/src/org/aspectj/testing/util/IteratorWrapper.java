/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.util;

import java.util.Iterator;
import java.util.List;

/** 
 * This iterates in order through the permutations of Lists. 
 * Order and numericity depend on the underlying list iterators
 * and the order in which the lists are supplied to the constructor.
 * @author isberg
 */
public class IteratorWrapper implements Iterator {

    final List[] lists;
    final Iterator[] iterators;
    final Object[] current;  
    Object[] next;
    
    /** number of elements in each array */
    final int maxDepth;

    /** 
     * Current level being iterated.
     * Set to 0 whenever depth is incremented.
     * Incremented when iterator for the level has no more elements.
     */
    int currentLevel; 
    
    /** 
     * Maximum depth iterated-to thus far.
     * Set to 0 on initialization.
     * Incremented when incrementing currentLevel brings it past depth.
     * Run completes when depth = maxDepth or any new iterator has no elements
     */
    int depth; 


    /** @throws IllegalArgumentException if lists or any element null */
	public IteratorWrapper(List[] lists) {
        if (null == lists) {
            throw new IllegalArgumentException("null lists");
        }
        maxDepth = lists.length;
        currentLevel = 0;
        depth = 0;
        List[] temp = new List[maxDepth];
        System.arraycopy(lists, 0, temp, 0, temp.length);
        for (int i = 0; i < maxDepth; i++) {
            if (null == temp[i]) {
                throw new IllegalArgumentException("null List[" + i + "]");
            }
		}
        this.lists = temp;
        current = new Object[maxDepth];
        iterators = new Iterator[maxDepth];
        reset();
    }
    
    /** Reset to the initial state of the iterator */
    public void reset() {
        next = null;
        for (int i = 0; i < lists.length; i++) {
			iterators[i] = lists[i].iterator();
            if (!iterators[i].hasNext()) { // one iterator is empty - never go
                depth = maxDepth;
                break;
            } else {
                current[i] = iterators[i].next();
            }
		}
        if (depth < maxDepth) {
            next = getCurrent();
        }
	}

    /** @throws UnsupportedOperationException always */
    public void remove() {
        throw new UnsupportedOperationException("operation ambiguous");
    }

	public boolean hasNext() {
		return (null != next);
	}

    /** 
     * @return Object[] with each element from the iterator of the
     * corresponding list which was passed to the constructor for this.
     */
	public Object next() {
        Object result = next;
        next = getNext();
        return result;
    }

    private Object[] getCurrent() {
        Object[] result = new Object[maxDepth];
        System.arraycopy(current, 0, result, 0, maxDepth);
        return result;
    }
    
    private Object[] getNext() {
        int initialLevel = currentLevel;
        while (depth < maxDepth) {
            if (iterators[currentLevel].hasNext()) {
                current[currentLevel] = iterators[currentLevel].next();
                if (currentLevel > initialLevel) {
                    currentLevel = 0;
                }
                return getCurrent();
            } else { // pop 
                // reset this level
                iterators[currentLevel] = lists[currentLevel].iterator();
                if (!iterators[currentLevel].hasNext()) { // empty iterator - quit
                    depth = maxDepth;
                    return null;
                }
                current[currentLevel] = iterators[currentLevel].next();
    
                // do the next level
                currentLevel++;
                if (currentLevel > depth) {
                    depth++;
                }
            }
        }
        return null;
    }
    /** @return "IteratorWrapper({{field}={value}}..)" for current, ceiling, and max */
    public String toString() {
        return "IteratorWrapper(currentLevel=" + currentLevel
            + ", maxLevel=" + depth
            + ", size=" + maxDepth + ")";
    }    
}
