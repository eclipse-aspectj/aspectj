/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

/**
 * <p>https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.31 (JVMS 17, §4.7.31, the class-file format).
 * <p>Arrived with JEP 409 in Java 17
 * @see Attribute
 */
@SuppressWarnings("serial")
public final class PermittedSubclasses extends Attribute {
	private int numberOfClasses;
	private int[] classes; // CONSTANT_Class_info references

	public PermittedSubclasses(PermittedSubclasses c) {
		this(c.getNameIndex(), c.getLength(), c.getClasses(), c.getConstantPool());
	}

	public PermittedSubclasses(int nameIndex, int length, int[] classes, ConstantPool cp) {
		super(Constants.ATTR_PERMITTED_SUBCLASSES, nameIndex, length, cp);
		setClasses(classes);
	}

	PermittedSubclasses(int nameIndex, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
		this(nameIndex, length, (int[]) null, constant_pool);
		numberOfClasses = file.readUnsignedShort();
		classes = new int[numberOfClasses];
		for (int i = 0; i < numberOfClasses; i++) {
			classes[i] = file.readUnsignedShort();
		}
	}

	@Override
	public void accept(ClassVisitor v) {
		v.visitPermittedSubclasses(this);
	}

	@Override
	public final void dump(DataOutputStream file) throws IOException {
		super.dump(file);
		file.writeShort(numberOfClasses);
		for (int i = 0; i < numberOfClasses; i++) {
			file.writeShort(classes[i]);
		}
	}

	public final int[] getClasses() {
		return classes;
	}

	public final void setClasses(int[] permitted_classes) {
		this.classes = permitted_classes;
		numberOfClasses = (permitted_classes == null) ? 0 : permitted_classes.length;
	}

	public final String[] getClassesNames() {
		String[] result = new String[numberOfClasses];
		for (int i = 0; i < numberOfClasses; i++) {
			ConstantClass constantClass = (ConstantClass)cpool.getConstant(classes[i],Constants.CONSTANT_Class);
			result[i] = constantClass.getClassname(cpool);
		}
		return result;
	}

	@Override
	public final String toString() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < numberOfClasses; i++) {
			ConstantClass constantClass = (ConstantClass)cpool.getConstant(classes[i],Constants.CONSTANT_Class);
			buf.append(constantClass.getClassname(cpool)).append(" ");
		}
		return "PermittedSubclasses("+buf.toString().trim()+")";
	}
}
