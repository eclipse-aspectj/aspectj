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
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.IconRegistry;
import org.aspectj.ajde.ui.GlobalStructureView;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewProperties;
import org.aspectj.ajde.ui.StructureViewRenderer;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * Represents the configuration of a structure view of the system, rendered
 * by the <CODE>StructureTreeManager</CODE>.
 *
 * @author Mik Kersten
 */
public class BrowserViewPanel extends JPanel implements StructureViewRenderer {

	private static final long serialVersionUID = 2201330630036486567L;

	private StructureTreeManager treeManager;
    //private StructureView structureView = null;
    //private int depthSliderVal = 0;
    private JComboBox view_comboBox = null;

    private BorderLayout borderLayout1 = new BorderLayout();
    private JToolBar view_toolBar = new JToolBar();
    private JSlider depth_slider = new JSlider();
    JScrollPane tree_ScrollPane = new JScrollPane();
    JPanel tree_panel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();

//	private final StructureViewRenderer VIEW_LISTENER = new StructureViewRenderer() {
//		public void viewUpdated() {
//			updateTree();
//		}
//	};

    public BrowserViewPanel(IconRegistry icons, java.util.List views, StructureViewProperties.Hierarchy visibleViewHierarchy) {
        try {
            view_comboBox = new JComboBox(views.toArray());
			for (Object view : views) {
				StructureViewProperties.Hierarchy hierarchy = (StructureViewProperties.Hierarchy) view;
				if (hierarchy == visibleViewHierarchy) {
					view_comboBox.setSelectedItem(hierarchy);
				}
			}
            //GlobalViewProperties visibleView = (GlobalViewProperties)viewProperties.get(visibleViewHierarchy.toString());
            treeManager = new StructureTreeManager();//, visibleView);
            jbInit();
            initDepthSlider();
            tree_ScrollPane.getViewport().add(treeManager.getStructureTree(), null);

            //Ajde.getDefault().getViewManager().getFileStructureView().addListener(VIEW_LISTENER);
        }
        catch(Exception e) {
        	Message msg = new Message("Could not initialize GUI.",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);
        }
    }

	public void setActiveNode(IStructureViewNode node) {
		throw new RuntimeException("not implemented");
	}

	public void setActiveNode(IStructureViewNode activeNode, int lineOffset) {
		throw new RuntimeException("not implemented");
	}

//    public void highlightNode(ProgramElementNode node) {
//        treeManager.navigationAction(node, true, true);
//    }

//    void updateTree() {
//		StructureViewProperties.Hierarchy hierarchy = ((StructureViewProperties.Hierarchy)view_comboBox.getSelectedItem());
//		GlobalStructureView structureView = Ajde.getDefault().getStructureViewManager().getGlobalStructureView(hierarchy);
//    	treeManager.updateTree(structureView, depthSliderVal);
//    }

    public void updateView(StructureView structureView) {
    	if (structureView instanceof GlobalStructureView) {
    		treeManager.updateTree(structureView);
    	}
    }

    void updateTree(String filePath) {
    	//treeManager.updateTree(Ajde.getDefault().getViewManager().getFileStructureView(filePath));
    }

    private void initDepthSlider() {
        depth_slider.setMinimum(0);
        depth_slider.setMaximum(9);
        depth_slider.setMinorTickSpacing(1);
        depth_slider.setValue(9);
        depth_slider.setSnapToTicks(true);
        depth_slider.setPaintTrack(true);
        depth_slider.setPaintTicks(true);
//        this.depth_slider.addChangeListener(
//            new ChangeListener() {
//                public void stateChanged(ChangeEvent e) {
//                    depthSliderVal = depth_slider.getValue();
//                    //AjdeUIManager.getDefault().getViewManager().updateView();
//                }
//            });
//        depthSliderVal = depth_slider.getValue();
    }

    private void view_comboBox_actionPerformed(ActionEvent e) {
        //updateTree(DECLARATION_VIEW);
        throw new RuntimeException("not implemented");
    }

    private void jbInit() throws Exception {
        tree_panel.setLayout(borderLayout2);
        this.setLayout(borderLayout1);
        view_comboBox.setPreferredSize(new Dimension(200, 20));
        view_comboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view_comboBox_actionPerformed(e);
            }
        });
        view_comboBox.setMinimumSize(new Dimension(40, 20));
        view_comboBox.setFont(new java.awt.Font("SansSerif", 0, 11));
        depth_slider.setMaximumSize(new Dimension(32767, 25));
        depth_slider.setToolTipText("");
        depth_slider.setMinimumSize(new Dimension(30, 20));
        depth_slider.setBorder(null);
        depth_slider.setPreferredSize(new Dimension(30, 25));
        depth_slider.setMaximum(3);
        depth_slider.setPaintTicks(true);
        depth_slider.setValue(1);
        depth_slider.setPaintLabels(true);
        view_toolBar.setFloatable(false);
        this.add(view_toolBar, BorderLayout.NORTH);
        view_toolBar.add(view_comboBox, null);
        view_toolBar.add(depth_slider, null);
        this.add(tree_panel,  BorderLayout.CENTER);
        tree_panel.add(tree_ScrollPane,  BorderLayout.CENTER);
    }
}
