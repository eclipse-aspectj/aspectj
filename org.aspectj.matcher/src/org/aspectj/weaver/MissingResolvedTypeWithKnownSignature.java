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
package org.aspectj.weaver;

import java.util.Collections;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.context.CompilationAndWeavingContext;

/**
 * When we try to resolve a type in the world that we require to be present,
 * and then fail to find it, we return an instance of this class. This class 
 * defers the production of the "can't find type error" until the first time
 * that someone asks a question that can't be answered solely from the signature.
 * This enables the weaver to be more tolerant of missing types.
 *
 */
public class MissingResolvedTypeWithKnownSignature extends ResolvedType {

	private static ResolvedMember[] NO_MEMBERS = new ResolvedMember[0];
	private static ResolvedType[] NO_TYPES = new ResolvedType[0];
	private boolean issuedCantFindTypeError = false;
	private boolean issuedJoinPointWarning = false;
	private boolean issuedMissingInterfaceWarning = false;
	
	/**
	 * @param signature
	 * @param world
	 */
	public MissingResolvedTypeWithKnownSignature(String signature, World world) {
		super(signature, world);
	}

	public boolean isMissing() { return true; }
	
	/**
	 * @param signature
	 * @param signatureErasure
	 * @param world
	 */
	public MissingResolvedTypeWithKnownSignature(String signature,
			String signatureErasure, World world) {
		super(signature, signatureErasure, world);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#getDeclaredFields()
	 */
	public ResolvedMember[] getDeclaredFields() {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_FIELDS);
		return NO_MEMBERS;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#getDeclaredMethods()
	 */
	public ResolvedMember[] getDeclaredMethods() {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_METHODS);
		return NO_MEMBERS;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#getDeclaredInterfaces()
	 */
	public ResolvedType[] getDeclaredInterfaces() {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_INTERFACES);
		return NO_TYPES;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#getDeclaredPointcuts()
	 */
	public ResolvedMember[] getDeclaredPointcuts() {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_POINTCUTS);
		return NO_MEMBERS;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#getSuperclass()
	 */
	public ResolvedType getSuperclass() {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_SUPERCLASS);
		return ResolvedType.MISSING;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#getModifiers()
	 */
	public int getModifiers() {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_MODIFIERS);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#getSourceContext()
	 */
	public ISourceContext getSourceContext() {
		return new ISourceContext() {

			public ISourceLocation makeSourceLocation(IHasPosition position) {
				return null;
			}

			public ISourceLocation makeSourceLocation(int line, int offset) {
				return null;
			}

			public int getOffset() {
				return 0;
			}
			
			public void tidy() {}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#isAssignableFrom(org.aspectj.weaver.ResolvedType)
	 */
	public boolean isAssignableFrom(ResolvedType other) {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_ASSIGNABLE,other.getName());
		return false;
	}
	
	public boolean isAssignableFrom(ResolvedType other, boolean allowMissing) {
		if (allowMissing) return false;
		else return isAssignableFrom(other);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedType#isCoerceableFrom(org.aspectj.weaver.ResolvedType)
	 */
	public boolean isCoerceableFrom(ResolvedType other) {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_COERCEABLE,other.getName());
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.AnnotatedElement#hasAnnotation(org.aspectj.weaver.UnresolvedType)
	 */
	public boolean hasAnnotation(UnresolvedType ofType) {
		raiseCantFindType(WeaverMessages.CANT_FIND_TYPE_ANNOTATION);
		return false;
	}
	
	public List getInterTypeMungers() {
		return Collections.EMPTY_LIST;
	}
	
	public List getInterTypeMungersIncludingSupers() {
		return Collections.EMPTY_LIST;
	}
	
	public List getInterTypeParentMungers() {
		return Collections.EMPTY_LIST;
	}
	
	public List getInterTypeParentMungersIncludingSupers() {
		return Collections.EMPTY_LIST;
	}
	
	protected void collectInterTypeMungers(List collector) {
		return;
	}
	
	public void raiseWarningOnJoinPointSignature(String signature) {
		if (issuedJoinPointWarning) return;
		String message = WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_JOINPOINT,getName(),signature);
		message += "\n" + CompilationAndWeavingContext.getCurrentContext();
		world.getLint().cantFindTypeAffectingJoinPointMatch.signal(message,null);
//		MessageUtil.warn(world.getMessageHandler(),message); 
		issuedJoinPointWarning = true;		
	}
	
	public void raiseWarningOnMissingInterfaceWhilstFindingMethods() {
		if (issuedMissingInterfaceWarning) return;
		String message = WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_INTERFACE_METHODS,getName(),signature);
		message += "\n" + CompilationAndWeavingContext.getCurrentContext();
		world.getLint().cantFindTypeAffectingJoinPointMatch.signal(message,null);
		//MessageUtil.warn(world.getMessageHandler(),message); 
		issuedMissingInterfaceWarning = true;				
	}
	
	private void raiseCantFindType(String key) {
		if (issuedCantFindTypeError) return;
		String message = WeaverMessages.format(key,getName());
		message += "\n" + CompilationAndWeavingContext.getCurrentContext();
		world.getLint().cantFindType.signal(message,null);
//		MessageUtil.error(world.getMessageHandler(),message);
		issuedCantFindTypeError = true;
	}

	private void raiseCantFindType(String key,String insert) {
		if (issuedCantFindTypeError) return;
		String message = WeaverMessages.format(key,getName(),insert);
		message += "\n" + CompilationAndWeavingContext.getCurrentContext();
		world.getLint().cantFindType.signal(message,null);
//		MessageUtil.error(world.getMessageHandler(),message);
		issuedCantFindTypeError = true;
	}

}
