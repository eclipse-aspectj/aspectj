/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	 Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.bcel.asm;

import java.lang.reflect.Method;

/**
 * Determines if a version of asm is around that will enable us to add stack map attributes to classes that we produce.
 * 
 * @author Andy Clement
 */
public class AsmDetector {

	public static boolean isAsmAround;

	static {
		try {
			Class<?> reader = Class.forName("aj.org.objectweb.asm.ClassReader");
			Class<?> visitor = Class.forName("aj.org.objectweb.asm.ClassVisitor");
			Method m = reader.getMethod("accept", new Class[] { visitor, Integer.TYPE });
			isAsmAround = m != null;
		} catch (Exception e) {
			isAsmAround = false;
		}
		// System.out.println(isAsmAround?"ASM detected":"No ASM found");
	}
}
