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
package org.aspectj.ajdt.internal.compiler;

import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.weaver.bcel.UnwovenClassFile;
import org.aspectj.weaver.bcel.UnwovenClassFileWithThirdPartyManagedBytecode;

/**
 * @author colyer
 *
 *	Adaptor for ClassFiles that lets them act as the bytecode repository
 *	for UnwovenClassFiles (asking a ClassFile for its bytes causes a 
 *	copy to be made).
 */
public class ClassFileBasedByteCodeProvider 
	   implements UnwovenClassFileWithThirdPartyManagedBytecode.IByteCodeProvider{
	
	private ClassFile cf;
		
	public ClassFileBasedByteCodeProvider(ClassFile cf) {
		this.cf = cf;
	}
		
	public byte[] getBytes() {
		return cf.getBytes();
	}
		
	public static UnwovenClassFile[] unwovenClassFilesFor(CompilationResult result, 
										            IOutputClassFileNameProvider nameProvider) {
		ClassFile[] cfs = result.getClassFiles();
		UnwovenClassFile[] ret = new UnwovenClassFile[result.compiledTypes.size()];
		int i=0;
		for (Object o : result.compiledTypes.keySet()) {
			char[] className = (char[]) o;
			ClassFile cf = (ClassFile) result.compiledTypes.get(className);
			// OPTIMIZE use char[] for classname
			ClassFileBasedByteCodeProvider p = new ClassFileBasedByteCodeProvider(cf);
			String fileName = nameProvider.getOutputClassFileName(cf.fileName(), result);
			ret[i++] = new UnwovenClassFileWithThirdPartyManagedBytecode(fileName, new String(className).replace('/', '.'), p);
		}
		return ret;
	}
		
}
