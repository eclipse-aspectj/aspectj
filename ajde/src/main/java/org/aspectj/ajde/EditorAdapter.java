/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.ajde;

import java.io.IOException;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;

/**
 * @author	Mik Kersten
 */
public interface EditorAdapter {

	/**
	 * Seek the editor to a source line in the file specified.
	 */
	void showSourceLine(String filePath, int lineNumber, boolean highlight);

	/**
	 * Seek the editor to a SourceLocation and highlight if specified.
	 */
	void showSourceLine(ISourceLocation sourceLocation, boolean highlight);

	/**
	 * Seek the editor to a source line in the current file.
	 */
	void showSourceLine(int lineNumber, boolean highlight);

	/**
	 * @return	full path to the file currently being edited.
	 */
	String getCurrFile();

	/**
	 * Save the contents of the current file being edited.
	 */
	void saveContents() throws IOException;

	/**
	 * Paste text into the current caret position of the editor.
	 */
	void pasteToCaretPos(String text);

	/**
	 * Implement if inline annotations are supported by the editor.  Make null
	 * implementation if inline annotations are not supported.
	 *
	 * @param	filePath	path to the file that should get the annotation
	 * @param	lineNumber	line number for the annotation
	 * @param	items		list of relations to be rendered as the annotation
	 */
	void showSourcelineAnnotation(String filePath, int lineNumber, List items);

	/**
	 * Implement if multipe editor views are supported by the editor.  Make null
	 * implementation if multiple editor views are not supported.
	 *
	 * @param	filePath	path to the source file
	 * @param	lineNumber	line number of the sourceline
	 */
	//public void addEditorViewForSourceLine(String filePath, int lineNumber);
}
