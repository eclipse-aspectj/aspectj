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

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.core.builder.BatchImageBuilder;

/**
 * @author colyer
 *
 * BatchImageBuilder used by IDEs (AJDT)
 */
public class AjBatchImageBuilder extends BatchImageBuilder {
	
	public AjBatchImageBuilder(AspectJBuilder builder) {
		super(builder);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.builder.AbstractImageBuilder#newCompiler()
	 */
	protected Compiler newCompiler() {
		// we must pass in an AjCompilerOptions instance...
		return super.newCompiler();
	}
}
