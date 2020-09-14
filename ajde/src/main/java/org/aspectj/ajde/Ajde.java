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
 *******************************************************************/

package org.aspectj.ajde;

import java.awt.Frame;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajde.core.IBuildProgressMonitor;
import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.internal.BuildConfigListener;
import org.aspectj.ajde.internal.BuildConfigManager;
import org.aspectj.ajde.internal.LstBuildConfigManager;
import org.aspectj.ajde.ui.FileStructureView;
import org.aspectj.ajde.ui.StructureSearchManager;
import org.aspectj.ajde.ui.StructureViewManager;
import org.aspectj.ajde.ui.swing.BrowserViewManager;
import org.aspectj.ajde.ui.swing.OptionsFrame;
import org.aspectj.ajde.ui.swing.StructureViewPanel;
import org.aspectj.ajde.ui.swing.SwingTreeViewNodeFactory;
import org.aspectj.ajde.ui.swing.TreeViewBuildConfigEditor;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.util.LangUtil;
import org.aspectj.util.Reflection;

/**
 * Singleton class used to initialize the Ajde ui as well as the properties required to run the compiler. Users must call
 * "Ajde.init(...)" before doing anything else. There are getter methods for the various properties that are set in the
 * initialization.
 * 
 * This also defines the factory for getting new AjCompiler instances.
 * 
 * @author Mik Kersten
 * @author Andy Clement
 */
public class Ajde {

	protected static final Ajde INSTANCE = new Ajde();
	private BrowserViewManager viewManager = null;

	private IdeUIAdapter ideUIAdapter = null;
	private TreeViewBuildConfigEditor buildConfigEditor = null;
	private IconRegistry iconRegistry;
	private IRuntimeProperties runtimeProperties;
	private boolean initialized = false;
	private AsmManager asm;
	private OptionsFrame optionsFrame = null;
	private Frame rootFrame = null;
	private StructureViewPanel fileStructurePanel = null;

	private EditorAdapter editorAdapter;
	private StructureViewManager structureViewManager;
	private StructureSearchManager structureSearchManager;
	private final BuildConfigManager configurationManager;

	// all to do with building....
	private ICompilerConfiguration compilerConfig;
	private IUIBuildMessageHandler uiBuildMsgHandler;
	private IBuildProgressMonitor buildProgressMonitor;
	private AjCompiler compiler;

	public AsmManager getModel() {
		return asm;
	}

	/**
	 * This class can only be constructured by itself (as a singleton) or by sub-classes.
	 */
	protected Ajde() {
		configurationManager = new LstBuildConfigManager();
	}

	/**
	 * Initializes the ajde ui and sets up the compiler
	 */
	public void init(ICompilerConfiguration compilerConfig, IUIBuildMessageHandler uiBuildMessageHandler,
			IBuildProgressMonitor monitor, EditorAdapter editorAdapter, IdeUIAdapter ideUIAdapter, IconRegistry iconRegistry,
			Frame rootFrame, IRuntimeProperties runtimeProperties, boolean useFileView) {
		try {

			INSTANCE.compilerConfig = compilerConfig;
			INSTANCE.uiBuildMsgHandler = uiBuildMessageHandler;
			INSTANCE.buildProgressMonitor = monitor;
			INSTANCE.asm = AsmManager.createNewStructureModel(Collections.<File,String>emptyMap());

			INSTANCE.iconRegistry = iconRegistry;
			INSTANCE.ideUIAdapter = ideUIAdapter;
			INSTANCE.buildConfigEditor = new TreeViewBuildConfigEditor();
			INSTANCE.rootFrame = rootFrame;
			INSTANCE.runtimeProperties = runtimeProperties;

			INSTANCE.configurationManager.addListener(INSTANCE.STRUCTURE_UPDATE_CONFIG_LISTENER);
			INSTANCE.ideUIAdapter = ideUIAdapter;
			INSTANCE.editorAdapter = editorAdapter;
			INSTANCE.structureSearchManager = new StructureSearchManager();
			INSTANCE.structureViewManager = new StructureViewManager(new SwingTreeViewNodeFactory(iconRegistry));

			if (useFileView) {
				FileStructureView structureView = structureViewManager.createViewForSourceFile(editorAdapter.getCurrFile(),
						structureViewManager.getDefaultViewProperties());
				structureViewManager.setDefaultFileView(structureView);
				fileStructurePanel = new StructureViewPanel(structureView);
			}

			viewManager = new BrowserViewManager();
			optionsFrame = new OptionsFrame(iconRegistry);

			initialized = true;
		} catch (Throwable t) {
			Message error = new Message("AJDE UI failed to initialize", IMessage.ABORT, t, null);
			uiBuildMsgHandler.handleMessage(error);
		}
	}

	public void showOptionsFrame() {
		int x = (rootFrame.getWidth() / 2) + rootFrame.getX() - optionsFrame.getWidth() / 2;
		int y = (rootFrame.getHeight() / 2) + rootFrame.getY() - optionsFrame.getHeight() / 2;
		optionsFrame.setLocation(x, y);
		optionsFrame.setVisible(true);
	}

	/**
	 * @return true if init(..) has been run, false otherwise
	 */
	public boolean isInitialized() {
		return initialized;
	}

	private final BuildConfigListener STRUCTURE_UPDATE_CONFIG_LISTENER = new BuildConfigListener() {
		public void currConfigChanged(String configFilePath) {
			if (configFilePath != null) {
				Ajde.getDefault().asm.readStructureModel(configFilePath);
			}
		}

		public void configsListUpdated(List configsList) {
		}
	};

	/**
	 * Utility to run the project main class from the project properties in the same VM using a class loader populated with the
	 * classpath and output path or jar. Errors are logged to the ErrorHandler.
	 * 
	 * @return Thread running with process, or null if unable to start
	 */
	public Thread runInSameVM() {
		final RunProperties props = new RunProperties(compilerConfig, runtimeProperties, uiBuildMsgHandler, rootFrame);
		if (!props.valid) {
			return null; // error already handled
		}
		Runnable runner = new Runnable() {
			public void run() {
				try {
					Reflection.runMainInSameVM(props.classpath, props.mainClass, props.args);
				} catch (Throwable e) {
					Message msg = new Message("Error running " + props.mainClass, IMessage.ERROR, e, null);
					uiBuildMsgHandler.handleMessage(msg);
				}
			}
		};
		Thread result = new Thread(runner, props.mainClass);
		result.start();
		return result;
	}

	/**
	 * Utility to run the project main class from the project properties in a new VM. Errors are logged to the ErrorHandler.
	 * 
	 * @return LangUtil.ProcessController running with process, or null if unable to start
	 */
	public LangUtil.ProcessController runInNewVM() {
		final RunProperties props = new RunProperties(compilerConfig, runtimeProperties, uiBuildMsgHandler, rootFrame);
		if (!props.valid) {
			return null; // error already handled
		}
		// setup to run asynchronously, pipe streams through, and report errors
		final StringBuffer command = new StringBuffer();
		LangUtil.ProcessController controller = new LangUtil.ProcessController() {
			public void doCompleting(Throwable thrown, int result) {
				LangUtil.ProcessController.Thrown any = getThrown();
				if (!any.thrown && (null == thrown) && (0 == result)) {
					return; // no errors
				}
				// handle errors
				String context = props.mainClass + " command \"" + command + "\"";
				if (null != thrown) {
					String m = "Exception running " + context;
					uiBuildMsgHandler.handleMessage(new Message(m, IMessage.ERROR, thrown, null));
				} else if (0 != result) {
					String m = "Result of running " + context;
					uiBuildMsgHandler.handleMessage(new Message(m, IMessage.ERROR, null, null));
				}
				if (null != any.fromInPipe) {
					String m = "Error processing input pipe for " + context;
					uiBuildMsgHandler.handleMessage(new Message(m, IMessage.ERROR, thrown, null));
				}
				if (null != any.fromOutPipe) {
					String m = "Error processing output pipe for " + context;
					uiBuildMsgHandler.handleMessage(new Message(m, IMessage.ERROR, thrown, null));
				}
				if (null != any.fromErrPipe) {
					String m = "Error processing error pipe for " + context;
					uiBuildMsgHandler.handleMessage(new Message(m, IMessage.ERROR, thrown, null));
				}
			}
		};

		controller = LangUtil.makeProcess(controller, props.classpath, props.mainClass, props.args);

		command.append(Arrays.asList(controller.getCommand()).toString());

		// now run the process
		controller.start();
		return controller;
	}

	/** struct class to interpret project properties */
	private static class RunProperties {
		final String mainClass;
		final String classpath;
		final String[] args;
		final boolean valid;
		private final Frame rootFrame;

		RunProperties(ICompilerConfiguration compilerConfig, IRuntimeProperties runtimeProperties, IUIBuildMessageHandler handler,
				Frame rootFrame) {
			// XXX really run arbitrary handler in constructor? hmm.
			LangUtil.throwIaxIfNull(runtimeProperties, "runtime properties");
			LangUtil.throwIaxIfNull(compilerConfig, "compiler configuration");
			LangUtil.throwIaxIfNull(handler, "handler");
			LangUtil.throwIaxIfNull(rootFrame, "rootFrame");
			String mainClass = null;
			String classpath = null;
			String[] args = null;
			boolean valid = false;
			this.rootFrame = rootFrame;

			mainClass = runtimeProperties.getClassToExecute();
			if (LangUtil.isEmpty(mainClass)) {
				showWarningMessage("No main class specified");
			} else {
				StringBuffer sb = new StringBuffer();
				List outputDirs = compilerConfig.getOutputLocationManager().getAllOutputLocations();
				for (Object outputDir : outputDirs) {
					File dir = (File) outputDir;
					sb.append(dir.getAbsolutePath() + File.pathSeparator);
				}
				classpath = LangUtil.makeClasspath(null, compilerConfig.getClasspath(), sb.toString(), compilerConfig.getOutJar());
				if (LangUtil.isEmpty(classpath)) {
					showWarningMessage("No classpath specified");
				} else {
					args = LangUtil.split(runtimeProperties.getExecutionArgs());
					valid = true;
				}
			}
			this.mainClass = mainClass;
			this.classpath = classpath;
			this.args = args;
			this.valid = valid;
		}

		private void showWarningMessage(String message) {
			JOptionPane.showMessageDialog(rootFrame, message, "Warning", JOptionPane.WARNING_MESSAGE);
		}

	}

	/**
	 * Set the build off in the same thread
	 * 
	 * @param configFile
	 * @param buildFresh - true if want to do a full build, false otherwise
	 */
	public void runBuildInSameThread(String configFile, boolean buildFresh) {
		AjCompiler c = getCompilerForConfigFile(configFile);
		if (c == null)
			return;
		if (buildFresh) {
			c.buildFresh();
		} else {
			c.build();
		}
	}

	/**
	 * Set the build off in a different thread. Would need to set the build off in a different thread if using a swing application
	 * to display the build progress.
	 * 
	 * @param configFile
	 * @param buildFresh - true if want to do a full build, false otherwise
	 */
	public void runBuildInDifferentThread(String configFile, boolean buildFresh) {
		AjCompiler c = getCompilerForConfigFile(configFile);
		if (c == null)
			return;
		CompilerThread compilerThread = new CompilerThread(c, buildFresh);
		compilerThread.start();
	}

	static class CompilerThread extends Thread {

		private final AjCompiler compiler;
		private final boolean buildFresh;

		public CompilerThread(AjCompiler compiler, boolean buildFresh) {
			this.compiler = compiler;
			this.buildFresh = buildFresh;
		}

		public void run() {
			if (buildFresh) {
				compiler.buildFresh();
			} else {
				compiler.build();
			}
		}
	}

	// ---------- getter methods for the ui --------------

	/**
	 * @return the singleton instance
	 */
	public static Ajde getDefault() {
		return INSTANCE;
	}

	/**
	 * @return the BrowserViewManager
	 */
	public BrowserViewManager getViewManager() {
		return viewManager;
	}

	/**
	 * @return the main frame
	 */
	public Frame getRootFrame() {
		return rootFrame;
	}

	/**
	 * @return the parent frame for the options panel
	 */
	public OptionsFrame getOptionsFrame() {
		return optionsFrame;
	}

	/**
	 * @return the IdeUIAdapter
	 */
	public IdeUIAdapter getIdeUIAdapter() {
		return ideUIAdapter;
	}

	/**
	 * @return the EditorAdapter
	 */
	public EditorAdapter getEditorAdapter() {
		return editorAdapter;
	}

	/**
	 * @return the TreeViewBuildConfigEditor
	 */
	public TreeViewBuildConfigEditor getBuildConfigEditor() {
		return buildConfigEditor;
	}

	/**
	 * @return the StructureViewPanel
	 */
	public StructureViewPanel getFileStructurePanel() {
		return fileStructurePanel;
	}

	/**
	 * @return the IconRegistry
	 */
	public IconRegistry getIconRegistry() {
		return iconRegistry;
	}

	/**
	 * @return the StructureViewManager
	 */
	public StructureViewManager getStructureViewManager() {
		return structureViewManager;
	}

	/**
	 * @return the StructureSearchManager
	 */
	public StructureSearchManager getStructureSearchManager() {
		return structureSearchManager;
	}

	/**
	 * @return the BuildConfigManager
	 */
	public BuildConfigManager getBuildConfigManager() {
		return configurationManager;
	}

	// -------------- getter methods for the compiler -------------

	/**
	 * @return the ICompilerConfiguration
	 */
	public ICompilerConfiguration getCompilerConfig() {
		return compilerConfig;
	}

	/**
	 * @return the IUIBuildMessageHandler
	 */
	public IUIBuildMessageHandler getMessageHandler() {
		return uiBuildMsgHandler;
	}

	/**
	 * @return the IBuildProgressMonitor
	 */
	public IBuildProgressMonitor getBuildProgressMonitor() {
		return buildProgressMonitor;
	}

	/**
	 * If the provided configFile is the same as the id for the last compiler then returns that, otherwise clears the state for the
	 * saved compiler and creates a new one for the provided configFile
	 * 
	 * @param configFile
	 * @return the AjCompiler with the id of the given configFile
	 */
	public AjCompiler getCompilerForConfigFile(String configFile) {
		if (configFile == null) {
			return null;
		}
		if ((compiler == null || !compiler.getId().equals(configFile))) {
			if (compiler != null) {
				// have to remove the incremental state of the previous
				// compiler - this will remove it from the
				// IncrementalStateManager's
				// list
				compiler.clearLastState();
			}
			getMessageHandler().reset();
			compiler = new AjCompiler(configFile, getCompilerConfig(), getBuildProgressMonitor(), getMessageHandler());
		}
		return compiler;
	}

	public AsmManager getModelForConfigFile(String configFile) {
		return compiler.getModel();
		// if ((compiler == null || !compiler.getId().equals(configFile))) {
		// if (compiler != null) {
		// // have to remove the incremental state of the previous
		// // compiler - this will remove it from the
		// // IncrementalStateManager's
		// // list
		// compiler.clearLastState();
		// }
		// getMessageHandler().reset();
		// compiler = new AjCompiler(configFile, getCompilerConfig(), getBuildProgressMonitor(), getMessageHandler());
		// }

	}
}
