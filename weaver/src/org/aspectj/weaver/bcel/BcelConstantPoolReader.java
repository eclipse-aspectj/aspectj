/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 * Andy Clement (SpringSource)
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.weaver.ConstantPoolReader;

/**
 * An implementation of the constant pool reader that speaks Bcel.
 * 
 * @author Andy Clement
 */
public class BcelConstantPoolReader implements ConstantPoolReader {

	private ConstantPool constantPool;

	public BcelConstantPoolReader(ConstantPool constantPool) {
		this.constantPool = constantPool;
	}

	public String readUtf8(int cpIndex) {
		return constantPool.getConstantUtf8(cpIndex).getValue();
	}

}
