/*******************************************************************************
 * Copyright (c) 2012 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc171;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */ 
public class Ajc171Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	public void testNpe_pr384401() {
		runTest("npe");
	}
	
	public void testUnresolvableEnum_pr387568() {
		runTest("unresolvable enum");
	}
	
	public void testAbstractItds_pr386049() {
		runTest("itd abstract");
	}
	
	public void testPublicITDFs_pr73507_1() {
		runTest("public ITDfs - 1");
	}

	public void testPublicITDFs_pr73507_2() {
		runTest("public ITDfs - 2");
	}

	public void testPublicITDFs_pr73507_3() {
		runTest("public ITDfs - 3");
	}

	public void testPublicITDFs_pr73507_4() {
		runTest("public ITDfs - 4");
	}
	
	public void testPublicITDFs_pr73507_5() {
		runTest("public ITDfs - 5");
	}
	
	public void testGenerics_384398() {
		runTest("generics itds");
	}
	
//	public void testGenerics_384398_2() {
//		runTest("generics itds 2");
//	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc171Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc171.xml");
	}

}
