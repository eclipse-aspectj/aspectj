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

import java.util.ArrayList;
import java.util.Collections;

import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.org.objectweb.asm.AnnotationVisitor;
import org.aspectj.org.objectweb.asm.Attribute;
import org.aspectj.org.objectweb.asm.Label;
import org.aspectj.org.objectweb.asm.MethodVisitor;

/**
 * Constructed with an AsmMethod to 'fill in' with attributes and annotations.
 * Bit of a shame we can't "terminate" before visiting the code as we know
 * we aren't interested in any of it.
 */
class MethVisitor implements MethodVisitor {
	AsmMethod am;
	
	public MethVisitor(AsmMethod rm) { am = rm;}

	public AnnotationVisitor visitAnnotationDefault() {
		return new EmptyVisitor(); // means we are ignoring default values - is that a problem?
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean isVisible) {
		AnnotationAJ annotation = new AnnotationAJ(desc,isVisible);
		am.addAnAnnotation(annotation);
		return new AnnVisitor(annotation);
	}

	public void visitAttribute(Attribute attr) {
		if (am.attributes==Collections.EMPTY_LIST) am.attributes = new ArrayList();
		am.attributes.add(attr);
	}
	
	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		return null; // AJ doesnt support these yet
	}
	
	public void visitCode() {}
	public void visitInsn(int arg0) {}
	public void visitIntInsn(int arg0, int arg1) {}
	public void visitVarInsn(int arg0, int arg1) {}
	public void visitTypeInsn(int arg0, String arg1) {}
	public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {}
	public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {}
	public void visitJumpInsn(int arg0, Label arg1) {}
	public void visitLabel(Label arg0) {}
	public void visitLdcInsn(Object arg0) {}
	public void visitIincInsn(int arg0, int arg1) {}
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {}
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {}
	public void visitMultiANewArrayInsn(String arg0, int arg1) {}
	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {}
	public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3, Label arg4, int arg5) {}
	public void visitLineNumber(int arg0, Label arg1) {}
	public void visitMaxs(int arg0, int arg1) {}
	public void visitEnd() {}
	
}