/* *******************************************************************
 * Copyright (c) 2005, 2017 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.aspectj.apache.bcel.classfile.AnnotationDefault;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.util.ClassLoaderRepository;
import org.aspectj.apache.bcel.util.NonCachingClassLoaderRepository;
import org.aspectj.apache.bcel.util.Repository;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelAnnotation;
import org.aspectj.weaver.bcel.BcelWeakClassLoaderReference;

/**
 *
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class Java15AnnotationFinder implements AnnotationFinder, ArgNameFinder {

	public static final ResolvedType[][] NO_PARAMETER_ANNOTATIONS = new ResolvedType[][] {};

	private Repository bcelRepository;
	private BcelWeakClassLoaderReference classLoaderRef;

	private static Repository staticBcelRepository;
	private static BcelWeakClassLoaderReference staticClassLoaderRef;

	private World world;
	private static boolean useCachingClassLoaderRepository;

	//Use single instance of Repository and ClassLoader
	public static final boolean useSingleInstances =
		System.getProperty("org.aspectj.apache.bcel.useSingleRepositoryInstance", "true").equalsIgnoreCase("true");

	static {
		try {
			useCachingClassLoaderRepository = System.getProperty("Xset:bcelRepositoryCaching","true").equalsIgnoreCase("true");
		} catch (Throwable t) {
			useCachingClassLoaderRepository = false;
		}
	}

	// must have no-arg constructor for reflective construction
	public Java15AnnotationFinder() {
	}

	public void setClassLoader(ClassLoader aLoader) {
		//Set class loader ref
		if (useSingleInstances && staticClassLoaderRef == null)
			staticClassLoaderRef = new BcelWeakClassLoaderReference(aLoader);
		else
			this.classLoaderRef = new BcelWeakClassLoaderReference(aLoader);

		//Set repository
		if (useCachingClassLoaderRepository) {
			if (useSingleInstances && staticBcelRepository == null)
				staticBcelRepository = new ClassLoaderRepository(getClassLoader());
			else
				this.bcelRepository = new ClassLoaderRepository(getClassLoader());
		}
		else {
			if (useSingleInstances && staticBcelRepository == null)
				staticBcelRepository = new NonCachingClassLoaderRepository(getClassLoader());
			else
				this.bcelRepository = new NonCachingClassLoaderRepository(getClassLoader());
		}
	}

	private Repository getBcelRepository() {
		return useSingleInstances ? staticBcelRepository : bcelRepository;
	}

	public void setWorld(World aWorld) {
		this.world = aWorld;
	}

	public Object getAnnotation(ResolvedType annotationType, Object onObject) {
		try {
			Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) Class.forName(annotationType.getName(),
					false, getClassLoader());
			if (onObject.getClass().isAnnotationPresent(annotationClass)) {
				return onObject.getClass().getAnnotation(annotationClass);
			}
		} catch (ClassNotFoundException ex) {
			// just return null
		}
		return null;
	}

	public Object getAnnotationFromClass(ResolvedType annotationType, Class aClass) {
		try {
			Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) Class.forName(annotationType.getName(),
					false, getClassLoader());
			if (aClass.isAnnotationPresent(annotationClass)) {
				return aClass.getAnnotation(annotationClass);
			}
		} catch (ClassNotFoundException ex) {
			// just return null
		}
		return null;
	}

	public Object getAnnotationFromMember(ResolvedType annotationType, Member aMember) {
		if (!(aMember instanceof AccessibleObject))
			return null;
		AccessibleObject ao = (AccessibleObject) aMember;
		try {
			Class annotationClass = Class.forName(annotationType.getName(), false, getClassLoader());
			if (ao.isAnnotationPresent(annotationClass)) {
				return ao.getAnnotation(annotationClass);
			}
		} catch (ClassNotFoundException ex) {
			// just return null
		}
		return null;
	}

	private ClassLoader getClassLoader() {
		return useSingleInstances ? staticClassLoaderRef.getClassLoader() : classLoaderRef.getClassLoader();
	}

	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType, Member onMember) {
		if (!(onMember instanceof AccessibleObject))
			return null;
		// here we really want both the runtime visible AND the class visible
		// annotations
		// so we bail out to Bcel and then chuck away the JavaClass so that we
		// don't hog
		// memory.
		try {
			JavaClass jc = bcelRepository.loadClass(onMember.getDeclaringClass());
			org.aspectj.apache.bcel.classfile.annotation.AnnotationGen[] anns = AnnotationGen.NO_ANNOTATIONS;
			if (onMember instanceof Method) {
				org.aspectj.apache.bcel.classfile.Method bcelMethod = jc.getMethod((Method) onMember);
				if (bcelMethod == null) {
					// pr220430
					// System.err.println(
					// "Unexpected problem in Java15AnnotationFinder: cannot retrieve annotations on method '"
					// +
					// onMember.getName()+"' in class '"+jc.getClassName()+"'");
				} else {
					anns = bcelMethod.getAnnotations();
				}
			} else if (onMember instanceof Constructor) {
				org.aspectj.apache.bcel.classfile.Method bcelCons = jc.getMethod((Constructor) onMember);
				anns = bcelCons.getAnnotations();
			} else if (onMember instanceof Field) {
				org.aspectj.apache.bcel.classfile.Field bcelField = jc.getField((Field) onMember);
				anns = bcelField.getAnnotations();
			}
			// the answer is cached and we don't want to hold on to memory
			getBcelRepository().clear();
			// OPTIMIZE make constant 0 size array for sharing
			if (anns == null)
				anns = AnnotationGen.NO_ANNOTATIONS;
			// convert to our Annotation type
			for (org.aspectj.apache.bcel.classfile.annotation.AnnotationGen ann : anns) {
				if (ann.getTypeSignature().equals(ofType.getSignature())) {
					return new BcelAnnotation(ann, world);
				}
			}
			return null;
		} catch (ClassNotFoundException cnfEx) {
			// just use reflection then
		}
		return null;
	}

	public String getAnnotationDefaultValue(Member onMember) {
		try {
			JavaClass jc = getBcelRepository().loadClass(onMember.getDeclaringClass());
			if (onMember instanceof Method) {
				org.aspectj.apache.bcel.classfile.Method bcelMethod = jc.getMethod((Method) onMember);

				if (bcelMethod == null) {
					// pr220430
					// System.err.println(
					// "Unexpected problem in Java15AnnotationFinder: cannot retrieve annotations on method '"
					// +
					// onMember.getName()+"' in class '"+jc.getClassName()+"'");
				} else {
					Attribute[] attrs = bcelMethod.getAttributes();
					for (Attribute attribute : attrs) {
						if (attribute.getName().equals("AnnotationDefault")) {
							AnnotationDefault def = (AnnotationDefault) attribute;
							return def.getElementValue().stringifyValue();
						}
					}
					return null;
				}
			}
		} catch (ClassNotFoundException cnfEx) {
			// just use reflection then
		}
		return null;
	}

	public ResolvedType[] getAnnotations(Member onMember, boolean areRuntimeAnnotationsSufficient) {
		if (!(onMember instanceof AccessibleObject)) {
			return ResolvedType.NONE;
		}
		// If annotations with class level retention are required then we need to open
		// open the class file. If only runtime retention annotations are required
		// we can just use reflection.
		if (!areRuntimeAnnotationsSufficient) {
			try {
				JavaClass jc = getBcelRepository().loadClass(onMember.getDeclaringClass());
				org.aspectj.apache.bcel.classfile.annotation.AnnotationGen[] anns = null;
				if (onMember instanceof Method) {
					org.aspectj.apache.bcel.classfile.Method bcelMethod = jc.getMethod((Method) onMember);
					if (bcelMethod != null) {
						anns = bcelMethod.getAnnotations();
					}
				} else if (onMember instanceof Constructor) {
					org.aspectj.apache.bcel.classfile.Method bcelCons = jc.getMethod((Constructor) onMember);
					anns = bcelCons.getAnnotations();
				} else if (onMember instanceof Field) {
					org.aspectj.apache.bcel.classfile.Field bcelField = jc.getField((Field) onMember);
					anns = bcelField.getAnnotations();
				}
				// the answer is cached and we don't want to hold on to memory
				getBcelRepository().clear();
				if (anns == null || anns.length == 0) {
					return ResolvedType.NONE;
				}
				ResolvedType[] annotationTypes = new ResolvedType[anns.length];
				for (int i = 0; i < anns.length; i++) {
					annotationTypes[i] = world.resolve(UnresolvedType.forSignature(anns[i].getTypeSignature()));
				}
				return annotationTypes;
			} catch (ClassNotFoundException cnfEx) {
				// just use reflection then
			}
		}

		AccessibleObject ao = (AccessibleObject) onMember;
		Annotation[] anns = ao.getDeclaredAnnotations();
		if (anns.length == 0) {
			return ResolvedType.NONE;
		}
		ResolvedType[] annotationTypes = new ResolvedType[anns.length];
		for (int i = 0; i < anns.length; i++) {
			annotationTypes[i] = UnresolvedType.forName(anns[i].annotationType().getName()).resolve(world);
		}
		return annotationTypes;
	}

	public ResolvedType[] getAnnotations(Class forClass, World inWorld) {
		// here we really want both the runtime visible AND the class visible
		// annotations so we bail out to Bcel and then chuck away the JavaClass so that we
		// don't hog memory.
		try {
			JavaClass jc = getBcelRepository().loadClass(forClass);
			org.aspectj.apache.bcel.classfile.annotation.AnnotationGen[] anns = jc.getAnnotations();
			getBcelRepository().clear();
			if (anns == null) {
				return ResolvedType.NONE;
			} else {
				ResolvedType[] ret = new ResolvedType[anns.length];
				for (int i = 0; i < ret.length; i++) {
					ret[i] = inWorld.resolve(UnresolvedType.forSignature(anns[i].getTypeSignature()));
				}
				return ret;
			}
		} catch (ClassNotFoundException cnfEx) {
			// just use reflection then
		}

		Annotation[] classAnnotations = forClass.getAnnotations();
		ResolvedType[] ret = new ResolvedType[classAnnotations.length];
		for (int i = 0; i < classAnnotations.length; i++) {
			ret[i] = inWorld.resolve(classAnnotations[i].annotationType().getName());
		}

		return ret;
	}

	public String[] getParameterNames(Member forMember) {
		if (!(forMember instanceof AccessibleObject))
			return null;

		try {
			JavaClass jc = getBcelRepository().loadClass(forMember.getDeclaringClass());
			LocalVariableTable lvt = null;
			int numVars = 0;
			if (forMember instanceof Method) {
				org.aspectj.apache.bcel.classfile.Method bcelMethod = jc.getMethod((Method) forMember);
				lvt = bcelMethod.getLocalVariableTable();
				numVars = bcelMethod.getArgumentTypes().length;
			} else if (forMember instanceof Constructor) {
				org.aspectj.apache.bcel.classfile.Method bcelCons = jc.getMethod((Constructor) forMember);
				lvt = bcelCons.getLocalVariableTable();
				numVars = bcelCons.getArgumentTypes().length;
			}
			return getParameterNamesFromLVT(lvt, numVars);
		} catch (ClassNotFoundException cnfEx) {
			; // no luck
		}

		return null;
	}

	private String[] getParameterNamesFromLVT(LocalVariableTable lvt, int numVars) {
		if (lvt == null)
			return null;// pr222987 - prevent NPE
		LocalVariable[] vars = lvt.getLocalVariableTable();
		if (vars.length < numVars) {
			// basic error, we can't get the names...
			return null;
		}
		String[] ret = new String[numVars];
		for (int i = 0; i < numVars; i++) {
			ret[i] = vars[i + 1].getName();
		}
		return ret;
	}

	public ResolvedType[][] getParameterAnnotationTypes(Member onMember) {
		if (!(onMember instanceof AccessibleObject))
			return NO_PARAMETER_ANNOTATIONS;
		// here we really want both the runtime visible AND the class visible
		// annotations
		// so we bail out to Bcel and then chuck away the JavaClass so that we
		// don't hog
		// memory.
		try {
			JavaClass jc = getBcelRepository().loadClass(onMember.getDeclaringClass());
			org.aspectj.apache.bcel.classfile.annotation.AnnotationGen[][] anns = null;
			if (onMember instanceof Method) {
				org.aspectj.apache.bcel.classfile.Method bcelMethod = jc.getMethod((Method) onMember);
				if (bcelMethod == null) {
					// pr220430
					// System.err.println(
					// "Unexpected problem in Java15AnnotationFinder: cannot retrieve annotations on method '"
					// +
					// onMember.getName()+"' in class '"+jc.getClassName()+"'");
				} else {
					anns = bcelMethod.getParameterAnnotations();
				}
			} else if (onMember instanceof Constructor) {
				org.aspectj.apache.bcel.classfile.Method bcelCons = jc.getMethod((Constructor) onMember);
				anns = bcelCons.getParameterAnnotations();
			} else if (onMember instanceof Field) {
				// anns = null;
			}
			// the answer is cached and we don't want to hold on to memory
			getBcelRepository().clear();
			if (anns == null)
				return NO_PARAMETER_ANNOTATIONS;
			ResolvedType[][] result = new ResolvedType[anns.length][];
			// CACHING??
			for (int i = 0; i < anns.length; i++) {
				if (anns[i] != null) {
					result[i] = new ResolvedType[anns[i].length];
					for (int j = 0; j < anns[i].length; j++) {
						result[i][j] = world.resolve(UnresolvedType.forSignature(anns[i][j].getTypeSignature()));
					}
				}
			}
			return result;
		} catch (ClassNotFoundException cnfEx) {
			// just use reflection then
		}

		// reflection...
		AccessibleObject ao = (AccessibleObject) onMember;
		Annotation[][] anns = null;
		if (onMember instanceof Method) {
			anns = ((Method) ao).getParameterAnnotations();
		} else if (onMember instanceof Constructor) {
			anns = ((Constructor) ao).getParameterAnnotations();
		} else if (onMember instanceof Field) {
			// anns = null;
		}
		if (anns == null)
			return NO_PARAMETER_ANNOTATIONS;
		ResolvedType[][] result = new ResolvedType[anns.length][];
		// CACHING??
		for (int i = 0; i < anns.length; i++) {
			if (anns[i] != null) {
				result[i] = new ResolvedType[anns[i].length];
				for (int j = 0; j < anns[i].length; j++) {
					result[i][j] = UnresolvedType.forName(anns[i][j].annotationType().getName()).resolve(world);
				}
			}
		}
		return result;
	}

}
