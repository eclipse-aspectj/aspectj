/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.annotationStyle;

import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedTypeX;

import java.lang.reflect.Modifier;

/**
 * Addition to AjcMemberMaker for @Aj aspect
 * Should end up there
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Ajc5MemberMaker {

    public final static TypeX ASPECT = TypeX.forName("org.aspectj.lang.annotation.Aspect");

    /**
     * Returns true if the given aspect is an @AJ aspect
     *
     * @param aspectType
     * @return
     */
    public static boolean isAnnotationStyleAspect(ResolvedTypeX aspectType) {
        if (aspectType != null) {
            if (aspectType.isAspect()) {
                return aspectType.isAnnotationStyleAspect();
            }
        }
        return false;
    }

    //temp proto code for aspectOf without pre-processing
    public static ResolvedMember perSingletonAspectOfMethod(TypeX declaringType) {
        return new ResolvedMember(
                Member.METHOD,
                TypeX.forName("org.aspectj.lang.Aspects"),
                Modifier.PUBLIC | Modifier.STATIC,
                "aspectOf$singleton",
                "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"
        );
    }

}
