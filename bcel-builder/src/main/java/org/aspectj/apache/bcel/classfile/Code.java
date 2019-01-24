package org.aspectj.apache.bcel.classfile;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

/**
 * This class represents a chunk of Java byte code contained in a method. It is instantiated by the
 * <em>Attribute.readAttribute()</em> method. A <em>Code</em> attribute contains informations about operand stack, local variables,
 * byte code and the exceptions handled within this method.
 * 
 * This attribute has attributes itself, namely <em>LineNumberTable</em> which is used for debugging purposes and
 * <em>LocalVariableTable</em> which contains information about the local variables.
 * 
 * @version $Id: Code.java,v 1.9 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see Attribute
 * @see CodeException
 * @see LineNumberTable
 * @see LocalVariableTable
 */
public final class Code extends Attribute {
	private int maxStack; // Maximum size of stack used by this method
	private int maxLocals; // Number of local variables
	private byte[] code; // Actual byte code
	private CodeException[] exceptionTable;
	private Attribute[] attributes;
	private static final CodeException[] NO_EXCEPTIONS = new CodeException[] {};

	/**
	 * Initialize from another object. Note that both objects use the same references (shallow copy). Use copy() for a physical
	 * copy.
	 */
	public Code(Code c) {
		this(c.getNameIndex(), c.getLength(), c.getMaxStack(), c.getMaxLocals(), c.getCode(), c.getExceptionTable(), c
				.getAttributes(), c.getConstantPool());
	}

	Code(int name_index, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
		// Initialize with some default values which will be overwritten later
		this(name_index, length, file.readUnsignedShort(), file.readUnsignedShort(), (byte[]) null, (CodeException[]) null,
				(Attribute[]) null, constant_pool);

		int len = file.readInt();
		code = new byte[len]; // Read byte code
		file.readFully(code);

		/*
		 * Read exception table that contains all regions where an exception handler is active, i.e., a try { ... } catch() block.
		 */
		len = file.readUnsignedShort();
		if (len == 0) {
			exceptionTable = NO_EXCEPTIONS;
		} else {
			exceptionTable = new CodeException[len];
			for (int i = 0; i < len; i++) {
				exceptionTable[i] = new CodeException(file);
			}
		}

		// Read all attributes, eg: LineNumberTable, LocalVariableTable
		attributes = AttributeUtils.readAttributes(file, constant_pool);

		/*
		 * Adjust length, because of setAttributes in this(), s.b. length is incorrect, because it didn't take the internal
		 * attributes into account yet! Very subtle bug, fixed in 3.1.1.
		 */
		this.length = length;
	}

	/**
	 * @param name_index Index pointing to the name <em>Code</em>
	 * @param length Content length in bytes
	 * @param max_stack Maximum size of stack
	 * @param max_locals Number of local variables
	 * @param code Actual byte code
	 * @param exception_table Table of handled exceptions
	 * @param attributes Attributes of code: LineNumber or LocalVariable
	 * @param constant_pool Array of constants
	 */
	public Code(int name_index, int length, int max_stack, int max_locals, byte[] code, CodeException[] exception_table,
			Attribute[] attributes, ConstantPool constant_pool) {
		super(Constants.ATTR_CODE, name_index, length, constant_pool);

		this.maxStack = max_stack;
		this.maxLocals = max_locals;

		setCode(code);
		setExceptionTable(exception_table);
		setAttributes(attributes); // Overwrites length!
	}

	/**
	 * Called by objects that are traversing the nodes of the tree implicitely defined by the contents of a Java class. I.e., the
	 * hierarchy of methods, fields, attributes, etc. spawns a tree of objects.
	 * 
	 * @param v Visitor object
	 */
	@Override
	public void accept(ClassVisitor v) {
		v.visitCode(this);
	}

	/**
	 * Dump code attribute to file stream in binary format.
	 * 
	 * @param file Output file stream
	 * @throws IOException
	 */
	@Override
	public final void dump(DataOutputStream file) throws IOException {
		super.dump(file);

		file.writeShort(maxStack);
		file.writeShort(maxLocals);
		file.writeInt(code.length);
		file.write(code, 0, code.length);

		file.writeShort(exceptionTable.length);
		for (int i = 0; i < exceptionTable.length; i++) {
			exceptionTable[i].dump(file);
		}

		file.writeShort(attributes.length);
		for (int i = 0; i < attributes.length; i++) {
			attributes[i].dump(file);
		}
	}

	/**
	 * @return Collection of code attributes.
	 * @see Attribute
	 */
	public final Attribute[] getAttributes() {
		return attributes;
	}

	/**
	 * @return LineNumberTable of Code, if it has one
	 */
	public LineNumberTable getLineNumberTable() {
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].tag == Constants.ATTR_LINE_NUMBER_TABLE) {
				return (LineNumberTable) attributes[i];
			}
		}
		return null;
	}

	/**
	 * @return LocalVariableTable of Code, if it has one
	 */
	public LocalVariableTable getLocalVariableTable() {
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].tag == Constants.ATTR_LOCAL_VARIABLE_TABLE) {
				return (LocalVariableTable) attributes[i];
			}
		}
		return null;
	}

	/**
	 * @return Actual byte code of the method.
	 */
	public final byte[] getCode() {
		return code;
	}

	/**
	 * @return Table of handled exceptions.
	 * @see CodeException
	 */
	public final CodeException[] getExceptionTable() {
		return exceptionTable;
	}

	/**
	 * @return Number of local variables.
	 */
	public final int getMaxLocals() {
		return maxLocals;
	}

	/**
	 * @return Maximum size of stack used by this method.
	 */

	public final int getMaxStack() {
		return maxStack;
	}

	/**
	 * @return the internal length of this code attribute (minus the first 6 bytes) and excluding all its attributes
	 */
	private final int getInternalLength() {
		return 2 /* max_stack */+ 2 /* max_locals */+ 4 /* code length */
				+ (code == null ? 0 : code.length) /* byte-code */
				+ 2 /* exception-table length */
				+ 8 * (exceptionTable == null ? 0 : exceptionTable.length) /* exception table */
				+ 2 /* attributes count */;
	}

	/**
	 * @return the full size of this code attribute, minus its first 6 bytes, including the size of all its contained attributes
	 */
	private final int calculateLength() {
		int len = 0;
		if (attributes != null) {
			for (int i = 0; i < attributes.length; i++) {
				len += attributes[i].length + 6 /* attribute header size */;
			}
		}
		return len + getInternalLength();
	}

	/**
	 * @param attributes.
	 */
	public final void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
		length = calculateLength(); // Adjust length
	}

	/**
	 * @param code byte code
	 */
	public final void setCode(byte[] code) {
		this.code = code;
	}

	/**
	 * @param exception_table exception table
	 */
	public final void setExceptionTable(CodeException[] exception_table) {
		this.exceptionTable = exception_table;
	}

	/**
	 * @param max_locals maximum number of local variables
	 */
	public final void setMaxLocals(int max_locals) {
		this.maxLocals = max_locals;
	}

	/**
	 * @param max_stack maximum stack size
	 */
	public final void setMaxStack(int max_stack) {
		this.maxStack = max_stack;
	}

	/**
	 * @return String representation of code chunk.
	 */
	public final String toString(boolean verbose) {
		StringBuffer buf;

		buf = new StringBuffer("Code(max_stack = " + maxStack + ", max_locals = " + maxLocals + ", code_length = " + code.length
				+ ")\n" + Utility.codeToString(code, cpool, 0, -1, verbose));

		if (exceptionTable.length > 0) {
			buf.append("\nException handler(s) = \n" + "From\tTo\tHandler\tType\n");

			for (int i = 0; i < exceptionTable.length; i++) {
				buf.append(exceptionTable[i].toString(cpool, verbose) + "\n");
			}
		}

		if (attributes.length > 0) {
			buf.append("\nAttribute(s) = \n");

			for (int i = 0; i < attributes.length; i++) {
				buf.append(attributes[i].toString() + "\n");
			}
		}

		return buf.toString();
	}

	/**
	 * @return String representation of code chunk.
	 */
	@Override
	public final String toString() {
		return toString(true);
	}

	// /**
	// * @return deep copy of this attribute
	// */
	// public Attribute copy(ConstantPool constant_pool) {
	// Code c = (Code)clone();
	// c.code = (byte[])code.clone();
	// c.cpool = constant_pool;
	//  
	// c.exceptionTable = new CodeException[exceptionTable.length];
	// for(int i=0; i < exceptionTable.length; i++)
	// c.exceptionTable[i] = exceptionTable[i].copy();
	//
	// c.attributes = new Attribute[attributes.length];
	// for(int i=0; i < attributes.length; i++)
	// c.attributes[i] = attributes[i].copy(constant_pool);
	//
	// return c;
	// }

	/**
	 * Returns the same as toString(true) except that the attribute information isn't included (line numbers). Can be used to check
	 * whether two pieces of code are equivalent.
	 */
	public String getCodeString() {
		StringBuffer codeString = new StringBuffer();
		codeString.append("Code(max_stack = ").append(maxStack);
		codeString.append(", max_locals = ").append(maxLocals);
		codeString.append(", code_length = ").append(code.length).append(")\n");
		codeString.append(Utility.codeToString(code, cpool, 0, -1, true));
		if (exceptionTable.length > 0) {
			codeString.append("\n").append("Exception entries =  ").append(exceptionTable.length).append("\n");
			for (int i = 0; i < exceptionTable.length; i++) {
				CodeException exc = exceptionTable[i];
				int type = exc.getCatchType();
				String name = "finally";
				if (type != 0) {
					name = this.cpool.getConstantString(type, Constants.CONSTANT_Class);
				}
				codeString.append(name).append("[");
				codeString.append(exc.getStartPC()).append(">").append(exc.getEndPC()).append("]\n");
			}
		}
		return codeString.toString();
	}
}
