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
 * @version $Id: Constants.java,v 1.7 2011/09/28 01:14:54 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public interface Constants {
	// Major and minor version of the code
	public final static short MAJOR_1_1 = 45;
	public final static short MINOR_1_1 = 3;
	public final static short MAJOR_1_2 = 46;
	public final static short MINOR_1_2 = 0;
	public final static short MAJOR_1_3 = 47;
	public final static short MINOR_1_3 = 0;
	public final static short MAJOR_1_4 = 48;
	public final static short MINOR_1_4 = 0;
	public final static short MAJOR_1_5 = 49;
	public final static short MINOR_1_5 = 0;
	public final static short MAJOR_1_6 = 50;
	public final static short MINOR_1_6 = 0;
	// Defaults
	public final static short MAJOR = MAJOR_1_1;
	public final static short MINOR = MINOR_1_1;

	/** Maximum value for an unsigned short */
	public final static int MAX_SHORT = 65535; // 2^16 - 1

	/** Maximum value for an unsigned byte */
	public final static int MAX_BYTE = 255; // 2^8 - 1

	/** Access flags for classes, fields and methods */
	public final static short ACC_PUBLIC = 0x0001;
	public final static short ACC_PRIVATE = 0x0002;
	public final static short ACC_PROTECTED = 0x0004;
	public final static short ACC_STATIC = 0x0008;

	public final static short ACC_FINAL = 0x0010;
	public final static short ACC_SYNCHRONIZED = 0x0020;
	public final static short ACC_VOLATILE = 0x0040;
	public final static short ACC_TRANSIENT = 0x0080;

	public final static short ACC_NATIVE = 0x0100;
	public final static short ACC_INTERFACE = 0x0200;
	public final static short ACC_ABSTRACT = 0x0400;
	public final static short ACC_STRICT = 0x0800;

	public final static short ACC_ANNOTATION = 0x2000;
	public final static short ACC_ENUM = 0x4000;
	public final static short ACC_BRIDGE = 0x0040;
	public final static short ACC_VARARGS = 0x0080;

	// Applies to classes compiled by new compilers only
	public final static short ACC_SUPER = 0x0020;

	public final static short MAX_ACC_FLAG = ACC_STRICT;

	public final static String[] ACCESS_NAMES = { "public", "private", "protected", "static", "final", "synchronized", "volatile",
			"transient", "native", "interface", "abstract", "strictfp" };

	/** Tags in constant pool to denote type of constant */
	public final static byte CONSTANT_Utf8 = 1;
	public final static byte CONSTANT_Integer = 3;
	public final static byte CONSTANT_Float = 4;
	public final static byte CONSTANT_Long = 5;
	public final static byte CONSTANT_Double = 6;
	public final static byte CONSTANT_Class = 7;
	public final static byte CONSTANT_Fieldref = 9;
	public final static byte CONSTANT_String = 8;
	public final static byte CONSTANT_Methodref = 10;
	public final static byte CONSTANT_InterfaceMethodref = 11;
	public final static byte CONSTANT_NameAndType = 12;
	
	public final static byte CONSTANT_MethodHandle = 15;
	public final static byte CONSTANT_MethodType = 16;
	public final static byte CONSTANT_InvokeDynamic = 18;

	public final static String[] CONSTANT_NAMES = { "", "CONSTANT_Utf8", "", "CONSTANT_Integer", "CONSTANT_Float", "CONSTANT_Long",
			"CONSTANT_Double", "CONSTANT_Class", "CONSTANT_String", "CONSTANT_Fieldref", "CONSTANT_Methodref",
			"CONSTANT_InterfaceMethodref", "CONSTANT_NameAndType","","","CONSTANT_MethodHandle","CONSTANT_MethodType","","CONSTANT_InvokeDynamic" };

	/**
	 * The name of the static initializer, also called &quot;class initialization method&quot; or &quot;interface initialization
	 * method&quot;. This is &quot;&lt;clinit&gt;&quot;.
	 */
	public final static String STATIC_INITIALIZER_NAME = "<clinit>";

	/**
	 * The name of every constructor method in a class, also called &quot;instance initialization method&quot;. This is
	 * &quot;&lt;init&gt;&quot;.
	 */
	public final static String CONSTRUCTOR_NAME = "<init>";

	/** The names of the interfaces implemented by arrays */
	public final static String[] INTERFACES_IMPLEMENTED_BY_ARRAYS = { "java.lang.Cloneable", "java.io.Serializable" };

	/**
	 * Limitations of the Java Virtual Machine. See The Java Virtual Machine Specification, Second Edition, page 152, chapter 4.10.
	 */
	public static final int MAX_CP_ENTRIES = 65535;
	public static final int MAX_CODE_SIZE = 65536; // bytes

	/**
	 * Java VM opcodes.
	 */
	public static final short NOP = 0;
	public static final short ACONST_NULL = 1;
	public static final short ICONST_M1 = 2;
	public static final short ICONST_0 = 3;
	public static final short ICONST_1 = 4;
	public static final short ICONST_2 = 5;
	public static final short ICONST_3 = 6;
	public static final short ICONST_4 = 7;
	public static final short ICONST_5 = 8;
	public static final short LCONST_0 = 9;
	public static final short LCONST_1 = 10;
	public static final short FCONST_0 = 11;
	public static final short FCONST_1 = 12;
	public static final short FCONST_2 = 13;
	public static final short DCONST_0 = 14;
	public static final short DCONST_1 = 15;
	public static final short BIPUSH = 16;
	public static final short SIPUSH = 17;
	public static final short LDC = 18;
	public static final short LDC_W = 19;
	public static final short LDC2_W = 20;
	public static final short ILOAD = 21;
	public static final short LLOAD = 22;
	public static final short FLOAD = 23;
	public static final short DLOAD = 24;
	public static final short ALOAD = 25;
	public static final short ILOAD_0 = 26;
	public static final short ILOAD_1 = 27;
	public static final short ILOAD_2 = 28;
	public static final short ILOAD_3 = 29;
	public static final short LLOAD_0 = 30;
	public static final short LLOAD_1 = 31;
	public static final short LLOAD_2 = 32;
	public static final short LLOAD_3 = 33;
	public static final short FLOAD_0 = 34;
	public static final short FLOAD_1 = 35;
	public static final short FLOAD_2 = 36;
	public static final short FLOAD_3 = 37;
	public static final short DLOAD_0 = 38;
	public static final short DLOAD_1 = 39;
	public static final short DLOAD_2 = 40;
	public static final short DLOAD_3 = 41;
	public static final short ALOAD_0 = 42;
	public static final short ALOAD_1 = 43;
	public static final short ALOAD_2 = 44;
	public static final short ALOAD_3 = 45;
	public static final short IALOAD = 46;
	public static final short LALOAD = 47;
	public static final short FALOAD = 48;
	public static final short DALOAD = 49;
	public static final short AALOAD = 50;
	public static final short BALOAD = 51;
	public static final short CALOAD = 52;
	public static final short SALOAD = 53;
	public static final short ISTORE = 54;
	public static final short LSTORE = 55;
	public static final short FSTORE = 56;
	public static final short DSTORE = 57;
	public static final short ASTORE = 58;
	public static final short ISTORE_0 = 59;
	public static final short ISTORE_1 = 60;
	public static final short ISTORE_2 = 61;
	public static final short ISTORE_3 = 62;
	public static final short LSTORE_0 = 63;
	public static final short LSTORE_1 = 64;
	public static final short LSTORE_2 = 65;
	public static final short LSTORE_3 = 66;
	public static final short FSTORE_0 = 67;
	public static final short FSTORE_1 = 68;
	public static final short FSTORE_2 = 69;
	public static final short FSTORE_3 = 70;
	public static final short DSTORE_0 = 71;
	public static final short DSTORE_1 = 72;
	public static final short DSTORE_2 = 73;
	public static final short DSTORE_3 = 74;
	public static final short ASTORE_0 = 75;
	public static final short ASTORE_1 = 76;
	public static final short ASTORE_2 = 77;
	public static final short ASTORE_3 = 78;
	public static final short IASTORE = 79;
	public static final short LASTORE = 80;
	public static final short FASTORE = 81;
	public static final short DASTORE = 82;
	public static final short AASTORE = 83;
	public static final short BASTORE = 84;
	public static final short CASTORE = 85;
	public static final short SASTORE = 86;
	public static final short POP = 87;
	public static final short POP2 = 88;
	public static final short DUP = 89;
	public static final short DUP_X1 = 90;
	public static final short DUP_X2 = 91;
	public static final short DUP2 = 92;
	public static final short DUP2_X1 = 93;
	public static final short DUP2_X2 = 94;
	public static final short SWAP = 95;
	public static final short IADD = 96;
	public static final short LADD = 97;
	public static final short FADD = 98;
	public static final short DADD = 99;
	public static final short ISUB = 100;
	public static final short LSUB = 101;
	public static final short FSUB = 102;
	public static final short DSUB = 103;
	public static final short IMUL = 104;
	public static final short LMUL = 105;
	public static final short FMUL = 106;
	public static final short DMUL = 107;
	public static final short IDIV = 108;
	public static final short LDIV = 109;
	public static final short FDIV = 110;
	public static final short DDIV = 111;
	public static final short IREM = 112;
	public static final short LREM = 113;
	public static final short FREM = 114;
	public static final short DREM = 115;
	public static final short INEG = 116;
	public static final short LNEG = 117;
	public static final short FNEG = 118;
	public static final short DNEG = 119;
	public static final short ISHL = 120;
	public static final short LSHL = 121;
	public static final short ISHR = 122;
	public static final short LSHR = 123;
	public static final short IUSHR = 124;
	public static final short LUSHR = 125;
	public static final short IAND = 126;
	public static final short LAND = 127;
	public static final short IOR = 128;
	public static final short LOR = 129;
	public static final short IXOR = 130;
	public static final short LXOR = 131;
	public static final short IINC = 132;
	public static final short I2L = 133;
	public static final short I2F = 134;
	public static final short I2D = 135;
	public static final short L2I = 136;
	public static final short L2F = 137;
	public static final short L2D = 138;
	public static final short F2I = 139;
	public static final short F2L = 140;
	public static final short F2D = 141;
	public static final short D2I = 142;
	public static final short D2L = 143;
	public static final short D2F = 144;
	public static final short I2B = 145;
	public static final short INT2BYTE = 145; // Old notion
	public static final short I2C = 146;
	public static final short INT2CHAR = 146; // Old notion
	public static final short I2S = 147;
	public static final short INT2SHORT = 147; // Old notion
	public static final short LCMP = 148;
	public static final short FCMPL = 149;
	public static final short FCMPG = 150;
	public static final short DCMPL = 151;
	public static final short DCMPG = 152;
	public static final short IFEQ = 153;
	public static final short IFNE = 154;
	public static final short IFLT = 155;
	public static final short IFGE = 156;
	public static final short IFGT = 157;
	public static final short IFLE = 158;
	public static final short IF_ICMPEQ = 159;
	public static final short IF_ICMPNE = 160;
	public static final short IF_ICMPLT = 161;
	public static final short IF_ICMPGE = 162;
	public static final short IF_ICMPGT = 163;
	public static final short IF_ICMPLE = 164;
	public static final short IF_ACMPEQ = 165;
	public static final short IF_ACMPNE = 166;
	public static final short GOTO = 167;
	public static final short JSR = 168;
	public static final short RET = 169;
	public static final short TABLESWITCH = 170;
	public static final short LOOKUPSWITCH = 171;
	public static final short IRETURN = 172;
	public static final short LRETURN = 173;
	public static final short FRETURN = 174;
	public static final short DRETURN = 175;
	public static final short ARETURN = 176;
	public static final short RETURN = 177;
	public static final short GETSTATIC = 178;
	public static final short PUTSTATIC = 179;
	public static final short GETFIELD = 180;
	public static final short PUTFIELD = 181;
	public static final short INVOKEVIRTUAL = 182;
	public static final short INVOKESPECIAL = 183;
	public static final short INVOKENONVIRTUAL = 183; // Old name in JDK 1.0
	public static final short INVOKESTATIC = 184;
	public static final short INVOKEINTERFACE = 185;
	public static final short INVOKEDYNAMIC = 186;
	public static final short NEW = 187;
	public static final short NEWARRAY = 188;
	public static final short ANEWARRAY = 189;
	public static final short ARRAYLENGTH = 190;
	public static final short ATHROW = 191;
	public static final short CHECKCAST = 192;
	public static final short INSTANCEOF = 193;
	public static final short MONITORENTER = 194;
	public static final short MONITOREXIT = 195;
	public static final short WIDE = 196;
	public static final short MULTIANEWARRAY = 197;
	public static final short IFNULL = 198;
	public static final short IFNONNULL = 199;
	public static final short GOTO_W = 200;
	public static final short JSR_W = 201;

	/**
	 * Non-legal opcodes, may be used by JVM internally.
	 */
	public static final short BREAKPOINT = 202;
	public static final short LDC_QUICK = 203;
	public static final short LDC_W_QUICK = 204;
	public static final short LDC2_W_QUICK = 205;
	public static final short GETFIELD_QUICK = 206;
	public static final short PUTFIELD_QUICK = 207;
	public static final short GETFIELD2_QUICK = 208;
	public static final short PUTFIELD2_QUICK = 209;
	public static final short GETSTATIC_QUICK = 210;
	public static final short PUTSTATIC_QUICK = 211;
	public static final short GETSTATIC2_QUICK = 212;
	public static final short PUTSTATIC2_QUICK = 213;
	public static final short INVOKEVIRTUAL_QUICK = 214;
	public static final short INVOKENONVIRTUAL_QUICK = 215;
	public static final short INVOKESUPER_QUICK = 216;
	public static final short INVOKESTATIC_QUICK = 217;
	public static final short INVOKEINTERFACE_QUICK = 218;
	public static final short INVOKEVIRTUALOBJECT_QUICK = 219;
	public static final short NEW_QUICK = 221;
	public static final short ANEWARRAY_QUICK = 222;
	public static final short MULTIANEWARRAY_QUICK = 223;
	public static final short CHECKCAST_QUICK = 224;
	public static final short INSTANCEOF_QUICK = 225;
	public static final short INVOKEVIRTUAL_QUICK_W = 226;
	public static final short GETFIELD_QUICK_W = 227;
	public static final short PUTFIELD_QUICK_W = 228;
	public static final short IMPDEP1 = 254;
	public static final short IMPDEP2 = 255;

	/**
	 * For internal purposes only.
	 */
	public static final short PUSH = 4711;
	public static final short SWITCH = 4712;

	/**
	 * Illegal codes
	 */
	public static final short UNDEFINED = '/' - '0'; // -1;
	public static final short UNPREDICTABLE = '.' - '0';// -2;
	public static final short RESERVED = -3;
	public static final String ILLEGAL_OPCODE = "<illegal opcode>";
	public static final String ILLEGAL_TYPE = "<illegal type>";

	public static final byte T_BOOLEAN = 4;
	public static final byte T_CHAR = 5;
	public static final byte T_FLOAT = 6;
	public static final byte T_DOUBLE = 7;
	public static final byte T_BYTE = 8;
	public static final byte T_SHORT = 9;
	public static final byte T_INT = 10;
	public static final byte T_LONG = 11;

	public static final byte T_VOID = 12; // Non-standard
	public static final byte T_ARRAY = 13;
	public static final byte T_OBJECT = 14;
	public static final byte T_REFERENCE = 14; // Deprecated
	public static final byte T_UNKNOWN = 15;
	public static final byte T_ADDRESS = 16;

	/**
	 * The primitive type names corresponding to the T_XX constants, e.g., TYPE_NAMES[T_INT] = "int"
	 */
	public static final String[] TYPE_NAMES = { ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, "boolean", "char", "float",
			"double", "byte", "short", "int", "long", "void", "array", "object", "unknown" // Non-standard
	};

	/**
	 * The primitive class names corresponding to the T_XX constants, e.g., CLASS_TYPE_NAMES[T_INT] = "java.lang.Integer"
	 */
	public static final String[] CLASS_TYPE_NAMES = { ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, "java.lang.Boolean",
			"java.lang.Character", "java.lang.Float", "java.lang.Double", "java.lang.Byte", "java.lang.Short", "java.lang.Integer",
			"java.lang.Long", "java.lang.Void", ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE };

	/**
	 * The signature characters corresponding to primitive types, e.g., SHORT_TYPE_NAMES[T_INT] = "I"
	 */
	public static final String[] SHORT_TYPE_NAMES = { ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE, "Z", "C", "F", "D",
			"B", "S", "I", "J", "V", ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE };

	public static int PUSH_INST = 0x0001;
	public static int CONSTANT_INST = 0x0002;
	public static long LOADCLASS_INST = 0x0004;
	public static int CP_INST = 0x0008;
	public static int INDEXED = 0x0010;
	public static int LOAD_INST = 0x0020; // load instruction
	public static int LV_INST = 0x0040; // local variable instruction
	public static int POP_INST = 0x0080;
	public static int STORE_INST = 0x0100;
	public static long STACK_INST = 0x0200;
	public static long BRANCH_INSTRUCTION = 0x0400;
	public static long TARGETER_INSTRUCTION = 0x0800;
	public static long NEGATABLE = 0x1000;
	public static long IF_INST = 0x2000;
	public static long JSR_INSTRUCTION = 0x4000;
	public static long RET_INST = 0x8000;
	public static long EXCEPTION_THROWER = 0x10000;

	public static final byte[] iLen = new byte[256];
	public static final byte UNDEFINED_LENGTH = 'X' - '0';
	public static final byte VARIABLE_LENGTH = 'V' - '0';
	public static final byte[] stackEntriesProduced = new byte[256];
	public static final Type[] types = new Type[256];
	public static final long[] instFlags = new long[256];

	public static final Class<Throwable>[][] instExcs = new Class[256][];

	static final Object _unused = ConstantsInitializer.initialize();

	/**
	 * How the byte code operands are to be interpreted.
	 */
	public static final short[][] TYPE_OF_OPERANDS = { {}/* nop */, {}/* aconst_null */, {}/* iconst_m1 */, {}/* iconst_0 */,
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
	public static final String[] OPCODE_NAMES = { "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1", "iconst_2",
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
	public static final int[] CONSUME_STACK = { 0/* nop */, 0/* aconst_null */, 0/* iconst_m1 */, 0/* iconst_0 */, 0/* iconst_1 */,
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
	public static final byte ATTR_UNKNOWN = -1;
	public static final byte ATTR_SOURCE_FILE = 0;
	public static final byte ATTR_CONSTANT_VALUE = 1;
	public static final byte ATTR_CODE = 2;
	public static final byte ATTR_EXCEPTIONS = 3;
	public static final byte ATTR_LINE_NUMBER_TABLE = 4;
	public static final byte ATTR_LOCAL_VARIABLE_TABLE = 5;
	public static final byte ATTR_INNER_CLASSES = 6;
	public static final byte ATTR_SYNTHETIC = 7;
	public static final byte ATTR_DEPRECATED = 8;
	public static final byte ATTR_PMG = 9;
	public static final byte ATTR_SIGNATURE = 10;
	public static final byte ATTR_STACK_MAP = 11;
	public static final byte ATTR_RUNTIME_VISIBLE_ANNOTATIONS = 12;
	public static final byte ATTR_RUNTIME_INVISIBLE_ANNOTATIONS = 13;
	public static final byte ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = 14;
	public static final byte ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = 15;
	public static final byte ATTR_LOCAL_VARIABLE_TYPE_TABLE = 16;
	public static final byte ATTR_ENCLOSING_METHOD = 17;
	public static final byte ATTR_ANNOTATION_DEFAULT = 18;
	public static final byte ATTR_BOOTSTRAPMETHODS = 19;

	public static final short KNOWN_ATTRIBUTES = 20;

	public static final String[] ATTRIBUTE_NAMES = { "SourceFile", "ConstantValue", "Code", "Exceptions", "LineNumberTable",
			"LocalVariableTable", "InnerClasses", "Synthetic", "Deprecated", "PMGClass", "Signature", "StackMap",
			"RuntimeVisibleAnnotations", "RuntimeInvisibleAnnotations", "RuntimeVisibleParameterAnnotations",
			"RuntimeInvisibleParameterAnnotations", "LocalVariableTypeTable", "EnclosingMethod", "AnnotationDefault","BootstrapMethods" };

	/**
	 * Constants used in the StackMap attribute.
	 */
	public static final byte ITEM_Bogus = 0;
	public static final byte ITEM_Integer = 1;
	public static final byte ITEM_Float = 2;
	public static final byte ITEM_Double = 3;
	public static final byte ITEM_Long = 4;
	public static final byte ITEM_Null = 5;
	public static final byte ITEM_InitObject = 6;
	public static final byte ITEM_Object = 7;
	public static final byte ITEM_NewObject = 8;

	public static final String[] ITEM_NAMES = { "Bogus", "Integer", "Float", "Double", "Long", "Null", "InitObject", "Object",
			"NewObject" };
}
