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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

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
import org.aspectj.weaver.UnresolvedType.TypeKind;
import org.aspectj.weaver.internal.tools.PointcutExpressionImpl;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.tools.PointcutExpression;
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
	private Java15AnnotationFinder annotationFinder = new Java15AnnotationFinder();
	

	public Java15ReflectionBasedReferenceTypeDelegate() {}
	
	public void initialize(ReferenceType aType, Class aClass, World aWorld) {
		super.initialize(aType, aClass, aWorld);
		myType = AjTypeSystem.getAjType(aClass);
	}
	
	
	public ReferenceType buildGenericType() {
	   	return (ReferenceType) UnresolvedType.forGenericTypeVariables(
	   				getResolvedTypeX().getSignature(), 
	   				getTypeVariables()).resolve(getWorld());	   	
	}
	
	public AnnotationX[] getAnnotations() {
		// AMC - we seem not to need to implement this method...
		throw new UnsupportedOperationException("getAnnotations on Java15ReflectionBasedReferenceTypeDelegate is not implemented yet");
		//return super.getAnnotations();
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
		if (superclass == null)
		  superclass = fromType(this.getBaseClass().getGenericSuperclass());
		 return superclass;
	}
	
	public TypeVariable[] getTypeVariables() {
		if (this.typeVariables == null) {
			java.lang.reflect.TypeVariable[] tVars = this.getBaseClass().getTypeParameters();
			this.typeVariables = new TypeVariable[tVars.length];
			for (int i = 0; i < tVars.length; i++) {
				this.typeVariables[i] = ((TypeVariableReferenceType) fromType(tVars[i])).getTypeVariable();
			}
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
	
	private ResolvedMember createGenericMethodMember(Method forMethod) {
		ReflectionBasedResolvedMemberImpl ret = 
		new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
			getResolvedTypeX(),
			forMethod.getModifiers(),
			fromType(forMethod.getGenericReturnType()),
			forMethod.getName(),
			fromTypes(forMethod.getGenericParameterTypes()),
			fromTypes(forMethod.getGenericExceptionTypes()),
			forMethod
			);
		return ret;
	}

	private ResolvedMember createGenericConstructorMember(Constructor forConstructor) {
		ReflectionBasedResolvedMemberImpl ret = 
		new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
			getResolvedTypeX(),
			forConstructor.getModifiers(),
			getResolvedTypeX(),
			"init",
			fromTypes(forConstructor.getGenericParameterTypes()),
			fromTypes(forConstructor.getGenericExceptionTypes()),
			forConstructor
			);
		return ret;
	}
	
	private ResolvedMember createGenericFieldMember(Field forField) {
		return new ReflectionBasedResolvedMemberImpl(
				org.aspectj.weaver.Member.FIELD,
				getResolvedTypeX(),
				forField.getModifiers(),
				fromType(forField.getGenericType()),
				forField.getName(),
				new UnresolvedType[0],
				forField);
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		if (pointcuts == null) {
			Pointcut[] pcs = this.myType.getDeclaredPointcuts();
			pointcuts = new ResolvedMember[pcs.length];
			PointcutParser parser = new PointcutParser();
			for (int i = 0; i < pcs.length; i++) {
				Class[] ptypes = pcs[i].getParameterTypes();
				String[] pnames = pcs[i].getParameterNames();
				if (pnames.length != ptypes.length) {
					throw new IllegalStateException("Required parameter names not available when parsing pointcut " + pcs[i].getName() + " in type " + getResolvedTypeX().getName());
				}
				PointcutParameter[] parameters = new PointcutParameter[ptypes.length];
				for (int j = 0; j < parameters.length; j++) {
					parameters[j] = parser.createPointcutParameter(pnames[j],ptypes[j]);
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
	
	private ResolvedType fromType(Type aType) {
		if (aType instanceof Class) {
			return getWorld().resolve(((Class)aType).getName());
		} else if (aType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) aType;
			ResolvedType baseType = fromType(pt.getRawType());
			Type[] args = pt.getActualTypeArguments();
			ResolvedType[] resolvedArgs = fromTypes(args);
			return TypeFactory.createParameterizedType(baseType, resolvedArgs, getWorld());
		} else if (aType instanceof java.lang.reflect.TypeVariable) {
			java.lang.reflect.TypeVariable tv = (java.lang.reflect.TypeVariable) aType;
			Type[] bounds = tv.getBounds();
			ResolvedType[] resBounds = fromTypes(bounds);
			ResolvedType upperBound = resBounds[0];
			ResolvedType[] additionalBounds = new ResolvedType[0];
			if (resBounds.length > 1) {
				additionalBounds = new ResolvedType[resBounds.length - 1];
				System.arraycopy(resBounds,1,additionalBounds,0,additionalBounds.length);
			}
			TypeVariable rt_tv = new TypeVariable(tv.getName(),upperBound,additionalBounds);
			return new TypeVariableReferenceType(rt_tv,getWorld());
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
			UnresolvedType.makeArray(fromType(componentType),1);
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

}

