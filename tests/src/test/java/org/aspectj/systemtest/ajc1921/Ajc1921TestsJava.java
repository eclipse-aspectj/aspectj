/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1921;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava21OrLater;

/**
 * @author Alexander Kriegisch
 */
public class Ajc1921TestsJava extends XMLBasedAjcTestCaseForJava21OrLater {

  public void testSwitchPatternMatchingPreview4Java() {
    runTest("switch pattern matching preview 4 java");
    checkVersion("SwitchPatternPreview4OK", Constants.MAJOR_21, Constants.MINOR_21);
  }

  public void testSwitchPatternMatchingPreview4Error() {
    runTest("switch pattern matching preview 4 error");
  }

  public void testSwitchPatternMatchingPreview3Aspect() {
    runTest("switch pattern matching preview 3 aspect");
    checkVersion("SwitchPatternPreview3Aspect", Constants.MAJOR_21, Constants.MINOR_21);
    checkVersion("Application", Constants.MAJOR_21, Constants.MINOR_21);
    checkVersion("Shape", Constants.MAJOR_21, Constants.MINOR_21);
    checkVersion("S", Constants.MAJOR_21, Constants.MINOR_21);
  }

  public void testSwitchPatternMatchingCaseLabelDominatedByPrecedingError() {
    runTest("switch pattern matching error");
  }

  public void testSwitchPatternMatchingPreview3Error1() {
    runTest("switch pattern matching preview 3 error 1");
  }

  public void testSwitchPatternMatchingPreview3Error2() {
    runTest("switch pattern matching preview 3 error 2");
  }

  public void testRecordPatternsPreview1OK() {
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/450
    runTest("record patterns");
  }

  public void testRecordPatternsPreview1Error() {
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/450 (fixed for preview 2 in Eclipse 2023-03, 4.27)
    runTest("record patterns error");
    checkVersion("RecordPatternsPreview1Error", Constants.MAJOR_21, Constants.MINOR_21);
    checkVersion("Box", Constants.MAJOR_21, Constants.MINOR_21);
  }

  public void testRecordPatternsPreview1ExhaustivenessOK1() {
    // Used to falsely throw 'An enhanced switch statement should be exhaustive; a default label expected' twice,
    // see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455. Fixed in Java 21.
    runTest("record patterns exhaustiveness 1");
  }

  public void testRecordPatternsPreview1Aspect() {
    runTest("record patterns aspect");
  }

  public void testRecordPatternsPreview1ExhaustivenessAspect() {
    // Used to falsely throw 'An enhanced switch statement should be exhaustive; a default label expected' twice,
    // see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455. Fixed in Java 21.
    runTest("record patterns exhaustiveness aspect");
  }

  public void testRecordPatternsPreview1ExhaustivenessError() {
    // See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455
    runTest("record patterns exhaustiveness error");
  }

  public void testRecordPatternsPreview1ExhaustivenessOK2() {
    // Used to falsely throw 'An enhanced switch statement should be exhaustive; a default label expected',
    // see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/398. Fixed in Java 21.
    runTest("record patterns exhaustiveness 2");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc1921TestsJava.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1921.xml");
  }

}
