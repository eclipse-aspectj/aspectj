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

import java.awt.Frame;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildListener;
import org.aspectj.ajde.BuildProgressMonitor;
import org.aspectj.ajde.EditorAdapter;
import org.aspectj.ajde.ErrorHandler;
import org.aspectj.ajde.ProjectPropertiesAdapter;
import org.aspectj.ajde.TaskListManager;
import org.aspectj.ajde.ui.FileStructureView;
import org.aspectj.ajde.ui.IdeUIAdapter;
import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.ajde.ui.internal.AjcBuildOptions;

/**
 * @author	Mik Kersten
 */
public class AjdeUIManager {

	protected static final AjdeUIManager INSTANCE = new AjdeUIManager();
	private BrowserViewManager viewManager = null;
	private BuildProgressMonitor buildProgressMonitor = null;
	private ErrorHandler errorHandler = null;
	private UserPreferencesAdapter userPreferencesAdapter = null;
	private AjcBuildOptions buildOptionsAdapter = null;
	private IdeUIAdapter ideUIAdapter = null;
	private TreeViewBuildConfigEditor buildConfigEditor = null;
	private IconRegistry iconRegistry;
	
	private OptionsFrame optionsFrame = null;
	private Frame rootFrame = null;
	private StructureViewPanel fileStructurePanel = null;

	/**
	 * Order of initialization is critical here.
	 */
	public void init(
		EditorAdapter editorAdapter,
		TaskListManager taskListManager,
		ProjectPropertiesAdapter projectProperties,
		UserPreferencesAdapter userPreferencesAdapter,
		IdeUIAdapter ideUIAdapter,
		IconRegistry iconRegistry,
		Frame rootFrame,
		boolean useFileView) {
		try {	
			BuildProgressMonitor compileProgress = new DefaultBuildProgressMonitor(rootFrame);
			ErrorHandler errorHandler = new AjdeErrorHandler();
			this.iconRegistry = iconRegistry;
			//ConfigurationManager configManager = new LstConfigurationManager();
			this.ideUIAdapter = ideUIAdapter;
			this.userPreferencesAdapter = userPreferencesAdapter;
			this.buildOptionsAdapter = new AjcBuildOptions(userPreferencesAdapter);
			this.buildConfigEditor = new TreeViewBuildConfigEditor();
			this.rootFrame = rootFrame;
			Ajde.init(
				editorAdapter,
				taskListManager,
				compileProgress,
				projectProperties,
				buildOptionsAdapter,
				new SwingTreeViewNodeFactory(iconRegistry),
				ideUIAdapter,
				errorHandler);
			
			Ajde.getDefault().getBuildManager().addListener(STATUS_TEXT_UPDATER);
			//Ajde.getDefault().setConfigurationManager(configManager);	
			
			if (useFileView) {
				FileStructureView structureView = Ajde.getDefault().getStructureViewManager().createViewForSourceFile(
	    			Ajde.getDefault().getEditorAdapter().getCurrFile(),
	    			Ajde.getDefault().getStructureViewManager().getDefaultViewProperties()
		    	);
		    	Ajde.getDefault().getStructureViewManager().setDefaultFileView(structureView);			
				fileStructurePanel = new StructureViewPanel(structureView);
			}
			
			viewManager = new BrowserViewManager();
			optionsFrame = new OptionsFrame(iconRegistry);
			
			//Ajde.getDefault().getStructureViewManager().refreshView(
			//	Ajde.getDefault().getStructureViewManager().getDefaultFileStructureView()
			//);
			
			//viewManager.updateView();
		} catch (Throwable t) {
			Ajde.getDefault().getErrorHandler().handleError("AJDE failed to initialize.", t);
		}
	}

	public static AjdeUIManager getDefault() {
		return INSTANCE;	
	}

	public BrowserViewManager getViewManager() {
		return viewManager;	
	}

	public Frame getRootFrame() {
		return rootFrame;	
	}

	public OptionsFrame getOptionsFrame() {
		return optionsFrame;	
	}

	public void showOptionsFrame() {
		int x = (rootFrame.getWidth()/2) + rootFrame.getX() - optionsFrame.getWidth()/2;
		int y = (rootFrame.getHeight()/2) + rootFrame.getY() - optionsFrame.getHeight()/2;
		optionsFrame.setLocation(x, y);
		optionsFrame.show();
	}
	
	public AjcBuildOptions getBuildOptions() {
		return buildOptionsAdapter;
	}
	
	private final BuildListener STATUS_TEXT_UPDATER = new BuildListener() {
		
		public void compileStarted(String buildConfigFile) { 
			ideUIAdapter.displayStatusInformation(" Building: " + buildConfigFile + "...");
		}
		
        public void compileFinished(String buildConfigFile, int buildTime, boolean succeeded, boolean warnings) {
            int timeInSeconds = buildTime/1000;
            if (succeeded) {
                ideUIAdapter.displayStatusInformation(" Build succeeded in " + timeInSeconds + " second(s).");
                //hideMessages();
            } else {
                ideUIAdapter.displayStatusInformation(" Build failed in " + timeInSeconds + " second(s)");
                //showMessages();
            }
        }
        
        public void compileAborted(String buildConfigFile, String message) { 
        	ideUIAdapter.displayStatusInformation("Compile aborted: " + message);
        }
    };

	public IdeUIAdapter getIdeUIAdapter() {
		return ideUIAdapter;
	}

	public TreeViewBuildConfigEditor getBuildConfigEditor() {
		return buildConfigEditor;
	}

	public StructureViewPanel getFileStructurePanel() {
		return fileStructurePanel;
	}
	
	public IconRegistry getIconRegistry() {
		return iconRegistry;
	}
}

//public abstract class AjdeAction {
//	public abstract void actionPerformed(ActionEvent e);
//
//    public abstract String getName();
//
//    public abstract ImageIcon getIcon();
//}

