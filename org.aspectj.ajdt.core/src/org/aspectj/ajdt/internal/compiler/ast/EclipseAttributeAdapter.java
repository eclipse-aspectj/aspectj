/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.weaver.AjAttribute;
import org.aspectj.org.eclipse.jdt.internal.compiler.IAttribute;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.ConstantPool;

public class EclipseAttributeAdapter implements IAttribute {
	AjAttribute attr;

	public EclipseAttributeAdapter(AjAttribute attr) {
		this.attr = attr;
	}

	public char[] getNameChars() {
		return attr.getNameChars();
	}

	public byte[] getAllBytes(short nameIndex, ConstantPool constantPool) {
		return attr.getAllBytes(nameIndex, new EclipseConstantPoolWriter(constantPool));
	}

}
