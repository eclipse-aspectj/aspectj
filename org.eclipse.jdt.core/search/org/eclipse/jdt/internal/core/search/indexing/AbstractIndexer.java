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
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IDocument;
import org.eclipse.jdt.internal.core.index.IIndexer;
import org.eclipse.jdt.internal.core.index.IIndexerOutput;

public abstract class AbstractIndexer implements IIndexer, IIndexConstants, IJavaSearchConstants {
	IIndexerOutput output;

public AbstractIndexer() {
	super();
}
public void addClassDeclaration(int modifiers, char[] packageName,char[] name,  char[][] enclosingTypeNames, char[] superclass, char[][] superinterfaces){

	this.output.addRef(encodeTypeEntry(packageName, enclosingTypeNames, name, true));
	
	addSuperTypeReference(modifiers, packageName, name, enclosingTypeNames, CLASS_SUFFIX, superclass, CLASS_SUFFIX);
	if (superinterfaces != null){
		for (int i = 0, max = superinterfaces.length; i < max; i++){
			addSuperTypeReference(modifiers, packageName, name, enclosingTypeNames, CLASS_SUFFIX, superinterfaces[i], INTERFACE_SUFFIX);			
		}
	}
	
}
public void addConstructorDeclaration(char[] typeName, char[][] parameterTypes, char[][] exceptionTypes){
	// Calculate the number of arguments of the constructor
	int numberOfArguments = 0;
	if (parameterTypes != null){
		numberOfArguments = parameterTypes.length;
		for (int i = 0; i < numberOfArguments; i++){
			this.addTypeReference(parameterTypes[i]);
		}
	}
	//convert the number of arguments into a char array
	char[] countChars;
	if (numberOfArguments < 10) {
		countChars = COUNTS[numberOfArguments];
	} else {
		countChars = String.valueOf(numberOfArguments).toCharArray();
	}
	//add the reference
	this.output.addRef(concat(CONSTRUCTOR_DECL, CharOperation.lastSegment(typeName,'.'), countChars, SEPARATOR));

	if (exceptionTypes != null){
		for (int i = 0, max = exceptionTypes.length; i < max; i++){
			this.addTypeReference(exceptionTypes[i]);
		}
	}
}
public void addConstructorReference(char[] typeName, int argCount){

	char[] countChars;
	if (argCount < 10) {
		countChars = COUNTS[argCount];
	} else {
		countChars = String.valueOf(argCount).toCharArray();
	}
	this.output.addRef(concat(CONSTRUCTOR_REF, CharOperation.lastSegment(typeName, '.'), countChars, SEPARATOR));
	
}
public void addFieldDeclaration(char[] typeName, char[] fieldName){
	this.output.addRef(CharOperation.concat(FIELD_DECL, fieldName));
	this.addTypeReference(typeName);
}
public void addFieldReference(char[] fieldName){
	this.output.addRef(CharOperation.concat(FIELD_REF, fieldName));	
}
public void addInterfaceDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, char[][] superinterfaces){

	this.output.addRef(encodeTypeEntry(packageName, enclosingTypeNames, name, false));
	
	if (superinterfaces != null){
		for (int i = 0, max = superinterfaces.length; i < max; i++){
			addSuperTypeReference(modifiers, packageName, name, enclosingTypeNames, INTERFACE_SUFFIX, superinterfaces[i], INTERFACE_SUFFIX);			
		}
	}
	/* if willing to connect interfaces to Object as a supertype, then uncomment the following 
	else {
		addSuperTypeReference(modifiers, packageName, name, INTERFACE_SUFFIX, null, CLASS_SUFFIX); // extends Object by default
	}
	*/
}
public void addMethodDeclaration(char[] methodName, char[][] parameterTypes, char[] returnType, char[][] exceptionTypes){
	// Calculate the number of arguments of the method
	int numberOfArguments = 0;
	if (parameterTypes != null){
		numberOfArguments = parameterTypes.length;
		for (int i = 0; i < numberOfArguments; i++){
			this.addTypeReference(parameterTypes[i]);
		}
	}
	//convert the number of arguments into a char array
	char[] countChars;
	if (numberOfArguments < 10) {
		countChars = COUNTS[numberOfArguments];
	} else {
		countChars = String.valueOf(numberOfArguments).toCharArray();
	}
	//add the reference
	this.output.addRef(concat(METHOD_DECL, methodName, countChars, SEPARATOR));

	if (exceptionTypes != null){
		for (int i = 0, max = exceptionTypes.length; i < max; i++){
			this.addTypeReference(exceptionTypes[i]);
		}
	}
	if (returnType != null) this.addTypeReference(returnType);
}
public void addMethodReference(char[] methodName, int argCount){
	char[] countChars;
	if (argCount < 10) {
		countChars = COUNTS[argCount];
	} else {
		countChars = String.valueOf(argCount).toCharArray();
	}
	this.output.addRef(concat(METHOD_REF, methodName, countChars, SEPARATOR));
	
}
public void addNameReference(char[] name){
	this.output.addRef(CharOperation.concat(REF, name));
}
private void addSuperTypeReference(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, char classOrInterface, char[] superTypeName, char superClassOrInterface){

	if (superTypeName == null) superTypeName = OBJECT;

	char[] enclosingTypeName = CharOperation.concatWith(enclosingTypeNames, '$');
	char[] typeSimpleName = CharOperation.lastSegment(typeName, '.');
	char[] superTypeSimpleName = CharOperation.lastSegment(superTypeName, '.');
	char[] superQualification;
	if (superTypeSimpleName == superTypeName){
		superQualification = null;
	} else {
		int length = superTypeName.length - superTypeSimpleName.length - 1;
		System.arraycopy(superTypeName, 0, superQualification = new char[length], 0, length);
	}
	// if the supertype name contains a $, then split it into: source name and append the $ prefix to the qualification
	//	e.g. p.A$B ---> p.A$ + B
	char[] superTypeSourceName = CharOperation.lastSegment(superTypeSimpleName, '$');
	if (superTypeSourceName != superTypeSimpleName){
		int start = superQualification == null ? 0 : superQualification.length+1;
		int prefixLength = superTypeSimpleName.length - superTypeSourceName.length;
		char[] mangledQualification = new char[start + prefixLength];
		if (superQualification != null){
			System.arraycopy(superQualification, 0, mangledQualification, 0, start-1);
			mangledQualification[start-1] = '.';
		}
		System.arraycopy(superTypeSimpleName, 0, mangledQualification, start, prefixLength);
		superQualification = mangledQualification;
		superTypeSimpleName = superTypeSourceName;
	} 
	this.output.addRef(concat(SUPER_REF, superTypeSimpleName, superQualification, superClassOrInterface, typeSimpleName, enclosingTypeName, packageName, classOrInterface, (char)modifiers, SEPARATOR));		
}
public void addTypeReference(char[] typeName){

	this.output.addRef(CharOperation.concat(TYPE_REF, CharOperation.lastSegment(typeName, '.')));
}
/**
 * Constructor declaration entries are encoded as follow: 'constructorDecl/' TypeName '/' Arity:
 * 	e.g. &nbsp;constructorDecl/X/0&nbsp;constructorDecl/Y/1
 *
 */
 public static final char[] bestConstructorDeclarationPrefix(char[] typeName, int arity, int matchMode, boolean isCaseSensitive) {

	if (!isCaseSensitive || typeName == null) return CONSTRUCTOR_DECL;
	switch(matchMode){
		case EXACT_MATCH :
			if (arity >= 0){
				char[] countChars;
				if (arity < 10) {
					countChars = COUNTS[arity];
				} else {
					countChars = String.valueOf(arity).toCharArray();
				}
				return concat(CONSTRUCTOR_DECL, typeName, countChars, SEPARATOR);
			}
		case PREFIX_MATCH :
			return CharOperation.concat(CONSTRUCTOR_DECL, typeName);
		case PATTERN_MATCH :
			int starPos = CharOperation.indexOf('*', typeName);
			switch(starPos) {
				case -1 :
					return CharOperation.concat(CONSTRUCTOR_DECL, typeName);
				default : 
					int refLength = CONSTRUCTOR_DECL.length;
					char[] result = new char[refLength+starPos];
					System.arraycopy(CONSTRUCTOR_DECL, 0, result, 0, refLength);
					System.arraycopy(typeName, 0, result, refLength, starPos);
					return result;
				case 0 : // fall through
			}
		default:
			return CONSTRUCTOR_DECL;
	}
}
/**
 * Constructor reference entries are encoded as follow: 'constructorRef/' TypeName '/' Arity:
 * 	e.g.&nbsp;constructorRef/X/0&nbsp;constructorRef/Y/1
 *
 */
 public static final char[] bestConstructorReferencePrefix(char[] typeName, int arity, int matchMode, boolean isCaseSensitive) {

	if (!isCaseSensitive || typeName == null) return CONSTRUCTOR_REF;
	switch(matchMode){
		case EXACT_MATCH :
			if (arity >= 0){
				char[] countChars;
				if (arity < 10) {
					countChars = COUNTS[arity];
				} else {
					countChars = String.valueOf(arity).toCharArray();
				}
				return concat(CONSTRUCTOR_REF, typeName, countChars, SEPARATOR);
			}
		case PREFIX_MATCH :
			return CharOperation.concat(CONSTRUCTOR_REF, typeName);
		case PATTERN_MATCH :
			int starPos = CharOperation.indexOf('*', typeName);
			switch(starPos) {
				case -1 :
					return CharOperation.concat(CONSTRUCTOR_REF, typeName);
				default : 
					int refLength = CONSTRUCTOR_REF.length;
					char[] result = new char[refLength+starPos];
					System.arraycopy(CONSTRUCTOR_REF, 0, result, 0, refLength);
					System.arraycopy(typeName, 0, result, refLength, starPos);
					return result;
				case 0 : // fall through
			}
		default:
			return CONSTRUCTOR_REF;
	}
}
/**
 * Method declaration entries are encoded as follow: 'fieldDecl/' Name
 * 	e.g.&nbsp;fieldDecl/x
 *
 */
 public static final char[] bestFieldDeclarationPrefix(char[] name, int matchMode, boolean isCaseSensitive) {

	if (!isCaseSensitive || name == null) return FIELD_DECL;
	switch(matchMode){
		case EXACT_MATCH :
		case PREFIX_MATCH :
			return CharOperation.concat(FIELD_DECL, name);
		case PATTERN_MATCH :
			int starPos = CharOperation.indexOf('*', name);
			switch(starPos) {
				case -1 :
					return CharOperation.concat(FIELD_DECL, name);
				default : 
					int refLength = FIELD_DECL.length;
					char[] result = new char[refLength+starPos];
					System.arraycopy(FIELD_DECL, 0, result, 0, refLength);
					System.arraycopy(name, 0, result, refLength, starPos);
					return result;
				case 0 : // fall through
			}
		default:
			return FIELD_DECL;
	}
}
/**
 * Method declaration entries are encoded as follow: 'methodDecl/' Selector '/' Arity
 * 	e.g.&nbsp;methodDecl/clone/0&nbsp;methodDecl/append/1
 *
 */
 public static final char[] bestMethodDeclarationPrefix(char[] selector, int arity, int matchMode, boolean isCaseSensitive) {

	if (!isCaseSensitive || selector == null) return METHOD_DECL;
	switch(matchMode){
		case EXACT_MATCH :
			if (arity >= 0){
				char[] countChars;
				if (arity < 10) {
					countChars = COUNTS[arity];
				} else {
					countChars = String.valueOf(arity).toCharArray();
				}
				return concat(METHOD_DECL, selector, countChars, SEPARATOR);
			}
		case PREFIX_MATCH :
			return CharOperation.concat(METHOD_DECL, selector);
		case PATTERN_MATCH :
			int starPos = CharOperation.indexOf('*', selector);
			switch(starPos) {
				case -1 :
					return CharOperation.concat(METHOD_DECL, selector);
				default : 
					int refLength = METHOD_DECL.length;
					char[] result = new char[refLength+starPos];
					System.arraycopy(METHOD_DECL, 0, result, 0, refLength);
					System.arraycopy(selector, 0, result, refLength, starPos);
					return result;
				case 0 : // fall through
			}
		default:
			return METHOD_DECL;
	}
}
/**
 * Method reference entries are encoded as follow: 'methodRef/' Selector '/' Arity
 * 	e.g.&nbsp;methodRef/clone/0&nbsp;methodRef/append/1
 *
 */
 public static final char[] bestMethodReferencePrefix(char[] selector, int arity, int matchMode, boolean isCaseSensitive) {

	if (!isCaseSensitive || selector == null) return METHOD_REF;
	switch(matchMode){
		case EXACT_MATCH :
			if (arity >= 0){
				char[] countChars;
				if (arity < 10) {
					countChars = COUNTS[arity];
				} else {
					countChars = String.valueOf(arity).toCharArray();
				}
				return concat(METHOD_REF, selector, countChars, SEPARATOR);
			}
		case PREFIX_MATCH :
			return CharOperation.concat(METHOD_REF, selector);
		case PATTERN_MATCH :
			int starPos = CharOperation.indexOf('*', selector);
			switch(starPos) {
				case -1 :
					return CharOperation.concat(METHOD_REF, selector);
				default : 
					int refLength = METHOD_REF.length;
					char[] result = new char[refLength+starPos];
					System.arraycopy(METHOD_REF, 0, result, 0, refLength);
					System.arraycopy(selector, 0, result, refLength, starPos);
					return result;
				case 0 : // fall through
			}
		default:
			return METHOD_REF;
	}
}
/**
 * Type entries are encoded as follow: '<tag>/' Name 
 * 	e.g.&nbsp;ref/Object&nbsp;ref/x
 */
 public static final char[] bestReferencePrefix(char[] tag, char[] name, int matchMode, boolean isCaseSensitive) {

	if (!isCaseSensitive || name == null) return tag;
	switch(matchMode){
		case EXACT_MATCH :
		case PREFIX_MATCH :
			return CharOperation.concat(tag, name);
		case PATTERN_MATCH :
			int starPos = CharOperation.indexOf('*', name);
			switch(starPos) {
				case -1 :
					return CharOperation.concat(tag, name);
				default : 
					int refLength = tag.length;
					char[] result = new char[refLength+starPos];
					System.arraycopy(tag, 0, result, 0, refLength);
					System.arraycopy(name, 0, result, refLength, starPos);
					return result;
				case 0 : // fall through
			}
		default:
			return tag;
	}
}
/**
 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'I') '/' PackageName '/' TypeName:
 * 	e.g.&nbsp;typeDecl/C/java.lang/Object&nbsp;typeDecl/I/java.lang/Cloneable
 *
 * Current encoding is optimized for queries: all classes/interfaces
 */
 public static final char[] bestTypeDeclarationPrefix(char[] packageName, char[] typeName, char classOrInterface, int matchMode, boolean isCaseSensitive) {
	// index is case sensitive, thus in case attempting case insensitive search, cannot consider
	// type name.
	if (!isCaseSensitive){
		packageName = null;
		typeName = null;
	}
	switch(classOrInterface){ 
		default :
			return TYPE_DECL; // cannot do better given encoding
		case CLASS_SUFFIX :
			if (packageName == null) return CLASS_DECL;
			break;
		case INTERFACE_SUFFIX :
			if (packageName == null) return INTERFACE_DECL;
			break;
	}
	switch(matchMode){
		case EXACT_MATCH :
		case PREFIX_MATCH :
			break;
		case PATTERN_MATCH :
			if (typeName != null){
				int starPos = CharOperation.indexOf('*', typeName);
				switch(starPos) {
					case -1 :
						break;
					case 0 :
						typeName = null;
						break;
					default : 
						typeName = CharOperation.subarray(typeName, 0, starPos);
				}
			}
	}
	int packageLength = packageName.length;
	int typeLength = typeName == null ? 0 : typeName.length;
	int pos;
	char[] result = new char[TYPE_DECL_LENGTH + packageLength + typeLength + 3];
	System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
	result[pos++] = classOrInterface;
	result[pos++] = SEPARATOR;
	System.arraycopy(packageName, 0, result, pos, packageLength);
	pos += packageLength;
	result[pos++] = SEPARATOR;
	if (typeLength > 0){
		System.arraycopy(typeName, 0, result, pos, typeName.length);
	}
	return result;
}
/**
 * Concat(first, second, third, fourth, fifth, sep) --> [first][second][sep][third][sep][fourth][sep][fifth]
 * i.e. no separator is inserted in between first and second
 */
protected static final char[] concat(char[] firstWithSeparator, char[] second, char[] third, char[] fourth, char[] fifth, char separator) {
	int length1= firstWithSeparator.length;
	int length2= second == null ? 0 : second.length;
	int length3= third == null ? 0 : third.length;
	int length4= fourth == null ? 0 : fourth.length;
	int length5= fifth == null ? 0 : fifth.length;
	char[] result= new char[length1 + length2 + length3 + length4 + length5 + 3 ];
	System.arraycopy(firstWithSeparator, 0, result, 0, length1);
	if (second != null) System.arraycopy(second, 0, result, length1 , length2);
	int pos = length1 + length2;
	result[pos]= separator;
	if (third != null) System.arraycopy(third, 0, result, pos + 1, length3);
	pos += length3+1;
	result[pos]= separator;
	if (fourth != null) System.arraycopy(fourth, 0, result, pos + 1, length4);
	pos += length4+1;
	result[pos]= separator;
	if (fifth != null) System.arraycopy(fifth, 0, result, pos + 1, length5);
	return result;
}
/**
 * Concat(first, second, third, sep) --> [first][second][sep][third]
 * i.e. no separator is inserted in between first and second
 */
protected static final char[] concat(char[] firstWithSeparator, char[] second, char[] third, char separator) {
	int length1= firstWithSeparator.length;
	int length2= second == null ? 0 : second.length;
	int length3= third == null ? 0 : third.length;
	char[] result= new char[length1 + length2 + length3 + 1];
	System.arraycopy(firstWithSeparator, 0, result, 0, length1);
	if (second != null) System.arraycopy(second, 0, result, length1 , length2);
	result[length1 + length2]= separator;
	if (third != null) System.arraycopy(third, 0, result, length1 + length2 + 1, length3);
	return result;
}
/**
 * Concat(first, second, third, charAfterThird, fourth, fifth, sixth, charAfterSixth, last, sep) --> [first][second][sep][third][sep][charAfterThird][sep][fourth][sep][fifth][sep][sixth][sep][charAfterSixth][last]
 * i.e. no separator is inserted in between first and second
 */
protected static final char[] concat(char[] firstWithSeparator, char[] second, char[] third, char charAfterThird, char[] fourth, char[] fifth, char[] sixth, char charAfterSixth, char last, char separator) {
	int length1= firstWithSeparator.length;
	int length2= second == null ? 0 : second.length;
	int length3= third == null ? 0 : third.length;
	int length4= fourth == null ? 0 : fourth.length;
	int length5= fifth == null ? 0 : fifth.length;
	int length6 = sixth == null ? 0 : sixth.length;
	char[] result= new char[length1 + length2 + length3 + length4 + length5 + length6 + 9 ];
	System.arraycopy(firstWithSeparator, 0, result, 0, length1);
	if (second != null) System.arraycopy(second, 0, result, length1 , length2);
	int pos = length1 + length2;
	result[pos]= separator;
	if (third != null) System.arraycopy(third, 0, result, pos + 1, length3);
	pos += length3+1;
	result[pos]= separator;
	result[++pos] = charAfterThird;
	result[++pos] = separator;
	if (fourth != null) System.arraycopy(fourth, 0, result, pos + 1, length4);
	pos += length4+1;
	result[pos]= separator;
	if (fifth != null) System.arraycopy(fifth, 0, result, pos + 1, length5);
	pos += length5+1;
	result[pos]= separator;
	if (sixth != null) System.arraycopy(sixth, 0, result, pos + 1, length6);
	pos += length6+1;
	result[pos]= separator;
	result[++pos] = charAfterSixth;
	result[++pos]=last;
	return result;
}
/**
 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'I') '/' PackageName '/' TypeName '/' EnclosingTypeName
 * 	e.g.<ul>
 * 	<li>typeDecl/C/java.lang/Object/</li>
 *	<li>typeDecl/I/java.lang/Cloneable/</li>
 *	<li>typeDecl/C/javax.swing/LazyValue/UIDefaults</li>
 * Current encoding is optimized for queries: all classes/interfaces
 */
 protected static final char[] encodeTypeEntry(char[] packageName, char[][] enclosingTypeNames, char[] typeName, boolean isClass) {
	int packageLength = packageName == null ? 0 : packageName.length;
	int enclosingTypeNamesLength = 0;
	if (enclosingTypeNames != null) {
		for (int i = 0, length = enclosingTypeNames.length; i < length; i++){
			enclosingTypeNamesLength += enclosingTypeNames[i].length + 1;
		}
	}
	int pos;
	char[] result = new char[TYPE_DECL_LENGTH + packageLength + typeName.length + enclosingTypeNamesLength + 4];
	System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
	result[pos++] = isClass ? CLASS_SUFFIX : INTERFACE_SUFFIX;
	result[pos++] = SEPARATOR;
	if (packageName != null){
		System.arraycopy(packageName, 0, result, pos, packageLength);
		pos += packageLength;
	}
	result[pos++] = SEPARATOR;
	System.arraycopy(typeName, 0, result, pos, typeName.length);
	pos += typeName.length;
	result[pos++] = SEPARATOR;
	if (enclosingTypeNames != null){
		for (int i = 0, length = enclosingTypeNames.length; i < length; i++){
			int enclosingTypeNameLength = enclosingTypeNames[i].length;
			System.arraycopy(enclosingTypeNames[i], 0, result, pos, enclosingTypeNameLength);
			pos += enclosingTypeNameLength;
			result[pos++] = SEPARATOR;
		}
	}
	return result;
}
/**
 * Returns the file types the <code>IIndexer</code> handles.
 */

public abstract String[] getFileTypes();
/**
 * @see IIndexer#index(IDocument document, IIndexerOutput output)
 */
public void index(IDocument document, IIndexerOutput output) throws IOException {
	this.output = output;
	if (shouldIndex(document)) indexFile(document);
}
protected abstract void indexFile(IDocument document) throws IOException;
/**
 * @see IIndexer#shouldIndex(IDocument document)
 */
public boolean shouldIndex(IDocument document) {
	String type = document.getType();
	String[] supportedTypes = this.getFileTypes();
	for (int i = 0; i < supportedTypes.length; ++i) {
		if (supportedTypes[i].equals(type))
			return true;
	}
	return false;
}
}
