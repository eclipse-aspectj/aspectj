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


package org.aspectj.ajde.ui.internal;

import java.util.Stack;

import org.aspectj.asm.ProgramElementNode;

/**
 * @author Mik Kersten
 */
public class NavigationHistoryModel {
    
    private ProgramElementNode currNode = null;
    private Stack backHistory = new Stack();
    private Stack forwardHistory = new Stack();
    
    /**
     * @return 	null if the history is empty
     */
	public ProgramElementNode navigateBack() {
		if (backHistory.isEmpty() || currNode == null) return null;
		
		forwardHistory.push(currNode);
		currNode = (ProgramElementNode)backHistory.pop();
		return currNode;
	}

    /**
     * @return 	null if the history is empty
     */ 
	public ProgramElementNode navigateForward() {
		if (forwardHistory.isEmpty() || currNode == null) return null;
		
		backHistory.push(currNode);
		currNode = (ProgramElementNode)forwardHistory.pop();
		return currNode;
	}

    
    public void navigateToNode(ProgramElementNode toNode) {
    	if (currNode != null) backHistory.push(currNode);
    	currNode = toNode; 
    }
}
