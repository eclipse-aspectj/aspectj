/*******************************************************************************
 * Copyright (c) 2009 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Andy Clement 
 *******************************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DeclareMixin annotation - see design and usage in https://bugs.eclipse.org/bugs/show_bug.cgi?id=266552
 * 
 * <p>
 * Attached to a factory method, this annotation indicates that any types matching the pattern specified in the annotation value
 * will have new methods mixed in. The methods will be selected based on a combination of the return type of the factory method,
 * possibly sub-setted by any list of interfaces specified in the interfaces annotation value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeclareMixin {

	/**
	 * The target types expression
	 */
	String value();

	/**
	 * Array of interfaces that are to be mixed in. This is optional and if not specified the return type of the annotated method
	 * will be used to determine the interface to mix in.
	 */
	Class[] interfaces() default { Object.class };

}
