/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Ron Bodkin		Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.ltw;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.Dump.IVisitor;
import org.aspectj.weaver.ICrossReferenceHandler;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.loadtime.IWeavingContext;
import org.aspectj.weaver.reflect.AnnotationFinder;
import org.aspectj.weaver.reflect.IReflectionWorld;
import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegateFactory;
import org.aspectj.weaver.reflect.ReflectionWorld;

/**
 * @author adrian
 * @author Ron Bodkin
 *
 *         For use in LT weaving
 *
 *         Backed by both a BcelWorld and a ReflectionWorld
 *
 *         Needs a callback when a woven class is defined This is the trigger for us to ditch the class from Bcel and cache it in
 *         the reflective world instead.
 *
 *         Create by passing in a classloader, message handler
 */
public class LTWWorld extends BcelWorld implements IReflectionWorld {

	private AnnotationFinder annotationFinder;
	private IWeavingContext weavingContext;
	private String classLoaderString;

	private String classLoaderParentString;

	/**
	 * Build a World from a ClassLoader, for LTW support
	 */
	public LTWWorld(ClassLoader loader, IWeavingContext weavingContext, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		super(loader, handler, xrefHandler);
		this.weavingContext = weavingContext;
		try {
			classLoaderString = loader.toString();
		} catch (Throwable t) {
			// Possibly some state in the loader isn't initialized but is used in the toString()
			classLoaderString = loader.getClass().getName()+":"+Integer.toString(System.identityHashCode(loader));
		}
		classLoaderParentString = (loader.getParent() == null ? "<NullParent>" : loader.getParent().toString());
		setBehaveInJava5Way(true);
		annotationFinder = ReflectionWorld.makeAnnotationFinderIfAny(loader, this);
	}

	public ClassLoader getClassLoader() {
		return weavingContext.getClassLoader();
	}

	// TEST
	// this is probably easier: just mark anything loaded while loading aspects as not
	// expendible... it also fixes a possible bug whereby non-rewoven aspects are deemed expendible
	// <exclude within="org.foo.aspects..*"/>
	// protected boolean isExpendable(ResolvedType type) {
	// return ((type != null) && !loadingAspects && !type.isAspect() && (!type
	// .isPrimitiveType()));
	// }

	/**
	 * Helper method to resolve the delegate from the reflection delegate factory.
	 */
	private ReferenceTypeDelegate resolveReflectionTypeDelegate(ReferenceType ty, ClassLoader resolutionLoader) {
		ReferenceTypeDelegate res = ReflectionBasedReferenceTypeDelegateFactory.createDelegate(ty, this, resolutionLoader);
		return res;
	}

	/**
	 * Remove this class from the typeMap. Call back to be made from a publishing class loader The class loader should, ideally,
	 * make this call on each not yet working
	 *
	 * @param clazz
	 */
	public void loadedClass(Class clazz) {
	}

	private static final long serialVersionUID = 1;

	public AnnotationFinder getAnnotationFinder() {
		return this.annotationFinder;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.aspectj.weaver.reflect.IReflectionWorld#resolve(java.lang.Class)
	 */
	public ResolvedType resolve(Class aClass) {
		return ReflectionWorld.resolve(this, aClass);
	}

	@Override
	public boolean isRunMinimalMemory() {
		if (isRunMinimalMemorySet()) {
			return super.isRunMinimalMemory();
		}
		return false;
	}

	// One type is completed at a time, if multiple need doing then they
	// are queued up
	private boolean typeCompletionInProgress = false;
	private List<ResolvedType> typesForCompletion = new ArrayList<>();

	@Override
	protected void completeBinaryType(ResolvedType ret) {
		if (isLocallyDefined(ret.getName())) {
			if (typeCompletionInProgress) {
				typesForCompletion.add(ret);
			} else {
				try {
					typeCompletionInProgress = true;
					completeHierarchyForType(ret);
				} finally {
					typeCompletionInProgress = false;
				}
				while (typesForCompletion.size() != 0) {
					ResolvedType rt = typesForCompletion.get(0);
					completeHierarchyForType(rt);
					typesForCompletion.remove(0);
				}
			}
		} else {
			if (!ret.needsModifiableDelegate()) {
				ret = completeNonLocalType(ret);
			}
		}
	}

	private void completeHierarchyForType(ResolvedType ret) {
		getLint().typeNotExposedToWeaver.setSuppressed(true);
		weaveInterTypeDeclarations(ret);
		getLint().typeNotExposedToWeaver.setSuppressed(false);
	}

	protected boolean needsCompletion() {
		return true;
	}

	@Override
	public boolean isLocallyDefined(String classname) {
		return weavingContext.isLocallyDefined(classname);
	}

	protected ResolvedType completeNonLocalType(ResolvedType ret) {
		if (ret.isMissing()) {
			return ret; // who knows ?!?
		}
		ResolvedType toResolve = ret;
		if (ret.isParameterizedType() || ret.isGenericType()) {
			toResolve = toResolve.getGenericType();
		}
		ReferenceTypeDelegate rtd = resolveReflectionTypeDelegate((ReferenceType) toResolve, getClassLoader());
		((ReferenceType) ret).setDelegate(rtd);
		return ret;
	}

	@Override
	public void storeClass(JavaClass clazz) {
		ensureRepositorySetup();
		delegate.storeClass(clazz);
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visitObject("Class loader:");
		visitor.visitObject(classLoaderString);
		visitor.visitObject("Class loader parent:");
		visitor.visitObject(classLoaderParentString);
		super.accept(visitor);
	}

	public boolean isLoadtimeWeaving() {
		return true;
	}

}
