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

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.patterns.TypePattern;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;

public class MethodDelegateTypeMunger extends ResolvedTypeMunger {

    private final ResolvedMember aspectFieldDelegate;

    private final TypePattern typePattern;

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

    public ResolvedMember getDelegate() {
        return aspectFieldDelegate;
    }

//    public ResolvedMember getInterMethodBody(UnresolvedType aspectType) {
//        return AjcMemberMaker.interMethodBody(signature, aspectType);
//    }
//
//    public ResolvedMember getInterMethodDispatcher(UnresolvedType aspectType) {
//        return AjcMemberMaker.interMethodDispatcher(signature, aspectType);
//    }

    public void write(DataOutputStream s) throws IOException {
        ;//FIXME AVITD needed as changes public signature throw new RuntimeException("unimplemented");
    }

//    public static ResolvedTypeMunger readMethod(VersionedDataInputStream s, ISourceContext context) throws IOException {
//        ResolvedMemberImpl rmi = ResolvedMemberImpl.readResolvedMember(s, context);
//        Set superMethodsCalled = readSuperMethodsCalled(s);
//        ISourceLocation sLoc = readSourceLocation(s);
//        ResolvedTypeMunger munger = new MethodDelegateTypeMunger(rmi, superMethodsCalled);
//        if (sLoc != null) munger.setSourceLocation(sLoc);
//        return munger;
//    }
//
//    public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedType aspectType) {
//        ResolvedMember ret = AjcMemberMaker.interMethodDispatcher(getSignature(), aspectType);
//        if (ResolvedType.matches(ret, member)) return getSignature();
//        return super.getMatchingSyntheticMember(member, aspectType);
//    }

    public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
        // match only on class
        if (matchType.isEnum() || matchType.isInterface() || matchType.isAnnotation()) {
            return false;
        }

        return typePattern.matchesStatically(matchType);
    }

    public boolean changesPublicSignature() {
        return true;
    }
}
