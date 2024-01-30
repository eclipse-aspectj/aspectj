package ca.ubc.cs.spl.aspectPatterns.examples.chainOfResponsibility.java;

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
  
import java.awt.event.ActionEvent;

/** 
 * Represents the <i>Request</i> in the <i>Chain of Responsibility</i>
 * pattern, which is a button click in this case. Provides methods for
 * accessing key masks associated with the click (to find out whether
 * the SHIFT, ALT, or CTRL keys were pressed during the click). 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 *
 */

public class Click { 
	
    /**
     * the ActionEvent that describes this Click
     */

	protected ActionEvent description;

	/** 
	 * Creates a <code>Click</code> described by the provided <code>
	 * ActionEvent</code>. 
	 *
	 * @param description the ActionEvent that describes this Click
	 */	 
     
    public Click(ActionEvent description) {
		this.description = description; 
	}      
	
    /** 
     * Convenience method for inquiring whether SHIFT was pressed while
     * the click occured. 
     * 
     * @return whether the SHIFT key was pressed when the click occured
     */	 
     
	public boolean hasShiftMask() {
		return ((description.getModifiers() & ActionEvent.SHIFT_MASK) != 0 );
	}

	/** 
	 * Convenience method for inquiring whether ALT was pressed while
	 * the click occured. 
	 * 
	 * @return whether the ALT key was pressed when the click occured
	 */	 
     
	public boolean hasAltMask() {
		return ((description.getModifiers() & ActionEvent.ALT_MASK) != 0 );
	}

	/** 
	 * Convenience method for inquiring whether CTRL was pressed while
	 * the click occured. 
	 * 
	 * @return whether the CTRL key was pressed when the click occured
	 */	 
     
	public boolean hasCtrlMask() {
		return ((description.getModifiers() & ActionEvent.CTRL_MASK) != 0 );
	}

}
	
	