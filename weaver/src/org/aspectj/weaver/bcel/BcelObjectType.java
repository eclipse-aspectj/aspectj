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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AbstractReferenceTypeDelegate;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.AnnotationTargetKind;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXConverter.GenericSignatureFormatException;
import org.aspectj.weaver.patterns.PerClause;



// ??? exposed for testing
public class BcelObjectType extends AbstractReferenceTypeDelegate {
    private JavaClass javaClass;
    private boolean isObject = false;  // set upon construction
	private LazyClassGen lazyClassGen = null;  // set lazily if it's an aspect

	// lazy, for no particular reason I can discern
    private ResolvedType[] interfaces = null;
    private ResolvedType superClass = null;
    private ResolvedMember[] fields = null;
    private ResolvedMember[] methods = null;
    private ResolvedType[] annotationTypes = null;
    private AnnotationX[] annotations = null;
    private TypeVariable[] typeVars = null;

    // track unpackAttribute. In some case (per clause inheritance) we encounter
    // unpacked state when calling getPerClause
    // this whole thing would require more clean up (AV)
    private boolean isUnpacked = false;

    // strangely non-lazy
    private ResolvedPointcutDefinition[] pointcuts = null;
	private PerClause perClause = null;
	private WeaverStateInfo weaverState = null;
	private AjAttribute.WeaverVersionInfo wvInfo = AjAttribute.WeaverVersionInfo.UNKNOWN;
	private List typeMungers = Collections.EMPTY_LIST;
	private List declares = Collections.EMPTY_LIST;
	private ResolvedMember[] privilegedAccess = null;

	private boolean discoveredWhetherAnnotationStyle = false;
    private boolean isAnnotationStyleAspect = false;// set upon construction
	private boolean isCodeStyleAspect = false; // not redundant with field above!

//  TODO asc need soon but not just yet...
	private boolean haveLookedForDeclaredSignature = false;
	private String declaredSignature = null;
	private boolean isGenericType = false;
	
	private boolean discoveredRetentionPolicy = false;
	private String retentionPolicy;
	private boolean discoveredAnnotationTargetKinds = false;
	private AnnotationTargetKind[] annotationTargetKinds;
	
	
	/**
	 * A BcelObjectType is 'damaged' if it has been modified from what was original constructed from
	 * the bytecode.  This currently happens if the parents are modified or an annotation is added -
	 * ideally BcelObjectType should be immutable but that's a bigger piece of work!!!!!!!!!! XXX
	 */
	private boolean damaged = false;

	public Collection getTypeMungers() {
		return typeMungers;	
	}
	

	public Collection getDeclares() {
		return declares;
	}
	
	public Collection getPrivilegedAccesses() {
		if (privilegedAccess == null) return Collections.EMPTY_LIST;
		return Arrays.asList(privilegedAccess);
	}
	
    
    // IMPORTANT! THIS DOESN'T do real work on the java class, just stores it away.
    BcelObjectType(ReferenceType resolvedTypeX, JavaClass javaClass, boolean exposedToWeaver) {
        super(resolvedTypeX, exposedToWeaver);
        this.javaClass = javaClass;

        //ATAJ: set the delegate right now for @AJ poincut, else it is done too late to lookup
        // @AJ pc refs annotation in class hierarchy
        resolvedTypeX.setDelegate(this);

        if (resolvedTypeX.getSourceContext() == null) {
        	resolvedTypeX.setSourceContext(new BcelSourceContext(this));
        }
        
        // this should only ever be java.lang.Object which is 
        // the only class in Java-1.4 with no superclasses
        isObject = (javaClass.getSuperclassNameIndex() == 0);
        unpackAspectAttributes();
    }
    
    
    // repeat initialization
    public void setJavaClass(JavaClass newclass) {
    	this.javaClass = newclass;
    	resetState();
    }
    
   
    
    
    public TypeVariable[] getTypeVariables() {
    	if (!isGeneric()) return new TypeVariable[0];
    	
    	if (typeVars == null) {
	    	Signature.ClassSignature classSig = javaClass.getGenericClassTypeSignature();
	    	typeVars = new TypeVariable[classSig.formalTypeParameters.length];
	    	for (int i = 0; i < typeVars.length; i++) {
				Signature.FormalTypeParameter ftp = classSig.formalTypeParameters[i];
				try {
					typeVars[i] = BcelGenericSignatureToTypeXConverter.formalTypeParameter2TypeVariable(
							ftp, 
							classSig.formalTypeParameters,
							getResolvedTypeX().getWorld());
				} catch (GenericSignatureFormatException e) {
					// this is a development bug, so fail fast with good info
					throw new IllegalStateException(
							"While getting the type variables for type " + this.toString()
							+ " with generic signature " + classSig + 
							" the following error condition was detected: " + e.getMessage());
				}
			}
    	}
    	return typeVars;
    }
    
    public int getModifiers() {
        return javaClass.getAccessFlags();
    }

    /**
     * Must take into account generic signature
     */
    public ResolvedType getSuperclass() {
        if (isObject) return null;
    	unpackGenericSignature();
        if (superClass == null) {
            superClass = getResolvedTypeX().getWorld().resolve(UnresolvedType.forName(javaClass.getSuperclassName()));
        }
        return superClass;
    }
        
    /**
     * Retrieves the declared interfaces - this allows for the generic signature on a type.  If specified
     * then the generic signature is used to work out the types - this gets around the results of
     * erasure when the class was originally compiled.
     */
    public ResolvedType[] getDeclaredInterfaces() {
    	unpackGenericSignature();
        if (interfaces == null) {
            String[] ifaceNames = javaClass.getInterfaceNames();
            interfaces = new ResolvedType[ifaceNames.length];
            for (int i = 0, len = ifaceNames.length; i < len; i++) {
                interfaces[i] = getResolvedTypeX().getWorld().resolve(UnresolvedType.forName(ifaceNames[i]));
            }
        }
        return interfaces;
    }
    
    public ResolvedMember[] getDeclaredMethods() {
    	unpackGenericSignature();
        if (methods == null) {
	        Method[] ms = javaClass.getMethods();
			ResolvedMember[] ret = new ResolvedMember[ms.length];
			for (int i = ms.length - 1; i >= 0; i--) {
				ret[i] = new BcelMethod(this, ms[i]);
			}
			methods = ret;
        }
        return methods;
    }			
    
    public ResolvedMember[] getDeclaredFields() {
    	unpackGenericSignature();
        if (fields == null) {
            Field[] fs = javaClass.getFields();
            ResolvedMember[] ret = new ResolvedMember[fs.length];
            for (int i = 0, len = fs.length; i < len; i++) {
                ret[i] = new BcelField(this, fs[i]);
            }
            fields = ret;
        }
        return fields;
    }

	// ----
	// fun based on the aj attributes 

    public ResolvedMember[] getDeclaredPointcuts() {
        return pointcuts;
    }           

	//??? method only used for testing
	public void addPointcutDefinition(ResolvedPointcutDefinition d) {
		damaged = true;
		int len = pointcuts.length;
		ResolvedPointcutDefinition[] ret = new ResolvedPointcutDefinition[len+1];
		System.arraycopy(pointcuts, 0, ret, 0, len);
		ret[len] = d;
		pointcuts = ret;
	}

    public boolean isAspect() {
		return perClause != null;
    }

    /**
     * Check if the type is an @AJ aspect (no matter if used from an LTW point of view).
     * Such aspects are annotated with @Aspect
     *
     * @return true for @AJ aspect
     */
    public boolean isAnnotationStyleAspect() {
		if (!discoveredWhetherAnnotationStyle) {
			discoveredWhetherAnnotationStyle = true;
			isAnnotationStyleAspect = !isCodeStyleAspect && hasAnnotation(AjcMemberMaker.ASPECT_ANNOTATION);
		}
        return isAnnotationStyleAspect;
    }

	private void unpackAspectAttributes() {
        isUnpacked = true;

		List pointcuts = new ArrayList();
		typeMungers = new ArrayList();
		declares = new ArrayList();
		// Pass in empty list that can store things for readAj5 to process
        List l = BcelAttributes.readAjAttributes(javaClass.getClassName(),javaClass.getAttributes(), getResolvedTypeX().getSourceContext(),getResolvedTypeX().getWorld().getMessageHandler(),AjAttribute.WeaverVersionInfo.UNKNOWN);
		processAttributes(l,pointcuts,false);
		l = AtAjAttributes.readAj5ClassAttributes(javaClass, getResolvedTypeX(), getResolvedTypeX().getSourceContext(), getResolvedTypeX().getWorld().getMessageHandler(),isCodeStyleAspect);
		AjAttribute.Aspect deferredAspectAttribute = processAttributes(l,pointcuts,true);
		
		this.pointcuts = (ResolvedPointcutDefinition[]) 
			pointcuts.toArray(new ResolvedPointcutDefinition[pointcuts.size()]);

		if (deferredAspectAttribute != null) {
			// we can finally process the aspect and its associated perclause...
			perClause = deferredAspectAttribute.reifyFromAtAspectJ(this.getResolvedTypeX());
		}

	}


    private AjAttribute.Aspect processAttributes(List attributeList, List pointcuts, boolean fromAnnotations) {
    	AjAttribute.Aspect deferredAspectAttribute = null;
		for (Iterator iter = attributeList.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			//System.err.println("unpacking: " + this + " and " + a);
			if (a instanceof AjAttribute.Aspect) {
				if (fromAnnotations) {
					deferredAspectAttribute = (AjAttribute.Aspect) a;
				} else {
					perClause = ((AjAttribute.Aspect)a).reify(this.getResolvedTypeX());
					isCodeStyleAspect = true;
				}
			} else if (a instanceof AjAttribute.PointcutDeclarationAttribute) {
				pointcuts.add(((AjAttribute.PointcutDeclarationAttribute)a).reify());
			} else if (a instanceof AjAttribute.WeaverState) {
				weaverState = ((AjAttribute.WeaverState)a).reify();
			} else if (a instanceof AjAttribute.TypeMunger) {
				typeMungers.add(((AjAttribute.TypeMunger)a).reify(getResolvedTypeX().getWorld(), getResolvedTypeX()));
			} else if (a instanceof AjAttribute.DeclareAttribute) {
				declares.add(((AjAttribute.DeclareAttribute)a).getDeclare());
			} else if (a instanceof AjAttribute.PrivilegedAttribute) {
				privilegedAccess = ((AjAttribute.PrivilegedAttribute)a).getAccessedMembers();
			} else if (a instanceof AjAttribute.SourceContextAttribute) {
				if (getResolvedTypeX().getSourceContext() instanceof BcelSourceContext) {
					((BcelSourceContext)getResolvedTypeX().getSourceContext()).addAttributeInfo((AjAttribute.SourceContextAttribute)a);
				}
			} else if (a instanceof AjAttribute.WeaverVersionInfo) {
				wvInfo = (AjAttribute.WeaverVersionInfo)a; // Set the weaver version used to build this type
			} else {
				throw new BCException("bad attribute " + a);
			}
		}
		return deferredAspectAttribute;
	}

	public PerClause getPerClause() {
        if (!isUnpacked) {
            unpackAspectAttributes();
        }
		return perClause;
	}
    
    JavaClass getJavaClass() {
        return javaClass;
    }
    
    public void ensureDelegateConsistent() {
    	if (damaged) {resetState();damaged=false;}
    }
    
    public void resetState() {
		this.interfaces = null;
    	this.superClass = null;
    	this.fields = null;
    	this.methods = null;
    	this.pointcuts = null;
    	this.perClause = null;
    	this.weaverState = null;
    	this.lazyClassGen = null;
    	this.annotations = null;
    	this.annotationTypes = null;
    	
    	isObject = (javaClass.getSuperclassNameIndex() == 0);
        unpackAspectAttributes();
		discoveredWhetherAnnotationStyle = false;
		isAnnotationStyleAspect=false;
    }
    
    public void finishedWith() {
    	// memory usage experiments....
//		this.interfaces = null;
//    	this.superClass = null;
//    	this.fields = null;
//    	this.methods = null;
//    	this.pointcuts = null;
//    	this.perClause = null;
//    	this.weaverState = null;
//    	this.lazyClassGen = null;
    	// this next line frees up memory, but need to understand incremental implications
    	// before leaving it in.
//    	getResolvedTypeX().setSourceContext(null);
    }
    
	public WeaverStateInfo getWeaverState() {
		return weaverState;
	}

	void setWeaverState(WeaverStateInfo weaverState) {
		this.weaverState = weaverState;
	}
	
    public void printWackyStuff(PrintStream out) {
    	if (typeMungers.size() > 0) {
			out.println("  TypeMungers: " + typeMungers);
    	}
    	if (declares.size() > 0) {
    		out.println("     declares: " + declares);
    	}
    }
    
    /**
     * Return the lazyClassGen associated with this type.  For aspect types, this
     * value will be cached, since it is used to inline advice.  For non-aspect
     * types, this lazyClassGen is always newly constructed.
     */
    public LazyClassGen getLazyClassGen() {
    	LazyClassGen ret = lazyClassGen;
    	if (ret == null) {
    		//System.err.println("creating lazy class gen for: " + this);
    		ret = new LazyClassGen(this);
    		//ret.print(System.err);
    		//System.err.println("made LCG from : " + this.getJavaClass().getSuperclassName() );
    		if (isAspect()) {
    			lazyClassGen = ret;
    		}				
    	}
    	return ret;
    }

	public boolean isInterface() {
		return javaClass.isInterface();
	}
	
	public boolean isEnum() {
		return javaClass.isEnum();
	}
	
	public boolean isAnnotation() {
		return javaClass.isAnnotation();
	}
	
	public boolean isAnonymous() {
		return javaClass.isAnonymous();
	}
	
	public boolean isNested() {
		return javaClass.isNested();
	}
	
	public void addAnnotation(AnnotationX annotation) {
		damaged = true;
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
		ret2[len] = getResolvedTypeX().getWorld().resolve(UnresolvedType.forName(annotation.getTypeName()));
		annotationTypes = ret2;
	}
	
	public boolean isAnnotationWithRuntimeRetention() {
		return getRetentionPolicy().equals("RUNTIME");
//	    if (!isAnnotation()) {
//	        return false;
//	    } else {
//	        Annotation[] annotationsOnThisType = javaClass.getAnnotations();
//	        for (int i = 0; i < annotationsOnThisType.length; i++) {
//	            Annotation a = annotationsOnThisType[i];
//	            if (a.getTypeName().equals(UnresolvedType.AT_RETENTION.getName())) {
//	                List values = a.getValues();
//	                boolean isRuntime = false;
//	                for (Iterator it = values.iterator(); it.hasNext();) {
//                        ElementNameValuePair element = (ElementNameValuePair) it.next();
//                        ElementValue v = element.getValue();
//                        isRuntime = v.stringifyValue().equals("RUNTIME");
//                    }
//	                return isRuntime;
//	            }
//	        }
//		}
//	    return false;
	}
	
	
	public String getRetentionPolicy() {
		if (discoveredRetentionPolicy) return retentionPolicy;
		discoveredRetentionPolicy=true;
        retentionPolicy=null; // null means we have no idea
		if (isAnnotation()) {
	        Annotation[] annotationsOnThisType = javaClass.getAnnotations();
	        for (int i = 0; i < annotationsOnThisType.length; i++) {
	            Annotation a = annotationsOnThisType[i];
	            if (a.getTypeName().equals(UnresolvedType.AT_RETENTION.getName())) {
	                List values = a.getValues();
	                boolean isRuntime = false;
	                for (Iterator it = values.iterator(); it.hasNext();) {
                        ElementNameValuePair element = (ElementNameValuePair) it.next();
                        ElementValue v = element.getValue();
                        retentionPolicy = v.stringifyValue();
                        return retentionPolicy;
                    }
	            }
	        }
		}
	    return retentionPolicy;
	}
	
	public boolean canAnnotationTargetType() {
		AnnotationTargetKind[] targetKinds = getAnnotationTargetKinds();
		if (targetKinds == null) return true;
		for (int i = 0; i < targetKinds.length; i++) {
			if (targetKinds[i].equals(AnnotationTargetKind.TYPE)) {
				return true;
			}
		}
		return false;
	}
	
	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		if (discoveredAnnotationTargetKinds) return annotationTargetKinds;
		discoveredAnnotationTargetKinds = true;
		annotationTargetKinds = null; // null means we have no idea or the @Target annotation hasn't been used
		List targetKinds = new ArrayList();
		if (isAnnotation()) {
	        Annotation[] annotationsOnThisType = javaClass.getAnnotations();
	        for (int i = 0; i < annotationsOnThisType.length; i++) {
	            Annotation a = annotationsOnThisType[i];
	            if (a.getTypeName().equals(UnresolvedType.AT_TARGET.getName())) {
	                List values = a.getValues();
	                for (Iterator it = values.iterator(); it.hasNext();) {
                        ElementNameValuePair element = (ElementNameValuePair) it.next();
                        ElementValue v = element.getValue();
                        String targetKind = v.stringifyValue();
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
							targetKinds.add(AnnotationTargetKind.TYPE);
						} 
                    }
	            }
	        }
			if (!targetKinds.isEmpty()) {
				annotationTargetKinds = new AnnotationTargetKind[targetKinds.size()];
				return (AnnotationTargetKind[]) targetKinds.toArray(annotationTargetKinds);	
			}
		}
		return annotationTargetKinds;
	}
	
	public boolean isSynthetic() {
		return getResolvedTypeX().isSynthetic();
	}

	public ISourceLocation getSourceLocation() {
		return getResolvedTypeX().getSourceContext().makeSourceLocation(0, 0); //FIXME ??? we can do better than this
	}
	
	public AjAttribute.WeaverVersionInfo getWeaverVersionAttribute() {
		return wvInfo;
	}

	public void addParent(ResolvedType newParent) {
		damaged = true;
		if (newParent.isClass()) {
			superClass = newParent;
		} else {
			ResolvedType[] oldInterfaceNames = getDeclaredInterfaces();
			int len = oldInterfaceNames.length;
			ResolvedType[] newInterfaceNames = new ResolvedType[len+1];
			System.arraycopy(oldInterfaceNames, 0, newInterfaceNames, 0, len);
			newInterfaceNames[len] = newParent;
			
			interfaces = newInterfaceNames;
		}
		//System.err.println("javaClass: " + Arrays.asList(javaClass.getInterfaceNames()) + " super " + javaClass.getSuperclassName());
		//if (lazyClassGen != null) lazyClassGen.print();
	}



	public boolean hasAnnotation(UnresolvedType ofType) {
		ensureAnnotationTypesRetrieved();
		for (int i = 0; i < annotationTypes.length; i++) {
			ResolvedType annX = annotationTypes[i];
			if (annX.equals(ofType)) return true;
		}
		return false;
	}
	
	private void ensureAnnotationTypesRetrieved() {
		if (annotationTypes == null) {
    		Annotation annos[] = javaClass.getAnnotations();
    		annotationTypes = new ResolvedType[annos.length];
    		annotations = new AnnotationX[annos.length];
    		for (int i = 0; i < annos.length; i++) {
				Annotation annotation = annos[i];
				ResolvedType rtx = getResolvedTypeX().getWorld().resolve(UnresolvedType.forName(annotation.getTypeName()));
				annotationTypes[i] = rtx;
				annotations[i] = new AnnotationX(annotation,getResolvedTypeX().getWorld());
			}
    	}
	}
	
	public ResolvedType[] getAnnotationTypes() {
    	ensureAnnotationTypesRetrieved();
    	return annotationTypes;
    }
	
	/** 
	 * Releases annotations wrapped in an annotationX
	 */
	public AnnotationX[] getAnnotations() {
		ensureAnnotationTypesRetrieved();
		return annotations;
	}


	public String getDeclaredGenericSignature() {
		if (!haveLookedForDeclaredSignature) {
			haveLookedForDeclaredSignature = true;
			Attribute[] as = javaClass.getAttributes();
			for (int i = 0; i < as.length && declaredSignature==null; i++) {
				Attribute attribute = as[i];
				if (attribute instanceof Signature) declaredSignature = ((Signature)attribute).getSignature();
			}
			if (declaredSignature!=null) isGenericType= (declaredSignature.charAt(0)=='<');
		}
		return declaredSignature;
	}
	
	Signature.ClassSignature getGenericClassTypeSignature() {
		return javaClass.getGenericClassTypeSignature();
	}
	
	private boolean genericSignatureUnpacked = false;
	private Signature.FormalTypeParameter[] formalsForResolution = null;
		
	private void unpackGenericSignature() {
		if (genericSignatureUnpacked) return;
		genericSignatureUnpacked = true;
		Signature.ClassSignature cSig = getGenericClassTypeSignature();
		if (cSig != null) {
			formalsForResolution = cSig.formalTypeParameters;
			if (isNestedClass()) {
				// we have to find any type variables from the outer type before proceeding with resolution.
				Signature.FormalTypeParameter[] extraFormals = getFormalTypeParametersFromOuterClass();
				if (extraFormals.length > 0) {
					List allFormals = new ArrayList();
					for (int i = 0; i < formalsForResolution.length; i++) {
						allFormals.add(formalsForResolution[i]);
					}
					for (int i = 0; i < extraFormals.length; i++) {
						allFormals.add(extraFormals[i]);
					}
					formalsForResolution = new Signature.FormalTypeParameter[allFormals.size()];
					allFormals.toArray(formalsForResolution);
				}
			}
			Signature.ClassTypeSignature superSig = cSig.superclassSignature;
			try {
				this.superClass = 
					BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
							superSig, formalsForResolution, getResolvedTypeX().getWorld());
			} catch (GenericSignatureFormatException e) {
				// development bug, fail fast with good info
				throw new IllegalStateException(
						"While determing the generic superclass of " + this.javaClass.getClassName()
						+ " with generic signature " + this.javaClass.getGenericSignature() + " the following error was detected: "
						+ e.getMessage());
			}
			this.interfaces = new ResolvedType[cSig.superInterfaceSignatures.length];
			for (int i = 0; i < cSig.superInterfaceSignatures.length; i++) {
				try {
					this.interfaces[i] = 
						BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
								cSig.superInterfaceSignatures[i],
								formalsForResolution, 
								getResolvedTypeX().getWorld());
				} catch (GenericSignatureFormatException e) {
					// development bug, fail fast with good info
					throw new IllegalStateException(
							"While determing the generic superinterfaces of " + this.javaClass.getClassName()
							+ " with generic signature " + this.javaClass.getGenericSignature() + " the following error was detected: "
							+ e.getMessage());
				}
			}
		}
		if (isGeneric()) {
			// update resolved typex to point at generic type not raw type.
			ReferenceType genericType = (ReferenceType) this.resolvedTypeX.getGenericType();
			genericType.setSourceContext(this.resolvedTypeX.getSourceContext());
			genericType.setStartPos(this.resolvedTypeX.getStartPos());
			this.resolvedTypeX = genericType;
		}
	}
	
	public Signature.FormalTypeParameter[] getAllFormals() {
		unpackGenericSignature();
		if (formalsForResolution == null) {
			return new Signature.FormalTypeParameter[0];
		} else {
			return formalsForResolution;
		}
	}
	
	private boolean isNestedClass() {
		return javaClass.getClassName().indexOf('$') != -1;
	}
	
	private ReferenceType getOuterClass() {
		if (!isNestedClass()) throw new IllegalStateException("Can't get the outer class of a non-nested type");
		int lastDollar = javaClass.getClassName().lastIndexOf('$');
		String superClassName = javaClass.getClassName().substring(0,lastDollar);
		UnresolvedType outer = UnresolvedType.forName(superClassName);
		return (ReferenceType) outer.resolve(getResolvedTypeX().getWorld());
	}
	
	private Signature.FormalTypeParameter[] getFormalTypeParametersFromOuterClass() {
		List typeParameters = new ArrayList();
		ReferenceType outer = getOuterClass();
		ReferenceTypeDelegate outerDelegate = outer.getDelegate();
		if (!(outerDelegate instanceof BcelObjectType)) {
			throw new IllegalStateException("How come we're in BcelObjectType resolving an inner type of something that is NOT a BcelObjectType??");
		}
		BcelObjectType outerObjectType = (BcelObjectType) outerDelegate;
		if (outerObjectType.isNestedClass()) {
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
	
	private void ensureGenericInfoProcessed() { getDeclaredGenericSignature();}
	
	public boolean isGeneric() {
	  ensureGenericInfoProcessed();
	  return isGenericType;
	}
	
	public String toString() {
		return (javaClass==null?"BcelObjectType":"BcelObjectTypeFor:"+javaClass.getClassName());
	}
	
} 
    
    
