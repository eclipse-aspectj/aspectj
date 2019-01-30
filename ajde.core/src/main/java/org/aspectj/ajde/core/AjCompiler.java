/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.core;

import java.io.File;

import org.aspectj.ajde.core.internal.AjdeCoreBuildManager;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * The class to be used by tools to drive a build. An AjCompiler is created with a unique id (for example the absolute pathname of a
 * project or .lst file) along with implementations of ICompilerConfiguration, IBuildProgressMonitor and IBuildMessageHandler. Tools
 * then call build() or buildFresh() on this AjCompiler.
 * 
 * <p>
 * An AjCompiler is associated with one id, therefore a new one needs to be created for a new id (project, .lst file etc.). It is
 * the responsibility of the tools to manage the lifecycle of the AjCompiler's.
 */
public class AjCompiler {

	private final String compilerId;
	private final ICompilerConfiguration compilerConfig;
	private final IBuildProgressMonitor monitor;
	private final IBuildMessageHandler handler;
	private final AjdeCoreBuildManager buildManager;

	/**
	 * Creates a new AjCompiler for the given id, ICompilerConfiguration, IBuildProgressMonitor and IBuildMessageHandler. None of
	 * the arguments can be null.
	 * 
	 * @param compilerId - Unique String used to identify this AjCompiler
	 * @param compilerConfig - ICompilerConfiguration implementation
	 * @param buildProgressMonitor - IBuildProgressMonitor implementation
	 * @param buildMessageHandler - IBuildMessageHandler implementation
	 */
	public AjCompiler(String compilerId, ICompilerConfiguration compilerConfig, IBuildProgressMonitor buildProgressMonitor,
			IBuildMessageHandler buildMessageHandler) {
		this.compilerConfig = compilerConfig;
		this.monitor = buildProgressMonitor;
		this.handler = buildMessageHandler;
		this.compilerId = compilerId;
		this.buildManager = new AjdeCoreBuildManager(this);
	}

	/**
	 * @return the id for this AjCompiler
	 */
	public String getId() {
		return compilerId;
	}

	/**
	 * @return the ICompilerConfiguration associated with this AjCompiler
	 */
	public ICompilerConfiguration getCompilerConfiguration() {
		return compilerConfig;
	}

	/**
	 * @return the IBuildProgressMonitor associated with this AjCompiler
	 */
	public IBuildProgressMonitor getBuildProgressMonitor() {
		return monitor;
	}

	/**
	 * @return the IBuildMessageHandler associated with this AjCompiler
	 */
	public IBuildMessageHandler getMessageHandler() {
		return handler;
	}

	/**
	 * Perform an incremental build if possible, otherwise it will default to a full build.
	 */
	public void build() {
		if (hasValidId()) {
			buildManager.performBuild(false);
		}
	}

	/**
	 * Perform a full build.
	 */
	public void buildFresh() {
		if (hasValidId()) {
			buildManager.performBuild(true);
		}
	}

	/**
	 * Clear the incremental state associated with this AjCompiler from the IncrementalStateManager. This is necessary until AjState
	 * is reworked and there's an AjState associated with an AjCompiler rather than requiring a map of them. If the environment is
	 * not cleaned up then jar locks may be kept.
	 */
	public void clearLastState() {
		IncrementalStateManager.removeIncrementalStateInformationFor(compilerId);
		buildManager.cleanupEnvironment();
	}

	public boolean addDependencies(File file, String[] typeNameDependencies) {
		AjState state = IncrementalStateManager.retrieveStateFor(compilerId);
		return state.recordDependencies(file, typeNameDependencies);
	}

	/**
	 * @return true if the underlying version of the compiler is compatible with Java 6, returns false otherwise.
	 */
	public boolean isJava6Compatible() {
		return CompilerOptions.versionToJdkLevel(JavaOptions.VERSION_16) != 0;
	}

	/**
	 * Ensures that the id associated with this compiler is non-null. If it is null then sends an ABORT message to the
	 * messageHandler.
	 */
	private boolean hasValidId() {
		if (compilerId == null) {
			Message msg = new Message("compiler didn't have an id associated with it", IMessage.ABORT, null, null);
			handler.handleMessage(msg);
			return false;
		}
		return true;
	}

	/**
	 * Set a CustomMungerFactory to the compiler's weaver
	 * 
	 * The type of factory should be org.aspectj.weaver.CustomMungerFactory but due to dependency problem of project ajde.core, it
	 * is Object for now.
	 * 
	 * @param factory
	 */
	public void setCustomMungerFactory(Object factory) {
		buildManager.setCustomMungerFactory(factory);
	}

	/**
	 * @return the CustomMungerFactory from the compiler's weaver
	 * 
	 *         The return type should be org.aspectj.weaver.CustomMungerFactory but due to dependency problem of project ajde.core,
	 *         it is Object for now.
	 */
	public Object getCustomMungerFactory() {
		return buildManager.getCustomMungerFactory();
	}

	public AsmManager getModel() {
		return buildManager.getStructureModel();
	}

	public AjdeCoreBuildManager getBuildManager() {
		return buildManager;
	}
}
