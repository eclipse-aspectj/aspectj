/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver;

import org.aspectj.bridge.ISourceLocation;

/**
 * Clients can pass a single cross-reference handler to the weaver on construction of a BcelWorld. Any cross-references detected
 * during munging will be notified to the handler.
 */
public interface ICrossReferenceHandler {

	void addCrossReference(ISourceLocation from, ISourceLocation to, String kind, boolean runtimeTest);

}
