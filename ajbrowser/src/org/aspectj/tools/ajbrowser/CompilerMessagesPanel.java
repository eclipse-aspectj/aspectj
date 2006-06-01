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


package org.aspectj.tools.ajbrowser;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;

import org.aspectj.ajde.*;
import org.aspectj.ajde.ui.swing.*;
import org.aspectj.bridge.*;
import org.aspectj.bridge.IMessage.Kind;

/**
 * Used to display a list of compiler messages that can be clicked in order
 * to seek to their corresponding sourceline.
 *
 * @author Mik Kersten
 */
public class CompilerMessagesPanel extends JPanel implements TaskListManager {
    
	private static final long serialVersionUID = -2251912345065588977L;
	private JScrollPane jScrollPane1 = new JScrollPane();
    //private JScrollPane jScrollPane2 = new JScrollPane();
    private JList list = new JList();
    private DefaultListModel listModel = new DefaultListModel();
    private BorderLayout borderLayout1 = new BorderLayout();
	private boolean hasWarning = false;

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
                     	IMessage message = (IMessage)listModel.getElementAt(index);
                     	Ajde.getDefault().getEditorAdapter().showSourceLine(message.getSourceLocation(), true);
                     }
                  }
             }
        };
        list.addMouseListener(mouseListener);
        list.setCellRenderer(new CompilerMessagesCellRenderer());
    } 

	public void addSourcelineTask(IMessage message) {
		listModel.addElement(message);
		checkIfWarning(message.getKind());
	}
 
	public void addSourcelineTask(String message, ISourceLocation sourceLocation, IMessage.Kind kind) {   
        listModel.addElement(new Message(message, kind, null, sourceLocation));
		checkIfWarning(kind);
    }

    public void addProjectTask(String message, IMessage.Kind kind) {
		listModel.addElement(new Message(message, kind, null, null));
		checkIfWarning(kind);
	}
  
	private void checkIfWarning(Kind kind) {
		if (kind.equals(IMessage.WARNING)) hasWarning = true;	
	}
  
    public void clearTasks() {
        listModel.clear();
        hasWarning = false;
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        this.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(list, null);
    }

	public boolean hasWarning() {
		return hasWarning;
	}

}



