/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValueGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePairGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementValueGen;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValueGen;
import org.aspectj.weaver.AbstractAnnotationAJ;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * Wraps a Bcel Annotation object and uses it to answer AnnotationAJ method calls. This is cheaper than translating all Bcel
 * annotations into AnnotationAJ objects.
 * 
 * @author AndyClement
 */
public class BcelAnnotation extends AbstractAnnotationAJ {

	private final AnnotationGen bcelAnnotation;

	public BcelAnnotation(AnnotationGen theBcelAnnotation, World world) {
		super(UnresolvedType.forSignature(theBcelAnnotation.getTypeSignature()).resolve(world));
		this.bcelAnnotation = theBcelAnnotation;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set /* of String */getTargets() {
		if (!type.equals(UnresolvedType.AT_TARGET)) {
			return Collections.EMPTY_SET;
		}
		List values = bcelAnnotation.getValues();
		ElementNameValuePairGen envp = (ElementNameValuePairGen) values.get(0);
		ArrayElementValueGen aev = (ArrayElementValueGen) envp.getValue();
		ElementValueGen[] evs = aev.getElementValuesArray();
		Set targets = new HashSet();
		for (int i = 0; i < evs.length; i++) {
			EnumElementValueGen ev = (EnumElementValueGen) evs[i];
			targets.add(ev.getEnumValueString());
		}
		return targets;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNameValuePair(String name, String value) {
		return bcelAnnotation.hasNameValuePair(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNamedValue(String name) {
		return bcelAnnotation.hasNamedValue(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String stringify() {
		StringBuffer sb = new StringBuffer();
		sb.append("@").append(type.getClassName());
		List values = bcelAnnotation.getValues();
		if (values != null && values.size() != 0) {
			sb.append("(");
			for (Iterator iterator = values.iterator(); iterator.hasNext();) {
				ElementNameValuePairGen nvPair = (ElementNameValuePairGen) iterator.next();
				sb.append(nvPair.getNameString()).append("=").append(nvPair.getValue().stringifyValue());
			}
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRuntimeVisible() {
		return this.bcelAnnotation.isRuntimeVisible();
	}

	/**
	 * @return return the real bcel annotation being wrapped
	 */
	public AnnotationGen getBcelAnnotation() {
		return bcelAnnotation;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringFormOfValue(String name) {
		List annotationValues = this.bcelAnnotation.getValues();
		if (annotationValues == null || annotationValues.size() == 0) {
			return null;
		} else {
			for (Iterator iterator = annotationValues.iterator(); iterator.hasNext();) {
				ElementNameValuePairGen nvPair = (ElementNameValuePairGen) iterator.next();
				if (nvPair.getNameString().equals(name)) {
					return nvPair.getValue().stringifyValue();
				}
			}
			return null;
		}
	}
}
