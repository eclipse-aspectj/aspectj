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
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Scanner aware of a cursor location so as to discard trailing portions of identifiers
 * containing the cursor location.
 *
 * Cursor location denotes the position of the last character behind which completion
 * got requested:
 *  -1 means completion at the very beginning of the source
 *	0  means completion behind the first character
 *  n  means completion behind the n-th character
 */
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

public class CompletionScanner extends Scanner {

	public char[] completionIdentifier;
	public int cursorLocation;
		
	/* Source positions of the completedIdentifier
	 * if inside actual identifier, end goes to the actual identifier 
	 * end, i.e. beyond cursor location
	 */
	public int completedIdentifierStart = 0;
	public int completedIdentifierEnd = -1;

	public static final char[] EmptyCompletionIdentifier = {};
public CompletionScanner(boolean assertMode) {
	super(false, false, false, assertMode);
}
/* 
 * Truncate the current identifier if it is containing the cursor location. Since completion is performed
 * on an identifier prefix.
 *
 */
public char[] getCurrentIdentifierSource() {

	if (completionIdentifier == null){
		if (cursorLocation < startPosition && currentPosition == startPosition){ // fake empty identifier got issued
			// remember actual identifier positions
			completedIdentifierStart = startPosition;
			completedIdentifierEnd = completedIdentifierStart - 1;			
			return completionIdentifier = EmptyCompletionIdentifier;					
		}
		if (cursorLocation+1 >= startPosition && cursorLocation < currentPosition){
			// remember actual identifier positions
			completedIdentifierStart = startPosition;
			completedIdentifierEnd = currentPosition - 1;
			if (withoutUnicodePtr != 0){			// check unicode scenario
				System.arraycopy(withoutUnicodeBuffer, 1, completionIdentifier = new char[withoutUnicodePtr], 0, withoutUnicodePtr);
			} else {
				int length = cursorLocation + 1 - startPosition;
				// no char[] sharing around completionIdentifier, we want it to be unique so as to use identity checks	
				System.arraycopy(source, startPosition, (completionIdentifier = new char[length]), 0, length);
			}
			return completionIdentifier;
		}
	}
	return super.getCurrentIdentifierSource();
}
/* 
 * Identifier splitting for unicodes.
 * Only store the current unicode if we did not pass the cursorLocation.
 * Note: this does not handle cases where the cursor is in the middle of a unicode
 */
public boolean getNextCharAsJavaIdentifierPart() {

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
			if (temp < cursorLocation && cursorLocation < currentPosition-1){
				throw new InvalidCursorLocation(InvalidCursorLocation.NO_COMPLETION_INSIDE_UNICODE);
			}
			// store the current unicode, only if we did not pass the cursorLocation
			// Note: this does not handle cases where the cursor is in the middle of a unicode
			if ((completionIdentifier != null)
				|| (startPosition <= cursorLocation+1 && cursorLocation >= currentPosition-1)){
				withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
			}
			return true;
		} //-------------end unicode traitement--------------
		else {
			if (!Character.isJavaIdentifierPart(currentCharacter)) {
				currentPosition = temp;
				return false;
			}

			if (withoutUnicodePtr != 0){
				// store the current unicode, only if we did not pass the cursorLocation
				// Note: this does not handle cases where the cursor is in the middle of a unicode
				if ((completionIdentifier != null)
					|| (startPosition <= cursorLocation+1 && cursorLocation >= currentPosition-1)){
					withoutUnicodeBuffer[++withoutUnicodePtr] = currentCharacter;
				}
			}
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
					if (recordLineSeparator
						&& ((currentCharacter == '\r') || (currentCharacter == '\n')))
						pushLineSeparator();
					isWhiteSpace = 
						(currentCharacter == ' ') || Character.isWhitespace(currentCharacter); 
				}
				/* completion requesting strictly inside blanks */
				if ((whiteStart != currentPosition)
					//&& (previousToken == TokenNameDOT)
					&& (completionIdentifier == null)
					&& (whiteStart <= cursorLocation+1)
					&& (cursorLocation < startPosition)
					&& !Character.isJavaIdentifierStart(currentCharacter)){
					currentPosition = startPosition; // for next token read
					return TokenNameIdentifier;
				}
			} while (isWhiteSpace);
			if (tokenizeWhiteSpace && (whiteStart != currentPosition - 1)) {
				// reposition scanner in case we are interested by spaces as tokens
				currentPosition--;
				startPosition = whiteStart;
				return TokenNameWHITESPACE;
			}
			//little trick to get out in the middle of a source computation
			if (currentPosition > eofPosition){
				/* might be completing at eof (e.g. behind a dot) */
				if (completionIdentifier == null && 
					startPosition == cursorLocation + 1){
					currentPosition = startPosition; // for being detected as empty free identifier
					return TokenNameIdentifier;
				}				
				return TokenNameEOF;
			}

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
					if (startPosition <= cursorLocation 
					    && cursorLocation < currentPosition){
					    	return TokenNameDOT; // completion inside .<|>12
				    }
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
					if (startPosition <= cursorLocation && cursorLocation <= currentPosition-1){
						throw new InvalidCursorLocation(InvalidCursorLocation.NO_COMPLETION_INSIDE_STRING);
					}
					return TokenNameStringLiteral;
				case '/' :
					{
						int test;
						if ((test = getNextChar('/', '*')) == 0) { //line comment 
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
								recordComment(false);
								if (startPosition <= cursorLocation && cursorLocation < currentPosition-1){
									throw new InvalidCursorLocation(InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
								}
								if (recordLineSeparator
									&& ((currentCharacter == '\r') || (currentCharacter == '\n')))
									pushLineSeparator();
								if (tokenizeComments) {
									currentPosition--; // reset one character behind
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
								// empty comment is not a javadoc /**/
								if (currentCharacter == '/') { 
									isJavadoc = false;
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
								recordComment(isJavadoc);
								if (startPosition <= cursorLocation && cursorLocation < currentPosition-1){
									throw new InvalidCursorLocation(InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
								}
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
	/* might be completing at very end of file (e.g. behind a dot) */
	if (completionIdentifier == null && 
		startPosition == cursorLocation + 1){
		currentPosition = startPosition; // for being detected as empty free identifier
		return TokenNameIdentifier;
	}
	return TokenNameEOF;
}
/*
 * In case we actually read a keyword, but the cursor is located inside,
 * we pretend we read an identifier.
 */
public int scanIdentifierOrKeyword() throws InvalidInputException {

	int id = super.scanIdentifierOrKeyword();

	// convert completed keyword into an identifier
	if (id != TokenNameIdentifier
		&& startPosition <= cursorLocation+1 
		&& cursorLocation < currentPosition){
		return TokenNameIdentifier;
	}
	return id;
}
public int scanNumber(boolean dotPrefix) throws InvalidInputException {
	
	int token = super.scanNumber(dotPrefix);

	// consider completion just before a number to be ok, will insert before it
	if (startPosition <= cursorLocation && cursorLocation < currentPosition){  
		throw new InvalidCursorLocation(InvalidCursorLocation.NO_COMPLETION_INSIDE_NUMBER);
	}
	return token;
}
}
