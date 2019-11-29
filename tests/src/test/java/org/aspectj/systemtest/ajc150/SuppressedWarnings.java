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

public class SuppressedWarnings extends XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(SuppressedWarnings.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("ajc150.xml");
  }
  
  // Check basic suppression
  public void testSuppression1() {
  	runTest("suppressing non-matching advice warnings");
  }
  
  // Checks source contexts aren't put out incorrectly
  // NOTE: Source contexts only come out if the primary source location in a message
  // matches the file currently being dealt with.  Because advice not matching
  // messages come out at the last stage of compilation, you currently only
  // get sourcecontext for advice not matching messages that point to places in
  // the last file being processed.  You do get source locations in all cases -
  // you just don't always get context, we could revisit this sometime...
  // (see bug 62073 reference in WeaverMessageHandler.handleMessage())
  public void testSuppression2() {
  	runTest("suppressing non-matching advice warnings when multiple source files involved");
  }
  
  public void testSuppressionWithCflow_pr93345() {
    runTest("XLint warning for advice not applied with cflow(execution)");
  }
  
  public void testSuppressionOfMessagesIssuedDuringMatching() {
	  runTest("SuppressAjWarnings raised during matching");
  }
}