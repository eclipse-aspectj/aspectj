/*
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Matthew Webster    initial implementation
 */
public aspect Tracing {

	private pointcut mainMethod () :
		execution(public static void main(String[]));

	before () : mainMethod() {
		System.out.println("> " + thisJoinPoint);
	}

	after () : mainMethod() {
		System.out.println("< " + thisJoinPoint);
	}
}
