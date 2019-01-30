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
package org.aspectj.ajde.ui.javaoptions;

import java.util.Map;

import org.aspectj.ajde.core.JavaOptions;

/**
 * Class which handles the setting of the java options and the java options map required by
 * ICompilerConfiguration#getJavaOptionsMap()
 */
public class JavaBuildOptions {

	private Map<String, String> javaBuildOptions;

	public JavaBuildOptions() {
		javaBuildOptions = JavaOptions.getDefaultJavaOptions();
	}

	public Map<String, String> getJavaBuildOptionsMap() {
		return javaBuildOptions;
	}

	public void setOption(String javaOption, String value) {
		javaBuildOptions.put(javaOption, value);
	}

	// ----------------- compliance settings ---------------

	// compliance
	public void setComplianceLevel(String level) {
		if (JavaOptions.isValidJvmVersion(level)) {
			javaBuildOptions.put(JavaOptions.COMPLIANCE_LEVEL, level);
		}
	}

	// source
	public void setSourceCompatibilityLevel(String level) {
		if (JavaOptions.isValidJvmVersion(level)) {
			javaBuildOptions.put(JavaOptions.SOURCE_COMPATIBILITY_LEVEL, level);
		}
	}

	// target
	public void setTargetLevel(String level) {
		if (JavaOptions.isValidJvmVersion(level)) {
			javaBuildOptions.put(JavaOptions.TARGET_COMPATIBILITY_LEVEL, level);
		}
	}

	// ---------------- compiler warning options ------------------

	// warn method with constructor name
	public void setWarnMethodWithConstructorName(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_METHOD_WITH_CONSTRUCTOR_NAME, option);
		}
	}

	// warn overriding package default method
	public void setWarnOverridingPackageDefaultMethod(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_OVERRIDING_PACKAGE_DEFAULT_METHOD, option);
		}
	}

	// warn deprecation
	public void setWarnDeprecation(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_DEPRECATION, option);
		}
	}

	// warn hidden catch blocks
	public void setWarnHiddenCatchBlocks(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_HIDDEN_CATCH_BLOCKS, option);
		}
	}

	// warn unused locals
	public void setWarnUnusedLocals(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_UNUSED_LOCALS, option);
		}
	}

	// warn unused parameters
	public void setWarnUnusedParameters(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_UNUSED_PARAMETER, option);
		}
	}

	// warn unused imports
	public void setWarnUnusedImports(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_UNUSED_IMPORTS, option);
		}
	}

	// warn synthetic access
	public void setWarnSyntheticAccess(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_SYNTHETIC_ACCESS, option);
		}
	}

	// warn assert identifier
	public void setWarnAssertIdentifier(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_ASSERT_IDENITIFIER, option);
		}
	}

	// warn non nls
	public void setWarnNonNLS(String option) {
		if (JavaOptions.isIgnoreOrWarning(option)) {
			javaBuildOptions.put(JavaOptions.WARN_NON_NLS, option);
		}
	}

	// --------------- debug options --------------------

	// debug source
	public void setDebugSource(String option) {
		if (JavaOptions.isGenerateOrNot(option)) {
			javaBuildOptions.put(JavaOptions.DEBUG_SOURCE, option);
		}
	}

	// debug lines
	public void setDebugLines(String option) {
		if (JavaOptions.isGenerateOrNot(option)) {
			javaBuildOptions.put(JavaOptions.DEBUG_LINES, option);
		}
	}

	// debug vars
	public void setDebugVariables(String option) {
		if (JavaOptions.isGenerateOrNot(option)) {
			javaBuildOptions.put(JavaOptions.DEBUG_VARS, option);
		}
	}

	// preserve all locals
	public void setPreserveAllLocals(String value) {
		if (JavaOptions.isValidPreserveAllLocalsOption(value)) {
			javaBuildOptions.put(JavaOptions.PRESERVE_ALL_LOCALS, value);
		}
	}

	// ----------- other settings
	// character encoding
	public void setCharacterEncoding(String value) {
		javaBuildOptions.put(JavaOptions.CHARACTER_ENCODING, value);
	}

}
