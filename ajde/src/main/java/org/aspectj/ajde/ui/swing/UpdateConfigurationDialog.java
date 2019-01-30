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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

import org.aspectj.ajde.Ajde;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * Used for automatically updating build configuration files (".lst") when a
 * project configuration has changed.
 *
 * @author Mik Kersten
 */
public class UpdateConfigurationDialog extends JFrame {
	private static final long serialVersionUID = 5885112642841314728L;
//	private Vector buildConfigFiles;
//    private Vector filesToUpdate;
//    private boolean addToConfiguration;

    private String message1 = " Project has been updated.";
    private String message2 = " File list below.";
    private Box box5;
    private JButton cancel_button = new JButton();
    private JButton ok_button = new JButton();
    private FlowLayout flowLayout1 = new FlowLayout();
    private JPanel globalButton_panel = new JPanel();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private TitledBorder titledBorder1;
    private TitledBorder titledBorder2;
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    private JPanel jPanel3 = new JPanel();
    private GridLayout gridLayout1 = new GridLayout();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JList updatedFilesList = new JList();
    private JScrollPane jScrollPane2 = new JScrollPane();
    private JList buildConfigList = new JList();
    private Box box2;
    private JLabel messageLabel1 = new JLabel();
    private JLabel messageLabel2 = new JLabel();

    public UpdateConfigurationDialog(Vector filesToUpdate, Vector buildConfigFiles, boolean addToConfiguration, Component parentComponent) {
        try {
//            this.buildConfigFiles = buildConfigFiles;
//            this.filesToUpdate = filesToUpdate;
//            this.addToConfiguration = addToConfiguration;

            updatedFilesList.setListData(filesToUpdate);
            String action = "removed from";
            if (addToConfiguration) action = "added to";
            message1 = " Files have been " + action + " the project, which will affect the build configurations\n";
            message2 = " listed below.  These build configurations listed can be updated automatically.";

            //buildConfigList.setCellRenderer(new CheckListCellRenderer());
            //CheckListener listener = new CheckListener(buildConfigList);
            buildConfigList.setListData(buildConfigFiles);

            jbInit();

            this.doLayout();
            this.setTitle("Build Configuration Update");
            this.setSize(500, 320);

            // center it
            int posX = parentComponent.getX() + 100;//(parentComponent.getWidth()/2);
            int posY = parentComponent.getY() + 100;//(parentComponent.getHeight()/2);
            this.setLocation(posX, posY);
        }
        catch(Exception e) {
        	Message msg = new Message("Could not open configuration dialog",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);
        }
    }

    void cancel_button_actionPerformed(ActionEvent e) {
        this.dispose();
    }
    void ok_button_actionPerformed(ActionEvent e) {
//        Object[] selected = buildConfigList.getSelectedValues();
//        //LstBuildConfigFileUpdater.updateBuildConfigFiles(buildConfigFiles, filesToUpdate, addToConfiguration);
        this.dispose();
    }

    private void jbInit() throws Exception {
        box5 = Box.createVerticalBox();
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),"Project Files Added/Removed");
        titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),"Build Configurations Affected");
        box2 = Box.createVerticalBox();
        this.getContentPane().setLayout(borderLayout1);
        cancel_button.setText("Cancel");
        cancel_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancel_button_actionPerformed(e);
            }
        });
        ok_button.setText("Update Selected Configurations");
        ok_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ok_button_actionPerformed(e);
            }
        });
        globalButton_panel.setLayout(flowLayout1);
        jPanel1.setLayout(borderLayout3);
        jPanel1.setBorder(titledBorder1);
        jPanel1.setMinimumSize(new Dimension(154, 20));
        jPanel1.setPreferredSize(new Dimension(154, 50));
        jPanel2.setBorder(titledBorder2);
        jPanel2.setMinimumSize(new Dimension(36, 50));
        jPanel2.setPreferredSize(new Dimension(272, 50));
        jPanel2.setLayout(borderLayout2);
        globalButton_panel.setMinimumSize(new Dimension(297, 37));
        globalButton_panel.setPreferredSize(new Dimension(297, 37));
        jPanel3.setLayout(gridLayout1);
        jPanel3.setMinimumSize(new Dimension(154, 20));
        jPanel3.setPreferredSize(new Dimension(154, 40));
        buildConfigList.setBorder(BorderFactory.createLoweredBevelBorder());
        messageLabel1.setText(message1);
        messageLabel2.setText(message2);
        updatedFilesList.setFont(new java.awt.Font("Dialog", 0, 10));
        this.getContentPane().add(box5, BorderLayout.CENTER);
        box5.add(jPanel3, null);
        jPanel3.add(box2, null);
        box2.add(messageLabel1, null);
        box2.add(messageLabel2, null);
        box5.add(jPanel1, null);
        jPanel1.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(updatedFilesList, null);
        box5.add(jPanel2, null);
        jPanel2.add(jScrollPane2, BorderLayout.CENTER);
        jScrollPane2.getViewport().add(buildConfigList, null);
        box5.add(globalButton_panel, null);
        globalButton_panel.add(ok_button, null);
        globalButton_panel.add(cancel_button, null);
    }
}

class CheckListCellRenderer extends JCheckBox implements ListCellRenderer {
	private static final long serialVersionUID = -9183012434083509581L;

	public CheckListCellRenderer() {
        super();
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
            setText(value.toString());
            setFont(new java.awt.Font("Dialog", 0, 10));
            setBackground(new Color(255, 255, 255));

            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setSelected(isSelected);

            return this;
    }
}

class CheckListener implements MouseListener, KeyListener {

    protected JList list;

    public CheckListener(JList list) {
        this.list = list;
    }

    public void mouseClicked(MouseEvent e) {
        doCheck();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == ' ') doCheck();
    }

    protected void doCheck() {
        int index = list.getSelectedIndex();
        if (index < 0) {
            return;
        }
        list.getModel().getElementAt(index);
        list.repaint();
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) { }
}
