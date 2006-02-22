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
package org.aspectj.weaver.asm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Signature.TypeVariableSignature;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.AnnotationsForMemberHolder;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.AdviceAttribute;
import org.aspectj.weaver.AjAttribute.EffectiveSignatureAttribute;
import org.aspectj.weaver.AjAttribute.MethodDeclarationLineNumberAttribute;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter;
import org.aspectj.weaver.bcel.Utility;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter.GenericSignatureFormatException;
import org.aspectj.org.objectweb.asm.Attribute;

public class AsmMethod extends ResolvedMemberImpl {

	// genericized version of return and parameter types
	private boolean unpackedGenericSignature = false;
	private String genericSignature = null;
	private UnresolvedType genericReturnType = null;
	private UnresolvedType[] genericParameterTypes = null;
	private boolean canBeParameterized = false;
	 
	private AnnotationsForMemberHolder annos;
	 
	private EffectiveSignatureAttribute esAttribute = null;
	private MethodDeclarationLineNumberAttribute mdlnAttribute = null;
	private ShadowMunger shadowMunger = null;
	private AsmDelegate classDelegate;
	public List /*Attribute*/ attributes       = Collections.EMPTY_LIST;
	private boolean unpackedAspectJAttributes = false;


	public AsmMethod(Kind kind,	UnresolvedType declaringType,int modifiers,String name,String signature) {
		super(kind,declaringType,modifiers,name,signature);
	}
	
	public void setClassDelegate(AsmDelegate del) { classDelegate = del;}
	public void setGenericSignature(String sig) { genericSignature = sig; }

	 public boolean canBeParameterized() {
		unpackGenericSignature();
		return canBeParameterized;
	 }

	 public UnresolvedType[] getGenericParameterTypes() {
		 unpackGenericSignature();
		 return genericParameterTypes;
	 }
	 
	 public UnresolvedType getGenericReturnType() {
		 unpackGenericSignature();
		 return genericReturnType;
	 }
	 

	public World getWorld() {
		return classDelegate.getWorld();
	}
		

	private void unpackGenericSignature() {
		 if (unpackedGenericSignature) return;
 		 if (!getWorld().isInJava5Mode()) { 
 			 this.genericReturnType = getReturnType();
 			 this.genericParameterTypes = getParameterTypes();
 			 return;
 		 }
 		 // ok, we have work to do...
		 unpackedGenericSignature = true;
		 String gSig = genericSignature;
		 if (gSig != null) {
			 Signature.MethodTypeSignature mSig = new GenericSignatureParser().parseAsMethodSignature(gSig);
 			 if (mSig.formalTypeParameters.length > 0) {
				// generic method declaration
				canBeParameterized = true;
			 }
 			 Signature.FormalTypeParameter[] parentFormals = classDelegate.getAllFormals();
 			 Signature.FormalTypeParameter[] formals = new
 			 	Signature.FormalTypeParameter[parentFormals.length + mSig.formalTypeParameters.length];
 			 // put method formal in front of type formals for overriding in lookup
 			 System.arraycopy(mSig.formalTypeParameters,0,formals,0,mSig.formalTypeParameters.length);
 			 System.arraycopy(parentFormals,0,formals,mSig.formalTypeParameters.length,parentFormals.length);
 			 
 			 typeVariables = new TypeVariable[mSig.formalTypeParameters.length];
 	    	  for (int i = 0; i < typeVariables.length; i++) {
 				Signature.FormalTypeParameter methodFtp = mSig.formalTypeParameters[i];
 				try {
 					typeVariables[i] = BcelGenericSignatureToTypeXConverter.formalTypeParameter2TypeVariable(
 							methodFtp, 
 							mSig.formalTypeParameters,
 							getWorld());
 				} catch (GenericSignatureFormatException e) {
 					// this is a development bug, so fail fast with good info
 					throw new IllegalStateException(
 							"While getting the type variables for method " + this.toString()
 							+ " with generic signature " + mSig + 
 							" the following error condition was detected: " + e.getMessage());
 				}
 			  }
 			 
 			 
 			 
 			 Signature.TypeSignature returnTypeSignature = mSig.returnType;
			 try {
				genericReturnType = BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(
						 returnTypeSignature, formals,
						 getWorld());
			} catch (GenericSignatureFormatException e) {
//				 development bug, fail fast with good info
				throw new IllegalStateException(
						"While determing the generic return type of " + this.toString()
						+ " with generic signature " + gSig + " the following error was detected: "
						+ e.getMessage());
			}
			 Signature.TypeSignature[] paramTypeSigs = mSig.parameters;
			 genericParameterTypes = new UnresolvedType[paramTypeSigs.length];
			 for (int i = 0; i < paramTypeSigs.length; i++) {
				try {
					genericParameterTypes[i] = 
						BcelGenericSignatureToTypeXConverter.typeSignature2TypeX(
								paramTypeSigs[i],formals,getWorld());
				} catch (GenericSignatureFormatException e) {
//					 development bug, fail fast with good info
					throw new IllegalStateException(
							"While determining the generic parameter types of " + this.toString()
							+ " with generic signature " + gSig + " the following error was detected: "
							+ e.getMessage());
				}
				if (paramTypeSigs[i] instanceof TypeVariableSignature) {
					canBeParameterized = true;
				}
			 }
		 } else {
			 genericReturnType = getReturnType();
			 genericParameterTypes = getParameterTypes();
		 }
	 }
	
	public EffectiveSignatureAttribute getEffectiveSignature() {
		unpackAspectJAttributes();
		return esAttribute;
	}
	
	public ShadowMunger getAssociatedShadowMunger() {
		unpackAspectJAttributes();
		return shadowMunger;
	}
	
	public int getDeclarationLineNumber() {
		unpackAspectJAttributes();
		if (mdlnAttribute==null) return -1;
		else return (mdlnAttribute.getLineNumber());
	}
	
	public boolean isAjSynthetic() {
		unpackAspectJAttributes();
		return super.isAjSynthetic();
	}
	
	private void unpackAspectJAttributes() {
		if (unpackedAspectJAttributes) return;
		List forRemoval = new ArrayList();
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Attribute element = (Attribute) iter.next();
			if (element instanceof AjASMAttribute) {
				if (element.type.equals(AjAttribute.AjSynthetic.AttributeName)) { 
					setAjSynthetic(true); 
					forRemoval.add(element); 
				}
				if (element.type.equals(AjAttribute.MethodDeclarationLineNumberAttribute.AttributeName)) { 
					mdlnAttribute = (MethodDeclarationLineNumberAttribute)((AjASMAttribute)element).unpack(classDelegate); 
					forRemoval.add(element); 
				}
				if (element.type.equals(AjAttribute.AdviceAttribute.AttributeName)) { 
					shadowMunger = ((AdviceAttribute)((AjASMAttribute)element).unpack(classDelegate)).reify(this,getWorld()); 
					forRemoval.add(element); 
				}
				if (element.type.equals(AjAttribute.EffectiveSignatureAttribute.AttributeName)) { 
					esAttribute = (EffectiveSignatureAttribute)((AjASMAttribute)element).unpack(classDelegate); 
					forRemoval.add(element); 
				}
			}
		}
		attributes.remove(forRemoval);
		unpackedAspectJAttributes = true;
	}

	// for testing - if we have this attribute, return it
	public AjAttribute[] getAttributes(String name) {
		List results = new ArrayList();
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Attribute element = (Attribute) iter.next();
			if (element.type.equals(name) && (element instanceof AjASMAttribute)) {
				results.add(((AjASMAttribute)element).unpack(this.classDelegate));
			}
		}
		return (AjAttribute[])results.toArray(new AjAttribute[]{});
	}
	
	// for testing - use with the method above
    public String[] getAttributeNames(boolean onlyIncludeAjOnes) {
		List strs = new ArrayList();
		int i = 0;
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Attribute element = (Attribute) iter.next();
			if (!onlyIncludeAjOnes || (element.type.startsWith(AjAttribute.AttributePrefix) && element instanceof AjASMAttribute))
			  strs.add(element.type);
		}
		return (String[])strs.toArray(new String[]{});
	}

	public String[] getParameterNames() {
		if (super.getParameterNames()==null) setParameterNames(Utility.makeArgNames(getArity()));
		return super.getParameterNames();
	}

	public ISourceContext getSourceContext() {
		return classDelegate.getSourceContext();
	}
	
	// --- 
	// annotation manipulation

	public void addAnAnnotation(AnnotationAJ oneAnnotation) {
		if (annos==null) annos = new AnnotationsForMemberHolder(getWorld());
		annos.addAnnotation(oneAnnotation);
	}

	public AnnotationX[] getAnnotations() {
		if (annos==null) return AnnotationX.NONE;
		return annos.getAnnotations();
	}

	public ResolvedType[] getAnnotationTypes() {
		if (annos==null) return ResolvedType.NONE;
		return annos.getAnnotationTypes();
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		if (annos==null) return false;
		return annos.hasAnnotation(ofType);
	}
	// ---
    
}
