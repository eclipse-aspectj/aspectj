/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

/**
 * @author Mik Kersten
 */
public class ModelWalker {

	private StructureModel model;

	public ModelWalker() {
		super();
	}
	
	public ModelWalker(StructureModel model) {
		this.model = model;
    }

    protected void preProcess(StructureNode node) { }
    
    protected void postProcess(StructureNode node) { }

    public StructureNode process(StructureNode node) {
		preProcess(node);
        node.walk(this);
        postProcess(node);
        return node;
    }
}
