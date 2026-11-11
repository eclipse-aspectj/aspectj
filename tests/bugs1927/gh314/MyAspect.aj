/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/

public aspect MyAspect {
	before(): call(void Foo.bar()) {
		System.out.println("before bar");
	}

	// around advice makes the weaver generate a closure class for the woven Foo
	void around(): call(void Foo.bar()) {
		System.out.println("around bar");
		proceed();
	}
}
