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
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.NullConstant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.*;

import java.io.*;
import java.util.Arrays;

public class ClassFileReader extends ClassFileStruct implements AttributeNamesConstants, IBinaryType {
	private int constantPoolCount;
	private int[] constantPoolOffsets;
	private int accessFlags;
	private char[] className;
	private char[] superclassName;
	private int interfacesCount;
	private char[][] interfaceNames;
	private int fieldsCount;
	private FieldInfo[] fields;
	private int methodsCount;
	private MethodInfo[] methods;
	private InnerClassInfo[] innerInfos;
	private char[] sourceFileName;
	// initialized in case the .class file is a nested type
	private InnerClassInfo innerInfo;
	private char[] classFileName;
	private int classNameIndex;
	private int innerInfoIndex;
/**
 * @param classFileBytes byte[]
 * 		Actual bytes of a .class file
 * 
 * @param fileName char[]
 * 		Actual name of the file that contains the bytes, can be null
 * 
 * @param fullyInitialize boolean
 * 		Flag to fully initialize the new object
 * @exception ClassFormatException
 */
public ClassFileReader(byte[] classFileBytes, char[] fileName, boolean fullyInitialize) throws ClassFormatException {
	// This method looks ugly but is actually quite simple, the constantPool is constructed
	// in 3 passes.  All non-primitive constant pool members that usually refer to other members
	// by index are tweaked to have their value in inst vars, this minor cost at read-time makes
	// all subsequent uses of the constant pool element faster.
	super(classFileBytes, 0);
	this.classFileName = fileName;
	int readOffset = 10;
	try {
		constantPoolCount = this.u2At(8);
		// Pass #1 - Fill in all primitive constants
		this.constantPoolOffsets = new int[constantPoolCount];
		for (int i = 1; i < constantPoolCount; i++) {
			int tag = this.u1At(readOffset);
			switch (tag) {
				case Utf8Tag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += u2At(readOffset + 1);
					readOffset += ConstantUtf8FixedSize;
					break;
				case IntegerTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantIntegerFixedSize;
					break;
				case FloatTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantFloatFixedSize;
					break;
				case LongTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantLongFixedSize;
					i++;
					break;
				case DoubleTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantDoubleFixedSize;
					i++;
					break;
				case ClassTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantClassFixedSize;
					break;
				case StringTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantStringFixedSize;
					break;
				case FieldRefTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantFieldRefFixedSize;
					break;
				case MethodRefTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantMethodRefFixedSize;
					break;
				case InterfaceMethodRefTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantInterfaceMethodRefFixedSize;
					break;
				case NameAndTypeTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ConstantNameAndTypeFixedSize;
			}
		}
		// Read and validate access flags
		this.accessFlags = u2At(readOffset);
		readOffset += 2;

		// Read the classname, use exception handlers to catch bad format
		this.classNameIndex = u2At(readOffset);
		this.className = getConstantClassNameAt(this.classNameIndex);
		readOffset += 2;

		// Read the superclass name, can be null for java.lang.Object
		int superclassNameIndex = u2At(readOffset);
		readOffset += 2;
		// if superclassNameIndex is equals to 0 there is no need to set a value for the 
		// field this.superclassName. null is fine.
		if (superclassNameIndex != 0) {
			this.superclassName = getConstantClassNameAt(superclassNameIndex);
		}

		// Read the interfaces, use exception handlers to catch bad format
		this.interfacesCount = u2At(readOffset);
		readOffset += 2;
		if (this.interfacesCount != 0) {
			this.interfaceNames = new char[this.interfacesCount][];
			for (int i = 0; i < this.interfacesCount; i++) {
				this.interfaceNames[i] = getConstantClassNameAt(u2At(readOffset));
				readOffset += 2;
			}
		}
		// Read the this.fields, use exception handlers to catch bad format
		this.fieldsCount = u2At(readOffset);
		readOffset += 2;
		if (this.fieldsCount != 0) {
			FieldInfo field;
			this.fields = new FieldInfo[this.fieldsCount];
			for (int i = 0; i < this.fieldsCount; i++) {
				field = new FieldInfo(reference, this.constantPoolOffsets, readOffset);
				this.fields[i] = field;
				readOffset += field.sizeInBytes();
			}
		}
		// Read the this.methods
		this.methodsCount = u2At(readOffset);
		readOffset += 2;
		if (this.methodsCount != 0) {
			this.methods = new MethodInfo[this.methodsCount];
			MethodInfo method;
			for (int i = 0; i < this.methodsCount; i++) {
				method = new MethodInfo(reference, this.constantPoolOffsets, readOffset);
				this.methods[i] = method;
				readOffset += method.sizeInBytes();
			}
		}

		// Read the attributes
		int attributesCount = u2At(readOffset);
		readOffset += 2;

		for (int i = 0; i < attributesCount; i++) {
			int utf8Offset = this.constantPoolOffsets[u2At(readOffset)];
			char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
			if (CharOperation.equals(attributeName, DeprecatedName)) {
				this.accessFlags |= AccDeprecated;
			} else {
				if (CharOperation.equals(attributeName, InnerClassName)) {
					int innerOffset = readOffset + 6;
					int number_of_classes = u2At(innerOffset);
					if (number_of_classes != 0) {
						this.innerInfos = new InnerClassInfo[number_of_classes];
						for (int j = 0; j < number_of_classes; j++) {
							this.innerInfos[j] = 
								new InnerClassInfo(reference, this.constantPoolOffsets, innerOffset + 2); 
							if (this.classNameIndex == this.innerInfos[j].innerClassNameIndex) {
								this.innerInfo = this.innerInfos[j];
								this.innerInfoIndex = j;
							}
							innerOffset += 8;
						}
					}
				} else {
					if (CharOperation.equals(attributeName, SourceName)) {
						utf8Offset = this.constantPoolOffsets[u2At(readOffset + 6)];
						this.sourceFileName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
					} else {
						if (CharOperation.equals(attributeName, SyntheticName)) {
							this.accessFlags |= AccSynthetic;
						}
					}
				}
			}
			readOffset += (6 + u4At(readOffset + 2));
		}
		if (fullyInitialize) {
			this.initialize();
		}
	} catch (Exception e) {
		throw new ClassFormatException(
			ClassFormatException.ErrTruncatedInput, 
			readOffset); 
	}
}

/**
 * @param classFileBytes Actual bytes of a .class file
 * @param fileName	Actual name of the file that contains the bytes, can be null
 * 
 * @exception ClassFormatException
 */
public ClassFileReader(byte classFileBytes[], char[] fileName) throws ClassFormatException {
	this(classFileBytes, fileName, false);
}

/**
 * 	Answer the receiver's access flags.  The value of the access_flags
 *	item is a mask of modifiers used with class and interface declarations.
 *  @return int 
 */
public int accessFlags() {
	return this.accessFlags;
}
/**
 * Answer the char array that corresponds to the class name of the constant class.
 * constantPoolIndex is the index in the constant pool that is a constant class entry.
 *
 * @param int constantPoolIndex
 * @return char[]
 */
private char[] getConstantClassNameAt(int constantPoolIndex) {
	int utf8Offset = this.constantPoolOffsets[u2At(this.constantPoolOffsets[constantPoolIndex] + 1)];
	return utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
}
/**
 * Answer the int array that corresponds to all the offsets of each entry in the constant pool
 *
 * @return int[]
 */
public int[] getConstantPoolOffsets() {
	return this.constantPoolOffsets;
}
/*
 * Answer the resolved compoundName of the enclosing type
 * or null if the receiver is a top level type.
 */
public char[] getEnclosingTypeName() {
	if (this.innerInfo != null && !this.isAnonymous()) {
		return this.innerInfo.getEnclosingTypeName();
	}
	return null;
}
/**
 * Answer the receiver's this.fields or null if the array is empty.
 * @return org.eclipse.jdt.internal.compiler.api.IBinaryField[]
 */
public IBinaryField[] getFields() {
	return this.fields;
}
/**
 * Answer the file name which defines the type.
 * The format is unspecified.
 */
public char[] getFileName() {
	return this.classFileName;
}
/**
 * Answer the source name if the receiver is a inner type. Return null if it is an anonymous class or if the receiver is a top-level class.
 * e.g.
 * public class A {
 *	public class B {
 *	}
 *	public void foo() {
 *		class C {}
 *	}
 *	public Runnable bar() {
 *		return new Runnable() {
 *			public void run() {}
 *		};
 *	}
 * }
 * It returns {'B'} for the member A$B
 * It returns null for A
 * It returns {'C'} for the local class A$1$C
 * It returns null for the anonymous A$1
 * @return char[]
 */
public char[] getInnerSourceName() {
	if (this.innerInfo != null)
		return this.innerInfo.getSourceName();
	return null;
}
/**
 * Answer the resolved names of the receiver's interfaces in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[][]
 */
public char[][] getInterfaceNames() {
	return this.interfaceNames;
}
/**
 * Answer the receiver's nested types or null if the array is empty.
 *
 * This nested type info is extracted from the inner class attributes.
 * Ask the name environment to find a member type using its compound name
 * @return org.eclipse.jdt.internal.compiler.api.IBinaryNestedType[]
 */
public IBinaryNestedType[] getMemberTypes() {
	// we might have some member types of the current type
	if (this.innerInfos == null) return null;

	int length = this.innerInfos.length;
	int startingIndex = this.innerInfo != null ? this.innerInfoIndex + 1 : 0;
	if (length != startingIndex) {
		IBinaryNestedType[] memberTypes = 
			new IBinaryNestedType[length - this.innerInfoIndex]; 
		int memberTypeIndex = 0;
		for (int i = startingIndex; i < length; i++) {
			InnerClassInfo currentInnerInfo = this.innerInfos[i];
			int outerClassNameIdx = currentInnerInfo.outerClassNameIndex;
			int innerNameIndex = currentInnerInfo.innerNameIndex;
			/*
			 * Checking that outerClassNameIDx is different from 0 should be enough to determine if an inner class
			 * attribute entry is a member class, but due to the bug:
			 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14592
			 * we needed to add an extra check. So we check that innerNameIndex is different from 0 as well.
			 */
			if (outerClassNameIdx != 0 && innerNameIndex != 0 && outerClassNameIdx == this.classNameIndex) {
				memberTypes[memberTypeIndex++] = currentInnerInfo;
			}
		}
		if (memberTypeIndex == 0) return null;
		if (memberTypeIndex != memberTypes.length) {
			// we need to resize the memberTypes array. Some local or anonymous classes
			// are present in the current class.
			System.arraycopy(
				memberTypes, 
				0, 
				(memberTypes = new IBinaryNestedType[memberTypeIndex]), 
				0, 
				memberTypeIndex); 
		}
		return memberTypes;
	}
	return null;
}
/**
 * Answer the receiver's this.methods or null if the array is empty.
 * @return org.eclipse.jdt.internal.compiler.api.env.IBinaryMethod[]
 */
public IBinaryMethod[] getMethods() {
	return this.methods;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * Set the AccDeprecated and AccSynthetic bits if necessary
 * @return int
 */
public int getModifiers() {
	if (this.innerInfo != null) {
		return this.innerInfo.getModifiers();
	}
	return this.accessFlags;
}
/**
 * Answer the resolved name of the type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[]
 */
public char[] getName() {
	return this.className;
}
/**
 * Answer the resolved name of the receiver's superclass in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if it does not have one.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[]
 */
public char[] getSuperclassName() {
	return this.superclassName;
}
/**
 * Answer true if the receiver is an anonymous type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isAnonymous() {
	if (this.innerInfo == null) return false;
	char[] sourceName = this.innerInfo.getSourceName();
	return (sourceName == null || sourceName.length == 0);
}
/**
 * Answer whether the receiver contains the resolved binary form
 * or the unresolved source form of the type.
 * @return boolean
 */
public boolean isBinaryType() {
	return true;
}
/**
 * Answer true if the receiver is a class. False otherwise.
 * @return boolean
 */
public boolean isClass() {
	return (getModifiers() & AccInterface) == 0;
}
/**
 * Answer true if the receiver is an interface. False otherwise.
 * @return boolean
 */
public boolean isInterface() {
	return (getModifiers() & AccInterface) != 0;
}
/**
 * Answer true if the receiver is a local type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isLocal() {
	return 
		this.innerInfo != null 
		&& this.innerInfo.getEnclosingTypeName() == null 
		&& this.innerInfo.getSourceName() != null;
}
/**
 * Answer true if the receiver is a member type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isMember() {
	return this.innerInfo != null && this.innerInfo.getEnclosingTypeName() != null;
}
/**
 * Answer true if the receiver is a nested type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isNestedType() {
	return this.innerInfo != null;
}
public static ClassFileReader read(File file) throws ClassFormatException, IOException {
	return read(file, false);
}
public static ClassFileReader read(File file, boolean fullyInitialize) throws ClassFormatException, IOException {
	byte classFileBytes[] = Util.getFileByteContent(file);
	ClassFileReader classFileReader = new ClassFileReader(classFileBytes, file.getAbsolutePath().toCharArray());
	if (fullyInitialize) {
		classFileReader.initialize();
	}
	return classFileReader;
}
public static ClassFileReader read(String fileName) throws ClassFormatException, java.io.IOException {
	return read(fileName, false);
}
public static ClassFileReader read(String fileName, boolean fullyInitialize) throws ClassFormatException, java.io.IOException {
	return read(new File(fileName), fullyInitialize);
}
public static ClassFileReader read(
	java.util.zip.ZipFile zip, 
	String filename)
	throws ClassFormatException, java.io.IOException {
		return read(zip, filename, false);
}
public static ClassFileReader read(
	java.util.zip.ZipFile zip, 
	String filename,
	boolean fullyInitialize)
	throws ClassFormatException, java.io.IOException {
	java.util.zip.ZipEntry ze = zip.getEntry(filename);
	if (ze == null)
		return null;
	byte classFileBytes[] = Util.getZipEntryByteContent(ze, zip);
	ClassFileReader classFileReader = new ClassFileReader(classFileBytes, filename.toCharArray());
	if (fullyInitialize) {
		classFileReader.initialize();
	}
	return classFileReader;
}

/**
 * Answer the source file name attribute. Return null if there is no source file attribute for the receiver.
 * 
 * @return char[]
 */
public char[] sourceFileName() {
	return this.sourceFileName;
}
public String toString() {
	java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
	java.io.PrintWriter print = new java.io.PrintWriter(out);
	
	print.println(this.getClass().getName() + "{"); //$NON-NLS-1$
	print.println(" this.className: " + new String(getName())); //$NON-NLS-1$
	print.println(" this.superclassName: " + (getSuperclassName() == null ? "null" : new String(getSuperclassName()))); //$NON-NLS-2$ //$NON-NLS-1$
	print.println(" access_flags: " + ClassFileStruct.printTypeModifiers(this.accessFlags()) + "(" + this.accessFlags() + ")"); //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$

	print.flush();
	return out.toString();
}
/**
 * Check if the receiver has structural changes compare to the byte array in argument.
 * Structural changes are:
 * - modifiers changes for the class, the this.fields or the this.methods
 * - signature changes for this.fields or this.methods.
 * - changes in the number of this.fields or this.methods
 * - changes for field constants
 * - changes for thrown exceptions
 * - change for the super class or any super interfaces.
 * - changes for member types name or modifiers
 * If any of these changes occurs, the method returns true. false otherwise. 
 * The synthetic fields are included and the members are not required to be sorted.
 * @param newBytes the bytes of the .class file we want to compare the receiver to
 * @return boolean Returns true is there is a structural change between the two .class files, false otherwise
 */
public boolean hasStructuralChanges(byte[] newBytes) {
	return hasStructuralChanges(newBytes, true, true);
}
/**
 * Check if the receiver has structural changes compare to the byte array in argument.
 * Structural changes are:
 * - modifiers changes for the class, the this.fields or the this.methods
 * - signature changes for this.fields or this.methods.
 * - changes in the number of this.fields or this.methods
 * - changes for field constants
 * - changes for thrown exceptions
 * - change for the super class or any super interfaces.
 * - changes for member types name or modifiers
 * If any of these changes occurs, the method returns true. false otherwise.
 * @param newBytes the bytes of the .class file we want to compare the receiver to
 * @param orderRequired a boolean indicating whether the members should be sorted or not
 * @param excludesSynthetics a boolean indicating whether the synthetic members should be used in the comparison
 * @return boolean Returns true is there is a structural change between the two .class files, false otherwise
 */
public boolean hasStructuralChanges(byte[] newBytes, boolean orderRequired, boolean excludesSynthetic) {
	try {
		ClassFileReader newClassFile =
			new ClassFileReader(newBytes, this.classFileName);
		// type level comparison
		// modifiers
		if (this.getModifiers() != newClassFile.getModifiers())
			return true;
		// superclass
		if (!CharOperation.equals(this.getSuperclassName(), newClassFile.getSuperclassName()))
			return true;
		// interfaces
		char[][] newInterfacesNames = newClassFile.getInterfaceNames();
		if (this.interfaceNames != newInterfacesNames) { // TypeConstants.NoSuperInterfaces
			int newInterfacesLength = newInterfacesNames == null ? 0 : newInterfacesNames.length;
			if (newInterfacesLength != this.interfacesCount)
				return true;
			for (int i = 0, max = this.interfacesCount; i < max; i++)
				if (!CharOperation.equals(this.interfaceNames[i], newInterfacesNames[i]))
					return true;
		}

		// member types
		IBinaryNestedType[] currentMemberTypes = (IBinaryNestedType[]) this.getMemberTypes();
		IBinaryNestedType[] otherMemberTypes = (IBinaryNestedType[]) newClassFile.getMemberTypes();
		if (currentMemberTypes != otherMemberTypes) { // TypeConstants.NoMemberTypes
			int currentMemberTypeLength = currentMemberTypes == null ? 0 : currentMemberTypes.length;
			int otherMemberTypeLength = otherMemberTypes == null ? 0 : otherMemberTypes.length;
			if (currentMemberTypeLength != otherMemberTypeLength)
				return true;
			for (int i = 0; i < currentMemberTypeLength; i++)
				if (!CharOperation.equals(currentMemberTypes[i].getName(), otherMemberTypes[i].getName())
					|| currentMemberTypes[i].getModifiers() != otherMemberTypes[i].getModifiers())
						return true;
		}

		// fields
		FieldInfo[] otherFieldInfos = (FieldInfo[]) newClassFile.getFields();
		int otherFieldInfosLength = otherFieldInfos == null ? 0 : otherFieldInfos.length;
		boolean compareFields = true;
		if (this.fieldsCount == otherFieldInfosLength) {
			int i = 0;
			for (; i < this.fieldsCount; i++)
				if (hasStructuralFieldChanges(this.fields[i], otherFieldInfos[i])) break;
			if ((compareFields = i != this.fieldsCount) && !orderRequired && !excludesSynthetic)
				return true;
		}
		if (compareFields) {
			if (this.fieldsCount != otherFieldInfosLength && !excludesSynthetic)
				return true;
			if (orderRequired) {
				if (this.fieldsCount != 0)
					Arrays.sort(this.fields);
				if (otherFieldInfosLength != 0)
					Arrays.sort(otherFieldInfos);
			}
			if (excludesSynthetic) {
				if (hasNonSyntheticFieldChanges(this.fields, otherFieldInfos))
					return true;
			} else {
				for (int i = 0; i < this.fieldsCount; i++)
					if (hasStructuralFieldChanges(this.fields[i], otherFieldInfos[i]))
						return true;
			}
		}
		
		// methods
		MethodInfo[] otherMethodInfos = (MethodInfo[]) newClassFile.getMethods();
		int otherMethodInfosLength = otherMethodInfos == null ? 0 : otherMethodInfos.length;
		boolean compareMethods = true;
		if (this.methodsCount == otherMethodInfosLength) {
			int i = 0;
			for (; i < this.methodsCount; i++)
				if (hasStructuralMethodChanges(this.methods[i], otherMethodInfos[i])) break;
			if ((compareMethods = i != this.methodsCount) && !orderRequired && !excludesSynthetic)
				return true;
		}
		if (compareMethods) {
			if (this.methodsCount != otherMethodInfosLength && !excludesSynthetic)
				return true;
			if (orderRequired) {
				if (this.methodsCount != 0)
					Arrays.sort(this.methods);
				if (otherMethodInfosLength != 0)
					Arrays.sort(otherMethodInfos);	
			}
			if (excludesSynthetic) {
				if (hasNonSyntheticMethodChanges(this.methods, otherMethodInfos))
					return true;
			} else {
				for (int i = 0; i < this.methodsCount; i++)
					if (hasStructuralMethodChanges(this.methods[i], otherMethodInfos[i]))
						return true;
			}
		}

		return false;
	} catch (ClassFormatException e) {
		return true;
	}
}
private boolean hasNonSyntheticFieldChanges(FieldInfo[] currentFieldInfos, FieldInfo[] otherFieldInfos) {
	int length1 = currentFieldInfos == null ? 0 : currentFieldInfos.length;
	int length2 = otherFieldInfos == null ? 0 : otherFieldInfos.length;
	int index1 = 0;
	int index2 = 0;

	end : while (index1 < length1 && index2 < length2) {
		while (currentFieldInfos[index1].isSynthetic()) {
			if (++index1 >= length1) break end;
		}
		while (otherFieldInfos[index2].isSynthetic()) {
			if (++index2 >= length2) break end;
		}
		if (hasStructuralFieldChanges(currentFieldInfos[index1++], otherFieldInfos[index2++]))
			return true;
	}

	while (index1 < length1) {
		if (!currentFieldInfos[index1++].isSynthetic()) return true;
	}
	while (index2 < length2) {
		if (!otherFieldInfos[index2++].isSynthetic()) return true;
	}
	return false;
}
private boolean hasStructuralFieldChanges(FieldInfo currentFieldInfo, FieldInfo otherFieldInfo) {
	if (currentFieldInfo.getModifiers() != otherFieldInfo.getModifiers())
		return true;
	if (!CharOperation.equals(currentFieldInfo.getName(), otherFieldInfo.getName()))
		return true;
	if (!CharOperation.equals(currentFieldInfo.getTypeName(), otherFieldInfo.getTypeName()))
		return true;
	if (currentFieldInfo.hasConstant() != otherFieldInfo.hasConstant())
		return true;
	if (currentFieldInfo.hasConstant()) {
		Constant currentConstant = currentFieldInfo.getConstant();
		Constant otherConstant = otherFieldInfo.getConstant();
		if (currentConstant.typeID() != otherConstant.typeID())
			return true;
		if (!currentConstant.getClass().equals(otherConstant.getClass()))
			return true;
		switch (currentConstant.typeID()) {
			case TypeIds.T_int :
				return currentConstant.intValue() != otherConstant.intValue();
			case TypeIds.T_byte :
				return currentConstant.byteValue() != otherConstant.byteValue();
			case TypeIds.T_short :
				return currentConstant.shortValue() != otherConstant.shortValue();
			case TypeIds.T_char :
				return currentConstant.charValue() != otherConstant.charValue();
			case TypeIds.T_float :
				return currentConstant.floatValue() != otherConstant.floatValue();
			case TypeIds.T_double :
				return currentConstant.doubleValue() != otherConstant.doubleValue();
			case TypeIds.T_boolean :
				return currentConstant.booleanValue() != otherConstant.booleanValue();
			case TypeIds.T_String :
				return !currentConstant.stringValue().equals(otherConstant.stringValue());
			case TypeIds.T_null :
				return otherConstant != NullConstant.Default;
		}
	}
	return false;
}
private boolean hasNonSyntheticMethodChanges(MethodInfo[] currentMethodInfos, MethodInfo[] otherMethodInfos) {
	int length1 = currentMethodInfos == null ? 0 : currentMethodInfos.length;
	int length2 = otherMethodInfos == null ? 0 : otherMethodInfos.length;
	int index1 = 0;
	int index2 = 0;

	MethodInfo m;
	end : while (index1 < length1 && index2 < length2) {
		while ((m = currentMethodInfos[index1]).isSynthetic() || m.isClinit()) {
			if (++index1 >= length1) break end;
		}
		while ((m = otherMethodInfos[index2]).isSynthetic() || m.isClinit()) {
			if (++index2 >= length2) break end;
		}
		if (hasStructuralMethodChanges(currentMethodInfos[index1++], otherMethodInfos[index2++]))
			return true;
	}

	while (index1 < length1) {
		if (!((m = currentMethodInfos[index1++]).isSynthetic() || m.isClinit())) return true;
	}
	while (index2 < length2) {
		if (!((m = otherMethodInfos[index2++]).isSynthetic() || m.isClinit())) return true;
	}
	return false;
}
private boolean hasStructuralMethodChanges(MethodInfo currentMethodInfo, MethodInfo otherMethodInfo) {
	if (currentMethodInfo.getModifiers() != otherMethodInfo.getModifiers())
		return true;
	if (!CharOperation.equals(currentMethodInfo.getSelector(), otherMethodInfo.getSelector()))
		return true;
	if (!CharOperation.equals(currentMethodInfo.getMethodDescriptor(), otherMethodInfo.getMethodDescriptor()))
		return true;

	char[][] currentThrownExceptions = currentMethodInfo.getExceptionTypeNames();
	char[][] otherThrownExceptions = otherMethodInfo.getExceptionTypeNames();
	if (currentThrownExceptions != otherThrownExceptions) { // TypeConstants.NoExceptions
		int currentThrownExceptionsLength = currentThrownExceptions == null ? 0 : currentThrownExceptions.length;
		int otherThrownExceptionsLength = otherThrownExceptions == null ? 0 : otherThrownExceptions.length;
		if (currentThrownExceptionsLength != otherThrownExceptionsLength)
			return true;
		for (int k = 0; k < currentThrownExceptionsLength; k++)
			if (!CharOperation.equals(currentThrownExceptions[k], otherThrownExceptions[k]))
				return true;
	}
	return false;
}
/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
private void initialize() {
	for (int i = 0, max = fieldsCount; i < max; i++) {
		fields[i].initialize();
	}
	for (int i = 0, max = methodsCount; i < max; i++) {
		methods[i].initialize();
	}
	if (innerInfos != null) {
		for (int i = 0, max = innerInfos.length; i < max; i++) {
			innerInfos[i].initialize();
		}
	}
	this.reset();
}
protected void reset() {
	this.constantPoolOffsets = null;
	super.reset();
}

}
