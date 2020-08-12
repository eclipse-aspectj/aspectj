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
 * Represents the BootstrapMethods attribute in Java 7 classes.
 * 
 * @author Andy Clement
 */
public final class BootstrapMethods extends Attribute {

	// if 'isInPackedState' then this data needs unpacking
	private boolean isInPackedState = false;
	private byte[] data; // discarded once unpacked

	private int numBootstrapMethods;
	private BootstrapMethod[] bootstrapMethods;

	public BootstrapMethods(BootstrapMethods c) {
		this(c.getNameIndex(), c.getLength(), c.getBootstrapMethods(), c.getConstantPool());
	}

	public BootstrapMethods(int nameIndex, int length, BootstrapMethod[] lineNumberTable, ConstantPool constantPool) {
		super(Constants.ATTR_BOOTSTRAPMETHODS, nameIndex, length, constantPool);
		setBootstrapMethods(lineNumberTable);
		isInPackedState = false;
	}

	public final void setBootstrapMethods(BootstrapMethod[] bootstrapMethods) {
		this.data = null;
		this.isInPackedState = false;
		this.bootstrapMethods = bootstrapMethods;
		this.numBootstrapMethods = bootstrapMethods==null?0:bootstrapMethods.length;
	}

	BootstrapMethods(int name_index, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
		this(name_index, length, (BootstrapMethod[])null, constant_pool);
		data = new byte[length];
		file.readFully(data);
		isInPackedState = true;
	}
	
	public static class BootstrapMethod {
		private int bootstrapMethodRef;
		private int[] bootstrapArguments;

		BootstrapMethod(DataInputStream file) throws IOException {
			this(file.readUnsignedShort(), readBootstrapArguments(file));
		}
		
		private static int[] readBootstrapArguments(DataInputStream dis) throws IOException {
			int numBootstrapMethods = dis.readUnsignedShort();
			int[] bootstrapArguments = new int[numBootstrapMethods];
			for (int i=0;i<numBootstrapMethods;i++) {
				bootstrapArguments[i] = dis.readUnsignedShort();
			}
			return bootstrapArguments;
		}
		
		public BootstrapMethod(int bootstrapMethodRef, int[] bootstrapArguments) {
			this.bootstrapMethodRef = bootstrapMethodRef;
			this.bootstrapArguments = bootstrapArguments;
		}
		
		public int getBootstrapMethodRef() {
			return bootstrapMethodRef;
		}
		
		public int[] getBootstrapArguments() {
			return bootstrapArguments;
		}

		public final void dump(DataOutputStream file) throws IOException {
			file.writeShort(bootstrapMethodRef);
			int len = bootstrapArguments.length;
			file.writeShort(len);
			for (int bootstrapArgument : bootstrapArguments) {
				file.writeShort(bootstrapArgument);
			}
		}
		
		public final int getLength() {
			return 2 /*bootstrapMethodRef*/+
					2 /*number of arguments*/+
					2 * bootstrapArguments.length;
		}
		
	}
	
	// Unpacks the byte array into the table
	private void unpack() {
		if (isInPackedState) {
			try {
				ByteArrayInputStream bs = new ByteArrayInputStream(data);
				DataInputStream dis = new DataInputStream(bs);
				numBootstrapMethods = dis.readUnsignedShort();
				bootstrapMethods = new BootstrapMethod[numBootstrapMethods];
				for (int i = 0; i < numBootstrapMethods; i++) {
					bootstrapMethods[i] = new BootstrapMethod(dis);
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
		v.visitBootstrapMethods(this);
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
			int blen = bootstrapMethods.length;
			file.writeShort(blen);
			for (BootstrapMethod bootstrapMethod : bootstrapMethods) {
				bootstrapMethod.dump(file);
			}
		}
	}

	public final BootstrapMethod[] getBootstrapMethods() {
		unpack();
		return bootstrapMethods;
	}


	/**
	 * @return String representation.
	 */
	@Override
	public final String toString() {
		unpack();
		StringBuffer buf = new StringBuffer();
		StringBuffer line = new StringBuffer();

		for (int i = 0; i < numBootstrapMethods; i++) {
			BootstrapMethod bm = bootstrapMethods[i];
			line.append("BootstrapMethod[").append(i).append("]:");
			int ref = bm.getBootstrapMethodRef();
			ConstantMethodHandle mh = (ConstantMethodHandle)getConstantPool().getConstant(ref);
			line.append("#"+ref+":");
			line.append(ConstantMethodHandle.kindToString(mh.getReferenceKind()));
			line.append(" ").append(getConstantPool().getConstant(mh.getReferenceIndex()));
			int [] args = bm.getBootstrapArguments();
			line.append(" argcount:").append(args==null?0:args.length).append(" ");
			if (args!=null) {
				for (int arg : args) {
					line.append(arg).append("(").append(getConstantPool().getConstant(arg)).append(") ");
				}
			}
			
			
			if (i < numBootstrapMethods - 1) {
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
	public final int getNumBootstrapMethods () {
		unpack();
		return bootstrapMethods.length;
	}
}
