/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc197;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava16OrLater;

/**
 * @author Alexander Kriegisch
 */
public class Ajc197TestsJava extends XMLBasedAjcTestCaseForJava16OrLater {

  public void testHiddenClass() {
    runTest("hidden class");
    checkVersion("HiddenClassDemo", Constants.ClassFileVersion.of(16).MAJOR, Constants.ClassFileVersion.of(16).MINOR);
  }

  public void testTextBlock1() {
    runTest("textblock 1");
    checkVersion("Code", Constants.ClassFileVersion.of(16).MAJOR, Constants.ClassFileVersion.of(16).MINOR);
  }

  public void testTextBlock2() {
    runTest("textblock 2");
    checkVersion("Code2", Constants.ClassFileVersion.of(16).MAJOR, Constants.ClassFileVersion.of(16).MINOR);
  }

  public void testRecords() {
    runTest("simple record");
    checkVersion("Person", Constants.ClassFileVersion.of(16).MAJOR, Constants.ClassFileVersion.of(16).MINOR);
  }

  public void testRecords2() {
    runTest("using a record");
    checkVersion("UsingPersonRecord", Constants.ClassFileVersion.of(16).MAJOR, Constants.ClassFileVersion.of(16).MINOR);
  }

  public void testAdvisingRecords() {
    runTest("advising records");
    checkVersion("TraceRecordComponents", Constants.ClassFileVersion.of(16).MAJOR, Constants.ClassFileVersion.of(16).MINOR);
  }

  public void testInstanceofPatterns() {
    runTest("instanceof patterns");
    checkVersion("Jep305", Constants.ClassFileVersion.of(16).MAJOR, Constants.ClassFileVersion.of(16).MINOR);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc197TestsJava.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc197.xml");
  }

}
