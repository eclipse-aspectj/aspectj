/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
