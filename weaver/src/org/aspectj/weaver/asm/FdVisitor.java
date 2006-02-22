/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.asm;

 
import org.aspectj.org.objectweb.asm.AnnotationVisitor;
import org.aspectj.org.objectweb.asm.Attribute;
import org.aspectj.org.objectweb.asm.FieldVisitor;
import org.aspectj.weaver.AnnotationAJ;

/**
 * Constructed with an AsmField to 'fill in' with attributes and annotations 
 */
class FdVisitor implements FieldVisitor {
	AsmField field;
	public FdVisitor(AsmField rm) { field = rm;}

	public AnnotationVisitor visitAnnotation(String desc, boolean isVisible) {
		AnnotationAJ annotation = new AnnotationAJ(desc,isVisible);
		field.addAnAnnotation(annotation);
//		if (am.annotations==null) am.annotations = new ArrayList();
//		am.annotations.add(annotation); 
		return new AnnVisitor(annotation);
	}

	public void visitAttribute(Attribute arg0) {}
	public void visitEnd() {}
	
}