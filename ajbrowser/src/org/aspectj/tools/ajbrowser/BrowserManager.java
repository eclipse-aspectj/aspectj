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

import java.io.IOException;
import java.util.*;

import javax.swing.JFrame;

import org.aspectj.ajde.*;
import org.aspectj.ajde.ui.*;
import org.aspectj.ajde.ui.internal.UserPreferencesStore;
import org.aspectj.ajde.ui.swing.*;
import org.aspectj.asm.*;
import org.aspectj.util.FileUtil;
//import org.aspectj.asm.internal.*;

/**
 * IDE manager for standalone AJDE application.
 *
 * @author  Mik Kersten
 */
public class BrowserManager {
	
	private static final BrowserManager INSTANCE = new BrowserManager();
	private BrowserProperties browserProjectProperties;
	private EditorManager editorManager;
	
	public static BrowserManager getDefault() {
		return INSTANCE;
	}
    	
	private List configFiles = new ArrayList();
	
	public static final String TITLE = "AspectJ Browser";
    
    private static TopFrame topFrame = null;
    
	public final IHierarchyListener VIEW_LISTENER = new IHierarchyListener() {
		public void elementsUpdated(IHierarchy model) {        	
			FileStructureView fsv = Ajde.getDefault().getStructureViewManager().getDefaultFileView();
			if (fsv != null) {
				fsv.setSourceFile(BrowserManager.getDefault().getEditorManager().getCurrFile());
			}
		}
	}; 
    
	public void init(String[] configFilesArgs, boolean visible) {
		try {
			UserPreferencesAdapter preferencesAdapter = new UserPreferencesStore(true);
			browserProjectProperties = new BrowserProperties(preferencesAdapter);
			TaskListManager taskListManager = new CompilerMessagesPanel();
			BasicEditor ajdeEditor = new BasicEditor();
			BrowserUIAdapter browserUIAdapter = new BrowserUIAdapter();
			topFrame = new TopFrame(); 
			configFiles = getConfigFilesList(configFilesArgs);	

			AjdeUIManager.getDefault().init(
				ajdeEditor,
				taskListManager,
				browserProjectProperties,
				preferencesAdapter,
				browserUIAdapter,
				new IconRegistry(),
				topFrame,
				true);	
			
			editorManager = new EditorManager(ajdeEditor);
			
			Ajde.getDefault().getBuildManager().addListener(BUILD_MESSAGES_LISTENER);
			
			MultiStructureViewPanel multiViewPanel = new MultiStructureViewPanel(
				AjdeUIManager.getDefault().getViewManager().getBrowserPanel(),
				AjdeUIManager.getDefault().getFileStructurePanel()
			);
			
			topFrame.init(
				multiViewPanel,
				(CompilerMessagesPanel)taskListManager,
				editorManager.getEditorPanel()
			);
				
			if (visible) topFrame.setVisible(true);
			  
			if (configFiles.size() == 0) {
				Ajde.getDefault().getErrorHandler().handleWarning(
					"No build configuration selected. "
						+ "Select a \".lst\" build configuration file in order to compile and navigate structure.");
			} else {
				//UiManager.getDefault().getViewManager().updateConfigsList();
			}
		
			AjdeUIManager.getDefault().getOptionsFrame().addOptionsPanel(new BrowserOptionsPanel());
		
			AsmManager.getDefault().addListener(VIEW_LISTENER);	
		
			//String lastOpenFilePath = browserProjectProperties.getLastOpenSourceFilePath();
			//editorManager.showSourceLine(lastOpenFilePath, 1, false);	
			//Ajde.getDefault().getStructureViewManager().fireNavigationAction(lastOpenFilePath, 6);
			//Ajde.getDefault().enableLogging(System.out); 
		
			if (configFilesArgs.length > 0 && configFilesArgs[0] != null) {
				Ajde.getDefault().getConfigurationManager().setActiveConfigFile(configFilesArgs[0]);	
			}
		} catch (Throwable t) {
			t.printStackTrace();
			Ajde.getDefault().getErrorHandler().handleError(
				"AJDE failed to initialize.",
				t);
		}
	}

    public void resetEditorFrame() {
        topFrame.resetSourceEditorPanel();
    }

	public void resetEditor() {
        BrowserManager.getDefault().getRootFrame().setSize(BrowserManager.getDefault().getRootFrame().getWidth()+1, BrowserManager.getDefault().getRootFrame().getHeight()+1);
        BrowserManager.getDefault().getRootFrame().doLayout();
        BrowserManager.getDefault().getRootFrame().repaint();
    }

    public void setStatusInformation(String text) {
        topFrame.statusText_label.setText(text);
    }

    public void setEditorStatusText(String text) {
        topFrame.setTitle(BrowserManager.TITLE + " - " + text);
    }

    public void saveAll() {
        editorManager.saveContents();
    }

    public void showMessages() {
        topFrame.showMessagesPanel();
    }

    public void hideMessages() {
        topFrame.hideMessagesPanel();
    }

    public JFrame getRootFrame() {
        return topFrame;
    }

	public void openFile(String filePath) {
		try {
			if (filePath.endsWith(".lst")) {
				AjdeUIManager.getDefault().getBuildConfigEditor().openFile(filePath);
				topFrame.setEditorPanel(AjdeUIManager.getDefault().getBuildConfigEditor());
			} else if (FileUtil.hasSourceSuffix(filePath)){
				editorManager.showSourceLine(filePath, 0, false);		
			} else {
				Ajde.getDefault().getErrorHandler().handleError("File: " + filePath 
					+ " could not be opened because the extension was not recoginzed.");	
			}
		} catch (IOException ioe) {
			Ajde.getDefault().getErrorHandler().handleError("Could not open file: " + filePath, ioe);
		} catch (InvalidResourceException ire) {
			Ajde.getDefault().getErrorHandler().handleError("Invalid file: " + filePath, ire);
		} 
		
		browserProjectProperties.setLastOpenSourceFilePath(filePath);
	}

	private List getConfigFilesList(String[] configFiles) {
		List configs = new ArrayList();
		for (int i = 0; i < configFiles.length; i++) {
            if (configFiles[i].endsWith(BuildConfigManager.CONFIG_FILE_SUFFIX)) {
                configs.add(configFiles[i]);
            }
        }
        return configs;
	}

//    private static class Runner {
//  
//        public static void invoke(String className) {
//            try {
//                if (className == null || className.length() == 0) {
//                    Ajde.getDefault().getErrorHandler().handleWarning("No main class specified, please select a class to run.");
//
//                } else {
//                    Class[] argTypes = { String[].class };
//                    java.lang.reflect.Method method = Class.forName(className).getDeclaredMethod("main", argTypes);
//                    Object[] args = { new String[0] };
//                    method.invoke(null, args);
//                }
//            } catch(ClassNotFoundException cnfe) {
//                Ajde.getDefault().getErrorHandler().handleWarning("Main class not found: " + className +
//                "\nMake sure that you have \".\" on your classpath.");
//            } catch(NoSuchMethodException nsme) {
//                Ajde.getDefault().getErrorHandler().handleWarning("Class: " + className + " does not declare public static void main(String[])");
//            } catch(java.lang.reflect.InvocationTargetException ite) {
//                Ajde.getDefault().getErrorHandler().handleWarning("Could not execute: " + className);
//            } catch(IllegalAccessException iae) {
//                Ajde.getDefault().getErrorHandler().handleWarning("Class: " + className + " does not declare public main method");
//            }
//        }
//    }
    
	private final BuildListener BUILD_MESSAGES_LISTENER = new BuildListener() {
		
		public void compileStarted(String buildConfigFile) { }
		
        public void compileFinished(String buildConfigFile, int buildTime, boolean succeeded, boolean warnings) {
            if (succeeded && !warnings) {
                hideMessages();
            } else {
                showMessages();
            }
        }
        
        public void compileAborted(String buildConfigFile, String message) { }
    };
    
	public List getConfigFiles() {
		return configFiles;
	}

	public BrowserProperties getBrowserProjectProperties() {
		return browserProjectProperties;
	}
	/**
	 * @return
	 */
	public EditorManager getEditorManager() {
		return editorManager;
	}

}
