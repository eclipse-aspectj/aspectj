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
package org.aspectj.tools.ajbrowser.core;

import javax.swing.JDialog;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.core.IBuildProgressMonitor;
import org.aspectj.ajde.ui.swing.BuildProgressPanel;
import org.aspectj.tools.ajbrowser.BrowserManager;
import org.aspectj.tools.ajbrowser.ui.BrowserMessageHandler;
import org.aspectj.tools.ajbrowser.ui.swing.TopFrame;

/**
 * Build progress monitor that shows the progress in a dialog containing
 * a JProgressBar. Also updates the progress bar at the bottom of AjBrowser
 * with the build progress information.
 */
public class BrowserBuildProgressMonitor extends Thread implements IBuildProgressMonitor {

	public static final String PROGRESS_HEADING = "AspectJ Build";
	
	private BuildProgressPanel progressDialog = null;
	private JDialog dialog = null;
	private TopFrame topFrame;
	
	private BrowserMessageHandler handler;
	
	public BrowserBuildProgressMonitor(BrowserMessageHandler handler) {
		this.handler = handler;
		topFrame = (TopFrame) BrowserManager.getDefault().getRootFrame();
        dialog = new JDialog(topFrame, PROGRESS_HEADING, false);
        progressDialog = new BuildProgressPanel();
        dialog.setContentPane(progressDialog);
        dialog.setSize(550, 120);
        try {
	        dialog.setLocationRelativeTo(topFrame);	
		} catch (NoSuchMethodError nsme) {
			// running on 1.3
		}
	}
	
	public void finish(boolean wasFullBuild) {
		Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("build finished...");
		progressDialog.finish();
		dialog.dispose();    	
		if (handler.getMessages().isEmpty()) {
			topFrame.hideMessagesPanel(handler);
		} else {
			topFrame.showMessagesPanel(handler);
		}
	}

	public boolean isCancelRequested() {
		boolean isCancel = progressDialog.isCancelRequested();
		if (isCancel) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("Compile aborted");
		}
		return isCancel;
	}

	public void setProgress(double percentDone) {
		progressDialog.setProgressBarVal((int) (percentDone*progressDialog.getProgressBarMax()));
	}

	public void setProgressText(String text) {
		Ajde.getDefault().getIdeUIAdapter().displayStatusInformation(text);
		progressDialog.setProgressText(text);
	}

	public void begin() {
		Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("starting build...");
		handler.reset();
    	progressDialog.setProgressBarVal(0);
    	progressDialog.setProgressText("starting build...");
		dialog.setLocationRelativeTo(Ajde.getDefault().getRootFrame());
		dialog.setVisible(true);
	}

}
