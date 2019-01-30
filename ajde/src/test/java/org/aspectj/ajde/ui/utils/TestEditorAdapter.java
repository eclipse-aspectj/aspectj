/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.ui.utils;

import java.io.IOException;
import java.util.List;

import org.aspectj.ajde.EditorAdapter;
import org.aspectj.bridge.ISourceLocation;

/**
 * EditorAdapter with empty implementation
 */
public class TestEditorAdapter implements EditorAdapter {

	public String getCurrFile() {
		return null;
	}

	public void pasteToCaretPos(String text) {
	}

	public void saveContents() throws IOException {
	}

	public void showSourceLine(String filePath, int lineNumber,
			boolean highlight) {
	}

	public void showSourceLine(ISourceLocation sourceLocation, boolean highlight) {
	}

	public void showSourceLine(int lineNumber, boolean highlight) {
	}

	public void showSourcelineAnnotation(String filePath, int lineNumber,
			List items) {
	}

}
