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
 * This class represents a table of line numbers for debugging purposes. This attribute is used by the <em>Code</em> attribute. It
 * contains pairs of PCs and line numbers.
 * 
 * @version $Id: LineNumberTable.java,v 1.8 2009/09/15 19:40:12 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see Code changes: asc Feb06 Made unpacking lazy
 */
public final class LineNumberTable extends Attribute {

	// if 'isInPackedState' then this data needs unpacking
	private boolean isInPackedState = false;
	private byte[] data; // discarded once unpacked

	private int tableLength;
	private LineNumber[] table;

	/*
	 * Initialize from another object. Note that both objects use the same references (shallow copy). Use copy() for a physical
	 * copy.
	 */
	public LineNumberTable(LineNumberTable c) {
		this(c.getNameIndex(), c.getLength(), c.getLineNumberTable(), c.getConstantPool());
	}

	public LineNumberTable(int nameIndex, int length, LineNumber[] lineNumberTable, ConstantPool constantPool) {
		super(Constants.ATTR_LINE_NUMBER_TABLE, nameIndex, length, constantPool);
		setLineNumberTable(lineNumberTable);
		isInPackedState = false;
	}

	/**
	 * Construct object from file stream.
	 * 
	 * @param name_index Index of name
	 * @param length Content length in bytes
	 * @param file Input stream
	 * @throws IOException
	 * @param constant_pool Array of constants
	 */
	LineNumberTable(int name_index, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
		this(name_index, length, (LineNumber[]) null, constant_pool);
		data = new byte[length];
		file.readFully(data);
		isInPackedState = true;
		// assert(bytesRead==length)
	}

	// Unpacks the byte array into the table
	private void unpack() {
		if (isInPackedState) {
			try {
				ByteArrayInputStream bs = new ByteArrayInputStream(data);
				DataInputStream dis = new DataInputStream(bs);
				tableLength = (dis.readUnsignedShort());
				table = new LineNumber[tableLength];
				for (int i = 0; i < tableLength; i++) {
					table[i] = new LineNumber(dis);
				}
				dis.close();
				data = null; // throw it away now
			} catch (IOException e) {
				throw new RuntimeException("Unpacking of LineNumberTable attribute failed");
			}
			isInPackedState = false;
		}
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
		v.visitLineNumberTable(this);
	}

	/**
	 * Dump line number table attribute to file stream in binary format.
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
			file.writeShort(tableLength);
			for (int i = 0; i < tableLength; i++) {
				table[i].dump(file);
			}
		}
	}

	/**
	 * @return Array of (pc offset, line number) pairs.
	 */
	public final LineNumber[] getLineNumberTable() {
		unpack();
		return table;
	}

	/**
	 * @param line_number_table.
	 */
	public final void setLineNumberTable(LineNumber[] line_number_table) {
		this.data = null;
		this.isInPackedState = false;
		this.table = line_number_table;
		this.tableLength = (line_number_table == null) ? 0 : line_number_table.length;
	}

	/**
	 * @return String representation.
	 */
	@Override
	public final String toString() {
		unpack();
		StringBuffer buf = new StringBuffer();
		StringBuffer line = new StringBuffer();

		for (int i = 0; i < tableLength; i++) {
			line.append(table[i].toString());

			if (i < tableLength - 1) {
				line.append(", ");
			}

			if (line.length() > 72) {
				line.append('\n');
				buf.append(line);
				line.setLength(0);
			}
		}

		buf.append(line);

		return buf.toString();
	}

	/**
	 * Map byte code positions to source code lines.
	 * 
	 * @param pos byte code offset
	 * @return corresponding line in source code
	 */
	public int getSourceLine(int pos) {
		unpack();
		int l = 0, r = tableLength - 1;

		if (r < 0) // array is empty
			return -1;

		int min_index = -1, min = -1;

		/*
		 * Do a binary search since the array is ordered.
		 */
		do {
			int i = (l + r) / 2;
			int j = table[i].getStartPC();

			if (j == pos)
				return table[i].getLineNumber();
			else if (pos < j) // else constrain search area
				r = i - 1;
			else
				// pos > j
				l = i + 1;

			/*
			 * If exact match can't be found (which is the most common case) return the line number that corresponds to the greatest
			 * index less than pos.
			 */
			if (j < pos && j > min) {
				min = j;
				min_index = i;
			}
		} while (l <= r);

		/*
		 * It's possible that we did not find any valid entry for the bytecode offset we were looking for.
		 */
		if (min_index < 0)
			return -1;

		return table[min_index].getLineNumber();
	}

	/**
	 * @return deep copy of this attribute
	 */
	// @Override
	// public Attribute copy(ConstantPool constant_pool) {
	// unpack();
	// LineNumberTable newTable = (LineNumberTable) clone();
	// newTable.table = new LineNumber[tableLength];
	// for (int i = 0; i < tableLength; i++) {
	// newTable.table[i] = table[i].copy();
	// }
	// newTable.cpool = constant_pool;
	// return newTable;
	// }
	public final int getTableLength() {
		unpack();
		return tableLength;
	}
}
