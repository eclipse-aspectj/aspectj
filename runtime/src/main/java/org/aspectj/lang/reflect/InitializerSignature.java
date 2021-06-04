/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2006 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.lang.reflect;
import java.lang.reflect.Constructor;

/**
 * Signature for static and instance initializers.
 * Static initializers have no parameters or exceptions,
 * so empty arrays are returned from the CodeSignature methods.
 */
public interface InitializerSignature extends CodeSignature {
    /**
     * @return Constructor associated with this initializer,
     * or null in the case of interface initializers and
     * static initializers.
     */
    Constructor getInitializer();
}
