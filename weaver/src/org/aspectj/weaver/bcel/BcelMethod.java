/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;

final class BcelMethod extends ResolvedMember {

	private Method method;
	private boolean isAjSynthetic;
	private ShadowMunger associatedShadowMunger;
	
    private ResolvedTypeX[] annotationTypes = null;
    private AnnotationX[] annotations = null;
	
	private AjAttribute.EffectiveSignatureAttribute effectiveSignature;
	private AjAttribute.MethodDeclarationLineNumberAttribute declarationLineNumber;
	private ResolvedTypeX[] resolvedAnnotations;
	private World world;

	BcelMethod(BcelObjectType declaringType, Method method) {
		super(
			method.getName().equals("<init>") ? CONSTRUCTOR : 
				(method.getName().equals("<clinit>") ? STATIC_INITIALIZATION : METHOD), 
			declaringType.getResolvedTypeX(),
			declaringType.isInterface() 
				? method.getAccessFlags() | Modifier.INTERFACE
				: method.getAccessFlags(),
			method.getName(), 
			method.getSignature());
		this.method = method;
		this.sourceContext = declaringType.getResolvedTypeX().getSourceContext();
		this.world = declaringType.getResolvedTypeX().getWorld();
		unpackAjAttributes(world);
		unpackJavaAttributes();
	}

	// ----

	private void unpackJavaAttributes() {
		ExceptionTable exnTable = method.getExceptionTable();
		checkedExceptions = (exnTable == null) 
			? TypeX.NONE
			: TypeX.forNames(exnTable.getExceptionNames());
			
		LocalVariableTable varTable = method.getLocalVariableTable();
		int len = getArity();
		if (varTable == null) {
			this.parameterNames = Utility.makeArgNames(len);
		} else {
			TypeX[] paramTypes = getParameterTypes();
			String[] paramNames = new String[len];
			int index = isStatic() ? 0 : 1;
			for (int i = 0; i < len; i++) {
				LocalVariable lv = varTable.getLocalVariable(index);
				if (lv == null) {
					paramNames[i] = "arg" + i;
				} else {
					paramNames[i] = lv.getName();
				}
				index += paramTypes[i].getSize();
			}
			this.parameterNames = paramNames;
		}
	}

	private void unpackAjAttributes(World world) {
		List as = BcelAttributes.readAjAttributes(getDeclaringType().getClassName(),method.getAttributes(), getSourceContext(world),world.getMessageHandler());
		//System.out.println("unpack: " + this + ", " + as);
		for (Iterator iter = as.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			if (a instanceof AjAttribute.MethodDeclarationLineNumberAttribute) {
				declarationLineNumber = (AjAttribute.MethodDeclarationLineNumberAttribute)a;
			} else if (a instanceof AjAttribute.AdviceAttribute) {
				associatedShadowMunger = ((AjAttribute.AdviceAttribute)a).reify(this, world);
				return;
			} else if (a instanceof AjAttribute.AjSynthetic) {
				isAjSynthetic = true;
			} else if (a instanceof AjAttribute.EffectiveSignatureAttribute) {
				//System.out.println("found effective: " + this);
				effectiveSignature = (AjAttribute.EffectiveSignatureAttribute)a;
			} else {
				throw new BCException("weird method attribute " + a);
			}
		}
		associatedShadowMunger = null;
	}

	public boolean isAjSynthetic() {
		return isAjSynthetic; // || getName().startsWith(NameMangler.PREFIX);
	}
	
	//FIXME ??? needs an isSynthetic method
	
	public ShadowMunger getAssociatedShadowMunger() {
		return associatedShadowMunger;
	}
	
	public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature() {
		return effectiveSignature;
	}
	
	public boolean hasDeclarationLineNumberInfo() {
		return declarationLineNumber != null;
	}
	
	public int getDeclarationLineNumber() {
		if (declarationLineNumber != null) {
			return declarationLineNumber.getLineNumber();
		} else {
			return -1;
		}
	}
    
    public ISourceLocation getSourceLocation() {
      ISourceLocation ret = super.getSourceLocation(); 
      if ((ret == null || ret.getLine()==0) && hasDeclarationLineNumberInfo()) {
        // lets see if we can do better
        ISourceContext isc = getSourceContext();
        if (isc !=null) ret = isc.makeSourceLocation(getDeclarationLineNumber());
        else            ret = new SourceLocation(null,getDeclarationLineNumber());
      }
      return ret;
    }
	
	public Kind getKind() {
		if (associatedShadowMunger != null) {
			return ADVICE;
		} else {
			return super.getKind();
		}
	}
	
	public boolean hasAnnotation(TypeX ofType) {
		ensureAnnotationTypesRetrieved();
		for (int i=0; i<annotationTypes.length; i++) {
			ResolvedTypeX aType = annotationTypes[i];
			if (aType.equals(ofType)) return true;
		}
		return false;
	}
	
	public AnnotationX[] getAnnotations() {
		ensureAnnotationTypesRetrieved();
		return annotations;
	}
	
	 public ResolvedTypeX[] getAnnotationTypes() {
	    ensureAnnotationTypesRetrieved();
	    return annotationTypes;
     }
	 
	 public void addAnnotation(AnnotationX annotation) {
	    ensureAnnotationTypesRetrieved();	
		// Add it to the set of annotations
		int len = annotations.length;
		AnnotationX[] ret = new AnnotationX[len+1];
		System.arraycopy(annotations, 0, ret, 0, len);
		ret[len] = annotation;
		annotations = ret;
		
		// Add it to the set of annotation types
		len = annotationTypes.length;
		ResolvedTypeX[] ret2 = new ResolvedTypeX[len+1];
		System.arraycopy(annotationTypes,0,ret2,0,len);
		ret2[len] =world.resolve(TypeX.forName(annotation.getTypeName()));
		annotationTypes = ret2;
		// FIXME asc looks like we are managing two 'bunches' of annotations, one
		// here and one in the real 'method' - should we reduce it to one layer?
		method.addAnnotation(annotation.getBcelAnnotation());
	 }
	 
	 private void ensureAnnotationTypesRetrieved() {
		if (annotationTypes == null) {
    		Annotation annos[] = method.getAnnotations();
    		annotationTypes = new ResolvedTypeX[annos.length];
    		annotations = new AnnotationX[annos.length];
    		for (int i = 0; i < annos.length; i++) {
				Annotation annotation = annos[i];
				ResolvedTypeX rtx = world.resolve(TypeX.forName(annotation.getTypeName()));
				annotationTypes[i] = rtx;
				annotations[i] = new AnnotationX(annotation,world);
			}
    	}
	}
}
