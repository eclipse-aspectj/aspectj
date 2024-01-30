package ca.ubc.cs.spl.aspectPatterns.patternLibrary;

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
 
import java.util.WeakHashMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;  

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.Command;
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.CommandInvoker;
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.CommandReceiver;

/**
 * This is the abstract <i>Command</i> protocol.
 * 
 * Note that this implementation allows only for exactly one command per
 * invoker. That is usually sufficient, but alternate implementations
 * could account for multiple commands by using composite 
 * (macro) commands (either with or without defined order).
 * 
 * To allow for some flexibility, commands can either be explicitly 
 * set or removed by <i>Client</i>s, or this can be done via
 * pointcuts. 
 * 
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public abstract aspect CommandProtocol { 
    
////////////////////////////////
// Invoker -> Command mapping //
////////////////////////////////

    /**
     * stores the mapping between CommandInvokers and Commands
     */
    
    private WeakHashMap mappingInvokerToCommand = new WeakHashMap();
    
    /**
     * Sets a new command for an invoker
     *
     * @param invoker the object which will invoke the command
     * @param command the command to be set
     * @return the former command
     */   
     
    public Object setCommand(CommandInvoker invoker, Command command) { 
    	return mappingInvokerToCommand.put(invoker, command); 
    }
    
    /**
     * Removes a command from an invoker
     *
     * @param invoker the object which will no longer invoke the command
     * @param command the command to be removed
     * @return the former command
     */   
     
    public Object removeCommand(CommandInvoker invoker) { 
    	return setCommand(invoker, null); 
    }


    /**
     * Returns the command for an invoker
     *
     * @param invoker the object for which to return the command 
     * @return the current command for the invoker
     */   
 
    public Command getCommand(CommandInvoker invoker) { 
    	return (Command) mappingInvokerToCommand.get(invoker); 
    }


/////////////////////////////////
// Command -> Receiver mapping //
/////////////////////////////////

    /**
     * stores the mapping between Coammnds and Receivers
     */
    
    private WeakHashMap mappingCommandToReceiver = new WeakHashMap();
    
    /**
     * Sets a new receiver for a command
     *
     * @param command the command to be set
     * @param receiver the object to be manipulated by the command's 
     *        execute() method
     * @return the former receiver
     */   
     
    public Object setReceiver(Command command, CommandReceiver receiver) { 
    	return mappingCommandToReceiver.put(command, receiver); 
    }
    
    /**
     * Returns the receiver for a particular command
     *
     * @param invoker the object for which to return the command 
     * @returns the current command for the invoker
     */   
 
    public CommandReceiver getReceiver(Command command) { 
    	return (CommandReceiver) mappingCommandToReceiver.get(command); 
    }


///////////////////////////////////////
// Command Execution via PC & advice //
///////////////////////////////////////


    /**
     * The join points after which to execute the command.
     * This replaces the normally scattered <i>Command.execute()</i> calls.
     *
     * @param invoker the object invoking the command
     */

    protected abstract pointcut commandTrigger(CommandInvoker invoker);


    /**
     * Calls <code>executeCommand()</code> when the command is triggered. 
     *
     * @param invoker the object invoking the command
     */ 
     
    after(CommandInvoker invoker): commandTrigger(invoker) { 
        Command command = getCommand(invoker);
    	if (command != null) {
    	    CommandReceiver receiver = getReceiver(command);
        	command.executeCommand(receiver);
        } else {
            // Do nothing: This Invoker has no associated command
        }
    }
    

//////////////////////////////////
// setCommand() via PC & advice //
//////////////////////////////////

    /**
     * The join points after which to set a command for an invoker.
     * This replaces the normally scattered <i>Invoker.add(Command)</i> calls.
     * The pointcut is provided in addition to the setCommand() method above,
     * to allow all pattern code to be removed from concrete invokers. 
     *
     * This PC is non-abstract, to make it optional for sub-aspcects to define
     * it.
     *
     * @param invoker the invoker to attach the command to
     * @param command the command to be attached to the invoker
     */

    protected pointcut setCommandTrigger(CommandInvoker invoker, Command command);   


    /**
     * Calls <code>addCommand()</code> when a command should be set. 
     *
     * @param invoker the invoker to attach the command to
     * @param command the command to be attached to the invoker
     */ 
     
    after (CommandInvoker invoker, Command command): 
    	setCommandTrigger(invoker, command) {                          
    	if (invoker != null) {
        	setCommand(invoker, command);
    	} else {
    		// If the invoker is null, the command cannot be set. 
    		// Either ignore this case or throw an exception  
    	}
    }

/////////////////////////////////////
// removeCommand() via PC & advice //
/////////////////////////////////////

    /**
     * The join points after which to remove a command from an invoker.
     * This replaces the normally scattered <code>Invoker.remove(Command)
     * </code> calls.
     * 
     * The pointcut is provided in addition to the <code>removeCommand()
     * </code> method above, to allow all pattern code to be removed from 
     * concrete invokers.
     *
     * This PC is non-abstract, to make it optional for sub-aspcects to define
     * it.
     *
     * @param invoker the invoker to remove the command from
     */

    protected pointcut removeCommandTrigger(CommandInvoker invoker);   
    
    /**
     * Calls <code>removeCommand()</code> when a command should be removed. 
     *
     * @param invoker the invoker to remove the command from
     */ 
     
    after(CommandInvoker invoker): removeCommandTrigger(invoker) {                          
    	if (invoker != null) {
        	removeCommand(invoker);
    	} else {
			// If the invoker is null, the command cannot be removed. 
			// Either ignore this case or throw an exception  
    	}
    }

////////////////////////////////////////////
// Command default method implementations //
////////////////////////////////////////////

    /**
     * Provides a deault implementation for the isExecutable method defined
     * in the Command interface. 
     *
     * @return true (default implementation). Can be overwritten by concrete
     * aspects or even concrete commands.
     */ 

    public boolean Command.isExecutable() {
        return true;
    } 
}
