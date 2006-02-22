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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationNameValuePair;
import org.aspectj.weaver.AnnotationTargetKind;
import org.aspectj.weaver.AnnotationValue;
import org.aspectj.weaver.ArrayAnnotationValue;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.AjAttribute.Aspect;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.org.objectweb.asm.AnnotationVisitor;
import org.aspectj.org.objectweb.asm.Attribute;
import org.aspectj.org.objectweb.asm.ClassVisitor;
import org.aspectj.org.objectweb.asm.FieldVisitor;
import org.aspectj.org.objectweb.asm.MethodVisitor;


public class TypeVisitor implements ClassVisitor {

	private final AsmDelegate relatedDelegate;
	
	// Populated as we go along, then dealt with at the end of the visit - setting
	// state on the relatedDelegate as appropriate
	private List methodsList = new ArrayList();
	private List fieldsList  = new ArrayList();
	private List attributes  = new ArrayList();
    protected List annotations = new ArrayList();
	
    private String name = null;
    
	TypeVisitor(AsmDelegate delegate) {
		relatedDelegate = delegate;
	}
	
	public void visit(int version, int mods, String name, String signature, String superclassname, String[] interfacenames) {
		relatedDelegate.superclassName = superclassname;
		relatedDelegate.interfaceNames = interfacenames;
		relatedDelegate.classModifiers = mods;
		relatedDelegate.declaredSignature  = signature; // <E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Collection<TE;>;
		relatedDelegate.isGenericType  = (signature!=null && signature.charAt(0)=='<');
//		relatedDelegate.erasureSignature=name;      // java/util/List;
		this.name = name;
	}

	public AnnotationVisitor visitAnnotation(String typeSignature, boolean vis) {
		AnnotationAJ annotation = new AnnotationAJ(typeSignature,vis);
		annotations.add(annotation); 
		return new AnnVisitor(annotation);
	}

	/**
	 * Store up the attributes - the only one to look out for is the version one, 
	 * as it will tell us the format of all the others.
	 */
	public void visitAttribute(Attribute attr) {
//		System.err.println("encountered attribute "+attr.type);
		if (attr.type.equals(WeaverVersionInfo.AttributeName)) {
			WeaverVersionInfo wv = (WeaverVersionInfo)((AjASMAttribute)attr).unpack(relatedDelegate);
			relatedDelegate.weaverVersion = wv;
		} else {
			attributes.add(attr);
		}
	}

	public void visitInnerClass(String name, String outer, String inner, int access) {
		if (name.equals(this.name)) {
			relatedDelegate.isNested=true;
			relatedDelegate.isAnonymous = (inner==null);
		}
	}

	public FieldVisitor visitField(int modifiers, String name, String descType, String signature, Object value) {
		AsmField rm = new AsmField(ResolvedMember.FIELD,this.relatedDelegate.getResolvedTypeX(),modifiers,name,descType);
		rm.setGenericSignature(signature);
		rm.setClassDelegate(relatedDelegate);
		fieldsList.add(rm);
		if (AsmDelegate.careAboutMemberAnnotationsAndAttributes) {
			return new FdVisitor(rm);
		} else {
			return null;
		}
	}

	public MethodVisitor visitMethod(int modifiers, String name, String desc, String signature, String[] exceptions) {
		if (Modifier.isInterface(relatedDelegate.getModifiers()) && Modifier.isAbstract(modifiers))  // wtf? (see testIterator in WorldTestCase)
				modifiers = modifiers | Modifier.INTERFACE;
		AsmMethod rm = new AsmMethod(ResolvedMember.METHOD,this.relatedDelegate.getResolvedTypeX(),modifiers,name,desc);
		rm.setGenericSignature(signature);
		rm.setClassDelegate(relatedDelegate);
		if (exceptions!=null && exceptions.length!=0) {
			UnresolvedType[] excs = new UnresolvedType[exceptions.length];
			for (int i = 0; i < exceptions.length; i++) {
				excs[i]=UnresolvedType.forSignature("L"+exceptions[i]+";");
			}
			rm.setCheckedExceptions(excs);
		}
		methodsList.add(rm);
		if (AsmDelegate.careAboutMemberAnnotationsAndAttributes) {
			return new MethVisitor(rm);
		} else {
			return null;
		}
	}

	public void visitSource(String sourcefilename, String debug) {
		relatedDelegate.setSourcefilename(sourcefilename); 
    }

	public void visitOuterClass(String arg0, String arg1, String arg2) {/*DeliberatelyBlank*/}

	// --- 

	/**
	 * Pick through what we learned whilst visiting the type - and set what we need to on the 
	 * related delegate.  Discard what we can now.
	 */
	public void visitEnd() {
		relatedDelegate.methods = (ResolvedMember[])methodsList.toArray(new ResolvedMember[]{});
		methodsList.clear();
		relatedDelegate.fields  = (ResolvedMember[])fieldsList.toArray(new ResolvedMember[]{});
		fieldsList.clear();
		// Fast pass of the attributes, unpacking lightweight ones.  Fast ones are:
		// Aspect
		if (attributes.size()>0) {
			relatedDelegate.attributes = new ArrayList();
			for (Iterator iter = attributes.iterator(); iter.hasNext();) {
				Attribute element = (Attribute) iter.next();
				//System.err.println("Processing:"+element);
				if (element instanceof AjASMAttribute) {
					// Aspect
					if (element.type.equals(Aspect.AttributeName)) {
						Aspect aspectAttribute = (Aspect)((AjASMAttribute)element).unpack(relatedDelegate);
						relatedDelegate.perClause = aspectAttribute.reify(null); // param not used
						relatedDelegate.isAspect  = true;
						continue;
					}
				}
			    relatedDelegate.attributes.add(element);
			}
		}
		
		// Similar thing for annotations, unpacking lightweight/common ones.  These are:
		// Retention
		// For annotations, retention policy should default to CLASS if not set.
		boolean retentionPolicySet = false;
		if (annotations.size()>0) relatedDelegate.annotations = new ArrayList();
		for (Iterator iter = annotations.iterator(); iter.hasNext();) {
			AnnotationAJ element = (AnnotationAJ) iter.next();
			
			// Retention
			if (element.getTypeSignature().equals(AnnotationRetention)) {
				relatedDelegate.retentionPolicy = element.getStringValueOf("value");
				relatedDelegate.isRuntimeRetention = relatedDelegate.retentionPolicy.equals("RUNTIME");
				retentionPolicySet=true;
//				continue; // possible optimization - dont store them, we've pulled out all the relevant stuff
			}
			
			// Target
			if (element.getTypeSignature().equals(AnnotationTarget)) {
				setDelegateFieldsForAnnotationTarget(element.getNameValuePairs());
//				continue; // possible optimization - dont store them, we've pulled out all the relevant stuff
			}
			
		    relatedDelegate.annotations.add(element);
		}
		if (relatedDelegate.isAnnotation() && !retentionPolicySet) { relatedDelegate.retentionPolicy="CLASS"; relatedDelegate.isRuntimeRetention=false;}
	}
	
	private void setDelegateFieldsForAnnotationTarget(List/*AnnotationNameValuePair*/ nvpairs) {
		// Should only be one nvpair, thats the value
		if (nvpairs.size()>0) {
			boolean canTargetType = false;
			ArrayAnnotationValue targetsArray = (ArrayAnnotationValue)((AnnotationNameValuePair)nvpairs.get(0)).getValue();
			AnnotationValue[] targets = targetsArray.getValues();
			List targetKinds = new ArrayList();
			for (int i = 0; i < targets.length; i++) {
				String targetKind = targets[i].stringify();
				if (targetKind.equals("ANNOTATION_TYPE")) {
					targetKinds.add(AnnotationTargetKind.ANNOTATION_TYPE);
                } else if (targetKind.equals("CONSTRUCTOR")) {
					targetKinds.add(AnnotationTargetKind.CONSTRUCTOR);
				} else if (targetKind.equals("FIELD")) {
					targetKinds.add(AnnotationTargetKind.FIELD);
				} else if (targetKind.equals("LOCAL_VARIABLE")) {
					targetKinds.add(AnnotationTargetKind.LOCAL_VARIABLE);
				} else if (targetKind.equals("METHOD")) {
					targetKinds.add(AnnotationTargetKind.METHOD);
				} else if (targetKind.equals("PACKAGE")) {
					targetKinds.add(AnnotationTargetKind.PACKAGE);
				} else if (targetKind.equals("PARAMETER")) {
					targetKinds.add(AnnotationTargetKind.PARAMETER);
				} else if (targetKind.equals("TYPE")) {
					targetKinds.add(AnnotationTargetKind.TYPE); canTargetType = true;
				} 
			}
			relatedDelegate.targetKinds = (AnnotationTargetKind[])targetKinds.toArray(new AnnotationTargetKind[]{});
			relatedDelegate.canAnnotationTargetType = canTargetType;
		}
	}
	
	// ---
	private static final String AnnotationRetention = "Ljava/lang/annotation/Retention;";
	private static final String AnnotationTarget = "Ljava/lang/annotation/Target;";
	
}