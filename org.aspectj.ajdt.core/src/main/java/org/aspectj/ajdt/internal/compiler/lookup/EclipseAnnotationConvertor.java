/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement                 initial implementation
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationNameValuePair;
import org.aspectj.weaver.AnnotationValue;
import org.aspectj.weaver.ArrayAnnotationValue;
import org.aspectj.weaver.ClassAnnotationValue;
import org.aspectj.weaver.EnumAnnotationValue;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.SimpleAnnotationValue;
import org.aspectj.weaver.StandardAnnotation;
import org.aspectj.weaver.World;

// not yet used...
public class EclipseAnnotationConvertor {
	/**
	 * Convert one eclipse annotation into an AnnotationX object containing an AnnotationAJ object.
	 *
	 * This code and the helper methods used by it will go *BANG* if they encounter anything not currently supported - this is safer
	 * than limping along with a malformed annotation. When the *BANG* is encountered the bug reporter should indicate the kind of
	 * annotation they were working with and this code can be enhanced to support it.
	 */
	public static AnnotationAJ convertEclipseAnnotation(Annotation eclipseAnnotation, World w, EclipseFactory factory) {
		// TODO if it is sourcevisible, we shouldn't let it through!!!!!!!!!
		// testcase!
		ResolvedType annotationType = factory.fromTypeBindingToRTX(eclipseAnnotation.type.resolvedType);
		// long bs = (eclipseAnnotation.bits & TagBits.AnnotationRetentionMASK);
		boolean isRuntimeVisible = (eclipseAnnotation.bits & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationRuntimeRetention;
		StandardAnnotation annotationAJ = new StandardAnnotation(annotationType, isRuntimeVisible);
		generateAnnotation(eclipseAnnotation, annotationAJ);
		return annotationAJ;
	}

	static class MissingImplementationException extends RuntimeException {
		MissingImplementationException(String reason) {
			super(reason);
		}
	}

	private static void generateAnnotation(Annotation annotation, StandardAnnotation annotationAJ) {
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
			MemberValuePair[] memberValuePairs = normalAnnotation.memberValuePairs;
			if (memberValuePairs != null) {
				int memberValuePairsLength = memberValuePairs.length;
				for (MemberValuePair memberValuePair : memberValuePairs) {
					MethodBinding methodBinding = memberValuePair.binding;
					if (methodBinding == null) {
						// is this just a marker annotation?
						throw new MissingImplementationException(
								"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation [" + annotation
										+ "]");
					} else {
						AnnotationValue av = generateElementValue(memberValuePair.value, methodBinding.returnType);
						AnnotationNameValuePair anvp = new AnnotationNameValuePair(new String(memberValuePair.name), av);
						annotationAJ.addNameValuePair(anvp);
					}
				}
			} else {
				throw new MissingImplementationException(
						"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation [" + annotation + "]");
			}
		} else if (annotation instanceof SingleMemberAnnotation) {
			// this is a single member annotation (one member value)
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
			MethodBinding methodBinding = singleMemberAnnotation.memberValuePairs()[0].binding;
			if (methodBinding == null) {
				throw new MissingImplementationException(
						"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation [" + annotation + "]");
			} else {
				AnnotationValue av = generateElementValue(singleMemberAnnotation.memberValue, methodBinding.returnType);
				annotationAJ.addNameValuePair(new AnnotationNameValuePair(new String(
						singleMemberAnnotation.memberValuePairs()[0].name), av));
			}
		} else if (annotation instanceof MarkerAnnotation) {
			MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
		} else {
			// this is a marker annotation (no member value pairs)
			throw new MissingImplementationException(
					"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation [" + annotation + "]");
		}
	}

	private static AnnotationValue generateElementValue(Expression defaultValue, TypeBinding memberValuePairReturnType) {
		Constant constant = defaultValue.constant;
		TypeBinding defaultValueBinding = defaultValue.resolvedType;
		if (defaultValueBinding == null) {
			throw new MissingImplementationException(
					"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value [" + defaultValue
					+ "]");
		} else {
			if (memberValuePairReturnType.isArrayType() && !defaultValueBinding.isArrayType()) {
				if (constant != null && constant != Constant.NotAConstant) {
					throw new MissingImplementationException(
							"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["
									+ defaultValue + "]");
					// generateElementValue(attributeOffset, defaultValue,
					// constant, memberValuePairReturnType.leafComponentType());
				} else {
					AnnotationValue av = generateElementValueForNonConstantExpression(defaultValue, defaultValueBinding);
					return new ArrayAnnotationValue(new AnnotationValue[] { av });
				}
			} else {
				if (constant != null && constant != Constant.NotAConstant) {
					if (constant instanceof IntConstant || constant instanceof BooleanConstant
							|| constant instanceof StringConstant) {
						AnnotationValue av = generateElementValueForConstantExpression(defaultValue, defaultValueBinding);
						return av;
					}
					throw new MissingImplementationException(
							"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["
									+ defaultValue + "]");
					// generateElementValue(attributeOffset, defaultValue,
					// constant, memberValuePairReturnType.leafComponentType());
				} else {
					AnnotationValue av = generateElementValueForNonConstantExpression(defaultValue, defaultValueBinding);
					return av;
				}
			}
		}
	}

	public static AnnotationValue generateElementValueForConstantExpression(Expression defaultValue, TypeBinding defaultValueBinding) {
		if (defaultValueBinding != null) {
			Constant c = defaultValue.constant;
			if (c instanceof IntConstant) {
				IntConstant iConstant = (IntConstant) c;
				return new SimpleAnnotationValue(ElementValue.PRIMITIVE_INT, iConstant.intValue());
			} else if (c instanceof BooleanConstant) {
				BooleanConstant iConstant = (BooleanConstant) c;
				return new SimpleAnnotationValue(ElementValue.PRIMITIVE_BOOLEAN, iConstant.booleanValue());
			} else if (c instanceof StringConstant) {
				StringConstant sConstant = (StringConstant) c;
				return new SimpleAnnotationValue(ElementValue.STRING, sConstant.stringValue());
			}
		}
		return null;
	}

	private static AnnotationValue generateElementValueForNonConstantExpression(Expression defaultValue,
			TypeBinding defaultValueBinding) {
		if (defaultValueBinding != null) {
			if (defaultValueBinding.isEnum()) {
				FieldBinding fieldBinding = null;
				if (defaultValue instanceof QualifiedNameReference) {
					QualifiedNameReference nameReference = (QualifiedNameReference) defaultValue;
					fieldBinding = (FieldBinding) nameReference.binding;
				} else if (defaultValue instanceof SingleNameReference) {
					SingleNameReference nameReference = (SingleNameReference) defaultValue;
					fieldBinding = (FieldBinding) nameReference.binding;
				} else {
					throw new MissingImplementationException(
							"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["
									+ defaultValue + "]");
				}
				if (fieldBinding != null) {
					String sig = new String(fieldBinding.type.signature());
					AnnotationValue enumValue = new EnumAnnotationValue(sig, new String(fieldBinding.name));
					return enumValue;
				}
				throw new MissingImplementationException(
						"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value [" + defaultValue
						+ "]");
			} else if (defaultValueBinding.isAnnotationType()) {
				throw new MissingImplementationException(
						"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value [" + defaultValue
						+ "]");
				// contents[contentsOffset++] = (byte) '@';
				// generateAnnotation((Annotation) defaultValue,
				// attributeOffset);
			} else if (defaultValueBinding.isArrayType()) {
				// array type
				if (defaultValue instanceof ArrayInitializer) {
					ArrayInitializer arrayInitializer = (ArrayInitializer) defaultValue;
					int arrayLength = arrayInitializer.expressions != null ? arrayInitializer.expressions.length : 0;
					AnnotationValue[] values = new AnnotationValue[arrayLength];
					for (int i = 0; i < arrayLength; i++) {
						values[i] = generateElementValue(arrayInitializer.expressions[i], defaultValueBinding.leafComponentType());// ,
						// attributeOffset
						// )
						// ;
					}
					ArrayAnnotationValue aav = new ArrayAnnotationValue(values);
					return aav;
				} else {
					throw new MissingImplementationException(
							"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value ["
									+ defaultValue + "]");
				}
			} else {
				// class type
				if (defaultValue instanceof ClassLiteralAccess) {
					ClassLiteralAccess cla = (ClassLiteralAccess)defaultValue;
					ClassAnnotationValue cav = new ClassAnnotationValue(new String(cla.targetType.signature()));
					return cav;
				}
				throw new MissingImplementationException(
						"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value [" + defaultValue
						+ "]");
			}
		} else {
			throw new MissingImplementationException(
					"Please raise an AspectJ bug.  AspectJ does not know how to convert this annotation value [" + defaultValue
					+ "]");
			// contentsOffset = attributeOffset;
		}
	}

}
