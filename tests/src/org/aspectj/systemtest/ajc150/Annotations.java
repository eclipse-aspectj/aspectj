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
 * Annotations, the rules/tests:
 * 
 * 1. cannot make ITD (C,M or F) on an annotation
 * 2. cannot use declare parents to change the super type of an annotation
 * 3. cannot use decp to make an annotation type implement an interface
 * 4. cannot use decp to dec java.lang.annotation.Annotation as the parent of any type
 * 5. cannot extend set of values in an annotation via an ITD like construct
 * 6. Compilation error if you explicitly identify an Annotation type.  
 * 7. Lint warning if a non-explicit type pattern would match an annotation type.
 */
public class Annotations extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/annotations");
  }
	
  // Cannot make ITD (c/m/f) on an annotation
  public void test001_itdsOnAnnotationsNotAllowed() {
  	CompilationResult cR = binaryWeave("testcode.jar","AnnotationAspect01.aj",3,0);
  	assertTrue("Expected three message about ITDs not allowed on Annotations but got: #"+
  			cR.getErrorMessages().size()+": \n"+cR.getErrorMessages(),
  			cR.getErrorMessages().size()==3);
  	IMessage msg1_ctor   = (IMessage)cR.getErrorMessages().get(0);
  	IMessage msg2_method = (IMessage)cR.getErrorMessages().get(1);
  	IMessage msg3_field  = (IMessage)cR.getErrorMessages().get(2);
  	assertTrue("Expected message about ITDCs on annotations not allowed, but got: \n"+msg1_ctor,
  			msg1_ctor.toString().indexOf("can't make inter-type constructor declarations")!=-1);
  	assertTrue("Expected message about ITDMs on annotations not allowed, but got: \n"+msg2_method,
  			msg2_method.toString().indexOf("can't make inter-type method declarations")!=-1);
  	assertTrue("Expected message about ITDFs on annotations not allowed, but got: \n"+msg3_field,
  			msg3_field.toString().indexOf("can't make inter-type field declarations")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
  
  // Deals with the cases where an explicit type is specified and it is an annotation type
  public void test002_decpOnAnnotationNotAllowed_errors() {
  	CompilationResult cR = binaryWeave("testcode.jar","AnnotationAspect04.aj",3,0,true);
  	IMessage msg = (IMessage)cR.getErrorMessages().get(0);
  	assertTrue("Expected a message about can't use decp to alter supertype of an annotation: "+msg,
  			msg.toString().indexOf("to alter supertype of annotation type")!=-1);
  	msg = (IMessage)cR.getErrorMessages().get(1);
  	assertTrue("Expected a message about can't use decp to make annotation implement interface: "+msg,
  			msg.toString().indexOf("implement an interface")!=-1);
  	msg = (IMessage)cR.getErrorMessages().get(2);
  	assertTrue("Expected a message about can't use decp to make Annotation parent of another type: "+msg,
  			msg.toString().indexOf("the parent of type")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
  
  //Deals with the cases where an wild type pattern is specified and it hits an annotation type
  public void test004_decpOnAnnotationNotAllowed_xlints() {
  	CompilationResult cR = binaryWeave("testcode.jar","AnnotationAspect05.aj",0,2,false);
  	IMessage msg = (IMessage)cR.getWarningMessages().get(0);
  	assertTrue("Expected a message about an annotation type matching a declare parents but being ignored: "+msg,
  			msg.toString().indexOf("matches a declare parents type pattern")!=-1);
  	msg = (IMessage)cR.getWarningMessages().get(1);
  	assertTrue("Expected a message about an annotation type matching a declare parents but being ignored: "+msg,
  			msg.toString().indexOf("matches a declare parents type pattern")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
  
  // TODO extra tests:
  // declare parents with annotation pattern
  // declare soft with annotation pattern
  // declare warning with annotation pattern
  // declare precedence with annotation pattern
}