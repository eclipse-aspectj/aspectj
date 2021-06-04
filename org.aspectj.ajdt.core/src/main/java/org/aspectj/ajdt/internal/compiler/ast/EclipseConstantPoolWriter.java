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
package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.aspectj.weaver.ConstantPoolWriter;

/**
 * An implementation of the constant pool writer that speaks Eclipse.
 *
 * @author Andy Clement
 */
public class EclipseConstantPoolWriter implements ConstantPoolWriter {

	private ConstantPool constantPool;

	public EclipseConstantPoolWriter(ConstantPool constantPool) {
		this.constantPool = constantPool;
	}

	public int writeUtf8(String name) {
		return constantPool.literalIndex(name.toCharArray());
	}

}
