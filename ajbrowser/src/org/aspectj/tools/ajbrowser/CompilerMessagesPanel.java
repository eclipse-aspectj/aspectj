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


package org.aspectj.tools.ajbrowser;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.TaskListManager;
import org.aspectj.ajde.ui.swing.CompilerMessage;
import org.aspectj.ajde.ui.swing.CompilerMessagesCellRenderer;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;

/**
 * Used to display a list of compiler messages that can be clicked in order
 * to seek to their corresponding sourceline.
 *
 * @author Mik Kersten
 */
public class CompilerMessagesPanel extends JPanel implements TaskListManager {
    private JScrollPane jScrollPane1 = new JScrollPane();
    //private JScrollPane jScrollPane2 = new JScrollPane();
    private JList list = new JList();
    private DefaultListModel listModel = new DefaultListModel();
    private BorderLayout borderLayout1 = new BorderLayout();

    public CompilerMessagesPanel() {
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        list.setModel(listModel);

        MouseListener mouseListener = new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() >= 1) {
                     int index = list.locationToIndex(e.getPoint());
                     if (listModel.getSize() >= index && index != -1) {
                     	CompilerMessage cm = (CompilerMessage)listModel.getElementAt(index);
                     	Ajde.getDefault().getEditorManager().showSourceLine(cm.sourceLocation, true);
                     }
                  }
             }
        };
        list.addMouseListener(mouseListener);
        list.setCellRenderer(new CompilerMessagesCellRenderer());
    } 

    public void addSourcelineTask(String message, ISourceLocation sourceLocation, IMessage.Kind kind) {   
        listModel.addElement(new CompilerMessage(message, sourceLocation,kind));
        BrowserManager.getDefault().showMessages();
    }

    public void addProjectTask(String message, IMessage.Kind kind) {
		listModel.addElement(new CompilerMessage(message,kind));
		BrowserManager.getDefault().showMessages();
	}

    public void clearTasks() {
        listModel.clear();
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        this.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(list, null);
    }
}



