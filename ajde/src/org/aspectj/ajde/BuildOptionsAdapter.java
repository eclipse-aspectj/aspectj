/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
 
package org.aspectj.ajde;

/**
 * When a particular option is not set its documented default is used.
 */
public interface BuildOptionsAdapter {
	
	/**
	 * Use javac to generate .class files.  The default is "false".
	 */
	public boolean getUseJavacMode();
	
	/**
	 * Only relevant with Use Javac or Preprocess modes.  Specify where to place
	 * intermediate .java files.  The default is "workingdir".
	 */
	public String getWorkingOutputPath();
	
	/**
	 * Generate regular Java code into the Working OutputPath.  Don't try to generate
	 * any .class files.  The default is "false".
	 */
	public boolean getPreprocessMode();
	
	/**
	 * Specify character encoding used by source files.  The default is the current
	 * JVM's default.
	 */
	public String getCharacterEncoding();
	
	/**
	 * Support assertions as defined in JLS-1.4.  The default is "false".
	 */
	public boolean getSourceOnePointFourMode();
	  
	/**
	 * Be extra-lenient in interpreting the Java specification.  The default is "false", 
	 * i.e. "regular" mode.
	 */
	public boolean getLenientSpecMode();
	
	/**
	 * Be extra-strict in interpreting the Java specification.  The default is "false", 
	 * i.e. "regular" mode.
	 */
	public boolean getStrictSpecMode();
	
	/**
	 * Make the use of some features from pre-1.0 versions of AspectJ be warnings to ease
	 * porting of old code.  The default is "false".
	 */
	public boolean getPortingMode();
	
	/**
	 * The non-standard, typically prefaced with -X when used with a command line compiler.
	 * The default is no non-standard options.
	 */
	public String getNonStandardOptions();
}
