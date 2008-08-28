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

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantClass;
import org.aspectj.apache.bcel.classfile.ConstantPool;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import com.sun.org.apache.bcel.internal.generic.LDC;

/**
 * Slass for instructions that use an index into the constant pool such as LDC, INVOKEVIRTUAL, etc.
 * 
 * @see ConstantPoolGen
 * @see LDC
 * @see INVOKEVIRTUAL
 * 
 * @version $Id: InstructionCP.java,v 1.3 2008/08/28 00:05:49 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class InstructionCP extends Instruction {
	protected int index; // index to constant pool

	public InstructionCP(short opcode, int index) {
		super(opcode);
		this.index = index;
	}

	public void dump(DataOutputStream out) throws IOException {
		if (opcode == LDC_W && index < 256) {
			out.writeByte(LDC);
			out.writeByte(index);
		} else {
			out.writeByte(opcode);
			if (Constants.iLen[opcode] == 2) {
				if (index > 255) {
					throw new IllegalStateException();
				}
				out.writeByte(index);
			} else {
				out.writeShort(index);
			}
		}
	}

	public int getLength() {
		if (opcode == LDC_W && index < 256) {
			return 2;
		} else {
			return super.getLength();
		}
	}

	/**
	 * Long output format:
	 * 
	 * &lt;name of opcode&gt; "["&lt;opcode number&gt;"]" "("&lt;length of instruction&gt;")" "&lt;"&lt; constant pool
	 * index&gt;"&gt;"
	 * 
	 * @param verbose long/short format switch
	 * @return mnemonic for instruction
	 */
	public String toString(boolean verbose) {
		return super.toString(verbose) + " " + index;
	}

	/**
	 * @return mnemonic for instruction with symbolic references resolved
	 */
	public String toString(ConstantPool cp) {
		Constant c = cp.getConstant(index);
		String str = cp.constantToString(c);

		if (c instanceof ConstantClass) {
			str = str.replace('.', '/');
		}

		return org.aspectj.apache.bcel.Constants.OPCODE_NAMES[opcode] + " " + str;
	}

	/**
	 * @return index in constant pool referred by this instruction.
	 */
	public final int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
		if (this.index > 255 && opcode == LDC) {
			// promote it
			opcode = LDC_W;
		}
	}

	public Type getType(ConstantPool cpg) {
		switch (cpg.getConstant(index).getTag()) {
		case CONSTANT_String:
			return Type.STRING;
		case CONSTANT_Float:
			return Type.FLOAT;
		case CONSTANT_Integer:
			return Type.INT;
		case CONSTANT_Long:
			return Type.LONG;
		case CONSTANT_Double:
			return Type.DOUBLE;
		case CONSTANT_Class:
			String name = cpg.getConstantString_CONSTANTClass(index);
			// ConstantPool cp = cpg.getConstantPool();
			// String name = cp.getConstantString(index, CONSTANT_Class);
			if (!name.startsWith("[")) {
				StringBuffer sb = new StringBuffer();
				sb.append("L").append(name).append(";");
				return Type.getType(sb.toString());
			} else {
				return Type.getType(name);
			}
		default: // Never reached
			throw new RuntimeException("Unknown or invalid constant type at " + index);
		}
	}

	public Object getValue(ConstantPool cpg) {
		org.aspectj.apache.bcel.classfile.Constant c = cpg.getConstant(index);

		switch (c.getTag()) {
		case org.aspectj.apache.bcel.Constants.CONSTANT_String:
			int i = ((org.aspectj.apache.bcel.classfile.ConstantString) c).getStringIndex();
			c = cpg.getConstant(i);
			return ((org.aspectj.apache.bcel.classfile.ConstantUtf8) c).getBytes();

		case org.aspectj.apache.bcel.Constants.CONSTANT_Float:
			return new Float(((org.aspectj.apache.bcel.classfile.ConstantFloat) c).getBytes());

		case org.aspectj.apache.bcel.Constants.CONSTANT_Integer:
			return new Integer(((org.aspectj.apache.bcel.classfile.ConstantInteger) c).getBytes());

			// from ldc2_w:
		case org.aspectj.apache.bcel.Constants.CONSTANT_Long:
			return new Long(((org.aspectj.apache.bcel.classfile.ConstantLong) c).getBytes());

		case org.aspectj.apache.bcel.Constants.CONSTANT_Double:
			return new Double(((org.aspectj.apache.bcel.classfile.ConstantDouble) c).getBytes());
		default: // Never reached
			throw new RuntimeException("Unknown or invalid constant type at " + index);
		}
	}

	public Class[] getExceptions() {
		return org.aspectj.apache.bcel.ExceptionConstants.EXCS_STRING_RESOLUTION;
	}
}
