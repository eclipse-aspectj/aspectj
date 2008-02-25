/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *               2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     AMC      extracted as interface 
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.aspectj.util.TypeSafeEnum;

public interface Member {

    public static class Kind extends TypeSafeEnum {
        public Kind(String name, int key) { super(name, key); }
        
        public static Kind read(DataInputStream s) throws IOException {
            int key = s.readByte();
            switch(key) {
                case 1: return METHOD;
                case 2: return FIELD;
                case 3: return CONSTRUCTOR;
                case 4: return STATIC_INITIALIZATION;
                case 5: return POINTCUT;
                case 6: return ADVICE;
                case 7: return HANDLER;
                case 8: return MONITORENTER;
                case 9: return MONITOREXIT;
            }
            throw new BCException("weird kind " + key);
        }
    }

	public static final Member[] NONE = new Member[0];
	public static final Kind METHOD = new Kind("METHOD", 1);
	public static final Kind FIELD = new Kind("FIELD", 2);
	public static final Kind CONSTRUCTOR = new Kind("CONSTRUCTOR", 3);
	public static final Kind STATIC_INITIALIZATION = new Kind("STATIC_INITIALIZATION", 4);
	public static final Kind POINTCUT = new Kind("POINTCUT", 5);
	public static final Kind ADVICE = new Kind("ADVICE", 6);
	public static final Kind HANDLER = new Kind("HANDLER", 7);
	public static final Kind MONITORENTER = new Kind("MONITORENTER", 8);
	public static final Kind MONITOREXIT = new Kind("MONITOREXIT", 9);

	public static final AnnotationX[][] NO_PARAMETER_ANNOTATIONXS = new AnnotationX[][]{};
	public static final ResolvedType[][] NO_PARAMETER_ANNOTATION_TYPES = new ResolvedType[][]{};
	
	public ResolvedMember resolve(World world);

	public int compareTo(Object other);

	public String toLongString();

	public Kind getKind();

	public UnresolvedType getDeclaringType();

	public UnresolvedType getReturnType();
	
	public UnresolvedType getGenericReturnType();
	public UnresolvedType[] getGenericParameterTypes();

	public UnresolvedType getType();

	public String getName();

	public UnresolvedType[] getParameterTypes();

	public AnnotationX[][] getParameterAnnotations();
	public ResolvedType[][] getParameterAnnotationTypes();
	
	public String getAnnotationDefaultValue();
	
	/**
	 * Return full signature, including return type, e.g. "()LFastCar;" for a signature without the return type,
	 * use getParameterSignature() - it is importnant to choose the right one in the face of covariance.
	 */
	public String getSignature();
	
    /**
     * All the signatures that a join point with this member as its signature has.
     */
    public Iterator getJoinPointSignatures(World world);

	public int getArity();

	/**
	 * Return signature without return type, e.g. "()" for a signature *with* the return type,
	 * use getSignature() - it is important to choose the right one in the face of covariance.
	 */
	public String getParameterSignature();

	public boolean isCompatibleWith(Member am);

	public int getModifiers(World world);
	
	public int getModifiers();

	public UnresolvedType[] getExceptions(World world);

	public boolean isProtected(World world);

	public boolean isStatic(World world);

	public boolean isStrict(World world);

	public boolean isStatic();

	public boolean isInterface();

	public boolean isPrivate();

	/**
	 * Returns true iff the member is generic (NOT parameterized)
	 * For example, a method declared in a generic type
	 */
	public boolean canBeParameterized();

	public int getCallsiteModifiers();

	public String getExtractableName();

	/**
	 * If you want a sensible answer, resolve the member and call
	 * hasAnnotation() on the ResolvedMember.
	 */
	public boolean hasAnnotation(UnresolvedType ofType);

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.AnnotatedElement#getAnnotationTypes()
	 */
	public ResolvedType[] getAnnotationTypes();

	public AnnotationX[] getAnnotations();

	public Collection/*ResolvedType*/getDeclaringTypes(World world);

	// ---- reflective thisJoinPoint stuff
	public String getSignatureMakerName();

	public String getSignatureType();

	public String getSignatureString(World world);

	public String[] getParameterNames(World world);

}