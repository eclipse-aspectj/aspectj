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

import java.util.*;

import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */ 
public abstract class StructureViewNodeFactory {

	private AbstractIconRegistry iconRegistry;	

	public StructureViewNodeFactory(AbstractIconRegistry iconRegistry) {
		this.iconRegistry = iconRegistry;			
	}

	public IStructureViewNode createNode(IProgramElement node) {
		return createNode(node, null);		
	}

	public IStructureViewNode createNode(IProgramElement node, List children) {
		AbstractIcon icon = iconRegistry.getStructureIcon(node.getKind(), node.getAccessibility());

		IStructureViewNode svNode = createDeclaration(node, icon, children);		
		IRelationship rel = AsmManager.getDefault().getMapper().get(node);
		if (rel != null && rel.getTargets().size() > 0) {
			IStructureViewNode relNode = createRelationship(
				rel, 
				iconRegistry.getIcon(rel.getKind())
			);
			svNode.add(relNode, 0);
			
			for (Iterator it = rel.getTargets().iterator(); it.hasNext(); ) {
				IProgramElement link = (IProgramElement)it.next();
				IStructureViewNode linkNode = createLink(
					link,   
					iconRegistry.getStructureIcon(link.getKind(), link.getAccessibility())  
				);	
				relNode.add(linkNode);
					
			}
		}
		return svNode;
	}

	/**
	 * Implementors must override this method in order to create link new nodes.
	 */ 
	protected abstract IStructureViewNode createLink(IProgramElement node, AbstractIcon icon);
	
	/**
	 * Implementors must override this method in order to create new relationship nodes.
	 */ 	
	protected abstract IStructureViewNode createRelationship(IRelationship relationship, AbstractIcon icon);

	/**
	 * Implementors must override this method in order to create new nodes.
	 */ 
	protected abstract IStructureViewNode createDeclaration(IProgramElement node, AbstractIcon icon, List children);
}
