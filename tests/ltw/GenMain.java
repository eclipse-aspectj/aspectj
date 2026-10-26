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
 * Simple program for the tests checking that weaving which causes the weaver to define additional (generated) classes works
 * without any warnings on stderr, see ltw.xml tests with title starting 'LTW generated class'.
 */
public class GenMain {

	public static void main(String[] args) {
		System.out.println(new GenMain().greet("x"));
	}

	String greet(String s) {
		return "hi " + s;
	}
}
