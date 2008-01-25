/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *  Contributors
 *  Andy Clement 
 * ******************************************************************/
package test;

public class A {
	public void a(String s) {}
	public void b(@A1 String s) {}
	public void c(@A1 @A2 String s) {}
	public void d(@A1 String s,@A2 String t) {}
	
	public void e(A1AnnotatedType s) {}
	public void f(A2AnnotatedType s) {}
	public void g(@A2 A1AnnotatedType s) {}
	public void h(@A1 A1AnnotatedType s) {}
	public void i(A1AnnotatedType s,@A2 String t) {}
	public void j(@A1 @A2 String s) {}

}
