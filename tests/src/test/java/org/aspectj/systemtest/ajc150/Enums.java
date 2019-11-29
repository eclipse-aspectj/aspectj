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
 * Enums, the rules/tests:
 * 
 * 1. cannot make ITDC on an enum
 * 2. cannot make ITDM or ITDF on an enum
 * 3. cannot use declare parents to change the super type of an enum
 * 4. cannot use decp to make an enum type implement an interface
 * 5. cannot use decp to dec java.lang.Enum as the parent of any type
 * 6. cannot extend set of values in an enum via an ITD like construct
 * 7. Compilation error if you explicitly identify an Enum type.  
 * 8. Lint warning if a non-explicit type pattern would match an enum type.
 * 
 */
public class Enums extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(Enums.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
	  
  // Cannot make ITDC on an enum
  public void test001_itdcsOnEnumNotAllowed() {
  	runTest("cant itd constructor on enum");
  }

  // Cannot make ITDM or ITDF on an enum
  public void test002_itdFieldOrMethodOnEnumNotAllowed() {
  	runTest("cant itd field or method on enum");
  }

  // Deals with the cases where an explicit type is specified and it is an enum type
  public void test003_decpOnEnumNotAllowed_errors() {
  	runTest("declare parents and enums");
  }

  //Deals with the cases where an wild type pattern is specified and it hits an enum type
  public void test004_decpOnEnumNotAllowed_xlints() {
  	runTest("wildcard enum match in itd");
  }
//  	CompilationResult cR = binaryWeave("testcode.jar","EnumAspect04.aj",0,2,false);
//  	IMessage msg = (IMessage)cR.getWarningMessages().get(0);
//  	assertTrue("Expected a message about an enum type matching a declare parents but being ignored: "+msg,
//  			msg.toString().indexOf("matches a declare parents type pattern")!=-1);
//  	msg = (IMessage)cR.getWarningMessages().get(1);
//  	assertTrue("Expected a message about an enum type matching a declare parents but being ignored: "+msg,
//  			msg.toString().indexOf("matches a declare parents type pattern")!=-1);
//  	verifyWeavingMessagesOutput(cR,new String[]{});
//  }

}