package org.aspectj.apache.bcel;

import org.aspectj.apache.bcel.generic.Type;

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

/**
 * Constants for the project, mostly defined in the JVM specification.
 *
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @author Andy Clement
 */
public interface Constants {
	// Major and minor version of the code
	short MAJOR_1_1 = 45;
	short MINOR_1_1 = 3;
	short MAJOR_1_2 = 46;
	short MINOR_1_2 = 0;
	short MAJOR_1_3 = 47;
	short MINOR_1_3 = 0;
	short MAJOR_1_4 = 48;
	short MINOR_1_4 = 0;
	short MAJOR_1_5 = 49;
	short MINOR_1_5 = 0;
	short MAJOR_1_6 = 50;
	short MINOR_1_6 = 0;
	short MAJOR_1_7 = 51;
	short MINOR_1_7 = 0;
	short MAJOR_1_8 = 52;
	short MINOR_1_8 = 0;
	short MAJOR_1_9 = 53;
	short MINOR_1_9 = 0;
	short MAJOR_10 = 54;
	short MINOR_10 = 0;
	short MAJOR_11 = 55;
	short MINOR_11 = 0;
	short MAJOR_12 = 56;
	short MINOR_12 = 0;
	short MAJOR_13 = 57;
	short MINOR_13 = 0;
	short MAJOR_14 = 58;
	short MINOR_14 = 0;

	int PREVIEW_MINOR_VERSION = 65535;

	// Defaults
	short MAJOR = MAJOR_1_1;
	short MINOR = MINOR_1_1;

	/** Maximum value for an unsigned short */
	int MAX_SHORT = 65535; // 2^16 - 1

	/** Maximum value for an unsigned byte */
	int MAX_BYTE = 255; // 2^8 - 1

	/** Access flags for classes, fields and methods */
	short ACC_PUBLIC = 0x0001;
	short ACC_PRIVATE = 0x0002;
	short ACC_PROTECTED = 0x0004;
	short ACC_STATIC = 0x0008;

	short ACC_FINAL = 0x0010;
	short ACC_SYNCHRONIZED = 0x0020;
	short ACC_VOLATILE = 0x0040;
	short ACC_TRANSIENT = 0x0080;

	short ACC_NATIVE = 0x0100;
	short ACC_INTERFACE = 0x0200;
	short ACC_ABSTRACT = 0x0400;
	short ACC_STRICT = 0x0800;

	short ACC_SYNTHETIC = 0x1000;

	short ACC_ANNOTATION = 0x2000;
	short ACC_ENUM = 0x4000;
	int ACC_MODULE = 0x8000;
	short ACC_BRIDGE = 0x0040;
	short ACC_VARARGS = 0x0080;

	// Module related
	// Indicates that any module which depends on the current module,
	// implicitly declares a dependence on the module indicated by this entry.
	int MODULE_ACC_TRANSITIVE   = 0x0020;
	// Indicates that this dependence is mandatory in the static phase, i.e., at
	// compile time, but is optional in the dynamic phase, i.e., at run time.
	int MODULE_ACC_STATIC_PHASE = 0x0040;
	// Indicates that this dependence was not explicitly or implicitly declared
	// in the source of the module declaration.
	int MODULE_ACC_SYNTHETIC    = 0x1000;
	// Indicates that this dependence was implicitly declared in the source of
	// the module declaration
	int MODULE_ACC_MANDATED     = 0x8000;

	// Applies to classes compiled by new compilers only
	short ACC_SUPER = 0x0020;

	short MAX_ACC_FLAG = ACC_STRICT;

	String[] ACCESS_NAMES = { "public", "private", "protected", "static", "final", "synchronized", "volatile",
			"transient", "native", "interface", "abstract", "strictfp" };

	/** Tags in constant pool to denote type of constant */
	byte CONSTANT_Utf8 = 1;
	byte CONSTANT_Integer = 3;
	byte CONSTANT_Float = 4;
	byte CONSTANT_Long = 5;
	byte CONSTANT_Double = 6;
	byte CONSTANT_Class = 7;
	byte CONSTANT_Fieldref = 9;
	byte CONSTANT_String = 8;
	byte CONSTANT_Methodref = 10;
	byte CONSTANT_InterfaceMethodref = 11;
	byte CONSTANT_NameAndType = 12;

	byte CONSTANT_MethodHandle = 15;
	byte CONSTANT_MethodType = 16;
	byte CONSTANT_Dynamic = 17;
	byte CONSTANT_InvokeDynamic = 18;

	byte CONSTANT_Module = 19;
	byte CONSTANT_Package = 20;


	String[] CONSTANT_NAMES = { "", "CONSTANT_Utf8", "", "CONSTANT_Integer", "CONSTANT_Float", "CONSTANT_Long",
			"CONSTANT_Double", "CONSTANT_Class", "CONSTANT_String", "CONSTANT_Fieldref", "CONSTANT_Methodref",
			"CONSTANT_InterfaceMethodref", "CONSTANT_NameAndType","","","CONSTANT_MethodHandle","CONSTANT_MethodType","","CONSTANT_InvokeDynamic",
			// J9:
			"CONSTANT_Module", "CONSTANT_Package"};

	/**
	 * The name of the static initializer, also called &quot;class initialization method&quot; or &quot;interface initialization
	 * method&quot;. This is &quot;&lt;clinit&gt;&quot;.
	 */
	String STATIC_INITIALIZER_NAME = "<clinit>";

	/**
	 * The name of every constructor method in a class, also called &quot;instance initialization method&quot;. This is
	 * &quot;&lt;init&gt;&quot;.
	 */
	String CONSTRUCTOR_NAME = "<init>";

	/** The names of the interfaces implemented by arrays */
	String[] INTERFACES_IMPLEMENTED_BY_ARRAYS = { "java.lang.Cloneable", "java.io.Serializable" };

	/**
	 * Limitations of the Java Virtual Machine. See The Java Virtual Machine Specification, Second Edition, page 152, chapter 4.10.
	 */
	int MAX_CP_ENTRIES = 65535;
	int MAX_CODE_SIZE = 65536; // bytes

	/**
	 * Java VM opcodes.
	 */
	short NOP = 0;
	short ACONST_NULL = 1;
	short ICONST_M1 = 2;
	short ICONST_0 = 3;
	short ICONST_1 = 4;
	short ICONST_2 = 5;
	short ICONST_3 = 6;
	short ICONST_4 = 7;
	short ICONST_5 = 8;
	short LCONST_0 = 9;
	short LCONST_1 = 10;
	short FCONST_0 = 11;
	short FCONST_1 = 12;
	short FCONST_2 = 13;
	short DCONST_0 = 14;
	short DCONST_1 = 15;
	short BIPUSH = 16;
	short SIPUSH = 17;
	short LDC = 18;
	short LDC_W = 19;
	short LDC2_W = 20;
	short ILOAD = 21;
	short LLOAD = 22;
	short FLOAD = 23;
	short DLOAD = 24;
	short ALOAD = 25;
	short ILOAD_0 = 26;
	short ILOAD_1 = 27;
	short ILOAD_2 = 28;
	short ILOAD_3 = 29;
	short LLOAD_0 = 30;
	short LLOAD_1 = 31;
	short LLOAD_2 = 32;
	short LLOAD_3 = 33;
	short FLOAD_0 = 34;
	short FLOAD_1 = 35;
	short FLOAD_2 = 36;
	short FLOAD_3 = 37;
	short DLOAD_0 = 38;
	short DLOAD_1 = 39;
	short DLOAD_2 = 40;
	short DLOAD_3 = 41;
	short ALOAD_0 = 42;
	short ALOAD_1 = 43;
	short ALOAD_2 = 44;
	short ALOAD_3 = 45;
	short IALOAD = 46;
	short LALOAD = 47;
	short FALOAD = 48;
	short DALOAD = 49;
	short AALOAD = 50;
	short BALOAD = 51;
	short CALOAD = 52;
	short SALOAD = 53;
	short ISTORE = 54;
	short LSTORE = 55;
	short FSTORE = 56;
	short DSTORE = 57;
	short ASTORE = 58;
	short ISTORE_0 = 59;
	short ISTORE_1 = 60;
	short ISTORE_2 = 61;
	short ISTORE_3 = 62;
	short LSTORE_0 = 63;
	short LSTORE_1 = 64;
	short LSTORE_2 = 65;
	short LSTORE_3 = 66;
	short FSTORE_0 = 67;
	short FSTORE_1 = 68;
	short FSTORE_2 = 69;
	short FSTORE_3 = 70;
	short DSTORE_0 = 71;
	short DSTORE_1 = 72;
	short DSTORE_2 = 73;
	short DSTORE_3 = 74;
	short ASTORE_0 = 75;
	short ASTORE_1 = 76;
	short ASTORE_2 = 77;
	short ASTORE_3 = 78;
	short IASTORE = 79;
	short LASTORE = 80;
	short FASTORE = 81;
	short DASTORE = 82;
	short AASTORE = 83;
	short BASTORE = 84;
	short CASTORE = 85;
	short SASTORE = 86;
	short POP = 87;
	short POP2 = 88;
	short DUP = 89;
	short DUP_X1 = 90;
	short DUP_X2 = 91;
	short DUP2 = 92;
	short DUP2_X1 = 93;
	short DUP2_X2 = 94;
	short SWAP = 95;
	short IADD = 96;
	short LADD = 97;
	short FADD = 98;
	short DADD = 99;
	short ISUB = 100;
	short LSUB = 101;
	short FSUB = 102;
	short DSUB = 103;
	short IMUL = 104;
	short LMUL = 105;
	short FMUL = 106;
	short DMUL = 107;
	short IDIV = 108;
	short LDIV = 109;
	short FDIV = 110;
	short DDIV = 111;
	short IREM = 112;
	short LREM = 113;
	short FREM = 114;
	short DREM = 115;
	short INEG = 116;
	short LNEG = 117;
	short FNEG = 118;
	short DNEG = 119;
	short ISHL = 120;
	short LSHL = 121;
	short ISHR = 122;
	short LSHR = 123;
	short IUSHR = 124;
	short LUSHR = 125;
	short IAND = 126;
	short LAND = 127;
	short IOR = 128;
	short LOR = 129;
	short IXOR = 130;
	short LXOR = 131;
	short IINC = 132;
	short I2L = 133;
	short I2F = 134;
	short I2D = 135;
	short L2I = 136;
	short L2F = 137;
	short L2D = 138;
	short F2I = 139;
	short F2L = 140;
	short F2D = 141;
	short D2I = 142;
	short D2L = 143;
	short D2F = 144;
	short I2B = 145;
	short INT2BYTE = 145; // Old notion
	short I2C = 146;
	short INT2CHAR = 146; // Old notion
	short I2S = 147;
	short INT2SHORT = 147; // Old notion
	short LCMP = 148;
	short FCMPL = 149;
	short FCMPG = 150;
	short DCMPL = 151;
	short DCMPG = 152;
	short IFEQ = 153;
	short IFNE = 154;
	short IFLT = 155;
	short IFGE = 156;
	short IFGT = 157;
	short IFLE = 158;
	short IF_ICMPEQ = 159;
	short IF_ICMPNE = 160;
	short IF_ICMPLT = 161;
	short IF_ICMPGE = 162;
	short IF_ICMPGT = 163;
	short IF_ICMPLE = 164;
	short IF_ACMPEQ = 165;
	short IF_ACMPNE = 166;
	short GOTO = 167;
	short JSR = 168;
	short RET = 169;
	short TABLESWITCH = 170;
	short LOOKUPSWITCH = 171;
	short IRETURN = 172;
	short LRETURN = 173;
	short FRETURN = 174;
	short DRETURN = 175;
	short ARETURN = 176;
	short RETURN = 177;
	short GETSTATIC = 178;
	short PUTSTATIC = 179;
	short GETFIELD = 180;
	short PUTFIELD = 181;
	short INVOKEVIRTUAL = 182;
	short INVOKESPECIAL = 183;
	short INVOKENONVIRTUAL = 183; // Old name in JDK 1.0
	short INVOKESTATIC = 184;
	short INVOKEINTERFACE = 185;
	short INVOKEDYNAMIC = 186;
	short NEW = 187;
	short NEWARRAY = 188;
	short ANEWARRAY = 189;
	short ARRAYLENGTH = 190;
	short ATHROW = 191;
	short CHECKCAST = 192;
	short INSTANCEOF = 193;
	short MONITORENTER = 194;
	short MONITOREXIT = 195;
	short WIDE = 196;
	short MULTIANEWARRAY = 197;
	short IFNULL = 198;
	short IFNONNULL = 199;
	short GOTO_W = 200;
	short JSR_W = 201;

	/**
	 * Non-legal opcodes, may be used by JVM internally.
	 */
	short BREAKPOINT = 202;
	short LDC_QUICK = 203;
	short LDC_W_QUICK = 204;
	short LDC2_W_QUICK = 205;
	short GETFIELD_QUICK = 206;
	short PUTFIELD_QUICK = 207;
	short GETFIELD2_QUICK = 208;
	short PUTFIELD2_QUICK = 209;
	short GETSTATIC_QUICK = 210;
	short PUTSTATIC_QUICK = 211;
	short GETSTATIC2_QUICK = 212;
	short PUTSTATIC2_QUICK = 213;
	short INVOKEVIRTUAL_QUICK = 214;
	short INVOKENONVIRTUAL_QUICK = 215;
	short INVOKESUPER_QUICK = 216;
	short INVOKESTATIC_QUICK = 217;
	short INVOKEINTERFACE_QUICK = 218;
	short INVOKEVIRTUALOBJECT_QUICK = 219;
	short NEW_QUICK = 221;
	short ANEWARRAY_QUICK = 222;
	short MULTIANEWARRAY_QUICK = 223;
	short CHECKCAST_QUICK = 224;
	short INSTANCEOF_QUICK = 225;
	short INVOKEVIRTUAL_QUICK_W = 226;
	short GETFIELD_QUICK_W = 227;
	short PUTFIELD_QUICK_W = 228;
	short IMPDEP1 = 254;
	short IMPDEP2 = 255;

	/**
	 * For internal purposes only.
	 */
	short PUSH = 4711;
	short SWITCH = 4712;

	/**
	 * Illegal codes
	 */
	short UNDEFINED = '/' - '0'; // -1;
	short UNPREDICTABLE = '.' - '0';// -2;
	short RESERVED = -3;
	String ILLEGAL_OPCODE = "<illegal opcode>";
	String ILLEGAL_TYPE = "<illegal type>";

	byte T_BOOLEAN = 4;
	byte T_CHAR = 5;
	byte T_FLOAT = 6;
	byte T_DOUBLE = 7;
	byte T_BYTE = 8;
	byte T_SHORT = 9;
	byte T_INT = 10;
	byte T_LONG = 11;

	byte T_VOID = 12; // Non-standard
	byte T_ARRAY = 13;
	byte T_OBJECT = 14;
	byte T_REFERENCE = 14; // Deprecated
	byte T_UNKNOWN = 15;
	byte T_ADDRESS = 16;

	/**
	 * The primitive type names corresponding to the T_XX constants, e.g., TYPE_NAMES[T_INT] = "int"
	 */
	String[] TYPE_NAMES = { ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, "boolean", "char", "float",
			"double", "byte", "short", "int", "long", "void", "array", "object", "unknown" // Non-standard
	};

	/**
	 * The primitive class names corresponding to the T_XX constants, e.g., CLASS_TYPE_NAMES[T_INT] = "java.lang.Integer"
	 */
	String[] CLASS_TYPE_NAMES = { ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, "java.lang.Boolean",
			"java.lang.Character", "java.lang.Float", "java.lang.Double", "java.lang.Byte", "java.lang.Short", "java.lang.Integer",
			"java.lang.Long", "java.lang.Void", ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE };

	/**
	 * The signature characters corresponding to primitive types, e.g., SHORT_TYPE_NAMES[T_INT] = "I"
	 */
	String[] SHORT_TYPE_NAMES = { ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, "Z", "C", "F", "D",
			"B", "S", "I", "J", "V", ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE };

	int PUSH_INST = 0x0001;
	int CONSTANT_INST = 0x0002;
	long LOADCLASS_INST = 0x0004;
	int CP_INST = 0x0008;
	int INDEXED = 0x0010;
	int LOAD_INST = 0x0020; // load instruction
	int LV_INST = 0x0040; // local variable instruction
	int POP_INST = 0x0080;
	int STORE_INST = 0x0100;
	long STACK_INST = 0x0200;
	long BRANCH_INSTRUCTION = 0x0400;
	long TARGETER_INSTRUCTION = 0x0800;
	long NEGATABLE = 0x1000;
	long IF_INST = 0x2000;
	long JSR_INSTRUCTION = 0x4000;
	long RET_INST = 0x8000;
	long EXCEPTION_THROWER = 0x10000;

	byte[] iLen = new byte[256];
	byte UNDEFINED_LENGTH = 'X' - '0';
	byte VARIABLE_LENGTH = 'V' - '0';
	byte[] stackEntriesProduced = new byte[256];
	Type[] types = new Type[256];
	long[] instFlags = new long[256];

	Class<Throwable>[][] instExcs = new Class[256][];

	Object _unused = ConstantsInitializer.initialize();

	/**
	 * How the byte code operands are to be interpreted.
	 */
	short[][] TYPE_OF_OPERANDS = { {}/* nop */, {}/* aconst_null */, {}/* iconst_m1 */, {}/* iconst_0 */,
			{}/* iconst_1 */, {}/* iconst_2 */, {}/* iconst_3 */, {}/* iconst_4 */, {}/* iconst_5 */, {}/* lconst_0 */, {}/* lconst_1 */,
			{}/* fconst_0 */, {}/* fconst_1 */, {}/* fconst_2 */, {}/* dconst_0 */, {}/* dconst_1 */, { T_BYTE }/* bipush */,
			{ T_SHORT }/* sipush */, { T_BYTE }/* ldc */, { T_SHORT }/* ldc_w */, { T_SHORT }/* ldc2_w */, { T_BYTE }/* iload */,
			{ T_BYTE }/* lload */, { T_BYTE }/* fload */, { T_BYTE }/* dload */, { T_BYTE }/* aload */, {}/* iload_0 */,
			{}/* iload_1 */, {}/* iload_2 */, {}/* iload_3 */, {}/* lload_0 */, {}/* lload_1 */, {}/* lload_2 */, {}/* lload_3 */,
			{}/* fload_0 */, {}/* fload_1 */, {}/* fload_2 */, {}/* fload_3 */, {}/* dload_0 */, {}/* dload_1 */, {}/* dload_2 */,
			{}/* dload_3 */, {}/* aload_0 */, {}/* aload_1 */, {}/* aload_2 */, {}/* aload_3 */, {}/* iaload */, {}/* laload */,
			{}/* faload */, {}/* daload */, {}/* aaload */, {}/* baload */, {}/* caload */, {}/* saload */, { T_BYTE }/* istore */,
			{ T_BYTE }/* lstore */, { T_BYTE }/* fstore */, { T_BYTE }/* dstore */, { T_BYTE }/* astore */, {}/* istore_0 */,
			{}/* istore_1 */, {}/* istore_2 */, {}/* istore_3 */, {}/* lstore_0 */, {}/* lstore_1 */, {}/* lstore_2 */, {}/* lstore_3 */,
			{}/* fstore_0 */, {}/* fstore_1 */, {}/* fstore_2 */, {}/* fstore_3 */, {}/* dstore_0 */, {}/* dstore_1 */, {}/* dstore_2 */,
			{}/* dstore_3 */, {}/* astore_0 */, {}/* astore_1 */, {}/* astore_2 */, {}/* astore_3 */, {}/* iastore */, {}/* lastore */,
			{}/* fastore */, {}/* dastore */, {}/* aastore */, {}/* bastore */, {}/* castore */, {}/* sastore */, {}/* pop */, {}/* pop2 */,
			{}/* dup */, {}/* dup_x1 */, {}/* dup_x2 */, {}/* dup2 */, {}/* dup2_x1 */, {}/* dup2_x2 */, {}/* swap */, {}/* iadd */,
			{}/* ladd */, {}/* fadd */, {}/* dadd */, {}/* isub */, {}/* lsub */, {}/* fsub */, {}/* dsub */, {}/* imul */, {}/* lmul */, {}/* fmul */,
			{}/* dmul */, {}/* idiv */, {}/* ldiv */, {}/* fdiv */, {}/* ddiv */, {}/* irem */, {}/* lrem */, {}/* frem */, {}/* drem */, {}/* ineg */,
			{}/* lneg */, {}/* fneg */, {}/* dneg */, {}/* ishl */, {}/* lshl */, {}/* ishr */, {}/* lshr */, {}/* iushr */, {}/* lushr */,
			{}/* iand */, {}/* land */, {}/* ior */, {}/* lor */, {}/* ixor */, {}/* lxor */, { T_BYTE, T_BYTE }/* iinc */, {}/* i2l */,
			{}/* i2f */, {}/* i2d */, {}/* l2i */, {}/* l2f */, {}/* l2d */, {}/* f2i */, {}/* f2l */, {}/* f2d */, {}/* d2i */, {}/* d2l */,
			{}/* d2f */, {}/* i2b */, {}/* i2c */, {}/* i2s */, {}/* lcmp */, {}/* fcmpl */, {}/* fcmpg */, {}/* dcmpl */,
			{}/* dcmpg */, { T_SHORT }/* ifeq */, { T_SHORT }/* ifne */, { T_SHORT }/* iflt */, { T_SHORT }/* ifge */,
			{ T_SHORT }/* ifgt */, { T_SHORT }/* ifle */, { T_SHORT }/* if_icmpeq */, { T_SHORT }/* if_icmpne */,
			{ T_SHORT }/* if_icmplt */, { T_SHORT }/* if_icmpge */, { T_SHORT }/* if_icmpgt */, { T_SHORT }/* if_icmple */,
			{ T_SHORT }/* if_acmpeq */, { T_SHORT }/* if_acmpne */, { T_SHORT }/* goto */, { T_SHORT }/* jsr */,
			{ T_BYTE }/* ret */, {}/* tableswitch */, {}/* lookupswitch */, {}/* ireturn */, {}/* lreturn */, {}/* freturn */,
			{}/* dreturn */, {}/* areturn */, {}/* return */, { T_SHORT }/* getstatic */, { T_SHORT }/* putstatic */,
			{ T_SHORT }/* getfield */, { T_SHORT }/* putfield */, { T_SHORT }/* invokevirtual */,
			{ T_SHORT }/* invokespecial */, { T_SHORT }/* invokestatic */, { T_SHORT, T_BYTE, T_BYTE }/* invokeinterface */, {},
			{ T_SHORT }/* new */, { T_BYTE }/* newarray */, { T_SHORT }/* anewarray */, {}/* arraylength */, {}/* athrow */,
			{ T_SHORT }/* checkcast */, { T_SHORT }/* instanceof */, {}/* monitorenter */, {}/* monitorexit */, { T_BYTE }/* wide */,
			{ T_SHORT, T_BYTE }/* multianewarray */, { T_SHORT }/* ifnull */, { T_SHORT }/* ifnonnull */, { T_INT }/* goto_w */,
			{ T_INT }/* jsr_w */, {}/* breakpoint */, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
			{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
			{}, {}, {}/* impdep1 */, {} /* impdep2 */
	};

	/**
	 * Names of opcodes.
	 */
	String[] OPCODE_NAMES = { "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1", "iconst_2",
			"iconst_3", "iconst_4", "iconst_5", "lconst_0", "lconst_1", "fconst_0", "fconst_1", "fconst_2", "dconst_0", "dconst_1",
			"bipush", "sipush", "ldc", "ldc_w", "ldc2_w", "iload", "lload", "fload", "dload", "aload", "iload_0", "iload_1",
			"iload_2", "iload_3", "lload_0", "lload_1", "lload_2", "lload_3", "fload_0", "fload_1", "fload_2", "fload_3",
			"dload_0", "dload_1", "dload_2", "dload_3", "aload_0", "aload_1", "aload_2", "aload_3", "iaload", "laload", "faload",
			"daload", "aaload", "baload", "caload", "saload", "istore", "lstore", "fstore", "dstore", "astore", "istore_0",
			"istore_1", "istore_2", "istore_3", "lstore_0", "lstore_1", "lstore_2", "lstore_3", "fstore_0", "fstore_1", "fstore_2",
			"fstore_3", "dstore_0", "dstore_1", "dstore_2", "dstore_3", "astore_0", "astore_1", "astore_2", "astore_3", "iastore",
			"lastore", "fastore", "dastore", "aastore", "bastore", "castore", "sastore", "pop", "pop2", "dup", "dup_x1", "dup_x2",
			"dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd", "fadd", "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul",
			"fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg", "lneg", "fneg", "dneg", "ishl",
			"lshl", "ishr", "lshr", "iushr", "lushr", "iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f", "i2d",
			"l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f", "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl",
			"dcmpg", "ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne", "if_icmplt", "if_icmpge",
			"if_icmpgt", "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ret", "tableswitch", "lookupswitch", "ireturn",
			"lreturn", "freturn", "dreturn", "areturn", "return", "getstatic", "putstatic", "getfield", "putfield",
			"invokevirtual", "invokespecial", "invokestatic", "invokeinterface", "invokedynamic", "new", "newarray", "anewarray",
			"arraylength", "athrow", "checkcast", "instanceof", "monitorenter", "monitorexit", "wide", "multianewarray", "ifnull",
			"ifnonnull", "goto_w", "jsr_w", "breakpoint", ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, "impdep1", "impdep2" };

	/**
	 * Number of words consumed on operand stack by instructions.
	 */
	int[] CONSUME_STACK = { 0/* nop */, 0/* aconst_null */, 0/* iconst_m1 */, 0/* iconst_0 */, 0/* iconst_1 */,
			0/* iconst_2 */, 0/* iconst_3 */, 0/* iconst_4 */, 0/* iconst_5 */, 0/* lconst_0 */, 0/* lconst_1 */, 0/* fconst_0 */,
			0/* fconst_1 */, 0/* fconst_2 */, 0/* dconst_0 */, 0/* dconst_1 */, 0/* bipush */, 0/* sipush */, 0/* ldc */,
			0/* ldc_w */, 0/* ldc2_w */, 0/* iload */, 0/* lload */, 0/* fload */, 0/* dload */, 0/* aload */, 0/* iload_0 */, 0/* iload_1 */,
			0/* iload_2 */, 0/* iload_3 */, 0/* lload_0 */, 0/* lload_1 */, 0/* lload_2 */, 0/* lload_3 */, 0/* fload_0 */,
			0/* fload_1 */, 0/* fload_2 */, 0/* fload_3 */, 0/* dload_0 */, 0/* dload_1 */, 0/* dload_2 */, 0/* dload_3 */,
			0/* aload_0 */, 0/* aload_1 */, 0/* aload_2 */, 0/* aload_3 */, 2/* iaload */, 2/* laload */, 2/* faload */,
			2/* daload */, 2/* aaload */, 2/* baload */, 2/* caload */, 2/* saload */, 1/* istore */, 2/* lstore */,
			1/* fstore */, 2/* dstore */, 1/* astore */, 1/* istore_0 */, 1/* istore_1 */, 1/* istore_2 */, 1/* istore_3 */,
			2/* lstore_0 */, 2/* lstore_1 */, 2/* lstore_2 */, 2/* lstore_3 */, 1/* fstore_0 */, 1/* fstore_1 */, 1/* fstore_2 */,
			1/* fstore_3 */, 2/* dstore_0 */, 2/* dstore_1 */, 2/* dstore_2 */, 2/* dstore_3 */, 1/* astore_0 */, 1/* astore_1 */,
			1/* astore_2 */, 1/* astore_3 */, 3/* iastore */, 4/* lastore */, 3/* fastore */, 4/* dastore */, 3/* aastore */,
			3/* bastore */, 3/* castore */, 3/* sastore */, 1/* pop */, 2/* pop2 */, 1/* dup */, 2/* dup_x1 */, 3/* dup_x2 */,
			2/* dup2 */, 3/* dup2_x1 */, 4/* dup2_x2 */, 2/* swap */, 2/* iadd */, 4/* ladd */, 2/* fadd */, 4/* dadd */, 2/* isub */,
			4/* lsub */, 2/* fsub */, 4/* dsub */, 2/* imul */, 4/* lmul */, 2/* fmul */, 4/* dmul */, 2/* idiv */, 4/* ldiv */, 2/* fdiv */,
			4/* ddiv */, 2/* irem */, 4/* lrem */, 2/* frem */, 4/* drem */, 1/* ineg */, 2/* lneg */, 1/* fneg */, 2/* dneg */, 2/* ishl */,
			3/* lshl */, 2/* ishr */, 3/* lshr */, 2/* iushr */, 3/* lushr */, 2/* iand */, 4/* land */, 2/* ior */, 4/* lor */, 2/* ixor */,
			4/* lxor */, 0/* iinc */, 1/* i2l */, 1/* i2f */, 1/* i2d */, 2/* l2i */, 2/* l2f */, 2/* l2d */, 1/* f2i */, 1/* f2l */,
			1/* f2d */, 2/* d2i */, 2/* d2l */, 2/* d2f */, 1/* i2b */, 1/* i2c */, 1/* i2s */, 4/* lcmp */, 2/* fcmpl */,
			2/* fcmpg */, 4/* dcmpl */, 4/* dcmpg */, 1/* ifeq */, 1/* ifne */, 1/* iflt */, 1/* ifge */, 1/* ifgt */, 1/* ifle */,
			2/* if_icmpeq */, 2/* if_icmpne */, 2/* if_icmplt */, 2 /* if_icmpge */, 2/* if_icmpgt */, 2/* if_icmple */, 2/* if_acmpeq */,
			2/* if_acmpne */, 0/* goto */, 0/* jsr */, 0/* ret */, 1/* tableswitch */, 1/* lookupswitch */, 1/* ireturn */,
			2/* lreturn */, 1/* freturn */, 2/* dreturn */, 1/* areturn */, 0/* return */, 0/* getstatic */,
			UNPREDICTABLE/* putstatic */, 1/* getfield */, UNPREDICTABLE/* putfield */, UNPREDICTABLE/* invokevirtual */,
			UNPREDICTABLE/* invokespecial */, UNPREDICTABLE/* invokestatic */, UNPREDICTABLE/* invokeinterface */, UNDEFINED,
			0/* new */, 1/* newarray */, 1/* anewarray */, 1/* arraylength */, 1/* athrow */, 1/* checkcast */, 1/* instanceof */,
			1/* monitorenter */, 1/* monitorexit */, 0/* wide */, UNPREDICTABLE/* multianewarray */, 1/* ifnull */,
			1/* ifnonnull */, 0/* goto_w */, 0/* jsr_w */, 0/* breakpoint */, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
			UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
			UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
			UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
			UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
			UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNPREDICTABLE/* impdep1 */, UNPREDICTABLE /* impdep2 */
	};

	// Attributes and their corresponding names.
	byte ATTR_UNKNOWN = -1;
	byte ATTR_SOURCE_FILE = 0;
	byte ATTR_CONSTANT_VALUE = 1;
	byte ATTR_CODE = 2;
	byte ATTR_EXCEPTIONS = 3;
	byte ATTR_LINE_NUMBER_TABLE = 4;
	byte ATTR_LOCAL_VARIABLE_TABLE = 5;
	byte ATTR_INNER_CLASSES = 6;
	byte ATTR_SYNTHETIC = 7;
	byte ATTR_DEPRECATED = 8;
	byte ATTR_PMG = 9;
	byte ATTR_SIGNATURE = 10;
	byte ATTR_STACK_MAP = 11;
	byte ATTR_RUNTIME_VISIBLE_ANNOTATIONS = 12;
	byte ATTR_RUNTIME_INVISIBLE_ANNOTATIONS = 13;
	byte ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = 14;
	byte ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = 15;
	byte ATTR_LOCAL_VARIABLE_TYPE_TABLE = 16;
	byte ATTR_ENCLOSING_METHOD = 17;
	byte ATTR_ANNOTATION_DEFAULT = 18;
	byte ATTR_BOOTSTRAPMETHODS = 19;
	byte ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS = 20;
	byte ATTR_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = 21;
	byte ATTR_METHOD_PARAMETERS = 22;

	// J9:
	byte ATTR_MODULE = 23;
	byte ATTR_MODULE_PACKAGES = 24;
	byte ATTR_MODULE_MAIN_CLASS = 25;

	// J11:
	byte ATTR_NEST_HOST = 26;
	byte ATTR_NEST_MEMBERS = 27;

	short KNOWN_ATTRIBUTES = 28;

	String[] ATTRIBUTE_NAMES = {
			"SourceFile", "ConstantValue", "Code", "Exceptions", "LineNumberTable", "LocalVariableTable",
			"InnerClasses", "Synthetic", "Deprecated", "PMGClass", "Signature", "StackMap",
			"RuntimeVisibleAnnotations", "RuntimeInvisibleAnnotations", "RuntimeVisibleParameterAnnotations",
			"RuntimeInvisibleParameterAnnotations", "LocalVariableTypeTable", "EnclosingMethod",
			"AnnotationDefault","BootstrapMethods", "RuntimeVisibleTypeAnnotations", "RuntimeInvisibleTypeAnnotations",
			"MethodParameters", "Module", "ModulePackages", "ModuleMainClass", "NestHost", "NestMembers"
	};

	/**
	 * Constants used in the StackMap attribute.
	 */
	byte ITEM_Bogus = 0;
	byte ITEM_Integer = 1;
	byte ITEM_Float = 2;
	byte ITEM_Double = 3;
	byte ITEM_Long = 4;
	byte ITEM_Null = 5;
	byte ITEM_InitObject = 6;
	byte ITEM_Object = 7;
	byte ITEM_NewObject = 8;

	String[] ITEM_NAMES = { "Bogus", "Integer", "Float", "Double", "Long", "Null", "InitObject", "Object",
	"NewObject" };
}
