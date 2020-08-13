/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer     initial implementation 
 *      Andy Clement     got it working
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * Represents a declare annotation statement, one of atField, atMethod, atConstructor or atType.
 * 
 * @author Andy Clement
 */
public class DeclareAnnotation extends Declare {

	public static final Kind AT_TYPE = new Kind(1, "type");
	public static final Kind AT_FIELD = new Kind(2, "field");
	public static final Kind AT_METHOD = new Kind(3, "method");
	public static final Kind AT_CONSTRUCTOR = new Kind(4, "constructor");
	public static final Kind AT_REMOVE_FROM_FIELD = new Kind(5, "removeFromField");

	private Kind kind;
	// for declare @type
	private TypePattern typePattern;
	// for declare @field,@method,@constructor
	private ISignaturePattern signaturePattern;
	private ResolvedType containingAspect;
	private List<String> annotationMethods;
	private List<String> annotationStrings;
	private AnnotationAJ annotation; // discovered when required
	private ResolvedType annotationType; // discovered when required

	// not serialized:
	private int annotationStart;
	private int annotationEnd;

	/**
	 * Constructor for declare atType.
	 */
	public DeclareAnnotation(Kind kind, TypePattern typePattern) {
		this.typePattern = typePattern;
		this.kind = kind;
		init();
	}

	/**
	 * Constructor for declare atMethod/atField/atConstructor.
	 */
	public DeclareAnnotation(Kind kind, ISignaturePattern sigPattern) {
		this.signaturePattern = sigPattern;
		this.kind = kind;
		init();
	}

	private void init() {
		this.annotationMethods = new ArrayList<>();
		annotationMethods.add("unknown");
		this.annotationStrings = new ArrayList<>();
		annotationStrings.add("@<annotation>");
	}

	/**
	 * Returns the string, useful before the real annotation has been resolved
	 */
	public String getAnnotationString() {
		return annotationStrings.get(0);
	}

	public boolean isExactPattern() {
		return typePattern instanceof ExactTypePattern;
	}

	public String getAnnotationMethod() {
		return annotationMethods.get(0);
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("declare @");
		ret.append(kind);
		ret.append(" : ");
		ret.append(typePattern != null ? typePattern.toString() : signaturePattern.toString());
		ret.append(" : ");
		ret.append(annotationStrings.get(0));
		return ret.toString();
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public void resolve(IScope scope) {
		if (!scope.getWorld().isInJava5Mode()) {
			String msg = null;
			if (kind == AT_TYPE) {
				msg = WeaverMessages.DECLARE_ATTYPE_ONLY_SUPPORTED_AT_JAVA5_LEVEL;
			} else if (kind == AT_METHOD) {
				msg = WeaverMessages.DECLARE_ATMETHOD_ONLY_SUPPORTED_AT_JAVA5_LEVEL;
			} else if (kind == AT_FIELD) {
				msg = WeaverMessages.DECLARE_ATFIELD_ONLY_SUPPORTED_AT_JAVA5_LEVEL;
			} else if (kind == AT_CONSTRUCTOR) {
				msg = WeaverMessages.DECLARE_ATCONS_ONLY_SUPPORTED_AT_JAVA5_LEVEL;
			}
			scope.message(MessageUtil.error(WeaverMessages.format(msg), getSourceLocation()));
			return;
		}
		if (typePattern != null) {
			typePattern = typePattern.resolveBindings(scope, Bindings.NONE, false, false);
		}
		if (signaturePattern != null) {
			signaturePattern = signaturePattern.resolveBindings(scope, Bindings.NONE);
		}
		this.containingAspect = scope.getEnclosingType();
	}

	@Override
	public Declare parameterizeWith(Map<String, UnresolvedType> typeVariableBindingMap, World w) {
		DeclareAnnotation ret;
		if (this.kind == AT_TYPE) {
			ret = new DeclareAnnotation(kind, this.typePattern.parameterizeWith(typeVariableBindingMap, w));
		} else {
			ret = new DeclareAnnotation(kind, this.signaturePattern.parameterizeWith(typeVariableBindingMap, w));
		}
		ret.annotationMethods = this.annotationMethods;
		ret.annotationStrings = this.annotationStrings;
		ret.annotation = this.annotation;
		ret.containingAspect = this.containingAspect;
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public boolean isAdviceLike() {
		return false;
	}

	public void setAnnotationString(String annotationString) {
		this.annotationStrings.set(0, annotationString);
	}

	public void setAnnotationLocation(int start, int end) {
		this.annotationStart = start;
		this.annotationEnd = end;
	}

	public int getAnnotationSourceStart() {
		return annotationStart;
	}

	public int getAnnotationSourceEnd() {
		return annotationEnd;
	}

	public void setAnnotationMethod(String methodName) {
		this.annotationMethods.set(0, methodName);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DeclareAnnotation)) {
			return false;
		}
		DeclareAnnotation other = (DeclareAnnotation) obj;
		if (!this.kind.equals(other.kind)) {
			return false;
		}
		if (!this.annotationStrings.get(0).equals(other.annotationStrings.get(0))) {
			return false;
		}
		if (!this.annotationMethods.get(0).equals(other.annotationMethods.get(0))) {
			return false;
		}
		if (this.typePattern != null) {
			if (!typePattern.equals(other.typePattern)) {
				return false;
			}
		}
		if (this.signaturePattern != null) {
			if (!signaturePattern.equals(other.signaturePattern)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 19;
		result = 37 * result + kind.hashCode();
		result = 37 * result + annotationStrings.get(0).hashCode();
		result = 37 * result + annotationMethods.get(0).hashCode();
		if (typePattern != null) {
			result = 37 * result + typePattern.hashCode();
		}
		if (signaturePattern != null) {
			result = 37 * result + signaturePattern.hashCode();
		}
		return result;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Declare.ANNOTATION);
		if (kind.id == AT_FIELD.id && isRemover) {
			s.writeInt(AT_REMOVE_FROM_FIELD.id);
		} else {
			s.writeInt(kind.id);
		}
		int max = 0;
		s.writeByte(max = annotationStrings.size());
		for (int i = 0; i < max; i++) {
			s.writeUTF(annotationStrings.get(i));
		}
		s.writeByte(max = annotationMethods.size());
		for (int i = 0; i < max; i++) {
			s.writeUTF(annotationMethods.get(i));
		}
		if (typePattern != null) {
			typePattern.write(s);
		}
		if (signaturePattern != null) {
			AbstractSignaturePattern.writeCompoundSignaturePattern(s, signaturePattern);
		}
		writeLocation(s);
	}

	public static Declare read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		DeclareAnnotation ret = null;
		boolean isRemover = false;
		int kind = s.readInt();
		if (kind == AT_REMOVE_FROM_FIELD.id) {
			kind = AT_FIELD.id;
			isRemover = true;
		}
		// old format was just a single string and method
		if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_AJ169) {
			// int numAnnotationStrings =
			s.readByte();
		}
		String annotationString = s.readUTF();
		if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_AJ169) {
			// int numAnnotationMethods =
			s.readByte();
		}
		String annotationMethod = s.readUTF();
		TypePattern tp = null;
		SignaturePattern sp = null;
		switch (kind) {
		case 1:
			tp = TypePattern.read(s, context);
			ret = new DeclareAnnotation(AT_TYPE, tp);
			break;
		case 2:
			if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_AJ169) {
				ret = new DeclareAnnotation(AT_FIELD, AbstractSignaturePattern.readCompoundSignaturePattern(s, context));
			} else {
				sp = SignaturePattern.read(s, context);
				ret = new DeclareAnnotation(AT_FIELD, sp);
			}
			if (isRemover) {
				ret.setRemover(true);
			}
			break;
		case 3:
			if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_AJ169) {
				ret = new DeclareAnnotation(AT_METHOD, AbstractSignaturePattern.readCompoundSignaturePattern(s, context));
			} else {
				sp = SignaturePattern.read(s, context);
				ret = new DeclareAnnotation(AT_METHOD, sp);
			}
			break;
		case 4:
			if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_AJ169) {
				ret = new DeclareAnnotation(AT_CONSTRUCTOR, AbstractSignaturePattern.readCompoundSignaturePattern(s, context));
			} else {
				sp = SignaturePattern.read(s, context);
				ret = new DeclareAnnotation(AT_CONSTRUCTOR, sp);
			}
			break;

		}
		ret.setAnnotationString(annotationString);
		ret.setAnnotationMethod(annotationMethod);
		ret.readLocation(context, s);
		return ret;
	}

	/**
	 * For declare atConstructor, atMethod, atField
	 */
	public boolean matches(ResolvedMember resolvedmember, World world) {
		if (kind == AT_METHOD || kind == AT_CONSTRUCTOR) {
			if (resolvedmember != null && resolvedmember.getName().charAt(0) == '<') {
				// <clinit> or <init>
				if (kind == AT_METHOD) {
					return false;
				}
			}
		}
		return signaturePattern.matches(resolvedmember, world, false);
	}

	/**
	 * For declare atType.
	 */
	public boolean matches(ResolvedType type) {
		if (!typePattern.matchesStatically(type)) {
			return false;
		}
		if (type.getWorld().getLint().typeNotExposedToWeaver.isEnabled() && !type.isExposedToWeaver()) {
			type.getWorld().getLint().typeNotExposedToWeaver.signal(type.getName(), getSourceLocation());
		}
		return true;
	}

	public void setAspect(ResolvedType typeX) {
		containingAspect = typeX;
	}

	public UnresolvedType getAspect() {
		return containingAspect;
	}

	public void copyAnnotationTo(ResolvedType onType) {
		ensureAnnotationDiscovered();
		if (!onType.hasAnnotation(annotation.getType())) {
			onType.addAnnotation(annotation);
		}
	}

	public AnnotationAJ getAnnotation() {
		ensureAnnotationDiscovered();
		return annotation;
	}

	/**
	 * The annotation specified in the declare @type is stored against a simple method of the form "ajc$declare_<NN>", this method
	 * finds that method and retrieves the annotation
	 */
	private void ensureAnnotationDiscovered() {
		if (annotation != null) {
			return;
		}
		String annotationMethod = annotationMethods.get(0);
		for (Iterator<ResolvedMember> iter = containingAspect.getMethods(true, true); iter.hasNext();) {
			ResolvedMember member = iter.next();
			if (member.getName().equals(annotationMethod)) {
				AnnotationAJ[] annos = member.getAnnotations();
				if (annos == null) {
					// if weaving broken code, this can happen
					return;
				}
				int idx = 0;
				if (annos.length > 0
						&& annos[0].getType().getSignature().equals("Lorg/aspectj/internal/lang/annotation/ajcDeclareAnnotation;")) {
					idx = 1;
				}
				annotation = annos[idx];
				break;
			}
		}
	}

	public TypePattern getTypePattern() {
		return typePattern;
	}

	public ISignaturePattern getSignaturePattern() {
		return signaturePattern;
	}

	public boolean isStarredAnnotationPattern() {
		if (typePattern != null) {
			return typePattern.isStarAnnotation();
		} else {
			return signaturePattern.isStarAnnotation();
		}
	}

	public Kind getKind() {
		return kind;
	}

	public boolean isDeclareAtConstuctor() {
		return kind.equals(AT_CONSTRUCTOR);
	}

	public boolean isDeclareAtMethod() {
		return kind.equals(AT_METHOD);
	}

	public boolean isDeclareAtType() {
		return kind.equals(AT_TYPE);
	}

	public boolean isDeclareAtField() {
		return kind.equals(AT_FIELD);
	}

	/**
	 * @return the type of the annotation
	 */
	public ResolvedType getAnnotationType() {
		if (annotationType == null) {
			String annotationMethod = annotationMethods.get(0);
			for (Iterator<ResolvedMember> iter = containingAspect.getMethods(true, true); iter.hasNext();) {
				ResolvedMember member = iter.next();
				if (member.getName().equals(annotationMethod)) {
					ResolvedType[] annoTypes = member.getAnnotationTypes();
					if (annoTypes == null) {
						// if weaving broken code, this can happen
						return null;
					}
					int idx = 0;
					if (annoTypes[0].getSignature().equals("Lorg/aspectj/internal/lang/annotation/ajcDeclareAnnotation;")) {
						idx = 1;
					}
					annotationType = annoTypes[idx];
					break;
				}
			}
		}
		return annotationType;
	}

	/**
	 * @return true if the annotation specified is allowed on a field
	 */
	public boolean isAnnotationAllowedOnField() {
		ensureAnnotationDiscovered();
		return annotation.allowedOnField();
	}

	public String getPatternAsString() {
		if (signaturePattern != null) {
			return signaturePattern.toString();
		}
		if (typePattern != null) {
			return typePattern.toString();
		}
		return "DONT KNOW";
	}

	/**
	 * Return true if this declare annotation could ever match something in the specified type - only really able to make
	 * intelligent decision if a type was specified in the sig/type pattern signature.
	 */
	public boolean couldEverMatch(ResolvedType type) {
		// Haven't implemented variant for typePattern (doesn't seem worth it!)
		// BUGWARNING This test might not be sufficient for funny cases relating
		// to interfaces and the use of '+' - but it seems really important to
		// do something here so we don't iterate over all fields and all methods
		// in all types exposed to the weaver! So look out for bugs here and
		// we can update the test as appropriate.
		if (signaturePattern != null) {
			return signaturePattern.couldEverMatch(type);
		}
		return true;
	}

	/**
	 * Provide a name suffix so that we can tell the different declare annotations forms apart in the AjProblemReporter
	 */
	@Override
	public String getNameSuffix() {
		return getKind().toString();
	}

	/**
	 * Captures type of declare annotation (method/type/field/constructor)
	 */
	public static class Kind {
		private final int id;
		private String s;

		private Kind(int n, String name) {
			id = n;
			s = name;
		}

		@Override
		public int hashCode() {
			return (19 + 37 * id);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Kind)) {
				return false;
			}
			Kind other = (Kind) obj;
			return other.id == id;
		}

		@Override
		public String toString() {
			return "at_" + s;
		}
	}

	boolean isRemover = false;

	public void setRemover(boolean b) {
		isRemover = b;
	}

	public boolean isRemover() {
		return isRemover;
	}
}
