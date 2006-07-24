/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/

package org.aspectj.ajde;

/**
 * @author Mik Kersten
 */
public class NullIdeProgressMonitor implements BuildProgressMonitor {

	public void start(String configFile) {
		
	}

	public void setProgressText(String text) {

	}

	public void setProgressBarVal(int newVal) {

	}

	public void incrementProgressBarVal() {

	}

	public void setProgressBarMax(int maxVal) {

	}

	public int getProgressBarMax() {
		return 0;
	}

	public void finish(boolean b) {

	}

}
