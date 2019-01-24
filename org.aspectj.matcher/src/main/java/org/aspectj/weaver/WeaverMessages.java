/*******************************************************************************
 * Copyright (c) 2004-2019 Contributors
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.weaver;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Andy Clement
 * @author IBM
 */
public class WeaverMessages {

	private static ResourceBundle bundle = ResourceBundle.getBundle("org.aspectj.weaver.weaver-messages");

	public static final String ARGS_IN_DECLARE = "argsInDeclare";
	public static final String CFLOW_IN_DECLARE = "cflowInDeclare";
	public static final String IF_IN_DECLARE = "ifInDeclare";
	public static final String THIS_OR_TARGET_IN_DECLARE = "thisOrTargetInDeclare";
	public static final String ABSTRACT_POINTCUT = "abstractPointcut";
	public static final String POINCUT_NOT_CONCRETE = "abstractPointcutNotMadeConcrete";
	public static final String POINTCUT_NOT_VISIBLE = "pointcutNotVisible";
	public static final String CONFLICTING_INHERITED_POINTCUTS = "conflictingInheritedPointcuts";
	public static final String CIRCULAR_POINTCUT = "circularPointcutDeclaration";
	public static final String CANT_FIND_POINTCUT = "cantFindPointcut";
	public static final String EXACT_TYPE_PATTERN_REQD = "exactTypePatternRequired";
	public static final String CANT_BIND_TYPE = "cantBindType";
	public static final String WILDCARD_NOT_ALLOWED = "wildcardTypePatternNotAllowed";
	public static final String FIELDS_CANT_HAVE_VOID_TYPE = "fieldCantBeVoid";
	public static final String NO_NEWARRAY_JOINPOINTS_BY_DEFAULT = "noNewArrayJoinpointsByDefault";
	public static final String UNSUPPORTED_POINTCUT_PRIMITIVE = "unsupportedPointcutPrimitive";
	public static final String MISSING_TYPE_PREVENTS_MATCH = "missingTypePreventsMatch";

	public static final String DECP_OBJECT = "decpObject";
	public static final String CANT_EXTEND_SELF = "cantExtendSelf";
	public static final String INTERFACE_CANT_EXTEND_CLASS = "interfaceExtendClass";
	public static final String DECP_HIERARCHY_ERROR = "decpHierarchy";

	public static final String MULTIPLE_MATCHES_IN_PRECEDENCE = "multipleMatchesInPrecedence";
	public static final String TWO_STARS_IN_PRECEDENCE = "circularityInPrecedenceStar";
	public static final String CLASSES_IN_PRECEDENCE = "nonAspectTypesInPrecedence";
	public static final String TWO_PATTERN_MATCHES_IN_PRECEDENCE = "circularityInPrecedenceTwo";

	public static final String NOT_THROWABLE = "notThrowable";

	public static final String ITD_CONS_ON_ASPECT = "itdConsOnAspect";
	public static final String ITD_RETURN_TYPE_MISMATCH = "returnTypeMismatch";
	public static final String ITD_PARAM_TYPE_MISMATCH = "paramTypeMismatch";
	public static final String ITD_VISIBILITY_REDUCTION = "visibilityReduction";
	public static final String ITD_DOESNT_THROW = "doesntThrow";
	public static final String ITD_OVERRIDDEN_STATIC = "overriddenStatic";
	public static final String ITD_OVERIDDING_STATIC = "overridingStatic";
	public static final String ITD_CONFLICT = "itdConflict";
	public static final String ITD_MEMBER_CONFLICT = "itdMemberConflict";
	public static final String ITD_NON_EXPOSED_IMPLEMENTOR = "itdNonExposedImplementor";
	public static final String ITD_ABSTRACT_MUST_BE_PUBLIC_ON_INTERFACE = "itdAbstractMustBePublicOnInterface";
	public static final String CANT_OVERRIDE_FINAL_MEMBER = "cantOverrideFinalMember";

	public static final String NON_VOID_RETURN = "nonVoidReturn";
	public static final String INCOMPATIBLE_RETURN_TYPE = "incompatibleReturnType";
	public static final String CANT_THROW_CHECKED = "cantThrowChecked";
	public static final String CIRCULAR_DEPENDENCY = "circularDependency";

	public static final String MISSING_PER_CLAUSE = "missingPerClause";
	public static final String WRONG_PER_CLAUSE = "wrongPerClause";

	public static final String ALREADY_WOVEN = "alreadyWoven";
	public static final String REWEAVABLE_MODE = "reweavableMode";
	public static final String PROCESSING_REWEAVABLE = "processingReweavable";
	public static final String MISSING_REWEAVABLE_TYPE = "missingReweavableType";
	public static final String VERIFIED_REWEAVABLE_TYPE = "verifiedReweavableType";
	public static final String ASPECT_NEEDED = "aspectNeeded";
	public static final String REWEAVABLE_ASPECT_NOT_REGISTERED = "reweavableAspectNotRegistered";

	public static final String CANT_FIND_TYPE = "cantFindType";
	public static final String CANT_FIND_CORE_TYPE = "cantFindCoreType";
	public static final String CANT_FIND_TYPE_WITHINPCD = "cantFindTypeWithinpcd";
	public static final String CANT_FIND_TYPE_DURING_AROUND_WEAVE = "cftDuringAroundWeave";
	public static final String CANT_FIND_TYPE_DURING_AROUND_WEAVE_PREINIT = "cftDuringAroundWeavePreinit";
	public static final String CANT_FIND_TYPE_EXCEPTION_TYPE = "cftExceptionType";
	public static final String CANT_FIND_TYPE_ARG_TYPE = "cftArgType";
	public static final String CANT_FIND_PARENT_TYPE = "cantFindParentType";
	public static final String CANT_FIND_PARENT_TYPE_NO_SUB = "cantFindParentTypeNoSub";
	public static final String CANT_FIND_TYPE_FIELDS = "cantFindTypeFields";
	public static final String CANT_FIND_TYPE_SUPERCLASS = "cantFindTypeSuperclass";
	public static final String CANT_FIND_TYPE_INTERFACES = "cantFindTypeInterfaces";
	public static final String CANT_FIND_TYPE_METHODS = "cantFindTypeMethods";
	public static final String CANT_FIND_TYPE_POINTCUTS = "cantFindTypePointcuts";
	public static final String CANT_FIND_TYPE_MODIFIERS = "cantFindTypeModifiers";
	public static final String CANT_FIND_TYPE_ANNOTATION = "cantFindTypeAnnotation";
	public static final String CANT_FIND_TYPE_ASSIGNABLE = "cantFindTypeAssignable";
	public static final String CANT_FIND_TYPE_COERCEABLE = "cantFindTypeCoerceable";
	public static final String CANT_FIND_TYPE_JOINPOINT = "cantFindTypeJoinPoint";
	public static final String CANT_FIND_TYPE_INTERFACE_METHODS = "cantFindTypeInterfaceMethods";

	public static final String DECP_BINARY_LIMITATION = "decpBinaryLimitation";
	public static final String OVERWRITE_JSR45 = "overwriteJSR45";
	public static final String IF_IN_PERCLAUSE = "ifInPerClause";
	public static final String IF_LEXICALLY_IN_CFLOW = "ifLexicallyInCflow";
	public static final String ONLY_BEFORE_ON_HANDLER = "onlyBeforeOnHandler";
	public static final String NO_AROUND_ON_SYNCHRONIZATION = "noAroundOnSynchronization";
	public static final String AROUND_ON_PREINIT = "aroundOnPreInit";
	public static final String AROUND_ON_INIT = "aroundOnInit";
	public static final String AROUND_ON_INTERFACE_STATICINIT = "aroundOnInterfaceStaticInit";

	public static final String PROBLEM_GENERATING_METHOD = "problemGeneratingMethod";
	public static final String CLASS_TOO_BIG = "classTooBig";

	public static final String ZIPFILE_ENTRY_MISSING = "zipfileEntryMissing";
	public static final String ZIPFILE_ENTRY_INVALID = "zipfileEntryInvalid";
	public static final String DIRECTORY_ENTRY_MISSING = "directoryEntryMissing";
	public static final String OUTJAR_IN_INPUT_PATH = "outjarInInputPath";

	public static final String XLINT_LOAD_ERROR = "problemLoadingXLint";
	public static final String XLINTDEFAULT_LOAD_ERROR = "unableToLoadXLintDefault";
	public static final String XLINTDEFAULT_LOAD_PROBLEM = "errorLoadingXLintDefault";
	public static final String XLINT_KEY_ERROR = "invalidXLintKey";
	public static final String XLINT_VALUE_ERROR = "invalidXLintMessageKind";

	public static final String UNBOUND_FORMAL = "unboundFormalInPC";
	public static final String AMBIGUOUS_BINDING = "ambiguousBindingInPC";
	public static final String AMBIGUOUS_BINDING_IN_OR = "ambiguousBindingInOrPC";
	public static final String NEGATION_DOESNT_ALLOW_BINDING = "negationDoesntAllowBinding";

	// Java5 messages
	public static final String ITDC_ON_ENUM_NOT_ALLOWED = "itdcOnEnumNotAllowed";
	public static final String ITDM_ON_ENUM_NOT_ALLOWED = "itdmOnEnumNotAllowed";
	public static final String ITDF_ON_ENUM_NOT_ALLOWED = "itdfOnEnumNotAllowed";
	public static final String CANT_DECP_ON_ENUM_TO_IMPL_INTERFACE = "cantDecpOnEnumToImplInterface";
	public static final String CANT_DECP_ON_ENUM_TO_EXTEND_CLASS = "cantDecpOnEnumToExtendClass";
	public static final String CANT_DECP_TO_MAKE_ENUM_SUPERTYPE = "cantDecpToMakeEnumSupertype";
	public static final String ITDC_ON_ANNOTATION_NOT_ALLOWED = "itdcOnAnnotationNotAllowed";
	public static final String ITDM_ON_ANNOTATION_NOT_ALLOWED = "itdmOnAnnotationNotAllowed";
	public static final String ITDF_ON_ANNOTATION_NOT_ALLOWED = "itdfOnAnnotationNotAllowed";
	public static final String CANT_DECP_ON_ANNOTATION_TO_IMPL_INTERFACE = "cantDecpOnAnnotationToImplInterface";
	public static final String CANT_DECP_ON_ANNOTATION_TO_EXTEND_CLASS = "cantDecpOnAnnotationToExtendClass";
	public static final String CANT_DECP_TO_MAKE_ANNOTATION_SUPERTYPE = "cantDecpToMakeAnnotationSupertype";
	public static final String REFERENCE_TO_NON_ANNOTATION_TYPE = "referenceToNonAnnotationType";
	public static final String BINDING_NON_RUNTIME_RETENTION_ANNOTATION = "bindingNonRuntimeRetentionAnnotation";
	
	public static final String UNSUPPORTED_ANNOTATION_VALUE_TYPE = "unsupportedAnnotationValueType";

	public static final String INCORRECT_TARGET_FOR_DECLARE_ANNOTATION = "incorrectTargetForDeclareAnnotation";
	public static final String NO_MATCH_BECAUSE_SOURCE_RETENTION = "noMatchBecauseSourceRetention";

	// Annotation Value messages
	public static final String INVALID_ANNOTATION_VALUE = "invalidAnnotationValue";
	public static final String UNKNOWN_ANNOTATION_VALUE = "unknownAnnotationValue";

	// < Java5 messages
	public static final String ATANNOTATION_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "atannotationNeedsJava5";
	public static final String ATWITHIN_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "atwithinNeedsJava5";
	public static final String ATWITHINCODE_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "atwithincodeNeedsJava5";
	public static final String ATTHIS_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "atthisNeedsJava5";
	public static final String ATTARGET_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "attargetNeedsJava5";
	public static final String ATARGS_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "atargsNeedsJava5";
	public static final String DECLARE_ATTYPE_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "declareAtTypeNeedsJava5";
	public static final String DECLARE_ATMETHOD_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "declareAtMethodNeedsJava5";
	public static final String DECLARE_ATFIELD_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "declareAtFieldNeedsJava5";
	public static final String DECLARE_ATCONS_ONLY_SUPPORTED_AT_JAVA5_LEVEL = "declareAtConsNeedsJava5";
	public static final String ANNOTATIONS_NEED_JAVA5 = "annotationsRequireJava5";

	// Generics
	public static final String CANT_DECP_MULTIPLE_PARAMETERIZATIONS = "cantDecpMultipleParameterizations";
	public static final String HANDLER_PCD_DOESNT_SUPPORT_PARAMETERS = "noParameterizedTypePatternInHandler";
	public static final String INCORRECT_NUMBER_OF_TYPE_ARGUMENTS = "incorrectNumberOfTypeArguments";
	public static final String VIOLATES_TYPE_VARIABLE_BOUNDS = "violatesTypeVariableBounds";
	public static final String NO_STATIC_INIT_JPS_FOR_PARAMETERIZED_TYPES = "noStaticInitJPsForParameterizedTypes";
	public static final String NOT_A_GENERIC_TYPE = "notAGenericType";
	public static final String WITHIN_PCD_DOESNT_SUPPORT_PARAMETERS = "noParameterizedTypePatternInWithin";
	public static final String THIS_AND_TARGET_DONT_SUPPORT_PARAMETERS = "noParameterizedTypesInThisAndTarget";
	public static final String GET_AND_SET_DONT_SUPPORT_DEC_TYPE_PARAMETERS = "noParameterizedTypesInGetAndSet";
	public static final String NO_INIT_JPS_FOR_PARAMETERIZED_TYPES = "noInitJPsForParameterizedTypes";
	public static final String NO_GENERIC_THROWABLES = "noGenericThrowables";
	public static final String WITHINCODE_DOESNT_SUPPORT_PARAMETERIZED_DECLARING_TYPES = "noParameterizedDeclaringTypesWithinCode";
	public static final String EXECUTION_DOESNT_SUPPORT_PARAMETERIZED_DECLARING_TYPES = "noParameterizedDeclaringTypesInExecution";
	public static final String CALL_DOESNT_SUPPORT_PARAMETERIZED_DECLARING_TYPES = "noParameterizedDeclaringTypesInCall";
	public static final String CANT_REFERENCE_POINTCUT_IN_RAW_TYPE = "noRawTypePointcutReferences";

	public static final String HAS_MEMBER_NOT_ENABLED = "hasMemberNotEnabled";

	public static final String MUST_KEEP_OVERWEAVING_ONCE_START = "mustKeepOverweavingOnceStart";
	
	// @AspectJ
	public static final String RETURNING_FORMAL_NOT_DECLARED_IN_ADVICE = "returningFormalNotDeclaredInAdvice";
	public static final String THROWN_FORMAL_NOT_DECLARED_IN_ADVICE = "thrownFormalNotDeclaredInAdvice";

	public static String format(String key) {
		return bundle.getString(key);
	}

	public static String format(String key, Object insert) {
		return MessageFormat.format(bundle.getString(key), new Object[] { insert });
	}

	public static String format(String key, Object insert1, Object insert2) {
		return MessageFormat.format(bundle.getString(key), new Object[] { insert1, insert2 });
	}

	public static String format(String key, Object insert1, Object insert2, Object insert3) {
		return MessageFormat.format(bundle.getString(key), new Object[] { insert1, insert2, insert3 });
	}

	public static String format(String key, Object insert1, Object insert2, Object insert3, Object insert4) {
		return MessageFormat.format(bundle.getString(key), new Object[] { insert1, insert2, insert3, insert4 });
	}

}
