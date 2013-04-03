/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AjAttribute.EffectiveSignatureAttribute;

/**
 * @author colyer Instances of this class are created by ResolvedMember.getSignatures() when collating all of the signatures for a
 *         member. We need to create entries in the set for the "gaps" in the hierarchy. For example:
 * 
 *         class A { void foo(); }
 * 
 *         class B extends A {}
 * 
 *         Join Point : call(* B.foo())
 * 
 *         has signatures:
 * 
 *         B.foo() AND A.foo() B.foo() will be created as a ResolvedMemberWithSubstituteDeclaringType
 * 
 *         Oh for a JDK 1.4 dynamic proxy.... we have to run on 1.3 :(
 */
public class JoinPointSignature implements ResolvedMember {

	public static final JoinPointSignature[] EMPTY_ARRAY = new JoinPointSignature[0];

	private ResolvedMember realMember;
	private ResolvedType substituteDeclaringType;

	public JoinPointSignature(ResolvedMember backing, ResolvedType aType) {
		this.realMember = backing;
		this.substituteDeclaringType = aType;
	}

	public UnresolvedType getDeclaringType() {
		return substituteDeclaringType;
	}

	public int getModifiers(World world) {
		return realMember.getModifiers(world);
	}

	public int getModifiers() {
		return realMember.getModifiers();
	}

	public UnresolvedType[] getExceptions(World world) {
		return realMember.getExceptions(world);
	}

	public UnresolvedType[] getExceptions() {
		return realMember.getExceptions();
	}

	public ShadowMunger getAssociatedShadowMunger() {
		return realMember.getAssociatedShadowMunger();
	}

	public boolean isAjSynthetic() {
		return realMember.isAjSynthetic();
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		return realMember.hasAnnotation(ofType);
	}

	public ResolvedType[] getAnnotationTypes() {
		return realMember.getAnnotationTypes();
	}

	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType) {
		return realMember.getAnnotationOfType(ofType);
	}

	public void setAnnotationTypes(ResolvedType[] annotationtypes) {
		realMember.setAnnotationTypes(annotationtypes);
	}
	
	public void setAnnotations(AnnotationAJ[] annotations) {
		realMember.setAnnotations(annotations);
	}

	public void addAnnotation(AnnotationAJ annotation) {
		realMember.addAnnotation(annotation);
	}

	public boolean isBridgeMethod() {
		return realMember.isBridgeMethod();
	}

	public boolean isVarargsMethod() {
		return realMember.isVarargsMethod();
	}

	public boolean isSynthetic() {
		return realMember.isSynthetic();
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		realMember.write(s);
	}

	public ISourceContext getSourceContext(World world) {
		return realMember.getSourceContext(world);
	}

	public String[] getParameterNames() {
		return realMember.getParameterNames();
	}

	public void setParameterNames(String[] names) {
		realMember.setParameterNames(names);
	}

	public String[] getParameterNames(World world) {
		return realMember.getParameterNames(world);
	}

	public EffectiveSignatureAttribute getEffectiveSignature() {
		return realMember.getEffectiveSignature();
	}

	public ISourceLocation getSourceLocation() {
		return realMember.getSourceLocation();
	}

	public int getEnd() {
		return realMember.getEnd();
	}

	public ISourceContext getSourceContext() {
		return realMember.getSourceContext();
	}

	public int getStart() {
		return realMember.getStart();
	}

	public void setPosition(int sourceStart, int sourceEnd) {
		realMember.setPosition(sourceStart, sourceEnd);
	}

	public void setSourceContext(ISourceContext sourceContext) {
		realMember.setSourceContext(sourceContext);
	}

	public boolean isAbstract() {
		return realMember.isAbstract();
	}

	public boolean isPublic() {
		return realMember.isPublic();
	}

	public boolean isDefault() {
		return realMember.isDefault();
	}

	public boolean isVisible(ResolvedType fromType) {
		return realMember.isVisible(fromType);
	}

	public void setCheckedExceptions(UnresolvedType[] checkedExceptions) {
		realMember.setCheckedExceptions(checkedExceptions);
	}

	public void setAnnotatedElsewhere(boolean b) {
		realMember.setAnnotatedElsewhere(b);
	}

	public boolean isAnnotatedElsewhere() {
		return realMember.isAnnotatedElsewhere();
	}

	public UnresolvedType getGenericReturnType() {
		return realMember.getGenericReturnType();
	}

	public UnresolvedType[] getGenericParameterTypes() {
		return realMember.getGenericParameterTypes();
	}

	public ResolvedMemberImpl parameterizedWith(UnresolvedType[] typeParameters, ResolvedType newDeclaringType,
			boolean isParameterized) {
		return realMember.parameterizedWith(typeParameters, newDeclaringType, isParameterized);
	}

	public ResolvedMemberImpl parameterizedWith(UnresolvedType[] typeParameters, ResolvedType newDeclaringType,
			boolean isParameterized, List<String> aliases) {
		return realMember.parameterizedWith(typeParameters, newDeclaringType, isParameterized, aliases);
	}

	public void setTypeVariables(TypeVariable[] types) {
		realMember.setTypeVariables(types);
	}

	public TypeVariable[] getTypeVariables() {
		return realMember.getTypeVariables();
	}

	public TypeVariable getTypeVariableNamed(String name) {
		return realMember.getTypeVariableNamed(name);
	}

	public boolean matches(ResolvedMember aCandidateMatch, boolean ignoreGenerics) {
		return realMember.matches(aCandidateMatch, ignoreGenerics);
	}

	public ResolvedMember resolve(World world) {
		return realMember.resolve(world);
	}

	public int compareTo(Member other) {
		return realMember.compareTo(other);
	}

	public MemberKind getKind() {
		return realMember.getKind();
	}

	public UnresolvedType getReturnType() {
		return realMember.getReturnType();
	}

	public UnresolvedType getType() {
		return realMember.getType();
	}

	public String getName() {
		return realMember.getName();
	}

	public UnresolvedType[] getParameterTypes() {
		return realMember.getParameterTypes();
	}

	public AnnotationAJ[][] getParameterAnnotations() {
		return realMember.getParameterAnnotations();
	}

	public ResolvedType[][] getParameterAnnotationTypes() {
		return realMember.getParameterAnnotationTypes();
	}

	public String getSignature() {
		return realMember.getSignature();
	}

	public int getArity() {
		return realMember.getArity();
	}

	public String getParameterSignature() {
		return realMember.getParameterSignature();
	}

	public boolean isCompatibleWith(Member am) {
		return realMember.isCompatibleWith(am);
	}

	public boolean canBeParameterized() {
		return realMember.canBeParameterized();
	}

	public AnnotationAJ[] getAnnotations() {
		return realMember.getAnnotations();
	}

	public Collection<ResolvedType> getDeclaringTypes(World world) {
		throw new UnsupportedOperationException("Adrian doesn't think you should be calling this...");
	}

	public JoinPointSignatureIterator getJoinPointSignatures(World world) {
		return realMember.getJoinPointSignatures(world);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getReturnType().getName());
		buf.append(' ');
		buf.append(getDeclaringType().getName());
		buf.append('.');
		buf.append(getName());
		if (getKind() != FIELD) {
			buf.append("(");
			UnresolvedType[] parameterTypes = getParameterTypes();
			if (parameterTypes.length != 0) {
				buf.append(parameterTypes[0]);
				for (int i = 1, len = parameterTypes.length; i < len; i++) {
					buf.append(", ");
					buf.append(parameterTypes[i].getName());
				}
			}
			buf.append(")");
		}
		return buf.toString();
	}

	public String toGenericString() {
		return realMember.toGenericString();
	}

	public String toDebugString() {
		return realMember.toDebugString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JoinPointSignature)) {
			return false;
		}
		JoinPointSignature other = (JoinPointSignature) obj;
		if (!realMember.equals(other.realMember)) {
			return false;
		}
		if (!substituteDeclaringType.equals(other.substituteDeclaringType)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 17 + (37 * realMember.hashCode()) + (37 * substituteDeclaringType.hashCode());
	}

	public boolean hasBackingGenericMember() {
		return realMember.hasBackingGenericMember();
	}

	public ResolvedMember getBackingGenericMember() {
		return realMember.getBackingGenericMember();
	}

	public void evictWeavingState() {
		realMember.evictWeavingState();
	}

	public ResolvedMember parameterizedWith(Map m, World w) {
		return realMember.parameterizedWith(m, w);
	}

	public String getAnnotationDefaultValue() {
		return realMember.getAnnotationDefaultValue();
	}

	public String getParameterSignatureErased() {
		return realMember.getParameterSignatureErased();
	}

	public String getSignatureErased() {
		return realMember.getSignatureErased();
	}

	public boolean isDefaultConstructor() {
		return realMember.isDefaultConstructor();
	}

	public boolean equalsApartFromDeclaringType(Object other) {
		return realMember.equalsApartFromDeclaringType(other);
	}
}
