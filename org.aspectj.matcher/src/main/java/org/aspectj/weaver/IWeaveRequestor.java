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
package org.aspectj.weaver;

/**
 * @author colyer
 * 
 *         This interface is implemented by clients driving weaving through the IClassFileProvider interface. It is used by the
 *         weaver to return woven class file results back to the client. The client can correlate weave results with inputs since it
 *         knows the last UnwovenClassFile returned by its iterator.
 */
public interface IWeaveRequestor {

	/*
	 * A class file resulting from a weave (yes, even though the type name says "unwoven"...).
	 */
	void acceptResult(IUnwovenClassFile result);

	// various notifications to the requestor about our progress...
	void processingReweavableState();

	void addingTypeMungers();

	void weavingAspects();

	void weavingClasses();

	void weaveCompleted();
}
