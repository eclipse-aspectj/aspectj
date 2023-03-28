/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1920;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava20Only;

/**
 * @author Alexander Kriegisch
 */
public class Java20PreviewFeaturesTests extends XMLBasedAjcTestCaseForJava20Only {

  public void testSwitchPatternMatchingPreview4Java() {
    runTest("switch pattern matching preview 4 java");
    checkVersion("SwitchPatternPreview4OK", Constants.MAJOR_20, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testSwitchPatternMatchingPreview4Error() {
    runTest("switch pattern matching preview 4 error");
  }

  public void testSwitchPatternMatchingPreview3Aspect() {
    runTest("switch pattern matching preview 3 aspect");
    checkVersion("SwitchPatternPreview3Aspect", Constants.MAJOR_20, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("Application", Constants.MAJOR_20, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("Shape", Constants.MAJOR_20, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("S", Constants.MAJOR_20, Constants.PREVIEW_MINOR_VERSION);
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
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/450 (fixed for preview 2 in Eclipse 2033-03, 4.27)
    runTest("record patterns error");
    checkVersion("RecordPatternsPreview1Error", Constants.MAJOR_20, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("Box", Constants.MAJOR_20, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testRecordPatternsPreview1ExhaustivenessOK1() {
    // Falsely throws 'An enhanced switch statement should be exhaustive; a default label expected' twice,
    // see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455
    // TODO: Remove redundant default clauses when fixed upstream
    System.out.println("TODO: fully activate when https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455 has been fixed");
    runTest("record patterns exhaustiveness 1");
  }

  public void testRecordPatternsPreview1Aspect() {
    runTest("record patterns aspect");
  }

  public void testRecordPatternsPreview1ExhaustivenessAspect() {
    // Falsely throws 'An enhanced switch statement should be exhaustive; a default label expected' twice,
    // see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455
    // TODO: Remove redundant default clauses when fixed upstream
    System.out.println("TODO: fully activate when https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455 has been fixed");
    runTest("record patterns exhaustiveness aspect");
  }

  public void testRecordPatternsPreview1ExhaustivenessError() {
    // See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455
    runTest("record patterns exhaustiveness error");
  }

  public void testRecordPatternsPreview1ExhaustivenessOK2() {
    // Falsely throws 'An enhanced switch statement should be exhaustive; a default label expected',
    // see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/398
    // TODO: activate when fixed
    System.out.println("TODO: activate when https://github.com/eclipse-jdt/eclipse.jdt.core/issues/398 has been fixed");
    //runTest("record patterns exhaustiveness 2");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java20PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1920.xml");
  }

}
