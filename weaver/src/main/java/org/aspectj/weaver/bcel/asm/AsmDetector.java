/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *	 Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.bcel.asm;

/**
 * Determines if a version of asm is around that will enable us to add stack map attributes to classes that we produce.
 *
 * @author Andy Clement
 */
public class AsmDetector {
	public static final String CLASS_READER = "org.objectweb.asm.ClassReader";
	public static final String CLASS_VISITOR = "org.objectweb.asm.ClassVisitor";
	public static boolean isAsmAround;

	static {
		try {
			Class<?> reader = Class.forName(CLASS_READER);
			Class<?> visitor = Class.forName(CLASS_VISITOR);
			reader.getMethod("accept", visitor, Integer.TYPE);
			isAsmAround = true;
		} catch (Exception e) {
			isAsmAround = false;
		}
		//System.out.println(isAsmAround ? "ASM detected" : "No ASM found");
	}
}
