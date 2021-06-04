/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.internal.lang.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author colyer
 * Marker annotation for code style declare precedence declarations
 * ajc prefix used to indicate that this annotation is *internal*
 * Can't use DeclarePrecedence as that has target = type and we
 * need method to cope with the (rare) case of multiple declare
 * precendence statements in the same aspect.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ajcDeclarePrecedence {
	String value();
}
