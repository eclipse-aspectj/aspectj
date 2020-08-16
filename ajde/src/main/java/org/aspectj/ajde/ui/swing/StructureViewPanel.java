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

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.FileStructureView;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewRenderer;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * Represents the configuration of a structure view of the system, rendered
 * by the <CODE>StructureTreeManager</CODE>.
 *
 * @author Mik Kersten
 */
public class StructureViewPanel extends JPanel implements StructureViewRenderer {

	private static final long serialVersionUID = 7549744200612883786L;
	protected StructureTreeManager treeManager = new StructureTreeManager();
    protected StructureView currentView = null;
//	private java.util.List structureViews = null;

    protected Border border1;
    protected Border border2;
    JScrollPane tree_ScrollPane = new JScrollPane();
    JPanel structureToolBar_panel = null;
    BorderLayout borderLayout1 = new BorderLayout();

	public StructureViewPanel(FileStructureView structureView) {
    	currentView = structureView;
		initView(structureView);
		structureToolBar_panel = new SimpleStructureViewToolPanel(currentView);
		init();
	}

	public StructureViewPanel(java.util.List structureViews) {
//		this.structureViews = structureViews;

		for (Object structureView : structureViews) {
			initView((StructureView) structureView);
		}
		currentView = (StructureView)structureViews.get(0);
		structureToolBar_panel = new BrowserStructureViewToolPanel(structureViews, currentView, this);
		init();
	}
	
	private void init() {
		try {
			jbInit();
		} catch (Exception e) {
        	Message msg = new Message("Could not initialize view panel.",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);
		}
		updateView(currentView);
	}

	public void setCurrentView(StructureView view) {
		currentView = view;
		treeManager.updateTree(view);
	}

    public void updateView(StructureView structureView) {
    	if (structureView == currentView) {
	    	treeManager.updateTree(structureView); 
    	}  
    }

	private void initView(StructureView view) {
		view.setRenderer(this);
	}

 	public void setActiveNode(IStructureViewNode node) {
 		setActiveNode(node, 0);
 	}

	public void setActiveNode(IStructureViewNode node, int lineOffset) {
		if (node == null) return;
// 		if (!(node.getStructureNode() instanceof IProgramElement)) return;
		IProgramElement pNode = node.getStructureNode();
 		treeManager.highlightNode(pNode);
 		if (pNode.getSourceLocation() != null) {
 			Ajde.getDefault().getEditorAdapter().showSourceLine(
	 			pNode.getSourceLocation().getSourceFile().getAbsolutePath(),
	 			pNode.getSourceLocation().getLine() + lineOffset,
	 			true
	 		);
 		}
	}

 	public void highlightActiveNode() {
 		if (currentView.getActiveNode() == null) return;
 		IProgramElement node = currentView.getActiveNode().getStructureNode();
 		if (node!=null) {
 			treeManager.highlightNode(node);
 		}
 	}

	protected void jbInit() {
        border1 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(156, 156, 158),new Color(109, 109, 110));
        border2 = BorderFactory.createEmptyBorder(0,1,0,0);

        this.setLayout(borderLayout1);
        this.add(tree_ScrollPane, BorderLayout.CENTER);
        this.add(structureToolBar_panel, BorderLayout.NORTH);

        tree_ScrollPane.getViewport().add(treeManager.getStructureTree(), null);
	}
}
