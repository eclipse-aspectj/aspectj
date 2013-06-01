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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.Pointcut;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;
import org.aspectj.weaver.tools.PointcutParameter;

/**
 * @author colyer Provides Java 5 behaviour in reflection based delegates (overriding 1.4 behaviour from superclass where
 *         appropriate)
 */
public class Java15ReflectionBasedReferenceTypeDelegate extends ReflectionBasedReferenceTypeDelegate {

	private AjType<?> myType;
	private ResolvedType[] annotations;
	private ResolvedMember[] pointcuts;
	private ResolvedMember[] methods;
	private ResolvedMember[] fields;
	private TypeVariable[] typeVariables;
	private ResolvedType superclass;
	private ResolvedType[] superInterfaces;
	private String genericSignature = null;
	private JavaLangTypeToResolvedTypeConverter typeConverter;
	private Java15AnnotationFinder annotationFinder = null;
	private ArgNameFinder argNameFinder = null;

	public Java15ReflectionBasedReferenceTypeDelegate() {
	}

	@Override
	public void initialize(ReferenceType aType, Class aClass, ClassLoader classLoader, World aWorld) {
		super.initialize(aType, aClass, classLoader, aWorld);
		myType = AjTypeSystem.getAjType(aClass);
		annotationFinder = new Java15AnnotationFinder();
		argNameFinder = annotationFinder;
		annotationFinder.setClassLoader(this.classLoaderReference.getClassLoader());
		annotationFinder.setWorld(aWorld);
		this.typeConverter = new JavaLangTypeToResolvedTypeConverter(aWorld);
	}

	@Override
	public ReferenceType buildGenericType() {
		return (ReferenceType) UnresolvedType.forGenericTypeVariables(getResolvedTypeX().getSignature(), getTypeVariables())
				.resolve(getWorld());
	}

	@Override
	public AnnotationAJ[] getAnnotations() {
		// AMC - we seem not to need to implement this method...
		// throw new UnsupportedOperationException(
		// "getAnnotations on Java15ReflectionBasedReferenceTypeDelegate is not implemented yet"
		// );
		// FIXME is this the right implementation in the reflective case?
		return super.getAnnotations();
	}

	@Override
	public ResolvedType[] getAnnotationTypes() {
		if (annotations == null) {
			annotations = annotationFinder.getAnnotations(getBaseClass(), getWorld());
		}
		return annotations;
	}

	@Override
	public boolean hasAnnotation(UnresolvedType ofType) {
		ResolvedType[] myAnns = getAnnotationTypes();
		ResolvedType toLookFor = ofType.resolve(getWorld());
		for (int i = 0; i < myAnns.length; i++) {
			if (myAnns[i] == toLookFor) {
				return true;
			}
		}
		return false;
	}

	// use the MAP to ensure that any aj-synthetic fields are filtered out
	@Override
	public ResolvedMember[] getDeclaredFields() {
		if (fields == null) {
			Field[] reflectFields = this.myType.getDeclaredFields();
			ResolvedMember[] rFields = new ResolvedMember[reflectFields.length];
			for (int i = 0; i < reflectFields.length; i++) {
				rFields[i] = createGenericFieldMember(reflectFields[i]);
			}
			this.fields = rFields;
		}
		return fields;
	}

	@Override
	public String getDeclaredGenericSignature() {
		if (this.genericSignature == null && isGeneric()) {
			// BUG? what the hell is this doing - see testcode in MemberTestCase15.testMemberSignatureCreation() and run it
			// off a Reflection World
		}
		return genericSignature;
	}

	@Override
	public ResolvedType[] getDeclaredInterfaces() {
		if (superInterfaces == null) {
			Type[] genericInterfaces = getBaseClass().getGenericInterfaces();
			this.superInterfaces = typeConverter.fromTypes(genericInterfaces);
		}
		return superInterfaces;
	}

	// If the superclass is null, return Object - same as bcel does
	@Override
	public ResolvedType getSuperclass() {
		if (superclass == null && getBaseClass() != Object.class) {// superclass
			// of Object
			// is null
			Type t = this.getBaseClass().getGenericSuperclass();
			if (t != null) {
				superclass = typeConverter.fromType(t);
			}
			if (t == null) {
				superclass = getWorld().resolve(UnresolvedType.OBJECT);
			}
		}
		return superclass;
	}

	@Override
	public TypeVariable[] getTypeVariables() {
		TypeVariable[] workInProgressSetOfVariables = getResolvedTypeX().getWorld().getTypeVariablesCurrentlyBeingProcessed(
				getBaseClass());
		if (workInProgressSetOfVariables != null) {
			return workInProgressSetOfVariables;
		}
		if (this.typeVariables == null) {
			java.lang.reflect.TypeVariable[] tVars = this.getBaseClass().getTypeParameters();
			TypeVariable[] rTypeVariables = new TypeVariable[tVars.length];
			// basic initialization
			for (int i = 0; i < tVars.length; i++) {
				rTypeVariables[i] = new TypeVariable(tVars[i].getName());
			}
			// stash it
			this.getResolvedTypeX().getWorld().recordTypeVariablesCurrentlyBeingProcessed(getBaseClass(), rTypeVariables);
			// now fill in the details...
			for (int i = 0; i < tVars.length; i++) {
				TypeVariableReferenceType tvrt = ((TypeVariableReferenceType) typeConverter.fromType(tVars[i]));
				TypeVariable tv = tvrt.getTypeVariable();
				rTypeVariables[i].setSuperclass(tv.getSuperclass());
				rTypeVariables[i].setAdditionalInterfaceBounds(tv.getSuperInterfaces());
				rTypeVariables[i].setDeclaringElement(tv.getDeclaringElement());
				rTypeVariables[i].setDeclaringElementKind(tv.getDeclaringElementKind());
				rTypeVariables[i].setRank(tv.getRank());
			}
			this.typeVariables = rTypeVariables;
			this.getResolvedTypeX().getWorld().forgetTypeVariablesCurrentlyBeingProcessed(getBaseClass());
		}
		return this.typeVariables;
	}

	// overrides super method since by using the MAP we can filter out advice
	// methods that really shouldn't be seen in this list
	@Override
	public ResolvedMember[] getDeclaredMethods() {
		if (methods == null) {
			Method[] reflectMethods = this.myType.getDeclaredMethods();
			Constructor[] reflectCons = this.myType.getDeclaredConstructors();
			ResolvedMember[] rMethods = new ResolvedMember[reflectMethods.length + reflectCons.length];
			for (int i = 0; i < reflectMethods.length; i++) {
				rMethods[i] = createGenericMethodMember(reflectMethods[i]);
			}
			for (int i = 0; i < reflectCons.length; i++) {
				rMethods[i + reflectMethods.length] = createGenericConstructorMember(reflectCons[i]);
			}
			this.methods = rMethods;
		}
		return methods;
	}

	/**
	 * Returns the generic type, regardless of the resolvedType we 'know about'
	 */
	public ResolvedType getGenericResolvedType() {
		ResolvedType rt = getResolvedTypeX();
		if (rt.isParameterizedType() || rt.isRawType()) {
			return rt.getGenericType();
		}
		return rt;
	}

	private ResolvedMember createGenericMethodMember(Method forMethod) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
				getGenericResolvedType(), forMethod.getModifiers(), typeConverter.fromType(forMethod.getReturnType()),
				forMethod.getName(), typeConverter.fromTypes(forMethod.getParameterTypes()), typeConverter.fromTypes(forMethod
						.getExceptionTypes()), forMethod);
		ret.setAnnotationFinder(this.annotationFinder);
		ret.setGenericSignatureInformationProvider(new Java15GenericSignatureInformationProvider(this.getWorld()));
		return ret;
	}

	private ResolvedMember createGenericConstructorMember(Constructor forConstructor) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
				getGenericResolvedType(), forConstructor.getModifiers(),
				// to return what BCEL returns the return type is void
				UnresolvedType.VOID,// getGenericResolvedType(),
				"<init>", typeConverter.fromTypes(forConstructor.getParameterTypes()), typeConverter.fromTypes(forConstructor
						.getExceptionTypes()), forConstructor);
		ret.setAnnotationFinder(this.annotationFinder);
		ret.setGenericSignatureInformationProvider(new Java15GenericSignatureInformationProvider(this.getWorld()));
		return ret;
	}

	private ResolvedMember createGenericFieldMember(Field forField) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.FIELD,
				getGenericResolvedType(), forField.getModifiers(), typeConverter.fromType(forField.getType()), forField.getName(),
				new UnresolvedType[0], forField);
		ret.setAnnotationFinder(this.annotationFinder);
		ret.setGenericSignatureInformationProvider(new Java15GenericSignatureInformationProvider(this.getWorld()));
		return ret;
	}

	@Override
	public ResolvedMember[] getDeclaredPointcuts() {
		if (pointcuts == null) {
			Pointcut[] pcs = this.myType.getDeclaredPointcuts();
			pointcuts = new ResolvedMember[pcs.length];
			InternalUseOnlyPointcutParser parser = null;
			World world = getWorld();
			if (world instanceof ReflectionWorld) {
				parser = new InternalUseOnlyPointcutParser(classLoaderReference.getClassLoader(), (ReflectionWorld) getWorld());
			} else {
				parser = new InternalUseOnlyPointcutParser(classLoaderReference.getClassLoader());
			}
			Set additionalPointcutHandlers = world.getRegisteredPointcutHandlers();
			for (Iterator handlerIterator = additionalPointcutHandlers.iterator(); handlerIterator.hasNext();) {
				PointcutDesignatorHandler handler = (PointcutDesignatorHandler) handlerIterator.next();
				parser.registerPointcutDesignatorHandler(handler);
			}

			// phase 1, create legitimate entries in pointcuts[] before we
			// attempt to resolve *any* of the pointcuts
			// resolution can sometimes cause us to recurse, and this two stage
			// process allows us to cope with that
			for (int i = 0; i < pcs.length; i++) {
				AjType<?>[] ptypes = pcs[i].getParameterTypes();
				UnresolvedType[] weaverPTypes = new UnresolvedType[ptypes.length];
				for (int j = 0; j < weaverPTypes.length; j++) {
					weaverPTypes[j] = this.typeConverter.fromType(ptypes[j].getJavaClass());
				}
				pointcuts[i] = new DeferredResolvedPointcutDefinition(getResolvedTypeX(), pcs[i].getModifiers(), pcs[i].getName(),
						weaverPTypes);
			}
			// phase 2, now go back round and resolve in-place all of the
			// pointcuts
			PointcutParameter[][] parameters = new PointcutParameter[pcs.length][];
			for (int i = 0; i < pcs.length; i++) {
				AjType<?>[] ptypes = pcs[i].getParameterTypes();
				String[] pnames = pcs[i].getParameterNames();
				if (pnames.length != ptypes.length) {
					pnames = tryToDiscoverParameterNames(pcs[i]);
					if (pnames == null || (pnames.length != ptypes.length)) {
						throw new IllegalStateException("Required parameter names not available when parsing pointcut "
								+ pcs[i].getName() + " in type " + getResolvedTypeX().getName());
					}
				}
				parameters[i] = new PointcutParameter[ptypes.length];
				for (int j = 0; j < parameters[i].length; j++) {
					parameters[i][j] = parser.createPointcutParameter(pnames[j], ptypes[j].getJavaClass());
				}
				String pcExpr = pcs[i].getPointcutExpression().toString();
				org.aspectj.weaver.patterns.Pointcut pc = parser.resolvePointcutExpression(pcExpr, getBaseClass(), parameters[i]);
				((ResolvedPointcutDefinition) pointcuts[i]).setParameterNames(pnames);
				((ResolvedPointcutDefinition) pointcuts[i]).setPointcut(pc);
			}
			// phase 3, now concretize them all
			for (int i = 0; i < pointcuts.length; i++) {
				ResolvedPointcutDefinition rpd = (ResolvedPointcutDefinition) pointcuts[i];
				rpd.setPointcut(parser.concretizePointcutExpression(rpd.getPointcut(), getBaseClass(), parameters[i]));
			}
		}
		return pointcuts;
	}

	// for @AspectJ pointcuts compiled by javac only...
	private String[] tryToDiscoverParameterNames(Pointcut pcut) {
		Method[] ms = pcut.getDeclaringType().getJavaClass().getDeclaredMethods();
		for (Method m : ms) {
			if (m.getName().equals(pcut.getName())) {
				return argNameFinder.getParameterNames(m);
			}
		}
		return null;
	}

	@Override
	public boolean isAnnotation() {
		return getBaseClass().isAnnotation();
	}

	@Override
	public boolean isAnnotationStyleAspect() {
		return getBaseClass().isAnnotationPresent(Aspect.class);
	}

	@Override
	public boolean isAnnotationWithRuntimeRetention() {
		if (!isAnnotation()) {
			return false;
		}
		if (getBaseClass().isAnnotationPresent(Retention.class)) {
			Retention retention = (Retention) getBaseClass().getAnnotation(Retention.class);
			RetentionPolicy policy = retention.value();
			return policy == RetentionPolicy.RUNTIME;
		} else {
			return false;
		}
	}

	@Override
	public boolean isAspect() {
		return this.myType.isAspect();
	}

	@Override
	public boolean isEnum() {
		return getBaseClass().isEnum();
	}

	@Override
	public boolean isGeneric() {
		// return false; // for now
		return getBaseClass().getTypeParameters().length > 0;
	}

	@Override
	public boolean isAnonymous() {
		return this.myClass.isAnonymousClass();
	}
	
	@Override
	public boolean isNested() {
		return this.myClass.isMemberClass();
	}

	@Override
	public ResolvedType getOuterClass() {
		 return ReflectionBasedReferenceTypeDelegateFactory.resolveTypeInWorld(
				 	myClass.getEnclosingClass(),world); 
	}

}
