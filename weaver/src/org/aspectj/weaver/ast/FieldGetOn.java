/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.weaver.ast;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.UnresolvedType;

/**
 * Represents a field access on a given type.
 * <p/>
 * Used when aspectOf is not existing in the aspect class (no pre-processing of aspects)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class FieldGetOn extends FieldGet {

    private UnresolvedType m_declaringType;

    public FieldGetOn(Member field, UnresolvedType declaringType) {
        super(field, null);
        m_declaringType = declaringType;
    }

    public UnresolvedType getDeclaringType() {
        return m_declaringType;
    }

    public void accept(IExprVisitor v) {
        v.visit(this);
    }

}
