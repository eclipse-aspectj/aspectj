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
package org.eclipse.jdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;

public class Scanner implements IScanner, ITerminalSymbols {

	/* APIs ares
	 - getNextToken() which return the current type of the token
	   (this value is not memorized by the scanner)
	 - getCurrentTokenSource() which provides with the token "REAL" source
	   (aka all unicode have been transformed into a correct char)
	 - sourceStart gives the position into the stream
	 - currentPosition-1 gives the sourceEnd position into the stream 
	*/

	// 1.4 feature 
	private boolean assertMode;
	public boolean useAssertAsAnIndentifier = false;
	//flag indicating if processed source contains occurrences of keyword assert 
	public boolean containsAssertKeyword = false; 
	
	public boolean recordLineSeparator;
	public char currentCharacter;
	public int startPosition;
	public int currentPosition;
	public int initialPosition, eofPosition;
	// after this position eof are generated instead of real token from the source

	public boolean tokenizeComments;
	public boolean tokenizeWhiteSpace;

	//source should be viewed as a window (aka a part)
	//of a entire very large stream
	public char source[];

	//unicode support
	public char[] withoutUnicodeBuffer;
	public int withoutUnicodePtr; //when == 0 ==> no unicode in the current token
	public boolean unicodeAsBackSlash = false;

	public boolean scanningFloatLiteral = false;

	//support for /** comments
	//public char[][] comments = new char[10][];
	public int[] commentStops = new int[10];
	public int[] commentStarts = new int[10];
	public int commentPtr = -1; // no comment test with commentPtr value -1

	//diet parsing support - jump over some method body when requested
	public boolean diet = false;

	//support for the  poor-line-debuggers ....
	//remember the position of the cr/lf
	public int[] lineEnds = new int[250];
	public int linePtr = -1;
	public boolean wasAcr = false;

	public static final String END_OF_SOURCE = "End_Of_Source"; //$NON-NLS-1$

	public static final String INVALID_HEXA = "Invalid_Hexa_Literal"; //$NON-NLS-1$
	public static final String INVALID_OCTAL = "Invalid_Octal_Literal"; //$NON-NLS-1$
	public static final String INVALID_CHARACTER_CONSTANT = 
		"Invalid_Character_Constant";  //$NON-NLS-1$
	public static final String INVALID_ESCAPE = "Invalid_Escape"; //$NON-NLS-1$
	public static final String INVALID_INPUT = "Invalid_Input"; //$NON-NLS-1$
	public static final String INVALID_UNICODE_ESCAPE = "Invalid_Unicode_Escape"; //$NON-NLS-1$
	public static final String INVALID_FLOAT = "Invalid_Float_Literal"; //$NON-NLS-1$

	public static final String NULL_SOURCE_STRING = "Null_Source_String"; //$NON-NLS-1$
	public static final String UNTERMINATED_STRING = "Unterminated_String"; //$NON-NLS-1$
	public static final String UNTERMINATED_COMMENT = "Unterminated_Comment"; //$NON-NLS-1$
	public static final String INVALID_CHAR_IN_STRING = "Invalid_Char_In_String"; //$NON-NLS-1$

	//----------------optimized identifier managment------------------
	static final char[] charArray_a = new char[] {'a'}, 
		charArray_b = new char[] {'b'}, 
		charArray_c = new char[] {'c'}, 
		charArray_d = new char[] {'d'}, 
		charArray_e = new char[] {'e'}, 
		charArray_f = new char[] {'f'}, 
		charArray_g = new char[] {'g'}, 
		charArray_h = new char[] {'h'}, 
		charArray_i = new char[] {'i'}, 
		charArray_j = new char[] {'j'}, 
		charArray_k = new char[] {'k'}, 
		charArray_l = new char[] {'l'}, 
		charArray_m = new char[] {'m'}, 
		charArray_n = new char[] {'n'}, 
		charArray_o = new char[] {'o'}, 
		charArray_p = new char[] {'p'}, 
		charArray_q = new char[] {'q'}, 
		charArray_r = new char[] {'r'}, 
		charArray_s = new char[] {'s'}, 
		charArray_t = new char[] {'t'}, 
		charArray_u = new char[] {'u'}, 
		charArray_v = new char[] {'v'}, 
		charArray_w = new char[] {'w'}, 
		charArray_x = new char[] {'x'}, 
		charArray_y = new char[] {'y'}, 
		charArray_z = new char[] {'z'}; 

	static final char[] initCharArray = 
		new char[] {'\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000'}; 
	static final int TableSize = 30, InternalTableSize = 6; //30*6 = 180 entries
	public static final int OptimizedLength = 6;
	public /*static*/ final char[][][][] charArray_length = 
		new char[OptimizedLength][TableSize][InternalTableSize][]; 
	// support for detecting non-externalized string literals
	int currentLineNr= -1;
	int previousLineNr= -1;
	NLSLine currentLine= null;
	List lines= new ArrayList();
	public static final String TAG_PREFIX= "//$NON-NLS-"; //$NON-NLS-1$
	public static final int TAG_PREFIX_LENGTH= TAG_PREFIX.length();
	public static final String TAG_POSTFIX= "$"; //$NON-NLS-1$
	public static final int TAG_POSTFIX_LENGTH= TAG_POSTFIX.length();
	public StringLiteral[] nonNLSStrings = null;
	public boolean checkNonExternalizedStringLiterals = true;
	public boolean wasNonExternalizedStringLiteral = false;
	
	/*static*/ {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < TableSize; j++) {
				for (int k = 0; k < InternalTableSize; k++) {
					charArray_length[i][j][k] = initCharArray;
				}
			}
		}
	}
	static int newEntry2 = 0, 
		newEntry3 = 0, 
		newEntry4 = 0, 
		newEntry5 = 0, 
		newEntry6 = 0;

	public static final int RoundBracket = 0;
	public static final int SquareBracket = 1;
	public static final int CurlyBracket = 2;	
	public static final int BracketKinds = 3;
public Scanner() {
	this(false, false);
}
public Scanner(boolean tokenizeComments, boolean tokenizeWhiteSpace) {
	this(tokenizeComments, tokenizeWhiteSpace, false);	
}
public  final boolean atEnd() {
	// This code is not relevant if source is 
	// Only a part of the real stream input

	return source.length == currentPosition;
}
public char[] getCurrentIdentifierSource() {
	//return the token REAL source (aka unicodes are precomputed)

	char[] result;
	if (withoutUnicodePtr != 0)
		//0 is used as a fast test flag so the real first char is in position 1
		System.arraycopy(
			withoutUnicodeBuffer, 
			1, 
			result = new char[withoutUnicodePtr], 
			0, 
			withoutUnicodePtr); 
	else {
		int length = currentPosition - startPosition;
		switch (length) { // see OptimizedLength
			case 1 :
				return optimizedCurrentTokenSource1();
			case 2 :
				return optimizedCurrentTokenSource2();
			case 3 :
				return optimizedCurrentTokenSource3();
			case 4 :
				return optimizedCurrentTokenSource4();
			case 5 :
				return optimizedCurrentTokenSource5();
			case 6 :
				return optimizedCurrentTokenSource6();
		}
		//no optimization
		System.arraycopy(source, startPosition, result = new char[length], 0, length);
	}
	return result;
}
public int getCurrentTokenEndPosition(){
	return this.currentPosition - 1;
}
public final char[] getCurrentTokenSource() {
	// Return the token REAL source (aka unicodes are precomputed)

	char[] result;
	if (withoutUnicodePtr != 0)
		// 0 is used as a fast test flag so the real first char is in position 1
		System.arraycopy(
			withoutUnicodeBuffer, 
			1, 
			result = new char[withoutUnicodePtr], 
			0, 
			withoutUnicodePtr); 
	else {
		int length;
		System.arraycopy(
			source, 
			startPosition, 
			result = new char[length = currentPosition - startPosition], 
			0, 
			length); 
	}
	return result;
}
public final char[] getCurrentTokenSourceString() {
	//return the token REAL source (aka unicodes are precomputed).
	//REMOVE the two " that are at the beginning and the end.

	char[] result;
	if (withoutUnicodePtr != 0)
		//0 is used as a fast test flag so the real first char is in position 1
		System.arraycopy(withoutUnicodeBuffer, 2,
		//2 is 1 (real start) + 1 (to jump over the ")
		result = new char[withoutUnicodePtr - 2], 0, withoutUnicodePtr - 2);
	else {
		int length;
		System.arraycopy(
			source, 
			startPosition + 1, 
			result = new char[length = currentPosition - startPosition - 2], 
			0, 
			length); 
	}
	return result;
}
public int getCurrentTokenStartPosition(){
	return this.startPosition;
}
/*
 * Search the source position corresponding to the end of a given line number
 *
 * Line numbers are 1-based, and relative to the scanner initialPosition. 
 * Character positions are 0-based.
 *
 * In case the given line number is inconsistent, answers -1.
 */
public final int getLineEnd(int lineNumber) {

	if (lineEnds == null) return -1;
	if (lineNumber >= lineEnds.length) return -1;
	if (lineNumber <= 0) return -1;
	
	if (lineNumber == lineEnds.length - 1) return eofPosition;
	return lineEnds[lineNumber-1]; // next line start one character behind the lineEnd of the previous line
}
/**
 * Search the source position corresponding to the beginning of a given line number
 *
 * Line numbers are 1-based, and relative to the scanner initialPosition. 
 * Character positions are 0-based.
 *
 * e.g.	getLineStart(1) --> 0	i.e. first line starts at character 0.
 *
 * In case the given line number is inconsistent, answers -1.
 */
public final int getLineStart(int lineNumber) {

	if (lineEnds == null) return -1;
	if (lineNumber >= lineEnds.length) return -1;
	if (lineNumber <= 0) return -1;
	
	if (lineNumber == 1) return initialPosition;
	return lineEnds[lineNumber-2]+1; // next line start one character behind the lineEnd of the previous line
}
public final boolean getNextChar(char testedChar) {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is == to the testedChar
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	int temp = currentPosition;
	try {
		if (((currentCharacter = source[currentPosition++]) == '\\')
			&& (source[currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			currentPosition++;
			while (source[currentPosition] == 'u') {
				currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(source[currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(source[currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(source[currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(source[currentPosition++])) > 15 || c4 < 0)) {
				currentPosition = temp;
				return false;
			}

			currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (currentCharacter != testedChar) {
				currentPosition = temp;
				return false;
			}
			unicodeAsBackSlash = currentCharacter == '\\';

			//need the unicode buffer
			if (withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				withoutUnicodePtr = currentPosition - unicodeSize - startPosition;
				System.arraycopy(
					source, 
					startPosition, 
					withoutUnicodeBuffer, 
					1, 
					withoutUnicodePtr); 
			}
			//fill the buffer with the char
			withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;

		} //-------------end unicode traitement--------------
		else {
			if (currentCharacter != testedChar) {
				currentPosition = temp;
				return false;
			}
			unicodeAsBackSlash = false;
			if (withoutUnicodePtr != 0)
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		unicodeAsBackSlash = false;
		currentPosition = temp;
		return false;
	}
}
public final int getNextChar(char testedChar1, char testedChar2) {
	//INT 0 : testChar1 \\\\///\\\\ 1 : testedChar2 \\\\///\\\\ -1 : others
	//test can be done with (x==0) for the first and (x>0) for the second
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is == to the testedChar1/2
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	int temp = currentPosition;
	try {
		int result;
		if (((currentCharacter = source[currentPosition++]) == '\\')
			&& (source[currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			currentPosition++;
			while (source[currentPosition] == 'u') {
				currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(source[currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(source[currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(source[currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(source[currentPosition++])) > 15 || c4 < 0)) {
				currentPosition = temp;
				return 2;
			}

			currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (currentCharacter == testedChar1)
				result = 0;
			else
				if (currentCharacter == testedChar2)
					result = 1;
				else {
					currentPosition = temp;
					return -1;
				}

			//need the unicode buffer
			if (withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				withoutUnicodePtr = currentPosition - unicodeSize - startPosition;
				System.arraycopy(
					source, 
					startPosition, 
					withoutUnicodeBuffer, 
					1, 
					withoutUnicodePtr); 
			}
			//fill the buffer with the char
			withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return result;
		} //-------------end unicode traitement--------------
		else {
			if (currentCharacter == testedChar1)
				result = 0;
			else
				if (currentCharacter == testedChar2)
					result = 1;
				else {
					currentPosition = temp;
					return -1;
				}

			if (withoutUnicodePtr != 0)
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return result;
		}
	} catch (IndexOutOfBoundsException e) {
		currentPosition = temp;
		return -1;
	}
}
public final boolean getNextCharAsDigit() {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is a digit
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	int temp = currentPosition;
	try {
		if (((currentCharacter = source[currentPosition++]) == '\\')
			&& (source[currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			currentPosition++;
			while (source[currentPosition] == 'u') {
				currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(source[currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(source[currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(source[currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(source[currentPosition++])) > 15 || c4 < 0)) {
				currentPosition = temp;
				return false;
			}

			currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (!Character.isDigit(currentCharacter)) {
				currentPosition = temp;
				return false;
			}

			//need the unicode buffer
			if (withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				withoutUnicodePtr = currentPosition - unicodeSize - startPosition;
				System.arraycopy(
					source, 
					startPosition, 
					withoutUnicodeBuffer, 
					1, 
					withoutUnicodePtr); 
			}
			//fill the buffer with the char
			withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;
		} //-------------end unicode traitement--------------
		else {
			if (!Character.isDigit(currentCharacter)) {
				currentPosition = temp;
				return false;
			}
			if (withoutUnicodePtr != 0)
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		currentPosition = temp;
		return false;
	}
}
public final boolean getNextCharAsDigit(int radix) {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is a digit base on radix
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	int temp = currentPosition;
	try {
		if (((currentCharacter = source[currentPosition++]) == '\\')
			&& (source[currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			currentPosition++;
			while (source[currentPosition] == 'u') {
				currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(source[currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(source[currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(source[currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(source[currentPosition++])) > 15 || c4 < 0)) {
				currentPosition = temp;
				return false;
			}

			currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (Character.digit(currentCharacter, radix) == -1) {
				currentPosition = temp;
				return false;
			}

			//need the unicode buffer
			if (withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				withoutUnicodePtr = currentPosition - unicodeSize - startPosition;
				System.arraycopy(
					source, 
					startPosition, 
					withoutUnicodeBuffer, 
					1, 
					withoutUnicodePtr); 
			}
			//fill the buffer with the char
			withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;
		} //-------------end unicode traitement--------------
		else {
			if (Character.digit(currentCharacter, radix) == -1) {
				currentPosition = temp;
				return false;
			}
			if (withoutUnicodePtr != 0)
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		currentPosition = temp;
		return false;
	}
}
public boolean getNextCharAsJavaIdentifierPart() {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is a JavaIdentifierPart
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	int temp = currentPosition;
	try {
		if (((currentCharacter = source[currentPosition++]) == '\\')
			&& (source[currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			currentPosition++;
			while (source[currentPosition] == 'u') {
				currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(source[currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(source[currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(source[currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(source[currentPosition++])) > 15 || c4 < 0)) {
				currentPosition = temp;
				return false;
			}

			currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (!Character.isJavaIdentifierPart(currentCharacter)) {
				currentPosition = temp;
				return false;
			}

			//need the unicode buffer
			if (withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				withoutUnicodePtr = currentPosition - unicodeSize - startPosition;
				System.arraycopy(
					source, 
					startPosition, 
					withoutUnicodeBuffer, 
					1, 
					withoutUnicodePtr); 
			}
			//fill the buffer with the char
			withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;
		} //-------------end unicode traitement--------------
		else {
			if (!Character.isJavaIdentifierPart(currentCharacter)) {
				currentPosition = temp;
				return false;
			}

			if (withoutUnicodePtr != 0)
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		currentPosition = temp;
		return false;
	}
}
public int getNextToken() throws InvalidInputException {

	this.wasAcr = false;
	if (diet) {
		jumpOverMethodBody();
		diet = false;
		return currentPosition > source.length ? TokenNameEOF : TokenNameRBRACE;
	}
	try {
		while (true) { //loop for jumping over comments
			withoutUnicodePtr = 0;
			//start with a new token (even comment written with unicode )

			// ---------Consume white space and handles startPosition---------
			int whiteStart = currentPosition;
			boolean isWhiteSpace;
			do {
				startPosition = currentPosition;
				if (((currentCharacter = source[currentPosition++]) == '\\')
					&& (source[currentPosition] == 'u')) {
					isWhiteSpace = jumpOverUnicodeWhiteSpace();
				} else {
					if ((currentCharacter == '\r') || (currentCharacter == '\n')) {
						checkNonExternalizeString();
						if (recordLineSeparator) {
							pushLineSeparator();
						} else {
							currentLine = null;
						}
					}
					isWhiteSpace = 
						(currentCharacter == ' ') || Character.isWhitespace(currentCharacter); 
				}
			} while (isWhiteSpace);
			if (tokenizeWhiteSpace && (whiteStart != currentPosition - 1)) {
				// reposition scanner in case we are interested by spaces as tokens
				currentPosition--;
				startPosition = whiteStart;
				return TokenNameWHITESPACE;
			}
			//little trick to get out in the middle of a source compuation
			if (currentPosition > eofPosition)
				return TokenNameEOF;

			// ---------Identify the next token-------------

			switch (currentCharacter) {
				case '(' :
					return TokenNameLPAREN;
				case ')' :
					return TokenNameRPAREN;
				case '{' :
					return TokenNameLBRACE;
				case '}' :
					return TokenNameRBRACE;
				case '[' :
					return TokenNameLBRACKET;
				case ']' :
					return TokenNameRBRACKET;
				case ';' :
					return TokenNameSEMICOLON;
				case ',' :
					return TokenNameCOMMA;
				case '.' :
					if (getNextCharAsDigit())
						return scanNumber(true);
					return TokenNameDOT;
				case '+' :
					{
						int test;
						if ((test = getNextChar('+', '=')) == 0)
							return TokenNamePLUS_PLUS;
						if (test > 0)
							return TokenNamePLUS_EQUAL;
						return TokenNamePLUS;
					}
				case '-' :
					{
						int test;
						if ((test = getNextChar('-', '=')) == 0)
							return TokenNameMINUS_MINUS;
						if (test > 0)
							return TokenNameMINUS_EQUAL;
						return TokenNameMINUS;
					}
				case '~' :
					return TokenNameTWIDDLE;
				case '!' :
					if (getNextChar('='))
						return TokenNameNOT_EQUAL;
					return TokenNameNOT;
				case '*' :
					if (getNextChar('='))
						return TokenNameMULTIPLY_EQUAL;
					return TokenNameMULTIPLY;
				case '%' :
					if (getNextChar('='))
						return TokenNameREMAINDER_EQUAL;
					return TokenNameREMAINDER;
				case '<' :
					{
						int test;
						if ((test = getNextChar('=', '<')) == 0)
							return TokenNameLESS_EQUAL;
						if (test > 0) {
							if (getNextChar('='))
								return TokenNameLEFT_SHIFT_EQUAL;
							return TokenNameLEFT_SHIFT;
						}
						return TokenNameLESS;
					}
				case '>' :
					{
						int test;
						if ((test = getNextChar('=', '>')) == 0)
							return TokenNameGREATER_EQUAL;
						if (test > 0) {
							if ((test = getNextChar('=', '>')) == 0)
								return TokenNameRIGHT_SHIFT_EQUAL;
							if (test > 0) {
								if (getNextChar('='))
									return TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL;
								return TokenNameUNSIGNED_RIGHT_SHIFT;
							}
							return TokenNameRIGHT_SHIFT;
						}
						return TokenNameGREATER;
					}
				case '=' :
					if (getNextChar('='))
						return TokenNameEQUAL_EQUAL;
					return TokenNameEQUAL;
				case '&' :
					{
						int test;
						if ((test = getNextChar('&', '=')) == 0)
							return TokenNameAND_AND;
						if (test > 0)
							return TokenNameAND_EQUAL;
						return TokenNameAND;
					}
				case '|' :
					{
						int test;
						if ((test = getNextChar('|', '=')) == 0)
							return TokenNameOR_OR;
						if (test > 0)
							return TokenNameOR_EQUAL;
						return TokenNameOR;
					}
				case '^' :
					if (getNextChar('='))
						return TokenNameXOR_EQUAL;
					return TokenNameXOR;
				case '?' :
					return TokenNameQUESTION;
				case ':' :
					return TokenNameCOLON;
				case '\'' :
					{
						int test;
						if ((test = getNextChar('\n', '\r')) == 0) {
							throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
						}
						if (test > 0) {
							// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
							for (int lookAhead = 0; lookAhead < 3; lookAhead++) {
								if (currentPosition + lookAhead == source.length)
									break;
								if (source[currentPosition + lookAhead] == '\n')
									break;
								if (source[currentPosition + lookAhead] == '\'') {
									currentPosition += lookAhead + 1;
									break;
								}
							}
							throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
						}
					}
					if (getNextChar('\'')) {
						// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
						for (int lookAhead = 0; lookAhead < 3; lookAhead++) {
							if (currentPosition + lookAhead == source.length)
								break;
							if (source[currentPosition + lookAhead] == '\n')
								break;
							if (source[currentPosition + lookAhead] == '\'') {
								currentPosition += lookAhead + 1;
								break;
							}
						}
						throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
					}
					if (getNextChar('\\'))
						scanEscapeCharacter();
					else { // consume next character
						unicodeAsBackSlash = false;
						if (((currentCharacter = source[currentPosition++]) == '\\')
							&& (source[currentPosition] == 'u')) {
							getNextUnicodeChar();
						} else {
							if (withoutUnicodePtr != 0) {
								withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
							}
						}
					}
					if (getNextChar('\''))
						return TokenNameCharacterLiteral;
					// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
					for (int lookAhead = 0; lookAhead < 20; lookAhead++) {
						if (currentPosition + lookAhead == source.length)
							break;
						if (source[currentPosition + lookAhead] == '\n')
							break;
						if (source[currentPosition + lookAhead] == '\'') {
							currentPosition += lookAhead + 1;
							break;
						}
					}
					throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
				case '"' :
					try {
						// consume next character
						unicodeAsBackSlash = false;
						if (((currentCharacter = source[currentPosition++]) == '\\')
							&& (source[currentPosition] == 'u')) {
							getNextUnicodeChar();
						} else {
							if (withoutUnicodePtr != 0) {
								withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
							}
						}

						while (currentCharacter != '"') {
							/**** \r and \n are not valid in string literals ****/
							if ((currentCharacter == '\n') || (currentCharacter == '\r')) {
								// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
								for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
									if (currentPosition + lookAhead == source.length)
										break;
									if (source[currentPosition + lookAhead] == '\n')
										break;
									if (source[currentPosition + lookAhead] == '\"') {
										currentPosition += lookAhead + 1;
										break;
									}
								}
								throw new InvalidInputException(INVALID_CHAR_IN_STRING);
							}
							if (currentCharacter == '\\') {
								int escapeSize = currentPosition;
								boolean backSlashAsUnicodeInString = unicodeAsBackSlash;
								//scanEscapeCharacter make a side effect on this value and we need the previous value few lines down this one
								scanEscapeCharacter();
								escapeSize = currentPosition - escapeSize;
								if (withoutUnicodePtr == 0) {
									//buffer all the entries that have been left aside....
									withoutUnicodePtr = currentPosition - escapeSize - 1 - startPosition;
									System.arraycopy(
										source, 
										startPosition, 
										withoutUnicodeBuffer, 
										1, 
										withoutUnicodePtr); 
									withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
								} else { //overwrite the / in the buffer
									withoutUnicodeBuffer[withoutUnicodePtr] = currentCharacter;
									if (backSlashAsUnicodeInString) { //there are TWO \ in the stream where only one is correct
										withoutUnicodePtr--;
									}
								}
							}
							// consume next character
							unicodeAsBackSlash = false;
							if (((currentCharacter = source[currentPosition++]) == '\\')
								&& (source[currentPosition] == 'u')) {
								getNextUnicodeChar();
							} else {
								if (withoutUnicodePtr != 0) {
									withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
								}
							}

						}
					} catch (IndexOutOfBoundsException e) {
						throw new InvalidInputException(UNTERMINATED_STRING);
					} catch (InvalidInputException e) {
						if (e.getMessage().equals(INVALID_ESCAPE)) {
							// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
							for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
								if (currentPosition + lookAhead == source.length)
									break;
								if (source[currentPosition + lookAhead] == '\n')
									break;
								if (source[currentPosition + lookAhead] == '\"') {
									currentPosition += lookAhead + 1;
									break;
								}
							}

						}
						throw e; // rethrow
					}
					if (checkNonExternalizedStringLiterals){ // check for presence of	NLS tags //$NON-NLS-?$ where ? is an int.
						if (currentLine == null) {
							currentLine= new NLSLine();
							lines.add(currentLine);
						}
						currentLine.add(
							new StringLiteral(
								getCurrentTokenSourceString(), 
								startPosition, 
								currentPosition - 1));
					}
					return TokenNameStringLiteral;
				case '/' :
					{
						int test;
						if ((test = getNextChar('/', '*')) == 0) { //line comment 
							int endPositionForLineComment = 0;
							try { //get the next char 
								if (((currentCharacter = source[currentPosition++]) == '\\')
									&& (source[currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
									currentPosition++;
									while (source[currentPosition] == 'u') {
										currentPosition++;
									}
									if ((c1 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c1 < 0
										|| (c2 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c2 < 0
										|| (c3 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c3 < 0
										|| (c4 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c4 < 0) {
										throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
									} else {
										currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
									}
								}

								//handle the \\u case manually into comment
								if (currentCharacter == '\\') {
									if (source[currentPosition] == '\\')
										currentPosition++;
								} //jump over the \\
								boolean isUnicode = false;
								while (currentCharacter != '\r' && currentCharacter != '\n') {
									//get the next char
									isUnicode = false;									
									if (((currentCharacter = source[currentPosition++]) == '\\')
										&& (source[currentPosition] == 'u')) {
										isUnicode = true;											
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										currentPosition++;
										while (source[currentPosition] == 'u') {
											currentPosition++;
										}
										if ((c1 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c1 < 0
											|| (c2 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c2 < 0
											|| (c3 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c3 < 0
											|| (c4 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c4 < 0) {
											throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
										} else {
											currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
									}
									//handle the \\u case manually into comment
									if (currentCharacter == '\\') {
										if (source[currentPosition] == '\\')
											currentPosition++;
									} //jump over the \\
								}
								if (isUnicode) {
									endPositionForLineComment = currentPosition - 6;
								} else {
									endPositionForLineComment = currentPosition - 1;
								}
								recordComment(false);
								if ((currentCharacter == '\r') || (currentCharacter == '\n')) {
									checkNonExternalizeString();
									if (recordLineSeparator) {
										if (isUnicode) {
											pushUnicodeLineSeparator();
										} else {
											pushLineSeparator();
										}
									} else {
										currentLine = null;
									}
								}
								if (tokenizeComments) {
									if (!isUnicode) {
										currentPosition = endPositionForLineComment; // reset one character behind
									}
									return TokenNameCOMMENT_LINE;
								}
							} catch (IndexOutOfBoundsException e) { //an eof will them be generated
									if (tokenizeComments) {
										currentPosition--; // reset one character behind
										return TokenNameCOMMENT_LINE;
									}
							}
							break;
						}
						if (test > 0) { //traditional and annotation comment
							boolean isJavadoc = false, star = false;
							// consume next character
							unicodeAsBackSlash = false;
							if (((currentCharacter = source[currentPosition++]) == '\\')
								&& (source[currentPosition] == 'u')) {
								getNextUnicodeChar();
							} else {
								if (withoutUnicodePtr != 0) {
									withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
								}
							}

							if (currentCharacter == '*') {
								isJavadoc = true;
								star = true;
							}
							if ((currentCharacter == '\r') || (currentCharacter == '\n')) {
								checkNonExternalizeString();
								if (recordLineSeparator) {
									pushLineSeparator();
								} else {
									currentLine = null;
								}
							}
							try { //get the next char 
								if (((currentCharacter = source[currentPosition++]) == '\\')
									&& (source[currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									getNextUnicodeChar();
								}
								//handle the \\u case manually into comment
								if (currentCharacter == '\\') {
									if (source[currentPosition] == '\\')
										currentPosition++; //jump over the \\
								}
								// empty comment is not a javadoc /**/
								if (currentCharacter == '/') { 
									isJavadoc = false;
								}
								//loop until end of comment */
								while ((currentCharacter != '/') || (!star)) {
									if ((currentCharacter == '\r') || (currentCharacter == '\n')) {
										checkNonExternalizeString();
										if (recordLineSeparator) {
											pushLineSeparator();
										} else {
											currentLine = null;
										}
									}
									star = currentCharacter == '*';
									//get next char
									if (((currentCharacter = source[currentPosition++]) == '\\')
										&& (source[currentPosition] == 'u')) {
										//-------------unicode traitement ------------
										getNextUnicodeChar();
									}
									//handle the \\u case manually into comment
									if (currentCharacter == '\\') {
										if (source[currentPosition] == '\\')
											currentPosition++;
									} //jump over the \\
								}
								recordComment(isJavadoc);
								if (tokenizeComments) {
									if (isJavadoc)
										return TokenNameCOMMENT_JAVADOC;
									return TokenNameCOMMENT_BLOCK;
								}
							} catch (IndexOutOfBoundsException e) {
								throw new InvalidInputException(UNTERMINATED_COMMENT);
							}
							break;
						}
						if (getNextChar('='))
							return TokenNameDIVIDE_EQUAL;
						return TokenNameDIVIDE;
					}
				case '\u001a' :
					if (atEnd())
						return TokenNameEOF;
					//the atEnd may not be <currentPosition == source.length> if source is only some part of a real (external) stream
					throw new InvalidInputException("Ctrl-Z"); //$NON-NLS-1$

				default :
					if (Character.isJavaIdentifierStart(currentCharacter))
						return scanIdentifierOrKeyword();
					if (Character.isDigit(currentCharacter))
						return scanNumber(false);
					return TokenNameERROR;
			}
		}
	} //-----------------end switch while try--------------------
	catch (IndexOutOfBoundsException e) {
	}
	return TokenNameEOF;
}
public final void getNextUnicodeChar()
	throws IndexOutOfBoundsException, InvalidInputException {
	//VOID
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	int c1 = 0, c2 = 0, c3 = 0, c4 = 0, unicodeSize = 6;
	currentPosition++;
	while (source[currentPosition] == 'u') {
		currentPosition++;
		unicodeSize++;
	}

	if ((c1 = Character.getNumericValue(source[currentPosition++])) > 15
		|| c1 < 0
		|| (c2 = Character.getNumericValue(source[currentPosition++])) > 15
		|| c2 < 0
		|| (c3 = Character.getNumericValue(source[currentPosition++])) > 15
		|| c3 < 0
		|| (c4 = Character.getNumericValue(source[currentPosition++])) > 15
		|| c4 < 0){
		throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
	} else {
		currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
		//need the unicode buffer
		if (withoutUnicodePtr == 0) {
			//buffer all the entries that have been left aside....
			withoutUnicodePtr = currentPosition - unicodeSize - startPosition;
			System.arraycopy(
				source, 
				startPosition, 
				withoutUnicodeBuffer, 
				1, 
				withoutUnicodePtr); 
		}
		//fill the buffer with the char
		withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
	}
	unicodeAsBackSlash = currentCharacter == '\\';
}
/* Tokenize a method body, assuming that curly brackets are properly balanced.
 */
public final void jumpOverMethodBody() {

	this.wasAcr = false;
	int found = 1;
	try {
		while (true) { //loop for jumping over comments
			// ---------Consume white space and handles startPosition---------
			boolean isWhiteSpace;
			do {
				startPosition = currentPosition;
				if (((currentCharacter = source[currentPosition++]) == '\\')
					&& (source[currentPosition] == 'u')) {
					isWhiteSpace = jumpOverUnicodeWhiteSpace();
				} else {
					if (recordLineSeparator
						&& ((currentCharacter == '\r') || (currentCharacter == '\n')))
						pushLineSeparator();
					isWhiteSpace = Character.isWhitespace(currentCharacter);
				}
			} while (isWhiteSpace);

			// -------consume token until } is found---------
			switch (currentCharacter) {
				case '{' :
					found++;
					break;
				case '}' :
					found--;
					if (found == 0)
						return;
					break;
				case '\'' :
					{
						boolean test;
						test = getNextChar('\\');
						if (test) {
							try {
								scanEscapeCharacter();
							} catch (InvalidInputException ex) {
							};
						} else {
							try { // consume next character
								unicodeAsBackSlash = false;
								if (((currentCharacter = source[currentPosition++]) == '\\')
									&& (source[currentPosition] == 'u')) {
									getNextUnicodeChar();
								} else {
									if (withoutUnicodePtr != 0) {
										withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
									}
								}
							} catch (InvalidInputException ex) {
							};
						}
						getNextChar('\'');
						break;
					}
				case '"' :
					try {
						try { // consume next character
							unicodeAsBackSlash = false;
							if (((currentCharacter = source[currentPosition++]) == '\\')
								&& (source[currentPosition] == 'u')) {
								getNextUnicodeChar();
							} else {
								if (withoutUnicodePtr != 0) {
									withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
								}
							}
						} catch (InvalidInputException ex) {
						};
						while (currentCharacter != '"') {
							if (currentCharacter == '\r'){
								if (source[currentPosition] == '\n') currentPosition++;
								break; // the string cannot go further that the line
							}
							if (currentCharacter == '\n'){
								break; // the string cannot go further that the line
							}
							if (currentCharacter == '\\') {
								try {
									scanEscapeCharacter();
								} catch (InvalidInputException ex) {
								};
							}
							try { // consume next character
								unicodeAsBackSlash = false;
								if (((currentCharacter = source[currentPosition++]) == '\\')
									&& (source[currentPosition] == 'u')) {
									getNextUnicodeChar();
								} else {
									if (withoutUnicodePtr != 0) {
										withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
									}
								}
							} catch (InvalidInputException ex) {
							};
						}
					} catch (IndexOutOfBoundsException e) {
						return;
					}
					break;
				case '/' :
					{
						int test;
						if ((test = getNextChar('/', '*')) == 0) { //line comment 
							try {
								//get the next char 
								if (((currentCharacter = source[currentPosition++]) == '\\')
									&& (source[currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
									currentPosition++;
									while (source[currentPosition] == 'u') {
										currentPosition++;
									}
									if ((c1 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c1 < 0
										|| (c2 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c2 < 0
										|| (c3 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c3 < 0
										|| (c4 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c4 < 0) { //error don't care of the value
										currentCharacter = 'A';
									} //something different from \n and \r
									else {
										currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
									}
								}

								while (currentCharacter != '\r' && currentCharacter != '\n') {
									//get the next char 
									if (((currentCharacter = source[currentPosition++]) == '\\')
										&& (source[currentPosition] == 'u')) {
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										currentPosition++;
										while (source[currentPosition] == 'u') {
											currentPosition++;
										}
										if ((c1 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c1 < 0
											|| (c2 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c2 < 0
											|| (c3 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c3 < 0
											|| (c4 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c4 < 0) { //error don't care of the value
											currentCharacter = 'A';
										} //something different from \n and \r
										else {
											currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
									}
								}
								if (recordLineSeparator
									&& ((currentCharacter == '\r') || (currentCharacter == '\n')))
									pushLineSeparator();
							} catch (IndexOutOfBoundsException e) {
							} //an eof will them be generated
							break;
						}
						if (test > 0) { //traditional and annotation comment
							boolean star = false;
							try { // consume next character
								unicodeAsBackSlash = false;
								if (((currentCharacter = source[currentPosition++]) == '\\')
									&& (source[currentPosition] == 'u')) {
									getNextUnicodeChar();
								} else {
									if (withoutUnicodePtr != 0) {
										withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
									}
								};
							} catch (InvalidInputException ex) {
							};
							if (currentCharacter == '*') {
								star = true;
							}
							if (recordLineSeparator
								&& ((currentCharacter == '\r') || (currentCharacter == '\n')))
								pushLineSeparator();
							try { //get the next char 
								if (((currentCharacter = source[currentPosition++]) == '\\')
									&& (source[currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
									currentPosition++;
									while (source[currentPosition] == 'u') {
										currentPosition++;
									}
									if ((c1 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c1 < 0
										|| (c2 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c2 < 0
										|| (c3 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c3 < 0
										|| (c4 = Character.getNumericValue(source[currentPosition++])) > 15
										|| c4 < 0) { //error don't care of the value
										currentCharacter = 'A';
									} //something different from * and /
									else {
										currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
									}
								}
								//loop until end of comment */ 
								while ((currentCharacter != '/') || (!star)) {
									if (recordLineSeparator
										&& ((currentCharacter == '\r') || (currentCharacter == '\n')))
										pushLineSeparator();
									star = currentCharacter == '*';
									//get next char
									if (((currentCharacter = source[currentPosition++]) == '\\')
										&& (source[currentPosition] == 'u')) {
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										currentPosition++;
										while (source[currentPosition] == 'u') {
											currentPosition++;
										}
										if ((c1 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c1 < 0
											|| (c2 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c2 < 0
											|| (c3 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c3 < 0
											|| (c4 = Character.getNumericValue(source[currentPosition++])) > 15
											|| c4 < 0) { //error don't care of the value
											currentCharacter = 'A';
										} //something different from * and /
										else {
											currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
									}
								}
							} catch (IndexOutOfBoundsException e) {
								return;
							}
							break;
						}
						break;
					}

				default :
					if (Character.isJavaIdentifierStart(currentCharacter)) {
						try {
							scanIdentifierOrKeyword();
						} catch (InvalidInputException ex) {
						};
						break;
					}
					if (Character.isDigit(currentCharacter)) {
						try {
							scanNumber(false);
						} catch (InvalidInputException ex) {
						};
						break;
					}
			}
		}
		//-----------------end switch while try--------------------
	} catch (IndexOutOfBoundsException e) {
	} catch (InvalidInputException e) {
	}
	return;
}
public final boolean jumpOverUnicodeWhiteSpace() throws InvalidInputException {
	//BOOLEAN
	//handle the case of unicode. Jump over the next whiteSpace
	//making startPosition pointing on the next available char
	//On false, the currentCharacter is filled up with a potential
	//correct char

	try {
		this.wasAcr = false;
		int c1, c2, c3, c4;
		int unicodeSize = 6;
		currentPosition++;
		while (source[currentPosition] == 'u') {
			currentPosition++;
			unicodeSize++;
		}

		if (((c1 = Character.getNumericValue(source[currentPosition++])) > 15
			|| c1 < 0)
			|| ((c2 = Character.getNumericValue(source[currentPosition++])) > 15 || c2 < 0)
			|| ((c3 = Character.getNumericValue(source[currentPosition++])) > 15 || c3 < 0)
			|| ((c4 = Character.getNumericValue(source[currentPosition++])) > 15 || c4 < 0)) {
			throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
		}

		currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
		if (recordLineSeparator
			&& ((currentCharacter == '\r') || (currentCharacter == '\n')))
			pushLineSeparator();
		if (Character.isWhitespace(currentCharacter))
			return true;

		//buffer the new char which is not a white space
		withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
		//withoutUnicodePtr == 1 is true here
		return false;
	} catch (IndexOutOfBoundsException e){
		throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
	}
}
public final int[] getLineEnds() {
	//return a bounded copy of this.lineEnds 

	int[] copy;
	System.arraycopy(lineEnds, 0, copy = new int[linePtr + 1], 0, linePtr + 1);
	return copy;
}

public char[] getSource(){
	return this.source;
}
final char[] optimizedCurrentTokenSource1() {
	//return always the same char[] build only once

	//optimization at no speed cost of 99.5 % of the singleCharIdentifier
	char charOne = source[startPosition];
	switch (charOne) {
		case 'a' :
			return charArray_a;
		case 'b' :
			return charArray_b;
		case 'c' :
			return charArray_c;
		case 'd' :
			return charArray_d;
		case 'e' :
			return charArray_e;
		case 'f' :
			return charArray_f;
		case 'g' :
			return charArray_g;
		case 'h' :
			return charArray_h;
		case 'i' :
			return charArray_i;
		case 'j' :
			return charArray_j;
		case 'k' :
			return charArray_k;
		case 'l' :
			return charArray_l;
		case 'm' :
			return charArray_m;
		case 'n' :
			return charArray_n;
		case 'o' :
			return charArray_o;
		case 'p' :
			return charArray_p;
		case 'q' :
			return charArray_q;
		case 'r' :
			return charArray_r;
		case 's' :
			return charArray_s;
		case 't' :
			return charArray_t;
		case 'u' :
			return charArray_u;
		case 'v' :
			return charArray_v;
		case 'w' :
			return charArray_w;
		case 'x' :
			return charArray_x;
		case 'y' :
			return charArray_y;
		case 'z' :
			return charArray_z;
		default :
			return new char[] {charOne};
	}
}
final char[] optimizedCurrentTokenSource2() {
	//try to return the same char[] build only once

	char c0, c1;
	int hash = 
		(((c0 = source[startPosition]) << 6) + (c1 = source[startPosition + 1]))
			% TableSize; 
	char[][] table = charArray_length[0][hash];
	int i = newEntry2;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry2;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1});
	newEntry2 = max;
	return r;
}
final char[] optimizedCurrentTokenSource3() {
	//try to return the same char[] build only once

	char c0, c1, c2;
	int hash = 
		(((c0 = source[startPosition]) << 12)
			+ ((c1 = source[startPosition + 1]) << 6)
			+ (c2 = source[startPosition + 2]))
			% TableSize; 
	char[][] table = charArray_length[1][hash];
	int i = newEntry3;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]) && (c2 == charArray[2]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry3;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]) && (c2 == charArray[2]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2});
	newEntry3 = max;
	return r;
}
final char[] optimizedCurrentTokenSource4() {
	//try to return the same char[] build only once

	char c0, c1, c2, c3;
	long hash = 
		((((long) (c0 = source[startPosition])) << 18)
			+ ((c1 = source[startPosition + 1]) << 12)
			+ ((c2 = source[startPosition + 2]) << 6)
			+ (c3 = source[startPosition + 3]))
			% TableSize; 
	char[][] table = charArray_length[2][(int) hash];
	int i = newEntry4;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry4;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2, c3});
	newEntry4 = max;
	return r;
	
}
final char[] optimizedCurrentTokenSource5() {
	//try to return the same char[] build only once

	char c0, c1, c2, c3, c4;
	long hash = 
		((((long) (c0 = source[startPosition])) << 24)
			+ (((long) (c1 = source[startPosition + 1])) << 18)
			+ ((c2 = source[startPosition + 2]) << 12)
			+ ((c3 = source[startPosition + 3]) << 6)
			+ (c4 = source[startPosition + 4]))
			% TableSize; 
	char[][] table = charArray_length[3][(int) hash];
	int i = newEntry5;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry5;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2, c3, c4});
	newEntry5 = max;
	return r;
		
}
final char[] optimizedCurrentTokenSource6() {
	//try to return the same char[] build only once

	char c0, c1, c2, c3, c4, c5;
	long hash = 
		((((long) (c0 = source[startPosition])) << 32)
			+ (((long) (c1 = source[startPosition + 1])) << 24)
			+ (((long) (c2 = source[startPosition + 2])) << 18)
			+ ((c3 = source[startPosition + 3]) << 12)
			+ ((c4 = source[startPosition + 4]) << 6)
			+ (c5 = source[startPosition + 5]))
			% TableSize; 
	char[][] table = charArray_length[4][(int) hash];
	int i = newEntry6;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4])
			&& (c5 == charArray[5]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry6;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4])
			&& (c5 == charArray[5]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2, c3, c4, c5});
	newEntry6 = max;
	return r;	
}
public final void pushLineSeparator() throws InvalidInputException {
	//see comment on isLineDelimiter(char) for the use of '\n' and '\r'
	final int INCREMENT = 250;
	
	if (this.checkNonExternalizedStringLiterals) {
	// reinitialize the current line for non externalize strings purpose
		currentLine = null;
	}
	//currentCharacter is at position currentPosition-1

	// cr 000D
	if (currentCharacter == '\r') {
		int separatorPos = currentPosition - 1;
		if ((linePtr > 0) && (lineEnds[linePtr] >= separatorPos)) return;
		//System.out.println("CR-" + separatorPos);
		try {
			lineEnds[++linePtr] = separatorPos;
		} catch (IndexOutOfBoundsException e) {
			//linePtr value is correct
			int oldLength = lineEnds.length;
			int[] old = lineEnds;
			lineEnds = new int[oldLength + INCREMENT];
			System.arraycopy(old, 0, lineEnds, 0, oldLength);
			lineEnds[linePtr] = separatorPos;
		}
		// look-ahead for merged cr+lf
		try {
			if (source[currentPosition] == '\n') {
				//System.out.println("look-ahead LF-" + currentPosition);			
				lineEnds[linePtr] = currentPosition;
				currentPosition++;
				wasAcr = false;
			} else {
				wasAcr = true;
			}
		} catch(IndexOutOfBoundsException e) {
			wasAcr = true;
		}
	} else {
		// lf 000A
		if (currentCharacter == '\n') { //must merge eventual cr followed by lf
			if (wasAcr && (lineEnds[linePtr] == (currentPosition - 2))) {
				//System.out.println("merge LF-" + (currentPosition - 1));							
				lineEnds[linePtr] = currentPosition - 1;
			} else {
				int separatorPos = currentPosition - 1;
				if ((linePtr > 0) && (lineEnds[linePtr] >= separatorPos)) return;
				// System.out.println("LF-" + separatorPos);							
				try {
					lineEnds[++linePtr] = separatorPos;
				} catch (IndexOutOfBoundsException e) {
					//linePtr value is correct
					int oldLength = lineEnds.length;
					int[] old = lineEnds;
					lineEnds = new int[oldLength + INCREMENT];
					System.arraycopy(old, 0, lineEnds, 0, oldLength);
					lineEnds[linePtr] = separatorPos;
				}
			}
			wasAcr = false;
		}
	}
}
public final void pushUnicodeLineSeparator() {
	// isUnicode means that the \r or \n has been read as a unicode character
	
	//see comment on isLineDelimiter(char) for the use of '\n' and '\r'

	final int INCREMENT = 250;
	//currentCharacter is at position currentPosition-1

	if (this.checkNonExternalizedStringLiterals) {
	// reinitialize the current line for non externalize strings purpose
		currentLine = null;
	}
	
	// cr 000D
	if (currentCharacter == '\r') {
		int separatorPos = currentPosition - 6;
		if ((linePtr > 0) && (lineEnds[linePtr] >= separatorPos)) return;
		//System.out.println("CR-" + separatorPos);
		try {
			lineEnds[++linePtr] = separatorPos;
		} catch (IndexOutOfBoundsException e) {
			//linePtr value is correct
			int oldLength = lineEnds.length;
			int[] old = lineEnds;
			lineEnds = new int[oldLength + INCREMENT];
			System.arraycopy(old, 0, lineEnds, 0, oldLength);
			lineEnds[linePtr] = separatorPos;
		}
		// look-ahead for merged cr+lf
		if (source[currentPosition] == '\n') {
			//System.out.println("look-ahead LF-" + currentPosition);			
			lineEnds[linePtr] = currentPosition;
			currentPosition++;
			wasAcr = false;
		} else {
			wasAcr = true;
		}
	} else {
		// lf 000A
		if (currentCharacter == '\n') { //must merge eventual cr followed by lf
			if (wasAcr && (lineEnds[linePtr] == (currentPosition - 7))) {
				//System.out.println("merge LF-" + (currentPosition - 1));							
				lineEnds[linePtr] = currentPosition - 6;
			} else {
				int separatorPos = currentPosition - 6;
				if ((linePtr > 0) && (lineEnds[linePtr] >= separatorPos)) return;
				// System.out.println("LF-" + separatorPos);							
				try {
					lineEnds[++linePtr] = separatorPos;
				} catch (IndexOutOfBoundsException e) {
					//linePtr value is correct
					int oldLength = lineEnds.length;
					int[] old = lineEnds;
					lineEnds = new int[oldLength + INCREMENT];
					System.arraycopy(old, 0, lineEnds, 0, oldLength);
					lineEnds[linePtr] = separatorPos;
				}
			}
			wasAcr = false;
		}
	}
}
public final void recordComment(boolean isJavadoc) {

	// a new annotation comment is recorded
	try {
		commentStops[++commentPtr] = isJavadoc ? currentPosition : -currentPosition;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = commentStops.length;
		int[] oldStack = commentStops;
		commentStops = new int[oldStackLength + 30];
		System.arraycopy(oldStack, 0, commentStops, 0, oldStackLength);
		commentStops[commentPtr] = isJavadoc ? currentPosition : -currentPosition;
		//grows the positions buffers too
		int[] old = commentStarts;
		commentStarts = new int[oldStackLength + 30];
		System.arraycopy(old, 0, commentStarts, 0, oldStackLength);
	}

	//the buffer is of a correct size here
	commentStarts[commentPtr] = startPosition;
}
public void resetTo(int begin, int end) {
	//reset the scanner to a given position where it may rescan again

	diet = false;
	initialPosition = startPosition = currentPosition = begin;
	eofPosition = end < Integer.MAX_VALUE ? end + 1 : end;
	commentPtr = -1; // reset comment stack
}

public final void scanEscapeCharacter() throws InvalidInputException {
	// the string with "\\u" is a legal string of two chars \ and u
	//thus we use a direct access to the source (for regular cases).

	if (unicodeAsBackSlash) {
		// consume next character
		unicodeAsBackSlash = false;
		if (((currentCharacter = source[currentPosition++]) == '\\') && (source[currentPosition] == 'u')) {
			getNextUnicodeChar();
		} else {
			if (withoutUnicodePtr != 0) {
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			}
		}
	} else
		currentCharacter = source[currentPosition++];
	switch (currentCharacter) {
		case 'b' :
			currentCharacter = '\b';
			break;
		case 't' :
			currentCharacter = '\t';
			break;
		case 'n' :
			currentCharacter = '\n';
			break;
		case 'f' :
			currentCharacter = '\f';
			break;
		case 'r' :
			currentCharacter = '\r';
			break;
		case '\"' :
			currentCharacter = '\"';
			break;
		case '\'' :
			currentCharacter = '\'';
			break;
		case '\\' :
			currentCharacter = '\\';
			break;
		default :
			// -----------octal escape--------------
			// OctalDigit
			// OctalDigit OctalDigit
			// ZeroToThree OctalDigit OctalDigit

			int number = Character.getNumericValue(currentCharacter);
			if (number >= 0 && number <= 7) {
				boolean zeroToThreeNot = number > 3;
				if (Character.isDigit(currentCharacter = source[currentPosition++])) {
					int digit = Character.getNumericValue(currentCharacter);
					if (digit >= 0 && digit <= 7) {
						number = (number * 8) + digit;
						if (Character.isDigit(currentCharacter = source[currentPosition++])) {
							if (zeroToThreeNot) {// has read \NotZeroToThree OctalDigit Digit --> ignore last character
								currentPosition--;
							} else {
								digit = Character.getNumericValue(currentCharacter);
								if (digit >= 0 && digit <= 7){ // has read \ZeroToThree OctalDigit OctalDigit
									number = (number * 8) + digit;
								} else {// has read \ZeroToThree OctalDigit NonOctalDigit --> ignore last character
									currentPosition--;
								}
							}
						} else { // has read \OctalDigit NonDigit--> ignore last character
							currentPosition--;
						}
					} else { // has read \OctalDigit NonOctalDigit--> ignore last character						
						currentPosition--;
					}
				} else { // has read \OctalDigit --> ignore last character
					currentPosition--;
				}
				if (number > 255)
					throw new InvalidInputException(INVALID_ESCAPE);
				currentCharacter = (char) number;
			} else
				throw new InvalidInputException(INVALID_ESCAPE);
	}
}
public int scanIdentifierOrKeyword() throws InvalidInputException {
	//test keywords

	//first dispatch on the first char.
	//then the length. If there are several
	//keywors with the same length AND the same first char, then do another
	//disptach on the second char :-)...cool....but fast !
	useAssertAsAnIndentifier = false;
	while (getNextCharAsJavaIdentifierPart()) {};

	int index, length;
	char[] data;
	char firstLetter;
	if (withoutUnicodePtr == 0)

		//quick test on length == 1 but not on length > 12 while most identifier
		//have a length which is <= 12...but there are lots of identifier with
		//only one char....

		{
		if ((length = currentPosition - startPosition) == 1)
			return TokenNameIdentifier;
		data = source;
		index = startPosition;
	} else {
		if ((length = withoutUnicodePtr) == 1)
			return TokenNameIdentifier;
		data = withoutUnicodeBuffer;
		index = 1;
	}

	firstLetter = data[index];
	switch (firstLetter) {

		case 'a' : 
			switch(length) {
				case 8: //abstract
					if ((data[++index] == 'b')
						&& (data[++index] == 's')
						&& (data[++index] == 't')
						&& (data[++index] == 'r')
						&& (data[++index] == 'a')
						&& (data[++index] == 'c')
						&& (data[++index] == 't')) {
							return TokenNameabstract;
						} else {
							return TokenNameIdentifier;
						}
				case 6: // assert
					if ((data[++index] == 's')
						&& (data[++index] == 's')
						&& (data[++index] == 'e')
						&& (data[++index] == 'r')
						&& (data[++index] == 't')) {
							if (assertMode) {
								containsAssertKeyword = true;
								return TokenNameassert;
							} else {
								useAssertAsAnIndentifier = true;
								return TokenNameIdentifier;								
							}
						} else {
							return TokenNameIdentifier;
						}
				default: 
					return TokenNameIdentifier;
			}
		case 'b' : //boolean break byte
			switch (length) {
				case 4 :
					if ((data[++index] == 'y') && (data[++index] == 't') && (data[++index] == 'e'))
						return TokenNamebyte;
					else
						return TokenNameIdentifier;
				case 5 :
					if ((data[++index] == 'r')
						&& (data[++index] == 'e')
						&& (data[++index] == 'a')
						&& (data[++index] == 'k'))
						return TokenNamebreak;
					else
						return TokenNameIdentifier;
				case 7 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'o')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e')
						&& (data[++index] == 'a')
						&& (data[++index] == 'n'))
						return TokenNameboolean;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'c' : //case char catch const class continue
			switch (length) {
				case 4 :
					if (data[++index] == 'a')
						if ((data[++index] == 's') && (data[++index] == 'e'))
							return TokenNamecase;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'h') && (data[++index] == 'a') && (data[++index] == 'r'))
							return TokenNamechar;
						else
							return TokenNameIdentifier;
				case 5 :
					if (data[++index] == 'a')
						if ((data[++index] == 't') && (data[++index] == 'c') && (data[++index] == 'h'))
							return TokenNamecatch;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'l')
							&& (data[++index] == 'a')
							&& (data[++index] == 's')
							&& (data[++index] == 's'))
							return TokenNameclass;
						else
							if ((data[index] == 'o')
								&& (data[++index] == 'n')
								&& (data[++index] == 's')
								&& (data[++index] == 't'))
								return TokenNameERROR; //const is not used in java ???????
					else
						return TokenNameIdentifier;
				case 8 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'n')
						&& (data[++index] == 't')
						&& (data[++index] == 'i')
						&& (data[++index] == 'n')
						&& (data[++index] == 'u')
						&& (data[++index] == 'e'))
						return TokenNamecontinue;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'd' : //default do double
			switch (length) {
				case 2 :
					if ((data[++index] == 'o'))
						return TokenNamedo;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'u')
						&& (data[++index] == 'b')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e'))
						return TokenNamedouble;
					else
						return TokenNameIdentifier;
				case 7 :
					if ((data[++index] == 'e')
						&& (data[++index] == 'f')
						&& (data[++index] == 'a')
						&& (data[++index] == 'u')
						&& (data[++index] == 'l')
						&& (data[++index] == 't'))
						return TokenNamedefault;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}
		case 'e' : //else extends
			switch (length) {
				case 4 :
					if ((data[++index] == 'l') && (data[++index] == 's') && (data[++index] == 'e'))
						return TokenNameelse;
					else
						return TokenNameIdentifier;
				case 7 :
					if ((data[++index] == 'x')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'n')
						&& (data[++index] == 'd')
						&& (data[++index] == 's'))
						return TokenNameextends;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'f' : //final finally float for false
			switch (length) {
				case 3 :
					if ((data[++index] == 'o') && (data[++index] == 'r'))
						return TokenNamefor;
					else
						return TokenNameIdentifier;
				case 5 :
					if (data[++index] == 'i')
						if ((data[++index] == 'n')
							&& (data[++index] == 'a')
							&& (data[++index] == 'l')) {
							return TokenNamefinal;
						} else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'l')
							&& (data[++index] == 'o')
							&& (data[++index] == 'a')
							&& (data[++index] == 't'))
							return TokenNamefloat;
						else
							if ((data[index] == 'a')
								&& (data[++index] == 'l')
								&& (data[++index] == 's')
								&& (data[++index] == 'e'))
								return TokenNamefalse;
							else
								return TokenNameIdentifier;
				case 7 :
					if ((data[++index] == 'i')
						&& (data[++index] == 'n')
						&& (data[++index] == 'a')
						&& (data[++index] == 'l')
						&& (data[++index] == 'l')
						&& (data[++index] == 'y'))
						return TokenNamefinally;
					else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}
		case 'g' : //goto
			if (length == 4) {
				if ((data[++index] == 'o')
					&& (data[++index] == 't')
					&& (data[++index] == 'o')) {
					return TokenNameERROR;
				}
			} //no goto in java are allowed, so why java removes this keyword ???
			return TokenNameIdentifier;

		case 'i' : //if implements import instanceof int interface
			switch (length) {
				case 2 :
					if (data[++index] == 'f')
						return TokenNameif;
					else
						return TokenNameIdentifier;
				case 3 :
					if ((data[++index] == 'n') && (data[++index] == 't'))
						return TokenNameint;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'm')
						&& (data[++index] == 'p')
						&& (data[++index] == 'o')
						&& (data[++index] == 'r')
						&& (data[++index] == 't'))
						return TokenNameimport;
					else
						return TokenNameIdentifier;
				case 9 :
					if ((data[++index] == 'n')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'r')
						&& (data[++index] == 'f')
						&& (data[++index] == 'a')
						&& (data[++index] == 'c')
						&& (data[++index] == 'e'))
						return TokenNameinterface;
					else
						return TokenNameIdentifier;
				case 10 :
					if (data[++index] == 'm')
						if ((data[++index] == 'p')
							&& (data[++index] == 'l')
							&& (data[++index] == 'e')
							&& (data[++index] == 'm')
							&& (data[++index] == 'e')
							&& (data[++index] == 'n')
							&& (data[++index] == 't')
							&& (data[++index] == 's'))
							return TokenNameimplements;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'n')
							&& (data[++index] == 's')
							&& (data[++index] == 't')
							&& (data[++index] == 'a')
							&& (data[++index] == 'n')
							&& (data[++index] == 'c')
							&& (data[++index] == 'e')
							&& (data[++index] == 'o')
							&& (data[++index] == 'f'))
							return TokenNameinstanceof;
						else
							return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'l' : //long
			if (length == 4) {
				if ((data[++index] == 'o')
					&& (data[++index] == 'n')
					&& (data[++index] == 'g')) {
					return TokenNamelong;
				}
			}
			return TokenNameIdentifier;

		case 'n' : //native new null
			switch (length) {
				case 3 :
					if ((data[++index] == 'e') && (data[++index] == 'w'))
						return TokenNamenew;
					else
						return TokenNameIdentifier;
				case 4 :
					if ((data[++index] == 'u') && (data[++index] == 'l') && (data[++index] == 'l'))
						return TokenNamenull;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'a')
						&& (data[++index] == 't')
						&& (data[++index] == 'i')
						&& (data[++index] == 'v')
						&& (data[++index] == 'e')) {
						return TokenNamenative;
					} else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'p' : //package private protected public
			switch (length) {
				case 6 :
					if ((data[++index] == 'u')
						&& (data[++index] == 'b')
						&& (data[++index] == 'l')
						&& (data[++index] == 'i')
						&& (data[++index] == 'c')) {
						return TokenNamepublic;
					} else
						return TokenNameIdentifier;
				case 7 :
					if (data[++index] == 'a')
						if ((data[++index] == 'c')
							&& (data[++index] == 'k')
							&& (data[++index] == 'a')
							&& (data[++index] == 'g')
							&& (data[++index] == 'e'))
							return TokenNamepackage;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'r')
							&& (data[++index] == 'i')
							&& (data[++index] == 'v')
							&& (data[++index] == 'a')
							&& (data[++index] == 't')
							&& (data[++index] == 'e')) {
							return TokenNameprivate;
						} else
							return TokenNameIdentifier;
				case 9 :
					if ((data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'c')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'd')) {
						return TokenNameprotected;
					} else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'r' : //return
			if (length == 6) {
				if ((data[++index] == 'e')
					&& (data[++index] == 't')
					&& (data[++index] == 'u')
					&& (data[++index] == 'r')
					&& (data[++index] == 'n')) {
					return TokenNamereturn;
				}
			}
			return TokenNameIdentifier;

		case 's' : //short static super switch synchronized strictfp
			switch (length) {
				case 5 :
					if (data[++index] == 'h')
						if ((data[++index] == 'o') && (data[++index] == 'r') && (data[++index] == 't'))
							return TokenNameshort;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'u')
							&& (data[++index] == 'p')
							&& (data[++index] == 'e')
							&& (data[++index] == 'r'))
							return TokenNamesuper;
						else
							return TokenNameIdentifier;

				case 6 :
					if (data[++index] == 't')
						if ((data[++index] == 'a')
							&& (data[++index] == 't')
							&& (data[++index] == 'i')
							&& (data[++index] == 'c')) {
							return TokenNamestatic;
						} else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'w')
							&& (data[++index] == 'i')
							&& (data[++index] == 't')
							&& (data[++index] == 'c')
							&& (data[++index] == 'h'))
							return TokenNameswitch;
						else
							return TokenNameIdentifier;
				case 8 :
					if ((data[++index] == 't')
						&& (data[++index] == 'r')
						&& (data[++index] == 'i')
						&& (data[++index] == 'c')
						&& (data[++index] == 't')
						&& (data[++index] == 'f')
						&& (data[++index] == 'p'))
						return TokenNamestrictfp;
					else
						return TokenNameIdentifier;
				case 12 :
					if ((data[++index] == 'y')
						&& (data[++index] == 'n')
						&& (data[++index] == 'c')
						&& (data[++index] == 'h')
						&& (data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 'n')
						&& (data[++index] == 'i')
						&& (data[++index] == 'z')
						&& (data[++index] == 'e')
						&& (data[++index] == 'd')) {
						return TokenNamesynchronized;
					} else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 't' : //try throw throws transient this true
			switch (length) {
				case 3 :
					if ((data[++index] == 'r') && (data[++index] == 'y'))
						return TokenNametry;
					else
						return TokenNameIdentifier;
				case 4 :
					if ((data[++index] == 'h') && (data[++index] == 'i') && (data[++index] == 's'))
						return TokenNamethis;
					else
						if ((data[index] == 'r') && (data[++index] == 'u') && (data[++index] == 'e'))
							return TokenNametrue;
						else
							return TokenNameIdentifier;
				case 5 :
					if ((data[++index] == 'h')
						&& (data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 'w'))
						return TokenNamethrow;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'h')
						&& (data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 'w')
						&& (data[++index] == 's'))
						return TokenNamethrows;
					else
						return TokenNameIdentifier;
				case 9 :
					if ((data[++index] == 'r')
						&& (data[++index] == 'a')
						&& (data[++index] == 'n')
						&& (data[++index] == 's')
						&& (data[++index] == 'i')
						&& (data[++index] == 'e')
						&& (data[++index] == 'n')
						&& (data[++index] == 't')) {
						return TokenNametransient;
					} else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'v' : //void volatile
			switch (length) {
				case 4 :
					if ((data[++index] == 'o') && (data[++index] == 'i') && (data[++index] == 'd'))
						return TokenNamevoid;
					else
						return TokenNameIdentifier;
				case 8 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'l')
						&& (data[++index] == 'a')
						&& (data[++index] == 't')
						&& (data[++index] == 'i')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e')) {
						return TokenNamevolatile;
					} else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'w' : //while widefp
			switch (length) {
				case 5 :
					if ((data[++index] == 'h')
						&& (data[++index] == 'i')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e'))
						return TokenNamewhile;
					else
						return TokenNameIdentifier;
					//case 6:if ( (data[++index] =='i') && (data[++index]=='d') && (data[++index]=='e') && (data[++index]=='f')&& (data[++index]=='p'))
					//return TokenNamewidefp ;
					//else
					//return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		default :
			return TokenNameIdentifier;
	}
}
public int scanNumber(boolean dotPrefix) throws InvalidInputException {

	//when entering this method the currentCharacter is the firt
	//digit of the number , i.e. it may be preceeded by a . when
	//dotPrefix is true

	boolean floating = dotPrefix;
	if ((!dotPrefix) && (currentCharacter == '0')) {
		if (getNextChar('x', 'X') >= 0) { //----------hexa-----------------
			//force the first char of the hexa number do exist...
			// consume next character
			unicodeAsBackSlash = false;
			if (((currentCharacter = source[currentPosition++]) == '\\')
				&& (source[currentPosition] == 'u')) {
				getNextUnicodeChar();
			} else {
				if (withoutUnicodePtr != 0) {
					withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
				}
			}
			if (Character.digit(currentCharacter, 16) == -1)
				throw new InvalidInputException(INVALID_HEXA);
			//---end forcing--
			while (getNextCharAsDigit(16)) {};
			if (getNextChar('l', 'L') >= 0)
				return TokenNameLongLiteral;
			else
				return TokenNameIntegerLiteral;
		}

		//there is x or X in the number
		//potential octal ! ... some one may write 000099.0 ! thus 00100 < 00078.0 is true !!!!! crazy language
		if (getNextCharAsDigit()) { //-------------potential octal-----------------
			while (getNextCharAsDigit()) {};

			if (getNextChar('l', 'L') >= 0) {
				return TokenNameLongLiteral;
			}

			if (getNextChar('f', 'F') >= 0) {
				return TokenNameFloatingPointLiteral;
			}

			if (getNextChar('d', 'D') >= 0) {
				return TokenNameDoubleLiteral;
			} else { //make the distinction between octal and float ....
				if (getNextChar('.')) { //bingo ! ....
					while (getNextCharAsDigit()) {};
					if (getNextChar('e', 'E') >= 0) { // consume next character
						unicodeAsBackSlash = false;
						if (((currentCharacter = source[currentPosition++]) == '\\')
							&& (source[currentPosition] == 'u')) {
							getNextUnicodeChar();
						} else {
							if (withoutUnicodePtr != 0) {
								withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
							}
						}

						if ((currentCharacter == '-')
							|| (currentCharacter == '+')) { // consume next character
							unicodeAsBackSlash = false;
							if (((currentCharacter = source[currentPosition++]) == '\\')
								&& (source[currentPosition] == 'u')) {
								getNextUnicodeChar();
							} else {
								if (withoutUnicodePtr != 0) {
									withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
								}
							}
						}
						if (!Character.isDigit(currentCharacter))
							throw new InvalidInputException(INVALID_FLOAT);
						while (getNextCharAsDigit()) {};
					}
					if (getNextChar('f', 'F') >= 0)
						return TokenNameFloatingPointLiteral;
					getNextChar('d', 'D'); //jump over potential d or D
					return TokenNameDoubleLiteral;
				} else {
					return TokenNameIntegerLiteral;
				}
			}
		} else {
			/* carry on */
		}
	}

	while (getNextCharAsDigit()) {};

	if ((!dotPrefix) && (getNextChar('l', 'L') >= 0))
		return TokenNameLongLiteral;

	if ((!dotPrefix) && (getNextChar('.'))) { //decimal part that can be empty
		while (getNextCharAsDigit()) {};
		floating = true;
	}

	//if floating is true both exponant and suffix may be optional

	if (getNextChar('e', 'E') >= 0) {
		floating = true;
		// consume next character
		unicodeAsBackSlash = false;
		if (((currentCharacter = source[currentPosition++]) == '\\')
			&& (source[currentPosition] == 'u')) {
			getNextUnicodeChar();
		} else {
			if (withoutUnicodePtr != 0) {
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			}
		}

		if ((currentCharacter == '-')
			|| (currentCharacter == '+')) { // consume next character
			unicodeAsBackSlash = false;
			if (((currentCharacter = source[currentPosition++]) == '\\')
				&& (source[currentPosition] == 'u')) {
				getNextUnicodeChar();
			} else {
				if (withoutUnicodePtr != 0) {
					withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
				}
			}
		}
		if (!Character.isDigit(currentCharacter))
			throw new InvalidInputException(INVALID_FLOAT);
		while (getNextCharAsDigit()) {};
	}

	if (getNextChar('d', 'D') >= 0)
		return TokenNameDoubleLiteral;
	if (getNextChar('f', 'F') >= 0)
		return TokenNameFloatingPointLiteral;

	//the long flag has been tested before

	return floating ? TokenNameDoubleLiteral : TokenNameIntegerLiteral;
}
/**
 * Search the line number corresponding to a specific position
 *
 */
public final int getLineNumber(int position) {

	if (lineEnds == null)
		return 1;
	int length = linePtr+1;
	if (length == 0)
		return 1;
	int g = 0, d = length - 1;
	int m = 0;
	while (g <= d) {
		m = (g + d) /2;
		if (position < lineEnds[m]) {
			d = m-1;
		} else if (position > lineEnds[m]) {
			g = m+1;
		} else {
			return m + 1;
		}
	}
	if (position < lineEnds[m]) {
		return m+1;
	}
	return m+2;
}
public final void setSource(char[] source){
	//the source-buffer is set to sourceString

	if (source == null) {
		this.source = new char[0];
	} else {
		this.source = source;
	}
	startPosition = -1;
	initialPosition = currentPosition = 0;
	containsAssertKeyword = false;
	withoutUnicodeBuffer = new char[this.source.length];

}

public String toString() {
	if (startPosition == source.length)
		return "EOF\n\n" + new String(source); //$NON-NLS-1$
	if (currentPosition > source.length)
		return "behind the EOF :-( ....\n\n" + new String(source); //$NON-NLS-1$

	char front[] = new char[startPosition];
	System.arraycopy(source, 0, front, 0, startPosition);

	int middleLength = (currentPosition - 1) - startPosition + 1;
	char middle[];
	if (middleLength > -1) {
		middle = new char[middleLength];
		System.arraycopy(
			source, 
			startPosition, 
			middle, 
			0, 
			middleLength);
	} else {
		middle = new char[0];
	}
	
	char end[] = new char[source.length - (currentPosition - 1)];
	System.arraycopy(
		source, 
		(currentPosition - 1) + 1, 
		end, 
		0, 
		source.length - (currentPosition - 1) - 1);
	
	return new String(front)
		+ "\n===============================\nStarts here -->" //$NON-NLS-1$
		+ new String(middle)
		+ "<-- Ends here\n===============================\n" //$NON-NLS-1$
		+ new String(end); 
}
public final String toStringAction(int act) {
	switch (act) {
		case TokenNameIdentifier :
			return "Identifier(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameabstract :
			return "abstract"; //$NON-NLS-1$
		case TokenNameboolean :
			return "boolean"; //$NON-NLS-1$
		case TokenNamebreak :
			return "break"; //$NON-NLS-1$
		case TokenNamebyte :
			return "byte"; //$NON-NLS-1$
		case TokenNamecase :
			return "case"; //$NON-NLS-1$
		case TokenNamecatch :
			return "catch"; //$NON-NLS-1$
		case TokenNamechar :
			return "char"; //$NON-NLS-1$
		case TokenNameclass :
			return "class"; //$NON-NLS-1$
		case TokenNamecontinue :
			return "continue"; //$NON-NLS-1$
		case TokenNamedefault :
			return "default"; //$NON-NLS-1$
		case TokenNamedo :
			return "do"; //$NON-NLS-1$
		case TokenNamedouble :
			return "double"; //$NON-NLS-1$
		case TokenNameelse :
			return "else"; //$NON-NLS-1$
		case TokenNameextends :
			return "extends"; //$NON-NLS-1$
		case TokenNamefalse :
			return "false"; //$NON-NLS-1$
		case TokenNamefinal :
			return "final"; //$NON-NLS-1$
		case TokenNamefinally :
			return "finally"; //$NON-NLS-1$
		case TokenNamefloat :
			return "float"; //$NON-NLS-1$
		case TokenNamefor :
			return "for"; //$NON-NLS-1$
		case TokenNameif :
			return "if"; //$NON-NLS-1$
		case TokenNameimplements :
			return "implements"; //$NON-NLS-1$
		case TokenNameimport :
			return "import"; //$NON-NLS-1$
		case TokenNameinstanceof :
			return "instanceof"; //$NON-NLS-1$
		case TokenNameint :
			return "int"; //$NON-NLS-1$
		case TokenNameinterface :
			return "interface"; //$NON-NLS-1$
		case TokenNamelong :
			return "long"; //$NON-NLS-1$
		case TokenNamenative :
			return "native"; //$NON-NLS-1$
		case TokenNamenew :
			return "new"; //$NON-NLS-1$
		case TokenNamenull :
			return "null"; //$NON-NLS-1$
		case TokenNamepackage :
			return "package"; //$NON-NLS-1$
		case TokenNameprivate :
			return "private"; //$NON-NLS-1$
		case TokenNameprotected :
			return "protected"; //$NON-NLS-1$
		case TokenNamepublic :
			return "public"; //$NON-NLS-1$
		case TokenNamereturn :
			return "return"; //$NON-NLS-1$
		case TokenNameshort :
			return "short"; //$NON-NLS-1$
		case TokenNamestatic :
			return "static"; //$NON-NLS-1$
		case TokenNamesuper :
			return "super"; //$NON-NLS-1$
		case TokenNameswitch :
			return "switch"; //$NON-NLS-1$
		case TokenNamesynchronized :
			return "synchronized"; //$NON-NLS-1$
		case TokenNamethis :
			return "this"; //$NON-NLS-1$
		case TokenNamethrow :
			return "throw"; //$NON-NLS-1$
		case TokenNamethrows :
			return "throws"; //$NON-NLS-1$
		case TokenNametransient :
			return "transient"; //$NON-NLS-1$
		case TokenNametrue :
			return "true"; //$NON-NLS-1$
		case TokenNametry :
			return "try"; //$NON-NLS-1$
		case TokenNamevoid :
			return "void"; //$NON-NLS-1$
		case TokenNamevolatile :
			return "volatile"; //$NON-NLS-1$
		case TokenNamewhile :
			return "while"; //$NON-NLS-1$

		case TokenNameIntegerLiteral :
			return "Integer(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameLongLiteral :
			return "Long(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameFloatingPointLiteral :
			return "Float(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameDoubleLiteral :
			return "Double(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameCharacterLiteral :
			return "Char(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameStringLiteral :
			return "String(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$

		case TokenNamePLUS_PLUS :
			return "++"; //$NON-NLS-1$
		case TokenNameMINUS_MINUS :
			return "--"; //$NON-NLS-1$
		case TokenNameEQUAL_EQUAL :
			return "=="; //$NON-NLS-1$
		case TokenNameLESS_EQUAL :
			return "<="; //$NON-NLS-1$
		case TokenNameGREATER_EQUAL :
			return ">="; //$NON-NLS-1$
		case TokenNameNOT_EQUAL :
			return "!="; //$NON-NLS-1$
		case TokenNameLEFT_SHIFT :
			return "<<"; //$NON-NLS-1$
		case TokenNameRIGHT_SHIFT :
			return ">>"; //$NON-NLS-1$
		case TokenNameUNSIGNED_RIGHT_SHIFT :
			return ">>>"; //$NON-NLS-1$
		case TokenNamePLUS_EQUAL :
			return "+="; //$NON-NLS-1$
		case TokenNameMINUS_EQUAL :
			return "-="; //$NON-NLS-1$
		case TokenNameMULTIPLY_EQUAL :
			return "*="; //$NON-NLS-1$
		case TokenNameDIVIDE_EQUAL :
			return "/="; //$NON-NLS-1$
		case TokenNameAND_EQUAL :
			return "&="; //$NON-NLS-1$
		case TokenNameOR_EQUAL :
			return "|="; //$NON-NLS-1$
		case TokenNameXOR_EQUAL :
			return "^="; //$NON-NLS-1$
		case TokenNameREMAINDER_EQUAL :
			return "%="; //$NON-NLS-1$
		case TokenNameLEFT_SHIFT_EQUAL :
			return "<<="; //$NON-NLS-1$
		case TokenNameRIGHT_SHIFT_EQUAL :
			return ">>="; //$NON-NLS-1$
		case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL :
			return ">>>="; //$NON-NLS-1$
		case TokenNameOR_OR :
			return "||"; //$NON-NLS-1$
		case TokenNameAND_AND :
			return "&&"; //$NON-NLS-1$
		case TokenNamePLUS :
			return "+"; //$NON-NLS-1$
		case TokenNameMINUS :
			return "-"; //$NON-NLS-1$
		case TokenNameNOT :
			return "!"; //$NON-NLS-1$
		case TokenNameREMAINDER :
			return "%"; //$NON-NLS-1$
		case TokenNameXOR :
			return "^"; //$NON-NLS-1$
		case TokenNameAND :
			return "&"; //$NON-NLS-1$
		case TokenNameMULTIPLY :
			return "*"; //$NON-NLS-1$
		case TokenNameOR :
			return "|"; //$NON-NLS-1$
		case TokenNameTWIDDLE :
			return "~"; //$NON-NLS-1$
		case TokenNameDIVIDE :
			return "/"; //$NON-NLS-1$
		case TokenNameGREATER :
			return ">"; //$NON-NLS-1$
		case TokenNameLESS :
			return "<"; //$NON-NLS-1$
		case TokenNameLPAREN :
			return "("; //$NON-NLS-1$
		case TokenNameRPAREN :
			return ")"; //$NON-NLS-1$
		case TokenNameLBRACE :
			return "{"; //$NON-NLS-1$
		case TokenNameRBRACE :
			return "}"; //$NON-NLS-1$
		case TokenNameLBRACKET :
			return "["; //$NON-NLS-1$
		case TokenNameRBRACKET :
			return "]"; //$NON-NLS-1$
		case TokenNameSEMICOLON :
			return ";"; //$NON-NLS-1$
		case TokenNameQUESTION :
			return "?"; //$NON-NLS-1$
		case TokenNameCOLON :
			return ":"; //$NON-NLS-1$
		case TokenNameCOMMA :
			return ","; //$NON-NLS-1$
		case TokenNameDOT :
			return "."; //$NON-NLS-1$
		case TokenNameEQUAL :
			return "="; //$NON-NLS-1$
		case TokenNameEOF :
			return "EOF"; //$NON-NLS-1$
		default :
			return "not-a-token"; //$NON-NLS-1$
	}
}

public Scanner(boolean tokenizeComments, boolean tokenizeWhiteSpace, boolean checkNonExternalizedStringLiterals) {
	this(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals, false);
}

public Scanner(boolean tokenizeComments, boolean tokenizeWhiteSpace, boolean checkNonExternalizedStringLiterals, boolean assertMode) {
	this.eofPosition = Integer.MAX_VALUE;
	this.tokenizeComments = tokenizeComments;
	this.tokenizeWhiteSpace = tokenizeWhiteSpace;
	this.checkNonExternalizedStringLiterals = checkNonExternalizedStringLiterals;
	this.assertMode = assertMode;
}

private void checkNonExternalizeString()  throws InvalidInputException {
	if (currentLine == null)
		return;
	parseTags(currentLine);
}

private void parseTags(NLSLine line) throws InvalidInputException {
	String s = new String(getCurrentTokenSource());
	int pos = s.indexOf(TAG_PREFIX);
	int lineLength = line.size();
	while (pos != -1) {
		int start = pos + TAG_PREFIX_LENGTH;
		int end = s.indexOf(TAG_POSTFIX, start);
		String index = s.substring(start, end);
		int i = 0;
		try {
			i = Integer.parseInt(index) - 1; // Tags are one based not zero based.
		} catch (NumberFormatException e) {
			i = -1; // we don't want to consider this as a valid NLS tag
		}
		if (line.exists(i)) {
			line.set(i, null);
		}
		pos = s.indexOf(TAG_PREFIX, start);
	}

	this.nonNLSStrings = new StringLiteral[lineLength];
	int nonNLSCounter = 0;
	for (Iterator iterator = line.iterator(); iterator.hasNext(); ) {
		StringLiteral literal = (StringLiteral) iterator.next();
		if (literal != null) {
			this.nonNLSStrings[nonNLSCounter++] = literal;
		}
	}
	if (nonNLSCounter == 0) {
		this.nonNLSStrings = null;
		currentLine = null;
		return;
	} 
	this.wasNonExternalizedStringLiteral = true;
	if (nonNLSCounter != lineLength) {
		System.arraycopy(this.nonNLSStrings, 0, (this.nonNLSStrings = new StringLiteral[nonNLSCounter]), 0, nonNLSCounter);
	}
	currentLine = null;
}
}
