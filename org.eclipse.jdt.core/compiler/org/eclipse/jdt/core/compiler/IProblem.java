/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/

package org.eclipse.jdt.core.compiler;
 
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;

/**
 * Description of a Java problem, as detected by the compiler or some of the underlying
 * technology reusing the compiler. 
 * A problem provides access to:
 * <ul>
 * <li> its location (originating source file name, source position, line number), </li>
 * <li> its message description and a predicate to check its severity (warning or error). </li>
 * <li> its ID : an number identifying the very nature of this problem. All possible IDs are listed
 * as constants on this interface. </li>
 * </ul>
 * 
 * Note: the compiler produces IProblems internally, which are turned into markers by the JavaBuilder
 * so as to persist problem descriptions. This explains why there is no API allowing to reach IProblem detected
 * when compiling. However, the Java problem markers carry equivalent information to IProblem, in particular
 * their ID (attribute "id") is set to one of the IDs defined on this interface.
 * 
 * @since 2.0
 */
public interface IProblem { 
	
	/**
	 * Answer back the original arguments recorded into the problem.
	 * @return the original arguments recorded into the problem
	 */
	String[] getArguments();

	/**
	 * Returns the problem id
	 * 
	 * @return the problem id
	 */
	int getID();

	/**
	 * Answer a localized, human-readable message string which describes the problem.
	 * 
	 * @return a localized, human-readable message string which describes the problem
	 */
	String getMessage();

	/**
	 * Answer the file name in which the problem was found.
	 * 
	 * @return the file name in which the problem was found
	 */
	char[] getOriginatingFileName();
	
	/**
	 * Answer the end position of the problem (inclusive), or -1 if unknown.
	 * 
	 * @return the end position of the problem (inclusive), or -1 if unknown
	 */
	int getSourceEnd();

	/**
	 * Answer the line number in source where the problem begins.
	 * 
	 * @return the line number in source where the problem begins
	 */
	int getSourceLineNumber();

	/**
	 * Answer the start position of the problem (inclusive), or -1 if unknown.
	 * 
	 * @return the start position of the problem (inclusive), or -1 if unknown
	 */
	int getSourceStart();

	/**
	 * Checks the severity to see if the Error bit is set.
	 * 
	 * @return true if the Error bit is set for the severity, false otherwise
	 */
	boolean isError();

	/**
	 * Checks the severity to see if the Error bit is not set.
	 * 
	 * @return true if the Error bit is not set for the severity, false otherwise
	 */
	boolean isWarning();

	/**
	 * Set the end position of the problem (inclusive), or -1 if unknown.
	 * Used for shifting problem positions.
	 * 
	 * @param sourceEnd the given end position
	 */
	void setSourceEnd(int sourceEnd);

	/**
	 * Set the line number in source where the problem begins.
	 * 
	 * @param lineNumber the given line number
	 */
	void setSourceLineNumber(int lineNumber);

	/**
	 * Set the start position of the problem (inclusive), or -1 if unknown.
	 * Used for shifting problem positions.
	 * 
	 * @param the given start position
	 */
	void setSourceStart(int sourceStart);
	
	/**
	 * Problem Categories
	 * The high bits of a problem ID contains information about the category of a problem. 
	 * e.g. (problemID & TypeRelated) != 0, indicates that this problem is type related.
	 * 
	 * A problem category can help to implement custom problem filters. Indeed, when numerous problems
	 * are listed, focusing on import related problems first might be relevant.
	 * 
	 * When a problem is tagged as Internal, it means that no change other than a local source code change
	 * can  fix the corresponding problem.
	 */
	int TypeRelated = 0x01000000;
	int FieldRelated = 0x02000000;
	int MethodRelated = 0x04000000;
	int ConstructorRelated = 0x08000000;
	int ImportRelated = 0x10000000;
	int Internal = 0x20000000;
	int Syntax =  0x40000000;
	
	/**
	 * Mask to use in order to filter out the category portion of the problem ID.
	 */
	int IgnoreCategoriesMask = 0xFFFFFF;

	/**
	 * Below are listed all available problem IDs. Note that this list could be augmented in the future, 
	 * as new features are added to the Java core implementation.
	 */

	/**
	 * ID reserved for referencing an internal error inside the JavaCore implementation which
	 * may be surfaced as a problem associated with the compilation unit which caused it to occur.
	 */
	int Unclassified = 0;

	/**
	 * Generic type related problems
	 */
	int ObjectHasNoSuperclass = TypeRelated + 1;
	int UndefinedType = TypeRelated + 2;
	int NotVisibleType = TypeRelated + 3;
	int AmbiguousType = TypeRelated + 4;
	int UsingDeprecatedType = TypeRelated + 5;
	int InternalTypeNameProvided = TypeRelated + 6;

	int IncompatibleTypesInEqualityOperator = TypeRelated + 15;
	int IncompatibleTypesInConditionalOperator = TypeRelated + 16;
	int TypeMismatch = TypeRelated + 17;

	/**
	 * Inner types related problems
	 */
	int MissingEnclosingInstanceForConstructorCall = TypeRelated + 20;
	int MissingEnclosingInstance = TypeRelated + 21;
	int IncorrectEnclosingInstanceReference = TypeRelated + 22;
	int IllegalEnclosingInstanceSpecification = TypeRelated + 23; 
	int CannotDefineStaticInitializerInLocalType = Internal + 24;
	int OuterLocalMustBeFinal = Internal + 25;
	int CannotDefineInterfaceInLocalType = Internal + 26;
	int IllegalPrimitiveOrArrayTypeForEnclosingInstance = TypeRelated + 27;
	int AnonymousClassCannotExtendFinalClass = TypeRelated + 29;

	// variables
	int UndefinedName = 50;
	int UninitializedLocalVariable = Internal + 51;
	int VariableTypeCannotBeVoid = Internal + 52;
	int VariableTypeCannotBeVoidArray = Internal + 53;
	int CannotAllocateVoidArray = Internal + 54;
	// local variables
	int RedefinedLocal = Internal + 55;
	int RedefinedArgument = Internal + 56;
	int DuplicateFinalLocalInitialization = Internal + 57;
	// final local variables
	int FinalOuterLocalAssignment = Internal + 60;
	int LocalVariableIsNeverUsed = Internal + 61;
	int ArgumentIsNeverUsed = Internal + 62;
	int BytecodeExceeds64KLimit = Internal + 63;
	int BytecodeExceeds64KLimitForClinit = Internal + 64;
	int TooManyArgumentSlots = Internal + 65;
	int TooManyLocalVariableSlots = Internal + 66;

	// fields
	int UndefinedField = FieldRelated + 70;
	int NotVisibleField = FieldRelated + 71;
	int AmbiguousField = FieldRelated + 72;
	int UsingDeprecatedField = FieldRelated + 73;
	int NonStaticFieldFromStaticInvocation = FieldRelated + 74;
	int ReferenceToForwardField = FieldRelated + Internal + 75;

	// blank final fields
	int FinalFieldAssignment = FieldRelated + 80;
	int UninitializedBlankFinalField = FieldRelated + 81;
	int DuplicateBlankFinalFieldInitialization = FieldRelated + 82;

	// methods
	int UndefinedMethod = MethodRelated + 100;
	int NotVisibleMethod = MethodRelated + 101;
	int AmbiguousMethod = MethodRelated + 102;
	int UsingDeprecatedMethod = MethodRelated + 103;
	int DirectInvocationOfAbstractMethod = MethodRelated + 104;
	int VoidMethodReturnsValue = MethodRelated + 105;
	int MethodReturnsVoid = MethodRelated + 106;
	int MethodRequiresBody = Internal + MethodRelated + 107;
	int ShouldReturnValue = Internal + MethodRelated + 108;
	int MethodButWithConstructorName = MethodRelated + 110;
	int MissingReturnType = TypeRelated + 111;
	int BodyForNativeMethod = Internal + MethodRelated + 112;
	int BodyForAbstractMethod = Internal + MethodRelated + 113;
	int NoMessageSendOnBaseType = MethodRelated + 114;
	int ParameterMismatch = MethodRelated + 115;
	int NoMessageSendOnArrayType = MethodRelated + 116;
    
	// constructors
	int UndefinedConstructor = ConstructorRelated + 130;
	int NotVisibleConstructor = ConstructorRelated + 131;
	int AmbiguousConstructor = ConstructorRelated + 132;
	int UsingDeprecatedConstructor = ConstructorRelated + 133;
	// explicit constructor calls
	int InstanceFieldDuringConstructorInvocation = ConstructorRelated + 135;
	int InstanceMethodDuringConstructorInvocation = ConstructorRelated + 136;
	int RecursiveConstructorInvocation = ConstructorRelated + 137;
	int ThisSuperDuringConstructorInvocation = ConstructorRelated + 138;
	// implicit constructor calls
	int UndefinedConstructorInDefaultConstructor = ConstructorRelated + 140;
	int NotVisibleConstructorInDefaultConstructor = ConstructorRelated + 141;
	int AmbiguousConstructorInDefaultConstructor = ConstructorRelated + 142;
	int UndefinedConstructorInImplicitConstructorCall = ConstructorRelated + 143;
	int NotVisibleConstructorInImplicitConstructorCall = ConstructorRelated + 144;
	int AmbiguousConstructorInImplicitConstructorCall = ConstructorRelated + 145;
	int UnhandledExceptionInDefaultConstructor = TypeRelated + 146;
	int UnhandledExceptionInImplicitConstructorCall = TypeRelated + 147;
				
	// expressions
	int ArrayReferenceRequired = Internal + 150;
	int NoImplicitStringConversionForCharArrayExpression = Internal + 151;
	// constant expressions
	int StringConstantIsExceedingUtf8Limit = Internal + 152;
	int NonConstantExpression = 153;
	int NumericValueOutOfRange = Internal + 154;
	// cast expressions
	int IllegalCast = TypeRelated + 156;
	// allocations
	int InvalidClassInstantiation = TypeRelated + 157;
	int CannotDefineDimensionExpressionsWithInit = Internal + 158;
	int MustDefineEitherDimensionExpressionsOrInitializer = Internal + 159;
	// operators
	int InvalidOperator = Internal + 160;
	// statements
	int CodeCannotBeReached = Internal + 161;
	int CannotReturnInInitializer = Internal + 162;
	int InitializerMustCompleteNormally = Internal + 163;
	
	// assert
	int InvalidVoidExpression = Internal + 164;
	// try
	int MaskedCatch = TypeRelated + 165;
	int DuplicateDefaultCase = 166;
	int UnreachableCatch = TypeRelated + MethodRelated + 167;
	int UnhandledException = TypeRelated + 168;
	// switch       
	int IncorrectSwitchType = TypeRelated + 169;
	int DuplicateCase = FieldRelated + 170;
	// labelled
	int DuplicateLabel = Internal + 171;
	int InvalidBreak = Internal + 172;
	int InvalidContinue = Internal + 173;
	int UndefinedLabel = Internal + 174;
	//synchronized
	int InvalidTypeToSynchronized = Internal + 175;
	int InvalidNullToSynchronized = Internal + 176;
	// throw
	int CannotThrowNull = Internal + 177;

	// inner emulation
	int NeedToEmulateFieldReadAccess = FieldRelated + 190;
	int NeedToEmulateFieldWriteAccess = FieldRelated + 191;
	int NeedToEmulateMethodAccess = MethodRelated + 192;
	int NeedToEmulateConstructorAccess = MethodRelated + 193;

	//inherited name hides enclosing name (sort of ambiguous)
	int InheritedMethodHidesEnclosingName = MethodRelated + 195;
	int InheritedFieldHidesEnclosingName = FieldRelated + 196;
	int InheritedTypeHidesEnclosingName = TypeRelated + 197;

	// miscellaneous
	int ThisInStaticContext = Internal + 200;
	int StaticMethodRequested = Internal + MethodRelated + 201;
	int IllegalDimension = Internal + 202;
	int InvalidTypeExpression = Internal + 203;
	int ParsingError = Syntax + Internal + 204;
	int ParsingErrorNoSuggestion = Syntax + Internal + 205;
	int InvalidUnaryExpression = Syntax + Internal + 206;

	// syntax errors
	int InterfaceCannotHaveConstructors = Syntax + Internal + 207;
	int ArrayConstantsOnlyInArrayInitializers = Syntax + Internal + 208;
	int ParsingErrorOnKeyword = Syntax + Internal + 209;	
	int ParsingErrorOnKeywordNoSuggestion = Syntax + Internal + 210;

	int UnmatchedBracket = Syntax + Internal + 220;
	int NoFieldOnBaseType = FieldRelated + 221;
	int InvalidExpressionAsStatement = Syntax + Internal + 222;
    
	// scanner errors
	int EndOfSource = Syntax + Internal + 250;
	int InvalidHexa = Syntax + Internal + 251;
	int InvalidOctal = Syntax + Internal + 252;
	int InvalidCharacterConstant = Syntax + Internal + 253;
	int InvalidEscape = Syntax + Internal + 254;
	int InvalidInput = Syntax + Internal + 255;
	int InvalidUnicodeEscape = Syntax + Internal + 256;
	int InvalidFloat = Syntax + Internal + 257;
	int NullSourceString = Syntax + Internal + 258;
	int UnterminatedString = Syntax + Internal + 259;
	int UnterminatedComment = Syntax + Internal + 260;

	// type related problems
	int InterfaceCannotHaveInitializers = TypeRelated + 300;
	int DuplicateModifierForType = TypeRelated + 301;
	int IllegalModifierForClass = TypeRelated + 302;
	int IllegalModifierForInterface = TypeRelated + 303;
	int IllegalModifierForMemberClass = TypeRelated + 304;
	int IllegalModifierForMemberInterface = TypeRelated + 305;
	int IllegalModifierForLocalClass = TypeRelated + 306;

	int IllegalModifierCombinationFinalAbstractForClass = TypeRelated + 308;
	int IllegalVisibilityModifierForInterfaceMemberType = TypeRelated + 309;
	int IllegalVisibilityModifierCombinationForMemberType = TypeRelated + 310;
	int IllegalStaticModifierForMemberType = TypeRelated + 311;
	int SuperclassMustBeAClass = TypeRelated + 312;
	int ClassExtendFinalClass = TypeRelated + 313;
	int DuplicateSuperInterface = TypeRelated + 314;
	int SuperInterfaceMustBeAnInterface = TypeRelated + 315;
	int HierarchyCircularitySelfReference = TypeRelated + 316;
	int HierarchyCircularity = TypeRelated + 317;
	int HidingEnclosingType = TypeRelated + 318;
	int DuplicateNestedType = TypeRelated + 319;
	int CannotThrowType = TypeRelated + 320;
	int PackageCollidesWithType = TypeRelated + 321;
	int TypeCollidesWithPackage = TypeRelated + 322;
	int DuplicateTypes = TypeRelated + 323;
	int IsClassPathCorrect = TypeRelated + 324;
	int PublicClassMustMatchFileName = TypeRelated + 325;
	int MustSpecifyPackage = 326;
	int HierarchyHasProblems = TypeRelated + 327;
	int PackageIsNotExpectedPackage = 328;

	// int InvalidSuperclassBase = TypeRelated + 329; // reserved to 334 included
	int SuperclassNotFound =  TypeRelated + 329 + ProblemReasons.NotFound; // TypeRelated + 330
	int SuperclassNotVisible =  TypeRelated + 329 + ProblemReasons.NotVisible; // TypeRelated + 331
	int SuperclassAmbiguous =  TypeRelated + 329 + ProblemReasons.Ambiguous; // TypeRelated + 332
	int SuperclassInternalNameProvided =  TypeRelated + 329 + ProblemReasons.InternalNameProvided; // TypeRelated + 333
	int SuperclassInheritedNameHidesEnclosingName =  TypeRelated + 329 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 334

	// int InvalidInterfaceBase = TypeRelated + 334; // reserved to 339 included
	int InterfaceNotFound =  TypeRelated + 334 + ProblemReasons.NotFound; // TypeRelated + 335
	int InterfaceNotVisible =  TypeRelated + 334 + ProblemReasons.NotVisible; // TypeRelated + 336
	int InterfaceAmbiguous =  TypeRelated + 334 + ProblemReasons.Ambiguous; // TypeRelated + 337
	int InterfaceInternalNameProvided =  TypeRelated + 334 + ProblemReasons.InternalNameProvided; // TypeRelated + 338
	int InterfaceInheritedNameHidesEnclosingName =  TypeRelated + 334 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 339

	// field related problems
	int DuplicateField = FieldRelated + 340;
	int DuplicateModifierForField = FieldRelated + 341;
	int IllegalModifierForField = FieldRelated + 342;
	int IllegalModifierForInterfaceField = FieldRelated + 343;
	int IllegalVisibilityModifierCombinationForField = FieldRelated + 344;
	int IllegalModifierCombinationFinalVolatileForField = FieldRelated + 345;
	int UnexpectedStaticModifierForField = FieldRelated + 346;

	// int FieldTypeProblemBase = FieldRelated + 349; //reserved to 354
	int FieldTypeNotFound =  FieldRelated + 349 + ProblemReasons.NotFound; // FieldRelated + 350
	int FieldTypeNotVisible =  FieldRelated + 349 + ProblemReasons.NotVisible; // FieldRelated + 351
	int FieldTypeAmbiguous =  FieldRelated + 349 + ProblemReasons.Ambiguous; // FieldRelated + 352
	int FieldTypeInternalNameProvided =  FieldRelated + 349 + ProblemReasons.InternalNameProvided; // FieldRelated + 353
	int FieldTypeInheritedNameHidesEnclosingName =  FieldRelated + 349 + ProblemReasons.InheritedNameHidesEnclosingName; // FieldRelated + 354
	
	// method related problems
	int DuplicateMethod = MethodRelated + 355;
	int IllegalModifierForArgument = MethodRelated + 356;
	int DuplicateModifierForMethod = MethodRelated + 357;
	int IllegalModifierForMethod = MethodRelated + 358;
	int IllegalModifierForInterfaceMethod = MethodRelated + 359;
	int IllegalVisibilityModifierCombinationForMethod = MethodRelated + 360;
	int UnexpectedStaticModifierForMethod = MethodRelated + 361;
	int IllegalAbstractModifierCombinationForMethod = MethodRelated + 362;
	int AbstractMethodInAbstractClass = MethodRelated + 363;
	int ArgumentTypeCannotBeVoid = MethodRelated + 364;
	int ArgumentTypeCannotBeVoidArray = MethodRelated + 365;
	int ReturnTypeCannotBeVoidArray = MethodRelated + 366;
	int NativeMethodsCannotBeStrictfp = MethodRelated + 367;
	int DuplicateModifierForArgument = MethodRelated + 368;

	//	int ArgumentProblemBase = MethodRelated + 369; // reserved to 374 included.
	int ArgumentTypeNotFound =  MethodRelated + 369 + ProblemReasons.NotFound; // MethodRelated + 370
	int ArgumentTypeNotVisible =  MethodRelated + 369 + ProblemReasons.NotVisible; // MethodRelated + 371
	int ArgumentTypeAmbiguous =  MethodRelated + 369 + ProblemReasons.Ambiguous; // MethodRelated + 372
	int ArgumentTypeInternalNameProvided =  MethodRelated + 369 + ProblemReasons.InternalNameProvided; // MethodRelated + 373
	int ArgumentTypeInheritedNameHidesEnclosingName =  MethodRelated + 369 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 374

	//	int ExceptionTypeProblemBase = MethodRelated + 374; // reserved to 379 included.
	int ExceptionTypeNotFound =  MethodRelated + 374 + ProblemReasons.NotFound; // MethodRelated + 375
	int ExceptionTypeNotVisible =  MethodRelated + 374 + ProblemReasons.NotVisible; // MethodRelated + 376
	int ExceptionTypeAmbiguous =  MethodRelated + 374 + ProblemReasons.Ambiguous; // MethodRelated + 377
	int ExceptionTypeInternalNameProvided =  MethodRelated + 374 + ProblemReasons.InternalNameProvided; // MethodRelated + 378
	int ExceptionTypeInheritedNameHidesEnclosingName =  MethodRelated + 374 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 379

	//	int ReturnTypeProblemBase = MethodRelated + 379;
	int ReturnTypeNotFound =  MethodRelated + 379 + ProblemReasons.NotFound; // MethodRelated + 380
	int ReturnTypeNotVisible =  MethodRelated + 379 + ProblemReasons.NotVisible; // MethodRelated + 381
	int ReturnTypeAmbiguous =  MethodRelated + 379 + ProblemReasons.Ambiguous; // MethodRelated + 382
	int ReturnTypeInternalNameProvided =  MethodRelated + 379 + ProblemReasons.InternalNameProvided; // MethodRelated + 383
	int ReturnTypeInheritedNameHidesEnclosingName =  MethodRelated + 379 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 384

	// import related problems
	int ConflictingImport = ImportRelated + 385;
	int DuplicateImport = ImportRelated + 386;
	int CannotImportPackage = ImportRelated + 387;
	int UnusedImport = ImportRelated + 388;

	//	int ImportProblemBase = ImportRelated + 389;
	int ImportNotFound =  ImportRelated + 389 + ProblemReasons.NotFound; // ImportRelated + 390
	int ImportNotVisible =  ImportRelated + 389 + ProblemReasons.NotVisible; // ImportRelated + 391
	int ImportAmbiguous =  ImportRelated + 389 + ProblemReasons.Ambiguous; // ImportRelated + 392
	int ImportInternalNameProvided =  ImportRelated + 389 + ProblemReasons.InternalNameProvided; // ImportRelated + 393
	int ImportInheritedNameHidesEnclosingName =  ImportRelated + 389 + ProblemReasons.InheritedNameHidesEnclosingName; // ImportRelated + 394

	
	// local variable related problems
	int DuplicateModifierForVariable = MethodRelated + 395;
	int IllegalModifierForVariable = MethodRelated + 396;

	// method verifier problems
	int AbstractMethodMustBeImplemented = MethodRelated + 400;
	int FinalMethodCannotBeOverridden = MethodRelated + 401;
	int IncompatibleExceptionInThrowsClause = MethodRelated + 402;
	int IncompatibleExceptionInInheritedMethodThrowsClause = MethodRelated + 403;
	int IncompatibleReturnType = MethodRelated + 404;
	int InheritedMethodReducesVisibility = MethodRelated + 405;
	int CannotOverrideAStaticMethodWithAnInstanceMethod = MethodRelated + 406;
	int CannotHideAnInstanceMethodWithAStaticMethod = MethodRelated + 407;
	int StaticInheritedMethodConflicts = MethodRelated + 408;
	int MethodReducesVisibility = MethodRelated + 409;
	int OverridingNonVisibleMethod = MethodRelated + 410;
	int AbstractMethodCannotBeOverridden = MethodRelated + 411;
	int OverridingDeprecatedMethod = MethodRelated + 412;

	// code snippet support
	int CodeSnippetMissingClass = Internal + 420;
	int CodeSnippetMissingMethod = Internal + 421;
	int NonExternalizedStringLiteral = Internal + 261;
	int CannotUseSuperInCodeSnippet = Internal + 422;
	
	//constant pool
	int TooManyConstantsInConstantPool = Internal + 430;
	
	// 1.4 features
	// assertion warning
	int UseAssertAsAnIdentifier = Internal + 440;
}
