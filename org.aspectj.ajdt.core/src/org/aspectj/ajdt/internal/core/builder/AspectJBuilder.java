/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.util.Map;

import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerAdapter;
import org.eclipse.jdt.internal.compiler.ICompilerAdapterFactory;
import org.eclipse.jdt.internal.core.builder.BatchImageBuilder;
import org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;

/**
 * @author colyer
 *
 * This is the builder class used by AJDT, and that the org.eclipse.ajdt.core
 * plugin references.
 */
public class AspectJBuilder extends JavaBuilder implements ICompilerAdapterFactory {
	
	
	// One builder instance per project  (important)
	private BcelWeaver myWeaver = null;
	private BcelWorld myBcelWorld = null;
		
	private boolean isBuildIncremental = false;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map ignored, IProgressMonitor monitor)
			throws CoreException {
		// super method always causes construction of a new XXXImageBuilder, which
		// causes construction of a new Compiler, so we will be detected as the 
		// adapter.
		Compiler.setCompilerAdapterFactory(this);
		initializeAjBuilder();
		return super.build(kind, ignored, monitor);
	}

	protected BatchImageBuilder getBatchImageBuilder() {
		isBuildIncremental = false;
		return new AjBatchImageBuilder(this);
	}
	
	protected IncrementalImageBuilder getIncrementalImageBuilder() {
		isBuildIncremental = true;
		return new AjIncrementalImageBuilder(this);
	}
	
	private void initializeAjBuilder() {
		// get hold of the project we are building (getProject()), and
		// create a world, weaver etc. if we do not already have them.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ICompilerAdapterFactory#getAdapter(org.eclipse.jdt.internal.compiler.Compiler)
	 */
	public ICompilerAdapter getAdapter(Compiler forCompiler) {
		// TODO Auto-generated method stub
		// to create an AjCompilerAdapter we need...
		// * a Compiler instance   [DONE - passed in]
		// * to know whether this is a batch or incremental build   [DONE]
		// * a world  -- local state, create if not available (or batch build)
		// * a weaver -- local state, create if not available (or batch build)
		// * an eclipse factory  -- create from AjLookupEnvironment, need to hide AjBuildManager field
		// * optional intermediate results requestor  -- not required (or use AjBuildNotifier)
		// * optional progress listener  -- build() is passed a progress monitor, JavaBuilder makes a BuildNotifier out of this, we
		//                                  should replace with AjBuildNotifier subclass - move creation to factory method in JavaBuilder??
		// * an output file name provider
		// * the set of binary source entries for this compile  -- from analyzing deltas
		// * the full set of binary source entries for the project -- from IAspectJProject
		// * the value of the -XNoWeave option  -- from aspectJProject.getOptions() (return a subclass of CompilerOptions).
		return null;
	}
	
	
}
