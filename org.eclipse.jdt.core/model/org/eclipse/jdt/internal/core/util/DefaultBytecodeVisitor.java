/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.util.IBytecodeVisitor;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IOpcodeMnemonics;
import org.eclipse.jdt.core.util.OpcodeStringValues;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Default implementation of ByteCodeVisitor
 */
public class DefaultBytecodeVisitor implements IBytecodeVisitor {
	private static final char[] INIT	= "<init>".toCharArray(); //$NON-NLS-1$
	private static final char[] EMPTY_NAME = new char[0];
	private static final int T_BOOLEAN = 4;
	private static final int T_CHAR = 5;
	private static final int T_FLOAT = 6;
	private static final int T_DOUBLE = 7;
	private static final int T_BYTE = 8;
	private static final int T_SHORT = 9;
	private static final int T_INT = 10;
	private static final int T_LONG = 11;

	private StringBuffer buffer;
	private String lineSeparator;
	private int tabNumber;
	
	public DefaultBytecodeVisitor(StringBuffer buffer, String lineSeparator, int tabNumber) {
		this.buffer = buffer;
		this.lineSeparator = lineSeparator;
		this.tabNumber = tabNumber + 1;
	}
	/**
	 * @see IBytecodeVisitor#_aaload(int)
	 */
	public void _aaload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.AALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_aastore(int)
	 */
	public void _aastore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.AASTORE]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_aconst_null(int)
	 */
	public void _aconst_null(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ACONST_NULL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_aload_0(int)
	 */
	public void _aload_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ALOAD_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_aload_1(int)
	 */
	public void _aload_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ALOAD_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_aload_2(int)
	 */
	public void _aload_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ALOAD_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_aload_3(int)
	 */
	public void _aload_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ALOAD_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_aload(int, int)
	 */
	public void _aload(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ALOAD])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_anewarray(int, int, IConstantPoolEntry)
	 */
	public void _anewarray(int pc, int index, IConstantPoolEntry constantClass) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ANEWARRAY])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnConstantClassName(constantClass));
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_areturn(int)
	 */
	public void _areturn(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ARETURN]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_arraylength(int)
	 */
	public void _arraylength(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ARRAYLENGTH]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_astore_0(int)
	 */
	public void _astore_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ASTORE_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_astore_1(int)
	 */
	public void _astore_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ASTORE_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_astore_2(int)
	 */
	public void _astore_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ASTORE_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_astore_3(int)
	 */
	public void _astore_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ASTORE_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_astore(int, int)
	 */
	public void _astore(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ASTORE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_athrow(int)
	 */
	public void _athrow(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ATHROW]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_baload(int)
	 */
	public void _baload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.BALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_bastore(int)
	 */
	public void _bastore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.BASTORE]);
		writeNewLine();
	}
	
	/**
	 * @see IBytecodeVisitor#_bipush(int, byte)
	 */
	public void _bipush(int pc, byte _byte) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.BIPUSH])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(_byte);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_caload(int)
	 */
	public void _caload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.CALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_castore(int)
	 */
	public void _castore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.CASTORE]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_checkcast(int, int, IConstantPoolEntry)
	 */
	public void _checkcast(int pc, int index, IConstantPoolEntry constantClass) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.CHECKCAST])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnConstantClassName(constantClass));
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_d2f(int)
	 */
	public void _d2f(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.D2F]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_d2i(int)
	 */
	public void _d2i(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.D2I]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_d2l(int)
	 */
	public void _d2l(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.D2L]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dadd(int)
	 */
	public void _dadd(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DADD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_daload(int)
	 */
	public void _daload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dastore(int)
	 */
	public void _dastore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DASTORE]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dcmpg(int)
	 */
	public void _dcmpg(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DCMPG]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dcmpl(int)
	 */
	public void _dcmpl(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DCMPL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dconst_0(int)
	 */
	public void _dconst_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DCONST_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dconst_1(int)
	 */
	public void _dconst_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DCONST_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ddiv(int)
	 */
	public void _ddiv(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DDIV]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dload_0(int)
	 */
	public void _dload_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DLOAD_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dload_1(int)
	 */
	public void _dload_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DLOAD_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dload_2(int)
	 */
	public void _dload_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DLOAD_2]);
		writeNewLine();
	}
	
	/**
	 * @see IBytecodeVisitor#_dload_3(int)
	 */
	public void _dload_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DLOAD_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dload(int, int)
	 */
	public void _dload(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DLOAD])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dmul(int)
	 */
	public void _dmul(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DMUL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dneg(int)
	 */
	public void _dneg(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DNEG]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_drem(int)
	 */
	public void _drem(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DREM]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dreturn(int)
	 */
	public void _dreturn(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DRETURN]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dstore_0(int)
	 */
	public void _dstore_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DSTORE_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dstore_1(int)
	 */
	public void _dstore_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DSTORE_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dstore_2(int)
	 */
	public void _dstore_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DSTORE_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dstore_3(int)
	 */
	public void _dstore_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DSTORE_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dstore(int,int)
	 */
	public void _dstore(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DSTORE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dsub(int)
	 */
	public void _dsub(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DSUB]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dup_x1(int)
	 */
	public void _dup_x1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DUP_X1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dup_x2(int)
	 */
	public void _dup_x2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DUP_X2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dup(int)
	 */
	public void _dup(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DUP]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dup2_x1(int)
	 */
	public void _dup2_x1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DUP2_X1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dup2_x2(int)
	 */
	public void _dup2_x2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DUP2_X2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_dup2(int)
	 */
	public void _dup2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.DUP2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_f2d(int)
	 */
	public void _f2d(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.F2D]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_f2i(int)
	 */
	public void _f2i(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.F2I]);
		writeNewLine();
	}
	
	/**
	 * @see IBytecodeVisitor#_f2l(int)
	 */
	public void _f2l(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.F2L]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fadd(int)
	 */
	public void _fadd(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FADD]);
		writeNewLine();
	}
	
	/**
	 * @see IBytecodeVisitor#_faload(int)
	 */
	public void _faload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fastore(int)
	 */
	public void _fastore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FASTORE]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fcmpg(int)
	 */
	public void _fcmpg(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FCMPG]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fcmpl(int)
	 */
	public void _fcmpl(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FCMPL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fconst_0(int)
	 */
	public void _fconst_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FCONST_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fconst_1(int)
	 */
	public void _fconst_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FCONST_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fconst_2(int)
	 */
	public void _fconst_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FCONST_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fdiv(int)
	 */
	public void _fdiv(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FDIV]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fload_0(int)
	 */
	public void _fload_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FLOAD_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fload_1(int)
	 */
	public void _fload_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FLOAD_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fload_2(int)
	 */
	public void _fload_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FLOAD_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fload_3(int)
	 */
	public void _fload_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FLOAD_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fload(int, int)
	 */
	public void _fload(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FLOAD])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fmul(int)
	 */
	public void _fmul(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FMUL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fneg(int)
	 */
	public void _fneg(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FNEG]);
		writeNewLine();
	}
	
	/**
	 * @see IBytecodeVisitor#_frem(int)
	 */
	public void _frem(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FREM]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_freturn(int)
	 */
	public void _freturn(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FRETURN]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fstore_0(int)
	 */
	public void _fstore_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FSTORE_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fstore_1(int)
	 */
	public void _fstore_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FSTORE_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fstore_2(int)
	 */
	public void _fstore_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FSTORE_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fstore_3(int)
	 */
	public void _fstore_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FSTORE_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fstore(int, int)
	 */
	public void _fstore(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FSTORE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_fsub(int)
	 */
	public void _fsub(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.FSUB]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_getfield(int, int, IConstantPoolEntry)
	 */
	public void _getfield(int pc, int index, IConstantPoolEntry constantFieldref) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.GETFIELD])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("classformat.getfield")) //$NON-NLS-1$
			.append(returnDeclaringClassName(constantFieldref))
			.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
			.append(constantFieldref.getFieldName())
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnFieldrefDescriptor(constantFieldref))
			.append(Util.bind("classformat.getfieldclose")); //$NON-NLS-1$
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_getstatic(int, int, IConstantPoolEntry)
	 */
	public void _getstatic(int pc, int index, IConstantPoolEntry constantFieldref) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.GETSTATIC])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("classformat.getstatic")) //$NON-NLS-1$
			.append(returnDeclaringClassName(constantFieldref))
			.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
			.append(constantFieldref.getFieldName())
			.append(returnFieldrefDescriptor(constantFieldref))
			.append(Util.bind("classformat.getstaticclose")); //$NON-NLS-1$
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_goto_w(int, int)
	 */
	public void _goto_w(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.GOTO_W])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_goto(int, int)
	 */
	public void _goto(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.GOTO])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_i2b(int)
	 */
	public void _i2b(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.I2B]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_i2c(int)
	 */
	public void _i2c(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.I2C]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_i2d(int)
	 */
	public void _i2d(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.I2D]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_i2f(int)
	 */
	public void _i2f(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.I2F]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_i2l(int)
	 */
	public void _i2l(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.I2L]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_i2s(int)
	 */
	public void _i2s(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.I2S]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iadd(int)
	 */
	public void _iadd(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IADD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iaload(int)
	 */
	public void _iaload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iand(int)
	 */
	public void _iand(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IAND]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iastore(int)
	 */
	public void _iastore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IASTORE]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_acmpeq(int, int)
	 */
	public void _if_acmpeq(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ACMPEQ])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_acmpne(int, int)
	 */
	public void _if_acmpne(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ACMPNE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_icmpeq(int, int)
	 */
	public void _if_icmpeq(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ICMPEQ])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_icmpge(int, int)
	 */
	public void _if_icmpge(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ICMPGE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_icmpgt(int, int)
	 */
	public void _if_icmpgt(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ICMPGT])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_icmple(int, int)
	 */
	public void _if_icmple(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ICMPLE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_icmplt(int, int)
	 */
	public void _if_icmplt(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ICMPLT])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_if_icmpne(int, int)
	 */
	public void _if_icmpne(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IF_ICMPNE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iconst_0(int)
	 */
	public void _iconst_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ICONST_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iconst_1(int)
	 */
	public void _iconst_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ICONST_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iconst_2(int)
	 */
	public void _iconst_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ICONST_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iconst_3(int)
	 */
	public void _iconst_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ICONST_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iconst_4(int)
	 */
	public void _iconst_4(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ICONST_4]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iconst_5(int)
	 */
	public void _iconst_5(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ICONST_5]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iconst_m1(int)
	 */
	public void _iconst_m1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ICONST_M1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_idiv(int)
	 */
	public void _idiv(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IDIV]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ifeq(int, int)
	 */
	public void _ifeq(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFEQ])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ifge(int, int)
	 */
	public void _ifge(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFGE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ifgt(int, int)
	 */
	public void _ifgt(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFGT])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ifle(int, int)
	 */
	public void _ifle(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFLE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iflt(int, int)
	 */
	public void _iflt(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFLT])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ifne(int, int)
	 */
	public void _ifne(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFNE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ifnonnull(int, int)
	 */
	public void _ifnonnull(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFNONNULL])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ifnull(int, int)
	 */
	public void _ifnull(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IFNULL])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iinc(int, int, int)
	 */
	public void _iinc(int pc, int index, int _const) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IINC])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(_const);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iload_0(int)
	 */
	public void _iload_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ILOAD_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iload_1(int)
	 */
	public void _iload_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ILOAD_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iload_2(int)
	 */
	public void _iload_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ILOAD_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iload_3(int)
	 */
	public void _iload_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ILOAD_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iload(int, int)
	 */
	public void _iload(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ILOAD])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_imul(int)
	 */
	public void _imul(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IMUL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ineg(int)
	 */
	public void _ineg(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.INEG]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_instanceof(int, int, IConstantPoolEntry)
	 */
	public void _instanceof(int pc, int index, IConstantPoolEntry constantClass) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.INSTANCEOF])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnConstantClassName(constantClass));
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_invokeinterface(int, int, byte, IConstantPoolEntry)
	 */
	public void _invokeinterface(
		int pc,
		int index,
		byte nargs,
		IConstantPoolEntry constantInterfaceMethodref) {

		char[] methodDescriptor = constantInterfaceMethodref.getMethodDescriptor();
		CharOperation.replace(methodDescriptor, '/', '.');

		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.INVOKEINTERFACE])
			.append(Util.bind("classformat.nargs")) //$NON-NLS-1$
			.append(nargs)
			.append(Util.bind("classformat.interfacemethodrefindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("classformat.invokeinterfacemethod")) //$NON-NLS-1$
			.append(returnDeclaringClassName(constantInterfaceMethodref))
			.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
			.append(
				Signature.toCharArray(
					methodDescriptor,
					constantInterfaceMethodref.getMethodName(),
					getParameterNames(methodDescriptor),
					true,
					true))
			.append(Util.bind("classformat.invokeinterfacemethodclose")); //$NON-NLS-1$
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_invokespecial(int, int, IConstantPoolEntry)
	 */
	public void _invokespecial(int pc, int index, IConstantPoolEntry constantMethodref) {
		char[] methodDescriptor = constantMethodref.getMethodDescriptor();
		CharOperation.replace(methodDescriptor, '/', '.');
		char[] methodName = constantMethodref.getMethodName();

		if (CharOperation.equals(INIT, methodName)) {
			methodName = EMPTY_NAME;
			writeTabs();
			buffer
				.append(pc)
				.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
				.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.INVOKESPECIAL])
				.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
				.append(index)
				.append(Util.bind("classformat.invokespecialconstructor")) //$NON-NLS-1$
				.append(returnDeclaringClassName(constantMethodref))
				.append(
					Signature.toCharArray(
						methodDescriptor,
						methodName,
						getParameterNames(methodDescriptor),
						true,
						false))
				.append(Util.bind("classformat.invokespecialconstructorclose")); //$NON-NLS-1$
			writeNewLine();
		} else {
			methodName = EMPTY_NAME;
			writeTabs();
			buffer
				.append(pc)
				.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
				.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ALOAD_0])
				.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
				.append(index)
				.append(Util.bind("classformat.invokespecialmethod")) //$NON-NLS-1$
				.append(returnDeclaringClassName(constantMethodref))
				.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
				.append(
					Signature.toCharArray(
						methodDescriptor,
						methodName,
						getParameterNames(methodDescriptor),
						true,
						true))
				.append(Util.bind("classformat.invokespecialmethodclose")); //$NON-NLS-1$
			writeNewLine();
		}
	}

	/**
	 * @see IBytecodeVisitor#_invokestatic(int, int, IConstantPoolEntry)
	 */
	public void _invokestatic(int pc, int index, IConstantPoolEntry constantMethodref) {
		char[] methodDescriptor = constantMethodref.getMethodDescriptor();
		CharOperation.replace(methodDescriptor, '/', '.');
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.INVOKESTATIC])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("classformat.invokestaticmethod")) //$NON-NLS-1$
			.append(returnDeclaringClassName(constantMethodref))
			.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
			.append(
				Signature.toCharArray(
					methodDescriptor,
					constantMethodref.getMethodName(),
					getParameterNames(methodDescriptor),
					true,
					true))
			.append(Util.bind("classformat.invokestaticmethodclose")); //$NON-NLS-1$
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_invokevirtual(int, int, IConstantPoolEntry)
	 */
	public void _invokevirtual(int pc, int index, IConstantPoolEntry constantMethodref) {
		char[] methodDescriptor = constantMethodref.getMethodDescriptor();
		CharOperation.replace(methodDescriptor, '/', '.');
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.INVOKEVIRTUAL])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("classformat.invokevirtualmethod")) //$NON-NLS-1$
			.append(returnDeclaringClassName(constantMethodref))
			.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
			.append(
				Signature.toCharArray(
					methodDescriptor,
					constantMethodref.getMethodName(),
					getParameterNames(methodDescriptor),
					true,
					true))
			.append(Util.bind("classformat.invokevirtualmethodclose")); //$NON-NLS-1$
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ior(int)
	 */
	public void _ior(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IOR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_irem(int)
	 */
	public void _irem(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IREM]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ireturn(int)
	 */
	public void _ireturn(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IRETURN]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ishl(int)
	 */
	public void _ishl(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISHL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ishr(int)
	 */
	public void _ishr(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISHR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_istore_0(int)
	 */
	public void _istore_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISTORE_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_istore_1(int)
	 */
	public void _istore_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISTORE_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_istore_2(int)
	 */
	public void _istore_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISTORE_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_istore_3(int)
	 */
	public void _istore_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISTORE_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_istore(int, int)
	 */
	public void _istore(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISTORE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_isub(int)
	 */
	public void _isub(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.ISUB]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_iushr(int)
	 */
	public void _iushr(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IUSHR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ixor(int)
	 */
	public void _ixor(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IXOR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_jsr_w(int, int)
	 */
	public void _jsr_w(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.JSR_W])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_jsr(int, int)
	 */
	public void _jsr(int pc, int branchOffset) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.JSR])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(branchOffset + pc);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_l2d(int)
	 */
	public void _l2d(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.L2D]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_l2f(int)
	 */
	public void _l2f(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.L2F]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_l2i(int)
	 */
	public void _l2i(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.L2I]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ladd(int)
	 */
	public void _ladd(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LADD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_laload(int)
	 */
	public void _laload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_land(int)
	 */
	public void _land(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LAND]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lastore(int)
	 */
	public void _lastore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LASTORE]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lcmp(int)
	 */
	public void _lcmp(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LCMP]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lconst_0(int)
	 */
	public void _lconst_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LCONST_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lconst_1(int)
	 */
	public void _lconst_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LCONST_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ldc_w(int, int, IConstantPoolEntry)
	 */
	public void _ldc_w(int pc, int index, IConstantPoolEntry constantPoolEntry) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LDC_W])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		switch (constantPoolEntry.getKind()) {
			case IConstantPoolConstant.CONSTANT_Float :
				appendOutputforConstantFloat(constantPoolEntry);
				break;
			case IConstantPoolConstant.CONSTANT_Integer :
				appendOutputforConstantInteger(constantPoolEntry);
				break;
			case IConstantPoolConstant.CONSTANT_String :
				appendOutputForConstantString(constantPoolEntry);
		}
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ldc(int, int, IConstantPoolEntry)
	 */
	public void _ldc(int pc, int index, IConstantPoolEntry constantPoolEntry) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LDC])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		switch (constantPoolEntry.getKind()) {
			case IConstantPoolConstant.CONSTANT_Float :
				appendOutputforConstantFloat(constantPoolEntry);
				break;
			case IConstantPoolConstant.CONSTANT_Integer :
				appendOutputforConstantInteger(constantPoolEntry);
				break;
			case IConstantPoolConstant.CONSTANT_String :
				appendOutputForConstantString(constantPoolEntry);
		}
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ldc2_w(int, int, IConstantPoolEntry)
	 */
	public void _ldc2_w(int pc, int index, IConstantPoolEntry constantPoolEntry) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LDC2_W])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		switch (constantPoolEntry.getKind()) {
			case IConstantPoolConstant.CONSTANT_Long :
				appendOutputForConstantLong(constantPoolEntry);
				break;
			case IConstantPoolConstant.CONSTANT_Double :
				appendOutputForConstantDouble(constantPoolEntry);
		}
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_ldiv(int)
	 */
	public void _ldiv(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LDIV]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lload_0(int)
	 */
	public void _lload_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LLOAD_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lload_1(int)
	 */
	public void _lload_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LLOAD_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lload_2(int)
	 */
	public void _lload_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LLOAD_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lload_3(int)
	 */
	public void _lload_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LLOAD_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lload(int, int)
	 */
	public void _lload(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LLOAD])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lmul(int)
	 */
	public void _lmul(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LMUL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lneg(int)
	 */
	public void _lneg(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LNEG]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lookupswitch(int, int, int, int[][])
	 */
	public void _lookupswitch(int pc, int defaultoffset, int npairs, int[][] offset_pairs) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LOOKUPSWITCH])
			.append(Util.bind("classfileformat.default")) //$NON-NLS-1$
			.append(defaultoffset + pc);
		writeNewLine();
		for (int i = 0; i < npairs; i++) {
			writeExtraTabs(1);
			buffer
				.append(Util.bind("classfileformat.case")) //$NON-NLS-1$
				.append(offset_pairs[i][0])
				.append(Util.bind("disassembler.colon")) //$NON-NLS-1$
				.append(offset_pairs[i][1] + pc);
			writeNewLine();
		}
	}

	/**
	 * @see IBytecodeVisitor#_lor(int)
	 */
	public void _lor(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LOR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lrem(int)
	 */
	public void _lrem(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LREM]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lreturn(int)
	 */
	public void _lreturn(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LRETURN]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lshl(int)
	 */
	public void _lshl(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSHL]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lshr(int)
	 */
	public void _lshr(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSHR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lstore_0(int)
	 */
	public void _lstore_0(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSTORE_0]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lstore_1(int)
	 */
	public void _lstore_1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSTORE_1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lstore_2(int)
	 */
	public void _lstore_2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSTORE_2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lstore_3(int)
	 */
	public void _lstore_3(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSTORE_3]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lstore(int, int)
	 */
	public void _lstore(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSTORE])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lsub(int)
	 */
	public void _lsub(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LSUB]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lushr(int)
	 */
	public void _lushr(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LUSHR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_lxor(int)
	 */
	public void _lxor(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.LXOR]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_monitorenter(int)
	 */
	public void _monitorenter(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.MONITORENTER]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_monitorexit(int)
	 */
	public void _monitorexit(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.MONITOREXIT]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_multianewarray(int, int, int, IConstantPoolEntry)
	 */
	public void _multianewarray(
		int pc,
		int index,
		int dimensions,
		IConstantPoolEntry constantClass) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.MULTIANEWARRAY])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnConstantClassName(constantClass));
		appendDimensions(dimensions);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_new(int, int, IConstantPoolEntry)
	 */
	public void _new(int pc, int index, IConstantPoolEntry constantClass) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.NEW])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnConstantClassName(constantClass));
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_newarray(int, int)
	 */
	public void _newarray(int pc, int atype) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.NEWARRAY])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(atype)
			.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		appendGetArrayType(atype);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_nop(int)
	 */
	public void _nop(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.NOP]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_pop(int)
	 */
	public void _pop(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.POP]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_pop2(int)
	 */
	public void _pop2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.POP2]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_putfield(int, int, IConstantPoolEntry)
	 */
	public void _putfield(int pc, int index, IConstantPoolEntry constantFieldref) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.PUTFIELD])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("classformat.putfield"))			 //$NON-NLS-1$
			.append(returnDeclaringClassName(constantFieldref))
			.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
			.append(constantFieldref.getFieldName())
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnFieldrefDescriptor(constantFieldref))
			.append(Util.bind("classformat.putfieldclose")); //$NON-NLS-1$
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_putstatic(int, int, IConstantPoolEntry)
	 */
	public void _putstatic(int pc, int index, IConstantPoolEntry constantFieldref) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.PUTSTATIC])
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(index)
			.append(Util.bind("classformat.putstatic")) //$NON-NLS-1$
			.append(returnDeclaringClassName(constantFieldref))
			.append(Util.bind("disassembler.classmemberseparator")) //$NON-NLS-1$
			.append(constantFieldref.getFieldName())
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(returnFieldrefDescriptor(constantFieldref))
			.append(Util.bind("classformat.putstaticclose")); //$NON-NLS-1$
		writeNewLine();
	}

	private char[] returnFieldrefDescriptor(IConstantPoolEntry constantFieldref) throws IllegalArgumentException {
		char[] fieldDescriptor = constantFieldref.getFieldDescriptor();
		CharOperation.replace(fieldDescriptor, '/', '.');
		return Signature.toCharArray(fieldDescriptor);
	}

	/**
	 * @see IBytecodeVisitor#_ret(int, int)
	 */
	public void _ret(int pc, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.RET])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(index);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_return(int)
	 */
	public void _return(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.RETURN]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_saload(int)
	 */
	public void _saload(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.SALOAD]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_sastore(int)
	 */
	public void _sastore(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.SASTORE]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_sipush(int, short)
	 */
	public void _sipush(int pc, short value) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.SIPUSH])
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(value);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_swap(int)
	 */
	public void _swap(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.SWAP]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_tableswitch(int, int, int, int, int[])
	 */
	public void _tableswitch(
		int pc, 
		int defaultoffset,
		int low,
		int high,
		int[] jump_offsets) {

		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.TABLESWITCH])
			.append(Util.bind("classfileformat.default")) //$NON-NLS-1$
			.append(defaultoffset + pc);
		writeNewLine();
		for (int i = low; i < high + 1; i++) {
			writeExtraTabs(1);
			buffer
				.append(Util.bind("classfileformat.case")) //$NON-NLS-1$
				.append(i)
				.append(Util.bind("disassembler.colon")) //$NON-NLS-1$
				.append(jump_offsets[i - low] + pc);
			writeNewLine();
		}
	}

	/**
	 * @see IBytecodeVisitor#_wide(int, int, int)
	 */
	public void _wide(int pc, int iincopcode, int index, int _const) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.WIDE]);
		writeNewLine();
		_iinc(pc + 1, index, _const);
	}

	/**
	 * @see IBytecodeVisitor#_wide(int, int, int)
	 */
	public void _wide(int pc, int opcode, int index) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.WIDE]);
		writeNewLine();
		switch(opcode) {
			case IOpcodeMnemonics.ILOAD :
				_iload(pc + 1, index);
				break;
			case IOpcodeMnemonics.FLOAD :
				_fload(pc + 1, index);
				break;
			case IOpcodeMnemonics.ALOAD :
				_aload(pc + 1, index);
				break;
			case IOpcodeMnemonics.LLOAD :
				_lload(pc + 1, index);
				break;
			case IOpcodeMnemonics.DLOAD :
				_dload(pc + 1, index);
				break;
			case IOpcodeMnemonics.ISTORE :
				_istore(pc + 1, index);
				break;
			case IOpcodeMnemonics.FSTORE :
				_fstore(pc + 1, index);
				break;
			case IOpcodeMnemonics.ASTORE :
				_astore(pc + 1, index);
				break;
			case IOpcodeMnemonics.LSTORE :
				_lstore(pc + 1, index);
				break;
			case IOpcodeMnemonics.DSTORE :
				_dstore(pc + 1, index);
				break;
			case IOpcodeMnemonics.RET :
				_ret(pc + 1, index);
		}
	}

	/**
	 * @see IBytecodeVisitor#_breakpoint(int)
	 */
	public void _breakpoint(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.BREAKPOINT]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_impdep1(int)
	 */
	public void _impdep1(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IMPDEP1]);
		writeNewLine();
	}

	/**
	 * @see IBytecodeVisitor#_impdep2(int)
	 */
	public void _impdep2(int pc) {
		writeTabs();
		buffer
			.append(pc)
			.append(Util.bind("disassembler.tab")) //$NON-NLS-1$
			.append(OpcodeStringValues.BYTECODE_NAMES[IOpcodeMnemonics.IMPDEP2]);
		writeNewLine();
	}

	private void appendDimensions(int dimensions) {
		for (int i = 0; i < dimensions; i++) {
			this.buffer.append(Util.bind("disassembler.arraydimensions")); //$NON-NLS-1$
		}
	}
	
	private void appendGetArrayType(int atype) {
		switch(atype) {
			case T_BOOLEAN :
				this.buffer.append(Util.bind("classfileformat.newarrayboolean")); //$NON-NLS-1$
				break;
			case T_CHAR :
				this.buffer.append(Util.bind("classfileformat.newarraychar")); //$NON-NLS-1$
				break;
			case T_FLOAT :
				this.buffer.append(Util.bind("classfileformat.newarrayfloat")); //$NON-NLS-1$
				break;
			case T_DOUBLE :
				this.buffer.append(Util.bind("classfileformat.newarraydouble")); //$NON-NLS-1$
				break;
			case T_BYTE :
				this.buffer.append(Util.bind("classfileformat.newarraybyte")); //$NON-NLS-1$
				break;
			case T_SHORT :
				this.buffer.append(Util.bind("classfileformat.newarrayshort")); //$NON-NLS-1$
				break;
			case T_INT :
				this.buffer.append(Util.bind("classfileformat.newarrayint")); //$NON-NLS-1$
				break;
			case T_LONG :
				this.buffer.append(Util.bind("classfileformat.newarraylong")); //$NON-NLS-1$
		}
	}

	private String returnConstantClassName(IConstantPoolEntry constantClass) {
		return new String(constantClass.getClassInfoName()).replace('/', '.');
	}

	private String returnDeclaringClassName(IConstantPoolEntry constantRef) {
		return new String(constantRef.getClassName()).replace('/', '.');
	}

	private char[][] getParameterNames(char[] methodDescriptor) {
		int paramCount = Signature.getParameterCount(methodDescriptor);
		char[][] parameterNames = new char[paramCount][];
		for (int i = 0; i < paramCount; i++) {
			parameterNames[i] = Util.bind("disassembler.parametername").toCharArray(); //$NON-NLS-1$
		}
		return parameterNames;
	}

	private void appendOutputForConstantDouble(IConstantPoolEntry constantPoolEntry) {
		this.buffer
			.append(Util.bind("disassembler.constantdouble")) //$NON-NLS-1$
			.append(constantPoolEntry.getDoubleValue())
			.append(Util.bind("disassembler.closeconstant")); //$NON-NLS-1$
	}

	private void appendOutputForConstantLong(IConstantPoolEntry constantPoolEntry) {
		this.buffer
			.append(Util.bind("disassembler.constantlong")) //$NON-NLS-1$
			.append(constantPoolEntry.getLongValue())
			.append(Util.bind("disassembler.closeconstant")); //$NON-NLS-1$
	}

	private void appendOutputForConstantString(IConstantPoolEntry constantPoolEntry) {
		this.buffer
			.append(Util.bind("disassembler.constantstring")) //$NON-NLS-1$
			.append(constantPoolEntry.getStringValue())
			.append(Util.bind("disassembler.closeconstant")); //$NON-NLS-1$
	}

	private void appendOutputforConstantInteger(IConstantPoolEntry constantPoolEntry) {
		this.buffer
			.append(Util.bind("disassembler.constantinteger")) //$NON-NLS-1$
			.append(constantPoolEntry.getIntegerValue())
			.append(Util.bind("disassembler.closeconstant")); //$NON-NLS-1$
	}

	private void appendOutputforConstantFloat(IConstantPoolEntry constantPoolEntry) {
		this.buffer
			.append(Util.bind("disassembler.constantfloat")) //$NON-NLS-1$
			.append(constantPoolEntry.getFloatValue())
			.append(Util.bind("disassembler.closeconstant")); //$NON-NLS-1$
	}

	private void writeNewLine() {
		this.buffer.append(lineSeparator);
	}	

	private void writeTabs() {
		for (int i = 0, max = this.tabNumber; i < max; i++) {
			this.buffer.append(Util.bind("disassembler.tab")); //$NON-NLS-1$
		}
	}	

	private void writeExtraTabs(int extraTabs) {
		for (int i = 0, max = this.tabNumber + extraTabs; i < max; i++) {
			this.buffer.append(Util.bind("disassembler.tab")); //$NON-NLS-1$
		}
	}	

}
