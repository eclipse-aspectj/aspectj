/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import org.aspectj.weaver.UnwovenClassFile;

public class BcelUnwovenClassFile extends UnwovenClassFile {

	public BcelUnwovenClassFile(String filename, byte[] bytes) {
		this.filename = filename;
		this.isModule = filename.toLowerCase().endsWith("module-info.java");
		this.bytes = bytes;
	}

	/** Use if the classname is known, saves a bytecode parse */
	public BcelUnwovenClassFile(String filename, String classname, byte[] bytes) {
		this.filename = filename;
		this.isModule = filename.toLowerCase().endsWith("module-info.class");
		this.className = classname;
		this.bytes = bytes;
	}

	public BcelClazz getJavaClass() {
		// XXX need to know when to make a new class and when not to
		// XXX this is an important optimization
		if (getBytes() == null) {
			System.out.println("no bytes for: " + getFilename());
			Thread.dumpStack();
		}
		return BcelClazz.asBcelClazz(Utility.makeJavaClass(filename, getBytes()));
		// if (javaClass == null) javaClass = Utility.makeJavaClass(filename, getBytes());
		// return javaClass;
	}

}
