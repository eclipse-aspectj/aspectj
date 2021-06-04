/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package reflect.tests;

/**
 * @author colyer
 * Part of the testdata for the org.aspectj.weaver.reflect tests
 */
public class C {

	public String foo(Object a) throws Exception {
		return null;
	}

	private void bar() {}

	public int f;
	private String s;
}

class D extends C implements java.io.Serializable {
	public int getNumberOfThingies() { return 0; }
	private Object o;
}
