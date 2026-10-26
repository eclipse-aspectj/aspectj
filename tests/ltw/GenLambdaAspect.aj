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
 * Around advice containing a lambda cannot be inlined, so a closure class gets generated at weave time.
 */
public aspect GenLambdaAspect {

	String around(String s): execution(String GenMain.greet(String)) && args(s) {
		Runnable r = () -> System.out.println("lambda in advice");
		r.run();
		return proceed(s + "!");
	}
}
