/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/

/**
 * Runs GenMain (so the weaver has defined generated classes), then checks that application code did not gain deep
 * reflective access to java.lang as a side effect of the weaver making that possible for itself.
 */
public class GenProbe {

	public static void main(String[] args) throws Exception {
		GenMain.main(args);
		try {
			java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod("defineClass",
				String.class, byte[].class, int.class, int.class, java.security.ProtectionDomain.class);
			m.setAccessible(true);
			System.out.println("java.lang is open to application code");
		}
		catch (RuntimeException e) {
			System.out.println("java.lang is not open to application code");
		}
	}
}
