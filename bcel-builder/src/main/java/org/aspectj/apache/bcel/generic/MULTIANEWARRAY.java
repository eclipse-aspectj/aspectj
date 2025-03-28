package org.aspectj.apache.bcel.generic;

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
 *        Apache Software Foundation (https://www.apache.org/)."
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
 * <https://www.apache.org/>.
 */
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.ExceptionConstants;
import org.aspectj.apache.bcel.classfile.ConstantPool;

/**
 * MULTIANEWARRAY - Create new mutidimensional array of references
 *
 * <PRE>
 * Stack: ..., count1, [count2, ...] -&gt; ..., arrayref
 * </PRE>
 *
 * @version $Id: MULTIANEWARRAY.java,v 1.4 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class MULTIANEWARRAY extends InstructionCP {
	private short dimensions;

	public MULTIANEWARRAY(int index, short dimensions) {
		super(Constants.MULTIANEWARRAY, index);
		this.dimensions = dimensions;
	}

	/**
	 * Dump instruction as byte code to stream out.
	 *
	 * @param out Output stream
	 */
	public void dump(DataOutputStream out) throws IOException {
		out.writeByte(opcode);
		out.writeShort(index);
		out.writeByte(dimensions);
	}

	/**
	 * Read needed data (i.e., no. dimension) from file.
	 */
	// protected void initFromFile(ByteSequence bytes, boolean wide)
	// throws IOException
	// {
	// super.initFromFile(bytes, wide);
	// dimensions = bytes.readByte();
	// // length = 4;
	// }

	/**
	 * @return number of dimensions to be created
	 */
	public final short getDimensions() {
		return dimensions;
	}

	/**
	 * @return mnemonic for instruction
	 */
	public String toString(boolean verbose) {
		return super.toString(verbose) + " " + index + " " + dimensions;
	}

	/**
	 * @return mnemonic for instruction with symbolic references resolved
	 */
	public String toString(ConstantPool cp) {
		return super.toString(cp) + " " + dimensions;
	}

	/**
	 * Also works for instructions whose stack effect depends on the constant pool entry they reference.
	 *
	 * @return Number of words consumed from stack by this instruction
	 */
	public int consumeStack(ConstantPool cpg) {
		return dimensions;
	}

	public Class[] getExceptions() {
		Class[] cs = new Class[2 + ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length];

		System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0, cs, 0,
				ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);

		cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length + 1] = ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION;
		cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length] = ExceptionConstants.ILLEGAL_ACCESS_ERROR;

		return cs;
	}

	public ObjectType getLoadClassType(ConstantPool cpg) {
		Type t = getType(cpg);

		if (t instanceof ArrayType) {
			t = ((ArrayType) t).getBasicType();
		}

		return (t instanceof ObjectType) ? (ObjectType) t : null;
	}

	// /**
	// * Call corresponding visitor method(s). The order is:
	// * Call visitor methods of implemented interfaces first, then
	// * call methods according to the class hierarchy in descending order,
	// * i.e., the most specific visitXXX() call comes last.
	// *
	// * @param v Visitor object
	// */
	// public void accept(Visitor v) {
	// v.visitLoadClass(this);
	// v.visitAllocationInstruction(this);
	// v.visitExceptionThrower(this);
	// v.visitTypedInstruction(this);
	// v.visitCPInstruction(this);
	// v.visitMULTIANEWARRAY(this);
	// }

	public boolean equals(Object other) {
		if (!(other instanceof MULTIANEWARRAY)) {
			return false;
		}
		MULTIANEWARRAY o = (MULTIANEWARRAY) other;
		return o.opcode == opcode && o.index == index && o.dimensions == dimensions;
	}

	public int hashCode() {
		return opcode * 37 + index * (dimensions + 17);
	}
}
