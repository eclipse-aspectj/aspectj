/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.UnresolvedType;

public class HelperInterfaceBinding extends SourceTypeBinding {
	private UnresolvedType typeX;
	SourceTypeBinding enclosingType;
	List<MethodBinding> methods = new ArrayList<>();

	public HelperInterfaceBinding(SourceTypeBinding enclosingType, UnresolvedType typeX) {
		super();
		this.fPackage = enclosingType.fPackage;
		// this.fileName = scope.referenceCompilationUnit().getFileName();
		this.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccInterface
				| ClassFileConstants.AccAbstract;
		this.sourceName = enclosingType.scope.referenceContext.name;
		this.enclosingType = enclosingType;
		this.typeX = typeX;
		this.typeVariables = Binding.NO_TYPE_VARIABLES;
		this.scope = enclosingType.scope;
		this.superInterfaces = new ReferenceBinding[0];
	}

	public HelperInterfaceBinding(char[][] compoundName, PackageBinding fPackage, ClassScope scope) {
		super(compoundName, fPackage, scope);
	}

	public char[] getFileName() {
		return enclosingType.getFileName();
	}

	public UnresolvedType getTypeX() {
		return typeX;
	}

	public void addMethod(EclipseFactory world, ResolvedMember member) {
		MethodBinding binding = world.makeMethodBinding(member);
		this.methods.add(binding);
	}

	public FieldBinding[] fields() {
		return new FieldBinding[0];
	}

	public MethodBinding[] methods() {
		return new MethodBinding[0];
	}

	public char[] constantPoolName() {
		String sig = typeX.getSignature();
		return sig.substring(1, sig.length() - 1).toCharArray();
	}

	public void generateClass(CompilationResult result, ClassFile enclosingClassFile) {
		ClassFile classFile = new ClassFile(this);
		classFile.initialize(this, enclosingClassFile, false);
//		classFile.recordInnerClasses(this);
		// classFile.addFieldInfos();
		classFile.contents[classFile.contentsOffset++] = (byte) 0;
		classFile.contents[classFile.contentsOffset++] = (byte) 0;
		classFile.setForMethodInfos();
		for (MethodBinding b: methods) {
			generateMethod(classFile, b);
		}
		classFile.addAttributes();
		result.record(this.constantPoolName(), classFile);
	}

	private void generateMethod(ClassFile classFile, MethodBinding binding) {
		classFile.generateMethodInfoHeader(binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttributes(binding);
		classFile.completeMethodInfo(binding, methodAttributeOffset, attributeNumber);
	}

	public ReferenceBinding[] superInterfaces() {
		return new ReferenceBinding[0];
	}

}
