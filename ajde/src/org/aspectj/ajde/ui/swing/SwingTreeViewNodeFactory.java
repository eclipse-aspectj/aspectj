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


package org.aspectj.ajde.ui.swing;

import java.util.List;

import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.StructureViewNode;
import org.aspectj.ajde.ui.StructureViewNodeFactory;
import org.aspectj.asm.StructureNode;

/**
 * @author Mik Kersten
 */
public class SwingTreeViewNodeFactory extends StructureViewNodeFactory {
	
	public SwingTreeViewNodeFactory(IconRegistry iconRegistry) {
		super(iconRegistry);	
	}
	
	protected StructureViewNode createConcreteNode(StructureNode node, AbstractIcon icon, List children) {
		return new SwingTreeViewNode(node, icon, children);
	}
}
