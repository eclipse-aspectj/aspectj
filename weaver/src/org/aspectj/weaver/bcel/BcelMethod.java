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
import java.util.StringTokenizer;

import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Signature.TypeVariableSignature;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

final class BcelMethod extends ResolvedMember {

	private Method method;
	private boolean isAjSynthetic;
	private ShadowMunger associatedShadowMunger;
	private ResolvedPointcutDefinition preResolvedPointcut;  // used when ajc has pre-resolved the pointcut of some @Advice
	
    private ResolvedType[] annotationTypes = null;
    private AnnotationX[] annotations = null;
	
	private AjAttribute.EffectiveSignatureAttribute effectiveSignature;
	private AjAttribute.MethodDeclarationLineNumberAttribute declarationLineNumber;
	private ResolvedType[] resolvedAnnotations;
	private World world;
	private BcelObjectType bcelObjectType;

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
		this.bcelObjectType = declaringType;
		unpackAjAttributes(world);
		unpackJavaAttributes();
	}

	// ----

	private void unpackJavaAttributes() {
		ExceptionTable exnTable = method.getExceptionTable();
		checkedExceptions = (exnTable == null) 
			? UnresolvedType.NONE
			: UnresolvedType.forNames(exnTable.getExceptionNames());
			
		LocalVariableTable varTable = method.getLocalVariableTable();
		int len = getArity();
		if (varTable == null) {
			this.parameterNames = Utility.makeArgNames(len);
		} else {
			UnresolvedType[] paramTypes = getParameterTypes();
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
		associatedShadowMunger = null;
        List as = BcelAttributes.readAjAttributes(getDeclaringType().getClassName(),method.getAttributes(), getSourceContext(world),world.getMessageHandler());
		processAttributes(world, as);
		as = AtAjAttributes.readAj5MethodAttributes(method, this, world.resolve(getDeclaringType()), preResolvedPointcut,getSourceContext(world), world.getMessageHandler());
		processAttributes(world,as);
	}

	private void processAttributes(World world, List as) {
		for (Iterator iter = as.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			if (a instanceof AjAttribute.MethodDeclarationLineNumberAttribute) {
				declarationLineNumber = (AjAttribute.MethodDeclarationLineNumberAttribute)a;
			} else if (a instanceof AjAttribute.AdviceAttribute) {
				associatedShadowMunger = ((AjAttribute.AdviceAttribute)a).reify(this, world);
				// return;
			} else if (a instanceof AjAttribute.AjSynthetic) {
				isAjSynthetic = true;
			} else if (a instanceof AjAttribute.EffectiveSignatureAttribute) {
				//System.out.println("found effective: " + this);
				effectiveSignature = (AjAttribute.EffectiveSignatureAttribute)a;
			} else if (a instanceof AjAttribute.PointcutDeclarationAttribute) {
				// this is an @AspectJ annotated advice method, with pointcut pre-resolved by ajc
				preResolvedPointcut = ((AjAttribute.PointcutDeclarationAttribute)a).reify();
			} else {
				throw new BCException("weird method attribute " + a);
			}
		}
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

    public int getDeclarationOffset() {
        if (declarationLineNumber != null) {
            return declarationLineNumber.getOffset();
        } else {
            return -1;
        }
    }

    public ISourceLocation getSourceLocation() {
      ISourceLocation ret = super.getSourceLocation(); 
      if ((ret == null || ret.getLine()==0) && hasDeclarationLineNumberInfo()) {
        // lets see if we can do better
        ISourceContext isc = getSourceContext();
        if (isc !=null) ret = isc.makeSourceLocation(getDeclarationLineNumber(), getDeclarationOffset());
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
	
	public boolean hasAnnotation(UnresolvedType ofType) {
		ensureAnnotationTypesRetrieved();
		for (int i=0; i<annotationTypes.length; i++) {
			ResolvedType aType = annotationTypes[i];
			if (aType.equals(ofType)) return true;
		}
		return false;
	}
	
	public AnnotationX[] getAnnotations() {
		ensureAnnotationTypesRetrieved();
		return annotations;
	}
	
	 public ResolvedType[] getAnnotationTypes() {
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
		ResolvedType[] ret2 = new ResolvedType[len+1];
		System.arraycopy(annotationTypes,0,ret2,0,len);
		ret2[len] =world.resolve(UnresolvedType.forName(annotation.getTypeName()));
		annotationTypes = ret2;
		// FIXME asc looks like we are managing two 'bunches' of annotations, one
		// here and one in the real 'method' - should we reduce it to one layer?
		method.addAnnotation(annotation.getBcelAnnotation());
	 }
	 
	 private void ensureAnnotationTypesRetrieved() {
		if (annotationTypes == null || method.getAnnotations().length!=annotations.length) { // sometimes the list changes underneath us!
    		Annotation annos[] = method.getAnnotations();
    		annotationTypes = new ResolvedType[annos.length];
    		annotations = new AnnotationX[annos.length];
    		for (int i = 0; i < annos.length; i++) {
				Annotation annotation = annos[i];
				ResolvedType rtx = world.resolve(UnresolvedType.forName(annotation.getTypeName()));
				annotationTypes[i] = rtx;
				annotations[i] = new AnnotationX(annotation,world);
			}
    	}
	}
	 

	 private boolean canBeParameterized = false;
	 /**
	  * A method can be parameterized if it has one or more generic
	  * parameters. A generic parameter (type variable parameter) is
	  * identified by the prefix "T"
	  */
	 public boolean canBeParameterized() {
		 unpackGenericSignature();
		return canBeParameterized;
	}
	 
	 // genericized version of return and parameter types
	 private boolean unpackedGenericSignature = false;
	 private UnresolvedType genericReturnType = null;
	 private UnresolvedType[] genericParameterTypes = null;
	 
	 public UnresolvedType[] getGenericParameterTypes() {
		 unpackGenericSignature();
		 return genericParameterTypes;
	 }
	 
	 public UnresolvedType getGenericReturnType() {
		 unpackGenericSignature();
		 return genericReturnType;
	 }
	 
	 private void unpackGenericSignature() {
		 if (unpackedGenericSignature) return;
		 unpackedGenericSignature = true;
		 String gSig = method.getGenericSignature();
		 if (gSig != null) {
			 Signature.MethodTypeSignature mSig = new GenericSignatureParser().parseAsMethodSignature(method.getGenericSignature());
 			 if (mSig.formalTypeParameters.length > 0) {
				// generic method declaration
				canBeParameterized = true;
			 }
 			 Signature.ClassSignature genericTypeSig = bcelObjectType.getGenericClassTypeSignature();
 			 Signature.FormalTypeParameter[] formals = new
 			 	Signature.FormalTypeParameter[genericTypeSig.formalTypeParameters.length + mSig.formalTypeParameters.length];
 			 // put method formal in front of type formals for overriding in lookup
 			 System.arraycopy(mSig.formalTypeParameters,0,formals,0,mSig.formalTypeParameters.length);
 			 System.arraycopy(genericTypeSig.formalTypeParameters,0,formals,mSig.formalTypeParameters.length,genericTypeSig.formalTypeParameters.length);
 			 Signature.TypeSignature returnTypeSignature = mSig.returnType;
			 genericReturnType = BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(
					 returnTypeSignature, formals,
					 world);
			 Signature.TypeSignature[] paramTypeSigs = mSig.parameters;
			 genericParameterTypes = new UnresolvedType[paramTypeSigs.length];
			 for (int i = 0; i < paramTypeSigs.length; i++) {
				genericParameterTypes[i] = 
					BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(
							paramTypeSigs[i],formals,world);
				if (paramTypeSigs[i] instanceof TypeVariableSignature) {
					canBeParameterized = true;
				}
			 }
		 } else {
			 genericReturnType = getReturnType();
			 genericParameterTypes = getParameterTypes();
		 }
	 }
}
