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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.Pointcut;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BoundedReferenceType;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.internal.tools.PointcutExpressionImpl;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;

/**
 * @author colyer
 * Provides Java 5 behaviour in reflection based delegates (overriding
 * 1.4 behaviour from superclass where appropriate)
 */
public class Java15ReflectionBasedReferenceTypeDelegate extends
		ReflectionBasedReferenceTypeDelegate {
	
	private AjType myType;
	private ResolvedType[] annotations;
	private ResolvedMember[] pointcuts;
	private ResolvedMember[] methods;
	private ResolvedMember[] fields;
	private TypeVariable[] typeVariables;
	private ResolvedType superclass;
	private ResolvedType[] superInterfaces;
	private String genericSignature = null;
	private Java15AnnotationFinder annotationFinder = null;
	

	public Java15ReflectionBasedReferenceTypeDelegate() {}
	
	@Override
	public void initialize(ReferenceType aType, Class aClass, ClassLoader classLoader, World aWorld) {
		super.initialize(aType, aClass, classLoader, aWorld);
		myType = AjTypeSystem.getAjType(aClass);
		annotationFinder = new Java15AnnotationFinder();
		annotationFinder.setClassLoader(classLoader);
	}
	
	
	public ReferenceType buildGenericType() {
	   	return (ReferenceType) UnresolvedType.forGenericTypeVariables(
	   				getResolvedTypeX().getSignature(), 
	   				getTypeVariables()).resolve(getWorld());	   	
	}
	
	public AnnotationX[] getAnnotations() {
		// AMC - we seem not to need to implement this method...
	 	//throw new UnsupportedOperationException("getAnnotations on Java15ReflectionBasedReferenceTypeDelegate is not implemented yet");
	    // FIXME is this the right implementation in the reflective case?
		return super.getAnnotations();
	}
	
	public ResolvedType[] getAnnotationTypes() {
		if (annotations == null) {
			annotations = annotationFinder.getAnnotations(getBaseClass(), getWorld());
		}
		return annotations;
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		ResolvedType[] myAnns = getAnnotationTypes();
		ResolvedType toLookFor = ofType.resolve(getWorld());
		for (int i = 0; i < myAnns.length; i++) {
			if (myAnns[i] == toLookFor) return true;
		}
		return false;
	}
	
	// use the MAP to ensure that any aj-synthetic fields are filtered out
	public ResolvedMember[] getDeclaredFields() {
		if (fields == null) {
			Field[] reflectFields = this.myType.getDeclaredFields();
			this.fields = new ResolvedMember[reflectFields.length];
			for (int i = 0; i < reflectFields.length; i++) {
				this.fields[i] = createGenericFieldMember(reflectFields[i]);
			}
		}
		return fields;
	}
	
	public String getDeclaredGenericSignature() {
		if (this.genericSignature == null && isGeneric()) {
			
		}
		return genericSignature;
	}
	
	public ResolvedType[] getDeclaredInterfaces() {
		if (superInterfaces == null) {
			Type[] genericInterfaces = getBaseClass().getGenericInterfaces();
			this.superInterfaces = fromTypes(genericInterfaces);
		}
		return superInterfaces;
	}
	
	public ResolvedType getSuperclass() {
		if (superclass == null && getBaseClass()!=Object.class) // superclass of Object is null
		  superclass = fromType(this.getBaseClass().getGenericSuperclass());
		 return superclass;
	}
	
	
	public TypeVariable[] getTypeVariables() {
		TypeVariable[] workInProgressSetOfVariables = (TypeVariable[])getResolvedTypeX().getWorld().getTypeVariablesCurrentlyBeingProcessed(getBaseClass());
		if (workInProgressSetOfVariables!=null) {
			return workInProgressSetOfVariables;
		}
		if (this.typeVariables == null) {
			java.lang.reflect.TypeVariable[] tVars = this.getBaseClass().getTypeParameters();
			this.typeVariables = new TypeVariable[tVars.length];
			// basic initialization
			for (int i = 0; i < tVars.length; i++) {
				typeVariables[i] = new TypeVariable(tVars[i].getName());
			}
			// stash it
			this.getResolvedTypeX().getWorld().recordTypeVariablesCurrentlyBeingProcessed(getBaseClass(),typeVariables);
			// now fill in the details...
			for (int i = 0; i < tVars.length; i++) {
				TypeVariableReferenceType tvrt = ((TypeVariableReferenceType) fromType(tVars[i]));
				TypeVariable tv = tvrt.getTypeVariable();
				typeVariables[i].setUpperBound(tv.getUpperBound());
				typeVariables[i].setAdditionalInterfaceBounds(tv.getAdditionalInterfaceBounds());
				typeVariables[i].setDeclaringElement(tv.getDeclaringElement());
				typeVariables[i].setDeclaringElementKind(tv.getDeclaringElementKind());
				typeVariables[i].setRank(tv.getRank());
				typeVariables[i].setLowerBound(tv.getLowerBound());
			}
			this.getResolvedTypeX().getWorld().forgetTypeVariablesCurrentlyBeingProcessed(getBaseClass());
		}		
		return this.typeVariables;
	}

	// overrides super method since by using the MAP we can filter out advice
	// methods that really shouldn't be seen in this list
	public ResolvedMember[] getDeclaredMethods() {
		if (methods == null) {
			Method[] reflectMethods = this.myType.getDeclaredMethods();
			Constructor[] reflectCons = this.myType.getDeclaredConstructors();
			this.methods = new ResolvedMember[reflectMethods.length + reflectCons.length];
			for (int i = 0; i < reflectMethods.length; i++) {
				this.methods[i] = createGenericMethodMember(reflectMethods[i]); 					
			}
			for (int i = 0; i < reflectCons.length; i++) {
				this.methods[i + reflectMethods.length] = 
					createGenericConstructorMember(reflectCons[i]);
			}
		}
		return methods;
	}
	
	/**
	 * Returns the generic type, regardless of the resolvedType we 'know about'
	 */
	public ResolvedType getGenericResolvedType() {
		ResolvedType rt = getResolvedTypeX();
		if (rt.isParameterizedType() || rt.isRawType()) return rt.getGenericType();
		return rt;
	}
	
	private ResolvedMember createGenericMethodMember(Method forMethod) {
		ReflectionBasedResolvedMemberImpl ret = 
		new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
			getGenericResolvedType(),
			forMethod.getModifiers(),
			fromType(forMethod.getGenericReturnType()),
			forMethod.getName(),
			fromTypes(forMethod.getGenericParameterTypes()),
			fromTypes(forMethod.getGenericExceptionTypes()),
			forMethod
			);
		ret.setAnnotationFinder(this.annotationFinder);
		return ret;
	}

	private ResolvedMember createGenericConstructorMember(Constructor forConstructor) {
		ReflectionBasedResolvedMemberImpl ret = 
		new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
			getGenericResolvedType(),
			forConstructor.getModifiers(),
			getGenericResolvedType(),
			"init",
			fromTypes(forConstructor.getGenericParameterTypes()),
			fromTypes(forConstructor.getGenericExceptionTypes()),
			forConstructor
			);
		ret.setAnnotationFinder(this.annotationFinder);
		return ret;
	}
	
	private ResolvedMember createGenericFieldMember(Field forField) {
		ReflectionBasedResolvedMemberImpl ret =
			new ReflectionBasedResolvedMemberImpl(
				org.aspectj.weaver.Member.FIELD,
				getGenericResolvedType(),
				forField.getModifiers(),
				fromType(forField.getGenericType()),
				forField.getName(),
				new UnresolvedType[0],
				forField);
		ret.setAnnotationFinder(this.annotationFinder);
		return ret;
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		if (pointcuts == null) {
			Pointcut[] pcs = this.myType.getDeclaredPointcuts();
			pointcuts = new ResolvedMember[pcs.length];
			PointcutParser parser = new PointcutParser();
			for (int i = 0; i < pcs.length; i++) {
				AjType<?>[] ptypes = pcs[i].getParameterTypes();
				String[] pnames = pcs[i].getParameterNames();
				if (pnames.length != ptypes.length) {
					throw new IllegalStateException("Required parameter names not available when parsing pointcut " + pcs[i].getName() + " in type " + getResolvedTypeX().getName());
				}
				PointcutParameter[] parameters = new PointcutParameter[ptypes.length];
				for (int j = 0; j < parameters.length; j++) {
					parameters[j] = parser.createPointcutParameter(pnames[j],ptypes[j].getJavaClass());
				}
				String pcExpr = pcs[i].getPointcutExpression().toString();
				PointcutExpressionImpl pEx = (PointcutExpressionImpl) parser.parsePointcutExpression(pcExpr,getBaseClass(),parameters);
				org.aspectj.weaver.patterns.Pointcut pc = pEx.getUnderlyingPointcut();
				UnresolvedType[] weaverPTypes = new UnresolvedType[ptypes.length];
				for (int j = 0; j < weaverPTypes.length; j++) {
					weaverPTypes[j] = UnresolvedType.forName(ptypes[j].getName());
				}
				pointcuts[i] = new ResolvedPointcutDefinition(getResolvedTypeX(),pcs[i].getModifiers(),pcs[i].getName(),weaverPTypes,pc);
			}
		}
		return pointcuts;
	}
	
	public boolean isAnnotation() {
		return getBaseClass().isAnnotation();
	}
	
	public boolean isAnnotationStyleAspect() {
		return getBaseClass().isAnnotationPresent(Aspect.class);
	}
	
	public boolean isAnnotationWithRuntimeRetention() {
		if (!isAnnotation()) return false;
		if (getBaseClass().isAnnotationPresent(Retention.class)) {
			Retention retention = (Retention) getBaseClass().getAnnotation(Retention.class);
			RetentionPolicy policy = retention.value();
			return policy == RetentionPolicy.RUNTIME;
		} else {
			return false;
		}
	}
	
	public boolean isAspect() {
		return this.myType.isAspect();
	}
	
	public boolean isEnum() {
		return getBaseClass().isEnum();
	}
	
	public boolean isGeneric() {
		//return false; // for now
		return getBaseClass().getTypeParameters().length > 0;
	}
	
	// Used to prevent recursion - we record what we are working on and return it if asked again *whilst* working on it
	private Map /*java.lang.reflect.TypeVariable > TypeVariableReferenceType */typeVariablesInProgress = new HashMap();
	
	private ResolvedType fromType(Type aType) {
		if (aType instanceof Class) {
			Class clazz = (Class)aType;
			String name = clazz.getName();
			/**
			 * getName() can return:
			 * 
			 * 1. If this class object represents a reference type that is not an array type 
			 *    then the binary name of the class is returned
			 * 2. If this class object represents a primitive type or void, then the 
			 *    name returned is a String equal to the Java language keyword corresponding to the primitive type or void.
			 * 3. If this class object represents a class of arrays, then the internal form 
			 *    of the name consists of the name of the element type preceded by one or more '[' characters representing the depth of the array nesting.
			 */
			if (clazz.isArray()) {
				UnresolvedType ut = UnresolvedType.forSignature(name);
				return getWorld().resolve(ut);
			} else {
				return getWorld().resolve(name);
			}
		} else if (aType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) aType;
			ResolvedType baseType = fromType(pt.getRawType());
			Type[] args = pt.getActualTypeArguments();
			ResolvedType[] resolvedArgs = fromTypes(args);
			return TypeFactory.createParameterizedType(baseType, resolvedArgs, getWorld());
		} else if (aType instanceof java.lang.reflect.TypeVariable) {
			if (typeVariablesInProgress.get(aType)!=null) // check if we are already working on this type
				return (TypeVariableReferenceType)typeVariablesInProgress.get(aType);

			java.lang.reflect.TypeVariable tv = (java.lang.reflect.TypeVariable) aType;
			TypeVariable rt_tv = new TypeVariable(tv.getName());
			TypeVariableReferenceType tvrt = new TypeVariableReferenceType(rt_tv,getWorld());
			
			typeVariablesInProgress.put(aType,tvrt); // record what we are working on, for recursion case
			
			Type[] bounds = tv.getBounds();
			ResolvedType[] resBounds = fromTypes(bounds);
			ResolvedType upperBound = resBounds[0];
			ResolvedType[] additionalBounds = new ResolvedType[0];
			if (resBounds.length > 1) {
				additionalBounds = new ResolvedType[resBounds.length - 1];
				System.arraycopy(resBounds,1,additionalBounds,0,additionalBounds.length);
			}
			rt_tv.setUpperBound(upperBound);
			rt_tv.setAdditionalInterfaceBounds(additionalBounds);
			
			typeVariablesInProgress.remove(aType); // we have finished working on it
			
			return tvrt;
		} else if (aType instanceof WildcardType) {
			WildcardType wildType = (WildcardType) aType;
			Type[] lowerBounds = wildType.getLowerBounds();
			Type[] upperBounds = wildType.getUpperBounds();
			ResolvedType bound = null;
			boolean isExtends = lowerBounds.length == 0;
			if (isExtends) {
				bound = fromType(upperBounds[0]);
			} else {
				bound = fromType(lowerBounds[0]);
			}
			return new BoundedReferenceType((ReferenceType)bound,isExtends,getWorld());
		} else if (aType instanceof GenericArrayType) {
			GenericArrayType gt = (GenericArrayType) aType;
			Type componentType = gt.getGenericComponentType();
			return UnresolvedType.makeArray(fromType(componentType),1).resolve(getWorld());
		}
		return ResolvedType.MISSING;
	}
	
	private ResolvedType[] fromTypes(Type[] types) {
		ResolvedType[] ret = new ResolvedType[types.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = fromType(types[i]);
		}
		return ret;
	}

	@Override
	public boolean isAnonymous() {		
		return this.myClass.isAnonymousClass();
	}

}

