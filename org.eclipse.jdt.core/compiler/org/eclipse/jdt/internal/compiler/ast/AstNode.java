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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;

public abstract class AstNode implements BaseTypes, CompilerModifiers, TypeConstants, TypeIds {
	
	public int sourceStart, sourceEnd;

	//some global provision for the hierarchy
	public final static Constant NotAConstant = Constant.NotAConstant;

	// storage for internal flags (32 bits)
	public int bits = IsReachableMASK; // reachable by default

	// for operators only
	// Reach . . . . . . . . . . . . . . . . . O O O O O O V VrR R R R
	public static final int ReturnTypeIDMASK = 15; // 4 lower bits for operators
	public static final int ValueForReturnMASK = 16; // for binary expressions
	public static final int OnlyValueRequiredMASK = 32; // for binary expressions
	public static final int OperatorSHIFT = 6;
	public static final int OperatorMASK = 63 << OperatorSHIFT;

	// for name references only
	// Reach . . . . . . . . . . . . . . . . D D D D D D D D VrF R R R
	public static final int RestrictiveFlagMASK = 7;
	// 3 lower bits for name references
	public static final int FirstAssignmentToLocalMASK = 8;
	// for single name references
	public static final int DepthSHIFT = 5;
	public static final int DepthMASK = 0xFF << DepthSHIFT;
	// 8 bits for actual depth value (max. 255)

	// for statements only
	public static final int IsReachableMASK = 0x80000000; // highest bit
	public static final int IsLocalDeclarationReachableMASK = 0x40000000; // below highest bit

	// for type declaration only
	public static final int AddAssertionMASK = 1; // lowest bit

	// for type, method and field declarations only
	public static final int HasLocalTypeMASK = 2;
	// cannot conflict with AddAssertionMASK

	/*
	public final static int BitMask1= 0x1; // decimal 1
	public final static int BitMask2= 0x2; // decimal 2
	public final static int BitMask3= 0x4; // decimal 4
	public final static int BitMask4= 0x8; // decimal 8
	public final static int BitMask5= 0x10; // decimal 16
	public final static int BitMask6= 0x20; // decimal 32
	public final static int BitMask7= 0x40; // decimal 64
	public final static int BitMask8= 0x80; // decimal 128
	public final static int BitMask9= 0x100; // decimal 256
	public final static int BitMask10= 0x200; // decimal 512
	public final static int BitMask11= 0x400; // decimal 1024
	public final static int BitMask12= 0x800; // decimal 2048
	public final static int BitMask13= 0x1000; // decimal 4096
	public final static int BitMask14= 0x2000; // decimal 8192
	public final static int BitMask15= 0x4000; // decimal 16384
	public final static int BitMask16= 0x8000; // decimal 32768
	public final static int BitMask17= 0x10000; // decimal 65536
	public final static int BitMask18= 0x20000; // decimal 131072
	public final static int BitMask19= 0x40000; // decimal 262144
	public final static int BitMask20= 0x80000; // decimal 524288
	public final static int BitMask21= 0x100000; // decimal 1048576
	public final static int BitMask22= 0x200000; // decimal 2097152
	public final static int BitMask23= 0x400000; // decimal 4194304
	public final static int BitMask24= 0x800000; // decimal 8388608
	public final static int BitMask25= 0x1000000; // decimal 16777216
	public final static int BitMask26= 0x2000000; // decimal 33554432
	public final static int BitMask27= 0x4000000; // decimal 67108864
	public final static int BitMask28= 0x8000000; // decimal 134217728
	public final static int BitMask29= 0x10000000; // decimal 268435456
	public final static int BitMask30= 0x20000000; // decimal 536870912
	public final static int BitMask31= 0x40000000; // decimal 1073741824
	public final static int BitMask32= 0x80000000; // decimal 2147483648	
	*/

	/**
	 * AstNode constructor comment.
	 */
	public AstNode() {

		super();
	}

	public boolean cannotReturn() {
		return false;
	}

	public AstNode concreteStatement() {
		return this;
	}

	/* Answer true if the field use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
	public final boolean isFieldUseDeprecated(FieldBinding field, Scope scope) {

		return field.isViewedAsDeprecated()
			&& !scope.isDefinedInSameUnit(field.declaringClass);
	}

	/* Answer true if the method use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
	public final boolean isMethodUseDeprecated(MethodBinding method, Scope scope) {
		return method.isViewedAsDeprecated()
			&& !scope.isDefinedInSameUnit(method.declaringClass);
	}

	public boolean isSuper() {

		return false;
	}

	public boolean isThis() {

		return false;
	}

	/* Answer true if the type use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
	public final boolean isTypeUseDeprecated(TypeBinding type, Scope scope) {

		if (type.isArrayType())
			type = ((ArrayBinding) type).leafComponentType;
		if (type.isBaseType())
			return false;

		ReferenceBinding refType = (ReferenceBinding) type;
		return refType.isViewedAsDeprecated() && !scope.isDefinedInSameUnit(refType);
	}

	public static String modifiersString(int modifiers) {

		String s = ""; //$NON-NLS-1$
		if ((modifiers & AccPublic) != 0)
			s = s + "public "; //$NON-NLS-1$
		if ((modifiers & AccPrivate) != 0)
			s = s + "private "; //$NON-NLS-1$
		if ((modifiers & AccProtected) != 0)
			s = s + "protected "; //$NON-NLS-1$
		if ((modifiers & AccStatic) != 0)
			s = s + "static "; //$NON-NLS-1$
		if ((modifiers & AccFinal) != 0)
			s = s + "final "; //$NON-NLS-1$
		if ((modifiers & AccSynchronized) != 0)
			s = s + "synchronized "; //$NON-NLS-1$
		if ((modifiers & AccVolatile) != 0)
			s = s + "volatile "; //$NON-NLS-1$
		if ((modifiers & AccTransient) != 0)
			s = s + "transient "; //$NON-NLS-1$
		if ((modifiers & AccNative) != 0)
			s = s + "native "; //$NON-NLS-1$
		if ((modifiers & AccAbstract) != 0)
			s = s + "abstract "; //$NON-NLS-1$
		return s;
	}

	/** 
	 * @deprecated - use field instead
	*/
	public int sourceEnd() {
		return sourceEnd;
	}
	
	/** 
	 * @deprecated - use field instead
	*/
	public int sourceStart() {
		return sourceStart;
	}

	public static String tabString(int tab) {

		String s = ""; //$NON-NLS-1$
		for (int i = tab; i > 0; i--)
			s = s + "  "; //$NON-NLS-1$
		return s;
	}

	public String toString() {

		return toString(0);
	}

	public String toString(int tab) {

		return "****" + super.toString() + "****";  //$NON-NLS-2$ //$NON-NLS-1$
	}
	
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	}
}