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

package org.aspectj.tools.ajbrowser.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.aspectj.ajde.EditorAdapter;
import org.aspectj.ajde.EditorListener;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.tools.ajbrowser.core.BrowserErrorHandler;

/**
 * Responsible for controlling the editor.
 * 
 * <p><b>TODO:</b> remove coupling to <CODE>BasicEditor</CODE></p>
 * @author Mik Kersten
 */
public class EditorManager {

	/** @return true if input modifiers have shift down */
	public static boolean isShiftDown(int modifiers) {
		return (0 != (modifiers & KeyEvent.SHIFT_MASK));
	}

	private EditorAdapter editor = null;
	private BasicEditor basicEditor = null;
	private List<EditorListener> editorListeners = new ArrayList<>();
	private Vector<EditorAdapter> editors = new Vector<>();
	private JPanel editor_panel = null;
	private Box editors_box = Box.createVerticalBox();

	public EditorManager(EditorAdapter ajdeEditor) {
		if (ajdeEditor instanceof BasicEditor) {
			this.basicEditor = (BasicEditor) ajdeEditor;
			editors.add(basicEditor);
			editors_box.add(basicEditor.getPanel());
			editor_panel = new JPanel(new BorderLayout());
			editor_panel.add(editors_box, BorderLayout.CENTER);
		} else {
			editors.add(ajdeEditor);
			this.editor = ajdeEditor;
		}
	}

	public void addListener(EditorListener editorListener) {
		editorListeners.add(editorListener);
	}

	public void removeListener(EditorListener editorListener) {
		editorListeners.remove(editorListener);
	}

	public void notifyCurrentFileChanged(String filePath) {
		for (EditorListener editorListener : editorListeners) {
			editorListener.currentFileChanged(filePath);
		}
	}

	public void addViewForSourceLine(final String filePath, final int lineNumber) {
		if (basicEditor == null)
			return;
		editors_box.remove(basicEditor.getPanel());
		final BasicEditor newEditor = new BasicEditor();
		editors.add(newEditor);

		Runnable update = new Runnable() {
			public void run() {
				editors_box.add(newEditor.getPanel());
				newEditor.showSourceLine(filePath, lineNumber, true);
				// AjdeUIManager.getDefault().getIdeUIAdapter().resetGUI();
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			update.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(update);
			} catch (Exception e) {
				BrowserErrorHandler.handleError("Could not add view for source line.", e);
			}
		}
	}

	public String getCurrFile() {
		if (basicEditor != null) {
			return basicEditor.getCurrFile();
		} else {
			return editor.getCurrFile();
		}
	}

	public void showSourceLine(ISourceLocation sourceLocation, boolean highlight) {
		if (sourceLocation != null) {
			showSourceLine(sourceLocation.getSourceFile().getAbsolutePath(), sourceLocation.getLine(), highlight);
		}
	}

	/**
	 * <b>TODO</b>: remove "instanceof AjdeManager" hack
	 */
	public void showSourceLine(String filePath, int lineNumber, boolean highlight) {
		if (editors.size() > 1) {
			editors_box.removeAll();
			editors_box.add(basicEditor.getPanel());
			// AjdeUIManager.getDefault().getIdeUIAdapter().resetGUI();
			editors.removeAllElements();
			editors.add(basicEditor);
		}

		if (basicEditor != null) {
			basicEditor.showSourceLine(filePath, lineNumber, highlight);
		} else {
			editor.showSourceLine(filePath, lineNumber, highlight);
		}
	}

	public void pasteToCaretPos(String text) {
		if (basicEditor != null) {
			basicEditor.pasteToCaretPos(text);
		} else {
			editor.pasteToCaretPos(text);
		}
	}

	public void showSourcelineAnnotation(String filePath, int lineNumber, java.util.List items) {
		editor.showSourcelineAnnotation(filePath, lineNumber, items);
	}

	public void saveContents() {
		try {
			for (EditorAdapter editorAdapter : editors) {
				editorAdapter.saveContents();
			}
		} catch (IOException ioe) {
			BrowserErrorHandler.handleError("Editor could not save the current file.", ioe);
		}
	}

	public JPanel getEditorPanel() {
		if (editor_panel != null) {
			return editor_panel;
		} else {
			return basicEditor.getPanel();
		}
	}
}
