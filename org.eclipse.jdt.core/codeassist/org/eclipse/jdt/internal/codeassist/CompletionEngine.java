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
package org.eclipse.jdt.internal.codeassist;

import java.util.*;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.codeassist.impl.*;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.complete.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.TypeConverter;
import org.eclipse.jdt.internal.compiler.impl.*;

/**
 * This class is the entry point for source completions.
 * It contains two public APIs used to call CodeAssist on a given source with
 * a given environment, assisting position and storage (and possibly options).
 */
public final class CompletionEngine
	extends Engine
	implements ISearchRequestor, TypeConstants , ITerminalSymbols , RelevanceConstants {
	
	public static boolean DEBUG = false;

	private final static char[] ERROR_PATTERN = "*error*".toCharArray();  //$NON-NLS-1$
	private final static char[] EXCEPTION_PATTERN = "*exception*".toCharArray();  //$NON-NLS-1$
	private final static char[] SEMICOLON = new char[] { ';' };
	TypeBinding[] expectedTypes;
	
	boolean assistNodeIsClass;
	boolean assistNodeIsException;
	boolean assistNodeIsInterface;
	
	CompletionParser parser;
	ICompletionRequestor requestor;
	ProblemReporter problemReporter;
	char[] source;
	char[] token;
	boolean resolvingImports = false;
	boolean insideQualifiedReference = false;
	int startPosition, actualCompletionPosition, endPosition, offset;
	HashtableOfObject knownPkgs = new HashtableOfObject(10);
	HashtableOfObject knownTypes = new HashtableOfObject(10);
	Scanner nameScanner;

	/*
		static final char[][] mainDeclarations =
			new char[][] {
				"package".toCharArray(),
				"import".toCharArray(),
				"abstract".toCharArray(),
				"final".toCharArray(),
				"public".toCharArray(),
				"class".toCharArray(),
				"interface".toCharArray()};
	
		static final char[][] modifiers = // may want field, method, type & member type modifiers
			new char[][] {
				"abstract".toCharArray(),
				"final".toCharArray(),
				"native".toCharArray(),
				"public".toCharArray(),
				"protected".toCharArray(),
				"private".toCharArray(),
				"static".toCharArray(),
				"strictfp".toCharArray(),
				"synchronized".toCharArray(),
				"transient".toCharArray(),
				"volatile".toCharArray()};
	*/
	static final char[][] baseTypes = new char[][] { 
		"boolean".toCharArray(), //$NON-NLS-1$
		"byte".toCharArray(), //$NON-NLS-1$
		"char".toCharArray(), //$NON-NLS-1$
		"double".toCharArray(), //$NON-NLS-1$
		"float".toCharArray(), //$NON-NLS-1$
		"int".toCharArray(), //$NON-NLS-1$
		"long".toCharArray(), //$NON-NLS-1$
		"short".toCharArray(), //$NON-NLS-1$
		"void".toCharArray(), //$NON-NLS-1$
	};
		
	static final char[] classField = "class".toCharArray();  //$NON-NLS-1$
	static final char[] lengthField = "length".toCharArray();  //$NON-NLS-1$
	static final char[] THIS = "this".toCharArray();  //$NON-NLS-1$
	static final char[] THROWS = "throws".toCharArray();  //$NON-NLS-1$
	
	static InvocationSite FakeInvocationSite = new InvocationSite(){
		public boolean isSuperAccess(){ return false; }
		public boolean isTypeAccess(){ return false; }
		public void setActualReceiverType(ReferenceBinding receiverType) {}
		public void setDepth(int depth){}
		public void setFieldIndex(int depth){}
	};

	/**
	 * The CompletionEngine is responsible for computing source completions.
	 *
	 * It requires a searchable name environment, which supports some
	 * specific search APIs, and a requestor to feed back the results to a UI.
	 *
	 *  @param nameEnvironment org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment
	 *      used to resolve type/package references and search for types/packages
	 *      based on partial names.
	 *
	 *  @param requestor org.eclipse.jdt.internal.codeassist.ICompletionRequestor
	 *      since the engine might produce answers of various forms, the engine 
	 *      is associated with a requestor able to accept all possible completions.
	 *
	 *  @param settings java.util.Map
	 *		set of options used to configure the code assist engine.
	 */
	public CompletionEngine(
		ISearchableNameEnvironment nameEnvironment,
		ICompletionRequestor requestor,
		Map settings) {

		super(settings);
		this.requestor = requestor;
		this.nameEnvironment = nameEnvironment;

		problemReporter = new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.compilerOptions,
				new DefaultProblemFactory(Locale.getDefault()) {
					public void record(IProblem problem, CompilationResult unitResult, ReferenceContext referenceContext) {
						if (problem.isError() && (problem.getID() & IProblem.Syntax) != 0) {
							CompletionEngine.this.requestor.acceptError(problem);
						}
					}
				});
		this.parser =
			new CompletionParser(problemReporter, this.compilerOptions.assertMode);
		this.lookupEnvironment =
			new LookupEnvironment(this, this.compilerOptions, problemReporter, nameEnvironment);
		this.nameScanner =
			new Scanner(false, false, false, this.compilerOptions.assertMode);
	}

	/**
	 * One result of the search consists of a new class.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	public void acceptClass(char[] packageName, char[] className, int modifiers) {

		char[] fullyQualifiedName = CharOperation.concat(packageName, className, '.');
		char[] completionName = fullyQualifiedName;
		
		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);
		
		int relevance = R_DEFAULT;
		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, SEMICOLON);
			relevance += computeRelevanceForCaseMatching(token, fullyQualifiedName);
		} else {
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, className)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != NoCharChar)
							return; // ignore types from the default package from outside it
				} else {
					completionName = className;
				}
			}
			relevance += computeRelevanceForCaseMatching(token, className);
			relevance += computeRelevanceForExpectingType(packageName, className);
			relevance += computeRelevanceForClass();
			relevance += computeRelevanceForException(className);
		}

		requestor.acceptClass(
			packageName,
			className,
			completionName,
			modifiers,
			startPosition - offset,
			endPosition - offset,
			relevance);
	}
	
	/**
	 * One result of the search consists of a new interface.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.I".
	 *    The default package is represented by an empty array.
	 */
	public void acceptInterface(
		char[] packageName,
		char[] interfaceName,
		int modifiers) {

		char[] fullyQualifiedName = CharOperation.concat(packageName, interfaceName, '.');
		char[] completionName = fullyQualifiedName;

		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);

		int relevance = R_DEFAULT;
		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, new char[] { ';' });
			relevance += computeRelevanceForCaseMatching(token, fullyQualifiedName);
		} else {
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, interfaceName)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != NoCharChar)
							return; // ignore types from the default package from outside it
				} else {
					completionName = interfaceName;
				}
			}
			relevance += computeRelevanceForCaseMatching(token, interfaceName);
			relevance += computeRelevanceForExpectingType(packageName, interfaceName);
			relevance += computeRelevanceForInterface();
		}
		
		requestor.acceptInterface(
			packageName,
			interfaceName,
			completionName,
			modifiers,
			startPosition - offset,
			endPosition - offset,
			relevance);
	}

	/**
	 * One result of the search consists of a new package.
	 *
	 * NOTE - All package names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    The default package is represented by an empty array.
	 */
	public void acceptPackage(char[] packageName) {

		if (this.knownPkgs.containsKey(packageName)) return;

		this.knownPkgs.put(packageName, this);
		
		int relevance = R_DEFAULT;
		relevance += computeRelevanceForCaseMatching(token, packageName);

		requestor.acceptPackage(
			packageName,
			resolvingImports
				? CharOperation.concat(packageName, new char[] { '.', '*', ';' })
				: packageName,
			startPosition - offset,
			endPosition - offset,
			relevance);
	}

	/**
	 * One result of the search consists of a new type.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	public void acceptType(char[] packageName, char[] typeName) {

		char[] fullyQualifiedName = CharOperation.concat(packageName, typeName, '.');
		char[] completionName = fullyQualifiedName;
		
		if (this.knownTypes.containsKey(completionName)) return;

		this.knownTypes.put(completionName, this);

		int relevance = R_DEFAULT;
		if (resolvingImports) {
			completionName = CharOperation.concat(completionName, new char[] { ';' });
			relevance += computeRelevanceForCaseMatching(token, fullyQualifiedName);
		} else {
			if (!insideQualifiedReference) {
				if (mustQualifyType(packageName, typeName)) {
					if (packageName == null || packageName.length == 0)
						if (unitScope != null && unitScope.fPackage.compoundName != NoCharChar)
							return; // ignore types from the default package from outside it
				} else {
					completionName = typeName;
				}
			}
			relevance += computeRelevanceForCaseMatching(token, typeName);
			relevance += computeRelevanceForExpectingType(packageName, typeName);
		}
		
		requestor.acceptType(
			packageName,
			typeName,
			completionName,
			startPosition - offset,
			endPosition - offset,
			relevance);
	}

	private void complete(AstNode astNode, Binding qualifiedBinding, Scope scope) {

		setSourceRange(astNode.sourceStart, astNode.sourceEnd);
		
		if(parser.assistNodeParent != null) {
			computeExpectedTypes(parser.assistNodeParent, scope);
		}

		// defaults... some nodes will change these
		if (astNode instanceof CompletionOnFieldType) {

			CompletionOnFieldType field = (CompletionOnFieldType) astNode;
			CompletionOnSingleTypeReference type = (CompletionOnSingleTypeReference) field.type;
			token = type.token;
			setSourceRange(type.sourceStart, type.sourceEnd);
			//		findKeywords(token, modifiers, scope); // could be the start of a field, method or member type
			findTypesAndPackages(token, scope);
			
			if(!field.isLocalVariable && field.modifiers == CompilerModifiers.AccDefault) {
				findMethods(token,null,scope.enclosingSourceType(),scope,new ObjectVector(),false,false,true,null,null,false);
			}
		} else {
			if(astNode instanceof CompletionOnMethodReturnType) {
				
				CompletionOnMethodReturnType method = (CompletionOnMethodReturnType) astNode;
				SingleTypeReference type = (CompletionOnSingleTypeReference) method.returnType;
				token = type.token;
				setSourceRange(type.sourceStart, type.sourceEnd);
				findTypesAndPackages(token, scope);
				
				if(method.modifiers == CompilerModifiers.AccDefault) {
					findMethods(token,null,scope.enclosingSourceType(),scope,new ObjectVector(),false,false,true,null,null,false);
				}
			} else {
				
				if (astNode instanceof CompletionOnSingleNameReference) {
	
					token = ((CompletionOnSingleNameReference) astNode).token;
					findVariablesAndMethods(
						token,
						scope,
						(CompletionOnSingleNameReference) astNode,
						scope);
					// can be the start of a qualified type name
					findTypesAndPackages(token, scope);
	
				} else {
	
					if (astNode instanceof CompletionOnSingleTypeReference) {
	
						token = ((CompletionOnSingleTypeReference) astNode).token;
						
						assistNodeIsClass = astNode instanceof CompletionOnClassReference;
						assistNodeIsException = astNode instanceof CompletionOnExceptionReference;
						assistNodeIsInterface = astNode instanceof CompletionOnInterfaceReference;
	
						// can be the start of a qualified type name
						if (qualifiedBinding == null) {
							findTypesAndPackages(token, scope);
							} else {
								findMemberTypes(
								token,
								(ReferenceBinding) qualifiedBinding,
								scope,
								scope.enclosingSourceType());
						}
					} else {
						
						if (astNode instanceof CompletionOnQualifiedNameReference) {
	
							insideQualifiedReference = true;
							CompletionOnQualifiedNameReference ref =
								(CompletionOnQualifiedNameReference) astNode;
							token = ref.completionIdentifier;
							long completionPosition = ref.sourcePositions[ref.sourcePositions.length - 1];
	
							if (qualifiedBinding instanceof VariableBinding) {
	
								setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
								TypeBinding receiverType = ((VariableBinding) qualifiedBinding).type;
								if (receiverType != null) {
									findFieldsAndMethods(token, receiverType, scope, ref, scope,false);
								}
	
							} else {
	
								if (qualifiedBinding instanceof ReferenceBinding) {
	
									ReferenceBinding receiverType = (ReferenceBinding) qualifiedBinding;
									setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
	
									findMemberTypes(token, receiverType, scope, scope.enclosingSourceType());
	
									findClassField(token, (TypeBinding) qualifiedBinding, scope);
	
									findFields(
										token,
										receiverType,
										scope,
										new ObjectVector(),
										new ObjectVector(),
										true,
										ref,
										scope,
										false);
	
									findMethods(
										token,
										null,
										receiverType,
										scope,
										new ObjectVector(),
										true,
										false,
										false,
										ref,
										scope,
										false);
	
								} else {
	
									if (qualifiedBinding instanceof PackageBinding) {
	
										setSourceRange(astNode.sourceStart, (int) completionPosition);
										// replace to the end of the completion identifier
										findTypesAndSubpackages(token, (PackageBinding) qualifiedBinding);
									}
								}
							}
	
						} else {
	
								if (astNode instanceof CompletionOnQualifiedTypeReference) {
	
								insideQualifiedReference = true;
								
								assistNodeIsClass = astNode instanceof CompletionOnQualifiedClassReference;
								assistNodeIsException = astNode instanceof CompletionOnQualifiedExceptionReference;
								assistNodeIsInterface = astNode instanceof CompletionOnQualifiedInterfaceReference;
								
								CompletionOnQualifiedTypeReference ref =
									(CompletionOnQualifiedTypeReference) astNode;
								token = ref.completionIdentifier;
								long completionPosition = ref.sourcePositions[ref.tokens.length];
	
								// get the source positions of the completion identifier
								if (qualifiedBinding instanceof ReferenceBinding) {
	
									setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
									findMemberTypes(
										token,
										(ReferenceBinding) qualifiedBinding,
										scope,
										scope.enclosingSourceType());
	
								} else {
	
									if (qualifiedBinding instanceof PackageBinding) {
	
										setSourceRange(astNode.sourceStart, (int) completionPosition);
										// replace to the end of the completion identifier
										findTypesAndSubpackages(token, (PackageBinding) qualifiedBinding);
									}
								}
	
							} else {
	
								if (astNode instanceof CompletionOnMemberAccess) {
	
									CompletionOnMemberAccess access = (CompletionOnMemberAccess) astNode;
									long completionPosition = access.nameSourcePosition;
									setSourceRange((int) (completionPosition >>> 32), (int) completionPosition);
					
									token = access.token;
	
									findFieldsAndMethods(
										token,
										(TypeBinding) qualifiedBinding,
										scope,
										access,
										scope,
										false);
	
								} else {
	
									if (astNode instanceof CompletionOnMessageSend) {
	
										CompletionOnMessageSend messageSend = (CompletionOnMessageSend) astNode;
										TypeBinding[] argTypes =
											computeTypes(messageSend.arguments, (BlockScope) scope);
										token = messageSend.selector;
										if (qualifiedBinding == null) {
											
											findImplicitMessageSends(token, argTypes, scope, messageSend, scope);
										} else {
	
											findMethods(
												token,
												argTypes,
												(ReferenceBinding) qualifiedBinding,
												scope,
												new ObjectVector(),
												false,
												true,
												false,
												messageSend,
												scope,
												false);
										}
	
									} else {
	
										if (astNode instanceof CompletionOnExplicitConstructorCall) {
	
											CompletionOnExplicitConstructorCall constructorCall =
												(CompletionOnExplicitConstructorCall) astNode;
											TypeBinding[] argTypes =
												computeTypes(constructorCall.arguments, (BlockScope) scope);
											findConstructors(
												(ReferenceBinding) qualifiedBinding,
												argTypes,
												scope,
												constructorCall,
												false);
	
										} else {
	
											if (astNode instanceof CompletionOnQualifiedAllocationExpression) {
	
												CompletionOnQualifiedAllocationExpression allocExpression =
													(CompletionOnQualifiedAllocationExpression) astNode;
												TypeBinding[] argTypes =
													computeTypes(allocExpression.arguments, (BlockScope) scope);
												
												ReferenceBinding ref = (ReferenceBinding) qualifiedBinding;
												if(ref.isClass()) {
													if(!ref.isAbstract()) {
														findConstructors(
															ref,
															argTypes,
															scope,
															allocExpression,
															false);
													}
												}
												if(!ref.isFinal()){
													findAnonymousType(
														ref,
														argTypes,
														scope,
														allocExpression);
												}
	
											} else {
	
												if (astNode instanceof CompletionOnClassLiteralAccess) {
													CompletionOnClassLiteralAccess access = (CompletionOnClassLiteralAccess) astNode;
													setSourceRange(access.classStart, access.sourceEnd);
									
													token = access.completionIdentifier;
									
													findClassField(token, (TypeBinding) qualifiedBinding, scope);
												} else {
													if(astNode instanceof CompletionOnMethodName) {
														CompletionOnMethodName method = (CompletionOnMethodName) astNode;
															
														setSourceRange(method.sourceStart, method.selectorEnd);
															
														FieldBinding[] fields = scope.enclosingSourceType().fields();
														char[][] excludeNames = new char[fields.length][];
														for(int i = 0 ; i < fields.length ; i++){
															excludeNames[i] = fields[i].name;
														}
														
														token = method.selector;
														
														findVariableNames(token, method.returnType, excludeNames);
													} else {
														if (astNode instanceof CompletionOnFieldName) {
															CompletionOnFieldName field = (CompletionOnFieldName) astNode;
															
															FieldBinding[] fields = scope.enclosingSourceType().fields();
															char[][] excludeNames = new char[fields.length][];
															for(int i = 0 ; i < fields.length ; i++){
																excludeNames[i] = fields[i].name;
															}
															
															token = field.realName;
															
															findVariableNames(field.realName, field.type, excludeNames);
														} else {
															if (astNode instanceof CompletionOnLocalName ||
																astNode instanceof CompletionOnArgumentName){
																LocalDeclaration variable = (LocalDeclaration) astNode;
																
																LocalVariableBinding[] locals = ((BlockScope)scope).locals;
																char[][] excludeNames = new char[locals.length][];
																int localCount = 0;
																for(int i = 0 ; i < locals.length ; i++){
																	if(locals[i] != null) {
																		excludeNames[localCount++] = locals[i].name;
																	}
																}
																System.arraycopy(excludeNames, 0, excludeNames = new char[localCount][], 0, localCount);
																
																if(variable instanceof CompletionOnLocalName){
																	token = ((CompletionOnLocalName) variable).realName;
																} else {
																	token = ((CompletionOnArgumentName) variable).realName;
																}
																findVariableNames(token, variable.type, excludeNames);
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void complete(IType type, char[] snippet, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic){	
		TypeConverter converter = new TypeConverter();
		
		IType topLevelType = type;
		while(topLevelType.getDeclaringType() != null) {
			topLevelType = topLevelType.getDeclaringType();
		}
		
		CompilationResult compilationResult = new CompilationResult((topLevelType.getElementName() + ".java").toCharArray(), 1, 1, this.compilerOptions.maxProblemsPerUnit); //$NON-NLS-1$
	
		CompilationUnitDeclaration compilationUnit = new CompilationUnitDeclaration(problemReporter, compilationResult, 0);
	
		try {
			TypeDeclaration typeDeclaration = converter.buildTypeDeclaration(type, compilationUnit, compilationResult, problemReporter);
		
			if(typeDeclaration != null) {	
				// build AST from snippet
				Initializer fakeInitializer = parseSnippeInitializer(snippet, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic);
				
				// merge AST
				FieldDeclaration[] oldFields = typeDeclaration.fields;
				FieldDeclaration[] newFields = new FieldDeclaration[oldFields.length + 1];
				System.arraycopy(oldFields, 0, newFields, 0, oldFields.length);
				newFields[oldFields.length] = fakeInitializer;
				typeDeclaration.fields = newFields;
		
				if(DEBUG) {
					System.out.println("SNIPPET COMPLETION AST :"); //$NON-NLS-1$
					System.out.println(compilationUnit.toString());
				}
				
				if (compilationUnit.types != null) {
					try {
						lookupEnvironment.buildTypeBindings(compilationUnit);
				
						if ((unitScope = compilationUnit.scope) != null) {
							lookupEnvironment.completeTypeBindings(compilationUnit, true);
							compilationUnit.scope.faultInTypes();
							compilationUnit.resolve();
						}
					} catch (CompletionNodeFound e) {
						//					completionNodeFound = true;
						if (e.astNode != null) {
							// if null then we found a problem in the completion node
							complete(e.astNode, e.qualifiedBinding, e.scope);
						}
					}
				}
			}
		} catch(JavaModelException e) {
			// Do nothing
		}
	}
	
	private Initializer parseSnippeInitializer(char[] snippet, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic){
		StringBuffer prefix = new StringBuffer();
		prefix.append("public class FakeType {\n "); //$NON-NLS-1$
		if(isStatic) {
			prefix.append("static "); //$NON-NLS-1$
		}
		prefix.append("{\n"); //$NON-NLS-1$
		for (int i = 0; i < localVariableTypeNames.length; i++) {
			prefix.append(AstNode.modifiersString(localVariableModifiers[i]));
			prefix.append(' ');
			prefix.append(localVariableTypeNames[i]);
			prefix.append(' ');
			prefix.append(localVariableNames[i]);
			prefix.append(';');
		}
		
		char[] fakeSource = CharOperation.concat(prefix.toString().toCharArray(), snippet, "}}".toCharArray());//$NON-NLS-1$ 
		offset = prefix.length();
		
		String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
		BasicCompilationUnit fakeUnit = new BasicCompilationUnit(
			fakeSource, 
			null,
			"FakeType.java", //$NON-NLS-1$
			encoding); 
			
		actualCompletionPosition = prefix.length() + position - 1;
			
		CompilationResult fakeResult = new CompilationResult(fakeUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration fakeAST = parser.dietParse(fakeUnit, fakeResult, actualCompletionPosition);
		
		parseMethod(fakeAST, actualCompletionPosition);
		
		return (Initializer)fakeAST.types[0].fields[0];
	}

	/**
	 * Ask the engine to compute a completion at the specified position
	 * of the given compilation unit.
	 *
	 *  @return void
	 *      completion results are answered through a requestor.
	 *
	 *  @param sourceUnit org.eclipse.jdt.internal.compiler.env.ICompilationUnit
	 *      the source of the current compilation unit.
	 *
	 *  @param completionPosition int
	 *      a position in the source where the completion is taking place. 
	 *      This position is relative to the source provided.
	 */
	public void complete(ICompilationUnit sourceUnit, int completionPosition, int offset) {

		if(DEBUG) {
			System.out.print("COMPLETION IN "); //$NON-NLS-1$
			System.out.print(sourceUnit.getFileName());
			System.out.print(" AT POSITION "); //$NON-NLS-1$
			System.out.println(completionPosition);
			System.out.println("COMPLETION - Source :"); //$NON-NLS-1$
			System.out.println(sourceUnit.getContents());
		}
		try {
			actualCompletionPosition = completionPosition - 1;
			this.offset = offset;
			// for now until we can change the UI.
			CompilationResult result = new CompilationResult(sourceUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
			CompilationUnitDeclaration parsedUnit = parser.dietParse(sourceUnit, result, actualCompletionPosition);

			//		boolean completionNodeFound = false;
			if (parsedUnit != null) {
				if(DEBUG) {
					System.out.println("COMPLETION - Diet AST :"); //$NON-NLS-1$
					System.out.println(parsedUnit.toString());
				}

				// scan the package & import statements first
				if (parsedUnit.currentPackage instanceof CompletionOnPackageReference) {
					findPackages((CompletionOnPackageReference) parsedUnit.currentPackage);
					return;
				}

				ImportReference[] imports = parsedUnit.imports;
				if (imports != null) {
					for (int i = 0, length = imports.length; i < length; i++) {
						ImportReference importReference = imports[i];
						if (importReference instanceof CompletionOnImportReference) {
							findImports((CompletionOnImportReference) importReference);
							return;
						}
					}
				}

				if (parsedUnit.types != null) {
					try {
						lookupEnvironment.buildTypeBindings(parsedUnit);

						if ((unitScope = parsedUnit.scope) != null) {
							source = sourceUnit.getContents();
							lookupEnvironment.completeTypeBindings(parsedUnit, true);
							parsedUnit.scope.faultInTypes();
							parseMethod(parsedUnit, actualCompletionPosition);
							if(DEBUG) {
								System.out.println("COMPLETION - AST :"); //$NON-NLS-1$
								System.out.println(parsedUnit.toString());
							}
							parsedUnit.resolve();
						}
					} catch (CompletionNodeFound e) {
						//					completionNodeFound = true;
						if (e.astNode != null) {
							if(DEBUG) {
								System.out.print("COMPLETION - Completion node : "); //$NON-NLS-1$
								System.out.println(e.astNode.toString());
							}
							// if null then we found a problem in the completion node
							complete(e.astNode, e.qualifiedBinding, e.scope);
						}
					}
				}
			}

			/* Ignore package, import, class & interface keywords for now...
					if (!completionNodeFound) {
						if (parsedUnit == null || parsedUnit.types == null) {
							// this is not good enough... can still be trying to define a second type
							CompletionScanner scanner = (CompletionScanner) parser.scanner;
							setSourceRange(scanner.completedIdentifierStart, scanner.completedIdentifierEnd);
							findKeywords(scanner.completionIdentifier, mainDeclarations, null);
						}
						// currently have no way to know if extends/implements are possible keywords
					}
			*/
		} catch (IndexOutOfBoundsException e) { // work-around internal failure - 1GEMF6D
		} catch (InvalidCursorLocation e) { // may eventually report a usefull error
		} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
		} catch (CompletionNodeFound e){ // internal failure - bugs 5618
		} finally {
			reset();
		}
	}

	private TypeBinding[] computeTypes(Expression[] arguments, BlockScope scope) {

		if (arguments == null)
			return null;

		int argsLength = arguments.length;
		TypeBinding[] argTypes = new TypeBinding[argsLength];
		for (int a = argsLength; --a >= 0;)
			argTypes[a] = arguments[a].resolveType(scope);
		return argTypes;
	}
	
	private void findAnonymousType(
		ReferenceBinding currentType,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite) {

		if (currentType.isInterface()) {
			char[] completion = TypeConstants.NoChar;
			// nothing to insert - do not want to replace the existing selector & arguments
			if (source == null
				|| source.length <= endPosition
				|| source[endPosition] != ')')
				completion = new char[] { ')' };
			
			requestor.acceptAnonymousType(
				currentType.qualifiedPackageName(),
				currentType.qualifiedSourceName(),
				TypeConstants.NoCharChar,
				TypeConstants.NoCharChar,
				TypeConstants.NoCharChar,
				completion,
				IConstants.AccPublic,
				endPosition - offset,
				endPosition - offset,
				R_DEFAULT);
		} else {
			findConstructors(
				currentType,
				argTypes,
				scope,
				invocationSite,
				true);
		}
	}

	private void findClassField(char[] token, TypeBinding receiverType, Scope scope) {

		if (token == null)
			return;

		if (token.length <= classField.length
			&& CharOperation.prefixEquals(token, classField, false /* ignore case */
		)) {
			int relevance = R_DEFAULT;
			relevance += computeRelevanceForCaseMatching(token, classField);
			relevance += computeRelevanceForExpectingType(scope.getJavaLangClass());
				
			requestor.acceptField(
				NoChar,
				NoChar,
				classField,
				NoChar,
				NoChar,
				classField,
				CompilerModifiers.AccStatic | CompilerModifiers.AccPublic,
				startPosition - offset,
				endPosition - offset,
				relevance);
		}
	}

	private void findConstructors(
		ReferenceBinding currentType,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite,
		boolean forAnonymousType) {

		// No visibility checks can be performed without the scope & invocationSite
		MethodBinding[] methods = currentType.availableMethods();
		if(methods != null) {
			int minArgLength = argTypes == null ? 0 : argTypes.length;
			next : for (int f = methods.length; --f >= 0;) {
				MethodBinding constructor = methods[f];
				if (constructor.isConstructor()) {
					
					if (constructor.isSynthetic()) continue next;
						
					if (options.checkVisibility
						&& !constructor.canBeSeenBy(invocationSite, scope)) continue next;
	
					TypeBinding[] parameters = constructor.parameters;
					int paramLength = parameters.length;
					if (minArgLength > paramLength)
						continue next;
					for (int a = minArgLength; --a >= 0;)
						if (argTypes[a] != null) // can be null if it could not be resolved properly
							if (!scope.areTypesCompatible(argTypes[a], constructor.parameters[a]))
								continue next;
	
					char[][] parameterPackageNames = new char[paramLength][];
					char[][] parameterTypeNames = new char[paramLength][];
					for (int i = 0; i < paramLength; i++) {
						TypeBinding type = parameters[i];
						parameterPackageNames[i] = type.qualifiedPackageName();
						parameterTypeNames[i] = type.qualifiedSourceName();
					}
					char[][] parameterNames = findMethodParameterNames(constructor,parameterTypeNames);
					
					char[] completion = TypeConstants.NoChar;
					// nothing to insert - do not want to replace the existing selector & arguments
					if (source == null
						|| source.length <= endPosition
						|| source[endPosition] != ')')
						completion = new char[] { ')' };
					
					if(forAnonymousType){
						requestor.acceptAnonymousType(
							currentType.qualifiedPackageName(),
							currentType.qualifiedSourceName(),
							parameterPackageNames,
							parameterTypeNames,
							parameterNames,
							completion,
							constructor.modifiers,
							endPosition - offset,
							endPosition - offset,
							R_DEFAULT);
					} else {
						requestor.acceptMethod(
							currentType.qualifiedPackageName(),
							currentType.qualifiedSourceName(),
							currentType.sourceName(),
							parameterPackageNames,
							parameterTypeNames,
							parameterNames,
							TypeConstants.NoChar,
							TypeConstants.NoChar,
							completion,
							constructor.modifiers,
							endPosition - offset,
							endPosition - offset,
							R_DEFAULT);
					}
				}
			}
		}
	}
	
	// Helper method for findFields(char[], ReferenceBinding, Scope, ObjectVector, boolean)
	private void findFields(
		char[] fieldName,
		FieldBinding[] fields,
		Scope scope,
		ObjectVector fieldsFound,
		ObjectVector localsFound,
		boolean onlyStaticFields,
		ReferenceBinding receiverType,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		// Inherited fields which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		
		int fieldLength = fieldName.length;
		next : for (int f = fields.length; --f >= 0;) {			
			FieldBinding field = fields[f];

			if (field.isSynthetic())	continue next;

			if (onlyStaticFields && !field.isStatic()) continue next;

			if (fieldLength > field.name.length) continue next;

			if (!CharOperation.prefixEquals(fieldName, field.name, false /* ignore case */))	continue next;

			if (options.checkVisibility
				&& !field.canBeSeenBy(receiverType, invocationSite, scope))	continue next;

			boolean prefixRequired = false;

			for (int i = fieldsFound.size; --i >= 0;) {
				Object[] other = (Object[])fieldsFound.elementAt(i);
				FieldBinding otherField = (FieldBinding) other[0];
				ReferenceBinding otherReceiverType = (ReferenceBinding) other[1];
				if (field == otherField && receiverType == otherReceiverType)
					continue next;
				if (CharOperation.equals(field.name, otherField.name, true)) {
					if (field.declaringClass.isSuperclassOf(otherField.declaringClass))
						continue next;
					if (otherField.declaringClass.isInterface())
						if (field.declaringClass.implementsInterface(otherField.declaringClass, true))
							continue next;
					if (field.declaringClass.isInterface())
						if (otherField.declaringClass.implementsInterface(field.declaringClass, true))
							continue next;
					prefixRequired = true;
				}
			}

			for (int l = localsFound.size; --l >= 0;) {
				LocalVariableBinding local = (LocalVariableBinding) localsFound.elementAt(l);	

				if (CharOperation.equals(field.name, local.name, true)) {
					SourceTypeBinding declarationType = scope.enclosingSourceType();
					if (declarationType.isAnonymousType() && declarationType != invocationScope.enclosingSourceType()) {
						continue next;
					}
					prefixRequired = true;
					break;
				}
			}
			
			fieldsFound.add(new Object[]{field, receiverType});
			
			char[] completion = field.name;
			
			if(prefixRequired || options.forceImplicitQualification){
				char[] prefix = computePrefix(scope.enclosingSourceType(), invocationScope.enclosingSourceType(), field.isStatic());
				completion = CharOperation.concat(prefix,completion,'.');
			}

			int relevance = R_DEFAULT;
			relevance += computeRelevanceForCaseMatching(fieldName, field.name);
			relevance += computeRelevanceForExpectingType(field.type);

			requestor
				.acceptField(
					field.declaringClass.qualifiedPackageName(),
					field.declaringClass.qualifiedSourceName(),
					field.name,
					field.type.qualifiedPackageName(),
					field.type.qualifiedSourceName(),
					completion,
			// may include some qualification to resolve ambiguities
			field.modifiers, startPosition - offset, endPosition - offset,
			relevance);
		}
	}

	private void findFields(
		char[] fieldName,
		ReferenceBinding receiverType,
		Scope scope,
		ObjectVector fieldsFound,
		ObjectVector localsFound,
		boolean onlyStaticFields,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		if (fieldName == null)
			return;

		ReferenceBinding currentType = receiverType;
		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;
		do {

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {

				if (interfacesToVisit == null)
					interfacesToVisit = new ReferenceBinding[5][];

				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(
						interfacesToVisit,
						0,
						interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
						0,
						lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}

			FieldBinding[] fields = currentType.availableFields();
			if(fields != null) {
				findFields(
					fieldName,
					fields,
					scope,
					fieldsFound,
					localsFound,
					onlyStaticFields,
					receiverType,
					invocationSite,
					invocationScope,
					implicitCall);
			}
			currentType = currentType.superclass();
		} while (currentType != null);

		if (interfacesToVisit != null) {
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {

					ReferenceBinding anInterface = interfaces[j];
					if ((anInterface.tagBits & TagBits.InterfaceVisited) == 0) {
						// if interface as not already been visited
						anInterface.tagBits |= TagBits.InterfaceVisited;

						FieldBinding[] fields = anInterface.availableFields();
						if(fields !=  null) {
							findFields(
								fieldName,
								fields,
								scope,
								fieldsFound,
								localsFound,
								onlyStaticFields,
								receiverType,
								invocationSite,
								invocationScope,
								implicitCall);
						}

						ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {
							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++)
					interfaces[j].tagBits &= ~TagBits.InterfaceVisited;
			}
		}
	}

	private void findFieldsAndMethods(
		char[] token,
		TypeBinding receiverType,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		if (token == null)
			return;

		if (receiverType.isBaseType())
			return; // nothing else is possible with base types

		if (receiverType.isArrayType()) {
			if (token.length <= lengthField.length
				&& CharOperation.prefixEquals(token, lengthField, false /* ignore case */
			)) {
				
				int relevance = R_DEFAULT;
				relevance += computeRelevanceForCaseMatching(token,lengthField);
				relevance += computeRelevanceForExpectingType(BaseTypes.IntBinding);
				
				requestor.acceptField(
					NoChar,
					NoChar,
					lengthField,
					NoChar,
					NoChar,
					lengthField,
					CompilerModifiers.AccPublic,
					startPosition - offset,
					endPosition - offset,
					relevance);
			}
			receiverType = scope.getJavaLangObject();
		}

		findFields(
			token,
			(ReferenceBinding) receiverType,
			scope,
			new ObjectVector(),
			new ObjectVector(),
			false,
			invocationSite,
			invocationScope,
			implicitCall);

		findMethods(
			token,
			null,
			(ReferenceBinding) receiverType,
			scope,
			new ObjectVector(),
			false,
			false,
			false,
			invocationSite,
			invocationScope,
			implicitCall);
	}

	private void findImports(CompletionOnImportReference importReference) {
		char[][] tokens = importReference.tokens;
			
		char[] importName = CharOperation.concatWith(tokens, '.');
		
		if (importName.length == 0)
			return;
			
		char[] lastToken = tokens[tokens.length - 1];
		if(lastToken != null && lastToken.length == 0)
			importName = CharOperation.concat(importName, new char[]{'.'});

		resolvingImports = true;
		setSourceRange(
			importReference.sourceStart,
			importReference.declarationSourceEnd);
			
		token =  importName;
		// want to replace the existing .*;
		nameEnvironment.findPackages(importName, this);
		nameEnvironment.findTypes(importName, this);
	}

	// what about onDemand types? Ignore them since it does not happen!
	// import p1.p2.A.*;
	private void findKeywords(char[] keyword, char[][] choices, Scope scope) {

		int length = keyword.length;
		if (length > 0)
			for (int i = 0; i < choices.length; i++)
				if (length <= choices[i].length
					&& CharOperation.prefixEquals(keyword, choices[i], false /* ignore case */
				)){
					int relevance = R_DEFAULT;
					relevance += computeRelevanceForCaseMatching(keyword, choices[i]);
					
					requestor.acceptKeyword(choices[i], startPosition - offset, endPosition - offset,relevance);
				}
	}

	// Helper method for findMemberTypes(char[], ReferenceBinding, Scope)
	private void findMemberTypes(
		char[] typeName,
		ReferenceBinding[] memberTypes,
		ObjectVector typesFound,
		ReferenceBinding receiverType,
		SourceTypeBinding invocationType) {

		// Inherited member types which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		int typeLength = typeName.length;
		next : for (int m = memberTypes.length; --m >= 0;) {
			ReferenceBinding memberType = memberTypes[m];
			//		if (!wantClasses && memberType.isClass()) continue next;
			//		if (!wantInterfaces && memberType.isInterface()) continue next;
			if (typeLength > memberType.sourceName.length)
				continue next;

			if (!CharOperation.prefixEquals(typeName, memberType.sourceName, false
				/* ignore case */
				))
				continue next;

			if (options.checkVisibility
				&& !memberType.canBeSeenBy(receiverType, invocationType))
				continue next;

			for (int i = typesFound.size; --i >= 0;) {
				ReferenceBinding otherType = (ReferenceBinding) typesFound.elementAt(i);

				if (memberType == otherType)
					continue next;

				if (CharOperation.equals(memberType.sourceName, otherType.sourceName, true)) {

					if (memberType.enclosingType().isSuperclassOf(otherType.enclosingType()))
						continue next;

					if (otherType.enclosingType().isInterface())
						if (memberType.enclosingType()
							.implementsInterface(otherType.enclosingType(), true))
							continue next;

					if (memberType.enclosingType().isInterface())
						if (otherType.enclosingType()
							.implementsInterface(memberType.enclosingType(), true))
							continue next;
				}
			}

			typesFound.add(memberType);

			int relevance = R_DEFAULT;
			relevance += computeRelevanceForCaseMatching(typeName, memberType.sourceName);
			relevance += computeRelevanceForExpectingType(memberType);

			if (memberType.isClass()) {
				relevance += computeRelevanceForClass();
				requestor.acceptClass(
					memberType.qualifiedPackageName(),
					memberType.qualifiedSourceName(),
					memberType.sourceName(),
					memberType.modifiers,
					startPosition - offset,
					endPosition - offset,
					relevance);

			} else {
				relevance += computeRelevanceForInterface();
				requestor.acceptInterface(
					memberType.qualifiedPackageName(),
					memberType.qualifiedSourceName(),
					memberType.sourceName(),
					memberType.modifiers,
					startPosition - offset,
					endPosition - offset,
					relevance);
			}
		}
	}

	private void findMemberTypes(
		char[] typeName,
		ReferenceBinding receiverType,
		Scope scope,
		SourceTypeBinding typeInvocation) {

		ReferenceBinding currentType = receiverType;
		if (typeName == null)
			return;

		if (currentType.superInterfaces() == null)
			return; // we're trying to find a supertype

		ObjectVector typesFound = new ObjectVector();
		if (insideQualifiedReference
			|| typeName.length == 0) { // do not search up the hierarchy

			findMemberTypes(
				typeName,
				currentType.memberTypes(),
				typesFound,
				receiverType,
				typeInvocation);
			return;
		}

		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;

		do {

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {

				if (interfacesToVisit == null)
					interfacesToVisit = new ReferenceBinding[5][];

				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(
						interfacesToVisit,
						0,
						interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
						0,
						lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}

			findMemberTypes(
				typeName,
				currentType.memberTypes(),
				typesFound,
				receiverType,
				typeInvocation);
			currentType = currentType.superclass();

		} while (currentType != null);

		if (interfacesToVisit != null) {
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {

					ReferenceBinding anInterface = interfaces[j];
					if ((anInterface.tagBits & TagBits.InterfaceVisited) == 0) {
						// if interface as not already been visited
						anInterface.tagBits |= TagBits.InterfaceVisited;

						findMemberTypes(
							typeName,
							anInterface.memberTypes(),
							typesFound,
							receiverType,
							typeInvocation);

						ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {

							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++)
					interfaces[j].tagBits &= ~TagBits.InterfaceVisited;
			}
		}
	}

	private void findIntefacesMethods(
		char[] selector,
		TypeBinding[] argTypes,
		ReferenceBinding receiverType,
		ReferenceBinding[] itsInterfaces,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		boolean isCompletingDeclaration,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		if (selector == null)
			return;

		if (itsInterfaces != NoSuperInterfaces) {
			ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
			int lastPosition = 0;
			interfacesToVisit[lastPosition] = itsInterfaces;
			
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];

				for (int j = 0, length = interfaces.length; j < length; j++) {
					ReferenceBinding currentType = interfaces[j];

					if ((currentType.tagBits & TagBits.InterfaceVisited) == 0) {
						// if interface as not already been visited
						currentType.tagBits |= TagBits.InterfaceVisited;

						MethodBinding[] methods = currentType.availableMethods();
						if(methods != null) {
							if(isCompletingDeclaration){
	
								findLocalMethodDeclarations(
									selector,
									methods,
									scope,
									methodsFound,
									onlyStaticMethods,
									exactMatch,
									receiverType);
	
							} else {
	
								findLocalMethods(
									selector,
									argTypes,
									methods,
									scope,
									methodsFound,
									onlyStaticMethods,
									exactMatch,
									receiverType,
									invocationSite,
									invocationScope,
									implicitCall);
							}
						}

						itsInterfaces = currentType.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {

							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];

				for (int j = 0, length = interfaces.length; j < length; j++){
					interfaces[j].tagBits &= ~TagBits.InterfaceVisited;
				}
			}
		}
	}
	
	private void findImplicitMessageSends(
		char[] token,
		TypeBinding[] argTypes,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope) {

		if (token == null)
			return;

		boolean staticsOnly = false;
		// need to know if we're in a static context (or inside a constructor)
		ObjectVector methodsFound = new ObjectVector();

		done : while (true) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (scope.kind) {

				case Scope.METHOD_SCOPE :
					// handle the error case inside an explicit constructor call (see MethodScope>>findField)
					MethodScope methodScope = (MethodScope) scope;
					staticsOnly |= methodScope.isStatic | methodScope.isConstructorCall;
					break;

				case Scope.CLASS_SCOPE :
					ClassScope classScope = (ClassScope) scope;
					SourceTypeBinding enclosingType = classScope.referenceContext.binding;
					findMethods(
						token,
						argTypes,
						enclosingType,
						classScope,
						methodsFound,
						staticsOnly,
						true,
						false,
						invocationSite,
						invocationScope,
						true);
					staticsOnly |= enclosingType.isStatic();
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					break done;
			}
			scope = scope.parent;
		}
	}

	// Helper method for findMethods(char[], TypeBinding[], ReferenceBinding, Scope, ObjectVector, boolean, boolean, boolean)
	private void findLocalMethods(
		char[] methodName,
		TypeBinding[] argTypes,
		MethodBinding[] methods,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		ReferenceBinding receiverType,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {

		// Inherited methods which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite

		int methodLength = methodName.length;
		int minArgLength = argTypes == null ? 0 : argTypes.length;

		next : for (int f = methods.length; --f >= 0;) {
			MethodBinding method = methods[f];

			if (method.isSynthetic()) continue next;

			if (method.isDefaultAbstract())	continue next;

			if (method.isConstructor()) continue next;

			//		if (noVoidReturnType && method.returnType == BaseTypes.VoidBinding) continue next;
			if (onlyStaticMethods && !method.isStatic()) continue next;

			if (options.checkVisibility
				&& !method.canBeSeenBy(receiverType, invocationSite, scope)) continue next;

			if (exactMatch) {
				if (!CharOperation.equals(methodName, method.selector, false /* ignore case */
					))
					continue next;

			} else {

				if (methodLength > method.selector.length)
					continue next;

				if (!CharOperation.prefixEquals(methodName, method.selector, false
					/* ignore case */
					))
					continue next;
			}
			if (minArgLength > method.parameters.length)
				continue next;

			for (int a = minArgLength; --a >= 0;){
				if (argTypes[a] != null){ // can be null if it could not be resolved properly
					if (!scope.areTypesCompatible(argTypes[a], method.parameters[a])) {
						continue next;
					}
				}
			}
			
			boolean prefixRequired = false;
			
			for (int i = methodsFound.size; --i >= 0;) {
				Object[] other = (Object[]) methodsFound.elementAt(i);
				MethodBinding otherMethod = (MethodBinding) other[0];
				ReferenceBinding otherReceiverType = (ReferenceBinding) other[1];
				if (method == otherMethod && receiverType == otherReceiverType)
					continue next;

				if (CharOperation.equals(method.selector, otherMethod.selector, true)
					&& method.areParametersEqual(otherMethod)) {

					if (method.declaringClass.isSuperclassOf(otherMethod.declaringClass))
						continue next;

					if (otherMethod.declaringClass.isInterface())
						if (method
							.declaringClass
							.implementsInterface(otherMethod.declaringClass, true))
							continue next;

					if (method.declaringClass.isInterface())
						if(otherMethod
							.declaringClass
							.implementsInterface(method.declaringClass,true))
							continue next;
					prefixRequired = true;
				}
			}

			methodsFound.add(new Object[]{method, receiverType});
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];

			for (int i = 0; i < length; i++) {
				TypeBinding type = method.parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}
			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);

			char[] completion = TypeConstants.NoChar;
			
			int previousStartPosition = startPosition;
			
			// nothing to insert - do not want to replace the existing selector & arguments
			if (!exactMatch) {
				if (source != null
					&& source.length > endPosition
					&& source[endPosition] == '(')
					completion = method.selector;
				else
					completion = CharOperation.concat(method.selector, new char[] { '(', ')' });
			} else {
				if(prefixRequired && (source != null)) {
					completion = CharOperation.subarray(source, startPosition, endPosition);
				} else {
					startPosition = endPosition;
				}
			}
			
			if(prefixRequired || options.forceImplicitQualification){
				char[] prefix = computePrefix(scope.enclosingSourceType(), invocationScope.enclosingSourceType(), method.isStatic());
				completion = CharOperation.concat(prefix,completion,'.');
			}

			int relevance = R_DEFAULT;
			relevance += computeRelevanceForCaseMatching(methodName, method.selector);
			relevance += computeRelevanceForExpectingType(method.returnType);

			requestor.acceptMethod(
				method.declaringClass.qualifiedPackageName(),
				method.declaringClass.qualifiedSourceName(),
				method.selector,
				parameterPackageNames,
				parameterTypeNames,
				parameterNames,
				method.returnType.qualifiedPackageName(),
				method.returnType.qualifiedSourceName(),
				completion,
				method.modifiers,
				startPosition - offset,
				endPosition - offset,
				relevance);
			startPosition = previousStartPosition;
		}
	}
	
	private int computeRelevanceForCaseMatching(char[] token, char[] proposalName){
		if (CharOperation.prefixEquals(token, proposalName, true /* do not ignore case */)) {
			return  R_CASE;
		} else {
			return R_DEFAULT;
		}
	}
	private int computeRelevanceForClass(){
		if(assistNodeIsClass) {
			return R_CLASS;
		}
		return 0;
	}
	private int computeRelevanceForInterface(){
		if(assistNodeIsInterface) {
			return R_INTERFACE;
		}
		return R_DEFAULT;
	}
	private int computeRelevanceForException(char[] proposalName){
		
		if(assistNodeIsException &&
			(CharOperation.match(EXCEPTION_PATTERN, proposalName, false) ||
			CharOperation.match(ERROR_PATTERN, proposalName, false))) { 
			return R_EXCEPTION;
		}
		return R_DEFAULT;
	}
	private int computeRelevanceForExpectingType(TypeBinding proposalType){
		if(expectedTypes != null && proposalType != null) {
			for (int i = 0; i < expectedTypes.length; i++) {
				if(Scope.areTypesCompatible(proposalType, expectedTypes[i])) {
					return R_EXPECTED_TYPE;
				}
			}
		} 
		return R_DEFAULT;
	}
	private int computeRelevanceForExpectingType(char[] packageName, char[] typeName){
		if(expectedTypes != null) {
			for (int i = 0; i < expectedTypes.length; i++) {
				if(CharOperation.equals(expectedTypes[i].qualifiedPackageName(), packageName) &&
					CharOperation.equals(expectedTypes[i].qualifiedSourceName(), typeName)) {
					return R_EXPECTED_TYPE;
				}
			}
		} 
		return R_DEFAULT;
	}

	// Helper method for findMethods(char[], MethodBinding[], Scope, ObjectVector, boolean, boolean, boolean, TypeBinding)
	private void findLocalMethodDeclarations(
		char[] methodName,
		MethodBinding[] methods,
		Scope scope,
		ObjectVector methodsFound,
		//	boolean noVoidReturnType, how do you know?
		boolean onlyStaticMethods,
		boolean exactMatch,
		ReferenceBinding receiverType) {

		// Inherited methods which are hidden by subclasses are filtered out
		// No visibility checks can be performed without the scope & invocationSite
		int methodLength = methodName.length;
		next : for (int f = methods.length; --f >= 0;) {

			MethodBinding method = methods[f];
			if (method.isSynthetic())	continue next;
				
			if (method.isDefaultAbstract()) continue next;
			
			if (method.isConstructor()) continue next;
				
			if (method.isFinal()) continue next;

			//		if (noVoidReturnType && method.returnType == BaseTypes.VoidBinding) continue next;
			if (onlyStaticMethods && !method.isStatic()) continue next;

			if (options.checkVisibility
				&& !method.canBeSeenBy(receiverType, FakeInvocationSite , scope)) continue next;

			if (exactMatch) {
				if (!CharOperation.equals(methodName, method.selector, false /* ignore case */
					))
					continue next;

			} else {

				if (methodLength > method.selector.length)
					continue next;

				if (!CharOperation.prefixEquals(methodName, method.selector, false
					/* ignore case */
					))
					continue next;
			}

			for (int i = methodsFound.size; --i >= 0;) {
				MethodBinding otherMethod = (MethodBinding) methodsFound.elementAt(i);
				if (method == otherMethod)
					continue next;

				if (CharOperation.equals(method.selector, otherMethod.selector, true)
					&& method.areParametersEqual(otherMethod)) {
					continue next;
				}
			}

			methodsFound.add(method);
			
			int length = method.parameters.length;
			char[][] parameterPackageNames = new char[length][];
			char[][] parameterTypeNames = new char[length][];
			
			for (int i = 0; i < length; i++) {
				TypeBinding type = method.parameters[i];
				parameterPackageNames[i] = type.qualifiedPackageName();
				parameterTypeNames[i] = type.qualifiedSourceName();
			}

			char[][] parameterNames = findMethodParameterNames(method,parameterTypeNames);
			
			StringBuffer completion = new StringBuffer(10);
			// flush uninteresting modifiers
			int insertedModifiers = method.modifiers & ~(CompilerModifiers.AccNative | CompilerModifiers.AccAbstract);

			if (!exactMatch) {
				if(insertedModifiers != CompilerModifiers.AccDefault){
					completion.append(AstNode.modifiersString(insertedModifiers));
				}
				char[] returnPackageName = method.returnType.qualifiedPackageName();
				char[] returnTypeName = method.returnType.qualifiedSourceName();
				if(mustQualifyType(returnPackageName, returnTypeName)) {
					completion.append(CharOperation.concat(returnPackageName, returnTypeName,'.'));
				} else {
					completion.append(method.returnType.sourceName());
				}
				completion.append(' ');
				completion.append(method.selector);
				completion.append('(');

				for(int i = 0; i < length ; i++){
					if(mustQualifyType(parameterPackageNames[i], parameterTypeNames[i])){
						completion.append(CharOperation.concat(parameterPackageNames[i], parameterTypeNames[i], '.'));
					} else {
						completion.append(parameterTypeNames[i]);
					}
					completion.append(' ');
					if(parameterNames != null){
						completion.append(parameterNames[i]);
					} else {
						completion.append('%');
					}
					if(i != (length - 1))
						completion.append(',');	
				}
				completion.append(')');
				
				ReferenceBinding[] exceptions = method.thrownExceptions;
				
				if (exceptions != null && exceptions.length > 0){
					completion.append(' ');
					completion.append(THROWS);
					completion.append(' ');
					for(int i = 0; i < exceptions.length ; i++){
						ReferenceBinding exception = exceptions[i];

						char[] exceptionPackageName = exception.qualifiedPackageName();
						char[] exceptionTypeName = exception.qualifiedSourceName();
						
						if(i != 0){
							completion.append(',');
							completion.append(' ');
						}
						
						if(mustQualifyType(exceptionPackageName, exceptionTypeName)){
							completion.append(CharOperation.concat(exceptionPackageName, exceptionTypeName, '.'));
						} else {
							completion.append(exception.sourceName());
						}
					}
				}
			}

			int relevance = R_DEFAULT;
			relevance += computeRelevanceForCaseMatching(methodName, method.selector);

			requestor.acceptMethodDeclaration(
				method.declaringClass.qualifiedPackageName(),
				method.declaringClass.qualifiedSourceName(),
				method.selector,
				parameterPackageNames,
				parameterTypeNames,
				parameterNames,
				method.returnType.qualifiedPackageName(),
				method.returnType.qualifiedSourceName(),
				completion.toString().toCharArray(),
				method.modifiers,
				startPosition - offset,
				endPosition - offset,
				relevance);
		}
	}
	private void findMethods(
		char[] selector,
		TypeBinding[] argTypes,
		ReferenceBinding receiverType,
		Scope scope,
		ObjectVector methodsFound,
		boolean onlyStaticMethods,
		boolean exactMatch,
		boolean isCompletingDeclaration,
		InvocationSite invocationSite,
		Scope invocationScope,
		boolean implicitCall) {
		if (selector == null)
			return;
		
		if(isCompletingDeclaration) {
			MethodBinding[] methods = receiverType.availableMethods();
			if (methods != null){
				for (int i = 0; i < methods.length; i++) {
					if(!methods[i].isDefaultAbstract()) {
						methodsFound.add(methods[i]);
					}
				}
			}
		}
		
		ReferenceBinding currentType = receiverType;
		if (receiverType.isInterface()) {
			if(isCompletingDeclaration) {
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					currentType.superInterfaces(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
			} else {
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					new ReferenceBinding[]{currentType},
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
			}
			
			currentType = scope.getJavaLangObject();
		} else {
			if(isCompletingDeclaration){
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					currentType.superInterfaces(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
				
				currentType = receiverType.superclass();
			}
		}
		boolean hasPotentialDefaultAbstractMethods = true;
		while (currentType != null) {
			
			MethodBinding[] methods = currentType.availableMethods();
			if(methods != null) {
				if(isCompletingDeclaration){
					findLocalMethodDeclarations(
						selector,
						methods,
						scope,
						methodsFound,
						onlyStaticMethods,
						exactMatch,
						receiverType);
				} else{
					findLocalMethods(
						selector,
						argTypes,
						methods,
						scope,
						methodsFound,
						onlyStaticMethods,
						exactMatch,
						receiverType,
						invocationSite,
						invocationScope,
						implicitCall);
				}
			}
			
			if(hasPotentialDefaultAbstractMethods && currentType.isAbstract()){
				findIntefacesMethods(
					selector,
					argTypes,
					receiverType,
					currentType.superInterfaces(),
					scope,
					methodsFound,
					onlyStaticMethods,
					exactMatch,
					isCompletingDeclaration,
					invocationSite,
					invocationScope,
					implicitCall);
			} else {
				hasPotentialDefaultAbstractMethods = false;
			}
			currentType = currentType.superclass();
		}
	}
	private char[][] findMethodParameterNames(MethodBinding method, char[][] parameterTypeNames){
		ReferenceBinding bindingType = method.declaringClass;

		char[][] parameterNames = null;
		
		int length = parameterTypeNames.length;

		if (length == 0){
			return TypeConstants.NoCharChar;
		}
		// look into the corresponding unit if it is available
		if (bindingType instanceof SourceTypeBinding){
			SourceTypeBinding sourceType = (SourceTypeBinding) bindingType;

			if (sourceType.scope != null){
				TypeDeclaration parsedType;

				if ((parsedType = sourceType.scope.referenceContext) != null){
					AbstractMethodDeclaration methodDecl = parsedType.declarationOf(method);

					if (methodDecl != null){
						Argument[] arguments = methodDecl.arguments;
						parameterNames = new char[length][];

						for(int i = 0 ; i < length ; i++){
							parameterNames[i] = arguments[i].name;
						}
					}
				}
			}
		}
		// look into the model		
		if(parameterNames == null){
			NameEnvironmentAnswer answer = nameEnvironment.findType(bindingType.compoundName);

			if(answer != null){
				if(answer.isSourceType()) {
					ISourceType sourceType = answer.getSourceTypes()[0];
					ISourceMethod[] sourceMethods = sourceType.getMethods();
					int len = sourceMethods == null ? 0 : sourceMethods.length;
					for(int i = 0; i < len ; i++){
						ISourceMethod sourceMethod = sourceMethods[i];
						char[][] argTypeNames = sourceMethod.getArgumentTypeNames();

						if(argTypeNames != null &&
							CharOperation.equals(method.selector,sourceMethod.getSelector()) &&
							CharOperation.equals(argTypeNames,parameterTypeNames)){
							parameterNames = sourceMethod.getArgumentNames();
							break;
						}
					}
				} 
			}
		}
		return parameterNames;
	}
	
	private void findNestedTypes(
		char[] typeName,
		SourceTypeBinding currentType,
		Scope scope) {
		if (typeName == null)
			return;

		int typeLength = typeName.length;

		while (scope != null) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (scope.kind) {

				case Scope.METHOD_SCOPE :
				case Scope.BLOCK_SCOPE :
					BlockScope blockScope = (BlockScope) scope;

					next : for (int i = 0, length = blockScope.scopeIndex; i < length; i++) {

						if (blockScope.subscopes[i] instanceof ClassScope) {
							SourceTypeBinding localType =
								((ClassScope) blockScope.subscopes[i]).referenceContext.binding;

							if (!localType.isAnonymousType()) {
								if (typeLength > localType.sourceName.length)
									continue next;
								if (!CharOperation.prefixEquals(typeName, localType.sourceName, false
									/* ignore case */
									))
									continue next;

								int relevance = R_DEFAULT;
								relevance += computeRelevanceForCaseMatching(typeName, localType.sourceName);
								relevance += computeRelevanceForExpectingType(localType);
								relevance += computeRelevanceForClass();
								
								requestor.acceptClass(
									localType.qualifiedPackageName(),
									localType.sourceName,
									localType.sourceName,
									localType.modifiers,
									startPosition - offset,
									endPosition - offset,
									relevance);
							}
						}
					}
					break;

				case Scope.CLASS_SCOPE :
					findMemberTypes(typeName, scope.enclosingSourceType(), scope, currentType);
					if (typeLength == 0)
						return; // do not search outside the class scope if no prefix was provided
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					return;
			}
			scope = scope.parent;
		}
	}

	private void findPackages(CompletionOnPackageReference packageStatement) {

		token = CharOperation.concatWith(packageStatement.tokens, '.');
		if (token.length == 0)
			return;

		setSourceRange(packageStatement.sourceStart, packageStatement.sourceEnd);
		nameEnvironment.findPackages(CharOperation.toLowerCase(token), this);
	}

	private void findTypesAndPackages(char[] token, Scope scope) {

		if (token == null)
			return;

		if (scope.enclosingSourceType() != null)
			findNestedTypes(token, scope.enclosingSourceType(), scope);

		if (unitScope != null) {
			int typeLength = token.length;
			SourceTypeBinding[] types = unitScope.topLevelTypes;

			for (int i = 0, length = types.length; i < length; i++) {
				SourceTypeBinding sourceType = types[i]; 

				if (typeLength > sourceType.sourceName.length)	continue;
				
				if (!CharOperation.prefixEquals(token, sourceType.sourceName, false))	continue;

				int relevance = R_DEFAULT;
				relevance += computeRelevanceForCaseMatching(token, sourceType.sourceName);
				relevance += computeRelevanceForExpectingType(sourceType);

				if (sourceType.isClass()){
					relevance += computeRelevanceForClass();
					requestor.acceptClass(
						sourceType.qualifiedPackageName(),
						sourceType.sourceName(),
						sourceType.sourceName(),
						sourceType.modifiers,
						startPosition - offset, 
						endPosition - offset,
						relevance);
				} else {
					relevance += computeRelevanceForInterface();
					requestor.acceptInterface(
						sourceType.qualifiedPackageName(),
						sourceType.sourceName(),
						sourceType.sourceName(),
						sourceType.modifiers,
						startPosition - offset,
						endPosition - offset,
						relevance);
				}
			}
		}

		if (token.length == 0)
			return;

		findKeywords(token, baseTypes, scope);
		nameEnvironment.findTypes(token, this);
		nameEnvironment.findPackages(token, this);
	}

	private void findTypesAndSubpackages(
		char[] token,
		PackageBinding packageBinding) {

		char[] qualifiedName =
			CharOperation.concatWith(packageBinding.compoundName, token, '.');

		if (token == null || token.length == 0) {
			int length = qualifiedName.length;
			System.arraycopy(
				qualifiedName,
				0,
				qualifiedName = new char[length + 1],
				0,
				length);
			qualifiedName[length] = '.';
		}
		nameEnvironment.findTypes(qualifiedName, this);
		nameEnvironment.findPackages(qualifiedName, this);
	}

	private void findVariablesAndMethods(
		char[] token,
		Scope scope,
		InvocationSite invocationSite,
		Scope invocationScope) {

		if (token == null)
			return;

		// Should local variables hide fields from the receiver type or any of its enclosing types?
		// we know its an implicit field/method access... see BlockScope getBinding/getImplicitMethod

		boolean staticsOnly = false;
		// need to know if we're in a static context (or inside a constructor)
		int tokenLength = token.length;

		ObjectVector localsFound = new ObjectVector();
		ObjectVector fieldsFound = new ObjectVector();
		ObjectVector methodsFound = new ObjectVector();

		Scope currentScope = scope;

		done1 : while (true) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (currentScope.kind) {

				case Scope.METHOD_SCOPE :
					// handle the error case inside an explicit constructor call (see MethodScope>>findField)
					MethodScope methodScope = (MethodScope) currentScope;
					staticsOnly |= methodScope.isStatic | methodScope.isConstructorCall;

				case Scope.BLOCK_SCOPE :
					BlockScope blockScope = (BlockScope) currentScope;

					next : for (int i = 0, length = blockScope.locals.length; i < length; i++) {
						LocalVariableBinding local = blockScope.locals[i];

						if (local == null)
							break next;

						if (tokenLength > local.name.length)
							continue next;

						if (!CharOperation.prefixEquals(token, local.name, false /* ignore case */
							))
							continue next;

						if (local.isSecret())
							continue next;

						for (int f = 0; f < localsFound.size; f++) {
							LocalVariableBinding otherLocal =
								(LocalVariableBinding) localsFound.elementAt(f);
							if (CharOperation.equals(otherLocal.name, local.name, true))
								continue next;
						}
						localsFound.add(local);

						int relevance = R_DEFAULT;
						relevance += computeRelevanceForCaseMatching(token, local.name);
						relevance += computeRelevanceForExpectingType(local.type);
						
						requestor.acceptLocalVariable(
							local.name,
							local.type == null 
								? NoChar
								: local.type.qualifiedPackageName(),
							local.type == null
								? local.declaration.type.toString().toCharArray()
								: local.type.qualifiedSourceName(),
							local.modifiers,
							startPosition - offset,
							endPosition - offset,
							relevance);
					}
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					break done1;
			}
			currentScope = currentScope.parent;
		}

		currentScope = scope;

		done2 : while (true) { // done when a COMPILATION_UNIT_SCOPE is found

			switch (currentScope.kind) {

				case Scope.CLASS_SCOPE :
					ClassScope classScope = (ClassScope) currentScope;
					SourceTypeBinding enclosingType = classScope.referenceContext.binding;
					/*				if (tokenLength == 0) { // only search inside the type itself if no prefix was provided
										findFields(token, enclosingType.fields(), classScope, fieldsFound, staticsOnly);
										findMethods(token, enclosingType.methods(), classScope, methodsFound, staticsOnly, false);
										break done;
									} else { */
					findFields(
						token,
						enclosingType,
						classScope,
						fieldsFound,
						localsFound,
						staticsOnly,
						invocationSite,
						invocationScope,
						true);

					findMethods(
						token,
						null,
						enclosingType,
						classScope,
						methodsFound,
						staticsOnly,
						false,
						false,
						invocationSite,
						invocationScope,
						true);
					staticsOnly |= enclosingType.isStatic();
					//				}
					break;

				case Scope.COMPILATION_UNIT_SCOPE :
					break done2;
			}
			currentScope = currentScope.parent;
		}
	}

	// Helper method for private void findVariableNames(char[] name, TypeReference type )
	private void findVariableName(char[] token, char[] qualifiedPackageName, char[] qualifiedSourceName, char[] sourceName, char[][] excludeNames, int dim){
			if(sourceName == null || sourceName.length == 0)
				return;
				
			char[] name = null;
			
			// compute variable name for base type
			try{
				nameScanner.setSource(sourceName);
				switch (nameScanner.getNextToken()) {
					case TokenNameint :
					case TokenNamebyte :
					case TokenNameshort :
					case TokenNamechar :
					case TokenNamelong :
					case TokenNamefloat :
					case TokenNamedouble :
					case TokenNameboolean :
						if(token != null && token.length != 0)
							return;
						name = computeBaseNames(sourceName[0], excludeNames);
						break;
				}
				if(name != null) {
					int relevance = R_DEFAULT;
					relevance += computeRelevanceForCaseMatching(token, name);
					
					// accept result
					requestor.acceptVariableName(
						qualifiedPackageName,
						qualifiedSourceName,
						name,
						name,
						startPosition - offset,
						endPosition - offset,
						relevance);
					return;
				}
			} catch(InvalidInputException e){
			}
			
			// compute variable name for non base type
			char[][] names = computeNames(sourceName, dim > 0);
			char[] displayName;
			if (dim > 0){
				int l = qualifiedSourceName.length;
				displayName = new char[l+(2*dim)];
				System.arraycopy(qualifiedSourceName, 0, displayName, 0, l);
				for(int i = 0; i < dim; i++){
					displayName[l+(i*2)] = '[';
					displayName[l+(i*2)+1] = ']';
				}
			} else {
				displayName = qualifiedSourceName;
			}
			next : for(int i = 0 ; i < names.length ; i++){
				name = names[i];
				
				if (!CharOperation.prefixEquals(token, name, false))
					continue next;
				
				// completion must be an identifier (not a keyword, ...).
				try{
					nameScanner.setSource(name);
					if(nameScanner.getNextToken() != TokenNameIdentifier)
						continue next;
				} catch(InvalidInputException e){
					continue next;
				}
				
				int count = 2;
				char[] originalName = name;
				for(int j = 0 ; j < excludeNames.length ; j++){
					if(CharOperation.equals(name, excludeNames[j], false)) {
						name = CharOperation.concat(originalName, String.valueOf(count++).toCharArray());
						j = 0;
					}	
				}
				
				int relevance = R_DEFAULT;
				relevance += computeRelevanceForCaseMatching(token, name);
				
				// accept result
				requestor.acceptVariableName(
					qualifiedPackageName,
					displayName,
					name,
					name,
					startPosition - offset,
					endPosition - offset,
					relevance);
			}
	}

	private void findVariableNames(char[] name, TypeReference type , char[][] excludeNames){

		if(type != null &&
			type.binding != null &&
			type.binding.problemId() == Binding.NoError){
			TypeBinding tb = type.binding;
			findVariableName(
				name,
				tb.leafComponentType().qualifiedPackageName(),
				tb.leafComponentType().qualifiedSourceName(),
				tb.leafComponentType().sourceName(),
				excludeNames,
				type.dimensions());
		}/*	else {
			char[][] typeName = type.getTypeName();
			findVariableName(
				name,
				NoChar,
				CharOperation.concatWith(typeName, '.'),
				typeName[typeName.length - 1],
				excludeNames,
				type.dimensions());
		}*/
	}
	
	public AssistParser getParser() {

		return parser;
	}

	protected void reset() {

		super.reset();
		this.knownPkgs = new HashtableOfObject(10);
		this.knownTypes = new HashtableOfObject(10);
	}

	private void setSourceRange(int start, int end) {

		this.startPosition = start;
		this.endPosition = end + 1;
	}
	
	private char[] computeBaseNames(char firstName, char[][] excludeNames){
		char[] name = new char[]{firstName};
		
		for(int i = 0 ; i < excludeNames.length ; i++){
			if(CharOperation.equals(name, excludeNames[i], false)) {
				name[0]++;
				if(name[0] > 'z')
					name[0] = 'a';
				if(name[0] == firstName)
					return null;
				i = 0;
			}	
		}
		
		return name;
	}
	private void computeExpectedTypes(AstNode parent, Scope scope){
		int expectedTypeCount = 0;
		expectedTypes = new TypeBinding[1];
		
		if(parent instanceof AbstractVariableDeclaration) {
			TypeBinding binding = ((AbstractVariableDeclaration)parent).type.binding;
			if(binding != null) {
				expectedTypes[expectedTypeCount++] = binding;
			}
		} else if(parent instanceof Assignment) {
			TypeBinding binding = ((Assignment)parent).lhsType;
			if(binding != null) {
				expectedTypes[expectedTypeCount++] = binding;
			}
		} else if(parent instanceof ReturnStatement) {
			MethodBinding methodBinding = ((AbstractMethodDeclaration) scope.methodScope().referenceContext).binding;
			TypeBinding binding = methodBinding  == null ? null : methodBinding.returnType;
			if(binding != null) {
				expectedTypes[expectedTypeCount++] = binding;
			}
		}
		
		System.arraycopy(expectedTypes, 0, expectedTypes = new TypeBinding[expectedTypeCount], 0, expectedTypeCount);
	}
	private char[][] computeNames(char[] sourceName, boolean forArray){
		char[][] names = new char[5][];
		int nameCount = 0;
		boolean previousIsUpperCase = false;
		for(int i = sourceName.length - 1 ; i >= 0 ; i--){
			boolean isUpperCase = Character.isUpperCase(sourceName[i]);
			if(isUpperCase && !previousIsUpperCase){
				char[] name = CharOperation.subarray(sourceName,i,sourceName.length);
				if(name.length > 1){
					if(nameCount == names.length) {
						System.arraycopy(names, 0, names = new char[nameCount * 2][], 0, nameCount);
					}
					name[0] = Character.toLowerCase(name[0]);
					
					if(forArray) {
						int length = name.length;
						if (name[length-1] == 's'){
							System.arraycopy(name, 0, name = new char[length + 2], 0, length);
							name[length] = 'e';
							name[length+1] = 's';
						} else {
							System.arraycopy(name, 0, name = new char[length + 1], 0, length);
							name[length] = 's';
						}
					}					
					names[nameCount++] = name;
				}
			}
			previousIsUpperCase = isUpperCase;
		}
		if(nameCount == 0){
			char[] name = CharOperation.toLowerCase(sourceName);
			if(forArray) {
				int length = name.length;
				if (name[length-1] == 's'){
					System.arraycopy(name, 0, name = new char[length + 2], 0, length);
					name[length] = 'e';
					name[length+1] = 's';
				} else {
					System.arraycopy(name, 0, name = new char[length + 1], 0, length);
					name[length] = 's';
				}
			}					
			names[nameCount++] = name;
			
		}
		System.arraycopy(names, 0, names = new char[nameCount][], 0, nameCount);
		return names;
	}
	
	private char[] computePrefix(SourceTypeBinding declarationType, SourceTypeBinding invocationType, boolean isStatic){
		
		StringBuffer completion = new StringBuffer(10);

		if (isStatic) {
			completion.append(declarationType.sourceName());
			
		} else if (declarationType == invocationType) {
			completion.append(THIS);
			
		} else {
			
			if (!declarationType.isNestedType()) {
				
				completion.append(declarationType.sourceName());
				completion.append('.');
				completion.append(THIS);

			} else if (!declarationType.isAnonymousType()) {
				
				completion.append(declarationType.sourceName());
				completion.append('.');
				completion.append(THIS);
				
			}
		}
		
		return completion.toString().toCharArray();
	}
	
	private boolean isEnclosed(ReferenceBinding possibleEnclosingType, ReferenceBinding type){
		if(type.isNestedType()){
			ReferenceBinding enclosing = type.enclosingType();
			while(enclosing != null ){
				if(possibleEnclosingType == enclosing)
					return true;
				enclosing = enclosing.enclosingType();
			}
		}
		return false;
	}

}
