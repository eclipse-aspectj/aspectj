package ca.ubc.cs.spl.aspectPatterns.examples.command.aspectj;

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
 * Implements a sample class that becomes a <i>Command</i> in the pattern 
 * context. Instead of implementing the <i>Command</i> interface directly, 
 * the pattern aspect assigns that role to this class and ensures 
 * implementation of the <code>executeCommand()</code> method.<p>
 * 
 * This illustrates that any exisiting class can be turned into a <i>Command
 * </i> without having to change the class itself. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 */
 
public class ButtonCommand2 {}

