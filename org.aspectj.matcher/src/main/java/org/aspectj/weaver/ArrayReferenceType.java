/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

import java.lang.reflect.Modifier;

/**
 * Represents a resolved array type
 * 
 * @author Andy Clement
 */
public class ArrayReferenceType extends ReferenceType {

	private final ResolvedType componentType;

	public ArrayReferenceType(String sig, String erasureSig, World world, ResolvedType componentType) {
		super(sig, erasureSig, world);
		this.componentType = componentType;
	}

	// These methods are from the original implementation when Array was a ResolvedType and not a ReferenceType

	public final ResolvedMember[] getDeclaredFields() {
		return ResolvedMember.NONE;
	}

	public final ResolvedMember[] getDeclaredMethods() {
		// ??? should this return clone? Probably not...
		// If it ever does, here is the code:
		// ResolvedMember cloneMethod =
		// new ResolvedMember(Member.METHOD,this,Modifier.PUBLIC,UnresolvedType.OBJECT,"clone",new UnresolvedType[]{});
		// return new ResolvedMember[]{cloneMethod};
		return ResolvedMember.NONE;
	}

	public final ResolvedType[] getDeclaredInterfaces() {
		return new ResolvedType[] { world.getCoreType(CLONEABLE), world.getCoreType(SERIALIZABLE) };
	}

	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType) {
		return null;
	}

	public AnnotationAJ[] getAnnotations() {
		return AnnotationAJ.EMPTY_ARRAY;
	}

	public ResolvedType[] getAnnotationTypes() {
		return ResolvedType.NONE;
	}

	public final ResolvedMember[] getDeclaredPointcuts() {
		return ResolvedMember.NONE;
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		return false;
	}

	public final ResolvedType getSuperclass() {
		return world.getCoreType(OBJECT);
	}

	public final boolean isAssignableFrom(ResolvedType o) {
		if (!o.isArray())
			return false;
		if (o.getComponentType().isPrimitiveType()) {
			return o.equals(this);
		} else {
			return getComponentType().resolve(world).isAssignableFrom(o.getComponentType().resolve(world));
		}
	}

	public boolean isAssignableFrom(ResolvedType o, boolean allowMissing) {
		return isAssignableFrom(o);
	}

	public final boolean isCoerceableFrom(ResolvedType o) {
		if (o.equals(UnresolvedType.OBJECT) || o.equals(UnresolvedType.SERIALIZABLE) || o.equals(UnresolvedType.CLONEABLE)) {
			return true;
		}
		if (!o.isArray())
			return false;
		if (o.getComponentType().isPrimitiveType()) {
			return o.equals(this);
		} else {
			return getComponentType().resolve(world).isCoerceableFrom(o.getComponentType().resolve(world));
		}
	}

	public final int getModifiers() {
		int mask = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;
		return (componentType.getModifiers() & mask) | Modifier.FINAL;
	}

	public UnresolvedType getComponentType() {
		return componentType;
	}

	public ResolvedType getResolvedComponentType() {
		return componentType;
	}

	public ISourceContext getSourceContext() {
		return getResolvedComponentType().getSourceContext();
	}

	// Methods overridden from ReferenceType follow

	public TypeVariable[] getTypeVariables() {
		if (this.typeVariables == null && componentType.getTypeVariables() != null) {
			this.typeVariables = componentType.getTypeVariables();
			for (TypeVariable typeVariable : this.typeVariables) {
				typeVariable.resolve(world);
			}
		}
		return this.typeVariables;
	}

	public boolean isAnnotation() {
		return false;
	}

	public boolean isAnonymous() {
		return false;
	}

	public boolean isAnnotationStyleAspect() {
		return false;
	}

	public boolean isAspect() {
		return false;
	}

	public boolean isPrimitiveType() {
		return typeKind == TypeKind.PRIMITIVE;
	}

	public boolean isSimpleType() {
		return typeKind == TypeKind.SIMPLE;
	}

	public boolean isRawType() {
		return typeKind == TypeKind.RAW;
	}

	public boolean isGenericType() {
		return typeKind == TypeKind.GENERIC;
	}

	public boolean isParameterizedType() {
		return typeKind == TypeKind.PARAMETERIZED;
	}

	public boolean isTypeVariableReference() {
		return typeKind == TypeKind.TYPE_VARIABLE;
	}

	public boolean isGenericWildcard() {
		return typeKind == TypeKind.WILDCARD;
	}

	public boolean isEnum() {
		return false;
	}

	public boolean isNested() {
		return false;
	}

	public boolean isClass() {
		return false;
	}

	@Override
	public boolean isExposedToWeaver() {
		return false;
	}

	public boolean canAnnotationTargetType() {
		return false;
	}

	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		return null;
	}

	public boolean isAnnotationWithRuntimeRetention() {
		return false;
	}

	public boolean isPrimitiveArray() {
		if (componentType.isPrimitiveType()) {
			return true;
		} else if (componentType.isArray()) {
			return componentType.isPrimitiveArray();
		} else {
			return false;
		}
	}
}
