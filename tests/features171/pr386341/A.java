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

class AtomicAction  {
  int status() { return 1; }
  int commit(int n) { return 1; }
}

public class A {
	public static void main(String []argv) throws Exception {
		A a = new A();
		a.m();
	}
	  
  public void m() throws Exception{
		Thread.sleep(5*1000);
  }

}

