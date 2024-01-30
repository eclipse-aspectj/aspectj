package ca.ubc.cs.spl.aspectPatterns.examples.observer.java;

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

import java.awt.Color; 

/**
 * Implements the driver for the Observer design pattern example.<p>
 *
 * Intent: <i>Define a one-to-many dependency between objects so that when one
 * object changes state, all its dependents are notified and updated 
 * automatically</i><p>
 *
 * Participating objects are <code>Point</code> p and <code>Screen</code>
 * s1, s2, s3, s4, and s5.<p>
 *
 * Three different kinds of observing relationships are realized: <UL>
 * <LI> <code>Screen</code> s1 and s2 observe color changes of <code>Point
 *      </code> p.
 * <LI> <code>Screen</code> s3 and s4 observe coordinate changes of <code>
 *      Point</code> p.
 * <LI> <code>Screen</code> s5 observes the <code>display(String)</code> 
 *      methods of <code>Screen</code> s2 and s4.
 * </UL>
 *
 * Every time an event of interest occurs, the observing <code>Screen</code>
 * prints an appropriate message to stdout. <p>
 *
 * <p>This is the Java version.</i><p> 
 *
 * The example illustrates that it is hard to
 * cleanly modularize the different observing relationships. The following
 * implementation issues have to be considered for the Java version: 
 * <UL>
 *   <LI> Observer and Subject can only be interfaces (as opposed to abstract
 *        classes) if we do not want to restrict inhertance and thus code
 *        reuse of existing classes completely.
 *   <LI> As interfaces, we cannot attach default implementations for methods
 *        like <i>attach(Observer)</i>, <i>notify()</i>, etc. Note that
 *        these two problems only apply because Java does not offer multiple
 *        inheritance. 
 *   <LI> Some implementation constraints are made implicit and are thus not
 *        enforced: I.e., each <i>Subject</i> needs a field to store its 
 *        <i>Observer</i>s
 *   <LI> The classes that become <i>Subject</i> and <i>Observer</i> in the 
 *        pattern context need to be modified. In particular, <i>Subject</i>s 
 *        need to store the mapping, implement the appropriate procedures. 
 *        <i>Observer</i>s need to
 *        implement <i>update()</i>
 *   <LI> If a particular class takes part in more than one observing
 *        relationship (as in this example), it is difficult to have both
 *        notify/update mechanisms go through the same interface and yet 
 *        separate them cleanly. 
 * </UL>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.11, 04/01/04
 */
 
public class Main {               
    
    /**
     * Implements the driver for the Observer example. It creates five 
     * <code>Screen</code> objects  and one <code>Point</code> object
     * and sets the appropriate observing relationships (see above). 
     * After the setup, the color of the point is changed, then it's 
     * x-coordinate. <p>
     * The following results should be expected: <OL>
     * <LI> The color change should trigger s1 and s2 to each print an 
     * appropriate message.
     * <LI> s2's message should trigger it's observer s5 to print
     * a message.
     * <LI> The coordinate change should trigger s3 and s4.
     * <LI> s4's message should trigger it's observer s5 again.
     */ 

    public static void main(String argv[]) {
	
    	Point p = new Point(5, 5, Color.blue);
    	
    	System.out.println("Creating Screen s1,s2,s3,s4,s5 and Point p");
    	
    	Screen s1 = new Screen("s1");
    	Screen s2 = new Screen("s2");
    	
    	Screen s3 = new Screen("s3");
    	Screen s4 = new Screen("s4");
    	
    	Screen s5 = new Screen("s5");

        System.out.println("Creating observing relationships:");    	
        System.out.println("- s1 and s2 observe color changes to p");    	
        System.out.println("- s3 and s4 observe coordinate changes to p");
        System.out.println("- s5 observes s2's and s4's display() method");
 
    	p.addObserver(s1); 
    	p.addObserver(s2);
    	
    	p.addObserver(s3); 
    	p.addObserver(s4);
            	
        s2.addObserver(s5);
        s4.addObserver(s5);
        
        System.out.println("Changing p's color:");    	

    	p.setColor(Color.red);

        System.out.println("Changing p's x-coordinate:");    	

    	p.setX(4); 
    	
        System.out.println("done.");    	
    }
}
