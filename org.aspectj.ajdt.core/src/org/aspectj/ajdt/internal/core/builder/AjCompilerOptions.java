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


package org.aspectj.ajdt.internal.core.builder;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;


/**
 * Compiler options used by Eclipse integration  (AJDT) 
 */
public class AjCompilerOptions extends CompilerOptions {

	// AspectJ Lint options
	public static final String OPTION_ReportInvalidAbsoluteTypeName = "org.aspectj.ajdt.core.compiler.lint.InvalidAbsoluteTypeName"; 
	public static final String OPTION_ReportInvalidWildcardTypeName = "org.aspectj.ajdt.core.compiler.lint.WildcardTypeName"; 
	public static final String OPTION_ReportUnresolvableMember      = "org.aspectj.ajdt.core.compiler.lint.UnresolvableMember"; 
	public static final String OPTION_ReportTypeNotExposedToWeaver  = "org.aspectj.ajdt.core.compiler.lint.TypeNotExposedToWeaver"; 
	public static final String OPTION_ReportShadowNotInStructure    = "org.aspectj.ajdt.core.compiler.lint.ShadowNotInStructure"; 
	public static final String OPTION_ReportUnmatchedSuperTypeInCall    = "org.aspectj.ajdt.core.compiler.list.UnmatchedSuperTypeInCall";
	public static final String OPTION_ReportCannotImplementLazyTJP  = "org.aspectj.ajdt.core.compiler.lint.CannotImplementLazyTJP"; 
	public static final String OPTION_ReportNeedSerialVersionUIDField   = "org.aspectj.ajdt.core.compiler.lint.NeedSerialVersionUIDField"; 
	public static final String OPTION_ReportIncompatibleSerialVersion   = "org.aspectj.ajdt.core.compiler.lint.BrokeSerialVersionCompatibility";
	
	// General AspectJ Compiler options (excludes paths etc, these are handled separately)
	public static final String OPTION_NoWeave                 = "org.aspectj.ajdt.core.compiler.weaver.NoWeave";
	public static final String OPTION_XSerializableAspects    = "org.aspectj.ajdt.core.compiler.weaver.XSerializableAspects";
	public static final String OPTION_XLazyThisJoinPoint      = "org.aspectj.ajdt.core.compiler.weaver.XLazyThisJoinPoint";
	public static final String OPTION_XNoInline               = "org.aspectj.ajdt.core.compiler.weaver.XNoInline";
	public static final String OPTION_XReweavable             = "org.aspectj.ajdt.core.compiler.weaver.XReweavable";
	public static final String OPTION_XReweavableCompress     = "org.aspectj.ajdt.core.compiler.weaver.XReweavableCompress";

	// these next three not exposed by IDEs
    public static final String OPTION_GenerateModel           = "org.aspectj.ajdt.core.compiler.model.GenerateModel";
    public static final String OPTION_GenerateJavaDocsInModel = "org.aspectj.ajdt.core.compiler.model.GenerateJavaDocsInModel";
    public static final String OPTION_Emacssym                = "org.aspectj.ajdt.core.compiler.model.Emacssym";
	
	// constants for irritant levels
	public static final long InvalidAbsoluteTypeName    = 0x20000000000L;
	public static final long InvalidWildCardTypeName    = 0x40000000000L;
	public static final long UnresolvableMember         = 0x80000000000L;
	public static final long TypeNotExposedToWeaver     = 0x100000000000L;
	public static final long ShadowNotInStructure       = 0x200000000000L;
	public static final long UnmatchedSuperTypeInCall   = 0x400000000000L;
	public static final long CannotImplementLazyTJP     = 0x800000000000L;
	public static final long NeedSerialVersionUIDField  = 0x1000000000000L;
	public static final long IncompatibleSerialVersion  = 0x2000000000000L;

	public boolean noWeave = false;
	public boolean xSerializableAspects = false;
	public boolean xLazyThisJoinPoint = false;
	public boolean xNoInline = false;
	public boolean xReweavable = false;
	public boolean xReweavableCompress = false;
	public boolean showWeavingInformation = false;
	
	// these next three not exposed by IDEs
	public boolean generateModel = false;
	public boolean generateJavaDocsInModel = false;
	public boolean generateEmacsSymFiles = false;

	
	/** 
	 * Initializing the compiler options with defaults
	 */
	public AjCompilerOptions(){
		super();
		setAspectJWarningDefaults();			
	}

	/** 
	 * Initializing the compiler options with external settings
	 * @param settings
	 */
	public AjCompilerOptions(Map settings){
		setAspectJWarningDefaults();
		if (settings == null) return;
		set(settings);	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.impl.CompilerOptions#getMap()
	 */
	public Map getMap() {
		Map map = super.getMap();
		// now add AspectJ additional options
		map.put(OPTION_ReportInvalidAbsoluteTypeName,getSeverityString(InvalidAbsoluteTypeName));
		map.put(OPTION_ReportInvalidWildcardTypeName,getSeverityString(InvalidWildCardTypeName));
		map.put(OPTION_ReportUnresolvableMember,getSeverityString(UnresolvableMember));
		map.put(OPTION_ReportTypeNotExposedToWeaver,getSeverityString(TypeNotExposedToWeaver));
		map.put(OPTION_ReportShadowNotInStructure,getSeverityString(ShadowNotInStructure));
		map.put(OPTION_ReportUnmatchedSuperTypeInCall,getSeverityString(UnmatchedSuperTypeInCall));
		map.put(OPTION_ReportCannotImplementLazyTJP,getSeverityString(CannotImplementLazyTJP));
		map.put(OPTION_ReportNeedSerialVersionUIDField,getSeverityString(NeedSerialVersionUIDField));
		map.put(OPTION_ReportIncompatibleSerialVersion,getSeverityString(IncompatibleSerialVersion));
		
		map.put(OPTION_NoWeave, this.noWeave ? ENABLED : DISABLED);
		map.put(OPTION_XSerializableAspects,this.xSerializableAspects ? ENABLED : DISABLED);
		map.put(OPTION_XLazyThisJoinPoint,this.xLazyThisJoinPoint ? ENABLED : DISABLED);
		map.put(OPTION_XNoInline,this.xNoInline ? ENABLED : DISABLED);
		map.put(OPTION_XReweavable,this.xReweavable ? ENABLED : DISABLED);
		map.put(OPTION_XReweavableCompress,this.xReweavableCompress ? ENABLED : DISABLED);

		map.put(OPTION_GenerateModel,this.generateModel ? ENABLED : DISABLED);
		map.put(OPTION_GenerateJavaDocsInModel,this.generateJavaDocsInModel ? ENABLED : DISABLED);
		map.put(OPTION_Emacssym,this.generateEmacsSymFiles ? ENABLED : DISABLED);
		
		return map;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.impl.CompilerOptions#set(java.util.Map)
	 */
	public void set(Map optionsMap) {
		super.set(optionsMap);
		Object optionValue;
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidAbsoluteTypeName)) != null) updateSeverity(InvalidAbsoluteTypeName, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidWildcardTypeName)) != null) updateSeverity(InvalidWildCardTypeName, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnresolvableMember)) != null) updateSeverity(UnresolvableMember, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportTypeNotExposedToWeaver)) != null) updateSeverity(TypeNotExposedToWeaver, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportShadowNotInStructure)) != null) updateSeverity(ShadowNotInStructure, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnmatchedSuperTypeInCall)) != null) updateSeverity(UnmatchedSuperTypeInCall, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportCannotImplementLazyTJP)) != null) updateSeverity(CannotImplementLazyTJP, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportNeedSerialVersionUIDField)) != null) updateSeverity(NeedSerialVersionUIDField, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportIncompatibleSerialVersion)) != null) updateSeverity(IncompatibleSerialVersion, optionValue);
		
		if ((optionValue = optionsMap.get(OPTION_NoWeave)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.noWeave = true;
			} else if (DISABLED.equals(optionValue)) {
				this.noWeave = false;
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
		if ((optionValue = optionsMap.get(OPTION_XReweavable)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xReweavable = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xReweavable = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_XReweavableCompress)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.xReweavableCompress = true;
			} else if (DISABLED.equals(optionValue)) {
				this.xReweavableCompress = false;
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
		
	}
	
	/**
	 * Add these warnings to the default set...
	 */
	private void setAspectJWarningDefaults() {
		super.warningThreshold = 
			super.warningThreshold |
			InvalidAbsoluteTypeName |
			UnresolvableMember |
			TypeNotExposedToWeaver |
			UnmatchedSuperTypeInCall |
			CannotImplementLazyTJP;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer( super.toString() );
		// now add AspectJ additional options
		buf.append("\n\tAspectJ Specific Options:");
		buf.append("\n\t- no weave: ").append(this.noWeave ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- no inline (X option): ").append(this.xNoInline ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- generate serializable aspects (X option): ").append(this.xSerializableAspects ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- lazy thisJoinPoint (X option): ").append(this.xLazyThisJoinPoint ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- generate reweavable class files (X option): ").append(this.xReweavable ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- compress reweavable class files (X option): ").append(this.xReweavableCompress ? ENABLED : DISABLED); //$NON-NLS-1$		

		buf.append("\n\t- generate AJDE model: ").append(this.generateModel ? ENABLED : DISABLED); //$NON-NLS-1$		
		buf.append("\n\t- generate Javadocs in AJDE model: ").append(this.generateJavaDocsInModel ? ENABLED : DISABLED); //$NON-NLS-1$		
		buf.append("\n\t- generate Emacs symbol files: ").append(this.generateEmacsSymFiles ? ENABLED : DISABLED); //$NON-NLS-1$		
		
		buf.append("\n\t- invalid absolute type name (XLint): ").append(getSeverityString(InvalidAbsoluteTypeName)); //$NON-NLS-1$
		buf.append("\n\t- invalid wildcard type name (XLint): ").append(getSeverityString(InvalidWildCardTypeName)); //$NON-NLS-1$
		buf.append("\n\t- unresolvable member (XLint): ").append(getSeverityString(UnresolvableMember)); //$NON-NLS-1$
		buf.append("\n\t- type not exposed to weaver (XLint): ").append(getSeverityString(TypeNotExposedToWeaver)); //$NON-NLS-1$
		buf.append("\n\t- shadow not in structure (XLint): ").append(getSeverityString(ShadowNotInStructure)); //$NON-NLS-1$
		buf.append("\n\t- unmatched super type in call (XLint): ").append(getSeverityString(UnmatchedSuperTypeInCall)); //$NON-NLS-1$
		buf.append("\n\t- cannot implement lazy thisJoinPoint (XLint): ").append(getSeverityString(CannotImplementLazyTJP)); //$NON-NLS-1$
		buf.append("\n\t- need serialVersionUID field (XLint): ").append(getSeverityString(NeedSerialVersionUIDField)); //$NON-NLS-1$
		buf.append("\n\t- incompatible serial version (XLint): ").append(getSeverityString(IncompatibleSerialVersion)); //$NON-NLS-1$
		
		return buf.toString();
	}
}
  
