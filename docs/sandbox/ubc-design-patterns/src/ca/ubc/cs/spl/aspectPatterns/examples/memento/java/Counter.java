package ca.ubc.cs.spl.aspectPatterns.examples.memento.java;

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
 * Implements a sample <i>Originator</i> class. Objects of this particular
 * <i>Originator</i> have state, an int representing the number of time
 * the <code>increment()</code> method was called.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 */

public class Counter {

    /**
     * the number of times <code>increment()</code> was called on this object
     */

	protected int currentValue = 0;

    /**
     * increments the counter (this <i>Originator</i>'s state) by one
     */

	public void increment() {
	    currentValue++;
	}

    /**
     * Displays the state of this <i>Originator</i>
     */

	public void show() {
	    System.out.println("Originator value is " + currentValue);
	}

    /**
     * Creates a <i>Memento</i> from this <i>Originator</i>, storing the
     * current state
     */

	public CounterMemento createMemento() {
	    return new CounterMemento(currentValue);
	}

    /**
     * Restores this <i>Originator</i> to former state stored by the
     * memento passed
     *
     * @param memento the <i>Memento</i> that stores the prior state
     */

	public void setMemento(CounterMemento memento) {
	    currentValue = memento.getState();
	}
}
