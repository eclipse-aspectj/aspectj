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

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class LongLiteral extends NumberLiteral {
	long value;
	
	static final Constant FORMAT_ERROR = new DoubleConstant(1.0/0.0); // NaN;	
		
public LongLiteral(char[] token, int s,int e) {
	super(token, s,e);
}
public LongLiteral(char[] token, int s,int e, long value) {
	this(token, s,e);
	this.value = value;
}
public void computeConstant() {
	//the overflow (when radix=10) is tested using the fact that
	//the value should always grow during its computation

	int length = source.length - 1; //minus one because the last char is 'l' or 'L'
	
	long computedValue ;
	if (source[0] == '0')
	{	if (length == 1) { 	constant = Constant.fromValue(0L);	return;	}
		final int shift,radix;
		int j ;
		if ( (source[1] == 'x') | (source[1] == 'X') )
		{	shift = 4 ; j = 2; radix = 16;}
		else
		{	shift = 3 ; j = 1; radix = 8;}
		int nbDigit = 0;
		while (source[j]=='0') 
		{	j++; //jump over redondant zero
			if ( j == length)
			{	//watch for 0000000000000L
				constant = Constant.fromValue(value = 0L);
				return ;}}
				
		int digitValue ;
		if ((digitValue = Character.digit(source[j++],radix))	< 0 ) 	
		{	constant = FORMAT_ERROR; return ;}
		if (digitValue >= 8) nbDigit = 4;
		else 	if (digitValue >= 4) nbDigit = 3;
				else 	if (digitValue >= 2) nbDigit = 2;
						else nbDigit = 1; //digitValue is not 0
		computedValue = digitValue ;
		while (j<length)
		{	if ((digitValue = Character.digit(source[j++],radix))	< 0 ) 	
			{	constant = FORMAT_ERROR; return ;}
			if ((nbDigit += shift) > 64) return /*constant stays null*/ ;
			computedValue = (computedValue<<shift) | digitValue ;}}

	else
	{	//-----------case radix=10-----------------
		long previous = computedValue = 0;
		for (int i = 0 ; i < length; i++) 
		{	int digitValue ;	
			if ((digitValue = Character.digit(source[i], 10)) < 0 ) return /*constant stays null*/ ;
			previous = computedValue;
			computedValue = 10 * computedValue + digitValue ;
			if (previous > computedValue) return /*constant stays null*/;}}
	
	constant = Constant.fromValue(value = computedValue);
}
/**
 * Code generation for long literal
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired)
		if ((implicitConversion >> 4) == T_long)
			codeStream.generateInlinedValue(value);
		else
			codeStream.generateConstant(constant, implicitConversion);
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public TypeBinding literalType(BlockScope scope) {
	return LongBinding;
}
public final boolean mayRepresentMIN_VALUE(){
	//a special autorized int literral is 9223372036854775808L
	//which is ONE over the limit. This special case 
	//only is used in combinaison with - to denote
	//the minimal value of int -9223372036854775808L

	return ((source.length == 20) &&
			(source[0] == '9') &&
			(source[1] == '2') &&
			(source[2] == '2') &&
			(source[3] == '3') &&			
			(source[4] == '3') &&
			(source[5] == '7') &&
			(source[6] == '2') &&
			(source[7] == '0') &&			
			(source[8] == '3') &&
			(source[9] == '6') &&
			(source[10] == '8') &&
			(source[11] == '5') &&
			(source[12] == '4') &&			
			(source[13] == '7') &&
			(source[14] == '7') &&
			(source[15] == '5') &&
			(source[16] == '8') &&			
			(source[17] == '0') &&
			(source[18] == '8'));}
public TypeBinding resolveType(BlockScope scope) {
	// the format may be incorrect while the scanner could detect
	// such error only on painfull tests...easier and faster here

	TypeBinding tb = super.resolveType(scope);
	if (constant == FORMAT_ERROR) {
		constant = NotAConstant;
		scope.problemReporter().constantOutOfFormat(this);
		return null;
	}
	return tb;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
}
