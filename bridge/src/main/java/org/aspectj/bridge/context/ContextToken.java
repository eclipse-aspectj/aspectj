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
package org.aspectj.bridge.context;

/**
 * When an entry is added to the CompilationAndWeavingContext stack,
 * a ContextToken is returned.
 * When leaving a compilation or weaving phase, this token must be supplied.
 * The token details are opaque to clients
 */
public interface ContextToken {}
