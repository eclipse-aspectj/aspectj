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

    /**
     * The field in the aspect that hosts the mixin instance
     */
    private final ResolvedMember aspectFieldDelegate;

    /**
     * Type pattern this munger applies to
     */
    private final TypePattern typePattern;

    /**
     * Construct a new type munger for @AspectJ ITD
     *
     * @param signature
     * @param aspect
     * @param fieldName
     * @param typePattern
     */
    public MethodDelegateTypeMunger(ResolvedMember signature, ResolvedType aspect, String fieldName, TypePattern typePattern) {
        super(MethodDelegate, signature);
        this.typePattern = typePattern;

        ResolvedMember[] fields = aspect.getDeclaredFields();//note: will unpack attributes
        ResolvedMember field = null;
        for (int i = 0; i < fields.length; i++) {
            if (fieldName.equals(fields[i].getName())) {
                field = fields[i];
                break;
            }
        }
        if (field == null) {           
            throw new RuntimeException("Should not happen: aspect field not found for @DeclareParents delegate");
        } else {
            aspectFieldDelegate = field;
        }
    }

    private MethodDelegateTypeMunger(ResolvedMember signature, ResolvedMember fieldDelegate, TypePattern typePattern) {
        super(MethodDelegate, signature);
        this.aspectFieldDelegate = fieldDelegate;
        this.typePattern = typePattern;
    }

    public ResolvedMember getDelegate() {
        return aspectFieldDelegate;
    }

    public void write(DataOutputStream s) throws IOException {
        kind.write(s);
        signature.write(s);
        aspectFieldDelegate.write(s);
        typePattern.write(s);
    }



    public static ResolvedTypeMunger readMethod(VersionedDataInputStream s, ISourceContext context) throws IOException {
        ResolvedMemberImpl signature = ResolvedMemberImpl.readResolvedMember(s, context);
        ResolvedMemberImpl field = ResolvedMemberImpl.readResolvedMember(s, context);
        TypePattern tp = TypePattern.read(s, context);
        return new MethodDelegateTypeMunger(signature, field, tp);
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
}
