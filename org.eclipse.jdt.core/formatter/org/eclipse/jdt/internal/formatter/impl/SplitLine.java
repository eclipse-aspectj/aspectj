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
package org.eclipse.jdt.internal.formatter.impl;

import org.eclipse.jdt.core.compiler.ITerminalSymbols;

/** Represents a split line: contains an operator and all substrings
*/
public class SplitLine implements ITerminalSymbols{
	public int[] operators; // the operator on which the string was split.
	public String[] substrings;
	public int[] startSubstringsIndexes;
/**
 * SplitLine constructor comment.
 */
public SplitLine(int[] operators, String[] substrings) {
	this(operators, substrings, null);
}
/**
 * SplitLine constructor comment.
 */
public SplitLine(int[] operators, String[] substrings, int[] startIndexes) {
	super();
	this.operators=operators;
	this.substrings=substrings;
	this.startSubstringsIndexes = startIndexes;
}
/**
 * Prints a nice representation of the receiver
 * @return java.lang.String
 */
public String toString() {
	StringBuffer result=new StringBuffer();
	String operatorString = new String();
		
	for (int i=0,max=substrings.length;i<max;i++){
		int currentOperator = operators[i];
		String currentString = substrings[i];
		boolean placeOperatorAhead = currentOperator != ITerminalSymbols.TokenNameCOMMA && currentOperator != ITerminalSymbols.TokenNameSEMICOLON;
		boolean placeOperatorBehind = currentOperator == ITerminalSymbols.TokenNameCOMMA || currentOperator == ITerminalSymbols.TokenNameSEMICOLON;
		


	switch (currentOperator){
		case TokenNameextends:
			operatorString="extends"; //$NON-NLS-1$
			break;
		case TokenNameimplements:
			operatorString="implements"; //$NON-NLS-1$
			break;
		case TokenNamethrows:
			operatorString="throws"; //$NON-NLS-1$
			break;
		case TokenNameSEMICOLON : // ;
			operatorString=";"; //$NON-NLS-1$
			break;
		case TokenNameCOMMA : // ,
			operatorString=","; //$NON-NLS-1$
			break;
		case TokenNameEQUAL : // =
			operatorString="="; //$NON-NLS-1$
			break;
		case TokenNameAND_AND : // && (15.22)
			operatorString="&&"; //$NON-NLS-1$
			break;
		case TokenNameOR_OR : // || (15.23)
			operatorString="||"; //$NON-NLS-1$
			break;
		case TokenNameQUESTION : // ? (15.24)
			operatorString="?"; //$NON-NLS-1$
			break;

		case TokenNameCOLON : // : (15.24)
			operatorString=":"; //$NON-NLS-1$
			break;
		case TokenNameEQUAL_EQUAL : // == (15.20, 15.20.1, 15.20.2, 15.20.3)
			operatorString="=="; //$NON-NLS-1$
			break;

		case TokenNameNOT_EQUAL : // != (15.20, 15.20.1, 15.20.2, 15.20.3)
			operatorString="!="; //$NON-NLS-1$
			break;

		case TokenNameLESS : // < (15.19.1)
			operatorString="<"; //$NON-NLS-1$
			break;

		case TokenNameLESS_EQUAL : // <= (15.19.1)
			operatorString="<="; //$NON-NLS-1$
			break;

		case TokenNameGREATER : // > (15.19.1)
			operatorString=">"; //$NON-NLS-1$
			break;

		case TokenNameGREATER_EQUAL : // >= (15.19.1)
			operatorString=">="; //$NON-NLS-1$
			break;

		case TokenNameinstanceof : // instanceof
			operatorString="instanceof"; //$NON-NLS-1$
			break;
		case TokenNamePLUS : // + (15.17, 15.17.2)
			operatorString="+"; //$NON-NLS-1$
			break;

		case TokenNameMINUS : // - (15.17.2)
			operatorString="-"; //$NON-NLS-1$
			break;
		case TokenNameMULTIPLY : // * (15.16.1)
			operatorString="*"; //$NON-NLS-1$
			break;

		case TokenNameDIVIDE : // / (15.16.2)
			operatorString="/"; //$NON-NLS-1$
			break;

		case TokenNameREMAINDER : // % (15.16.3)
			operatorString="%"; //$NON-NLS-1$
			break;
		case TokenNameLEFT_SHIFT : // << (15.18)
			operatorString="<<"; //$NON-NLS-1$
			break;

		case TokenNameRIGHT_SHIFT : // >> (15.18)
			operatorString=">>"; //$NON-NLS-1$
			break;

		case TokenNameUNSIGNED_RIGHT_SHIFT : // >>> (15.18)
			operatorString=">>>"; //$NON-NLS-1$
			break;
		case TokenNameAND : // & (15.21, 15.21.1, 15.21.2)
			operatorString="&"; //$NON-NLS-1$
			break;

		case TokenNameOR : // | (15.21, 15.21.1, 15.21.2)
			operatorString="|"; //$NON-NLS-1$
			break;

		case TokenNameXOR : // ^ (15.21, 15.21.1, 15.21.2)
			operatorString="^"; //$NON-NLS-1$
			break;
		case TokenNameMULTIPLY_EQUAL : // *= (15.25.2)
			operatorString="*="; //$NON-NLS-1$
			break;

		case TokenNameDIVIDE_EQUAL : // /= (15.25.2)
			operatorString="/="; //$NON-NLS-1$
			break;
		case TokenNameREMAINDER_EQUAL : // %= (15.25.2)
			operatorString="%="; //$NON-NLS-1$
			break;

		case TokenNamePLUS_EQUAL : // += (15.25.2)
			operatorString="+="; //$NON-NLS-1$
			break;

		case TokenNameMINUS_EQUAL : // -= (15.25.2)
			operatorString="-="; //$NON-NLS-1$
			break;

		case TokenNameLEFT_SHIFT_EQUAL : // <<= (15.25.2)
			operatorString="<<="; //$NON-NLS-1$
			break;

		case TokenNameRIGHT_SHIFT_EQUAL : // >>= (15.25.2)
			operatorString=">>="; //$NON-NLS-1$
			break;

		case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL : // >>>= (15.25.2)
			operatorString=">>>="; //$NON-NLS-1$
			break;

		case TokenNameAND_EQUAL : // &= (15.25.2)
			operatorString="&="; //$NON-NLS-1$
			break;

		case TokenNameXOR_EQUAL : // ^= (15.25.2)
			operatorString="^="; //$NON-NLS-1$
			break;

		case TokenNameOR_EQUAL : // |= (15.25.2)
			operatorString="|="; //$NON-NLS-1$
			break;
		case TokenNameDOT : // .
			operatorString="."; //$NON-NLS-1$
			break;

		default:
			operatorString=""; //$NON-NLS-1$
	}
		if (placeOperatorAhead){
			result.append(operatorString);
		}
		result.append(currentString);
		if (placeOperatorBehind){
			result.append(operatorString);
		}
		result.append('\n');
	}
	return ""; //$NON-NLS-1$
}
}
