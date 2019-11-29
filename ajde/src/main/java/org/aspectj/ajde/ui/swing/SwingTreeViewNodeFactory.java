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
 *     Helen Hawkins  Converted to new interface (bug 148190) 
 * ******************************************************************/


package org.aspectj.ajde.ui.swing;

import java.util.List;

import org.aspectj.ajde.IconRegistry;
import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureViewNodeFactory;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;

/**
 * @author Mik Kersten
 */
public class SwingTreeViewNodeFactory extends StructureViewNodeFactory {
	
	public SwingTreeViewNodeFactory(IconRegistry iconRegistry) {
		super(iconRegistry);	
	}

	protected IStructureViewNode createDeclaration(
		IProgramElement node,
		AbstractIcon icon,
		List children) {
		return new SwingTreeViewNode(node, icon, children);
	}

	protected IStructureViewNode createLink(
		IProgramElement node,
		AbstractIcon icon) {
		return new SwingTreeViewNode(node, icon);
	}

	protected IStructureViewNode createRelationship(
		IRelationship relationship,
		AbstractIcon icon) {
		return new SwingTreeViewNode(relationship, icon);
	}

}
