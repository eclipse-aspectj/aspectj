/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     	initial implementation 
 *     AMC	01.20.2003  extended for AspectJ 1.1 compiler options
 * ******************************************************************/

 
 
package org.aspectj.ajde;

import java.util.Map;
import java.util.Set;

/**
 * When a particular option is not set its documented default is used.
 */
public interface BuildOptionsAdapter {
	
	// Version constants
	public static final String VERSION_13 = "1.3";
	public static final String VERSION_14 = "1.4";
	public static final String VERSION_15 = "1.5";
	public static final String VERSION_16 = "1.6";
		
	// Warning constants
	public static final String WARN_CONSTRUCTOR_NAME   	 = "constructorName";
	public static final String WARN_PACKAGE_DEFAULT_METHOD = "packageDefaultMethod";	
	public static final String WARN_DEPRECATION 		   	 = "deprecation";	
	public static final String WARN_MASKED_CATCH_BLOCKS 	 = "maskedCatchBlocks";	
	public static final String WARN_UNUSED_LOCALS 		 = "unusedLocals";	
	public static final String WARN_UNUSED_ARGUMENTS 		 = "unusedArguments";	
	public static final String WARN_UNUSED_IMPORTS 		 = "unusedImports";	
	public static final String WARN_SYNTHETIC_ACCESS 		 = "syntheticAccess";	
	public static final String WARN_ASSERT_IDENITIFIER 	 = "assertIdentifier";	
	public static final String WARN_NLS					 = "nonExternalisedString";
	
	// Debug constants	
	public static final String DEBUG_SOURCE = "source";
	public static final String DEBUG_LINES  = "lines";
	public static final String DEBUG_VARS   = "vars";
	public static final String DEBUG_ALL    = "all";
	
	
	/**
	 * This map shortcuts any other Java-specific options that would get set by return
	 * values from the other methods.
	 * 
	 * @return	a map of all the java-specific options, null if individual options will be passed
	 */
	public Map getJavaOptionsMap();  
	
//	/**
//	 * Use javac to generate .class files.  The default is "false".
//	 * From -usejavac
//	 * @deprecated Not supported from AspectJ 1.1 onwards
//	 */
//	public boolean getUseJavacMode();
//	
//	/**
//	 * Only relevant with Use Javac or Preprocess modes.  Specify where to place
//	 * intermediate .java files.  The default is "workingdir".
//	 * From -workingdir
//	 * @deprecated Not supported from AspectJ 1.1 onwards
//	 */
//	public String getWorkingOutputPath();
	
//	/**
//	 * Generate regular Java code into the Working OutputPath.  Don't try to generate
//	 * any .class files.  The default is "false".
//	 * From -source
//	 * @deprecated Not supported from AspectJ 1.1 onwards
//	 */
//	public boolean getPreprocessMode();
//	
	/**
	 * Specify character encoding used by source files.  The default is the current
	 * JVM's default.
	 * From -encoding
	 */
	public String getCharacterEncoding();
	
//	/**
//	 * Support assertions as defined in JLS-1.4.  The default is "false".
//	 * @deprecated Use getComplianceLevel instead
//	 */
//	public boolean getSourceOnePointFourMode();
//	

    /**
     * Run compiles incrementally.
     * @since AspectJ 1.1
     */
    public boolean getIncrementalMode();

//	/**
//	 * Be extra-lenient in interpreting the Java specification.  The default is "false", 
//	 * i.e. "regular" mode.
//	 * From -lenient
//	 * @deprecated Not supported from AspectJ 1.1 onwards
//	 */
//	public boolean getLenientSpecMode();
	
//	/**
//	 * Be extra-strict in interpreting the Java specification.  The default is "false", 
//	 * i.e. "regular" mode.
//	 * From -strict
//	 * @deprecated Not supported from AspectJ 1.1 onwards
//	 */
//	public boolean getStrictSpecMode();
//	
//	/**
//	 * Make the use of some features from pre-1.0 versions of AspectJ be warnings to ease
//	 * porting of old code.  The default is "false".
//	 * From -porting
//	 * @deprecated Not supported from AspectJ 1.1 onwards
//	 */
//	public boolean getPortingMode();
	
	/**
	 * The non-standard, typically prefaced with -X when used with a command line compiler.
	 * The default is no non-standard options.
	 */
	public String getNonStandardOptions();
	
	// ----------------------------------
	// New options added for AspectJ 1.1 from this point onwards
	
	/**
	 * JDK Compliance level to be used by the compiler, either 
	 * VERSION_13, VERSION_14 or VERSION_15.
	 * From -1.3 / -1.4 / -1.5
	 */
	public String getComplianceLevel();
	
	/**
	 * Source compatibility level, either VERSION_13, VERSION_14
	 * or VERSION_15
	 * From -source (eclipse option)
	 */
	public String getSourceCompatibilityLevel();
	
	/**
	 * Optional warnings, empty List is equivalent to -warn:none,
	 * returning null uses eclipse compiler default settings
	 * From -warn:xxx,yyy
	 */
	public Set getWarnings();
	
	/**
	 * Debug level. DEBUG_ALL == {SOURCE, LINES, VARS}.
	 * Empty list is equivalent to -g:none, returning
	 * non uses eclipse compiler default settings
	 * From -g:xxx
	 */
	public Set getDebugLevel();
	
	/**
	 * No errors generated for unresolved imports
	 * From -noImportError
	 */
	public boolean getNoImportError();
	
	/**
	 * Preserve all unused local variables (for debug)
	 * From -preserveAllLocals
	 */
	public boolean getPreserveAllLocals();
	
}
