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

/**
 * Can store annotations for any member and ensures they are resolved/unpacked as lazily as possible.
 * The set of annotations is unpacked *once* then discarded, so ensure all annotations have been
 * added via 'addAnnotation()' before calling the public accessor
 * methods 'hasAnnotation/getAnnotations/getAnnotationTypes'
 * 
 * @author AndyClement
 */
public class AnnotationsForMemberHolder {

	private boolean annotationTypesCorrect = false; // guard for lazy initialization of annotationTypes
	private ResolvedType[] annotationTypes;
	private boolean annotationXsCorrect = false;
	private AnnotationX[] annotationXs;
	public List /*AnnotationAJ*/ annotations   = null;
	private World world;
	
	public AnnotationsForMemberHolder(World w) {
		this.world = w;
	}
	
	public AnnotationX[] getAnnotations() {
		ensureAnnotationXsUnpacked();
		return annotationXs;
	}

	public ResolvedType[] getAnnotationTypes() {
		ensureAnnotationsUnpacked();
		return annotationTypes;
	}
	
	public boolean hasAnnotation(UnresolvedType ofType) {
		ensureAnnotationsUnpacked();
		for (int i = 0; i < annotationTypes.length; i++) {
			if (annotationTypes[i].equals(ofType)) return true;
		}
		return false;
	}
	
	private void ensureAnnotationXsUnpacked() {
		if (annotationTypesCorrect && annotationXsCorrect) return;
		ensureAnnotationsUnpacked();
		if (annotations==null) {
			annotationXs = AnnotationX.NONE;
		} else {
			annotationXs = new AnnotationX[annotations.size()];
			int pos = 0;
			for (Iterator iter = annotations.iterator(); iter.hasNext();) {
				AnnotationAJ element = (AnnotationAJ) iter.next();
				annotationXs[pos++] = new AnnotationX(element,world);
			}
			annotations=null; // finished with
		}
		annotationXsCorrect = true;
	}
	
	
  private void ensureAnnotationsUnpacked() {
	if (annotationTypesCorrect) return;
	if (annotations==null) {
		annotationTypes = ResolvedType.NONE;
	} else {
		annotationTypes = new ResolvedType[annotations.size()];
		int pos = 0;
		for (Iterator iter = annotations.iterator(); iter.hasNext();) {
			AnnotationAJ element = (AnnotationAJ) iter.next();
			annotationTypes[pos++] = world.resolve(UnresolvedType.forSignature(element.getTypeSignature()));
		}
	}
	annotationTypesCorrect = true;
  }

	public void addAnnotation(AnnotationAJ oneAnnotation) {
		if (annotations==null) annotations = new ArrayList();
		annotations.add(oneAnnotation);
	}

}
