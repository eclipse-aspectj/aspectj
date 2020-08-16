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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IProgramElement;

/**
 * Creates a popup menu that displays all the available .lst files. When one
 * is selected it runs a full build of files within the selected .lst file
 * in a separate thread.  
 */
public class BuildConfigPopupMenu extends JPopupMenu {

	private static final long serialVersionUID = -6730132748667530482L;

	public BuildConfigPopupMenu(final AbstractAction action) {
		List configFiles = Ajde.getDefault().getBuildConfigManager().getAllBuildConfigFiles();
		for (Object configFile : configFiles) {
			final String buildConfig = (String) configFile;
			JMenuItem buildItem = new JMenuItem(buildConfig);
			buildItem.setFont(AjdeWidgetStyles.DEFAULT_LABEL_FONT);
			buildItem.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Ajde.getDefault().getBuildConfigManager().setActiveConfigFile(buildConfig);
							// A separate thread is required here because the buildProgresssMonitor
							// that monitors the build needs to be in a different thread
							// to that which is doing the build (swing threading issues)
							Ajde.getDefault().runBuildInDifferentThread(buildConfig, true);
							action.actionPerformed(e);
						}
					});
			buildItem.setIcon((Icon) Ajde.getDefault().getIconRegistry().getIcon(IProgramElement.Kind.FILE_LST).getIconResource());
			this.add(buildItem);
		}
	}
}
