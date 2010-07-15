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

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.util.LangUtil;
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
 */
public class ReflectionWorld extends World implements IReflectionWorld {

	private WeakClassLoaderReference classLoaderReference;
	private AnnotationFinder annotationFinder;
	private boolean mustUseOneFourDelegates = false; // for testing

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

	public ReflectionWorld(ClassLoader aClassLoader) {
		super();
		this.setMessageHandler(new ExceptionBasedMessageHandler());
		setBehaveInJava5Way(LangUtil.is15VMOrGreater());
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
			if (LangUtil.is15VMOrGreater()) {
				Class java15AnnotationFinder = Class.forName("org.aspectj.weaver.reflect.Java15AnnotationFinder");
				annotationFinder = (AnnotationFinder) java15AnnotationFinder.newInstance();
				annotationFinder.setClassLoader(loader);
				annotationFinder.setWorld(world);
			}
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

	public static ResolvedType resolve(World world, Class aClass) {
		// classes that represent arrays return a class name that is the
		// signature of the array type, ho-hum...
		String className = aClass.getName();
		if (aClass.isArray()) {
			return world.resolve(UnresolvedType.forSignature(className.replace('.', '/')));
		} else {
			return world.resolve(className);
		}
	}

	protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
		if (mustUseOneFourDelegates) {
			return ReflectionBasedReferenceTypeDelegateFactory.create14Delegate(ty, this, classLoaderReference.getClassLoader());
		} else {
			return ReflectionBasedReferenceTypeDelegateFactory.createDelegate(ty, this, classLoaderReference.getClassLoader());
		}
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
