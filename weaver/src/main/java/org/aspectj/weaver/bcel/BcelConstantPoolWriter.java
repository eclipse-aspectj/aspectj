/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 * Andy Clement (SpringSource)
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.weaver.ConstantPoolWriter;

/**
 * An implementation of the constant pool writer that speaks Bcel.
 *
 * @author Andy Clement
 */
class BcelConstantPoolWriter implements ConstantPoolWriter {

	ConstantPool pool;

	public BcelConstantPoolWriter(ConstantPool pool) {
		this.pool = pool;
	}

	public int writeUtf8(String name) {
		return pool.addUtf8(name);
	}

}
