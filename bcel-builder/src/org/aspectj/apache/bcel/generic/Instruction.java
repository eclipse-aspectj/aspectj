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
import java.io.Serializable;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.util.ByteSequence;

/**
 * Abstract super class for all Java byte codes.
 * 
 * @version $Id: Instruction.java,v 1.10 2011/04/05 15:15:33 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class Instruction implements Cloneable, Serializable, Constants {
	public short opcode = -1;

	public Instruction(short opcode) {
		this.opcode = opcode;
	}

	public void dump(DataOutputStream out) throws IOException {
		out.writeByte(opcode);
	}

	public String getName() {
		return Constants.OPCODE_NAMES[opcode];
	}

	/**
	 * Use with caution, since 'BranchInstruction's have a 'target' reference which is not copied correctly (only basic types are).
	 * This also applies for 'Select' instructions with their multiple branch targets.
	 * 
	 * @return (shallow) copy of an instruction
	 */
	// GET RID OF THIS - make it throw an exception and track the callers
	final public Instruction copy() {
		// if overridden correctly can just return 'this' here
		if (InstructionConstants.INSTRUCTIONS[opcode] != null) { // immutable instructions do not need copying
			return this;
		} else {
			Instruction i = null;
			try {// OPTIMIZE is clone the right thing to do here? it is horrible
				i = (Instruction) clone();
			} catch (CloneNotSupportedException e) {
				System.err.println(e);
			}
			return i;
		}
	}

	/**
	 * Read an instruction bytecode from an input stream and return the appropriate object.
	 * 
	 * @param file file to read from
	 * @return instruction object being read
	 */
	public static final Instruction readInstruction(ByteSequence bytes) throws IOException {
		boolean wide = false;
		short opcode = (short) bytes.readUnsignedByte();

		if (opcode == Constants.WIDE) {
			wide = true;
			opcode = (short) bytes.readUnsignedByte();
		}

		Instruction constantInstruction = InstructionConstants.INSTRUCTIONS[opcode];

		if (constantInstruction != null) {
			return constantInstruction;
		}

		Instruction obj = null;
		try {
			switch (opcode) {
			case Constants.BIPUSH:
				obj = new InstructionByte(Constants.BIPUSH, bytes.readByte());
				break;
			case Constants.SIPUSH:
				obj = new InstructionShort(Constants.SIPUSH, bytes.readShort());
				break;
			case Constants.LDC:
				obj = new InstructionCP(Constants.LDC, bytes.readUnsignedByte());
				break;
			case Constants.LDC_W:
			case Constants.LDC2_W:
				obj = new InstructionCP(opcode, bytes.readUnsignedShort());
				break;
			case Constants.ILOAD:
			case Constants.LLOAD:
			case Constants.FLOAD:
			case Constants.DLOAD:
			case Constants.ALOAD:
			case Constants.ISTORE:
			case Constants.LSTORE:
			case Constants.FSTORE:
			case Constants.DSTORE:
			case Constants.ASTORE:
				obj = new InstructionLV(opcode, wide ? bytes.readUnsignedShort() : bytes.readUnsignedByte());
				break;
			case Constants.IINC:
				obj = new IINC(wide ? bytes.readUnsignedShort() : bytes.readUnsignedByte(), wide ? bytes.readShort()
						: bytes.readByte(), wide);
				break;
			case Constants.IFNULL:
			case Constants.IFNONNULL:
			case Constants.IFEQ:
			case Constants.IFNE:
			case Constants.IFLT:
			case Constants.IFGE:
			case Constants.IFGT:
			case Constants.IFLE:
			case Constants.IF_ICMPEQ:
			case Constants.IF_ICMPNE:
			case Constants.IF_ICMPLT:
			case Constants.IF_ICMPGE:
			case Constants.IF_ICMPGT:
			case Constants.IF_ICMPLE:
			case Constants.IF_ACMPEQ:
			case Constants.IF_ACMPNE:
			case Constants.GOTO:
			case Constants.JSR:
				obj = new InstructionBranch(opcode, bytes.readShort());
				break;
			case Constants.GOTO_W:
			case Constants.JSR_W:
				obj = new InstructionBranch(opcode, bytes.readInt());
				break;
			case Constants.TABLESWITCH:
				obj = new TABLESWITCH(bytes);
				break;
			case Constants.LOOKUPSWITCH:
				obj = new LOOKUPSWITCH(bytes);
				break;
			case Constants.RET:
				obj = new RET(wide ? bytes.readUnsignedShort() : bytes.readUnsignedByte(), wide);
				break;
			case Constants.NEW:
				obj = new InstructionCP(Constants.NEW, bytes.readUnsignedShort());
				break;
			case Constants.GETSTATIC:
			case Constants.PUTSTATIC:
			case Constants.GETFIELD:
			case Constants.PUTFIELD:
				obj = new FieldInstruction(opcode, bytes.readUnsignedShort());
				break;
			case Constants.INVOKEVIRTUAL:
			case Constants.INVOKESPECIAL:
			case Constants.INVOKESTATIC:
				obj = new InvokeInstruction(opcode, bytes.readUnsignedShort());
				break;
			case Constants.INVOKEINTERFACE:
				obj = new INVOKEINTERFACE(bytes.readUnsignedShort(), bytes.readUnsignedByte(), bytes.readByte());
				break;
			case Constants.INVOKEDYNAMIC:
				obj = new InvokeDynamic(bytes.readUnsignedShort(),bytes.readUnsignedShort());
				break;
			case Constants.NEWARRAY:
				obj = new InstructionByte(Constants.NEWARRAY, bytes.readByte());
				break;
			case Constants.ANEWARRAY:
			case Constants.CHECKCAST:
				obj = new InstructionCP(opcode, bytes.readUnsignedShort());
				break;
			case Constants.INSTANCEOF:
				obj = new InstructionCP(Constants.INSTANCEOF, bytes.readUnsignedShort());
				break;
			case Constants.MULTIANEWARRAY:
				obj = new MULTIANEWARRAY(bytes.readUnsignedShort(), bytes.readByte());
				break;
			default:
				throw new ClassGenException("Illegal opcode detected");
			}
		} catch (ClassGenException e) {
			throw e;
		} catch (Exception e) {
			throw new ClassGenException(e.toString());
		}

		return obj;
	}

	/**
	 * @return Number of words consumed from stack by this instruction, or Constants.UNPREDICTABLE, if this can not be computed
	 *         statically
	 */
	public int consumeStack(ConstantPool cpg) {
		return Constants.CONSUME_STACK[opcode];
	}

	/**
	 * @return Number of words produced onto stack by this instruction, or Constants.UNPREDICTABLE, if this can not be computed
	 *         statically
	 */
	public int produceStack(ConstantPool cpg) {
		return Constants.stackEntriesProduced[opcode];
	}

	public short getOpcode() {
		return opcode;
	}

	public int getLength() {
		// if it is zero, it should have been provided by an overriding implementation of getLength()
		int len = Constants.iLen[opcode];
		assert len != 0;
		// if (len == 0) {
		// throw new IllegalStateException("Length not right for " + getName().toUpperCase());
		// }
		return len;
	}

	/** Some instructions may be reused, so don't do anything by default */
	void dispose() {
	}

	@Override
	public boolean equals(Object other) {
		if (this.getClass() != Instruction.class) {
			throw new RuntimeException("NO WAY " + this.getClass());
		}
		if (!(other instanceof Instruction)) {
			return false;
		}
		return ((Instruction) other).opcode == opcode;

		// IMPLEMENT EQUALS AND HASHCODE IN THE SUBTYPES!

		// Instruction i1 = this;
		// Instruction i2 = (Instruction) that;
		// if (i1.opcode == i2.opcode) {
		// if (i1.isConstantInstruction()) {
		// return i1.getValue().equals(i2.getValue());
		// } else if (i1.isIndexedInstruction()) {
		// return i1.getIndex() == i2.getIndex();
		// } else if (i1.opcode == Constants.NEWARRAY) {
		// return ((InstructionByte) i1).getTypecode() == ((InstructionByte) i2).getTypecode();
		// } else {
		// return true;
		// }
		// }
		//
		// return false;
	}

	@Override
	public int hashCode() {
		if (this.getClass() != Instruction.class) {
			throw new RuntimeException("NO WAY " + this.getClass());
		}
		return opcode * 37;
		// int result = 17 + opcode * 37;
		// if (isConstantInstruction()) {
		// result = 37 * getValue().hashCode() + result;
		// } else if (isIndexedInstruction()) {
		// result = 37 * getIndex() + result;
		// } else if (opcode == Constants.NEWARRAY) {
		// result = 37 * ((InstructionByte) this).getTypecode() + result;
		// }
		// return result;
	}

	public Type getType() {
		return getType(null);
	}

	public Type getType(ConstantPool cp) {
		// if (types[opcode]==null) throw new RuntimeException(getName()+" is not a typed instruction");
		Type t = Constants.types[opcode];
		if (t != null) {
			return t;
		}
		throw new RuntimeException("Do not know type for instruction " + getName() + "(" + opcode + ")");
	}

	public Number getValue() {
		assert (instFlags[opcode] & CONSTANT_INST) == 0;
		// if ((instFlags[opcode] & CONSTANT_INST) == 0) {
		// throw new RuntimeException(getName() + " is not a constant instruction");
		// }
		switch (opcode) {
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
			return new Integer(opcode - ICONST_0);
		default:
			throw new IllegalStateException("Not implemented yet for " + getName());
		}
	}

	public int getIndex() {
		return -1;
	}

	public void setIndex(int i) {
		throw new IllegalStateException("Shouldnt be asking " + getName().toUpperCase());
	}

	public Object getValue(ConstantPool cpg) {
		throw new IllegalStateException("Shouldnt be asking " + getName().toUpperCase());
	}

	public boolean isLoadInstruction() {
		return (Constants.instFlags[opcode] & LOAD_INST) != 0;
	}

	// remove these from here, leave them in the InstructionLV
	public boolean isASTORE() {
		return false;
	}

	public boolean isALOAD() {
		return false;
	}

	public boolean isStoreInstruction() {
		return (Constants.instFlags[opcode] & STORE_INST) != 0;
	}

	// public boolean containsTarget(InstructionHandle ih) {
	// throw new IllegalStateException("Dont ask!!");
	// }

	public boolean isJsrInstruction() {
		return (Constants.instFlags[opcode] & JSR_INSTRUCTION) != 0;
	}

	public boolean isConstantInstruction() {
		return (Constants.instFlags[opcode] & CONSTANT_INST) != 0;
	}

	public boolean isConstantPoolInstruction() {
		return (Constants.instFlags[opcode] & CP_INST) != 0;
	}

	public boolean isStackProducer() {
		return Constants.stackEntriesProduced[opcode] != 0;
	}

	public boolean isStackConsumer() {
		return Constants.CONSUME_STACK[opcode] != 0;
	}

	public boolean isIndexedInstruction() {
		return (Constants.instFlags[opcode] & INDEXED) != 0;
	}

	public boolean isArrayCreationInstruction() {
		return opcode == NEWARRAY || opcode == ANEWARRAY || opcode == MULTIANEWARRAY;
	}

	public ObjectType getLoadClassType(ConstantPool cpg) {
		assert (Constants.instFlags[opcode] & Constants.LOADCLASS_INST) == 0;
		// if ((Constants.instFlags[opcode] & Constants.LOADCLASS_INST) == 0) {
		// throw new IllegalStateException("This opcode " + opcode + " does not have the property "
		// + Long.toHexString(Constants.LOADCLASS_INST));
		// }
		Type t = getType(cpg);
		if (t instanceof ArrayType) {
			t = ((ArrayType) t).getBasicType();
		}
		return t instanceof ObjectType ? (ObjectType) t : null;
	}

	public boolean isReturnInstruction() {
		return (Constants.instFlags[opcode] & RET_INST) != 0;
	}

	// public boolean isGoto() {
	// return opcode == GOTO || opcode == GOTO_W;
	// }

	public boolean isLocalVariableInstruction() {
		return (Constants.instFlags[opcode] & LV_INST) != 0;
	}

	/**
	 * Long output format: 'name of opcode' "[" 'opcode number' "]" "(" 'length of instruction' ")"
	 */
	public String toString(boolean verbose) {
		if (verbose) {
			StringBuffer sb = new StringBuffer();
			sb.append(getName()).append("[").append(opcode).append("](size").append(Constants.iLen[opcode]).append(")");
			return sb.toString();
		} else {
			return getName();
		}
	}

	@Override
	public String toString() {
		return toString(true);
	}
}
