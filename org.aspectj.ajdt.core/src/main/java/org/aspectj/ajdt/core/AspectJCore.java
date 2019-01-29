/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.core;

import java.util.Map;

import org.aspectj.ajdt.internal.core.builder.AjCompilerOptions;
import org.aspectj.org.eclipse.jdt.core.JavaCore;

/**
 * This is the plugin class for AspectJ.
 */
public class AspectJCore extends JavaCore {

	public static final String COMPILER_PB_INVALID_ABSOLUTE_TYPE_NAME  = AjCompilerOptions.OPTION_ReportInvalidAbsoluteTypeName;
	public static final String COMPILER_PB_INVALID_WILDCARD_TYPE_NAME  = AjCompilerOptions.OPTION_ReportInvalidWildcardTypeName;
	public static final String COMPILER_PB_UNRESOLVABLE_MEMBER         = AjCompilerOptions.OPTION_ReportUnresolvableMember;
	public static final String COMPILER_PB_TYPE_NOT_EXPOSED_TO_WEAVER  = AjCompilerOptions.OPTION_ReportTypeNotExposedToWeaver;
	public static final String COMPILER_PB_SHADOW_NOT_IN_STRUCTURE     = AjCompilerOptions.OPTION_ReportShadowNotInStructure;
	public static final String COMPILER_PB_UNMATCHED_SUPERTYPE_IN_CALL = AjCompilerOptions.OPTION_ReportUnmatchedSuperTypeInCall;
	public static final String COMPILER_PB_CANNOT_IMPLEMENT_LAZY_TJP   = AjCompilerOptions.OPTION_ReportCannotImplementLazyTJP;
	public static final String COMPILER_PB_NEED_SERIAL_VERSION_UID     = AjCompilerOptions.OPTION_ReportNeedSerialVersionUIDField;
	public static final String COMPILER_PB_INCOMPATIBLE_SERIAL_VERSION = AjCompilerOptions.OPTION_ReportIncompatibleSerialVersion;
	
	public static final String COMPILER_TERMINATE_AFTER_COMPILATION             = AjCompilerOptions.OPTION_TerminateAfterCompilation;
	public static final String COMPILER_SERIALIZABLE_ASPECTS = AjCompilerOptions.OPTION_XSerializableAspects;
	public static final String COMPILER_LAZY_TJP             = AjCompilerOptions.OPTION_XLazyThisJoinPoint;
	public static final String COMPILER_NO_ADVICE_INLINE     = AjCompilerOptions.OPTION_XNoInline;
	public static final String COMPILER_NOT_REWEAVABLE       = AjCompilerOptions.OPTION_XNotReweavable;
	
	public AspectJCore() {
		super();
	}
	
	public static AspectJCore getAspectJCore() {
		return (AspectJCore) getPlugin();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.JavaCore#getCompilerOptions()
	 */
	protected Map getCompilerOptions() {
		return new AjCompilerOptions().getMap();
	}
}
