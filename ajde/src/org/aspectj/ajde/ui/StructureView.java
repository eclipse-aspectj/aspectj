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


package org.aspectj.ajde.ui;

import java.util.Iterator;

import org.aspectj.asm.ProgramElementNode;

/**
 * @author Mik Kersten
 */
public abstract class StructureView {

	private StructureViewNode rootNode = null; 
	private StructureViewNode activeNode = null; 
	protected StructureViewProperties viewProperties = null;
	protected StructureViewRenderer renderer = null;
	
	public StructureViewProperties getViewProperties() {
		return viewProperties;	
	}  
	
	public StructureViewNode getRootNode() {
		return rootNode; 
	}

	public void setRootNode(StructureViewNode rootNode) {
		this.rootNode = rootNode;
	}

	public void setViewProperties(StructureViewProperties viewProperties) {
		this.viewProperties = viewProperties;
	}

	public void setRenderer(StructureViewRenderer renderer) {
        this.renderer = renderer;
    }

	protected void notifyViewUpdated() {
		if (renderer != null) renderer.updateView(this);
    }

	/**
	 * @return		the view node corresponding to the active ProgramElementNode or null
	 */
	public StructureViewNode getActiveNode() {
		if (activeNode != null 
			&& activeNode.getStructureNode() instanceof ProgramElementNode) {
			return activeNode;
		} else {
			return null;
		}
	}

	/**
	 * Searches from the root node of the view down in order to find matches.
	 * 
	 * @return		the first match
	 */
	public StructureViewNode findCorrespondingViewNode(ProgramElementNode node) {
		return findCorrespondingViewNodeHelper(rootNode, node);
	}

	private StructureViewNode findCorrespondingViewNodeHelper(StructureViewNode node, ProgramElementNode pNode) {
		
		if (node != null
			&& node.getStructureNode() != null 
			&& node.getStructureNode().equals(pNode)) {
			return node;	
		} 
		
		if (node != null && node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				StructureViewNode foundNode = findCorrespondingViewNodeHelper((StructureViewNode)it.next(), pNode); 		
				if (foundNode != null) return foundNode;
			}
		}
		
		return null;
	}	

	public void setActiveNode(StructureViewNode activeNode) {
		this.activeNode = activeNode;
		if (renderer != null) renderer.setActiveNode(activeNode);
	}
	
	public void setActiveNode(StructureViewNode activeNode, int sourceLine) {
		this.activeNode = activeNode;
		if (renderer != null) renderer.setActiveNode(activeNode, sourceLine);
	}
}
 
