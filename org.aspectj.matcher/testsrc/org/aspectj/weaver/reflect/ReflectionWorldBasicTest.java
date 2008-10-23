/* *******************************************************************
 * Copyright (c) 2002-2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import org.aspectj.weaver.CommonWorldTests;
import org.aspectj.weaver.World;

public class ReflectionWorldBasicTest extends CommonWorldTests {

	protected boolean getSupportsAutoboxing() {
		return false;
	}

	protected World getWorld() {
		return new ReflectionWorld(true, getClass().getClassLoader());
	}

}
