/* *******************************************************************
 * Copyright (c) 2002-2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import org.aspectj.weaver.CommonReferenceTypeTests;
import org.aspectj.weaver.World;

public class ReflectionWorldReferenceTypeTest extends CommonReferenceTypeTests {

	protected boolean getSupportsAutoboxing() {
		return true;
	}

	public World getWorld() {
		return new ReflectionWorld(false, getClass().getClassLoader());
	}

}
