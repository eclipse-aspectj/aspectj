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
package org.aspectj.weaver.bcel;

import java.util.List;

import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;

/**
 * An AnnotationAccessVar represents access to a particular annotation, whilst an AnnotationAccessFieldVar represents access to a
 * specific field of that annotation.
 * 
 * @author Andy Clement
 */
class AnnotationAccessFieldVar extends BcelVar {

	private AnnotationAccessVar annoAccessor;
	private ResolvedType annoFieldOfInterest;
	private String name;
	private int elementValueType;

	public AnnotationAccessFieldVar(AnnotationAccessVar aav, ResolvedType annoFieldOfInterest, String name) {
		super(annoFieldOfInterest, 0);
		this.annoAccessor = aav;
		this.name = name;
		String sig = annoFieldOfInterest.getSignature();
		if (sig.length() == 1) {
			switch (sig.charAt(0)) {
			case 'I':
				elementValueType = ElementValue.PRIMITIVE_INT;
				break;
			default:
				throw new IllegalStateException(sig);
			}
		} else if (sig.equals("Ljava/lang/String;")) {
			elementValueType = ElementValue.STRING;
		} else if (annoFieldOfInterest.isEnum()) {
			elementValueType = ElementValue.ENUM_CONSTANT;
		} else {
			throw new IllegalStateException(sig);
		}
		this.annoFieldOfInterest = annoFieldOfInterest;
	}

	@Override
	public void appendLoadAndConvert(InstructionList il, InstructionFactory fact, ResolvedType toType) {
		// Only possible to do annotation field value extraction at MethodExecution
		if (annoAccessor.getKind() != Shadow.MethodExecution) {
			return;
		}
		String annotationOfInterestSignature = annoAccessor.getType().getSignature();
		// So we have an entity that has an annotation on and within it is the value we want
		Member holder = annoAccessor.getMember();
		AnnotationAJ[] annos = holder.getAnnotations();
		for (AnnotationAJ anno : annos) {
			AnnotationGen annotation = ((BcelAnnotation) anno).getBcelAnnotation();
			boolean foundValueInAnnotationUsage = false;
			if (annotation.getTypeSignature().equals(annotationOfInterestSignature)) {
				ResolvedMember[] annotationFields = toType.getWorld()
						.resolve(UnresolvedType.forSignature(annotation.getTypeSignature())).getDeclaredMethods();
				// Check how many fields there are of the type we are looking for. If >1 then we'll need
				// to use the name to choose the right one
				int countOfType = 0;
				for (ResolvedMember annotationField : annotationFields) {
					if (annotationField.getType().equals(annoFieldOfInterest)) {
						countOfType++;
					}
				}

				// this block deals with an annotation that has actual values (i.e. not falling back to default values)
				List<NameValuePair> nvps = annotation.getValues();
				for (NameValuePair nvp : nvps) {
					// If multiple of the same type, match by name
					if (countOfType > 1) {
						if (!nvp.getNameString().equals(name)) {
							continue;
						}
					}
					ElementValue o = nvp.getValue();
					if (o.getElementValueType() != elementValueType) {
						continue;
					}
					if (o instanceof EnumElementValue) {
						EnumElementValue v = (EnumElementValue) o;
						String s = v.getEnumTypeString();
						ResolvedType rt = toType.getWorld().resolve(UnresolvedType.forSignature(s));
						if (rt.equals(toType)) {
							il.append(fact.createGetStatic(rt.getName(), v.getEnumValueString(), Type.getType(rt.getSignature())));
							foundValueInAnnotationUsage = true;
						}
					} else if (o instanceof SimpleElementValue) {
						SimpleElementValue v = (SimpleElementValue) o;
						switch (v.getElementValueType()) {
						case ElementValue.PRIMITIVE_INT:
							il.append(fact.createConstant(v.getValueInt()));
							foundValueInAnnotationUsage = true;
							break;
						case ElementValue.STRING:
							il.append(fact.createConstant(v.getValueString()));
							foundValueInAnnotationUsage = true;
							break;
						default:
							throw new IllegalStateException("NYI: Unsupported annotation value binding for " + o);
						}
					}
					if (foundValueInAnnotationUsage) {
						break;
					}
				}
				// this block deals with default values
				if (!foundValueInAnnotationUsage) {
					for (ResolvedMember annotationField : annotationFields) {
						if (countOfType > 1) {
							if (!annotationField.getName().equals(name)) {
								continue;
							}
						}
						if (!annotationField.getType().getSignature().equals(annoFieldOfInterest.getSignature())) {
							continue;
						}
						if (annotationField.getType().getSignature().equals("I")) {
							int ivalue = Integer.parseInt(annotationField.getAnnotationDefaultValue());
							il.append(fact.createConstant(ivalue));
							foundValueInAnnotationUsage = true;
							break;
						} else if (annotationField.getType().getSignature().equals("Ljava/lang/String;")) {
							String svalue = annotationField.getAnnotationDefaultValue();
							il.append(fact.createConstant(svalue));
							foundValueInAnnotationUsage = true;
							break;
						} else {
							String dvalue = annotationField.getAnnotationDefaultValue();
							// form will be LBLAHBLAHBLAH;X where X is the field within X
							String typename = dvalue.substring(0, dvalue.lastIndexOf(';') + 1);
							String field = dvalue.substring(dvalue.lastIndexOf(';') + 1);
							ResolvedType rt = toType.getWorld().resolve(UnresolvedType.forSignature(typename));
							il.append(fact.createGetStatic(rt.getName(), field, Type.getType(rt.getSignature())));
							foundValueInAnnotationUsage = true;
							break;
						}
					}
				}
			}
			if (foundValueInAnnotationUsage) {
				break;
			}
		}
	}

	@Override
	public void insertLoad(InstructionList il, InstructionFactory fact) {
		// Only possible to do annotation field value extraction at
		// MethodExecution
		if (annoAccessor.getKind() != Shadow.MethodExecution) {
			return;
		}
		appendLoadAndConvert(il, fact, annoFieldOfInterest);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
