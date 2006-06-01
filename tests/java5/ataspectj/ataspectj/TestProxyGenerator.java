/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
