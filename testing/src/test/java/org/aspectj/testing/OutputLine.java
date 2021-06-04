/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Adrian Colyer,
 * ******************************************************************/
package org.aspectj.testing;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class OutputLine {

	// Expected text
	private String text;

	// Comma separated list of vm versions on which this is expected
	private String vm;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getVm() {
		return vm;
	}

	public void setVm(String vm) {
		this.vm = vm;
	}

}
