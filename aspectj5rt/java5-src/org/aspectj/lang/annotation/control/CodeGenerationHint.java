/*******************************************************************************
 * Copyright (c) 2013 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Andy Clement
 *******************************************************************************/
package org.aspectj.lang.annotation.control;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provide code generation hints to the compiler (e.g. names to use for generated members).
 *
 * @author Andy Clement
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface CodeGenerationHint {

	/**
	 * Defines the name suffix to use for a generated member representing an if pointcut (prefix will be 'ajc$if$').
	 * If left blank, a suffix will be generated.
	 */
    String ifNameSuffix() default "";

}
