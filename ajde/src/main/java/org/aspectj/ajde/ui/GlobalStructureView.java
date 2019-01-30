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


/**
 * Should only be created by the {@link StructureViewManager} or an
 * equivalent factory.
 * 
 * @author Mik Kersten
 */
public class GlobalStructureView extends FileStructureView {

	private GlobalViewProperties viewProperties;

	public GlobalStructureView(GlobalViewProperties viewProperties) {
		super(viewProperties);
		this.viewProperties = viewProperties;	
	}	
	
	public GlobalViewProperties getGlobalViewProperties() {
		return viewProperties;	
	}
	
//	public void setActiveNode(StructureViewNode activeNode) {
//		StructureNode node = activeNode.getStructureNode();
//		
//		activeNode = activeNode;
//		if (renderer != null) renderer.setActiveNode(activeNode);
//	}
//	
//	public void setActiveNode(StructureViewNode activeNode, int sourceLine) {
//		activeNode = activeNode;
//		if (renderer != null) renderer.setActiveNode(activeNode, sourceLine);
//	}
	
	public String toString() {
		return viewProperties.getHierarchy().toString();	
	}
}
