/**
 * 
 */
package org.aspectj.weaver;

import java.util.Collection;
import java.util.Collections;

import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;

class BoundedReferenceTypeDelegate extends AbstractReferenceTypeDelegate {

	public BoundedReferenceTypeDelegate(ReferenceType backing) {
		super(backing, false);
	}

	public boolean isAspect() {
		return resolvedTypeX.isAspect();
	}

	public boolean isAnnotationStyleAspect() {
		return resolvedTypeX.isAnnotationStyleAspect();
	}

	public boolean isInterface() {
		return resolvedTypeX.isInterface();
	}

	public boolean isEnum() {
		return resolvedTypeX.isEnum();
	}

	public boolean isAnnotation() {
		return resolvedTypeX.isAnnotation();
	}

	public boolean isAnnotationWithRuntimeRetention() {
		return resolvedTypeX.isAnnotationWithRuntimeRetention();
	}

	public boolean isAnonymous() {
		return resolvedTypeX.isAnonymous();
	}

	public boolean isNested() {
		return resolvedTypeX.isNested();
	}

	public ResolvedType getOuterClass() {
		return resolvedTypeX.getOuterClass();
	}

	public String getRetentionPolicy() {
		return resolvedTypeX.getRetentionPolicy();
	}

	public boolean canAnnotationTargetType() {
		return resolvedTypeX.canAnnotationTargetType();
	}

	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		return resolvedTypeX.getAnnotationTargetKinds();
	}

	public boolean isGeneric() {
		return resolvedTypeX.isGenericType();
	}

	public String getDeclaredGenericSignature() {
		return resolvedTypeX.getDeclaredGenericSignature();
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		return resolvedTypeX.hasAnnotation(ofType);
	}

	public AnnotationAJ[] getAnnotations() {
		return resolvedTypeX.getAnnotations();
	}

	public ResolvedType[] getAnnotationTypes() {
		return resolvedTypeX.getAnnotationTypes();
	}

	public ResolvedMember[] getDeclaredFields() {
		return resolvedTypeX.getDeclaredFields();
	}

	public ResolvedType[] getDeclaredInterfaces() {
		return resolvedTypeX.getDeclaredInterfaces();
	}

	public ResolvedMember[] getDeclaredMethods() {
		return resolvedTypeX.getDeclaredMethods();
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		return resolvedTypeX.getDeclaredPointcuts();
	}

	public PerClause getPerClause() {
		return resolvedTypeX.getPerClause();
	}

	public Collection<Declare> getDeclares() {
		return resolvedTypeX.getDeclares();
	}

	public Collection<ConcreteTypeMunger> getTypeMungers() {
		return resolvedTypeX.getTypeMungers();
	}

	public Collection<ResolvedMember> getPrivilegedAccesses() {
		return Collections.emptyList();
	}

	public int getModifiers() {
		return resolvedTypeX.getModifiers();
	}

	public ResolvedType getSuperclass() {
		return resolvedTypeX.getSuperclass();
	}

	public WeaverStateInfo getWeaverState() {
		return null;
	}

	public TypeVariable[] getTypeVariables() {
		return resolvedTypeX.getTypeVariables();
	}

}