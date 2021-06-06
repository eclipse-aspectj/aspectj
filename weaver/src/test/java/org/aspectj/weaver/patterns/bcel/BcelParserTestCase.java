/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.patterns.bcel;

import org.aspectj.weaver.WeaverTestCase;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.patterns.ParserTestCase;

public class BcelParserTestCase extends ParserTestCase {

	public World getWorld() {
		return new BcelWorld(WeaverTestCase.TESTDATA_PATH + "/testcode.jar");
	}

}
