/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - June 2005 - separated out from ResolvedType
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Signature.ClassSignature;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.patterns.Declare;

public abstract class AbstractReferenceTypeDelegate implements ReferenceTypeDelegate {
	
	public final static String UNKNOWN_SOURCE_FILE = "<Unknown>"; // Just randomly picked, happens to match BCEL javaclass default
	
	private String sourcefilename = UNKNOWN_SOURCE_FILE; // Sourcefilename is stored only here
	protected boolean exposedToWeaver;
	protected ReferenceType resolvedTypeX;
	private ISourceContext sourceContext = SourceContextImpl.UNKNOWN_SOURCE_CONTEXT;
	protected ClassSignature cachedGenericClassTypeSignature;

    public AbstractReferenceTypeDelegate(ReferenceType resolvedTypeX, boolean exposedToWeaver) {
        this.resolvedTypeX = resolvedTypeX;
        this.exposedToWeaver = exposedToWeaver;
    }
        
	public final boolean isClass() {
    	return !isAspect() && !isInterface();
    }
	
	

	/**
	 * Designed to be overriden by EclipseType to disable collection of shadow mungers
	 * during pre-weave compilation phase
	 */
	public boolean doesNotExposeShadowMungers() {
		return false;
	}

	public boolean isExposedToWeaver() {
		return exposedToWeaver;
	}

	public ReferenceType getResolvedTypeX() {
		return resolvedTypeX;
	}
	
    /**
     * Create the string representation for a delegate, allowing us to 
     * more easily compare delegate implementations.
     */
    public String stringifyDelegate() {
    	
    	StringBuffer result = new StringBuffer();
    	result.append("=== Delegate for "+getResolvedTypeX().getName()+"\n");
    	
    	result.append("isAspect?"+isAspect()+"\n");
    	result.append("isAnnotationStyleAspect?"+isAnnotationStyleAspect()+"\n");
    	result.append("isInterface?"+isInterface()+"\n");
    	result.append("isEnum?"+isEnum()+"\n");
    	result.append("isClass?"+isClass()+"\n");
    	result.append("-\n");
    	result.append("isAnnotation?"+isAnnotation()+"\n");
    	result.append("retentionPolicy="+getRetentionPolicy()+"\n");
    	result.append("canAnnotationTargetType?"+canAnnotationTargetType()+"\n");
    	AnnotationTargetKind[] kinds = getAnnotationTargetKinds();
    	if (kinds!=null && kinds.length>0) {
    		result.append("annotationTargetKinds:[");
    		for (int i = 0; i < kinds.length; i++) {
				AnnotationTargetKind kind = kinds[i];
				result.append(kind);
				if ((i+1)<kinds.length) result.append(" ");
			}
    		result.append("]\n");
    	}
    	result.append("isAnnotationWithRuntimeRetention?"+isAnnotationWithRuntimeRetention()+"\n");
    	result.append("-\n");
    	
    	result.append("isAnonymous?"+isAnonymous()+"\n");
    	result.append("isNested?"+isNested()+"\n");
    	result.append("-\n");

    	result.append("isGeneric?"+isGeneric()+"\n");
    	result.append("declaredGenericSignature="+getDeclaredGenericSignature()+"\n");
    	result.append("-\n");
    	
    	AnnotationX[] axs = getAnnotations();
    	if (axs!=null && axs.length>0) {
    		result.append("getAnnotations() returns: "+axs.length+" annotations\n");
    		for (int i = 0; i < axs.length; i++) {
				AnnotationX annotationX = axs[i];
				result.append("  #"+i+") "+annotationX+"\n");
    		}
		} else {
			result.append("getAnnotations() returns nothing\n");
		}
    	ResolvedType[] axtypes = getAnnotationTypes();
    	if (axtypes!=null && axtypes.length>0) {
    		result.append("getAnnotationTypes() returns: "+axtypes.length+" annotations\n");
    		for (int i = 0; i < axtypes.length; i++) {
				ResolvedType annotation = axtypes[i];
				result.append("  #"+i+") "+annotation+":"+annotation.getClass()+"\n");
			}
    	} else {
			result.append("getAnnotationTypes() returns nothing\n");
		}
    	
    	result.append("isExposedToWeaver?"+isExposedToWeaver()+"\n");
    	result.append("getSuperclass?"+getSuperclass()+"\n");
    	result.append("getResolvedTypeX?"+getResolvedTypeX()+"\n");
    	result.append("--\n");
    	
    	ResolvedMember[] fields = getDeclaredFields();
    	if (fields!=null && fields.length>0) {
        	result.append("The fields: "+fields.length+"\n");
    		for (int i = 0; i < fields.length; i++) {
				ResolvedMember member = fields[i];
				result.append("f"+i+") "+member.toDebugString()+"\n");
			}
    	}
    	ResolvedMember[] methods = getDeclaredMethods();
    	if (methods!=null && methods.length>0) {
        	result.append("The methods: "+methods.length+"\n");
    		for (int i = 0; i < methods.length; i++) {
				ResolvedMember member = methods[i];
				result.append("m"+i+") "+member.toDebugString()+"\n");
			}
    	}
    	ResolvedType[] interfaces = getDeclaredInterfaces();
    	if (interfaces!=null && interfaces.length>0) {
        	result.append("The interfaces: "+interfaces.length+"\n");
    		for (int i = 0; i < interfaces.length; i++) {
    			ResolvedType member = interfaces[i];
				result.append("i"+i+") "+member+"\n");
			}
    	}

    	result.append("getModifiers?"+getModifiers()+"\n");
    	
    	result.append("perclause="+getPerClause()+"\n");
    	
    	result.append("aj:weaverstate="+getWeaverState()+"\n");
    	
    	ResolvedMember[] pointcuts = getDeclaredPointcuts();
    	if (pointcuts!=null && pointcuts.length>0) {
    		result.append("The pointcuts: "+pointcuts.length+"\n");
    		
    		// Sort the damn things
        	List sortedSetOfPointcuts = new ArrayList();
        	for (int i = 0; i < pointcuts.length; i++) {sortedSetOfPointcuts.add(pointcuts[i]);}
        	Collections.sort(sortedSetOfPointcuts);
        	
        	int i =0;
        	for (Iterator iter = sortedSetOfPointcuts.iterator(); iter.hasNext();) {
				ResolvedMember member = (ResolvedMember) iter.next();
				result.append("p"+i+") "+member.toDebugString()+"\n");
				i++;
			}
    	}
    	
    	Collection declares = getDeclares();
    	if (declares.size()>0) {
    		result.append("The declares: "+declares.size()+"\n");
    		
//    		// Sort the damn things
//        	List sortedSetOfPointcuts = new ArrayList();
//        	for (int i = 0; i < pointcuts.length; i++) {sortedSetOfPointcuts.add(pointcuts[i]);}
//        	Collections.sort(sortedSetOfPointcuts);
        	
        	int i=0;
        	for (Iterator iter = declares.iterator(); iter.hasNext();) {
				Declare dec = (Declare) iter.next();
				result.append("d"+i+") "+dec.toString()+"\n");
				i++;
			}
    	}
    	
    	TypeVariable[] tv = getTypeVariables();
    	if (tv!=null && tv.length>0) {
        	result.append("The type variables: "+tv.length+"\n");
    		for (int i = 0; i < tv.length; i++) {
				result.append("tv"+i+") "+tv[i]+"\n");
			}
    	}
    	
    	Collection tmungers = getTypeMungers();
    	if (tmungers.size()>0) {
    		List sorted = new ArrayList();
    		sorted.addAll(tmungers);
    		Collections.sort(sorted,new Comparator() {
    			public int compare(Object arg0, Object arg1) {
    				 return arg0.toString().compareTo(arg1.toString());
    			}
    		});
    		result.append("The type mungers: "+tmungers.size()+"\n");
    		int i=0;
        	for (Iterator iter = sorted.iterator(); iter.hasNext();) {
				ConcreteTypeMunger mun = (ConcreteTypeMunger) iter.next();
				result.append("tm"+i+") "+mun.toString()+"\n");
				i++;
			}
    	}

    	result.append("doesNotExposeShadowMungers?"+doesNotExposeShadowMungers()+"\n");
    	
    	Collection pas = getPrivilegedAccesses();
    	if (pas!=null && pas.size()>0) {
//    		List sorted = new ArrayList();
//    		sorted.addAll(tmungers);
//    		Collections.sort(sorted,new Comparator() {
//    			public int compare(Object arg0, Object arg1) {
//    				 return arg0.toString().compareTo(arg1.toString());
//    			}
//    		});
    		result.append("The privileged accesses: "+pas.size()+"\n");
    		int i=0;
        	for (Iterator iter = pas.iterator(); iter.hasNext();) {
				ResolvedMember mun = (ResolvedMember) iter.next();
				result.append("tm"+i+") "+mun.toDebugString()+"\n");
				i++;
			}
    	}

//    	public Collection getPrivilegedAccesses();
//      public boolean hasAnnotation(UnresolvedType ofType);	
    	result.append("===");
    	return result.toString();
    }
    
    public final String getSourcefilename() {
    	return sourcefilename;
    }
    
    public final void setSourcefilename(String sourceFileName) {
		this.sourcefilename = sourceFileName;
		if (sourceFileName!=null && sourceFileName.equals(AbstractReferenceTypeDelegate.UNKNOWN_SOURCE_FILE)) {
			this.sourcefilename = "Type '"+ getResolvedTypeX().getName()+"' (no debug info available)";
		} else {
			String pname = getResolvedTypeX().getPackageName();
			if (pname != null) {
				this.sourcefilename = pname.replace('.', '/') + '/' + sourceFileName;
			}
		}
    }
    
	public ISourceLocation getSourceLocation() {
		return getSourceContext().makeSourceLocation(0, 0); 
	}
	
	public ISourceContext getSourceContext() {
		return sourceContext;
	}
	
	public void setSourceContext(ISourceContext isc) {
		this.sourceContext = isc;
	}

	public Signature.ClassSignature getGenericClassTypeSignature() {
		if (cachedGenericClassTypeSignature==null) {
			String sig = getDeclaredGenericSignature();
			if (sig!=null) {
				GenericSignatureParser parser = new GenericSignatureParser();
				cachedGenericClassTypeSignature = parser.parseAsClassSignature(sig);
			}
		}
		return cachedGenericClassTypeSignature;
	}
	
	protected Signature.FormalTypeParameter[] getFormalTypeParametersFromOuterClass() {
		List typeParameters = new ArrayList();
		ReferenceType outer = (ReferenceType)getOuterClass();
		ReferenceTypeDelegate outerDelegate = outer.getDelegate();
		AbstractReferenceTypeDelegate outerObjectType = (AbstractReferenceTypeDelegate) outerDelegate;
		if (outerObjectType.isNested()) {
			Signature.FormalTypeParameter[] parentParams = outerObjectType.getFormalTypeParametersFromOuterClass();
			for (int i = 0; i < parentParams.length; i++) {
				typeParameters.add(parentParams[i]);
			}
		}
		Signature.ClassSignature outerSig = outerObjectType.getGenericClassTypeSignature();
		if (outerSig != null) {
			for (int i = 0; i < outerSig.formalTypeParameters .length; i++) {
				typeParameters.add(outerSig.formalTypeParameters[i]);
			}
		} 
		
		Signature.FormalTypeParameter[] ret = new Signature.FormalTypeParameter[typeParameters.size()];
		typeParameters.toArray(ret);
		return ret;
	}


}