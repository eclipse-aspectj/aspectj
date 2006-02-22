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


import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.AnnotationsForMemberHolder;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter.GenericSignatureFormatException;

public class AsmField extends ResolvedMemberImpl {

	private AsmDelegate classDelegate;
	private AnnotationsForMemberHolder annos;
	
	private String genericSignature = null;
    private boolean unpackedGenericSignature = false;
	private UnresolvedType genericFieldType = null;
	
	public void setClassDelegate(AsmDelegate del) { classDelegate = del;}
	public void setGenericSignature(String sig) { genericSignature = sig; }
	 
	public AsmField(Kind kind,	UnresolvedType declaringType,int modifiers,String name,String signature) {
		super(kind,declaringType,modifiers,name,signature);
	}
		
	public UnresolvedType getGenericReturnType() {
		unpackGenericSignature();
		return genericFieldType;
	}
	
	private void unpackGenericSignature() {
		if (unpackedGenericSignature) { return; }
		if (!classDelegate.getWorld().isInJava5Mode()) {
			this.genericFieldType = getReturnType();
			return; 
		}
		unpackedGenericSignature = true;
		String gSig = genericSignature;
		if (gSig != null) {
			// get from generic
			Signature.FieldTypeSignature fts = new GenericSignatureParser().parseAsFieldSignature(gSig);
//			Signature.ClassSignature genericTypeSig = classDelegate.getGenericClassTypeSignature();
//
//			Signature.ClassSignature cSig = parser.parseAsClassSignature(declaredSignature);
			
			Signature.FormalTypeParameter[] parentFormals = classDelegate.getAllFormals();
			Signature.FormalTypeParameter[] typeVars = 
				((parentFormals == null) ? new Signature.FormalTypeParameter[0] : parentFormals);//.formalTypeParameters);
			Signature.FormalTypeParameter[] formals = 
				new Signature.FormalTypeParameter[parentFormals.length + typeVars.length];
			// put method formal in front of type formals for overriding in
			// lookup
			System.arraycopy(typeVars, 0, formals, 0, typeVars.length);
			System.arraycopy(parentFormals, 0, formals, typeVars.length,parentFormals.length);

			try {
				genericFieldType = BcelGenericSignatureToTypeXConverter
						.fieldTypeSignature2TypeX(fts, formals, classDelegate.getWorld());
			} catch (GenericSignatureFormatException e) {
				// development bug, fail fast with good info
				throw new IllegalStateException(
						"While determing the generic field type of "
								+ this.toString() + " with generic signature "
								+ gSig + " the following error was detected: "
								+ e.getMessage());
			}
		} else {
			genericFieldType = getReturnType();
		}
	}
	
	
    //	 --- 
	// annotation manipulation
	public void addAnAnnotation(AnnotationAJ oneAnnotation) {
		if (annos==null) annos = new AnnotationsForMemberHolder(classDelegate.getWorld());
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
}
