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


package org.aspectj.ajde.ui.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureViewNodeFactory;
//import org.aspectj.ajde.ui.IStructureViewNode.Kind;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;

/**
 * @author Mik Kersten
 */
public class SwingTreeViewNode extends DefaultMutableTreeNode implements IStructureViewNode {

	private static final long serialVersionUID = 4957761341510335532L;
	private String relationshipName;
	private IProgramElement programElement;
	private AbstractIcon icon;
	private IStructureViewNode.Kind kind;

	/**
	 * Create a declaration node.
	 */	
	public SwingTreeViewNode(IProgramElement programElement, AbstractIcon icon, List children) {
		super(programElement, true);
		this.programElement = programElement;
		this.icon = icon;
		this.kind = Kind.DECLARATION;
		
		if (children != null) {
			for (Object o : children) {
				SwingTreeViewNode child = (SwingTreeViewNode) o;
				if (StructureViewNodeFactory.acceptNode(programElement, child.getStructureNode())) {
					super.add(child);
				}
			}
		}
	}

	/**
	 * Create a relationship node.
	 */	
	public SwingTreeViewNode(IRelationship relationship, AbstractIcon icon) {
		super(null, true);
		this.icon = icon;
		this.kind = Kind.RELATIONSHIP;
		this.relationshipName = relationship.getName();
	}
	
	/**
	 * Create a link.
	 */	
	public SwingTreeViewNode(IProgramElement programElement, AbstractIcon icon) {
		super(programElement, false);
		this.programElement = programElement;
		this.kind = Kind.LINK;
		this.icon = icon;
	}
	
	public IProgramElement getStructureNode() {
		return programElement;	
	}
	
	public AbstractIcon getIcon() {
		return icon;
	}	

	public void add(IStructureViewNode child) { 
		super.add((DefaultMutableTreeNode)child);
	}

	public void add(IStructureViewNode child, int position) { 
		super.insert((DefaultMutableTreeNode)child, position);
	}
	
	public void remove(IStructureViewNode child) { 
		super.remove((DefaultMutableTreeNode)child);
	}
	
	public List getChildren() {
		if (children == null) {
			return new ArrayList();
		} else {
			return children;
		}	
	}
	
	public Kind getKind() {
		return kind;
	}

	public String getRelationshipName() {
		return relationshipName;
	}
	
	public String toString() {
		if (kind == IStructureViewNode.Kind.RELATIONSHIP) {
			return relationshipName;
		} else if (kind == IStructureViewNode.Kind.LINK) {
			return programElement.toLinkLabelString();	
		} else {
			return programElement.toLabelString();
		}
	}

}

