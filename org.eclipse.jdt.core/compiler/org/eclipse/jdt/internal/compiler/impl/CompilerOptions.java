/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;


public class CompilerOptions implements ProblemReasons, ProblemSeverities {
	
	/**
	 * Option IDs
	 */
	public static final String OPTION_LocalVariableAttribute = "org.eclipse.jdt.core.compiler.debug.localVariable"; //$NON-NLS-1$
	public static final String OPTION_LineNumberAttribute = "org.eclipse.jdt.core.compiler.debug.lineNumber"; //$NON-NLS-1$
	public static final String OPTION_SourceFileAttribute = "org.eclipse.jdt.core.compiler.debug.sourceFile"; //$NON-NLS-1$
	public static final String OPTION_PreserveUnusedLocal = "org.eclipse.jdt.core.compiler.codegen.unusedLocal"; //$NON-NLS-1$
	public static final String OPTION_ReportUnreachableCode = "org.eclipse.jdt.core.compiler.problem.unreachableCode"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidImport = "org.eclipse.jdt.core.compiler.problem.invalidImport"; //$NON-NLS-1$
	public static final String OPTION_ReportMethodWithConstructorName = "org.eclipse.jdt.core.compiler.problem.methodWithConstructorName"; //$NON-NLS-1$
	public static final String OPTION_ReportOverridingPackageDefaultMethod = "org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$
	public static final String OPTION_ReportDeprecation = "org.eclipse.jdt.core.compiler.problem.deprecation"; //$NON-NLS-1$
	public static final String OPTION_ReportHiddenCatchBlock = "org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedLocal = "org.eclipse.jdt.core.compiler.problem.unusedLocal"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedParameter = "org.eclipse.jdt.core.compiler.problem.unusedParameter"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedImport = "org.eclipse.jdt.core.compiler.problem.unusedImport"; //$NON-NLS-1$
	public static final String OPTION_ReportSyntheticAccessEmulation = "org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation"; //$NON-NLS-1$
	public static final String OPTION_ReportNonExternalizedStringLiteral = "org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral"; //$NON-NLS-1$
	public static final String OPTION_Source = "org.eclipse.jdt.core.compiler.source"; //$NON-NLS-1$
	public static final String OPTION_TargetPlatform = "org.eclipse.jdt.core.compiler.codegen.targetPlatform"; //$NON-NLS-1$
	public static final String OPTION_ReportAssertIdentifier = "org.eclipse.jdt.core.compiler.problem.assertIdentifier"; //$NON-NLS-1$
	public static final String OPTION_Compliance = "org.eclipse.jdt.core.compiler.compliance"; //$NON-NLS-1$
	public static final String OPTION_Encoding = "org.eclipse.jdt.core.encoding"; //$NON-NLS-1$
	public static final String OPTION_MaxProblemPerUnit = "org.eclipse.jdt.core.compiler.maxProblemPerUnit"; //$NON-NLS-1$

	/* should surface ??? */
	public static final String OPTION_PrivateConstructorAccess = "org.eclipse.jdt.core.compiler.codegen.constructorAccessEmulation"; //$NON-NLS-1$

	/**
	 * Possible values for configurable options
	 */
	public static final String GENERATE = "generate";//$NON-NLS-1$
	public static final String DO_NOT_GENERATE = "do not generate"; //$NON-NLS-1$
	public static final String PRESERVE = "preserve"; //$NON-NLS-1$
	public static final String OPTIMIZE_OUT = "optimize out"; //$NON-NLS-1$
	public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$
	public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$
	public static final String VERSION_1_3 = "1.3"; //$NON-NLS-1$
	public static final String VERSION_1_4 = "1.4"; //$NON-NLS-1$
	public static final String ERROR = "error"; //$NON-NLS-1$
	public static final String WARNING = "warning"; //$NON-NLS-1$
	public static final String IGNORE = "ignore"; //$NON-NLS-1$
	
	/**
	 * Bit mask for configurable problems (error/warning threshold)
	 */
	public static final int UnreachableCode = 0x100;
	public static final int ImportProblem = 0x400;
	public static final int MethodWithConstructorName = 0x1000;
	public static final int OverriddenPackageDefaultMethod = 0x2000;
	public static final int UsingDeprecatedAPI = 0x4000;
	public static final int MaskedCatchBlock = 0x8000;
	public static final int UnusedLocalVariable = 0x10000;
	public static final int UnusedArgument = 0x20000;
	public static final int NoImplicitStringConversion = 0x40000;
	public static final int AccessEmulation = 0x80000;
	public static final int NonExternalizedString = 0x100000;
	public static final int AssertUsedAsAnIdentifier = 0x200000;
	public static final int UnusedImport = 0x400000;
		
	// Default severity level for handlers
	public int errorThreshold = UnreachableCode | ImportProblem;
	public int warningThreshold = 
		MethodWithConstructorName | OverriddenPackageDefaultMethod
		| UsingDeprecatedAPI | MaskedCatchBlock 
		| AssertUsedAsAnIdentifier | NoImplicitStringConversion;

	// Debug attributes
	public static final int Source = 1; // SourceFileAttribute
	public static final int Lines = 2; // LineNumberAttribute
	public static final int Vars = 4; // LocalVariableTableAttribute

	// By default only lines and source attributes are generated.
	public int produceDebugAttributes = Lines | Source;


	// JDK 1.1, 1.2, 1.3 or 1.4
	public static final int JDK1_1 = 0;
	public static final int JDK1_2 = 1;
	public static final int JDK1_3 = 2;
	public static final int JDK1_4 = 3;
	
	public int targetJDK = JDK1_1; // default generates for JVM1.1
	public int complianceLevel = JDK1_3; // by default be compliant with 1.3

	// toggle private access emulation for 1.2 (constr. accessor has extra arg on constructor) or 1.3 (make private constructor default access when access needed)
	public boolean isPrivateConstructorAccessChangingVisibility = false; // by default, follows 1.2

	// 1.4 feature
	public boolean assertMode = false; //1.3 behavior by default
	
	// source encoding format
	public String defaultEncoding = null; // will use the platform default encoding
	
	// print what unit is being processed
	public boolean verbose = Compiler.DEBUG;

	// indicates if reference info is desired
	public boolean produceReferenceInfo = true;

	// indicates if unused/optimizable local variables need to be preserved (debugging purpose)
	public boolean preserveAllLocalVariables = false;

	// indicates whether literal expressions are inlined at parse-time or not
	public boolean parseLiteralExpressionsAsConstants = true;

	// exception raised for unresolved compile errors
	public String runtimeExceptionNameForCompileError = "java.lang.Error"; //$NON-NLS-1$

	// max problems per compilation unit
	public int maxProblemsPerUnit = 100; // no more than 100 problems per default
	
	/** 
	 * Initializing the compiler options with defaults
	 */
	public CompilerOptions(){
	}

	/** 
	 * Initializing the compiler options with external settings
	 */
	public CompilerOptions(Map settings){

		if (settings == null) return;
		
		// filter options which are related to the compiler component
		Object[] entries = settings.entrySet().toArray();
		for (int i = 0, max = entries.length; i < max; i++){
			Map.Entry entry = (Map.Entry)entries[i];
			if (!(entry.getKey() instanceof String)) continue;
			if (!(entry.getValue() instanceof String)) continue;
			String optionID = (String) entry.getKey();
			String optionValue = (String) entry.getValue();
			
			// Local variable attribute
			if(optionID.equals(OPTION_LocalVariableAttribute)){
				if (optionValue.equals(GENERATE)) {
					this.produceDebugAttributes |= Vars;
				} else if (optionValue.equals(DO_NOT_GENERATE)){
					this.produceDebugAttributes &= ~Vars;
				}
				continue;
			}  
			// Line number attribute	
			if(optionID.equals(OPTION_LineNumberAttribute)) {
				if (optionValue.equals(GENERATE)) {
					this.produceDebugAttributes |= Lines;
				} else if (optionValue.equals(DO_NOT_GENERATE)) {
					this.produceDebugAttributes &= ~Lines;
				}
				continue;
			} 
			// Source file attribute	
			if(optionID.equals(OPTION_SourceFileAttribute)) {
				if (optionValue.equals(GENERATE)) {
					this.produceDebugAttributes |= Source;
				} else if (optionValue.equals(DO_NOT_GENERATE)) {
					this.produceDebugAttributes &= ~Source;
				}
				continue;
			} 
			// Preserve unused local	
			if(optionID.equals(OPTION_PreserveUnusedLocal)){
				if (optionValue.equals(PRESERVE)) {
					this.preserveAllLocalVariables = true;
				} else if (optionValue.equals(OPTIMIZE_OUT)) {
					this.preserveAllLocalVariables = false;
				}
				continue;
			} 
			// Report unreachable code				
			if(optionID.equals(OPTION_ReportUnreachableCode)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= UnreachableCode;
					this.warningThreshold &= ~UnreachableCode;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~UnreachableCode;
					this.warningThreshold |= UnreachableCode;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~UnreachableCode;
					this.warningThreshold &= ~UnreachableCode;
				}
				continue;
			} 
			// Report invalid import	
			if(optionID.equals(OPTION_ReportInvalidImport)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= ImportProblem;
					this.warningThreshold &= ~ImportProblem;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~ImportProblem;
					this.warningThreshold |= ImportProblem;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~ImportProblem;
					this.warningThreshold &= ~ImportProblem;
				}
				continue;
			} 
			// Define the target JDK tag for .classfiles
			if(optionID.equals(OPTION_TargetPlatform)){
				if (optionValue.equals(VERSION_1_1)) {
					this.targetJDK = JDK1_1;
				} else if (optionValue.equals(VERSION_1_2)) {
					this.targetJDK = JDK1_2;
				} else if (optionValue.equals(VERSION_1_3)) {
					this.targetJDK = JDK1_3;
				} else if (optionValue.equals(VERSION_1_4)) {
					this.targetJDK = JDK1_4;
				}
				continue;
			} 
			// Define the JDK compliance level
			if(optionID.equals(OPTION_Compliance)){
				if (optionValue.equals(VERSION_1_1)) {
					this.complianceLevel = JDK1_1;
				} else if (optionValue.equals(VERSION_1_2)) {
					this.complianceLevel = JDK1_2;
				} else if (optionValue.equals(VERSION_1_3)) {
					this.complianceLevel = JDK1_3;
				} else if (optionValue.equals(VERSION_1_4)) {
					this.complianceLevel = JDK1_4;
				}
				continue;
			} 
			// Private constructor access emulation (extra arg vs. visibility change)
			if(optionID.equals(OPTION_PrivateConstructorAccess)){
				if (optionValue.equals(VERSION_1_1)) {
					this.isPrivateConstructorAccessChangingVisibility = false;
				} else if (optionValue.equals(VERSION_1_2)) {
					this.isPrivateConstructorAccessChangingVisibility = false;
				} else if (optionValue.equals(VERSION_1_3)) {
					this.isPrivateConstructorAccessChangingVisibility = true;
				} else if (optionValue.equals(VERSION_1_4)) {
					this.isPrivateConstructorAccessChangingVisibility = true;
				}
				continue;
			} 
			// Report method with constructor name
			if(optionID.equals(OPTION_ReportMethodWithConstructorName)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= MethodWithConstructorName;
					this.warningThreshold &= ~MethodWithConstructorName;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~MethodWithConstructorName;
					this.warningThreshold |= MethodWithConstructorName;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~MethodWithConstructorName;
					this.warningThreshold &= ~MethodWithConstructorName;
				}
				continue;
			} 
			// Report overriding package default method
			if(optionID.equals(OPTION_ReportOverridingPackageDefaultMethod)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= OverriddenPackageDefaultMethod;
					this.warningThreshold &= ~OverriddenPackageDefaultMethod;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~OverriddenPackageDefaultMethod;
					this.warningThreshold |= OverriddenPackageDefaultMethod;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~OverriddenPackageDefaultMethod;
					this.warningThreshold &= ~OverriddenPackageDefaultMethod;
				}
				continue;
			} 
			// Report deprecation
			if(optionID.equals(OPTION_ReportDeprecation)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= UsingDeprecatedAPI;
					this.warningThreshold &= ~UsingDeprecatedAPI;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~UsingDeprecatedAPI;
					this.warningThreshold |= UsingDeprecatedAPI;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~UsingDeprecatedAPI;
					this.warningThreshold &= ~UsingDeprecatedAPI;
				}
				continue;
			} 
			// Report hidden catch block
			if(optionID.equals(OPTION_ReportHiddenCatchBlock)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= MaskedCatchBlock;
					this.warningThreshold &= ~MaskedCatchBlock;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~MaskedCatchBlock;
					this.warningThreshold |= MaskedCatchBlock;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~MaskedCatchBlock;
					this.warningThreshold &= ~MaskedCatchBlock;
				}
				continue;
			} 
			// Report unused local variable
			if(optionID.equals(OPTION_ReportUnusedLocal)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= UnusedLocalVariable;
					this.warningThreshold &= ~UnusedLocalVariable;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~UnusedLocalVariable;
					this.warningThreshold |= UnusedLocalVariable;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~UnusedLocalVariable;
					this.warningThreshold &= ~UnusedLocalVariable;
				}
				continue;
			} 
			// Report unused parameter
			if(optionID.equals(OPTION_ReportUnusedParameter)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= UnusedArgument;
					this.warningThreshold &= ~UnusedArgument;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~UnusedArgument;
					this.warningThreshold |= UnusedArgument;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~UnusedArgument;
					this.warningThreshold &= ~UnusedArgument;
				}
				continue;
			} 
			// Report unused parameter
			if(optionID.equals(OPTION_ReportUnusedImport)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= UnusedImport;
					this.warningThreshold &= ~UnusedImport;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~UnusedImport;
					this.warningThreshold |= UnusedImport;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~UnusedImport;
					this.warningThreshold &= ~UnusedImport;
				}
				continue;
			} 
			// Report synthetic access emulation
			if(optionID.equals(OPTION_ReportSyntheticAccessEmulation)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= AccessEmulation;
					this.warningThreshold &= ~AccessEmulation;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~AccessEmulation;
					this.warningThreshold |= AccessEmulation;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~AccessEmulation;
					this.warningThreshold &= ~AccessEmulation;
				}
				continue;
			}
			// Report non-externalized string literals
			if(optionID.equals(OPTION_ReportNonExternalizedStringLiteral)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= NonExternalizedString;
					this.warningThreshold &= ~NonExternalizedString;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~NonExternalizedString;
					this.warningThreshold |= NonExternalizedString;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~NonExternalizedString;
					this.warningThreshold &= ~NonExternalizedString;
				}
				continue;
			}
			// Report usage of 'assert' as an identifier
			if(optionID.equals(OPTION_ReportAssertIdentifier)){
				if (optionValue.equals(ERROR)) {
					this.errorThreshold |= AssertUsedAsAnIdentifier;
					this.warningThreshold &= ~AssertUsedAsAnIdentifier;
				} else if (optionValue.equals(WARNING)) {
					this.errorThreshold &= ~AssertUsedAsAnIdentifier;
					this.warningThreshold |= AssertUsedAsAnIdentifier;
				} else if (optionValue.equals(IGNORE)) {
					this.errorThreshold &= ~AssertUsedAsAnIdentifier;
					this.warningThreshold &= ~AssertUsedAsAnIdentifier;
				}
				continue;
			}
			// Set the source compatibility mode (assertions)
			if(optionID.equals(OPTION_Source)){
				if (optionValue.equals(VERSION_1_3)) {
					this.assertMode = false;
				} else if (optionValue.equals(VERSION_1_4)) {
					this.assertMode = true;
				}
				continue;
			}
			// Set the default encoding format
			if(optionID.equals(OPTION_Encoding)){
				if (optionValue.length() == 0){
					this.defaultEncoding = null;
				} else {
					try { // ignore unsupported encoding
						new InputStreamReader(new ByteArrayInputStream(new byte[0]), optionValue);
						this.defaultEncoding = optionValue;
					} catch(UnsupportedEncodingException e){
					}
				}
				continue;
			}
			// Set the threshold for problems per unit
			if(optionID.equals(OPTION_MaxProblemPerUnit)){
				try {
					int val = Integer.parseInt(optionValue);
					if (val >= 0) this.maxProblemsPerUnit = val;
				} catch(NumberFormatException e){
				}				
				continue;
			}
		}
	}
	
	public int getTargetJDK() {
		return this.targetJDK;
	}

	public int getNonExternalizedStringLiteralSeverity() {
		if((warningThreshold & NonExternalizedString) != 0)
			return Warning;
		if((errorThreshold & NonExternalizedString) != 0)
			return Error;
		return Ignore;
	}

	public void produceReferenceInfo(boolean flag) {
		this.produceReferenceInfo = flag;
	}

	public void setVerboseMode(boolean flag) {
		this.verbose = flag;
	}

	public String toString() {
	

		StringBuffer buf = new StringBuffer("CompilerOptions:"); //$NON-NLS-1$
		if ((produceDebugAttributes & Vars) != 0){
			buf.append("\n-local variables debug attributes: ON"); //$NON-NLS-1$
		} else {
			buf.append("\n-local variables debug attributes: OFF"); //$NON-NLS-1$
		}
		if ((produceDebugAttributes & Lines) != 0){
			buf.append("\n-line number debug attributes: ON"); //$NON-NLS-1$
		} else {
			buf.append("\n-line number debug attributes: OFF"); //$NON-NLS-1$
		}
		if ((produceDebugAttributes & Source) != 0){
			buf.append("\n-source debug attributes: ON"); //$NON-NLS-1$
		} else {
			buf.append("\n-source debug attributes: OFF"); //$NON-NLS-1$
		}
		if (preserveAllLocalVariables){
			buf.append("\n-preserve all local variables: ON"); //$NON-NLS-1$
		} else {
			buf.append("\n-preserve all local variables: OFF"); //$NON-NLS-1$
		}
		if ((errorThreshold & UnreachableCode) != 0){
			buf.append("\n-unreachable code: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & UnreachableCode) != 0){
				buf.append("\n-unreachable code: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-unreachable code: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & ImportProblem) != 0){
			buf.append("\n-import problem: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & ImportProblem) != 0){
				buf.append("\n-import problem: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-import problem: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & MethodWithConstructorName) != 0){
			buf.append("\n-method with constructor name: ERROR");		 //$NON-NLS-1$
		} else {
			if ((warningThreshold & MethodWithConstructorName) != 0){
				buf.append("\n-method with constructor name: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-method with constructor name: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & OverriddenPackageDefaultMethod) != 0){
			buf.append("\n-overridden package default method: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & OverriddenPackageDefaultMethod) != 0){
				buf.append("\n-overridden package default method: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-overridden package default method: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & UsingDeprecatedAPI) != 0){
			buf.append("\n-deprecation: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & UsingDeprecatedAPI) != 0){
				buf.append("\n-deprecation: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-deprecation: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & MaskedCatchBlock) != 0){
			buf.append("\n-masked catch block: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & MaskedCatchBlock) != 0){
				buf.append("\n-masked catch block: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-masked catch block: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & UnusedLocalVariable) != 0){
			buf.append("\n-unused local variable: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & UnusedLocalVariable) != 0){
				buf.append("\n-unused local variable: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-unused local variable: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & UnusedArgument) != 0){
			buf.append("\n-unused parameter: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & UnusedArgument) != 0){
				buf.append("\n-unused parameter: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-unused parameter: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & UnusedImport) != 0){
			buf.append("\n-unused import: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & UnusedImport) != 0){
				buf.append("\n-unused import: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-unused import: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & AccessEmulation) != 0){
			buf.append("\n-synthetic access emulation: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & AccessEmulation) != 0){
				buf.append("\n-synthetic access emulation: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-synthetic access emulation: IGNORE"); //$NON-NLS-1$
			}
		}
		if ((errorThreshold & NonExternalizedString) != 0){
			buf.append("\n-non externalized string: ERROR"); //$NON-NLS-1$
		} else {
			if ((warningThreshold & NonExternalizedString) != 0){
				buf.append("\n-non externalized string: WARNING"); //$NON-NLS-1$
			} else {
				buf.append("\n-non externalized string: IGNORE"); //$NON-NLS-1$
			}
		}
		switch(targetJDK){
			case JDK1_1 :
				buf.append("\n-target JDK: 1.1"); //$NON-NLS-1$
				break;
			case JDK1_2 :
				buf.append("\n-target JDK: 1.2"); //$NON-NLS-1$
				break;
			case JDK1_3 :
				buf.append("\n-target JDK: 1.3"); //$NON-NLS-1$
				break;
			case JDK1_4 :
				buf.append("\n-target JDK: 1.4"); //$NON-NLS-1$
				break;
		}
		switch(complianceLevel){
			case JDK1_1 :
				buf.append("\n-compliance JDK: 1.1"); //$NON-NLS-1$
				break;
			case JDK1_2 :
				buf.append("\n-compliance JDK: 1.2"); //$NON-NLS-1$
				break;
			case JDK1_3 :
				buf.append("\n-compliance JDK: 1.3"); //$NON-NLS-1$
				break;
			case JDK1_4 :
				buf.append("\n-compliance JDK: 1.4"); //$NON-NLS-1$
				break;
		}
		if (isPrivateConstructorAccessChangingVisibility){
			buf.append("\n-private constructor access emulation: extra argument"); //$NON-NLS-1$
		} else {
			buf.append("\n-private constructor access emulation: make default access"); //$NON-NLS-1$
		}
		buf.append("\n-verbose : " + (verbose ? "ON" : "OFF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-produce reference info : " + (produceReferenceInfo ? "ON" : "OFF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-parse literal expressions as constants : " + (parseLiteralExpressionsAsConstants ? "ON" : "OFF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-runtime exception name for compile error : " + runtimeExceptionNameForCompileError); //$NON-NLS-1$
		buf.append("\n-encoding : " + (defaultEncoding == null ? "<default>" : defaultEncoding)); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
