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


/**
 * Responsible for the build process, including compiler invocation, threading, and error
 * reporting.
 *
 * @author Mik Kersten
 */
public interface BuildManager {

	/**
	 * Build the structure model for the default build configuration.  No ".class" files
	 * are generated.
	 */
    public void buildStructure();

	/**
	 * Build the default build configuration.
	 */
    public void build();

    /**
     * Batch-build the default build configuration
     * when in incremental mode.
     */
    public void buildFresh();
    
    /**
     * Build the specified build configuration.
     */
    public void build(String configFile);

    /**
     * Batch-build the specified build configuration
     * when in incremental mode.
     */
    public void buildFresh(String configFile);

	/**
	 * Exit the build immediately, before completion.
	 */ 
    public void abortBuild();

	public BuildOptionsAdapter getBuildOptions();

    public boolean isStructureDirty();

    public void setStructureDirty(boolean structureDirty);

	public void setBuildModelMode(boolean mode);

    public void addListener(BuildListener compilerListener);

    public void removeListener(BuildListener compilerListener);
}

