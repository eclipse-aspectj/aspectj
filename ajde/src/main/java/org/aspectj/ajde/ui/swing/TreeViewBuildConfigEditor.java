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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.BuildConfigEditor;
import org.aspectj.ajde.ui.BuildConfigModel;
import org.aspectj.ajde.ui.BuildConfigNode;
import org.aspectj.ajde.ui.InvalidResourceException;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
/**
 * UI for editing build configuration (".lst") files via a graphical tree-based
 * representation.
 *
 * @author Mik Kersten
 */
public class TreeViewBuildConfigEditor extends JPanel implements BuildConfigEditor {

	private static final long serialVersionUID = 8071799814661969685L;
	private ConfigTreeNode root;
//	private ConfigTreeNode currNode;
	private BuildConfigModel model = null;
	
    private static java.util.List selectedEntries = new ArrayList();
//    private String configFile = null;
//    private File sourcePath = null;
    //private BuildConfigModelBuilder configTreeBuilder = new BuildConfigModelBuilder();
    
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JLabel jLabel1 = new JLabel();
    JPanel jPanel2 = new JPanel();
    JButton cancel_button = new JButton();
    BorderLayout borderLayout2 = new BorderLayout();
    JButton save_button = new JButton();
    JScrollPane jScrollPane = new JScrollPane();
    JTree buildConfig_tree = new JTree();

    public void openFile(String configFile) throws IOException, InvalidResourceException {
        try {
            if (configFile == null) {
            	Message msg = new Message("No structure is selected for editing.",IMessage.ERROR,null,null);
            	Ajde.getDefault().getMessageHandler().handleMessage(msg);
                return;
            }
 //           this.configFile = configFile;
 //           sourcePath = new File(new File(configFile).getParent());
            jbInit();
            jLabel1.setText(" Build configuration: " + configFile);

            model = Ajde.getDefault().getBuildConfigManager().buildModel(configFile);            
			root = buildTree(model.getRoot());

            buildConfig_tree.setModel(new DefaultTreeModel(root));
            buildConfig_tree.addMouseListener(new ConfigFileMouseAdapter(buildConfig_tree));
            buildConfig_tree.setCellRenderer(new ConfigTreeCellRenderer());

            for (int j = 0; j < buildConfig_tree.getRowCount(); j++) {
                buildConfig_tree.expandPath(buildConfig_tree.getPathForRow(j));
            }
        } catch(Exception e) {
        	Message msg = new Message("Could not open file.",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);
        }
    }
    
    private ConfigTreeNode buildTree(BuildConfigNode node) {
    	ConfigTreeNode treeNode = new ConfigTreeNode(node);
		for (BuildConfigNode childNode : node.getChildren()) {
			treeNode.add(buildTree(childNode));
		}
    	return treeNode;
    }	
    
    private void saveModel() {
    	Ajde.getDefault().getBuildConfigManager().writeModel(model);
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabel1.setMaximumSize(new Dimension(80, 30));
        jLabel1.setMinimumSize(new Dimension(80, 20));
        jLabel1.setPreferredSize(new Dimension(80, 20));
        jLabel1.setText("Config File Editor");
        cancel_button.setFont(new java.awt.Font("Dialog", 0, 11));
        cancel_button.setMaximumSize(new Dimension(73, 20));
        cancel_button.setMinimumSize(new Dimension(73, 20));
        cancel_button.setPreferredSize(new Dimension(73, 20));
        cancel_button.setText("Cancel");
        cancel_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel_button_actionPerformed(e);
            }
        });
        jPanel1.setLayout(borderLayout2);
        save_button.setText("Save");
        save_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save_button_actionPerformed(e);
            }
        });
        save_button.setPreferredSize(new Dimension(73, 20));
        save_button.setMinimumSize(new Dimension(73, 20));
        save_button.setMaximumSize(new Dimension(73, 20));
        save_button.setFont(new java.awt.Font("Dialog", 0, 11));
        this.add(jPanel1,  BorderLayout.NORTH);
        jPanel1.add(jPanel2,  BorderLayout.EAST);
        jPanel2.add(save_button, null);
        //jPanel2.add(cancel_button, null);
        jPanel1.add(jLabel1, BorderLayout.CENTER);
        this.add(jScrollPane,  BorderLayout.CENTER);
        jScrollPane.getViewport().add(buildConfig_tree, null);
    }

    private static class ConfigTreeNode extends DefaultMutableTreeNode {
       
		private static final long serialVersionUID = 1L;
		public JCheckBox checkBox = null;
        public BuildConfigNode modelNode;

        public ConfigTreeNode(BuildConfigNode modelNode) {
            super(modelNode.getName(), true);
            this.modelNode = modelNode;
            checkBox = new JCheckBox();
        }
		public BuildConfigNode getModelNode() {
			return modelNode;
		}

		public void setModelNode(BuildConfigNode modelNode) {
			this.modelNode = modelNode;
		}

    }

    private static class ConfigFileMouseAdapter extends MouseAdapter {
        private JTree tree = null;
        final JCheckBox checkBoxProto = new JCheckBox();
        final int width = checkBoxProto.getPreferredSize().width;

        public ConfigFileMouseAdapter(JTree tree) {
            super();
            this.tree = tree;
        }

        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            TreePath path = tree.getClosestPathForLocation(x,y);
            ConfigTreeNode node = (ConfigTreeNode)path.getLastPathComponent();

            // if (isCheckBox(x, tree.getPathBounds(path).x)) {
            if (node.checkBox.isSelected()) {
            	node.getModelNode().setActive(false);
	           	node.checkBox.setSelected(false);	
            } else {
            	node.getModelNode().setActive(true);
            	node.checkBox.setSelected(true);	
            }
            
            ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
            if (node.getModelNode().getName() != null) {
                if (node.checkBox.isSelected()) {
                    selectedEntries.add(node.getModelNode().getName());
                } else {
                    selectedEntries.remove(node.getModelNode().getName());
                }
            }
            super.mousePressed(e);

        }

        boolean isCheckBox(int x, int x_) {
            int d = x - x_;
            return (d < width) && (d > 0);
        }
    }

    static class ConfigTreeCellRenderer extends DefaultTreeCellRenderer {
 		private static final long serialVersionUID = -3120665318910899066L;

		public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                               leaf, row, hasFocus);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.setBackground(Color.white);
            //if (leaf)  {
            setFont(new Font("Dialog", Font.PLAIN, 11));
            final JCheckBox cbox = ((ConfigTreeNode)value).checkBox;
            cbox.setBackground(Color.white);
            if (row != 0) {
	            p.add(cbox);  
            }

            ConfigTreeNode ctn = (ConfigTreeNode)value;
            //if (TreeViewBuildConfigEditor.selectedEntries.contains(ctn.getSourceFile())) {
            if (ctn.getModelNode().isActive()) {
                cbox.setSelected(true);
            }   
            
            if (!ctn.getModelNode().isValidResource()) {
            	ctn.checkBox.setEnabled(false);	
            }
            
            //}
            BuildConfigNode.Kind kind = ctn.getModelNode().getBuildConfigNodeKind();
            if (kind.equals(BuildConfigNode.Kind.FILE_ASPECTJ)) {
            	setIcon(Ajde.getDefault().getIconRegistry().getStructureSwingIcon(IProgramElement.Kind.FILE_ASPECTJ));	
            } else if (kind.equals(BuildConfigNode.Kind.FILE_JAVA)) {
            	setIcon(Ajde.getDefault().getIconRegistry().getStructureSwingIcon(IProgramElement.Kind.FILE_JAVA));	
            } else if (kind.equals(BuildConfigNode.Kind.FILE_LST)) {
            	setIcon(Ajde.getDefault().getIconRegistry().getStructureSwingIcon(IProgramElement.Kind.FILE_LST));	
            } else if (kind.equals(BuildConfigNode.Kind.DIRECTORY)) {
            	setIcon(Ajde.getDefault().getIconRegistry().getStructureSwingIcon(IProgramElement.Kind.PACKAGE));	
            } else {
            	setIcon((Icon)Ajde.getDefault().getIconRegistry().getIcon(IProgramElement.Kind.ERROR).getIconResource());	
            	p.remove(cbox);
            }
           
//            if (ctn.getModelNode().getResourcePath() != null) {
//	            if (ctn.getModelNode().getResourcePath().endsWith(".java")) {
//	            	this.setIcon(AjdeUIManager.getDefault().getIconRegistry().getStructureSwingIcon(ProgramElementNode.Kind.CLASS));	
//	            } else if (ctn.getModelNode().getResourcePath().endsWith(".aj")) {
//	            	this.setIcon(AjdeUIManager.getDefault().getIconRegistry().getStructureSwingIcon(ProgramElementNode.Kind.ASPECT));	
//	            } else {
//	            	this.setIcon(AjdeUIManager.getDefault().getIconRegistry().getStructureSwingIcon(ProgramElementNode.Kind.PACKAGE));	
//	            }
//            }

            p.add(this);
            return p;
        }
    }

    void cancel_button_actionPerformed(ActionEvent e) {
        //resetEditorFrame();
    }

    void save_button_actionPerformed(ActionEvent e) {
        saveModel();
        //resetEditorFrame();
    }

//    private void resetEditorFrame() {
//        BrowserManager.getDefault().resetEditorFrame();
//    }
}

