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

import org.aspectj.asm.LinkNode;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.RelationNode;
import org.aspectj.asm.StructureNode;

/**
 * @author Mik Kersten
 */ 
public abstract class StructureViewNodeFactory {

	private AbstractIconRegistry iconRegistry;	

	public StructureViewNodeFactory(AbstractIconRegistry iconRegistry) {
		this.iconRegistry = iconRegistry;			
	}

	public StructureViewNode createNode(StructureNode node) {
		return createNode(node, null);		
	}

	public StructureViewNode createNode(StructureNode node, List children) {
		AbstractIcon icon;
		if (node instanceof ProgramElementNode) {
			ProgramElementNode pNode = (ProgramElementNode)node;
			icon = iconRegistry.getStructureIcon(pNode.getProgramElementKind(), pNode.getAccessibility());
		} else if (node instanceof RelationNode) {
			RelationNode relationNode = (RelationNode)node;
			icon = iconRegistry.getRelationIcon(relationNode.getRelation());
		} else if (node instanceof LinkNode) {
			LinkNode linkNode = (LinkNode)node;
			icon = iconRegistry.getStructureIcon(
				linkNode.getProgramElementNode().getProgramElementKind(), 
				linkNode.getProgramElementNode().getAccessibility());
		} else {
			icon = new AbstractIcon(null);	
		}
		return createConcreteNode(node, icon, children);
	}
	
	/**
	 * Implementors must override this method in order to create new nodes.
	 */ 
	protected abstract StructureViewNode createConcreteNode(StructureNode node, AbstractIcon icon, List children);
}
