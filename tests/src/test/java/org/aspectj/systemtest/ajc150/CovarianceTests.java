/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/*

class Car {}

class FastCar extends Car {}

class Super {
  Car getCar() {
    return new Car();
  }
}

class Sub extends Super {
  FastCar getCar() {
    return new FastCar();
  }
}

public class CovBaseProgram01 {
  public static void main(String[] argv) {
    new CovBaseProgram01().run();
  }

  public void run() {
    Super instance_super = new Super();
    Sub   instance_sub   = new Sub();

    Car c1 = instance_super.getCar(); // Line 26
    Car c2 = instance_sub.getCar(); // Line 27
  }
}

// Line26: callJPs: call(Car Super.getCar())
// Line27: callJPs: call(FastCar Sub.getCar()) call(Car Super.getCar())

 */

/**
 * Covariance is simply where a type overrides some inherited implementation and narrows the return type.
 */
public class CovarianceTests extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(CovarianceTests.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
	private boolean verbose = false;


	/**
	 * call(* getCar()) should match both
	 */
	public void testCOV001() {
		runTest("covariance 1");
	}


	/**
	 * call(* Super.getCar()) should match both
	 *
	 * This test required a change to the compiler.  When we are looking at signatures and comparing them we walk up
	 * the hierarchy looking for supertypes that declare the same method.  The problem is that in the comparison for
	 * whether to methods are compatible we were including the return type - this meant 'Car getCar()' on Super was
	 * different to 'FastCar getCar()' on Sub - it thought they were entirely different methods.  In fact the return
	 * type is irrelevant here, we just want to make sure the names and the parameter types are the same - so I
	 * added a parameterSignature to the Member class that looks like '()' where the full signature looks like
	 * '()LFastCar;' (which includes the return type).  If the full signature comparison fails then it looks at the
	 * parameter signature - I did it that way to try and preserve some performance.  I haven't changed the
	 * definition of 'signature' for a member as trimming the return type off it seems rather serious !
	 *
	 * What might break:
	 * - 'matches' can now return true for things that have different return types - I guess whether this is a problem
	 *   depends on what the caller of matches is expecting, their code will have been written before covariance was
	 *   a possibility.  All the tests pass so I'll leave it like this for now.
	 */
	public void testCOV002() {
		runTest("covariance 2");
	}

	/**
	 * call(Car getCar()) should match both
	 *
	 * Had to implement proper covariance support here...
	 */
	public void testCOV003() {
		runTest("covariance 3");
	}

	/**
	 * *** Different base program, where Sub does not extend Super.
	 * call(Car Super.getCar()) should only match first call to getCar()
	 */
	public void testCOV004() {
		runTest("covariance 4");
	}

	/**
	 * *** Original base program
	 * call(Car Super.getCar()) should match both
	 */
	public void testCOV005() {
		runTest("covariance 5");
	}

	/**
	 * call(Car Sub.getCar()) should not match anything
	 */
	public void testCOV006() {
		runTest("covariance 6");
	}

	/**
	 * call(Car+ Sub.getCar()) should match 2nd call with xlint for the 1st call
	 */
	public void testCOV007() {
		runTest("covariance 7");
	}

	/**
	 * *** aspect now contains two pointcuts and two pieces of advice
	 * call(FastCar getCar()) matches on 2nd call
	 * call(FastCar Sub.getCar()) matches on 2nd call
	 */
	public void testCOV008() {
		runTest("covariance 8");
	}

	/**
	 * call(FastCar Super.getCar()) matches nothing
	 */
	public void testCOV009() {
		runTest("covariance 9");
	}

	/**
	 * call(Car+ getCar()) matches both
	 */
	public void testCOV010() {
		runTest("covariance 10");
	}

	public void testAJDKExamples() {
		runTest("ajdk: covariance");
	}
}
