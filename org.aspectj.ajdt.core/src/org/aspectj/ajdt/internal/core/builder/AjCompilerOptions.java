/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.util.Map;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.aspectj.weaver.Constants;

/**
 * Compiler options used by Eclipse integration (AJDT)
 */
public class AjCompilerOptions extends CompilerOptions {

	// AspectJ Lint options
	public static final String OPTION_ReportInvalidAbsoluteTypeName = "org.aspectj.ajdt.core.compiler.lint.InvalidAbsoluteTypeName";
	public static final String OPTION_ReportInvalidWildcardTypeName = "org.aspectj.ajdt.core.compiler.lint.WildcardTypeName";
	public static final String OPTION_ReportUnresolvableMember = "org.aspectj.ajdt.core.compiler.lint.UnresolvableMember";
	public static final String OPTION_ReportTypeNotExposedToWeaver = "org.aspectj.ajdt.core.compiler.lint.TypeNotExposedToWeaver";
	public static final String OPTION_ReportShadowNotInStructure = "org.aspectj.ajdt.core.compiler.lint.ShadowNotInStructure";
	public static final String OPTION_ReportUnmatchedSuperTypeInCall = "org.aspectj.ajdt.core.compiler.list.UnmatchedSuperTypeInCall";
	public static final String OPTION_ReportCannotImplementLazyTJP = "org.aspectj.ajdt.core.compiler.lint.CannotImplementLazyTJP";
	public static final String OPTION_ReportNeedSerialVersionUIDField = "org.aspectj.ajdt.core.compiler.lint.NeedSerialVersionUIDField";
	public static final String OPTION_ReportIncompatibleSerialVersion = "org.aspectj.ajdt.core.compiler.lint.BrokeSerialVersionCompatibility";

	// General AspectJ Compiler options (excludes paths etc, these are handled separately)
	public static final String OPTION_TerminateAfterCompilation = "org.aspectj.ajdt.core.compiler.weaver.TerminateAfterCompilation";
	public static final String OPTION_XSerializableAspects = "org.aspectj.ajdt.core.compiler.weaver.XSerializableAspects";
	public static final String OPTION_XLazyThisJoinPoint = "org.aspectj.ajdt.core.compiler.weaver.XLazyThisJoinPoint";
	public static final String OPTION_XNoInline = "org.aspectj.ajdt.core.compiler.weaver.XNoInline";
	public static final String OPTION_XNotReweavable = "org.aspectj.ajdt.core.compiler.weaver.XNotReweavable";
	public static final String OPTION_XHasMember = "org.aspectj.ajdt.core.compiler.weaver.XHasMember";
	public static final String OPTION_XdevPinpoint = "org.aspectj.ajdt.core.compiler.weaver.XdevPinpoint";

	// these next four not exposed by IDEs
	public static final String OPTION_XDevNoAtAspectJProcessing = "org.aspectj.ajdt.core.compiler.ast.NoAtAspectJProcessing";
	public static final String OPTION_GenerateModel = "org.aspectj.ajdt.core.compiler.model.GenerateModel";
	public static final String OPTION_GenerateJavaDocsInModel = "org.aspectj.ajdt.core.compiler.model.GenerateJavaDocsInModel";
	public static final String OPTION_Emacssym = "org.aspectj.ajdt.core.compiler.model.Emacssym";


	// constants for irritant levels
	public static final int InvalidAbsoluteTypeName = IrritantSet.GROUP3 | ASTNode.Bit1;
	public static final int InvalidWildCardTypeName = IrritantSet.GROUP3 | ASTNode.Bit2;
	public static final int UnresolvableMember = IrritantSet.GROUP3 | ASTNode.Bit3;
	public static final int TypeNotExposedToWeaver = IrritantSet.GROUP3 | ASTNode.Bit4;
	public static final int ShadowNotInStructure = IrritantSet.GROUP3 | ASTNode.Bit5;
	public static final int UnmatchedSuperTypeInCall = IrritantSet.GROUP3 | ASTNode.Bit6;
	public static final int CannotImplementLazyTJP = IrritantSet.GROUP3 | ASTNode.Bit7;
	public static final int NeedSerialVersionUIDField = IrritantSet.GROUP3 | ASTNode.Bit8;
	public static final int IncompatibleSerialVersion = IrritantSet.GROUP3 | ASTNode.Bit9;

	public boolean terminateAfterCompilation = false;
	public boolean xSerializableAspects = false;
	public boolean xLazyThisJoinPoint = false;
	public boolean xNoInline = false;
	public boolean xNotReweavable = false;
	public boolean xHasMember = false;
	public boolean xdevPinpoint = false;
	public boolean showWeavingInformation = false;

	public String xOptionalJoinpoints = null;

	// If true - autoboxing behaves differently ...
	public boolean behaveInJava5Way = true;

	public boolean timing = false;

	// Specifies the level of the aspectjrt.jar we are targetting
	public String targetAspectjRuntimeLevel = Constants.RUNTIME_LEVEL_DEFAULT;

	public String xConfigurationInfo;
	public boolean addSerialVerUID = false;
	public boolean xmlConfigured = false;
	public boolean makeReflectable = false;

	// these next four not exposed by IDEs
	public boolean generateModel = false;
	public boolean generateJavaDocsInModel = false;
	public boolean generateEmacsSymFiles = false;
	public boolean noAtAspectJProcessing = false;

	/**
	 * Generates a map of cross references based on information in the structure model.
	 */
	public boolean generateCrossRefs = false;

	// Check aspectjrt*.jar exists and within it the version number is right (see AjBuildManager.checkRtJar())
	public boolean checkRuntimeVersion = true;

	public boolean proceedOnError = false;

	/**
	 * Initializing the compiler options with defaults
	 */
	public AjCompilerOptions() {
		super();
		setAspectJWarningDefaults();
	}

	public AjCompilerOptions(Map<String,String> settings) {
		setAspectJWarningDefaults();
		if (settings == null) {
			return;
		}
		set(settings);
	}

	@Override
	public Map<String,String> getMap() {
		Map<String,String> map = super.getMap();
		// now add AspectJ additional options		
		map.put(OPTION_ReportInvalidAbsoluteTypeName, getSeverityString(InvalidAbsoluteTypeName));
		map.put(OPTION_ReportInvalidWildcardTypeName, getSeverityString(InvalidWildCardTypeName));
		map.put(OPTION_ReportUnresolvableMember, getSeverityString(UnresolvableMember));
		map.put(OPTION_ReportTypeNotExposedToWeaver, getSeverityString(TypeNotExposedToWeaver));
		map.put(OPTION_ReportShadowNotInStructure, getSeverityString(ShadowNotInStructure));
		map.put(OPTION_ReportUnmatchedSuperTypeInCall, getSeverityString(UnmatchedSuperTypeInCall));
		map.put(OPTION_ReportCannotImplementLazyTJP, getSeverityString(CannotImplementLazyTJP));
		map.put(OPTION_ReportNeedSerialVersionUIDField, getSeverityString(NeedSerialVersionUIDField));
		map.put(OPTION_ReportIncompatibleSerialVersion, getSeverityString(IncompatibleSerialVersion));
		map.put(CompilerOptions.OPTION_ReportSwallowedExceptionInCatchBlock,
				getSeverityString(CompilerOptions.SwallowedExceptionInCatchBlock));

		map.put(OPTION_TerminateAfterCompilation, this.terminateAfterCompilation ? ENABLED : DISABLED);
		map.put(OPTION_XSerializableAspects, this.xSerializableAspects ? ENABLED : DISABLED);
		map.put(OPTION_XLazyThisJoinPoint, this.xLazyThisJoinPoint ? ENABLED : DISABLED);
		map.put(OPTION_XNoInline, this.xNoInline ? ENABLED : DISABLED);
		map.put(OPTION_XNotReweavable, this.xNotReweavable ? ENABLED : DISABLED);
		map.put(OPTION_XHasMember, this.xHasMember ? ENABLED : DISABLED);
		map.put(OPTION_XdevPinpoint, this.xdevPinpoint ? ENABLED : DISABLED);

		map.put(OPTION_GenerateModel, this.generateModel ? ENABLED : DISABLED);
		map.put(OPTION_GenerateJavaDocsInModel, this.generateJavaDocsInModel ? ENABLED : DISABLED);
		map.put(OPTION_Emacssym, this.generateEmacsSymFiles ? ENABLED : DISABLED);
		map.put(OPTION_XDevNoAtAspectJProcessing, this.noAtAspectJProcessing ? ENABLED : DISABLED);

		return map;
	}

	@Override
	public void set(Map<String,String> optionsMap) {
		super.set(optionsMap);
		Object optionValue;
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedPrivateMember)) != null) {
			updateSeverity(UnusedPrivateMember, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidAbsoluteTypeName)) != null) {
			updateSeverity(InvalidAbsoluteTypeName, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidWildcardTypeName)) != null) {
			updateSeverity(InvalidWildCardTypeName, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportUnresolvableMember)) != null) {
			updateSeverity(UnresolvableMember, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportTypeNotExposedToWeaver)) != null) {
			updateSeverity(TypeNotExposedToWeaver, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportShadowNotInStructure)) != null) {
			updateSeverity(ShadowNotInStructure, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportUnmatchedSuperTypeInCall)) != null) {
			updateSeverity(UnmatchedSuperTypeInCall, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportCannotImplementLazyTJP)) != null) {
			updateSeverity(CannotImplementLazyTJP, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportNeedSerialVersionUIDField)) != null) {
			updateSeverity(NeedSerialVersionUIDField, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportIncompatibleSerialVersion)) != null) {
			updateSeverity(IncompatibleSerialVersion, optionValue);
		}
		if ((optionValue = optionsMap.get(CompilerOptions.OPTION_ReportSwallowedExceptionInCatchBlock)) != null) {
			updateSeverity(CompilerOptions.SwallowedExceptionInCatchBlock, optionValue);
		}

		if ((optionValue = optionsMap.get(OPTION_TerminateAfterCompilation)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.terminateAfterCompilation = true;
			} else if (DISABLED.equals(optionValue)) {
				this.terminateAfterCompilation = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_XSerializableAspects)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xSerializableAspects = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xSerializableAspects = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_XLazyThisJoinPoint)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xLazyThisJoinPoint = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xLazyThisJoinPoint = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_XNoInline)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xNoInline = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xNoInline = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_XNotReweavable)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xNotReweavable = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xNotReweavable = false;
			}
		}
		/*
		 * if ((optionValue = optionsMap.get(OPTION_XReweavableCompress)) != null) { if (ENABLED.equals(optionValue)) {
		 * this.xReweavableCompress = true; } else if (DISABLED.equals(optionValue)) { this.xReweavableCompress = false; } }
		 */

		if ((optionValue = optionsMap.get(OPTION_XHasMember)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xHasMember = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xHasMember = false;
			}
		}

		if ((optionValue = optionsMap.get(OPTION_XdevPinpoint)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xdevPinpoint = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xdevPinpoint = false;
			}
		}

		if ((optionValue = optionsMap.get(OPTION_GenerateModel)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.generateModel = true;
			} else if (DISABLED.equals(optionValue)) {
				this.generateModel = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_GenerateJavaDocsInModel)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.generateJavaDocsInModel = true;
			} else if (DISABLED.equals(optionValue)) {
				this.generateJavaDocsInModel = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_Emacssym)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.generateEmacsSymFiles = true;
			} else if (DISABLED.equals(optionValue)) {
				this.generateEmacsSymFiles = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_XDevNoAtAspectJProcessing)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.noAtAspectJProcessing = true;
			} else if (DISABLED.equals(optionValue)) {
				this.noAtAspectJProcessing = false;
			}
		}

	}

	/**
	 * Add these warnings to the default set...
	 */
	private void setAspectJWarningDefaults() {
		super.warningThreshold = new IrritantSet(super.warningThreshold);
		super.warningThreshold.set(InvalidAbsoluteTypeName | UnresolvableMember | TypeNotExposedToWeaver
				| UnmatchedSuperTypeInCall | CannotImplementLazyTJP);
		super.warningThreshold.set(CompilerOptions.SwallowedExceptionInCatchBlock);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(super.toString());
		// now add AspectJ additional options
		buf.append("\n\tAspectJ Specific Options:");
		buf.append("\n\t- terminate after compilation: ").append(this.terminateAfterCompilation ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- no inline (X option): ").append(this.xNoInline ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- generate serializable aspects (X option): ").append(this.xSerializableAspects ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- lazy thisJoinPoint (X option): ").append(this.xLazyThisJoinPoint ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- generate non-reweavable class files (X option): ").append(this.xNotReweavable ? ENABLED : DISABLED); //$NON-NLS-1$	
		buf.append("\n\t- has member support (X option): ").append(this.xHasMember ? ENABLED : DISABLED); //$NON-NLS-1$

		buf.append("\n\t- generate AJDE model: ").append(this.generateModel ? ENABLED : DISABLED); //$NON-NLS-1$		
		buf.append("\n\t- generate Javadocs in AJDE model: ").append(this.generateJavaDocsInModel ? ENABLED : DISABLED); //$NON-NLS-1$		
		buf.append("\n\t- generate Emacs symbol files: ").append(this.generateEmacsSymFiles ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- suppress @AspectJ processing: ").append(this.noAtAspectJProcessing ? ENABLED : DISABLED); //$NON-NLS-1$

		buf.append("\n\t- invalid absolute type name (XLint): ").append(getSeverityString(InvalidAbsoluteTypeName)); //$NON-NLS-1$
		buf.append("\n\t- invalid wildcard type name (XLint): ").append(getSeverityString(InvalidWildCardTypeName)); //$NON-NLS-1$
		buf.append("\n\t- unresolvable member (XLint): ").append(getSeverityString(UnresolvableMember)); //$NON-NLS-1$
		buf.append("\n\t- type not exposed to weaver (XLint): ").append(getSeverityString(TypeNotExposedToWeaver)); //$NON-NLS-1$
		buf.append("\n\t- shadow not in structure (XLint): ").append(getSeverityString(ShadowNotInStructure)); //$NON-NLS-1$
		buf.append("\n\t- unmatched super type in call (XLint): ").append(getSeverityString(UnmatchedSuperTypeInCall)); //$NON-NLS-1$
		buf.append("\n\t- cannot implement lazy thisJoinPoint (XLint): ").append(getSeverityString(CannotImplementLazyTJP)); //$NON-NLS-1$
		buf.append("\n\t- need serialVersionUID field (XLint): ").append(getSeverityString(NeedSerialVersionUIDField)); //$NON-NLS-1$
		buf.append("\n\t- incompatible serial version (XLint): ").append(getSeverityString(IncompatibleSerialVersion)); //$NON-NLS-1$
		buf.append("\n\t- swallowed exception in catch block (XLint): ").append(getSeverityString(CompilerOptions.SwallowedExceptionInCatchBlock)); //$NON-NLS-1$

		return buf.toString();
	}
}
