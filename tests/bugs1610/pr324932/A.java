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

class AtomicAction  {
  int status() { return 1; }
  int commit(int n) { return 1; }
}

public class A {
	public static void main(String []argv) {
		System.out.println("It WORKS");
	}

  AtomicAction f;

  public void m() {
    	switch (f.status()) {
    	case 1:
    		throw new RuntimeException("abc");
      	case 2:
    	  f.commit(1);
    	  return;
      	}
        switch (f.commit(1)) {
        case 1:
        	throw new RuntimeException();
        }
  }

}

