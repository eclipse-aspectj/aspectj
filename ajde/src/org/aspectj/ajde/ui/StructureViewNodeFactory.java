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

import java.util.List;

import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */ 
public abstract class StructureViewNodeFactory {

	private AbstractIconRegistry iconRegistry;	

	public StructureViewNodeFactory(AbstractIconRegistry iconRegistry) {
		this.iconRegistry = iconRegistry;			
	}

	public StructureViewNode createNode(IProgramElement node) {
		return createNode(node, null);		
	}

	public StructureViewNode createNode(IProgramElement node, List children) {
		AbstractIcon icon;
//		if (node instanceof IProgramElement) {
//		IProgramElement pNode = (IProgramElement)node;
		icon = iconRegistry.getStructureIcon(node.getKind(), node.getAccessibility());
//		} else if (node instanceof IRelationship) {
//			IRelationship relationNode = (IRelationship)node;
//			icon = iconRegistry.getRelationIcon(relationNode.getKind());
////		} else if (node instanceof LinkNode) {
////			LinkNode linkNode = (LinkNode)node;
////			icon = iconRegistry.getStructureIcon(
////				linkNode.getProgramElementNode().getProgramElementKind(), 
////				linkNode.getProgramElementNode().getAccessibility());
//		} else {
//			icon = new AbstractIcon(null);	
//		}
//		node.setChildren(children);
		return createConcreteNode(node, icon, children);
	}

	public StructureViewNode createNode(IRelationship relationship) {
		AbstractIcon icon;
		icon = iconRegistry.getRelationIcon(relationship.getKind());
		return createConcreteNode(relationship, icon);
	}
	
	protected abstract StructureViewNode createConcreteNode(IRelationship relationship, AbstractIcon icon);

	/**
	 * Implementors must override this method in order to create new nodes.
	 */ 
	protected abstract StructureViewNode createConcreteNode(IProgramElement node, AbstractIcon icon, List children);
}
