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
 * Tests the use of Annotations in pointcuts
 */
public class AnnotationPointcuts extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/annotations");
  }
	
  // before(): call(@SimpleAnnotation * *(..)) { }
  public void test001_usingAnnotationsInPointcuts() {
  	CompilationResult cR = binaryWeave("testcode.jar","AnnotationAspect02.aj",0,0);
  	System.err.println(cR.getStandardError());
  	System.err.println(cR.getErrorMessages());
  	System.err.println(cR.getInfoMessages());
  	verifyWeavingMessagesOutput(cR,new String[]{
"weaveinfo Type 'AnnotatedType' (AnnotatedType.java:3) advised by before advice from 'AnnotationAspect02' (AnnotationAspect02.aj:4)",
"weaveinfo Type 'AnnotatedType' (AnnotatedType.java:3) advised by before advice from 'AnnotationAspect02' (AnnotationAspect02.aj:2)",
"weaveinfo Type 'AnnotatedType' (AnnotatedType.java:4) advised by before advice from 'AnnotationAspect02' (AnnotationAspect02.aj:4)"});
  
//  	assertTrue("Expected three message about ITDs not allowed on Annotations but got: #"+
//  			cR.getErrorMessages().size()+": \n"+cR.getErrorMessages(),
//  			cR.getErrorMessages().size()==3);
//  	IMessage msg1_ctor   = (IMessage)cR.getErrorMessages().get(0);
//  	IMessage msg2_method = (IMessage)cR.getErrorMessages().get(1);
//  	IMessage msg3_field  = (IMessage)cR.getErrorMessages().get(2);
//  	assertTrue("Expected message about ITDCs on annotations not allowed, but got: \n"+msg1_ctor,
//  			msg1_ctor.toString().indexOf("can't make inter-type constructor declarations")!=-1);
//  	assertTrue("Expected message about ITDMs on annotations not allowed, but got: \n"+msg2_method,
//  			msg2_method.toString().indexOf("can't make inter-type method declarations")!=-1);
//  	assertTrue("Expected message about ITDFs on annotations not allowed, but got: \n"+msg3_field,
//  			msg3_field.toString().indexOf("can't make inter-type field declarations")!=-1);
//  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
}