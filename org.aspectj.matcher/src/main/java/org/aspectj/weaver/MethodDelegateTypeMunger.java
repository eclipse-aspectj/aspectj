/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Vasseur     initial implementation
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.IOException;

import org.aspectj.weaver.patterns.TypePattern;

/**
 * Type munger for annotation style ITD declare parents. with an interface AND an implementation. Given the aspect that has a field
 * public static Interface fieldI = ... // impl. we will weave in the Interface' methods and delegate to the aspect public static
 * field fieldI
 *
 * Note: this munger DOES NOT handles the interface addition to the target classes - a regular Parent kinded munger must be added in
 * coordination.
 */
public class MethodDelegateTypeMunger extends ResolvedTypeMunger {

	private final UnresolvedType aspect;

	private UnresolvedType fieldType;

	/**
	 * The mixin implementation (which should have a no-argument constructor)
	 */
	private final String implClassName;

	/**
	 * Type pattern this munger applies to
	 */
	private final TypePattern typePattern;

	/**
	 * When created to represent a mixed in method for @DeclareMixin, these hold the signature of the factory method
	 */
	private String factoryMethodName;
	private String factoryMethodSignature;

	private int bitflags;
	private static final int REPLACING_EXISTING_METHOD = 0x001;

	/**
	 * Construct a new type munger for @AspectJ ITD
	 *
	 * @param signature
	 * @param aspect
	 * @param implClassName
	 * @param typePattern
	 */
	public MethodDelegateTypeMunger(ResolvedMember signature, UnresolvedType aspect, String implClassName, TypePattern typePattern) {
		super(MethodDelegate2, signature);
		this.aspect = aspect;
		this.typePattern = typePattern;
		this.implClassName = implClassName;
		factoryMethodName = "";
		factoryMethodSignature = "";
	}

	public MethodDelegateTypeMunger(ResolvedMember signature, UnresolvedType aspect, String implClassName, TypePattern typePattern,
			String factoryMethodName, String factoryMethodSignature) {
		super(MethodDelegate2, signature);
		this.aspect = aspect;
		this.typePattern = typePattern;
		this.implClassName = implClassName;
		this.factoryMethodName = factoryMethodName;
		this.factoryMethodSignature = factoryMethodSignature;
	}

	public boolean equals(Object other) {
		if (!(other instanceof MethodDelegateTypeMunger)) {
			return false;
		}
		MethodDelegateTypeMunger o = (MethodDelegateTypeMunger) other;
		return ((o.aspect == null) ? (aspect == null) : aspect.equals(o.aspect))
				&& ((o.typePattern == null) ? (typePattern == null) : typePattern.equals(o.typePattern))
				&& ((o.implClassName == null) ? (implClassName == null) : implClassName.equals(o.implClassName))
				&& ((o.fieldType == null ? (fieldType == null) : fieldType.equals(o.fieldType)))
				&& ((o.factoryMethodName == null) ? (factoryMethodName == null) : factoryMethodName.equals(o.factoryMethodName))
				&& ((o.factoryMethodSignature == null) ? (factoryMethodSignature == null) : factoryMethodSignature
						.equals(o.factoryMethodSignature)) && o.bitflags == bitflags;
	}

	private volatile int hashCode = 0;

	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 37 * result + ((aspect == null) ? 0 : aspect.hashCode());
			result = 37 * result + ((typePattern == null) ? 0 : typePattern.hashCode());
			result = 37 * result + ((implClassName == null) ? 0 : implClassName.hashCode());
			result = 37 * result + ((fieldType == null) ? 0 : fieldType.hashCode());
			result = 37 * result + ((factoryMethodName == null) ? 0 : factoryMethodName.hashCode());
			result = 37 * result + ((factoryMethodSignature == null) ? 0 : factoryMethodSignature.hashCode());
			result = 37 * result + bitflags;
			hashCode = result;
		}
		return hashCode;
	}

	public ResolvedMember getDelegate(ResolvedType targetType) {
		return AjcMemberMaker.itdAtDeclareParentsField(targetType, fieldType, aspect);
	}

	public ResolvedMember getDelegateFactoryMethod(World w) {
		ResolvedType aspectType = w.resolve(aspect);
		ResolvedMember[] methods = aspectType.getDeclaredMethods();
		for (ResolvedMember rm : methods) {
			if (rm.getName().equals(factoryMethodName) && rm.getSignature().equals(factoryMethodSignature)) {
				return rm;
			}
		}
		return null;
	}

	public String getImplClassName() {
		return implClassName;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		aspect.write(s);
		s.writeUTF(implClassName==null?"":implClassName);
		typePattern.write(s);
		fieldType.write(s);
		s.writeUTF(factoryMethodName);
		s.writeUTF(factoryMethodSignature);
		s.writeInt(bitflags);
	}

	public static ResolvedTypeMunger readMethod(VersionedDataInputStream s, ISourceContext context, boolean isEnhanced)
			throws IOException {
		ResolvedMemberImpl signature = ResolvedMemberImpl.readResolvedMember(s, context);
		UnresolvedType aspect = UnresolvedType.read(s);
		String implClassName = s.readUTF();
		if (implClassName.equals("")) {
			implClassName = null;
		}
		TypePattern tp = TypePattern.read(s, context);
		MethodDelegateTypeMunger typeMunger = new MethodDelegateTypeMunger(signature, aspect, implClassName, tp);
		UnresolvedType fieldType = null;
		if (isEnhanced) {
			fieldType = UnresolvedType.read(s);
		} else {
			// a guess... that will work in a lot of cases
			fieldType = signature.getDeclaringType();
		}
		typeMunger.setFieldType(fieldType);
		if (isEnhanced) {
			typeMunger.factoryMethodName = s.readUTF();
			typeMunger.factoryMethodSignature = s.readUTF();
			typeMunger.bitflags = s.readInt();
		}
		return typeMunger;
	}

	/**
	 * Match based on given type pattern, only classes can be matched
	 *
	 * @param matchType
	 * @param aspectType
	 * @return true if match
	 */
	public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
		// match only on class
		if (matchType.isEnum() || matchType.isInterface() || matchType.isAnnotation()) {
			return false;
		}

		return typePattern.matchesStatically(matchType);
	}

	/**
	 * Needed for reweavable
	 *
	 * @return true
	 */
	public boolean changesPublicSignature() {
		return true;
	}

	public static class FieldHostTypeMunger extends ResolvedTypeMunger {

		private final UnresolvedType aspect;

		/**
		 * Type pattern this munger applies to
		 */
		private final TypePattern typePattern;

		/**
		 * Construct a new type munger for @AspectJ ITD
		 *
		 * @param field
		 * @param aspect
		 * @param typePattern
		 */
		public FieldHostTypeMunger(ResolvedMember field, UnresolvedType aspect, TypePattern typePattern) {
			super(FieldHost, field);
			this.aspect = aspect;
			this.typePattern = typePattern;
		}

		public boolean equals(Object other) {
			if (!(other instanceof FieldHostTypeMunger)) {
				return false;
			}
			FieldHostTypeMunger o = (FieldHostTypeMunger) other;
			return ((o.aspect == null) ? (aspect == null) : aspect.equals(o.aspect))
					&& ((o.typePattern == null) ? (typePattern == null) : typePattern.equals(o.typePattern));
		}

		public int hashCode() {
			int result = 17;
			result = 37 * result + ((aspect == null) ? 0 : aspect.hashCode());
			result = 37 * result + ((typePattern == null) ? 0 : typePattern.hashCode());
			return result;
		}

		public void write(CompressingDataOutputStream s) throws IOException {
			kind.write(s);
			signature.write(s);
			aspect.write(s);
			typePattern.write(s);
		}

		public static ResolvedTypeMunger readFieldHost(VersionedDataInputStream s, ISourceContext context) throws IOException {
			ResolvedMemberImpl signature = ResolvedMemberImpl.readResolvedMember(s, context);
			UnresolvedType aspect = UnresolvedType.read(s);
			TypePattern tp = TypePattern.read(s, context);
			return new FieldHostTypeMunger(signature, aspect, tp);
		}

		/**
		 * Match based on given type pattern, only classes can be matched
		 *
		 * @param matchType
		 * @param aspectType
		 * @return true if match
		 */
		public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
			// match only on class
			if (matchType.isEnum() || matchType.isInterface() || matchType.isAnnotation()) {
				return false;
			}

			return typePattern.matchesStatically(matchType);
		}

		public boolean changesPublicSignature() {
			return false;
		}

		public boolean existsToSupportShadowMunging() {
			return true;
		}
	}

	public void setFieldType(UnresolvedType fieldType) {
		this.fieldType = fieldType;
	}

	public boolean specifiesDelegateFactoryMethod() {
		return factoryMethodName != null && factoryMethodName.length() != 0;
	}

	public String getFactoryMethodName() {
		return factoryMethodName;
	}

	public String getFactoryMethodSignature() {
		return factoryMethodSignature;
	}

	public UnresolvedType getAspect() {
		return aspect;
	}

	public boolean existsToSupportShadowMunging() {
		return true;
	}

	public void tagAsReplacingExistingMethod() {
		bitflags |= REPLACING_EXISTING_METHOD;
	}

	public boolean isReplacingExistingMethod() {
		return (bitflags & REPLACING_EXISTING_METHOD) != 0;
	}
}
