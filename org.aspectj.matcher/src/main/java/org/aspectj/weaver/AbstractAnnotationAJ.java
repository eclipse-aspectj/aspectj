/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractAnnotationAJ implements AnnotationAJ {

	protected final ResolvedType type;

	private Set<String> supportedTargets = null; // @target meta annotation

	public AbstractAnnotationAJ(ResolvedType type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	public final ResolvedType getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getTypeSignature() {
		return type.getSignature();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getTypeName() {
		return type.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean allowedOnAnnotationType() {
		ensureAtTargetInitialized();
		if (supportedTargets.isEmpty()) {
			return true;
		}
		return supportedTargets.contains("ANNOTATION_TYPE");
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean allowedOnField() {
		ensureAtTargetInitialized();
		if (supportedTargets.isEmpty()) {
			return true;
		}
		return supportedTargets.contains("FIELD");
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean allowedOnRegularType() {
		ensureAtTargetInitialized();
		if (supportedTargets.isEmpty()) {
			return true;
		}
		return supportedTargets.contains("TYPE");
	}

	/**
	 * {@inheritDoc}
	 */
	public final void ensureAtTargetInitialized() {
		if (supportedTargets == null) {
			AnnotationAJ atTargetAnnotation = retrieveAnnotationOnAnnotation(UnresolvedType.AT_TARGET);
			if (atTargetAnnotation == null) {
				supportedTargets = Collections.emptySet();
			} else {
				supportedTargets = atTargetAnnotation.getTargets();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getValidTargets() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (Iterator<String> iter = supportedTargets.iterator(); iter.hasNext();) {
			String evalue = iter.next();
			sb.append(evalue);
			if (iter.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean specifiesTarget() {
		ensureAtTargetInitialized();
		return !supportedTargets.isEmpty();
	}

	/**
	 * Helper method to retrieve an annotation on an annotation e.g. retrieveAnnotationOnAnnotation(UnresolvedType.AT_TARGET)
	 */
	private final AnnotationAJ retrieveAnnotationOnAnnotation(UnresolvedType requiredAnnotationSignature) {
		AnnotationAJ[] annos = type.getAnnotations();
		for (int i = 0; i < annos.length; i++) {
			AnnotationAJ a = annos[i];
			if (a.getTypeSignature().equals(requiredAnnotationSignature.getSignature())) {
				return annos[i];
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public abstract boolean isRuntimeVisible();

	/**
	 * {@inheritDoc}
	 */
	public abstract Set<String> getTargets();

	/**
	 * {@inheritDoc}
	 */
	public abstract boolean hasNameValuePair(String name, String value);

	/**
	 * {@inheritDoc}
	 */
	public abstract boolean hasNamedValue(String name);

	/**
	 * {@inheritDoc}
	 */
	public abstract String stringify();

}
