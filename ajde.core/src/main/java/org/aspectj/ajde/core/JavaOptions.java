/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.core;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Class containing the current custom java options
 */
public final class JavaOptions {
	
	public static final String COMPLIANCE_LEVEL = CompilerOptions.OPTION_Compliance;
	public static final String SOURCE_COMPATIBILITY_LEVEL = CompilerOptions.OPTION_Source;
	public static final String TARGET_COMPATIBILITY_LEVEL = CompilerOptions.OPTION_TargetPlatform;
	// Version constants
	public static final String VERSION_13 = CompilerOptions.VERSION_1_3;
	public static final String VERSION_14 = CompilerOptions.VERSION_1_4;
	public static final String VERSION_15 = CompilerOptions.VERSION_1_5;
	public static final String VERSION_16 = CompilerOptions.VERSION_1_6;
	
	// by default will use the platform default encoding 
	public static final String CHARACTER_ENCODING = CompilerOptions.OPTION_Encoding;
	
	// indicates if unused/optimizable local variables need to be preserved (debugging purpose)
	public static final String PRESERVE_ALL_LOCALS = CompilerOptions.OPTION_PreserveUnusedLocal;
	public static final String PRESERVE = CompilerOptions.PRESERVE;
	public static final String OPTIMIZE = CompilerOptions.OPTIMIZE_OUT;
	
	// Warning constants
	public static final String WARN_METHOD_WITH_CONSTRUCTOR_NAME = CompilerOptions.OPTION_ReportMethodWithConstructorName;
	public static final String WARN_OVERRIDING_PACKAGE_DEFAULT_METHOD = CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod;	
	public static final String WARN_DEPRECATION = CompilerOptions.OPTION_ReportDeprecation;	
	public static final String WARN_HIDDEN_CATCH_BLOCKS = CompilerOptions.OPTION_ReportHiddenCatchBlock;	
	public static final String WARN_UNUSED_LOCALS = CompilerOptions.OPTION_ReportUnusedLocal;	
	public static final String WARN_UNUSED_PARAMETER = CompilerOptions.OPTION_ReportUnusedParameter;	
	public static final String WARN_UNUSED_IMPORTS = CompilerOptions.OPTION_ReportUnusedImport;	
	public static final String WARN_SYNTHETIC_ACCESS = CompilerOptions.OPTION_ReportSyntheticAccessEmulation;	
	public static final String WARN_ASSERT_IDENITIFIER = CompilerOptions.OPTION_ReportAssertIdentifier;	
	public static final String WARN_NON_NLS = CompilerOptions.OPTION_ReportNonExternalizedStringLiteral;
	// warning option constants
	public static final String IGNORE = CompilerOptions.IGNORE;
	public static final String WARNING = CompilerOptions.WARNING;
	
	// Debug constants	
	public static final String DEBUG_SOURCE = CompilerOptions.OPTION_SourceFileAttribute;
	public static final String DEBUG_LINES  = CompilerOptions.OPTION_LocalVariableAttribute;
	public static final String DEBUG_VARS   = CompilerOptions.OPTION_LineNumberAttribute;
	// Debug option constants
	public static final String GENERATE = CompilerOptions.GENERATE;
	public static final String DO_NOT_GENERATE = CompilerOptions.DO_NOT_GENERATE;
	
	private static Map<String,String> defaultOptionsMap;
	
	/**
	 * @return the java options map with the default settings
	 */
	public static Map<String,String> getDefaultJavaOptions() {
		if (defaultOptionsMap != null) return defaultOptionsMap;
		
		defaultOptionsMap = new HashMap<>();
		defaultOptionsMap.put(COMPLIANCE_LEVEL, VERSION_14);
		defaultOptionsMap.put(SOURCE_COMPATIBILITY_LEVEL, VERSION_13);
		defaultOptionsMap.put(PRESERVE_ALL_LOCALS, OPTIMIZE);
		defaultOptionsMap.put(WARN_METHOD_WITH_CONSTRUCTOR_NAME, IGNORE);
		defaultOptionsMap.put(WARN_OVERRIDING_PACKAGE_DEFAULT_METHOD, IGNORE);
		defaultOptionsMap.put(WARN_DEPRECATION, IGNORE);
		defaultOptionsMap.put(WARN_HIDDEN_CATCH_BLOCKS, IGNORE);
		defaultOptionsMap.put(WARN_UNUSED_LOCALS, IGNORE);
		defaultOptionsMap.put(WARN_UNUSED_PARAMETER, IGNORE);
		defaultOptionsMap.put(WARN_UNUSED_IMPORTS, IGNORE);
		defaultOptionsMap.put(WARN_SYNTHETIC_ACCESS, IGNORE);
		defaultOptionsMap.put(WARN_ASSERT_IDENITIFIER, IGNORE);
		defaultOptionsMap.put(WARN_NON_NLS, IGNORE);
		defaultOptionsMap.put(DEBUG_SOURCE, GENERATE);
		defaultOptionsMap.put(DEBUG_LINES, GENERATE);
		defaultOptionsMap.put(DEBUG_VARS, DO_NOT_GENERATE);
		
		return defaultOptionsMap;
	}
	
	/**
	 * @return true if the given value is a valid JVM version
	 * (JavaOptions.VERSION_13, JavaOptions.VERSION_134, JavaOptions.VERSION_15,
	 * JavaOptions.VERSION_16) and false otherwise
	 */
	public static boolean isValidJvmVersion(String value) {
		return VERSION_13.equals(value) || VERSION_14.equals(value)
			|| VERSION_15.equals(value) || VERSION_16.equals(value);
	}

	/**
	 * @return true if the given option is JavaOptions.PRESERVE or 
	 * JavaOptions.OPTIMIZE and false otherwise
	 */
	public static boolean isValidPreserveAllLocalsOption(String option) {
		return PRESERVE.equals(option) || OPTIMIZE.equals(option);
	}
	
	/**
	 * @return true if the given option is JavaOptions.IGNORE or 
	 * JavaOptions.WARNING and false otherwise
	 */
	public static boolean isIgnoreOrWarning(String option) {
		return IGNORE.equals(option) || WARNING.equals(option);
	}
	
	/**
	 * @return true if the given option is JavaOptions.GENERATE or 
	 * JavaOptions.DO_NOT_GENERATE and false otherwise
	 */
	public static boolean isGenerateOrNot(String option) {
		return GENERATE.equals(option) || DO_NOT_GENERATE.equals(option);
	}

}
