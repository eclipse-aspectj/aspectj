/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC       initial implementation
 *     Helen Hawkins    Converted to new interface (bug 148190) 
 * ******************************************************************/

package org.aspectj.testing.util;

import java.io.File;
import java.util.List;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.asm.IProgramElement;

import junit.framework.TestSuite;

/**
 * @author Mik Kersten
 */
public class StructureModelUtilTest extends AjdeCoreTestCase {

	private final String[] files = new String[] { "figures" + File.separator + "Debug.java",
			"figures" + File.separator + "Figure.java", "figures" + File.separator + "FigureElement.java",
			"figures" + File.separator + "Main.java", "figures" + File.separator + "composites" + File.separator + "Line.java",
			"figures" + File.separator + "composites" + File.separator + "Square.java",
			"figures" + File.separator + "primitives" + File.separator + "planar" + File.separator + "Point.java",
			"figures" + File.separator + "primitives" + File.separator + "solid" + File.separator + "SolidPoint.java" };

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureModelUtilTest.class);
		return result;
	}

	public void testPackageViewUtil() {
		List packages = StructureModelUtil.getPackagesInModel(getCompiler().getModel());
		assertTrue("packages list not null", packages != null);
		assertTrue("packages list not empty", !packages.isEmpty());

		IProgramElement packageNode = (IProgramElement) ((Object[]) packages.get(0))[0];
		assertTrue("package node not null", packageNode != null);

		List files = StructureModelUtil.getFilesInPackage(packageNode);
		assertTrue("fle list not null", files != null);

		// TODO: re-enable
		// Map lineAdviceMap = StructureModelUtil.getLinesToAspectMap(
		// ((IProgramElement)files.get(0)).getSourceLocation().getSourceFile().getAbsolutePath()
		// );
		//		
		// assertTrue("line->advice map not null", lineAdviceMap != null);
		//		
		// Set aspects = StructureModelUtil.getAspectsAffectingPackage(packageNode);
		// assertTrue("aspect list not null", aspects != null);
	}

	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("figures-coverage");
		TestCompilerConfiguration compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		doBuild();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
