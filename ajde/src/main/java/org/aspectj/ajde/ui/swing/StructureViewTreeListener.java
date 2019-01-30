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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.IStructureViewNode;

/**
 * @author  Mik Kersten
 */
class StructureViewTreeListener implements TreeSelectionListener, MouseListener {
    private StructureTree tree;
	private SwingTreeViewNode lastSelectedNode = null;

    public StructureViewTreeListener(StructureTree tree) {
        this.tree = tree;
    }

    public void valueChanged(TreeSelectionEvent e) { }

    public void mouseEntered(MouseEvent e) { }

    public void mouseExited(MouseEvent e) { }

    public void mousePressed(MouseEvent e) { }

    public void mouseReleased(MouseEvent e) { }

    public void mouseClicked(MouseEvent e) {
        navigate(e);
    }

    public void navigate(MouseEvent e) {
        SwingTreeViewNode treeNode = (SwingTreeViewNode)tree.getLastSelectedPathComponent();
		if (treeNode == null || lastSelectedNode == treeNode) return;
		lastSelectedNode = treeNode;
		
		//if (e.getClickCount() == 2) {
		Ajde.getDefault().getStructureViewManager().fireNavigationAction(
			treeNode.getStructureNode(),
			treeNode.getKind() == IStructureViewNode.Kind.LINK
		);
		//}
    }   
}


