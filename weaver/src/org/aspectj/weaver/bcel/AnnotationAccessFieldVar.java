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

import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValue;
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

	public AnnotationAccessFieldVar(AnnotationAccessVar aav, ResolvedType annoFieldOfInterest) {
		super(annoFieldOfInterest, 0);
		this.annoAccessor = aav;
		this.annoFieldOfInterest = annoFieldOfInterest;
	}

	public void appendLoadAndConvert(InstructionList il, InstructionFactory fact, ResolvedType toType) {
		// Only possible to do annotation field value extraction at
		// MethodExecution
		if (annoAccessor.getKind() != Shadow.MethodExecution) {
			return;
		}
		String annotationOfInterestSignature = annoAccessor.getType().getSignature();
		// So we have an entity that has an annotation on and within it is the
		// value we want
		Member holder = annoAccessor.getMember();
		AnnotationAJ[] annos = holder.getAnnotations();
		for (int i = 0; i < annos.length; i++) {
			AnnotationGen annotation = ((BcelAnnotation) annos[i]).getBcelAnnotation();
			if (annotation.getTypeSignature().equals(annotationOfInterestSignature)) {
				List vals = annotation.getValues();
				boolean doneAndDusted = false;
				for (Iterator iterator = vals.iterator(); iterator.hasNext();) {
					NameValuePair object = (NameValuePair) iterator.next();
					EnumElementValue v = (EnumElementValue) object.getValue();
					String s = v.getEnumTypeString();
					ResolvedType rt = toType.getWorld().resolve(UnresolvedType.forSignature(s));
					if (rt.equals(toType)) {
						il.append(fact.createGetStatic(rt.getName(), v.getEnumValueString(), Type.getType(rt.getSignature())));
						doneAndDusted = true;
					}
				}
				if (!doneAndDusted) {
					ResolvedMember[] annotationFields = toType.getWorld().resolve(
							UnresolvedType.forSignature(annotation.getTypeSignature())).getDeclaredMethods();

					// ResolvedMember[] fs = rt.getDeclaredFields();
					for (int ii = 0; ii < annotationFields.length; ii++) {
						if (annotationFields[ii].getType().equals(annoFieldOfInterest)) {
							String dvalue = annotationFields[ii].getAnnotationDefaultValue();
							// form will be LBLAHBLAHBLAH;X where X is the field
							// within X
							String typename = dvalue.substring(0, dvalue.lastIndexOf(';') + 1);
							String field = dvalue.substring(dvalue.lastIndexOf(';') + 1);
							ResolvedType rt = toType.getWorld().resolve(UnresolvedType.forSignature(typename));
							il.append(fact.createGetStatic(rt.getName(), field, Type.getType(rt.getSignature())));
						}
					}
				}
			}
		}
	}

	public void insertLoad(InstructionList il, InstructionFactory fact) {
		// Only possible to do annotation field value extraction at
		// MethodExecution
		if (annoAccessor.getKind() != Shadow.MethodExecution) {
			return;
		}
		appendLoadAndConvert(il, fact, annoFieldOfInterest);
	}

	public String toString() {
		return super.toString();
	}
}
