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


package org.aspectj.ajde.ui.swing;

import java.awt.Frame;

import javax.swing.JDialog;

import org.aspectj.ajde.BuildProgressMonitor;
  
/**
 * This dialog box is open while ajc is compiling the system and displays
 * a corresponding progress bar.
 *
 * @author  Mik Kersten
 */
public class DefaultBuildProgressMonitor extends Thread implements BuildProgressMonitor {

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
    public void start(String configFilePath) {
    	progressDialog.setConfigFile(configFilePath);
    	progressDialog.setProgressBarVal(0);
    	progressDialog.setProgressText("starting build...");
		dialog.setLocationRelativeTo(AjdeUIManager.getDefault().getRootFrame());
		dialog.setVisible(true);
    }

	/**
	 * Sets the label describing the current progress phase.
	 */
    public void setProgressText(String text) {
    	progressDialog.setProgressText(text);
    }

    /**
     * Jumps the progress bar to <CODE>newVal</CODE>.
     */
    public void setProgressBarVal(int newVal) {
    	progressDialog.setProgressBarVal(newVal);
    }

    /**
     * Makes the progress bar by one.
     */
    public void incrementProgressBarVal() {
    	progressDialog.incrementProgressBarVal();
    }

	/**
	 * @param	maxVal	sets the value at which the progress will finish.
	 */
    public void setProgressBarMax(int maxVal) {
    	progressDialog.setProgressBarMax(maxVal);
    }

	/**
	 * @return	the value at which the progress monitoring will finish.
	 */
    public int getProgressBarMax() {
		return progressDialog.getProgressBarMax();    	
    }

    /**
     * Jump the progress bar to the end and finish progress monitoring.
     */
    public void finish(boolean wasFullBuild) {
		progressDialog.finish();
		dialog.dispose();    	
    }
}
