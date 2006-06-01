/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
	private NullIdeProperties projectProperties = null;
	private boolean initialized = false;
	
	public static NullIdeManager getIdeManager() {
		if ( null == ideManager ) {
			ideManager = new NullIdeManager();
		}
		return ideManager;
	}
	
	public void init(String testProjectPath) {
		try {
			UserPreferencesAdapter preferencesAdapter = new UserPreferencesStore(false);
			projectProperties = new NullIdeProperties(testProjectPath);
			taskListManager = new NullIdeTaskListManager();
			EditorAdapter ajdeEditor = new NullIdeEditorAdapter();
			IdeUIAdapter uiAdapter = new NullIdeUIAdapter();
			JFrame nullFrame = new JFrame();

			AjdeUIManager.getDefault().init(
				ajdeEditor,
				taskListManager,
				projectProperties,  
				preferencesAdapter,
				uiAdapter,
				new IconRegistry(),
				nullFrame,
				new NullIdeProgressMonitor(),
				new NullIdeErrorHandler(),
				true);	
			initialized = true;
		} catch (Throwable t) {
			initialized = false;
			t.printStackTrace();
			Ajde.getDefault().getErrorHandler().handleError(
				"Null IDE failed to initialize.",
				t);
		}
	}
	
	public List getCompilationSourceLineTasks() {
		return taskListManager.getSourceLineTasks();
	}
	
	public NullIdeProperties getProjectProperties() {
		return projectProperties;
	}

	public void setProjectProperties(NullIdeProperties properties) {
		projectProperties = properties;
	}

	public boolean isInitialized() {
		return initialized && AjdeUIManager.getDefault().isInitialized();
	}

}
