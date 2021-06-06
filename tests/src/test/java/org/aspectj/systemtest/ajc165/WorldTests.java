/*******************************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc165;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.Test;

public class WorldTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testWorldSize() {
		BcelWorld world = new BcelWorld("../weaver5/bin/");
		assertEquals(9, world.getFixed().size()); // 9 primitive types
		assertEquals(0, world.getExpendable().size()); // nothing loaded

		world.resolve(UnresolvedType.forSignature("LMA;"));
		assertEquals(9, world.getFixed().size());
		assertEquals(1, world.getExpendable().size());

	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(WorldTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("world.xml");
//		return new File("../tests/src/org/aspectj/systemtest/ajc165/world.xml");
	}

}
