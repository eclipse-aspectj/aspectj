
	import java.util.Hashtable;

	import javax.naming.Context;
	import javax.naming.NamingException;
	import javax.naming.directory.DirContext;
	import javax.naming.directory.InitialDirContext;
/*******************************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	

