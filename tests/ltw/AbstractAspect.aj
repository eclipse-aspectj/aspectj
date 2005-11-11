/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Matthew Webster         initial implementation
 *******************************************************************************/
import java.lang.reflect.*;

public abstract aspect AbstractAspect {

	/*
	 * These should not take effect unless a concrete sub-aspect is defined
	 */
	declare parents : TestITDMethod implements Runnable;
	
	declare soft : InvocationTargetException : execution(public void TestITDMethod.*());

	declare warning : execution(public void main(..)) :
		"AbstractAspect_main";
	
	/*
	 * This should always take effect
	 */
	public void TestITDMethod.test () {
		System.err.println("AbstractAspect_TestITDMethod.test");
	}
}
