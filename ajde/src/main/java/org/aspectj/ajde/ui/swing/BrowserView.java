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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.IconRegistry;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * @author Mik Kersten
 */
class BrowserView extends JPanel {
	private static final long serialVersionUID = 1L;
//	private BrowserViewPanel masterView;
    private BrowserViewPanel slaveView;
    private boolean slaveViewVisible = false;
    private String lastSelectedConfig = "";

    private IconRegistry icons = null;
    private BorderLayout borderLayout1 = new BorderLayout();
    private Border default_border;
    private JPanel toolBar_panel = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();
    JPanel mainToolBar_panel = new JPanel();
    JToolBar config_toolBar = new JToolBar();
    JComboBox configs_comboBox = null;
    BorderLayout borderLayout3 = new BorderLayout();
    JToolBar nav_toolBar = new JToolBar();
    JButton forward_button = new JButton();
    JButton back_button = new JButton();
    GridLayout gridLayout1 = new GridLayout();
    JSplitPane views_splitPane = new JSplitPane();
    JToolBar command_toolBar = new JToolBar();
    JToggleButton splitView_button = new JToggleButton();
    JToggleButton zoomToFile_button = new JToggleButton();
    JButton joinpointProbe_button = new JButton();

    public BrowserView(BrowserViewPanel masterView, BrowserViewPanel slaveView, IconRegistry icons) {
        try {
 //           this.masterView = masterView;
            this.slaveView = slaveView;
            this.icons = icons;
            configs_comboBox = new JComboBox(Ajde.getDefault().getBuildConfigManager().getAllBuildConfigFiles().toArray());
            configs_comboBox.setRenderer(new ConfigsCellRenderer());
//            configs_comboBox.addItemListener(new ItemListener() {
//            	public void itemStateChanged(ItemEvent e) {
//            		Ajde.getDefault().getConfigurationManager().setCurrConfigFile(lastSelectedConfig);	
//            	}
//            });
            
            if (Ajde.getDefault().getBuildConfigManager().getAllBuildConfigFiles().size() > 0) {
            	Ajde.getDefault().getBuildConfigManager().setActiveConfigFile((String)Ajde.getDefault().getBuildConfigManager().getAllBuildConfigFiles().get(0));	
            }
            
            jbInit();
            fixButtonBorders();
            views_splitPane.add(masterView, JSplitPane.TOP);
            views_splitPane.add(slaveView, JSplitPane.BOTTOM);
            setSlaveViewVisible(false);

            nav_toolBar.remove(joinpointProbe_button);
        } catch(Exception e) {
        	Message msg = new Message("Could not initialize GUI.",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);

        }
    }

    public void setSlaveViewVisible(boolean visible) {
        slaveViewVisible = visible;
        if (visible) {
            views_splitPane.add(slaveView, JSplitPane.BOTTOM);
            views_splitPane.setDividerLocation(this.getHeight()-250);
            //masterView.scrollToHighlightedNode();
        } else {
            views_splitPane.remove(slaveView);
            views_splitPane.setDividerLocation(this.getHeight());
        }
    }

    public boolean isSlaveViewVisible() {
        return slaveViewVisible;
    }

    public void updateConfigs(java.util.List configsList) {
        configs_comboBox.removeAllItems();
		for (Object o : configsList) {
			configs_comboBox.addItem(o);
		}
    }

    public void setSelectedConfig(String config) {
        for (int i = 0; i < configs_comboBox.getItemCount(); i++) {
            if (configs_comboBox.getItemAt(i).equals(config)) {
                configs_comboBox.setSelectedIndex(i);
            }
        }
    }

    public String getSelectedConfig() {
        return (String)configs_comboBox.getSelectedItem();
    }

    /**
     * @todo    get rid of this method and make the GUI-designer generated code work properly
     */
    private void fixButtonBorders() {
        back_button.setBorder(BorderFactory.createEmptyBorder());
        forward_button.setBorder(BorderFactory.createEmptyBorder());
        zoomToFile_button.setBorder(BorderFactory.createEmptyBorder());
        splitView_button.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.blue));
    }

    private void jbInit() throws Exception {
        default_border = BorderFactory.createEmptyBorder();
        this.setLayout(borderLayout1);
        toolBar_panel.setLayout(borderLayout2);
        toolBar_panel.setBorder(BorderFactory.createEtchedBorder());
        config_toolBar.setBorder(default_border);
        config_toolBar.setFloatable(false);
        configs_comboBox.setPreferredSize(new Dimension(200, 20));
        configs_comboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configs_comboBox_actionPerformed(e);
            }
        });
        configs_comboBox.setMinimumSize(new Dimension(40, 20));
        configs_comboBox.setFont(new java.awt.Font("SansSerif", 0, 11));
        mainToolBar_panel.setLayout(borderLayout3);
        nav_toolBar.setFloatable(false);
        nav_toolBar.setBorder(default_border);
        forward_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                forward_button_actionPerformed(e);
            }
        });
        forward_button.setIcon(icons.getForwardIcon());
        forward_button.setToolTipText("Navigate forward");
        forward_button.setPreferredSize(new Dimension(20, 20));
        forward_button.setMinimumSize(new Dimension(20, 20));
        forward_button.setBorder(default_border);
        forward_button.setMaximumSize(new Dimension(24, 20));
        back_button.setMaximumSize(new Dimension(24, 20));
        back_button.setBorder(default_border);
        back_button.setMinimumSize(new Dimension(20, 20));
        back_button.setPreferredSize(new Dimension(20, 20));
        back_button.setToolTipText("Navigate back");
        back_button.setIcon(icons.getBackIcon());
        back_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                back_button_actionPerformed(e);
            }
        });
//        structureViews_box.add(comment_editorPane, null);
        views_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        views_splitPane.setDividerSize(2);
        command_toolBar.setBorder(default_border);
        command_toolBar.setFloatable(false);
        splitView_button.setFont(new java.awt.Font("Dialog", 0, 11));
        splitView_button.setBorder(default_border);
        splitView_button.setMaximumSize(new Dimension(24, 24));
        splitView_button.setPreferredSize(new Dimension(20, 20));
        splitView_button.setToolTipText("Togge split-tree view mode");
        splitView_button.setIcon(icons.getSplitStructureViewIcon());
        splitView_button.setSelectedIcon(icons.getMergeStructureViewIcon());
        splitView_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //splitView_button_actionPerformed(e);
            }
        });
        zoomToFile_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //zoomToFile_button_actionPerformed(e);
            }
        });
        zoomToFile_button.setIcon(icons.getZoomStructureToFileModeIcon());
        zoomToFile_button.setSelectedIcon(icons.getZoomStructureToGlobalModeIcon());
        zoomToFile_button.setBorder(BorderFactory.createRaisedBevelBorder());
        zoomToFile_button.setMaximumSize(new Dimension(24, 24));
        zoomToFile_button.setPreferredSize(new Dimension(20, 20));
        zoomToFile_button.setToolTipText("Toggle file-view mode");
        zoomToFile_button.setFont(new java.awt.Font("Dialog", 0, 11));
        joinpointProbe_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                joinpointProbe_button_actionPerformed(e);
            }
        });
        joinpointProbe_button.setIcon(icons.getStructureSwingIcon(IProgramElement.Kind.POINTCUT));
        joinpointProbe_button.setToolTipText("Create joinpoint probe");
        joinpointProbe_button.setPreferredSize(new Dimension(20, 20));
        joinpointProbe_button.setMinimumSize(new Dimension(20, 20));
        joinpointProbe_button.setBorder(default_border);
        joinpointProbe_button.setMaximumSize(new Dimension(24, 20));
        this.add(toolBar_panel,  BorderLayout.NORTH);
        toolBar_panel.add(mainToolBar_panel, BorderLayout.NORTH);
        mainToolBar_panel.add(config_toolBar, BorderLayout.CENTER);
        config_toolBar.add(configs_comboBox, null);
        mainToolBar_panel.add(nav_toolBar,  BorderLayout.EAST);
        nav_toolBar.add(splitView_button, null);
        nav_toolBar.add(zoomToFile_button, null);
        nav_toolBar.add(joinpointProbe_button, null);
        nav_toolBar.add(back_button, null);
        nav_toolBar.add(forward_button, null);
        mainToolBar_panel.add(command_toolBar,  BorderLayout.WEST);
        this.add(views_splitPane,  BorderLayout.CENTER);
        views_splitPane.setDividerLocation(400);
    }

    void forward_button_actionPerformed(ActionEvent e) {
        //AjdeUIManager.getDefault().getViewManager().navigateForwardAction();
    }
    void back_button_actionPerformed(ActionEvent e) {
        //AjdeUIManager.getDefault().getViewManager().navigateBackAction();
    }

//    void splitView_button_actionPerformed(ActionEvent e) {
//        AjdeUIManager.getDefault().getViewManager().setSplitViewMode(!slaveViewVisible);
//    }

    static class ConfigsCellRenderer extends JLabel implements ListCellRenderer {

    		private static final long serialVersionUID = 8795959045339903340L;

		public ConfigsCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list,
                                                        Object value,
                                                        int index,
                                                        boolean isSelected,
                                                        boolean cellHasFocus) {
            if (value == null) return this;

            java.io.File file = new File(value.toString());
            setText(file.getName());
            setBackground(isSelected ? Color.gray : Color.lightGray);
//            setForeground(isSelected ? Color.lightGray : Color.gray);
            return this;
        }
    }

    void configDesigner_button_mouseClicked(MouseEvent e) {

    }
    void configDesigner_button_mousePressed(MouseEvent e) {

    }
    void configDesigner_button_mouseReleased(MouseEvent e) {

    }
    void configDesigner_button_mouseEntered(MouseEvent e) {

    }
    void configDesigner_button_mouseExited(MouseEvent e) {

    }
    void configDesigner_button_actionPerformed(ActionEvent e) {

    }
    void viewManager_button_mouseClicked(MouseEvent e) {

    }
    void viewManager_button_mousePressed(MouseEvent e) {

    }
    void viewManager_button_mouseReleased(MouseEvent e) {

    }
    void viewManager_button_mouseEntered(MouseEvent e) {

    }
    void viewManager_button_mouseExited(MouseEvent e) {

    }
    void viewManager_button_actionPerformed(ActionEvent e) {

    }

//    void zoomToFile_button_actionPerformed(ActionEvent e) {
//        AjdeUIManager.getDefault().getViewManager().setGlobalMode(!AjdeUIManager.getDefault().getViewManager().isGlobalMode());
//        AjdeUIManager.getDefault().getViewManager().updateView();
//    }

    void configs_comboBox_actionPerformed(ActionEvent e) {
        if (configs_comboBox.getSelectedItem() != null) {
            if (!configs_comboBox.getSelectedItem().toString().equals(lastSelectedConfig)) {
                //TopManager.INSTANCE.VIEW_MANAGER.readStructureView();
                lastSelectedConfig = configs_comboBox.getSelectedItem().toString();
                Ajde.getDefault().getBuildConfigManager().setActiveConfigFile(lastSelectedConfig);
            }
        }
    }

    private void joinpointProbe_button_actionPerformed(ActionEvent e) {
        //joinpointProbeWizard();
    }
}
