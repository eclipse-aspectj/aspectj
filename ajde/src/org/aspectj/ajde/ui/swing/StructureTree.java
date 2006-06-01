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

import java.awt.Font;

import javax.swing.JTree;

import org.aspectj.ajde.Ajde;

/**
 * @author Mik Kersten
 */
class StructureTree extends JTree {

	private static final long serialVersionUID = -5599178058976534562L;

	public static final Font DEFAULT_FONT = new java.awt.Font("Dialog", 0, 11);

    private String rootFilePath = null;

    public StructureTree() {
        try {
            jbInit();
        }
        catch(Exception e) {
            Ajde.getDefault().getErrorHandler().handleError("Could not initialize GUI.", e);
        }
    }

    public void setRootFilePath(String rootFilePath) {
        this.rootFilePath = rootFilePath;
    }

    public String getRootFilePath() {
        return rootFilePath;
    }

    private void jbInit() throws Exception {
        this.setFont(DEFAULT_FONT);
    }

    public int getToggleClickCount() {
        return 1;
    }    
}
