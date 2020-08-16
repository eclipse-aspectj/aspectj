/* *******************************************************************
 * Copyright (c) 2005-2017 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.IWeavingSupport;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeakClassLoaderReference;
import org.aspectj.weaver.World;

/**
 * A ReflectionWorld is used solely for purposes of type resolution based on the runtime classpath (java.lang.reflect). It does not
 * support weaving operations (creation of mungers etc..).
 * 
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class ReflectionWorld extends World implements IReflectionWorld {

	private static Map<WeakClassLoaderReference, ReflectionWorld> rworlds = Collections.synchronizedMap(new HashMap<>());

	private WeakClassLoaderReference classLoaderReference;
	private AnnotationFinder annotationFinder;
	private boolean mustUseOneFourDelegates = false; // for testing
	private Map<String,Class<?>> inProgressResolutionClasses = new HashMap<>();
	
	public static ReflectionWorld getReflectionWorldFor(WeakClassLoaderReference classLoaderReference) {
		
		// Temporarily do as before. Although the cache makes things faster it needs a bit more thought because
		// if the world has pointcutdesignators registered then someone may inadvertently register additional
		// ones on reusing a world (when they would be expecting a clean world). We can't automatically
		// clear them because we don't know when they are finished with.
		return new ReflectionWorld(classLoaderReference);
		
		/*
		synchronized (rworlds) {
			// Tidyup any no longer relevant entries...
			for (Iterator<Map.Entry<WeakClassLoaderReference, ReflectionWorld>> it = rworlds.entrySet().iterator();
					it.hasNext();) {
				Map.Entry<WeakClassLoaderReference, ReflectionWorld> entry = it.next();
				if (entry.getKey().getClassLoader() == null) {
					it.remove();
				}
			}
			ReflectionWorld rworld = null;
			if (classLoaderReference.getClassLoader() != null) {
				rworld = rworlds.get(classLoaderReference);
				if (rworld == null) {
					rworld = new ReflectionWorld(classLoaderReference);
					rworlds.put(classLoaderReference, rworld);
				}
			}
			return rworld;
		}
		*/
	}
	
	public static void cleanUpWorlds() {
		synchronized (rworlds) {
			rworlds.clear();
		}
	}
	
	private ReflectionWorld() {
		// super();
		// this.setMessageHandler(new ExceptionBasedMessageHandler());
		// setBehaveInJava5Way(LangUtil.is15VMOrGreater());
		// this.classLoaderReference = new
		// WeakClassLoaderReference(ReflectionWorld.class.getClassLoader());
		// this.annotationFinder =
		// makeAnnotationFinderIfAny(classLoaderReference.getClassLoader(),
		// this);
	}
	
	public ReflectionWorld(WeakClassLoaderReference classloaderRef) {
		this.setMessageHandler(new ExceptionBasedMessageHandler());
		setBehaveInJava5Way(true);
		classLoaderReference = classloaderRef;
		annotationFinder = makeAnnotationFinderIfAny(classLoaderReference.getClassLoader(), this);
	}

	public ReflectionWorld(ClassLoader aClassLoader) {
		super();
		this.setMessageHandler(new ExceptionBasedMessageHandler());
		setBehaveInJava5Way(true);
		classLoaderReference = new WeakClassLoaderReference(aClassLoader);
		annotationFinder = makeAnnotationFinderIfAny(classLoaderReference.getClassLoader(), this);
	}

	public ReflectionWorld(boolean forceUseOf14Delegates, ClassLoader aClassLoader) {
		this(aClassLoader);
		this.mustUseOneFourDelegates = forceUseOf14Delegates;
		if (forceUseOf14Delegates) {
			// Dont use 1.4 delegates and yet allow autoboxing
			this.setBehaveInJava5Way(false);
		}
	}

	public static AnnotationFinder makeAnnotationFinderIfAny(ClassLoader loader, World world) {
		AnnotationFinder annotationFinder = null;
		try {
			Class<?> java15AnnotationFinder = Class.forName("org.aspectj.weaver.reflect.Java15AnnotationFinder");
			annotationFinder = (AnnotationFinder) java15AnnotationFinder.newInstance();
			annotationFinder.setClassLoader(loader);
			annotationFinder.setWorld(world);
		} catch (ClassNotFoundException ex) {
			// must be on 1.4 or earlier
		} catch (IllegalAccessException ex) {
			// not so good
			throw new BCException("AspectJ internal error", ex);
		} catch (InstantiationException ex) {
			throw new BCException("AspectJ internal error", ex);
		}
		return annotationFinder;
	}

	public ClassLoader getClassLoader() {
		return classLoaderReference.getClassLoader();
	}

	public AnnotationFinder getAnnotationFinder() {
		return annotationFinder;
	}

	public ResolvedType resolve(Class aClass) {
		return resolve(this, aClass);
	}

	public static ResolvedType resolve(World world, Class<?> aClass) {
		// classes that represent arrays return a class name that is the
		// signature of the array type, ho-hum...
		String className = aClass.getName();
		if (aClass.isArray()) {
			return world.resolve(UnresolvedType.forSignature(className.replace('.', '/')));
		} else {
			return world.resolve(className);
		}
	}
	
	/**
	 * Resolve a type using the specified class. Normal resolution in a reflection
	 * world uses Class.forName() via the classloader (attached to this world)
	 * in order to find a named type then builds a reference type and a reference
	 * type delegate based on that. For some classes generated at runtime (e.g.
	 * proxy or lambda representation) the forName() call will not work. In those
	 * situations we should just use the clazz we have.
	 * 
	 * Should the whole thing switch from using forName() to using the clazz objects?
	 * Possibly but that introduces a lot of change and we don't have a lot
	 * of test coverage for this scenario (reflection world). What we are doing
	 * right now is that this can optionally be used if the regular resolution
	 * scheme did not work.
	 * 
	 * Although AspectJ is *not* multi threaded or re-entrant, Spring doesn't
	 * always respect that. There might be an issue here if two attempts are
	 * made to resolve the same thing at the same time via this method.
	 * 
	 * @param clazz the class to use as the delegate for the resolved type
	 */
	public ResolvedType resolveUsingClass(Class<?> clazz) {
		String signature = UnresolvedType.forName(clazz.getName()).getSignature();
		try {
			inProgressResolutionClasses.put(signature, clazz);
			return resolve(clazz.getName());
		} finally {
			inProgressResolutionClasses.remove(signature);
		}
	}

	protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
		ReferenceTypeDelegate result;
		if (mustUseOneFourDelegates) {
			result = ReflectionBasedReferenceTypeDelegateFactory.create14Delegate(ty, this, classLoaderReference.getClassLoader());
		} else {
			result = ReflectionBasedReferenceTypeDelegateFactory.createDelegate(ty, this, classLoaderReference.getClassLoader());
		}
		if (result == null && inProgressResolutionClasses.size() != 0) {
			// Is it a class that cannot be loaded (i.e. it was generated) but we already know about?
			Class<?> clazz = inProgressResolutionClasses.get(ty.getSignature());
			if (clazz != null) {
				result = ReflectionBasedReferenceTypeDelegateFactory.createDelegate(ty,this,clazz);
			}
		}
		return result;
	}

	public static class ReflectionWorldException extends RuntimeException {

		private static final long serialVersionUID = -3432261918302793005L;

		public ReflectionWorldException(String message) {
			super(message);
		}
	}

	private static class ExceptionBasedMessageHandler implements IMessageHandler {

		public boolean handleMessage(IMessage message) throws AbortException {
			throw new ReflectionWorldException(message.toString());
		}

		public boolean isIgnoring(org.aspectj.bridge.IMessage.Kind kind) {
			if (kind == IMessage.INFO) {
				return true;
			} else {
				return false;
			}
		}

		public void dontIgnore(org.aspectj.bridge.IMessage.Kind kind) {
			// empty
		}

		public void ignore(org.aspectj.bridge.IMessage.Kind kind) {
			// empty
		}

	}

	public IWeavingSupport getWeavingSupport() {
		return null;
	}

	public boolean isLoadtimeWeaving() {
		return true;
	}

}
