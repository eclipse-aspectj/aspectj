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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;

/**
 * @author Mik Kersten
 */
class SwingTreeViewNodeRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -4561164526650924465L;

	public Component getTreeCellRendererComponent(JTree tree,
                                                    Object treeNode,
                                                    boolean sel,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus) {
		if (treeNode == null) return null; 
		this.setFont(StructureTree.DEFAULT_FONT);       
        SwingTreeViewNode viewNode = (SwingTreeViewNode)treeNode;
        IProgramElement node = viewNode.getStructureNode();

        if (viewNode.getKind() == IStructureViewNode.Kind.LINK) {
            ISourceLocation sourceLoc = node.getSourceLocation();
            if ((null != sourceLoc) 
                && (null != sourceLoc.getSourceFile().getAbsolutePath())) {
                setTextNonSelectionColor(AjdeWidgetStyles.LINK_NODE_COLOR);
            } else {
                setTextNonSelectionColor(AjdeWidgetStyles.LINK_NODE_NO_SOURCE_COLOR);
            }
            
        } else if (viewNode.getKind() == IStructureViewNode.Kind.RELATIONSHIP) {
			this.setFont(new Font(this.getFont().getName(), Font.ITALIC, this.getFont().getSize()));
			setTextNonSelectionColor(new Color(0, 0, 0));
			
        } else if (viewNode.getKind() == IStructureViewNode.Kind.DECLARATION) {
			setTextNonSelectionColor(new Color(0, 0, 0));
        }
 
		super.getTreeCellRendererComponent(tree, treeNode, sel, expanded, leaf, row, hasFocus);       
		if (viewNode.getIcon() != null && viewNode.getIcon().getIconResource() != null) {
			setIcon((Icon)viewNode.getIcon().getIconResource());
		} else {
			setIcon(null);
		}
         
        if (node != null) {
        	if (node.isRunnable()) {
        		setIcon(Ajde.getDefault().getIconRegistry().getExecuteIcon());
        	}	 
			if (node.getMessage() != null) {
				if (node.getMessage().getKind().equals(IMessage.WARNING)) {
					setIcon(Ajde.getDefault().getIconRegistry().getWarningIcon());
				} else if (node.getMessage().getKind().equals(IMessage.ERROR)) {
					setIcon(Ajde.getDefault().getIconRegistry().getErrorIcon());
				} else {
					setIcon(Ajde.getDefault().getIconRegistry().getInfoIcon());
				}
			}

        } 	
        return this;
    }
}

