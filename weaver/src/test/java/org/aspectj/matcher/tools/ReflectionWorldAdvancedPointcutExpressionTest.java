/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 * Andy Clement
 * ******************************************************************/
package org.aspectj.matcher.tools;

import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

/**
 * Run all the pointcut parsing/matching tests against a ReflectionWorld.
 *
 * @author Andy Clement
 */
public class ReflectionWorldAdvancedPointcutExpressionTest extends CommonAdvancedPointcutExpressionTests {

	protected World getWorld() {
		World w = new ReflectionWorld(false, getClass().getClassLoader());
		w.setBehaveInJava5Way(true);
		return w;
	}

}
