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
import java.util.Map;

/**
 * acts as a bridge from ajde's OutputLocationManager interface to the compiler internals
 * 
 * @author adrian
 * 
 */
public interface CompilationResultDestinationManager {

	/**
	 * Return the directory root under which the results of compiling the given source file. For example, if the source file
	 * contains the type a.b.C, and this method returns "target/classes" the resulting class file will be written to
	 * "target/classes/a/b/C.class"
	 * 
	 * @param compilationUnit the compilation unit that has been compiled
	 * @return a File object representing the root directory under which compilation results for this unit should be written
	 */
	File getOutputLocationForClass(File compilationUnit);

	/**
	 * Return the source folder where this source file came from, relative to the project root. For example 'src' or 'src/main/java'
	 * or 'src/test/java'
	 * 
	 * @param sourceFile the file for which the source folder should be determined
	 * @return the source folder
	 */
	String getSourceFolderForFile(File sourceFile);

	/**
	 * When copying resources from source folders to output location, return the root directory under which the resource should be
	 * copied.
	 * 
	 * @param resource the resource to be copied
	 * @return a File object representing the root directory under which this resource should be copied
	 */
	File getOutputLocationForResource(File resource);

	/**
	 * Return a list of all output locations handled by this OutputLocationManager
	 */
	List /* File */getAllOutputLocations();

	/**
	 * Return the default output location (for example, &lt;my_project&gt;/bin). This is where classes which are on the inpath will be
	 * placed.
	 */
	File getDefaultOutputLocation();

	/**
	 * Report that a class file is being written to the specified location.
	 * 
	 * @param outputfile the output file (including .class suffix)
	 */
	void reportFileWrite(String outputfile, int filetype);

	/**
	 * Report that a class file is being deleted from the specified location.
	 * 
	 * @param outputfile the output file (including .class suffix)
	 */
	void reportFileRemove(String outputfile, int filetype);

	Map getInpathMap();

	int discoverChangesSince(File dir, long buildtime);

	// match numbers in IOutputLocationManager - ought to factor into super interface
	int FILETYPE_UNKNOWN = 0;
	int FILETYPE_CLASS = 1;
	int FILETYPE_OUTJAR = 2;
	int FILETYPE_RESOURCE = 3;
	
}
