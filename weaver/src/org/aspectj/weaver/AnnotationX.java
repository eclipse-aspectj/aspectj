/*******************************************************************************
 * Copyright (c) 2005 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement       initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValue;

/**
 * An AnnotationX is the 'holder' for a BCEL annotation - we have this holder
 * so that types about the bcel weaver package can work with something not
 * BCEL specific.
 */
public class AnnotationX {
	
  private Annotation theRealAnnotation;
  private ResolvedTypeX signature = null;
  
  // @target meta-annotation related stuff, built lazily
  private boolean    lookedForAtTargetAnnotation = false;
  private AnnotationX atTargetAnnotation          = null;
  private Set         supportedTargets            = null;
  
  public AnnotationX(Annotation a,World world) {
  	theRealAnnotation = a;
  	signature = TypeX.forSignature(theRealAnnotation.getTypeSignature()).resolve(world);
  }

  public Annotation getBcelAnnotation() {
	return theRealAnnotation;
  }
  
  public TypeX getSignature() {
  	return signature;
  }
  
  public String toString() {
  	return theRealAnnotation.toString();
  }


  public String getTypeName() {
	return theRealAnnotation.getTypeName();
  }

  public String getTypeSignature() {
	return theRealAnnotation.getTypeSignature();
  }

  
  // @target related helpers
  /**
   * return true if this annotation can target an annotation type
   */
  public boolean allowedOnAnnotationType() {
  	ensureAtTargetInitialized();
  	if (atTargetAnnotation == null) return true; // if no target specified, then return true
  	return supportedTargets.contains("ANNOTATION_TYPE");
  }

  /**
   * return true if this annotation is marked with @target()
   */
  public boolean specifiesTarget() {
  	ensureAtTargetInitialized();
  	return atTargetAnnotation!=null;
  }

  /**
   * return true if this annotation can target a 'regular' type.
   * A 'regular' type is enum/class/interface - it is *not* annotation.
   */
  public boolean allowedOnRegularType() {
  	ensureAtTargetInitialized();
  	if (atTargetAnnotation == null) return true; // if no target specified, then return true
  	return supportedTargets.contains("TYPE");
  }

  /** 
   * Use in messages about this annotation
   */
  public String stringify() {
  	return signature.getName();
  }
  
  public String getValidTargets() {
  	StringBuffer sb = new StringBuffer();
  	sb.append("{");
  	for (Iterator iter = supportedTargets.iterator(); iter.hasNext();) {
		String evalue = (String) iter.next();
		sb.append(evalue);
		if (iter.hasNext()) sb.append(",");
	}
  	sb.append("}");
  	return sb.toString();
  }
  
  
  
  // privates

  /**
   * Helper method to retrieve an annotation on an annotation e.g.
   * retrieveAnnotationOnAnnotation(TypeX.AT_TARGET)
   */
  private AnnotationX retrieveAnnotationOnAnnotation(TypeX requiredAnnotationSignature) {
	AnnotationX[] annos = signature.getAnnotations();
	for (int i = 0; i < annos.length; i++) {
		AnnotationX annotationX = annos[i];
		if (annotationX.getSignature().equals(requiredAnnotationSignature)) return annos[i];
	}
	return null;
  }

  /**
   * Makes sure we have looked for the @target() annotation on this annotation.
   * Calling this method initializes (and caches) the information for later use.
   */
  private void ensureAtTargetInitialized() {
	if (!lookedForAtTargetAnnotation) {
  		lookedForAtTargetAnnotation = true;
  		atTargetAnnotation = retrieveAnnotationOnAnnotation(TypeX.AT_TARGET);
  		if (atTargetAnnotation != null) {
  			supportedTargets = new HashSet();
  			List values = atTargetAnnotation.getBcelAnnotation().getValues();
  		  	ElementNameValuePair envp = (ElementNameValuePair)values.get(0);
  		  	ArrayElementValue aev = (ArrayElementValue)envp.getValue();
  		  	ElementValue[] evs = aev.getElementValuesArray();
  		  	for (int i = 0; i < evs.length; i++) {
  				EnumElementValue ev = (EnumElementValue)evs[i];
  				supportedTargets.add(ev.getEnumValueString());
  			}
  		}
  	}
  }

  /**
   * @return true if this annotation can be put on a field
   */
  public boolean allowedOnField() {
	ensureAtTargetInitialized();
	if (atTargetAnnotation == null) return true; // if no target specified, then return true
  	return supportedTargets.contains("FIELD");
  }

}