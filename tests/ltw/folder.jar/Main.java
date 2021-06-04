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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Main {

	public void test1 () {
		System.out.println("Main.test1");
	}

	public void test2 () {
		System.out.println("Main.test2");
	}

	public void invokeDeclaredMethods () throws Exception {
		Method[] methods = getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			if (!Modifier.isStatic(modifiers) && !method.getName().equals("invokeDeclaredMethods")) {
				method.invoke(this,new Object[] {});
			}
		}
	}

	public static void main (String[] args) throws Exception {
		System.out.println("Main.main");
		new Main().test1();
		new Main().test2();
	}
}
