/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
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
