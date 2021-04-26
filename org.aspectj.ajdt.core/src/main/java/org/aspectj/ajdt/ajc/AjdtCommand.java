/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.ajc;

import org.aspectj.ajdt.internal.core.builder.*;
import org.aspectj.bridge.*;
import org.aspectj.weaver.Dump;
import org.aspectj.org.eclipse.jdt.internal.core.builder.MissingSourceFileException;

/**
 * ICommand adapter for the AspectJ compiler.
 * Not thread-safe.
 */
public class AjdtCommand implements ICommand {
    
    /** Message String for any AbortException thrown from ICommand API's */
    public static final String ABORT_MESSAGE = "ABORT";
    
//    private boolean canRepeatCommand = true;
	
	AjBuildManager buildManager = null;
	String[] savedArgs = null;
          
	/**
     * Run AspectJ compiler, wrapping any exceptions thrown as
     * ABORT messages (containing ABORT_MESSAGE String).
     * @param args the String[] for the compiler
     * @param handler the IMessageHandler for any messages
	 * @see org.aspectj.bridge.ICommand#runCommand(String[], IMessageHandler)
     * @return false if handler has errors or the command failed
	 */
	public boolean runCommand(String[] args, IMessageHandler handler) {
		buildManager = new AjBuildManager(handler); 
		savedArgs = new String[args.length];
        System.arraycopy(args, 0, savedArgs, 0, savedArgs.length);
        return doCommand(handler, false);
    }

    /**
     * Run AspectJ compiler, wrapping any exceptions thrown as
     * ABORT messages (containing ABORT_MESSAGE String).
     * @param handler the IMessageHandler for any messages
     * @see org.aspectj.bridge.ICommand#repeatCommand(IMessageHandler)
     * @return false if handler has errors or the command failed
     */
	public boolean repeatCommand(IMessageHandler handler) {
		if (null == buildManager) {
            MessageUtil.abort(handler, "repeatCommand called before runCommand");
            return false;            
        }
        return doCommand(handler, true);
    }
    
    /** 
     * Delegate of both runCommand and repeatCommand.
     * This invokes the argument parser each time
     * (even when repeating).
     * If the parser detects errors, this signals an 
     * abort with the usage message and returns false.
     * @param handler the IMessageHandler sink for any messages
     * @param repeat if true, do incremental build, else do batch build
     * @return false if handler has any errors or command failed
     */
    protected boolean doCommand(IMessageHandler handler, boolean repeat) {
        try {
            if (handler instanceof IMessageHolder) {
                Dump.saveMessageHolder((IMessageHolder) handler);
            }
			// buildManager.setMessageHandler(handler);
            CountingMessageHandler counter = new CountingMessageHandler(handler);
            if (counter.hasErrors()) {
                return false;
            }
            // regenerate configuration b/c world might have changed (?)
            AjBuildConfig config = genBuildConfig(savedArgs, counter);
            if (!config.shouldProceed()) {
            	return true;
            }
            if (!config.hasSources()) {
                MessageUtil.error(counter, "no sources specified");
            }
            if (counter.hasErrors())  {
                // Do *not* print usage for config errors (just like ECJ does it). Otherwise the user would have to
                // scroll up several screens in order to actually see the error messages.
                // String usage = BuildArgParser.getUsage();
                // MessageUtil.abort(handler, usage);
                return false;
            }
            //System.err.println("errs: " + counter.hasErrors());          
            boolean result = ((repeat 
                        		? buildManager.incrementalBuild(config, handler)
                        		: buildManager.batchBuild(config, handler))
                    		   && !counter.hasErrors());
			Dump.dumpOnExit();
			return result;
        } catch (AbortException ae) {
            if (ae.isSilent()) {
                throw ae;
            } else {
                MessageUtil.abort(handler, ABORT_MESSAGE, ae);
            }
        } catch (MissingSourceFileException t) { 
            MessageUtil.error(handler, t.getMessage());
        } catch (Throwable t) {
            MessageUtil.abort(handler, ABORT_MESSAGE, t);
			Dump.dumpWithException(t);
        } 
        return false;
    }
	
    /** 
     * This creates a build configuration for the arguments.
     * Errors reported to the handler:
     * <ol>
     *   <li>The parser detects some directly</li>
     *   <li>The parser grabs some from the error stream
     *       emitted by its superclass</li>
     *   <li>The configuration has a self-test</li>
     * </ol>
     * In the latter two cases, the errors do not have
     * a source location context for locating the error.
     */
    public static AjBuildConfig genBuildConfig(String[] args, CountingMessageHandler handler) {
        BuildArgParser parser = new BuildArgParser(handler);
        AjBuildConfig config = parser.genBuildConfig(args);

		ISourceLocation location = null;
		if (config.getConfigFile() != null) {
			location = new SourceLocation(config.getConfigFile(), 0); 
		}

		String message = parser.getOtherMessages(true);
        if (null != message) {  
            IMessage.Kind kind = inferKind(message);
            IMessage m = new Message(message, kind, null, location);            
            handler.handleMessage(m);
        }  
//        message = config.configErrors();
//        if (null != message) {
//            IMessage.Kind kind = inferKind(message);
//            IMessage m = new Message(message, kind, null, location);            
//            handler.handleMessage(m);
//        }
        return config;
    }
    
    /**
     * Heuristically infer the type of output message logged by the AspectJ compiler. This is a simple keyword matcher
     * looking for substrings like "[error]", "[warning]", "AspectJ-specific options:", "AspectJ-specific non-standard
     * options:", "Warning options:".
     *
     * @param message AspectJ compiler message
     * @return inferred message kind, either of ERROR, WARNING, USAGE, INFO
     */
    protected static IMessage.Kind inferKind(String message) { // XXX dubious
      if (message.contains("[error]")) {
        return IMessage.ERROR;
      }
      else if (message.contains("[warning]")) {
        return IMessage.WARNING;
      }
      else if (
        containsAll(message, "Usage: <options>", "AspectJ-specific options:", "Classpath options:") ||
          containsAll(message, "Warning options:", "-nowarn", "localHiding", "uselessTypeCheck") ||
          containsAll(message, "AspectJ-specific non-standard options:", "-XnoInline", "-Xjoinpoints:")
      )
      {
        return IMessage.USAGE;
      }
      else {
        return IMessage.INFO;
      }
    }

  private static boolean containsAll(String message, String... searchStrings) {
    for (String searchString : searchStrings)
      if (!message.contains(searchString))
        return false;
    return true;
  }
}
