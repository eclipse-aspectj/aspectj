/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC       initial implementation 
 *     AMC  03.27.2003  changed to allow access to NullIdeManager
 * 						as a singleton - needed for verifying
 * 						compiler warning and error messages.
 * ******************************************************************/


package org.aspectj.ajde;

import java.util.List;

import javax.swing.JFrame;

import org.aspectj.ajde.ui.IdeUIAdapter;
import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.ajde.ui.internal.UserPreferencesStore;
import org.aspectj.ajde.ui.swing.*;

/**
 * @author Mik Kersten
 */
public class NullIdeManager {
	
	private static NullIdeManager ideManager = null;
	private NullIdeTaskListManager taskListManager = null;
	
	public static NullIdeManager getIdeManager() {
		if ( null == ideManager ) {
			ideManager = new NullIdeManager();
		}
		return ideManager;
	}
	
	public void init(String testProjectPath) {
		try {
			UserPreferencesAdapter preferencesAdapter = new UserPreferencesStore();
			ProjectPropertiesAdapter browserProjectProperties = new NullIdeProperties(testProjectPath);
			taskListManager = new NullIdeTaskListManager();
			BasicEditor ajdeEditor = new BasicEditor();
			IdeUIAdapter uiAdapter = new NullIdeUIAdapter();
			JFrame nullFrame = new JFrame();
			//configurationManager.setConfigFiles(getConfigFilesList(configFiles));	

			AjdeUIManager.getDefault().init(
				ajdeEditor,
				taskListManager,
				browserProjectProperties,  
				preferencesAdapter,
				uiAdapter,
				new IconRegistry(),
				nullFrame,
				true);	
				
			//Ajde.getDefault().enableLogging( System.out );
		} catch (Throwable t) {
			t.printStackTrace();
			Ajde.getDefault().getErrorHandler().handleError(
				"Null IDE failed to initialize.",
				t);
		}
	}
	
	public List getCompilationSourceLineTasks() {
		return taskListManager.getSourceLineTasks();
	}
}
