/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.ast;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.TypeX;

/**
 * Note: Used when aspectOf is not existing in the aspect class (no pre-processing of aspects)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class FieldGetOn extends FieldGet {
    public TypeX getDeclaringType() {
        return m_declaringType;
    }

    private TypeX m_declaringType;

    public FieldGetOn(Member field, TypeX declaringType) {
        super(field, null);
        m_declaringType = declaringType;
    }

    public void accept(IExprVisitor v) {
        v.visit(this);
    }

}
