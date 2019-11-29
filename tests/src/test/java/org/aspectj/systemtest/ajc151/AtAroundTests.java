/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * This testcode shows what is possible with code style and the current limitations
 * of @AJ style.  Each program is written in both styles and those variations
 * not currently possible are commented out.
 * 
 * @author AndyClement
 *
 */
public class AtAroundTests extends XMLBasedAjcTestCase {

  public void testCodeBasic()       { runTest("code style - basic"); }
  public void testAtBasicNoInline() { runTest("annotation style - basic - noinline"); }
  public void testAtBasic()         { runTest("annotation style - basic"); }
  
  public void testCodeBindingTarget()       { runTest("code style - correct usage, binding and passing same target for call"); }
  public void testAtBindingTargetNoInline() { runTest("annotation style - correct usage, binding and passing same target for call - noinline"); }
  public void testAtBindingTarget()         { runTest("annotation style - correct usage, binding and passing same target for call"); }

  public void testCodeBindingTarget2()       { runTest("code style - correct usage, binding and passing new target for call"); }
  public void testAtBindingTargetNoInline2() { runTest("annotation style - correct usage, binding and passing new target for call - noinline"); }
  public void testAtBindingTarget2()         { runTest("annotation style - correct usage, binding and passing new target for call"); }

  public void testCodeErrorCase1() {     runTest("code style - forget to pass target");}
  // Don't think we can report correct errors for @AJ as the parameters are specified as an object array
  //public void testAtErrorCase1() { runTest("annotation style - forget to pass target");}

  public void testCodeBindThisCallChangeProceed()       { runTest("code style - bind this on call - change on proceed - no effect");} 
  public void testAtBindThisCallChangeProceedNoInline() { runTest("annotation style - bind this on call - change on proceed - no effect - noinline");}
  public void testAtBindThisCallChangeProceed()         { runTest("annotation style - bind this on call - change on proceed - no effect");}

  public void testCodeBindThisExecutionChangeProceed()       { runTest("code style - bind this on execution - change on proceed - works");}
  public void testAtBindThisExecutionChangeProceedNoInline() { runTest("annotation style - bind this on execution - change on proceed - works - noinline");}
  public void testAtBindThisExecutionChangeProceed()         { runTest("annotation style - bind this on execution - change on proceed - works");}

  public void testCodeBindBothExecutionChangeProceed()       { runTest("code style - bind this and target on execution - change on proceed - works");}
  public void testAtBindBothExecutionChangeProceedNoInline() { runTest("annotation style - bind this and target on execution - change on proceed - works - noinline");}
  public void testAtBindBothExecutionChangeProceed()         { runTest("annotation style - bind this and target on execution - change on proceed - works");}

  public void testCodeErrorCase2() {     runTest("code style - incorrect arg types");}
  // Don't think we can report correct errors for @AJ as the parameters are specified as an object array
  // public void testAtErrorCase2() { runTest("annotation style - incorrect arg types");}

  public void testCodeChangingTargetDifferingOrder() {       runTest("code style - changing target for call - reverse order"); }
  // @AJ cant cope with the changing of the order of arguments bound and passed through proceed
  //public void testAtChangingTargetDifferingOrder()   { runTest("annotation style - changing target for call - reverse order"); }

  public void testCodeBindBothCallChangeProceed()       { runTest("code style - bind this and target on call - change on proceed - works");}
  public void testAtBindBothCallChangeProceedNoInline() { runTest("annotation style - bind this and target on call - change on proceed - works - noinline");}
  public void testAtBindBothCallChangeProceed()         { runTest("annotation style - bind this and target on call - change on proceed - works");}
  
  public void testBreakingIt1() { runTest("breaking it - one");}
  public void testBreakingIt2() { runTest("breaking it - two");}
  
  public void testBugCase1() { runTest("bug case one");}
  public void testBugCase2() { runTest("bug case two");}
  public void testMultipleArgs() { runTest("multiple args");}
  
  public void testCodeSubsetArguments() {     runTest("code style - works with subset of arguments in advice");}
  // cant do this for annotation style
  //public void testAtSubsetArguments() { runTest("annotation style - works with subset of arguments in advice");}
  
  // ---
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(AtAroundTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("ataround.xml");
  }
	
}
