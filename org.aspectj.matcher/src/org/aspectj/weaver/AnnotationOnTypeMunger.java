/* *******************************************************************
 * Copyright (c) 2005 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.IOException;

/**
 * Represents adding an annotation to a type
 */
public class AnnotationOnTypeMunger extends ResolvedTypeMunger {
	AnnotationAJ newAnnotation;

	public AnnotationOnTypeMunger(AnnotationAJ anno) {
		super(AnnotationOnType, null);
		newAnnotation = anno;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		throw new RuntimeException("unimplemented");
	}

	public AnnotationAJ getNewAnnotation() {
		return newAnnotation;
	}

	public boolean equals(Object other) {
		if (!(other instanceof AnnotationOnTypeMunger)) {
			return false;
		}
		AnnotationOnTypeMunger o = (AnnotationOnTypeMunger) other;
		// TODO does not check equality of annotation values
		return newAnnotation.getTypeSignature().equals(o.newAnnotation.getTypeSignature());
	}

	private volatile int hashCode = 0;

	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 37 * result + newAnnotation.getTypeSignature().hashCode();
			hashCode = result;
		}
		return hashCode;
	}

}
