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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.FieldInfo;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IDocument;

public class BinaryIndexer extends AbstractIndexer {
	public static final String[] FILE_TYPES= new String[] {"class"}; //$NON-NLS-1$
	private static final char[] BYTE = "byte".toCharArray(); //$NON-NLS-1$
	private static final char[] CHAR = "char".toCharArray(); //$NON-NLS-1$
	private static final char[] DOUBLE = "double".toCharArray(); //$NON-NLS-1$
	private static final char[] FLOAT = "float".toCharArray(); //$NON-NLS-1$
	private static final char[] INT = "int".toCharArray(); //$NON-NLS-1$
	private static final char[] LONG = "long".toCharArray(); //$NON-NLS-1$
	private static final char[] SHORT = "short".toCharArray(); //$NON-NLS-1$
	private static final char[] BOOLEAN = "boolean".toCharArray(); //$NON-NLS-1$
	private static final char[] VOID = "void".toCharArray(); //$NON-NLS-1$
	private static final char[] INIT = "<init>".toCharArray(); //$NON-NLS-1$

	private boolean needReferences;
/**
 * BinaryIndexer constructor comment.
 */
public BinaryIndexer() {
	needReferences = false;
}
/**
 * BinaryIndexer constructor comment.
 */
public BinaryIndexer(boolean retrieveReferences) {
	needReferences = retrieveReferences;
}
/**
 * For example:
 *   - int foo(String[]) is ([Ljava/lang/String;)I => java.lang.String[] in a char[][]
 *   - void foo(int) is (I)V ==> int
 */
private void convertToArrayType(char[][] parameterTypes, int counter, int arrayDim) {
	int length = parameterTypes[counter].length;
	char[] arrayType = new char[length + arrayDim*2];
	System.arraycopy(parameterTypes[counter], 0, arrayType, 0, length);
	for (int i = 0; i < arrayDim; i++) {
		arrayType[length + (i * 2)] = '[';
		arrayType[length + (i * 2) + 1] = ']';
	}
	parameterTypes[counter] = arrayType;
}
/**
 * For example:
 *   - int foo(String[]) is ([Ljava/lang/String;)I => java.lang.String[] in a char[][]
 *   - void foo(int) is (I)V ==> int
 */
private char[] convertToArrayType(char[] typeName, int arrayDim) {
	int length = typeName.length;
	char[] arrayType = new char[length + arrayDim*2];
	System.arraycopy(typeName, 0, arrayType, 0, length);
	for (int i = 0; i < arrayDim; i++) {
		arrayType[length + (i * 2)] = '[';
		arrayType[length + (i * 2) + 1] = ']';
	}
	return arrayType;
}
private char[] decodeFieldType(char[] signature) throws ClassFormatException {
	if (signature == null) return null;
	int arrayDim = 0;
	for (int i = 0, max = signature.length; i < max; i++) {
		switch(signature[i]) {
			case 'B':
				if (arrayDim > 0) {
					return convertToArrayType(BYTE, arrayDim);
				} else {
					return BYTE;
				}
			case 'C':
				if (arrayDim > 0) {
					return convertToArrayType(CHAR, arrayDim);
				} else {
					return CHAR;
				}
			case 'D':
				if (arrayDim > 0) {
					return convertToArrayType(DOUBLE, arrayDim);
				} else {
					return DOUBLE;
				}
			case 'F':
				if (arrayDim > 0) {
					return convertToArrayType(FLOAT, arrayDim);
				} else {
					return FLOAT;
				}
			case 'I':
				if (arrayDim > 0) {
					return convertToArrayType(INT, arrayDim);
				} else {
					return INT;
				}
			case 'J':
				if (arrayDim > 0) {
					return convertToArrayType(LONG, arrayDim);
				} else {
					return LONG;
				}
			case 'L':
				int indexOfSemiColon = CharOperation.indexOf(';', signature, i+1);
				if (indexOfSemiColon == -1) throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
				if (arrayDim > 0) {
					return convertToArrayType(replace('/','.',CharOperation.subarray(signature, i + 1, indexOfSemiColon)), arrayDim);
				} else {
					return replace('/','.',CharOperation.subarray(signature, i + 1, indexOfSemiColon));
				}
			case 'S':
				if (arrayDim > 0) {
					return convertToArrayType(SHORT, arrayDim);
				} else {
					return SHORT;
				}
			case 'Z':
				if (arrayDim > 0) {
					return convertToArrayType(BOOLEAN, arrayDim);
				} else {
					return BOOLEAN;
				}
			case 'V':
				return VOID;
			case '[':
				arrayDim++;
				break;
			default:
				throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
		}
	}
	return null;
}
/**
 * For example:
 *   - int foo(String[]) is ([Ljava/lang/String;)I => java.lang.String[] in a char[][]
 *   - void foo(int) is (I)V ==> int
 */
private char[][] decodeParameterTypes(char[] signature) throws ClassFormatException {
	if (signature == null) return null;
	int indexOfClosingParen = CharOperation.lastIndexOf(')', signature);
	if (indexOfClosingParen == 1) {
		// there is no parameter
		return null;
	}
	if (indexOfClosingParen == -1) {
		throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
	}
	char[][] parameterTypes = new char[3][];
	int parameterTypesCounter = 0;
	int arrayDim = 0;
	for (int i = 1; i < indexOfClosingParen; i++) {
		if (parameterTypesCounter == parameterTypes.length) {
			// resize
			System.arraycopy(parameterTypes, 0, (parameterTypes = new char[parameterTypesCounter * 2][]), 0, parameterTypesCounter);
		}
		switch(signature[i]) {
			case 'B':
				parameterTypes[parameterTypesCounter++] = BYTE;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case 'C':
				parameterTypes[parameterTypesCounter++] = CHAR;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case 'D':
				parameterTypes[parameterTypesCounter++] = DOUBLE;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case 'F':
				parameterTypes[parameterTypesCounter++] = FLOAT;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case 'I':
				parameterTypes[parameterTypesCounter++] = INT;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case 'J':
				parameterTypes[parameterTypesCounter++] = LONG;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case 'L':
				int indexOfSemiColon = CharOperation.indexOf(';', signature, i+1);
				if (indexOfSemiColon == -1) throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
				parameterTypes[parameterTypesCounter++] = replace('/','.',CharOperation.subarray(signature, i + 1, indexOfSemiColon));
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				i = indexOfSemiColon;
				arrayDim = 0;
				break;
			case 'S':
				parameterTypes[parameterTypesCounter++] = SHORT;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case 'Z':
				parameterTypes[parameterTypesCounter++] = BOOLEAN;
				if (arrayDim > 0) {
					convertToArrayType(parameterTypes, parameterTypesCounter-1, arrayDim);
				}
				arrayDim = 0;
				break;
			case '[':
				arrayDim++;
				break;
			default:
				throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
		}
	}
	if (parameterTypes.length != parameterTypesCounter) {
		System.arraycopy(parameterTypes, 0, parameterTypes = new char[parameterTypesCounter][], 0, parameterTypesCounter);
	}
	return parameterTypes;
}
private char[] decodeReturnType(char[] signature) throws ClassFormatException {
	if (signature == null) return null;
	int indexOfClosingParen = CharOperation.lastIndexOf(')', signature);
	if (indexOfClosingParen == -1) throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
	int arrayDim = 0;
	for (int i = indexOfClosingParen + 1, max = signature.length; i < max; i++) {
		switch(signature[i]) {
			case 'B':
				if (arrayDim > 0) {
					return convertToArrayType(BYTE, arrayDim);
				} else {
					return BYTE;
				}
			case 'C':
				if (arrayDim > 0) {
					return convertToArrayType(CHAR, arrayDim);
				} else {
					return CHAR;
				}
			case 'D':
				if (arrayDim > 0) {
					return convertToArrayType(DOUBLE, arrayDim);
				} else {
					return DOUBLE;
				}
			case 'F':
				if (arrayDim > 0) {
					return convertToArrayType(FLOAT, arrayDim);
				} else {
					return FLOAT;
				}
			case 'I':
				if (arrayDim > 0) {
					return convertToArrayType(INT, arrayDim);
				} else {
					return INT;
				}
			case 'J':
				if (arrayDim > 0) {
					return convertToArrayType(LONG, arrayDim);
				} else {
					return LONG;
				}
			case 'L':
				int indexOfSemiColon = CharOperation.indexOf(';', signature, i+1);
				if (indexOfSemiColon == -1) throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
				if (arrayDim > 0) {
					return convertToArrayType(replace('/','.',CharOperation.subarray(signature, i + 1, indexOfSemiColon)), arrayDim);
				} else {
					return replace('/','.',CharOperation.subarray(signature, i + 1, indexOfSemiColon));
				}
			case 'S':
				if (arrayDim > 0) {
					return convertToArrayType(SHORT, arrayDim);
				} else {
					return SHORT;
				}
			case 'Z':
				if (arrayDim > 0) {
					return convertToArrayType(BOOLEAN, arrayDim);
				} else {
					return BOOLEAN;
				}
			case 'V':
				return VOID;
			case '[':
				arrayDim++;
				break;
			default:
				throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
		}
	}
	return null;
}
private int extractArgCount(char[] signature) throws ClassFormatException {
	int indexOfClosingParen = CharOperation.lastIndexOf(')', signature);
	if (indexOfClosingParen == 1) {
		// there is no parameter
		return 0;
	}
	if (indexOfClosingParen == -1) {
		throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
	}
	int parameterTypesCounter = 0;
	for (int i = 1; i < indexOfClosingParen; i++) {
		switch(signature[i]) {
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'S':
			case 'Z':
				parameterTypesCounter++;
				break;
			case 'L':
				int indexOfSemiColon = CharOperation.indexOf(';', signature, i+1);
				if (indexOfSemiColon == -1) throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
				parameterTypesCounter++;
				i = indexOfSemiColon;
				break;
			case '[':
				break;
			default:
				throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
		}
	}
	return parameterTypesCounter;
}
private final char[] extractClassName(int[] constantPoolOffsets, ClassFileReader reader, int index) {
	// the entry at i has to be a field ref or a method/interface method ref.
	int class_index = reader.u2At(constantPoolOffsets[index] + 1);
	int utf8Offset = constantPoolOffsets[reader.u2At(constantPoolOffsets[class_index] + 1)];
	return reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
}
private final char[] extractName(int[] constantPoolOffsets, ClassFileReader reader, int index) {
	int nameAndTypeIndex = reader.u2At(constantPoolOffsets[index] + 3);
	int utf8Offset = constantPoolOffsets[reader.u2At(constantPoolOffsets[nameAndTypeIndex] + 1)];
	return reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
}
private final char[] extractClassReference(int[] constantPoolOffsets, ClassFileReader reader, int index) {
	// the entry at i has to be a class ref.
	int utf8Offset = constantPoolOffsets[reader.u2At(constantPoolOffsets[index] + 1)];
	return reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
}
/**
 * Extract all type, method, field and interface method references from the constant pool
 */
private void extractReferenceFromConstantPool(byte[] contents, ClassFileReader reader) throws ClassFormatException {
	int[] constantPoolOffsets = reader.getConstantPoolOffsets();
	int constantPoolCount = constantPoolOffsets.length;
	for (int i = 1; i < constantPoolCount; i++) {
		int tag = reader.u1At(constantPoolOffsets[i]);
		/**
		 * u1 tag
		 * u2 class_index
		 * u2 name_and_type_index
		 */
		char[] name = null;
		char[] type = null;
		switch (tag) {
			case ClassFileStruct.FieldRefTag :
				// add reference to the class/interface and field name and type
				name = extractName(constantPoolOffsets, reader, i);
				addFieldReference(name);
				break;
			case ClassFileStruct.MethodRefTag :
				// add reference to the class and method name and type
			case ClassFileStruct.InterfaceMethodRefTag :
				// add reference to the interface and method name and type
				name = extractName(constantPoolOffsets, reader, i);
				type = extractType(constantPoolOffsets, reader, i);
				if (CharOperation.equals(INIT, name)) {
					// add a constructor reference
					char[] className = replace('/', '.', extractClassName(constantPoolOffsets, reader, i)); // so that it looks like java.lang.String
					addConstructorReference(className, extractArgCount(type));
				} else {
					// add a method reference
					addMethodReference(name, extractArgCount(type));
				}
				break;
			case ClassFileStruct.ClassTag :
				name = replace('/', '.', extractClassReference(constantPoolOffsets, reader, i)); // so that it looks like java.lang.String
				addTypeReference(name);
		}
	}
}
private final char[] extractType(int[] constantPoolOffsets, ClassFileReader reader, int index) {
	int constantPoolIndex = reader.u2At(constantPoolOffsets[index] + 3);
	int utf8Offset = constantPoolOffsets[reader.u2At(constantPoolOffsets[constantPoolIndex] + 3)];
	return reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
}
/**
 * getFileTypes method comment.
 */
public String[] getFileTypes() {
	return FILE_TYPES;
}
private void indexClassFile(byte[] contents, char[] documentName) throws IOException {
	try {
		ClassFileReader reader = new ClassFileReader(contents, documentName);

		// first add type references
		char[] className = replace('/', '.', reader.getName()); // looks like java/lang/String
		// need to extract the package name and the simple name
		int packageNameIndex = CharOperation.lastIndexOf('.', className);
		char[] packageName = null;
		char[] name = null;
		if (packageNameIndex >= 0) {
			packageName = CharOperation.subarray(className, 0, packageNameIndex);
			name = CharOperation.subarray(className, packageNameIndex + 1, className.length);
		} else {
			name = className;
		}
		char[] enclosingTypeName = null;
		if (reader.isNestedType()) {
			if (reader.isAnonymous()) {
				name = NO_CHAR;
			} else {
				name = reader.getInnerSourceName();
			}
			if (reader.isLocal() || reader.isAnonymous()) {
				enclosingTypeName = ONE_ZERO;
			} else {
				char[] fullEnclosingName = reader.getEnclosingTypeName();
				int nameLength = fullEnclosingName.length - packageNameIndex - 1;
				if (nameLength <= 0) {
					// See PR 1GIR345: ITPJCORE:ALL - Indexer: NegativeArraySizeException
					return;
				}
				enclosingTypeName = new char[nameLength]; 
				System.arraycopy(fullEnclosingName, packageNameIndex + 1, enclosingTypeName, 0, nameLength);
			}
		}
		// eliminate invalid innerclasses (1G4KCF7)
		if (name == null) return;
		
		char[][] superinterfaces = replace('/', '.', reader.getInterfaceNames());
		char[][] enclosingTypeNames = enclosingTypeName == null ? null : new char[][] {enclosingTypeName};
		if (reader.isInterface()) {
			addInterfaceDeclaration(reader.getModifiers(), packageName, name, enclosingTypeNames, superinterfaces);
		} else {
			char[] superclass = replace('/', '.', reader.getSuperclassName());
			addClassDeclaration(reader.getModifiers(), packageName, name, enclosingTypeNames, superclass, superinterfaces);
		}

		// first reference all methods declarations and field declarations
		MethodInfo[] methods = (MethodInfo[]) reader.getMethods();
		if (methods != null) {
			for (int i = 0, max = methods.length; i < max; i++) {
				MethodInfo method = methods[i];
				char[] descriptor = method.getMethodDescriptor();
				char[][] parameterTypes = decodeParameterTypes(descriptor);
				char[] returnType = decodeReturnType(descriptor);
				char[][] exceptionTypes = replace('/', '.', method.getExceptionTypeNames());
				if (method.isConstructor()) {
					addConstructorDeclaration(className, parameterTypes, exceptionTypes);
				} else {
					if (!method.isClinit()) {
						addMethodDeclaration(method.getSelector(), parameterTypes, returnType, exceptionTypes);
					}
				}
			}
		}
		FieldInfo[] fields = (FieldInfo[]) reader.getFields();
		if (fields != null) {
			for (int i = 0, max = fields.length; i < max; i++) {
				FieldInfo field = fields[i];
				char[] fieldName = field.getName();
				char[] fieldType = decodeFieldType(replace('/', '.', field.getTypeName()));
				addFieldDeclaration(fieldType, fieldName);
			}
		}

		// record all references found inside the .class file
		if (needReferences) {
			extractReferenceFromConstantPool(contents, reader);
		}
	} catch (ClassFormatException e) {
	}
}
/**
 * indexFile method comment.
 */
protected void indexFile(IDocument document) throws IOException {
	// Add the name of the file to the index
	output.addDocument(document);
	indexClassFile(document.getByteContent(), document.getName().toCharArray());
}
/**
 * Modify the array by replacing all occurences of toBeReplaced with newChar
 */
private char[][] replace(char toBeReplaced, char newChar, char[][] array) {
	if (array == null) return null;
	for (int i = 0, max = array.length; i < max; i++) {
		replace(toBeReplaced, newChar, array[i]);
	}
	return array;
}
/**
 * Modify the array by replacing all occurences of toBeReplaced with newChar
 */
private char[] replace(char toBeReplaced, char newChar, char[] array) {
	if (array == null) return null;
	for (int i = 0, max = array.length; i < max; i++) {
		if (array[i] == toBeReplaced) {
			array[i] = newChar;
		}
	}
	return array;
}
/**
 * setFileTypes method comment.
 */
public void setFileTypes(String[] fileTypes) {}
}