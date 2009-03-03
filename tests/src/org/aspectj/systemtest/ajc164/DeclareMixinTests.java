/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc164;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * <h4>Design and test coverage</h4><br>
 * In many ways the design is similar to DeclareParents now - so we have to plug in at the same points, but the code generation for
 * generating the delegate object and the choice of which interfaces (and methods within those) to mixin is different.
 * 
 * <h4>Design considerations:</h4><br>
 * <ul>
 * <li>model relationships
 * <li>incremental compilation
 * </ul>
 * 
 * @author Andy Clement
 */
public class DeclareMixinTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testSimpleCase() {
	//	runTest("simple case");
	}

	// --

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(DeclareMixinTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc164/declareMixin.xml");
	}

}