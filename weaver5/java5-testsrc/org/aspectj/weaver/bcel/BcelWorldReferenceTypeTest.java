/* *******************************************************************
 * Copyright (c) 2002-2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.weaver.CommonReferenceTypeTests;
import org.aspectj.weaver.World;

public class BcelWorldReferenceTypeTest extends CommonReferenceTypeTests {

	public World getWorld() {
		return new BcelWorld();
	}

}
