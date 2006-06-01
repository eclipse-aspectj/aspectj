/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.IOException;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;

/**
 * @author beatmik
 */
public class NullIdeEditorAdapter implements EditorAdapter {

	public void showSourceLine(
		String filePath,
		int lineNumber,
		boolean highlight) {

	}

	public void showSourceLine(
		ISourceLocation sourceLocation,
		boolean highlight) {

	}

	public void showSourceLine(int lineNumber, boolean highlight) {

	}

	public String getCurrFile() {
		return null;
	}

	public void saveContents() throws IOException {
	}

	public void pasteToCaretPos(String text) {

	}


	public void showSourcelineAnnotation(
		String filePath,
		int lineNumber,
		List items) {

	}

}
