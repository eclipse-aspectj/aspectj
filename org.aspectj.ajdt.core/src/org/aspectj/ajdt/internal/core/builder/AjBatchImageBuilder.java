/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.core.CompilationGroup;
import org.aspectj.org.eclipse.jdt.internal.core.builder.BatchImageBuilder;
import org.aspectj.org.eclipse.jdt.internal.core.builder.SourceFile;

/**
 * @author colyer
 *
 * BatchImageBuilder used by IDEs (AJDT)
 */
public class AjBatchImageBuilder extends BatchImageBuilder {
	
	public AjBatchImageBuilder(AspectJBuilder builder) {
		super(builder, true, CompilationGroup.MAIN);
	}
	
	@Override
	public void acceptResult(CompilationResult result) {
		if ((result.getCompilationUnit() != null) && (result.getCompilationUnit() instanceof SourceFile)) {
			super.acceptResult(result);
		} else {
			// it's a file originating from binary source...
			// we need to handle it ourselves
			// TODO handle binary source output
		}
	}
}
