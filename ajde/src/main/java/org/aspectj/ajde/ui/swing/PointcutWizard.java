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

import java.awt.BorderLayout;
//import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//import org.aspectj.asm.IRelationship;
import org.aspectj.ajde.ui.StructureViewProperties;

/**
 * @author Mik Kersten
 */
class PointcutWizard extends JFrame {
 	private static final long serialVersionUID = -9058319919402871975L;
//	private BrowserViewPanel typeTreeView = null;
//    private java.util.List signatures = null;

    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    JPanel jPanel4 = new JPanel();
    JLabel jLabel1 = new JLabel();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JLabel jLabel4 = new JLabel();
    JPanel jPanel3 = new JPanel();
    JCheckBox jCheckBox5 = new JCheckBox();
    JCheckBox jCheckBox4 = new JCheckBox();
    JCheckBox jCheckBox3 = new JCheckBox();
    JCheckBox jCheckBox2 = new JCheckBox();
    JCheckBox jCheckBox1 = new JCheckBox();
    JButton cancel_button = new JButton();
    JButton ok_button = new JButton();
    JPanel jPanel5 = new JPanel();

    public PointcutWizard(java.util.List signatures) {
 //       this.signatures = signatures;
        List views = new ArrayList();
        views.add(StructureViewProperties.Hierarchy.INHERITANCE);
//        typeTreeView = new BrowserViewPanel(AjdeUIManager.getDefault().getIconRegistry(), views, StructureViewProperties.Hierarchy.INHERITANCE);
        
        throw new RuntimeException("unimplemented, can't get the current file");
        //typeTreeView.updateTree(Ajde.getDefault().getEditorManager().getCurrFile());
//        try {
//            jbInit();
//        }
//        catch(Exception e) {
//            Ajde.getDefault().getErrorHandler().handleError("Could not initialize GUI.", e);
//        }
//        this.setSize(400, 400);
//        this.setIconImage(((ImageIcon)AjdeUIManager.getDefault().getIconRegistry().getStructureSwingIcon(ProgramElementNode.Kind.POINTCUT)).getImage());
    }

//    private Map getViewProperties() {
//        Map views = new HashMap();
//        GlobalViewProperties INHERITANCE_VIEW = new GlobalViewProperties(StructureViewProperties.Hierarchy.INHERITANCE);
////        INHERITANCE_VIEW.addRelation(IRelationship.Kind.INHERITANCE);
////        views.put(INHERITANCE_VIEW.toString(), INHERITANCE_VIEW);
//        return views;
//    }
//
//    private void jbInit() throws Exception {
//        jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
//        jLabel1.setText("Generate pointcut designator for corresponding joinpoints:");
//        jPanel1.setLayout(borderLayout1);
//        jPanel4.setLayout(borderLayout2);
//        jPanel2.setLayout(borderLayout3);
//        jLabel4.setText("Select the target type that will host the generated pointcut:");
//        jLabel4.setFont(new java.awt.Font("Dialog", 0, 11));
//        jLabel4.setToolTipText("");
//        jPanel3.setMaximumSize(new Dimension(32767, 34));
//        jCheckBox5.setEnabled(false);
//        jCheckBox5.setFont(new java.awt.Font("Dialog", 0, 11));
//        jCheckBox5.setSelected(true);
//        jCheckBox5.setText("call");
//        jCheckBox4.setEnabled(false);
//        jCheckBox4.setFont(new java.awt.Font("Dialog", 0, 11));
//        jCheckBox4.setText("execution");
//        jCheckBox3.setEnabled(false);
//        jCheckBox3.setFont(new java.awt.Font("Dialog", 0, 11));
//        jCheckBox3.setText("initialization");
//        jCheckBox2.setEnabled(false);
//        jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 11));
//        jCheckBox2.setText("static initialization");
//        jCheckBox1.setEnabled(false);
//        jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 11));
//        jCheckBox1.setText("field get/set");
//        cancel_button.setFont(new java.awt.Font("Dialog", 0, 11));
//        cancel_button.setText("Cancel");
//        cancel_button.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                cancel_button_actionPerformed(e);
//            }
//        });
//        ok_button.setText("OK");
//        ok_button.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                ok_button_actionPerformed(e);
//            }
//        });
//        ok_button.setFont(new java.awt.Font("Dialog", 0, 11));
//        this.setTitle("Pointcut Wizard");
//        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
//        jPanel1.add(jPanel4, BorderLayout.NORTH);
//        jPanel4.add(jLabel1,  BorderLayout.NORTH);
//        jPanel4.add(jPanel3, BorderLayout.CENTER);
//        jPanel3.add(jCheckBox5, null);
//        jPanel3.add(jCheckBox4, null);
//        jPanel3.add(jCheckBox3, null);
//        jPanel3.add(jCheckBox2, null);
//        jPanel3.add(jCheckBox1, null);
//        jPanel1.add(jPanel2,  BorderLayout.CENTER);
//        jPanel2.add(jLabel4, BorderLayout.NORTH);
//        jPanel2.add(typeTreeView,  BorderLayout.CENTER);
//        jPanel1.add(jPanel5,  BorderLayout.SOUTH);
//        jPanel5.add(ok_button, null);
//        jPanel5.add(cancel_button, null);
//    }

//    private void ok_button_actionPerformed(ActionEvent e) {
//		throw new RuntimeException("unimplemented, can't paste");
////        Ajde.getDefault().getEditorManager().pasteToCaretPos(generatePcd());
////        this.dispose();
//    }
//
//    private void cancel_button_actionPerformed(ActionEvent e) {
//        this.dispose();
//    }
//
//    private String generatePcd() {
//        String pcd = "\n\n" +
//            "    pointcut temp(): \n";
//        for (Iterator it = signatures.iterator(); it.hasNext(); ) {
//            pcd += "        call(* " + it.next() + ")";
//            if (it.hasNext()) {
//                pcd += " ||";
//            } else {
//                pcd += ";";
//            }
//            pcd += "\n";
//        }
//        return pcd;
//    }
}

