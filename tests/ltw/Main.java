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

public class Main {

	public void test1 () {
		System.out.println("Main.test1");
	}

	public void test2 () {
		System.out.println("Main.test2");
	}

	public static void main (String[] args) {
		System.out.println("Main.main");
		new Main().test1();
		new Main().test2();
	}
}
