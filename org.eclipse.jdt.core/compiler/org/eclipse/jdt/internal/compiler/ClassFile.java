/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import java.io.*;
import java.util.*;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

// AspectJ - Added minimal support for extensible attributes
/**
 * Represents a class file wrapper on bytes, it is aware of its actual
 * type name.
 *
 * Public APIs are listed below:
 *
 * byte[] getBytes();
 *		Answer the actual bytes of the class file
 *
 * char[][] getCompoundName();
 * 		Answer the compound name of the class file.
 * 		For example, {{java}, {util}, {Hashtable}}.
 *
 * byte[] getReducedBytes();
 * 		Answer a smaller byte format, which is only contains some structural 
 *      information. Those bytes are decodable with a regular class file reader, 
 *      such as DietClassFileReader
 */
public class ClassFile
	implements AttributeNamesConstants, CompilerModifiers, TypeConstants, TypeIds {
	public SourceTypeBinding referenceBinding;
	public ConstantPool constantPool;
	public ClassFile enclosingClassFile;
	// used to generate private access methods
	public int produceDebugAttributes;
	public ReferenceBinding[] innerClassesBindings;
	public int numberOfInnerClasses;
	public byte[] header;
	// the header contains all the bytes till the end of the constant pool
	public byte[] contents;
	// that collection contains all the remaining bytes of the .class file
	public int headerOffset;
	public int contentsOffset;
	public int constantPoolOffset;
	public int methodCountOffset;
	public int methodCount;
	protected boolean creatingProblemType;
	public static final int INITIAL_CONTENTS_SIZE = 1000;
	public static final int INITIAL_HEADER_SIZE = 1000;
	public static final int INCREMENT_SIZE = 1000;
	public static final int INNER_CLASSES_SIZE = 5;
	protected HashtableOfType nameUsage;
	public CodeStream codeStream;
	protected int problemLine;	// used to create line number attributes for problem methods

	public List/*<IAttribute>*/ extraAttributes = new ArrayList(1);

	/**
	 * INTERNAL USE-ONLY
	 * This methods creates a new instance of the receiver.
	 */
	public ClassFile() {
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods creates a new instance of the receiver.
	 *
	 * @param aType org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding
	 * @param enclosingClassFile org.eclipse.jdt.internal.compiler.ClassFile
	 * @param creatingProblemType <CODE>boolean</CODE>
	 */
	public ClassFile(
		SourceTypeBinding aType,
		ClassFile enclosingClassFile,
		boolean creatingProblemType) {
		referenceBinding = aType;
		header = new byte[INITIAL_HEADER_SIZE];
		// generate the magic numbers inside the header
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 24);
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 16);
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 8);
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 0);
		switch(((SourceTypeBinding) referenceBinding).scope.environment().options.targetJDK) {
			case CompilerOptions.JDK1_4 :
				// Compatible with JDK 1.4
				header[headerOffset++] = 0;
				header[headerOffset++] = 0;
				header[headerOffset++] = 0;
				header[headerOffset++] = 48;
				break;
			case CompilerOptions.JDK1_3 :
				// Compatible with JDK 1.3
				header[headerOffset++] = 0;
				header[headerOffset++] = 0;
				header[headerOffset++] = 0;
				header[headerOffset++] = 47;
				break;
			case CompilerOptions.JDK1_2 :
				// Compatible with JDK 1.2
				header[headerOffset++] = 0;
				header[headerOffset++] = 0;
				header[headerOffset++] = 0;
				header[headerOffset++] = 46;
				break;
			case CompilerOptions.JDK1_1 :
				// Compatible with JDK 1.1
				header[headerOffset++] = 0;
				header[headerOffset++] = 3;
				header[headerOffset++] = 0;
				header[headerOffset++] = 45;
		}
		constantPoolOffset = headerOffset;
		headerOffset += 2;
		constantPool = new ConstantPool(this);
		
		// Modifier manipulations for classfile
		int accessFlags = aType.getAccessFlags();
		if (aType.isPrivate()) { // rewrite private to non-public
			accessFlags &= ~AccPublic;
		}
		if (aType.isProtected()) { // rewrite protected into public
			accessFlags |= AccPublic;
		}
		// clear all bits that are illegal for a class or an interface
		accessFlags
			&= ~(
				AccStrictfp
					| AccProtected
					| AccPrivate
					| AccStatic
					| AccSynchronized
					| AccNative);
					
		// set the AccSuper flag (has to be done after clearing AccSynchronized - since same value)
		accessFlags |= AccSuper;
		
		this.enclosingClassFile = enclosingClassFile;
		// innerclasses get their names computed at code gen time
		if (aType.isLocalType()) {
			((LocalTypeBinding) aType).constantPoolName(
				computeConstantPoolName((LocalTypeBinding) aType));
			ReferenceBinding[] memberTypes = aType.memberTypes();
			for (int i = 0, max = memberTypes.length; i < max; i++) {
				((LocalTypeBinding) memberTypes[i]).constantPoolName(
					computeConstantPoolName((LocalTypeBinding) memberTypes[i]));
			}
		}
		contents = new byte[INITIAL_CONTENTS_SIZE];
		// now we continue to generate the bytes inside the contents array
		contents[contentsOffset++] = (byte) (accessFlags >> 8);
		contents[contentsOffset++] = (byte) accessFlags;
		int classNameIndex = constantPool.literalIndex(aType);
		contents[contentsOffset++] = (byte) (classNameIndex >> 8);
		contents[contentsOffset++] = (byte) classNameIndex;
		int superclassNameIndex;
		if (aType.isInterface()) {
			superclassNameIndex = constantPool.literalIndexForJavaLangObject();
		} else {
			superclassNameIndex =
				(aType.superclass == null ? 0 : constantPool.literalIndex(aType.superclass));
		}
		contents[contentsOffset++] = (byte) (superclassNameIndex >> 8);
		contents[contentsOffset++] = (byte) superclassNameIndex;
		ReferenceBinding[] superInterfacesBinding = aType.superInterfaces();
		int interfacesCount = superInterfacesBinding.length;
		contents[contentsOffset++] = (byte) (interfacesCount >> 8);
		contents[contentsOffset++] = (byte) interfacesCount;
		if (superInterfacesBinding != null) {
			for (int i = 0; i < interfacesCount; i++) {
				int interfaceIndex = constantPool.literalIndex(superInterfacesBinding[i]);
				contents[contentsOffset++] = (byte) (interfaceIndex >> 8);
				contents[contentsOffset++] = (byte) interfaceIndex;
			}
		}
		produceDebugAttributes =
			((SourceTypeBinding) referenceBinding)
				.scope
				.environment()
				.options
				.produceDebugAttributes;
		innerClassesBindings = new ReferenceBinding[INNER_CLASSES_SIZE];
		this.creatingProblemType = creatingProblemType;
		codeStream = new CodeStream(this);

		// retrieve the enclosing one guaranteed to be the one matching the propagated flow info
		// 1FF9ZBU: LFCOM:ALL - Local variable attributes busted (Sanity check)
		ClassFile outermostClassFile = this.outerMostEnclosingClassFile();
		if (this == outermostClassFile) {
			codeStream.maxFieldCount = aType.scope.referenceType().maxFieldCount;
		} else {
			codeStream.maxFieldCount = outermostClassFile.codeStream.maxFieldCount;
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus method.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 */
	public void addAbstractMethod(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding) {

		// force the modifiers to be public and abstract
		methodBinding.modifiers = AccPublic | AccAbstract;

		this.generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		int attributeNumber = this.generateMethodInfoAttribute(methodBinding);
		this.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods generate all the attributes for the receiver.
	 * For a class they could be:
	 * - source file attribute
	 * - inner classes attribute
	 * - deprecated attribute
	 */
	public void addAttributes() {
		// update the method count
		contents[methodCountOffset++] = (byte) (methodCount >> 8);
		contents[methodCountOffset] = (byte) methodCount;

		int attributeNumber = 0;
		// leave two bytes for the number of attributes and store the current offset
		int attributeOffset = contentsOffset;
		contentsOffset += 2;
		int contentsLength;

		// source attribute
		if ((produceDebugAttributes & CompilerOptions.Source) != 0) {
			String fullFileName =
				new String(referenceBinding.scope.referenceCompilationUnit().getFileName());
			fullFileName = fullFileName.replace('\\', '/');
			int lastIndex = fullFileName.lastIndexOf('/');
			if (lastIndex != -1) {
				fullFileName = fullFileName.substring(lastIndex + 1, fullFileName.length());
			}
			// check that there is enough space to write all the bytes for the field info corresponding
			// to the @fieldBinding
			if (contentsOffset + 8 >= (contentsLength = contents.length)) {
				System.arraycopy(
					contents,
					0,
					(contents = new byte[contentsLength + INCREMENT_SIZE]),
					0,
					contentsLength);
			}
			int sourceAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SourceName);
			contents[contentsOffset++] = (byte) (sourceAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) sourceAttributeNameIndex;
			// The length of a source file attribute is 2. This is a fixed-length
			// attribute
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 2;
			// write the source file name
			int fileNameIndex = constantPool.literalIndex(fullFileName.toCharArray());
			contents[contentsOffset++] = (byte) (fileNameIndex >> 8);
			contents[contentsOffset++] = (byte) fileNameIndex;
			attributeNumber++;
		}
		// Deprecated attribute
		if (referenceBinding.isDeprecated()) {
			// check that there is enough space to write all the bytes for the field info corresponding
			// to the @fieldBinding
			if (contentsOffset + 6 >= (contentsLength = contents.length)) {
				System.arraycopy(
					contents,
					0,
					(contents = new byte[contentsLength + INCREMENT_SIZE]),
					0,
					contentsLength);
			}
			int deprecatedAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
			contents[contentsOffset++] = (byte) (deprecatedAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) deprecatedAttributeNameIndex;
			// the length of a deprecated attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			attributeNumber++;
		}
		// Inner class attribute
		if (numberOfInnerClasses != 0) {
			// Generate the inner class attribute
			int exSize;
			if (contentsOffset + (exSize = (8 * numberOfInnerClasses + 8))
				>= (contentsLength = contents.length)) {
				System.arraycopy(
					contents,
					0,
					(contents =
						new byte[contentsLength
							+ (exSize >= INCREMENT_SIZE ? exSize : INCREMENT_SIZE)]),
					0,
					contentsLength);
			}
			// Now we now the size of the attribute and the number of entries
			// attribute name
			int attributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.InnerClassName);
			contents[contentsOffset++] = (byte) (attributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) attributeNameIndex;
			int value = (numberOfInnerClasses << 3) + 2;
			contents[contentsOffset++] = (byte) (value >> 24);
			contents[contentsOffset++] = (byte) (value >> 16);
			contents[contentsOffset++] = (byte) (value >> 8);
			contents[contentsOffset++] = (byte) value;
			contents[contentsOffset++] = (byte) (numberOfInnerClasses >> 8);
			contents[contentsOffset++] = (byte) numberOfInnerClasses;
			for (int i = 0; i < numberOfInnerClasses; i++) {
				ReferenceBinding innerClass = innerClassesBindings[i];
				int accessFlags = innerClass.getAccessFlags();
				int innerClassIndex = constantPool.literalIndex(innerClass);
				// inner class index
				contents[contentsOffset++] = (byte) (innerClassIndex >> 8);
				contents[contentsOffset++] = (byte) innerClassIndex;
				// outer class index: anonymous and local have no outer class index
				if (innerClass.isMemberType()) {
					// member or member of local
					int outerClassIndex = constantPool.literalIndex(innerClass.enclosingType());
					contents[contentsOffset++] = (byte) (outerClassIndex >> 8);
					contents[contentsOffset++] = (byte) outerClassIndex;
				} else {
					// equals to 0 if the innerClass is not a member type
					contents[contentsOffset++] = 0;
					contents[contentsOffset++] = 0;
				}
				// name index
				if (!innerClass.isAnonymousType()) {
					int nameIndex = constantPool.literalIndex(innerClass.sourceName());
					contents[contentsOffset++] = (byte) (nameIndex >> 8);
					contents[contentsOffset++] = (byte) nameIndex;
				} else {
					// equals to 0 if the innerClass is an anonymous type
					contents[contentsOffset++] = 0;
					contents[contentsOffset++] = 0;
				}
				// access flag
				if (innerClass.isAnonymousType()) {
					accessFlags |= AccPrivate;
				} else
					if (innerClass.isLocalType() && !innerClass.isMemberType()) {
						accessFlags |= AccPrivate;
					}
				contents[contentsOffset++] = (byte) (accessFlags >> 8);
				contents[contentsOffset++] = (byte) accessFlags;
			}
			attributeNumber++;
		}
		
		// write any "extraAttributes"
		if (extraAttributes != null) {
			for (int i=0, len=extraAttributes.size(); i < len; i++) {
				IAttribute attribute = (IAttribute)extraAttributes.get(i);
				short nameIndex = (short)constantPool.literalIndex(attribute.getNameChars());
				writeToContents(attribute.getAllBytes(nameIndex));
				attributeNumber++;
			}
		}
		
		
		// update the number of attributes
		contentsLength = contents.length;
		if (attributeOffset + 2 >= contentsLength) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		contents[attributeOffset++] = (byte) (attributeNumber >> 8);
		contents[attributeOffset] = (byte) attributeNumber;

		// resynchronize all offsets of the classfile
		header = constantPool.poolContent;
		headerOffset = constantPool.currentOffset;
		int constantPoolCount = constantPool.currentIndex;
		header[constantPoolOffset++] = (byte) (constantPoolCount >> 8);
		header[constantPoolOffset] = (byte) constantPoolCount;
	}

	

	/**
	 * INTERNAL USE-ONLY
	 * This methods generate all the default abstract method infos that correpond to
	 * the abstract methods inherited from superinterfaces.
	 */
	public void addDefaultAbstractMethods() { // default abstract methods
		MethodBinding[] defaultAbstractMethods =
			referenceBinding.getDefaultAbstractMethods();
		for (int i = 0, max = defaultAbstractMethods.length; i < max; i++) {
			generateMethodInfoHeader(defaultAbstractMethods[i]);
			int methodAttributeOffset = contentsOffset;
			int attributeNumber = generateMethodInfoAttribute(defaultAbstractMethods[i]);
			completeMethodInfo(methodAttributeOffset, attributeNumber);
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods generates the bytes for the field binding passed like a parameter
	 * @param fieldBinding org.eclipse.jdt.internal.compiler.lookup.FieldBinding
	 */
	public void addFieldInfo(FieldBinding fieldBinding) {
		int attributeNumber = 0;
		// check that there is enough space to write all the bytes for the field info corresponding
		// to the @fieldBinding
		int contentsLength;
		if (contentsOffset + 30 >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		// Generate two attribute: constantValueAttribute and SyntheticAttribute
		// Now we can generate all entries into the byte array
		// First the accessFlags
		int accessFlags = fieldBinding.getAccessFlags();
		contents[contentsOffset++] = (byte) (accessFlags >> 8);
		contents[contentsOffset++] = (byte) accessFlags;
		// Then the nameIndex
		int nameIndex = constantPool.literalIndex(fieldBinding.name);
		contents[contentsOffset++] = (byte) (nameIndex >> 8);
		contents[contentsOffset++] = (byte) nameIndex;
		// Then the descriptorIndex
		int descriptorIndex = constantPool.literalIndex(fieldBinding.type.signature());
		contents[contentsOffset++] = (byte) (descriptorIndex >> 8);
		contents[contentsOffset++] = (byte) descriptorIndex;
		// leave some space for the number of attributes
		int fieldAttributeOffset = contentsOffset;
		contentsOffset += 2;
		// 4.7.2 only static constant fields get a ConstantAttribute
		if (fieldBinding.constant != Constant.NotAConstant
			&& fieldBinding.constant.typeID() != T_null) {
			// Now we generate the constant attribute corresponding to the fieldBinding
			int constantValueNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.ConstantValueName);
			contents[contentsOffset++] = (byte) (constantValueNameIndex >> 8);
			contents[contentsOffset++] = (byte) constantValueNameIndex;
			// The attribute length = 2 in case of a constantValue attribute
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 2;
			attributeNumber++;
			// Need to add the constant_value_index
			switch (fieldBinding.constant.typeID()) {
				case T_boolean :
					int booleanValueIndex =
						constantPool.literalIndex(fieldBinding.constant.booleanValue() ? 1 : 0);
					contents[contentsOffset++] = (byte) (booleanValueIndex >> 8);
					contents[contentsOffset++] = (byte) booleanValueIndex;
					break;
				case T_byte :
				case T_char :
				case T_int :
				case T_short :
					int integerValueIndex =
						constantPool.literalIndex(fieldBinding.constant.intValue());
					contents[contentsOffset++] = (byte) (integerValueIndex >> 8);
					contents[contentsOffset++] = (byte) integerValueIndex;
					break;
				case T_float :
					int floatValueIndex =
						constantPool.literalIndex(fieldBinding.constant.floatValue());
					contents[contentsOffset++] = (byte) (floatValueIndex >> 8);
					contents[contentsOffset++] = (byte) floatValueIndex;
					break;
				case T_double :
					int doubleValueIndex =
						constantPool.literalIndex(fieldBinding.constant.doubleValue());
					contents[contentsOffset++] = (byte) (doubleValueIndex >> 8);
					contents[contentsOffset++] = (byte) doubleValueIndex;
					break;
				case T_long :
					int longValueIndex =
						constantPool.literalIndex(fieldBinding.constant.longValue());
					contents[contentsOffset++] = (byte) (longValueIndex >> 8);
					contents[contentsOffset++] = (byte) longValueIndex;
					break;
				case T_String :
					int stringValueIndex =
						constantPool.literalIndex(
							((StringConstant) fieldBinding.constant).stringValue());
					if (stringValueIndex == -1) {
						if (!creatingProblemType) {
							// report an error and abort: will lead to a problem type classfile creation
							TypeDeclaration typeDeclaration = referenceBinding.scope.referenceContext;
							FieldDeclaration[] fieldDecls = typeDeclaration.fields;
							for (int i = 0, max = fieldDecls.length; i < max; i++) {
								if (fieldDecls[i].binding == fieldBinding) {
									// problem should abort
									typeDeclaration.scope.problemReporter().stringConstantIsExceedingUtf8Limit(
										fieldDecls[i]);
								}
							}
						} else {
							// already inside a problem type creation : no constant for this field
							contentsOffset = fieldAttributeOffset + 2;
							// +2 is necessary to keep the two byte space for the attribute number
							attributeNumber--;
						}
					} else {
						contents[contentsOffset++] = (byte) (stringValueIndex >> 8);
						contents[contentsOffset++] = (byte) stringValueIndex;
					}
			}
		}
		if (fieldBinding.isSynthetic()) {
			int syntheticAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
			contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
			// the length of a synthetic attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			attributeNumber++;
		}
		if (fieldBinding.isDeprecated()) {
			int deprecatedAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
			contents[contentsOffset++] = (byte) (deprecatedAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) deprecatedAttributeNameIndex;
			// the length of a deprecated attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			attributeNumber++;
		}
		contents[fieldAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[fieldAttributeOffset] = (byte) attributeNumber;
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods generate all the fields infos for the receiver.
	 * This includes:
	 * - a field info for each defined field of that class
	 * - a field info for each synthetic field (e.g. this$0)
	 */
	public void addFieldInfos() {
		SourceTypeBinding currentBinding = referenceBinding;
		FieldBinding[] syntheticFields = currentBinding.syntheticFields();
		int fieldCount =
			currentBinding.fieldCount()
				+ (syntheticFields == null ? 0 : syntheticFields.length);

		// write the number of fields
		contents[contentsOffset++] = (byte) (fieldCount >> 8);
		contents[contentsOffset++] = (byte) fieldCount;

		FieldBinding[] fieldBindings = currentBinding.fields();
		for (int i = 0, max = fieldBindings.length; i < max; i++) {
			addFieldInfo(fieldBindings[i]);
		}
		if (syntheticFields != null) {
			for (int i = 0, max = syntheticFields.length; i < max; i++) {
				addFieldInfo(syntheticFields[i]);
			}
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods stores the bindings for each inner class. They will be used to know which entries
	 * have to be generated for the inner classes attributes.
	 * @param referenceBinding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding 
	 */
	public void addInnerClasses(ReferenceBinding referenceBinding) {
		// check first if that reference binding is there
		for (int i = 0; i < numberOfInnerClasses; i++) {
			if (innerClassesBindings[i] == referenceBinding)
				return;
		}
		int length = innerClassesBindings.length;
		if (numberOfInnerClasses == length) {
			System.arraycopy(
				innerClassesBindings,
				0,
				(innerClassesBindings = new ReferenceBinding[length * 2]),
				0,
				length);
		}
		innerClassesBindings[numberOfInnerClasses++] = referenceBinding;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem clinit method info that correspond to a boggus method.
	 *
	 * @param problem org.eclipse.jdt.internal.compiler.problem.Problem[]
	 */
	public void addProblemClinit(IProblem[] problems) {
		generateMethodInfoHeaderForClinit();
		// leave two spaces for the number of attributes
		contentsOffset -= 2;
		int attributeOffset = contentsOffset;
		contentsOffset += 2;
		int attributeNumber = 0;

		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.resetForProblemClinit(this);
		String problemString = "" ; //$NON-NLS-1$
		if (problems != null) {
			int max = problems.length;
			StringBuffer buffer = new StringBuffer(25);
			int count = 0;
			for (int i = 0; i < max; i++) {
				IProblem problem = problems[i];
				if ((problem != null) && (problem.isError())) {
					buffer.append("\t"  +problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
					count++;
					if (problemLine == 0) {
						problemLine = problem.getSourceLineNumber();
					}
					problems[i] = null;
				}
			} // insert the top line afterwards, once knowing how many problems we have to consider
			if (count > 1) {
				buffer.insert(0, Util.bind("compilation.unresolvedProblems" )); //$NON-NLS-1$
			} else {
				buffer.insert(0, Util.bind("compilation.unresolvedProblem" )); //$NON-NLS-1$
			}
			problemString = buffer.toString();
		}

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		int[] exceptionHandler =
			codeStream.generateCodeAttributeForProblemMethod(
				referenceBinding
					.scope
					.environment()
					.options
					.runtimeExceptionNameForCompileError,
				problemString);
		attributeNumber++; // code attribute
		completeCodeAttributeForClinit(
			codeAttributeOffset,
			exceptionHandler,
			referenceBinding
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.lineSeparatorPositions);
		contents[attributeOffset++] = (byte) (attributeNumber >> 8);
		contents[attributeOffset] = (byte) attributeNumber;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus constructor.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problem org.eclipse.jdt.internal.compiler.problem.Problem[]
	 */
	public void addProblemConstructor(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		IProblem[] problems) {

		// always clear the strictfp/native/abstract bit for a problem method
		methodBinding.modifiers &= ~(AccStrictfp | AccNative | AccAbstract);

		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = contentsOffset;
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		
		// Code attribute
		attributeNumber++;
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		final ProblemReporter problemReporter = method.scope.problemReporter();
		codeStream.reset(method, this);
		String problemString = "" ; //$NON-NLS-1$
		if (problems != null) {
			int max = problems.length;
			StringBuffer buffer = new StringBuffer(25);
			int count = 0;
			for (int i = 0; i < max; i++) {
				IProblem problem = problems[i];
				if ((problem != null) && (problem.isError())) {
					buffer.append("\t"  +problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
					count++;
					if (problemLine == 0) {
						problemLine = problem.getSourceLineNumber();
					}
				}
			} // insert the top line afterwards, once knowing how many problems we have to consider
			if (count > 1) {
				buffer.insert(0, Util.bind("compilation.unresolvedProblems" )); //$NON-NLS-1$
			} else {
				buffer.insert(0, Util.bind("compilation.unresolvedProblem" )); //$NON-NLS-1$
			}
			problemString = buffer.toString();
		}

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		int[] exceptionHandler =
			codeStream.generateCodeAttributeForProblemMethod(
				problemReporter.options.runtimeExceptionNameForCompileError,
				problemString);
		completeCodeAttributeForProblemMethod(
			method,
			methodBinding,
			codeAttributeOffset,
			exceptionHandler,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.lineSeparatorPositions);
		completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus constructor.
	 * Reset the position inside the contents byte array to the savedOffset.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problem org.eclipse.jdt.internal.compiler.problem.Problem[]
	 * @param savedOffset <CODE>int</CODE>
	 */
	public void addProblemConstructor(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		IProblem[] problems,
		int savedOffset) {
		// we need to move back the contentsOffset to the value at the beginning of the method
		contentsOffset = savedOffset;
		methodCount--; // we need to remove the method that causes the problem
		addProblemConstructor(method, methodBinding, problems);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus method.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problem org.eclipse.jdt.internal.compiler.problem.Problem[]
	 */
	public void addProblemMethod(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		IProblem[] problems) {
		if (methodBinding.isAbstract() && methodBinding.declaringClass.isInterface()) {
			method.abort(AbstractMethodDeclaration.AbortType);
		}
		// always clear the strictfp/native/abstract bit for a problem method
		methodBinding.modifiers &= ~(AccStrictfp | AccNative | AccAbstract);

		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = contentsOffset;
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		
		// Code attribute
		attributeNumber++;
		
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		final ProblemReporter problemReporter = method.scope.problemReporter();
		codeStream.reset(method, this);
		String problemString = "" ; //$NON-NLS-1$
		if (problems != null) {
			int max = problems.length;
			StringBuffer buffer = new StringBuffer(25);
			int count = 0;
			for (int i = 0; i < max; i++) {
				IProblem problem = problems[i];
				if ((problem != null)
					&& (problem.isError())
					&& (problem.getSourceStart() >= method.declarationSourceStart)
					&& (problem.getSourceEnd() <= method.declarationSourceEnd)) {
					buffer.append("\t"  +problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
					count++;
					if (problemLine == 0) {
						problemLine = problem.getSourceLineNumber();
					}
					problems[i] = null;
				}
			} // insert the top line afterwards, once knowing how many problems we have to consider
			if (count > 1) {
				buffer.insert(0, Util.bind("compilation.unresolvedProblems" )); //$NON-NLS-1$
			} else {
				buffer.insert(0, Util.bind("compilation.unresolvedProblem" )); //$NON-NLS-1$
			}
			problemString = buffer.toString();
		}

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		int[] exceptionHandler =
			codeStream.generateCodeAttributeForProblemMethod(
				problemReporter.options.runtimeExceptionNameForCompileError,
				problemString);
		completeCodeAttributeForProblemMethod(
			method,
			methodBinding,
			codeAttributeOffset,
			exceptionHandler,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.lineSeparatorPositions);
		completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus method.
	 * Reset the position inside the contents byte array to the savedOffset.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problem org.eclipse.jdt.internal.compiler.problem.Problem[]
	 * @param savedOffset <CODE>int</CODE>
	 */
	public void addProblemMethod(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		IProblem[] problems,
		int savedOffset) {
		// we need to move back the contentsOffset to the value at the beginning of the method
		contentsOffset = savedOffset;
		methodCount--; // we need to remove the method that causes the problem
		addProblemMethod(method, methodBinding, problems);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for all the special method infos.
	 * They are:
	 * - synthetic access methods
	 * - default abstract methods
	 */
	public void addSpecialMethods() {
		// add all methods (default abstract methods and synthetic)

		// default abstract methods
		SourceTypeBinding currentBinding = referenceBinding;
		MethodBinding[] defaultAbstractMethods =
			currentBinding.getDefaultAbstractMethods();
		for (int i = 0, max = defaultAbstractMethods.length; i < max; i++) {
			generateMethodInfoHeader(defaultAbstractMethods[i]);
			int methodAttributeOffset = contentsOffset;
			int attributeNumber = generateMethodInfoAttribute(defaultAbstractMethods[i]);
			completeMethodInfo(methodAttributeOffset, attributeNumber);
		}
		// add synthetic methods infos
		SyntheticAccessMethodBinding[] syntheticAccessMethods =
			currentBinding.syntheticAccessMethods();
		if (syntheticAccessMethods != null) {
			for (int i = 0, max = syntheticAccessMethods.length; i < max; i++) {
				SyntheticAccessMethodBinding accessMethodBinding = syntheticAccessMethods[i];
				switch (accessMethodBinding.accessType) {
					case SyntheticAccessMethodBinding.FieldReadAccess :
						// generate a method info to emulate an reading access to
						// a private field
						addSyntheticFieldReadAccessMethod(syntheticAccessMethods[i]);
						break;
					case SyntheticAccessMethodBinding.FieldWriteAccess :
						// generate a method info to emulate an writing access to
						// a private field
						addSyntheticFieldWriteAccessMethod(syntheticAccessMethods[i]);
						break;
					case SyntheticAccessMethodBinding.MethodAccess :
						// generate a method info to emulate an access to a private method
						addSyntheticMethodAccessMethod(syntheticAccessMethods[i]);
						break;
					case SyntheticAccessMethodBinding.ConstructorAccess :
						// generate a method info to emulate an access to a private method
						addSyntheticConstructorAccessMethod(syntheticAccessMethods[i]);
				}
			}
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for problem method infos that correspond to missing abstract methods.
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=3179
	 *
	 * @param methodDeclarations Array of all missing abstract methods
	 */
	public void generateMissingAbstractMethods(MethodDeclaration[] methodDeclarations, CompilationResult compilationResult) {
		if (methodDeclarations != null) {
			for (int i = 0, max = methodDeclarations.length; i < max; i++) {
				MethodDeclaration methodDeclaration = methodDeclarations[i];
				MethodBinding methodBinding = methodDeclaration.binding;
		 		String readableName = new String(methodBinding.readableName());
		 		IProblem[] problems = compilationResult.problems;
		 		int problemsCount = compilationResult.problemCount;
				for (int j = 0; j < problemsCount; j++) {
					IProblem problem = problems[j];
					if (problem != null
						&& problem.getID() == IProblem.AbstractMethodMustBeImplemented
						&& problem.getMessage().indexOf(readableName) != -1) {
							// we found a match
							addMissingAbstractProblemMethod(methodDeclaration, methodBinding, problem, compilationResult);
						}
				}
			}
		}
	}
	
	private void addMissingAbstractProblemMethod(MethodDeclaration methodDeclaration, MethodBinding methodBinding, IProblem problem, CompilationResult compilationResult) {
		// always clear the strictfp/native/abstract bit for a problem method
		methodBinding.modifiers &= ~(AccStrictfp | AccNative | AccAbstract);
		
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = contentsOffset;
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		
		// Code attribute
		attributeNumber++;
		
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		StringBuffer buffer = new StringBuffer(25);
		buffer.append("\t"  + problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.insert(0, Util.bind("compilation.unresolvedProblem" )); //$NON-NLS-1$
		String problemString = buffer.toString();
		this.problemLine = problem.getSourceLineNumber();
		
		final ProblemReporter problemReporter = methodDeclaration.scope.problemReporter();
		codeStream.init(this);
		codeStream.preserveUnusedLocals = true;
		codeStream.initializeMaxLocals(methodBinding);

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		int[] exceptionHandler =
			codeStream.generateCodeAttributeForProblemMethod(
				problemReporter.options.runtimeExceptionNameForCompileError,
				problemString);
				
		completeCodeAttributeForMissingAbstractProblemMethod(
			methodBinding,
			codeAttributeOffset,
			exceptionHandler,
			compilationResult.lineSeparatorPositions);
			
		completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	/**
	 * 
	 */
	public void completeCodeAttributeForMissingAbstractProblemMethod(
		MethodBinding binding,
		int codeAttributeOffset,
		int[] exceptionHandler,
		int[] startLineIndexes) {
		// reinitialize the localContents with the byte modified by the code stream
		byte[] localContents = contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside localContents byte array before we started to write// any information about the codeAttribute// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset// to get the right position, 6 for the max_stack etc...
		int max_stack = codeStream.stackMax;
		localContents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		localContents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		localContents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		localContents[codeAttributeOffset + 9] = (byte) max_locals;
		int code_length = codeStream.position;
		localContents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		localContents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		localContents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		localContents[codeAttributeOffset + 13] = (byte) code_length;
		// write the exception table
		int contentsLength;
		if (localContentsOffset + 50 >= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		localContents[localContentsOffset++] = 0;
		localContents[localContentsOffset++] = 1;
		int start = exceptionHandler[0];
		localContents[localContentsOffset++] = (byte) (start >> 8);
		localContents[localContentsOffset++] = (byte) start;
		int end = exceptionHandler[1];
		localContents[localContentsOffset++] = (byte) (end >> 8);
		localContents[localContentsOffset++] = (byte) end;
		int handlerPC = exceptionHandler[2];
		localContents[localContentsOffset++] = (byte) (handlerPC >> 8);
		localContents[localContentsOffset++] = (byte) handlerPC;
		int nameIndex = constantPool.literalIndexForJavaLangException();
		localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
		localContents[localContentsOffset++] = (byte) nameIndex; // debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0; // leave two bytes for the attribute_length
		localContentsOffset += 2; // first we handle the linenumber attribute

		if (codeStream.generateLineNumberAttributes) {
			/* Create and add the line number attribute (used for debugging) 
			    * Build the pairs of:
			    * (bytecodePC lineNumber)
			    * according to the table of start line indexes and the pcToSourceMap table
			    * contained into the codestream
			    */
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			localContents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			localContents[localContentsOffset++] = (byte) lineNumberNameIndex;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 6;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 1;
			if (problemLine == 0) {
				problemLine = searchLineNumber(startLineIndexes, binding.sourceStart());
			}
			// first entry at pc = 0
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = (byte) (problemLine >> 8);
			localContents[localContentsOffset++] = (byte) problemLine;
			// now we change the size of the line number attribute
			attributeNumber++;
		}
		
		// then we do the local variable attribute
		// update the number of attributes// ensure first that there is enough space available inside the localContents array
		if (codeAttributeAttributeOffset + 2
			>= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		localContents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		localContents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		localContents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		localContents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		localContents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		localContents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a synthetic method that
	 * generate an access to a private constructor.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticConstructorAccessMethod(SyntheticAccessMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		// We know that we won't get more than 2 attribute: the code attribute + synthetic attribute
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 2;
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForConstructorAccess(methodBinding);
		completeCodeAttributeForSyntheticAccessMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.lineSeparatorPositions);
		// add the synthetic attribute
		int syntheticAttributeNameIndex =
			constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
		contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
		contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
		// the length of a synthetic attribute is equals to 0
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a synthetic method that
	 * generate an read access to a private field.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticFieldReadAccessMethod(SyntheticAccessMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		// We know that we won't get more than 2 attribute: the code attribute + synthetic attribute
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 2;
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForFieldReadAccess(methodBinding);
		completeCodeAttributeForSyntheticAccessMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.lineSeparatorPositions);
		// add the synthetic attribute
		int syntheticAttributeNameIndex =
			constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
		contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
		contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
		// the length of a synthetic attribute is equals to 0
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a synthetic method that
	 * generate an write access to a private field.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticFieldWriteAccessMethod(SyntheticAccessMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		// We know that we won't get more than 2 attribute: the code attribute + synthetic attribute
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 2;
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForFieldWriteAccess(methodBinding);
		completeCodeAttributeForSyntheticAccessMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.lineSeparatorPositions);
		// add the synthetic attribute
		int syntheticAttributeNameIndex =
			constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
		contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
		contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
		// the length of a synthetic attribute is equals to 0
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a synthetic method that
	 * generate an access to a private method.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticMethodAccessMethod(SyntheticAccessMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		// We know that we won't get more than 2 attribute: the code attribute + synthetic attribute
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 2;
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForMethodAccess(methodBinding);
		completeCodeAttributeForSyntheticAccessMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.lineSeparatorPositions);
		// add the synthetic attribute
		int syntheticAttributeNameIndex =
			constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
		contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
		contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
		// the length of a synthetic attribute is equals to 0
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 0;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Build all the directories and subdirectories corresponding to the packages names
	 * into the directory specified in parameters.
	 *
	 * outputPath is formed like:
	 *	   c:\temp\ the last character is a file separator
	 * relativeFileName is formed like:
	 *     java\lang\String.class *
	 * 
	 * @param outputPath java.lang.String
	 * @param relativeFileName java.lang.String
	 * @return java.lang.String
	 */
	public static String buildAllDirectoriesInto(
		String outputPath,
		String relativeFileName)
		throws IOException {
		char fileSeparatorChar = File.separatorChar;
		String fileSeparator = File.separator;
		File f;
		// First we ensure that the outputPath exists
		outputPath = outputPath.replace('/', fileSeparatorChar);
		// To be able to pass the mkdirs() method we need to remove the extra file separator at the end of the outDir name
		if (outputPath.endsWith(fileSeparator)) {
			outputPath = outputPath.substring(0, outputPath.length() - 1);
		}
		f = new File(outputPath);
		if (f.exists()) {
			if (!f.isDirectory()) {
				System.out.println(Util.bind("output.isFile" , f.getAbsolutePath())); //$NON-NLS-1$
				throw new IOException(Util.bind("output.isFileNotDirectory" )); //$NON-NLS-1$
			}
		} else {
			// we have to create that directory
			if (!f.mkdirs()) {
				System.out.println(Util.bind("output.dirName" , f.getAbsolutePath())); //$NON-NLS-1$
				throw new IOException(Util.bind("output.notValidAll" )); //$NON-NLS-1$
			}
		}
		StringBuffer outDir = new StringBuffer(outputPath);
		outDir.append(fileSeparator);
		StringTokenizer tokenizer =
			new StringTokenizer(relativeFileName, fileSeparator);
		String token = tokenizer.nextToken();
		while (tokenizer.hasMoreTokens()) {
			f = new File(outDir.append(token).append(fileSeparator).toString());
			if (f.exists()) {
				// The outDir already exists, so we proceed the next entry
				// System.out.println("outDir: " + outDir + " already exists.");
			} else {
				// Need to add the outDir
				if (!f.mkdir()) {
					System.out.println(Util.bind("output.fileName" , f.getName())); //$NON-NLS-1$
					throw new IOException(Util.bind("output.notValid" )); //$NON-NLS-1$
				}
			}
			token = tokenizer.nextToken();
		}
		// token contains the last one
		return outDir.append(token).toString();
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttribute(int codeAttributeOffset) {
		// reinitialize the localContents with the byte modified by the code stream
		byte[] localContents = contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside localContents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int contentsLength;
		int code_length = codeStream.position;
		if (code_length > 65535) {
			codeStream.methodDeclaration.scope.problemReporter().bytecodeExceeds64KLimit(
				codeStream.methodDeclaration);
		}
		if (localContentsOffset + 20 >= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		int max_stack = codeStream.stackMax;
		localContents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		localContents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		localContents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		localContents[codeAttributeOffset + 9] = (byte) max_locals;
		localContents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		localContents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		localContents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		localContents[codeAttributeOffset + 13] = (byte) code_length;

		// write the exception table
		int exceptionHandlersNumber = codeStream.exceptionHandlersNumber;
		ExceptionLabel[] exceptionHandlers = codeStream.exceptionHandlers;
		int exSize;
		if (localContentsOffset + (exSize = (exceptionHandlersNumber * 8 + 2))
			>= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents =
					contents =
						new byte[contentsLength + (exSize > INCREMENT_SIZE ? exSize : INCREMENT_SIZE)]),
				0,
				contentsLength);
		}
		// there is no exception table, so we need to offset by 2 the current offset and move 
		// on the attribute generation
		localContents[localContentsOffset++] = (byte) (exceptionHandlersNumber >> 8);
		localContents[localContentsOffset++] = (byte) exceptionHandlersNumber;
		for (int i = 0; i < exceptionHandlersNumber; i++) {
			ExceptionLabel exceptionHandler = exceptionHandlers[i];
			int start = exceptionHandler.start;
			localContents[localContentsOffset++] = (byte) (start >> 8);
			localContents[localContentsOffset++] = (byte) start;
			int end = exceptionHandler.end;
			localContents[localContentsOffset++] = (byte) (end >> 8);
			localContents[localContentsOffset++] = (byte) end;
			int handlerPC = exceptionHandler.position;
			localContents[localContentsOffset++] = (byte) (handlerPC >> 8);
			localContents[localContentsOffset++] = (byte) handlerPC;
			if (exceptionHandler.exceptionType == null) {
				// any exception handler
				localContents[localContentsOffset++] = 0;
				localContents[localContentsOffset++] = 0;
			} else {
				int nameIndex;
				if (exceptionHandler.exceptionType == TypeBinding.NullBinding) {
					/* represents ClassNotFoundException, see class literal access*/
					nameIndex = constantPool.literalIndexForJavaLangClassNotFoundException();
				} else {
					nameIndex = constantPool.literalIndex(exceptionHandler.exceptionType);
				}
				localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
				localContents[localContentsOffset++] = (byte) nameIndex;
			}
		}
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0;
		// leave two bytes for the attribute_length
		localContentsOffset += 2;

		// first we handle the linenumber attribute
		if (codeStream.generateLineNumberAttributes) {
			/* Create and add the line number attribute (used for debugging) 
			 * Build the pairs of:
			 * 	(bytecodePC lineNumber)
			 * according to the table of start line indexes and the pcToSourceMap table
			 * contained into the codestream
			 */
			int[] pcToSourceMapTable;
			if (((pcToSourceMapTable = codeStream.pcToSourceMap) != null)
				&& (codeStream.pcToSourceMapSize != 0)) {
				int lineNumberNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
				if (localContentsOffset + 8 >= (contentsLength = localContents.length)) {
					System.arraycopy(
						contents,
						0,
						(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
						0,
						contentsLength);
				}
				localContents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
				localContents[localContentsOffset++] = (byte) lineNumberNameIndex;
				int lineNumberTableOffset = localContentsOffset;
				localContentsOffset += 6;
				// leave space for attribute_length and line_number_table_length
				int numberOfEntries = 0;
				int length = codeStream.pcToSourceMapSize;
				for (int i = 0; i < length;) {
					// write the entry
					if (localContentsOffset + 4 >= (contentsLength = localContents.length)) {
						System.arraycopy(
							contents,
							0,
							(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
							0,
							contentsLength);
					}
					int pc = pcToSourceMapTable[i++];
					localContents[localContentsOffset++] = (byte) (pc >> 8);
					localContents[localContentsOffset++] = (byte) pc;
					int lineNumber = pcToSourceMapTable[i++];
					localContents[localContentsOffset++] = (byte) (lineNumber >> 8);
					localContents[localContentsOffset++] = (byte) lineNumber;
					numberOfEntries++;
				}
				// now we change the size of the line number attribute
				int lineNumberAttr_length = numberOfEntries * 4 + 2;
				localContents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 24);
				localContents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 16);
				localContents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 8);
				localContents[lineNumberTableOffset++] = (byte) lineNumberAttr_length;
				localContents[lineNumberTableOffset++] = (byte) (numberOfEntries >> 8);
				localContents[lineNumberTableOffset++] = (byte) numberOfEntries;
				attributeNumber++;
			}
		}
		// then we do the local variable attribute
		if (codeStream.generateLocalVariableTableAttributes) {
			int localVariableTableOffset = localContentsOffset;
			int numberOfEntries = 0;
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			if (localContentsOffset + 8 >= (contentsLength = localContents.length)) {
				System.arraycopy(
					contents,
					0,
					(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
					0,
					contentsLength);
			}
			localContents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			localContents[localContentsOffset++] = (byte) localVariableNameIndex;
			localContentsOffset += 6;
			// leave space for attribute_length and local_variable_table_length
			int nameIndex;
			int descriptorIndex;
			if (!codeStream.methodDeclaration.isStatic()) {
				numberOfEntries++;
				if (localContentsOffset + 10 >= (contentsLength = localContents.length)) {
					System.arraycopy(
						contents,
						0,
						(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
						0,
						contentsLength);
				}
				localContentsOffset += 2; // the startPC for this is always 0
				localContents[localContentsOffset++] = (byte) (code_length >> 8);
				localContents[localContentsOffset++] = (byte) code_length;
				nameIndex = constantPool.literalIndex(QualifiedNamesConstants.This);
				localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
				localContents[localContentsOffset++] = (byte) nameIndex;
				descriptorIndex =
					constantPool.literalIndex(
						codeStream.methodDeclaration.binding.declaringClass.signature());
				localContents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
				localContents[localContentsOffset++] = (byte) descriptorIndex;
				localContentsOffset += 2; // the resolved position for this is always 0
			}
			for (int i = 0; i < codeStream.allLocalsCounter; i++) {
				LocalVariableBinding localVariable = codeStream.locals[i];
				for (int j = 0; j < localVariable.initializationCount; j++) {
					int startPC = localVariable.initializationPCs[j << 1];
					int endPC = localVariable.initializationPCs[(j << 1) + 1];
					if (startPC != endPC) { // only entries for non zero length
						if (endPC == -1) {
							localVariable.declaringScope.problemReporter().abortDueToInternalError(
								Util.bind("abort.invalidAttribute" , new String(localVariable.name)), //$NON-NLS-1$
								(AstNode) localVariable.declaringScope.methodScope().referenceContext);
						}
						if (localContentsOffset + 10 >= (contentsLength = localContents.length)) {
							System.arraycopy(
								contents,
								0,
								(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
								0,
								contentsLength);
						}
						// now we can safely add the local entry
						numberOfEntries++;
						localContents[localContentsOffset++] = (byte) (startPC >> 8);
						localContents[localContentsOffset++] = (byte) startPC;
						int length = endPC - startPC;
						localContents[localContentsOffset++] = (byte) (length >> 8);
						localContents[localContentsOffset++] = (byte) length;
						nameIndex = constantPool.literalIndex(localVariable.name);
						localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
						localContents[localContentsOffset++] = (byte) nameIndex;
						descriptorIndex = constantPool.literalIndex(localVariable.type.signature());
						localContents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
						localContents[localContentsOffset++] = (byte) descriptorIndex;
						int resolvedPosition = localVariable.resolvedPosition;
						localContents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
						localContents[localContentsOffset++] = (byte) resolvedPosition;
					}
				}
			}
			int value = numberOfEntries * 10 + 2;
			localVariableTableOffset += 2;
			localContents[localVariableTableOffset++] = (byte) (value >> 24);
			localContents[localVariableTableOffset++] = (byte) (value >> 16);
			localContents[localVariableTableOffset++] = (byte) (value >> 8);
			localContents[localVariableTableOffset++] = (byte) value;
			localContents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
			localContents[localVariableTableOffset] = (byte) numberOfEntries;
			attributeNumber++;
		}
		// update the number of attributes
		// ensure first that there is enough space available inside the localContents array
		if (codeAttributeAttributeOffset + 2
			>= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		localContents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		localContents[codeAttributeAttributeOffset] = (byte) attributeNumber;

		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		localContents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		localContents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		localContents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		localContents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttributeForClinit(int codeAttributeOffset) {
		// reinitialize the contents with the byte modified by the code stream
		byte[] localContents = contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside contents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int contentsLength;
		int code_length = codeStream.position;
		if (code_length > 65535) {
			codeStream.methodDeclaration.scope.problemReporter().bytecodeExceeds64KLimit(
				codeStream.methodDeclaration.scope.referenceType());
		}
		if (localContentsOffset + 20 >= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		int max_stack = codeStream.stackMax;
		localContents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		localContents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		localContents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		localContents[codeAttributeOffset + 9] = (byte) max_locals;
		localContents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		localContents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		localContents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		localContents[codeAttributeOffset + 13] = (byte) code_length;

		// write the exception table
		int exceptionHandlersNumber = codeStream.exceptionHandlersNumber;
		ExceptionLabel[] exceptionHandlers = codeStream.exceptionHandlers;
		int exSize;
		if (localContentsOffset + (exSize = (exceptionHandlersNumber * 8 + 2))
			>= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents =
					contents =
						new byte[contentsLength + (exSize > INCREMENT_SIZE ? exSize : INCREMENT_SIZE)]),
				0,
				contentsLength);
		}
		// there is no exception table, so we need to offset by 2 the current offset and move 
		// on the attribute generation
		localContents[localContentsOffset++] = (byte) (exceptionHandlersNumber >> 8);
		localContents[localContentsOffset++] = (byte) exceptionHandlersNumber;
		for (int i = 0; i < exceptionHandlersNumber; i++) {
			ExceptionLabel exceptionHandler = exceptionHandlers[i];
			int start = exceptionHandler.start;
			localContents[localContentsOffset++] = (byte) (start >> 8);
			localContents[localContentsOffset++] = (byte) start;
			int end = exceptionHandler.end;
			localContents[localContentsOffset++] = (byte) (end >> 8);
			localContents[localContentsOffset++] = (byte) end;
			int handlerPC = exceptionHandler.position;
			localContents[localContentsOffset++] = (byte) (handlerPC >> 8);
			localContents[localContentsOffset++] = (byte) handlerPC;
			if (exceptionHandler.exceptionType == null) {
				// any exception handler
				localContentsOffset += 2;
			} else {
				int nameIndex;
				if (exceptionHandler.exceptionType == TypeBinding.NullBinding) {
					/* represents denote ClassNotFoundException, see class literal access*/
					nameIndex = constantPool.literalIndexForJavaLangClassNotFoundException();
				} else {
					nameIndex = constantPool.literalIndex(exceptionHandler.exceptionType);
				}
				localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
				localContents[localContentsOffset++] = (byte) nameIndex;
			}
		}
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0;
		// leave two bytes for the attribute_length
		localContentsOffset += 2;

		// first we handle the linenumber attribute
		if (codeStream.generateLineNumberAttributes) {
			/* Create and add the line number attribute (used for debugging) 
			 * Build the pairs of:
			 * 	(bytecodePC lineNumber)
			 * according to the table of start line indexes and the pcToSourceMap table
			 * contained into the codestream
			 */
			int[] pcToSourceMapTable;
			if (((pcToSourceMapTable = codeStream.pcToSourceMap) != null)
				&& (codeStream.pcToSourceMapSize != 0)) {
				int lineNumberNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
				if (localContentsOffset + 8 >= (contentsLength = localContents.length)) {
					System.arraycopy(
						contents,
						0,
						(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
						0,
						contentsLength);
				}
				localContents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
				localContents[localContentsOffset++] = (byte) lineNumberNameIndex;
				int lineNumberTableOffset = localContentsOffset;
				localContentsOffset += 6;
				// leave space for attribute_length and line_number_table_length
				int numberOfEntries = 0;
				int length = codeStream.pcToSourceMapSize;
				for (int i = 0; i < length;) {
					// write the entry
					if (localContentsOffset + 4 >= (contentsLength = localContents.length)) {
						System.arraycopy(
							contents,
							0,
							(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
							0,
							contentsLength);
					}
					int pc = pcToSourceMapTable[i++];
					localContents[localContentsOffset++] = (byte) (pc >> 8);
					localContents[localContentsOffset++] = (byte) pc;
					int lineNumber = pcToSourceMapTable[i++];
					localContents[localContentsOffset++] = (byte) (lineNumber >> 8);
					localContents[localContentsOffset++] = (byte) lineNumber;
					numberOfEntries++;
				}
				// now we change the size of the line number attribute
				int lineNumberAttr_length = numberOfEntries * 4 + 2;
				localContents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 24);
				localContents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 16);
				localContents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 8);
				localContents[lineNumberTableOffset++] = (byte) lineNumberAttr_length;
				localContents[lineNumberTableOffset++] = (byte) (numberOfEntries >> 8);
				localContents[lineNumberTableOffset++] = (byte) numberOfEntries;
				attributeNumber++;
			}
		}
		// then we do the local variable attribute
		if (codeStream.generateLocalVariableTableAttributes) {
			int localVariableTableOffset = localContentsOffset;
			int numberOfEntries = 0;
			//		codeAttribute.addLocalVariableTableAttribute(this);
			if ((codeStream.pcToSourceMap != null)
				&& (codeStream.pcToSourceMapSize != 0)) {
				int localVariableNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
				if (localContentsOffset + 8 >= (contentsLength = localContents.length)) {
					System.arraycopy(
						contents,
						0,
						(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
						0,
						contentsLength);
				}
				localContents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
				localContents[localContentsOffset++] = (byte) localVariableNameIndex;
				localContentsOffset += 6;
				// leave space for attribute_length and local_variable_table_length
				int nameIndex;
				int descriptorIndex;
				for (int i = 0; i < codeStream.allLocalsCounter; i++) {
					LocalVariableBinding localVariable = codeStream.locals[i];
					for (int j = 0; j < localVariable.initializationCount; j++) {
						int startPC = localVariable.initializationPCs[j << 1];
						int endPC = localVariable.initializationPCs[(j << 1) + 1];
						if (startPC != endPC) { // only entries for non zero length
							if (endPC == -1) {
								localVariable.declaringScope.problemReporter().abortDueToInternalError(
									Util.bind("abort.invalidAttribute" , new String(localVariable.name)), //$NON-NLS-1$
									(AstNode) localVariable.declaringScope.methodScope().referenceContext);
							}
							if (localContentsOffset + 10 >= (contentsLength = localContents.length)) {
								System.arraycopy(
									contents,
									0,
									(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
									0,
									contentsLength);
							}
							// now we can safely add the local entry
							numberOfEntries++;
							localContents[localContentsOffset++] = (byte) (startPC >> 8);
							localContents[localContentsOffset++] = (byte) startPC;
							int length = endPC - startPC;
							localContents[localContentsOffset++] = (byte) (length >> 8);
							localContents[localContentsOffset++] = (byte) length;
							nameIndex = constantPool.literalIndex(localVariable.name);
							localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
							localContents[localContentsOffset++] = (byte) nameIndex;
							descriptorIndex = constantPool.literalIndex(localVariable.type.signature());
							localContents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
							localContents[localContentsOffset++] = (byte) descriptorIndex;
							int resolvedPosition = localVariable.resolvedPosition;
							localContents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
							localContents[localContentsOffset++] = (byte) resolvedPosition;
						}
					}
				}
				int value = numberOfEntries * 10 + 2;
				localVariableTableOffset += 2;
				localContents[localVariableTableOffset++] = (byte) (value >> 24);
				localContents[localVariableTableOffset++] = (byte) (value >> 16);
				localContents[localVariableTableOffset++] = (byte) (value >> 8);
				localContents[localVariableTableOffset++] = (byte) value;
				localContents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
				localContents[localVariableTableOffset] = (byte) numberOfEntries;
				attributeNumber++;
			}
		}
		// update the number of attributes
		// ensure first that there is enough space available inside the contents array
		if (codeAttributeAttributeOffset + 2
			>= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		localContents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		localContents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		localContents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		localContents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		localContents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		localContents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param codeAttributeOffset <CODE>int</CODE>
	 * @param exceptionHandler int[]
	 * @param startIndexes int[]
	 */
	public void completeCodeAttributeForClinit(
		int codeAttributeOffset,
		int[] exceptionHandler,
		int[] startLineIndexes) {
		// reinitialize the contents with the byte modified by the code stream
		byte[] localContents = contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside contents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int contentsLength;
		int code_length = codeStream.position;
		if (code_length > 65535) {
			codeStream.methodDeclaration.scope.problemReporter().bytecodeExceeds64KLimit(
				codeStream.methodDeclaration.scope.referenceType());
		}
		if (localContentsOffset + 20 >= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		int max_stack = codeStream.stackMax;
		localContents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		localContents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		localContents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		localContents[codeAttributeOffset + 9] = (byte) max_locals;
		localContents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		localContents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		localContents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		localContents[codeAttributeOffset + 13] = (byte) code_length;

		// write the exception table
		localContents[localContentsOffset++] = 0;
		localContents[localContentsOffset++] = 1;
		int start = exceptionHandler[0];
		localContents[localContentsOffset++] = (byte) (start >> 8);
		localContents[localContentsOffset++] = (byte) start;
		int end = exceptionHandler[1];
		localContents[localContentsOffset++] = (byte) (end >> 8);
		localContents[localContentsOffset++] = (byte) end;
		int handlerPC = exceptionHandler[2];
		localContents[localContentsOffset++] = (byte) (handlerPC >> 8);
		localContents[localContentsOffset++] = (byte) handlerPC;
		int nameIndex = constantPool.literalIndexForJavaLangException();
		localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
		localContents[localContentsOffset++] = (byte) nameIndex;

		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0; // leave two bytes for the attribute_length
		localContentsOffset += 2; // first we handle the linenumber attribute

		// first we handle the linenumber attribute
		if (codeStream.generateLineNumberAttributes) {
			/* Create and add the line number attribute (used for debugging) 
			    * Build the pairs of:
			    * (bytecodePC lineNumber)
			    * according to the table of start line indexes and the pcToSourceMap table
			    * contained into the codestream
			    */
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			localContents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			localContents[localContentsOffset++] = (byte) lineNumberNameIndex;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 6;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 1;
			// first entry at pc = 0
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = (byte) (problemLine >> 8);
			localContents[localContentsOffset++] = (byte) problemLine;
			// now we change the size of the line number attribute
			attributeNumber++;
		}
		// then we do the local variable attribute
		if (codeStream.generateLocalVariableTableAttributes) {
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			if (localContentsOffset + 8 >= (contentsLength = localContents.length)) {
				System.arraycopy(
					contents,
					0,
					(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
					0,
					contentsLength);
			}
			localContents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			localContents[localContentsOffset++] = (byte) localVariableNameIndex;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 2;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			attributeNumber++;
		}
		// update the number of attributes
		// ensure first that there is enough space available inside the contents array
		if (codeAttributeAttributeOffset + 2
			>= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		localContents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		localContents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		localContents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		localContents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		localContents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		localContents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param codeAttributeOffset <CODE>int</CODE>
	 * @param exceptionHandler int[] 
	 */
	public void completeCodeAttributeForProblemMethod(
		AbstractMethodDeclaration method,
		MethodBinding binding,
		int codeAttributeOffset,
		int[] exceptionHandler,
		int[] startLineIndexes) {
		// reinitialize the localContents with the byte modified by the code stream
		byte[] localContents = contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside localContents byte array before we started to write// any information about the codeAttribute// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset// to get the right position, 6 for the max_stack etc...
		int max_stack = codeStream.stackMax;
		localContents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		localContents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		localContents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		localContents[codeAttributeOffset + 9] = (byte) max_locals;
		int code_length = codeStream.position;
		localContents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		localContents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		localContents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		localContents[codeAttributeOffset + 13] = (byte) code_length;
		// write the exception table
		int contentsLength;
		if (localContentsOffset + 50 >= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		localContents[localContentsOffset++] = 0;
		localContents[localContentsOffset++] = 1;
		int start = exceptionHandler[0];
		localContents[localContentsOffset++] = (byte) (start >> 8);
		localContents[localContentsOffset++] = (byte) start;
		int end = exceptionHandler[1];
		localContents[localContentsOffset++] = (byte) (end >> 8);
		localContents[localContentsOffset++] = (byte) end;
		int handlerPC = exceptionHandler[2];
		localContents[localContentsOffset++] = (byte) (handlerPC >> 8);
		localContents[localContentsOffset++] = (byte) handlerPC;
		int nameIndex = constantPool.literalIndexForJavaLangException();
		localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
		localContents[localContentsOffset++] = (byte) nameIndex; // debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0; // leave two bytes for the attribute_length
		localContentsOffset += 2; // first we handle the linenumber attribute

		if (codeStream.generateLineNumberAttributes) {
			/* Create and add the line number attribute (used for debugging) 
			    * Build the pairs of:
			    * (bytecodePC lineNumber)
			    * according to the table of start line indexes and the pcToSourceMap table
			    * contained into the codestream
			    */
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			localContents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			localContents[localContentsOffset++] = (byte) lineNumberNameIndex;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 6;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 1;
			if (problemLine == 0) {
				problemLine = searchLineNumber(startLineIndexes, binding.sourceStart());
			}
			// first entry at pc = 0
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = 0;
			localContents[localContentsOffset++] = (byte) (problemLine >> 8);
			localContents[localContentsOffset++] = (byte) problemLine;
			// now we change the size of the line number attribute
			attributeNumber++;
		}
		// then we do the local variable attribute
		if (codeStream.generateLocalVariableTableAttributes) {
			// compute the resolved position for the arguments of the method
			int argSize;
			int localVariableTableOffset = localContentsOffset;
			int numberOfEntries = 0;
			//		codeAttribute.addLocalVariableTableAttribute(this);
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			if (localContentsOffset + 8 >= (contentsLength = localContents.length)) {
				System.arraycopy(
					contents,
					0,
					(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
					0,
					contentsLength);
			}
			localContents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			localContents[localContentsOffset++] = (byte) localVariableNameIndex;
			localContentsOffset += 6;
			// leave space for attribute_length and local_variable_table_length
			int descriptorIndex;
			if (!codeStream.methodDeclaration.isStatic()) {
				numberOfEntries++;
				if (localContentsOffset + 10 >= (contentsLength = localContents.length)) {
					System.arraycopy(
						contents,
						0,
						(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
						0,
						contentsLength);
				}
				localContents[localContentsOffset++] = 0;
				localContents[localContentsOffset++] = 0;
				localContents[localContentsOffset++] = (byte) (code_length >> 8);
				localContents[localContentsOffset++] = (byte) code_length;
				nameIndex = constantPool.literalIndex(QualifiedNamesConstants.This);
				localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
				localContents[localContentsOffset++] = (byte) nameIndex;
				descriptorIndex =
					constantPool.literalIndex(
						codeStream.methodDeclaration.binding.declaringClass.signature());
				localContents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
				localContents[localContentsOffset++] = (byte) descriptorIndex;
				// the resolved position for this is always 0
				localContents[localContentsOffset++] = 0;
				localContents[localContentsOffset++] = 0;
			}
			if (binding.isConstructor()) {
				ReferenceBinding declaringClass = binding.declaringClass;
				if (declaringClass.isNestedType()) {
					NestedTypeBinding methodDeclaringClass = (NestedTypeBinding) declaringClass;
					argSize = methodDeclaringClass.syntheticArgumentsOffset;
					SyntheticArgumentBinding[] syntheticArguments;
					if ((syntheticArguments = methodDeclaringClass.syntheticEnclosingInstances())
						!= null) {
						for (int i = 0, max = syntheticArguments.length; i < max; i++) {
							LocalVariableBinding localVariable = syntheticArguments[i];
							if (localContentsOffset + 10 >= (contentsLength = localContents.length)) {
								System.arraycopy(
									contents,
									0,
									(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
									0,
									contentsLength);
							}
							// now we can safely add the local entry
							numberOfEntries++;
							localContents[localContentsOffset++] = 0;
							localContents[localContentsOffset++] = 0;
							localContents[localContentsOffset++] = (byte) (code_length >> 8);
							localContents[localContentsOffset++] = (byte) code_length;
							nameIndex = constantPool.literalIndex(localVariable.name);
							localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
							localContents[localContentsOffset++] = (byte) nameIndex;
							descriptorIndex = constantPool.literalIndex(localVariable.type.signature());
							localContents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
							localContents[localContentsOffset++] = (byte) descriptorIndex;
							int resolvedPosition = localVariable.resolvedPosition;
							localContents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
							localContents[localContentsOffset++] = (byte) resolvedPosition;
						}
					}
				} else {
					argSize = 1;
				}
			} else {
				argSize = binding.isStatic() ? 0 : 1;
			}
			if (method.binding != null) {
				TypeBinding[] parameters = method.binding.parameters;
				Argument[] arguments = method.arguments;
				if ((parameters != null) && (arguments != null)) {
					for (int i = 0, max = parameters.length; i < max; i++) {
						TypeBinding argumentBinding = parameters[i];
						if (localContentsOffset + 10 >= (contentsLength = localContents.length)) {
							System.arraycopy(
								contents,
								0,
								(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
								0,
								contentsLength);
						}
						// now we can safely add the local entry
						numberOfEntries++;
						localContents[localContentsOffset++] = 0;
						localContents[localContentsOffset++] = 0;
						localContents[localContentsOffset++] = (byte) (code_length >> 8);
						localContents[localContentsOffset++] = (byte) code_length;
						nameIndex = constantPool.literalIndex(arguments[i].name);
						localContents[localContentsOffset++] = (byte) (nameIndex >> 8);
						localContents[localContentsOffset++] = (byte) nameIndex;
						descriptorIndex = constantPool.literalIndex(argumentBinding.signature());
						localContents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
						localContents[localContentsOffset++] = (byte) descriptorIndex;
						int resolvedPosition = argSize;
						if ((argumentBinding == TypeBinding.LongBinding)
							|| (argumentBinding == TypeBinding.DoubleBinding))
							argSize += 2;
						else
							argSize++;
						localContents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
						localContents[localContentsOffset++] = (byte) resolvedPosition;
					}
				}
			}
			int value = numberOfEntries * 10 + 2;
			localVariableTableOffset += 2;
			localContents[localVariableTableOffset++] = (byte) (value >> 24);
			localContents[localVariableTableOffset++] = (byte) (value >> 16);
			localContents[localVariableTableOffset++] = (byte) (value >> 8);
			localContents[localVariableTableOffset++] = (byte) value;
			localContents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
			localContents[localVariableTableOffset] = (byte) numberOfEntries;
			attributeNumber++;
		}
		// update the number of attributes// ensure first that there is enough space available inside the localContents array
		if (codeAttributeAttributeOffset + 2
			>= (contentsLength = localContents.length)) {
			System.arraycopy(
				contents,
				0,
				(localContents = contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		localContents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		localContents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		localContents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		localContents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		localContents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		localContents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.SyntheticAccessMethodBinding
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttributeForSyntheticAccessMethod(
		SyntheticAccessMethodBinding binding,
		int codeAttributeOffset,
		int[] startLineIndexes) {
		// reinitialize the contents with the byte modified by the code stream
		contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside contents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int max_stack = codeStream.stackMax;
		contents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		contents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		contents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		contents[codeAttributeOffset + 9] = (byte) max_locals;
		int code_length = codeStream.position;
		contents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		contents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		contents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		contents[codeAttributeOffset + 13] = (byte) code_length;
		int contentsLength;
		if ((localContentsOffset + 40) >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		// there is no exception table, so we need to offset by 2 the current offset and move 
		// on the attribute generation
		localContentsOffset += 2;
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0;
		// leave two bytes for the attribute_length
		localContentsOffset += 2;

		// first we handle the linenumber attribute
		if (codeStream.generateLineNumberAttributes) {
			int index = 0;
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			contents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			contents[localContentsOffset++] = (byte) lineNumberNameIndex;
			int lineNumberTableOffset = localContentsOffset;
			localContentsOffset += 6;
			// leave space for attribute_length and line_number_table_length
			// Seems like do would be better, but this preserves the existing behavior.
			index = searchLineNumber(startLineIndexes, binding.sourceStart);
			contents[localContentsOffset++] = 0;
			contents[localContentsOffset++] = 0;
			contents[localContentsOffset++] = (byte) (index >> 8);
			contents[localContentsOffset++] = (byte) index;
			// now we change the size of the line number attribute
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 6;
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 1;
			attributeNumber++;
		}
		// then we do the local variable attribute
		if (codeStream.generateLocalVariableTableAttributes) {
			int localVariableTableOffset = localContentsOffset;
			int numberOfEntries = 0;
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			if (localContentsOffset + 8 > (contentsLength = contents.length)) {
				System.arraycopy(
					contents,
					0,
					(contents = new byte[contentsLength + INCREMENT_SIZE]),
					0,
					contentsLength);
			}
			contents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			contents[localContentsOffset++] = (byte) localVariableNameIndex;
			localContentsOffset += 6;
			// leave space for attribute_length and local_variable_table_length
			int nameIndex;
			int descriptorIndex;
			for (int i = 0; i < codeStream.allLocalsCounter; i++) {
				LocalVariableBinding localVariable = codeStream.locals[i];
				for (int j = 0; j < localVariable.initializationCount; j++) {
					int startPC = localVariable.initializationPCs[j << 1];
					int endPC = localVariable.initializationPCs[(j << 1) + 1];
					if (startPC != endPC) { // only entries for non zero length
						if (endPC == -1) {
							localVariable.declaringScope.problemReporter().abortDueToInternalError(
								Util.bind("abort.invalidAttribute" , new String(localVariable.name)), //$NON-NLS-1$
								(AstNode) localVariable.declaringScope.methodScope().referenceContext);
						}
						if (localContentsOffset + 10 > (contentsLength = contents.length)) {
							System.arraycopy(
								contents,
								0,
								(contents = new byte[contentsLength + INCREMENT_SIZE]),
								0,
								contentsLength);
						}
						// now we can safely add the local entry
						numberOfEntries++;
						contents[localContentsOffset++] = (byte) (startPC >> 8);
						contents[localContentsOffset++] = (byte) startPC;
						int length = endPC - startPC;
						contents[localContentsOffset++] = (byte) (length >> 8);
						contents[localContentsOffset++] = (byte) length;
						nameIndex = constantPool.literalIndex(localVariable.name);
						contents[localContentsOffset++] = (byte) (nameIndex >> 8);
						contents[localContentsOffset++] = (byte) nameIndex;
						descriptorIndex = constantPool.literalIndex(localVariable.type.signature());
						contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
						contents[localContentsOffset++] = (byte) descriptorIndex;
						int resolvedPosition = localVariable.resolvedPosition;
						contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
						contents[localContentsOffset++] = (byte) resolvedPosition;
					}
				}
			}
			int value = numberOfEntries * 10 + 2;
			localVariableTableOffset += 2;
			contents[localVariableTableOffset++] = (byte) (value >> 24);
			contents[localVariableTableOffset++] = (byte) (value >> 16);
			contents[localVariableTableOffset++] = (byte) (value >> 8);
			contents[localVariableTableOffset++] = (byte) value;
			contents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
			contents[localVariableTableOffset] = (byte) numberOfEntries;
			attributeNumber++;
		}
		// update the number of attributes
		// ensure first that there is enough space available inside the contents array
		if (codeAttributeAttributeOffset + 2 >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		contents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[codeAttributeAttributeOffset] = (byte) attributeNumber;

		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Complete the creation of a method info by setting up the number of attributes at the right offset.
	 *
	 * @param methodAttributeOffset <CODE>int</CODE>
	 * @param attributeNumber <CODE>int</CODE> 
	 */
	public void completeMethodInfo(
		int methodAttributeOffset,
		int attributeNumber) {
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;
	}

	/*
	 * INTERNAL USE-ONLY
	 * Innerclasses get their name computed as they are generated, since some may not
	 * be actually outputed if sitting inside unreachable code.
	 *
	 * @param localType org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding
	 */
	public char[] computeConstantPoolName(LocalTypeBinding localType) {
		if (localType.constantPoolName() != null) {
			return localType.constantPoolName();
		}
		// delegates to the outermost enclosing classfile, since it is the only one with a global vision of its innertypes.
		if (enclosingClassFile != null) {
			return this.outerMostEnclosingClassFile().computeConstantPoolName(localType);
		}
		if (nameUsage == null)
			nameUsage = new HashtableOfType();

		// ensure there is not already such a local type name defined by the user
		int index = 0;
		char[] candidateName;
		while(true) {
			if (localType.isMemberType()){
				if (index == 0){
					candidateName = CharOperation.concat(
						localType.enclosingType().constantPoolName(),
						localType.sourceName,
						'$');
				} else {
					// in case of collision, then member name gets extra $1 inserted
					// e.g. class X { { class L{} new X(){ class L{} } } }
					candidateName = CharOperation.concat(
						localType.enclosingType().constantPoolName(),
						'$',
						String.valueOf(index).toCharArray(),
						'$',
						localType.sourceName);
				}
			} else if (localType.isAnonymousType()){
					candidateName = CharOperation.concat(
						referenceBinding.constantPoolName(),
						String.valueOf(index+1).toCharArray(),
						'$');
			} else {
					candidateName = CharOperation.concat(
						referenceBinding.constantPoolName(),
						'$',
						String.valueOf(index+1).toCharArray(),
						'$',
						localType.sourceName);
			}						
			if (nameUsage.get(candidateName) != null) {
				index ++;
			} else {
				nameUsage.put(candidateName, localType);
				break;
			}
		}
		return candidateName;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Request the creation of a ClassFile compatible representation of a problematic type
	 *
	 * @param typeDeclaration org.eclipse.jdt.internal.compiler.ast.TypeDeclaration
	 * @param unitResult org.eclipse.jdt.internal.compiler.CompilationUnitResult
	 */
	public static void createProblemType(
		TypeDeclaration typeDeclaration,
		CompilationResult unitResult) {
		SourceTypeBinding typeBinding = typeDeclaration.binding;
		ClassFile classFile = new ClassFile(typeBinding, null, true);

		// inner attributes
		if (typeBinding.isMemberType())
			classFile.recordEnclosingTypeAttributes(typeBinding);

		// add its fields
		FieldBinding[] fields = typeBinding.fields;
		if ((fields != null) && (fields != NoFields)) {
			for (int i = 0, max = fields.length; i < max; i++) {
				if (fields[i].constant == null) {
					FieldReference.getConstantFor(fields[i], false, null, null, 0);
				}
			}
			classFile.addFieldInfos();
		} else {
			// we have to set the number of fields to be equals to 0
			classFile.contents[classFile.contentsOffset++] = 0;
			classFile.contents[classFile.contentsOffset++] = 0;
		}
		// leave some space for the methodCount
		classFile.setForMethodInfos();
		// add its user defined methods
		MethodBinding[] methods = typeBinding.methods;
		AbstractMethodDeclaration[] methodDeclarations = typeDeclaration.methods;
		int maxMethodDecl = methodDeclarations == null ? 0 : methodDeclarations.length;
		int problemsLength;
		IProblem[] problems = unitResult.getProblems();
		if (problems == null) {
			problems = new IProblem[0];
		}
		IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
		System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
		if (methods != null) {
			if (typeBinding.isInterface()) {
				// we cannot create problem methods for an interface. So we have to generate a clinit
				// which should contain all the problem
				classFile.addProblemClinit(problemsCopy);
				for (int i = 0, max = methods.length; i < max; i++) {
					MethodBinding methodBinding;
					if ((methodBinding = methods[i]) != null) {
						// find the corresponding method declaration
						for (int j = 0; j < maxMethodDecl; j++) {
							if ((methodDeclarations[j] != null)
								&& (methodDeclarations[j].binding == methods[i])) {
								if (!methodBinding.isConstructor()) {
									classFile.addAbstractMethod(methodDeclarations[j], methodBinding);
								}
								break;
							}
						}
					}
				}
			} else {
				for (int i = 0, max = methods.length; i < max; i++) {
					MethodBinding methodBinding;
					if ((methodBinding = methods[i]) != null) {
						// find the corresponding method declaration
						for (int j = 0; j < maxMethodDecl; j++) {
							if ((methodDeclarations[j] != null)
								&& (methodDeclarations[j].binding == methods[i])) {
								AbstractMethodDeclaration methodDecl;
								if ((methodDecl = methodDeclarations[j]).isConstructor()) {
									classFile.addProblemConstructor(methodDecl, methodBinding, problemsCopy);
								} else {
									classFile.addProblemMethod(methodDecl, methodBinding, problemsCopy);
								}
								break;
							}
						}
					}
				}
			}
			// add abstract methods
			classFile.addDefaultAbstractMethods();
		}
		// propagate generation of (problem) member types
		if (typeDeclaration.memberTypes != null) {
			for (int i = 0, max = typeDeclaration.memberTypes.length; i < max; i++) {
				TypeDeclaration memberType = typeDeclaration.memberTypes[i];
				if (memberType.binding != null) {
					classFile.recordNestedMemberAttribute(memberType.binding);
					ClassFile.createProblemType(memberType, unitResult);
				}
			}
		}
		classFile.addAttributes();
		unitResult.record(typeBinding.constantPoolName(), classFile);
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods returns a char[] representing the file name of the receiver
	 *
	 * @return char[]
	 */
	public char[] fileName() {
		return constantPool.UTF8Cache.returnKeyFor(1);
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method generates the header of a code attribute.
	 * - the index inside the constant pool for the attribute name (i.e.&nbsp;Code)
	 * - leave some space for attribute_length(4), max_stack(2), max_locals(2), code_length(4).
	 */
	public void generateCodeAttributeHeader() {
		int contentsLength;
		if (contentsOffset + 20 >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		int constantValueNameIndex =
			constantPool.literalIndex(AttributeNamesConstants.CodeName);
		contents[contentsOffset++] = (byte) (constantValueNameIndex >> 8);
		contents[contentsOffset++] = (byte) constantValueNameIndex;
		// leave space for attribute_length(4), max_stack(2), max_locals(2), code_length(4)
		contentsOffset += 12;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method generates the attributes of a code attribute.
	 * They could be:
	 * - an exception attribute for each try/catch found inside the method
	 * - a deprecated attribute
	 * - a synthetic attribute for synthetic access methods
	 *
	 * It returns the number of attributes created for the code attribute.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
	 * @return <CODE>int</CODE>
	 */
	public int generateMethodInfoAttribute(MethodBinding methodBinding) {
		return generateMethodInfoAttribute(methodBinding, null);
	}

	public int generateMethodInfoAttribute(MethodBinding methodBinding, List extraAttributes) {
		// leave two bytes for the attribute_number
		contentsOffset += 2;
		// now we can handle all the attribute for that method info:
		// it could be:
		// - a CodeAttribute
		// - a ExceptionAttribute
		// - a DeprecatedAttribute
		// - a SyntheticAttribute

		// Exception attribute
		ReferenceBinding[] thrownsExceptions;
		int contentsLength;
		int attributeNumber = 0;
		if ((thrownsExceptions = methodBinding.thrownExceptions) != NoExceptions) {
			// The method has a throw clause. So we need to add an exception attribute
			// check that there is enough space to write all the bytes for the exception attribute
			int length = thrownsExceptions.length;
			if (contentsOffset + (8 + length * 2) >= (contentsLength = contents.length)) {
				System.arraycopy(
					contents,
					0,
					(contents =
						new byte[contentsLength + Math.max(INCREMENT_SIZE, (8 + length * 2))]),
					0,
					contentsLength);
			}
			int exceptionNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.ExceptionsName);
			contents[contentsOffset++] = (byte) (exceptionNameIndex >> 8);
			contents[contentsOffset++] = (byte) exceptionNameIndex;
			// The attribute length = length * 2 + 2 in case of a exception attribute
			int attributeLength = length * 2 + 2;
			contents[contentsOffset++] = (byte) (attributeLength >> 24);
			contents[contentsOffset++] = (byte) (attributeLength >> 16);
			contents[contentsOffset++] = (byte) (attributeLength >> 8);
			contents[contentsOffset++] = (byte) attributeLength;
			contents[contentsOffset++] = (byte) (length >> 8);
			contents[contentsOffset++] = (byte) length;
			for (int i = 0; i < length; i++) {
				int exceptionIndex = constantPool.literalIndex(thrownsExceptions[i]);
				contents[contentsOffset++] = (byte) (exceptionIndex >> 8);
				contents[contentsOffset++] = (byte) exceptionIndex;
			}
			attributeNumber++;
		}
		// Deprecated attribute
		// Check that there is enough space to write the deprecated attribute
		if (contentsOffset + 6 >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		if (methodBinding.isDeprecated()) {
			int deprecatedAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
			contents[contentsOffset++] = (byte) (deprecatedAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) deprecatedAttributeNameIndex;
			// the length of a deprecated attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;

			attributeNumber++;
		}
		// Synthetic attribute
		// Check that there is enough space to write the deprecated attribute
		if (contentsOffset + 6 >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		if (methodBinding.isSynthetic()) {
			int syntheticAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
			contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
			// the length of a synthetic attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;

			attributeNumber++;
		}
		
		if (extraAttributes != null) {
			for (int i=0, len = extraAttributes.size(); i < len; i++) {
				IAttribute attribute = (IAttribute)extraAttributes.get(i);
				short nameIndex = (short)constantPool.literalIndex(attribute.getNameChars());
				writeToContents(attribute.getAllBytes(nameIndex));
				attributeNumber++;
			}
		}
		
		return attributeNumber;
	}
	
	void writeToContents(byte[] data) {
		int N = data.length;
		int contentsLength;
		while (contentsOffset + N >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		
		System.arraycopy(data, 0, contents, contentsOffset, N);
		contentsOffset += N;		
	}
	
	

	/**
	 * INTERNAL USE-ONLY
	 * That method generates the header of a method info:
	 * The header consists in:
	 * - the access flags
	 * - the name index of the method name inside the constant pool
	 * - the descriptor index of the signature of the method inside the constant pool.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
	 */
	public void generateMethodInfoHeader(MethodBinding methodBinding) {
		// check that there is enough space to write all the bytes for the method info corresponding
		// to the @methodBinding
		int contentsLength;
		methodCount++; // add one more method
		if (contentsOffset + 10 >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		int accessFlags = methodBinding.getAccessFlags();
		if (methodBinding.isRequiredToClearPrivateModifier()) {
			accessFlags &= ~AccPrivate;
		}
		contents[contentsOffset++] = (byte) (accessFlags >> 8);
		contents[contentsOffset++] = (byte) accessFlags;
		int nameIndex = constantPool.literalIndex(methodBinding.selector);
		contents[contentsOffset++] = (byte) (nameIndex >> 8);
		contents[contentsOffset++] = (byte) nameIndex;
		int descriptorIndex = constantPool.literalIndex(methodBinding.signature());
		contents[contentsOffset++] = (byte) (descriptorIndex >> 8);
		contents[contentsOffset++] = (byte) descriptorIndex;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method generates the method info header of a clinit:
	 * The header consists in:
	 * - the access flags (always default access + static)
	 * - the name index of the method name (always <clinit>) inside the constant pool 
	 * - the descriptor index of the signature (always ()V) of the method inside the constant pool.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
	 */
	public void generateMethodInfoHeaderForClinit() {
		// check that there is enough space to write all the bytes for the method info corresponding
		// to the @methodBinding
		int contentsLength;
		methodCount++; // add one more method
		if (contentsOffset + 10 >= (contentsLength = contents.length)) {
			System.arraycopy(
				contents,
				0,
				(contents = new byte[contentsLength + INCREMENT_SIZE]),
				0,
				contentsLength);
		}
		contents[contentsOffset++] = (byte) ((AccDefault | AccStatic) >> 8);
		contents[contentsOffset++] = (byte) (AccDefault | AccStatic);
		int nameIndex = constantPool.literalIndex(QualifiedNamesConstants.Clinit);
		contents[contentsOffset++] = (byte) (nameIndex >> 8);
		contents[contentsOffset++] = (byte) nameIndex;
		int descriptorIndex =
			constantPool.literalIndex(QualifiedNamesConstants.ClinitSignature);
		contents[contentsOffset++] = (byte) (descriptorIndex >> 8);
		contents[contentsOffset++] = (byte) descriptorIndex;
		// We know that we won't get more than 1 attribute: the code attribute
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 1;
	}

	/**
	 * EXTERNAL API
	 * Answer the actual bytes of the class file
	 *
	 * This method encodes the receiver structure into a byte array which is the content of the classfile.
	 * Returns the byte array that represents the encoded structure of the receiver.
	 *
	 * @return byte[]
	 */
	public byte[] getBytes() {
			byte[] fullContents = new byte[headerOffset + contentsOffset];
			System.arraycopy(header, 0, fullContents, 0, headerOffset);
			System.arraycopy(contents, 0, fullContents, headerOffset, contentsOffset);
		return fullContents;
		}
		
	/**
	 * EXTERNAL API
	 * Answer the compound name of the class file.
	 * @return char[][]
	 * e.g. {{java}, {util}, {Hashtable}}.
	 */
	public char[][] getCompoundName() {
		return CharOperation.splitOn('/', fileName());
	}

	/**
	 * INTERNAL USE-ONLY
	 * Returns the most enclosing classfile of the receiver. This is used know to store the constant pool name
	 * for all inner types of the receiver.
	 * @return org.eclipse.jdt.internal.compiler.codegen.ClassFile
	 */
	public ClassFile outerMostEnclosingClassFile() {
		ClassFile current = this;
		while (current.enclosingClassFile != null)
			current = current.enclosingClassFile;
		return current;
	}

	/**
	 * INTERNAL USE-ONLY
	 * This is used to store a new inner class. It checks that the binding @binding doesn't already exist inside the
	 * collection of inner classes. Add all the necessary classes in the right order to fit to the specifications.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
	 */
	public void recordEnclosingTypeAttributes(ReferenceBinding binding) {
		// add all the enclosing types
		ReferenceBinding enclosingType = referenceBinding.enclosingType();
		int depth = 0;
		while (enclosingType != null) {
			depth++;
			enclosingType = enclosingType.enclosingType();
		}
		enclosingType = referenceBinding;
		ReferenceBinding enclosingTypes[];
		if (depth >= 2) {
			enclosingTypes = new ReferenceBinding[depth];
			for (int i = depth - 1; i >= 0; i--) {
				enclosingTypes[i] = enclosingType;
				enclosingType = enclosingType.enclosingType();
			}
			for (int i = 0; i < depth; i++) {
				addInnerClasses(enclosingTypes[i]);
			}
		} else {
			addInnerClasses(referenceBinding);
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * This is used to store a new inner class. It checks that the binding @binding doesn't already exist inside the
	 * collection of inner classes. Add all the necessary classes in the right order to fit to the specifications.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
	 */
	public void recordNestedLocalAttribute(ReferenceBinding binding) {
		// add all the enclosing types
		ReferenceBinding enclosingType = referenceBinding.enclosingType();
		int depth = 0;
		while (enclosingType != null) {
			depth++;
			enclosingType = enclosingType.enclosingType();
		}
		enclosingType = referenceBinding;
		ReferenceBinding enclosingTypes[];
		if (depth >= 2) {
			enclosingTypes = new ReferenceBinding[depth];
			for (int i = depth - 1; i >= 0; i--) {
				enclosingTypes[i] = enclosingType;
				enclosingType = enclosingType.enclosingType();
			}
			for (int i = 0; i < depth; i++)
				addInnerClasses(enclosingTypes[i]);
		} else {
			addInnerClasses(binding);
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * This is used to store a new inner class. It checks that the binding @binding doesn't already exist inside the
	 * collection of inner classes. Add all the necessary classes in the right order to fit to the specifications.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
	 */
	public void recordNestedMemberAttribute(ReferenceBinding binding) {
		addInnerClasses(binding);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Search the line number corresponding to a specific position
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public static final int searchLineNumber(
		int[] startLineIndexes,
		int position) {
		// this code is completely useless, but it is the same implementation than
		// org.eclipse.jdt.internal.compiler.problem.ProblemHandler.searchLineNumber(int[], int)
		// if (startLineIndexes == null)
		//	return 1;
		int length = startLineIndexes.length;
		if (length == 0)
			return 1;
		int g = 0, d = length - 1;
		int m = 0;
		while (g <= d) {
			m = (g + d) / 2;
			if (position < startLineIndexes[m]) {
				d = m - 1;
			} else
				if (position > startLineIndexes[m]) {
					g = m + 1;
				} else {
					return m + 1;
				}
		}
		if (position < startLineIndexes[m]) {
			return m + 1;
		}
		return m + 2;
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods leaves the space for method counts recording.
	 */
	public void setForMethodInfos() {
		// leave some space for the methodCount
		methodCountOffset = contentsOffset;
		contentsOffset += 2;
	}

	/**
	 * INTERNAL USE-ONLY
	 * outputPath is formed like:
	 *	   c:\temp\ the last character is a file separator
	 * relativeFileName is formed like:
	 *     java\lang\String.class
	 * @param generatePackagesStructure a flag to know if the packages structure has to be generated.
	 * @param outputPath the output directory
	 * @param relativeFileName java.lang.String
	 * @param contents byte[]
	 * 
	 */
	public static void writeToDisk(
		boolean generatePackagesStructure,
		String outputPath,
		String relativeFileName,
		byte[] contents)
		throws IOException {
			
		BufferedOutputStream output = null;
		if (generatePackagesStructure) {
			output = new BufferedOutputStream(
				new FileOutputStream(
						new File(buildAllDirectoriesInto(outputPath, relativeFileName))));
		} else {
			String fileName = null;
			char fileSeparatorChar = File.separatorChar;
			String fileSeparator = File.separator;
			// First we ensure that the outputPath exists
			outputPath = outputPath.replace('/', fileSeparatorChar);
			// To be able to pass the mkdirs() method we need to remove the extra file separator at the end of the outDir name
			int indexOfPackageSeparator = relativeFileName.lastIndexOf(fileSeparatorChar);
			if (indexOfPackageSeparator == -1) {
				if (outputPath.endsWith(fileSeparator)) {
					fileName = outputPath + relativeFileName;
				} else {
					fileName = outputPath + fileSeparator + relativeFileName;
				}
			} else {
				int length = relativeFileName.length();
				if (outputPath.endsWith(fileSeparator)) {
					fileName = outputPath + relativeFileName.substring(indexOfPackageSeparator + 1, length);
				} else {
					fileName = outputPath + fileSeparator + relativeFileName.substring(indexOfPackageSeparator + 1, length);
				}
			}
			output = new BufferedOutputStream(
				new FileOutputStream(
						new File(fileName)));
		}
		try {
			output.write(contents);
		} finally {
			output.flush();
			output.close();
		}
	}
}