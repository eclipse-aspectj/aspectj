/*
 * Created on Jul 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.aspectj.ajde;

import java.io.IOException;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;

/**
 * @author beatmik
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
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
