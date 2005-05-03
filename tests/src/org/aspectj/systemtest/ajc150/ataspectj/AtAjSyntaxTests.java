/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.AutowiredXMLBasedAjcTestCase;

/**                              
 * A suite for @AspectJ aspects located in java5/ataspectj
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjSyntaxTests extends AutowiredXMLBasedAjcTestCase {
	
	public static Test suite() {
	    return AutowiredXMLBasedAjcTestCase.loadSuite(AtAjSyntaxTests.class);
	}

	protected File getSpecFile() {
	  return new File("../tests/src/org/aspectj/systemtest/ajc150/ataspectj/syntax.xml");
	}

}