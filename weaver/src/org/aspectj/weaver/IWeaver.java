/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

/**
 * A weaver is given all the aspects it will weave.  It should create an appropriate kind of 
 * IWorld.  It then should be given a bunch of classes (types with implementation), creates an
 * appropriate IClassWeaver for each such class, and weaves.  The IWeaver is responsible for 
 * IO.
 */
public interface IWeaver {

}
