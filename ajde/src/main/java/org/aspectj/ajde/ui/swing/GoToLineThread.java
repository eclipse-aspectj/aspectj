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

 

package org.aspectj.ajde.ui.swing;

import javax.swing.SwingUtilities;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.EditorAdapter;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * Used to ensure that a source line has been seeked to.  Will repeatedly attempt
 * to seek to the line until this has succeeded.
 * 
 * @author	Mik Kersten
 */
public class GoToLineThread extends Thread {
	private EditorAdapter editorAdapter = null;
	
    private int lineNumber = 0;
    private String fileToSeekTo = "";
    private boolean finished = false;

    public boolean isFinished() {
        return finished;
    }

    public boolean needsRetry() {
        return !this.isAlive() && !finished;
    }

    public GoToLineThread(String fileToSeekTo, int lineNumber, EditorAdapter editorAdapter) {
        this.lineNumber = lineNumber;
        this.fileToSeekTo = fileToSeekTo;
        this.editorAdapter = editorAdapter;
    }

    public void run() {

        while(true) {
            String file = editorAdapter.getCurrFile();
            if (file != null) {
                if (file.equals(this.fileToSeekTo)) {
                    try {
                    SwingUtilities.invokeAndWait( new Runnable() {
                        public void run() {
                            editorAdapter.showSourceLine(lineNumber, true);
                        }
                    });
                    } catch (Exception e) { 
                    	Message msg = new Message("Could not seek to line.",IMessage.ERROR,e,null);
                    	Ajde.getDefault().getMessageHandler().handleMessage(msg);
                    }
                    finished = true;
                    break;
                }
                shortPause();
            }
        }
    }

    private void shortPause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
