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
package org.aspectj.tools.ajbrowser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.IconRegistry;
import org.aspectj.ajde.internal.BuildConfigManager;
import org.aspectj.ajde.ui.FileStructureView;
import org.aspectj.ajde.ui.InvalidResourceException;
import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.ajde.ui.internal.UserPreferencesStore;
import org.aspectj.ajde.ui.javaoptions.JavaBuildOptions;
import org.aspectj.ajde.ui.javaoptions.JavaCompilerWarningsOptionsPanel;
import org.aspectj.ajde.ui.javaoptions.JavaComplianceOptionsPanel;
import org.aspectj.ajde.ui.javaoptions.JavaDebugOptionsPanel;
import org.aspectj.ajde.ui.javaoptions.JavaOtherOptionsPanel;
import org.aspectj.ajde.ui.swing.MultiStructureViewPanel;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IHierarchyListener;
import org.aspectj.tools.ajbrowser.core.BrowserBuildProgressMonitor;
import org.aspectj.tools.ajbrowser.core.BrowserCompilerConfiguration;
import org.aspectj.tools.ajbrowser.core.BrowserErrorHandler;
import org.aspectj.tools.ajbrowser.ui.BasicEditor;
import org.aspectj.tools.ajbrowser.ui.BrowserMessageHandler;
import org.aspectj.tools.ajbrowser.ui.BrowserRuntimeProperties;
import org.aspectj.tools.ajbrowser.ui.BrowserUIAdapter;
import org.aspectj.tools.ajbrowser.ui.EditorManager;
import org.aspectj.tools.ajbrowser.ui.swing.BrowserOptionsPanel;
import org.aspectj.tools.ajbrowser.ui.swing.MessageHandlerPanel;
import org.aspectj.tools.ajbrowser.ui.swing.TopFrame;
import org.aspectj.util.FileUtil;

/**
 * IDE manager for standalone AJDE application.
 * 
 * @author Mik Kersten
 */
public class BrowserManager {

	public static final String TITLE = "AspectJ Browser";

	private static final BrowserManager INSTANCE = new BrowserManager();
	private EditorManager editorManager;
	private UserPreferencesAdapter preferencesAdapter;
	private static TopFrame topFrame = null;

	private List<String> configFiles = new ArrayList<>();
	private JavaBuildOptions javaBuildOptions;

	public static BrowserManager getDefault() {
		return INSTANCE;
	}

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
			javaBuildOptions = new JavaBuildOptions();
			preferencesAdapter = new UserPreferencesStore(true);
			topFrame = new TopFrame();

			BasicEditor ajdeEditor = new BasicEditor();
			editorManager = new EditorManager(ajdeEditor);

			BrowserMessageHandler messageHandler = new BrowserMessageHandler();

			Ajde.getDefault().init(new BrowserCompilerConfiguration(preferencesAdapter), messageHandler,
					new BrowserBuildProgressMonitor(messageHandler), ajdeEditor, new BrowserUIAdapter(), new IconRegistry(),
					topFrame, new BrowserRuntimeProperties(preferencesAdapter), true);

			setUpTopFrame(visible);
			addOptionsPanels();

			setUpConfigFiles(configFilesArgs);

			Ajde.getDefault().getModel().addListener(VIEW_LISTENER);

		} catch (Throwable t) {
			t.printStackTrace();
			BrowserErrorHandler.handleError("AJDE failed to initialize.", t);
		}
	}

	/**
	 * Find and create the set of build configuration files
	 * 
	 * @param configFilesArgs
	 */
	private void setUpConfigFiles(String[] configFilesArgs) {
		configFiles = getConfigFilesList(configFilesArgs);
		if (configFiles.size() == 0) {
			BrowserErrorHandler.handleWarning("No build configuration selected. "
					+ "Select a \".lst\" build configuration file in order to compile and navigate structure.");
		} else {
			Ajde.getDefault().getBuildConfigManager().setActiveConfigFile((String) configFiles.get(0));
		}
	}

	/**
	 * Create the top frame of the browser
	 */
	private void setUpTopFrame(boolean visible) {
		MultiStructureViewPanel multiViewPanel = new MultiStructureViewPanel(Ajde.getDefault().getViewManager().getBrowserPanel(),
				Ajde.getDefault().getFileStructurePanel());

		topFrame.init(multiViewPanel, new MessageHandlerPanel(), editorManager.getEditorPanel());

		if (visible)
			topFrame.setVisible(true);
	}

	public void resetEditorFrame() {
		topFrame.resetSourceEditorPanel();
	}

	public void resetEditor() {
		BrowserManager
				.getDefault()
				.getRootFrame()
				.setSize(BrowserManager.getDefault().getRootFrame().getWidth() + 1,
						BrowserManager.getDefault().getRootFrame().getHeight() + 1);
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

	public JFrame getRootFrame() {
		return topFrame;
	}

	public void openFile(String filePath) {
		try {
			if (filePath.endsWith(".lst")) {
				Ajde.getDefault().getBuildConfigEditor().openFile(filePath);
				topFrame.setEditorPanel(Ajde.getDefault().getBuildConfigEditor());
			} else if (FileUtil.hasSourceSuffix(filePath)) {
				editorManager.showSourceLine(filePath, 0, false);
			} else {
				BrowserErrorHandler.handleError("File: " + filePath
						+ " could not be opened because the extension was not recoginzed.");
			}
		} catch (IOException ioe) {
			BrowserErrorHandler.handleError("Could not open file: " + filePath, ioe);
		} catch (InvalidResourceException ire) {
			BrowserErrorHandler.handleError("Invalid file: " + filePath, ire);
		}
	}

	private List<String> getConfigFilesList(String[] configFiles) {
		List<String> configs = new ArrayList<>();
		for (String configFile : configFiles) {
			if (configFile.endsWith(BuildConfigManager.CONFIG_FILE_SUFFIX)) {
				configs.add(configFile);
			}
		}
		return configs;
	}

	/**
	 * Add the different options panels to the main options frame (adds panels for java compliance, compiler warnings, debug
	 * warnings, other java options and options specific to ajbrowser)
	 */
	private void addOptionsPanels() {
		Ajde.getDefault().getOptionsFrame().addOptionsPanel(new JavaComplianceOptionsPanel(javaBuildOptions));
		Ajde.getDefault().getOptionsFrame().addOptionsPanel(new JavaCompilerWarningsOptionsPanel(javaBuildOptions));
		Ajde.getDefault().getOptionsFrame().addOptionsPanel(new JavaOtherOptionsPanel(javaBuildOptions));
		Ajde.getDefault().getOptionsFrame().addOptionsPanel(new JavaDebugOptionsPanel(javaBuildOptions));
		Ajde.getDefault().getOptionsFrame().addOptionsPanel(new BrowserOptionsPanel());
	}

	/**
	 * @return the EditorManager
	 */
	public EditorManager getEditorManager() {
		return editorManager;
	}

	/**
	 * @return the UserPreferencesAdapter
	 */
	public UserPreferencesAdapter getPreferencesAdapter() {
		return preferencesAdapter;
	}

	/**
	 * @return the JavaBuildOptions instance being used
	 */
	public JavaBuildOptions getJavaBuildOptions() {
		return javaBuildOptions;
	}

}
