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

import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;

/**
 * @author colyer
 *
 * XXX lightweight subclass of ClassFile that only genuinely supports fileName and getBytes
 * operations. This nasty hack enables us to keep the rest of the implementation much simpler.
 */
public class AjClassFile extends ClassFile {
	char[] filename;
	byte[] bytes;

	public AjClassFile(char[] fileName, byte[] byteCodes) {
		this.filename = fileName;
		bytes = byteCodes;
	}

	public char[] fileName() {
		return filename;
	}

	public byte[] getBytes() {
		return bytes;
	}
}
