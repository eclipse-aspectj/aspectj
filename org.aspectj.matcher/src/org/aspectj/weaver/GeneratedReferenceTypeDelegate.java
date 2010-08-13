/* *******************************************************************
 * Copyright (c) 2008 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Collection;

import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;

/**
 * A delegate that can sit in the ReferenceType instance created for an aspect generated from aop.xml. Only answers the minimal set
 * of information required as the type is processed.
 * 
 * @author Andy Clement
 */
public class GeneratedReferenceTypeDelegate extends AbstractReferenceTypeDelegate {

	private ResolvedType superclass;

	public GeneratedReferenceTypeDelegate(ReferenceType backing) {
		super(backing, false);
	}

	public boolean isAspect() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isAnnotationStyleAspect() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isInterface() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isEnum() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isAnnotation() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isAnnotationWithRuntimeRetention() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isAnonymous() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isNested() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public ResolvedType getOuterClass() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public String getRetentionPolicy() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean canAnnotationTargetType() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean isGeneric() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public String getDeclaredGenericSignature() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public AnnotationAJ[] getAnnotations() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public ResolvedType[] getAnnotationTypes() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public ResolvedMember[] getDeclaredFields() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public ResolvedType[] getDeclaredInterfaces() {
		return ResolvedType.NONE;
	}

	public ResolvedMember[] getDeclaredMethods() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public PerClause getPerClause() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public Collection<Declare> getDeclares() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public Collection<ConcreteTypeMunger> getTypeMungers() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public Collection<ResolvedMember> getPrivilegedAccesses() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public int getModifiers() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public void setSuperclass(ResolvedType superclass) {
		this.superclass = superclass;
	}

	public ResolvedType getSuperclass() {
		return this.superclass;
	}

	public WeaverStateInfo getWeaverState() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

	public TypeVariable[] getTypeVariables() {
		throw new UnsupportedOperationException("Not supported for GeneratedReferenceTypeDelegate");
	}

}