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

import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.patterns.PerClause;

// ??? exposed for testing
public class BcelObjectType extends ResolvedTypeX.ConcreteName {
    private JavaClass javaClass;
    private boolean isObject = false;  // set upon construction
	private LazyClassGen lazyClassGen = null;  // set lazily if it's an aspect

	// lazy, for no particular reason I can discern
    private ResolvedTypeX[] interfaces = null;
    private ResolvedTypeX superClass = null;
    private ResolvedMember[] fields = null;
    private ResolvedMember[] methods = null;
    private ResolvedTypeX[] resolvedAnnotations = null;
            
    // strangely non-lazy
    private ResolvedPointcutDefinition[] pointcuts = null;
	private PerClause perClause = null;
	private WeaverStateInfo weaverState = null;
	private AjAttribute.WeaverVersionInfo wvInfo = null;
	private List typeMungers = Collections.EMPTY_LIST;
	private List declares = Collections.EMPTY_LIST;
	private ResolvedMember[] privilegedAccess = null;
	
	
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
    BcelObjectType(ResolvedTypeX.Name resolvedTypeX, JavaClass javaClass, boolean exposedToWeaver) {
        super(resolvedTypeX, exposedToWeaver);
        this.javaClass = javaClass;
        
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

	private void unpackAspectAttributes() {
		List pointcuts = new ArrayList();
		typeMungers = new ArrayList();
		declares = new ArrayList();
		List l = BcelAttributes.readAjAttributes(javaClass.getAttributes(), getResolvedTypeX().getSourceContext(),getResolvedTypeX().getWorld().getMessageHandler());
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			//System.err.println("unpacking: " + this + " and " + a);
			if (a instanceof AjAttribute.Aspect) {
				perClause = ((AjAttribute.Aspect)a).reify(this.getResolvedTypeX());
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
				wvInfo = (AjAttribute.WeaverVersionInfo)a;
				if (wvInfo.getMajorVersion() > WeaverVersionInfo.getCurrentWeaverMajorVersion()) {
					// The class file containing this attribute was created by a version of AspectJ that
					// added some behavior that 'this' version of AspectJ doesn't understand.  And the
					// class file contains changes that mean 'this' version of AspectJ cannot continue.
					throw new BCException("Unable to continue, this version of AspectJ supports classes built with weaver version "+
							WeaverVersionInfo.toCurrentVersionString()+" but the class "+ javaClass.getClassName()+" is version "+wvInfo.toString());
				}
			} else {
				throw new BCException("bad attribute " + a);
			}
		}
		this.pointcuts = (ResolvedPointcutDefinition[]) 
			pointcuts.toArray(new ResolvedPointcutDefinition[pointcuts.size()]);
//		this.typeMungers = (BcelTypeMunger[]) 
//			typeMungers.toArray(new BcelTypeMunger[typeMungers.size()]);
//		this.declares = (Declare[])
//			declares.toArray(new Declare[declares.size()]);	
	}

	public PerClause getPerClause() {
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
    	
    	isObject = (javaClass.getSuperclassNameIndex() == 0);
        unpackAspectAttributes();
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
	
	public boolean isSynthetic() {
		return getResolvedTypeX().isSynthetic();
	}

	public ISourceLocation getSourceLocation() {
		return getResolvedTypeX().getSourceContext().makeSourceLocation(0); //FIXME, we can do better than this
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



	public boolean hasAnnotation(ResolvedTypeX ofType) {
		Annotation[] annotationsOnThisType = javaClass.getAnnotations();
		for (int i = 0; i < annotationsOnThisType.length; i++) {
			Annotation a = annotationsOnThisType[i];
			if (a.getTypeName().equals(ofType.getName())) return true;
		}
		return false;
	}
	
	public ResolvedTypeX[] getAnnotationTypes() {
    	if (resolvedAnnotations == null) {
    		Annotation annotations[] = javaClass.getAnnotations();
    		resolvedAnnotations = new ResolvedTypeX[annotations.length];
    		for (int i = 0; i < annotations.length; i++) {
				Annotation annotation = annotations[i];
				ResolvedTypeX rtx = getResolvedTypeX().getWorld().resolve(TypeX.forName(annotation.getTypeName()));
				resolvedAnnotations[i] = rtx;
			}
    	}
    	return resolvedAnnotations;
    }
} 
    
    
