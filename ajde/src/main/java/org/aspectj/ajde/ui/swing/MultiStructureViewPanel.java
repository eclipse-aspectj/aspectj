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

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.aspectj.ajde.Ajde;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * @author  Mik Kersten
 */
public class MultiStructureViewPanel extends JPanel {
 
	private static final long serialVersionUID = -4409192026967597082L;
	JSplitPane views_splitPane;
    BorderLayout borderLayout1 = new BorderLayout();

	public MultiStructureViewPanel(StructureViewPanel topPanel, StructureViewPanel bottomPanel) {
    	super();
        try {
        	views_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
            jbInit(); 
        } catch(Exception e) {
        	Message msg = new Message("Could not initialize GUI.",IMessage.ERROR,e,null);
        	Ajde.getDefault().getMessageHandler().handleMessage(msg);
        }
	}

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        this.add(views_splitPane, BorderLayout.CENTER);
        views_splitPane.setDividerSize(4);
        views_splitPane.setDividerLocation(300);
    }

}
