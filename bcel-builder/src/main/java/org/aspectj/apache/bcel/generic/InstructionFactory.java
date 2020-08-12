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
import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Utility;

/**
 * Instances of this class may be used, e.g., to generate typed versions of instructions. Its main purpose is to be used as the byte
 * code generating backend of a compiler. You can subclass it to add your own create methods.
 * 
 * @version $Id: InstructionFactory.java,v 1.7 2010/08/23 20:44:10 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see Constants
 */
public class InstructionFactory implements InstructionConstants {
	protected ClassGen cg;
	protected ConstantPool cp;

	public InstructionFactory(ClassGen cg, ConstantPool cp) {
		this.cg = cg;
		this.cp = cp;
	}

	public InstructionFactory(ClassGen cg) {
		this(cg, cg.getConstantPool());
	}

	public InstructionFactory(ConstantPool cp) {
		this(null, cp);
	}

	public InvokeInstruction createInvoke(String class_name, String name, Type ret_type, Type[] arg_types, short kind) {
		return createInvoke(class_name, name, ret_type, arg_types, kind, false);
	}
		
	/**
	 * Create an invoke instruction.
	 * 
	 * @param class_name name of the called class
	 * @param name name of the called method
	 * @param ret_type return type of method
	 * @param arg_types argument types of method
	 * @param kind how to invoke, i.e., INVOKEINTERFACE, INVOKESTATIC, INVOKEVIRTUAL, or INVOKESPECIAL
	 * @param isInterface for an invokestatic on an interface allows us to tell this method the target is an interface
	 * @see Constants
	 */
	public InvokeInstruction createInvoke(String class_name, String name, Type ret_type, Type[] arg_types, short kind, boolean isInterface) {

		String signature = Utility.toMethodSignature(ret_type, arg_types);

		int index;
		if (kind == Constants.INVOKEINTERFACE || isInterface) {
			index = cp.addInterfaceMethodref(class_name, name, signature);
		} else if (kind == Constants.INVOKEDYNAMIC){
			throw new IllegalStateException("NYI");
		} else {
			index = cp.addMethodref(class_name, name, signature);
		}

		switch (kind) {
		case Constants.INVOKESPECIAL:
			return new InvokeInstruction(Constants.INVOKESPECIAL, index);
		case Constants.INVOKEVIRTUAL:
			return new InvokeInstruction(Constants.INVOKEVIRTUAL, index);
		case Constants.INVOKESTATIC:
			return new InvokeInstruction(Constants.INVOKESTATIC, index);
		case Constants.INVOKEINTERFACE:
			int nargs = 0;
			for (Type arg_type : arg_types) {
				nargs += arg_type.getSize();
			}
			return new INVOKEINTERFACE(index, nargs + 1, 0);
		default:
			throw new RuntimeException("Oops: Unknown invoke kind:" + kind);
		}
	}

	public InvokeInstruction createInvoke(String class_name, String name, String signature, short kind) {
		int index;
		if (kind == Constants.INVOKEINTERFACE) {
			index = cp.addInterfaceMethodref(class_name, name, signature);
		} else if (kind == Constants.INVOKEDYNAMIC){
			throw new IllegalStateException("NYI");
		} else {
			index = cp.addMethodref(class_name, name, signature);
		}

		switch (kind) {
		case Constants.INVOKESPECIAL:
			return new InvokeInstruction(Constants.INVOKESPECIAL, index);
		case Constants.INVOKEVIRTUAL:
			return new InvokeInstruction(Constants.INVOKEVIRTUAL, index);
		case Constants.INVOKESTATIC:
			return new InvokeInstruction(Constants.INVOKESTATIC, index);
		case Constants.INVOKEINTERFACE:
			Type[] argumentTypes = Type.getArgumentTypes(signature);
			int nargs = 0;
			for (Type argumentType : argumentTypes) {// Count size of arguments
				nargs += argumentType.getSize();
			}
			return new INVOKEINTERFACE(index, nargs + 1, 0);
		default:
			throw new RuntimeException("Oops: Unknown invoke kind:" + kind);
		}
	}

	public static Instruction createALOAD(int n) {
		if (n < 4) {
			return new InstructionLV((short) (Constants.ALOAD_0 + n));
		}
		return new InstructionLV(Constants.ALOAD, n);
	}

	public static Instruction createASTORE(int n) {
		if (n < 4) {
			return new InstructionLV((short) (Constants.ASTORE_0 + n));
		}
		return new InstructionLV(Constants.ASTORE, n);
	}

	/**
	 * Uses PUSH to push a constant value onto the stack.
	 * 
	 * @param value must be of type Number, Boolean, Character or String
	 */
	// OPTIMIZE callers should use the PUSH methods where possible if they know the types
	public Instruction createConstant(Object value) {
		Instruction instruction;

		if (value instanceof Number) {
			instruction = InstructionFactory.PUSH(cp, (Number) value);
		} else if (value instanceof String) {
			instruction = InstructionFactory.PUSH(cp, (String) value);
		} else if (value instanceof Boolean) {
			instruction = InstructionFactory.PUSH(cp, (Boolean) value);
		} else if (value instanceof Character) {
			instruction = InstructionFactory.PUSH(cp, (Character) value);
		} else if (value instanceof ObjectType) {
			instruction = InstructionFactory.PUSH(cp, (ObjectType) value);
		} else {
			throw new ClassGenException("Illegal type: " + value.getClass());
		}

		return instruction;
	}

	/**
	 * Create a field instruction.
	 * 
	 * @param class_name name of the accessed class
	 * @param name name of the referenced field
	 * @param type type of field
	 * @param kind how to access, i.e., GETFIELD, PUTFIELD, GETSTATIC, PUTSTATIC
	 * @see Constants
	 */
	public FieldInstruction createFieldAccess(String class_name, String name, Type type, short kind) {
		int index;
		String signature = type.getSignature();

		index = cp.addFieldref(class_name, name, signature);

		switch (kind) {
		case Constants.GETFIELD:
			return new FieldInstruction(Constants.GETFIELD, index);
		case Constants.PUTFIELD:
			return new FieldInstruction(Constants.PUTFIELD, index);
		case Constants.GETSTATIC:
			return new FieldInstruction(Constants.GETSTATIC, index);
		case Constants.PUTSTATIC:
			return new FieldInstruction(Constants.PUTSTATIC, index);

		default:
			throw new RuntimeException("Oops: Unknown getfield kind:" + kind);
		}
	}

	/**
	 * Create reference to `this'
	 */
	public static Instruction createThis() {
		return new InstructionLV(Constants.ALOAD, 0);
	}

	/**
	 * Create typed return
	 */
	public static Instruction createReturn(Type type) {
		switch (type.getType()) {
		case Constants.T_ARRAY:
		case Constants.T_OBJECT:
			return ARETURN;
		case Constants.T_INT:
		case Constants.T_SHORT:
		case Constants.T_BOOLEAN:
		case Constants.T_CHAR:
		case Constants.T_BYTE:
			return IRETURN;
		case Constants.T_FLOAT:
			return FRETURN;
		case Constants.T_DOUBLE:
			return DRETURN;
		case Constants.T_LONG:
			return LRETURN;
		case Constants.T_VOID:
			return RETURN;

		default:
			throw new RuntimeException("Invalid type: " + type);
		}
	}

	/**
	 * @param size size of operand, either 1 (int, e.g.) or 2 (double)
	 */
	public static Instruction createPop(int size) {
		return (size == 2) ? POP2 : POP;
	}

	/**
	 * @param size size of operand, either 1 (int, e.g.) or 2 (double)
	 */
	public static Instruction createDup(int size) {
		return (size == 2) ? DUP2 : DUP;
	}

	/**
	 * @param size size of operand, either 1 (int, e.g.) or 2 (double)
	 */
	public static Instruction createDup_2(int size) {
		return (size == 2) ? DUP2_X2 : DUP_X2;
	}

	/**
	 * @param size size of operand, either 1 (int, e.g.) or 2 (double)
	 */
	public static Instruction createDup_1(int size) {
		return (size == 2) ? DUP2_X1 : DUP_X1;
	}

	/**
	 * @param index index of local variable
	 */
	public static InstructionLV createStore(Type type, int index) {
		switch (type.getType()) {
		case Constants.T_BOOLEAN:
		case Constants.T_CHAR:
		case Constants.T_BYTE:
		case Constants.T_SHORT:
		case Constants.T_INT:
			return new InstructionLV(Constants.ISTORE, index);
		case Constants.T_FLOAT:
			return new InstructionLV(Constants.FSTORE, index);
		case Constants.T_DOUBLE:
			return new InstructionLV(Constants.DSTORE, index);
		case Constants.T_LONG:
			return new InstructionLV(Constants.LSTORE, index);
		case Constants.T_ARRAY:
		case Constants.T_OBJECT:
			return new InstructionLV(Constants.ASTORE, index);
		default:
			throw new RuntimeException("Invalid type " + type);
		}
	}

	/**
	 * @param index index of local variable
	 */
	public static InstructionLV createLoad(Type type, int index) {
		switch (type.getType()) {
		case Constants.T_BOOLEAN:
		case Constants.T_CHAR:
		case Constants.T_BYTE:
		case Constants.T_SHORT:
		case Constants.T_INT:
			return new InstructionLV(Constants.ILOAD, index);
		case Constants.T_FLOAT:
			return new InstructionLV(Constants.FLOAD, index);
		case Constants.T_DOUBLE:
			return new InstructionLV(Constants.DLOAD, index);
		case Constants.T_LONG:
			return new InstructionLV(Constants.LLOAD, index);
		case Constants.T_ARRAY:
		case Constants.T_OBJECT:
			return new InstructionLV(Constants.ALOAD, index);
		default:
			throw new RuntimeException("Invalid type " + type);
		}
	}

	/**
	 * @param type type of elements of array, i.e., array.getElementType()
	 */
	public static Instruction createArrayLoad(Type type) {
		switch (type.getType()) {
		case Constants.T_BOOLEAN:
		case Constants.T_BYTE:
			return BALOAD;
		case Constants.T_CHAR:
			return CALOAD;
		case Constants.T_SHORT:
			return SALOAD;
		case Constants.T_INT:
			return IALOAD;
		case Constants.T_FLOAT:
			return FALOAD;
		case Constants.T_DOUBLE:
			return DALOAD;
		case Constants.T_LONG:
			return LALOAD;
		case Constants.T_ARRAY:
		case Constants.T_OBJECT:
			return AALOAD;
		default:
			throw new RuntimeException("Invalid type " + type);
		}
	}

	/**
	 * @param type type of elements of array, i.e., array.getElementType()
	 */
	public static Instruction createArrayStore(Type type) {
		switch (type.getType()) {
		case Constants.T_BOOLEAN:
		case Constants.T_BYTE:
			return BASTORE;
		case Constants.T_CHAR:
			return CASTORE;
		case Constants.T_SHORT:
			return SASTORE;
		case Constants.T_INT:
			return IASTORE;
		case Constants.T_FLOAT:
			return FASTORE;
		case Constants.T_DOUBLE:
			return DASTORE;
		case Constants.T_LONG:
			return LASTORE;
		case Constants.T_ARRAY:
		case Constants.T_OBJECT:
			return AASTORE;
		default:
			throw new RuntimeException("Invalid type " + type);
		}
	}

	private static final char[] shortNames = { 'C', 'F', 'D', 'B', 'S', 'I', 'L' };

	/**
	 * Create conversion operation for two stack operands, this may be an I2C, instruction, e.g., if the operands are basic types
	 * and CHECKCAST if they are reference types.
	 */
	public Instruction createCast(Type src_type, Type dest_type) {
		if ((src_type instanceof BasicType) && (dest_type instanceof BasicType)) {
			byte dest = dest_type.getType();
			byte src = src_type.getType();

			if (dest == Constants.T_LONG && (src == Constants.T_CHAR || src == Constants.T_BYTE || src == Constants.T_SHORT)) {
				src = Constants.T_INT;
			}

			if (src == Constants.T_DOUBLE) {
				switch (dest) {
				case Constants.T_FLOAT:
					return InstructionConstants.D2F;
				case Constants.T_INT:
					return InstructionConstants.D2I;
				case Constants.T_LONG:
					return InstructionConstants.D2L;
				}
			} else if (src == Constants.T_FLOAT) {
				switch (dest) {
				case Constants.T_DOUBLE:
					return InstructionConstants.F2D;
				case Constants.T_INT:
					return InstructionConstants.F2I;
				case Constants.T_LONG:
					return InstructionConstants.F2L;
				}
			} else if (src == Constants.T_INT) {
				switch (dest) {
				case Constants.T_BYTE:
					return InstructionConstants.I2B;
				case Constants.T_CHAR:
					return InstructionConstants.I2C;
				case Constants.T_DOUBLE:
					return InstructionConstants.I2D;
				case Constants.T_FLOAT:
					return InstructionConstants.I2F;
				case Constants.T_LONG:
					return InstructionConstants.I2L;
				case Constants.T_SHORT:
					return InstructionConstants.I2S;
				}
			} else if (src == Constants.T_LONG) {
				switch (dest) {
				case Constants.T_DOUBLE:
					return InstructionConstants.L2D;
				case Constants.T_FLOAT:
					return InstructionConstants.L2F;
				case Constants.T_INT:
					return InstructionConstants.L2I;
				}
			}

			// String name = "org.aspectj.apache.bcel.generic." + short_names[src - Constants.T_CHAR] +
			// "2" + short_names[dest - Constants.T_CHAR];

			// Instruction i = null;
			// try {
			// i = (Instruction)java.lang.Class.forName(name).newInstance();
			// } catch(Exception e) {
			// throw new RuntimeException("Could not find instruction: " + name);
			// }

			return null;
			// return i;
		} else if ((src_type instanceof ReferenceType) && (dest_type instanceof ReferenceType)) {
			if (dest_type instanceof ArrayType) {
				return new InstructionCP(Constants.CHECKCAST, cp.addArrayClass((ArrayType) dest_type));
			} else {
				return new InstructionCP(Constants.CHECKCAST, cp.addClass(((ObjectType) dest_type).getClassName()));
			}
		} else {
			throw new RuntimeException("Can not cast " + src_type + " to " + dest_type);
		}
	}

	public FieldInstruction createGetField(String class_name, String name, Type t) {
		return new FieldInstruction(Constants.GETFIELD, cp.addFieldref(class_name, name, t.getSignature()));
	}

	public FieldInstruction createGetStatic(String class_name, String name, Type t) {
		return new FieldInstruction(Constants.GETSTATIC, cp.addFieldref(class_name, name, t.getSignature()));
	}

	public FieldInstruction createPutField(String class_name, String name, Type t) {
		return new FieldInstruction(Constants.PUTFIELD, cp.addFieldref(class_name, name, t.getSignature()));
	}

	public FieldInstruction createPutStatic(String class_name, String name, Type t) {
		return new FieldInstruction(Constants.PUTSTATIC, cp.addFieldref(class_name, name, t.getSignature()));
	}

	public Instruction createCheckCast(ReferenceType t) {
		if (t instanceof ArrayType) {
			return new InstructionCP(Constants.CHECKCAST, cp.addArrayClass((ArrayType) t));
		} else {
			return new InstructionCP(Constants.CHECKCAST, cp.addClass((ObjectType) t));
		}
	}

	public Instruction createInstanceOf(ReferenceType t) {
		if (t instanceof ArrayType) {
			return new InstructionCP(Constants.INSTANCEOF, cp.addArrayClass((ArrayType) t));
		} else {
			return new InstructionCP(Constants.INSTANCEOF, cp.addClass((ObjectType) t));
		}
	}

	public Instruction createNew(ObjectType t) {
		return new InstructionCP(Constants.NEW, cp.addClass(t));
	}

	public Instruction createNew(String s) {
		return createNew(new ObjectType(s));
	}

	/**
	 * Create new array of given size and type.
	 * 
	 * @return an instruction that creates the corresponding array at runtime, i.e. is an AllocationInstruction
	 */
	public Instruction createNewArray(Type t, short dim) {
		if (dim == 1) {
			if (t instanceof ObjectType) {
				return new InstructionCP(Constants.ANEWARRAY, cp.addClass((ObjectType) t));
			} else if (t instanceof ArrayType) {
				return new InstructionCP(Constants.ANEWARRAY, cp.addArrayClass((ArrayType) t));
			} else {
				return new InstructionByte(Constants.NEWARRAY, ((BasicType) t).getType());
			}
		} else {
			ArrayType at;

			if (t instanceof ArrayType) {
				at = (ArrayType) t;
			} else {
				at = new ArrayType(t, dim);
			}

			return new MULTIANEWARRAY(cp.addArrayClass(at), dim);
		}
	}

	/**
	 * Create "null" value for reference types, 0 for basic types like int
	 */
	public static Instruction createNull(Type type) {
		switch (type.getType()) {
		case Constants.T_ARRAY:
		case Constants.T_OBJECT:
			return ACONST_NULL;
		case Constants.T_INT:
		case Constants.T_SHORT:
		case Constants.T_BOOLEAN:
		case Constants.T_CHAR:
		case Constants.T_BYTE:
			return ICONST_0;
		case Constants.T_FLOAT:
			return FCONST_0;
		case Constants.T_DOUBLE:
			return DCONST_0;
		case Constants.T_LONG:
			return LCONST_0;
		case Constants.T_VOID:
			return NOP;

		default:
			throw new RuntimeException("Invalid type: " + type);
		}
	}

	/**
	 * Create branch instruction by given opcode, except LOOKUPSWITCH and TABLESWITCH. For those you should use the SWITCH compound
	 * instruction.
	 */
	public static InstructionBranch createBranchInstruction(short opcode, InstructionHandle target) {
		switch (opcode) {
		case Constants.IFEQ:
			return new InstructionBranch(Constants.IFEQ, target);
		case Constants.IFNE:
			return new InstructionBranch(Constants.IFNE, target);
		case Constants.IFLT:
			return new InstructionBranch(Constants.IFLT, target);
		case Constants.IFGE:
			return new InstructionBranch(Constants.IFGE, target);
		case Constants.IFGT:
			return new InstructionBranch(Constants.IFGT, target);
		case Constants.IFLE:
			return new InstructionBranch(Constants.IFLE, target);
		case Constants.IF_ICMPEQ:
			return new InstructionBranch(Constants.IF_ICMPEQ, target);
		case Constants.IF_ICMPNE:
			return new InstructionBranch(Constants.IF_ICMPNE, target);
		case Constants.IF_ICMPLT:
			return new InstructionBranch(Constants.IF_ICMPLT, target);
		case Constants.IF_ICMPGE:
			return new InstructionBranch(Constants.IF_ICMPGE, target);
		case Constants.IF_ICMPGT:
			return new InstructionBranch(Constants.IF_ICMPGT, target);
		case Constants.IF_ICMPLE:
			return new InstructionBranch(Constants.IF_ICMPLE, target);
		case Constants.IF_ACMPEQ:
			return new InstructionBranch(Constants.IF_ACMPEQ, target);
		case Constants.IF_ACMPNE:
			return new InstructionBranch(Constants.IF_ACMPNE, target);
		case Constants.GOTO:
			return new InstructionBranch(Constants.GOTO, target);
		case Constants.JSR:
			return new InstructionBranch(Constants.JSR, target);
		case Constants.IFNULL:
			return new InstructionBranch(Constants.IFNULL, target);
		case Constants.IFNONNULL:
			return new InstructionBranch(Constants.IFNONNULL, target);
		case Constants.GOTO_W:
			return new InstructionBranch(Constants.GOTO_W, target);
		case Constants.JSR_W:
			return new InstructionBranch(Constants.JSR_W, target);
		default:
			throw new RuntimeException("Invalid opcode: " + opcode);
		}
	}

	public void setClassGen(ClassGen c) {
		cg = c;
	}

	public ClassGen getClassGen() {
		return cg;
	}

	public void setConstantPool(ConstantPool c) {
		cp = c;
	}

	public ConstantPool getConstantPool() {
		return cp;
	}

	/**
	 * Returns the right instruction for putting whatever you want onto the stack
	 */
	public static Instruction PUSH(ConstantPool cp, int value) {
		Instruction instruction = null;
		if ((value >= -1) && (value <= 5)) {
			return INSTRUCTIONS[Constants.ICONST_0 + value];
		} else if ((value >= -128) && (value <= 127)) {
			instruction = new InstructionByte(Constants.BIPUSH, (byte) value);
		} else if ((value >= -32768) && (value <= 32767)) {
			instruction = new InstructionShort(Constants.SIPUSH, (short) value);
		} else // If everything fails create a Constant pool entry
		{
			int pos = cp.addInteger(value);
			if (pos <= Constants.MAX_BYTE) {
				instruction = new InstructionCP(Constants.LDC, pos);
			} else {
				instruction = new InstructionCP(Constants.LDC_W, pos);
			}
		}
		return instruction;
	}

	public static Instruction PUSH(ConstantPool cp, ObjectType t) {
		return new InstructionCP(Constants.LDC_W, cp.addClass(t));
	}

	public static Instruction PUSH(ConstantPool cp, boolean value) {
		return INSTRUCTIONS[Constants.ICONST_0 + (value ? 1 : 0)];
	}

	public static Instruction PUSH(ConstantPool cp, float value) {
		Instruction instruction = null;
		if (value == 0.0) {
			instruction = FCONST_0;
		} else if (value == 1.0) {
			instruction = FCONST_1;
		} else if (value == 2.0) {
			instruction = FCONST_2;
		} else {
			// Create a Constant pool entry
			int i = cp.addFloat(value);
			instruction = new InstructionCP(i <= Constants.MAX_BYTE ? Constants.LDC : Constants.LDC_W, i);
		}
		return instruction;
	}

	public static Instruction PUSH(ConstantPool cp, long value) {
		Instruction instruction = null;
		if (value == 0) {
			instruction = LCONST_0;
		} else if (value == 1) {
			instruction = LCONST_1;
		} else {
			instruction = new InstructionCP(Constants.LDC2_W, cp.addLong(value));
		}
		return instruction;
	}

	public static Instruction PUSH(ConstantPool cp, double value) {
		Instruction instruction = null;
		if (value == 0.0) {
			instruction = DCONST_0;
		} else if (value == 1.0) {
			instruction = DCONST_1;
		} else {
			// Create a Constant pool entry
			instruction = new InstructionCP(Constants.LDC2_W, cp.addDouble(value));
		}
		return instruction;
	}

	public static Instruction PUSH(ConstantPool cp, String value) {
		Instruction instruction = null;
		if (value == null) {
			instruction = ACONST_NULL;
		} else {
			int i = cp.addString(value);
			instruction = new InstructionCP(i <= Constants.MAX_BYTE ? Constants.LDC : Constants.LDC_W, i);
		}
		return instruction;
	}

	public static Instruction PUSH(ConstantPool cp, Number value) {
		Instruction instruction = null;
		if ((value instanceof Integer) || (value instanceof Short) || (value instanceof Byte)) {
			instruction = PUSH(cp, value.intValue());
		} else if (value instanceof Double) {
			instruction = PUSH(cp, value.doubleValue());
		} else if (value instanceof Float) {
			instruction = PUSH(cp, value.floatValue());
		} else if (value instanceof Long) {
			instruction = PUSH(cp, value.longValue());
		} else {
			throw new ClassGenException("What's this: " + value);
		}
		return instruction;
	}

	public static Instruction PUSH(ConstantPool cp, Character value) {
		return PUSH(cp, value.charValue());
	}

	public static Instruction PUSH(ConstantPool cp, Boolean value) {
		return PUSH(cp, value.booleanValue());
	}

	/**
	 * Return a list that will load the Class object - on 1.5 or later use the class variant of ldc, whilst on earlier JVMs use the
	 * regular Class.forName.
	 */
	public InstructionList PUSHCLASS(ConstantPool cp, String className) {
		InstructionList iList = new InstructionList();
		int classIndex = cp.addClass(className);
		if (cg != null && cg.getMajor() >= Constants.MAJOR_1_5) {
			if (classIndex <= Constants.MAX_BYTE) {
				iList.append(new InstructionCP(Instruction.LDC, classIndex));
			} else {
				iList.append(new InstructionCP(Instruction.LDC_W, classIndex));
			}
		} else {
			className = className.replace('/', '.');
			iList.append(InstructionFactory.PUSH(cp, className));
			iList.append(this.createInvoke("java.lang.Class", "forName", ObjectType.CLASS, Type.STRINGARRAY1,
					Constants.INVOKESTATIC));
		}
		return iList;
	}
}
