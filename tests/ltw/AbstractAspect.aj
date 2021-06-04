/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
