/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package ataspectj;

import java.lang.reflect.Proxy;

public class TestProxyGenerator implements TestInterface {

	public void testMethod() {
	}

	public static void main(String[] args) {
		Class clazz = TestProxyGenerator.class;
		Class proxyClazz = Proxy.getProxyClass(clazz.getClassLoader(),new Class[] { TestInterface.class});
		System.out.println("TestProxyGenerator.main() proxyClazz=" + proxyClazz + ", proxyClassLoader=" + proxyClazz.getClassLoader());
	}

}
