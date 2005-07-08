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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
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
public class ResolvedMember extends Member implements IHasPosition, AnnotatedElement {
    
    public String[] parameterNames = null;
    protected TypeX[] checkedExceptions = TypeX.NONE;
    private Set annotationTypes = null;
	// Some members are 'created' to represent other things (for example ITDs).  These
	// members have their annotations stored elsewhere, and this flag indicates that is
	// the case.  It is up to the caller to work out where that is!
	// Once determined the caller may choose to stash the annotations in this member...
	private boolean isAnnotatedElsewhere = false; // this field is not serialized.
    
    
    // these three fields hold the source location of this member
	protected int start, end;
	protected ISourceContext sourceContext = null;
    
    //XXX deprecate this in favor of the constructor below
	public ResolvedMember(
		Kind kind,
		TypeX declaringType,
		int modifiers,
		TypeX returnType,
		String name,
		TypeX[] parameterTypes)
	{
		super(kind, declaringType, modifiers, returnType, name, parameterTypes);
	}

    
    
	public ResolvedMember(
		Kind kind,
		TypeX declaringType,
		int modifiers,
		TypeX returnType,
		String name,
		TypeX[] parameterTypes,
		TypeX[] checkedExceptions) 
	{
		super(kind, declaringType, modifiers, returnType, name, parameterTypes);
		this.checkedExceptions = checkedExceptions;
	}
    
	public ResolvedMember(
		Kind kind,
		TypeX declaringType,
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
	

    public final TypeX[] getExceptions(World world) {
        return getExceptions();
    }
    
    public TypeX[] getExceptions() {
        return checkedExceptions;
    }
    
    public ShadowMunger getAssociatedShadowMunger() {
		return null;
    }
    
    // ??? true or false?
    public boolean isAjSynthetic() {
    	return true;
    }
	
	public boolean hasAnnotations() {
		return  (annotationTypes==null);
	}

    public boolean hasAnnotation(TypeX ofType) {
        // The ctors don't allow annotations to be specified ... yet - but
        // that doesn't mean it is an error to call this method.
        // Normally the weaver will be working with subtypes of 
        // this type - BcelField/BcelMethod
        if (annotationTypes==null) return false;
		return annotationTypes.contains(ofType);
    }
    
    public ResolvedTypeX[] getAnnotationTypes() {
    	// The ctors don't allow annotations to be specified ... yet - but
    	// that doesn't mean it is an error to call this method.
    	// Normally the weaver will be working with subtypes of
    	// this type - BcelField/BcelMethod
    	if (annotationTypes == null) return null;
		return (ResolvedTypeX[])annotationTypes.toArray(new ResolvedTypeX[]{});
    }
    
	public void setAnnotationTypes(TypeX[] annotationtypes) {
		if (annotationTypes == null) annotationTypes = new HashSet();
		for (int i = 0; i < annotationtypes.length; i++) {
			TypeX typeX = annotationtypes[i];
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
		TypeX.writeArray(getExceptions(), s);

		s.writeInt(getStart());
		s.writeInt(getEnd());

    }

    public static void writeArray(ResolvedMember[] members, DataOutputStream s) throws IOException {
		s.writeInt(members.length);
		for (int i = 0, len = members.length; i < len; i++) {
			members[i].write(s);
		}
    }

    
    public static ResolvedMember readResolvedMember(DataInputStream s, ISourceContext sourceContext) throws IOException {
    	ResolvedMember m = new ResolvedMember(Kind.read(s), TypeX.read(s), s.readInt(), s.readUTF(), s.readUTF());
		m.checkedExceptions = TypeX.readArray(s);
		m.start = s.readInt();
		m.end = s.readInt();
		m.sourceContext = sourceContext;
		return m;
    }
    
    public static ResolvedMember[] readResolvedMemberArray(DataInputStream s, ISourceContext context) throws IOException {
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
			TypeX element = (TypeX) iter.next();
			r.add(world.resolve(element));
		  }
		  annotationTypes = r;
	    }
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

	public boolean isVisible(ResolvedTypeX fromType) {
		World world = fromType.getWorld();
		return ResolvedTypeX.isVisible(getModifiers(), getDeclaringType().resolve(world),
					fromType);
	}
	public void setCheckedExceptions(TypeX[] checkedExceptions) {
		this.checkedExceptions = checkedExceptions;
	}

	public void setAnnotatedElsewhere(boolean b) {
		isAnnotatedElsewhere = b;
	}

	public boolean isAnnotatedElsewhere() {
		return isAnnotatedElsewhere;
	}
	
	/**
	 * Get the TypeX for the return type, taking generic signature into account
	 */
	public TypeX getGenericReturnType() {
		return getReturnType();
	}
	
	/**
	 * Get the TypeXs of the parameter types, taking generic signature into account
	 */
	public TypeX[] getGenericParameterTypes() {
		return getParameterTypes();
	}
	
	// return a resolved member in which all type variables in the signature of this
	// member have been replaced with the given bindings.
	public ResolvedMember parameterizedWith(TypeX[] typeParameters) {
		if (!this.getDeclaringType().isGeneric()) {
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
		TypeX parameterizedReturnType = parameterize(getGenericReturnType(),typeMap);
		TypeX[] parameterizedParameterTypes = new TypeX[getGenericParameterTypes().length];
		for (int i = 0; i < parameterizedParameterTypes.length; i++) {
			parameterizedParameterTypes[i] = 
				parameterize(getGenericParameterTypes()[i], typeMap);
		}
		return new ResolvedMember(
					getKind(),
					getDeclaringType(),
					getModifiers(),
					parameterizedReturnType,
					getName(),
					parameterizedParameterTypes,
					getExceptions()
				);
	}
	
	private TypeX parameterize(TypeX aType, Map typeVariableMap) {
		if (aType instanceof TypeVariableReferenceType) {
			String variableName = ((TypeVariableReferenceType)aType).getTypeVariable().getName();
			if (!typeVariableMap.containsKey(variableName)) {
				throw new IllegalStateException("Type variable " + variableName + " not bound in type map");
			}
			return (TypeX) typeVariableMap.get(variableName);
		} else {
			return aType;
		}
	}
}
   
