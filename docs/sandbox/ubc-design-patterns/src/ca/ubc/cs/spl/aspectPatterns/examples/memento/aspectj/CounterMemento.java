package ca.ubc.cs.spl.aspectPatterns.examples.memento.aspectj; 

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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.Memento;
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.MementoException;
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.MementoProtocol;

/**
 * Implements an instance of the Memento design pattern. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 *
 * @see MyOriginator 
 */


public aspect CounterMemento extends MementoProtocol { 

    /**
     * Assigns the <i>Originator</i> role to <code>Counter</code>
     */

	declare parents: Counter implements Originator;  

    /**
     * Creates a <i>Memento</i> object for an <i>Originator</i>. An anonymous
     * class is used to realize the Memento
     *
     * @param o the originator to create a memento for
     * @return the <i>Memento</i> storing the originator's state
     */

	public Memento createMementoFor(Originator o) {
	    if (o instanceof Counter) { 
   		Memento m = new Memento() {
    			private Integer state;
    			
    			public void setState(Object state) {
    				this.state = (Integer) state;
    			}
    			
    			public Object getState() {
    				return state;
    			}
    		};
    		m.setState(new Integer(((Counter)o).currentValue));
    		return m;
    	} else {
    	    throw new MementoException("Invalid originator");
    	}
	}
	
    /**
     * Restores this <i>Originator</i> to former state using the 
     * <i>Memento</i> passed
     *
     * @param o the originator to restore
     * @param m the memento that stores the prior state
     */

	public void setMemento(Originator o, Memento m) {
	    if (o instanceof Counter) {
    		Integer integer = (Integer) m.getState(); 
	    	((Counter)o).currentValue = integer.intValue();
    	} else {
    	    throw new MementoException("Invalid originator");
    	}
	}
}