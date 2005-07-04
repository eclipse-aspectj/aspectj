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
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.patterns.PerClause;


// ??? exposed for testing
public class BcelObjectType extends AbstractReferenceTypeDelegate {
    private JavaClass javaClass;
    private boolean isObject = false;  // set upon construction
	private LazyClassGen lazyClassGen = null;  // set lazily if it's an aspect

	// lazy, for no particular reason I can discern
    private ResolvedTypeX[] interfaces = null;
    private ResolvedTypeX superClass = null;
    private ResolvedMember[] fields = null;
    private ResolvedMember[] methods = null;
    private ResolvedTypeX[] annotationTypes = null;
    private AnnotationX[] annotations = null;

    // track unpackAttribute. In some case (per clause inheritance) we encounter
    // unpacked state when calling getPerClause
    // this whole thing would require more clean up (AV)
    private boolean isUnpacked = false;

    // strangely non-lazy
    private ResolvedPointcutDefinition[] pointcuts = null;
	private PerClause perClause = null;
	private WeaverStateInfo weaverState = null;
	private AjAttribute.WeaverVersionInfo wvInfo = null;
	private List typeMungers = Collections.EMPTY_LIST;
	private List declares = Collections.EMPTY_LIST;
	private ResolvedMember[] privilegedAccess = null;

	private boolean discoveredWhetherAnnotationStyle = false;
    private boolean isAnnotationStyleAspect = false;// set upon construction
	private boolean isCodeStyleAspect = false; // not redundant with field above!

//  TODO asc need soon but not just yet...
//	private boolean discoveredDeclaredSignature = false;
//	private String declaredSignature = null;

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
    
    public int getModifiers() {
        return javaClass.getAccessFlags();
    }

    public ResolvedTypeX getSuperclass() {
        if (isObject) return null;
        if (superClass == null) {
            superClass = getResolvedTypeX().getWorld().resolve(TypeX.forName(javaClass.getSuperclassName()));
        }
        return superClass;
    }
        
    public ResolvedTypeX[] getDeclaredInterfaces() {
        if (interfaces == null) {
            String[] ifaceNames = javaClass.getInterfaceNames();
            interfaces = new ResolvedTypeX[ifaceNames.length];
            for (int i = 0, len = ifaceNames.length; i < len; i++) {
                interfaces[i] = getResolvedTypeX().getWorld().resolve(TypeX.forName(ifaceNames[i]));
            }
        }
        return interfaces;
    }
    
    public ResolvedMember[] getDeclaredMethods() {
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
        List l = BcelAttributes.readAjAttributes(javaClass.getClassName(),javaClass.getAttributes(), getResolvedTypeX().getSourceContext(),getResolvedTypeX().getWorld().getMessageHandler());
		processAttributes(l,pointcuts,false);
		l = AtAjAttributes.readAj5ClassAttributes(javaClass, getResolvedTypeX(), getResolvedTypeX().getSourceContext(), getResolvedTypeX().getWorld().getMessageHandler(),isCodeStyleAspect);
		processAttributes(l,pointcuts,true);
		
		this.pointcuts = (ResolvedPointcutDefinition[]) 
			pointcuts.toArray(new ResolvedPointcutDefinition[pointcuts.size()]);
		// Test isn't quite right, leaving this out for now...
//		if (isAspect() && wvInfo.getMajorVersion() == WeaverVersionInfo.UNKNOWN.getMajorVersion()) {
//			throw new BCException("Unable to continue, this version of AspectJ cannot use aspects as input that were built "+
//					"with an AspectJ earlier than version 1.2.1.  Please rebuild class: "+javaClass.getClassName());
//		}
		
//		this.typeMungers = (BcelTypeMunger[]) 
//			typeMungers.toArray(new BcelTypeMunger[typeMungers.size()]);
//		this.declares = (Declare[])
//			declares.toArray(new Declare[declares.size()]);	
	}


	private void processAttributes(List attributeList, List pointcuts, boolean fromAnnotations) {
		for (Iterator iter = attributeList.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			//System.err.println("unpacking: " + this + " and " + a);
			if (a instanceof AjAttribute.Aspect) {
				perClause = ((AjAttribute.Aspect)a).reify(this.getResolvedTypeX());
				if (!fromAnnotations) isCodeStyleAspect = true;
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
	
	public void addAnnotation(AnnotationX annotation) {
		
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
		ret2[len] = getResolvedTypeX().getWorld().resolve(TypeX.forName(annotation.getTypeName()));
		annotationTypes = ret2;
	}
	
	public boolean isAnnotationWithRuntimeRetention() {
	    if (!isAnnotation()) {
	        return false;
	    } else {
	        Annotation[] annotationsOnThisType = javaClass.getAnnotations();
	        for (int i = 0; i < annotationsOnThisType.length; i++) {
	            Annotation a = annotationsOnThisType[i];
	            if (a.getTypeName().equals(TypeX.AT_RETENTION.getName())) {
	                List values = a.getValues();
	                boolean isRuntime = false;
	                for (Iterator it = values.iterator(); it.hasNext();) {
                        ElementNameValuePair element = (ElementNameValuePair) it.next();
                        ElementValue v = element.getValue();
                        isRuntime = v.stringifyValue().equals("RUNTIME");
                    }
	                return isRuntime;
	            }
	        }
		}
	    return false;
	}
	
	public boolean isSynthetic() {
		return getResolvedTypeX().isSynthetic();
	}

	public ISourceLocation getSourceLocation() {
		return getResolvedTypeX().getSourceContext().makeSourceLocation(0); //FIXME ??? we can do better than this
	}
	
	public AjAttribute.WeaverVersionInfo getWeaverVersionAttribute() {
		return wvInfo;
	}

	public void addParent(ResolvedTypeX newParent) {
		if (newParent.isClass()) {
			superClass = newParent;
		} else {
			ResolvedTypeX[] oldInterfaceNames = getDeclaredInterfaces();
			int len = oldInterfaceNames.length;
			ResolvedTypeX[] newInterfaceNames = new ResolvedTypeX[len+1];
			System.arraycopy(oldInterfaceNames, 0, newInterfaceNames, 0, len);
			newInterfaceNames[len] = newParent;
			
			interfaces = newInterfaceNames;
		}
		//System.err.println("javaClass: " + Arrays.asList(javaClass.getInterfaceNames()) + " super " + javaClass.getSuperclassName());
		//if (lazyClassGen != null) lazyClassGen.print();
	}



	public boolean hasAnnotation(TypeX ofType) {
		ensureAnnotationTypesRetrieved();
		for (int i = 0; i < annotationTypes.length; i++) {
			ResolvedTypeX annX = annotationTypes[i];
			if (annX.equals(ofType)) return true;
		}
		return false;
	}
	
	private void ensureAnnotationTypesRetrieved() {
		if (annotationTypes == null) {
    		Annotation annos[] = javaClass.getAnnotations();
    		annotationTypes = new ResolvedTypeX[annos.length];
    		annotations = new AnnotationX[annos.length];
    		for (int i = 0; i < annos.length; i++) {
				Annotation annotation = annos[i];
				ResolvedTypeX rtx = getResolvedTypeX().getWorld().resolve(TypeX.forName(annotation.getTypeName()));
				annotationTypes[i] = rtx;
				annotations[i] = new AnnotationX(annotation,getResolvedTypeX().getWorld());
			}
    	}
	}
	
	public ResolvedTypeX[] getAnnotationTypes() {
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
		Attribute[] as = javaClass.getAttributes();
		for (int i = 0; i < as.length; i++) {
			Attribute attribute = as[i];
			if (attribute instanceof Signature) return ((Signature)attribute).getSignature();
		}
		throw new RuntimeException("Should not have been asked for the signature?");
	}
} 
    
    
