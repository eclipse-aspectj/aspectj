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
package org.eclipse.jdt.internal.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.ICodeFormatter;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ConfigurableOption;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.formatter.impl.FormatterOptions;
import org.eclipse.jdt.internal.formatter.impl.SplitLine;

/** <h2>How to format a piece of code ?</h2>
 * <ul><li>Create an instance of <code>CodeFormatter</code>
 * <li>Use the method <code>void format(aString)</code>
 * on this instance to format <code>aString</code>.
 * It will return the formatted string.</ul>
*/
public class CodeFormatter implements ITerminalSymbols, ICodeFormatter {

	public FormatterOptions options;

	/** 
	 * Represents a block in the <code>constructions</code> stack.
	 */
	public static final int BLOCK = ITerminalSymbols.TokenNameLBRACE;

	/** 
	 * Represents a block following a control statement in the <code>constructions</code> stack.
	 */
	public static final int NONINDENT_BLOCK = -100;

	/** 
	 * Contains the formatted output.
	 */
	StringBuffer formattedSource;

	/** 
	 * Contains the current line.<br>
	 * Will be dumped at the next "newline"
	 */
	StringBuffer currentLineBuffer;

	/** 
	 * Used during the formatting to get each token.
	 */
	Scanner scanner;

	/** 
	 * Contains the tokens responsible for the current indentation level
	 * and the blocks not closed yet.
	 */
	private int[] constructions;

	/** 
	 * Index in the <code>constructions</code> array.
	 */
	private int constructionsCount;

	/** 
	 * Level of indentation of the current token (number of tab char put in front of it).
	 */
	private int indentationLevel;

	/** 
	 * Regular level of indentation of all the lines
	 */
	private int initialIndentationLevel;

	/** 
	 * Used to split a line.
	 */
	Scanner splitScanner;

	/** 
	 * To remember the offset between the beginning of the line and the
	 * beginning of the comment.
	 */
	int currentCommentOffset;
	int currentLineIndentationLevel;
	int maxLineSize = 30;
	private boolean containsOpenCloseBraces;
	private int indentationLevelForOpenCloseBraces;

	/**
	 * Collections of positions to map
	 */
	private int[] positionsToMap;

	/**
	 * Collections of mapped positions
	 */
	private int[] mappedPositions;

	private int indexToMap;

	private int indexInMap;

	private int globalDelta;

	private int lineDelta;

	private int splitDelta;

	private int beginningOfLineIndex;

	private int multipleLineCommentCounter;
	
	/** 
	 * Creates a new instance of Code Formatter using the given settings.
	 * 
	 * @deprecated backport 1.0 internal functionality
	 */
	public CodeFormatter(ConfigurableOption[] settings) {
		this(convertConfigurableOptions(settings));
	}
	
	/** 
	 * Creates a new instance of Code Formatter using the FormattingOptions object
	 * given as argument
	 * @deprecated Use CodeFormatter(ConfigurableOption[]) instead
	 */
	public CodeFormatter() {
		this((Map)null);
	}
	/** 
	 * Creates a new instance of Code Formatter using the given settings.
	 */
	public CodeFormatter(Map settings) {

		// initialize internal state
		constructionsCount = 0;
		constructions = new int[10];
		currentLineIndentationLevel = indentationLevel = initialIndentationLevel;
		currentCommentOffset = -1;

		// initialize primary and secondary scanners
		scanner = new Scanner(true, true); // regular scanner for forming lines
		scanner.recordLineSeparator = true;
		// to remind of the position of the beginning of the line.
		splitScanner = new Scanner(true, true);
		// secondary scanner to split long lines formed by primary scanning

		// initialize current line buffer
		currentLineBuffer = new StringBuffer();
		this.options = new FormatterOptions(settings);
	}

	/**
	 * Returns true if a lineSeparator has to be inserted before <code>operator</code>
	 * false otherwise.
	 */
	private static boolean breakLineBeforeOperator(int operator) {
		switch (operator) {
			case TokenNameCOMMA :
			case TokenNameSEMICOLON :
			case TokenNameEQUAL :
				return false;
			default :
				return true;
		}
	}
	
		/** 
	 * @deprecated backport 1.0 internal functionality
	 */
	private static Map convertConfigurableOptions(ConfigurableOption[] settings) {
		Hashtable options = new Hashtable(10);
		
		for (int i = 0; i < settings.length; i++) {
			if(settings[i].getComponentName().equals(CodeFormatter.class.getName())){
				String optionName = settings[i].getOptionName();
				int valueIndex = settings[i].getCurrentValueIndex();
				
				if(optionName.equals("newline.openingBrace")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.newline.openingBrace", valueIndex == 0 ? "insert" : "do not insert"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				} else if(optionName.equals("newline.controlStatement")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.newline.controlStatement",  valueIndex == 0 ? "insert" : "do not insert"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				} else if(optionName.equals("newline.clearAll")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.newline.clearAll",  valueIndex == 0 ? "clear all" : "preserve one"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				} else if(optionName.equals("newline.elseIf")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.newline.elseIf",  valueIndex == 0 ? "do not insert" : "insert" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				} else if(optionName.equals("newline.emptyBlock")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.newline.emptyBlock",  valueIndex == 0 ? "insert" : "do not insert"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				} else if(optionName.equals("lineSplit")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.lineSplit", String.valueOf(valueIndex)); //$NON-NLS-1$ //$NON-NLS-2$
				
				} else if(optionName.equals("style.assignment")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.style.assignment",  valueIndex == 0 ? "compact" : "normal"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				} else if(optionName.equals("tabulation.char")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.tabulation.char",  valueIndex == 0 ? "tab" : "space"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				} else if(optionName.equals("tabulation.size")) { //$NON-NLS-1$
					options.put("org.eclipse.jdt.core.formatter.tabulation.size", String.valueOf(valueIndex)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		return options;
	}

	/** 
	 * Returns the end of the source code.
	 */
	private final String copyRemainingSource() {
		char str[] = scanner.source;
		int startPosition = scanner.startPosition;
		int length = str.length - startPosition;
		StringBuffer bufr = new StringBuffer(length);
		if (startPosition < str.length) {
			bufr.append(str, startPosition, length);
		}
		return (bufr.toString());
	}

	/**
	 * Inserts <code>tabCount</code> tab character or their equivalent number of spaces.
	 */
	private void dumpTab(int tabCount) {
		if (options.indentWithTab) {
			for (int j = 0; j < tabCount; j++) {
				formattedSource.append('\t');
				increaseSplitDelta(1);
			}
		} else {
			for (int i = 0, max = options.tabSize * tabCount; i < max; i++) {
				formattedSource.append(' ');
				increaseSplitDelta(1);
			}
		}
	}

	/**
	 * Dumps <code>currentLineBuffer</code> into the formatted string.
	 */
	private void flushBuffer() {
		String currentString = currentLineBuffer.toString();
		splitDelta = 0;
		beginningOfLineIndex = formattedSource.length();
		if (containsOpenCloseBraces) {
			containsOpenCloseBraces = false;
			outputLine(
				currentString,
				false,
				indentationLevelForOpenCloseBraces,
				0,
				-1,
				null,
				0);
			indentationLevelForOpenCloseBraces = currentLineIndentationLevel;
		} else {
			outputLine(currentString, false, currentLineIndentationLevel, 0, -1, null, 0);
		}
		int scannerSourceLength = scanner.source.length;
		if (scannerSourceLength > 2) {
			if (scanner.source[scannerSourceLength - 1] == '\n' && 
				scanner.source[scannerSourceLength - 2] == '\r') {
					formattedSource.append(options.lineSeparatorSequence);
					increaseGlobalDelta(options.lineSeparatorSequence.length - 2);
			} else if (scanner.source[scannerSourceLength - 1] == '\n') {
				formattedSource.append(options.lineSeparatorSequence);
				increaseGlobalDelta(options.lineSeparatorSequence.length - 1);
			} else if (scanner.source[scannerSourceLength - 1] == '\r') {
				formattedSource.append(options.lineSeparatorSequence);
				increaseGlobalDelta(options.lineSeparatorSequence.length - 1);
			}
		}
		updateMappedPositions(scanner.startPosition);
	}

	/** 
	 * Formats the input string.
	 */
	private void format() {
		int token = 0;
		int previousToken = 0;
		int previousCompilableToken = 0;
		int indentationOffset = 0;
		int newLinesInWhitespace = 0;

		// number of new lines in the previous whitespace token
		// (used to leave blank lines before comments)
		int pendingNewLines = 0;
		boolean expectingOpenBrace = false;
		boolean clearNonBlockIndents = false;
		// true if all indentations till the 1st { (usefull after } or ;)
		boolean pendingSpace = true;
		boolean pendingNewlineAfterParen = false;
		// true when a cr is to be put after a ) (in conditional statements)
		boolean inAssignment = false;
		boolean inArrayAssignment = false;
		boolean inThrowsClause = false;
		boolean inClassOrInterfaceHeader = false;

		// openBracketCount is used to count the number of open brackets not closed yet.
		int openBracketCount = 0;
		int unarySignModifier = 0;

		// openParenthesis[0] is used to count the parenthesis not belonging to a condition
		// (eg foo();). parenthesis in for (...) are count elsewhere in the array.
		int openParenthesisCount = 1;
		int[] openParenthesis = new int[10];

		// tokenBeforeColon is used to know what token goes along with the current :
		// it can be case or ?
		int tokenBeforeColonCount = 0;
		int[] tokenBeforeColon = new int[10];

		constructionsCount = 0; // initializes the constructions count.

		// contains DO if in a DO..WHILE statement, UNITIALIZED otherwise.
		int nlicsToken = 0;

		// fix for 1FF17XY: LFCOM:ALL - Format problem on not matching } and else 
		boolean specialElse = false;

		// OPTION (IndentationLevel): initial indentation level may be non-zero.
		currentLineIndentationLevel += constructionsCount;

		// An InvalidInputException exception might cause the termination of this loop.
		try {
			while (true) {
				// Get the next token.  Catch invalid input and output it
				// with minimal formatting, also catch end of input and
				// exit the loop.
				try {
					token = scanner.getNextToken();
				} catch (InvalidInputException e) {
					if (!handleInvalidToken(e)) {
						throw e;
					}
					token = 0;
				}
				if (token == Scanner.TokenNameEOF)
					break;

				/* ## MODIFYING the indentation level before generating new lines
				and indentation in the output string
				*/

				// Removes all the indentations made by statements not followed by a block
				// except if the current token is ELSE, CATCH or if we are in a switch/case
				if (clearNonBlockIndents && (token != Scanner.TokenNameWHITESPACE)) {
					switch (token) {
						case TokenNameelse :
							if (constructionsCount > 0
								&& constructions[constructionsCount - 1] == TokenNameelse) {
								pendingNewLines = 1;
								specialElse = true;
							}
							indentationLevel += popInclusiveUntil(TokenNameif);
							break;
						case TokenNamecatch :
							indentationLevel += popInclusiveUntil(TokenNamecatch);
							break;
						case TokenNamefinally :
							indentationLevel += popInclusiveUntil(TokenNamecatch);
							break;
						case TokenNamewhile :
							if (nlicsToken == TokenNamedo) {
								indentationLevel += pop(TokenNamedo);
								break;
							}
						default :
							indentationLevel += popExclusiveUntilBlockOrCase();
							// clear until a CASE, DEFAULT or BLOCK is encountered.
							// Thus, the indentationLevel is correctly cleared either
							// in a switch/case statement or in any other situation.
					}
					clearNonBlockIndents = false;
				}
				// returns to the indentation level created by the SWITCH keyword
				// if the current token is a CASE or a DEFAULT
				if (token == TokenNamecase || token == TokenNamedefault) {
					indentationLevel += pop(TokenNamecase);
				}
				if (token == Scanner.TokenNamethrows) {
					inThrowsClause = true;
				}
				if ((token == Scanner.TokenNameclass || token == Scanner.TokenNameinterface) && previousToken != Scanner.TokenNameDOT) {
					inClassOrInterfaceHeader = true;
				}

				/* ## APPEND newlines and indentations to the output string
				*/
				// Do not add a new line between ELSE and IF, if the option elseIfOnSameLine is true.
				// Fix for 1ETLWPZ: IVJCOM:ALL - incorrect "else if" formatting
				if (pendingNewlineAfterParen
					&& previousCompilableToken == TokenNameelse
					&& token == TokenNameif
					&& options.compactElseIfMode) {
					pendingNewlineAfterParen = false;
					pendingNewLines = 0;
					indentationLevel += pop(TokenNameelse);
					// because else if is now one single statement,
					// the indentation level after it is increased by one and not by 2
					// (else = 1 indent, if = 1 indent, but else if = 1 indent, not 2).
				}
				// Add a newline & indent to the formatted source string if
				// a for/if-else/while statement was scanned and there is no block
				// following it.
				pendingNewlineAfterParen =
					pendingNewlineAfterParen
						|| (previousCompilableToken == TokenNameRPAREN && token == TokenNameLBRACE);
				if (pendingNewlineAfterParen && token != Scanner.TokenNameWHITESPACE) {
					pendingNewlineAfterParen = false;

					// Do to add a newline & indent sequence if the current token is an
					// open brace or a period or if the current token is a semi-colon and the
					// previous token is a close paren.
					// add a new line if a parenthesis belonging to a for() statement
					// has been closed and the current token is not an opening brace
					if (token != TokenNameLBRACE
						&& !isComment(token) // to avoid adding new line between else and a comment
						&& token != TokenNameDOT
						&& !(previousCompilableToken == TokenNameRPAREN && token == TokenNameSEMICOLON)) {
						newLine(1);
						currentLineIndentationLevel = indentationLevel;
						pendingNewLines = 0;
						pendingSpace = false;
					} else {
						if (token == TokenNameLBRACE && options.newLineBeforeOpeningBraceMode) {
							newLine(1);
							if (constructionsCount > 0
								&& constructions[constructionsCount - 1] != BLOCK
								&& constructions[constructionsCount - 1] != NONINDENT_BLOCK) {
								currentLineIndentationLevel = indentationLevel - 1;
							} else {
								currentLineIndentationLevel = indentationLevel;
							}
							pendingNewLines = 0;
							pendingSpace = false;
						}
					}
				}
				if (token == TokenNameLBRACE
					&& options.newLineBeforeOpeningBraceMode
					&& constructionsCount > 0
					&& constructions[constructionsCount - 1] == TokenNamedo) {
					newLine(1);
					currentLineIndentationLevel = indentationLevel - 1;
					pendingNewLines = 0;
					pendingSpace = false;
				}
				// see PR 1G5G8EC
				if (token == TokenNameLBRACE && inThrowsClause) {
					inThrowsClause = false;
					if (options.newLineBeforeOpeningBraceMode) {
						newLine(1);
						currentLineIndentationLevel = indentationLevel;
						pendingNewLines = 0;
						pendingSpace = false;
					}
				}
				// see PR 1G5G82G
				if (token == TokenNameLBRACE && inClassOrInterfaceHeader) {
					inClassOrInterfaceHeader = false;
					if (options.newLineBeforeOpeningBraceMode) {
						newLine(1);
						currentLineIndentationLevel = indentationLevel;
						pendingNewLines = 0;
						pendingSpace = false;
					}
				}
				// Add pending new lines to the formatted source string.
				// Note: pending new lines are not added if the current token
				// is a single line comment or whitespace.
				// if the comment is between parenthesis, there is no blank line preservation
				// (if it's a one-line comment, a blank line is added after it).
				if (((pendingNewLines > 0 && (!isComment(token)))
					|| (newLinesInWhitespace > 0 && (openParenthesisCount <= 1 && isComment(token)))
					|| (previousCompilableToken == TokenNameLBRACE && token == TokenNameRBRACE))
					&& token != Scanner.TokenNameWHITESPACE) {

					// Do not add newline & indent between an adjoining close brace and
					// close paren.  Anonymous inner classes may use this form.
					boolean closeBraceAndCloseParen =
						previousToken == TokenNameRBRACE && token == TokenNameRPAREN;

					// OPTION (NewLineInCompoundStatement): do not add newline & indent
					// between close brace and else, (do) while, catch, and finally if
					// newlineInCompoundStatement is true.
					boolean nlicsOption =
						previousToken == TokenNameRBRACE
							&& !options.newlineInControlStatementMode
							&& (token == TokenNameelse
								|| (token == TokenNamewhile && nlicsToken == TokenNamedo)
								|| token == TokenNamecatch
								|| token == TokenNamefinally);

					// Do not add a newline & indent between a close brace and semi-colon.
					boolean semiColonAndCloseBrace =
						previousToken == TokenNameRBRACE && token == TokenNameSEMICOLON;

					// Do not add a new line & indent between a multiline comment and a opening brace
					boolean commentAndOpenBrace =
						previousToken == Scanner.TokenNameCOMMENT_BLOCK && token == TokenNameLBRACE;

					// Do not add a newline & indent between a close brace and a colon (in array assignments, for example).
					boolean commaAndCloseBrace =
						previousToken == TokenNameRBRACE && token == TokenNameCOMMA;

					// Add a newline and indent, if appropriate.
					if (specialElse
						|| (!commentAndOpenBrace
							&& !closeBraceAndCloseParen
							&& !nlicsOption
							&& !semiColonAndCloseBrace
							&& !commaAndCloseBrace)) {

						// if clearAllBlankLinesMode=false, leaves the blank lines
						// inserted by the user
						// if clearAllBlankLinesMode=true, removes all of then
						// and insert only blank lines required by the formatting.
						if (!options.clearAllBlankLinesMode) {
							//  (isComment(token))
							pendingNewLines =
								(pendingNewLines < newLinesInWhitespace)
									? newLinesInWhitespace
									: pendingNewLines;
							pendingNewLines = (pendingNewLines > 2) ? 2 : pendingNewLines;
						}
						if (previousCompilableToken == TokenNameLBRACE && token == TokenNameRBRACE) {
							containsOpenCloseBraces = true;
							indentationLevelForOpenCloseBraces = currentLineIndentationLevel;
							if (isComment(previousToken)) {
								newLine(pendingNewLines);
							} else {
								/*  if (!(constructionsCount > 1
								        && constructions[constructionsCount-1] == NONINDENT_BLOCK
								        && (constructions[constructionsCount-2] == TokenNamefor 
								         || constructions[constructionsCount-2] == TokenNamewhile))) {*/
								if (options.newLineInEmptyBlockMode) {
									if (inArrayAssignment) {
										newLine(1); // array assigment with an empty block
									} else {
										newLine(pendingNewLines);
									}
								}
								// }
							}
						} else {
							// see PR 1FKKC3U: LFCOM:WINNT - Format problem with a comment before the ';'
							if (!((previousToken == Scanner.TokenNameCOMMENT_BLOCK
								|| previousToken == Scanner.TokenNameCOMMENT_JAVADOC)
								&& token == TokenNameSEMICOLON)) {
								newLine(pendingNewLines);
							}
						}
						if (((previousCompilableToken == TokenNameSEMICOLON)
							|| (previousCompilableToken == TokenNameLBRACE)
							|| (previousCompilableToken == TokenNameRBRACE)
							|| (isComment(previousToken)))
							&& (token == TokenNameRBRACE)) {
							indentationOffset = -1;
							indentationLevel += popExclusiveUntilBlock();
						}
						if (previousToken == Scanner.TokenNameCOMMENT_LINE && inAssignment) {
							// PR 1FI5IPO
							currentLineIndentationLevel++;
						} else {
							currentLineIndentationLevel = indentationLevel + indentationOffset;
						}
						pendingSpace = false;
						indentationOffset = 0;
					}
					pendingNewLines = 0;
					newLinesInWhitespace = 0;
					specialElse = false;

					if (nlicsToken == TokenNamedo && token == TokenNamewhile) {
						nlicsToken = 0;
					}
				}
				switch (token) {
					case TokenNameelse :
					case TokenNamefinally :
						expectingOpenBrace = true;
						pendingNewlineAfterParen = true;
						indentationLevel += pushControlStatement(token);
						break;
					case TokenNamecase :
					case TokenNamedefault :
						if (tokenBeforeColonCount == tokenBeforeColon.length) {
							System.arraycopy(
								tokenBeforeColon,
								0,
								(tokenBeforeColon = new int[tokenBeforeColonCount * 2]),
								0,
								tokenBeforeColonCount);
						}
						tokenBeforeColon[tokenBeforeColonCount++] = TokenNamecase;
						indentationLevel += pushControlStatement(TokenNamecase);
						break;
					case TokenNameQUESTION :
						if (tokenBeforeColonCount == tokenBeforeColon.length) {
							System.arraycopy(
								tokenBeforeColon,
								0,
								(tokenBeforeColon = new int[tokenBeforeColonCount * 2]),
								0,
								tokenBeforeColonCount);
						}
						tokenBeforeColon[tokenBeforeColonCount++] = token;
						break;
					case TokenNameswitch :
					case TokenNamefor :
					case TokenNameif :
					case TokenNamewhile :
						if (openParenthesisCount == openParenthesis.length) {
							System.arraycopy(
								openParenthesis,
								0,
								(openParenthesis = new int[openParenthesisCount * 2]),
								0,
								openParenthesisCount);
						}
						openParenthesis[openParenthesisCount++] = 0;
						expectingOpenBrace = true;

						indentationLevel += pushControlStatement(token);
						break;
					case TokenNametry :
						pendingNewlineAfterParen = true;
					case TokenNamecatch :
						// several CATCH statements can be contiguous.
						// a CATCH is encountered pop until first CATCH (if a CATCH follows a TRY it works the same way,
						// as CATCH and TRY are the same token in the stack).
						expectingOpenBrace = true;
						indentationLevel += pushControlStatement(TokenNamecatch);
						break;

					case TokenNamedo :
						expectingOpenBrace = true;
						indentationLevel += pushControlStatement(token);
						nlicsToken = token;
						break;
					case TokenNamenew :
						break;
					case TokenNameLPAREN :
						if (previousToken == TokenNamesynchronized) {
							indentationLevel += pushControlStatement(previousToken);
						} else {
							// Put a space between the previous and current token if the
							// previous token was not a keyword, open paren, logical
							// compliment (eg: !), semi-colon, open brace, close brace,
							// super, or this.
							if (previousCompilableToken != TokenNameLBRACKET
								&& previousToken != TokenNameIdentifier
								&& previousToken != 0
								&& previousToken != TokenNameNOT
								&& previousToken != TokenNameLPAREN
								&& previousToken != TokenNameTWIDDLE
								&& previousToken != TokenNameSEMICOLON
								&& previousToken != TokenNameLBRACE
								&& previousToken != TokenNameRBRACE
								&& previousToken != TokenNamesuper
								&& previousToken != TokenNamethis) {
								space();
							}
							// If in a for/if/while statement, increase the parenthesis count
							// for the current openParenthesisCount
							// else increase the count for stand alone parenthesis.
							if (openParenthesisCount > 0)
								openParenthesis[openParenthesisCount - 1]++;
							else
								openParenthesis[0]++;
	
							pendingSpace = false;
						}
						break;
					case TokenNameRPAREN :

						// Decrease the parenthesis count
						// if there is no more unclosed parenthesis,
						// a new line and indent may be append (depending on the next token).
						if ((openParenthesisCount > 1)
							&& (openParenthesis[openParenthesisCount - 1] > 0)) {
							openParenthesis[openParenthesisCount - 1]--;
							if (openParenthesis[openParenthesisCount - 1] <= 0) {
								pendingNewlineAfterParen = true;
								inAssignment = false;
								openParenthesisCount--;
							}
						} else {
							openParenthesis[0]--;
						}
						pendingSpace = false;
						break;
					case TokenNameLBRACE :
						if ((previousCompilableToken == TokenNameRBRACKET)
							|| (previousCompilableToken == TokenNameEQUAL)) {
							//                  if (previousCompilableToken == TokenNameRBRACKET) {
							inArrayAssignment = true;
							inAssignment = false;
						}
						if (inArrayAssignment) {
							indentationLevel += pushBlock();
						} else {
							// Add new line and increase indentation level after open brace.
							pendingNewLines = 1;
							indentationLevel += pushBlock();
						}
						break;
					case TokenNameRBRACE :
						if (previousCompilableToken == TokenNameRPAREN) {
							pendingSpace = false;
						}
						if (inArrayAssignment) {
							inArrayAssignment = false;
							pendingNewLines = 1;
							indentationLevel += popInclusiveUntilBlock();
						} else {
							pendingNewLines = 1;
							indentationLevel += popInclusiveUntilBlock();

							if (previousCompilableToken == TokenNameRPAREN) {
								// fix for 1FGDDV6: LFCOM:WIN98 - Weird splitting on message expression
								currentLineBuffer.append(options.lineSeparatorSequence);
								increaseLineDelta(options.lineSeparatorSequence.length);
							}
							if (constructionsCount > 0) {
								switch (constructions[constructionsCount - 1]) {
									case TokenNamefor :
										//indentationLevel += popExclusiveUntilBlock();
										//break;
									case TokenNameswitch :
									case TokenNameif :
									case TokenNameelse :
									case TokenNametry :
									case TokenNamecatch :
									case TokenNamefinally :
									case TokenNamewhile :
									case TokenNamedo :
									case TokenNamesynchronized :
										clearNonBlockIndents = true;
									default :
										break;
								}
							}
						}
						break;
					case TokenNameLBRACKET :
						openBracketCount++;
						pendingSpace = false;
						break;
					case TokenNameRBRACKET :
						openBracketCount -= (openBracketCount > 0) ? 1 : 0;
						// if there is no left bracket to close, the right bracket is ignored.
						pendingSpace = false;
						break;
					case TokenNameCOMMA :
					case TokenNameDOT :
						pendingSpace = false;
						break;
					case TokenNameSEMICOLON :

						// Do not generate line terminators in the definition of
						// the for statement.
						// if not in this case, jump a line and reduce indentation after the brace
						// if the block it closes belongs to a conditional statement (if, while, do...).
						if (openParenthesisCount <= 1) {
							pendingNewLines = 1;
							if (expectingOpenBrace) {
								clearNonBlockIndents = true;
								expectingOpenBrace = false;
							}
						}
						inAssignment = false;
						pendingSpace = false;
						break;
					case TokenNamePLUS_PLUS :
					case TokenNameMINUS_MINUS :

						// Do not put a space between a post-increment/decrement
						// and the identifier being modified.
						if (previousToken == TokenNameIdentifier
							|| previousToken == TokenNameRBRACKET) {
							pendingSpace = false;
						}
						break;
					case TokenNamePLUS : // previously ADDITION
					case TokenNameMINUS :

						// Handle the unary operators plus and minus via a flag
						if (!isLiteralToken(previousToken)
							&& previousToken != TokenNameIdentifier
							&& previousToken != TokenNameRPAREN
							&& previousToken != TokenNameRBRACKET) {
							unarySignModifier = 1;
						}
						break;
					case TokenNameCOLON :
						// In a switch/case statement, add a newline & indent
						// when a colon is encountered.
						if (tokenBeforeColonCount > 0) {
							if (tokenBeforeColon[tokenBeforeColonCount - 1] == TokenNamecase) {
								pendingNewLines = 1;
							}
							tokenBeforeColonCount--;
						}
						break;
					case TokenNameEQUAL :
						inAssignment = true;
						break;
					case Scanner.TokenNameCOMMENT_LINE :
						pendingNewLines = 1;
						if (inAssignment) {
							currentLineIndentationLevel++;
						}
						break; // a line is always inserted after a one-line comment
					case Scanner.TokenNameCOMMENT_JAVADOC :
					case Scanner.TokenNameCOMMENT_BLOCK :
						currentCommentOffset = getCurrentCommentOffset();
						pendingNewLines = 1;
						break;
					case Scanner.TokenNameWHITESPACE :

						// Count the number of line terminators in the whitespace so
						// line spacing can be preserved near comments.
						char[] source = scanner.source;
						newLinesInWhitespace = 0;
						for (int i = scanner.startPosition, max = scanner.currentPosition;
							i < max;
							i++) {
							if (source[i] == '\r') {
								if (i < max - 1) {
									if (source[++i] == '\n') {
										newLinesInWhitespace++;
									} else {
										newLinesInWhitespace++;
									}
								} else {
									newLinesInWhitespace++;
								}
							} else if (source[i] == '\n') {
									newLinesInWhitespace++;
							}
						}
						increaseLineDelta(scanner.startPosition - scanner.currentPosition);
						break;
					default :
						if ((token == TokenNameIdentifier)
							|| isLiteralToken(token)
							|| token == TokenNamesuper
							|| token == TokenNamethis) {

							// Do not put a space between a unary operator
							// (eg: ++, --, +, -) and the identifier being modified.
							if (previousToken == TokenNamePLUS_PLUS
								|| previousToken == TokenNameMINUS_MINUS
								|| (previousToken == TokenNamePLUS && unarySignModifier > 0)
								|| (previousToken == TokenNameMINUS && unarySignModifier > 0)) {
								pendingSpace = false;
							}
							unarySignModifier = 0;
						}
						break;
				}
				// Do not output whitespace tokens.
				if (token != Scanner.TokenNameWHITESPACE) {

					/* Add pending space to the formatted source string.
					Do not output a space under the following circumstances:
					1) this is the first pass
					2) previous token is an open paren
					3) previous token is a period
					4) previous token is the logical compliment (eg: !)
					5) previous token is the bitwise compliment (eg: ~)
					6) previous token is the open bracket (eg: [)
					7) in an assignment statement, if the previous token is an 
					open brace or the current token is a close brace
					8) previous token is a single line comment
					*/
					boolean openAndCloseBrace =
						previousCompilableToken == TokenNameLBRACE && token == TokenNameRBRACE;

					if (pendingSpace
						&& insertSpaceAfter(previousToken)
						&& !(inAssignment
							&& (previousToken == TokenNameLBRACE || token == TokenNameRBRACE))
						&& previousToken != Scanner.TokenNameCOMMENT_LINE) {
						if ((!(options.compactAssignmentMode && token == TokenNameEQUAL))
							&& !openAndCloseBrace)
							space();
					}
					// Add the next token to the formatted source string.
					outputCurrentToken(token);
					if (token == Scanner.TokenNameCOMMENT_LINE && openParenthesisCount > 1) {
						pendingNewLines = 0;
						currentLineBuffer.append(options.lineSeparatorSequence);
						increaseLineDelta(options.lineSeparatorSequence.length);
					}
					pendingSpace = true;
				}
				// Whitespace tokens do not need to be remembered.
				if (token != Scanner.TokenNameWHITESPACE) {
					previousToken = token;
					if (token != Scanner.TokenNameCOMMENT_BLOCK
						&& token != Scanner.TokenNameCOMMENT_LINE
						&& token != Scanner.TokenNameCOMMENT_JAVADOC) {
						previousCompilableToken = token;
					}
				}
			}
			output(copyRemainingSource());
			flushBuffer(); // dump the last token of the source in the formatted output.
		} catch (InvalidInputException e) {
			output(copyRemainingSource());
			flushBuffer(); // dump the last token of the source in the formatted output.
		}
	}

	/** 
	 * Formats the char array <code>sourceString</code>,
	 * and returns a string containing the formatted version.
	 * @return the formatted ouput.
	 */
	public String formatSourceString(String sourceString) {
		char[] sourceChars = sourceString.toCharArray();
		formattedSource = new StringBuffer(sourceChars.length);
		scanner.setSource(sourceChars);
		format();
		return formattedSource.toString();
	}

	/** 
	 * Formats the char array <code>sourceString</code>,
	 * and returns a string containing the formatted version.
	 * @param string the string to format
	 * @param indentationLevel the initial indentation level
	 * @return the formatted ouput.
	 */
	public String format(String string, int indentationLevel) {
		return format(string, indentationLevel, (int[])null);
	}	
	
	/** 
	 * Formats the char array <code>sourceString</code>,
	 * and returns a string containing the formatted version.
	 * The positions array is modified to contain the mapped positions.
	 * @param string the string to format
	 * @param indentationLevel the initial indentation level
	 * @param positions the array of positions to map
	 * @return the formatted ouput.
	 */
	public String format(String string, int indentationLevel, int[] positions) {
		return this.format(string, indentationLevel, positions, null);
	}
	
	public String format(String string, int indentationLevel, int[] positions, String lineSeparator) {
		if (lineSeparator != null){
			this.options.setLineSeparator(lineSeparator);
		}
		if (positions != null) {
			this.setPositionsToMap(positions);
			this.setInitialIndentationLevel(indentationLevel);
			String formattedString = this.formatSourceString(string);
			int[] mappedPositions = this.getMappedPositions();
			System.arraycopy(mappedPositions, 0, positions, 0, positions.length);
			return formattedString;
		} else {
			this.setInitialIndentationLevel(indentationLevel);
			return this.formatSourceString(string);
		}
	}	
	/** 
	 * Formats the char array <code>sourceString</code>,
	 * and returns a string containing the formatted version. The initial indentation level is 0.
	 * @param string the string to format
	 * @return the formatted ouput.
	 */
	public String format(String string) {
		return this.format(string, 0, (int[])null);
	}
	
	/** 
	 * Formats a given source string, starting indenting it at a particular 
	 * depth and using the given options
	 * 
	 * @deprecated backport 1.0 internal functionality
	 */
	public static String format(String sourceString, int initialIndentationLevel, ConfigurableOption[] options) {
		CodeFormatter formatter = new CodeFormatter(options);
		formatter.setInitialIndentationLevel(initialIndentationLevel);
		return formatter.formatSourceString(sourceString);
	}
	
	/**
	 * Returns the number of characters and tab char between the beginning of the line
	 * and the beginning of the comment.
	 */
	private int getCurrentCommentOffset() {
		int linePtr = scanner.linePtr;
		// if there is no beginning of line, return 0.
		if (linePtr < 0)
			return 0;
		int offset = 0;
		int beginningOfLine = scanner.lineEnds[linePtr];
		int currentStartPosition = scanner.startPosition;
		char[] source = scanner.source;

		// find the position of the beginning of the line containing the comment
		while (beginningOfLine > currentStartPosition) {
			if (linePtr > 0) {
				beginningOfLine = scanner.lineEnds[--linePtr];
			} else {
				beginningOfLine = 0;
				break;
			}
		}
		for (int i = currentStartPosition - 1; i >= beginningOfLine ; i--) {
			char currentCharacter = source[i];
			switch (currentCharacter) {
				case '\t' :
					offset += options.tabSize;
					break;
				case ' ' :
					offset++;
					break;
				case '\r' :
				case '\n' :
					break;
				default:
					return offset;
			}
		}
		return offset;
	}
	
	/**
	 * Returns an array of descriptions for the configurable options.
	 * The descriptions may be changed and passed back to a different
	 * compiler.
	 * 
	 * @deprecated backport 1.0 internal functionality
	 */
	public static ConfigurableOption[] getDefaultOptions(Locale locale) {
		String componentName = CodeFormatter.class.getName();
		FormatterOptions options = new FormatterOptions();
		return new ConfigurableOption[] {
			new ConfigurableOption(componentName, "newline.openingBrace",  locale, options.newLineBeforeOpeningBraceMode ? 0 : 1), //$NON-NLS-1$
			new ConfigurableOption(componentName, "newline.controlStatement",  locale, options.newlineInControlStatementMode ? 0 : 1), //$NON-NLS-1$
			new ConfigurableOption(componentName, "newline.clearAll",  locale, options.clearAllBlankLinesMode ? 0 : 1), //$NON-NLS-1$
			new ConfigurableOption(componentName, "newline.elseIf",  locale, options.compactElseIfMode ? 0 : 1), //$NON-NLS-1$
			new ConfigurableOption(componentName, "newline.emptyBlock",  locale, options.newLineInEmptyBlockMode ? 0 : 1), //$NON-NLS-1$
			new ConfigurableOption(componentName, "line.split",  locale, options.maxLineLength),//$NON-NLS-1$
			new ConfigurableOption(componentName, "style.compactAssignment",  locale, options.compactAssignmentMode ? 0 : 1), //$NON-NLS-1$
			new ConfigurableOption(componentName, "tabulation.char",  locale, options.indentWithTab ? 0 : 1), //$NON-NLS-1$
			new ConfigurableOption(componentName, "tabulation.size",  locale, options.tabSize)	//$NON-NLS-1$
		};
	}

	/**
	 * Returns the array of mapped positions.
	 * Returns null is no positions have been set.
	 * @return int[]
	 * @deprecated There is no need to retrieve the mapped positions anymore.
	 */
	public int[] getMappedPositions() {
		return mappedPositions;
	}

	/**
	 * Returns the priority of the token given as argument<br>
	 * The most prioritary the token is, the smallest the return value is.
	 * @return the priority of <code>token</code>
	 * @param token the token of which the priority is requested
	 */
	private static int getTokenPriority(int token) {
		switch (token) {
			case TokenNameextends :
			case TokenNameimplements :
			case TokenNamethrows :
				return 10;
			case TokenNameSEMICOLON : // ;
				return 20;
			case TokenNameCOMMA : // ,
				return 25;
			case TokenNameEQUAL : // =
				return 30;
			case TokenNameAND_AND : // && 
			case TokenNameOR_OR : // || 
				return 40;
			case TokenNameQUESTION : // ? 
			case TokenNameCOLON : // :
				return 50; // it's better cutting on ?: than on ;
			case TokenNameEQUAL_EQUAL : // == 
			case TokenNameNOT_EQUAL : // != 
				return 60;
			case TokenNameLESS : // < 
			case TokenNameLESS_EQUAL : // <= 
			case TokenNameGREATER : // > 
			case TokenNameGREATER_EQUAL : // >= 
			case TokenNameinstanceof : // instanceof
				return 70;
			case TokenNamePLUS : // + 
			case TokenNameMINUS : // - 
				return 80;
			case TokenNameMULTIPLY : // * 
			case TokenNameDIVIDE : // / 
			case TokenNameREMAINDER : // % 
				return 90;
			case TokenNameLEFT_SHIFT : // << 
			case TokenNameRIGHT_SHIFT : // >> 
			case TokenNameUNSIGNED_RIGHT_SHIFT : // >>> 
				return 100;
			case TokenNameAND : // &
			case TokenNameOR : // | 
			case TokenNameXOR : // ^ 
				return 110;
			case TokenNameMULTIPLY_EQUAL : // *= 
			case TokenNameDIVIDE_EQUAL : // /= 
			case TokenNameREMAINDER_EQUAL : // %= 
			case TokenNamePLUS_EQUAL : // += 
			case TokenNameMINUS_EQUAL : // -= 
			case TokenNameLEFT_SHIFT_EQUAL : // <<= 
			case TokenNameRIGHT_SHIFT_EQUAL : // >>= 
			case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL : // >>>=
			case TokenNameAND_EQUAL : // &= 
			case TokenNameXOR_EQUAL : // ^= 
			case TokenNameOR_EQUAL : // |= 
				return 120;
			case TokenNameDOT : // .
				return 130;
			default :
				return Integer.MAX_VALUE;
		}
	}

	/**
	 * Handles the exception raised when an invalid token is encountered.
	 * Returns true if the exception has been handled, false otherwise.
	 */
	private boolean handleInvalidToken(Exception e) {
		if (e.getMessage().equals(Scanner.INVALID_CHARACTER_CONSTANT)
			|| e.getMessage().equals(Scanner.INVALID_CHAR_IN_STRING)
			|| e.getMessage().equals(Scanner.INVALID_ESCAPE)) {
			return true;
		}
		return false;
	}

	private final void increaseGlobalDelta(int offset) {
		globalDelta += offset;
	}

	private final void increaseLineDelta(int offset) {
		lineDelta += offset;
	}

	private final void increaseSplitDelta(int offset) {
		splitDelta += offset;
	}

	/**
	 * Returns true if a space has to be inserted after <code>operator</code>
	 * false otherwise.
	 */
	private boolean insertSpaceAfter(int token) {
		switch (token) {
			case TokenNameLPAREN :
			case TokenNameNOT :
			case TokenNameTWIDDLE :
			case TokenNameDOT :
			case 0 : // no token
			case TokenNameLBRACKET :
			case Scanner.TokenNameCOMMENT_LINE :
				return false;
			default :
				return true;
		}
	}

	/**
	 * Returns true if a space has to be inserted before <code>operator</code>
	 * false otherwise.<br>
	 * Cannot be static as it uses the code formatter options
	 * (to know if the compact assignment mode is on).
	 */
	private boolean insertSpaceBefore(int token) {
		switch (token) {
			case TokenNameEQUAL :
				return (!options.compactAssignmentMode);
			default :
				return false;
		}
	}

	private static boolean isComment(int token) {
		boolean result =
			token == Scanner.TokenNameCOMMENT_BLOCK
				|| token == Scanner.TokenNameCOMMENT_LINE
				|| token == Scanner.TokenNameCOMMENT_JAVADOC;
		return result;
	}

	private static boolean isLiteralToken(int token) {
		boolean result =
			token == TokenNameIntegerLiteral
				|| token == TokenNameLongLiteral
				|| token == TokenNameFloatingPointLiteral
				|| token == TokenNameDoubleLiteral
				|| token == TokenNameCharacterLiteral
				|| token == TokenNameStringLiteral;
		return result;
	}

	/**
	 * If the length of <code>oneLineBuffer</code> exceeds <code>maxLineLength</code>,
	 * it is split and the result is dumped in <code>formattedSource</code>
	 * @param newLineCount the number of new lines to append
	 */
	private void newLine(int newLineCount) {

		// format current line
		splitDelta = 0;
		beginningOfLineIndex = formattedSource.length();
		String currentLine = currentLineBuffer.toString();
		if (containsOpenCloseBraces) {
			containsOpenCloseBraces = false;
			outputLine(
				currentLine,
				false,
				indentationLevelForOpenCloseBraces,
				0,
				-1,
				null,
				0);
			indentationLevelForOpenCloseBraces = currentLineIndentationLevel;
		} else {
			outputLine(currentLine, false, currentLineIndentationLevel, 0, -1, null, 0);
		}
		// dump line break(s)
		for (int i = 0; i < newLineCount; i++) {
			formattedSource.append(options.lineSeparatorSequence);
			increaseSplitDelta(options.lineSeparatorSequence.length);
		}
		// reset formatter for next line
		int currentLength = currentLine.length();
		currentLineBuffer =
			new StringBuffer(
				currentLength > maxLineSize ? maxLineSize = currentLength : maxLineSize);

		increaseGlobalDelta(splitDelta);
		increaseGlobalDelta(lineDelta);
		lineDelta = 0;
		currentLineIndentationLevel = initialIndentationLevel;
	}

	private String operatorString(int operator) {
		switch (operator) {
			case TokenNameextends :
				return "extends"; //$NON-NLS-1$

			case TokenNameimplements :
				return "implements"; //$NON-NLS-1$

			case TokenNamethrows :
				return "throws"; //$NON-NLS-1$

			case TokenNameSEMICOLON : // ;
				return ";"; //$NON-NLS-1$

			case TokenNameCOMMA : // ,
				return ","; //$NON-NLS-1$

			case TokenNameEQUAL : // =
				return "="; //$NON-NLS-1$

			case TokenNameAND_AND : // && (15.22)
				return "&&"; //$NON-NLS-1$

			case TokenNameOR_OR : // || (15.23)
				return "||"; //$NON-NLS-1$

			case TokenNameQUESTION : // ? (15.24)
				return "?"; //$NON-NLS-1$

			case TokenNameCOLON : // : (15.24)
				return ":"; //$NON-NLS-1$

			case TokenNameEQUAL_EQUAL : // == (15.20, 15.20.1, 15.20.2, 15.20.3)
				return "=="; //$NON-NLS-1$

			case TokenNameNOT_EQUAL : // != (15.20, 15.20.1, 15.20.2, 15.20.3)
				return "!="; //$NON-NLS-1$

			case TokenNameLESS : // < (15.19.1)
				return "<"; //$NON-NLS-1$

			case TokenNameLESS_EQUAL : // <= (15.19.1)
				return "<="; //$NON-NLS-1$

			case TokenNameGREATER : // > (15.19.1)
				return ">"; //$NON-NLS-1$

			case TokenNameGREATER_EQUAL : // >= (15.19.1)
				return ">="; //$NON-NLS-1$

			case TokenNameinstanceof : // instanceof
				return "instanceof"; //$NON-NLS-1$

			case TokenNamePLUS : // + (15.17, 15.17.2)
				return "+"; //$NON-NLS-1$

			case TokenNameMINUS : // - (15.17.2)
				return "-"; //$NON-NLS-1$

			case TokenNameMULTIPLY : // * (15.16.1)
				return "*"; //$NON-NLS-1$

			case TokenNameDIVIDE : // / (15.16.2)
				return "/"; //$NON-NLS-1$

			case TokenNameREMAINDER : // % (15.16.3)
				return "%"; //$NON-NLS-1$

			case TokenNameLEFT_SHIFT : // << (15.18)
				return "<<"; //$NON-NLS-1$

			case TokenNameRIGHT_SHIFT : // >> (15.18)
				return ">>"; //$NON-NLS-1$

			case TokenNameUNSIGNED_RIGHT_SHIFT : // >>> (15.18)
				return ">>>"; //$NON-NLS-1$

			case TokenNameAND : // & (15.21, 15.21.1, 15.21.2)
				return "&"; //$NON-NLS-1$

			case TokenNameOR : // | (15.21, 15.21.1, 15.21.2)
				return "|"; //$NON-NLS-1$

			case TokenNameXOR : // ^ (15.21, 15.21.1, 15.21.2)
				return "^"; //$NON-NLS-1$

			case TokenNameMULTIPLY_EQUAL : // *= (15.25.2)
				return "*="; //$NON-NLS-1$

			case TokenNameDIVIDE_EQUAL : // /= (15.25.2)
				return "/="; //$NON-NLS-1$

			case TokenNameREMAINDER_EQUAL : // %= (15.25.2)
				return "%="; //$NON-NLS-1$

			case TokenNamePLUS_EQUAL : // += (15.25.2)
				return "+="; //$NON-NLS-1$

			case TokenNameMINUS_EQUAL : // -= (15.25.2)
				return "-="; //$NON-NLS-1$

			case TokenNameLEFT_SHIFT_EQUAL : // <<= (15.25.2)
				return "<<="; //$NON-NLS-1$

			case TokenNameRIGHT_SHIFT_EQUAL : // >>= (15.25.2)
				return ">>="; //$NON-NLS-1$

			case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL : // >>>= (15.25.2)
				return ">>>="; //$NON-NLS-1$

			case TokenNameAND_EQUAL : // &= (15.25.2)
				return "&="; //$NON-NLS-1$

			case TokenNameXOR_EQUAL : // ^= (15.25.2)
				return "^="; //$NON-NLS-1$

			case TokenNameOR_EQUAL : // |= (15.25.2)
				return "|="; //$NON-NLS-1$

			case TokenNameDOT : // .
				return "."; //$NON-NLS-1$

			default :
				return ""; //$NON-NLS-1$
		}
	}

	/** 
	 * Appends <code>stringToOutput</code> to the formatted output.<br>
	 * If it contains \n, append a LINE_SEPARATOR and indent after it.
	 */
	private void output(String stringToOutput) {
		char currentCharacter;
		for (int i = 0, max = stringToOutput.length(); i < max; i++) {
			currentCharacter = stringToOutput.charAt(i);
			if (currentCharacter != '\t') {
				currentLineBuffer.append(currentCharacter);
			}
		}
	}

	/** 
	 * Appends <code>token</code> to the formatted output.<br>
	 * If it contains <code>\n</code>, append a LINE_SEPARATOR and indent after it.
	 */
	private void outputCurrentToken(int token) {
		char[] source = scanner.source;
		int startPosition = scanner.startPosition;

		switch (token) {
			case Scanner.TokenNameCOMMENT_JAVADOC :
			case Scanner.TokenNameCOMMENT_BLOCK :
			case Scanner.TokenNameCOMMENT_LINE :
				boolean endOfLine = false;
				int currentCommentOffset = getCurrentCommentOffset();
				int beginningOfLineSpaces = 0;
				endOfLine = false;
				currentCommentOffset = getCurrentCommentOffset();
				beginningOfLineSpaces = 0;
				boolean pendingCarriageReturn = false;
				for (int i = startPosition, max = scanner.currentPosition; i < max; i++) {
					char currentCharacter = source[i];
					updateMappedPositions(i);
					switch (currentCharacter) {
						case '\r' :
							pendingCarriageReturn = true;
							endOfLine = true;
							break;
						case '\n' :
							if (pendingCarriageReturn) {
								increaseGlobalDelta(options.lineSeparatorSequence.length - 2);
							} else {
								increaseGlobalDelta(options.lineSeparatorSequence.length - 1);
							}
							pendingCarriageReturn = false;
							currentLineBuffer.append(options.lineSeparatorSequence);
							beginningOfLineSpaces = 0;
							endOfLine = true;
							break;
						case '\t' :
							if (pendingCarriageReturn) {
								pendingCarriageReturn = false;
								increaseGlobalDelta(options.lineSeparatorSequence.length - 1);
								currentLineBuffer.append(options.lineSeparatorSequence);
								beginningOfLineSpaces = 0;
								endOfLine = true;
							}
							if (endOfLine) {
								// we remove a maximum of currentCommentOffset characters (tabs are converted to space numbers).
								beginningOfLineSpaces += options.tabSize;
								if (beginningOfLineSpaces > currentCommentOffset) {
									currentLineBuffer.append(currentCharacter);
								} else {
									increaseGlobalDelta(-1);
								}
							} else {
								currentLineBuffer.append(currentCharacter);
							}
							break;
						case ' ' :
							if (pendingCarriageReturn) {
								pendingCarriageReturn = false;
								increaseGlobalDelta(options.lineSeparatorSequence.length - 1);
								currentLineBuffer.append(options.lineSeparatorSequence);
								beginningOfLineSpaces = 0;
								endOfLine = true;
							}
							if (endOfLine) {
								// we remove a maximum of currentCommentOffset characters (tabs are converted to space numbers).
								beginningOfLineSpaces++;
								if (beginningOfLineSpaces > currentCommentOffset) {
									currentLineBuffer.append(currentCharacter);
								} else {
									increaseGlobalDelta(-1);
								}
							} else {
								currentLineBuffer.append(currentCharacter);
							}
							break;
						default :
							if (pendingCarriageReturn) {
								pendingCarriageReturn = false;
								increaseGlobalDelta(options.lineSeparatorSequence.length - 1);
								currentLineBuffer.append(options.lineSeparatorSequence);
								beginningOfLineSpaces = 0;
								endOfLine = true;
							} else {
								beginningOfLineSpaces = 0;
								currentLineBuffer.append(currentCharacter);
								endOfLine = false;								
							}
					}
				}
				updateMappedPositions(scanner.currentPosition - 1);
				multipleLineCommentCounter++;
				break;
			default :
				for (int i = startPosition, max = scanner.currentPosition; i < max; i++) {
					char currentCharacter = source[i];
					updateMappedPositions(i);
					currentLineBuffer.append(currentCharacter);
				}
		}
	}
	
	/**
	 * Outputs <code>currentString</code>:<br>
	 * <ul><li>If its length is < maxLineLength, output
	 * <li>Otherwise it is split.</ul>
	 * @param currentString string to output
	 * @param preIndented whether the string to output was pre-indented
	 * @param depth number of indentation to put in front of <code>currentString</code>
	 * @param operator value of the operator belonging to <code>currentString</code>.
	 */
	private void outputLine(
		String currentString,
		boolean preIndented,
		int depth,
		int operator,
		int substringIndex,
		int[] startSubstringIndexes,
		int offsetInGlobalLine) {

		boolean emptyFirstSubString = false;
		String operatorString = operatorString(operator);
		boolean placeOperatorBehind = !breakLineBeforeOperator(operator);
		boolean placeOperatorAhead = !placeOperatorBehind;

		// dump prefix operator?
		if (placeOperatorAhead) {
			if (!preIndented) {
				dumpTab(depth);
				preIndented = true;
			}
			if (operator != 0) {
				if (insertSpaceBefore(operator)) {
					formattedSource.append(' ');
					increaseSplitDelta(1);
				}
				formattedSource.append(operatorString);
				increaseSplitDelta(operatorString.length());

				if (insertSpaceAfter(operator)
					&& operator != TokenNameimplements
					&& operator != TokenNameextends
					&& operator != TokenNamethrows) {
					formattedSource.append(' ');
					increaseSplitDelta(1);
				}
			}
		}
		SplitLine splitLine = null;
		if (options.maxLineLength == 0
			|| getLength(currentString, depth) < options.maxLineLength
			|| (splitLine = split(currentString, offsetInGlobalLine)) == null) {

			// depending on the type of operator, outputs new line before of after dumping it
			// indent before postfix operator
			// indent also when the line cannot be split
			if (operator == TokenNameextends
				|| operator == TokenNameimplements
				|| operator == TokenNamethrows) {
				formattedSource.append(' ');
				increaseSplitDelta(1);
			}
			if (placeOperatorBehind) {
				if (!preIndented) {
					dumpTab(depth);
				}
			}
			int max = currentString.length();
			if (multipleLineCommentCounter != 0) {
				try {
					BufferedReader reader = new BufferedReader(new StringReader(currentString));
					String line = reader.readLine();
					while (line != null) {
						updateMappedPositionsWhileSplitting(
							beginningOfLineIndex,
							beginningOfLineIndex + line.length() + options.lineSeparatorSequence.length);
						formattedSource.append(line);
						beginningOfLineIndex = beginningOfLineIndex + line.length();
						if ((line = reader.readLine()) != null) {
							formattedSource.append(options.lineSeparatorSequence);
							beginningOfLineIndex += options.lineSeparatorSequence.length;
							dumpTab(currentLineIndentationLevel);
						}
					}
					reader.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			} else {
				updateMappedPositionsWhileSplitting(
					beginningOfLineIndex,
					beginningOfLineIndex + max);
				for (int i = 0; i < max; i++) {
					char currentChar = currentString.charAt(i);
					switch (currentChar) {
						case '\r' :
							break;
						case '\n' :
							if (i != max - 1) {
								// fix for 1FFYL5C: LFCOM:ALL - Incorrect indentation when split with a comment inside a condition
								// a substring cannot end with a lineSeparatorSequence,
								// except if it has been added by format() after a one-line comment
								formattedSource.append(options.lineSeparatorSequence);
	
								// 1FGDDV6: LFCOM:WIN98 - Weird splitting on message expression
								dumpTab(depth - 1);
							}
							break;
						default :
							formattedSource.append(currentChar);
					}
				}
			}
			// update positions inside the mappedPositions table
			if (substringIndex != -1) {
				if (multipleLineCommentCounter == 0) {
					int startPosition =
						beginningOfLineIndex + startSubstringIndexes[substringIndex];
					updateMappedPositionsWhileSplitting(startPosition, startPosition + max);
				}

				// compute the splitDelta resulting with the operator and blank removal
				if (substringIndex + 1 != startSubstringIndexes.length) {
					increaseSplitDelta(
						startSubstringIndexes[substringIndex]
							+ max
							- startSubstringIndexes[substringIndex + 1]);
				}
			}
			// dump postfix operator?
			if (placeOperatorBehind) {
				if (insertSpaceBefore(operator)) {
					formattedSource.append(' ');
					if (operator != 0) {
						increaseSplitDelta(1);
					}
				}
				formattedSource.append(operatorString);
				if (operator != 0) {
					increaseSplitDelta(operatorString.length());
				}
			}
			return;
		}
		// fix for 1FG0BA3: LFCOM:WIN98 - Weird splitting on interfaces
		// extends has to stand alone on a line when currentString has been split.
		if (options.maxLineLength != 0
			&& splitLine != null
			&& (operator == TokenNameextends
				|| operator == TokenNameimplements
				|| operator == TokenNamethrows)) {
			formattedSource.append(options.lineSeparatorSequence);
			increaseSplitDelta(options.lineSeparatorSequence.length);
			dumpTab(depth + 1);
		} else {
			if (operator == TokenNameextends
				|| operator == TokenNameimplements
				|| operator == TokenNamethrows) {
				formattedSource.append(' ');
				increaseSplitDelta(1);
			}
		}
		// perform actual splitting
		String result[] = splitLine.substrings;
		int[] splitOperators = splitLine.operators;

		if (result[0].length() == 0) {
			// when the substring 0 is null, the substring 1 is correctly indented.
			depth--;
			emptyFirstSubString = true;
		}
		// the operator going in front of the result[0] string is the operator parameter
		for (int i = 0, max = result.length; i < max; i++) {
			// the new depth is the current one if this is the first substring,
			// the current one + 1 otherwise.
			// if the substring is a comment, use the current indentation Level instead of the depth
			// (-1 because the ouputline increases depth).
			// (fix for 1FFC72R: LFCOM:ALL - Incorrect line split in presence of line comments)
			String currentResult = result[i];

			if (currentResult.length() != 0 || splitOperators[i] != 0) {
					int newDepth =
						(currentResult.startsWith("/*") //$NON-NLS-1$
							|| currentResult.startsWith("//")) //$NON-NLS-1$ 
								? indentationLevel - 1 : depth;
				outputLine(
					currentResult,
					i == 0 || (i == 1 && emptyFirstSubString) ? preIndented : false,
					i == 0 ? newDepth : newDepth + 1,
					splitOperators[i],
					i,
					splitLine.startSubstringsIndexes,
					currentString.indexOf(currentResult));
				if (i != max - 1) {
					formattedSource.append(options.lineSeparatorSequence);
					increaseSplitDelta(options.lineSeparatorSequence.length);
				}
			}
		}
		if (result.length == splitOperators.length - 1) {
			int lastOperator = splitOperators[result.length];
			String lastOperatorString = operatorString(lastOperator);
			formattedSource.append(options.lineSeparatorSequence);
			increaseSplitDelta(options.lineSeparatorSequence.length);

			if (breakLineBeforeOperator(lastOperator)) {
				dumpTab(depth + 1);
				if (lastOperator != 0) {
					if (insertSpaceBefore(lastOperator)) {
						formattedSource.append(' ');
						increaseSplitDelta(1);
					}
					formattedSource.append(lastOperatorString);
					increaseSplitDelta(lastOperatorString.length());

					if (insertSpaceAfter(lastOperator)
						&& lastOperator != TokenNameimplements
						&& lastOperator != TokenNameextends
						&& lastOperator != TokenNamethrows) {
						formattedSource.append(' ');
						increaseSplitDelta(1);
					}
				}
			}
		}
		if (placeOperatorBehind) {
			if (insertSpaceBefore(operator)) {
				formattedSource.append(' ');
				increaseSplitDelta(1);
			}
			formattedSource.append(operatorString);
			//increaseSplitDelta(operatorString.length());
		}
	}
	
	/**
	 * Pops the top statement of the stack if it is <code>token</code>
	 */
	private int pop(int token) {
		int delta = 0;
		if ((constructionsCount > 0)
			&& (constructions[constructionsCount - 1] == token)) {
			delta--;
			constructionsCount--;
		}
		return delta;
	}
	
	/**
	 * Pops the top statement of the stack if it is a <code>BLOCK</code> or a <code>NONINDENT_BLOCK</code>.
	 */
	private int popBlock() {
		int delta = 0;
		if ((constructionsCount > 0)
			&& ((constructions[constructionsCount - 1] == BLOCK)
				|| (constructions[constructionsCount - 1] == NONINDENT_BLOCK))) {
			if (constructions[constructionsCount - 1] == BLOCK)
				delta--;
			constructionsCount--;
		}
		return delta;
	}
	
	/**
	 * Pops elements until the stack is empty or the top element is <code>token</code>.<br>
	 * Does not remove <code>token</code> from the stack.
	 * @param token the token to be left as the top of the stack
	 */
	private int popExclusiveUntil(int token) {
		int delta = 0;
		int startCount = constructionsCount;
		for (int i = startCount - 1; i >= 0 && constructions[i] != token; i--) {
			if (constructions[i] != NONINDENT_BLOCK)
				delta--;
			constructionsCount--;
		}
		return delta;
	}
	
	/**
	 * Pops elements until the stack is empty or the top element is
	 * a <code>BLOCK</code> or a <code>NONINDENT_BLOCK</code>.<br>
	 * Does not remove it from the stack.
	 */
	private int popExclusiveUntilBlock() {
		int startCount = constructionsCount;
		int delta = 0;
		for (int i = startCount - 1;
			i >= 0 && constructions[i] != BLOCK && constructions[i] != NONINDENT_BLOCK;
			i--) {
			constructionsCount--;
			delta--;
		}
		return delta;
	}
	
	/**
	 * Pops elements until the stack is empty or the top element is
	 * a <code>BLOCK</code>, a <code>NONINDENT_BLOCK</code> or a <code>CASE</code>.<br>
	 * Does not remove it from the stack.
	 */
	private int popExclusiveUntilBlockOrCase() {
		int startCount = constructionsCount;
		int delta = 0;
		for (int i = startCount - 1;
			i >= 0
				&& constructions[i] != BLOCK
				&& constructions[i] != NONINDENT_BLOCK
				&& constructions[i] != TokenNamecase;
			i--) {
			constructionsCount--;
			delta--;
		}
		return delta;
	}
	
	/**
	 * Pops elements until the stack is empty or the top element is <code>token</code>.<br>
	 * Removes <code>token</code> from the stack too.
	 * @param token the token to remove from the stack
	 */
	private int popInclusiveUntil(int token) {
		int startCount = constructionsCount;
		int delta = 0;
		for (int i = startCount - 1; i >= 0 && constructions[i] != token; i--) {
			if (constructions[i] != NONINDENT_BLOCK)
				delta--;
			constructionsCount--;
		}
		if (constructionsCount > 0) {
			if (constructions[constructionsCount - 1] != NONINDENT_BLOCK)
				delta--;
			constructionsCount--;
		}
		return delta;
	}
	
	/**
	 * Pops elements until the stack is empty or the top element is
	 * a <code>BLOCK</code> or a <code>NONINDENT_BLOCK</code>.<br>
	 * Does not remove it from the stack.
	 */
	private int popInclusiveUntilBlock() {
		int startCount = constructionsCount;
		int delta = 0;
		for (int i = startCount - 1;
			i >= 0 && (constructions[i] != BLOCK && constructions[i] != NONINDENT_BLOCK);
			i--) {
			delta--;
			constructionsCount--;
		}
		if (constructionsCount > 0) {
			if (constructions[constructionsCount - 1] == BLOCK)
				delta--;
			constructionsCount--;
		}
		return delta;
	}
	
	/** 
	 * Pushes a block in the stack.<br>
	 * Pushes a <code>BLOCK</code> if the stack is empty or if the top element is a <code>BLOCK</code>,
	 * pushes <code>NONINDENT_BLOCK</code> otherwise.
	 * Creates a new bigger array if the current one is full.
	 */
	private int pushBlock() {
		int delta = 0;
		if (constructionsCount == constructions.length)
			System.arraycopy(
				constructions,
				0,
				(constructions = new int[constructionsCount * 2]),
				0,
				constructionsCount);

		if ((constructionsCount == 0)
			|| (constructions[constructionsCount - 1] == BLOCK)
			|| (constructions[constructionsCount - 1] == NONINDENT_BLOCK)
			|| (constructions[constructionsCount - 1] == TokenNamecase)) {
			delta++;
			constructions[constructionsCount++] = BLOCK;
		} else {
			constructions[constructionsCount++] = NONINDENT_BLOCK;
		}
		return delta;
	}
	
	/** 
	 * Pushes <code>token</code>.<br>
	 * Creates a new bigger array if the current one is full.
	 */
	private int pushControlStatement(int token) {
		if (constructionsCount == constructions.length)
			System.arraycopy(
				constructions,
				0,
				(constructions = new int[constructionsCount * 2]),
				0,
				constructionsCount);
		constructions[constructionsCount++] = token;
		return 1;
	}
	
	private static boolean separateFirstArgumentOn(int currentToken) {
		//return (currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON);
		return currentToken != TokenNameif
			&& currentToken != TokenNameLPAREN
			&& currentToken != TokenNameNOT
			&& currentToken != TokenNamewhile
			&& currentToken != TokenNamefor
			&& currentToken != TokenNameswitch;
	}
	
	/**
	 * Set the positions to map. The mapped positions should be retrieved using the
	 * getMappedPositions() method.
	 * @param positions int[]
	 * @deprecated Set the positions to map using the format(String, int, int[]) method.
	 * 
	 * @see #getMappedPositions()
	 */
	public void setPositionsToMap(int[] positions) {
		positionsToMap = positions;
		lineDelta = 0;
		globalDelta = 0;
		mappedPositions = new int[positions.length];
	}
		
	/** 
	 * Appends a space character to the current line buffer.
	 */
	private void space() {
		currentLineBuffer.append(' ');
		increaseLineDelta(1);
	}
	
	/**
	 * Splits <code>stringToSplit</code> on the top level token<br>
	 * If there are several identical token at the same level,
	 * the string is cut into many pieces.
	 * @return an object containing the operator and all the substrings
	 * or null if the string cannot be split
	 */
	public SplitLine split(String stringToSplit) {
		return split(stringToSplit, 0);
	}
	
	/**
	 * Splits <code>stringToSplit</code> on the top level token<br>
	 * If there are several identical token at the same level,
	 * the string is cut into many pieces.
	 * @return an object containing the operator and all the substrings
	 * or null if the string cannot be split
	 */
	public SplitLine split(String stringToSplit, int offsetInGlobalLine) {
		/*
		 * See http://dev.eclipse.org/bugs/show_bug.cgi?id=12540 and
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14387 
		 */
		if (stringToSplit.indexOf("//$NON-NLS") != -1) { //$NON-NLS-1$
			return null;
		}
		// local variables
		int currentToken = 0;
		int splitTokenType = 0;
		int splitTokenDepth = Integer.MAX_VALUE;
		int splitTokenPriority = Integer.MAX_VALUE;

		int[] substringsStartPositions = new int[10];
		// contains the start position of substrings
		int[] substringsEndPositions = new int[10];
		// contains the start position of substrings
		int substringsCount = 1; // index in the substringsStartPosition array
		int[] splitOperators = new int[10];
		// contains the start position of substrings
		int splitOperatorsCount = 0; // index in the substringsStartPosition array
		int[] openParenthesisPosition = new int[10];
		int openParenthesisPositionCount = 0;
		int position = 0;
		int lastOpenParenthesisPosition = -1;
		// used to remember the position of the 1st open parenthesis
		// needed for a pattern like: A.B(C); we want formatted like A.B( split C);
		// setup the scanner with a new source
		int lastCommentStartPosition = -1;
		// to remember the start position of the last comment
		int firstTokenOnLine = -1;
		// to remember the first token of the line
		int previousToken = -1;
		// to remember the previous token.
		splitScanner.setSource(stringToSplit.toCharArray());

		try {
			// start the loop
			while (true) {
				// takes the next token
				try {
					if (currentToken != Scanner.TokenNameWHITESPACE)
						previousToken = currentToken;
					currentToken = splitScanner.getNextToken();
				} catch (InvalidInputException e) {
					if (!handleInvalidToken(e))
						throw e;
					currentToken = 0; // this value is not modify when an exception is raised.
				}
				if (currentToken == TokenNameEOF)
					break;

				if (firstTokenOnLine == -1) {
					firstTokenOnLine = currentToken;
				}
				switch (currentToken) {
					case TokenNameRBRACE :
					case TokenNameRPAREN :
						if (openParenthesisPositionCount > 0) {
							if (openParenthesisPositionCount == 1
								&& lastOpenParenthesisPosition < openParenthesisPosition[0]) {
								lastOpenParenthesisPosition = openParenthesisPosition[0];
							} else if (
								(splitTokenDepth == Integer.MAX_VALUE)
									|| (splitTokenDepth > openParenthesisPositionCount
										&& openParenthesisPositionCount == 1)) {
								splitTokenType = 0;
								splitTokenDepth = openParenthesisPositionCount;
								splitTokenPriority = Integer.MAX_VALUE;
								substringsStartPositions[0] = 0;
								// better token means the whole line until now is the first substring
								substringsCount = 1; // resets the count of substrings
								substringsEndPositions[0] = openParenthesisPosition[0];
								// substring ends on operator start
								position = openParenthesisPosition[0];
								// the string mustn't be cut before the closing parenthesis but after the opening one.
								splitOperatorsCount = 1; // resets the count of split operators
								splitOperators[0] = 0;
							}
							openParenthesisPositionCount--;
						}
						break;
					case TokenNameLBRACE :
					case TokenNameLPAREN :
						if (openParenthesisPositionCount == openParenthesisPosition.length) {
							System.arraycopy(
								openParenthesisPosition,
								0,
								(openParenthesisPosition = new int[openParenthesisPositionCount * 2]),
								0,
								openParenthesisPositionCount);
						}
						openParenthesisPosition[openParenthesisPositionCount++] =
							splitScanner.currentPosition;
						if (currentToken == TokenNameLPAREN && previousToken == TokenNameRPAREN) {
							openParenthesisPosition[openParenthesisPositionCount - 1] =
								splitScanner.startPosition;
						}
						break;
					case TokenNameSEMICOLON : // ;
					case TokenNameCOMMA : // ,
					case TokenNameEQUAL : // =
						if (openParenthesisPositionCount < splitTokenDepth
							|| (openParenthesisPositionCount == splitTokenDepth
								&& splitTokenPriority > getTokenPriority(currentToken))) {
							// the current token is better than the one we currently have
							// (in level or in priority if same level)
							// reset the substringsCount
							splitTokenDepth = openParenthesisPositionCount;
							splitTokenType = currentToken;
							splitTokenPriority = getTokenPriority(currentToken);
							substringsStartPositions[0] = 0;
							// better token means the whole line until now is the first substring

							if (separateFirstArgumentOn(firstTokenOnLine)
								&& openParenthesisPositionCount > 0) {
								substringsCount = 2; // resets the count of substrings

								substringsEndPositions[0] = openParenthesisPosition[splitTokenDepth - 1];
								substringsStartPositions[1] = openParenthesisPosition[splitTokenDepth - 1];
								substringsEndPositions[1] = splitScanner.startPosition;
								splitOperatorsCount = 2; // resets the count of split operators
								splitOperators[0] = 0;
								splitOperators[1] = currentToken;
								position = splitScanner.currentPosition;
								// next substring will start from operator end
							} else {
								substringsCount = 1; // resets the count of substrings

								substringsEndPositions[0] = splitScanner.startPosition;
								// substring ends on operator start
								position = splitScanner.currentPosition;
								// next substring will start from operator end
								splitOperatorsCount = 1; // resets the count of split operators
								splitOperators[0] = currentToken;
							}
						} else {
							if ((openParenthesisPositionCount == splitTokenDepth
								&& splitTokenPriority == getTokenPriority(currentToken))
								&& splitTokenType != TokenNameEQUAL
								&& currentToken != TokenNameEQUAL) {
								// fix for 1FG0BCN: LFCOM:WIN98 - Missing one indentation after split
								// take only the 1st = into account.
								// if another token with the same priority is found,
								// push the start position of the substring and
								// push the token into the stack.
								// create a new array object if the current one is full.
								if (substringsCount == substringsStartPositions.length) {
									System.arraycopy(
										substringsStartPositions,
										0,
										(substringsStartPositions = new int[substringsCount * 2]),
										0,
										substringsCount);
									System.arraycopy(
										substringsEndPositions,
										0,
										(substringsEndPositions = new int[substringsCount * 2]),
										0,
										substringsCount);
								}
								if (splitOperatorsCount == splitOperators.length) {
									System.arraycopy(
										splitOperators,
										0,
										(splitOperators = new int[splitOperatorsCount * 2]),
										0,
										splitOperatorsCount);
								}
								substringsStartPositions[substringsCount] = position;
								substringsEndPositions[substringsCount++] = splitScanner.startPosition;
								// substring ends on operator start
								position = splitScanner.currentPosition;
								// next substring will start from operator end
								splitOperators[splitOperatorsCount++] = currentToken;
							}
						}
						break;

					case TokenNameCOLON : // : (15.24)
						// see 1FK7C5R, we only split on a colon, when it is associated with a question-mark.
						// indeed it might appear also behind a case statement, and we do not to break at this point.
						if ((splitOperatorsCount == 0)
							|| splitOperators[splitOperatorsCount - 1] != TokenNameQUESTION) {
							break;
						}
					case TokenNameextends :
					case TokenNameimplements :
					case TokenNamethrows :

					case TokenNameDOT : // .
					case TokenNameMULTIPLY : // * (15.16.1)
					case TokenNameDIVIDE : // / (15.16.2)
					case TokenNameREMAINDER : // % (15.16.3)
					case TokenNamePLUS : // + (15.17, 15.17.2)
					case TokenNameMINUS : // - (15.17.2)
					case TokenNameLEFT_SHIFT : // << (15.18)
					case TokenNameRIGHT_SHIFT : // >> (15.18)
					case TokenNameUNSIGNED_RIGHT_SHIFT : // >>> (15.18)
					case TokenNameLESS : // < (15.19.1)
					case TokenNameLESS_EQUAL : // <= (15.19.1)
					case TokenNameGREATER : // > (15.19.1)
					case TokenNameGREATER_EQUAL : // >= (15.19.1)
					case TokenNameinstanceof : // instanceof
					case TokenNameEQUAL_EQUAL : // == (15.20, 15.20.1, 15.20.2, 15.20.3)
					case TokenNameNOT_EQUAL : // != (15.20, 15.20.1, 15.20.2, 15.20.3)
					case TokenNameAND : // & (15.21, 15.21.1, 15.21.2)
					case TokenNameOR : // | (15.21, 15.21.1, 15.21.2)
					case TokenNameXOR : // ^ (15.21, 15.21.1, 15.21.2)
					case TokenNameAND_AND : // && (15.22)
					case TokenNameOR_OR : // || (15.23)
					case TokenNameQUESTION : // ? (15.24)
					case TokenNameMULTIPLY_EQUAL : // *= (15.25.2)
					case TokenNameDIVIDE_EQUAL : // /= (15.25.2)
					case TokenNameREMAINDER_EQUAL : // %= (15.25.2)
					case TokenNamePLUS_EQUAL : // += (15.25.2)
					case TokenNameMINUS_EQUAL : // -= (15.25.2)
					case TokenNameLEFT_SHIFT_EQUAL : // <<= (15.25.2)
					case TokenNameRIGHT_SHIFT_EQUAL : // >>= (15.25.2)
					case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL : // >>>= (15.25.2)
					case TokenNameAND_EQUAL : // &= (15.25.2)
					case TokenNameXOR_EQUAL : // ^= (15.25.2)
					case TokenNameOR_EQUAL : // |= (15.25.2)

						if ((openParenthesisPositionCount < splitTokenDepth
							|| (openParenthesisPositionCount == splitTokenDepth
								&& splitTokenPriority > getTokenPriority(currentToken)))
							&& !((currentToken == TokenNamePLUS || currentToken == TokenNameMINUS)
								&& (previousToken == TokenNameLBRACE
									|| previousToken == TokenNameLBRACKET
									|| splitScanner.startPosition == 0))) {
							// the current token is better than the one we currently have
							// (in level or in priority if same level)
							// reset the substringsCount
							splitTokenDepth = openParenthesisPositionCount;
							splitTokenType = currentToken;
							splitTokenPriority = getTokenPriority(currentToken);
							substringsStartPositions[0] = 0;
							// better token means the whole line until now is the first substring

							if (separateFirstArgumentOn(firstTokenOnLine)
								&& openParenthesisPositionCount > 0) {
								substringsCount = 2; // resets the count of substrings

								substringsEndPositions[0] = openParenthesisPosition[splitTokenDepth - 1];
								substringsStartPositions[1] = openParenthesisPosition[splitTokenDepth - 1];
								substringsEndPositions[1] = splitScanner.startPosition;
								splitOperatorsCount = 3; // resets the count of split operators
								splitOperators[0] = 0;
								splitOperators[1] = 0;
								splitOperators[2] = currentToken;
								position = splitScanner.currentPosition;
								// next substring will start from operator end
							} else {
								substringsCount = 1; // resets the count of substrings

								substringsEndPositions[0] = splitScanner.startPosition;
								// substring ends on operator start
								position = splitScanner.currentPosition;
								// next substring will start from operator end
								splitOperatorsCount = 2; // resets the count of split operators
								splitOperators[0] = 0;
								// nothing for first operand since operator will be inserted in front of the second operand
								splitOperators[1] = currentToken;

							}
						} else {
							if (openParenthesisPositionCount == splitTokenDepth
								&& splitTokenPriority == getTokenPriority(currentToken)) {
								// if another token with the same priority is found,
								// push the start position of the substring and
								// push the token into the stack.
								// create a new array object if the current one is full.
								if (substringsCount == substringsStartPositions.length) {
									System.arraycopy(
										substringsStartPositions,
										0,
										(substringsStartPositions = new int[substringsCount * 2]),
										0,
										substringsCount);
									System.arraycopy(
										substringsEndPositions,
										0,
										(substringsEndPositions = new int[substringsCount * 2]),
										0,
										substringsCount);
								}
								if (splitOperatorsCount == splitOperators.length) {
									System.arraycopy(
										splitOperators,
										0,
										(splitOperators = new int[splitOperatorsCount * 2]),
										0,
										splitOperatorsCount);
								}
								substringsStartPositions[substringsCount] = position;
								substringsEndPositions[substringsCount++] = splitScanner.startPosition;
								// substring ends on operator start
								position = splitScanner.currentPosition;
								// next substring will start from operator end
								splitOperators[splitOperatorsCount++] = currentToken;
							}
						}
					default :
						break;
				}
				if (isComment(currentToken)) {
					lastCommentStartPosition = splitScanner.startPosition;
				} else {
					lastCommentStartPosition = -1;
				}
			}
		} catch (InvalidInputException e) {
			return null;
		}
		// if the string cannot be split, return null.
		if (splitOperatorsCount == 0)
			return null;

		// ## SPECIAL CASES BEGIN
		if (((splitOperatorsCount == 2
			&& splitOperators[1] == TokenNameDOT
			&& splitTokenDepth == 0
			&& lastOpenParenthesisPosition > -1)
			|| (splitOperatorsCount > 2
				&& splitOperators[1] == TokenNameDOT
				&& splitTokenDepth == 0
				&& lastOpenParenthesisPosition > -1
				&& lastOpenParenthesisPosition <= options.maxLineLength)
			|| (separateFirstArgumentOn(firstTokenOnLine)
				&& splitTokenDepth > 0
				&& lastOpenParenthesisPosition > -1))
			&& (lastOpenParenthesisPosition < splitScanner.source.length
				&& splitScanner.source[lastOpenParenthesisPosition] != ')')) {
			// fix for 1FH4J2H: LFCOM:WINNT - Formatter - Empty parenthesis should not be broken on two lines
			// only one split on a top level .
			// or more than one split on . and substring before open parenthesis fits one line.
			// or split inside parenthesis and first token is not a for/while/if
			SplitLine sl =
				split(
					stringToSplit.substring(lastOpenParenthesisPosition),
					lastOpenParenthesisPosition);
			if (sl == null || sl.operators[0] != TokenNameCOMMA) {
				// trim() is used to remove the extra blanks at the end of the substring. See PR 1FGYPI1
				return new SplitLine(
					new int[] { 0, 0 },
					new String[] {
						stringToSplit.substring(0, lastOpenParenthesisPosition).trim(),
						stringToSplit.substring(lastOpenParenthesisPosition)},
					new int[] {
						offsetInGlobalLine,
						lastOpenParenthesisPosition + offsetInGlobalLine });
			} else {
				// right substring can be split and is split on comma
				// copy substrings and operators
				// except if the 1st string is empty.
				int startIndex = (sl.substrings[0].length() == 0) ? 1 : 0;
				int subStringsLength = sl.substrings.length + 1 - startIndex;
				String[] result = new String[subStringsLength];
				int[] startIndexes = new int[subStringsLength];
				int operatorsLength = sl.operators.length + 1 - startIndex;
				int[] operators = new int[operatorsLength];

				result[0] = stringToSplit.substring(0, lastOpenParenthesisPosition);
				operators[0] = 0;

				System.arraycopy(
					sl.startSubstringsIndexes,
					startIndex,
					startIndexes,
					1,
					subStringsLength - 1);
				for (int i = subStringsLength - 1; i >= 0; i--) {
					startIndexes[i] += offsetInGlobalLine;
				}
				System.arraycopy(sl.substrings, startIndex, result, 1, subStringsLength - 1);
				System.arraycopy(sl.operators, startIndex, operators, 1, operatorsLength - 1);

				return new SplitLine(operators, result, startIndexes);
			}
		}
		// if the last token is a comment and the substring before the comment fits on a line,
		// split before the comment and return the result.
		if (lastCommentStartPosition > -1
			&& lastCommentStartPosition < options.maxLineLength
			&& splitTokenPriority > 50) {
			int end = lastCommentStartPosition;
			int start = lastCommentStartPosition;
			if (stringToSplit.charAt(end - 1) == ' ') {
				end--;
			}
			if (start != end && stringToSplit.charAt(start) == ' ') {
				start++;
			}
			return new SplitLine(
				new int[] { 0, 0 },
				new String[] { stringToSplit.substring(0, end), stringToSplit.substring(start)},
				new int[] { 0, start });
		}
		if (position != stringToSplit.length()) {
			if (substringsCount == substringsStartPositions.length) {
				System.arraycopy(
					substringsStartPositions,
					0,
					(substringsStartPositions = new int[substringsCount * 2]),
					0,
					substringsCount);
				System.arraycopy(
					substringsEndPositions,
					0,
					(substringsEndPositions = new int[substringsCount * 2]),
					0,
					substringsCount);
			}
			// avoid empty extra substring, e.g. line terminated with a semi-colon
			substringsStartPositions[substringsCount] = position;
			substringsEndPositions[substringsCount++] = stringToSplit.length();
		}
		if (splitOperatorsCount == splitOperators.length) {
			System.arraycopy(
				splitOperators,
				0,
				(splitOperators = new int[splitOperatorsCount * 2]),
				0,
				splitOperatorsCount);
		}
		splitOperators[splitOperatorsCount] = 0;

		// the last element of the stack is the position of the end of StringToSPlit
		// +1 because the substring method excludes the last character
		String[] result = new String[substringsCount];
		for (int i = 0; i < substringsCount; i++) {
			int start = substringsStartPositions[i];
			int end = substringsEndPositions[i];
			if (stringToSplit.charAt(start) == ' ') {
				start++;
				substringsStartPositions[i]++;
			}
			if (end != start && stringToSplit.charAt(end - 1) == ' ') {
				end--;
			}
			result[i] = stringToSplit.substring(start, end);
			substringsStartPositions[i] += offsetInGlobalLine;
		}
		if (splitOperatorsCount > substringsCount) {
			System.arraycopy(
				substringsStartPositions,
				0,
				(substringsStartPositions = new int[splitOperatorsCount]),
				0,
				substringsCount);
			System.arraycopy(
				substringsEndPositions,
				0,
				(substringsEndPositions = new int[splitOperatorsCount]),
				0,
				substringsCount);
			for (int i = substringsCount; i < splitOperatorsCount; i++) {
				substringsStartPositions[i] = position;
				substringsEndPositions[i] = position;
			}
			System.arraycopy(
				splitOperators,
				0,
				(splitOperators = new int[splitOperatorsCount]),
				0,
				splitOperatorsCount);
		} else {
			System.arraycopy(
				substringsStartPositions,
				0,
				(substringsStartPositions = new int[substringsCount]),
				0,
				substringsCount);
			System.arraycopy(
				substringsEndPositions,
				0,
				(substringsEndPositions = new int[substringsCount]),
				0,
				substringsCount);
			System.arraycopy(
				splitOperators,
				0,
				(splitOperators = new int[substringsCount]),
				0,
				substringsCount);
		}
		SplitLine splitLine =
			new SplitLine(splitOperators, result, substringsStartPositions);
		return splitLine;
	}

	private void updateMappedPositions(int startPosition) {
		if (positionsToMap == null) {
			return;
		}
		char[] source = scanner.source;
		int sourceLength = source.length;
		while (indexToMap < positionsToMap.length
			&& positionsToMap[indexToMap] <= startPosition) {
			int posToMap = positionsToMap[indexToMap];
			if (posToMap < 0
				|| posToMap >= sourceLength) { // protection against out of bounds position
				if (posToMap == sourceLength) {
					mappedPositions[indexToMap] = formattedSource.length();
				}
				indexToMap = positionsToMap.length; // no more mapping
				return;
			}
			if (Character.isWhitespace(source[posToMap])) {
				mappedPositions[indexToMap] = startPosition + globalDelta + lineDelta;
			} else {
				if (posToMap == sourceLength - 1) {
					mappedPositions[indexToMap] = startPosition + globalDelta + lineDelta;
				} else {
					mappedPositions[indexToMap] = posToMap + globalDelta + lineDelta;
				}
			}
			indexToMap++;
		}
	}
	
	private void updateMappedPositionsWhileSplitting(
		int startPosition,
		int endPosition) {
		if (mappedPositions == null || mappedPositions.length == indexInMap)
			return;

		while (indexInMap < mappedPositions.length
			&& startPosition <= mappedPositions[indexInMap]
			&& mappedPositions[indexInMap] < endPosition
			&& indexInMap < indexToMap) {
			mappedPositions[indexInMap] += splitDelta;
			indexInMap++;
		}
	}
	
	private int getLength(String s, int tabDepth) {
		int length = 0;
		for (int i = 0; i < tabDepth; i++) {
			length += options.tabSize;
		}
		for (int i = 0, max = s.length(); i < max; i++) {
			char currentChar = s.charAt(i);
			switch (currentChar) {
				case '\t' :
					length += options.tabSize;
					break;
				default :
					length++;
			}
		}
		return length;
	}
	
	/** 
	* Sets the initial indentation level
	* @param indentationLevel new indentation level
	* 
	* @deprecated
	*/
	public void setInitialIndentationLevel(int newIndentationLevel) {
		this.initialIndentationLevel =
			currentLineIndentationLevel = indentationLevel = newIndentationLevel;
	}
}