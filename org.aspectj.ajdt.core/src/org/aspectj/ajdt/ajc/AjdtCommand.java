/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.ajc;

import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.CountingMessageHandler;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.eclipse.jdt.internal.core.builder.MissingSourceFileException;

/**
 * ICommand adapter for the AspectJ eclipse-based compiler.
 */
public class AjdtCommand implements ICommand {
    
    /** Message String for any AbortException thrown from ICommand API's */
    public static final String ABORT_MESSAGE = "ABORT";
    
    private boolean canRepeatCommand = true;
	
	AjBuildManager buildManager = null;
	String[] savedArgs = null;
          
	/**
     * Run AspectJ compiler, wrapping any exceptions thrown as
     * ABORT messages (containing ABORT_MESSAGE String).
     * @param args the String[] for the compiler
     * @param handler the IMessageHandler for any messages
	 * @see org.aspectj.bridge.ICommand#runCommand(String[], IMessageHandler)
     * @return false if command failed
	 */
	public boolean runCommand(String[] args, IMessageHandler handler) {
		try {
			buildManager = new AjBuildManager(handler); 
			savedArgs = new String[args.length];
            System.arraycopy(args, 0, savedArgs, 0, savedArgs.length);
			CountingMessageHandler counter = new CountingMessageHandler(handler);
            AjBuildConfig config = genBuildConfig(savedArgs, counter);
            return (!counter.hasErrors()
                && buildManager.batchBuild(config, counter)
                && !counter.hasErrors());
		} catch (AbortException ae) {
        	if (ae.isSilent()) {
        		throw ae;
        	} else {
        		MessageUtil.abort(handler, ABORT_MESSAGE, ae);
        		return false;
        	}
        } catch (MissingSourceFileException t) { // XXX special handling - here only?
            MessageUtil.error(handler, t.getMessage());
            return false;
    	} catch (Throwable t) {
			//System.err.println("caught: " + t);
            MessageUtil.abort(handler, ABORT_MESSAGE, t);
            return false;
		} 
	} 

    /**
     * Run AspectJ compiler, wrapping any exceptions thrown as
     * ABORT messages (containing ABORT_MESSAGE String).
     * @see org.aspectj.bridge.ICommand#repeatCommand(IMessageHandler)
     * @return false if command failed
     */
	public boolean repeatCommand(IMessageHandler handler) {
		if (null == buildManager) {
            MessageUtil.abort(handler, "repeatCommand called before runCommand");
            return false;            
        }
        try {
			//buildManager.setMessageHandler(handler);
            CountingMessageHandler counter = new CountingMessageHandler(handler);
            // regenerate configuration b/c world might have changed (?)
			AjBuildConfig config = genBuildConfig(savedArgs, counter);  
			System.err.println("errs: " + counter.hasErrors());          
            return (!counter.hasErrors()
                    && buildManager.incrementalBuild(config, handler)
                    && !counter.hasErrors());
        } catch (MissingSourceFileException t) {
        	System.err.println("missing file");
            return false; // already converted to error
		} catch (Throwable t) {
            MessageUtil.abort(handler, ABORT_MESSAGE, t);
            return false;
		}
	}

    /** @throws AbortException.ABORT on error after logging message */
    AjBuildConfig genBuildConfig(String[] args, IMessageHandler handler) {
        BuildArgParser parser = new BuildArgParser();
        AjBuildConfig config = parser.genBuildConfig(args, handler);
        String message = parser.getOtherMessages(true);       

        if (null != message) {
            IMessage.Kind kind = inferKind(message);
            handler.handleMessage(new Message(message, kind, null, null));
            throw new AbortException(); // XXX tangled - assumes handler prints?
        }
        return config;
    }
    
    /** @return IMessage.WARNING unless message contains error or info */
    protected IMessage.Kind inferKind(String message) { // XXX dubious
        if (-1 == message.indexOf("error")) {
            return IMessage.ERROR;
        } else if (-1 == message.indexOf("info")) {
            return IMessage.INFO;
        } else {
            return IMessage.WARNING;
        }
    }
}
