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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents adding an annotation to a type
 */
public class AnnotationOnTypeMunger extends ResolvedTypeMunger {
	AnnotationX newAnnotation;
	
	public AnnotationOnTypeMunger(AnnotationX anno) {
		super(AnnotationOnType,null);
		newAnnotation = anno;
	}

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("unimplemented");
	}


	public AnnotationX getNewAnnotation() {
		return newAnnotation;
	}

    public boolean equals(Object other) {
    	if (!(other instanceof AnnotationOnTypeMunger)) return false;
    	AnnotationOnTypeMunger o = (AnnotationOnTypeMunger)other;
    	return newAnnotation.getSignature().equals(o.newAnnotation.getSignature());
    }

    private volatile int hashCode = 0;
    public int hashCode() {
    	if (hashCode == 0) {
    		int result = 17;
    	    result = 37*result + newAnnotation.getSignature().hashCode();
    	    hashCode = result;
		}
	    return hashCode;
    }
	
}
