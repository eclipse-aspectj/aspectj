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
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ISearchPattern;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants; 

public abstract class SearchPattern implements ISearchPattern, IIndexConstants, IJavaSearchConstants {

	protected int matchMode;
	protected boolean isCaseSensitive;
	protected boolean needsResolve = true;

	/* match level */
	public static final int IMPOSSIBLE_MATCH = 0;
	public static final int POSSIBLE_MATCH = 1;
	public static final int ACCURATE_MATCH = 2;
	public static final int INACCURATE_MATCH = 3;

	/* match container */
	public static final int COMPILATION_UNIT = 1;
	public static final int CLASS = 2;
	public static final int FIELD = 4;
	public static final int METHOD = 8;
	


public SearchPattern(int matchMode, boolean isCaseSensitive) {
	this.matchMode = matchMode;
	this.isCaseSensitive = isCaseSensitive;
}
/**
 * Constructor pattern are formed by [declaringQualification.]type[(parameterTypes)]
 * e.g. java.lang.Object()
 *		Main(*)
 */
private static SearchPattern createConstructorPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	Scanner scanner = new Scanner(false, true); // tokenize white spaces
	scanner.setSource(patternString.toCharArray());
	final int InsideName = 1;
	final int InsideParameter = 2;
	
	String declaringQualification = null, typeName = null, parameterType = null;
	String[] parameterTypes = null;
	int parameterCount = -1;
	boolean foundClosingParenthesis = false;
	int mode = InsideName;
	int token;
	try {
		token = scanner.getNextToken();
	} catch (InvalidInputException e) {
		return null;
	}
	while (token != ITerminalSymbols.TokenNameEOF){
		switch(mode){

			// read declaring type and selector
			case InsideName :
				switch (token) {
					case ITerminalSymbols.TokenNameDOT:
						if (declaringQualification == null){
							if (typeName == null) return null;
							declaringQualification = typeName;
						} else {
							String tokenSource = new String(scanner.getCurrentTokenSource());
							declaringQualification += tokenSource + typeName;
						}
						typeName = null;
						break;
					case ITerminalSymbols.TokenNameLPAREN:
						parameterTypes = new String[5];
						parameterCount = 0;
						mode = InsideParameter;
						break;
					case Scanner.TokenNameWHITESPACE:
						break;
					case ITerminalSymbols.TokenNameIdentifier:
					case ITerminalSymbols.TokenNameMULTIPLY:
						if (typeName == null) {
							typeName = new String(scanner.getCurrentTokenSource());
						} else {
							typeName += new String(scanner.getCurrentTokenSource());
						}
						break;
					default:
						return null;
				}
				break;
			// read parameter types
			case InsideParameter :
				switch (token) {
					case Scanner.TokenNameWHITESPACE:
						break;
					case ITerminalSymbols.TokenNameCOMMA:
						if (parameterType == null) return null;
						if (parameterTypes.length == parameterCount){
							System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
						}
						parameterTypes[parameterCount++] = parameterType;
						parameterType = null;
						break;
					case ITerminalSymbols.TokenNameRPAREN:
						foundClosingParenthesis = true;
						if (parameterType != null){
							if (parameterTypes.length == parameterCount){
								System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
							}
							parameterTypes[parameterCount++] = parameterType;
						}
						break;
					case ITerminalSymbols.TokenNameDOT:
					case ITerminalSymbols.TokenNameIdentifier:
					case ITerminalSymbols.TokenNameMULTIPLY:
					case ITerminalSymbols.TokenNameLBRACKET:
					case ITerminalSymbols.TokenNameRBRACKET:
					case ITerminalSymbols.TokenNameboolean:
					case ITerminalSymbols.TokenNamebyte:
					case ITerminalSymbols.TokenNamechar:
					case ITerminalSymbols.TokenNamedouble:
					case ITerminalSymbols.TokenNamefloat:
					case ITerminalSymbols.TokenNameint:
					case ITerminalSymbols.TokenNamelong:
					case ITerminalSymbols.TokenNameshort:
					case ITerminalSymbols.TokenNamevoid:
						if (parameterType == null){
							parameterType = new String(scanner.getCurrentTokenSource());
						} else {
							parameterType += new String(scanner.getCurrentTokenSource());
						}
						break;
					default:
						return null;
				}
				break;
		}
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
	}
	// parenthesis mismatch
	if (parameterCount>0 && !foundClosingParenthesis) return null;
	if (typeName == null) return null;

	char[] typeNameChars = typeName.toCharArray();
	if (typeNameChars.length == 1 && typeNameChars[0] == '*') typeNameChars = null;
		
	char[] declaringQualificationChars = null;
	if (declaringQualification != null) declaringQualificationChars = declaringQualification.toCharArray();
	char[][] parameterTypeQualifications = null, parameterTypeSimpleNames = null;

	// extract parameter types infos
	if (parameterCount >= 0){
		parameterTypeQualifications = new char[parameterCount][];
		parameterTypeSimpleNames = new char[parameterCount][];
		for (int i = 0; i < parameterCount; i++){
			char[] parameterTypePart = parameterTypes[i].toCharArray();
			int lastDotPosition = CharOperation.lastIndexOf('.', parameterTypePart);
			if (lastDotPosition >= 0){
				parameterTypeQualifications[i] = CharOperation.subarray(parameterTypePart, 0, lastDotPosition);
				if (parameterTypeQualifications[i].length == 1 && parameterTypeQualifications[i][0] == '*') {
					parameterTypeQualifications[i] = null;
				} else {
					// prefix with a '*' as the full qualification could be bigger 
					// (i.e. because of an import)
					parameterTypeQualifications[i] = CharOperation.concat(ONE_STAR, parameterTypeQualifications[i]);
				}
				parameterTypeSimpleNames[i] = CharOperation.subarray(parameterTypePart, lastDotPosition+1, parameterTypePart.length);
			} else {
				parameterTypeQualifications[i] = null;
				parameterTypeSimpleNames[i] = parameterTypePart;
			}
			if (parameterTypeSimpleNames[i].length == 1 && parameterTypeSimpleNames[i][0] == '*') parameterTypeSimpleNames[i] = null;
		}
	}	
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = 
				new ConstructorDeclarationPattern(
					typeNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringQualificationChars, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = 
				new ConstructorReferencePattern(
					typeNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringQualificationChars, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					null);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new ConstructorDeclarationPattern(
					typeNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringQualificationChars, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames),
				new ConstructorReferencePattern(
					typeNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringQualificationChars, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					null));
			break;
	}
	return searchPattern;

}
/**
 * Field pattern are formed by [declaringType.]name[type]
 * e.g. java.lang.String.serialVersionUID long
 *		field*
 */
private static SearchPattern createFieldPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	Scanner scanner = new Scanner(false, true); // tokenize white spaces
	scanner.setSource(patternString.toCharArray());
	final int InsideDeclaringPart = 1;
	final int InsideType = 2;
	int lastToken = -1;
	
	String declaringType = null, fieldName = null;
	String type = null;
	int mode = InsideDeclaringPart;
	int token;
	try {
		token = scanner.getNextToken();
	} catch (InvalidInputException e) {
		return null;
	}
	while (token != ITerminalSymbols.TokenNameEOF){
		switch(mode){

			// read declaring type and fieldName
			case InsideDeclaringPart :
				switch (token) {
					case ITerminalSymbols.TokenNameDOT:
						if (declaringType == null){
							if (fieldName == null) return null;
							declaringType = fieldName;
						} else {
							String tokenSource = new String(scanner.getCurrentTokenSource());
							declaringType += tokenSource + fieldName;
						}
						fieldName = null;
						break;
					case Scanner.TokenNameWHITESPACE:
						if (!(Scanner.TokenNameWHITESPACE == lastToken 
							|| ITerminalSymbols.TokenNameDOT == lastToken)){
							mode = InsideType;
						}
						break;
					case ITerminalSymbols.TokenNameIdentifier:
					case ITerminalSymbols.TokenNameMULTIPLY:
						if (fieldName == null) {
							fieldName = new String(scanner.getCurrentTokenSource());
						} else {
							fieldName += new String(scanner.getCurrentTokenSource());
						}
						break;
					default:
						return null;
				}
				break;
			// read type 
			case InsideType:
				switch (token) {
					case Scanner.TokenNameWHITESPACE:
						break;
					case ITerminalSymbols.TokenNameDOT:
					case ITerminalSymbols.TokenNameIdentifier:
					case ITerminalSymbols.TokenNameMULTIPLY:
					case ITerminalSymbols.TokenNameLBRACKET:
					case ITerminalSymbols.TokenNameRBRACKET:
					case ITerminalSymbols.TokenNameboolean:
					case ITerminalSymbols.TokenNamebyte:
					case ITerminalSymbols.TokenNamechar:
					case ITerminalSymbols.TokenNamedouble:
					case ITerminalSymbols.TokenNamefloat:
					case ITerminalSymbols.TokenNameint:
					case ITerminalSymbols.TokenNamelong:
					case ITerminalSymbols.TokenNameshort:
					case ITerminalSymbols.TokenNamevoid:
						if (type == null){
							type = new String(scanner.getCurrentTokenSource());
						} else {
							type += new String(scanner.getCurrentTokenSource());
						}
						break;
					default:
						return null;
				}
				break;
		}
		lastToken = token;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
	}
	if (fieldName == null) return null;

	char[] fieldNameChars = fieldName.toCharArray();
	if (fieldNameChars.length == 1 && fieldNameChars[0] == '*') fieldNameChars = null;
		
	char[] declaringTypeQualification = null, declaringTypeSimpleName = null;
	char[] typeQualification = null, typeSimpleName = null;

	// extract declaring type infos
	if (declaringType != null){
		char[] declaringTypePart = declaringType.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', declaringTypePart);
		if (lastDotPosition >= 0){
			declaringTypeQualification = CharOperation.subarray(declaringTypePart, 0, lastDotPosition);
			if (declaringTypeQualification.length == 1 && declaringTypeQualification[0] == '*') declaringTypeQualification = null;
			declaringTypeSimpleName = CharOperation.subarray(declaringTypePart, lastDotPosition+1, declaringTypePart.length);
		} else {
			declaringTypeQualification = null;
			declaringTypeSimpleName = declaringTypePart;
		}
		if (declaringTypeSimpleName.length == 1 && declaringTypeSimpleName[0] == '*') declaringTypeSimpleName = null;
	}
	// extract type infos
	if (type != null){
		char[] typePart = type.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', typePart);
		if (lastDotPosition >= 0){
			typeQualification = CharOperation.subarray(typePart, 0, lastDotPosition);
			if (typeQualification.length == 1 && typeQualification[0] == '*') {
				typeQualification = null;
			} else {
				// prefix with a '*' as the full qualification could be bigger 
				// (i.e. because of an import)
				typeQualification = CharOperation.concat(ONE_STAR, typeQualification);
			}
			typeSimpleName = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
		} else {
			typeQualification = null;
			typeSimpleName = typePart;
		}
		if (typeSimpleName.length == 1 && typeSimpleName[0] == '*') typeSimpleName = null;
	}
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = 
				new FieldDeclarationPattern(
				fieldNameChars, 
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				typeQualification, 
				typeSimpleName);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = 
				new FieldReferencePattern(
					fieldNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					true, // read access
					true); // write access
			break;
		case IJavaSearchConstants.READ_ACCESSES :
			searchPattern = 
				new FieldReferencePattern(
					fieldNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					true, // read access only
					false);
			break;
		case IJavaSearchConstants.WRITE_ACCESSES :
			searchPattern = 
				new FieldReferencePattern(
					fieldNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					false,
					true); // write access only
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new FieldDeclarationPattern(
					fieldNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName),
				new FieldReferencePattern(
					fieldNameChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					true, // read access
					true)); // write access
			break;
	}
	return searchPattern;

}
/**
 * Method pattern are formed by [declaringType.]selector[(parameterTypes)][returnType]
 * e.g. java.lang.Runnable.run() void
 *		main(*)
 */
private static SearchPattern createMethodPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	Scanner scanner = new Scanner(false, true); // tokenize white spaces
	scanner.setSource(patternString.toCharArray());
	final int InsideSelector = 1;
	final int InsideParameter = 2;
	final int InsideReturnType = 3;
	int lastToken = -1;
	
	String declaringType = null, selector = null, parameterType = null;
	String[] parameterTypes = null;
	int parameterCount = -1;
	String returnType = null;
	boolean foundClosingParenthesis = false;
	int mode = InsideSelector;
	int token;
	try {
		token = scanner.getNextToken();
	} catch (InvalidInputException e) {
		return null;
	}
	while (token != ITerminalSymbols.TokenNameEOF){
		switch(mode){

			// read declaring type and selector
			case InsideSelector :
				switch (token) {
					case ITerminalSymbols.TokenNameDOT:
						if (declaringType == null){
							if (selector == null) return null;
							declaringType = selector;
						} else {
							String tokenSource = new String(scanner.getCurrentTokenSource());
							declaringType += tokenSource + selector;
						}
						selector = null;
						break;
					case ITerminalSymbols.TokenNameLPAREN:
						parameterTypes = new String[5];
						parameterCount = 0;
						mode = InsideParameter;
						break;
					case Scanner.TokenNameWHITESPACE:
						if (!(Scanner.TokenNameWHITESPACE == lastToken 
							|| ITerminalSymbols.TokenNameDOT == lastToken)){
							mode = InsideReturnType;
						}
						break;
					case ITerminalSymbols.TokenNameIdentifier:
					case ITerminalSymbols.TokenNameMULTIPLY:
						if (selector == null) {
							selector = new String(scanner.getCurrentTokenSource());
						} else {
							selector += new String(scanner.getCurrentTokenSource());
						}
						break;
					default:
						return null;
				}
				break;
			// read parameter types
			case InsideParameter :
				switch (token) {
					case Scanner.TokenNameWHITESPACE:
						break;
					case ITerminalSymbols.TokenNameCOMMA:
						if (parameterType == null) return null;
						if (parameterTypes.length == parameterCount){
							System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
						}
						parameterTypes[parameterCount++] = parameterType;
						parameterType = null;
						break;
					case ITerminalSymbols.TokenNameRPAREN:
						foundClosingParenthesis = true;
						if (parameterType != null){
							if (parameterTypes.length == parameterCount){
								System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
							}
							parameterTypes[parameterCount++] = parameterType;
						}
						mode = InsideReturnType;
						break;
					case ITerminalSymbols.TokenNameDOT:
					case ITerminalSymbols.TokenNameIdentifier:
					case ITerminalSymbols.TokenNameMULTIPLY:
					case ITerminalSymbols.TokenNameLBRACKET:
					case ITerminalSymbols.TokenNameRBRACKET:
					case ITerminalSymbols.TokenNameboolean:
					case ITerminalSymbols.TokenNamebyte:
					case ITerminalSymbols.TokenNamechar:
					case ITerminalSymbols.TokenNamedouble:
					case ITerminalSymbols.TokenNamefloat:
					case ITerminalSymbols.TokenNameint:
					case ITerminalSymbols.TokenNamelong:
					case ITerminalSymbols.TokenNameshort:
					case ITerminalSymbols.TokenNamevoid:
						if (parameterType == null){
							parameterType = new String(scanner.getCurrentTokenSource());
						} else {
							parameterType += new String(scanner.getCurrentTokenSource());
						}
						break;
					default:
						return null;
				}
				break;
			// read return type
			case InsideReturnType:
				switch (token) {
					case Scanner.TokenNameWHITESPACE:
						break;
					case ITerminalSymbols.TokenNameDOT:
					case ITerminalSymbols.TokenNameIdentifier:
					case ITerminalSymbols.TokenNameMULTIPLY:
					case ITerminalSymbols.TokenNameLBRACKET:
					case ITerminalSymbols.TokenNameRBRACKET:
					case ITerminalSymbols.TokenNameboolean:
					case ITerminalSymbols.TokenNamebyte:
					case ITerminalSymbols.TokenNamechar:
					case ITerminalSymbols.TokenNamedouble:
					case ITerminalSymbols.TokenNamefloat:
					case ITerminalSymbols.TokenNameint:
					case ITerminalSymbols.TokenNamelong:
					case ITerminalSymbols.TokenNameshort:
					case ITerminalSymbols.TokenNamevoid:
						if (returnType == null){
							returnType = new String(scanner.getCurrentTokenSource());
						} else {
							returnType += new String(scanner.getCurrentTokenSource());
						}
						break;
					default:
						return null;
				}
				break;
		}
		lastToken = token;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
	}
	// parenthesis mismatch
	if (parameterCount>0 && !foundClosingParenthesis) return null;
	if (selector == null) return null;

	char[] selectorChars = selector.toCharArray();
	if (selectorChars.length == 1 && selectorChars[0] == '*') selectorChars = null;
		
	char[] declaringTypeQualification = null, declaringTypeSimpleName = null;
	char[] returnTypeQualification = null, returnTypeSimpleName = null;
	char[][] parameterTypeQualifications = null, parameterTypeSimpleNames = null;

	// extract declaring type infos
	if (declaringType != null){
		char[] declaringTypePart = declaringType.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', declaringTypePart);
		if (lastDotPosition >= 0){
			declaringTypeQualification = CharOperation.subarray(declaringTypePart, 0, lastDotPosition);
			if (declaringTypeQualification.length == 1 && declaringTypeQualification[0] == '*') declaringTypeQualification = null;
			declaringTypeSimpleName = CharOperation.subarray(declaringTypePart, lastDotPosition+1, declaringTypePart.length);
		} else {
			declaringTypeQualification = null;
			declaringTypeSimpleName = declaringTypePart;
		}
		if (declaringTypeSimpleName.length == 1 && declaringTypeSimpleName[0] == '*') declaringTypeSimpleName = null;
	}
	// extract parameter types infos
	if (parameterCount >= 0){
		parameterTypeQualifications = new char[parameterCount][];
		parameterTypeSimpleNames = new char[parameterCount][];
		for (int i = 0; i < parameterCount; i++){
			char[] parameterTypePart = parameterTypes[i].toCharArray();
			int lastDotPosition = CharOperation.lastIndexOf('.', parameterTypePart);
			if (lastDotPosition >= 0){
				parameterTypeQualifications[i] = CharOperation.subarray(parameterTypePart, 0, lastDotPosition);
				if (parameterTypeQualifications[i].length == 1 && parameterTypeQualifications[i][0] == '*') {
					parameterTypeQualifications[i] = null;
				} else {
					// prefix with a '*' as the full qualification could be bigger 
					// (i.e. because of an import)
					parameterTypeQualifications[i] = CharOperation.concat(ONE_STAR, parameterTypeQualifications[i]);
				}
				parameterTypeSimpleNames[i] = CharOperation.subarray(parameterTypePart, lastDotPosition+1, parameterTypePart.length);
			} else {
				parameterTypeQualifications[i] = null;
				parameterTypeSimpleNames[i] = parameterTypePart;
			}
			if (parameterTypeSimpleNames[i].length == 1 && parameterTypeSimpleNames[i][0] == '*') parameterTypeSimpleNames[i] = null;
		}
	}	
	// extract return type infos
	if (returnType != null){
		char[] returnTypePart = returnType.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', returnTypePart);
		if (lastDotPosition >= 0){
			returnTypeQualification = CharOperation.subarray(returnTypePart, 0, lastDotPosition);
			if (returnTypeQualification.length == 1 && returnTypeQualification[0] == '*') {
				returnTypeQualification = null;
			} else {
				// (i.e. because of an import)
				returnTypeQualification = CharOperation.concat(ONE_STAR, returnTypeQualification);
			}			
			returnTypeSimpleName = CharOperation.subarray(returnTypePart, lastDotPosition+1, returnTypePart.length);
		} else {
			returnTypeQualification = null;
			returnTypeSimpleName = returnTypePart;
		}
		if (returnTypeSimpleName.length == 1 && returnTypeSimpleName[0] == '*') returnTypeSimpleName = null;
	}
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = 
				new MethodDeclarationPattern(
					selectorChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					returnTypeQualification, 
					returnTypeSimpleName, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = 
				new MethodReferencePattern(
					selectorChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					returnTypeQualification, 
					returnTypeSimpleName, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					null);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new MethodDeclarationPattern(
					selectorChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					returnTypeQualification, 
					returnTypeSimpleName, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames),
				new MethodReferencePattern(
					selectorChars, 
					matchMode, 
					isCaseSensitive, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					returnTypeQualification, 
					returnTypeSimpleName, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					null));
			break;
	}
	return searchPattern;

}
private static SearchPattern createPackagePattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = new PackageDeclarationPattern(patternString.toCharArray(), matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = new PackageReferencePattern(patternString.toCharArray(), matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new PackageDeclarationPattern(patternString.toCharArray(), matchMode, isCaseSensitive),
				new PackageReferencePattern(patternString.toCharArray(), matchMode, isCaseSensitive)
			);
			break;
	}
	return searchPattern;

}
public static SearchPattern createPattern(String patternString, int searchFor, int limitTo, int matchMode, boolean isCaseSensitive) {

	if (patternString == null || patternString.length() == 0)
		return null;

	SearchPattern searchPattern = null;
	switch (searchFor) {

		case IJavaSearchConstants.TYPE:
			searchPattern = createTypePattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.METHOD:
			searchPattern = createMethodPattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;			
		case IJavaSearchConstants.CONSTRUCTOR:
			searchPattern = createConstructorPattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;			
		case IJavaSearchConstants.FIELD:
			searchPattern = createFieldPattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.PACKAGE:
			searchPattern = createPackagePattern(patternString, limitTo, matchMode, isCaseSensitive);
	}
	return searchPattern;
}
public static SearchPattern createPattern(IJavaElement element, int limitTo) {
	SearchPattern searchPattern = null;
	int lastDot;
	switch (element.getElementType()) {
		case IJavaElement.FIELD :
			IField field = (IField) element; 
			String fullDeclaringName = field.getDeclaringType().getFullyQualifiedName().replace('$', '.');
			lastDot = fullDeclaringName.lastIndexOf('.');
			char[] declaringSimpleName = (lastDot != -1 ? fullDeclaringName.substring(lastDot + 1) : fullDeclaringName).toCharArray();
			char[] declaringQualification = lastDot != -1 ? fullDeclaringName.substring(0, lastDot).toCharArray() : NO_CHAR;
			char[] name = field.getElementName().toCharArray();
			char[] typeSimpleName;
			char[] typeQualification;
			try {
				String typeSignature = Signature.toString(field.getTypeSignature()).replace('$', '.');
				lastDot = typeSignature.lastIndexOf('.');
				typeSimpleName = (lastDot != -1 ? typeSignature.substring(lastDot + 1) : typeSignature).toCharArray();
				typeQualification = 
					lastDot != -1 ? 
						// prefix with a '*' as the full qualification could be bigger 
						// (i.e. because of an import)
						CharOperation.concat(ONE_STAR, typeSignature.substring(0, lastDot).toCharArray()) : 
						null;
			} catch (JavaModelException e) {
				return null;
			}
			switch (limitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					searchPattern = 
						new FieldDeclarationPattern(
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName);
					break;
				case IJavaSearchConstants.REFERENCES :
					searchPattern = 
						new FieldReferencePattern(
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName,
							true,  // read access
							true); // write access
					break;
				case IJavaSearchConstants.READ_ACCESSES :
					searchPattern = 
						new FieldReferencePattern(
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName,
							true,  // read access only
							false);
					break;
				case IJavaSearchConstants.WRITE_ACCESSES :
					searchPattern = 
						new FieldReferencePattern(
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName,
							false,
							true); // write access only
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					searchPattern = new OrPattern(
						new FieldDeclarationPattern(
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName), 
						new FieldReferencePattern(
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName,
							true,  // read access
							true)); // write access
					break;
			}
			break;
		case IJavaElement.IMPORT_DECLARATION :
			String elementName = element.getElementName();
			lastDot = elementName.lastIndexOf('.');
			if (lastDot == -1) return null; // invalid import declaration
			IImportDeclaration importDecl = (IImportDeclaration)element;
			if (importDecl.isOnDemand()) {
				searchPattern = createPackagePattern(elementName.substring(0, lastDot), limitTo, EXACT_MATCH, CASE_SENSITIVE);
			} else {
				searchPattern = 
					createTypePattern(
						elementName.substring(lastDot+1).toCharArray(),
						elementName.substring(0, lastDot).toCharArray(),
						null,
						limitTo);
			}
			break;
		case IJavaElement.METHOD :
			IMethod method = (IMethod) element;
			boolean isConstructor;
			try {
				isConstructor = method.isConstructor();
			} catch (JavaModelException e) {
				return null;
			}
			fullDeclaringName = method.getDeclaringType().getFullyQualifiedName().replace('$', '.');
			lastDot = fullDeclaringName.lastIndexOf('.');
			declaringSimpleName = (lastDot != -1 ? fullDeclaringName.substring(lastDot + 1) : fullDeclaringName).toCharArray();
			declaringQualification = lastDot != -1 ? fullDeclaringName.substring(0, lastDot).toCharArray() : NO_CHAR;
			char[] selector = method.getElementName().toCharArray();
			char[] returnSimpleName;
			char[] returnQualification;
			try {
				String returnType = Signature.toString(method.getReturnType()).replace('$', '.');
				lastDot = returnType.lastIndexOf('.');
				returnSimpleName = (lastDot != -1 ? returnType.substring(lastDot + 1) : returnType).toCharArray();
				returnQualification = 
					lastDot != -1 ? 
						// prefix with a '*' as the full qualification could be bigger 
						// (i.e. because of an import)
						CharOperation.concat(ONE_STAR, returnType.substring(0, lastDot).toCharArray()) : 
						null;
			} catch (JavaModelException e) {
				return null;
			}
			String[] parameterTypes = method.getParameterTypes();
			int paramCount = parameterTypes.length;
			char[][] parameterSimpleNames = new char[paramCount][];
			char[][] parameterQualifications = new char[paramCount][];
			for (int i = 0; i < paramCount; i++) {
				String signature = Signature.toString(parameterTypes[i]).replace('$', '.');
				lastDot = signature.lastIndexOf('.');
				parameterSimpleNames[i] = (lastDot != -1 ? signature.substring(lastDot + 1) : signature).toCharArray();
				parameterQualifications[i] = 
					lastDot != -1 ? 
						// prefix with a '*' as the full qualification could be bigger 
						// (i.e. because of an import)
						CharOperation.concat(ONE_STAR, signature.substring(0, lastDot).toCharArray()) : 
						null;
			}
			switch (limitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					if (isConstructor) {
						searchPattern = 
							new ConstructorDeclarationPattern(
								declaringSimpleName, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								parameterQualifications, 
								parameterSimpleNames);
					} else {
						searchPattern = 
							new MethodDeclarationPattern(
								selector, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								declaringSimpleName, 
								returnQualification, 
								returnSimpleName, 
								parameterQualifications, 
								parameterSimpleNames);
					}
					break;
				case IJavaSearchConstants.REFERENCES :
					if (isConstructor) {
						searchPattern = 
							new ConstructorReferencePattern(
								declaringSimpleName, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType());
					} else {
						searchPattern = 
							new MethodReferencePattern(
								selector, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								declaringSimpleName, 
								returnQualification, 
								returnSimpleName, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType());
					}
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					if (isConstructor) {
						searchPattern = new OrPattern(
							new ConstructorDeclarationPattern(
								declaringSimpleName, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								parameterQualifications, 
								parameterSimpleNames), 
							new ConstructorReferencePattern(
								declaringSimpleName, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType()));
					} else {
						searchPattern = new OrPattern(
							new MethodDeclarationPattern(
								selector, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								declaringSimpleName, 
								returnQualification, 
								returnSimpleName, 
								parameterQualifications, 
								parameterSimpleNames), 
							new MethodReferencePattern(
								selector, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								declaringSimpleName, 
								returnQualification, 
								returnSimpleName, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType()));
					}
					break;
			}
			break;
		case IJavaElement.TYPE :
			IType type = (IType)element;
			searchPattern = 
				createTypePattern(
					type.getElementName().toCharArray(), 
					type.getPackageFragment().getElementName().toCharArray(),
					enclosingTypeNames(type),
					limitTo);
			break;
		case IJavaElement.PACKAGE_DECLARATION :
		case IJavaElement.PACKAGE_FRAGMENT :
			searchPattern = createPackagePattern(element.getElementName(), limitTo, EXACT_MATCH, CASE_SENSITIVE);
			break;
	}
	return searchPattern;
}
private static SearchPattern createTypePattern(char[] simpleName, char[] packageName, char[][] enclosingTypeNames, int limitTo) {
	SearchPattern searchPattern = null;
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = 
				new TypeDeclarationPattern(
					packageName, 
					enclosingTypeNames, 
					simpleName, 
					TYPE_SUFFIX, 
					EXACT_MATCH, 
					CASE_SENSITIVE);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = 
				new TypeReferencePattern(
					CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
					simpleName, 
					EXACT_MATCH, 
					CASE_SENSITIVE);
			break;
		case IJavaSearchConstants.IMPLEMENTORS : 
			searchPattern = 
				new SuperInterfaceReferencePattern(
					CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
					simpleName, 
					EXACT_MATCH, 
					CASE_SENSITIVE);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new TypeDeclarationPattern(
					packageName, 
					enclosingTypeNames, 
					simpleName, 
					TYPE_SUFFIX, 
					EXACT_MATCH, 
					CASE_SENSITIVE), 
				new TypeReferencePattern(
					CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
					simpleName, 
					EXACT_MATCH, 
					CASE_SENSITIVE));
			break;
	}
	return searchPattern;
}
/**
 * Type pattern are formed by [qualification.]type
 * e.g. java.lang.Object
 *		Runnable
 *
 */
private static SearchPattern createTypePattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	Scanner scanner = new Scanner(false, true); // tokenize white spaces
	scanner.setSource(patternString.toCharArray());
	String type = null;
	int token;
	try {
		token = scanner.getNextToken();
	} catch (InvalidInputException e) {
		return null;
	}
	while (token != ITerminalSymbols.TokenNameEOF){
		switch (token) {
			case Scanner.TokenNameWHITESPACE:
				break;
			case ITerminalSymbols.TokenNameDOT:
			case ITerminalSymbols.TokenNameIdentifier:
			case ITerminalSymbols.TokenNameMULTIPLY:
			case ITerminalSymbols.TokenNameLBRACKET:
			case ITerminalSymbols.TokenNameRBRACKET:
			case ITerminalSymbols.TokenNameboolean:
			case ITerminalSymbols.TokenNamebyte:
			case ITerminalSymbols.TokenNamechar:
			case ITerminalSymbols.TokenNamedouble:
			case ITerminalSymbols.TokenNamefloat:
			case ITerminalSymbols.TokenNameint:
			case ITerminalSymbols.TokenNamelong:
			case ITerminalSymbols.TokenNameshort:
			case ITerminalSymbols.TokenNamevoid:
				if (type == null){
					type = new String(scanner.getCurrentTokenSource());
				} else {
					type += new String(scanner.getCurrentTokenSource());
				}
				break;
			default:
				return null;
		}
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
	}
	if (type == null) return null;

	char[] qualificationChars = null, typeChars = null;

	// extract declaring type infos
	if (type != null){
		char[] typePart = type.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', typePart);
		if (lastDotPosition >= 0){
			qualificationChars = CharOperation.subarray(typePart, 0, lastDotPosition);
			if (qualificationChars.length == 1 && qualificationChars[0] == '*') qualificationChars = null;
			typeChars = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
		} else {
			qualificationChars = null;
			typeChars = typePart;
		}
		if (typeChars.length == 1 && typeChars[0] == '*') typeChars = null;
	}
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS : // cannot search for explicit member types
			searchPattern = new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, TYPE_SUFFIX, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = new TypeReferencePattern(qualificationChars, typeChars, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.IMPLEMENTORS : 
			searchPattern = new SuperInterfaceReferencePattern(qualificationChars, typeChars, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, TYPE_SUFFIX, matchMode, isCaseSensitive),// cannot search for explicit member types
				new TypeReferencePattern(qualificationChars, typeChars, matchMode, isCaseSensitive));
			break;
	}
	return searchPattern;

}
protected abstract void decodeIndexEntry(IEntryResult entryResult);
/**
 * Returns the enclosing type names of the given type.
 */
private static char[][] enclosingTypeNames(IType type) {
	IJavaElement parent = type.getParent();
	switch (parent.getElementType()) {
		case IJavaElement.CLASS_FILE:
			// For a binary type, the parent is not the enclosing type, but the declaring type is.
			// (see bug 20532  Declaration of member binary type not found)
			IType declaringType = type.getDeclaringType();
			if (declaringType == null) {
				return NO_CHAR_CHAR;
			} else {
				return CharOperation.arrayConcat(
					enclosingTypeNames(declaringType), 
					declaringType.getElementName().toCharArray());
			}
		case IJavaElement.COMPILATION_UNIT:
			return 	NO_CHAR_CHAR;
		case IJavaElement.TYPE:
			return 	CharOperation.arrayConcat(
				enclosingTypeNames((IType)parent), 
				parent.getElementName().toCharArray());
		default:
			return null;
	}
}
/**
 * Feed the requestor according to the current search pattern
 */
public abstract void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope)  throws IOException ;
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IIndex index, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	IndexInput input = new BlocksIndexInput(index.getIndexFile());
	try {
		input.open();
		findIndexMatches(input, requestor, detailLevel, progressMonitor,scope);
	} finally {
		input.close();
	}
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	
	/* narrow down a set of entries using prefix criteria */
	IEntryResult[] entries = input.queryEntriesPrefixedBy(indexEntryPrefix());
	if (entries == null) return;
	
	/* only select entries which actually match the entire search pattern */
	for (int i = 0, max = entries.length; i < max; i++){

		if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

		/* retrieve and decode entry */	
		IEntryResult entry = entries[i];
		decodeIndexEntry(entry);
		if (matchIndexEntry()){
			feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), input, scope);
		}
	}
}
/**
 * Answers the suitable prefix that should be used in order
 * to query indexes for the corresponding item.
 * The more accurate the prefix and the less false hits will have
 * to be eliminated later on.
 */
public abstract char[] indexEntryPrefix();
/**
 * Check if the given ast node syntactically matches this pattern.
 * If it does, add it to the match set.
 */
protected void matchCheck(AstNode node, MatchSet set) {
	int matchLevel = this.matchLevel(node, false);
	switch (matchLevel) {
		case SearchPattern.POSSIBLE_MATCH:
			set.addPossibleMatch(node);
			break;
		case SearchPattern.ACCURATE_MATCH:
			set.addTrustedMatch(node);
	}
}

/**
 * Returns the type of container of this pattern, i.e. is it in compilation unit,
 * in class declarations, field declarations, or in method declarations.
 */
protected abstract int matchContainer();
/**
 * Finds out whether the given binary info matches this search pattern.
 * Default is to return false.
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	return false;
}
/**
 * Returns whether the given name matches the given pattern.
 */
protected boolean matchesName(char[] pattern, char[] name) {
	if (name != null){
		switch (this.matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(pattern, name, this.isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(pattern, name, this.isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(pattern, name, this.isCaseSensitive);
		}
	}
	return false;
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * and qualification pattern.
 */
protected boolean matchesType(char[] simpleNamePattern, char[] qualificationPattern, char[] fullyQualifiedTypeName) {
	char[] pattern;
	if (simpleNamePattern == null) {
		if (qualificationPattern == null) {
			pattern = ONE_STAR;
		} else {
			pattern = CharOperation.concat(qualificationPattern, ONE_STAR, '.');
		}
	} else {
		if (qualificationPattern == null) {
			pattern = CharOperation.concat(ONE_STAR, simpleNamePattern);
		} else {
			pattern = CharOperation.concat(qualificationPattern, simpleNamePattern, '.');
		}
	}
	return 
		CharOperation.match(
			pattern,
			fullyQualifiedTypeName,
			this.isCaseSensitive
		);
}
/**
 * Checks whether an entry matches the current search pattern
 */
protected abstract boolean matchIndexEntry();
/**
 * Report the match of the given import reference
 */
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	// default is to report a match as a regular ref.
	this.matchReportReference(importRef, element, accuracy, locator);
}
/**
 * Reports the match of the given reference.
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	// default is to report a match on the whole node.
	locator.report(reference.sourceStart, reference.sourceEnd, element, accuracy);
}

/**
 * Add square brackets to the given simple name
 */
protected char[] toArrayName(char[] simpleName, int dimensions) {
	if (dimensions == 0) return simpleName;
	int length = simpleName.length;
	char[] result = new char[length + dimensions * 2];
	System.arraycopy(simpleName, 0, result, 0, length);
	for (int i = 0; i < dimensions; i++) {
		result[simpleName.length + i*2] = '[';
		result[simpleName.length + i*2 + 1] = ']';
	}
	return result;
}
public String toString(){
	return "SearchPattern"; //$NON-NLS-1$
}







/**
 * Initializes this search pattern so that polymorphic search can be performed.
 */ 
public void initializePolymorphicSearch(MatchLocator locator, IProgressMonitor progressMonitor) {
	// default is to do nothing
}

/**
 * Finds out whether the given ast node matches this search pattern.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 * Returns POSSIBLE_MATCH if it potentially matches this search pattern 
 * and it has not been reolved, and it needs to be resolved to get more information.
 * Returns ACCURATE_MATCH if it matches exactly this search pattern (ie. 
 * it doesn't need to be resolved or it has already been resolved.)
 * Returns INACCURATE_MATCH if it potentially exactly this search pattern (ie. 
 * it has already been resolved but resolving failed.)
 */
public abstract int matchLevel(AstNode node, boolean resolve);

/**
 * Finds out whether the given binding matches this search pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed but match is still possible.
 * Retunrs IMPOSSIBLE_MATCH otherwise.
 * Default is to return INACCURATE_MATCH.
 */
public int matchLevel(Binding binding) {
	return INACCURATE_MATCH;
}

/**
 * Returns whether the given reference type binding matches or is a subtype of a type
 * that matches the given simple name pattern and qualification pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve fails
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int matchLevelAsSubtype(ReferenceBinding type, char[] simpleNamePattern, char[] qualificationPattern) {
	if (type == null) return INACCURATE_MATCH;
	
	int level;
	
	// matches type
	if ((level = this.matchLevelForType(simpleNamePattern, qualificationPattern, type)) != IMPOSSIBLE_MATCH)
		return level;
	
	// matches superclass
	if (!type.isInterface() && !CharOperation.equals(type.compoundName, TypeConstants.JAVA_LANG_OBJECT)) {
		if ((level = this.matchLevelAsSubtype(type.superclass(), simpleNamePattern, qualificationPattern)) != IMPOSSIBLE_MATCH) {
			return level;
		}
	}

	// matches interfaces
	ReferenceBinding[] interfaces = type.superInterfaces();
	if (interfaces == null) {
		return INACCURATE_MATCH;
	} else {
		for (int i = 0; i < interfaces.length; i++) {
			if ((level = this.matchLevelAsSubtype(interfaces[i], simpleNamePattern, qualificationPattern)) != IMPOSSIBLE_MATCH) {
				return level;
			};
		}
	}

	return IMPOSSIBLE_MATCH;
}

/**
 * Returns whether one of the given declaring types is the given receiver type.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int matchLevelForType(char[][][] declaringTypes, ReferenceBinding receiverType) {
	if (receiverType == null) return INACCURATE_MATCH;
	if (declaringTypes == null) {
		return INACCURATE_MATCH; // we were not able to compute the declaring types, default to inaccurate
	} else {
		for (int i = 0, max = declaringTypes.length; i < max; i++) {
			if (CharOperation.equals(declaringTypes[i], receiverType.compoundName)) {
				return ACCURATE_MATCH;
			}
		}
		return IMPOSSIBLE_MATCH;
	}
}

/**
 * Returns whether the given type binding matches the given simple name pattern 
 * and qualification pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int matchLevelForType(char[] simpleNamePattern, char[] qualificationPattern, TypeBinding type) {
	if (type == null) return INACCURATE_MATCH;
	char[] qualifiedPackageName = type.qualifiedPackageName();
	char[] qualifiedSourceName = 
		type instanceof LocalTypeBinding ?
			CharOperation.concat("1".toCharArray(), type.qualifiedSourceName(), '.') : //$NON-NLS-1$
			type.qualifiedSourceName();
	if (this.matchesType(
			simpleNamePattern, 
			qualificationPattern, 
			qualifiedPackageName.length == 0 ? 
				qualifiedSourceName : 
				CharOperation.concat(qualifiedPackageName, qualifiedSourceName, '.'))) {
		return ACCURATE_MATCH;
	} else {
		return IMPOSSIBLE_MATCH;
	}
}
}
