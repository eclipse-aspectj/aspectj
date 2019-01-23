/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.bridge;


/**
 * Command wrapper with collecting parameter for messages.
 */
public interface ICommand {
    /**
     * Run command with the given options.
     * @param args the String[] options for the command
     * @param handler the IMessageHandler for all output from
     * the command
     * @return true if the command completed successfully
     */
    boolean runCommand(String[] args, IMessageHandler handler);
    
    /**
     * Rerun the command.
     * 
     * @param handler the IMessageHandler for all output from the command
     * 
     * @return true if the command completed successfully
     */
	boolean repeatCommand(IMessageHandler handler);   
}
