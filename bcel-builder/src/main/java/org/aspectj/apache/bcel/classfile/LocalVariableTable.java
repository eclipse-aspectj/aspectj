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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

/**
 * This class represents collection of local variables in a method. This attribute is contained in the <em>Code</em> attribute.
 * 
 * @version $Id: LocalVariableTable.java,v 1.8 2009/09/15 19:40:12 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see Code
 * @see LocalVariable Updates: Andy 14Feb06 - Made unpacking of the data lazy, depending on someone actually asking for it.
 */
public class LocalVariableTable extends Attribute {

	// if 'isInPackedState' then this data needs unpacking
	private boolean isInPackedState = false;
	private byte[] data;

	private int localVariableTableLength;
	private LocalVariable[] localVariableTable;

	/**
	 * Initialize from another object. Note that both objects use the same references (shallow copy). Use copy() for a physical
	 * copy.
	 */
	public LocalVariableTable(LocalVariableTable c) {
		this(c.getNameIndex(), c.getLength(), c.getLocalVariableTable(), c.getConstantPool());
	}

	/**
	 * @param name_index Index in constant pool to `LocalVariableTable'
	 * @param length Content length in bytes
	 * @param local_variable_table Table of local variables
	 * @param constant_pool Array of constants
	 */
	public LocalVariableTable(int name_index, int length, LocalVariable[] local_variable_table, ConstantPool constant_pool) {
		super(Constants.ATTR_LOCAL_VARIABLE_TABLE, name_index, length, constant_pool);
		setLocalVariableTable(local_variable_table);
	}

	/**
	 * Construct object from file stream.
	 * 
	 * @param name_index Index in constant pool
	 * @param length Content length in bytes
	 * @param file Input stream
	 * @param constant_pool Array of constants
	 * @throws IOException
	 */
	LocalVariableTable(int name_index, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
		super(Constants.ATTR_LOCAL_VARIABLE_TABLE, name_index, length, constant_pool);
		data = new byte[length];
		file.readFully(data);
		isInPackedState = true;
		// assert(bytesRead==length)
	}

	/**
	 * Called by objects that are traversing the nodes of the tree implicitely defined by the contents of a Java class. I.e., the
	 * hierarchy of methods, fields, attributes, etc. spawns a tree of objects.
	 * 
	 * @param v Visitor object
	 */
	@Override
	public void accept(ClassVisitor v) {
		unpack();
		v.visitLocalVariableTable(this);
	}

	/**
	 * Dump local variable table attribute to file stream in binary format.
	 * 
	 * @param file Output file stream
	 * @throws IOException
	 */
	@Override
	public final void dump(DataOutputStream file) throws IOException {
		super.dump(file);
		if (isInPackedState) {
			file.write(data);
		} else {
			file.writeShort(localVariableTableLength);
			for (int i = 0; i < localVariableTableLength; i++)
				localVariableTable[i].dump(file);
		}
	}

	/**
	 * @return Array of local variables of method.
	 */
	public final LocalVariable[] getLocalVariableTable() {
		unpack();
		return localVariableTable;
	}

	/**
	 * @return first matching variable using index
	 */
	public final LocalVariable getLocalVariable(int index) {
		unpack();
		for (int i = 0; i < localVariableTableLength; i++) {
			if (localVariableTable[i] != null && localVariableTable[i].getIndex() == index) {
				return localVariableTable[i];
			}
		}
		return null;
	}

	public final void setLocalVariableTable(LocalVariable[] local_variable_table) {
		data = null;
		isInPackedState = false;
		this.localVariableTable = local_variable_table;
		localVariableTableLength = (local_variable_table == null) ? 0 : local_variable_table.length;
	}

	/**
	 * @return String representation.
	 */
	@Override
	public final String toString() {
		StringBuffer buf = new StringBuffer("");
		unpack();
		for (int i = 0; i < localVariableTableLength; i++) {
			buf.append(localVariableTable[i].toString());

			if (i < localVariableTableLength - 1)
				buf.append('\n');
		}

		return buf.toString();
	}

	/**
	 * @return deep copy of this attribute
	 */
	// public Attribute copy(ConstantPool constant_pool) {
	// unpack();
	// LocalVariableTable c = (LocalVariableTable) clone();
	//
	// c.localVariableTable = new LocalVariable[localVariableTableLength];
	// for (int i = 0; i < localVariableTableLength; i++)
	// c.localVariableTable[i] = localVariableTable[i].copy();
	//
	// c.cpool = constant_pool;
	// return c;
	// }
	public final int getTableLength() {
		unpack();
		return localVariableTableLength;
	}

	// ---
	// Unpacks the byte array into the table
	private void unpack() {
		if (!isInPackedState)
			return;
		try {
			ByteArrayInputStream bs = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bs);
			localVariableTableLength = (dis.readUnsignedShort());
			localVariableTable = new LocalVariable[localVariableTableLength];
			for (int i = 0; i < localVariableTableLength; i++)
				localVariableTable[i] = new LocalVariable(dis, cpool);
			dis.close();
			data = null; // throw it away now
		} catch (IOException e) {
			throw new RuntimeException("Unpacking of LocalVariableTable attribute failed");
		}
		isInPackedState = false;
	}
}
