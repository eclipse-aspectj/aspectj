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
package org.aspectj.weaver.bcel;

/**
 * @author colyer This subclass of UnwovenClassFile allows a third-party to manage the actual bytes that comprise the class. This
 *         means the third party can return a reference to an existing array, or create the bytes on demand, or apply any other
 *         strategy that makes sense. By refering to bytes held elsewhere, the goal is to reduce the overall memory consumption by
 *         not holding a copy.
 */
public class UnwovenClassFileWithThirdPartyManagedBytecode extends UnwovenClassFile {

	IByteCodeProvider provider;

	public interface IByteCodeProvider {
		byte[] getBytes();
	}

	// OPTIMIZE make classname an input char[]
	public UnwovenClassFileWithThirdPartyManagedBytecode(String filename, String classname, IByteCodeProvider provider) {
		super(filename, classname, null);
		this.provider = provider;
	}

	public byte[] getBytes() {
		return provider.getBytes();
	}
}
