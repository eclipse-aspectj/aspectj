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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.CommandProtocol;
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.Command;
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.CommandInvoker;
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.CommandReceiver; 
import java.io.PrintStream;

/**
 * Sets up the Command pattern.
 * 
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see Button
 */

public aspect ButtonCommanding extends CommandProtocol {

    declare parents: Button         implements CommandInvoker;
    declare parents: Printer        implements CommandReceiver;
    declare parents: ButtonCommand  implements Command;     // Unneccessay
    declare parents: ButtonCommand2 implements Command;     // "Making" a class
                                                            // a Command
  
    /**
     * Implements a sample <i>Command</i> for the ButtonCommand2 class. 
     * This one prints a short message to <code>System.out</code> 
     * whenever it executes. The message is
     * <quote>"ButtonCommand number 2 executed"</quote>. 
     */
 
    public void ButtonCommand2.executeCommand(CommandReceiver receiver) {
        ((Printer) receiver).println("ButtonCommand number 2 executed");
    }  

    /**
     * The join points after which to execute the command.
     * This replaces the normally scattered myCommand.execute() calls. 
     * In this example, a call to <code>Button.clicked()</code> triggers
     * the execution of the command.
     *
     * @param invoker the object invoking the command
     */

    protected pointcut commandTrigger(CommandInvoker invoker): 
        call(void Button.clicked()) && target(invoker);
}

