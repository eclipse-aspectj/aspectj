
	import java.util.Hashtable;

	import javax.naming.Context;
	import javax.naming.NamingException;
	import javax.naming.directory.DirContext;
	import javax.naming.directory.InitialDirContext;
/*******************************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - Repro test case
 *    Abraham Nevado
 *******************************************************************************/


	public class A implements java.io.Serializable{

		  public A()
		  {
		  }
		  public void doSomething() throws Exception
		  {


		 }
		 public static void main(String[] args) throws Exception {
			 A lc = new A();
			 lc.doSomething();

		   }
	}


