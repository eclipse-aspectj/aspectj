/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler;

import java.util.Map;

/**
 * @author colyer
 *
 * Implementors of this interface are called by the CompilerAdapter just before
 * it does a weave, and should return the set of binary source files (ie. those
 * resources from injars and inpath) that are to be included in the weave.
 * Used to manage incremental compilation of binary sources.
 */
public interface IBinarySourceProvider {

	Map /* fileName |-> List<UnwovenClassFile> */ getBinarySourcesForThisWeave();

}
