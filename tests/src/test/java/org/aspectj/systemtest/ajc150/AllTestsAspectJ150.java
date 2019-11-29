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

import org.aspectj.systemtest.ajc150.ataspectj.AtAjLTWTests;
import org.aspectj.systemtest.ajc150.ataspectj.AtAjMisuseTests;
import org.aspectj.systemtest.ajc150.ataspectj.AtAjSyntaxTests;
import org.aspectj.systemtest.ajc150.ltw.LTWServerTests;
import org.aspectj.systemtest.ajc150.ltw.LTWTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This pulls together tests we have written for AspectJ 1.5.0 that don't need Java 1.5 to run
 */
public class AllTestsAspectJ150 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ1.5.0 tests");
		// $JUnit-BEGIN$
		suite.addTestSuite(MigrationTests.class);
		suite.addTest(Ajc150Tests.suite());
		suite.addTestSuite(SCCSFixTests.class);

		suite.addTest(AccBridgeMethods.suite());
		suite.addTestSuite(CovarianceTests.class);
		suite.addTestSuite(Enums.class);
		suite.addTest(AnnotationsBinaryWeaving.suite());
		suite.addTest(AnnotationPointcutsTests.suite());
		suite.addTestSuite(VarargsTests.class);
		suite.addTestSuite(StaticImports.class);
		suite.addTest(AnnotationRuntimeTests.suite());
		suite.addTestSuite(PerTypeWithinTests.class);

		suite.addTest(Autoboxing.suite());
		suite.addTest(Annotations.suite());
		suite.addTest(AnnotationBinding.suite());
		suite.addTest(RuntimeAnnotations.suite());

		suite.addTest(SuppressedWarnings.suite());
		suite.addTest(DeclareAnnotationTests.suite());
		suite.addTest(GenericsTests.suite());
		suite.addTest(GenericITDsDesign.suite());
		suite.addTest(AtAjSyntaxTests.suite());
		suite.addTest(AtAjMisuseTests.suite());
		suite.addTest(AtAjLTWTests.suite());
		suite.addTest(HasMember.suite());

		suite.addTestSuite(LTWTests.class);
		suite.addTestSuite(LTWServerTests.class);
		// $JUnit-END$
		return suite;
	}
}
