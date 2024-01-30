package ca.ubc.cs.spl.aspectPatterns.examples.builder.java;

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
 * Implements the driver for the Builder design pattern example.<p> 
 *
 * Intent: <i>Separate the construction of a complex object from its 
 * representation so that the same construction process can create different 
 * representations</i><p>
 *
 * Participating objects are <code>TextCreator</code> and 
 * <code>XMLCreator</code> which act as <i>Builder</i>s that implement the
 * <code>Creator</code> interface.<p>
 *
 * In this example, <code>Main</code> acts as the <i>Director</i> that
 * uses two different builders to build string representations of a
 * person. <code>TextCreator</code> creates a text-like representation,
 * <code>XMLCreator</code> an XML-like one.
 *
 * <p><i>This is the Java version.</i><p>
 *
 * In Java, the <i>Builder</i> has to be an abstract class (as opposed to
 * an interface) to allow to define variables or default implementations. 
 * Consequently, all <i>ConcreteBuilders</i> have to have that
 * class as their superclass, making it impossible to be part of another
 * class hierarchy.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/26/04
 * 
 * @see Builder
 * @see TextCreator
 * @see XMLCreator
 */

public class Main {

    /** 
     * Builds a string representation of a person using a given builder.
     *
     * @param builder the builder to use.
     */

    protected static void build(Creator builder) {
        builder.processType("Person");
        builder.processAttribute("Name");
        builder.processValue("James Brick");
        builder.processAttribute("Age");
        builder.processValue("33");
        builder.processAttribute("Occupation");
        builder.processValue("Builder"); 
    }

    /**
     * Implements the driver for the Builder design pattern example.<p> 
     *
     * In this example, <code>Main</code> acts as the <i>Director</i> that
     * uses two different builders to build string representations of a
     * person. <code>TextCreator</code> creates a text-like representation,
     * <code>XMLCreator</code> an XML-like one.
     * 
     * @param args the command-line parameters, unused.
     *
     */

    public static void main(String[] args) {
        
        Creator builder1 = new TextCreator();
        Creator builder2 = new XMLCreator();
        
        build(builder1);
        build(builder2);
        
        System.out.println(builder1.getRepresentation());
        System.out.println(builder2.getRepresentation());
    }
}