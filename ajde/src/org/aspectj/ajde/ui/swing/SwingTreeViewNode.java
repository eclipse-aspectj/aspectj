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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.StructureViewNode;
import org.aspectj.asm.StructureNode;

/**
 * @author Mik Kersten
 */
public class SwingTreeViewNode extends DefaultMutableTreeNode implements StructureViewNode {

	private StructureNode structureNode;
	private AbstractIcon icon;
	
	public SwingTreeViewNode(StructureNode structureNode, AbstractIcon icon, List children) {
		super(structureNode, true);
		this.structureNode = structureNode;
		this.icon = icon;
		
		if (children != null) {
			for (Iterator it = children.iterator(); it.hasNext(); ) { 
				super.add((SwingTreeViewNode)it.next());	
			}
		}
	}
	
	public StructureNode getStructureNode() {
		return structureNode;	
	}
	
	public AbstractIcon getIcon() {
		return icon;
	}	

	public void add(StructureViewNode child) { 
		super.add((DefaultMutableTreeNode)child);
	}
	
	public void remove(StructureViewNode child) { 
		super.remove((DefaultMutableTreeNode)child);
	}
	
	public List getChildren() {
		if (children == null) {
			return new ArrayList();
		} else {
			return children;
		}	
	}
}

