/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;

/**
 * This type represents the weavers abstraction of an annotation - it is
 * not tied to any underlying BCI toolkit.  The weaver actualy handles these
 * through AnnotationX wrapper objects - until we start transforming the
 * BCEL annotations into this form (expensive) or offer a clever
 * visitor mechanism over the BCEL annotation stuff that builds these
 * annotation types directly.
 *
 * @author AndyClement
 */
public class AnnotationAJ {

	private String type;
	private boolean isRuntimeVisible;
	
	private List /*of AnnotationNVPair*/ nvPairs = null;
	
	public AnnotationAJ(String type,boolean isRuntimeVisible) {
		this.type = type;
		this.isRuntimeVisible = isRuntimeVisible;
	}
	
	public String getTypeSignature() {
		return type;
	}
	
	public List getNameValuePairs() {
		return nvPairs;
	}
	
	public boolean hasNameValuePairs() {
		return nvPairs!=null && nvPairs.size()!=0;
	}
	
	public boolean isRuntimeVisible() {
		return isRuntimeVisible;
	}
	
	public String stringify() {
		StringBuffer sb = new StringBuffer();
		sb.append("@").append(UnresolvedType.forSignature(type).getClassName());
		if (hasNameValuePairs()) {
			sb.append("(");
			for (Iterator iter = nvPairs.iterator(); iter.hasNext();) {
				AnnotationNameValuePair element = (AnnotationNameValuePair) iter.next();
				sb.append(element.stringify());
			}
			sb.append(")");
		}
		return sb.toString();
	}

	public String getStringValueOf(Object name) {
		if (!hasNameValuePairs()) return null;
		for (Iterator iter = nvPairs.iterator(); iter.hasNext();) {
			AnnotationNameValuePair nvpair = (AnnotationNameValuePair) iter.next();
			if (nvpair.getName().equals(name)) return nvpair.getValue().stringify();
		}
		return null;
	}

	public void addNameValuePair(AnnotationNameValuePair pair) {
		if (nvPairs==null) nvPairs=new ArrayList();
		nvPairs.add(pair);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ANNOTATION ["+getTypeSignature()+"] ["+
				(isRuntimeVisible?"runtimeVisible":"runtimeInvisible")+"] [");
		if (nvPairs!=null) { 
			for (Iterator iter = nvPairs.iterator(); iter.hasNext();) {
				AnnotationNameValuePair element = (AnnotationNameValuePair) iter.next();
				sb.append(element.toString());
				if (iter.hasNext()) sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public boolean hasNamedValue(String n) {
		if (nvPairs==null) return false;
		for (int i=0;i<nvPairs.size();i++) {
			AnnotationNameValuePair pair = (AnnotationNameValuePair)nvPairs.get(i);
			if (pair.getName().equals(n)) return true;
		}
		return false;
	}

	/**
     * Return true if the annotation has a value with the specified name (n) and value (v)
     */
	public boolean hasNameValuePair(String n, String v) {
		if (nvPairs==null) return false;
		for (int i=0;i<nvPairs.size();i++) {
			AnnotationNameValuePair pair = (AnnotationNameValuePair)nvPairs.get(i);
			if (pair.getName().equals(n)) {
				if (pair.getValue().stringify().equals(v)) return true;
			}
		}
		return false;
	}
}
