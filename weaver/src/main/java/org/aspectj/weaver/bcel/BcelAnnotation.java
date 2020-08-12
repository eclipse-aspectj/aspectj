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
import java.util.List;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.weaver.AbstractAnnotationAJ;
import org.aspectj.weaver.ResolvedType;
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

	public BcelAnnotation(AnnotationGen theBcelAnnotation, ResolvedType resolvedAnnotationType) {
		super(resolvedAnnotationType);
		this.bcelAnnotation = theBcelAnnotation;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		List<NameValuePair> nvPairs = bcelAnnotation.getValues();
		sb.append("Anno[" + getTypeSignature() + " " + (isRuntimeVisible() ? "rVis" : "rInvis"));
		if (nvPairs.size() > 0) {
			sb.append(" ");
			int i = 0;
			for (NameValuePair element : nvPairs) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(element.getNameString()).append("=").append(element.getValue().toString());
				i++;
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getTargets() {
		if (!type.equals(UnresolvedType.AT_TARGET)) {
			return Collections.emptySet();
		}
		List<NameValuePair> values = bcelAnnotation.getValues();
		NameValuePair envp = values.get(0);
		ArrayElementValue aev = (ArrayElementValue) envp.getValue();
		ElementValue[] evs = aev.getElementValuesArray();
		Set<String> targets = new HashSet<>();
		for (ElementValue elementValue : evs) {
			EnumElementValue ev = (EnumElementValue) elementValue;
			targets.add(ev.getEnumValueString());
		}
		return targets;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNameValuePair(String name, String value) {
		return bcelAnnotation.hasNameValuePair(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNamedValue(String name) {
		return bcelAnnotation.hasNamedValue(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String stringify() {
		StringBuffer sb = new StringBuffer();
		sb.append("@").append(type.getClassName());
		List<NameValuePair> values = bcelAnnotation.getValues();
		if (values != null && values.size() != 0) {
			sb.append("(");
			for (NameValuePair nvPair : values) {
				sb.append(nvPair.getNameString()).append("=").append(nvPair.getValue().stringifyValue());
			}
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
		List<NameValuePair> annotationValues = this.bcelAnnotation.getValues();
		if (annotationValues == null || annotationValues.size() == 0) {
			return null;
		} else {
			for (NameValuePair nvPair : annotationValues) {
				if (nvPair.getNameString().equals(name)) {
					return nvPair.getValue().stringifyValue();
				}
			}
			return null;
		}
	}
}
