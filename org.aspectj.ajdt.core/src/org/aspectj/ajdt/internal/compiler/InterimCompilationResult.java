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

import java.util.List;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * Holds a compilation result produced by the Java compilation phase, ready for weaving in the weave phase. NOTE: This class defines
 * equality based solely on the fileName of the compiled file (not the bytecodes produced for example)
 */
public class InterimCompilationResult {

	private CompilationResult result;
	private UnwovenClassFile[] unwovenClassFiles; // longer term would be nice not to have two copies of

	// the byte codes, one in result.classFiles and another
	// in unwovenClassFiles;

	public InterimCompilationResult(CompilationResult cr, IOutputClassFileNameProvider np) {
		result = cr;
		unwovenClassFiles = ClassFileBasedByteCodeProvider.unwovenClassFilesFor(result, np);
	}

	public InterimCompilationResult(CompilationResult cr, List<UnwovenClassFile> ucfList) {
		result = cr;
		unwovenClassFiles = new UnwovenClassFile[ucfList.size()];
		for (int i = 0; i < ucfList.size(); i++) {
			UnwovenClassFile element = ucfList.get(i);
			unwovenClassFiles[i] = element;
			AjClassFile ajcf = new AjClassFile(element.getClassNameAsChars(), element.getBytes());
			result.record(ajcf.fileName(), ajcf);
		}
	}

	public CompilationResult result() {
		return result;
	}

	public UnwovenClassFile[] unwovenClassFiles() {
		return unwovenClassFiles;
	}

	public String fileName() {
		return new String(result.fileName);
	}

	public boolean equals(Object other) {
		if (other == null || !(other instanceof InterimCompilationResult)) {
			return false;
		}
		InterimCompilationResult ir = (InterimCompilationResult) other;
		return fileName().equals(ir.fileName());
	}

	public int hashCode() {
		return fileName().hashCode();
	}

}
