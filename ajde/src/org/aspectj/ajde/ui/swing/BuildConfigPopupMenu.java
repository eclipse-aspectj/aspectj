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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IProgramElement;

public class BuildConfigPopupMenu extends JPopupMenu {

	public BuildConfigPopupMenu(final AbstractAction action) {
		java.util.List configFiles = Ajde.getDefault().getProjectProperties().getBuildConfigFiles();
		for (Iterator it = configFiles.iterator(); it.hasNext(); ) {
	    	final String buildConfig = (String)it.next();
	    	JMenuItem buildItem = new JMenuItem(buildConfig);
	    	buildItem.setFont(AjdeWidgetStyles.DEFAULT_LABEL_FONT);
	    	buildItem.addActionListener(
	    		new ActionListener() {
		    		public void actionPerformed(ActionEvent e) {
		    			Ajde.getDefault().getConfigurationManager().setActiveConfigFile(buildConfig);
							// ??? should we be able to do a build refresh if shift is down?
//                        if (EditorManager.isShiftDown(e.getModifiers())) {
//                            Ajde.getDefault().getBuildManager().buildFresh();
//                        } else {
                            Ajde.getDefault().getBuildManager().build();
//                        }
				        action.actionPerformed(e);
					}
	    		});
	    	buildItem.setIcon((Icon)AjdeUIManager.getDefault().getIconRegistry().getStructureIcon(IProgramElement.Kind.FILE_LST).getIconResource());
	    	this.add(buildItem);
		}
	}
}
