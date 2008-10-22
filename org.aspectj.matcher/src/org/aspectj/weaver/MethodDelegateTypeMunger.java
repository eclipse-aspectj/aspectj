/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *     Alexandre Vasseur     initial implementation
 * ******************************************************************/


package org.aspectj.weaver;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.weaver.patterns.TypePattern;

/**
 * Type munger for @AspectJ ITD declare parents ie with an interface AND an implementation.
 * Given the aspect that has a field public static Interface fieldI = ... // impl.
 * we will weave in the Interface' methods and delegate to the aspect public static field fieldI
 *
 * Note: this munger DOES NOT handles the interface addition to the target classes - a regular Parent kinded munger
 * must be added in coordination.
 */
public class MethodDelegateTypeMunger extends ResolvedTypeMunger {

    private final UnresolvedType aspect;

    /**
     * The mixin impl (no arg ctor)
     */
    private final String implClassName;

    /**
     * Type pattern this munger applies to
     */
    private final TypePattern typePattern;

    /**
     * Construct a new type munger for @AspectJ ITD
     *
     * @param signature
     * @param aspect
     * @param implClassName
     * @param typePattern
     */
    public MethodDelegateTypeMunger(ResolvedMember signature, UnresolvedType aspect, String implClassName, TypePattern typePattern) {
        super(MethodDelegate, signature);
        this.aspect = aspect;
        this.typePattern = typePattern;
        this.implClassName = implClassName;
    }

    public boolean equals(Object other) {
    	if (!(other instanceof MethodDelegateTypeMunger)) return false;
    	MethodDelegateTypeMunger o = (MethodDelegateTypeMunger)other;
    	return ((o.aspect == null) ? (aspect == null ) : aspect.equals(o.aspect))
    			&& ((o.typePattern == null) ? (typePattern == null ) : typePattern.equals(o.typePattern))
    			&& ((o.implClassName == null) ? (implClassName == null) : implClassName.equals(o.implClassName));
    }

    private volatile int hashCode = 0;
    public int hashCode() {
    	if (hashCode == 0) {
    	 	int result = 17;
    	    result = 37*result + ((aspect == null) ? 0 : aspect.hashCode());
    	    result = 37*result + ((typePattern == null) ? 0 : typePattern.hashCode());
    	    result = 37*result + ((implClassName == null) ? 0 : implClassName.hashCode());
    	    hashCode = result;
		}
	    return hashCode;
    }
    
    public ResolvedMember getDelegate(ResolvedType targetType) {
        return AjcMemberMaker.itdAtDeclareParentsField(
                targetType,
                signature.getDeclaringType(),
                aspect
        );
    }

    public String getImplClassName() {
        return implClassName;
    }

    public void write(DataOutputStream s) throws IOException {
        kind.write(s);
        signature.write(s);
        aspect.write(s);
        s.writeUTF(implClassName);
        typePattern.write(s);
    }

    public static ResolvedTypeMunger readMethod(VersionedDataInputStream s, ISourceContext context) throws IOException {
        ResolvedMemberImpl signature = ResolvedMemberImpl.readResolvedMember(s, context);
        UnresolvedType aspect = UnresolvedType.read(s);
        String implClassName = s.readUTF();
        TypePattern tp = TypePattern.read(s, context);
        return new MethodDelegateTypeMunger(signature, aspect, implClassName, tp);
    }

    /**
     * Match based on given type pattern, only classes can be matched
     *
     * @param matchType
     * @param aspectType
     * @return true if match
     */
    public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
        // match only on class
        if (matchType.isEnum() || matchType.isInterface() || matchType.isAnnotation()) {
            return false;
        }

        return typePattern.matchesStatically(matchType);
    }

    /**
     * Needed for reweavable
     *
     * @return true
     */
    public boolean changesPublicSignature() {
        return true;
    }

    public static class FieldHostTypeMunger extends ResolvedTypeMunger {

        private UnresolvedType aspect;

        /**
         * Type pattern this munger applies to
         */
        private final TypePattern typePattern;

        /**
         * Construct a new type munger for @AspectJ ITD
         *
         * @param field
         * @param aspect
         * @param typePattern
         */
        public FieldHostTypeMunger(ResolvedMember field, UnresolvedType aspect, TypePattern typePattern) {
            super(FieldHost, field);
            this.aspect = aspect;
            this.typePattern = typePattern;
        }

        public boolean equals(Object other) {
        	if (!(other instanceof FieldHostTypeMunger)) return false;
        	FieldHostTypeMunger o = (FieldHostTypeMunger)other;
        	return ((o.aspect == null) ? (aspect == null ) : aspect.equals(o.aspect))
        			&& ((o.typePattern == null) ? (typePattern == null ) : typePattern.equals(o.typePattern));
        }

        public int hashCode() {
    	 	int result = 17;
    	    result = 37*result + ((aspect == null) ? 0 : aspect.hashCode());
    	    result = 37*result + ((typePattern == null) ? 0 : typePattern.hashCode());
    	    return result;
        }
        
        public void write(DataOutputStream s) throws IOException {
            kind.write(s);
            signature.write(s);
            aspect.write(s);
            typePattern.write(s);
        }

        public static ResolvedTypeMunger readFieldHost(VersionedDataInputStream s, ISourceContext context) throws IOException {
            ResolvedMemberImpl signature = ResolvedMemberImpl.readResolvedMember(s, context);
            UnresolvedType aspect = UnresolvedType.read(s);
            TypePattern tp = TypePattern.read(s, context);
            return new FieldHostTypeMunger(signature, aspect, tp);
        }

        /**
         * Match based on given type pattern, only classes can be matched
         *
         * @param matchType
         * @param aspectType
         * @return true if match
         */
        public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
            // match only on class
            if (matchType.isEnum() || matchType.isInterface() || matchType.isAnnotation()) {
                return false;
            }

            return typePattern.matchesStatically(matchType);
        }

        public boolean changesPublicSignature() {
            return false;
        }

    }
}
