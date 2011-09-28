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
package org.aspectj.apache.bcel;

import org.aspectj.apache.bcel.generic.Type;

public class ConstantsInitializer {

	public static Object initialize() {
		Constants.types[Constants.ILOAD] = Type.INT;
		Constants.types[Constants.ISTORE] = Type.INT;
		Constants.types[Constants.ILOAD_0] = Type.INT;
		Constants.types[Constants.ISTORE_0] = Type.INT;
		Constants.types[Constants.ILOAD_1] = Type.INT;
		Constants.types[Constants.ISTORE_1] = Type.INT;
		Constants.types[Constants.ILOAD_2] = Type.INT;
		Constants.types[Constants.ISTORE_2] = Type.INT;
		Constants.types[Constants.ILOAD_3] = Type.INT;
		Constants.types[Constants.ISTORE_3] = Type.INT;
		Constants.types[Constants.LLOAD] = Type.LONG;
		Constants.types[Constants.LSTORE] = Type.LONG;
		Constants.types[Constants.LLOAD_0] = Type.LONG;
		Constants.types[Constants.LSTORE_0] = Type.LONG;
		Constants.types[Constants.LLOAD_1] = Type.LONG;
		Constants.types[Constants.LSTORE_1] = Type.LONG;
		Constants.types[Constants.LLOAD_2] = Type.LONG;
		Constants.types[Constants.LSTORE_2] = Type.LONG;
		Constants.types[Constants.LLOAD_3] = Type.LONG;
		Constants.types[Constants.LSTORE_3] = Type.LONG;
		Constants.types[Constants.DLOAD] = Type.DOUBLE;
		Constants.types[Constants.DSTORE] = Type.DOUBLE;
		Constants.types[Constants.DLOAD_0] = Type.DOUBLE;
		Constants.types[Constants.DSTORE_0] = Type.DOUBLE;
		Constants.types[Constants.DLOAD_1] = Type.DOUBLE;
		Constants.types[Constants.DSTORE_1] = Type.DOUBLE;
		Constants.types[Constants.DLOAD_2] = Type.DOUBLE;
		Constants.types[Constants.DSTORE_2] = Type.DOUBLE;
		Constants.types[Constants.DLOAD_3] = Type.DOUBLE;
		Constants.types[Constants.DSTORE_3] = Type.DOUBLE;
		Constants.types[Constants.FLOAD] = Type.FLOAT;
		Constants.types[Constants.FSTORE] = Type.FLOAT;
		Constants.types[Constants.FLOAD_0] = Type.FLOAT;
		Constants.types[Constants.FSTORE_0] = Type.FLOAT;
		Constants.types[Constants.FLOAD_1] = Type.FLOAT;
		Constants.types[Constants.FSTORE_1] = Type.FLOAT;
		Constants.types[Constants.FLOAD_2] = Type.FLOAT;
		Constants.types[Constants.FSTORE_2] = Type.FLOAT;
		Constants.types[Constants.FLOAD_3] = Type.FLOAT;
		Constants.types[Constants.FSTORE_3] = Type.FLOAT;
		Constants.types[Constants.ALOAD] = Type.OBJECT;
		Constants.types[Constants.ASTORE] = Type.OBJECT;
		Constants.types[Constants.ALOAD_0] = Type.OBJECT;
		Constants.types[Constants.ASTORE_0] = Type.OBJECT;
		Constants.types[Constants.ALOAD_1] = Type.OBJECT;
		Constants.types[Constants.ASTORE_1] = Type.OBJECT;
		Constants.types[Constants.ALOAD_2] = Type.OBJECT;
		Constants.types[Constants.ASTORE_2] = Type.OBJECT;
		Constants.types[Constants.ALOAD_3] = Type.OBJECT;
		Constants.types[Constants.ASTORE_3] = Type.OBJECT;

		// INSTRUCTION_FLAGS - set for all
		Constants.instFlags[Constants.NOP] = 0;
		Constants.instFlags[Constants.ACONST_NULL] = Constants.PUSH_INST;
		Constants.instFlags[Constants.ICONST_M1] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.ICONST_0] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.ICONST_1] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.ICONST_2] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.ICONST_3] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.ICONST_4] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.ICONST_5] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.LCONST_0] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.LCONST_1] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.FCONST_0] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.FCONST_1] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.FCONST_2] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.DCONST_0] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.DCONST_1] = Constants.PUSH_INST | Constants.CONSTANT_INST;

		Constants.instFlags[Constants.BIPUSH] = Constants.PUSH_INST | Constants.CONSTANT_INST;
		Constants.instFlags[Constants.SIPUSH] = Constants.PUSH_INST | Constants.CONSTANT_INST;

		Constants.instFlags[Constants.LDC] = Constants.EXCEPTION_THROWER | Constants.PUSH_INST | Constants.CP_INST
				| Constants.INDEXED;

		Constants.instFlags[Constants.LDC_W] = Constants.EXCEPTION_THROWER | Constants.PUSH_INST | Constants.CP_INST
				| Constants.INDEXED;

		Constants.instFlags[Constants.LDC2_W] = Constants.EXCEPTION_THROWER | Constants.PUSH_INST | Constants.CP_INST
				| Constants.INDEXED;

		// the next five could be 'wide' prefixed and so have longer lengths
		Constants.instFlags[Constants.ILOAD] = Constants.INDEXED | Constants.LOAD_INST | Constants.PUSH_INST | Constants.LV_INST;
		Constants.instFlags[Constants.LLOAD] = Constants.INDEXED | Constants.LOAD_INST | Constants.PUSH_INST | Constants.LV_INST;
		Constants.instFlags[Constants.FLOAD] = Constants.INDEXED | Constants.LOAD_INST | Constants.PUSH_INST | Constants.LV_INST;
		Constants.instFlags[Constants.DLOAD] = Constants.INDEXED | Constants.LOAD_INST | Constants.PUSH_INST | Constants.LV_INST;
		Constants.instFlags[Constants.ALOAD] = Constants.INDEXED | Constants.LOAD_INST | Constants.PUSH_INST | Constants.LV_INST;
		for (int ii = Constants.ILOAD_0; ii <= Constants.ALOAD_3; ii++) {
			Constants.instFlags[ii] = Constants.INDEXED | Constants.LOAD_INST | Constants.PUSH_INST | Constants.LV_INST;
		}

		// the next five could be 'wide' prefixed and so have longer lengths
		Constants.instFlags[Constants.ISTORE] = Constants.INDEXED | Constants.STORE_INST | Constants.POP_INST | Constants.LV_INST;
		Constants.instFlags[Constants.LSTORE] = Constants.INDEXED | Constants.STORE_INST | Constants.POP_INST | Constants.LV_INST;
		Constants.instFlags[Constants.FSTORE] = Constants.INDEXED | Constants.STORE_INST | Constants.POP_INST | Constants.LV_INST;
		Constants.instFlags[Constants.DSTORE] = Constants.INDEXED | Constants.STORE_INST | Constants.POP_INST | Constants.LV_INST;
		Constants.instFlags[Constants.ASTORE] = Constants.INDEXED | Constants.STORE_INST | Constants.POP_INST | Constants.LV_INST;
		for (int ii = Constants.ISTORE_0; ii <= Constants.ASTORE_3; ii++) {
			Constants.instFlags[ii] = Constants.INDEXED | Constants.STORE_INST | Constants.POP_INST | Constants.LV_INST;
		}

		Constants.instFlags[Constants.IDIV] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.IDIV] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.ARITHMETIC_EXCEPTION };
		Constants.instFlags[Constants.IREM] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.IREM] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.ARITHMETIC_EXCEPTION };
		Constants.instFlags[Constants.LDIV] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.LDIV] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.ARITHMETIC_EXCEPTION };
		Constants.instFlags[Constants.LREM] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.LREM] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.ARITHMETIC_EXCEPTION };

		Constants.instFlags[Constants.ARRAYLENGTH] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.ARRAYLENGTH] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.NULL_POINTER_EXCEPTION };
		Constants.instFlags[Constants.ATHROW] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.ATHROW] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.THROWABLE };

		Constants.instFlags[Constants.AALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.AALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.IALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.IALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.BALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.BALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.FALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.FALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.DALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.DALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.CALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.CALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.LALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.LALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.SALOAD] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.SALOAD] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;

		Constants.instFlags[Constants.AASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.AASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.IASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.IASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.BASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.BASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.FASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.FASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.DASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.DASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.CASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.CASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.LASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.LASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
		Constants.instFlags[Constants.SASTORE] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.SASTORE] = org.aspectj.apache.bcel.ExceptionConstants.EXCS_ARRAY_EXCEPTION;

		// stack instructions
		Constants.instFlags[Constants.DUP] = Constants.PUSH_INST | Constants.STACK_INST;
		Constants.instFlags[Constants.DUP_X1] = Constants.STACK_INST; // TODO fixme - aren't these two push/stack producers?
																		// (although peculiar ones...)
		Constants.instFlags[Constants.DUP_X2] = Constants.STACK_INST;
		Constants.instFlags[Constants.DUP2] = Constants.PUSH_INST | Constants.STACK_INST;
		Constants.instFlags[Constants.DUP2_X1] = Constants.STACK_INST; // TODO fixme - aren't these two push/stack producers?
																		// (although peculiar ones...)
		Constants.instFlags[Constants.DUP2_X2] = Constants.STACK_INST;
		Constants.instFlags[Constants.POP] = Constants.STACK_INST | Constants.POP_INST;
		Constants.instFlags[Constants.POP2] = Constants.STACK_INST | Constants.POP_INST;
		Constants.instFlags[Constants.SWAP] = Constants.STACK_INST;

		Constants.instFlags[Constants.MONITORENTER] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.MONITORENTER] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.NULL_POINTER_EXCEPTION };
		Constants.instFlags[Constants.MONITOREXIT] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.MONITOREXIT] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.NULL_POINTER_EXCEPTION };

		// branching instructions
		Constants.instFlags[Constants.GOTO] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION;
		Constants.instFlags[Constants.GOTO_W] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION;
		Constants.instFlags[Constants.JSR] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.JSR_INSTRUCTION;
		Constants.instFlags[Constants.JSR_W] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.JSR_INSTRUCTION;

		Constants.instFlags[Constants.IFGT] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION | Constants.NEGATABLE
				| Constants.IF_INST;
		Constants.instFlags[Constants.IFLE] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION | Constants.NEGATABLE
				| Constants.IF_INST;
		Constants.instFlags[Constants.IFNE] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION | Constants.NEGATABLE
				| Constants.IF_INST;
		Constants.instFlags[Constants.IFEQ] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION | Constants.NEGATABLE
				| Constants.IF_INST;
		Constants.instFlags[Constants.IFGE] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION | Constants.NEGATABLE
				| Constants.IF_INST;
		Constants.instFlags[Constants.IFLT] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION | Constants.NEGATABLE
				| Constants.IF_INST;
		Constants.instFlags[Constants.IFNULL] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION | Constants.NEGATABLE
				| Constants.IF_INST;
		Constants.instFlags[Constants.IFNONNULL] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ACMPEQ] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ACMPNE] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ICMPEQ] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ICMPGE] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ICMPGT] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ICMPLE] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ICMPLT] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;
		Constants.instFlags[Constants.IF_ICMPNE] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION
				| Constants.NEGATABLE | Constants.IF_INST;

		Constants.instFlags[Constants.LOOKUPSWITCH] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION;
		Constants.instFlags[Constants.TABLESWITCH] = Constants.BRANCH_INSTRUCTION | Constants.TARGETER_INSTRUCTION;

		// fixme these class arrays should be constants
		Constants.instFlags[Constants.ARETURN] = Constants.RET_INST | Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.ARETURN] = new Class[] { ExceptionConstants.ILLEGAL_MONITOR_STATE };
		Constants.instFlags[Constants.DRETURN] = Constants.RET_INST | Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.DRETURN] = new Class[] { ExceptionConstants.ILLEGAL_MONITOR_STATE };
		Constants.instFlags[Constants.FRETURN] = Constants.RET_INST | Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.FRETURN] = new Class[] { ExceptionConstants.ILLEGAL_MONITOR_STATE };
		Constants.instFlags[Constants.IRETURN] = Constants.RET_INST | Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.IRETURN] = new Class[] { ExceptionConstants.ILLEGAL_MONITOR_STATE };
		Constants.instFlags[Constants.LRETURN] = Constants.RET_INST | Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.LRETURN] = new Class[] { ExceptionConstants.ILLEGAL_MONITOR_STATE };
		Constants.instFlags[Constants.RETURN] = Constants.RET_INST | Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.RETURN] = new Class[] { ExceptionConstants.ILLEGAL_MONITOR_STATE };

		Constants.instFlags[Constants.NEW] = Constants.LOADCLASS_INST | Constants.EXCEPTION_THROWER | Constants.CP_INST
				| Constants.INDEXED;
		Constants.instExcs[Constants.NEW] = ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION_FOR_ALLOCATIONS;
		Constants.instFlags[Constants.NEWARRAY] = Constants.EXCEPTION_THROWER;
		Constants.instExcs[Constants.NEWARRAY] = new Class[] { org.aspectj.apache.bcel.ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION };

		Constants.types[Constants.IINC] = Type.INT;
		Constants.instFlags[Constants.IINC] = Constants.LV_INST | Constants.INDEXED;
		Constants.instFlags[Constants.RET] = Constants.INDEXED;

		Constants.instFlags[Constants.ANEWARRAY] = Constants.CP_INST | Constants.LOADCLASS_INST | Constants.EXCEPTION_THROWER
				| Constants.INDEXED;
		Constants.instExcs[Constants.ANEWARRAY] = ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION_ANEWARRAY;
		Constants.instFlags[Constants.CHECKCAST] = Constants.CP_INST | Constants.LOADCLASS_INST | Constants.EXCEPTION_THROWER
				| Constants.INDEXED;
		Constants.instExcs[Constants.CHECKCAST] = ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION_CHECKCAST;
		Constants.instFlags[Constants.INSTANCEOF] = Constants.CP_INST | Constants.LOADCLASS_INST | Constants.EXCEPTION_THROWER
				| Constants.INDEXED;
		Constants.instExcs[Constants.INSTANCEOF] = ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION;
		Constants.instFlags[Constants.MULTIANEWARRAY] = Constants.CP_INST | Constants.LOADCLASS_INST | Constants.EXCEPTION_THROWER
				| Constants.INDEXED;
		Constants.instExcs[Constants.MULTIANEWARRAY] = ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION_ANEWARRAY; // fixme i
																															// think
																															// this
																															// is a
		// stackproducer, old
		// bcel says no...

		Constants.instFlags[Constants.GETFIELD] = Constants.EXCEPTION_THROWER | Constants.CP_INST | Constants.LOADCLASS_INST
				| Constants.INDEXED;
		Constants.instExcs[Constants.GETFIELD] = ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION_GETFIELD_PUTFIELD;
		Constants.instFlags[Constants.GETSTATIC] = Constants.PUSH_INST | Constants.EXCEPTION_THROWER | Constants.LOADCLASS_INST
				| Constants.CP_INST | Constants.INDEXED;
		Constants.instExcs[Constants.GETSTATIC] = ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION_GETSTATIC_PUTSTATIC;
		Constants.instFlags[Constants.PUTFIELD] = Constants.POP_INST | Constants.EXCEPTION_THROWER | Constants.LOADCLASS_INST
				| Constants.CP_INST | Constants.INDEXED;
		Constants.instExcs[Constants.PUTFIELD] = ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION_GETFIELD_PUTFIELD;
		Constants.instFlags[Constants.PUTSTATIC] = Constants.EXCEPTION_THROWER | Constants.POP_INST | Constants.CP_INST
				| Constants.LOADCLASS_INST | Constants.INDEXED;
		Constants.instExcs[Constants.PUTSTATIC] = ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION_GETSTATIC_PUTSTATIC;

		Constants.instFlags[Constants.INVOKEINTERFACE] = Constants.EXCEPTION_THROWER | Constants.CP_INST | Constants.LOADCLASS_INST
				| Constants.INDEXED;
		Constants.instExcs[Constants.INVOKEINTERFACE] = ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION_INVOKEINTERFACE;
		Constants.instFlags[Constants.INVOKESPECIAL] = Constants.EXCEPTION_THROWER | Constants.CP_INST | Constants.LOADCLASS_INST
				| Constants.INDEXED;
		Constants.instExcs[Constants.INVOKESPECIAL] = ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION_INVOKESPECIAL_INVOKEVIRTUAL;
		Constants.instFlags[Constants.INVOKESTATIC] = Constants.EXCEPTION_THROWER | Constants.CP_INST | Constants.LOADCLASS_INST
				| Constants.INDEXED;
		Constants.instExcs[Constants.INVOKESTATIC] = ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION_INVOKESTATIC;
		Constants.instFlags[Constants.INVOKEVIRTUAL] = Constants.EXCEPTION_THROWER | Constants.CP_INST | Constants.LOADCLASS_INST
				| Constants.INDEXED;
		Constants.instExcs[Constants.INVOKEVIRTUAL] = ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION_INVOKESPECIAL_INVOKEVIRTUAL;

		char[] lengths = // . = varies in length, / = undefined
		("1111111111111111" + // nop > dconst_1
				"2323322222111111" + // bipush > lload_1
				"1111111111111111" + // lload_2 > laload
				"1111112222211111" + // faload > lstore_0
				"1111111111111111" + // lstore_1 > iastore
				"1111111111111111" + // lastore > swap
				"1111111111111111" + // iadd > ddiv
				"1111111111111111" + // irem > land
				"1111311111111111" + // ior > d2l
				"1111111113333333" + // d2f > if_icmpeq
				"3333333332..1111" + // if_icmpne > dreturn
				"1133333335/32311" + // areturn > athrow
				"3311.433551/////").toCharArray(); // checkcast >
		int count = 0;
		for (; count < lengths.length; count++) {
			Constants.iLen[count] = (byte) (lengths[count] - 48);
		}
		while (count < 256) {
			Constants.iLen[count] = Constants.UNDEFINED;
			count++;
		}
		Constants.iLen[Constants.BREAKPOINT] = 1;
		Constants.iLen[Constants.IMPDEP1] = 1;
		Constants.iLen[Constants.IMPDEP2] = 1;

		char[] producesOnStack = ("0111111112211122" + // nop > dconst_1
				"1111212121111122" + // bipush > lload_1
				"2211112222111112" + // lload_2 > laload
				"1211110000000000" + // faload > lstore_0
				"0000000000000000" + // lstore_1 > iastore
				"0000000002344562" + // lastore > swap
				"1212121212121212" + // iadd > ddiv
				"1212121212121212" + // irem > land
				"1212021211212212" + // ior > d2l
				"1111111110000000" + // d2f > if_icmpeq
				"0000000010000000" + // if_icmpne > dreturn
				"00.0.0..../11111" + // areturn > athrow
				"11000100010/").toCharArray(); // checkcast >
		count = 0;
		for (; count < producesOnStack.length; count++) {
			Constants.stackEntriesProduced[count] = (byte) (producesOnStack[count] - 48);
		}
		while (count < 256) {
			Constants.iLen[count] = Constants.UNDEFINED;
			count++;
		}
		return null;
	}
}