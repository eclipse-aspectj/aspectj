package org.aspectj.apache.bcel.verifier;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.util.Stack;

import org.aspectj.apache.bcel.classfile.AnnotationDefault;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.AttributeUtils;
import org.aspectj.apache.bcel.classfile.BootstrapMethods;
import org.aspectj.apache.bcel.classfile.ClassVisitor;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.CodeException;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantClass;
import org.aspectj.apache.bcel.classfile.ConstantDouble;
import org.aspectj.apache.bcel.classfile.ConstantDynamic;
import org.aspectj.apache.bcel.classfile.ConstantFieldref;
import org.aspectj.apache.bcel.classfile.ConstantFloat;
import org.aspectj.apache.bcel.classfile.ConstantInteger;
import org.aspectj.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.aspectj.apache.bcel.classfile.ConstantInvokeDynamic;
import org.aspectj.apache.bcel.classfile.ConstantLong;
import org.aspectj.apache.bcel.classfile.ConstantMethodHandle;
import org.aspectj.apache.bcel.classfile.ConstantMethodType;
import org.aspectj.apache.bcel.classfile.ConstantMethodref;
import org.aspectj.apache.bcel.classfile.ConstantModule;
import org.aspectj.apache.bcel.classfile.ConstantNameAndType;
import org.aspectj.apache.bcel.classfile.ConstantPackage;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantString;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.ConstantValue;
import org.aspectj.apache.bcel.classfile.Deprecated;
import org.aspectj.apache.bcel.classfile.EnclosingMethod;
import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.InnerClass;
import org.aspectj.apache.bcel.classfile.InnerClasses;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LineNumber;
import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.LocalVariableTypeTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.MethodParameters;
import org.aspectj.apache.bcel.classfile.Module;
import org.aspectj.apache.bcel.classfile.ModuleMainClass;
import org.aspectj.apache.bcel.classfile.ModulePackages;
import org.aspectj.apache.bcel.classfile.NestHost;
import org.aspectj.apache.bcel.classfile.NestMembers;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.SourceFile;
import org.aspectj.apache.bcel.classfile.StackMap;
import org.aspectj.apache.bcel.classfile.StackMapEntry;
import org.aspectj.apache.bcel.classfile.Synthetic;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisTypeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisTypeAnnos;

/**
 * Traverses a JavaClass with another Visitor object 'piggy-backed' that is
 * applied to all components of a JavaClass object. I.e. this class supplies the
 * traversal strategy, other classes can make use of it.
 *
 * @version $Id: DescendingVisitor.java,v 1.4 2009/09/15 19:40:22 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class DescendingVisitor implements ClassVisitor {
	private JavaClass clazz;
	private ClassVisitor visitor;
	private Stack<Object> stack = new Stack<Object>();

	/**
	 * @return container of current entitity, i.e., predecessor during traversal
	 */
	public Object predecessor() {
		return predecessor(0);
	}

	/**
	 * @param level
	 *            nesting level, i.e., 0 returns the direct predecessor
	 * @return container of current entitity, i.e., predecessor during traversal
	 */
	public Object predecessor(int level) {
		int size = stack.size();

		if ((size < 2) || (level < 0))
			return null;
		else
			return stack.elementAt(size - (level + 2)); // size - 1 == current
	}

	/**
	 * @return current object
	 */
	public Object current() {
		return stack.peek();
	}

	/**
	 * @param clazz
	 *            Class to traverse
	 * @param visitor
	 *            visitor object to apply to all components
	 */
	public DescendingVisitor(JavaClass clazz, ClassVisitor visitor) {
		this.clazz = clazz;
		this.visitor = visitor;
	}

	/**
	 * Start traversal.
	 */
	public void visit() {
		clazz.accept(this);
	}

	@Override
	public void visitJavaClass(JavaClass clazz) {
		stack.push(clazz);
		clazz.accept(visitor);

		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++)
			fields[i].accept(this);

		Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++)
			methods[i].accept(this);

		AttributeUtils.accept(clazz.getAttributes(), visitor);
		// clazz.getAttributes().accept(this);
		clazz.getConstantPool().accept(this);
		stack.pop();
	}

	@Override
	public void visitField(Field field) {
		stack.push(field);
		field.accept(visitor);
		AttributeUtils.accept(field.getAttributes(), visitor);
		// field.getAttributes().accept(this);
		stack.pop();
	}

	@Override
	public void visitConstantValue(ConstantValue cv) {
		stack.push(cv);
		cv.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitMethod(Method method) {
		stack.push(method);
		method.accept(visitor);
		AttributeUtils.accept(method.getAttributes(), visitor);
		stack.pop();
	}

	@Override
	public void visitExceptionTable(ExceptionTable table) {
		stack.push(table);
		table.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitCode(Code code) {
		stack.push(code);
		code.accept(visitor);

		CodeException[] table = code.getExceptionTable();
		for (int i = 0; i < table.length; i++)
			table[i].accept(this);

		Attribute[] attributes = code.getAttributes();
		for (int i = 0; i < attributes.length; i++)
			attributes[i].accept(this);
		stack.pop();
	}

	@Override
	public void visitCodeException(CodeException ce) {
		stack.push(ce);
		ce.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitLineNumberTable(LineNumberTable table) {
		stack.push(table);
		table.accept(visitor);

		LineNumber[] numbers = table.getLineNumberTable();
		for (int i = 0; i < numbers.length; i++)
			numbers[i].accept(this);
		stack.pop();
	}

	@Override
	public void visitLineNumber(LineNumber number) {
		stack.push(number);
		number.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitLocalVariableTable(LocalVariableTable table) {
		stack.push(table);
		table.accept(visitor);

		LocalVariable[] vars = table.getLocalVariableTable();
		for (int i = 0; i < vars.length; i++)
			vars[i].accept(this);
		stack.pop();
	}

	@Override
	public void visitStackMap(StackMap table) {
		stack.push(table);
		table.accept(visitor);

		StackMapEntry[] vars = table.getStackMap();

		for (int i = 0; i < vars.length; i++)
			vars[i].accept(this);
		stack.pop();
	}

	@Override
	public void visitStackMapEntry(StackMapEntry var) {
		stack.push(var);
		var.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitLocalVariable(LocalVariable var) {
		stack.push(var);
		var.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantPool(ConstantPool cp) {
		stack.push(cp);
		cp.accept(visitor);

		Constant[] constants = cp.getConstantPool();
		for (int i = 1; i < constants.length; i++) {
			if (constants[i] != null)
				constants[i].accept(this);
		}

		stack.pop();
	}

	@Override
	public void visitConstantClass(ConstantClass constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantDouble(ConstantDouble constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantFieldref(ConstantFieldref constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantFloat(ConstantFloat constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantInteger(ConstantInteger constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantLong(ConstantLong constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantMethodref(ConstantMethodref constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantMethodHandle(ConstantMethodHandle constant) {
		throw new IllegalStateException("nyi");
	}

	@Override
	public void visitConstantMethodType(ConstantMethodType obj) {
		throw new IllegalStateException("nyi");
	}

	@Override
	public void visitConstantInvokeDynamic(ConstantInvokeDynamic obj) {
		throw new IllegalStateException("nyi");
	}

	@Override
	public void visitConstantDynamic(ConstantDynamic obj) {
		throw new IllegalStateException("nyi");
	}

	@Override
	public void visitBootstrapMethods(BootstrapMethods obj) {
		throw new IllegalStateException("nyi");
	}

	@Override
	public void visitConstantNameAndType(ConstantNameAndType constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantString(ConstantString constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantModule(ConstantModule constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantPackage(ConstantPackage constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitConstantUtf8(ConstantUtf8 constant) {
		stack.push(constant);
		constant.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitInnerClasses(InnerClasses ic) {
		stack.push(ic);
		ic.accept(visitor);

		InnerClass[] ics = ic.getInnerClasses();
		for (int i = 0; i < ics.length; i++)
			ics[i].accept(this);
		stack.pop();
	}

	@Override
	public void visitInnerClass(InnerClass inner) {
		stack.push(inner);
		inner.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitDeprecated(Deprecated attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitSignature(Signature attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	// J5SUPPORT:
	@Override
	public void visitEnclosingMethod(EnclosingMethod attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitRuntimeVisibleAnnotations(RuntimeVisAnnos attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitRuntimeInvisibleAnnotations(RuntimeInvisAnnos attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitRuntimeVisibleParameterAnnotations(RuntimeVisParamAnnos attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitRuntimeInvisibleParameterAnnotations(RuntimeInvisParamAnnos attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitRuntimeVisibleTypeAnnotations(RuntimeVisTypeAnnos attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitMethodParameters(MethodParameters attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitRuntimeInvisibleTypeAnnotations(RuntimeInvisTypeAnnos attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitAnnotationDefault(AnnotationDefault attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitLocalVariableTypeTable(LocalVariableTypeTable table) {
		stack.push(table);
		table.accept(visitor);

		LocalVariable[] vars = table.getLocalVariableTypeTable();
		for (int i = 0; i < vars.length; i++)
			vars[i].accept(this);
		stack.pop();
	}

	@Override
	public void visitSourceFile(SourceFile attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitSynthetic(Synthetic attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitUnknown(Unknown attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitModule(Module attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitModulePackages(ModulePackages attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitModuleMainClass(ModuleMainClass attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitNestHost(NestHost attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}

	@Override
	public void visitNestMembers(NestMembers attribute) {
		stack.push(attribute);
		attribute.accept(visitor);
		stack.pop();
	}
}
