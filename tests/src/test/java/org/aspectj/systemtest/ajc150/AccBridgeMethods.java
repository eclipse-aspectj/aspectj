/*******************************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;


/**
 * <b>These tests check binary weaving of code compiled with the 1.5 compiler.  If you need to rebuild
 * the class files then you will have to run tests/java5/bridgeMethods/build.xml.</b>
 *
 * <p>Bridge methods are generated when a type extends or implements a parameterized class or interface and
 * type erasure changes the signature of any inherited method.
 *
 * <p>They impact AspectJ in two ways:
 * <ol>
 * <li>They exist as a method execution join point, and their 'body' exists as a set of new join points
 *   (although their body is normally coded simply to delegate to the method they are bridging too).
 * <li> They create a potential call join point where a call can be made to the bridge method.
 * </ol>
 *
 * <p>The principal things we have to do are avoid weaving their body and ignore their existence
 * as a method execution join point.  Their existence as a potential target for a call join point are
 * more complicated.  Although they exist in the code, a 1.5 compiler will prevent a call to them with
 * an error like this:
 *
 * M.java:3: compareTo(Number) in Number cannot be applied to (java.lang.String)
 * new Number(5).compareTo("abc");
 *
 * Our problem is that a Java 1.4 or earlier compiler will allow you to write calls to this bridge method
 * and it will let them through.
 */
public class AccBridgeMethods extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(AccBridgeMethods.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("ajc150.xml");
  }


  /**
   * AspectX attempts to weave call and execution of the method for which a 'bridge method' is also created.
   * If the test works then only two weaving messages come out.  If it fails then usually 4 messages come out
   * and we have incorrectly woven the bridge method (the 3rd message is execution of the bridge method and
   * the 4th message is the call within the bridge method to the real method).
   */
  public void test001_bridgeMethodIgnored() {
  	runTest("Ignore bridge methods");
  }


}