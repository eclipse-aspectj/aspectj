package ca.ubc.cs.spl.aspectPatterns.examples.bridge.aspectj;

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
 * Implements the driver for the Bridge design pattern example. <p> 
 *
 * Intent: <i> Decouple an abstraction from its implementation so that the 
 * two can vary independently.</i><p>
 *
 * Scenario: Have seperate hierarchies for Abstractions (here: Screens)
 *           and Implementors (here: ScreenImplementation), so that both 
 *           can change independently of each other 
 *
 * Participants: <UL>
 *
 * <LI> <code>Screen</code> - <i>Abstraction</i> that defines 
 *      an interface for printing text and boxedText to stdout. 
 * <LI> <code>GreetingScreen</code> - <i>RefinedAbstraction</i> that prints
 *      a boxed greeting message
 * <LI> <code>InformationScreen</code> - <i>RefinedAbstraction</i> that prints
 *      the system time (boxed)
 * <LI> <code>ScreenImplementation</code> - <i>Implementor</i> interface, 
 *      defines basic operations to output formatted strings
 * <LI> <code>StarImplementation</code> - <i>ConcreteImplementation</i> that 
 *      creates textBoxes of stars
 * <LI> <code>CrossCapitalImplementation</code> - <i>ConcreteImplementation
 *      </i> that creates textBoxes of double crosses (hashes) and prints all
 *      text capitalized
 * <LI> <code>AbstractionImplementation</code> - An aspect that defines
 *      the implementation of the methods of the <i>Abstraction</i>
 * </UL><p>
 *
 * <i>This is the AspectJ implementation.</i><p>
 * 
 * The implementations for methods on the <i>Abstraction</i> are declared
 * in the <code>AbstractImplementation</code> aspect.
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/26/04
 *
 * @see Screen
 * @see InformationScreen
 * @see GreetingScreen
 * @see ScreenImplementation
 * @see StarImplementation
 * @see CrossCapitalImplementation
 * @see AbstractionImplementation
 */
 
 
public class Main {

    /**
     * Implements the driver for this example. The two different screens
     * and screen implementations are tested in all possible combinations.
     *
     * @param args required by Java, but ignored
     */
     
    public static void main(String[] args) { 
        
        System.out.println("Creating implementations...");
    
        ScreenImplementation i1 = new StarImplementation();
        ScreenImplementation i2 = new CrossCapitalImplementation();
        
        System.out.println("Creating abstraction (screens) / implementation combinations...");
        
        GreetingScreen gs1 = new GreetingScreen(i1);
        GreetingScreen gs2 = new GreetingScreen(i2);
        InformationScreen is1 = new InformationScreen(i1);
        InformationScreen is2 = new InformationScreen(i2);  
        
        System.out.println("Starting test:\n");
        
        gs1.drawText("\nScreen 1 (Refined Abstraction 1, Implementation 1):");
        gs1.drawGreeting();
        
        gs2.drawText("\nScreen 2 (Refined Abstraction 1, Implementation 2):");
        gs2.drawGreeting();
        
        is1.drawText("\nScreen 3 (Refined Abstraction 2, Implementation 1):");
        is1.drawInfo();

        is2.drawText("\nScreen 4 (Refined Abstraction 2, Implementation 2):");
        is2.drawInfo();
    }
}