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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.aspectj.asm.LinkNode;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.RelationNode;
import org.aspectj.asm.StructureNode;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;

/**
 * @author Mik Kersten
 */
class SwingTreeViewNodeRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree,
                                                    Object treeNode,
                                                    boolean sel,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus) {
        if (treeNode == null) return null;        
        SwingTreeViewNode viewNode = (SwingTreeViewNode)treeNode;
        StructureNode node = viewNode.getStructureNode();

        if (node instanceof LinkNode) {
            ISourceLocation sourceLoc = ((LinkNode)node).getProgramElementNode().getSourceLocation();
            if ((null != sourceLoc) 
                && (null != sourceLoc.getSourceFile().getAbsolutePath())) {
                setTextNonSelectionColor(AjdeWidgetStyles.LINK_NODE_COLOR);
            } else {
                setTextNonSelectionColor(AjdeWidgetStyles.LINK_NODE_NO_SOURCE_COLOR);
            }
        } else {
        	setTextNonSelectionColor(new Color(0, 0, 0));	
        }
        
        super.getTreeCellRendererComponent(tree, treeNode, sel, expanded, leaf, row, hasFocus);
        this.setFont(StructureTree.DEFAULT_FONT);
        
		if (viewNode.getIcon() != null && viewNode.getIcon().getIconResource() != null) {
			setIcon((Icon)viewNode.getIcon().getIconResource());
		} else {
			setIcon(null);
		}
        
        if (node instanceof ProgramElementNode) {
        	ProgramElementNode pNode = (ProgramElementNode)node;
        	if (pNode.isRunnable()) {
        		//setIcon(AjdeUIManager.getDefault().getIconRegistry().getExecuteIcon());
        	}	 
        	if (pNode.isImplementor()) {
        		//this.setText("<implementor>");
        	}
        	if (pNode.isOverrider()) {
        		//this.setText("<overrider>");
        	}
        } else if (node instanceof RelationNode) {
        	this.setFont(new Font(this.getFont().getName(), Font.ITALIC, this.getFont().getSize()));
        }

        if (node.getMessage() != null) {
        	if (node.getMessage().getKind().equals(IMessage.WARNING)) {
        		setIcon(AjdeUIManager.getDefault().getIconRegistry().getWarningIcon());
        	} else if (node.getMessage().getKind().equals(IMessage.ERROR)) {
        		setIcon(AjdeUIManager.getDefault().getIconRegistry().getErrorIcon());
        	} else {
        		setIcon(AjdeUIManager.getDefault().getIconRegistry().getInfoIcon());
        	}
        }
		
        return this;
    }
}

