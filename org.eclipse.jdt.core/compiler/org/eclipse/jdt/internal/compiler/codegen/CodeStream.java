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
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.*;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CodeStream implements OperatorIds, ClassFileConstants, Opcodes, BaseTypes, TypeConstants, TypeIds {
	// It will be responsible for the following items.

	// -> Tracking Max Stack.

	public int stackMax; // Use Ints to keep from using extra bc when adding
	public int stackDepth; // Use Ints to keep from using extra bc when adding
	public int maxLocals;
	public static final int max = 100; // Maximum size of the code array
	public static final int growFactor = 400;
	public static final int LABELS_INCREMENT = 5;
	public byte[] bCodeStream;
	public int pcToSourceMapSize;
	public int[] pcToSourceMap = new int[24];
	public int lastEntryPC; // last entry recorded
	public int[] lineSeparatorPositions;
	public int position; // So when first set can be incremented
	public int classFileOffset;
	public int startingClassFileOffset; // I need to keep the starting point inside the byte array
	public ConstantPool constantPool; // The constant pool used to generate bytecodes that need to store information into the constant pool
	public ClassFile classFile; // The current classfile it is associated to.
	// local variable attributes output
	public static final int LOCALS_INCREMENT = 10;
	public LocalVariableBinding[] locals = new LocalVariableBinding[LOCALS_INCREMENT];
	static LocalVariableBinding[] noLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	public LocalVariableBinding[] visibleLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	static LocalVariableBinding[] noVisibleLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	int visibleLocalsCount;
	public AbstractMethodDeclaration methodDeclaration;
	public ExceptionLabel[] exceptionHandlers = new ExceptionLabel[LABELS_INCREMENT];
	static ExceptionLabel[] noExceptionHandlers = new ExceptionLabel[LABELS_INCREMENT];
	public int exceptionHandlersNumber;
	public static FieldBinding[] ImplicitThis = new FieldBinding[] {};
	public boolean generateLineNumberAttributes;
	public boolean generateLocalVariableTableAttributes;
	public boolean preserveUnusedLocals;
	// store all the labels placed at the current position to be able to optimize
	// a jump to the next bytecode.
	public Label[] labels = new Label[LABELS_INCREMENT];
	static Label[] noLabels = new Label[LABELS_INCREMENT];
	public int countLabels;
	public int allLocalsCounter;
	public int maxFieldCount;
	// to handle goto_w
	public boolean wideMode = false;
	public static final CompilationResult RESTART_IN_WIDE_MODE = new CompilationResult((char[])null, 0, 0, 0);
	
public CodeStream(ClassFile classFile) {
	generateLineNumberAttributes = (classFile.produceDebugAttributes & CompilerOptions.Lines) != 0;
	generateLocalVariableTableAttributes = (classFile.produceDebugAttributes & CompilerOptions.Vars) != 0;
	if (generateLineNumberAttributes) {
		lineSeparatorPositions = classFile.referenceBinding.scope.referenceCompilationUnit().compilationResult.lineSeparatorPositions;
	}
}
final public void aaload() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_aaload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_aaload);
	}
}
final public void aastore() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_aastore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_aastore);
	}
}
final public void aconst_null() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_aconst_null;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_aconst_null);
	}
}
public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
	if (!generateLocalVariableTableAttributes)
		return;
/*	if (initStateIndex == lastInitStateIndexWhenAddingInits)
		return;
	lastInitStateIndexWhenAddingInits = initStateIndex;
	if (lastInitStateIndexWhenRemovingInits != initStateIndex){
		lastInitStateIndexWhenRemovingInits = -2; // reinitialize remove index 
		// remove(1)-add(1)-remove(1) -> ignore second remove
		// remove(1)-add(2)-remove(1) -> perform second remove
	}
	
*/	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null) {
			// Check if the local is definitely assigned
			if ((initStateIndex != -1) && isDefinitelyAssigned(scope, initStateIndex, localBinding)) {
				if ((localBinding.initializationCount == 0) || (localBinding.initializationPCs[((localBinding.initializationCount - 1) << 1) + 1] != -1)) {
					/* There are two cases:
					 * 1) there is no initialization interval opened ==> add an opened interval
					 * 2) there is already some initialization intervals but the last one is closed ==> add an opened interval
					 * An opened interval means that the value at localBinding.initializationPCs[localBinding.initializationCount - 1][1]
					 * is equals to -1.
					 * initializationPCs is a collection of pairs of int:
					 * 	first value is the startPC and second value is the endPC. -1 one for the last value means that the interval
					 * 	is not closed yet.
					 */
					localBinding.recordInitializationStartPC(position);
				}
			}
		}
	}
}
public void addLabel(Label aLabel) {
	if (countLabels == labels.length)
		System.arraycopy(labels, 0, (labels = new Label[countLabels + LABELS_INCREMENT]), 0, countLabels);
	labels[countLabels++] = aLabel;
}
public void addVisibleLocalVariable(LocalVariableBinding localBinding) {
	if (!generateLocalVariableTableAttributes)
		return;

	if (visibleLocalsCount >= visibleLocals.length) {
		System.arraycopy(visibleLocals, 0, (visibleLocals = new LocalVariableBinding[visibleLocalsCount * 2]), 0, visibleLocalsCount);
	}
	visibleLocals[visibleLocalsCount++] = localBinding;
}
final public void aload(int iArg) {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_aload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_aload);
		}
		writeUnsignedShort(iArg);
	} else {
		// Don't need to use the wide bytecode
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_aload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_aload);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) (iArg);
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void aload_0() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_aload_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_aload_0);
	}
}
final public void aload_1() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_aload_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_aload_1);
	}
}
final public void aload_2() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_aload_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_aload_2);
	}
}
final public void aload_3() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_aload_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_aload_3);
	}
}
public final void anewarray(TypeBinding typeBinding) {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_anewarray;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_anewarray);
	}
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
public void anewarrayJavaLangClass() {
	// anewarray: java.lang.Class
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_anewarray;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_anewarray);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangClass());
}
public void anewarrayJavaLangObject() {
	// anewarray: java.lang.Object
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_anewarray;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_anewarray);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangObject());
}
final public void areturn() {
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_areturn;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_areturn);
	}
}
public void arrayAt(int typeBindingID) {
	switch (typeBindingID) {
		case T_int :
			this.iaload();
			break;
		case T_byte :
		case T_boolean :
			this.baload();
			break;
		case T_short :
			this.saload();
			break;
		case T_char :
			this.caload();
			break;
		case T_long :
			this.laload();
			break;
		case T_float :
			this.faload();
			break;
		case T_double :
			this.daload();
			break;
		default :
			this.aaload();
	}
}
public void arrayAtPut(int elementTypeID, boolean valueRequired) {
	switch (elementTypeID) {
		case T_int :
			if (valueRequired)
				dup_x2();
			iastore();
			break;
		case T_byte :
		case T_boolean :
			if (valueRequired)
				dup_x2();
			bastore();
			break;
		case T_short :
			if (valueRequired)
				dup_x2();
			sastore();
			break;
		case T_char :
			if (valueRequired)
				dup_x2();
			castore();
			break;
		case T_long :
			if (valueRequired)
				dup2_x2();
			lastore();
			break;
		case T_float :
			if (valueRequired)
				dup_x2();
			fastore();
			break;
		case T_double :
			if (valueRequired)
				dup2_x2();
			dastore();
			break;
		default :
			if (valueRequired)
				dup_x2();
			aastore();
	}
}
final public void arraylength() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_arraylength;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_arraylength);
	}
}
final public void astore(int iArg) {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_astore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_astore);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_astore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_astore);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void astore_0() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_astore_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_astore_0);
	}
}
final public void astore_1() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_astore_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_astore_1);
	}
}
final public void astore_2() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_astore_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_astore_2);
	}
}
final public void astore_3() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_astore_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_astore_3);
	}
}
final public void athrow() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_athrow;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_athrow);
	}
}
final public void baload() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_baload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_baload);
	}
}
final public void bastore() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_bastore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_bastore);
	}
}
final public void bipush(byte b) {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_bipush;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_bipush);
	}
	writeSignedByte(b);
}
final public void caload() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_caload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_caload);
	}
}
final public void castore() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_castore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_castore);
	}
}
public final void checkcast(TypeBinding typeBinding) {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_checkcast;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_checkcast);
	}
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
public final void checkcastJavaLangError() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_checkcast;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_checkcast);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangError());
}
final public void d2f() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_d2f;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_d2f);
	}
}
final public void d2i() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_d2i;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_d2i);
	}
}
final public void d2l() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_d2l;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_d2l);
	}
}
final public void dadd() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dadd;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dadd);
	}
}
final public void daload() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_daload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_daload);
	}
}
final public void dastore() {
	countLabels = 0;
	stackDepth -= 4;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dastore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dastore);
	}
}
final public void dcmpg() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dcmpg;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dcmpg);
	}
}
final public void dcmpl() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dcmpl;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dcmpl);
	}
}
final public void dconst_0() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dconst_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dconst_0);
	}
}
final public void dconst_1() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dconst_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dconst_1);
	}
}
final public void ddiv() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ddiv;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ddiv);
	}
}
public void decrStackSize(int offset) {
	stackDepth -= offset;
}
final public void dload(int iArg) {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < iArg + 2) {
		maxLocals = iArg + 2; // + 2 because it is a double
	}
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_dload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_dload);
		}
		writeUnsignedShort(iArg);
	} else {
		// Don't need to use the wide bytecode
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_dload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_dload);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void dload_0() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dload_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dload_0);
	}
}
final public void dload_1() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dload_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dload_1);
	}
}
final public void dload_2() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dload_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dload_2);
	}
}
final public void dload_3() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dload_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dload_3);
	}
}
final public void dmul() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dmul;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dmul);
	}
}
final public void dneg() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dneg;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dneg);
	}
}
final public void drem() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_drem;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_drem);
	}
}
final public void dreturn() {
	countLabels = 0;
	stackDepth -= 2;
	// the stackDepth should be equal to 0 
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dreturn;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dreturn);
	}
}
final public void dstore(int iArg) {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_dstore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_dstore);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_dstore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_dstore);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void dstore_0() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dstore_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dstore_0);
	}
}
final public void dstore_1() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dstore_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dstore_1);
	}
}
final public void dstore_2() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dstore_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dstore_2);
	}
}
final public void dstore_3() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dstore_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dstore_3);
	}
}
final public void dsub() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dsub;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dsub);
	}
}
final public void dup() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dup;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dup);
	}
}
final public void dup_x1() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dup_x1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dup_x1);
	}
}
final public void dup_x2() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dup_x2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dup_x2);
	}
}
final public void dup2() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dup2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dup2);
	}
}
final public void dup2_x1() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dup2_x1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dup2_x1);
	}
}
final public void dup2_x2() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_dup2_x2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_dup2_x2);
	}
}
public void exitUserScope(BlockScope blockScope) {
	// mark all the scope's locals as loosing their definite assignment

	if (!generateLocalVariableTableAttributes)
		return;
	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding visibleLocal = visibleLocals[i];
		if ((visibleLocal != null) && (visibleLocal.declaringScope == blockScope)) { 
			// there maybe some some preserved locals never initialized
			if (visibleLocal.initializationCount > 0){
				visibleLocals[i].recordInitializationEndPC(position);
			}
			visibleLocals[i] = null; // this variable is no longer visible afterwards
		}
	}
}
final public void f2d() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_f2d;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_f2d);
	}
}
final public void f2i() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_f2i;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_f2i);
	}
}
final public void f2l() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_f2l;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_f2l);
	}
}
final public void fadd() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fadd;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fadd);
	}
}
final public void faload() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_faload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_faload);
	}
}
final public void fastore() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fastore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fastore);
	}
}
final public void fcmpg() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fcmpg;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fcmpg);
	}
}
final public void fcmpl() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fcmpl;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fcmpl);
	}
}
final public void fconst_0() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fconst_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fconst_0);
	}
}
final public void fconst_1() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fconst_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fconst_1);
	}
}
final public void fconst_2() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fconst_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fconst_2);
	}
}
final public void fdiv() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fdiv;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fdiv);
	}
}
final public void fload(int iArg) {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_fload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_fload);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_fload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_fload);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void fload_0() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fload_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fload_0);
	}
}
final public void fload_1() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fload_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fload_1);
	}
}
final public void fload_2() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fload_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fload_2);
	}
}
final public void fload_3() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fload_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fload_3);
	}
}
final public void fmul() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fmul;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fmul);
	}
}
final public void fneg() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fneg;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fneg);
	}
}
final public void frem() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_frem;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_frem);
	}
}
final public void freturn() {
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_freturn;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_freturn);
	}
}
final public void fstore(int iArg) {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_fstore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_fstore);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_fstore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_fstore);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void fstore_0() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fstore_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fstore_0);
	}
}
final public void fstore_1() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fstore_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fstore_1);
	}
}
final public void fstore_2() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fstore_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fstore_2);
	}
}
final public void fstore_3() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fstore_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fstore_3);
	}
}
final public void fsub() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_fsub;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_fsub);
	}
}
/**
 * Macro for building a class descriptor object
 */
public void generateClassLiteralAccessForType(TypeBinding accessedType, FieldBinding syntheticFieldBinding) {
	Label endLabel;
	ExceptionLabel anyExceptionHandler;
	int saveStackSize;
	if (accessedType.isBaseType() && accessedType != NullBinding) {
		this.getTYPE(accessedType.id);
		return;
	}
	endLabel = new Label(this);

	if (syntheticFieldBinding != null) { // non interface case
		this.getstatic(syntheticFieldBinding);
		this.dup();
		this.ifnonnull(endLabel);
		this.pop();
	}

	/* Macro for building a class descriptor object... using or not a field cache to store it into...
	this sequence is responsible for building the actual class descriptor.
	
	If the fieldCache is set, then it is supposed to be the body of a synthetic access method
	factoring the actual descriptor creation out of the invocation site (saving space).
	If the fieldCache is nil, then we are dumping the bytecode on the invocation site, since
	we have no way to get a hand on the field cache to do better. */


	// Wrap the code in an exception handler to convert a ClassNotFoundException into a NoClassDefError

	anyExceptionHandler = new ExceptionLabel(this, TypeBinding.NullBinding /* represents ClassNotFoundException*/);
	this.ldc(accessedType == TypeBinding.NullBinding ? "java.lang.Object" : String.valueOf(accessedType.constantPoolName()).replace('/', '.')); //$NON-NLS-1$
	this.invokeClassForName();

	/* We need to protect the runtime code from binary inconsistencies
	in case the accessedType is missing, the ClassNotFoundException has to be converted
	into a NoClassDefError(old ex message), we thus need to build an exception handler for this one. */
	anyExceptionHandler.placeEnd();

	if (syntheticFieldBinding != null) { // non interface case
		this.dup();
		this.putstatic(syntheticFieldBinding);
	}
	this.goto_(endLabel);


	// Generate the body of the exception handler
	saveStackSize = stackDepth;
	stackDepth = 1;
	/* ClassNotFoundException on stack -- the class literal could be doing more things
	on the stack, which means that the stack may not be empty at this point in the
	above code gen. So we save its state and restart it from 1. */

	anyExceptionHandler.place();

	// Transform the current exception, and repush and throw a 
	// NoClassDefFoundError(ClassNotFound.getMessage())

	this.newNoClassDefFoundError();
	this.dup_x1();
	this.swap();

	// Retrieve the message from the old exception
	this.invokeThrowableGetMessage();

	// Send the constructor taking a message string as an argument
	this.invokeNoClassDefFoundErrorStringConstructor();
	this.athrow();
	endLabel.place();
	stackDepth = saveStackSize;
}
/**
 * This method returns the exception handler to be able to generate the exception handler
 * attribute.
 */
final public int[] generateCodeAttributeForProblemMethod(String errorName, String problemMessage) {
	/**
	 * Equivalent code:
	 *	try {
	 *		throw ((Error) (Class.forName(errorName).getConstructor(new Class[] {Class.forName("java.lang.String")})).newInstance(new Object[] {problemMessage}));
	 *	} catch (Exception e) {
	 *		throw (NullPointerException) null;
	 *	}
	 */
	int endPC, handlerPC;
	ldc(errorName);
	invokeClassForName();
	iconst_1();
	anewarrayJavaLangClass();
	dup();
	iconst_0();
	ldc("java.lang.String"); //$NON-NLS-1$
	invokeClassForName();
	aastore();
	invokeConstructorGetConstructor();
	iconst_1();
	anewarrayJavaLangObject();
	dup();
	iconst_0();
	ldc(problemMessage);
	aastore();
	invokeObjectNewInstance();
	checkcastJavaLangError();
	athrow();
	endPC = handlerPC = position;
	pop();
	aconst_null();
	athrow();
	return_();
	return new int[] {0, endPC, handlerPC};
}
public void generateConstant(Constant constant, int implicitConversionCode) {
	int targetTypeID = implicitConversionCode >> 4;
	switch (targetTypeID) {
		case T_boolean :
			generateInlinedValue(constant.booleanValue());
			break;
		case T_char :
			generateInlinedValue(constant.charValue());
			break;
		case T_byte :
			generateInlinedValue(constant.byteValue());
			break;
		case T_short :
			generateInlinedValue(constant.shortValue());
			break;
		case T_int :
			generateInlinedValue(constant.intValue());
			break;
		case T_long :
			generateInlinedValue(constant.longValue());
			break;
		case T_float :
			generateInlinedValue(constant.floatValue());
			break;
		case T_double :
			generateInlinedValue(constant.doubleValue());
			break;
		case T_String :
			this.ldc(constant.stringValue());
			break;
		default : //reference object (constant can be from T_null or T_String)
			if (constant.typeID() == T_String)
				ldc(constant.stringValue());
			else
				aconst_null();
	}
}
/**
 * @param implicitConversionCode int
 */
public void generateImplicitConversion(int implicitConversionCode) {
	switch (implicitConversionCode) {
		case Float2Char :
			this.f2i();
			this.i2c();
			break;
		case Double2Char :
			this.d2i();
			this.i2c();
			break;
		case Int2Char :
		case Short2Char :
		case Byte2Char :
			this.i2c();
			break;
		case Long2Char :
			this.l2i();
			this.i2c();
			break;
		case Char2Float :
		case Short2Float :
		case Int2Float :
		case Byte2Float :
			this.i2f();
			break;
		case Double2Float :
			this.d2f();
			break;
		case Long2Float :
			this.l2f();
			break;
		case Float2Byte :
			this.f2i();
			this.i2b();
			break;
		case Double2Byte :
			this.d2i();
			this.i2b();
			break;
		case Int2Byte :
		case Short2Byte :
		case Char2Byte :
			this.i2b();
			break;
		case Long2Byte :
			this.l2i();
			this.i2b();
			break;
		case Byte2Double :
		case Char2Double :
		case Short2Double :
		case Int2Double :
			this.i2d();
			break;
		case Float2Double :
			this.f2d();
			break;
		case Long2Double :
			this.l2d();
			break;
		case Byte2Short :
		case Char2Short :
		case Int2Short :
			this.i2s();
			break;
		case Double2Short :
			this.d2i();
			this.i2s();
			break;
		case Long2Short :
			this.l2i();
			this.i2s();
			break;
		case Float2Short :
			this.f2i();
			this.i2s();
			break;
		case Double2Int :
			this.d2i();
			break;
		case Float2Int :
			this.f2i();
			break;
		case Long2Int :
			this.l2i();
			break;
		case Int2Long :
		case Char2Long :
		case Byte2Long :
		case Short2Long :
			this.i2l();
			break;
		case Double2Long :
			this.d2l();
			break;
		case Float2Long :
			this.f2l();
	}
}
public void generateInlinedValue(byte inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
	}
}
public void generateInlinedValue(char inlinedValue) {
	switch (inlinedValue) {
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((6 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			if ((128 <= inlinedValue) && (inlinedValue <= 32767)) {
				this.sipush(inlinedValue);
				return;
			}
			this.ldc(inlinedValue);
	}
}
public void generateInlinedValue(double inlinedValue) {
	if (inlinedValue == 0.0) {
		if (Double.doubleToLongBits(inlinedValue) != 0L)
			this.ldc2_w(inlinedValue);
		else
			this.dconst_0();
		return;
	}
	if (inlinedValue == 1.0) {
		this.dconst_1();
		return;
	}
	this.ldc2_w(inlinedValue);
}
public void generateInlinedValue(float inlinedValue) {
	if (inlinedValue == 0.0f) {
		if (Float.floatToIntBits(inlinedValue) != 0)
			this.ldc(inlinedValue);
		else
			this.fconst_0();
		return;
	}
	if (inlinedValue == 1.0f) {
		this.fconst_1();
		return;
	}
	if (inlinedValue == 2.0f) {
		this.fconst_2();
		return;
	}
	this.ldc(inlinedValue);
}
public void generateInlinedValue(int inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			if ((-32768 <= inlinedValue) && (inlinedValue <= 32767)) {
				this.sipush(inlinedValue);
				return;
			}
			this.ldc(inlinedValue);
	}
}
public void generateInlinedValue(long inlinedValue) {
	if (inlinedValue == 0) {
		this.lconst_0();
		return;
	}
	if (inlinedValue == 1) {
		this.lconst_1();
		return;
	}
	this.ldc2_w(inlinedValue);
}
public void generateInlinedValue(short inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			this.sipush(inlinedValue);
	}
}
public void generateInlinedValue(boolean inlinedValue) {
	if (inlinedValue)
		this.iconst_1();
	else
		this.iconst_0();
}
public void generateOuterAccess(Object[] mappingSequence, AstNode invocationSite, Scope scope) {
	if (mappingSequence == null)
		return;
	if (mappingSequence == BlockScope.EmulationPathToImplicitThis) {
		if (scope.methodScope().isConstructorCall){
			scope.problemReporter().errorThisSuperInStatic(invocationSite);
		}
		this.aload_0();
		return;
	}
	if (mappingSequence[0] instanceof FieldBinding) {
		FieldBinding fieldBinding = (FieldBinding) mappingSequence[0];
		if (scope.methodScope().isConstructorCall){
			scope.problemReporter().errorThisSuperInStatic(invocationSite);
		}
		this.aload_0();
		this.getfield(fieldBinding);
	} else {
		load((LocalVariableBinding) mappingSequence[0]);
	}
	for (int i = 1, length = mappingSequence.length; i < length; i++) {
		if (mappingSequence[i] instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding) mappingSequence[i];
			this.getfield(fieldBinding);
		} else {
			this.invokestatic((MethodBinding) mappingSequence[i]);
		}
	}
}
/**
 * The equivalent code performs a string conversion:
 *
 * @param oper1 org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param oper1 org.eclipse.jdt.internal.compiler.ast.Expression
 * @param oper2 org.eclipse.jdt.internal.compiler.ast.Expression
 */
public void generateStringAppend(BlockScope blockScope, Expression oper1, Expression oper2) {
	int pc;
	if (oper1 == null) {
		/* Operand is already on the stack, and maybe nil:
		note type1 is always to  java.lang.String here.*/
		this.newStringBuffer();
		this.dup_x1();
		this.swap();
		// If argument is reference type, need to transform it 
		// into a string (handles null case)
		this.invokeStringValueOf(T_Object);
		this.invokeStringBufferStringConstructor();
	} else {
		pc = position;
		oper1.generateOptimizedStringBufferCreation(blockScope, this, oper1.implicitConversion & 0xF);
		this.recordPositionsFrom(pc, oper1.sourceStart);
	}
	pc = position;
	oper2.generateOptimizedStringBuffer(blockScope, this, oper2.implicitConversion & 0xF);
	this.recordPositionsFrom(pc, oper2.sourceStart);
	this.invokeStringBufferToString();
}
/**
 * Code responsible to generate the suitable code to supply values for the synthetic arguments of
 * a constructor invocation of a nested type.
 */
public void generateSyntheticArgumentValues(BlockScope currentScope, ReferenceBinding targetType, Expression enclosingInstance, AstNode invocationSite) {

	// perform some emulation work in case there is some and we are inside a local type only
	ReferenceBinding[] syntheticArgumentTypes;

	// generate the enclosing instance first
	if ((syntheticArgumentTypes = targetType.syntheticEnclosingInstanceTypes()) != null) {

		ReferenceBinding targetEnclosingType = targetType.isAnonymousType() ? 
				targetType.superclass().enclosingType() // supplying enclosing instance for the anonymous type's superclass
				: targetType.enclosingType();
				
		for (int i = 0, max = syntheticArgumentTypes.length; i < max; i++) {
			ReferenceBinding syntheticArgType = syntheticArgumentTypes[i];
			if (enclosingInstance != null && i == 0) {
				if (syntheticArgType != targetEnclosingType) {
					currentScope.problemReporter().unnecessaryEnclosingInstanceSpecification(enclosingInstance, targetType);
				}
				//if (currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4){
				enclosingInstance.generateCode(currentScope, this, true);
				if (syntheticArgType == targetEnclosingType){
					this.dup();
				} 
				this.invokeObjectGetClass(); // causes null check for all explicit enclosing instances
				this.pop();
				//} else {
				//	enclosingInstance.generateCode(currentScope, this, syntheticArgType == targetEnclosingType);
				//}			
			} else {
				Object[] emulationPath = currentScope.getCompatibleEmulationPath(syntheticArgType);
				if (emulationPath == null) {
					currentScope.problemReporter().missingEnclosingInstanceSpecification(syntheticArgType, invocationSite);
				} else {
					this.generateOuterAccess(emulationPath, invocationSite, currentScope);
				}
			}
		}
	} else { // we may still have an enclosing instance to consider
		if (enclosingInstance != null) {
			currentScope.problemReporter().unnecessaryEnclosingInstanceSpecification(enclosingInstance, targetType);
			//if (currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4){
			enclosingInstance.generateCode(currentScope, this, true);
			this.invokeObjectGetClass(); // causes null check for all explicit enclosing instances
			this.pop();
			//} else {
			//	enclosingInstance.generateCode(currentScope, this, false); // do not want the value
			//}			
		}
	}
	// generate the synthetic outer arguments then
	SyntheticArgumentBinding syntheticArguments[];
	if ((syntheticArguments = targetType.syntheticOuterLocalVariables()) != null) {
		for (int i = 0, max = syntheticArguments.length; i < max; i++) {
			VariableBinding[] emulationPath = currentScope.getEmulationPath(syntheticArguments[i].actualOuterLocalVariable);
			if (emulationPath == null) {
				// could not emulate a path to a given outer local variable (internal error)
				currentScope.problemReporter().needImplementation();
			} else {
				this.generateOuterAccess(emulationPath, invocationSite, currentScope);
			}
		}
	}
}
/**
 * @param parameters org.eclipse.jdt.internal.compiler.lookup.TypeBinding[]
 * @param constructorBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
 */
public void generateSyntheticBodyForConstructorAccess(SyntheticAccessMethodBinding accessBinding) {

	initializeMaxLocals(accessBinding);

	MethodBinding constructorBinding = accessBinding.targetMethod;
	TypeBinding[] parameters = constructorBinding.parameters;
	int length = parameters.length;
	int resolvedPosition = 1;
	this.aload_0();
	if (constructorBinding.declaringClass.isNestedType()) {
		NestedTypeBinding nestedType = (NestedTypeBinding) constructorBinding.declaringClass;
		SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticEnclosingInstances();
		for (int i = 0; i < (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
			TypeBinding type;
			load((type = syntheticArguments[i].type), resolvedPosition);
			if ((type == DoubleBinding) || (type == LongBinding))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
		syntheticArguments = nestedType.syntheticOuterLocalVariables();
		for (int i = 0; i < (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
			TypeBinding type;
			load((type = syntheticArguments[i].type), resolvedPosition);
			if ((type == DoubleBinding) || (type == LongBinding))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
	}
	for (int i = 0; i < length; i++) {
		load(parameters[i], resolvedPosition);
		if ((parameters[i] == DoubleBinding) || (parameters[i] == LongBinding))
			resolvedPosition += 2;
		else
			resolvedPosition++;
	}
	this.invokespecial(constructorBinding);
	this.return_();
}
public void generateSyntheticBodyForFieldReadAccess(SyntheticAccessMethodBinding accessBinding) {
	initializeMaxLocals(accessBinding);
	FieldBinding fieldBinding = accessBinding.targetReadField;
	TypeBinding type;
	if (fieldBinding.isStatic())
		this.getstatic(fieldBinding);
	else {
		this.aload_0();
		this.getfield(fieldBinding);
	}
	if ((type = fieldBinding.type).isBaseType()) {
		if (type == IntBinding)
			this.ireturn();
		else
			if (type == FloatBinding)
				this.freturn();
			else
				if (type == LongBinding)
					this.lreturn();
				else
					if (type == DoubleBinding)
						this.dreturn();
					else
						this.ireturn();
	} else
		this.areturn();
}
public void generateSyntheticBodyForFieldWriteAccess(SyntheticAccessMethodBinding accessBinding) {
	initializeMaxLocals(accessBinding);
	FieldBinding fieldBinding = accessBinding.targetWriteField;
	if (fieldBinding.isStatic()) {
		load(fieldBinding.type, 0);
		this.putstatic(fieldBinding);
	} else {
		this.aload_0();
		load(fieldBinding.type, 1);
		this.putfield(fieldBinding);
	}
	this.return_();
}
public void generateSyntheticBodyForMethodAccess(SyntheticAccessMethodBinding accessBinding) {

	initializeMaxLocals(accessBinding);
	MethodBinding methodBinding = accessBinding.targetMethod;
	TypeBinding[] parameters = methodBinding.parameters;
	int length = parameters.length;
	int resolvedPosition;
	if (methodBinding.isStatic())
		resolvedPosition = 0;
	else {
		this.aload_0();
		resolvedPosition = 1;
	}
	for (int i = 0; i < length; i++) {
		load(parameters[i], resolvedPosition);
		if ((parameters[i] == DoubleBinding) || (parameters[i] == LongBinding))
			resolvedPosition += 2;
		else
			resolvedPosition++;
	}
	TypeBinding type;
	if (methodBinding.isStatic())
		this.invokestatic(methodBinding);
	else {
		if (methodBinding.isConstructor()
			|| methodBinding.isPrivate()
			// qualified super "X.super.foo()" targets methods from superclass
			|| (methodBinding.declaringClass != methodDeclaration.binding.declaringClass)){
			this.invokespecial(methodBinding);
		} else {
			if (methodBinding.declaringClass.isInterface()){
				this.invokeinterface(methodBinding);
			} else {
				this.invokevirtual(methodBinding);
			}
		}
	}
	if ((type = methodBinding.returnType).isBaseType())
		if (type == VoidBinding)
			this.return_();
		else
			if (type == IntBinding)
				this.ireturn();
			else
				if (type == FloatBinding)
					this.freturn();
				else
					if (type == LongBinding)
						this.lreturn();
					else
						if (type == DoubleBinding)
							this.dreturn();
						else
							this.ireturn();
	else
		this.areturn();
}
final public byte[] getContents() {
	byte[] contents;
	System.arraycopy(bCodeStream, 0, contents = new byte[position], 0, position);
	return contents;
}
final public void getfield(FieldBinding fieldBinding) {
	countLabels = 0;
	if ((fieldBinding.type.id == T_double) || (fieldBinding.type.id == T_long)) {
		if (++stackDepth > stackMax)
			stackMax = stackDepth;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_getfield;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_getfield);
	}
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
final public void getstatic(FieldBinding fieldBinding) {
	countLabels = 0;
	if ((fieldBinding.type.id == T_double) || (fieldBinding.type.id == T_long))
		stackDepth += 2;
	else
		stackDepth += 1;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_getstatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_getstatic);
	}
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
public void getSystemOut() {
	countLabels = 0;
	if (++stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_getstatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_getstatic);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangSystemOut());
}
public void getTYPE(int baseTypeID) {
	countLabels = 0;
	if (++stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_getstatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_getstatic);
	}
	switch (baseTypeID) {
		// getstatic: java.lang.Byte.TYPE			
		case T_byte :
			writeUnsignedShort(constantPool.literalIndexForJavaLangByteTYPE());
			break;
			// getstatic: java.lang.Short.TYPE			
		case T_short :
			writeUnsignedShort(constantPool.literalIndexForJavaLangShortTYPE());
			break;
			// getstatic: java.lang.Character.TYPE			
		case T_char :
			writeUnsignedShort(constantPool.literalIndexForJavaLangCharacterTYPE());
			break;
			// getstatic: java.lang.Integer.TYPE			
		case T_int :
			writeUnsignedShort(constantPool.literalIndexForJavaLangIntegerTYPE());
			break;
			// getstatic: java.lang.Long.TYPE			
		case T_long :
			writeUnsignedShort(constantPool.literalIndexForJavaLangLongTYPE());
			break;
			// getstatic: java.lang.Float.TYPE			
		case T_float :
			writeUnsignedShort(constantPool.literalIndexForJavaLangFloatTYPE());
			break;
			// getstatic: java.lang.Double.TYPE			
		case T_double :
			writeUnsignedShort(constantPool.literalIndexForJavaLangDoubleTYPE());
			break;
			// getstatic: java.lang.Boolean.TYPE			
		case T_boolean :
			writeUnsignedShort(constantPool.literalIndexForJavaLangBooleanTYPE());
			break;
			// getstatic: java.lang.Void.TYPE
		case T_void :
			writeUnsignedShort(constantPool.literalIndexForJavaLangVoidTYPE());
			break;
	}
}
/**
 * We didn't call it goto, because there is a conflit with the goto keyword
 */
final public void goto_(Label lbl) {
	if (this.wideMode) {
		this.goto_w(lbl);
		return;
	}
	try {
		lbl.inlineForwardReferencesFromLabelsTargeting(position);
		/*
		 Possible optimization for code such as:
		 public Object foo() {
			boolean b = true;
			if (b) {
				if (b)
					return null;
			} else {
				if (b) {
					return null;
				}
			}
			return null;
		}
		The goto around the else block for the first if will
		be unreachable, because the thenClause of the second if
		returns.
		See inlineForwardReferencesFromLabelsTargeting defined
		on the Label class for the remaining part of this
		optimization.
		 if (!lbl.isBranchTarget(position)) {
			switch(bCodeStream[classFileOffset-1]) {
				case OPC_return :
				case OPC_areturn:
					return;
			}
		}*/
		position++;
		bCodeStream[classFileOffset++] = OPC_goto;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_goto);
	}
	lbl.branch();
}

/**
 * We didn't call it goto, because there is a conflit with the goto keyword
 */
final public void internal_goto_(Label lbl) {
	try {
		lbl.inlineForwardReferencesFromLabelsTargeting(position);
		/*
		 Possible optimization for code such as:
		 public Object foo() {
			boolean b = true;
			if (b) {
				if (b)
					return null;
			} else {
				if (b) {
					return null;
				}
			}
			return null;
		}
		The goto around the else block for the first if will
		be unreachable, because the thenClause of the second if
		returns.
		See inlineForwardReferencesFromLabelsTargeting defined
		on the Label class for the remaining part of this
		optimization.
		 if (!lbl.isBranchTarget(position)) {
			switch(bCodeStream[classFileOffset-1]) {
				case OPC_return :
				case OPC_areturn:
					return;
			}
		}*/
		position++;
		bCodeStream[classFileOffset++] = OPC_goto;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_goto);
	}
	lbl.branch();
}
final public void goto_w(Label lbl) {
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_goto_w;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_goto_w);
	}
	lbl.branchWide();
}
final public void i2b() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_i2b;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_i2b);
	}
}
final public void i2c() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_i2c;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_i2c);
	}
}
final public void i2d() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_i2d;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_i2d);
	}
}
final public void i2f() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_i2f;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_i2f);
	}
}
final public void i2l() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_i2l;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_i2l);
	}
}
final public void i2s() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_i2s;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_i2s);
	}
}
final public void iadd() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iadd;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iadd);
	}
}
final public void iaload() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iaload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iaload);
	}
}
final public void iand() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iand;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iand);
	}
}
final public void iastore() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iastore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iastore);
	}
}
final public void iconst_0() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iconst_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iconst_0);
	}
}
final public void iconst_1() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iconst_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iconst_1);
	}
}
final public void iconst_2() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iconst_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iconst_2);
	}
}
final public void iconst_3() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iconst_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iconst_3);
	}
}
final public void iconst_4() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iconst_4;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iconst_4);
	}
}
final public void iconst_5() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iconst_5;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iconst_5);
	}
}
final public void iconst_m1() {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iconst_m1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iconst_m1);
	}
}
final public void idiv() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_idiv;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_idiv);
	}
}
final public void if_acmpeq(Label lbl) {
	countLabels = 0;
	stackDepth-=2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_acmpeq, lbl);
	} else {	
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_acmpeq;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_acmpeq);
		}
		lbl.branch();
	}
}
final public void if_acmpne(Label lbl) {
	countLabels = 0;
	stackDepth-=2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_acmpne, lbl);
	} else {	
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_acmpne;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_acmpne);
		}
		lbl.branch();
	}
}
final public void if_icmpeq(Label lbl) {
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_icmpeq, lbl);
	} else {	
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_icmpeq;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_icmpeq);
		}
		lbl.branch();
	}
}
final public void if_icmpge(Label lbl) {
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_icmpge, lbl);
	} else {	
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_icmpge;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_icmpge);
		}
		lbl.branch();
	}
}
final public void if_icmpgt(Label lbl) {
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_icmpgt, lbl);
	} else {	
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_icmpgt;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_icmpgt);
		}
		lbl.branch();
	}
}
final public void if_icmple(Label lbl) {
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_icmple, lbl);
	} else {	
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_icmple;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_icmple);
		}
		lbl.branch();
	}
}
final public void if_icmplt(Label lbl) {
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_icmplt, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_icmplt;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_icmplt);
		}
		lbl.branch();
	}
}
final public void if_icmpne(Label lbl) {
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_if_icmpne, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_if_icmpne;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_if_icmpne);
		}
		lbl.branch();
	}
}
final public void ifeq(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_ifeq, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ifeq;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ifeq);
		}
		lbl.branch();
	}
}
final public void ifge(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_ifge, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ifge;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ifge);
		}
		lbl.branch();
	}
}
final public void ifgt(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_ifgt, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ifgt;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ifgt);
		}
		lbl.branch();
	}
}
final public void ifle(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_ifle, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ifle;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ifle);
		}
		lbl.branch();
	}
}
final public void iflt(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_iflt, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_iflt;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_iflt);
		}
		lbl.branch();
	}
}
final public void ifne(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_ifne, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ifne;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ifne);
		}
		lbl.branch();
	}
}
final public void ifnonnull(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_ifnonnull, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ifnonnull;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ifnonnull);
		}
		lbl.branch();
	}
}
final public void ifnull(Label lbl) {
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideConditionalBranch(OPC_ifnull, lbl);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ifnull;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ifnull);
		}
		lbl.branch();
	}
}
final public void iinc(int index, int value) {
	countLabels = 0;
	if ((index > 255) || (value < -128 || value > 127)) { // have to widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_iinc;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_iinc);
		}
		writeUnsignedShort(index);
		writeSignedShort(value);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_iinc;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_iinc);
		}
		writeUnsignedByte(index);
		writeSignedByte(value);
	}
}
final public void iload(int iArg) {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_iload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_iload);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_iload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_iload);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void iload_0() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 0) {
		maxLocals = 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iload_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iload_0);
	}
}
final public void iload_1() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iload_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iload_1);
	}
}
final public void iload_2() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iload_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iload_2);
	}
}
final public void iload_3() {
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iload_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iload_3);
	}
}
final public void imul() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_imul;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_imul);
	}
}
public void incrementTemp(LocalVariableBinding localBinding, int value) {
	if (value == (short) value) {
		this.iinc(localBinding.resolvedPosition, value);
		return;
	}
	load(localBinding);
	this.ldc(value);
	this.iadd();
	store(localBinding, false);
}
public void incrStackSize(int offset) {
	if ((stackDepth += offset) > stackMax)
		stackMax = stackDepth;
}
public int indexOfSameLineEntrySincePC(int pc, int line) {
	for (int index = pc, max = pcToSourceMapSize; index < max; index+=2) {
		if (pcToSourceMap[index+1] == line)
			return index;
	}
	return -1;
}
final public void ineg() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ineg;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ineg);
	}
}
public void init(ClassFile classFile) {
	this.classFile = classFile;
	this.constantPool = classFile.constantPool;
	this.bCodeStream = classFile.contents;
	this.classFileOffset = classFile.contentsOffset;
	this.startingClassFileOffset = this.classFileOffset;
	pcToSourceMapSize = 0;
	lastEntryPC = 0;
	int length = visibleLocals.length;
	if (noVisibleLocals.length < length) {
		noVisibleLocals = new LocalVariableBinding[length];
	}
	System.arraycopy(noVisibleLocals, 0, visibleLocals, 0, length);
	visibleLocalsCount = 0;
	
	length = locals.length;
	if (noLocals.length < length) {
		noLocals = new LocalVariableBinding[length];
	}
	System.arraycopy(noLocals, 0, locals, 0, length);
	allLocalsCounter = 0;

	length = exceptionHandlers.length;
	if (noExceptionHandlers.length < length) {
		noExceptionHandlers = new ExceptionLabel[length];
	}
	System.arraycopy(noExceptionHandlers, 0, exceptionHandlers, 0, length);
	exceptionHandlersNumber = 0;
	
	length = labels.length;
	if (noLabels.length < length) {
		noLabels = new Label[length];
	}
	System.arraycopy(noLabels, 0, labels, 0, length);
	countLabels = 0;

	stackMax = 0;
	stackDepth = 0;
	maxLocals = 0;
	position = 0;
}
/**
 * @param methodDeclaration org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
 */
public void initializeMaxLocals(MethodBinding methodBinding) {

	maxLocals = (methodBinding == null || methodBinding.isStatic()) ? 0 : 1;
	// take into account the synthetic parameters
	if (methodBinding != null) {
		if (methodBinding.isConstructor() && methodBinding.declaringClass.isNestedType()) {
			ReferenceBinding enclosingInstanceTypes[];
			if ((enclosingInstanceTypes = methodBinding.declaringClass.syntheticEnclosingInstanceTypes()) != null) {
				for (int i = 0, max = enclosingInstanceTypes.length; i < max; i++) {
					maxLocals++; // an enclosingInstanceType can only be a reference binding. It cannot be
					// LongBinding or DoubleBinding
				}
			}
			SyntheticArgumentBinding syntheticArguments[];
			if ((syntheticArguments = methodBinding.declaringClass.syntheticOuterLocalVariables()) != null) {
				for (int i = 0, max = syntheticArguments.length; i < max; i++) {
					TypeBinding argType;
					if (((argType = syntheticArguments[i].type) == LongBinding) || (argType == DoubleBinding)) {
						maxLocals += 2;
					} else {
						maxLocals++;
					}
				}
			}
		}
		TypeBinding[] arguments;
		if ((arguments = methodBinding.parameters) != null) {
			for (int i = 0, max = arguments.length; i < max; i++) {
				TypeBinding argType;
				if (((argType = arguments[i]) == LongBinding) || (argType == DoubleBinding)) {
					maxLocals += 2;
				} else {
					maxLocals++;
				}
			}
		}
	}
}
/**
 * This methods searches for an existing entry inside the pcToSourceMap table with a pc equals to @pc.
 * If there is an existing entry it returns -1 (no insertion required).
 * Otherwise it returns the index where the entry for the pc has to be inserted.
 * This is based on the fact that the pcToSourceMap table is sorted according to the pc.
 *
 * @param int pc
 * @return int
 */
public static int insertionIndex(int[] pcToSourceMap, int length, int pc) {
	int g = 0;
	int d = length - 2;
	int m = 0;
	while (g <= d) {
		m = (g + d) / 2;
		// we search only on even indexes
		if ((m % 2) != 0)
			m--;
		int currentPC = pcToSourceMap[m];
		if (pc < currentPC) {
			d = m - 2;
		} else
			if (pc > currentPC) {
				g = m + 2;
			} else {
				return -1;
			}
	}
	if (pc < pcToSourceMap[m])
		return m;
	return m + 2;
}
/**
 * We didn't call it instanceof because there is a conflit with the
 * instanceof keyword
 */
final public void instance_of(TypeBinding typeBinding) {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_instanceof;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_instanceof);
	}
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
public void invokeClassForName() {
	// invokestatic: java.lang.Class.forName(Ljava.lang.String;)Ljava.lang.Class;
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokestatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokestatic);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangClassForName());
}

public void invokeJavaLangClassDesiredAssertionStatus() {
	// invokevirtual: java.lang.Class.desiredAssertionStatus()Z;
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangClassDesiredAssertionStatus());
}

public void invokeConstructorGetConstructor() {
	// invokevirtual: java.lang.Class.getConstructor(java.lang.Class[])Ljava.lang.reflect.Constructor;
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangClassGetConstructor());
}
final public void invokeinterface(MethodBinding methodBinding) {
	// initialized to 1 to take into account this  immediately
	countLabels = 0;
	int argCount = 1;
	int id;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokeinterface;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokeinterface);
	}
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount += 1;
	writeUnsignedByte(argCount);
	// Generate a  0 into the byte array. Like the array is already fill with 0, we just need to increment
	// the number of bytes.
	position++;
	classFileOffset++;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long))
		stackDepth += (2 - argCount);
	else
		if (id == T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
public void invokeJavaLangErrorConstructor() {
	// invokespecial: java.lang.Error<init>(Ljava.lang.String;)V
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokespecial;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokespecial);
	}
	stackDepth -= 2;
	writeUnsignedShort(constantPool.literalIndexForJavaLangErrorConstructor());
}
public void invokeNoClassDefFoundErrorStringConstructor() {
	// invokespecial: java.lang.NoClassDefFoundError.<init>(Ljava.lang.String;)V
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokespecial;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokespecial);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangNoClassDefFoundErrorStringConstructor());
	stackDepth -= 2;
}
public void invokeObjectNewInstance() {
	// invokevirtual: java.lang.reflect.Constructor.newInstance(java.lang.Object[])Ljava.lang.Object;
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangReflectConstructorNewInstance());
}

public void invokeObjectGetClass() {
	// invokevirtual: java.lang.Object.getClass()Ljava.lang.Class;
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangObjectGetClass());
}

final public void invokespecial(MethodBinding methodBinding) {
	// initialized to 1 to take into account this  immediately
	countLabels = 0;
	int argCount = 1;
	int id;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokespecial;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokespecial);
	}
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	if (methodBinding.isConstructor() && methodBinding.declaringClass.isNestedType()) {
		// enclosing instances
		TypeBinding[] syntheticArgumentTypes = methodBinding.declaringClass.syntheticEnclosingInstanceTypes();
		if (syntheticArgumentTypes != null) {
			for (int i = 0, max = syntheticArgumentTypes.length; i < max; i++) {
				if (((id = syntheticArgumentTypes[i].id) == T_double) || (id == T_long)) {
					argCount += 2;
				} else {
					argCount++;
				}
			}
		}
		// outer local variables
		SyntheticArgumentBinding[] syntheticArguments = methodBinding.declaringClass.syntheticOuterLocalVariables();
		if (syntheticArguments != null) {
			for (int i = 0, max = syntheticArguments.length; i < max; i++) {
				if (((id = syntheticArguments[i].type.id) == T_double) || (id == T_long)) {
					argCount += 2;
				} else {
					argCount++;
				}
			}
		}
	}
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount++;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long))
		stackDepth += (2 - argCount);
	else
		if (id == T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
final public void invokestatic(MethodBinding methodBinding) {
	// initialized to 0 to take into account that there is no this for
	// a static method
	countLabels = 0;
	int argCount = 0;
	int id;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokestatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokestatic);
	}
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount += 1;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long))
		stackDepth += (2 - argCount);
	else
		if (id == T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
/**
 * The equivalent code performs a string conversion of the TOS
 * @param typeID <CODE>int</CODE>
 */
public void invokeStringBufferAppendForType(int typeID) {
	countLabels = 0;
	int usedTypeID;
	if (typeID == T_null)
		usedTypeID = T_String;
	else
		usedTypeID = typeID;
	// invokevirtual
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferAppend(typeID));
	if ((usedTypeID == T_long) || (usedTypeID == T_double))
		stackDepth -= 2;
	else
		stackDepth--;
}

public void invokeJavaLangAssertionErrorConstructor(int typeBindingID) {
	// invokespecial: java.lang.AssertionError.<init>(typeBindingID)V
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokespecial;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokespecial);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangAssertionErrorConstructor(typeBindingID));
	stackDepth -= 2;
}

public void invokeJavaLangAssertionErrorDefaultConstructor() {
	// invokespecial: java.lang.AssertionError.<init>()V
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokespecial;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokespecial);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangAssertionErrorDefaultConstructor());
	stackDepth --;
}

public void invokeStringBufferDefaultConstructor() {
	// invokespecial: java.lang.StringBuffer.<init>()V
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokespecial;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokespecial);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferDefaultConstructor());
	stackDepth--;
}
public void invokeStringBufferStringConstructor() {
	// invokespecial: java.lang.StringBuffer.<init>(Ljava.lang.String;)V
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokespecial;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokespecial);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferConstructor());
	stackDepth -= 2;
}

public void invokeStringBufferToString() {
	// invokevirtual: StringBuffer.toString()Ljava.lang.String;
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferToString());
}
public void invokeStringIntern() {
	// invokevirtual: java.lang.String.intern()
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringIntern());
}
public void invokeStringValueOf(int typeID) {
	// invokestatic: java.lang.String.valueOf(argumentType)
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokestatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokestatic);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringValueOf(typeID));
}
public void invokeSystemExit() {
	// invokestatic: java.lang.System.exit(I)
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokestatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokestatic);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangSystemExitInt());
	stackDepth--; // int argument
}
public void invokeThrowableGetMessage() {
	// invokevirtual: java.lang.Throwable.getMessage()Ljava.lang.String;
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangThrowableGetMessage());
}
final public void invokevirtual(MethodBinding methodBinding) {
	// initialized to 1 to take into account this  immediately
	countLabels = 0;
	int argCount = 1;
	int id;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_invokevirtual;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_invokevirtual);
	}
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount++;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long))
		stackDepth += (2 - argCount);
	else
		if (id == T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
final public void ior() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ior;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ior);
	}
}
final public void irem() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_irem;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_irem);
	}
}
final public void ireturn() {
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ireturn;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ireturn);
	}
}
public boolean isDefinitelyAssigned(Scope scope, int initStateIndex, LocalVariableBinding local) {
	// Dependant of UnconditionalFlowInfo.isDefinitelyAssigned(..)
	if (initStateIndex == -1)
		return false;
	if (local.isArgument) {
		return true;
	}
	int position = local.id + maxFieldCount;
	MethodScope methodScope = scope.methodScope();
	// id is zero-based
	if (position < UnconditionalFlowInfo.BitCacheSize) {
		return (methodScope.definiteInits[initStateIndex] & (1L << position)) != 0; // use bits
	}
	// use extra vector
	long[] extraInits = methodScope.extraDefiniteInits[initStateIndex];
	if (extraInits == null)
		return false; // if vector not yet allocated, then not initialized
	int vectorIndex;
	if ((vectorIndex = (position / UnconditionalFlowInfo.BitCacheSize) - 1) >= extraInits.length)
		return false; // if not enough room in vector, then not initialized 
	return ((extraInits[vectorIndex]) & (1L << (position % UnconditionalFlowInfo.BitCacheSize))) != 0;
}
final public void ishl() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ishl;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ishl);
	}
}
final public void ishr() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ishr;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ishr);
	}
}
final public void istore(int iArg) {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_istore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_istore);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_istore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_istore);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void istore_0() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_istore_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_istore_0);
	}
}
final public void istore_1() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_istore_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_istore_1);
	}
}
final public void istore_2() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_istore_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_istore_2);
	}
}
final public void istore_3() {
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_istore_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_istore_3);
	}
}
final public void isub() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_isub;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_isub);
	}
}
final public void iushr() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_iushr;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_iushr);
	}
}
final public void ixor() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ixor;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ixor);
	}
}
final public void jsr(Label lbl) {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_jsr;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_jsr);
	}
	lbl.branch();
}
final public void jsr_w(Label lbl) {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_jsr_w;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_jsr_w);
	}
	lbl.branchWide();
}
final public void l2d() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_l2d;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_l2d);
	}
}
final public void l2f() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_l2f;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_l2f);
	}
}
final public void l2i() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_l2i;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_l2i);
	}
}
final public void ladd() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ladd;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ladd);
	}
}
final public void laload() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_laload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_laload);
	}
}
final public void land() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_land;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_land);
	}
}
final public void lastore() {
	countLabels = 0;
	stackDepth -= 4;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lastore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lastore);
	}
}
final public void lcmp() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lcmp;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lcmp);
	}
}
final public void lconst_0() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lconst_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lconst_0);
	}
}
final public void lconst_1() {
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lconst_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lconst_1);
	}
}
final public void ldc(float constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		// Generate a ldc_w
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ldc_w;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ldc_w);
		}
		writeUnsignedShort(index);
	} else {
		// Generate a ldc
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ldc;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ldc);
		}
		writeUnsignedByte(index);
	}
}
final public void ldc(int constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		// Generate a ldc_w
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ldc_w;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ldc_w);
		}
		writeUnsignedShort(index);
	} else {
		// Generate a ldc
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ldc;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ldc);
		}
		writeUnsignedByte(index);
	}
}
final public void ldc(String constant) {
	countLabels = 0;
	int currentConstantPoolIndex = constantPool.currentIndex;
	int currentConstantPoolOffset = constantPool.currentOffset;
	int currentCodeStreamPosition = position;
	int index = constantPool.literalIndexForLdc(constant.toCharArray());
	if (index > 0) {
		// the string already exists inside the constant pool
		// we reuse the same index
		stackDepth++;
		if (stackDepth > stackMax)
			stackMax = stackDepth;
		if (index > 255) {
			// Generate a ldc_w
			try {
				position++;
				bCodeStream[classFileOffset++] = OPC_ldc_w;
			} catch (IndexOutOfBoundsException e) {
				resizeByteArray(OPC_ldc_w);
			}
			writeUnsignedShort(index);
		} else {
			// Generate a ldc
			try {
				position++;
				bCodeStream[classFileOffset++] = OPC_ldc;
			} catch (IndexOutOfBoundsException e) {
				resizeByteArray(OPC_ldc);
			}
			writeUnsignedByte(index);
		}
	} else {
		// the string is too big to be utf8-encoded in one pass.
		// we have to split it into different pieces.
		// first we clean all side-effects due to the code above
		// this case is very rare, so we can afford to lose time to handle it
		char[] constantChars = constant.toCharArray();
		position = currentCodeStreamPosition;
		constantPool.currentIndex = currentConstantPoolIndex;
		constantPool.currentOffset = currentConstantPoolOffset;
		constantPool.stringCache.remove(constantChars);
		constantPool.UTF8Cache.remove(constantChars);
		int i = 0;
		int length = 0;
		int constantLength = constant.length();
		byte[] utf8encoding = new byte[Math.min(constantLength + 100, 65535)];
		int utf8encodingLength = 0;
		while ((length < 65532) && (i < constantLength)) {
			char current = constantChars[i];
			// we resize the byte array immediately if necessary
			if (length + 3 > (utf8encodingLength = utf8encoding.length)) {
				System.arraycopy(utf8encoding, 0, (utf8encoding = new byte[Math.min(utf8encodingLength + 100, 65535)]), 0, length);
			}
			if ((current >= 0x0001) && (current <= 0x007F)) {
				// we only need one byte: ASCII table
				utf8encoding[length++] = (byte) current;
			} else {
				if (current > 0x07FF) {
					// we need 3 bytes
					utf8encoding[length++] = (byte) (0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
					utf8encoding[length++] = (byte) (0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
					utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				} else {
					// we can be 0 or between 0x0080 and 0x07FF
					// In that case we only need 2 bytes
					utf8encoding[length++] = (byte) (0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
					utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				}
			}
			i++;
		}
		// check if all the string is encoded (PR 1PR2DWJ)
		// the string is too big to be encoded in one pass
		newStringBuffer();
		dup();
		// write the first part
		char[] subChars = new char[i];
		System.arraycopy(constantChars, 0, subChars, 0, i);
		System.arraycopy(utf8encoding, 0, (utf8encoding = new byte[length]), 0, length);
		index = constantPool.literalIndex(subChars, utf8encoding);
		stackDepth++;
		if (stackDepth > stackMax)
			stackMax = stackDepth;
		if (index > 255) {
			// Generate a ldc_w
			try {
				position++;
				bCodeStream[classFileOffset++] = OPC_ldc_w;
			} catch (IndexOutOfBoundsException e) {
				resizeByteArray(OPC_ldc_w);
			}
			writeUnsignedShort(index);
		} else {
			// Generate a ldc
			try {
				position++;
				bCodeStream[classFileOffset++] = OPC_ldc;
			} catch (IndexOutOfBoundsException e) {
				resizeByteArray(OPC_ldc);
			}
			writeUnsignedByte(index);
		}
		// write the remaining part
		invokeStringBufferStringConstructor();
		while (i < constantLength) {
			length = 0;
			utf8encoding = new byte[Math.min(constantLength - i + 100, 65535)];
			int startIndex = i;
			while ((length < 65532) && (i < constantLength)) {
				char current = constantChars[i];
				// we resize the byte array immediately if necessary
				if (constantLength + 2 > (utf8encodingLength = utf8encoding.length)) {
					System.arraycopy(utf8encoding, 0, (utf8encoding = new byte[Math.min(utf8encodingLength + 100, 65535)]), 0, length);
				}
				if ((current >= 0x0001) && (current <= 0x007F)) {
					// we only need one byte: ASCII table
					utf8encoding[length++] = (byte) current;
				} else {
					if (current > 0x07FF) {
						// we need 3 bytes
						utf8encoding[length++] = (byte) (0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
						utf8encoding[length++] = (byte) (0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
						utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					} else {
						// we can be 0 or between 0x0080 and 0x07FF
						// In that case we only need 2 bytes
						utf8encoding[length++] = (byte) (0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
						utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					}
				}
				i++;
			}
			// the next part is done
			subChars = new char[i - startIndex];
			System.arraycopy(constantChars, startIndex, subChars, 0, i - startIndex);
			System.arraycopy(utf8encoding, 0, (utf8encoding = new byte[length]), 0, length);
			index = constantPool.literalIndex(subChars, utf8encoding);
			stackDepth++;
			if (stackDepth > stackMax)
				stackMax = stackDepth;
			if (index > 255) {
				// Generate a ldc_w
				try {
					position++;
					bCodeStream[classFileOffset++] = OPC_ldc_w;
				} catch (IndexOutOfBoundsException e) {
					resizeByteArray(OPC_ldc_w);
				}
				writeUnsignedShort(index);
			} else {
				// Generate a ldc
				try {
					position++;
					bCodeStream[classFileOffset++] = OPC_ldc;
				} catch (IndexOutOfBoundsException e) {
					resizeByteArray(OPC_ldc);
				}
				writeUnsignedByte(index);
			}
			// now on the stack it should be a StringBuffer and a string.
			invokeStringBufferAppendForType(T_String);
		}
		invokeStringBufferToString();
		invokeStringIntern();
	}
}
final public void ldc2_w(double constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	// Generate a ldc2_w
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ldc2_w;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ldc2_w);
	}
	writeUnsignedShort(index);
}
final public void ldc2_w(long constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	// Generate a ldc2_w
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ldc2_w;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ldc2_w);
	}
	writeUnsignedShort(index);
}
final public void ldiv() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_ldiv;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_ldiv);
	}
}
final public void lload(int iArg) {
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_lload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_lload);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_lload;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_lload);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void lload_0() {
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lload_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lload_0);
	}
}
final public void lload_1() {
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lload_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lload_1);
	}
}
final public void lload_2() {
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lload_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lload_2);
	}
}
final public void lload_3() {
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lload_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lload_3);
	}
}
final public void lmul() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lmul;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lmul);
	}
}
final public void lneg() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lneg;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lneg);
	}
}
public final void load(LocalVariableBinding localBinding) {
	countLabels = 0;
	TypeBinding typeBinding = localBinding.type;
	int resolvedPosition = localBinding.resolvedPosition;
	// Using dedicated int bytecode
	if (typeBinding == IntBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}
	// Using dedicated float bytecode
	if (typeBinding == FloatBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.fload_0();
				break;
			case 1 :
				this.fload_1();
				break;
			case 2 :
				this.fload_2();
				break;
			case 3 :
				this.fload_3();
				break;
			default :
				this.fload(resolvedPosition);
		}
		return;
	}
	// Using dedicated long bytecode
	if (typeBinding == LongBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.lload_0();
				break;
			case 1 :
				this.lload_1();
				break;
			case 2 :
				this.lload_2();
				break;
			case 3 :
				this.lload_3();
				break;
			default :
				this.lload(resolvedPosition);
		}
		return;
	}
	// Using dedicated double bytecode
	if (typeBinding == DoubleBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.dload_0();
				break;
			case 1 :
				this.dload_1();
				break;
			case 2 :
				this.dload_2();
				break;
			case 3 :
				this.dload_3();
				break;
			default :
				this.dload(resolvedPosition);
		}
		return;
	}
	// boolean, byte, char and short are handled as int
	if ((typeBinding == ByteBinding) || (typeBinding == CharBinding) || (typeBinding == BooleanBinding) || (typeBinding == ShortBinding)) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}

	// Reference object
	switch (resolvedPosition) {
		case 0 :
			this.aload_0();
			break;
		case 1 :
			this.aload_1();
			break;
		case 2 :
			this.aload_2();
			break;
		case 3 :
			this.aload_3();
			break;
		default :
			this.aload(resolvedPosition);
	}
}
public final void load(TypeBinding typeBinding, int resolvedPosition) {
	countLabels = 0;
	// Using dedicated int bytecode
	if (typeBinding == IntBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}
	// Using dedicated float bytecode
	if (typeBinding == FloatBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.fload_0();
				break;
			case 1 :
				this.fload_1();
				break;
			case 2 :
				this.fload_2();
				break;
			case 3 :
				this.fload_3();
				break;
			default :
				this.fload(resolvedPosition);
		}
		return;
	}
	// Using dedicated long bytecode
	if (typeBinding == LongBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.lload_0();
				break;
			case 1 :
				this.lload_1();
				break;
			case 2 :
				this.lload_2();
				break;
			case 3 :
				this.lload_3();
				break;
			default :
				this.lload(resolvedPosition);
		}
		return;
	}
	// Using dedicated double bytecode
	if (typeBinding == DoubleBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.dload_0();
				break;
			case 1 :
				this.dload_1();
				break;
			case 2 :
				this.dload_2();
				break;
			case 3 :
				this.dload_3();
				break;
			default :
				this.dload(resolvedPosition);
		}
		return;
	}
	// boolean, byte, char and short are handled as int
	if ((typeBinding == ByteBinding) || (typeBinding == CharBinding) || (typeBinding == BooleanBinding) || (typeBinding == ShortBinding)) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}

	// Reference object
	switch (resolvedPosition) {
		case 0 :
			this.aload_0();
			break;
		case 1 :
			this.aload_1();
			break;
		case 2 :
			this.aload_2();
			break;
		case 3 :
			this.aload_3();
			break;
		default :
			this.aload(resolvedPosition);
	}
}
public final void loadInt(int resolvedPosition) {
	// Using dedicated int bytecode
	switch (resolvedPosition) {
		case 0 :
			this.iload_0();
			break;
		case 1 :
			this.iload_1();
			break;
		case 2 :
			this.iload_2();
			break;
		case 3 :
			this.iload_3();
			break;
		default :
			this.iload(resolvedPosition);
	}
}
public final void loadObject(int resolvedPosition) {
	switch (resolvedPosition) {
		case 0 :
			this.aload_0();
			break;
		case 1 :
			this.aload_1();
			break;
		case 2 :
			this.aload_2();
			break;
		case 3 :
			this.aload_3();
			break;
		default :
			this.aload(resolvedPosition);
	}
}
final public void lookupswitch(CaseLabel defaultLabel, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	countLabels = 0;
	stackDepth--;
	int length = keys.length;
	int pos = position;
	defaultLabel.placeInstruction();
	for (int i = 0; i < length; i++) {
		casesLabel[i].placeInstruction();
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lookupswitch;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lookupswitch);
	}
	for (int i = (3 - (pos % 4)); i > 0; i--) {
		position++; // Padding
		classFileOffset++;
	}
	defaultLabel.branch();
	writeSignedWord(length);
	for (int i = 0; i < length; i++) {
		writeSignedWord(keys[sortedIndexes[i]]);
		casesLabel[sortedIndexes[i]].branch();
	}
}
final public void lor() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lor;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lor);
	}
}
final public void lrem() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lrem;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lrem);
	}
}
final public void lreturn() {
	countLabels = 0;
	stackDepth -= 2;
	// the stackDepth should be equal to 0 
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lreturn;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lreturn);
	}
}
final public void lshl() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lshl;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lshl);
	}
}
final public void lshr() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lshr;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lshr);
	}
}
final public void lstore(int iArg) {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (iArg > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_lstore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_lstore);
		}
		writeUnsignedShort(iArg);
	} else {
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_lstore;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_lstore);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) iArg;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) iArg);
		}
	}
}
final public void lstore_0() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lstore_0;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lstore_0);
	}
}
final public void lstore_1() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lstore_1;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lstore_1);
	}
}
final public void lstore_2() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lstore_2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lstore_2);
	}
}
final public void lstore_3() {
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lstore_3;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lstore_3);
	}
}
final public void lsub() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lsub;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lsub);
	}
}
final public void lushr() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lushr;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lushr);
	}
}
final public void lxor() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_lxor;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_lxor);
	}
}
final public void monitorenter() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_monitorenter;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_monitorenter);
	}
}
final public void monitorexit() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_monitorexit;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_monitorexit);
	}
}
final public void multianewarray(TypeBinding typeBinding, int dimensions) {
	countLabels = 0;
	stackDepth += (1 - dimensions);
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_multianewarray;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_multianewarray);
	}
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
	writeUnsignedByte(dimensions);
}
public static void needImplementation() {
}
/**
 * We didn't call it new, because there is a conflit with the new keyword
 */
final public void new_(TypeBinding typeBinding) {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_new;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_new);
	}
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
final public void newarray(int array_Type) {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_newarray;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_newarray);
	}
	writeUnsignedByte(array_Type);
}
public void newArray(Scope scope, ArrayBinding arrayBinding) {
	TypeBinding component = arrayBinding.elementsType(scope);
	switch (component.id) {
		case T_int :
			this.newarray(10);
			break;
		case T_byte :
			this.newarray(8);
			break;
		case T_boolean :
			this.newarray(4);
			break;
		case T_short :
			this.newarray(9);
			break;
		case T_char :
			this.newarray(5);
			break;
		case T_long :
			this.newarray(11);
			break;
		case T_float :
			this.newarray(6);
			break;
		case T_double :
			this.newarray(7);
			break;
		default :
			this.anewarray(component);
	}
}
public void newJavaLangError() {
	// new: java.lang.Error
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_new;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_new);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangError());
}

public void newJavaLangAssertionError() {
	// new: java.lang.AssertionError
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_new;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_new);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangAssertionError());
}

public void newNoClassDefFoundError() { // new: java.lang.NoClassDefFoundError
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_new;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_new);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangNoClassDefFoundError());
}
public void newStringBuffer() { // new: java.lang.StringBuffer
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_new;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_new);
	}
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuffer());
}
public void newWrapperFor(int typeID) {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_new;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_new);
	}
	switch (typeID) {
		case T_int : // new: java.lang.Integer
			writeUnsignedShort(constantPool.literalIndexForJavaLangInteger());
			break;
		case T_boolean : // new: java.lang.Boolean
			writeUnsignedShort(constantPool.literalIndexForJavaLangBoolean());
			break;
		case T_byte : // new: java.lang.Byte
			writeUnsignedShort(constantPool.literalIndexForJavaLangByte());
			break;
		case T_char : // new: java.lang.Character
			writeUnsignedShort(constantPool.literalIndexForJavaLangCharacter());
			break;
		case T_float : // new: java.lang.Float
			writeUnsignedShort(constantPool.literalIndexForJavaLangFloat());
			break;
		case T_double : // new: java.lang.Double
			writeUnsignedShort(constantPool.literalIndexForJavaLangDouble());
			break;
		case T_short : // new: java.lang.Short
			writeUnsignedShort(constantPool.literalIndexForJavaLangShort());
			break;
		case T_long : // new: java.lang.Long
			writeUnsignedShort(constantPool.literalIndexForJavaLangLong());
			break;
		case T_void : // new: java.lang.Void
			writeUnsignedShort(constantPool.literalIndexForJavaLangVoid());
	}
}
final public void nop() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_nop;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_nop);
	}
}
final public void pop() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_pop;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_pop);
	}
}
final public void pop2() {
	countLabels = 0;
	stackDepth -= 2;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_pop2;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_pop2);
	}
}
final public void putfield(FieldBinding fieldBinding) {
	countLabels = 0;
	int id;
	if (((id = fieldBinding.type.id) == T_double) || (id == T_long))
		stackDepth -= 3;
	else
		stackDepth -= 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_putfield;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_putfield);
	}
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
final public void putstatic(FieldBinding fieldBinding) {
	countLabels = 0;
	int id;
	if (((id = fieldBinding.type.id) == T_double) || (id == T_long))
		stackDepth -= 2;
	else
		stackDepth -= 1;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_putstatic;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_putstatic);
	}
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
public void record(LocalVariableBinding local) {
	if (!generateLocalVariableTableAttributes)
		return;
	if (allLocalsCounter == locals.length) {
		// resize the collection
		System.arraycopy(locals, 0, (locals = new LocalVariableBinding[allLocalsCounter + LOCALS_INCREMENT]), 0, allLocalsCounter);
	}
	locals[allLocalsCounter++] = local;
	local.initializationPCs = new int[4];
	local.initializationCount = 0;
}
public void recordPositionsFrom(int startPC, int sourcePos) {

	/* Record positions in the table, only if nothing has 
	 * already been recorded. Since we output them on the way 
	 * up (children first for more specific info)
	 * The pcToSourceMap table is always sorted.
	 */

	if (!generateLineNumberAttributes)
		return;
	if (sourcePos == 0)
		return;

	// no code generated for this node. e.g. field without any initialization
	if (position == startPC)
		return;

	// Widening an existing entry that already has the same source positions
	if (pcToSourceMapSize + 4 > pcToSourceMap.length) {
		// resize the array pcToSourceMap
		System.arraycopy(pcToSourceMap, 0, (pcToSourceMap = new int[pcToSourceMapSize << 1]), 0, pcToSourceMapSize);
	}
	int newLine = ClassFile.searchLineNumber(lineSeparatorPositions, sourcePos);
	// lastEntryPC represents the endPC of the lastEntry.
	if (pcToSourceMapSize > 0) {
		// in this case there is already an entry in the table
		if (pcToSourceMap[pcToSourceMapSize - 1] != newLine) {
			if (startPC < lastEntryPC) {
				// we forgot to add an entry.
				// search if an existing entry exists for startPC
				int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
				if (insertionIndex != -1) {
					// there is no existing entry starting with startPC.
					int existingEntryIndex = indexOfSameLineEntrySincePC(startPC, newLine); // index for PC
					/* the existingEntryIndex corresponds to en entry with the same line and a PC >= startPC.
						in this case it is relevant to widen this entry instead of creating a new one.
						line1: this(a,
						  b,
						  c);
						with this code we generate each argument. We generate a aload0 to invoke the constructor. There is no entry for this
						aload0 bytecode. The first entry is the one for the argument a.
						But we want the constructor call to start at the aload0 pc and not just at the pc of the first argument.
						So we widen the existing entry (if there is one) or we create a new entry with the startPC.
					*/
					if (existingEntryIndex != -1) {
						// widen existing entry
						pcToSourceMap[existingEntryIndex] = startPC;
					} else {
						// we have to add an entry that won't be sorted. So we sort the pcToSourceMap.
						System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - insertionIndex);
						pcToSourceMap[insertionIndex++] = startPC;
						pcToSourceMap[insertionIndex] = newLine;
						pcToSourceMapSize += 2;
					}
				}
				if (position != lastEntryPC) { // no bytecode since last entry pc
					pcToSourceMap[pcToSourceMapSize++] = lastEntryPC;
					pcToSourceMap[pcToSourceMapSize++] = newLine;
				}
			} else {
				// we can safely add the new entry. The endPC of the previous entry is not in conflit with the startPC of the new entry.
				pcToSourceMap[pcToSourceMapSize++] = startPC;
				pcToSourceMap[pcToSourceMapSize++] = newLine;
			}
		} else {
			/* the last recorded entry is on the same line. But it could be relevant to widen this entry.
			   we want to extend this entry forward in case we generated some bytecode before the last entry that are not related to any statement
			*/	
			if (startPC < pcToSourceMap[pcToSourceMapSize - 2]) {
				int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
				if (insertionIndex != -1) {
					// widen the existing entry
					// we have to figure out if we need to move the last entry at another location to keep a sorted table
					if ((pcToSourceMapSize > 4) && (pcToSourceMap[pcToSourceMapSize - 4] > startPC)) {
						System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - 2 - insertionIndex);
						pcToSourceMap[insertionIndex++] = startPC;
						pcToSourceMap[insertionIndex] = newLine;						
					} else {
						pcToSourceMap[pcToSourceMapSize - 2] = startPC;
					}
				}
			}
		}
		lastEntryPC = position;
	} else {
		// record the first entry
		pcToSourceMap[pcToSourceMapSize++] = startPC;
		pcToSourceMap[pcToSourceMapSize++] = newLine;
		lastEntryPC = position;
	}
}
/**
 * @param anExceptionLabel org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel
 */
public void registerExceptionHandler(ExceptionLabel anExceptionLabel) {
	int length;
	if (exceptionHandlersNumber >= (length = exceptionHandlers.length)) {
		// resize the exception handlers table
		System.arraycopy(exceptionHandlers, 0, exceptionHandlers = new ExceptionLabel[length + LABELS_INCREMENT], 0, length);
	}
	// no need to resize. So just add the new exception label
	exceptionHandlers[exceptionHandlersNumber++] = anExceptionLabel;
}
public final void removeNotDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// given some flow info, make sure we did not loose some variables initialization
	// if this happens, then we must update their pc entries to reflect it in debug attributes
	if (!generateLocalVariableTableAttributes)
		return;
/*	if (initStateIndex == lastInitStateIndexWhenRemovingInits)
		return;
		
	lastInitStateIndexWhenRemovingInits = initStateIndex;
	if (lastInitStateIndexWhenAddingInits != initStateIndex){
		lastInitStateIndexWhenAddingInits = -2;// reinitialize add index 
		// add(1)-remove(1)-add(1) -> ignore second add
		// add(1)-remove(2)-add(1) -> perform second add
	}*/
	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null) {
			if (initStateIndex == -1 || !isDefinitelyAssigned(scope, initStateIndex, localBinding)) {
				if (localBinding.initializationCount > 0) {
					localBinding.recordInitializationEndPC(position);
				}
			}
		}
	}
}
/**
 * @param methodDeclaration org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
 */
public void reset(AbstractMethodDeclaration methodDeclaration, ClassFile classFile) {
	init(classFile);
	this.methodDeclaration = methodDeclaration;
	preserveUnusedLocals = methodDeclaration.scope.problemReporter().options.preserveAllLocalVariables;
	initializeMaxLocals(methodDeclaration.binding);
}
/**
 * @param methodDeclaration org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
 */
public void resetForProblemClinit(ClassFile classFile) {
	init(classFile);
	maxLocals = 0;
}
protected final void resizeByteArray() {
	int actualLength = bCodeStream.length;
	int requiredSize = actualLength + growFactor;
	if (classFileOffset > requiredSize) {
		requiredSize = classFileOffset + growFactor;
	}
	System.arraycopy(bCodeStream, 0, (bCodeStream = new byte[requiredSize]), 0, actualLength);
}
/**
 * This method is used to resize the internal byte array in 
 * case of a ArrayOutOfBoundsException when adding the value b.
 * Resize and add the new byte b inside the array.
 * @param b byte
 */
protected final void resizeByteArray(byte b) {
	resizeByteArray();
	bCodeStream[classFileOffset - 1] = b;
}
final public void ret(int index) {
	countLabels = 0;
	if (index > 255) { // Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_wide;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_wide);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ret;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ret);
		}
		writeUnsignedShort(index);
	} else { // Don't Widen
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_ret;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_ret);
		}
		try {
			position++;
			bCodeStream[classFileOffset++] = (byte) index;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray((byte) index);
		}
	}
}
final public void return_() {
	countLabels = 0;
	// the stackDepth should be equal to 0 
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_return;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_return);
	}
}
final public void saload() {
	countLabels = 0;
	stackDepth--;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_saload;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_saload);
	}
}
final public void sastore() {
	countLabels = 0;
	stackDepth -= 3;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_sastore;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_sastore);
	}
}
/**
 * @param operatorConstant int
 * @param type_ID int
 */
public void sendOperator(int operatorConstant, int type_ID) {
	switch (type_ID) {
		case T_int :
		case T_boolean :
		case T_char :
		case T_byte :
		case T_short :
			switch (operatorConstant) {
				case PLUS :
					this.iadd();
					break;
				case MINUS :
					this.isub();
					break;
				case MULTIPLY :
					this.imul();
					break;
				case DIVIDE :
					this.idiv();
					break;
				case REMAINDER :
					this.irem();
					break;
				case LEFT_SHIFT :
					this.ishl();
					break;
				case RIGHT_SHIFT :
					this.ishr();
					break;
				case UNSIGNED_RIGHT_SHIFT :
					this.iushr();
					break;
				case AND :
					this.iand();
					break;
				case OR :
					this.ior();
					break;
				case XOR :
					this.ixor();
					break;
			}
			break;
		case T_long :
			switch (operatorConstant) {
				case PLUS :
					this.ladd();
					break;
				case MINUS :
					this.lsub();
					break;
				case MULTIPLY :
					this.lmul();
					break;
				case DIVIDE :
					this.ldiv();
					break;
				case REMAINDER :
					this.lrem();
					break;
				case LEFT_SHIFT :
					this.lshl();
					break;
				case RIGHT_SHIFT :
					this.lshr();
					break;
				case UNSIGNED_RIGHT_SHIFT :
					this.lushr();
					break;
				case AND :
					this.land();
					break;
				case OR :
					this.lor();
					break;
				case XOR :
					this.lxor();
					break;
			}
			break;
		case T_float :
			switch (operatorConstant) {
				case PLUS :
					this.fadd();
					break;
				case MINUS :
					this.fsub();
					break;
				case MULTIPLY :
					this.fmul();
					break;
				case DIVIDE :
					this.fdiv();
					break;
				case REMAINDER :
					this.frem();
			}
			break;
		case T_double :
			switch (operatorConstant) {
				case PLUS :
					this.dadd();
					break;
				case MINUS :
					this.dsub();
					break;
				case MULTIPLY :
					this.dmul();
					break;
				case DIVIDE :
					this.ddiv();
					break;
				case REMAINDER :
					this.drem();
			}
	}
}
final public void sipush(int s) {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_sipush;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_sipush);
	}
	writeSignedShort(s);
}
public static final void sort(int[] tab, int lo0, int hi0, int[] result) {
	int lo = lo0;
	int hi = hi0;
	int mid;
	if (hi0 > lo0) {
		/* Arbitrarily establishing partition element as the midpoint of
		  * the array.
		  */
		mid = tab[ (lo0 + hi0) / 2];
		// loop through the array until indices cross
		while (lo <= hi) {
			/* find the first element that is greater than or equal to 
			 * the partition element starting from the left Index.
			 */
			while ((lo < hi0) && (tab[lo] < mid))
				++lo;
			/* find an element that is smaller than or equal to 
			 * the partition element starting from the right Index.
			 */
			while ((hi > lo0) && (tab[hi] > mid))
				--hi;
			// if the indexes have not crossed, swap
			if (lo <= hi) {
				swap(tab, lo, hi, result);
				++lo;
				--hi;
			}
		}
		/* If the right index has not reached the left side of array
		  * must now sort the left partition.
		  */
		if (lo0 < hi)
			sort(tab, lo0, hi, result);
		/* If the left index has not reached the right side of array
		  * must now sort the right partition.
		  */
		if (lo < hi0)
			sort(tab, lo, hi0, result);
	}
}
public final void store(LocalVariableBinding localBinding, boolean valueRequired) {
	TypeBinding type = localBinding.type;
	int position = localBinding.resolvedPosition;
	// Using dedicated int bytecode
	if ((type == IntBinding) || (type == CharBinding) || (type == ByteBinding) || (type == ShortBinding) || (type == BooleanBinding)) {
		if (valueRequired)
			this.dup();
		switch (position) {
			case 0 :
				this.istore_0();
				break;
			case 1 :
				this.istore_1();
				break;
			case 2 :
				this.istore_2();
				break;
			case 3 :
				this.istore_3();
				break;
			default :
				this.istore(position);
		}
		return;
	}
	// Using dedicated float bytecode
	if (type == FloatBinding) {
		if (valueRequired)
			this.dup();
		switch (position) {
			case 0 :
				this.fstore_0();
				break;
			case 1 :
				this.fstore_1();
				break;
			case 2 :
				this.fstore_2();
				break;
			case 3 :
				this.fstore_3();
				break;
			default :
				this.fstore(position);
		}
		return;
	}
	// Using dedicated long bytecode
	if (type == LongBinding) {
		if (valueRequired)
			this.dup2();
		switch (position) {
			case 0 :
				this.lstore_0();
				break;
			case 1 :
				this.lstore_1();
				break;
			case 2 :
				this.lstore_2();
				break;
			case 3 :
				this.lstore_3();
				break;
			default :
				this.lstore(position);
		}
		return;
	}
	// Using dedicated double bytecode
	if (type == DoubleBinding) {
		if (valueRequired)
			this.dup2();
		switch (position) {
			case 0 :
				this.dstore_0();
				break;
			case 1 :
				this.dstore_1();
				break;
			case 2 :
				this.dstore_2();
				break;
			case 3 :
				this.dstore_3();
				break;
			default :
				this.dstore(position);
		}
		return;
	}
	// Reference object
	if (valueRequired)
		this.dup();
	switch (position) {
		case 0 :
			this.astore_0();
			break;
		case 1 :
			this.astore_1();
			break;
		case 2 :
			this.astore_2();
			break;
		case 3 :
			this.astore_3();
			break;
		default :
			this.astore(position);
	}
}
public final void store(TypeBinding type, int position) {
	// Using dedicated int bytecode
	if ((type == IntBinding) || (type == CharBinding) || (type == ByteBinding) || (type == ShortBinding) || (type == BooleanBinding)) {
		switch (position) {
			case 0 :
				this.istore_0();
				break;
			case 1 :
				this.istore_1();
				break;
			case 2 :
				this.istore_2();
				break;
			case 3 :
				this.istore_3();
				break;
			default :
				this.istore(position);
		}
		return;
	}
	// Using dedicated float bytecode
	if (type == FloatBinding) {
		switch (position) {
			case 0 :
				this.fstore_0();
				break;
			case 1 :
				this.fstore_1();
				break;
			case 2 :
				this.fstore_2();
				break;
			case 3 :
				this.fstore_3();
				break;
			default :
				this.fstore(position);
		}
		return;
	}
	// Using dedicated long bytecode
	if (type == LongBinding) {
		switch (position) {
			case 0 :
				this.lstore_0();
				break;
			case 1 :
				this.lstore_1();
				break;
			case 2 :
				this.lstore_2();
				break;
			case 3 :
				this.lstore_3();
				break;
			default :
				this.lstore(position);
		}
		return;
	}
	// Using dedicated double bytecode
	if (type == DoubleBinding) {
		switch (position) {
			case 0 :
				this.dstore_0();
				break;
			case 1 :
				this.dstore_1();
				break;
			case 2 :
				this.dstore_2();
				break;
			case 3 :
				this.dstore_3();
				break;
			default :
				this.dstore(position);
		}
		return;
	}
	// Reference object
	switch (position) {
		case 0 :
			this.astore_0();
			break;
		case 1 :
			this.astore_1();
			break;
		case 2 :
			this.astore_2();
			break;
		case 3 :
			this.astore_3();
			break;
		default :
			this.astore(position);
	}
}
public final void storeInt(int position) {
	switch (position) {
		case 0 :
			this.istore_0();
			break;
		case 1 :
			this.istore_1();
			break;
		case 2 :
			this.istore_2();
			break;
		case 3 :
			this.istore_3();
			break;
		default :
			this.istore(position);
	}
}
public final void storeObject(int position) {
	switch (position) {
		case 0 :
			this.astore_0();
			break;
		case 1 :
			this.astore_1();
			break;
		case 2 :
			this.astore_2();
			break;
		case 3 :
			this.astore_3();
			break;
		default :
			this.astore(position);
	}
}
final public void swap() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_swap;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_swap);
	}
}
private static final void swap(int a[], int i, int j, int result[]) {
	int T;
	T = a[i];
	a[i] = a[j];
	a[j] = T;
	T = result[j];
	result[j] = result[i];
	result[i] = T;
}
final public void tableswitch(CaseLabel defaultLabel, int low, int high, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	countLabels = 0;
	stackDepth--;
	int length = casesLabel.length;
	int pos = position;
	defaultLabel.placeInstruction();
	for (int i = 0; i < length; i++)
		casesLabel[i].placeInstruction();
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_tableswitch;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_tableswitch);
	}
	for (int i = (3 - (pos % 4)); i > 0; i--) {
		position++; // Padding
		classFileOffset++;
	}
	defaultLabel.branch();
	writeSignedWord(low);
	writeSignedWord(high);
	int i = low, j = low;
	// the index j is used to know if the index i is one of the missing entries in case of an 
	// optimized tableswitch
	while (true) {
		int index;
		int key = keys[index = sortedIndexes[j - low]];
		if (key == i) {
			casesLabel[index].branch();
			j++;
			if (i == high) break; // if high is maxint, then avoids wrapping to minint.
		} else {
			defaultLabel.branch();
		}
		i++;
	}
}
public String toString() {
	StringBuffer buffer = new StringBuffer("( position:"); //$NON-NLS-1$
	buffer.append(position);
	buffer.append(",\nstackDepth:"); //$NON-NLS-1$
	buffer.append(stackDepth);
	buffer.append(",\nmaxStack:"); //$NON-NLS-1$
	buffer.append(stackMax);
	buffer.append(",\nmaxLocals:"); //$NON-NLS-1$
	buffer.append(maxLocals);
	buffer.append(")"); //$NON-NLS-1$
	return buffer.toString();
}
public void updateLastRecordedEndPC(int pos) {

	/* Tune positions in the table, this is due to some 
	 * extra bytecodes being
	 * added to some user code (jumps). */
	/** OLD CODE
		if (!generateLineNumberAttributes)
			return;
		pcToSourceMap[pcToSourceMapSize - 1][1] = position;
		// need to update the initialization endPC in case of generation of local variable attributes.
		updateLocalVariablesAttribute(pos);	
	*/	

	if (!generateLineNumberAttributes)
		return;
	// need to update the initialization endPC in case of generation of local variable attributes.
	updateLocalVariablesAttribute(pos);
}
public void updateLocalVariablesAttribute(int pos) {
	// need to update the initialization endPC in case of generation of local variable attributes.
	if (generateLocalVariableTableAttributes) {
		for (int i = 0, max = locals.length; i < max; i++) {
			LocalVariableBinding local = locals[i];
			if ((local != null) && (local.initializationCount > 0)) {
				if (local.initializationPCs[((local.initializationCount - 1) << 1) + 1] == pos) {
					local.initializationPCs[((local.initializationCount - 1) << 1) + 1] = position;
				}
			}
		}
	}
}
final public void wide() {
	countLabels = 0;
	try {
		position++;
		bCodeStream[classFileOffset++] = OPC_wide;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(OPC_wide);
	}
}
public final void writeByte(byte b) {
	try {
		position++;
		bCodeStream[classFileOffset++] = b;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray(b);
	}
}
public final void writeByteAtPos(int pos, byte b) {
	try {
		bCodeStream[pos] = b;
	} catch (IndexOutOfBoundsException ex) {
		resizeByteArray();
		bCodeStream[pos] = b;
	}
}
/**
 * Write a unsigned 8 bits value into the byte array
 * @param b the signed byte
 */
public final void writeSignedByte(int b) {
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) b;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) b);
	}
}
/**
 * Write a signed 16 bits value into the byte array
 * @param b the signed short
 */
public final void writeSignedShort(int b) {
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) (b >> 8);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) (b >> 8));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) b;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) b);
	}
}
public final void writeSignedShort(int pos, int b) {
	int currentOffset = startingClassFileOffset + pos;
	try {
		bCodeStream[currentOffset] = (byte) (b >> 8);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray();
		bCodeStream[currentOffset] = (byte) (b >> 8);
	}
	try {
		bCodeStream[currentOffset + 1] = (byte) b;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray();
		bCodeStream[currentOffset + 1] = (byte) b;
	}
}
public final void writeSignedWord(int value) {
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) ((value & 0xFF000000) >> 24);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) ((value & 0xFF000000) >> 24));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) ((value & 0xFF0000) >> 16);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) ((value & 0xFF0000) >> 16));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) ((value & 0xFF00) >> 8);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) ((value & 0xFF00) >> 8));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) (value & 0xFF);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) (value & 0xFF));
	}
}
public final void writeSignedWord(int pos, int value) {
	int currentOffset = startingClassFileOffset + pos;
	try {
		bCodeStream[currentOffset++] = (byte) ((value & 0xFF000000) >> 24);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray();
		bCodeStream[currentOffset-1] = (byte) ((value & 0xFF000000) >> 24);
	}
	try {
		bCodeStream[currentOffset++] = (byte) ((value & 0xFF0000) >> 16);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray();
		bCodeStream[currentOffset-1] = (byte) ((value & 0xFF0000) >> 16);
	}
	try {
		bCodeStream[currentOffset++] = (byte) ((value & 0xFF00) >> 8);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray();
		bCodeStream[currentOffset-1] = (byte) ((value & 0xFF00) >> 8);
	}
	try {
		bCodeStream[currentOffset++] = (byte) (value & 0xFF);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray();
		bCodeStream[currentOffset-1] = (byte) (value & 0xFF);
	}
}
/**
 * Write a unsigned 8 bits value into the byte array
 * @param b the unsigned byte
 */
public final void writeUnsignedByte(int b) {
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) b;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) b);
	}
}
/**
 * Write a unsigned 16 bits value into the byte array
 * @param b the unsigned short
 */
public final void writeUnsignedShort(int b) {
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) (b >>> 8);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) (b >>> 8));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) b;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) b);
	}
}
/**
 * Write a unsigned 32 bits value into the byte array
 * @param value the unsigned word
 */
public final void writeUnsignedWord(int value) {
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) (value >>> 24);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) (value >>> 24));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) (value >>> 16);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) (value >>> 16));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) (value >>> 8);
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) (value >>> 8));
	}
	try {
		position++;
		bCodeStream[classFileOffset++] = (byte) value;
	} catch (IndexOutOfBoundsException e) {
		resizeByteArray((byte) value);
	}
}

public void generateWideConditionalBranch(byte opcode, Label lbl) {
		/* we handle the goto_w problem inside an if.... with some macro expansion
		 * at the bytecode level
		 * instead of:
		 * if_...... lbl
		 * we have:
		 *    ifne <l1>
		 *    goto <l2>
		 * l1 gotow <l3> // l3 is a wide target
		 * l2 ....
		 */
		Label l1 = new Label(this);
		try {
			position++;
			bCodeStream[classFileOffset++] = opcode;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(opcode);
		}
		l1.branch();
		Label l2 = new Label(this);
		this.internal_goto_(l2);
		l1.place();
		this.goto_w(lbl);
		l2.place();
}
}
