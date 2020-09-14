/********************************************************************
 * Copyright (c) 2006 Contributors.All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 *   Helen Hawkins          bug 166580 and 148190
 * ******************************************************************/
package org.aspectj.ajde.core;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Interface that handles where the compilation output is sent. Allows for the output folder to be different for different source
 * files.
 */
public interface IOutputLocationManager {

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
	 * For environments where multiple source folders are supported, they need to be included in the model. This method allows
	 * AspectJ to determine which source folder a source file came from. Example return values would be "src" or "main/java"
	 * 
	 * @param sourceFile the File object for the source file
	 * @return the source folder where this file came from, or null if in project root or source folders not supported.
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
	List<File> getAllOutputLocations();

	/**
	 * Return the default output location (for example, &lt;my_project&gt;/bin). This is where classes which are on the inpath will be
	 * placed.
	 */
	File getDefaultOutputLocation();

	/**
	 * Callback from the compiler to indicate that a file has been written to disk, the type of the file (if known) is also
	 * supplied.
	 * 
	 * @param outputfile the file that has been written
	 * @param fileType the kind of file from the FILETYPE_XXX constants defined in this type
	 */
	void reportFileWrite(String outputfile, int fileType);

	/**
	 * @return a Map&lt;File,String&gt; from inpath absolute paths to handle components
	 */
	Map<File, String> getInpathMap();

	/**
	 * Callback from the compiler to indicate that a file has been removed from disk, the type of the file (if known) is also
	 * supplied.
	 * 
	 * @param file the file that has been written
	 * @param fileType the kind of file from the FILETYPE_XXX constants defined in this type
	 */
	void reportFileRemove(String file, int fileType);

	int discoverChangesSince(File dir, long buildtime);

	// match numbers in CompilationResultDestinationManager - ought to factor into super interface
	int FILETYPE_UNKNOWN = 0;
	int FILETYPE_CLASS = 1;
	int FILETYPE_OUTJAR = 2;
	int FILETYPE_RESOURCE = 3;
}
