package ca.ubc.cs.spl.aspectPatterns.examples.facade.aspectj;

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
 * Implements the driver for the Facade design pattern example.<p> 
 *
 * Intent: <i>Provide a unified interface to a set of interfaces in a 
 * subsystem. Facade defines a higher-level interface that makes the 
 * subsystem easier to use.</i><p>
 *
 * <p><i>This is the AspectJ version.</i><p>    
 *
 * For this pattern, both Java and AspectJ implementations are identical.
 * The pattern introduces no crosscutting and is not abstractable. The
 * java implementation is not duplicated here: All this class does is to 
 * print a message explaining that the versions are identical.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 */
 
public class Main {

    public static void main(String[] args) {
        System.out.println("For this pattern, the AspectJ implementation ");
        System.out.println("is identical to the Java implementation. It is "); 
        System.out.println("not duplicated here. "); 
    }

}