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
   
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * @author  Mik Kersten
 */
public class BuildProgressPanel extends JPanel {

	//private static final long serialVersionUID = -8045879840621749183L;
	private static final int MAX_VAL = 100;
	//private JDialog dialog = null;

	BorderLayout borderLayout1 = new BorderLayout();
	JPanel cancel_panel = new JPanel();
	JButton cancel_button = new JButton();
	JPanel jPanel2 = new JPanel();
	JLabel progress_label = new JLabel();
    JLabel configFile_label = new JLabel();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JProgressBar compile_progressBar = new JProgressBar();

    private boolean buildIsCancelled = false;
	
	public BuildProgressPanel() {
		try {
			jbInit();
			compile_progressBar.setMaximum(MAX_VAL);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

//		public void start() {
//			dialog =
//				new JDialog(TopManager.INSTANCE.getRootFrame(), "ajc Build Progress", false);
//			//        progressDialog = new CompileProgressPanel();
//			dialog.setContentPane(this);
//			dialog.setSize(500, 110);
//			dialog.setLocationRelativeTo(TopManager.INSTANCE.getRootFrame());
//			dialog.setVisible(true);
//		}

	public void setProgressText(String text) {
		progress_label.setText("   " + text);
	}

	public void setConfigFile(String configFile) {
		configFile_label.setText("   Build configuration: " + configFile);
	}

	/**
	 * Jumps the progress bar <CODE>newVal</CODE> seconds ahead.
	 */
	public void setProgressBarVal(int newVal) {
		compile_progressBar.setValue(newVal);
	}

	/**
	 * @param   maxVal          the value to which value to which the progress bar will
	 *                          count up to (in seconds)
	 */
	public void setProgressBarMax(int maxVal) {
		compile_progressBar.setMaximum(maxVal);
	}

	public int getProgressBarMax() {
		return compile_progressBar.getMaximum();
	}

	/**
	 * Makes the progress bar move one second ahead.
	 */
	public void incrementProgressBarVal() {
		int newVal = compile_progressBar.getValue() + 1;
		compile_progressBar.setValue(newVal);
	}

	/**
	 * Jumps the progress bar to the end.
	 */
	public void finish() {
		compile_progressBar.setValue(compile_progressBar.getMaximum());
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		cancel_button.setFont(new java.awt.Font("Dialog", 0, 11));
		cancel_button.setText("Cancel");
		cancel_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel_button_actionPerformed(e);
			}
		});
		progress_label.setFont(new java.awt.Font("Dialog", 0, 11));
        progress_label.setText("");
		jPanel2.setPreferredSize(new Dimension(360, 24));
        jPanel2.setLayout(borderLayout3);
        configFile_label.setFont(new java.awt.Font("Dialog", 0, 11));
        configFile_label.setText("");
        compile_progressBar.setPreferredSize(new Dimension(330, 14));
        this.add(cancel_panel, BorderLayout.SOUTH);
		cancel_panel.add(cancel_button, null);
		this.add(jPanel2,  BorderLayout.CENTER);
		jPanel2.add(configFile_label,  BorderLayout.NORTH);
		jPanel2.add(progress_label,  BorderLayout.SOUTH);
        jPanel2.add(jPanel1,  BorderLayout.CENTER);
        jPanel1.add(compile_progressBar, null);
	}

	void cancel_button_actionPerformed(ActionEvent e) {
		buildIsCancelled = true;
	}
	
	public boolean isCancelRequested() {
		return buildIsCancelled;
	}
}
