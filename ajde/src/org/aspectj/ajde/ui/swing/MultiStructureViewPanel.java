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


package org.aspectj.ajde.ui.swing;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.aspectj.ajde.Ajde;

/**
 * @author  Mik Kersten
 */
public class MultiStructureViewPanel extends JPanel {
    JSplitPane views_splitPane;
    BorderLayout borderLayout1 = new BorderLayout();

	public MultiStructureViewPanel(StructureViewPanel topPanel, StructureViewPanel bottomPanel) {
    	super();
        try {
        	views_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
            jbInit(); 
        } catch(Exception e) {
            Ajde.getDefault().getErrorHandler().handleError("Could not initialize GUI.", e);
        }
	}

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        this.add(views_splitPane, BorderLayout.CENTER);
        views_splitPane.setDividerSize(4);
        views_splitPane.setDividerLocation(300);
    }

}
