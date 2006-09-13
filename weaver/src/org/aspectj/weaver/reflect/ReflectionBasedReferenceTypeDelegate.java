/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;

import org.aspectj.weaver.AnnotationTargetKind;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.SourceContextImpl;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.PerClause;

/**
 * @author colyer
 * A delegate for a resolved type that uses runtime type information (java.lang.reflect)
 * to answer questions. This class uses only Java 1.4 features to answer questions.
 * In a Java 1.5 environment use the Java5ReflectionBasedReferenceTypeDelegate subtype.
 */
public class ReflectionBasedReferenceTypeDelegate implements ReferenceTypeDelegate {

	private static final ClassLoader BootClassLoader = new URLClassLoader(new URL[0]);
	
	protected Class myClass = null;
	protected ClassLoader classLoader = null;
	private World world;
	private ReferenceType resolvedType;
	private ResolvedMember[] fields = null;
	private ResolvedMember[] methods = null;
	private ResolvedType[] interfaces = null;
	
	public ReflectionBasedReferenceTypeDelegate(Class forClass, ClassLoader aClassLoader, World inWorld, ReferenceType resolvedType) {
		initialize(resolvedType,forClass, aClassLoader, inWorld);
	}
	
	/** for reflective construction only */
	public ReflectionBasedReferenceTypeDelegate() {}
	
	public void initialize(ReferenceType aType, Class aClass, ClassLoader aClassLoader, World aWorld) {
		this.myClass = aClass;
		this.resolvedType = aType;
		this.world = aWorld;
		this.classLoader = (aClassLoader != null) ? aClassLoader : BootClassLoader;
	}
	
	protected Class getBaseClass() { 
		return this.myClass;
	}
	
	protected World getWorld() {
		return this.world;
	}
		
	public ReferenceType buildGenericType() {
		throw new UnsupportedOperationException("Shouldn't be asking for generic type at 1.4 source level or lower");
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#addAnnotation(org.aspectj.weaver.AnnotationX)
	 */
	public void addAnnotation(AnnotationX annotationX) {
		throw new UnsupportedOperationException("Cannot add an annotation to a reflection based delegate");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isAspect()
	 */
	public boolean isAspect() {
		// we could do better than this in Java 5 by looking at the annotations on the type...
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isAnnotationStyleAspect()
	 */
	public boolean isAnnotationStyleAspect() {
		// we could do better than this in Java 5 by looking at the annotations on the type...
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isInterface()
	 */
	public boolean isInterface() {
		return this.myClass.isInterface();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isEnum()
	 */
	public boolean isEnum() {
		// cant be an enum in Java 1.4 or prior
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isAnnotation()
	 */
	public boolean isAnnotation() {
		// cant be an annotation in Java 1.4 or prior
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isAnnotationWithRuntimeRetention()
	 */
	public boolean isAnnotationWithRuntimeRetention() {
		// cant be an annotation in Java 1.4 or prior
		return false;
	}
	
	public String getRetentionPolicy() {
		// cant be an annotation in Java 1.4 or prior
		return null;
	}

	public boolean canAnnotationTargetType() {
		return false;
	}
	
	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isClass()
	 */
	public boolean isClass() {
		return !this.myClass.isInterface() && !this.myClass.isPrimitive() && !this.myClass.isArray();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isGeneric()
	 */
	public boolean isGeneric() {
		// cant be generic in 1.4
		return false;
	}
	
	public boolean isAnonymous() {
		return false;
	}
	
	public boolean isNested() {
		// FIXME this is *wrong* but isMemberClass() doesnt exist in pre-1.5... (same deal as isAnonymous above...)
		return true;
//		boolean member = this.myClass.isMemberClass();
//		return member;
	}
	
	public ResolvedType getOuterClass() {
		// FIXME getEnclosingClass() is Java5 ... dammit
//		return ReflectionBasedReferenceTypeDelegateFactory.resolveTypeInWorld(myClass.getEnclosingClass(),world);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#isExposedToWeaver()
	 */
	public boolean isExposedToWeaver() {
		// reflection based types are never exposed to the weaver
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#hasAnnotation(org.aspectj.weaver.UnresolvedType)
	 */
	public boolean hasAnnotation(UnresolvedType ofType) {
		// in Java 1.4 we cant have an annotation
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getAnnotations()
	 */
	public AnnotationX[] getAnnotations() {
		// no annotations in Java 1.4
		return new AnnotationX[0];
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getAnnotationTypes()
	 */
	public ResolvedType[] getAnnotationTypes() {
		// no annotations in Java 1.4
		return new ResolvedType[0];
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getDeclaredFields()
	 */
	public ResolvedMember[] getDeclaredFields() {
		if (fields == null) {
			Field[] reflectFields = this.myClass.getDeclaredFields();
			this.fields = new ResolvedMember[reflectFields.length];
			for (int i = 0; i < reflectFields.length; i++) {
				this.fields[i] = 
					ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(reflectFields[i], world);
			}
		}
		return fields;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getDeclaredInterfaces()
	 */
	public ResolvedType[] getDeclaredInterfaces() {
		if (interfaces == null) {
			Class[] reflectInterfaces = this.myClass.getInterfaces();
			this.interfaces = new ResolvedType[reflectInterfaces.length];
			for (int i = 0; i < reflectInterfaces.length; i++) {
				this.interfaces[i] = ReflectionBasedReferenceTypeDelegateFactory
					.resolveTypeInWorld(reflectInterfaces[i],world);
			}
		}
		return interfaces;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getDeclaredMethods()
	 */
	public ResolvedMember[] getDeclaredMethods() {
		if (methods == null) {
			Method[] reflectMethods = this.myClass.getDeclaredMethods();
			Constructor[] reflectCons = this.myClass.getDeclaredConstructors();
			this.methods = new ResolvedMember[reflectMethods.length + reflectCons.length];
			for (int i = 0; i < reflectMethods.length; i++) {
				this.methods[i] = 
					ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(reflectMethods[i], world);
			}
			for (int i = 0; i < reflectCons.length; i++) {
				this.methods[i + reflectMethods.length] = 
					ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(reflectCons[i], world);
			}
		}
		return methods;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getDeclaredPointcuts()
	 */
	public ResolvedMember[] getDeclaredPointcuts() {
		return new ResolvedMember[0];
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getTypeVariables()
	 */
	public TypeVariable[] getTypeVariables() {
		// no type variables in Java 1.4
		return new TypeVariable[0];
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getPerClause()
	 */
	public PerClause getPerClause() {
		// no per clause...
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getDeclares()
	 */
	public Collection getDeclares() {
		// no declares
		return Collections.EMPTY_SET;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getTypeMungers()
	 */
	public Collection getTypeMungers() {
		// no type mungers
		return Collections.EMPTY_SET;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getPrivilegedAccesses()
	 */
	public Collection getPrivilegedAccesses() {
		// no aspect members..., not used for weaving
		return Collections.EMPTY_SET;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getModifiers()
	 */
	public int getModifiers() {
		return this.myClass.getModifiers();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getSuperclass()
	 */
	public ResolvedType getSuperclass() {
		if (this.myClass.getSuperclass() == null) {
			if (myClass==Object.class) {
				return null;
			}
			return world.resolve(UnresolvedType.OBJECT);
		}
		return ReflectionBasedReferenceTypeDelegateFactory
				.resolveTypeInWorld(this.myClass.getSuperclass(),world);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getWeaverState()
	 */
	public WeaverStateInfo getWeaverState() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getResolvedTypeX()
	 */
	public ReferenceType getResolvedTypeX() {
		return this.resolvedType;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#doesNotExposeShadowMungers()
	 */
	public boolean doesNotExposeShadowMungers() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ReferenceTypeDelegate#getDeclaredGenericSignature()
	 */
	public String getDeclaredGenericSignature() {
		// no generic sig in 1.4
		return null;
	}

	public void ensureDelegateConsistent() {
		// Nothing to do - a reflection based delegate can't become inconsistent...
	}
	
	public ReflectionBasedResolvedMemberImpl createResolvedMemberFor(Member aMember) {
		return null;
	}
	
	public String getSourcefilename() {
      // crappy guess..
      return resolvedType.getName() + ".class";
	}

	public ISourceContext getSourceContext() {
		return SourceContextImpl.UNKNOWN_SOURCE_CONTEXT;
	}

}
