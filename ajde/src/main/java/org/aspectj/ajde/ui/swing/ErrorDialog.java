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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class ErrorDialog extends JDialog {
	private static final long serialVersionUID = 5646564514289861666L;
	JPanel top_panel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel button_panel = new JPanel();
    JButton close_button = new JButton();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea stackTrace_textArea = new JTextArea();
    JPanel jPanel1 = new JPanel();
    JLabel error_label1 = new JLabel();
    JLabel error_label2 = new JLabel();
    BorderLayout borderLayout1 = new BorderLayout();
    Border border1;
    BorderLayout borderLayout3 = new BorderLayout();

    public ErrorDialog(Frame owner, String title, Throwable throwable, String message, String details) {
        super(owner, title, true);
        try {
            jbInit();
            String exceptionName = "<unknown exception>";
            if (throwable != null) exceptionName = throwable.getClass().getName();
            this.error_label1.setText("Exception: " + exceptionName);
            this.error_label2.setText("If you can't fix it, please submit a bug to http://dev.eclipse.org/bugs");
            this.stackTrace_textArea.setText("Message: " + message + '\n' + "Stack trace: " + details);
            this.setSize(420, 330);
            this.setLocationRelativeTo(owner);
            this.getContentPane().setLayout(borderLayout1);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        border1 = BorderFactory.createEmptyBorder(5,5,5,5);
        this.getContentPane().setLayout(borderLayout1);
        top_panel.setLayout(borderLayout2);
        close_button.setFont(new java.awt.Font("Dialog", 0, 11));
        close_button.setText("Close");
        close_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close_button_actionPerformed(e);
            }
        });
        top_panel.setBorder(border1);
        top_panel.setPreferredSize(new Dimension(400, 290));
        stackTrace_textArea.setFont(new java.awt.Font("Monospaced", 0, 11));
        error_label1.setFont(new java.awt.Font("Dialog", 0, 11));
        error_label1.setFont(new java.awt.Font("Dialog", 0, 11));
        error_label1.setForeground(Color.black);
        error_label1.setMaximumSize(new Dimension(400, 16));
        error_label1.setPreferredSize(new Dimension(390, 16));
        error_label1.setText("label1");
        jPanel1.setLayout(borderLayout3);
        error_label2.setFont(new java.awt.Font("Dialog", 0, 11));
        error_label2.setMaximumSize(new Dimension(400, 16));
        error_label2.setPreferredSize(new Dimension(390, 16));
        error_label2.setText("label2");
        error_label2.setForeground(Color.black);
        jPanel1.setPreferredSize(new Dimension(600, 44));
        this.getContentPane().add(top_panel, BorderLayout.CENTER);
        top_panel.add(button_panel,  BorderLayout.SOUTH);
        button_panel.add(close_button, null);
        top_panel.add(jScrollPane1,  BorderLayout.CENTER);
        top_panel.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(error_label1, BorderLayout.NORTH);
        jPanel1.add(error_label2, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(stackTrace_textArea, null);
    }

    void close_button_actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
