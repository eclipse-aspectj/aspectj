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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TestITDMethod {

	public void invokeDeclaredMethods (String[] names) throws Exception {
		for (int i = 0; i < names.length; i++) {
			Method method = getClass().getDeclaredMethod(names[i],new Class[] {});
			method.invoke(this,new Object[] {});
		}
	}
	
	public static void main (String[] args) throws Exception {
		System.out.println("TestITDMethod.main");
		new TestITDMethod().invokeDeclaredMethods(args);
	}
}
