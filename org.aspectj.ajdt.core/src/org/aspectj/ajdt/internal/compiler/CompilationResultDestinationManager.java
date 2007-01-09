/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler;

import java.io.File;
import java.util.List;

/**
 * acts as a bridge from ajde's OutputLocationManager interface to the compiler internals
 * @author adrian
 *
 */
public interface CompilationResultDestinationManager {

	/**
	 * Return the directory root under which the results of compiling the given
	 * source file. For example, if the source file contains the type a.b.C, and
	 * this method returns "target/classes" the resulting class file will be written
	 * to "target/classes/a/b/C.class"
	 * 
	 * @param compilationUnit  the compilation unit that has been
	 *  compiled
	 * @return a File object representing the root directory under which compilation results for this
	 *  unit should be written
	 */
	File getOutputLocationForClass(File compilationUnit);
	
	/**
	 * When copying resources from source folders to output location, return the
	 * root directory under which the resource should be copied.
	 * 
	 * @param resource the resource to be copied
	 * @return a File object representing the root directory under which this resource
	 * should be copied
	 */
	File getOutputLocationForResource(File resource);
	
	/**
	 * Return a list of all output locations handled by this OutputLocationManager
	 */
	List /*File*/ getAllOutputLocations();
	
	/**
	 * Return the default output location (for example, <my_project>/bin). This is
	 * where classes which are on the inpath will be placed.
	 */
	File getDefaultOutputLocation();
	
}
