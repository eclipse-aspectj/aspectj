package ca.ubc.cs.spl.aspectPatterns.examples.observer.aspectj;

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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.ObserverProtocol;

/**
 * Concretizes the observing relationship for <code>Point</code> (subject) 
 * and <code>Screen</code> (observer). Coordinate changes trigger updates. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.11, 04/01/04
 */

public aspect CoordinateObserver extends ObserverProtocol{


	/**
	 * Assings the <i>Subject</i> role to the <code>Point</code> class.
	 * Roles are modeled as (empty) interfaces.
	 */
   
	declare parents: Point  implements Subject;

	/**
	 * Assings the <i>Observer</i> role to the <code>Screen</code> class.
	 * Roles are modeled as (empty) interfaces.
	 */
   
	declare parents: Screen implements Observer;

	/**
	 * Specifies the join points that represent a change to the
	 * <i>Subject</i>. Captures calls to <code>Point.setX(int)
	 * </code> and <code>Point.setY(int)</code>. 
	 * @param subject the <code>Point</code> acting as <i>Subject</i>
	 */

	protected pointcut subjectChange(Subject subject): 
		(call(void Point.setX(int)) ||
		 call(void Point.setY(int)) ) && target(subject);

	/**
	 * Defines how <i>Observer</i>s are to be updated when a change
	 * to a <i>Subject</i> occurs. 
	 *
	 * @param subject the <i>Subject</i> on which a change of interest occured
	 * @param observer the <i>bserver</i> to be notifed of the change  
	 */

	protected void updateObserver(Subject subject, Observer observer) {
		((Screen)observer).display("Screen updated "+
			"(point subject changed coordinates).");
	}
}
