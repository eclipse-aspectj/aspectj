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
import java.util.Map;

public class FormatterOptions {	
	/**
	 * Option IDs
	 */
	public static final String OPTION_InsertNewlineBeforeOpeningBrace = "org.eclipse.jdt.core.formatter.newline.openingBrace"; //$NON-NLS-1$
	public static final String OPTION_InsertNewlineInControlStatement = "org.eclipse.jdt.core.formatter.newline.controlStatement"; //$NON-NLS-1$
	public static final String OPTION_InsertNewLineBetweenElseAndIf = "org.eclipse.jdt.core.formatter.newline.elseIf"; //$NON-NLS-1$
	public static final String OPTION_InsertNewLineInEmptyBlock = "org.eclipse.jdt.core.formatter.newline.emptyBlock"; //$NON-NLS-1$
	public static final String OPTION_ClearAllBlankLines = "org.eclipse.jdt.core.formatter.newline.clearAll"; //$NON-NLS-1$
	public static final String OPTION_SplitLineExceedingLength = "org.eclipse.jdt.core.formatter.lineSplit"; //$NON-NLS-1$
	public static final String OPTION_CompactAssignment = "org.eclipse.jdt.core.formatter.style.assignment"; //$NON-NLS-1$
	public static final String OPTION_TabulationChar = "org.eclipse.jdt.core.formatter.tabulation.char"; //$NON-NLS-1$
	public static final String OPTION_TabulationSize = "org.eclipse.jdt.core.formatter.tabulation.size"; //$NON-NLS-1$
	
	public static final String INSERT = "insert"; //$NON-NLS-1$
	public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
	public static final String PRESERVE_ONE = "preserve one"; //$NON-NLS-1$
	public static final String CLEAR_ALL = "clear all"; //$NON-NLS-1$
	public static final String NORMAL = "normal"; //$NON-NLS-1$
	public static final String COMPACT = "compact"; //$NON-NLS-1$
	public static final String TAB = "tab"; //$NON-NLS-1$
	public static final String SPACE = "space"; //$NON-NLS-1$
	
	// by default, do not insert blank line before opening brace
	public boolean newLineBeforeOpeningBraceMode = false;

	// by default, do not insert blank line behind keywords (ELSE, CATCH, FINALLY,...) in control statements
	public boolean newlineInControlStatementMode = false;

	// by default, preserve one blank line per sequence of blank lines
	public boolean clearAllBlankLinesMode = false;
	
	// line splitting will occur when line exceeds this length
	public int maxLineLength = 80;

	public boolean compactAssignmentMode = false; // if isTrue, assignments look like x= 12 (not like x = 12);

	//number of consecutive spaces used to replace the tab char
	public int tabSize = 4; // n spaces for one tab
	public boolean indentWithTab = true;

	public boolean compactElseIfMode = true; // if true, else and if are kept on the same line.
	public boolean newLineInEmptyBlockMode = true; // if false, no new line in {} if it's empty.
	
	public char[] lineSeparatorSequence = System.getProperty("line.separator").toCharArray(); //$NON-NLS-1$
/** 
 * Initializing the formatter options with default settings
 */
public FormatterOptions(){
}
/** 
 * Initializing the formatter options with external settings
 */
public FormatterOptions(Map settings){
	if (settings == null) return;

	// filter options which are related to the assist component
	Object[] entries = settings.entrySet().toArray();
	for (int i = 0, max = entries.length; i < max; i++){
		Map.Entry entry = (Map.Entry)entries[i];
		if (!(entry.getKey() instanceof String)) continue;
		if (!(entry.getValue() instanceof String)) continue;
		String optionID = (String) entry.getKey();
		String optionValue = (String) entry.getValue();
		
		if(optionID.equals(OPTION_InsertNewlineBeforeOpeningBrace)){
			if (optionValue.equals(INSERT)){
				this.newLineBeforeOpeningBraceMode = true;
			} else if (optionValue.equals(DO_NOT_INSERT)){
				this.newLineBeforeOpeningBraceMode = false;
			}
			continue;
		}
		if(optionID.equals(OPTION_InsertNewlineInControlStatement)){
			if (optionValue.equals(INSERT)){
				this.newlineInControlStatementMode = true;
			} else if (optionValue.equals(DO_NOT_INSERT)){
				this.newlineInControlStatementMode = false;
			}
			continue;
		}
		if(optionID.equals(OPTION_ClearAllBlankLines)){
			if (optionValue.equals(CLEAR_ALL)){
				this.clearAllBlankLinesMode = true;
			} else if (optionValue.equals(PRESERVE_ONE)){
				this.clearAllBlankLinesMode = false;
			}
			continue;
		}
		if(optionID.equals(OPTION_InsertNewLineBetweenElseAndIf)){
			if (optionValue.equals(INSERT)){
				this.compactElseIfMode = false;
			} else if (optionValue.equals(DO_NOT_INSERT)){
				this.compactElseIfMode = true;
			}
			continue;
		}
		if(optionID.equals(OPTION_InsertNewLineInEmptyBlock)){
			if (optionValue.equals(INSERT)){
				this.newLineInEmptyBlockMode = true;
			} else if (optionValue.equals(DO_NOT_INSERT)){
				this.newLineInEmptyBlockMode = false;
			}
			continue;
		}
		if(optionID.equals(OPTION_SplitLineExceedingLength)){
			try {
				int val = Integer.parseInt(optionValue);
				if (val >= 0) this.maxLineLength = val;
			} catch(NumberFormatException e){
			}
		}
		if(optionID.equals(OPTION_CompactAssignment)){
			if (optionValue.equals(COMPACT)){
				this.compactAssignmentMode = true;
			} else if (optionValue.equals(NORMAL)){
				this.compactAssignmentMode = false;
			}
			continue;
		}
		if(optionID.equals(OPTION_TabulationChar)){
			if (optionValue.equals(TAB)){
				this.indentWithTab = true;
			} else if (optionValue.equals(SPACE)){
				this.indentWithTab = false;
			}
			continue;
		}
		if(optionID.equals(OPTION_TabulationSize)){
			try {
				int val = Integer.parseInt(optionValue);
				if (val > 0) this.tabSize = val;
			} catch(NumberFormatException e){
			}
		}
	}
}

/**
 * 
 * @return int
 */
public int getMaxLineLength() {
	return maxLineLength;
}
public int getTabSize() {
	return tabSize;
}
public boolean isAddingNewLineBeforeOpeningBrace() {
	return newLineBeforeOpeningBraceMode;
}
public boolean isAddingNewLineInControlStatement() {
	return newlineInControlStatementMode;
}
public boolean isAddingNewLineInEmptyBlock() {
	return newLineInEmptyBlockMode;
}
public boolean isClearingAllBlankLines() {
	return clearAllBlankLinesMode;
}
public boolean isCompactingAssignment() {
	return compactAssignmentMode;
}
public boolean isCompactingElseIf() {
	return compactElseIfMode;
}
public boolean isUsingTabForIndenting() {
	return indentWithTab;
}
public void setLineSeparator(String lineSeparator) {
	lineSeparatorSequence = lineSeparator.toCharArray();
}
/**
 * @deprecated - should use a Map when creating the options.
 */
public void setMaxLineLength(int maxLineLength) {
	this.maxLineLength = maxLineLength;
}
/**
 * @deprecated - should use a Map when creating the options.
 */
public void setCompactElseIfMode(boolean flag) {
	compactElseIfMode = flag;
}

}
