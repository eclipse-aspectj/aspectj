/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde.internal;

import java.io.File;
import java.util.*;

import org.aspectj.ajde.*;
import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.bridge.*;
import org.aspectj.util.ConfigParser;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class CompilerAdapter {

	private AjBuildManager buildManager = null;
    private MessageHandlerAdapter messageHandler = null;
    private BuildNotifierAdapter currNotifier = null;
	private boolean initialized = false;
	private boolean structureDirty = true;
	private boolean firstBuild = true;
	
	public CompilerAdapter() {
		super();
	}

	public void requestCompileExit() {
		if (currNotifier != null) currNotifier.cancelBuild();
//		buildManager.getJavaBuilder().notifier.setCancelling(true);
	}

	public boolean isStructureDirty() {
		return structureDirty;
	}

	public void setStructureDirty(boolean structureDirty) {
		this.structureDirty = structureDirty;
	}

	public boolean compile(String configFile, BuildProgressMonitor progressMonitor) {
		init();
		try {	 
			AjBuildConfig buildConfig = genBuildConfig(configFile);
			buildConfig.setGenerateModelMode(true);
			currNotifier = new BuildNotifierAdapter(
				AjBuildManager.DEFAULT_PROJECT,
				progressMonitor,
				buildConfig.getFiles().size());			
			buildManager.setBuildNotifier(currNotifier);
			messageHandler.setBuildNotifierAdapter(currNotifier);
			
			String rtInfo = buildManager.checkRtJar(buildConfig); // !!! will get called twice
			if (rtInfo != null) {
				Ajde.getDefault().getErrorHandler().handleWarning(
					"AspectJ Runtime error: " + rtInfo
		            + "  Please place a valid aspectjrt.jar in the lib/ext directory.");
	            return false;
			}
			
			if (firstBuild) {
				firstBuild = false;
				return buildManager.batchBuild(buildConfig, messageHandler);  
			} else {
				return buildManager.batchBuild(buildConfig, messageHandler);  // XXX incremental not implemented
//				return buildManager.incrementalBuild(buildConfig);
			}
		} catch (OperationCanceledException ce) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("build cancelled by user");
			return false;
		} catch (Throwable t) {
			t.printStackTrace();
			return false; 
//			messageHandler.handleMessage(new Message(t.toString(), Message.ERROR, t, null));
		} 
	}

	public AjBuildConfig genBuildConfig(String configFile) {
	    AjBuildConfig buildConfig = new AjBuildConfig();
	    File config = new File(configFile);
        if (!config.exists()) {
            Ajde.getDefault().getErrorHandler().handleWarning("Config file \"" + configFile + "\" does not exist."); 
        } else {
	        ConfigParser configParser = new ConfigParser();
	        configParser.parseConfigFile(config);
			buildConfig.setFiles(configParser.getFiles());
			buildConfig.setConfigFile(config);
        }
		
		String classpathString = 
			Ajde.getDefault().getProjectProperties().getBootClasspath()
			+ File.pathSeparator
			+ Ajde.getDefault().getProjectProperties().getClasspath();
			
		StringTokenizer st = new StringTokenizer(
			classpathString,
			File.pathSeparator
		);
		List classpath = new ArrayList();
		while (st.hasMoreTokens()) classpath.add(st.nextToken());
		buildConfig.setClasspath(classpath);  
		Ajde.getDefault().logEvent("building with classpath: " + classpath);

		if (Ajde.getDefault().getBuildManager().getBuildOptions().getSourceOnePointFourMode()) {
			buildConfig.getJavaOptions().put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);	 
		} 

		// XXX problematic restriction, support multiple source roots
		List sourceRoots = new ArrayList();
		sourceRoots.add(new File(configFile).getParentFile());
		buildConfig.setSourceRoots(sourceRoots);

		buildConfig.setOutputDir(
			new File(Ajde.getDefault().getProjectProperties().getOutputPath())
		);
		
		buildConfig.setGenerateModelMode(true);
		
		return buildConfig;
	}

	private void init() {
		if (!initialized) {  // XXX plug into AJDE initialization
//			Ajde.getDefault().setErrorHandler(new DebugErrorHandler());
			this.messageHandler = new MessageHandlerAdapter();
			buildManager = new AjBuildManager(messageHandler);
			initialized = true;
		}
	}
	
	class MessageHandlerAdapter implements IMessageHandler {
		private TaskListManager taskListManager;
		private BuildNotifierAdapter buildNotifierAdapter;
		
		public MessageHandlerAdapter() {
			this.taskListManager = Ajde.getDefault().getTaskListManager();
		}	
		
		public boolean handleMessage(IMessage message) throws AbortException {
			if (isIgnoring(message.getKind())) return true;
			
			// ??? relies on only info messages being class-file written messages
			if (message.getKind().equals(IMessage.INFO)) {
				if (buildNotifierAdapter != null) {
					buildNotifierAdapter.generatedBytecode(message.getMessage());
				}
			} else {
				taskListManager.addSourcelineTask(
					message.getMessage(),
					message.getISourceLocation(),
					message.getKind()
				);	
			}
			return true;	
		}
	
		public boolean isIgnoring(IMessage.Kind kind) {
			// XXX implement for INFO, DEBUG?
			return false;
		}
		
		public void setBuildNotifierAdapter(BuildNotifierAdapter buildNotifierAdapter) {
			this.buildNotifierAdapter = buildNotifierAdapter;
		}

	}
}
