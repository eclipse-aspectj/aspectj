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


package org.aspectj.ajde.ui;

import org.aspectj.asm.IProgramElement;

/**
 * @author Mik Kersten
 */
public abstract class StructureView {

	private IStructureViewNode rootNode = null; 
	private IStructureViewNode activeNode = null; 
	protected StructureViewProperties viewProperties = null;
	protected StructureViewRenderer renderer = null;
	
	public StructureViewProperties getViewProperties() {
		return viewProperties;	
	}  
	
	public IStructureViewNode getRootNode() {
		return rootNode; 
	}

	public void setRootNode(IStructureViewNode rootNode) {
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
	public IStructureViewNode getActiveNode() {
		if (activeNode != null 
			&& activeNode.getStructureNode()!=null) {
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
	public IStructureViewNode findCorrespondingViewNode(IProgramElement node) {
		return findCorrespondingViewNodeHelper(rootNode, node);
	}

	private IStructureViewNode findCorrespondingViewNodeHelper(IStructureViewNode node, IProgramElement pNode) {		
		if (node != null
			&& node.getStructureNode() != null 
			&& node.getStructureNode().equals(pNode)
			&& node.getKind() == IStructureViewNode.Kind.DECLARATION) {	
				
			return node;	  
		} 
		
		if (node != null && node.getChildren() != null) {
			for (Object o : node.getChildren()) {
				IStructureViewNode foundNode = findCorrespondingViewNodeHelper((IStructureViewNode) o, pNode);
				if (foundNode != null) return foundNode;
			}
		}
		
		return null;
	}	

	public void setActiveNode(IStructureViewNode activeNode) {
		this.activeNode = activeNode;
		if (renderer != null) renderer.setActiveNode(activeNode);
	}
	
	public void setActiveNode(IStructureViewNode activeNode, int sourceLine) {
		this.activeNode = activeNode;
		if (renderer != null) renderer.setActiveNode(activeNode, sourceLine);
	}
}
 
