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

import java.awt.Frame;

import javax.swing.JDialog;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.core.IBuildProgressMonitor;
  
/**
 * This dialog box is open while ajc is compiling the system and displays
 * a corresponding progress bar.
 *
 * @author  Mik Kersten
 */
public class DefaultBuildProgressMonitor extends Thread implements IBuildProgressMonitor {

	public static final String PROGRESS_HEADING = "AspectJ Build";
	
	private BuildProgressPanel progressDialog = null;
	private JDialog dialog = null;

	public DefaultBuildProgressMonitor(Frame parent) {
        dialog = new JDialog(parent, PROGRESS_HEADING, false);
        progressDialog = new BuildProgressPanel();
        dialog.setContentPane(progressDialog);
        dialog.setSize(550, 120);
        try {
	        dialog.setLocationRelativeTo(parent);	
		} catch (NoSuchMethodError nsme) {
			// running on 1.3
		}
	}

    /**
     * Start the progress monitor.
     */
    public void begin() {
    	progressDialog.setProgressBarVal(0);
    	progressDialog.setProgressText("starting build...");
		dialog.setLocationRelativeTo(Ajde.getDefault().getRootFrame());
		dialog.setVisible(true);
    }

	/**
	 * Sets the label describing the current progress phase.
	 */
    public void setProgressText(String text) {
    	progressDialog.setProgressText(text);
    }

    /**
     * Jump the progress bar to the end and finish progress monitoring.
     */
    public void finish(boolean wasFullBuild) {
		progressDialog.finish();
		dialog.dispose();    	
    }

	public boolean isCancelRequested() {
		return progressDialog.isCancelRequested();
	}

	public void setProgress(double percentDone) {
		progressDialog.setProgressBarVal((int) (percentDone*progressDialog.getProgressBarMax()));	
	}
}
