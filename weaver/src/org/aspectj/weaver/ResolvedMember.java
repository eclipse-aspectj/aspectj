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


package org.aspectj.weaver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.ISourceLocation;

/**
 * This is the declared member, i.e. it will always correspond to an
 * actual method/... declaration
 */
public class ResolvedMember extends Member implements IHasPosition, AnnotatedElement, TypeVariableDeclaringElement {
    
    public String[] parameterNames = null;
    protected UnresolvedType[] checkedExceptions = UnresolvedType.NONE;
    /**
     * if this member is a parameterized version of a member in a generic type,
     * then this field holds a reference to the member we parameterize.
     */
    protected ResolvedMember backingGenericMember = null;
    private Set annotationTypes = null;
	// Some members are 'created' to represent other things (for example ITDs).  These
	// members have their annotations stored elsewhere, and this flag indicates that is
	// the case.  It is up to the caller to work out where that is!
	// Once determined the caller may choose to stash the annotations in this member...
	private boolean isAnnotatedElsewhere = false; // this field is not serialized.
	private boolean isAjSynthetic = true;
    
    // generic methods have type variables
	private UnresolvedType[] typeVariables;
    
    // these three fields hold the source location of this member
	protected int start, end;
	protected ISourceContext sourceContext = null;
    
    //XXX deprecate this in favor of the constructor below
	public ResolvedMember(
		Kind kind,
		UnresolvedType declaringType,
		int modifiers,
		UnresolvedType returnType,
		String name,
		UnresolvedType[] parameterTypes)
	{
		super(kind, declaringType, modifiers, returnType, name, parameterTypes);
	}

    
    
	public ResolvedMember(
		Kind kind,
		UnresolvedType declaringType,
		int modifiers,
		UnresolvedType returnType,
		String name,
		UnresolvedType[] parameterTypes,
		UnresolvedType[] checkedExceptions) 
	{
		super(kind, declaringType, modifiers, returnType, name, parameterTypes);
		this.checkedExceptions = checkedExceptions;
	}
    
	public ResolvedMember(
			Kind kind,
			UnresolvedType declaringType,
			int modifiers,
			UnresolvedType returnType,
			String name,
			UnresolvedType[] parameterTypes,
			UnresolvedType[] checkedExceptions,
			ResolvedMember backingGenericMember) 
		{
			this(kind, declaringType, modifiers, returnType, name, parameterTypes,checkedExceptions);
			this.backingGenericMember = backingGenericMember;
			this.isAjSynthetic = backingGenericMember.isAjSynthetic();
		}
	
	public ResolvedMember(
		Kind kind,
		UnresolvedType declaringType,
		int modifiers,
		String name,
		String signature) 
	{
		super(kind, declaringType, modifiers, name, signature);
	}    

    public static final ResolvedMember[] NONE = new ResolvedMember[0];

	// ----

    public final int getModifiers(World world) {
        return modifiers;
    }
    public final int getModifiers() {
        return modifiers;
    }

	// ----
	

    public final UnresolvedType[] getExceptions(World world) {
        return getExceptions();
    }
    
    public UnresolvedType[] getExceptions() {
        return checkedExceptions;
    }
    
    public ShadowMunger getAssociatedShadowMunger() {
		return null;
    }
    
    // ??? true or false?
    public boolean isAjSynthetic() {
    	return isAjSynthetic;
    }
	
	public boolean hasAnnotations() {
		return  (annotationTypes==null);
	}

    public boolean hasAnnotation(UnresolvedType ofType) {
        // The ctors don't allow annotations to be specified ... yet - but
        // that doesn't mean it is an error to call this method.
        // Normally the weaver will be working with subtypes of 
        // this type - BcelField/BcelMethod
        if (annotationTypes==null) return false;
		return annotationTypes.contains(ofType);
    }
    
    public ResolvedType[] getAnnotationTypes() {
    	// The ctors don't allow annotations to be specified ... yet - but
    	// that doesn't mean it is an error to call this method.
    	// Normally the weaver will be working with subtypes of
    	// this type - BcelField/BcelMethod
    	if (annotationTypes == null) return null;
		return (ResolvedType[])annotationTypes.toArray(new ResolvedType[]{});
    }
    
	public void setAnnotationTypes(UnresolvedType[] annotationtypes) {
		if (annotationTypes == null) annotationTypes = new HashSet();
		for (int i = 0; i < annotationtypes.length; i++) {
			UnresolvedType typeX = annotationtypes[i];
			annotationTypes.add(typeX);
		}
	}
	
	public void addAnnotation(AnnotationX annotation) {
	    // FIXME asc only allows for annotation types, not instances - should it?
		if (annotationTypes == null) annotationTypes = new HashSet();
		annotationTypes.add(annotation.getSignature());
	}
	    
    public boolean isBridgeMethod() {
    	return (modifiers & Constants.ACC_BRIDGE)!=0;
    }
    
    public boolean isVarargsMethod() {
    	return (modifiers & Constants.ACC_VARARGS)!=0;
    }
    
	public boolean isSynthetic() {
		return false;
	}
    
    public void write(DataOutputStream s) throws IOException {
    	getKind().write(s);
    	getDeclaringType().write(s);
    	s.writeInt(modifiers);
    	s.writeUTF(getName());
    	s.writeUTF(getSignature());
		UnresolvedType.writeArray(getExceptions(), s);

		s.writeInt(getStart());
		s.writeInt(getEnd());

		// Write out any type variables...
		if (typeVariables==null) {
			s.writeInt(0);
		} else {
			s.writeInt(typeVariables.length);
			for (int i = 0; i < typeVariables.length; i++) {
				typeVariables[i].write(s);
			}
		}
    }

    public static void writeArray(ResolvedMember[] members, DataOutputStream s) throws IOException {
		s.writeInt(members.length);
		for (int i = 0, len = members.length; i < len; i++) {
			members[i].write(s);
		}
    }

    
    public static ResolvedMember readResolvedMember(VersionedDataInputStream s, ISourceContext sourceContext) throws IOException {
    	ResolvedMember m = new ResolvedMember(Kind.read(s), UnresolvedType.read(s), s.readInt(), s.readUTF(), s.readUTF());
		m.checkedExceptions = UnresolvedType.readArray(s);

		m.start = s.readInt();
		m.end = s.readInt();
		m.sourceContext = sourceContext;
		
		// Read in the type variables...
		if (s.getMajorVersion()>=AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
			int tvcount = s.readInt();
			if (tvcount!=0) {
				m.typeVariables = new UnresolvedType[tvcount];
				for (int i=0;i<tvcount;i++) {
					m.typeVariables[i]=UnresolvedType.read(s);
				}
			}
		}
		return m;
    }
    
    public static ResolvedMember[] readResolvedMemberArray(VersionedDataInputStream s, ISourceContext context) throws IOException {
    	int len = s.readInt();
		ResolvedMember[] members = new ResolvedMember[len];
		for (int i=0; i < len; i++) {
			members[i] = ResolvedMember.readResolvedMember(s, context);
		}
		return members;
    }
    
    
    
	public ResolvedMember resolve(World world) {
	    // FIXME asc guard with a check on resolution having happened !
        if (annotationTypes!=null) {
          Set r = new HashSet();
          for (Iterator iter = annotationTypes.iterator(); iter.hasNext();) {
			UnresolvedType element = (UnresolvedType) iter.next();
			r.add(world.resolve(element));
		  }
		  annotationTypes = r;
	    }
        declaringType = declaringType.resolve(world);
        if (declaringType.isRawType()) declaringType = ((ReferenceType)declaringType).getGenericType();
		return this;
	}
	
	public ISourceContext getSourceContext(World world) {
		return getDeclaringType().resolve(world).getSourceContext();
	}

	public final String[] getParameterNames() {
		return parameterNames;
	}
	public final String[] getParameterNames(World world) {
		return getParameterNames();
	}
	
	public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature() {
		return null;
	}
	
    public ISourceLocation getSourceLocation() {
    	//System.out.println("get context: " + this + " is " + sourceContext);
    	if (sourceContext == null) {
    		//System.err.println("no context: " + this);
    		return null;
    	}
    	return sourceContext.makeSourceLocation(this);
    }
    
	public int getEnd() {
		return end;
	}

	public ISourceContext getSourceContext() {
		return sourceContext;
	}

	public int getStart() {
		return start;
	}

	public void setPosition(int sourceStart, int sourceEnd) {
		this.start = sourceStart;
		this.end = sourceEnd;
	}

	public void setSourceContext(ISourceContext sourceContext) {
		this.sourceContext = sourceContext;
	}
	
	public boolean isAbstract() {
		return Modifier.isAbstract(modifiers);
	}

	public boolean isPublic() {
		return Modifier.isPublic(modifiers);
	}
    
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);   
    }
	
	public boolean isNative() {
		return Modifier.isNative(modifiers);
	}
    
    public boolean isDefault() {
        return !(isPublic() || isProtected() || isPrivate());
    }

	public boolean isVisible(ResolvedType fromType) {
		World world = fromType.getWorld();
		return ResolvedType.isVisible(getModifiers(), getDeclaringType().resolve(world),
					fromType);
	}
	public void setCheckedExceptions(UnresolvedType[] checkedExceptions) {
		this.checkedExceptions = checkedExceptions;
	}

	public void setAnnotatedElsewhere(boolean b) {
		isAnnotatedElsewhere = b;
	}

	public boolean isAnnotatedElsewhere() {
		return isAnnotatedElsewhere;
	}
	
	/**
	 * Get the UnresolvedType for the return type, taking generic signature into account
	 */
	public UnresolvedType getGenericReturnType() {
		return getReturnType();
	}
	
	/**
	 * Get the TypeXs of the parameter types, taking generic signature into account
	 */
	public UnresolvedType[] getGenericParameterTypes() {
		return getParameterTypes();
	}
	
	// return a resolved member in which all type variables in the signature of this
	// member have been replaced with the given bindings.
	// the isParameterized flag tells us whether we are creating a raw type version or not
	// if isParameterized List<T> will turn into List<String> (for example), 
	// but if !isParameterized List<T> will turn into List.
	public ResolvedMember parameterizedWith(UnresolvedType[] typeParameters,ResolvedType newDeclaringType, boolean isParameterized) {
		if (!this.getDeclaringType().isGenericType()) {
			throw new IllegalStateException("Can't ask to parameterize a member of a non-generic type");
		}
		TypeVariable[] typeVariables = getDeclaringType().getTypeVariables();
		if (typeVariables.length != typeParameters.length) {
			throw new IllegalStateException("Wrong number of type parameters supplied");
		}
		Map typeMap = new HashMap();
		for (int i = 0; i < typeVariables.length; i++) {
			typeMap.put(typeVariables[i].getName(), typeParameters[i]);
		}
		UnresolvedType parameterizedReturnType = parameterize(getGenericReturnType(),typeMap,isParameterized);
		UnresolvedType[] parameterizedParameterTypes = new UnresolvedType[getGenericParameterTypes().length];
		for (int i = 0; i < parameterizedParameterTypes.length; i++) {
			parameterizedParameterTypes[i] = 
				parameterize(getGenericParameterTypes()[i], typeMap,isParameterized);
		}
		return new ResolvedMember(
					getKind(),
					newDeclaringType,
					getModifiers(),
					parameterizedReturnType,
					getName(),
					parameterizedParameterTypes,
					getExceptions(),
					this
				);
	}
	
	
	public void setTypeVariables(UnresolvedType[] types) {
		typeVariables = types;
	}
	
	public UnresolvedType[] getTypeVariables() {
		return typeVariables;
	}
	
	private UnresolvedType parameterize(UnresolvedType aType, Map typeVariableMap, boolean inParameterizedType) {
		if (aType instanceof TypeVariableReferenceType) {
			String variableName = ((TypeVariableReferenceType)aType).getTypeVariable().getName();
			if (!typeVariableMap.containsKey(variableName)) {
				return aType; // if the type variable comes from the method (and not the type) thats OK
			}
			return (UnresolvedType) typeVariableMap.get(variableName);
		} else if (aType.isParameterizedType()) {
			if (inParameterizedType) {
				return aType.parameterize(typeVariableMap);
			} else {
				return aType.getRawType();
			}
		} 
		return aType;		
	}
	
	
	/**
	 * If this member is defined by a parameterized super-type, return the erasure
	 * of that member.
	 * For example:
	 * interface I<T> { T foo(T aTea); }
	 * class C implements I<String> {
	 *   String foo(String aString) { return "something"; }
	 * }
	 * The resolved member for C.foo has signature String foo(String). The
	 * erasure of that member is Object foo(Object)  -- use upper bound of type
	 * variable.
	 * A type is a supertype of itself.
	 */
	public ResolvedMember getErasure() {
		if (calculatedMyErasure) return myErasure;
		calculatedMyErasure = true;
		ResolvedType resolvedDeclaringType = (ResolvedType) getDeclaringType();
		// this next test is fast, and the result is cached.
		if (!resolvedDeclaringType.hasParameterizedSuperType()) {
			return null;
		} else {
			// we have one or more parameterized super types.
			// this member may be defined by one of them... we need to find out.
			Collection declaringTypes = this.getDeclaringTypes(resolvedDeclaringType.getWorld());
			for (Iterator iter = declaringTypes.iterator(); iter.hasNext();) {
				ResolvedType aDeclaringType = (ResolvedType) iter.next();
				if (aDeclaringType.isParameterizedType()) {
					// we've found the (a?) parameterized type that defines this member.
					// now get the erasure of it
					ResolvedMember matchingMember = aDeclaringType.lookupMemberNoSupers(this);
					if (matchingMember != null && matchingMember.backingGenericMember != null) {
						myErasure = matchingMember.backingGenericMember;
						return myErasure;
					}
				}
			}
		}
		return null;
	}
	
	private ResolvedMember myErasure = null;
	private boolean calculatedMyErasure = false;
}
   
