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

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import org.aspectj.ajde.ui.*;
import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */
public class SwingTreeViewNode extends DefaultMutableTreeNode implements StructureViewNode {

	private IProgramElement programElement;
	private AbstractIcon icon;
	
	public SwingTreeViewNode(IProgramElement programElement, AbstractIcon icon, List children) {
		super(programElement, true);
		this.programElement = programElement;
		this.icon = icon;
		
		if (children != null) {
			for (Iterator it = children.iterator(); it.hasNext(); ) { 
				super.add((SwingTreeViewNode)it.next());	
			}
		}
	}

	public SwingTreeViewNode(IRelationship relationship, AbstractIcon icon) {
//		super(IProgramElement, true);
		throw new RuntimeException("unimplemented");
//		this.IProgramElement = IProgramElement;
//		this.icon = icon;
//		
//		if (children != null) {
//			for (Iterator it = children.iterator(); it.hasNext(); ) { 
//				super.add((SwingTreeViewNode)it.next());	
//			}
//		}
	}
	
	public IProgramElement getStructureNode() {
		return programElement;	
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

