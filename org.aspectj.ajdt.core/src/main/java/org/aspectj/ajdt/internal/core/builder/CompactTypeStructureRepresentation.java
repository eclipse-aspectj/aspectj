/* *******************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement   promoted member type from AjState
 * ******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

/**
 * Used to determine if a type has structurally changed during incremental compilation. At the end of compilation we create one of
 * these objects using the bytes for the class about to be woven. On a subsequent incremental compile we compare the new form of the
 * class with a previously stored CompactTypeStructureRepresentation instance. A structural change will indicate we need to do
 * recompile other dependent types.
 */
public class CompactTypeStructureRepresentation implements IBinaryType {
	static char[][] NoInterface = CharOperation.NO_CHAR_CHAR;
	static IBinaryNestedType[] NoNestedType = new IBinaryNestedType[0];
	static IBinaryField[] NoField = new IBinaryField[0];
	static IBinaryMethod[] NoMethod = new IBinaryMethod[0];

	// this is the core state for comparison
	char[] className;
	int modifiers;
	char[] genericSignature;
	char[] superclassName;
	char[][] interfaces;

	char[] enclosingMethod;

	char[][][] missingTypeNames;

	// this is the extra state that enables us to be an IBinaryType
	char[] enclosingTypeName;
	boolean isLocal, isAnonymous, isMember;
	char[] sourceFileName;
	char[] fileName;
	char[] sourceName;
	long tagBits;
	boolean isBinaryType;
	IBinaryField[] binFields;
	IBinaryMethod[] binMethods;
	IBinaryNestedType[] memberTypes;
	IBinaryAnnotation[] annotations;
	IBinaryTypeAnnotation[] typeAnnotations;


	public CompactTypeStructureRepresentation(ClassFileReader cfr, boolean isAspect) {

		this.enclosingTypeName = cfr.getEnclosingTypeName();
		this.isLocal = cfr.isLocal();
		this.isAnonymous = cfr.isAnonymous();
		this.isMember = cfr.isMember();
		this.sourceFileName = cfr.sourceFileName();
		this.fileName = cfr.getFileName();
		this.missingTypeNames = cfr.getMissingTypeNames();
		this.tagBits = cfr.getTagBits();
		this.enclosingMethod = cfr.getEnclosingMethod();
		this.isBinaryType = cfr.isBinaryType();
		this.binFields = cfr.getFields();
		if (binFields == null) {
			binFields = NoField;
		}
		this.binMethods = cfr.getMethods();
		if (binMethods == null) {
			binMethods = NoMethod;
		}
		// If we are an aspect we (for now) need to grab even the malformed inner type info as it
		// may be there because it refers to an ITD'd innertype. This needs to be improved - perhaps
		// using a real attribute against which memberTypes can be compared to see which are just
		// references and which were real declarations
		this.memberTypes = cfr.getMemberTypes(isAspect);
		this.annotations = cfr.getAnnotations();
		this.typeAnnotations = cfr.getTypeAnnotations();
		this.sourceName = cfr.getSourceName();
		this.className = cfr.getName(); // slashes...
		this.modifiers = cfr.getModifiers();
		this.genericSignature = cfr.getGenericSignature();
		// if (this.genericSignature.length == 0) {
		// this.genericSignature = null;
		// }
		this.superclassName = cfr.getSuperclassName(); // slashes...
		interfaces = cfr.getInterfaceNames();

	}

	public char[][][] getMissingTypeNames() {
		return missingTypeNames;
	}

	public char[] getEnclosingTypeName() {
		return enclosingTypeName;
	}

	public int getModifiers() {
		return modifiers;
	}

	public char[] getGenericSignature() {
		return genericSignature;
	}

	public char[] getEnclosingMethod() {
		return enclosingMethod;
	}

	public char[][] getInterfaceNames() {
		return interfaces;
	}

	public boolean isAnonymous() {
		return isAnonymous;
	}

	public char[] sourceFileName() {
		return sourceFileName;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public boolean isMember() {
		return isMember;
	}

	public char[] getSuperclassName() {
		return superclassName;
	}

	public char[] getFileName() {
		return fileName;
	}

	public char[] getName() {
		return className;
	}

	public long getTagBits() {
		return tagBits;
	}

	public boolean isBinaryType() {
		return isBinaryType;
	}

	public IBinaryField[] getFields() {
		return binFields;
	}

	public IBinaryMethod[] getMethods() {
		return binMethods;
	}

	public IBinaryNestedType[] getMemberTypes() {
		return memberTypes;
	}

	public IBinaryAnnotation[] getAnnotations() {
		return annotations;
	}

	public char[] getSourceName() {
		return sourceName;
	}

	public IBinaryTypeAnnotation[] getTypeAnnotations() {
		return typeAnnotations;
	}

	public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member,
			LookupEnvironment environment) {
		// TODO[1.8.7] more to do here? In what contexts?
		return walker;
	}

	public char[] getModule() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExternalAnnotationStatus getExternalAnnotationStatus() {
		return ExternalAnnotationStatus.NOT_EEA_CONFIGURED;
	}

}