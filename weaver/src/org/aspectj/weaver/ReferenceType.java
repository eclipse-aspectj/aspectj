/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - June 2005 - separated out from ResolvedTypeX
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Collection;
import java.util.Iterator;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.patterns.PerClause;

/**
 * A reference type represents some 'real' type, not a primitive, not an array - but
 * a real type, for example java.util.List.  Each ReferenceType has a delegate
 * that is the underlying artifact - either an eclipse artifact or a
 * bcel artifact.  If the type represents a raw type (i.e. there is
 * a generic form) then the genericType field is set to point to the
 * generic type.  If it is for a parameterized type then the generic type
 * is also set to point to the generic form.
 */
public class ReferenceType extends ResolvedTypeX {
	
	/**
	 * For parameterized types (or the raw type) - this field points to the actual
	 * reference type from which they are derived.
	 */
	ReferenceType genericType = null;
	
	ReferenceTypeDelegate delegate = null;
	ISourceContext sourceContext = null;
	int startPos = 0;
	int endPos = 0;

	//??? should set delegate before any use
    public ReferenceType(String signature, World world) {
        super(signature, world);
    }
	
    /**
     * Create a reference type for a generic type
     */
	public ReferenceType(TypeX genericType, World world) {
		super(genericType.getSignature(),world);
		genericSignature=genericType.genericSignature;
	}
        
    public final boolean isClass() {
    	return delegate.isClass();
    }
    
    public final boolean isGeneric() {
    	return delegate.isGeneric();
    }

    public AnnotationX[] getAnnotations() {
    	return delegate.getAnnotations();
    }
    
    public void addAnnotation(AnnotationX annotationX) {
    	delegate.addAnnotation(annotationX);
    }
    public boolean hasAnnotation(TypeX ofType) {
    	return delegate.hasAnnotation(ofType);
    }
    
    public ResolvedTypeX[] getAnnotationTypes() {
    	return delegate.getAnnotationTypes(); 
    }
    
    public boolean isAspect() {
    	return delegate.isAspect();
    }

    public boolean isAnnotationStyleAspect() {
        return delegate.isAnnotationStyleAspect();
    }

    public boolean isEnum() {
    	return delegate.isEnum();
    }
    
    public boolean isAnnotation() {
    	return delegate.isAnnotation();
    }
    
    public boolean isAnnotationWithRuntimeRetention() {
        return delegate.isAnnotationWithRuntimeRetention();
    }
     
    public final boolean needsNoConversionFrom(TypeX o) {
        return isAssignableFrom(o);
    }
     
    public final boolean isAssignableFrom(TypeX o) {
    	if (o.isPrimitive()) {
    		if (!world.behaveInJava5Way) return false;
    		if (ResolvedTypeX.validBoxing.contains(this.getSignature()+o.getSignature())) return true;
    	}
        ResolvedTypeX other = o.resolve(world);

        return isAssignableFrom(other);
    }
    
    public final boolean isCoerceableFrom(TypeX o) {
        ResolvedTypeX other = o.resolve(world);

        if (this.isAssignableFrom(other) || other.isAssignableFrom(this)) {
            return true;
        }          
        if (!this.isInterface() && !other.isInterface()) {
            return false;
        }
        if (this.isFinal() || other.isFinal()) {
            return false;
        }            
        // ??? needs to be Methods, not just declared methods? JLS 5.5 unclear
        ResolvedMember[] a = getDeclaredMethods();
        ResolvedMember[] b = other.getDeclaredMethods();  //??? is this cast always safe
        for (int ai = 0, alen = a.length; ai < alen; ai++) {
            for (int bi = 0, blen = b.length; bi < blen; bi++) {
                if (! b[bi].isCompatibleWith(a[ai])) return false;
            }
        } 
        return true;
    }
    
    private boolean isAssignableFrom(ResolvedTypeX other) {
        if (this == other) return true;
        for(Iterator i = other.getDirectSupertypes(); i.hasNext(); ) {
            if (this.isAssignableFrom((ResolvedTypeX) i.next())) return true;
        }       
        return false;
    }

	public ISourceContext getSourceContext() {
		return sourceContext;
	}
	
	public ISourceLocation getSourceLocation() {
		if (sourceContext == null) return null;
		return sourceContext.makeSourceLocation(new Position(startPos, endPos));
	}

	public boolean isExposedToWeaver() {
		return (delegate == null) || delegate.isExposedToWeaver();  //??? where does this belong
	}
	
	public WeaverStateInfo getWeaverState() {
		return delegate.getWeaverState();
	}

	public ResolvedMember[] getDeclaredFields() {
		return delegate.getDeclaredFields();
	}

	public ResolvedTypeX[] getDeclaredInterfaces() {
		return delegate.getDeclaredInterfaces();
	}

	public ResolvedMember[] getDeclaredMethods() {
		return delegate.getDeclaredMethods();
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		return delegate.getDeclaredPointcuts();
	}

	public PerClause getPerClause() { return delegate.getPerClause(); }
	protected Collection getDeclares() { return delegate.getDeclares(); }
	protected Collection getTypeMungers() { return delegate.getTypeMungers(); }
	
	protected Collection getPrivilegedAccesses() { return delegate.getPrivilegedAccesses(); }


	public int getModifiers() {
		return delegate.getModifiers();
	}

	public ResolvedTypeX getSuperclass() {
		return delegate.getSuperclass();
	}


	public ReferenceTypeDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(ReferenceTypeDelegate delegate) {
		this.delegate = delegate;
	}
    
	public int getEndPos() {
		return endPos;
	}

	public int getStartPos() {
		return startPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	public void setSourceContext(ISourceContext sourceContext) {
		this.sourceContext = sourceContext;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
	
	public boolean doesNotExposeShadowMungers() {
		return delegate.doesNotExposeShadowMungers();
	}
	
	public String getDeclaredGenericSignature() {
		return delegate.getDeclaredGenericSignature();
	}
	
	public void setGenericType(ReferenceType rt) {
		this.genericType = rt;
	}
	
	public ResolvedTypeX getGenericType() {
		return genericType;
	}

}