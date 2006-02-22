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

class EmptyVisitor implements AnnotationVisitor {

	public void visit(String arg0, Object arg1) {}

	public void visitEnum(String arg0, String arg1, String arg2) {}

	public AnnotationVisitor visitAnnotation(String arg0, String arg1) {return this;}

	public AnnotationVisitor visitArray(String arg0) {return this;}

	public void visitEnd() {}
	
}