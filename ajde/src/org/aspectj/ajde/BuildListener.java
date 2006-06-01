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



package org.aspectj.ajde;

import java.util.EventListener;

/**
 * Compiler listeners get notified of compilation events.
 *
 * @author Mik Kersten
 */
public interface BuildListener extends EventListener {
    
    /**
     * Called when a new build is started.
     * 
     * @param   buildConfigFile configuration file used for the last compile
     */
    public void compileStarted(String buildConfigFile);

    /**
     * Called when a build completes.
     * 
     * @param   buildConfigFile configuration file used for the last compile
     * @param   buildTime   	 compilation time in miliseconds
     * @param   succeeded   	 true if build succeeded without errors
     */
    public void compileFinished(String buildConfigFile, int buildTime, boolean succeeded, boolean warnings);
    
    /**
     * Called when a build is aborted before completion.
     * 
     * @param	buildConfigFile	configuration used for the last compile
     * @param	message			message explaining reason for abort
     */
    public void compileAborted(String buildConfigFile, String message);
}

