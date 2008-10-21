/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
