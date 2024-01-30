package ca.ubc.cs.spl.aspectPatterns.examples.proxy.java;

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
 * Defines the <i>Subject</i> interface that is implemented by both 
 * <code>RequestCounter</code> and <code>OutputImplementation</code>.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */  
 
public interface OutputSubject { 
    
    /**
     * A type of <i>request(..)</i>.
     *
     * @param s the string to print
     */
     
	public void safeRequest(String s);

    /**
     * A type of <i>request(..)</i>.
     *
     * @param s the string to print
     */

	public void regularRequest(String s);
	
	/**
     * A type of <i>request(..)</i>.
	 *
	 * @param s the string to print
	 */

	public void unsafeRequest(String s);

}