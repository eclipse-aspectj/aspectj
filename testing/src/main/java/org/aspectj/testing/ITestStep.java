/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
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

import org.aspectj.tools.ajc.AjcTestCase;

/**
 * @author Adrian Colyer
 */
public interface ITestStep {

	void execute(AjcTestCase inTestCase);
	
	void addExpectedMessage(ExpectedMessageSpec message);
	
	void setBaseDir(String dir);
	
	void setTest(AjcTest test);
}
