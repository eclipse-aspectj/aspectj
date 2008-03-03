/*******************************************************************************
 * Copyright (c) 2005 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.aspectj.apache.bcel.classfile.Utility;

/**
 * AnnotationX instances are holders for an annotation from either Bcel or
 * eclipse.  We have this holder so that types about the bcel weaver package 
 * can work with something not bytecode toolkit specific.
 */
public class AnnotationX {
	
  public static final AnnotationX[] NONE = new AnnotationX[0];
  
  private Annotation theRealBcelAnnotation;
  private AnnotationAJ theRealEclipseAnnotation;
  private int mode = -1;
  private final static int MODE_ECLIPSE = 1;
  private final static int  MODE_BCEL = 2;
  
  private ResolvedType signature = null;
  
  // @target meta-annotation related stuff, built lazily
  private boolean    lookedForAtTargetAnnotation = false;
  private AnnotationX atTargetAnnotation          = null;
  private Set         supportedTargets            = null;
  
  public AnnotationX(Annotation a,World world) {
  	theRealBcelAnnotation = a;
  	signature = UnresolvedType.forSignature(theRealBcelAnnotation.getTypeSignature()).resolve(world);
  	mode = MODE_BCEL;
  }
  
  public AnnotationX(AnnotationAJ a,World world) {
	  	theRealEclipseAnnotation = a;
	  	signature = UnresolvedType.forSignature(theRealEclipseAnnotation.getTypeSignature()).resolve(world);
	  	mode= MODE_ECLIPSE;
  }

  public Annotation getBcelAnnotation() {
	return theRealBcelAnnotation;
  }
  
  public UnresolvedType getSignature() {
  	return signature;
  }
  
  public String toString() {
	  if (mode==MODE_BCEL) return theRealBcelAnnotation.toString();
	  else 				   return theRealEclipseAnnotation.toString();
  }


  public String getTypeName() {
	if (mode==MODE_BCEL) return theRealBcelAnnotation.getTypeName();
	else				 return Utility.signatureToString(theRealEclipseAnnotation.getTypeSignature());
  }

  public String getTypeSignature() {
	  if (mode==MODE_BCEL) return theRealBcelAnnotation.getTypeSignature();
		else				 return theRealEclipseAnnotation.getTypeSignature();
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
   * retrieveAnnotationOnAnnotation(UnresolvedType.AT_TARGET)
   */
  private AnnotationX retrieveAnnotationOnAnnotation(UnresolvedType requiredAnnotationSignature) {
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
  		atTargetAnnotation = retrieveAnnotationOnAnnotation(UnresolvedType.AT_TARGET);
  		if (atTargetAnnotation != null) {
  			supportedTargets = atTargetAnnotation.getTargets();
  		}
  	}
  }
  
  /**
   * For the @Target annotation, this will return a set of the elementtypes it can be applied to.
   * For non @Target annotations, it returns null.
   */
  public Set /* of String */ getTargets() {
	  if (!signature.equals(UnresolvedType.AT_TARGET)) return null;
	  Set supportedTargets = new HashSet();
	  if (mode==MODE_BCEL) {
	    List values = getBcelAnnotation().getValues();
	  	ElementNameValuePair envp = (ElementNameValuePair)values.get(0);
	  	ArrayElementValue aev = (ArrayElementValue)envp.getValue();
	  	ElementValue[] evs = aev.getElementValuesArray();
	  	for (int i = 0; i < evs.length; i++) {
			EnumElementValue ev = (EnumElementValue)evs[i];
			supportedTargets.add(ev.getEnumValueString());
		}
	  } else {
		  List values = theRealEclipseAnnotation.getNameValuePairs();
		  AnnotationNameValuePair nvp = (AnnotationNameValuePair)values.get(0);
		  ArrayAnnotationValue aav = (ArrayAnnotationValue)nvp.getValue();
		  AnnotationValue[] avs = aav.getValues();
		  for (int i = 0; i < avs.length; i++) {
			AnnotationValue value = avs[i];
			supportedTargets.add(value.stringify());
		  }
	  }
	  return supportedTargets;
  }

  /**
   * @return true if this annotation can be put on a field
   */
  public boolean allowedOnField() {
	ensureAtTargetInitialized();
	if (atTargetAnnotation == null) return true; // if no target specified, then return true
  	return supportedTargets.contains("FIELD");
  }

  public boolean isRuntimeVisible() {
	return theRealBcelAnnotation.isRuntimeVisible();
  }

  public void print(StringBuffer sb) {
	  if (mode==MODE_BCEL) sb.append(theRealBcelAnnotation.toString());
	  else	               sb.append(theRealEclipseAnnotation.stringify());
  }

  public boolean hasNameValuePair(String n, String v) {
	  if (mode==MODE_BCEL) return theRealBcelAnnotation.hasNameValuePair(n,v);
	  else	               return theRealEclipseAnnotation.hasNameValuePair(n,v);
  }

  public boolean hasNamedValue(String n) {
	  if (mode==MODE_BCEL) return theRealBcelAnnotation.hasNamedValue(n);
	  else	               return theRealEclipseAnnotation.hasNamedValue(n);
  }

}