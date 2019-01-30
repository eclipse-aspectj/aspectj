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
 *     Helen Hawkins  Converted to new interface (bug 148190) 
 * ******************************************************************/


package org.aspectj.tools.ajbrowser.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.EditorAdapter;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.tools.ajbrowser.BrowserManager;
import org.aspectj.tools.ajbrowser.core.BrowserErrorHandler;

/**
 * Bare-bones editor implementation used when the framework is being used
 * standalone.
 *
 * @author Mik Kersten
 */
public class BasicEditor implements EditorAdapter {

	private String NO_FILE = "<no file selected>";
    private String filePath = NO_FILE;
    private JPanel editor_panel = new JPanel();

    // @todo    get rid of these
    private int currHighlightStart = 0;
    private int currHighlightEnd = 0;

    private BorderLayout borderLayout1 = new BorderLayout();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JEditorPane editorPane = new JEditorPane();

    public BasicEditor() {
        try {
            editorPane.setEditable(true);
            editorPane.setContentType("text/plain");
            editorPane.setFont(new Font("Monospaced", 0, 11));
            editor_panel.add(editorPane);
            jbInit();
        }
        catch(Exception e) {
            BrowserErrorHandler.handleError("Could not initialize GUI.", e);
        }
    }

    public String getCurrFile() {
		return filePath;
    }

    public void showSourceLine(ISourceLocation sourceLocation, boolean highlight) {
    	try {
    		showSourceLine(sourceLocation.getSourceFile().getAbsolutePath(), sourceLocation.getLine(), highlight);
    	} catch (NullPointerException npe) {
    		Ajde.getDefault().getIdeUIAdapter().displayStatusInformation(" no corresponding source line to seek to");
    	}
    }
    	
    public void showSourceLine(int lineNumber, boolean highlight) {
        showSourceLine(filePath, lineNumber, highlight);
    }

    public void pasteToCaretPos(String text) {
        if (currHighlightEnd < 1) return;
        String contents = editorPane.getText();
        String pasted = contents.substring(0, currHighlightEnd) +
            text + contents.substring(currHighlightEnd, contents.length());
        editorPane.setText(pasted);
    }

    public void showSourceLine(String filePath, int lineNumber, boolean highlight) {
    	//AjdeUIManager.getDefault().getIdeUIAdapter().resetEditor();
        
        this.filePath = filePath;
//        if (oldPath != filePath && !Ajde.INSTANCE.BROWSER_MANAGER.isGlobalMode()) {
//            Ajde.INSTANCE.BROWSER_MANAGER.updateView();
//        }

//        Ajde.IDE_MANAGER.setEditorStatusText(filePath);

        currHighlightStart = 0;
        currHighlightEnd = 0;
        editorPane.setText(readFile(filePath, lineNumber));
        try {
            editorPane.getHighlighter().addHighlight(currHighlightStart, currHighlightEnd, DefaultHighlighter.DefaultPainter);
            editorPane.setCaretPosition(currHighlightStart);
        } catch (BadLocationException ble) {
        	BrowserErrorHandler.handleError("Could not highlight location.", ble);
        }
        BrowserManager.getDefault().getEditorManager().notifyCurrentFileChanged(filePath);
    }

    /**
     * Not implemented.
     */
    public void showSourcelineAnnotation(String filePath, int lineNumber, java.util.List items) { }

	public void addEditorViewForSourceLine(String filePath, int lineNumber) {
		
	}

    public void saveContents() throws IOException {
        if (!filePath.equals(NO_FILE) && !filePath.equals("") && !editorPane.getText().equals("")) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(editorPane.getText());
            writer.close();
        }
    }

    public JPanel getPanel() {
        return editor_panel;
    }

    public void showSourceForFile(String filePath) { }

    public void showSourceForLine(int lineNumber, boolean highlight) { }

    public void showSourceForSourceLine(String filePath, int lineNumber, boolean highlight) { }

    public String getCurrSourceFilePath() { return null; }

    public void setBreakpointRequest(String filePath, int lineNumber, boolean isDeferred) { }

    public void clearBreakpointRequest(String filePath, int lineNumber) { }

    private String readFile(String filePath, int lineNumber) {
        try {
//            URL url = ClassLoader.getSystemResource(filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                return "ERROR: file \"" + filePath + "\" does not exist.";
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer contents = new StringBuffer();
            String line = reader.readLine();
            int numLines = 0;
            while (line != null) {
                numLines++;
                if (numLines < lineNumber) {
                    currHighlightStart += line.length()+1;
                }
                if (numLines == lineNumber) {
                    currHighlightEnd = currHighlightStart + line.length();
                }
                contents.append(line);
                contents.append('\n');
                line = reader.readLine();
            }
            reader.close();
            return contents.toString();
        } catch (IOException ioe) {
            return "ERROR: could not read file \"" + filePath + "\", make sure that you have mounted /project/aop on X:\\";
        }
    }

    private void jbInit() throws Exception {
        editor_panel.setFont(new java.awt.Font("DialogInput", 1, 12));
        editor_panel.setLayout(borderLayout1);
        editor_panel.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(editorPane, null);
    }
}
