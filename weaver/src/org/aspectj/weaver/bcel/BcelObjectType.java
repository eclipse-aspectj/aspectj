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
import java.util.*;

import org.apache.bcel.classfile.*;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.*;
import org.aspectj.weaver.patterns.PerClause;

// ??? exposed for testing
public class BcelObjectType extends ResolvedTypeX.Name {
    private JavaClass javaClass;
    private boolean isObject = false;  // set upon construction
	private LazyClassGen lazyClassGen = null;  // set lazily if it's an aspect

	// lazy, for no particular reason I can discern
    private ResolvedTypeX[] interfaces = null;
    private ResolvedTypeX superClass = null;
    private ResolvedMember[] fields = null;
    private ResolvedMember[] methods = null;
            
    // strangely non-lazy
    private ResolvedPointcutDefinition[] pointcuts = null;
	private PerClause perClause = null;
	private WeaverStateKind weaverState = null;
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
    BcelObjectType(String signature, World world, JavaClass javaClass, boolean exposedToWeaver) {
        super(signature, world, exposedToWeaver);
        this.javaClass = javaClass;
        
        sourceContext = new BcelSourceContext(this);
        
        // this should only ever be java.lang.Object which is 
        // the only class in Java-1.4 with no superclasses
        isObject = (javaClass.getSuperclassNameIndex() == 0);
        unpackAspectAttributes();
    }
    
    public int getModifiers() {
        return javaClass.getAccessFlags();
    }

    public ResolvedTypeX getSuperclass() {
        if (isObject) return null;
        if (superClass == null) {
            superClass = world.resolve(TypeX.forName(javaClass.getSuperclassName()));
        }
        return superClass;
    }
        
    public ResolvedTypeX[] getDeclaredInterfaces() {
        if (interfaces == null) {
            String[] ifaceNames = javaClass.getInterfaceNames();
            interfaces = new ResolvedTypeX[ifaceNames.length];
            for (int i = 0, len = ifaceNames.length; i < len; i++) {
                interfaces[i] = world.resolve(TypeX.forName(ifaceNames[i]));
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
		List l = BcelAttributes.readAjAttributes(javaClass.getAttributes(), getSourceContext());
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			//System.err.println("unpacking: " + this + " and " + a);
			if (a instanceof AjAttribute.Aspect) {
				perClause = ((AjAttribute.Aspect)a).reify(this);
			} else if (a instanceof AjAttribute.PointcutDeclarationAttribute) {
				pointcuts.add(((AjAttribute.PointcutDeclarationAttribute)a).reify());
			} else if (a instanceof AjAttribute.WeaverState) {
				weaverState = ((AjAttribute.WeaverState)a).reify();
			} else if (a instanceof AjAttribute.TypeMunger) {
				typeMungers.add(((AjAttribute.TypeMunger)a).reify(getWorld(), this));
			} else if (a instanceof AjAttribute.DeclareAttribute) {
				declares.add(((AjAttribute.DeclareAttribute)a).getDeclare());
			} else if (a instanceof AjAttribute.PrivilegedAttribute) {
				privilegedAccess = ((AjAttribute.PrivilegedAttribute)a).getAccessedMembers();
			} else if (a instanceof AjAttribute.SourceContextAttribute) {
				((BcelSourceContext)sourceContext).addAttributeInfo((AjAttribute.SourceContextAttribute)a);
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
    
    void resetState() {
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
    
    
    /**
     * Switch to a new JavaClass and clear all caches
     */
    void replaceJavaClass(JavaClass jc) {
    	if (this.javaClass == jc) return;
    	
    	this.javaClass = jc;
		resetState();
	}

	public WeaverStateKind getWeaverState() {
		return weaverState;
	}

	public void setWeaverState(WeaverStateKind weaverState) {
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
    		if (isAspect()) {
    			lazyClassGen = ret;
    		}				
    	}
    	return ret;
    }
} 
    
    
