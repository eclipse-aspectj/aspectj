/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/



package org.aspectj.ajdt.internal.compiler.parser;

import org.aspectj.ajdt.compiler.IAjTerminalSymbols;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.CharOperation;


public class AjScanner extends Scanner implements IScanner {
	public AjScanner(
		boolean tokenizeComments,
		boolean tokenizeWhiteSpace,
		boolean checkNonExternalizedStringLiterals) {
		super(
			tokenizeComments,
			tokenizeWhiteSpace,
			checkNonExternalizedStringLiterals);
	}

	public AjScanner(
		boolean tokenizeComments,
		boolean tokenizeWhiteSpace,
		boolean checkNonExternalizedStringLiterals,
		boolean assertMode) {
		super(
			tokenizeComments,
			tokenizeWhiteSpace,
			checkNonExternalizedStringLiterals,
			assertMode);
	}
	
	
	private static final char[] aspectV = "aspect".toCharArray();
	private static final char[] pointcutV = "pointcut".toCharArray();
	private static final char[] privilegedV = "privileged".toCharArray();
	private static final char[] beforeV = "before".toCharArray();
	private static final char[] afterV = "after".toCharArray();
	private static final char[] aroundV = "around".toCharArray();
	private static final char[] declareV = "declare".toCharArray();
	
	
	
	public int scanIdentifierOrKeyword() throws InvalidInputException {
		int kind = super.scanIdentifierOrKeyword();
		if (kind != IAjTerminalSymbols.TokenNameIdentifier) return kind;
		
		char[] contents = getCurrentIdentifierSource();
		
		//XXX performance here is less than optimal, but code simplicity is pretty damn good
		if (CharOperation.equals(aspectV, contents)) return IAjTerminalSymbols.TokenNameaspect;
		else if (CharOperation.equals(pointcutV, contents)) return IAjTerminalSymbols.TokenNamepointcut;
		else if (CharOperation.equals(privilegedV, contents)) return IAjTerminalSymbols.TokenNameprivileged;
		else if (CharOperation.equals(beforeV, contents)) return IAjTerminalSymbols.TokenNamebefore;
		else if (CharOperation.equals(afterV, contents)) return IAjTerminalSymbols.TokenNameafter;
		else if (CharOperation.equals(aroundV, contents)) return IAjTerminalSymbols.TokenNamearound;
		else if (CharOperation.equals(declareV, contents)) return IAjTerminalSymbols.TokenNamedeclare;
	
		return kind;
	}
}
