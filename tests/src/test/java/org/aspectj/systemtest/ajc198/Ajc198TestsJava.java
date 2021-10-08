/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc198;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava17OrLater;

/**
 * @author Alexander Kriegisch
 */
public class Ajc198TestsJava extends XMLBasedAjcTestCaseForJava17OrLater {

	public void testSealedClassWithLegalSubclasses() {
		runTest("sealed class with legal subclasses");
		// TODO: replace 0 by Constants.PREVIEW_MINOR_VERSION after no longer using EA build, but final JDK version
		checkVersion("Employee", Constants.MAJOR_17, 0 /*Constants.PREVIEW_MINOR_VERSION*/);
		checkVersion("Manager", Constants.MAJOR_17, 0 /*Constants.PREVIEW_MINOR_VERSION*/);
	}

	public void testSealedClassWithIllegalSubclass() {
		runTest("sealed class with illegal subclass");
		// TODO: replace 0 by Constants.PREVIEW_MINOR_VERSION after no longer using EA build, but final JDK version
		checkVersion("Person", Constants.MAJOR_17, 0 /*Constants.PREVIEW_MINOR_VERSION*/);
	}

	public void testWeaveSealedClass() {
		runTest("weave sealed class");
		// TODO: replace 0 by Constants.PREVIEW_MINOR_VERSION after no longer using EA build, but final JDK version
		checkVersion("PersonAspect", Constants.MAJOR_17, 0 /*Constants.PREVIEW_MINOR_VERSION*/);
	}

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc198TestsJava.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc198.xml");
  }

}
