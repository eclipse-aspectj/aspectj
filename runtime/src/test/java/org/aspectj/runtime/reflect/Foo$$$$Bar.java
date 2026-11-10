/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.runtime.reflect;

/**
 * A top level class with '$' in its name that has a nested class: the '$' in the nested class name that is a separator
 * is not one of the '$' in the name of the class it is nested in. Used by StringMakerTest.
 */
public class Foo$$$$Bar {
	public static class Boo {
		public class Deeper {
		}
	}
}
