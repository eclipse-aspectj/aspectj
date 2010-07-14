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

import java.util.Iterator;

import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * @author colyer
 * 
 *         Clients implementing the IClassFileProvider can have a set of class files under their control woven by a weaver, by
 *         calling the weave(IClassFileProvider source) method. The contract is that a call to getRequestor().acceptResult() is
 *         providing a result for the class file most recently returned from the getClassFileIterator().
 */
public interface IClassFileProvider {

	/**
	 * Answer an iterator that can be used to iterate over a set of UnwovenClassFiles to be woven. During a weave, this method may
	 * be called multiple times.
	 * 
	 * @return iterator over UnwovenClassFiles.
	 */
	Iterator<UnwovenClassFile> getClassFileIterator();

	/**
	 * The client to which the woven results should be returned.
	 */
	IWeaveRequestor getRequestor();

	/**
	 * @return true if weaver should only do some internal munging as the one needed for @AspectJ aspectOf methods creation
	 */
	boolean isApplyAtAspectJMungersOnly();

}
