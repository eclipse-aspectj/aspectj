/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;

import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.CompilationResult;


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
public class Enums extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/enums");
  }
  
  // Cannot make ITDC on an enum
  public void test001_itdcsOnEnumNotAllowed() {
  	CompilationResult cR = binaryWeave("testcode.jar","EnumAspect01.aj",1,0);
  	IMessage msg = (IMessage)cR.getErrorMessages().get(0);
  	assertTrue("Expected a message about ITDCs not allowed on enums but got: "+msg,
  			msg.toString().indexOf("can't make inter-type constructor declarations")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
  
  // Cannot make ITDM or ITDF on an enum
  public void test002_itdFieldOrMethodOnEnumNotAllowed() {
  	CompilationResult cR = binaryWeave("testcode.jar","EnumAspect02.aj",2,0);
  	IMessage msg1 = (IMessage)cR.getErrorMessages().get(0);
 	IMessage msg2 = (IMessage)cR.getErrorMessages().get(1);
  	assertTrue("Expected a message about ITD methods not allowed on enums but got: "+msg1,
  			msg1.toString().indexOf("can't make inter-type method declarations")!=-1);
  	assertTrue("Expected a message about ITD fields not allowed on enums but got: "+msg2,
  			msg2.toString().indexOf("can't make inter-type field declarations")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
  
  // Deals with the cases where an explicit type is specified and it is an enum type
  public void test003_decpOnEnumNotAllowed_errors() {
  	CompilationResult cR = binaryWeave("testcode.jar","EnumAspect03.aj",3,0,true);
  	// THE ORDERING CAN BE SENSITIVE HERE... OUGHT TO FIX IT PROPERLY AND ALLOW FOR THEM
  	// IN ANY POSITION
  	IMessage msg = (IMessage)cR.getErrorMessages().get(1);
  	assertTrue("Expected a message about can't use decp to alter supertype of an enum: "+msg,
  			msg.toString().indexOf("to alter supertype of enum type")!=-1);
  	msg = (IMessage)cR.getErrorMessages().get(2);
  	assertTrue("Expected a message about can't use decp to make enum implement interface: "+msg,
  			msg.toString().indexOf("implement an interface")!=-1);
  	msg = (IMessage)cR.getErrorMessages().get(0);
  	assertTrue("Expected a message about can't use decp to make Enum parent of another type: "+msg,
  			msg.toString().indexOf("the parent of type")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }

  //Deals with the cases where an wild type pattern is specified and it hits an enum type
  public void test004_decpOnEnumNotAllowed_xlints() {
  	CompilationResult cR = binaryWeave("testcode.jar","EnumAspect04.aj",0,2,false);
  	IMessage msg = (IMessage)cR.getWarningMessages().get(0);
  	assertTrue("Expected a message about an enum type matching a declare parents but being ignored: "+msg,
  			msg.toString().indexOf("matches a declare parents type pattern")!=-1);
  	msg = (IMessage)cR.getWarningMessages().get(1);
  	assertTrue("Expected a message about an enum type matching a declare parents but being ignored: "+msg,
  			msg.toString().indexOf("matches a declare parents type pattern")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }

}