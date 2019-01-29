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
package org.aspectj.ajdt.internal.compiler;

/**
 * @author colyer
 *
 * Receives intermediate results from the compiler (post java-compile, but
 * before weaving).
 */
public interface IIntermediateResultsRequestor {
	void acceptResult(InterimCompilationResult intRes);
}
