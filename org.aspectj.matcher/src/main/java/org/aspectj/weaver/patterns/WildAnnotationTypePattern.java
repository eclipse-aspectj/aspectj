/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * @author colyer
 * @author Andy Clement
 */
public class WildAnnotationTypePattern extends AnnotationTypePattern {

	private TypePattern typePattern;
	private boolean resolved = false;
	Map<String, String> annotationValues;

	public WildAnnotationTypePattern(TypePattern typePattern) {
		super();
		this.typePattern = typePattern;
		this.setLocation(typePattern.getSourceContext(), typePattern.start, typePattern.end);
	}

	public WildAnnotationTypePattern(TypePattern typePattern, Map<String, String> annotationValues) {
		super();
		this.typePattern = typePattern;
		this.annotationValues = annotationValues;
		// PVAL make the location be from start of type pattern to end of values
		this.setLocation(typePattern.getSourceContext(), typePattern.start, typePattern.end);
	}

	public TypePattern getTypePattern() {
		return typePattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#matches(org.aspectj.weaver.AnnotatedElement)
	 */
	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return matches(annotated, null);
	}

	/**
	 * Resolve any annotation values specified, checking they are all well formed (valid names, valid values)
	 * 
	 * @param annotationType the annotation type for which the values have been specified
	 * @param scope the scope within which to resolve type references (eg. Color.GREEN)
	 */
	protected void resolveAnnotationValues(ResolvedType annotationType, IScope scope) {
		if (annotationValues == null) {
			return;
		}
		// Check any values specified are OK:
		// - the value names are for valid annotation fields
		// - the specified values are of the correct type
		// - for enums, check the specified values can be resolved in the specified scope
		Map<String,String> replacementValues = new HashMap<>();
		Set<String> keys = annotationValues.keySet();
		ResolvedMember[] ms = annotationType.getDeclaredMethods();
		for (String k: keys) {
			String key = k;
			// a trailing ! indicates the the user expressed key!=value rather than key=value as a match constraint
			if (k.endsWith("!")) {
				key = key.substring(0, k.length() - 1);
			}
			String v = annotationValues.get(k);
			boolean validKey = false;
			for (ResolvedMember resolvedMember : ms) {
				if (resolvedMember.getName().equals(key) && resolvedMember.isAbstract()) {
					validKey = true;
					ResolvedType t = resolvedMember.getReturnType().resolve(scope.getWorld());
					if (t.isEnum()) {
						// value must be an enum reference X.Y
						int pos = v.lastIndexOf(".");
						if (pos == -1) {
							IMessage m = MessageUtil.error(
									WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "enum"), getSourceLocation());
							scope.getWorld().getMessageHandler().handleMessage(m);
						} else {
							String typename = v.substring(0, pos);
							ResolvedType rt = scope.lookupType(typename, this).resolve(scope.getWorld());
							v = rt.getSignature() + v.substring(pos + 1); // from 'Color.RED' to 'Lp/Color;RED'
							replacementValues.put(k, v);
							break;
						}
					} else if (t.isPrimitiveType()) {
						if (t.getSignature().equals("I")) {
							try {
								int value = Integer.parseInt(v);
								replacementValues.put(k, Integer.toString(value));
								break;
							} catch (NumberFormatException nfe) {
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "int"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}
						} else if (t.getSignature().equals("F")) {
							try {
								float value = Float.parseFloat(v);
								replacementValues.put(k, Float.toString(value));
								break;
							} catch (NumberFormatException nfe) {
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "float"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}

						} else if (t.getSignature().equals("Z")) {
							if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) {
								// is it ok !
							} else {
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "boolean"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}
						} else if (t.getSignature().equals("S")) {
							try {
								short value = Short.parseShort(v);
								replacementValues.put(k, Short.toString(value));
								break;
							} catch (NumberFormatException nfe) {
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "short"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}
						} else if (t.getSignature().equals("J")) {
							try {
								replacementValues.put(k, Long.toString(Long.parseLong(v)));
								break;
							} catch (NumberFormatException nfe) {
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "long"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}
						} else if (t.getSignature().equals("D")) {
							try {
								replacementValues.put(k, Double.toString(Double.parseDouble(v)));
								break;
							} catch (NumberFormatException nfe) {
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "double"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}
						} else if (t.getSignature().equals("B")) {
							try {
								replacementValues.put(k, Byte.toString(Byte.parseByte(v)));
								break;
							} catch (NumberFormatException nfe) {
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "byte"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}
						} else if (t.getSignature().equals("C")) {
							if (v.length() != 3) { // '?'
								IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE, v, "char"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							} else {
								replacementValues.put(k, v.substring(1, 2));
								break;
							}
						} else {
							throw new RuntimeException("Not implemented for " + t);
						}
					} else if (t.equals(ResolvedType.JL_STRING)) {
						// nothing to do, it will be OK
					} else if (t.equals(ResolvedType.JL_CLASS) || (t.isParameterizedOrGenericType() && t.getRawType().equals(ResolvedType.JL_CLASS))) {
						String typename = v.substring(0, v.lastIndexOf('.')); // strip off '.class'
						ResolvedType rt = scope.lookupType(typename, this).resolve(scope.getWorld());
						if (rt.isMissing()) {
							IMessage m = MessageUtil.error("Unable to resolve type '" + v + "' specified for value '" + k + "'",
									getSourceLocation());
							scope.getWorld().getMessageHandler().handleMessage(m);
						}
						replacementValues.put(k, rt.getSignature());
						break;
					} else {
						if (t.isAnnotation()) {
							if (v.contains("(")) {
								throw new RuntimeException(
										"Compiler limitation: annotation values can only currently be marker annotations (no values): "
												+ v);
							}
							String typename = v.substring(1);
							ResolvedType rt = scope.lookupType(typename, this).resolve(scope.getWorld());
							if (rt.isMissing()) {
								IMessage m = MessageUtil.error(
										"Unable to resolve type '" + v + "' specified for value '" + k + "'", getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);
							}
							replacementValues.put(k, rt.getSignature());
							break;
//						} else if (t.isArray()) {
							// Looks like {} aren't pseudotokens in the parser so they don't get through for our pointcut parser
//							// @Foo(value=[Foo.class])
//							String typename = v.substring(0, v.lastIndexOf('.')); // strip off '.class'
//							ResolvedType rt = scope.lookupType(typename, this).resolve(scope.getWorld());
//							if (rt.isMissing()) {
//								IMessage m = MessageUtil.error("Unable to resolve type '" + v + "' specified for value '" + k + "'",
//										getSourceLocation());
//								scope.getWorld().getMessageHandler().handleMessage(m);
//							}
//							replacementValues.put(k, rt.getSignature());
						} else {
							scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.UNSUPPORTED_ANNOTATION_VALUE_TYPE, t), getSourceLocation()));
							replacementValues.put(k, "");
						}
					}
				}
			}
			if (!validKey) {
				IMessage m = MessageUtil.error(WeaverMessages.format(WeaverMessages.UNKNOWN_ANNOTATION_VALUE, annotationType, k),
						getSourceLocation());
				scope.getWorld().getMessageHandler().handleMessage(m);
			}
		}
		annotationValues.putAll(replacementValues);
	}

	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations) {
		if (!resolved) {
			throw new IllegalStateException("Can't match on an unresolved annotation type pattern");
		}
		if (annotationValues != null && !typePattern.hasFailedResolution()) {
			// PVAL improve this restriction, would allow '*(value=Color.RED)'
			throw new IllegalStateException("Cannot use annotationvalues with a wild annotation pattern");
		}
		if (isForParameterAnnotationMatch()) {
			if (parameterAnnotations != null && parameterAnnotations.length != 0) {
				for (ResolvedType parameterAnnotation : parameterAnnotations) {
					if (typePattern.matches(parameterAnnotation, TypePattern.STATIC).alwaysTrue()) {
						return FuzzyBoolean.YES;
					}
				}
			}
		} else {
			// matches if the type of any of the annotations on the AnnotatedElement is
			// matched by the typePattern.
			ResolvedType[] annTypes = annotated.getAnnotationTypes();
			if (annTypes != null && annTypes.length != 0) {
				for (ResolvedType annType : annTypes) {
					if (typePattern.matches(annType, TypePattern.STATIC).alwaysTrue()) {
						return FuzzyBoolean.YES;
					}
				}
			}
		}
		return FuzzyBoolean.NO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolve(org.aspectj.weaver.World)
	 */
	@Override
	public void resolve(World world) {
		if (!resolved) {
			// attempt resolution - this helps with the Spring bug where they resolve() the pointcut in no scope (SPR-5307)
			if (typePattern instanceof WildTypePattern && (annotationValues == null || annotationValues.isEmpty())) {
				WildTypePattern wildTypePattern = (WildTypePattern) typePattern;
				String fullyQualifiedName = wildTypePattern.maybeGetCleanName();
				if (fullyQualifiedName != null && fullyQualifiedName.contains(".")) {
					ResolvedType resolvedType = world.resolve(UnresolvedType.forName(fullyQualifiedName));
					if (resolvedType != null && !resolvedType.isMissing()) {
						typePattern = new ExactTypePattern(resolvedType, false, false);
					}
				}
			}
			resolved = true;
		}
	}

	/**
	 * This can modify in place, or return a new TypePattern if the type changes.
	 */
	@Override
	public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding) {
		if (!scope.getWorld().isInJava5Mode()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.ANNOTATIONS_NEED_JAVA5), getSourceLocation()));
			return this;
		}
		if (resolved) {
			return this;
		}
		this.typePattern = typePattern.resolveBindings(scope, bindings, false, false);
		resolved = true;
		if (typePattern instanceof ExactTypePattern) {
			ExactTypePattern et = (ExactTypePattern) typePattern;
			if (!et.getExactType().resolve(scope.getWorld()).isAnnotation()) {
				IMessage m = MessageUtil.error(
						WeaverMessages.format(WeaverMessages.REFERENCE_TO_NON_ANNOTATION_TYPE, et.getExactType().getName()),
						getSourceLocation());
				scope.getWorld().getMessageHandler().handleMessage(m);
				resolved = false;
			}
			ResolvedType annotationType = et.getExactType().resolve(scope.getWorld());
			resolveAnnotationValues(annotationType, scope);
			ExactAnnotationTypePattern eatp = new ExactAnnotationTypePattern(annotationType, annotationValues);
			eatp.copyLocationFrom(this);
			if (isForParameterAnnotationMatch()) {
				eatp.setForParameterAnnotationMatch();
			}
			return eatp;
		} else {
			return this;
		}
	}

	@Override
	public AnnotationTypePattern parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		WildAnnotationTypePattern ret = new WildAnnotationTypePattern(typePattern.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		ret.resolved = resolved;
		return ret;
	}

	private static final byte VERSION = 1; // rev if ser. form changes

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.WILD);
		s.writeByte(VERSION);
		typePattern.write(s);
		writeLocation(s);
		s.writeBoolean(isForParameterAnnotationMatch());
		// PVAL
		if (annotationValues == null) {
			s.writeInt(0);
		} else {
			s.writeInt(annotationValues.size());
			Set<String> key = annotationValues.keySet();
			for (String k : key) {
				s.writeUTF(k);
				s.writeUTF(annotationValues.get(k));
			}
		}
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		WildAnnotationTypePattern ret;
		byte version = s.readByte();
		if (version > VERSION) {
			throw new BCException("ExactAnnotationTypePattern was written by a newer version of AspectJ");
		}
		TypePattern t = TypePattern.read(s, context);
		ret = new WildAnnotationTypePattern(t);
		ret.readLocation(context, s);
		if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ160) {
			if (s.readBoolean()) {
				ret.setForParameterAnnotationMatch();
			}
		}
		if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ160M2) {
			int annotationValueCount = s.readInt();
			if (annotationValueCount > 0) {
				Map<String, String> aValues = new HashMap<>();
				for (int i = 0; i < annotationValueCount; i++) {
					String key = s.readUTF();
					String val = s.readUTF();
					aValues.put(key, val);
				}
				ret.annotationValues = aValues;
			}
		}
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WildAnnotationTypePattern)) {
			return false;
		}
		WildAnnotationTypePattern other = (WildAnnotationTypePattern) obj;
		return other.typePattern.equals(typePattern)
				&& this.isForParameterAnnotationMatch() == other.isForParameterAnnotationMatch()
				&& (annotationValues == null ? other.annotationValues == null : annotationValues.equals(other.annotationValues));
	}

	@Override
	public int hashCode() {
		return (((17 + 37 * typePattern.hashCode()) * 37 + (isForParameterAnnotationMatch() ? 0 : 1)) * 37)
				+ (annotationValues == null ? 0 : annotationValues.hashCode());
	}

	@Override
	public String toString() {
		return "@(" + typePattern.toString() + ")";
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
