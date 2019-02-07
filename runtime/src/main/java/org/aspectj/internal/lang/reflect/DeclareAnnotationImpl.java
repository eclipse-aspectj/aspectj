/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;

import java.lang.annotation.Annotation;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.DeclareAnnotation;
import org.aspectj.lang.reflect.SignaturePattern;
import org.aspectj.lang.reflect.TypePattern;

/**
 * @author colyer
 *
 */
public class DeclareAnnotationImpl implements DeclareAnnotation {

	private Annotation theAnnotation;
	private String annText;
	private AjType<?> declaringType;
	private DeclareAnnotation.Kind kind;
	private TypePattern typePattern;
	private SignaturePattern signaturePattern;
	
	public DeclareAnnotationImpl(AjType<?> declaring, String kindString, String pattern, Annotation ann, String annText) {
		this.declaringType = declaring;
		if (kindString.equals("at_type")) this.kind = DeclareAnnotation.Kind.Type;
		else if (kindString.equals("at_field")) this.kind = DeclareAnnotation.Kind.Field;
		else if (kindString.equals("at_method")) this.kind = DeclareAnnotation.Kind.Method;
		else if (kindString.equals("at_constructor")) this.kind = DeclareAnnotation.Kind.Constructor;
		else throw new IllegalStateException("Unknown declare annotation kind: " + kindString);
		if (kind == DeclareAnnotation.Kind.Type) {
			this.typePattern = new TypePatternImpl(pattern);
		} else {
			this.signaturePattern = new SignaturePatternImpl(pattern);
		}
		this.theAnnotation = ann;
		this.annText = annText;
	}

	public AjType<?> getDeclaringType() {
		return this.declaringType;
	}

	public Kind getKind() {
		return this.kind;
	}

	public SignaturePattern getSignaturePattern() {
		return this.signaturePattern;
	}

	public TypePattern getTypePattern() {
		return this.typePattern;
	}

	public Annotation getAnnotation() {
		return this.theAnnotation;
	}
	
	public String getAnnotationAsText() {
		return this.annText;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("declare @");
		switch(getKind()) {
		case Type:
			sb.append("type : ");
			sb.append(getTypePattern().asString());
			break;
		case Method: 
			sb.append("method : "); 
			sb.append(getSignaturePattern().asString());
			break;
		case Field: 
			sb.append("field : "); 
			sb.append(getSignaturePattern().asString());
			break;
		case Constructor: 
			sb.append("constructor : "); 
			sb.append(getSignaturePattern().asString());
			break;
		}
		sb.append(" : ");
		sb.append(getAnnotationAsText());
		return sb.toString();
	}

}
