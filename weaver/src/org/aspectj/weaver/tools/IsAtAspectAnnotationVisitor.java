/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Eugene Kuleshov, Ron Bodkin    initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.tools;

import org.aspectj.org.objectweb.asm.AnnotationVisitor;
import org.aspectj.org.objectweb.asm.Attribute;
import org.aspectj.org.objectweb.asm.ClassVisitor;
import org.aspectj.org.objectweb.asm.FieldVisitor;
import org.aspectj.org.objectweb.asm.Label;
import org.aspectj.org.objectweb.asm.MethodVisitor;

// should extend EmptyVisitor but it's not currently included with AspectJ's ASM...
public class IsAtAspectAnnotationVisitor implements ClassVisitor, FieldVisitor,
		MethodVisitor, AnnotationVisitor {
	private boolean isAspect = false;

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if ("Lorg/aspectj/lang/annotation/Aspect;".equals(desc)) {
			isAspect = true;
		}
		return this;
	}

	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return null;
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return null;
	}

	public boolean isAspect() {
		return isAspect;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
	}

	public void visitSource(String source, String debug) {
	}

	public void visitOuterClass(String owner, String name, String desc) {
	}

	public void visitAttribute(Attribute attr) {
	}

	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {
	}

	public void visitEnd() {
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return this;
	}

	public AnnotationVisitor visitParameterAnnotation(int parameter,
			String desc, boolean visible) {
		return this;
	}

	public void visitCode() {
	}

	public void visitInsn(int opcode) {
	}

	public void visitIntInsn(int opcode, int operand) {
	}

	public void visitVarInsn(int opcode, int var) {
	}

	public void visitTypeInsn(int opcode, String desc) {
	}

	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
	}

	public void visitJumpInsn(int opcode, Label label) {
	}

	public void visitLabel(Label label) {
	}

	public void visitLdcInsn(Object cst) {
	}

	public void visitIincInsn(int var, int increment) {
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label labels[]) {
	}

	public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {
	}

	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
	}

	public void visitLineNumber(int line, Label start) {
	}

	public void visitMaxs(int maxStack, int maxLocals) {
	}

	public void visit(String name, Object value) {
	}

	public void visitEnum(String name, String desc, String value) {
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return this;
	}

	public AnnotationVisitor visitArray(String name) {
		return this;
	}
}
